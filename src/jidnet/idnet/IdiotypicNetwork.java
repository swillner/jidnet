package jidnet.idnet;

import java.util.Random;

/**
 * Idiotypic network with very simple statistic and analysis, for more analysis extend
 *
 * @author Sven Willner
 */
public class IdiotypicNetwork {

    /** Sum of total occupation of network */
    protected int total_sum_n;
    /** Center of gravity vector */
    protected double[] cog;
    /** Highest possible occupation */
    protected int N;
    /** Length of bitstrings */
    protected int d;
    /** Influx propability */
    protected double p;
    /** Lower range for update window rule */
    protected int t_l;
    /** Upper range for update window rule */
    protected int t_u;
    /** Idiotypes of current, next (ng) and last (lg) timestep */
    protected Idiotype[] idiotypes, idiotypes_ng, idiotypes_lg;
    /** Time (generation) */
    protected int t;
    /** Weighting of links, i-th component is link with i mismatches */
    protected double[] linkWeighting;
    /** Do neighbour occupation statistics? (has to be recalculated after update) */
    protected boolean statNeighbourOccupations = false;
    /** Calculate center of gravity */
    protected boolean statCenterOfGravity = false;
    /** Array of idiotpye indices to be kept occupied as external influx */
    protected int[] externalInflux;
    /** Random number generator */
    protected Random rng;

    /**
     * Does the influx on the network
     */
    protected final void influx() {
        for (int i = 0; i < (1 << d); i++) {
            idiotypes_lg[i].n = idiotypes[i].n;
            if (idiotypes[i].n < N && rng.nextDouble() < p) {
                if (idiotypes[i].n == 0)
                    idiotypes[i].b++;
                idiotypes[i].n++;
            }
        }
    }

    /**
     * Does external influx to keep certain idiotypes occupied
     */
    protected final void externalInflux() {
        if (externalInflux != null)
            for (int i = 0; i < externalInflux.length; i++)
                if (idiotypes[externalInflux[i]].n < N)
                    idiotypes[externalInflux[i]].n++;
    }

    /**
     * Calculates the neighbour occupation (weighted) of j with
     * mismatches on <code>mismatchMask</code> >> 1 to <code>mismatchMask</code> == 1
     * (distance to original node is <code>dist</code>)
     *
     * @param j
     * @param mismatchMask
     * @param dist
     * @return
     */
    private double calcWeightedNeighboutOccSumRec(int j, int mismatchMask, int dist) {
        double res = 0;
        while (mismatchMask != 0) {
            mismatchMask >>= 1;
            res += idiotypes[j ^ mismatchMask].n * linkWeighting[dist];
            if (linkWeighting[dist + 1] > 0) // Stop, when link weighting of larger distance == 0
                res += calcWeightedNeighboutOccSumRec(j ^ mismatchMask, mismatchMask, dist + 1);
        }
        return res;
    }

    /**
     * Calculates the neighbour occupation (weighted) of <code>i</code>'s neighbours
     *
     * @param i Index of idiotype
     * @return
     */
    protected final double calcWeightedNeighbourOccSum(int i) {
        int complement = ~i & ((1 << d) - 1);
        return idiotypes[complement].n * linkWeighting[0] + calcWeightedNeighboutOccSumRec(complement, 1 << d, 1);
    }

    /**
     * Does the update on the network
     */
    protected final void update() {
        total_sum_n = 0;

        // Fills idiotypes_ng to not overwrite current network idiotypes

        for (int i = 0; i < (1 << d); i++) {

            double sum_n_d = calcWeightedNeighbourOccSum(i);

            // Do window rule (weighted by node's occupation)
            if (idiotypes[i].n > 0)
                if ((sum_n_d / idiotypes[i].n >= t_l) && (sum_n_d / idiotypes[i].n <= t_u))
                    idiotypes_ng[i].n = idiotypes[i].n;
                else
                    idiotypes_ng[i].n = idiotypes[i].n - 1;
            else {
                idiotypes_ng[i].n = 0;
                idiotypes_ng[i].cluster = null;
            }

            idiotypes_ng[i].b = idiotypes[i].b;

            if (!statNeighbourOccupations) {
                // If no extra-statistics about neighbour occupations is needed, set simple statistics (has errors in it!)
                idiotypes_ng[i].n_d = sum_n_d;
                idiotypes_ng[i].sum_n_d = idiotypes[i].sum_n_d + sum_n_d;
            }

            if (idiotypes_ng[i].n > 0) {
                idiotypes_ng[i].sum_n = idiotypes[i].sum_n + idiotypes_ng[i].n;
                idiotypes_ng[i].tau = idiotypes[i].tau + 1;
                total_sum_n += idiotypes_ng[i].n;

                if (statCenterOfGravity)
                    for (int j = 0; j < d; j++)
                        if ((i & (1 << j)) != 0)
                            cog[j] += idiotypes_ng[i].n;
                        else
                            cog[j] -= idiotypes_ng[i].n;

            } else
                idiotypes_ng[i].tau = 0;

        }
    }

    /**
     * Does one iteration step
     *
     * Before call: time is <code>t</code>
     *
     * After call: time is <code>t+1</code>
     *      <code>idiotypes_lg</code> = last generation (<code>t</code>)
     *      <code>idiotypes</code>    = current generation (<code>t+1</code>)
     *      <code>idiotypes_ng</code> = prelast generation (<code>t-1</code>)
     */
    public void iterate() {
        if (statCenterOfGravity)
            for (int i = 0; i < d; i++)
                cog[i] = 0;

        influx();
        /**
         * At this point:
         *      <code>idiotypes_lg</code> = last generation (<code>t</code>)
         *      <code>idiotypes</code>    = last generation (<code>t</code>) + influx
         *      <code>idiotypes_ng</code> = prelast generation (<code>t-1</code>)
         */
        if (externalInflux != null)
            externalInflux();
        update();

        // Norm center of gravity
        if (statCenterOfGravity)
            for (int i = 0; i < d; i++)
                cog[i] /= total_sum_n;

        // Permutate generations of idiotypes:
        // <code>idiotypes</code> becomes <code>idiotypes_ng</code> becomes <code>idiotypes_lg</code> becomes <code>idiotypes</code>
        Idiotype[] tmp = idiotypes_lg;
        idiotypes_lg = idiotypes;
        idiotypes = idiotypes_ng;
        idiotypes_ng = tmp;

        // if extra statistics of neighbour occupation is needed, calculate new neighbour occupations (weighted)
        if (statNeighbourOccupations)
            for (int i = 0; i < (1 << d); i++) {
                double sum_n_d = calcWeightedNeighbourOccSum(i);
                idiotypes[i].n_d = sum_n_d;
                idiotypes[i].sum_n_d += sum_n_d;
            }

        // Next timestep
        t++;
    }

    /**
     * Iterates <code>n</code> times
     * @param n
     */
    public void iterate(int n) {
        for (int i = 0; i < n; i++)
            iterate();
    }

    /**
     * Resets network (sets everything to 0)
     */
    public void reset() {
        for (int i = 0; i < (1 << d); i++) {
            idiotypes[i].reset();
            idiotypes_ng[i].reset();
            idiotypes_lg[i].reset();
        }
        recalc();
    }

    /**
     * Resets statistics like sum of occupations, ...
     */
    public void recalc() {
        for (int i = 0; i < (1 << d); i++) {
            idiotypes[i].recalc();
            idiotypes_ng[i].recalc();
            idiotypes_lg[i].recalc();
        }

        total_sum_n = 0;
        t = 0;
    }

    /**
     * Calculates occupation of determinant bit group S_<code>l</code>
     * (with changed determinant bits, reduces number of determinant bits from call to call)
     *
     * @param maskDeterminantBit
     * @param valuesDeterminantBits
     * @param l
     * @param mismatchMask
     * @return
     */
    private int calcGroupOccupationRec(int maskDeterminantBits, int valuesDeterminantBits, int l, int mismatchMask) {
        int res = 0;
        if (l == 0) {
            for (int i = 0; i < (1 << d); i++)
                if ((i & maskDeterminantBits) == valuesDeterminantBits)
                    res += idiotypes[i].n;
        } else
            // if l > 0, change values of determinant bits for next call and reduce l by 1
            while (mismatchMask > (1 << (l - 1))) {
                mismatchMask >>= 1;
                if ((maskDeterminantBits & mismatchMask) != 0)
                    res += calcGroupOccupationRec(maskDeterminantBits, valuesDeterminantBits ^ mismatchMask, l - 1,
                            mismatchMask);
            }
        return res;
    }

    /**
     * Calculates occupation of determinant bit group S_<code>l</code>
     *
     * @param maskDeterminantBits
     * @param valuesDeterminantBits
     * @param l Index of group
     * @return
     */
    public int calcGroupOccupation(int maskDeterminantBits, int valuesDeterminantBits, int l) {
        return calcGroupOccupationRec(maskDeterminantBits, valuesDeterminantBits, l, (1 << d));
    }

    /**
     * Creates new IdiotypicNetwork with given parameters
     *
     * @param d Length of bitstrings (cannot be changed afterwards)
     * @param p Influx propability
     * @param t_l Lower range of update window rule
     * @param t_u Upper range of update window rule
     * @param N highest possible occupation
     */
    public IdiotypicNetwork(int d, double p, int t_l, int t_u, int N) {
        this.d = d;
        this.p = p;
        this.t_l = t_l;
        this.t_u = t_u;
        this.N = N;

        idiotypes = new Idiotype[1 << d];
        idiotypes_ng = new Idiotype[1 << d];
        idiotypes_lg = new Idiotype[1 << d];
        cog = new double[d];

        for (int i = 0; i < (1 << d); i++) {
            idiotypes[i] = new Idiotype();
            idiotypes_ng[i] = new Idiotype();
            idiotypes_lg[i] = new Idiotype();
        }

        rng = new Random();

        linkWeighting = new double[d];
        // Normally apply unweighted two-mismatch links
        linkWeighting[0] = 1;
        if (d > 1)
            linkWeighting[1] = 1;
        if (d > 2)
            linkWeighting[2] = 1;

        reset();

    }

    /**
     * Gets weighting of link with mismatch count <code>dist</code>
     *
     * @param dist Number of mismatches
     * @return
     */
    public double getLinkWeighting(int dist) {
        if (dist < d)
            return linkWeighting[dist];
        else
            return -1;
    }

    /**
     * Sets weighting of link with mismatch count <code>dist</code> to <code>weighting</code>
     *
     * @param dist
     * @param weighting
     */
    public void setLinkWeighting(int dist, double weighting) {
        if (dist < d) {
            linkWeighting[dist] = weighting;
            if (weighting == 0) {
                for (int i = dist; i < d; i++)
                    linkWeighting[i] = 0;
            }
        }
    }

    /**
     * Gets influx propability
     *
     * @return
     */
    public double getp() {
        return p;
    }

    /**
     * Gets bitstring length
     *
     * @return
     */
    public int getd() {
        return d;
    }

    /**
     * Gets current idiotypes
     *
     * @return
     */
    public Idiotype[] getIdiotypes() {
        return idiotypes;
    }

    /**
     * Gets current time
     *
     * @return
     */
    public int gett() {
        return t;
    }

    /**
     * Gets lower range of update window rule
     *
     * @return
     */
    public int gett_l() {
        return t_l;
    }

    /**
     * Gets upper ranger of update window rule
     *
     * @return
     */
    public int gett_u() {
        return t_u;
    }

    /**
     * Gets center of gravity
     *
     * @return
     */
    public double[] getCOG() {
        return cog;
    }

    /**
     * Gets highest possible occupation
     *
     * @return
     */
    public int getN() {
        return N;
    }

    /**
     * Gets current total occupation of network
     *
     * @return
     */
    public int gettotal_sum_n() {
        return total_sum_n;
    }

    /**
     * Gets the external influx
     *
     * @return
     */
    public int[] getExternalInflux() {
        return externalInflux;
    }

    /**
     * Sets the external influx
     * 
     * @param externalInflux
     */
    public void setExternalInflux(int[] externalInflux) {
        this.externalInflux = externalInflux;
    }

    /**
     * Sets if center of gravity should be calculated
     *
     * @param statCenterOfGravity
     */
    public void setStatCenterOfGravity(boolean statCenterOfGravity) {
        this.statCenterOfGravity = statCenterOfGravity;
    }

    /**
     * Sets if advanced neighbour occupation statistics should be done
     * (slows program down by factor 2)
     * 
     * @param statNeighbourOccupations
     */
    public void setStatNeighbourOccupations(boolean statNeighbourOccupations) {
        this.statNeighbourOccupations = statNeighbourOccupations;
    }

    /**
     * Sets influx propability
     *
     * @param p
     */
    public void setp(double p) {
        this.p = p;
    }

    /**
     * Sets lower range of update window rule
     *
     * @param t_l
     */
    public void sett_l(int t_l) {
        this.t_l = t_l;
    }

    /**
     * Sets upper range of update window rule
     *
     * @param t_u
     */
    public void sett_u(int t_u) {
        this.t_u = t_u;
    }

    /**
     * Sets highest possible occupation
     * 
     * @param N
     */
    public void setN(int N) {
        this.N = N;
    }

}

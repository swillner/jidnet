package jidnet.idnet;

import java.util.Random;

/**
 * Idiotypic network with very simple statistic and analysis, for more analysis extend
 *
 * @author Sven Willner
 */
public class IdiotypicNetwork {

    int total_sum_n; // Sum of total occupation
    double[] cog; // Center of gravity vector
    int N; // Highest possible occupation
    int d; // Length of bitstrings
    double p; // Influx propability
    int t_l, t_u; // Lower and upper range for update rule
    Idiotype[] idiotypes, idiotypes_ng, idiotypes_lg; // Idiotypes of current, next (ng) and last (lg) generation
    int t; // Time / generation
    double[] linkWeighting;
    boolean statNeighbourOccupations = false;
    boolean statCenterOfGravity = false;
    boolean statClusters = false;
    Random rng;

    protected final void influx() {
        for (int i = 0; i < (1 << d); i++) {
            idiotypes_lg[i].n = idiotypes[i].n; // TODO
            if (idiotypes[i].n < N && rng.nextDouble() < p) {
                if (idiotypes[i].n == 0)
                    idiotypes[i].b++;
                idiotypes[i].n++;
            }
        }
    }

    private double calcWeightedLinkSumRec(int j, int mismatch, int dist) {
        double res = 0;
        while (mismatch != 0) {
            mismatch >>= 1;
            res += idiotypes[j ^ mismatch].n * linkWeighting[dist];
            if (linkWeighting[dist + 1] > 0)
                res += calcWeightedLinkSumRec(j ^ mismatch, mismatch, dist + 1);
        }
        return res;
    }

    protected final double calcWeightedLinkSum(int i) {
        int complement = ~i & ((1 << d) - 1);
        return idiotypes[complement].n * linkWeighting[0] + calcWeightedLinkSumRec(complement, 1 << d, 1);
    }

    protected final void update() {
        total_sum_n = 0;

        for (int i = 0; i < (1 << d); i++) {

            double sum_n_d = calcWeightedLinkSum(i);

            if (idiotypes[i].n > 0)
                if (sum_n_d / idiotypes[i].n >= t_l && sum_n_d / idiotypes[i].n <= t_u)
                    idiotypes_ng[i].n = idiotypes[i].n;
                else
                    idiotypes_ng[i].n = idiotypes[i].n - 1;
            else
                idiotypes_ng[i].n = 0;

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
                            cog[j]++;
                        else
                            cog[j]--;

            } else {
                idiotypes_ng[i].tau = 0;
                idiotypes_ng[i].cluster_size = 0;
            }

        }
    }

    /*
    Nach iterate(), vor Aufruf Zeit t, nachher t+1
    idiotypes_lg = letzte Generation (t)
    idiotypes    = aktuelle Generation (t+1)
    idiotypes_ng = vorletzte Generation (t-1)
     */
    public void iterate() {
        if (statCenterOfGravity)
            for (int i = 0; i < d; i++)
                cog[i] = 0;

        influx();
        /*
        Hier:
        idiotypes_lg = letzte Generation (t)
        idiotypes    = letzte Generation (t) + Influx
        idiotypes_ng = vorletzte Generation (t-1)
         */
        update();

        if (statCenterOfGravity)
            for (int i = 0; i < d; i++)
                cog[i] /= total_sum_n;

        // Permutiere
        // idiotypes wird idiotypes_ng wird idiotypes_lg wird idiotypes
        Idiotype[] tmp = idiotypes_lg;
        idiotypes_lg = idiotypes;
        idiotypes = idiotypes_ng;
        idiotypes_ng = tmp;

        if (statNeighbourOccupations)
            for (int i = 0; i < (1 << d); i++) {
                double sum_n_d = calcWeightedLinkSum(i);
                idiotypes[i].n_d = sum_n_d;
                idiotypes[i].sum_n_d += sum_n_d;
            }

        /*if (statClusters) {
        for (int j = 0; j < (1 << d); j++) {
        idiotypes[j].cluster_size = 0;
        cluster_sizes[j] = 0;
        types[j] = 0;
        }
        int c = 0;
        for (int i = 0; i < (1 << d); i++) {
        if (idiotypes[i].n > 0) {
        cluster_sizes[c] = calc_all_clusters_rec(i, cluster_sizes[c], types);
        c++;
        }
        }
        for (int j = 0; j < (1 << d); j++) {
        if (types[j] != 0) {
        idiotypes[j].cluster_size = types[j];
        idiotypes[j].total_cluster_size += idiotypes[j].cluster_size;
        } else {
        idiotypes[j].cluster_size = 0;
        }
        }
        }*/

        t++;
    }

    public void iterate(int n) {
        for (int i = 0; i < n; i++)
            iterate();
    }

    public void reset() {
        for (int i = 0; i < (1 << d); i++) {
            idiotypes[i].reset();
            idiotypes_ng[i].reset();
            idiotypes_lg[i].reset();
        }
        recalc();
    }

    public void recalc() {
        for (int i = 0; i < (1 << d); i++) {
            idiotypes[i].recalc();
            idiotypes_ng[i].recalc();
            idiotypes_lg[i].recalc();
        }

        total_sum_n = 0;
        t = 0;
    }

    // TODO int calcAllClustersRec(int i, int[] p, int[][] types) {    }
    private int calcGroupOccupationRec(int maskDeterminantBit, int valuesDeterminantBits, int l, int mismatch) {
        int res = 0;
        if (l == 0) {
            for (int i = 0; i < (1 << d); i++)
                if ((i & maskDeterminantBit) == valuesDeterminantBits)
                    res += idiotypes[i].n;
        } else
            while (mismatch > (1 << (l - 1))) {
                mismatch = mismatch >> 1;
                res += calcGroupOccupationRec(maskDeterminantBit, valuesDeterminantBits ^ mismatch, l - 1, mismatch);
            }
        return res;
    }

    public int calcGroupOccupation(int maskDeterminantBits, int valuesDeterminantBits, int l) {
        return calcGroupOccupationRec(maskDeterminantBits, valuesDeterminantBits, l, (1 << d));
    }

    public IdiotypicNetwork(int d, double p, int t_l, int t_u, int N) {
        this.d = d;
        this.p = p;
        this.t_l = t_l;
        this.t_u = t_u;
        this.N = N;

        idiotypes = new Idiotype[1 << d];
        idiotypes_ng = new Idiotype[1 << d];
        idiotypes_lg = new Idiotype[1 << d];

        for (int i = 0; i < (1 << d); i++) {
            idiotypes[i] = new Idiotype();
            idiotypes_ng[i] = new Idiotype();
            idiotypes_lg[i] = new Idiotype();
        }

        rng = new Random();

        linkWeighting = new double[d];
        linkWeighting[0] = 1;
        if (d > 1)
            linkWeighting[1] = 1;
        if (d > 2)
            linkWeighting[2] = 1;

        reset();

        cog = new double[d];
    }

    public double getLinkWeighting(int component) {
        if (component < d)
            return linkWeighting[component];
        else
            return -1;
    }

    public void setLinkWeighting(int component, double weighting) {
        if (component < d)
            linkWeighting[component] = weighting;
    }

    public double getp() {
        return p;
    }

    public int getd() {
        return d;
    }

    public Idiotype[] getIdiotypes() {
        return idiotypes;
    }

    public int gett() {
        return t;
    }

    public int gett_l() {
        return t_l;
    }

    public int gett_u() {
        return t_u;
    }

    public double[] getCOG() {
        return cog;
    }

    public int getN() {
        return N;
    }

    public int getSum_n() {
        return total_sum_n;
    }

    public boolean isStatCenterOfGravity() {
        return statCenterOfGravity;
    }

    public void setStatCenterOfGravity(boolean statCenterOfGravity) {
        this.statCenterOfGravity = statCenterOfGravity;
    }

    public boolean isStatClusters() {
        return statClusters;
    }

    public void setStatClusters(boolean statClusters) {
        this.statClusters = statClusters;
    }

    public boolean isStatNeighbourOccupations() {
        return statNeighbourOccupations;
    }

    public void setStatNeighbourOccupations(boolean statNeighbourOccupations) {
        this.statNeighbourOccupations = statNeighbourOccupations;
    }

    public void setp(double p) {
        this.p = p;
    }

    public void sett_l(int t_l) {
        this.t_l = t_l;
    }

    public void sett_u(int t_u) {
        this.t_u = t_u;
    }

    public void setN(int N) {
        this.N = N;
    }

}

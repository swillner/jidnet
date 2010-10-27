package jidnet.idnet;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Properties;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Extended IdiotypicNetwork, implements analysis of determinant bits, 
 * loading and storage of parameters, center of gravity
 *
 * @author Sven Willner
 */
public class IdnetManager extends IdiotypicNetwork {

    /** History of center of gravity */
    private double[][] cogWindow;
    /** Size of center of gravity history */
    private final int cogWindowSize = 100;
    /** Maximal standard deviation of center of gravity component to be assumed to be constant */
    private double max_s;
    /** Currently used seed for random number generator */
    private long seed;
    /** Saves number of determinant bits, after they have been calculated */
    private int d_m = 0;
    /** Calculate mean occupations of S_l groups */
    private boolean calcMeanGroupOccs = false;
    /** Saves total occupation of determinant bit groups */
    private int[] totalGroupOccs;
    /** Saves last calculated determinat bits */
    DeterminantBits detBits;

    /**
     * Default parameters: <code>d<code> = 12, <code>p</code> = 0.027, <code>t_l</code> = 1, <code>t_u</code> = 10,
     * <code>N</code> = 1, <code>max_s</code> = 0.04
     */
    public IdnetManager() {
        super(12, 0.027, 1, 10, 1);

        max_s = 0.04;
        setStatCenterOfGravity(true);

        initialize();
    }

    @Override
    protected final void initialize() {
        super.initialize();
        cogWindow = new double[cogWindowSize][d];
    }

    /**
     * Gets current occupation of determinant bit group S_<code>l</code>
     * (No recalculation of determinant bits!)
     *
     * @param l
     * @return
     */
    public int getGroupOccupation(int l) {
        detBits = calcDeterminantBits();
        return calcGroupOccupation(detBits.mask, detBits.values, l);
    }

    /**
     * Returns size of determinant bit group S_<code>l</code>
     * (Not current occupation of that group!)
     *
     * @param l
     * @param d_m
     * @return
     */
    public int calcGroupSize(int l, int d_m) {
        if (l > d_m || l < 0) {
            return 0;
        }
        return (1 << (d - d_m)) * Helper.binomial(d_m, l);
    }

    public static int calcLinkMatrixElem(int i, int j, int d_m, int m, int d) {
        i++;
        j++;
        int sum = 0;
        int delta_i_j = d_m - i - j + 2;
        if (delta_i_j == 0) {
            int l_max = Math.min(d - d_m, m);
            for (int l = 0; l <= l_max; l++) {
                int k_max = Math.min(i - 1, Math.min(d_m - i + 1, (m - l) / 2));
                for (int k = 0; k <= k_max; k++) {
                    sum += Helper.binomial(d - d_m, l) * Helper.binomial(i - 1, k) * Helper.binomial(d_m - i + 1, k);
                }
            }
        } else if (delta_i_j > 0) {
            int l_max = Math.min(d - d_m, m - delta_i_j);
            for (int l = 0; l <= l_max; l++) {
                int k_max = Math.min(i - 1, Math.min(d_m - i + 1 - delta_i_j, (m - l - delta_i_j) / 2));
                for (int k = 0; k <= k_max; k++) {
                    sum += Helper.binomial(d - d_m, l) * Helper.binomial(i - 1, k) * Helper.binomial(d_m - i + 1, k
                            + delta_i_j);
                }
            }
        } else { // delta_i_j < 0
            int l_max = Math.min(d - d_m, m + delta_i_j);
            for (int l = 0; l <= l_max; l++) {
                int k_max = Math.min(i - 1 + delta_i_j, Math.min(d_m - i + 1, (m - l + delta_i_j) / 2));
                for (int k = 0; k <= k_max; k++) {
                    sum += Helper.binomial(d - d_m, l) * Helper.binomial(i - 1, k - delta_i_j) * Helper.binomial(
                            d_m - i + 1, k);
                }
            }
        }
        return sum;
    }

    /**
     * Gets total occupation of determinat bit groups
     *
     * @return
     */
    public int[] getTotalGroupOccs() {
        return totalGroupOccs;
    }

    /**
     * Gets number of last calculated determinant bits (does not recalculate!)
     * 
     * @return
     */
    public int getd_m() {
        return d_m;
    }

    /**
     * Sets if mean occupations of determinant bit groups should be calculated
     * 
     * @param calcMeanGroupOccs
     */
    public void setCalcMeanGroupOccs(boolean calcMeanGroupOccs) {
        this.calcMeanGroupOccs = calcMeanGroupOccs;
        if (calcMeanGroupOccs) {
            detBits = calcDeterminantBits();
            d_m = Helper.hammingWeight(detBits.mask);
            totalGroupOccs = new int[d_m + 1];
            recalc();
        }
    }

    /**
     * Sets <code>seed</code> of random number generator
     *
     * @param seed Seed
     */
    public void reseed(long seed) {
        rng.setSeed(seed);
        this.seed = seed;
    }

    /**
     * Gets currently used <code>seed</code>
     *
     * @return
     */
    public long getSeed() {
        return seed;
    }

    /**
     * Do one iteration step (overridden to do center of gravity statistics)
     */
    @Override
    public void iterate() {
        super.iterate();
        System.arraycopy(cog, 0, cogWindow[t % cogWindowSize], 0, d);
        if (calcMeanGroupOccs) {
            for (int l = 0; l <= d_m; l++) {
                totalGroupOccs[l] += getGroupOccupation(l);
            }
        }
    }

    /**
     * Calculates the standard deviation of center of gravity component <code>c</code>
     *
     * @param c Component of cog-vector
     * @return Standard deviation
     */
    public double getCOGStandardDeviation(int c) {
        double mean = getCOGMean(c);
        double s = 0;
        for (int i = 0; i < cogWindowSize; i++) {
            s += (cogWindow[i][c] - mean) * (cogWindow[i][c] - mean);
        }
        s = Math.sqrt(s / (cogWindowSize - 1));
        return s;
    }

    public double getCOGMean(int c) {
        double mean = 0;
        for (int i = 0; i < cogWindowSize; i++) {
            mean += cogWindow[i][c];
        }
        mean /= cogWindowSize;
        return mean;
    }

    /**
     * Tries do determine determinant bits
     *
     * @return
     */
    public DeterminantBits calcDeterminantBits() {
        class MyComparator implements Comparator {

            public int compare(Object obj1, Object obj2) {
                int result = 0;
                Map.Entry<Integer, Double> e1 = (Map.Entry<Integer, Double>) obj1;
                Map.Entry<Integer, Double> e2 = (Map.Entry<Integer, Double>) obj2;
                Double value1 = (Double) e1.getValue();
                Double value2 = (Double) e2.getValue();

                if (value1.compareTo(value2) == 0) {
                    Integer int1 = (Integer) e1.getKey();
                    Integer int2 = (Integer) e2.getKey();
                    result = int1.compareTo(int2);
                } else {
                    result = value2.compareTo(value1);
                }

                return result;
            }
        }

        DeterminantBits result = new DeterminantBits();
        HashMap<Integer, Double> order = new HashMap<Integer, Double>();
        for (int j = 0; j < d; j++) {
            double s = getCOGStandardDeviation(j);
            if (s < max_s) {
                double mean = getCOGMean(j);
                if (mean > max_s) {
                    result.mask |= 1 << j;
                    result.values |= 1 << j;
                    order.put(j, cog[j]);
                } else if (mean < -max_s) {
                    result.mask |= 1 << j;
                    order.put(j, -cog[j]);
                } else {
                    order.put(j, Double.NEGATIVE_INFINITY);
                }
            } else {
                order.put(j, Double.NEGATIVE_INFINITY);
            }
        }

        ArrayList<Map.Entry<Integer, Double>> myArrayList = new ArrayList<Map.Entry<Integer, Double>>(order.entrySet());
        Collections.sort(myArrayList, new MyComparator());

        result.order = new int[d];
        int j = 0;
        for (Map.Entry<Integer, Double> e : myArrayList) {
            result.order[j] = e.getKey();
            j++;
        }

        return result;
    }

    /**
     * Gets maximal standard deviation of center of gravity component to be assumed to be constant
     *
     * @return
     */
    public double getmax_s() {
        return max_s;
    }

    /**
     * Sets maximal standard deviation of center of gravity component to be assumed to be constant
     *
     * @param detBitsMaxDeviation
     */
    public void setmax_s(double max_s) {
        this.max_s = max_s;
    }

    /**
     * Calculates cluster of <code>j</code> (descends into mismatched neighbours)
     *
     * @param j
     * @param cluster Cluster of <code>j</code>
     * @param mismatchMask Missmatches to last <code>j</code>
     * @param dist Distance to original <code>j</code>
     */
    private void calcClusterRecIntern(int j, ArrayList<Idiotype> cluster, int mismatchMask, int dist) {
        while (mismatchMask != 0) {
            mismatchMask >>= 1;
            calcClusterRec(j ^ mismatchMask, cluster);
            if (linkWeighting[dist + 1] > 0) {
                calcClusterRecIntern(j ^ mismatchMask, cluster, mismatchMask, dist + 1);
            }
        }
    }

    /**
     * Calculates cluster of <code>j</code>
     *
     * @param j
     * @param cluster Cluster of <code>j</code>
     */
    private void calcClusterRec(int j, ArrayList<Idiotype> cluster) {
        if (idiotypes[j].n > 0 && idiotypes[j].cluster == null) {
            idiotypes[j].cluster = cluster;
            cluster.add(idiotypes[j]);
            int complement = ~j & ((1 << d) - 1);
            if (linkWeighting[0] > 0) {
                calcClusterRecIntern(complement, cluster, 1 << d, 1);
            }
        }
    }

    /**
     * Calculates clusters and their sizes
     *
     * @return Vector of all clusters in network
     */
    public ArrayList<ArrayList<Idiotype>> calcClusters() {
        ArrayList<ArrayList<Idiotype>> res = new ArrayList<ArrayList<Idiotype>>();
        for (int i = 0; i < (1 << d); i++) {
            idiotypes[i].cluster = null;
        }
        for (int i = 0; i < (1 << d); i++) {
            if (idiotypes[i].n > 0 && idiotypes[i].cluster == null) {
                ArrayList<Idiotype> cluster = new ArrayList<Idiotype>();
                res.add(cluster);
                calcClusterRec(i, cluster);

            }
        }
        return res;
    }

    /**
     * Saves network snapshot to file
     *
     * @param fileName File name
     * @throws IOException
     */
    public void saveNetwork(String fileName, String comment) throws IOException {
        Properties prop = new Properties();
        prop.setProperty("d", Integer.toString(d));
        prop.setProperty("comment", comment);
        for (int i = 0; i < (1 << d); i++) {
            if (idiotypes[i].n > 0) {
                prop.setProperty(Integer.toString(i), Integer.toString(idiotypes[i].n));
            }
        }
        prop.storeToXML(new FileOutputStream(fileName), "jIdNet network snapshot");
    }

    /**
     * Loads network snapshot from file
     *
     * @param fileName File name
     * @throws IOException
     */
    public void loadNetwork(String fileName) throws IOException {
        reset();
        Properties prop = new Properties();
        prop.loadFromXML(new FileInputStream(fileName));
        setd(Integer.parseInt(prop.getProperty("d")));
        for (int i = 0; i < (1 << d); i++) {
            idiotypes[i].n = Integer.parseInt(prop.getProperty(fileName, "0"));
        }
        setChanged();
        notifyObservers("load");
    }

    public void saveStartConfiguration(String fileName, String comments) throws IOException {
        Properties prop = new Properties();
        prop.setProperty("d", Integer.toString(d));
        prop.setProperty("N", Integer.toString(N));
        prop.setProperty("p", Double.toString(p));
        prop.setProperty("seed", Long.toString(seed));
        prop.setProperty("t_l", Double.toString(t_l));
        prop.setProperty("t_u", Double.toString(t_u));
        prop.setProperty("comments", comments);
        for (int i = 0; i < linkWeighting.length; i++) {
            if (linkWeighting[i] > 0) {
                prop.setProperty("lw" + i, Double.toString(linkWeighting[i]));
            }
        }
        prop.storeToXML(new FileOutputStream(fileName), "jIdNet start configuration");
    }

    public void loadStartConfiguration(String fileName) throws IOException {
        Properties prop = new Properties();
        prop.loadFromXML(new FileInputStream(fileName));
        d = Integer.parseInt(prop.getProperty("d"));
        initialize();

        setp(Double.parseDouble(prop.getProperty("p")));
        sett_l(Double.parseDouble(prop.getProperty("t_l")));
        sett_u(Double.parseDouble(prop.getProperty("t_u")));
        setN(Integer.parseInt(prop.getProperty("N")));
        reseed(Long.parseLong(prop.getProperty("seed")));
        for (int i = 0; i < d; i++) {
            linkWeighting[i] = Double.parseDouble(prop.getProperty("lw" + i, "0"));
        }
    }
}

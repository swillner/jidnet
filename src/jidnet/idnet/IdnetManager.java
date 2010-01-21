package jidnet.idnet;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.Vector;

/**
 * Extended IdiotypicNetwork, implements analysis of determinant bits, 
 * loading and storage of parameters, center of gravity
 *
 * @author Sven Willner
 */
public class IdnetManager extends IdiotypicNetwork {

    /** Parameters (for saving / loading) */
    private Properties params;
    /** Histogram information for one <code>p</code> value */
    private int[] histogramMO, histogramLT, histogramON;
    /** History of center of gravity */
    private double[][] cogWindow;
    /** Size of center of gravity history */
    private final int cogWindowSize = 100;
    /** Maximal standard deviation of center of gravity component to be assumed to be constant */
    private double max_s;
    /** Currently used seed for random number generator */
    private long seed;

    public class DeterminantBits {

        public int mask = 0;
        public int values = 0;
    }

    /**
     * Default parameters: <code>d<code> = 12, <code>p</code> = 0.027, <code>t_l</code> = 1, <code>t_u</code> = 10,
     * <code>N</code> = 1, <code>max_s</code> = 0.04
     */
    public IdnetManager() {
        super(12, 0.027, 1, 10, 1);

        max_s = 0.04;
        setStatCenterOfGravity(true);

        cogWindow = new double[cogWindowSize][d];

        params = new Properties();
        params.setProperty("d", "12");
        params.setProperty("p", "0.027");
        params.setProperty("t_l", "1");
        params.setProperty("t_u", "10");
        params.setProperty("N", "1");
        params.setProperty("max_s", "0.04");
        params.setProperty("seed", "0");
        params.setProperty("lw0", "1");
        params.setProperty("lw1", "1");
        params.setProperty("lw2", "1");
        params.setProperty("lw3", "0");
        params.setProperty("lw4", "0");
        params.setProperty("lw5", "0");
        params.setProperty("lw6", "0");
        params.setProperty("lw7", "0");
        params.setProperty("lw8", "0");
        params.setProperty("lw9", "0");
        params.setProperty("lw10", "0");
        params.setProperty("lw11", "0");
    }

    /**
     * Loads parameters from file
     *
     * @param fileName
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void loadParams(String fileName) throws FileNotFoundException,
                                                   IOException {
        params.loadFromXML(new FileInputStream(fileName));
        loadParams(params);
    }

    /**
     * Loads parameters from other <code>Properties</code>-Object
     * @param params
     */
    public void loadParams(Properties params) {
        this.params.putAll(params);
        this.setp(Double.parseDouble(params.getProperty("p")));
        this.sett_l(Integer.parseInt(params.getProperty("t_l")));
        this.sett_u(Integer.parseInt(params.getProperty("t_u")));
        this.setN(Integer.parseInt(params.getProperty("N")));
        this.setmax_s(Double.parseDouble(params.getProperty("max_s")));
        this.reseed(Long.parseLong(params.getProperty("seed")));
        linkWeighting[0] = Double.parseDouble(params.getProperty("lw0"));
        linkWeighting[1] = Double.parseDouble(params.getProperty("lw1"));
        linkWeighting[2] = Double.parseDouble(params.getProperty("lw2"));
        linkWeighting[3] = Double.parseDouble(params.getProperty("lw3"));
        linkWeighting[4] = Double.parseDouble(params.getProperty("lw4"));
        linkWeighting[5] = Double.parseDouble(params.getProperty("lw5"));
        linkWeighting[6] = Double.parseDouble(params.getProperty("lw6"));
        linkWeighting[7] = Double.parseDouble(params.getProperty("lw7"));
        linkWeighting[8] = Double.parseDouble(params.getProperty("lw8"));
        linkWeighting[9] = Double.parseDouble(params.getProperty("lw9"));
        linkWeighting[10] = Double.parseDouble(params.getProperty("lw10"));
        linkWeighting[11] = Double.parseDouble(params.getProperty("lw11"));
    }

    public int calcGroupOccupation(int l) {
        DeterminantBits detBits = getDeterminantBits();
        return calcGroupOccupation(detBits.mask, detBits.values, l);
    }

    /**
     * Gets parameters
     * 
     * @return
     */
    public Properties getParams() {
        return params;
    }

    @Override
    public void setp(double p) {
        super.setp(p);
        params.setProperty("p", Double.toString(p));
    }

    @Override
    public void sett_l(int t_l) {
        super.sett_l(t_l);
        params.setProperty("t_l", Integer.toString(t_l));
    }

    @Override
    public void sett_u(int t_u) {
        super.sett_u(t_u);
        params.setProperty("t_u", Integer.toString(t_u));
    }

    @Override
    public void setN(int N) {
        super.setN(N);
        params.setProperty("N", Integer.toString(N));
    }

    @Override
    public void setLinkWeighting(int component, double weighting) {
        if (component < d) {
            super.setLinkWeighting(component, weighting);
            params.setProperty("lw" + component, Double.toString(weighting));
        }
    }

    /**
     * Saves parameters to XML-file
     *
     * @param fileName File name
     * @throws IOException
     */
    public void saveParams(String fileName) throws IOException {
        params.storeToXML(new FileOutputStream(fileName),
                "Idiotypic network parameters");
    }

    /**
     * Sets <code>seed</code> of random number generator
     *
     * @param seed Seed
     */
    public void reseed(long seed) {
        params.setProperty("seed", Long.toString(seed));
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
     * Calculates a step in the histogram (for one <code>p</code> value)
     *
     * @param numLoops Number of loops (reset for each loop) to take the mean of
     * @param tWait Number of iterations to wait before doing the statistics
     * @param tWindow Number of iterations to do the statistics on
     * @param ySteps Number of steps to devide the y-axis into
     * @throws IOException
     */
    private void calcHistogramStep(int numLoops, int tWait, int tWindow,
                                   int ySteps) throws IOException {
        for (int loop = 0; loop < numLoops; loop++) {
            this.reset();
            this.iterate(tWait);
            this.recalc();
            this.iterate(tWindow);

            for (int i = 0; i < (1 << this.getd()); i++) {
                Idiotype node = this.getIdiotypes()[i];

                if (histogramMO != null)
                    histogramMO[(int) ((double) node.sum_n * (double) (ySteps -
                            1) / (double) tWindow)]++;

                if (histogramON != null)
                    if ((double) this.gettotal_sum_n() / (double) tWindow < 80.)
                        histogramON[(int) ((double) node.sum_n_d / 80. *
                                (double) (ySteps - 1) / (double) tWindow)]++;

                if (histogramLT != null) {
                    double mlt = (double) node.sum_n / (double) node.b;
                    if (mlt < 10000. && mlt >= 1)
                        histogramLT[(int) (Math.log10(mlt) / 5. *
                                (double) (ySteps - 1))]++;
                }
            }
        }

    }

    /**
     * Do one iteration step (overridden to do center of gravity statistics)
     */
    @Override
    public void iterate() {
        super.iterate();
        for (int i = 0; i < 12; i++)
            cogWindow[t % cogWindowSize][i] = cog[i];
    }

    /**
     * Calculates the standard deviation of center of gravity component <code>c</code>
     *
     * @param c Component of cog-vector
     * @return Standard deviation
     */
    public double getCOGStandardDeviation(int c) {
        double mean = 0, s = 0;
        for (int i = 0; i < cogWindowSize; i++)
            mean += cogWindow[i][c];
        mean /= cogWindowSize;
        for (int i = 0; i < cogWindowSize; i++)
            s += (cogWindow[i][c] - mean) * (cogWindow[i][c] - mean);
        s = Math.sqrt(s / (cogWindowSize - 1));
        return s;
    }

    /**
     * Tries do determine determinant bits
     *
     * @return
     */
    public DeterminantBits getDeterminantBits() {
        DeterminantBits result = new DeterminantBits();
        for (int j = 0; j < d; j++) {
            double s = getCOGStandardDeviation(j);
            if (s < max_s)
                if (cog[j] > 5 * s) {
                    result.mask |= 1 << j;
                    result.values |= 1 << j;
                } else if (cog[j] < -5 * s)
                    result.mask |= 1 << j;
            // TODO : Proper recognision of determinant bits
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
        params.setProperty("max_s", Double.toString(max_s));
    }

    /**
     * Calculates cluster of <code>j</code>
     *
     * @param j
     * @param cluster Cluster of <code>j</code>
     * @param visited Boolean-Array which idiotypes have been counted already
     * @param mismatchMask Missmatches to last <code>j</code>
     * @param dist Distance to original <code>j</code>
     */
    private void calcClusterRec(int j, Vector<Idiotype> cluster, boolean[] visited, int mismatchMask, int dist) {
        while (mismatchMask != 0) {
            mismatchMask >>= 1;
            if (idiotypes[j ^ mismatchMask].n > 0 && !visited[j ^ mismatchMask]) {
                visited[j ^ mismatchMask] = true;
                idiotypes[j ^ mismatchMask].cluster = cluster;
                cluster.add(idiotypes[j ^ mismatchMask]);
            }
            if (linkWeighting[dist + 1] > 0)
                calcClusterRec(j ^ mismatchMask, cluster, visited, mismatchMask, dist + 1);
        }
    }

    /**
     * Calculates clusters and their sizes
     *
     * @return Vector of all clusters in network
     */
    public Vector<Vector<Idiotype>> calcClusters() {
        Vector<Vector<Idiotype>> res = new Vector<Vector<Idiotype>>();
        boolean[] visited = new boolean[1 << d];
        for (int i = 0; i < (1 << d); i++)
            if (!visited[i] && idiotypes[i].n > 0) {
                visited[i] = true;
                Vector<Idiotype> cluster = new Vector<Idiotype>();
                res.add(cluster);
                cluster.add(idiotypes[i]);
                idiotypes[i].cluster = cluster;

                int complement = ~i & ((1 << d) - 1);
                if (idiotypes[complement].n > 0) {
                    visited[complement] = true;
                    idiotypes[complement].cluster = cluster;
                    cluster.add(idiotypes[complement]);
                }

                if (linkWeighting[0] > 0)
                    calcClusterRec(complement, cluster, visited, 1 << d, 1);
            }
        return res;
    }

    /**
     * Creates histogram, reads settings from config file
     *
     * @param configFileName Name of config file
     * @throws Exception
     */
    public void createHistogram(String configFileName) throws Exception {
        Properties config = new Properties();
        config.loadFromXML(new FileInputStream(configFileName));
        if (config.getProperty("p_from") == null)
            throw new Exception("Configuration property 'p_from' missing");
        double pFrom = Double.parseDouble(config.getProperty("p_from"));
        if (config.getProperty("p_to") == null)
            throw new Exception("Configuration property 'p_to' missing");
        double pTo = Double.parseDouble(config.getProperty("p_to"));
        if (config.getProperty("p_steps") == null)
            throw new Exception("Configuration property 'p_steps' missing");
        int pSteps = Integer.parseInt(config.getProperty("p_steps"));
        if (config.getProperty("num_loops") == null)
            throw new Exception("Configuration property 'num_loops' missing");
        int numLoops = Integer.parseInt(config.getProperty("num_loops"));
        if (config.getProperty("y_steps") == null)
            throw new Exception("Configuration property 'y_steps' missing");
        int ySteps = Integer.parseInt(config.getProperty("y_steps"));
        if (config.getProperty("t_wait") == null)
            throw new Exception("Configuration property 't_wait' missing");
        int tWait = Integer.parseInt(config.getProperty("t_wait"));
        if (config.getProperty("t_window") == null)
            throw new Exception("Configuration property 't_window' missing");
        int tWindow = Integer.parseInt(config.getProperty("t_window"));

        String fileNameLT = config.getProperty("filename_LT");
        String fileNameMO = config.getProperty("filename_MO");
        String fileNameON = config.getProperty("filename_ON");

        if (fileNameLT != null)
            histogramLT = new int[ySteps];
        if (fileNameMO != null)
            histogramMO = new int[ySteps];
        if (fileNameON != null)
            histogramON = new int[ySteps];

        FileWriter fileWriterLT = null, fileWriterMO = null, fileWriterON = null;
        if (fileNameLT != null)
            new FileWriter(fileNameLT, false).close();
        if (fileNameMO != null)
            new FileWriter(fileNameMO, false).close();
        if (fileNameON != null)
            new FileWriter(fileNameON, false).close();

        for (int i_p = 0; i_p < pSteps; i_p++) {
            double p = pFrom + i_p * pTo / pSteps;
            this.setp(p);
            this.reset();
            calcHistogramStep(numLoops, tWait, tWindow, ySteps);

            if (histogramLT != null)
                fileWriterLT = new FileWriter(fileNameLT, true);
            if (histogramMO != null)
                fileWriterMO = new FileWriter(fileNameMO, true);
            if (histogramON != null)
                fileWriterON = new FileWriter(fileNameON, true);
            for (int i = 0; i < ySteps; i++) {
                if (fileNameMO != null) {
                    fileWriterMO.write(p + " " + (double) i * this.getN() /
                            (double) ySteps + " ");
                    fileWriterMO.write((double) histogramMO[i] /
                            (double) numLoops + "\n");
                    histogramMO[i] = 0;
                }
                if (fileNameLT != null) {
                    fileWriterLT.write(p + " " + Math.pow(10, (double) i * 5 /
                            (double) ySteps) + " ");
                    fileWriterLT.write((double) histogramLT[i] /
                            (double) numLoops + "\n");
                    histogramLT[i] = 0;
                }
                if (fileNameON != null) {
                    fileWriterON.write(p + " " + (double) i * this.getN() * 80 /
                            (double) ySteps + " ");
                    fileWriterON.write((double) histogramON[i] /
                            (double) numLoops + "\n");
                    histogramON[i] = 0;
                }
            }
            if (fileNameLT != null) {
                fileWriterLT.write("\n");
                fileWriterLT.close();
            }
            if (fileNameMO != null) {
                fileWriterMO.write("\n");
                fileWriterMO.close();
            }
            if (fileNameON != null) {
                fileWriterON.write("\n");
                fileWriterON.close();
            }

            System.out.println(i_p);

        }
    }

    /**
     * Saves network snapshot to file
     *
     * @param fileName File name
     * @throws IOException
     */
    public void saveNetwork(String fileName) throws IOException {
        FileWriter writer = new FileWriter(fileName);
        for (int i = 0; i < (1 << d); i++)
            writer.write(i + " " + idiotypes[i].n + "\n");
        writer.close();
    }

    /**
     * Loads network snapshot from file
     *
     * TODO : Proper parsing, read out d
     *
     * @param fileName File name
     * @throws IOException
     */
    public void loadNetwork(String fileName) throws IOException {
        reset();
        if (d != 12)
            return;
        FileReader reader = new FileReader(fileName);
        for (int i = 0; i < (1 << d); i++) {
            while (reader.read() != ' ') {
            }
            String s = "";
            char c;
            while ((c = (char) reader.read()) != '\n')
                s += c;
            idiotypes[i].n = Integer.parseInt(s);
        }
        reader.close();
    }

}

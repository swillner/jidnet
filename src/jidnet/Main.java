package jidnet;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import jidnet.idnet.DeterminantBits;
import jidnet.idnet.Helper;
import jidnet.idnet.Idiotype;
import jidnet.idnet.IdnetManager;

/**
 *
 * @author Sven Willner
 */
public class Main {

    private static IdnetManager idnetManager = new IdnetManager();
    private static Properties config = new Properties();
    /** Histogram information for one <code>p</code> value */
    private static int[] histogramMO, histogramLT, histogramON;

    public static void main(String[] args) throws Exception {
        try {
            config.loadFromXML(new FileInputStream("config.xml"));
        } catch (Exception e) {
            System.err.println("Couldn't load config file 'config.xml', terminating");
            System.exit(-1);
        }

        if (config.getProperty("action") == null)
            System.out.println("No action defined");
        else {
            long t0 = System.currentTimeMillis();

            try {
                System.out.println("Performing action '" + config.getProperty("action") + "'");
                if (config.getProperty("action").equals("histogram"))
                    createHistogram();
                else if (config.getProperty("action").equals("topology_histogram"))
                    createTopologyHistogram();
                else if (config.getProperty("action").equals("show_link_matrix"))
                    showLinkMatrix();
                else if (config.getProperty("action").equals("stat_from_snapshot"))
                    calcDetBitsFromSnapShot();
                else
                    System.err.println("Action unknown");
            } catch (Exception e) {
                System.out.println(e.toString());
            }

            long t1 = System.currentTimeMillis();
            System.out.println("Time needed: " + (t1 - t0) / 60000 + "min");
        }
    }

    private static String getConfigProperty(String name) throws Exception {
        if (config.getProperty(name) == null)
            throw new Exception("Configuration property '" + name + "' missing");
        return config.getProperty(name);
    }

    private static void calcDetBitsFromSnapShot() throws Exception {
        class MyComparator implements Comparator {

            public int compare(Object obj1, Object obj2) {
                int result = 0;
                Map.Entry<String, Integer> e1 = (Map.Entry<String, Integer>) obj1;
                Map.Entry<String, Integer> e2 = (Map.Entry<String, Integer>) obj2;
                Integer value1 = (Integer) e1.getValue();
                Integer value2 = (Integer) e2.getValue();

                if (value1.compareTo(value2) == 0) {
                    String int1 = (String) e1.getKey();
                    String int2 = (String) e2.getKey();
                    result = int1.compareTo(int2);
                } else
                    result = value2.compareTo(value1);

                return result;
            }
        }

        Hashtable<String, Integer> possibleDetBits = new Hashtable<String, Integer>();
        Hashtable<String, Long> possibleDetBitsSeeds = new Hashtable<String, Long>();
        int tWait = Integer.parseInt(getConfigProperty("t_wait"));
        idnetManager.setp(Double.parseDouble(getConfigProperty("p")));
        idnetManager.setmax_s(Double.parseDouble(getConfigProperty("max_s")));
        for (int i = 0; i < Integer.parseInt(getConfigProperty("num_loops")); i++) {

            idnetManager.loadNetwork(getConfigProperty("snapshot_file"));
            idnetManager.reseed(Long.parseLong(getConfigProperty("first_seed")));
            idnetManager.iterate();
            long seed = System.currentTimeMillis();
            idnetManager.reseed(seed);

            idnetManager.iterate(tWait);

            DeterminantBits detBits = idnetManager.calcDeterminantBits();
            if (Helper.hammingWeight(detBits.mask) % 2 == 1) {
                idnetManager.iterate(5000);
                detBits = idnetManager.calcDeterminantBits();
            }
            String str = Helper.getBitString(detBits.mask, 12) + " / " + Helper.getBitString(detBits.values, 12);
            if (possibleDetBits.containsKey(str))
                possibleDetBits.put(str, possibleDetBits.get(str) + 1);
            else {
                possibleDetBits.put(str, 1);
                possibleDetBitsSeeds.put(str, seed);
            }
            System.out.println(i);
        }

        ArrayList<Map.Entry<String, Integer>> myArrayList =
                new ArrayList<Map.Entry<String, Integer>>(possibleDetBits.entrySet());
        Collections.sort(myArrayList, new MyComparator());


        for (Entry<String, Integer> e : myArrayList)
            System.out.println(e.getKey() + " x " + e.getValue() + " (e.g. "
                    + possibleDetBitsSeeds.get(e.getKey()) + ")");
    }

    private static void showLinkMatrix() throws Exception {
        int d = Integer.parseInt(getConfigProperty("d"));
        int d_m = Integer.parseInt(getConfigProperty("d_m"));
        int m = Integer.parseInt(getConfigProperty("m"));

        for (int i = 0; i <= d_m; i++) {
            for (int j = 0; j <= d_m; j++)
                System.out.print(IdnetManager.calcLinkMatrixElem(i, j, d_m, m, d) + " ");
            System.out.println();
        }
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
    private static void calcHistogramStep(int numLoops, int tWait, int tWindow,
            int ySteps) throws IOException {
        for (int loop = 0; loop < numLoops; loop++) {
            idnetManager.reset();
            idnetManager.setStatNeighbourOccupations(false);
            idnetManager.iterate(tWait);
            idnetManager.setStatNeighbourOccupations(true);
            idnetManager.recalc();
            idnetManager.iterate(tWindow);

            for (int i = 0; i < (1 << idnetManager.getd()); i++) {
                Idiotype node = idnetManager.getIdiotypes()[i];

                if (histogramMO != null)
                    histogramMO[(int) ((double) node.sum_n * (double) (ySteps
                            - 1) / (double) tWindow)]++;

                if (histogramON != null)
                    if ((double) node.sum_n_d / (double) tWindow < 80.)
                        histogramON[(int) ((double) node.sum_n_d / 80.
                                * (double) (ySteps - 1) / (double) tWindow)]++;

                if (histogramLT != null) {
                    double mlt = (double) node.sum_n / (double) node.b;
                    if (mlt < 10000. && mlt >= 1)
                        histogramLT[(int) (Math.log10(mlt) / 5.
                                * (double) (ySteps - 1))]++;
                }
            }
        }

    }

    private static void createHistogram() throws Exception {
        double pFrom = Double.parseDouble(getConfigProperty("p_from"));
        double pTo = Double.parseDouble(getConfigProperty("p_to"));
        int pSteps = Integer.parseInt(getConfigProperty("p_steps"));
        int numLoops = Integer.parseInt(getConfigProperty("num_loops"));
        int ySteps = Integer.parseInt(getConfigProperty("y_steps"));
        int tWait = Integer.parseInt(getConfigProperty("t_wait"));
        int tWindow = Integer.parseInt(getConfigProperty("t_window"));

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

        idnetManager.setStatCenterOfGravity(false);

        for (int i_p = 0; i_p < pSteps; i_p++) {
            double p = pFrom + i_p * pTo / pSteps;
            idnetManager.setp(p);
            idnetManager.reset();
            calcHistogramStep(numLoops, tWait, tWindow, ySteps);

            if (histogramLT != null)
                fileWriterLT = new FileWriter(fileNameLT, true);
            if (histogramMO != null)
                fileWriterMO = new FileWriter(fileNameMO, true);
            if (histogramON != null)
                fileWriterON = new FileWriter(fileNameON, true);
            for (int i = 0; i < ySteps; i++) {
                if (fileNameMO != null) {
                    fileWriterMO.write(p + " " + (double) i * idnetManager.getN()
                            / (double) ySteps + " ");
                    fileWriterMO.write((double) histogramMO[i]
                            / (double) numLoops + "\n");
                    histogramMO[i] = 0;
                }
                if (fileNameLT != null) {
                    fileWriterLT.write(p + " " + Math.pow(10, (double) i * 5
                            / (double) ySteps) + " ");
                    fileWriterLT.write((double) histogramLT[i]
                            / (double) numLoops + "\n");
                    histogramLT[i] = 0;
                }
                if (fileNameON != null) {
                    fileWriterON.write(p + " " + (double) i * idnetManager.getN() * 80.
                            / (double) ySteps + " ");
                    fileWriterON.write((double) histogramON[i]
                            / (double) numLoops + "\n");
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

    private static void createTopologyHistogram() throws Exception {
        double pFrom = Double.parseDouble(getConfigProperty("p_from"));
        double pTo = Double.parseDouble(getConfigProperty("p_to"));
        int pSteps = Integer.parseInt(getConfigProperty("p_steps"));
        int numLoops = Integer.parseInt(getConfigProperty("num_loops"));
        int maxNeighbourCount = Integer.parseInt(getConfigProperty("max_neighbour_count"));
        int tWait = Integer.parseInt(getConfigProperty("t_wait"));
        int tWindow = Integer.parseInt(getConfigProperty("t_window"));
        int d_m = Integer.parseInt(getConfigProperty("d_m"));
        boolean waitForPattern = Boolean.parseBoolean(getConfigProperty("wait_for_pattern"));
        int[] neighbourCounts;

        FileWriter fw;
        (new FileWriter(getConfigProperty("filename_topology_histogram"), false)).close();

        idnetManager.setStatCenterOfGravity(waitForPattern);
        idnetManager.setmax_s(Double.parseDouble(getConfigProperty("max_s")));

        long seed = 0;

        for (int i_p = 0; i_p < pSteps; i_p++) {
            double p = Math.round((pFrom + (double) i_p * (pTo - pFrom) / (double) pSteps) * 10000.0) / 10000.0;
            if (p == 0)
                continue;
            idnetManager.setp(p);

            neighbourCounts = new int[maxNeighbourCount + 1];

            boolean found = false;

            for (int loop = 0; loop < numLoops; loop++) {
                seed = System.currentTimeMillis();
                idnetManager.reseed(seed);
                idnetManager.reset();

                int k = 1;
                int mask = (1 << 11) - 1;
                Idiotype[] idiotypes = idnetManager.getIdiotypes();
                for (int i = 0; i < (1 << 12); i++)
                    if (Helper.hammingWeight(i & mask) <= k && Math.random() < 0.5)
                        idiotypes[i].n = 1;

                idnetManager.setStatNeighbourOccupations(false);
                idnetManager.iterate(tWait);
                idnetManager.setStatNeighbourOccupations(true);

                if (waitForPattern)
                    if (Helper.hammingWeight(idnetManager.calcDeterminantBits().mask) != d_m)
                        continue;
                    else {
                        idnetManager.iterate(100);
                        if (Helper.hammingWeight(idnetManager.calcDeterminantBits().mask) != d_m)
                            continue;
                        else {
                            idnetManager.iterate(100);
                            if (Helper.hammingWeight(idnetManager.calcDeterminantBits().mask) != d_m)
                                continue;
                        }
                    }
                for (int n = 0; n < tWindow; n++) {
                    idnetManager.iterate();
                    for (Idiotype i : idnetManager.getIdiotypes())
                        if (i.n > 0 && (int) i.n_d <= maxNeighbourCount)
                            neighbourCounts[(int) i.n_d]++;
                }
                if (waitForPattern)
                    if (Helper.hammingWeight(idnetManager.calcDeterminantBits().mask) != d_m) {
                        neighbourCounts = new int[maxNeighbourCount + 1];
                        continue;
                    }
                found = true;
                break;
            }

            if (waitForPattern)
                if (found)
                    System.out.print("   pattern found for p=");
                else
                    System.out.print("no pattern found for p=");

            fw = new FileWriter(getConfigProperty("filename_topology_histogram"), true);
            fw.write("#seed " + seed + "\n");

            for (int n = 0; n <= maxNeighbourCount; n++)
                fw.write(p + " " + n + " " + neighbourCounts[n] + "\n");

            fw.write("\n");
            fw.close();

            System.out.println(p);
        }
    }
}

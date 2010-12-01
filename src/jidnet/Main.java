package jidnet;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import jidnet.idnet.Antigen;
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
            config.loadFromXML(new FileInputStream("jIdNet/config.xml"));
        } catch (Exception e) {
            System.err.println("Couldn't load config file 'config.xml', terminating");
            System.exit(-1);
        }

        if (config.getProperty("action") == null) {
            System.out.println("No action defined");
        } else {
            long t0 = System.currentTimeMillis();

            try {
                System.out.println("#Performing action '" + config.getProperty("action") + "'");
                if (config.getProperty("action").equals("histogram")) {
                    createHistogram();
                } else if (config.getProperty("action").equals("topology_histogram")) {
                    createTopologyHistogram();
                } else if (config.getProperty("action").equals("show_link_matrix")) {
                    showLinkMatrix();
                } else if (config.getProperty("action").equals("stat_from_snapshot")) {
                    calcDetBitsFromSnapShot();
                } else if (config.getProperty("action").equals("cog_diagram")) {
                    createCOGDiagram();
                } else if (config.getProperty("action").equals("test_antigen")) {
                    testAntigen();
                } else if (config.getProperty("action").equals("test_antigen2")) {
                    testAntigen2();
                } else if (config.getProperty("action").equals("test_decay")) {
                    testDecay();
                } else if (config.getProperty("action").equals("test_decay2")) {
                    testDecay2();
                } else if (config.getProperty("action").equals("pattern_decay")) {
                    patternDecay();
                } else if (config.getProperty("action").equals("entropy_histogram")) {
                    entropyHistogram();
                } else {
                    System.err.println("Action unknown");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            long t1 = System.currentTimeMillis();
            System.out.println("Time needed: " + (t1 - t0) / 60000 + "min");
        }
    }

    public static double calcS(IdnetManager idnetManager) {
        double S = 0;
        for (int j = 0; j < (1 << idnetManager.getd()); j++) {
            double p1 = (double) idnetManager.getIdiotypes()[j].sum_n / (double) idnetManager.gett();
            double p0 = 1 - p1;
            if (p0 != 0 && p1 != 0) {
                S += p1 * Math.log(p1) + p0 * Math.log(p0);
            }
        }
        S = -S / Math.log(2) / (1 << idnetManager.getd());
        return S;
    }

    private static Antigen insertAntigen(IdnetManager idnetManager) {
        int a = 0;
        DeterminantBits detBits = idnetManager.calcDeterminantBits();
        int m = 1;
        for (int i = 0; i < (1 << idnetManager.getd()); i++) {
            if (Helper.hammingWeight(i & detBits.mask) == 1) {
                m = i;
                break;
            }
        }
        a = detBits.values ^ m;
        return new Antigen(idnetManager, new int[]{a});
    }

    private static void testAntigen() throws Exception {
        Antigen antigen = null;
        idnetManager.setStatCenterOfGravity(true);
        idnetManager.setStatNeighbourOccupations(false);

        idnetManager.setd(Integer.parseInt(config.getProperty("d", "12")));
        idnetManager.setp(Double.parseDouble(getConfigProperty("p")));
        idnetManager.setmax_s(Double.parseDouble(getConfigProperty("max_s")));
        idnetManager.sett_l(Double.parseDouble(getConfigProperty("t_l")));
        idnetManager.sett_u(Double.parseDouble(getConfigProperty("t_u")));
        for (int i = 0; i < idnetManager.getd(); i++) {
            idnetManager.setLinkWeighting(i, Double.parseDouble(config.getProperty("lw" + i, "0")));
        }

        int wi = 0;
        int wo = 0;
        int wi_to_wo = 0;
        int wo_to_wi = 0;

        final int max = Integer.parseInt(getConfigProperty("max"));
        final int wait = Integer.parseInt(getConfigProperty("wait"));
        final int wait2 = Integer.parseInt(getConfigProperty("wait2"));
        final boolean enableAntigen = Boolean.parseBoolean(getConfigProperty("enable_antigen"));

        System.out.println("\nmax=" + max + "; wait=" + wait + "; wait2=" + wait2 + "; antigen " + (enableAntigen ? "enabled" : "disabled"));
        for (int i = 0; i < 100; i++) {
            System.out.print("#");
        }
        System.out.println();
        for (int i = 0; i < max; i++) {
            idnetManager.reseed(System.currentTimeMillis());
            idnetManager.reset();
            idnetManager.iterate(wait);
            if (Helper.hammingWeight(idnetManager.calcDeterminantBits().mask) == 1) {
                if (calcS(idnetManager) > 0.5) {
                    wo++;
                    if (wait2 > 0) {
                        if (enableAntigen) {
                            antigen = insertAntigen(idnetManager);
                        }
                        idnetManager.iterate(wait2);
                        if (Helper.hammingWeight(idnetManager.calcDeterminantBits().mask) == 1) {
                            if (calcS(idnetManager) <= 0.5) {
                                wo_to_wi++;
                            }
                        }
                        if (antigen != null) {
                            antigen.kill();
                        }
                    }
                } else {
                    wi++;
                    if (wait2 > 0) {
                        idnetManager.iterate(wait2);
                        if (Helper.hammingWeight(idnetManager.calcDeterminantBits().mask) == 1) {
                            if (calcS(idnetManager) > 0.5) {
                                wi_to_wo++;
                            }
                        }
                    }
                }
            }
            if (i % (max / 100) == 0) {
                System.out.print("#");
            }
        }
        System.out.println("\nFrom " + max + " simulations: " + wi + " with, " + wo + " without subpattern");
        System.out.println(wi_to_wo + " turned from with to without");
        System.out.println(wo_to_wi + " turned from without to with");
    }

    private static void testAntigen2() throws Exception {
        Antigen antigen = null;
        idnetManager.setStatCenterOfGravity(true);
        idnetManager.setStatNeighbourOccupations(false);

        idnetManager.setd(Integer.parseInt(config.getProperty("d", "12")));
        idnetManager.setp(Double.parseDouble(getConfigProperty("p")));
        idnetManager.setmax_s(Double.parseDouble(getConfigProperty("max_s")));
        idnetManager.sett_l(Double.parseDouble(getConfigProperty("t_l")));
        idnetManager.sett_u(Double.parseDouble(getConfigProperty("t_u")));
        for (int i = 0; i < idnetManager.getd(); i++) {
            idnetManager.setLinkWeighting(i, Double.parseDouble(config.getProperty("lw" + i, "0")));
        }

        int wi = 0;
        int wo = 0;
        int wi_to_wo = 0;
        int wo_to_wi = 0;

        final int max = Integer.parseInt(getConfigProperty("max"));
        final int wait = Integer.parseInt(getConfigProperty("wait"));
        final int wait2 = Integer.parseInt(getConfigProperty("wait2"));
        final int wait3 = Integer.parseInt(getConfigProperty("wait3"));
        final boolean enableAntigen = Boolean.parseBoolean(getConfigProperty("enable_antigen"));

        System.out.println("\nmax=" + max + "; wait=" + wait + "; wait2=" + wait2 + "; antigen " + (enableAntigen ? "enabled" : "disabled") + "; wait3=" + wait3);
        for (int i = 0; i < 100; i++) {
            System.out.print("#");
        }
        System.out.println();
        for (int i = 0; i < max; i++) {
            idnetManager.reseed(System.currentTimeMillis());
            idnetManager.reset();
            idnetManager.iterate(wait);
            idnetManager.recalc();
            idnetManager.iterate(wait3);
            if (Helper.hammingWeight(idnetManager.calcDeterminantBits().mask) == 1) {
                if (calcS(idnetManager) > 0.5) {
                    wo++;
                    if (wait2 > 0) {
                        if (enableAntigen) {
                            antigen = insertAntigen(idnetManager);
                        }
                        idnetManager.iterate(wait2);
                        idnetManager.recalc();
                        idnetManager.iterate(wait3);
                        if (Helper.hammingWeight(idnetManager.calcDeterminantBits().mask) == 1) {
                            if (calcS(idnetManager) <= 0.5) {
                                wo_to_wi++;
                            }
                        }
                        if (antigen != null) {
                            antigen.kill();
                        }
                    }
                } else {
                    wi++;
                    if (wait2 > 0) {
                        idnetManager.iterate(wait2);
                        idnetManager.recalc();
                        idnetManager.iterate(wait3);
                        if (Helper.hammingWeight(idnetManager.calcDeterminantBits().mask) == 1) {
                            if (calcS(idnetManager) > 0.5) {
                                wi_to_wo++;
                            }
                        }
                    }
                }
            }
            if (i % (max / 100) == 0) {
                System.out.print("#");
            }
        }
        System.out.println("\nFrom " + max + " simulations: " + wi + " with, " + wo + " without subpattern");
        System.out.println(wi_to_wo + " turned from with to without");
        System.out.println(wo_to_wi + " turned from without to with");
    }

    private static void testDecay() throws Exception {
        final int max = Integer.parseInt(getConfigProperty("max"));
        final int wait = Integer.parseInt(getConfigProperty("wait"));
        final int num_loops = Integer.parseInt(getConfigProperty("num_loops"));
        final boolean enableAntigen = Boolean.parseBoolean(getConfigProperty("enable_antigen"));

        System.out.println("#antigen " + (enableAntigen ? "enabled" : "disabled") + " max=" + max + " wait=" + wait);

        IdnetManager[] idnetManager = new IdnetManager[max];

        for (int i = 0; i < max; i++) {
            idnetManager[i] = new IdnetManager();
            idnetManager[i].setStatCenterOfGravity(true);
            idnetManager[i].setStatNeighbourOccupations(false);

            idnetManager[i].setd(Integer.parseInt(config.getProperty("d", "12")));
            idnetManager[i].setp(Double.parseDouble(getConfigProperty("p")));
            idnetManager[i].setmax_s(Double.parseDouble(getConfigProperty("max_s")));
            idnetManager[i].sett_l(Double.parseDouble(getConfigProperty("t_l")));
            idnetManager[i].sett_u(Double.parseDouble(getConfigProperty("t_u")));
            for (int j = 0; j < idnetManager[i].getd(); j++) {
                idnetManager[i].setLinkWeighting(j, Double.parseDouble(config.getProperty("lw" + j, "0")));
            }
            do {
                idnetManager[i].reseed(System.currentTimeMillis());
                idnetManager[i].reset();
                idnetManager[i].iterate(wait);
            } while (!(Helper.hammingWeight(idnetManager[i].calcDeterminantBits().mask) == 1 && calcS(idnetManager[i]) > 0.5));
            if (enableAntigen) {
                insertAntigen(idnetManager[i]);
            }
            System.out.println("#prepared " + i);
            System.out.flush();
        }

        for (int loop = 0; loop < num_loops; loop++) {
            int num = 0;
            for (int i = 0; i < max; i++) {
                idnetManager[i].iterate();
                if (Helper.hammingWeight(idnetManager[i].calcDeterminantBits().mask) == 1 && calcS(idnetManager[i]) > 0.5) {
                    num++;
                }
            }
            System.out.println(loop + " " + num);
        }
    }

    private static void testDecay2() throws Exception {
        final int max = Integer.parseInt(getConfigProperty("max"));
        final int wait = Integer.parseInt(getConfigProperty("wait"));
        final int wait2 = Integer.parseInt(getConfigProperty("wait2"));
        final int num_loops = Integer.parseInt(getConfigProperty("num_loops"));
        final boolean enableAntigen = Boolean.parseBoolean(getConfigProperty("enable_antigen"));
        final boolean secondAntigen = (config.getProperty("close_antigen") != null);
        final boolean closeAntigen = Boolean.parseBoolean(config.getProperty("close_antigen", "false"));

        System.out.println("#antigen " + (enableAntigen ? "enabled" : "disabled") + " max=" + max + " wait=" + wait + " wait2=" + wait2);
        if (secondAntigen)
            System.out.println("#second antigen " + (closeAntigen ? "close" : "distant") + " antigen (in hamming distance)");

        IdnetManager[] idnetManager = new IdnetManager[max];

        for (int i = 0; i < max; i++) {
            idnetManager[i] = new IdnetManager();
            idnetManager[i].setStatCenterOfGravity(true);
            idnetManager[i].setStatNeighbourOccupations(false);

            idnetManager[i].setd(Integer.parseInt(config.getProperty("d", "12")));
            idnetManager[i].setp(Double.parseDouble(getConfigProperty("p")));
            idnetManager[i].setmax_s(Double.parseDouble(getConfigProperty("max_s")));
            idnetManager[i].sett_l(Double.parseDouble(getConfigProperty("t_l")));
            idnetManager[i].sett_u(Double.parseDouble(getConfigProperty("t_u")));
            for (int j = 0; j < idnetManager[i].getd(); j++) {
                idnetManager[i].setLinkWeighting(j, Double.parseDouble(config.getProperty("lw" + j, "0")));
            }
            do {
                idnetManager[i].reseed(System.currentTimeMillis());
                idnetManager[i].reset();
                idnetManager[i].iterate(wait);
                idnetManager[i].recalc();
                idnetManager[i].iterate(wait2);
            } while (!(Helper.hammingWeight(idnetManager[i].calcDeterminantBits().mask) == 1 && calcS(idnetManager[i]) > 0.5));
            if (enableAntigen) {
                int a = 0;
                DeterminantBits detBits = idnetManager[i].calcDeterminantBits();
                a = detBits.values ^ detBits.mask;
                new Antigen(idnetManager[i], new int[]{a});
                if (secondAntigen) {
                    if (closeAntigen) {
                        if (detBits.mask == 1)
                            a = a ^ 2;
                        else
                            a = a ^ 1;
                    } else {
                        a = ~a & ((1 << idnetManager[i].getd()) - 1);
                        a = a ^ detBits.mask;
                    }
                    new Antigen(idnetManager[i], new int[]{a});
                }
            }
            System.out.println("#prepared " + i);
            System.out.flush();
        }

        for (int loop = 0; loop < num_loops; loop++) {
            int num = 0;
            for (int i = 0; i < max; i++) {
                idnetManager[i].recalc();
                idnetManager[i].iterate(wait2);
                if (Helper.hammingWeight(idnetManager[i].calcDeterminantBits().mask) == 1 && calcS(idnetManager[i]) > 0.5) {
                    num++;
                }
            }
            System.out.println((loop + 1) * wait2 + " " + num);
        }
    }

    private static void patternDecay() throws Exception {
        final int d_m = Integer.parseInt(getConfigProperty("d_m"));
        final int max = Integer.parseInt(getConfigProperty("max"));
        final int wait = Integer.parseInt(getConfigProperty("wait"));
        final int num_loops = Integer.parseInt(getConfigProperty("num_loops"));
        final int d = Integer.parseInt(config.getProperty("d", "12"));

        System.out.println("#d_m=" + d_m + " max=" + max + " wait=" + wait);
        System.out.println(
                "#d=" + Integer.parseInt(config.getProperty("d", "12"))
                + " p=" + Double.parseDouble(getConfigProperty("p"))
                + " max_s=" + Double.parseDouble(getConfigProperty("max_s"))
                + " t_l=" + Double.parseDouble(getConfigProperty("t_l"))
                + " t_u=" + Double.parseDouble(getConfigProperty("t_u")));

        IdnetManager[] idnetManager = new IdnetManager[max];

        for (int i = 0; i < max; i++) {
            idnetManager[i] = new IdnetManager();
            idnetManager[i].setStatCenterOfGravity(true);
            idnetManager[i].setStatNeighbourOccupations(false);

            idnetManager[i].setd(Integer.parseInt(config.getProperty("d", "12")));
            idnetManager[i].setp(Double.parseDouble(getConfigProperty("p")));
            idnetManager[i].setmax_s(Double.parseDouble(getConfigProperty("max_s")));
            idnetManager[i].sett_l(Double.parseDouble(getConfigProperty("t_l")));
            idnetManager[i].sett_u(Double.parseDouble(getConfigProperty("t_u")));
            for (int j = 0; j < idnetManager[i].getd(); j++) {
                idnetManager[i].setLinkWeighting(j, Double.parseDouble(config.getProperty("lw" + j, "0")));
            }
            if (d_m == 4) {
                do {
                    idnetManager[i].reset();
                    idnetManager[i].reseed(System.currentTimeMillis());
                    int mask = (1 << d_m) - 1;
                    for (int j = 0; j < (1 << d); j++) {
                        if (Helper.hammingWeight(j & mask) == 1) {
                            idnetManager[i].getIdiotypes()[j].n = 1;
                        }
                    }
                    idnetManager[i].iterate(wait);
                } while (Helper.hammingWeight(idnetManager[i].calcDeterminantBits().mask) != d_m);
            } else {
                do {
                    idnetManager[i].reseed(System.currentTimeMillis());
                    idnetManager[i].reset();
                    idnetManager[i].iterate(wait);
                } while (Helper.hammingWeight(idnetManager[i].calcDeterminantBits().mask) != d_m);
            }
            System.out.println("#prepared " + i);
            System.out.flush();
        }

        for (int loop = 0; loop < num_loops; loop++) {
            int num = 0;
            for (int i = 0; i < max; i++) {
                idnetManager[i].iterate();
                if (Helper.hammingWeight(idnetManager[i].calcDeterminantBits().mask) == d_m) {
                    num++;
                }
            }
            System.out.println(loop + " " + num);
        }
    }

    private static void entropyHistogram() throws Exception {
        final int max = Integer.parseInt(getConfigProperty("max"));
        final int wait = Integer.parseInt(getConfigProperty("wait"));
        final int wait2 = Integer.parseInt(getConfigProperty("wait2"));
        final int wait3 = Integer.parseInt(getConfigProperty("wait3"));
        final int wait4 = Integer.parseInt(getConfigProperty("wait4"));
        final boolean enableAntigen = Boolean.parseBoolean(getConfigProperty("enable_antigen"));

        System.out.println("#antigen " + (enableAntigen ? "enabled" : "disabled") + " max=" + max + " wait=" + wait + " wait2=" + wait2);

        idnetManager = new IdnetManager();
        idnetManager.setStatCenterOfGravity(true);
        idnetManager.setStatNeighbourOccupations(false);

        idnetManager.setd(Integer.parseInt(config.getProperty("d", "12")));
        idnetManager.setp(Double.parseDouble(getConfigProperty("p")));
        idnetManager.setmax_s(Double.parseDouble(getConfigProperty("max_s")));
        idnetManager.sett_l(Double.parseDouble(getConfigProperty("t_l")));
        idnetManager.sett_u(Double.parseDouble(getConfigProperty("t_u")));
        for (int j = 0; j < idnetManager.getd(); j++) {
            idnetManager.setLinkWeighting(j, Double.parseDouble(config.getProperty("lw" + j, "0")));
        }

        for (int i = 0; i < max; i++) {
            do {
                idnetManager.reseed(System.currentTimeMillis());
                idnetManager.reset();
                idnetManager.iterate(wait);
                idnetManager.recalc();
                idnetManager.iterate(wait2);
            } while (!(Helper.hammingWeight(idnetManager.calcDeterminantBits().mask) == 1 && calcS(idnetManager) > 0.5));
            if (enableAntigen) {
                insertAntigen(idnetManager);
            }
            idnetManager.iterate(wait3);
            idnetManager.recalc();
            idnetManager.iterate(wait4);
            System.out.println(i + " " + calcS(idnetManager));
            System.out.flush();
        }
    }

    private static String getConfigProperty(String name) throws Exception {
        if (config.getProperty(name) == null) {
            throw new Exception("Configuration property '" + name + "' missing");
        }
        return config.getProperty(name);
    }

    private static void createCOGDiagram() throws Exception {
        idnetManager.setp(Double.parseDouble(getConfigProperty("p")));
        idnetManager.reseed(Long.parseLong(getConfigProperty("first_seed")));
        int tWindow = Integer.parseInt(getConfigProperty("t_window"));
        idnetManager.setStatCenterOfGravity(true);
        FileWriter fw = new FileWriter("cog_diagram.dat");
        for (int i = 0; i < tWindow; i++) {
            fw.write(i + " ");
            for (int j = 0; j < idnetManager.getd(); j++) {
                fw.write((j > 0 ? " " : "") + idnetManager.getCOG()[j]);
            }
            fw.write("\n");
            idnetManager.iterate();
        }
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
                } else {
                    result = value2.compareTo(value1);
                }

                return result;
            }
        }

        HashMap<String, Integer> possibleDetBits = new HashMap<String, Integer>();
        HashMap<String, Long> possibleDetBitsSeeds = new HashMap<String, Long>();
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
            if (possibleDetBits.containsKey(str)) {
                possibleDetBits.put(str, possibleDetBits.get(str) + 1);
            } else {
                possibleDetBits.put(str, 1);
                possibleDetBitsSeeds.put(str, seed);
            }
            System.out.println(i);
        }

        ArrayList<Map.Entry<String, Integer>> myArrayList =
                new ArrayList<Map.Entry<String, Integer>>(possibleDetBits.entrySet());
        Collections.sort(myArrayList, new MyComparator());


        for (Entry<String, Integer> e : myArrayList) {
            System.out.println(e.getKey() + " x " + e.getValue() + " (e.g. "
                    + possibleDetBitsSeeds.get(e.getKey()) + ")");
        }
    }

    private static void showLinkMatrix() throws Exception {
        int d = Integer.parseInt(getConfigProperty("d"));
        int d_m = Integer.parseInt(getConfigProperty("d_m"));
        int m = Integer.parseInt(getConfigProperty("m"));

        for (int i = 0; i <= d_m; i++) {
            for (int j = 0; j <= d_m; j++) {
                System.out.print(IdnetManager.calcLinkMatrixElem(i, j, d_m, m, d) + " ");
            }
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

                if (histogramMO != null) {
                    histogramMO[(int) ((double) node.sum_n * (double) (ySteps
                            - 1) / (double) tWindow)]++;
                }

                if (histogramON != null) {
                    if ((double) node.sum_n_d / (double) tWindow < 80.) {
                        histogramON[(int) ((double) node.sum_n_d / 80.
                                * (double) (ySteps - 1) / (double) tWindow)]++;
                    }
                }

                if (histogramLT != null) {
                    double mlt = (double) node.sum_n / (double) node.b;
                    if (mlt < 10000. && mlt >= 1) {
                        histogramLT[(int) (Math.log10(mlt) / 5.
                                * (double) (ySteps - 1))]++;
                    }
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

        if (fileNameLT != null) {
            histogramLT = new int[ySteps];
        }
        if (fileNameMO != null) {
            histogramMO = new int[ySteps];
        }
        if (fileNameON != null) {
            histogramON = new int[ySteps];
        }

        FileWriter fileWriterLT = null, fileWriterMO = null, fileWriterON = null;
        if (fileNameLT != null) {
            new FileWriter(fileNameLT, false).close();
        }
        if (fileNameMO != null) {
            new FileWriter(fileNameMO, false).close();
        }
        if (fileNameON != null) {
            new FileWriter(fileNameON, false).close();
        }

        idnetManager.setStatCenterOfGravity(false);

        for (int i_p = 0; i_p < pSteps; i_p++) {
            double p = pFrom + i_p * pTo / pSteps;
            idnetManager.setp(p);
            idnetManager.reset();
            calcHistogramStep(numLoops, tWait, tWindow, ySteps);

            if (histogramLT != null) {
                fileWriterLT = new FileWriter(fileNameLT, true);
            }
            if (histogramMO != null) {
                fileWriterMO = new FileWriter(fileNameMO, true);
            }
            if (histogramON != null) {
                fileWriterON = new FileWriter(fileNameON, true);
            }
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
            if (p == 0) {
                continue;
            }
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
                for (int i = 0; i < (1 << 12); i++) {
                    if (Helper.hammingWeight(i & mask) <= k && Math.random() < 0.5) {
                        idiotypes[i].n = 1;
                    }
                }

                idnetManager.setStatNeighbourOccupations(false);
                idnetManager.iterate(tWait);
                idnetManager.setStatNeighbourOccupations(true);

                if (waitForPattern) {
                    if (Helper.hammingWeight(idnetManager.calcDeterminantBits().mask) != d_m) {
                        continue;
                    } else {
                        idnetManager.iterate(100);
                        if (Helper.hammingWeight(idnetManager.calcDeterminantBits().mask) != d_m) {
                            continue;
                        } else {
                            idnetManager.iterate(100);
                            if (Helper.hammingWeight(idnetManager.calcDeterminantBits().mask) != d_m) {
                                continue;
                            }
                        }
                    }
                }
                for (int n = 0; n < tWindow; n++) {
                    idnetManager.iterate();
                    for (Idiotype i : idnetManager.getIdiotypes()) {
                        if (i.n > 0 && (int) i.n_d <= maxNeighbourCount) {
                            neighbourCounts[(int) i.n_d]++;
                        }
                    }
                }
                if (waitForPattern) {
                    if (Helper.hammingWeight(idnetManager.calcDeterminantBits().mask) != d_m) {
                        neighbourCounts = new int[maxNeighbourCount + 1];
                        continue;
                    }
                }
                found = true;
                break;
            }

            if (waitForPattern) {
                if (found) {
                    System.out.print("   pattern found for p=");
                } else {
                    System.out.print("no pattern found for p=");
                }
            }

            fw = new FileWriter(getConfigProperty("filename_topology_histogram"), true);
            fw.write("#seed " + seed + "\n");

            for (int n = 0; n <= maxNeighbourCount; n++) {
                fw.write(p + " " + n + " " + neighbourCounts[n] + "\n");
            }

            fw.write("\n");
            fw.close();

            System.out.println(p);
        }
    }
}

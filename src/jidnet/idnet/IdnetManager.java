package jidnet.idnet;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.Random;

/**
 *
 * @author sven
 */
public class IdnetManager extends IdiotypicNetwork {

    private Properties params;
    private int[] histogramMO, histogramLT, histogramON;
    private double[][] cogWindow;
    private int cogWindowSize = 100;
    private double max_s;

    public class DeterminantBits {

        public int mask = 0;
        public int values = 0;

    }

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

    public void loadParams(String fileName) throws FileNotFoundException,
                                                   IOException {
        params.loadFromXML(new FileInputStream(fileName));
        this.setp(Double.parseDouble(params.getProperty("p")));
        this.sett_l(Integer.parseInt(params.getProperty("t_l")));
        this.sett_u(Integer.parseInt(params.getProperty("t_u")));
        this.setN(Integer.parseInt(params.getProperty("N")));
        this.setMax_s(Double.parseDouble(params.getProperty("max_s")));
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

    public void saveParams(String fileName) throws IOException {
        params.setProperty("d", Integer.toString(this.getd()));
        params.setProperty("p", Double.toString(this.getp()));
        params.setProperty("t_l", Integer.toString(this.gett_l()));
        params.setProperty("t_u", Integer.toString(this.gett_u()));
        params.setProperty("N", Integer.toString(this.getN()));
        params.setProperty("max_s", Double.toString(this.getMax_s()));
        params.setProperty("lw0", Double.toString(linkWeighting[0]));
        params.setProperty("lw1", Double.toString(linkWeighting[1]));
        params.setProperty("lw2", Double.toString(linkWeighting[2]));
        params.setProperty("lw3", Double.toString(linkWeighting[3]));
        params.setProperty("lw4", Double.toString(linkWeighting[4]));
        params.setProperty("lw5", Double.toString(linkWeighting[5]));
        params.setProperty("lw6", Double.toString(linkWeighting[6]));
        params.setProperty("lw7", Double.toString(linkWeighting[7]));
        params.setProperty("lw8", Double.toString(linkWeighting[8]));
        params.setProperty("lw9", Double.toString(linkWeighting[9]));
        params.setProperty("lw10", Double.toString(linkWeighting[10]));
        params.setProperty("lw11", Double.toString(linkWeighting[11]));

        params.storeToXML(new FileOutputStream(fileName),
                "Idiotypic network parameters");
    }

    public void reseed(long seed) {
        rng.setSeed(seed);
    }

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
                    if ((double) this.getSum_n() / (double) tWindow < 80.)
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

    @Override
    public void iterate() {
        super.iterate();
        for (int i = 0; i < 12; i++)
            cogWindow[t % cogWindowSize][i] = cog[i];
    }

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
            // TODO : Parameter
        }
        return result;
    }

    public double getMax_s() {
        return max_s;
    }

    public void setMax_s(double detBitsMaxDeviation) {
        this.max_s = detBitsMaxDeviation;
    }

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

}

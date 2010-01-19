package jidnet;

import java.io.FileInputStream;
import java.util.Properties;
import jidnet.idnet.IdnetManager;

/**
 *
 * @author Sven Willner
 */
public class Main {

    public static void main(String[] args) throws Exception {
        IdnetManager idnetManager = new IdnetManager();

        Properties config = new Properties();
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

            if (config.getProperty("action").equals("histogram"))
                idnetManager.createHistogram("histogram.xml");
            else
                System.out.println("Action unknown");

            long t1 = System.currentTimeMillis();
            System.out.println("Time needed: " + (t1 - t0) / 60000 + "min");
        }
    }

}

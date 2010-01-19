package jidnet.gui;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import jidnet.idnet.IdnetManager;

/**
 *
 * @author Sven Willner
 */
public final class Application {

    private static IdnetManager idnetManager;
    private static MainWindow mainWindow;
    private static Properties config;
    private static Vector<Properties> configurations;

    public static void main(String args[]) {

        idnetManager = new IdnetManager();
        config = new Properties();

        try {
            idnetManager.loadParams("params.xml");
        } catch (Exception e) {
            //
        }

        try {
            config.loadFromXML(new FileInputStream("config.xml"));
        } catch (Exception e) {
            //
        }

        try {
            loadConfigurations("configs.dat");
        } catch (Exception e) {
            configurations = new Vector<Properties>();
        }

        try {
            java.awt.EventQueue.invokeAndWait(new Runnable() {

                public void run() {
                    try {
                        // Set System L&F
                        UIManager.setLookAndFeel(
                                UIManager.getSystemLookAndFeelClassName());
                    } catch (UnsupportedLookAndFeelException e) {
                        // handle exception
                    } catch (ClassNotFoundException e) {
                        // handle exception
                    } catch (InstantiationException e) {
                        // handle exception
                    } catch (IllegalAccessException e) {
                        // handle exception
                    }

                    mainWindow = new MainWindow();
                    mainWindow.setState(JFrame.MAXIMIZED_BOTH);
                    mainWindow.setVisible(true);
                }

            });
        } catch (InterruptedException e1) {
        } catch (InvocationTargetException e2) {
        }

    }

    private static void loadConfigurations(String fileName) throws Exception {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName));
        configurations = (Vector) ois.readObject();
    }

    private static void saveConfigurations(String fileName) throws Exception {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName));
        oos.writeObject(configurations);
        oos.close();
    }

    public static void closing() {
        try {
            idnetManager.saveParams("params.xml");
        } catch (Exception ex) {
            //
        }

        try {
            config.storeToXML(new FileOutputStream("config.xml"), "Configuration of jIdNet Application");
        } catch (Exception e) {
            //
        }
        try {
            saveConfigurations("configs.dat");
        } catch (Exception e) {
            //
        }

    }

    public static IdnetManager getIdnetManager() {
        return idnetManager;
    }

    public static Properties getConfiguration() {
        return config;
    }

    public static Vector<Properties> getConfigurations() {
        return configurations;
    }

}

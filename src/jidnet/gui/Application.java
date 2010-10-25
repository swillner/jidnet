package jidnet.gui;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Properties;
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
    private static ArrayList<Properties> configurations;

    public static void main(String args[]) {

        idnetManager = new IdnetManager();
        config = new Properties();

        try {
            idnetManager.loadParams("jIdNet/params.xml");
        } catch (Exception e) {
            //
        }

        try {
            config.loadFromXML(new FileInputStream("jIdNet/config.xml"));
        } catch (Exception e) {
            //
        }

        try {
            loadConfigurations("jIdNet/configs.dat");
        } catch (Exception e) {
            configurations = new ArrayList<Properties>();
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
        configurations = (ArrayList) ois.readObject();
    }

    private static void saveConfigurations(String fileName) throws Exception {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName));
        oos.writeObject(configurations);
        oos.close();
    }

    public static void closing() {
        try {
            idnetManager.saveParams("jIdNet/params.xml");
        } catch (Exception ex) {
            //
        }

        try {
            config.storeToXML(new FileOutputStream("jIdNet/config.xml"), "Configuration of jIdNet Application");
        } catch (Exception e) {
            //
        }
        try {
            saveConfigurations("jIdNet/configs.dat");
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

    public static ArrayList<Properties> getConfigurations() {
        return configurations;
    }

    public static String getClipboardContents() {
        String result = "";
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        //odd: the Object param of getContents is not currently used
        Transferable contents = clipboard.getContents(null);
        boolean hasTransferableText =
                (contents != null)
                && contents.isDataFlavorSupported(DataFlavor.stringFlavor);
        if (hasTransferableText) {
            try {
                result = (String) contents.getTransferData(DataFlavor.stringFlavor);
            } catch (UnsupportedFlavorException ex) {
                //highly unlikely since we are using a standard DataFlavor
                System.out.println(ex);
                ex.printStackTrace();
            } catch (IOException ex) {
                System.out.println(ex);
                ex.printStackTrace();
            }
        }
        return result;
    }
}

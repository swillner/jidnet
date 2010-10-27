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

    public static void main(String args[]) {

        idnetManager = new IdnetManager();
        config = new Properties();

        try {
            idnetManager.loadStartConfiguration("jIdNet/default.conf");
        } catch (Exception e) {
            //
        }

        try {
            config.loadFromXML(new FileInputStream("jIdNet/jIdNet.xml"));
        } catch (Exception e) {
            //
        }
        idnetManager.setmax_s(Double.parseDouble(config.getProperty("max_s", "0.04")));

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
            e1.printStackTrace();
        } catch (InvocationTargetException e2) {
            e2.printStackTrace();
        }

    }

    public static void closing() {
        try {
            idnetManager.saveStartConfiguration("jIdNet/default.conf", "Last configuration");
        } catch (Exception ex) {
            //
        }

        try {
            config.setProperty("max_s", Double.toString(idnetManager.getmax_s()));
            config.storeToXML(new FileOutputStream("jIdNet/jIdNet.xml"), "Configuration of jIdNet Application");
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

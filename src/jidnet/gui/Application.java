package jidnet.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import javax.swing.JFrame;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import jidnet.idnet.IdnetManager;
import jidnet.idnet.IdnetManager;

/**
 *
 * @author sven
 */
public final class Application {

    private static IdnetManager idnetManager;
    private static MainWindow mainWindow;

    public static IdnetManager getIdiotypicNetwork() {
        return idnetManager;
    }

    public static IdnetManager getIdnetManager() {
        return idnetManager;
    }

    public static void main(String args[]) {

        idnetManager = new IdnetManager();

        try {
            idnetManager.loadParams("params.xml");
        } catch (Exception e) {
            //
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

    public static void closing() {
        try {
            idnetManager.saveParams("params.xml");
        } catch (Exception ex) {
            //
        }
    }
}

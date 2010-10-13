/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jidnet;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import jidnet.gui.Network2DPanel;
import jidnet.idnet.DeterminantBits;
import jidnet.idnet.Helper;
import jidnet.idnet.IdnetManager;

/**
 *
 * @author sven
 */
public class CreateVideo {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        IdnetManager idnetManager = new IdnetManager();
        try {
            idnetManager.loadParams("jIdNet/params.xml");
        } catch (Exception e) {
            //
        }
        Network2DPanel panel = new Network2DPanel(idnetManager);
        //panel.arrangeByDetBitGroups(new DeterminantBits(Integer.parseInt("000011100001", 2), Integer.parseInt("000001000000", 2)));
        panel.setOffset(0, 0);
        for (int j = 0; j <= 500; j++) {
            idnetManager.iterate(5);
            String fileName = "video/v" + (j < 100 ? "0" : "") + (j < 10 ? "0" : "") + j + ".png";
            try {
                BufferedImage i = new BufferedImage(640, 640, BufferedImage.TYPE_INT_ARGB);
                panel.paintComponent(i.createGraphics());
                ImageIO.write(i, "png", new File(fileName));
            } catch (IOException io) {
                io.printStackTrace();
            }
        }
    }
}

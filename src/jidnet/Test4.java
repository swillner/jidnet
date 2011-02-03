/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jidnet;

import java.io.FileOutputStream;
import java.util.Properties;
import jidnet.idnet.Antigen;
import jidnet.idnet.DeterminantBits;
import jidnet.idnet.Helper;
import jidnet.idnet.IdnetManager;

/**
 *
 * @author sven
 */
public class Test4 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        IdnetManager idnetManager = new IdnetManager();
        idnetManager.setp(0.06);
        idnetManager.sett_l(2.75);
        idnetManager.setStatCenterOfGravity(true);
        idnetManager.setStatNeighbourOccupations(false);
        idnetManager.setLinkWeighting(0, 1);
        idnetManager.setLinkWeighting(1, 0.5);
        idnetManager.setLinkWeighting(2, 0.25);
        idnetManager.setLinkWeighting(3, 0.005);
        long seed;

        idnetManager.reset();
        idnetManager.reseed(1291217179939L);
        idnetManager.iterate(500);

        DeterminantBits detBits = idnetManager.calcDeterminantBits();
        Antigen ag = new Antigen(idnetManager, new int[]{detBits.values ^ detBits.mask});
        idnetManager.recalc();

        System.out.println("##########");
        for (int i = 0; i < 10; i++) {
            idnetManager.iterate(10000);
            System.out.print(".");
        }
        System.out.println();

        Properties p = new Properties();
        for (int i = 0; i < (1 << idnetManager.getd()); i++) {
            p.setProperty(""+i, ""+(double) idnetManager.getIdiotypes()[i].sum_n / (double) idnetManager.gett());
        }
        try {
            p.storeToXML(new FileOutputStream("ag6stat.txt"), "");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}

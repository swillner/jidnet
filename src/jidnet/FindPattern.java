/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jidnet;

import jidnet.idnet.Helper;
import jidnet.idnet.IdnetManager;

/**
 *
 * @author sven
 */
public class FindPattern {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        IdnetManager idnetManager = new IdnetManager();
        idnetManager.setp(0.02);
        idnetManager.sett_l(1);
        idnetManager.setStatCenterOfGravity(true);
        idnetManager.setStatNeighbourOccupations(false);
        idnetManager.setLinkWeighting(0, 1);
        idnetManager.setLinkWeighting(1, 1);
        idnetManager.setLinkWeighting(2, 1);
        idnetManager.setmax_s(0.08);
        long seed;

        while (true) {
            idnetManager.reset();
            seed = System.currentTimeMillis();
            idnetManager.reseed(seed);
            idnetManager.iterate(500);
            if (Helper.hammingWeight(idnetManager.calcDeterminantBits().mask) == 4) {
                continue;
            }
            idnetManager.iterate(500);
            //System.out.println(Helper.hammingWeight(idnetManager.calcDeterminantBits().mask) + " : " + seed);
            if (Helper.hammingWeight(idnetManager.calcDeterminantBits().mask) == 6) {
                System.out.println(seed);
                //break;
            }
        }
    }
}

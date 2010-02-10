/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jidnet;

import jidnet.idnet.IdnetManager;

/**
 *
 * @author Sven Willner
 */
public class Test {

    public static void main(String[] args) throws Exception {
        int d = 12;
        int d_m = 11;
        int m = 2;

        for (int i = 0; i <= d_m; i++) {
            for (int j = 0; j <= d_m; j++)
                System.out.print(IdnetManager.calcLinkMatrixElem(i, j, d_m, m, d) + " ");
            System.out.println();
        }
    }

}

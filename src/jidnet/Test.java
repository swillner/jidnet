/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jidnet;

import java.io.FileWriter;
import jidnet.idnet.Helper;
import jidnet.idnet.Idiotype;
import jidnet.idnet.IdnetManager;

/**
 *
 * @author Sven Willner
 */
public class Test {

    public static void main(String[] args) throws Exception {
        /*        int d = 12;
        int d_m = 11;
        int m = 2;

        for (int i = 0; i <= d_m; i++) {
        for (int j = 0; j <= d_m; j++)
        System.out.print(IdnetManager.calcLinkMatrixElem(i, j, d_m, m, d) + " ");
        System.out.println();
        }*/

        IdnetManager idnetManager = new IdnetManager();

        int P_STEPS = 100;
        int MAX_NEIGHBOUR_COUNT = 11;
        int T_TO_STATIC = 1000;
        int T_WINDOW = 500;
        int[] neighbourCounts;

        FileWriter fw = new FileWriter("topology.dat");

        long t0 = System.currentTimeMillis();

        for (int j = 1; j < P_STEPS; j++) {
            double p = (double)j * 0.1 / (double) P_STEPS;
            idnetManager.setp(p);
            idnetManager.reset();

            neighbourCounts = new int[MAX_NEIGHBOUR_COUNT + 1];

            idnetManager.setStatNeighbourOccupations(false);
            idnetManager.iterate(T_TO_STATIC);
            idnetManager.setStatNeighbourOccupations(true);

            for (int n = 0; n < T_WINDOW; n++)
                for (Idiotype i : idnetManager.getIdiotypes())
                    if (i.n > 0 && (int) i.n_d <= MAX_NEIGHBOUR_COUNT)
                        neighbourCounts[(int) i.n_d]++;

            for (int n = 0; n <= MAX_NEIGHBOUR_COUNT; n++)
                fw.write(p + " " + n + " " + neighbourCounts[n] + "\n");

            fw.write("\n");
            fw.flush();

            System.out.println(p);
        }

        fw.close();

        long t1 = System.currentTimeMillis();
        System.out.println("Time needed: " + (t1 - t0) / 60000 + "min");
    }

}


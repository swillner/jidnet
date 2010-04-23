/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jidnet;

import java.util.Vector;
import jidnet.idnet.Helper;
import jidnet.idnet.Idiotype;
import jidnet.idnet.IdnetManager;

/**
 *
 * @author Sven Willner
 */
public class Test {

    public static void main(String[] args) throws Exception {

        IdnetManager idnetManager = new IdnetManager();

        idnetManager.setp(0.025);
        idnetManager.setmax_s(0.04);


        for (int i = 0; i < 10000; i++) {
            long seed = System.currentTimeMillis();
            idnetManager.reseed(seed);
            idnetManager.reset();
            for (int j = 0; j < 1; j++) {
                idnetManager.iterate(1000);
                //int dm = Helper.hammingWeight(idnetManager.calcDeterminantBits().mask);
                //if (dm <= 4)
                //    break;
                //else {
                    Vector<Vector<Idiotype>> clusters = idnetManager.calcClusters();
                    for (Vector<Idiotype> cluster : clusters)
                        if (cluster.size() == 24) {
                            System.out.println(seed);
                            break;
                        }
                //}
            }
        }
        
        /*
        IdnetManager idnetManager = new IdnetManager();

        idnetManager.setp(0.015);
        idnetManager.setmax_s(0.04);
        idnetManager.loadNetwork("1.dat");
        idnetManager.reseed(1267623876539L);
        idnetManager.iterate();

        DeterminantBits detBits = new DeterminantBits(Integer.parseInt("011001100000", 2), Integer.parseInt(
                "011001100000", 2));

        int d_m = d_m = Helper.hammingWeight(detBits.mask);
        final int MAX_NEIGHBOUR_COUNT = 80;
        int[] neighbourCounts = new int[MAX_NEIGHBOUR_COUNT + 1];
        int[] neighbourCountsOccupied = new int[MAX_NEIGHBOUR_COUNT + 1];
        int[][] groupNeighbourCounts = new int[d_m + 1][MAX_NEIGHBOUR_COUNT + 1];
        int[][] bitwiseNeighbourCounts = new int[12][MAX_NEIGHBOUR_COUNT + 1];

        int max = 1;
        for (Idiotype i : idnetManager.getIdiotypes())
            if ((int) i.n_d <= MAX_NEIGHBOUR_COUNT) {
                neighbourCounts[(int) i.n_d]++;
                if (i.n > 0)
                    neighbourCountsOccupied[(int) i.n_d]++;
                if (detBits != null)
                    groupNeighbourCounts[Helper.hammingWeight((detBits.mask & i.i) ^ detBits.values)][(int) i.n_d]++;
                for (int j = 0; j < 12; j++)
                    if ((i.i & (1 << j)) != 0)
                        bitwiseNeighbourCounts[j][(int) i.n_d]++;
                max = Math.max(max, neighbourCounts[(int) i.n_d]);
            }
        for (int l = 0; l < 12; l++) {
            System.out.print(l + "\t");
            //System.out.print(idnetManager.calcGroupSize(l, d_m) + "\t");

            int ton = 0;
            for (int i = 0; i <= MAX_NEIGHBOUR_COUNT; i++)
                ton += bitwiseNeighbourCounts[l][i];
            //System.out.print(ton + "\t");

            int ton_w = 0;
            for (int i = 1; i <= 10; i++)
                ton_w += bitwiseNeighbourCounts[l][i];
            //System.out.print(ton_w + "\t");

            System.out.print((double) ton_w / (double) ton + "\n");
        }
/*
        //System.out.println("Group\tNodes\tTotal ON in [1;10]\tRatio\n");
        //System.out.print("Ist:\n");
        for (int l = 0; l <= d_m; l++) {
            System.out.print("S_" + l + "\t");
            //System.out.print(idnetManager.calcGroupSize(l, d_m) + "\t");

            int ton = 0;
            for (int i = 0; i <= MAX_NEIGHBOUR_COUNT; i++)
                ton += groupNeighbourCounts[l][i];
            //System.out.print(ton + "\t");

            int ton_w = 0;
            for (int i = 1; i <= 10; i++)
                ton_w += groupNeighbourCounts[l][i];
            //System.out.print(ton_w + "\t");

            System.out.print((double) ton_w / (double) ton + "\n");
        }
*/
       /* idnetManager.reseed(1267631750048L);
        System.out.print("\nBei d_M=4 im Mittel:\n");
        max = 1;
        idnetManager.iterate(1000);
        for (int l = 0; l < 1000; l++) {
            idnetManager.iterate();
        for (Idiotype i : idnetManager.getIdiotypes())
            if ((int) i.n_d <= MAX_NEIGHBOUR_COUNT) {
                neighbourCounts[(int) i.n_d]++;
                if (i.n > 0)
                    neighbourCountsOccupied[(int) i.n_d]++;
                if (detBits != null)
                    groupNeighbourCounts[Helper.hammingWeight((detBits.mask & i.i) ^ detBits.values)][(int) i.n_d]++;
                for (int j = 0; j < 12; j++)
                    if ((i.i & (1 << j)) != 0)
                        bitwiseNeighbourCounts[j][(int) i.n_d]++;
                max = Math.max(max, neighbourCounts[(int) i.n_d]);
            }
        }
        for (int l = 0; l <= d_m; l++) {
            System.out.print("S_" + l + "\t");
            //System.out.print(idnetManager.calcGroupSize(l, d_m) + "\t");

            int ton = 0;
            for (int i = 0; i <= MAX_NEIGHBOUR_COUNT; i++)
                ton += groupNeighbourCounts[l][i];
            //System.out.print(ton + "\t");

            int ton_w = 0;
            for (int i = 1; i <= 10; i++)
                ton_w += groupNeighbourCounts[l][i];
            //System.out.print(ton_w + "\t");

            System.out.print((double) ton_w / (double) ton + "\n");
        }*/
    }

}


/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jidnet;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.imageio.ImageIO;
import jidnet.idnet.Helper;

/**
 *
 * @author Sven Willner
 */
public class Test {

    private static int d = 12;
    private static int m = 2;

    private static void calcLinkMatrixRec(long j, long mismatchMask, int dist, int[] b, int mask) {
        if (dist >= d)
            return;
        while (mismatchMask != 0) {
            mismatchMask >>= 1;
            if (mismatchMask == 0)
                break;
            //test++;
            //System.out.println(Helper.getBitString(j ^mismatchMask) + " " + dist);
            b[(int) ((j ^ mismatchMask) & mask)]++;
            if (dist + 1 <= m)
                calcLinkMatrixRec(j ^ mismatchMask, mismatchMask, dist + 1, b, mask);
        }
    }

    private static int[] getBitVector(long v, int l) {
        int[] res = new int[l];
        for (int i = 0; i < l; i++)
            res[i] = ((1 << i) & v) == 0 ? 0 : 1;
        return res;
    }

    public static void main(String[] args) throws Exception {

        class MyComparator implements Comparator {

            public int compare(Object obj1, Object obj2) {
                int result = 0;
                Map.Entry<ArrayList<Integer>, ArrayList<Integer>> e1 = (Map.Entry<ArrayList<Integer>, ArrayList<Integer>>) obj1;
                Map.Entry<ArrayList<Integer>, ArrayList<Integer>> e2 = (Map.Entry<ArrayList<Integer>, ArrayList<Integer>>) obj2;
                ArrayList<Integer> value1 = (ArrayList<Integer>) e1.getValue();
                ArrayList<Integer> value2 = (ArrayList<Integer>) e2.getValue();

                /*int[] order = { 0, 3, 18, 36, 40 };
                for (int i = 0; i < order.length; i++) {
                if (value2.indexOf(order[i]) > value1.indexOf(order[i]))
                return 1;
                else if (value2.indexOf(order[i]) < value1.indexOf(order[i]))
                return -1;
                }*/
                for (int i = 0; i < value2.size(); i++)
                    if (value2.get(i) > value1.get(i))
                        return 1;
                    else if (value2.get(i) < value1.get(i))
                        return -1;

                //result = v2 > v1 ? 1 : -1;

                return result;
            }
        }


        int d_m = 9;

        HashMap<Long, HashMap<Integer, Integer>> stateNeighbours = new HashMap<Long, HashMap<Integer, Integer>>();
        HashMap<Long, HashMap<ArrayList<Integer>, ArrayList<Integer>>> stateVectors = new HashMap<Long, HashMap<ArrayList<Integer>, ArrayList<Integer>>>();

        int mask = (1 << d_m) - 1;

        int[] values = new int[m + 1];
        int c = 0;
        for (int i = 0; i <= m; i++) {
            c += Helper.binomial(d - d_m, i);
            values[i] = c;
        }

        int[][] b = new int[1 << d_m][1 << d_m];

        for (int i = 0; i < (1 << d_m); i++) {
            long complement = (~(long) i) & ((long) (1 << d) - 1);
            /*for (int n = 0; n < (1 << d); n++)
            if (Helper.hammingWeight(n ^ complement) <= m)
            b[i][n & mask]++;*/
            b[i][(int) (complement & mask)]++;
            calcLinkMatrixRec(complement, 1 << d, 1, b[i], mask);
            //System.out.println(i + " / " + (1 << d_m) + " done");
        }

        int[] v = new int[1 << d_m];

        String prep = "31";
        for (int j = 0; j < d_m - 4; j++) {
            String tmp = "";
            for (int i = 0; i < prep.length(); i++)
                switch (prep.charAt(i)) {
                    case '1':
                        if (i > 0 && (i % 2 == 1) && prep.charAt(i - 1) == '3')
                            tmp += "21";
                        else
                            tmp += "11";
                        break;
                    case '2':
                        tmp += "31";
                        break;
                    case '3':
                        if (i < prep.length() - 1 && (i % 2 == 0) && prep.charAt(i + 1) == '1')
                            tmp += "32";
                        else
                            tmp += "33";
                        break;
                    case 'c':
                        tmp += "c3";
                        break;
                }
            prep = tmp;
        }

        prep = "ccc" + prep.substring(3, prep.length() - 3) + "000";

        System.out.println(prep);


        //String prep = "3333"+"3221"+"3221"+"1111";
        //int[] prep = { 3,3,3,3,  3,3,3,3,  3,3,3,1, 3,1,1,1, 3,3,3,1, 3,1,1,1, 1,1,1,1, 1,1,1,1 };
        for (int i = 0; i < prep.length(); i++)
            switch (prep.charAt(i)) {
                case '1':
                    v[8 * i] = 1;
                    break;
                case '2':
                    v[8 * i] = v[8 * i + 4] = 1;
                    break;
                case '3':
                    v[8 * i + 1] = v[8 * i + 2] = v[8 * i + 4] = 1;
                    break;
                case 'c':
                    v[8 * i + 3] = v[8 * i + 5] = v[8 * i + 6] = 1;
                    break;
            }

        /*try {
            FileWriter fw = new FileWriter("dm" + d_m + "_c3.net");
            for (int i = 0; i < (1 << d); i++)
                fw.write(i + " " + v[i & ((1 << d_m) - 1)] + "\n");
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        //for (int i = 0; i < (1 << (d_m - 2)); i++)
        //    v[i] = 1;

        System.out.println(Arrays.toString(v));
        int maxbit = 0;
        while (true) {
            boolean found = false;

            int[] t = new int[1 << d_m];
            found = true;
            for (int j = 0; j < (1 << d_m); j++) {
                for (int k = 0; k < (1 << d_m); k++)
                    t[j] += b[j][k] * v[k];
                System.out.println(j + " : " + v[j] + " -> " + t[j]);
                if (t[j] >= 1 && t[j] <= 10) {
                    if (v[j] == 0) {
                        System.out.println("!!!!!!!!!!!!!!!!!!!!!!");
                        found = false;
                    }
                } else if (v[j] == 1) {
                    found = false;
                        System.out.println("!!!!!!!!!!!!!!!!!!!!!!");
                        found = false;
                    }
            }
            System.out.println(found);

            /*if (found) {
            for (int j = 0; j < (1 << (d_m - 1)); j++)
            if (v[2 * j] != v[2 * j + 1]) {
            found = false;
            break;
            }

            if (!found) {
            for (int j = 0; j < (1 << (d_m - 1)); j++)
            if (!((v[2 * j] == 1 && v[2 * j + 1] == 0) || (v[2 * j] == 0 && v[2 * j + 1] == 0))) {
            found = true;
            break;
            }

            if (found) {
            for (int j = 0; j < (1 << (d_m - 1)); j++)
            if (!((v[2 * j] == 0 && v[2 * j + 1] == 1) || (v[2 * j] == 0 && v[2 * j + 1] == 0))) {
            found = false;
            break;
            }

            if (!found) {
            for (int j = 0; j < (1 << d_m); j++)
            System.out.print(v[j] + ", ");
            System.out.print("     ");
            for (int j = 0; j < (1 << d_m); j++)
            System.out.print(t[j] + ", ");
            System.out.println();
            }
            }
            }
            }*/

            if (found) {
                System.out.print(Arrays.toString(v) + "     " + Arrays.toString(t));

                long checksum = 0;
                for (int j = 0; j < (1 << d_m); j++)
                    checksum += t[j] * t[j] * t[j];

                // System.out.println(checksum);
                if (!stateVectors.containsKey(checksum)) {
                    ArrayList<Integer> v2 = new ArrayList<Integer>();
                    for (int j = 0; j < (1 << d_m); j++)
                        v2.add(v[j]);
                    ArrayList<Integer> v3 = new ArrayList<Integer>();
                    for (int j = 0; j < (1 << d_m); j++)
                        v3.add(t[j]);
                    HashMap<ArrayList<Integer>, ArrayList<Integer>> hm = new HashMap<ArrayList<Integer>, ArrayList<Integer>>();
                    hm.put(v2, v3);
                    stateVectors.put(checksum, hm);
                    HashMap<Integer, Integer> n = new HashMap<Integer, Integer>();
                    for (int j = 0; j < (1 << d_m); j++)
                        if (n.containsKey(t[j]))
                            n.put(t[j], n.get(t[j]) + 1);
                        else
                            n.put(t[j], 1);
                    stateNeighbours.put(checksum, n);
                } else {
                    ArrayList<Integer> v2 = new ArrayList<Integer>();
                    for (int j = 0; j < (1 << d_m); j++)
                        v2.add(v[j]);
                    ArrayList<Integer> v3 = new ArrayList<Integer>();
                    for (int j = 0; j < (1 << d_m); j++)
                        v3.add(t[j]);
                    stateVectors.get(checksum).put(v2, v3);
                }
            }

            if (1 < 2)
                break;

            found = false;
            int leastBit = -1;
            for (int i = 0; i < (1 << d_m); i++)
                if (v[i] == 1) {
                    leastBit = i;
                    break;
                }
            if (v[leastBit + 1] == 0) {
                v[leastBit + 1] = 1;
                v[leastBit] = 0;
            } else
                for (int j = leastBit + 1; j <= (1 << d_m); j++) {
                    if (j == (1 << d_m)) {
                        found = true;
                        break;
                    }
                    if (v[j] == 0) {
                        v[j] = 1;
                        if (maxbit < j) {
                            System.out.println();
                            maxbit = j;
                        }
                        for (int k = 0; k < j; k++)
                            if (k < j - leastBit - 1)
                                v[k] = 1;
                            else
                                v[k] = 0;
                        break;
                    }
                }
            //System.out.println(Arrays.toString(v));
            if (found)
                break;
        }

        System.out.println("\n\n\n");
        int j = 0;
        for (Entry<Long, HashMap<Integer, Integer>> e : stateNeighbours.entrySet()) {
            for (Entry<Integer, Integer> e2 : e.getValue().entrySet())
                System.out.print("(" + e2.getValue() + "x) " + e2.getKey() + ", ");
            HashMap<ArrayList<Integer>, ArrayList<Integer>> l = stateVectors.get(e.getKey());
            System.out.println("\n" + l.size());
            BufferedImage img = new BufferedImage((1 << d_m), l.size(), BufferedImage.TYPE_INT_ARGB);
            Graphics g = img.createGraphics();
            int y = 0;

            ArrayList<Entry<ArrayList<Integer>, ArrayList<Integer>>> oa = new ArrayList<Entry<ArrayList<Integer>, ArrayList<Integer>>>(l.entrySet());
            Collections.sort(oa, new MyComparator());

            for (Entry<ArrayList<Integer>, ArrayList<Integer>> i : oa) {
                System.out.println(i.getKey().toString());
                for (int k = 0; k < i.getKey().size(); k++) {
                    if (i.getKey().get(k) == 0)
                        g.setColor(Color.WHITE);
                    else
                        g.setColor(Color.BLACK);

                    g.fillRect(k, y, 1, 1);
                }
                y++;
            }
            //ImageIO.write(img, "png", new File("m" + j + ".png"));

            System.out.println("\n");
            j++;
        }

        /*System.out.print("      ");
        for (int i = 0; i < (1 << d_m); i++)
        System.out.print(Helper.printInteger100(i) + " ");
        System.out.println();
        System.out.print("      ");
        for (int i = 0; i < (1 << d_m); i++)
        System.out.print("----");
        System.out.println();
        for (int i = 0; i < (1 << d_m); i++) {
        System.out.print(Helper.printInteger100(i) + " | ");
        for (int j = 0; j < (1 << d_m); j++)
        System.out.print(Helper.printInteger100(b[i][j]) + " ");
        System.out.println(" |");
        }
        System.out.println();


        /*
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
        }*/




        /*        int c = 0;
        for (int k = 0; k <= 10; k++) {
        c += Helper.binomial(10, k);
        System.out.println(k + "\t" + c);
        }*/

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


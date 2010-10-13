package jidnet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import jidnet.idnet.Helper;

public class Subvectorspaces {

    public static void main(String[] args) {

        int d = 6;
        int m = 2;
        String[] baseU = {"00011", "00110", "01100", "111000"};

        int neighbourCount = 0;
        for (int i = 0; i <= m; i++)
            neighbourCount += Helper.binomial(d, i);
        System.out.println("NC=" + neighbourCount);
        int[] neighbourMasks = new int[neighbourCount];
        int c = 0;
        for (int i = 0; i < (1 << d); i++)
            if (Helper.hammingWeight(i) >= d - m) {
                neighbourMasks[c] = i;
                //System.out.println(Helper.getBitString(i, d));
                c++;
            }

        HashMap<Integer, ArrayList<Integer>> blocks = new HashMap<Integer, ArrayList<Integer>>();

        int[] U;
        int aorb = -1;
        if (aorb < 0) {
            // aus baseU:
            U = new int[1 << baseU.length];
            U[0] = 0;
            for (int i = 0; i < baseU.length; i++) {
                int u = Integer.parseInt(baseU[i], 2);
                for (int i_u = 0; i_u < (1 << i); i_u++)
                    U[(1 << i) + i_u] = U[i_u] ^ u;
            }
        } else {

            // aus d_M:
            int d_M = 4;
            U = new int[1 << (d - d_M)];
            int mask = (1 << (d - d_M)) - 1;
            int c0 = 0;
            for (int i = 1; i < (1 << d); i++)
                if ((i & mask) == i) {
                    U[c0] = i;
                    c0++;
                }
        }


        //for (int i_u = 0; i_u < U.length; i_u++)
        //       System.out.println(Helper.getBitString(U[i_u], d));

        //for (int i = 0; i < (1 << d); i++) {
        for (int j = 0; j < neighbourMasks.length; j++) {
            int i = neighbourMasks[j];
            int min = -1;
            for (int i_u = 0; i_u < U.length; i_u++)
                if (min > (i ^ U[i_u]) || min == -1)
                    min = (i ^ U[i_u]);
            if (blocks.containsKey(min))
                blocks.get(min).add(i);
            else {
                ArrayList<Integer> v = new ArrayList<Integer>();
                v.add(i);
                blocks.put(min, v);
            }
        }

        for (Map.Entry<Integer, ArrayList<Integer>> v : blocks.entrySet()) {
            System.out.print(Helper.getBitString(v.getKey(), d) + " -> [ ");
            System.out.print(v.getValue().size() + " ");
            //for (Integer i : v.getValue())
            //    System.out.print(Helper.getBitString(i, d) + ", ");
            System.out.println("]");
        }

        /*System.out.println();

        for (Integer v : blocks.keySet()) {
        System.out.print(Helper.getBitString(v, d) + " : ");
        for (ArrayList<Integer> block : blocks.values()) {
        int count = 0;
        for (int j = 0; j < neighbourCount; j++)
        if (block.contains(neighbourMasks[j] ^ v))
        count++;
        System.out.print(Helper.printInteger100(count) + " ");
        }
        System.out.println();
        }*/
    }
}

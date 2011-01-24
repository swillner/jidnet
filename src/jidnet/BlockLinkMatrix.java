/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jidnet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import jidnet.idnet.Helper;

/**
 *
 * @author sven
 */
public class BlockLinkMatrix {

    private static HashMap<Integer, ArrayList<Integer>> blocks;
    private static int dimSubgroup;
    private static int d;

    private static void setSubgroupCreators(ArrayList<Integer> s) {
        dimSubgroup = s.size();
        blocks = new HashMap<Integer, ArrayList<Integer>>();
        blocks.put(0, new ArrayList<Integer>());
        blocks.get(0).add(0);
        for (int i = 0; i < dimSubgroup; i++) {
            int size = blocks.get(0).size();
            for (int j = 0; j < size; j++) {
                blocks.get(0).add(s.get(i) ^ blocks.get(0).get(j));
            }
        }
        for (int i = 0; i < (1 << d); i++) {
            boolean found = false;
            for (ArrayList<Integer> b : blocks.values()) {
                if (b.contains(i)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                ArrayList<Integer> b = new ArrayList<Integer>();
                for (int j = 0; j < blocks.get(0).size(); j++) {
                    b.add(i ^ blocks.get(0).get(j));
                }
                blocks.put(i, b);
            }
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        d = 12;
        ArrayList<Integer> s = new ArrayList<Integer>();
        s.add(Integer.parseInt("000000100000", 2));
        s.add(Integer.parseInt("000000001000", 2));
        s.add(Integer.parseInt("000000000100", 2));
        s.add(Integer.parseInt("100000000000", 2));
        s.add(Integer.parseInt("010000000000", 2));
        s.add(Integer.parseInt("001000000000", 2));
        s.add(Integer.parseInt("000100000000", 2));
        s.add(Integer.parseInt("000001000000", 2));
        s.add(Integer.parseInt("000010000011", 2));
        setSubgroupCreators(s);

        LinkedHashMap<String, ArrayList<Integer>> blocks2 = new LinkedHashMap<String, ArrayList<Integer>>();

        int tmp = 0;
        ArrayList<Integer> tmpa = blocks.get(0);
        char n = 'A';
        for(int i = 0; i < blocks.size(); i++) {
            blocks2.put(Character.toString(n), tmpa);
            n++;
            if (i == blocks.size()-1)
                break;
            int tmp2 = 1 << d;
            for(Entry<Integer, ArrayList<Integer>> e2 : blocks.entrySet()) {
                if (e2.getValue().get(0) < tmp2 && e2.getValue().get(0) > tmp)
                    tmp2 = e2.getValue().get(0);
            }
            tmpa = blocks.get(tmp2);
            tmp = tmp2;
        }

        double[] weightings = new double[4];
        weightings[0] = 1;
        weightings[1] = 0.5;
        weightings[2] = 0.25;
        weightings[3] = 0.005;


        for (int m = 0; m < 4; m++) {
            System.out.println("m=" + m);
            for (Entry<String, ArrayList<Integer>> b : blocks2.entrySet()) {
                //System.out.print(Helper.getBitString(b.get(0), 12) + " & ");
                System.out.print(b.getKey() + " & ");
            }
            System.out.println("\\\\");
            for (Entry<String, ArrayList<Integer>> b : blocks2.entrySet()) {
                //System.out.println(Helper.getBitString(b.get(0), 12) + " & ");
                System.out.println(b.getKey() + " & ");
                for (ArrayList<Integer> b2 : blocks2.values()) {
                    int c = 0;
                    for (Integer i : b2) {
                        if (Helper.hammingWeight(i ^ b.getValue().get(0)) == d - m) {
                            c++;
                        }
                    }
                    System.out.print(c*weightings[m] + " & ");
                }
                System.out.println("\\\\");
            }
        }
    }
}

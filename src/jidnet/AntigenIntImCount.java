/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jidnet;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;
import jidnet.idnet.Antigen;
import jidnet.idnet.DeterminantBits;
import jidnet.idnet.Helper;
import jidnet.idnet.IdnetManager;

/**
 *
 * @author sven
 */
public class AntigenIntImCount {

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
        String sgstr = "010000000000\n000001000000\n001000000000\n000000100000\n"
                + "000100000000\n000000010000\n000010000000\n000000001000\n000000000111";
        if (!sgstr.endsWith("\n"))
            sgstr = sgstr + "\n";
        String str[] = sgstr.split("\n");
        for (int i = 0; i < str.length; i++) {
            s.add(Integer.parseInt(str[i], 2));
        }

        setSubgroupCreators(s);

        int d_H = 11;
        for (Entry<Integer, ArrayList<Integer>> b : blocks.entrySet()) {
            System.out.print(Helper.getBitString(b.getKey(), 12) + ": ");
            int c = 0;
            for (Integer i : b.getValue()) {
                if (Helper.hammingWeight(i)==d_H)
                    c++;
            }
            System.out.println(c);
        }
    }
}

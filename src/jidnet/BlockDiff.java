/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jidnet;

import java.io.FileInputStream;
import java.io.FileReader;
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
public class BlockDiff {

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

    private final static String dStr(double d) {
        return Double.toString((double) ((int) (d * 10000)) / 10000.0);
    }

    private static String getLine(FileReader fr) throws Exception {
        String s = "";
        int c;
        do {
            c=fr.read();
            if(c==-1)
                break;
            s += (char)c;
            if ((char)c=='\n')
                break;
        } while (true);
        return s;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //Properties config1 = new Properties();
        Properties config2 = new Properties();
        try {
            //config1.loadFromXML(new FileInputStream("jIdNet/config.xml"));
            config2.loadFromXML(new FileInputStream(args[0]));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }


        d = 12;
        ArrayList<Integer> s = new ArrayList<Integer>();
        String sgstr = config2.getProperty("blocks");
        if (!sgstr.endsWith("\n")) {
            sgstr = sgstr + "\n";
        }
        String str[] = sgstr.split("\n");
        for (int i = 0; i < str.length; i++) {
            s.add(Integer.parseInt(str[i], 2));
        }

        setSubgroupCreators(s);

        String bstr = "";//config1.getProperty("stats");

        try {
             FileReader fr = new FileReader(args[1]);
            getLine(fr);
            getLine(fr);
            getLine(fr);
            String l = getLine(fr);
            while (!l.equals("")) {
                bstr += l;
                l = getLine(fr);
            }
        } catch(Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        HashMap<Integer, Double> bStats = new HashMap<Integer, Double>();
        int v__ = -1;
        for (String s1 : bstr.split("\n")) {
            bStats.put(Integer.parseInt(s1.split(" ")[0], 2), Double.parseDouble(s1.split(" ")[3]));
            if (Double.parseDouble(s1.split(" ")[3]) == 0)
                v__ = Integer.parseInt(s1.split(" ")[0], 2);
        }

        //int v = Integer.parseInt(config1.getProperty("v"), 2) ^ (detBits.values ^ detBits.mask);
        int v_s = 0;
        int v_l = 0;
        double sum_s = 1E10;
        double sum_l = -1;

        double[] mo2;
        double[] mo = new double[1 << 12];
        Properties p = new Properties();
        try {
            p.loadFromXML(new FileInputStream("ag6stat.txt"));
        } catch(Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        /*double sum2 = 0;
        for (int i = 0; i < (1 << 12); i++) {
            if ((i & (1<<9)) != 0)
                sum2 += Math.pow(Double.parseDouble(p.getProperty(""+i))-0.444,2);
            else
                sum2 += Math.pow(Double.parseDouble(p.getProperty(""+i))-0.0460,2);
        }
        System.out.println(sum2);
        System.exit(1);*/
        int pos1 = 9;
        int pos2 = Integer.parseInt(config2.getProperty("det_bit_pos"));

        for (int i = 0; i < (1 << 12); i++) {
                int j = i;
                boolean b1 = ((1 << pos1) & i) != 0;
                boolean b2 = ((1 << pos2) & i) != 0;
                if (b2 != b1) {
                    j = i ^ (1 << pos1) ^ (1 << pos2);
                }
            mo[j] = Double.parseDouble(p.getProperty(""+i));
        }
        /*boolean[] mobility = new boolean[12];
        int[] perm = new int[12];
        for (int i = 0; i < 12; i++) {
            mobility[i] = false;
            perm[i] = i;
        }
        int c = 0;
        double[] mo2;
        double[] mo = new double[1 << idnetManager.getd()];
        for (int i = 0; i < (1 << idnetManager.getd()); i++) {
            mo[i] = (double) idnetManager.getIdiotypes()[i].sum_n / (double) idnetManager.gett();
        }
        while (true) {

            int largest_mobile_int = -1;
            int k = -1;
            for (int i = 0; i < 12; i++) {
                if (mobility[i]) {
                    if (i < 11 && perm[i] > perm[i + 1] && perm[i] > largest_mobile_int) {
                        largest_mobile_int = perm[i];
                        k = i;
                    }
                } else {
                    if (i > 0 && perm[i] > perm[i - 1] && perm[i] > largest_mobile_int) {
                        largest_mobile_int = perm[i];
                        k = i;
                    }
                }
            }
            if (k == -1) {
                break;
            }
            System.out.println(c);
            c++;

            boolean tmp = mobility[k + (mobility[k] ? 1 : -1)];
            mobility[k + (mobility[k] ? 1 : -1)] = mobility[k];
            mobility[k] = tmp;
            int tmp2 = perm[k + (mobility[k] ? 1 : -1)];
            perm[k + (mobility[k] ? 1 : -1)] = perm[k];
            perm[k] = tmp2;

            mo2 = new double[1 << idnetManager.getd()];
            for (int i = 0; i < (1 << idnetManager.getd()); i++) {
                int j = i;
                boolean b1 = ((1 << perm[k + (mobility[k] ? 1 : -1)]) & i) != 0;
                boolean b2 = ((1 << perm[k]) & i) != 0;
                if (b2 != b1) {
                    j = i ^ (1 << perm[k + (mobility[k] ? 1 : -1)]) ^ (1 << perm[k]);
                }
                mo2[j] = mo[i];
            }
            mo = mo2;
*/
        double sum__ = 0;
        //int v__ = Integer.parseInt(config1.getProperty("v"), 2);
            for (int v = 0; v < 4096; v++) {
                mo2 = new double[1 << 12];
                for (int i = 0; i < (1 << 12); i++) {
                    mo2[i ^ v] = mo[i];
                }
                //mo = mo2;

                double sum = 0;
                for (Entry<Integer, ArrayList<Integer>> b : blocks.entrySet()) {
                    //System.out.print(Helper.getBitString(b.getKey(), 12) + " & ");
                    double mo_s = 0;
                    for (Integer i : b.getValue()) {
                        mo_s += Math.pow(bStats.get(b.getKey()) - mo2[i], 2);
                    }
                    sum += mo_s;
                    //System.out.println("$ " + dStr(mo_s) + " $ \\\\");
                }
                //System.out.println(Helper.getBitString(v, 12) + " -> " + sum);
                if (sum < sum_s) {
                    v_s = v;
                    sum_s = sum;
                }
                if (sum > sum_l) {
                    v_l = v;
                    sum_l = sum;
                }
                if (v == v__)
                    sum__ = sum;
            }
        //}

        System.out.println("smallest: " + Helper.getBitString(v_s, 12) + " -> " + sum_s);
        System.out.println("largest: " + Helper.getBitString(v_l, 12) + " -> " + sum_l);
        System.out.println("intended: " + Helper.getBitString(v__, 12) + " -> " + sum__);
    }
}

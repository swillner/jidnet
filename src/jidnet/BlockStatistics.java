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
public class BlockStatistics {

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

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Properties config1 = new Properties();
        Properties config2 = new Properties();
        try {
            config1.loadFromXML(new FileInputStream("jIdNet/config.xml"));
            config2.loadFromXML(new FileInputStream("jIdNet/" + args[0]));
        } catch (Exception e) {
            System.err.println("Couldn't load config file 'config.xml', terminating");
            System.exit(-1);
        }


        d = 12;
        ArrayList<Integer> s = new ArrayList<Integer>();
        String sgstr = config2.getProperty("blocks");
        if (!sgstr.endsWith("\n"))
            sgstr = sgstr + "\n";
        String str[] = sgstr.split("\n");
        for (int i = 0; i < str.length; i++) {
            s.add(Integer.parseInt(str[i], 2));
        }

        setSubgroupCreators(s);

        long seed = Long.parseLong(config2.getProperty("seed"));
        IdnetManager idnetManager = new IdnetManager();
        idnetManager.setp(0.06);
        idnetManager.sett_l(2.75);
        idnetManager.setStatCenterOfGravity(false);
        idnetManager.setStatNeighbourOccupations(false);
        idnetManager.setLinkWeighting(0, 1);
        idnetManager.setLinkWeighting(1, 0.5);
        idnetManager.setLinkWeighting(2, 0.25);
        idnetManager.setLinkWeighting(3, 0.005);
        idnetManager.reseed(seed);

        int wait = Integer.parseInt(config1.getProperty("wait"));
        int wait3 = Integer.parseInt(config1.getProperty("wait3"));
        idnetManager.iterate(wait);
        idnetManager.recalc();

        // TEMP!!
        DeterminantBits detBits = idnetManager.calcDeterminantBits();
        Antigen ag = new Antigen(idnetManager, new int[]{detBits.values ^ detBits.mask});
        idnetManager.iterate(12000);
        idnetManager.recalc();
        ag.kill();
        System.out.println("##########");
        for (int i = 0; i < 10; i++) {
            idnetManager.iterate(100000);
            System.out.print(".");
        }
        System.out.println();
        //idnetManager.iterate(wait3);


        System.out.println("wait = " + wait + "; wait3 = " + wait3);
        System.out.println("Seed = " + seed);
        System.out.println("S = " + idnetManager.calcS());

        for (Entry<Integer, ArrayList<Integer>> b : blocks.entrySet()) {
            System.out.print(Helper.getBitString(b.getKey(), 12) + " & ");
            double mo = 0, mno = 0, mo_s = 0, mno_s = 0;
            for (Integer i : b.getValue()) {
                mo += (double) idnetManager.getIdiotypes()[i].sum_n / (double) idnetManager.gett();
                mno += (double) idnetManager.getIdiotypes()[i].sum_n_d / (double) idnetManager.gett();
            }
            mo /= b.getValue().size();
            mno /= b.getValue().size();
            for (Integer i : b.getValue()) {
                mo_s += Math.pow(mo - (double) idnetManager.getIdiotypes()[i].sum_n / (double) idnetManager.gett(), 2);
                mno_s += Math.pow(mno - (double) idnetManager.getIdiotypes()[i].sum_n_d / (double) idnetManager.gett(), 2);
            }
            System.out.println("$ " + dStr(mo) + " \\pm " + dStr(mo_s) + " $ & $ " + dStr(mno) + " \\pm " + dStr(mno_s) + " $ \\\\");
        }
    }
}

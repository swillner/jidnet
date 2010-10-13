/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jidnet;

import java.util.ArrayList;
import jidnet.idnet.Helper;

/**
 *
 * @author sven
 */
public class Test3 {

    // Looking for Hamming Cycles
    public static void main(String[] args) {
        int d = 6;
        int m = 2;
        ArrayList<Integer> l = new ArrayList<Integer>();
        ArrayList<Integer> comps = new ArrayList<Integer>();
        ArrayList<Integer> res = new ArrayList<Integer>();

        boolean success = false;
        while (!success) {
            l.clear();
            res.clear();
            for (int i = 0; i < (1 << d); i++)
                l.add(i);

            int c = 0;
            for (int i = 0; i < (1 << d); i++) {
                res.add(c);
                if (i == (1 << d) - 1) {
                    success = (Helper.hammingWeight(c) >= d - m);
                    break;
                }
                l.remove(new Integer(c));
                comps.clear();
                for (int j = l.size() - 1; j >= 0; j--)
                    if (Helper.hammingWeight(l.get(j) ^ c) >= d - m)
                        comps.add(l.get(j));
                if (comps.size() == 0)
                    break;

                c = comps.get((int) (Math.random() * (comps.size() - 1)));
                if (l.indexOf(c) == -1)
                    break;
                l.remove(new Integer(c));
            }
        }
        for (int i = 0; i < (1 << d); i++)
            System.out.println(Helper.getBitString(res.get(i), d) + "  " + res.get(i));

    }
}

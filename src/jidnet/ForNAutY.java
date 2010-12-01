/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jidnet;

import java.io.FileWriter;
import jidnet.idnet.Helper;

/**
 *
 * @author sven
 */
public class ForNAutY {

    private static long size(int d, int m) {
        long res = (long) Math.pow(2, d);
        for (int i = 1; i <= d + ((d - m) % 2); i++) {
            res *= i;
        }
        return res;
    }

    public static void main(String[] args) throws Exception {
        FileWriter o2 = new FileWriter("nauty.out");
        o2.close();

        for (int d = 3; d < 13; d++) {
            for (int m = 1; m < (d - 1); m++) {
                o2 = new FileWriter("nauty.out", true);
                o2.write("d=" + d + "; m=" + m + "\nI say:   grpsize=" + size(d, m) + "\n");
                o2.close();

                FileWriter out = new FileWriter("na.input");
                out.write("n=" + (1 << d) + " g\n");
                for (int i = 0; i < (1 << d); i++) {
                    for (int j = 0; j < (1 << d); j++) {
                        if (Helper.hammingWeight(i ^ j) >= d - m) {
                            out.write(j + " ");
                        }
                    }
                    out.write(";\n");
                }
                out.write("x\n");
                out.close();
                Process p = Runtime.getRuntime().exec("./append_dreadnaut");
                p.waitFor();

                o2 = new FileWriter("nauty.out", true);
                o2.write("\n");
                o2.close();

                System.out.println("d=" + d + "; m=" + m);
            }
        }

    }
}

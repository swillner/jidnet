/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jidnet;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.math.BigInteger;
import jidnet.idnet.Helper;

/**
 *
 * @author sven
 */
public class ForNAutY {

    private static BigInteger size(int d, int m) {
        BigInteger res = new BigInteger("1");//(long) Math.pow(2, d);
        for (long i = 1; i <= d+((d - m) % 2); i++) {
            res = res.multiply(new BigInteger("" + i));
        }
        return res.shiftLeft(d);
    }

    public static void main(String[] args) throws Exception {
        int min_d = 16;
        int max_d = 31;
        FileWriter o2 = new FileWriter("nauty" + max_d + ".out");
        o2.close();

        for (int d = min_d; d < 20; d++) {
            for (int m = 1; m < (d - 1); m++) {
                o2 = new FileWriter("nauty" + max_d + ".out", true);
                o2.write("d=" + d + "; m=" + m + "\nI say:   grpsize=" + size(d, m) + "\n");
                o2.close();
                int c = 0;
                Process p = Runtime.getRuntime().exec("./dreadnaut");
                p.getOutputStream().write(("n=" + (1<<d) + " g\n").getBytes());
                p.getOutputStream().flush();
                for (int i = 0; i < (1<<d); i++) {
                    for (int j = 0; j < (1<<d); j++) {
                        if (Helper.hammingWeightL(i ^ j) >= d - m) {
                            p.getOutputStream().write((j + " ").getBytes());
                        }
                    }
                    p.getOutputStream().write(";\n".getBytes());
                    p.getOutputStream().flush();
                }
                p.getOutputStream().write("xq\n".getBytes());
                p.getOutputStream().flush();

                String res = "", line = "";
                BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));

                while(line!=null) {
                    line=br.readLine();
                    if (line !=null && line.startsWith("1 orbit; grpsize=")) {
                        res = line;
                    }
                }
                br.close();
                p.waitFor();

                o2 = new FileWriter("nauty" + max_d + ".out", true);
                o2.write(res + "\n");
                o2.close();

                System.out.println("d=" + d + "; m=" + m);
            }
        }

    }
}

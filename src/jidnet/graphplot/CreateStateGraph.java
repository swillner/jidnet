package jidnet.graphplot;

import java.io.FileWriter;
import java.util.HashMap;
import jidnet.idnet.Helper;

public class CreateStateGraph {

    private static final int d = 5; // <6 !
    private static final int m = 1;
    private static final int t_l = 1;
    private static final int t_u = 10;
    private static final double p = 0.01;
    private static float[] probabilities;

    private static long update(long state) {
        for (int i = 0; i < (1 << d); i++)
            if ((state & (1 << i)) != 0) {
                int sum = 0;
                for (int j = 0; j < (1 << d); j++)
                    if (Helper.hammingWeight(i ^ j) <= m && (state & (1 << j)) != 0)
                        sum++;
                if (t_l > sum || sum > t_u)
                    state ^= 1 << i;
            }
        return state;
    }

    public static void main(String[] args) {
        try {
            //FileWriter f = new FileWriter("graph4.dat");
            /*f.write("digraph idnet {\n"
                    + "graph [overlap=false,bgcolor=\"#9999FF\"]\n"
                    + "node [shape=point,color=\"#AA2222\"]\n"
                    + "edge [colorscheme=\"greys9\",arrowsize=0.2]\n");*/

            //int array_size = 1 << 26;
            //probabilities = new float[array_size];
            //double threshold = 0;

            long size = (long)Math.pow(2,1<<d);
            //System.out.println(size);

            //for (long i = 0; i < size / array_size; i++) {
            for (long state = 0; state < size; state++) {

                //for (int j = 0; j < (1 << (1 << d)); j++)
                //    probabilities[j] = 0;

                if (update(state) != state)
                    continue;

                double P = 0;
                for (long j = 0; j < size; j++) {
                    if ((state & j) == 0) {
                        long res = update(state | j);
                        if (res == state)
                            P+= Math.pow(p, Helper.hammingWeightL(j)) * Math.pow(1 - p, (1 << d) - Helper.hammingWeightL(state | j));
                        //if (res > i*size/array_size && res < (i+1)*size/array_size)
                        //probabilities[(int)(res % array_size)] += Math.pow(p, Helper.hammingWeightL(j)) * Math.pow(1 - p, (1 << d) - Helper.hammingWeightL(i | j));
                    }
                    if (j % 100000000 == 0)
                        System.out.print(".");
                }
                System.out.println(P);

                //System.out.println(" ->" + probabilities[i]);

                /*for (int j = 0; j < (1 << (1 << d)); j++)
                    if (probabilities[j] > threshold && i != j)
                        //f.write(i + "->" + j + " [label=\"" + (double) ((int) (probabilities[j] * 10000)) / 10000 + "\"]\n");
                        f.write(i + "->" + j + " ["
                                //+ "weight=" + (int) (probabilities[j] * 10000) + ","
                                + "color=\"" + ((int) (probabilities[j] / 0.01 * 8 + 1)) + "\""
                                + "]\n");*/
                //if (state % 10 == 0) {
                //    System.out.println(state);
                    //f.flush();
                //}
            }
                    //System.out.println(" -> "+ i);
            //}
                //for (int j = 0; j < (1 << (1 << d)); j++)
                //    f.write(j + "\t" + probabilities[j] + "\n");
            //f.write("}\n");
            //f.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

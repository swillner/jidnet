package jidnet;

import jidnet.idnet.RandomGenerator;
import jidnet.idnet.RandomGeneratorMT;

public class Test2 {

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
        
    public static void main(String[] args) {
        int d_m = 8;

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

        // prep = "c" + prep.substring(1, prep.length() - 1) + "0"; // alternative

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

            int mask = (1 << d_m) - 1;
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

            float p = 0.02f;

            for (int i = 0; i < (1 << d_m); i++) {
                System.out.print("v[" + i + "] = " + v[i] + " ->  ");
                float sum2 = 0;
                int sum1 = 0;
                for (int j = 0; j < (1 << d_m); j++) {
                    sum1 += b[i][j] * v[j];
                    sum2 += b[i][j] * (v[j] + (1-v[j]) * p);
                }
                System.out.print(sum1 + " / " + sum2);
                System.out.println();
            }

    }
}

package jidnet.idnet;

/**
 *
 * @author sven
 */
public final class Helper {
    public static int hammingWeight(int i) {
        int weight = 0;
        for (int j = 0; j < 12; j++)
            if ((i & (1 << j)) != 0)
                weight++;
        return weight;
    }

    public static String getBitString(int v) {
        String str = Integer.toString(v, 2);
        if (str.length() < 12)
            str = "000000000000".substring(str.length()) + str;
        return str;
    }

    public static int nextPermutation(int v) {
        if (v == 0)
            return 0;
        else {
            int t = (v | (v - 1)) + 1;
            return t | ((((t & -t) / (v & -v)) >> 1) - 1);
        }
    }

}

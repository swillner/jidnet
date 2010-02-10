package jidnet.idnet;

/**
 * Final class with static helping functions to work with bitstrings
 *
 * @author Sven Willner
 */
public final class Helper {

    /**
     * Calculates the hamming weight of bitstring <code>i</code>, i.e. the number of bits with value 1
     *
     * @param i
     * @return
     */
    public static int hammingWeight(int i) {
        int weight = 0;
        for (int j = 0; j < 12; j++)
            if ((i & (1 << j)) != 0)
                weight++;
        return weight;
    }

    /**
     * Gets the bitstring <code>v</code> as a string object with 12 bits according to the values of <code>v</code>
     *
     * @param v
     * @return
     */
    public static String getBitString(int v) {
        String str = Integer.toString(v, 2);
        if (str.length() < 12)
            str = "000000000000".substring(str.length()) + str;
        return str;
    }

    /**
     * Permutates the bits with value 1 in bitstring <code>v</code>, return next permutation
     *
     * @param v
     * @return
     */
    public static int nextPermutation(int v) {
        if (v == 0)
            return 0;
        else {
            int t = (v | (v - 1)) + 1;
            return t | ((((t & -t) / (v & -v)) >> 1) - 1);
        }
    }

    /**
     * Returns binomial coefficient
     *
     * @param n
     * @param k
     * @return
     */
    public static int binomial(int n, int k) {
        if (n < 0 || k < 0 || k > n)
            return 0;
        int res = 1;
        for (int i = k + 1; i <= n; i++)
            res *= i;
        for (int i = 2; i <= n - k; i++)
            res /= i;
        return res;
    }

}

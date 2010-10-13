package jidnet.idnet;

public class RandomGeneratorBoost implements RandomGenerator {
    private Process p;
    private static final byte[] next = "0\n".getBytes();
    private char[] out;

    public RandomGeneratorBoost() {
        out = new char[51];
    }

    public final double nextDouble() {
        if (p == null)
            return 0;
        try {
            p.getOutputStream().write(next);
            p.getOutputStream().flush();
            int c = 0;
            while ((out[c]=(char)p.getInputStream().read()) != '\n')
                c++;
            return Double.parseDouble(String.valueOf(out));
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void setSeed(long seed) {
        if (p != null)
            close();
        try {
            p = Runtime.getRuntime().exec("jIdNet/boost_rng " + seed);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void close() {
        try {
            p.getOutputStream().write("q\n".getBytes());
            p.getOutputStream().flush();
        } catch (Exception e) {
        }
    }
}

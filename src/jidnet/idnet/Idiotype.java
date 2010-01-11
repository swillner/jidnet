package jidnet.idnet;

/**
 *
 * @author sven
 */
public class Idiotype {
    public int n;
    public double sum_n;
    public double sum_n_d;
    public double n_d;
    public int tau;
    public int b;
    public int cluster_size;
    public int total_cluster_size;

    public void recalc() {
        sum_n = sum_n_d = n_d = tau = 0;
        b = (n > 0) ? 1 : 0;
        cluster_size = 0;
        total_cluster_size = 0;
    }

    public void reset() {
        n = 0;
        recalc();
    }
}

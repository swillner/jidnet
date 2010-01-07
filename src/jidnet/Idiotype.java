package jidnet;

/**
 *
 * @author sven
 */
public class Idiotype {
    int n;
    double sum_n;
    double sum_n_d;
    double n_d;
    int tau;
    int b;
    int cluster_size;
    int total_cluster_size;

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

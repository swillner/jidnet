package jidnet.idnet;

import java.util.Vector;

/**
 * represents one idiotype
 *
 * @author Sven Willner
 */
public class Idiotype {

    /** Index / bitstring */
    public int i;
    /** Current occupation */
    public int n;
    /** Sum of occupation over all time */
    public int sum_n;
    /** Sum of occupied neighbours (weighted) over all time */
    public double sum_n_d;
    /** Sum of currently occupied neighbours (weighted) */
    public double n_d;
    /** Length of current life span */
    public int tau;
    /** Number of births (n==0 -> n>0) by influx */
    public int b;
    /** Cluster this idiotype lives in */
    public Vector<Idiotype> cluster;

    public Idiotype(int i) {
        this.i = i;
    }

    /**
     * Resets all statistics like sum of occupied neighbours, ...
     */
    public void recalc() {
        sum_n = tau = 0;
        sum_n_d = n_d = 0;
        b = (n > 0) ? 1 : 0;
        cluster = null;
    }

    /**
     * Resets idiotype (set everything to 0)
     */
    public void reset() {
        n = 0;
        recalc();
    }
}

package jidnet.idnet;

import java.util.Observable;
import java.util.Observer;

public class Antigen implements Observer {

    private IdiotypicNetwork idnet;
    private double x;
    private static final double l = 0.01d;
    private static final double k = 2;
    private int[] antigen;

    public Antigen(IdiotypicNetwork idnet, int[] antigen) {
        this.idnet = idnet;
        idnet.addObserver(this);
        x = 1.0;
        this.antigen = antigen;
    }

    public void update(Observable o, Object arg) {
        for (int i = 0; i < antigen.length; i++) {
            idnet.getIdiotypes()[antigen[i]].n = idnet.getN();
            //if (arg.equals("influx")) {
            //    idnet.getIdiotypes()[antigen[i]].sum_n++;
            //}
        }
        if (arg.equals("influx")) {
            //x = (k - l * idnet.calcWeightedNeighbourOccSum(antigen[0])) / (1 + (k - 1) * x) * x;
            x = 0;
            for (int i = 0; i < antigen.length; i++) {
                x += idnet.calcWeightedNeighbourOccSum(antigen[i]) / 79;
            }
            x /= antigen.length;
        }
    }

    public double getX() {
        return x;
    }

    public void kill() {
        idnet.deleteObserver(this);
    }
}

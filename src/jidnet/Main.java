package jidnet;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 *
 * @author sven
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
	IdnetManager im = new IdnetManager(12, 0.027, 1, 10, 1);
        im.reset();
        long t0 = System.currentTimeMillis();
	for (int i = 0; i < 10000; i++)
		im.iterate();
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        OutputStreamWriter w = new FileWriter("test.dat");
        for (int i = 0; i < 4096; i++)
            w.write(i + " " + im.getIdiotypes()[i].sum_n / 10000 + "\n");
        w.close();
    }

}

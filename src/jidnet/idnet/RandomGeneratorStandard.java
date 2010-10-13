package jidnet.idnet;

import java.util.Random;

public class RandomGeneratorStandard implements RandomGenerator {
    private Random random;

    public RandomGeneratorStandard() {
        random = new Random();
    }

    public final double nextDouble() {
        return random.nextDouble();
    }

    public void setSeed(long seed) {
        random.setSeed(seed);
    }
}

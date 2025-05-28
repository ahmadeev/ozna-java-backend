package digital.ozna.javabackend.utils;

import java.util.Random;

public class RandomNumberGenerator {
    public static int generateNumber(Random random, int max, int min) {
        return random.nextInt(min, max + 1);
    }
}

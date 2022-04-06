package org.hcmc.hcplayground.model;

import java.util.Random;

public class RandomNumber {

    private static final float bound = 100.0f;

    public RandomNumber() {

    }

    public static boolean checkBingo(float rate) {
        if (rate >= bound) return true;
        if (rate <= 0.0f) return false;

        Random rnd = new Random();
        float randomValue = rnd.nextFloat(bound);
        return randomValue < rate;
    }

    public static int getRandomNumber(int bound){
        Random random=new Random();
        return random.nextInt(bound);
    }

    public static double getRandomNumber(double min, double max) {
        if (min < 0) min = 0;
        if (max <= min) return 0;

        double result = 0;
        double delta = max - min;
        Random random = new Random();
        result = random.nextDouble() * delta + min;

        return result;
    }
}

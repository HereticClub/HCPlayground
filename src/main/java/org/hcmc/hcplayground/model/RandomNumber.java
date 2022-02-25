package org.hcmc.hcplayground.model;

import java.util.Random;

public class RandomNumber {

    private static float randomValue;
    private static final float bound = 100.0f;

    public RandomNumber() {
        /* TODO: */
    }

    public float getRandomValue() {
        return randomValue;
    }

    public static boolean checkBingo(float rate) {
        if (rate >= bound) return true;
        if (rate <= 0.0f) return false;

        Random rnd = new Random();
        randomValue = rnd.nextFloat(bound);

        return randomValue < rate;
    }
}

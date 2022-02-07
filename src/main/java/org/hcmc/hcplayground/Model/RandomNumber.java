package org.hcmc.hcplayground.Model;

import java.util.Random;

public class RandomNumber {

    public RandomNumber() {
        /* TODO: */
    }

    public static boolean checkBingo(float rate) {
        if (rate >= 100.0f) return true;
        if (rate <= 0.0f) return false;

        float bound = 100.0f;
        Random rnd = new Random();
        float number = rnd.nextFloat(bound);

        return number < rate;
    }
}

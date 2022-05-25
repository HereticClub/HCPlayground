package org.hcmc.hcplayground.utility;

import java.util.Random;

/**
 * 伪随机数类
 */
public class RandomNumber {

    private static final float bound = 100.0f;

    public RandomNumber() {

    }

    /**
     * 该函数会产生一个随机浮点数，该浮点数会对比参数rate，如果小于参数rate，则返回true，否则返回false
     *
     * @param rate 该函数内产生的随机数的检测阈值
     * @return 函数内的浮点随机数小于参数rate，则返回true，否则返回false
     */
    public static boolean checkBingo(float rate) {
        if (rate >= bound) return true;
        if (rate <= 0.0f) return false;

        Random rnd = new Random();
        float randomValue = rnd.nextFloat() * bound;
        return randomValue < rate;
    }

    /**
     * 获取一个从0到bound之间的随机正整数，不包括bound
     *
     * @param bound 获取随机数的约束值，必须为正整数
     * @return 从0到bound之间的正整数，不包括bound
     */
    public static int getRandomNumber(int bound) {
        if (bound <= 0) return 0;

        Random random = new Random();
        return random.nextInt(bound);
    }

    public static boolean getRandomBoolean() {
        Random random = new Random();
        return random.nextBoolean();
    }

    /**
     * 获取一个从min到max之间的随机浮点数，包含min，但不包含max
     *
     * @param min 浮点随机数的起始约束值
     * @param max 浮点随机数的终止约束值
     * @return 从min到max之间的随机浮点数，包含min，但不包含max
     */
    public static double getRandomNumber(double min, double max) {
        if (max <= min) return 0;

        double delta = max - min;
        Random random = new Random();

        return random.nextDouble() * delta + min;
    }
}

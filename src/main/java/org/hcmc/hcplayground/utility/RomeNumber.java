package org.hcmc.hcplayground.utility;

public class RomeNumber {

    public RomeNumber() {

    }

    public static int toInteger(String rome) {
        // TODO：罗马数字转换10进制数字，BUG
        int value = 0;
        char[] list = rome.toCharArray();
        for (int i = list.length - 2; i >= 0; i--) {
            int j = i + 1;

            int ii = getValue(list[i]);
            int jj = getValue(list[j]);
            System.out.println("ii: " + ii);
            System.out.println("jj: " + jj);

            if (jj > ii) {
                value -= getValue(list[i]);
            } else {
                value += getValue(list[i]);
            }
            System.out.println("value: " + value);
        }

        return value;
    }

    private static int getValue(char ch) {
        return switch (ch) {
            case 'I' -> 1;
            case 'V' -> 5;
            case 'X' -> 10;
            case 'L' -> 50;
            case 'C' -> 100;
            case 'D' -> 500;
            case 'M' -> 1000;
            default -> 0;
        };
    }
}

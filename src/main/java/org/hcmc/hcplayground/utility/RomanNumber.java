package org.hcmc.hcplayground.utility;

import org.apache.commons.lang.StringUtils;

import java.util.*;

public class RomanNumber {

    private static final List<Integer> arabic = Arrays.asList(1, 4, 5, 9, 10, 40, 50, 90, 100, 400, 500, 900, 1000);
    private static final List<String> roman = Arrays.asList("I", "IV", "V", "IX", "X", "XL", "L", "XC", "C", "CD", "D", "CM", "M");

    public RomanNumber() {

    }

    /**
     * 阿拉伯数字转换罗马数字
     * @param number 要转换的阿拉伯数字
     * @return 罗马数字
     * @throws Exception 当阿拉伯数字小于1或大于3999时抛出异常<br>
     * 常规罗马数字只能支持从 I(1) 到 MMMCMXCIX(3999)<br>
     * 常规罗马数字没有0的表示方式<br>
     * 需要用特殊的方式以表示大于4000的罗马数字
     */
    public static String fromInteger(int number) throws Exception {
        if (number >= 4000 || number <= 0) throw new Exception("Only support from 1 to 3999");
        StringBuilder sb = new StringBuilder();

        arabic.sort(Comparator.reverseOrder());
        roman.sort(Comparator.reverseOrder());

        int index = 0;
        int _number = number;

        while (_number > 0) {
            int div = number / arabic.get(index);
            sb.append(String.valueOf(roman.get(index)).repeat(Math.max(0, div)));

            _number %= arabic.get(index);
            index++;
        }

        return sb.toString();
    }

    /**
     * 罗马数字转换阿拉伯数字<br>
     * 只能转换从 I(1) 到 MMMCMXCIX(3999) 的罗马数字
     * @param roman 罗马数字字符串
     * @return 阿拉伯数字
     */
    public static int toInteger(String roman) {
        int result = 0;
        String _roman = roman.toUpperCase();

        if (StringUtils.isBlank(_roman)) return 0;
        if (_roman.length() <= 1) return getValue(_roman.charAt(0));

        int current;
        int next;

        for (int index = 0; index < _roman.length(); index++) {
            if (index < _roman.length() - 1) {
                current = getValue(_roman.charAt(index));
                next = getValue(_roman.charAt(index + 1));
            } else {
                current = getValue(_roman.charAt(index));
                next = getValue(_roman.charAt(index));
            }

            if (current >= next) {
                result += current;
            } else {
                result -= current;
            }
        }
        return result;
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
            /*
            case 'S' -> 5000;
            case 'R' -> 10000;
            case 'Q' -> 50000;
            case 'P' -> 100000;
            case 'O' -> 500000;
            case 'N' -> 1000000;
             */
            default -> 0;
        };
    }
}

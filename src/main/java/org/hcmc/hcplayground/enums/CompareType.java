package org.hcmc.hcplayground.enums;

public enum CompareType {
    /**
     * 数值比较
     */
    COMPARE_NUMERIC,
    /**
     * 字符串比较，不区分大小写
     */
    COMPARE_STRING,
    /**
     * 检查字符串是否在或者不在数组内
     */
    COMPARE_STRING_LIST,
    /**
     * 布尔比较
     */
    COMPARE_BOOLEAN,
    /**
     * 权限比较
     */
    COMPARE_PERMISSION,
}

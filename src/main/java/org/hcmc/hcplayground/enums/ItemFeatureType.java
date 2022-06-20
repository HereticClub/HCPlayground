package org.hcmc.hcplayground.enums;

/**
 * 自定义物品的特性
 */
public enum ItemFeatureType {
    /**
     * 书本类物品的特性<br>
     * 物品的Material必须是WRITTEN_BOOK<br>
     * 标识了该特性的物品会在玩家登陆后作为书本界面展示<br>
     * 非书本物品使用该特性会抛出异常
     */
    OPEN_BOOK_ON_JOIN,
}

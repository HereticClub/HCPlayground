package org.hcmc.hcplayground.enums;

/**
 * 奖励类型
 */
public enum RewardType {
    /**
     * 金币奖励，需要Vault
     */
    MONEY,
    /**
     * 点数奖励，可能需要playerpoints
     */
    POINT,
    /**
     * 物品奖励<br>
     * 普通物品的Material名称<br>
     * 自定义物品的id<br>
     */
    ITEM,
    /**
     * 配方奖励<br>
     * 配方的id
     */
    RECIPE,
    /**
     * 粒子效果奖励<br>
     * 粒子效果id
     */
    PARTICLE,
}

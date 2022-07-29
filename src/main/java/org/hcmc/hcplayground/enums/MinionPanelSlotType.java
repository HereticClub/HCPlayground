package org.hcmc.hcplayground.enums;

/**
 * 爪牙控制面板的插槽类型
 */
public enum MinionPanelSlotType {
    /**
     * 非活动，该插槽不会做任何事情
     */
    INACTIVE,
    /**
     * 爪牙采集物品的储存位置
     */
    STORAGE,
    /**
     * 能量插槽，表示可以为爪牙提速
     */
    ENERGY,
    /**
     * 赚外快，表示可以把多余的物品按比率自动售卖
     */
    PERKS,
    /**
     * 压缩，表示自动压缩采集得到的物品
     */
    COMPACT,
    /**
     * 冶炼，表示可以自动冶炼采集的物品
     */
    SMELT,
    /**
     * 将所有已采集的物品放到玩家的背包
     */
    PICKUP,
    /**
     * 升级，表示升级爪牙等级
     */
    UPGRADE,
    /**
     * 玩法说明
     */
    BRIEFING,
    /**
     * 回收，表示回收当前爪牙
     */
    RECLAIM,
}

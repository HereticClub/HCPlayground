package org.hcmc.hcplayground.items.weapon;

import org.hcmc.hcplayground.items.ItemBase;

public class Weapon extends ItemBase {

    /**
     * 攻击伤害
     */
    public float attackDamage = 0.0F;
    /**
     * 攻击距离，实验性内容，该版本暂不支持，保留属性
     */
    public float attackReach = 0;
    /**
     * 攻击速度
     */
    public float attackSpeed = 0.0F;
    /**
     * 暴击
     */
    public float crit = 0.0F;


    public Weapon() {

    }
}

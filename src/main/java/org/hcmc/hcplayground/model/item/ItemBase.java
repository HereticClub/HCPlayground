package org.hcmc.hcplayground.model.item;

import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.hcmc.hcplayground.utility.MaterialData;

public interface ItemBase {

    // 主键
    String PERSISTENT_MAIN_KEY = "hccraft";
    // 次键
    String PERSISTENT_SUB_KEY = "content";
    // 生命
    String PERSISTENT_HEALTH_KEY = "health";
    // 药水效果
    String PERSISTENT_POTIONS_KEY = "potions";
    // 暴击
    String PERSISTENT_CRITICAL_KEY = "critical";
    // 暴击
    String PERSISTENT_CRITICAL_DAMAGE_KEY = "critical_damage";
    // 吸血
    String PERSISTENT_BLOOD_SUCKING_KEY = "blood_sucking";
    // 攻击伤害
    String PERSISTENT_ATTACK_DAMAGE_KEY = "attack_damage";
    // 攻击距离
    String PERSISTENT_ATTACK_REACH_KEY = "attack_reach";
    // 攻击速度
    String PERSISTENT_ATTACK_SPEED_KEY = "attack_speed";
    // 恢复
    String PERSISTENT_RECOVER_KEY = "recover";
    // 盔甲
    String PERSISTENT_ARMOR_KEY = "armor";
    // 韧性
    String PERSISTENT_ARMOR_TOUGHNESS_KEY = "armor_toughness";
    // 击退抗性
    String PERSISTENT_KNOCKBACK_RESISTANCE_KEY = "knockback_resistance";
    // 移动速度
    String PERSISTENT_MOVEMENT_SPEED_KEY = "movement_speed";
    // 装备位置
    String PERSISTENT_EQUIPMENT_SLOT_KEY = "equipment_slot";
    // 幸运值
    String PERSISTENT_LUCK_KEY = "luck";

    int getAmount();

    String getId();

    String getName();

    MaterialData getMaterial();

    boolean getUnbreakable();

    boolean getGlowing();

    String[] getLore();

    ItemFlag[] getFlags();

    void setAmount(int value);

    void setId(String value);

    void setName(String value);

    void setMaterial(MaterialData value);

    void setUnbreakable(boolean value);

    void setGlowing(boolean value);

    void setLore(String[] value);

    void setFlags(ItemFlag[] value);

    ItemMeta setBaseItemMeta(ItemStack is);

    String setLoreString(float value, boolean isWeapon, boolean isPercentage);

    ItemStack toItemStack();
}

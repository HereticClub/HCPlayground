package org.hcmc.hcplayground.model.item;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.hcmc.hcplayground.enums.ItemFeatureType;
import org.hcmc.hcplayground.model.enchantment.EnchantmentItem;
import org.hcmc.hcplayground.utility.MaterialData;

import java.util.List;

public interface ItemBase {

    /**
     * 主键
     */
    String PERSISTENT_MAIN_KEY = "hccraft";
    /**
     * 次键
     */
    String PERSISTENT_SUB_KEY = "content";
    /**
     * 当前Minecraft版本直接支持的玩家基本属性，数值可直接添加和提取
     * 当前生命值
     */
    String PERSISTENT_HEALTH_KEY = "health";
    /**
     * 最大生命值
     */
    String PERSISTENT_MAX_HEALTH_KEY = "max_health";
    /**
     * 盔甲
     */
    String PERSISTENT_ARMOR_KEY = "armor";
    /**
     * 韧性
     */
    String PERSISTENT_ARMOR_TOUGHNESS_KEY = "armor_toughness";
    /**
     * 击退抗性
     */
    String PERSISTENT_KNOCKBACK_RESISTANCE_KEY = "knockback_resistance";
    /**
     * 幸运值
     */
    String PERSISTENT_LUCK_KEY = "luck";
    /**
     * 移动速度
     */
    String PERSISTENT_MOVEMENT_SPEED_KEY = "movement_speed";
    /**
     * 攻击伤害
     */
    String PERSISTENT_ATTACK_DAMAGE_KEY = "attack_damage";
    /**
     * 攻击速度
     */
    String PERSISTENT_ATTACK_SPEED_KEY = "attack_speed";
    /**
     * 攻击距离，实验性功能，目前的Minecraft版本还不支持
     */
    String PERSISTENT_ATTACK_REACH_KEY = "attack_reach";
    /**
     * 当前Minecraft版本没有的玩家属性，需要用代码来实施和运算
     * 暴击
     */
    String PERSISTENT_CRITICAL_KEY = "critical";
    /**
     * 暴击的百分比格式
     */
    String PERSISTENT_CRITICAL_PERCENTAGE_KEY = "critical_percentage";
    /**
     * 暴击伤害
     */
    String PERSISTENT_CRITICAL_DAMAGE_KEY = "critical_damage";
    /**
     * 暴击伤害的百分比格式
     */
    String PERSISTENT_CRITICAL_DAMAGE_PERCENTAGE_KEY = "critical_damage_percentage";
    /**
     * 吸血
     */
    String PERSISTENT_BLOOD_SUCKING_KEY = "blood_sucking";
    /**
     * 恢复
     */
    String PERSISTENT_RECOVER_KEY = "recover";
    /**
     * 智力
     */
    String PERSISTENT_INTELLIGENCE = "intelligence";
    /**
     * 其他特有属性
     * 挖掘速度 - 镐专有属性
     */
    String PERSISTENT_DIGGING_SPEED = "digging_speed";
    /**
     * 伐木速度 - 斧专有属性
     */
    String PERSISTENT_LOGGING_SPEED = "logging_speed";
    /**
     * 药水效果
     */
    String PERSISTENT_POTIONS_KEY = "potions";
    /**
     * 装备位置
     */
    String PERSISTENT_EQUIPMENT_SLOT_KEY = "equipment_slot";
    /**
     * 自定义方块类型键
     */
    String PERSISTENT_CRAZY_TYPE = "crazy_type";

    String getId();

    String getName();

    MaterialData getMaterial();

    int getAmount();

    boolean isUnbreakable();

    boolean isGlowing();

    List<PotionEffect> getPotions();

    List<ItemFeatureType> getFeatures();

    List<String> getBasicLore();

    List<ItemFlag> getFlags();

    List<String> getWorlds();

    List<String> getEnchantLore();

    List<String> getPotionLore();

    List<EnchantmentItem> getEnchantments();

    List<String> getAttributeLore();

    List<String> getEquipmentLore();

    void setWorlds(List<String> worlds);

    void setGlowing(boolean glowing);

    void setInteractedBlock(boolean interactedBlock);

    void setInteractedItem(boolean interactedItem);

    void setUnbreakable(boolean unbreakable);

    void setAmount(int value);

    void setId(String value);

    void setName(String value);

    void setMaterial(MaterialData value);

    void setBasicLore(List<String> value);

    void setFlags(List<ItemFlag> value);

    void setFeatures(List<ItemFeatureType> features);

    boolean isInteractedItem();

    boolean isInteractedBlock();

    boolean isBook();

    boolean isEnabledWorld(Player player);

    boolean isDisabledWorld(Player player);

    boolean isWrittenBook();

    boolean isNativeItemStack();

    ItemMeta setBaseItemMeta(ItemStack is);

    String setWeaponLore(float value, boolean isWeapon, boolean isPercentage);

    void updateLore(ItemMeta meta);
    void updateLore(ItemStack item);
    void updateAttributeLore();

    void setAttributeLore(List<String> attributeLore);

    void setEnchantLore(List<String> enchantLore);

    void setEnchantments(List<EnchantmentItem> enchantments);

    void setEquipmentLore(List<String> equipmentLore);

    void setPotionLore(List<String> potionLore);

    void setPotions(List<PotionEffect> potions);

    ItemStack toItemStack();

    String toString();
}

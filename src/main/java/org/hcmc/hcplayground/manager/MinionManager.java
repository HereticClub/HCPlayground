package org.hcmc.hcplayground.manager;

import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.enums.MinionType;
import org.hcmc.hcplayground.model.minion.MinionEntity;
import org.hcmc.hcplayground.model.minion.MinionTemplate;
import org.hcmc.hcplayground.utility.Global;

import java.util.*;

public class MinionManager {

    public final static String PERSISTENT_MAIN_KEY = "minion";
    public final static String PERSISTENT_SUB_KEY = "content";
    public final static String PERSISTENT_LEVEL_KEY = "level";
    /**
     * Minion修整工作平台周期，单位: 秒
     */
    public final static int DRESSING_PERIOD = 5;
    private final static Plugin plugin = HCPlayground.getInstance();
    private static final Map<String, List<MinionTemplate>> mapMinions = new HashMap<>();

    public MinionManager() {

    }

    public static void Load(YamlConfiguration yaml) throws IllegalAccessException {
        Set<String> keys = yaml.getKeys(false);
        for (String key : keys) {
            ConfigurationSection section = yaml.getConfigurationSection(key);
            if (section == null) continue;

            List<MinionTemplate> templates = Global.SetItemList(section, MinionTemplate.class);
            mapMinions.put(key, templates);
            for (MinionTemplate template : templates) {
                String[] ss = template.getId().split("\\.");
                if (template.getRequirement() == null)
                    Global.LogWarning(String.format("%s level %s has no define REQUIREMENT property", key, ss[1]));
                if (template.getPeriod() <= 0)
                    Global.LogWarning(String.format("%s level %s has no define PERIOD property", key, ss[1]));

                template.setLevel(Integer.parseInt(ss[1]));
                Map<EquipmentSlot, ItemStack> equipments = new HashMap<>();
                template.setEquipments(equipments);

                EquipmentSlot[] slots = EquipmentSlot.values();
                for (EquipmentSlot slot : slots) {
                    String equipKey = String.format("%s.%s.equipments.%s", key, ss[1], slot.toString().toLowerCase());
                    String m = yaml.getString(equipKey + ".material");
                    String c = yaml.getString(equipKey + ".color");
                    if (StringUtils.isBlank(m)) continue;

                    ItemStack is = new ItemStack(Material.valueOf(m.toUpperCase()), 1);
                    ItemMeta im = is.getItemMeta();
                    if (im instanceof LeatherArmorMeta meta && !StringUtils.isBlank(c)) {
                        Color color = Color.fromRGB(Integer.decode("0x" + c));
                        meta.setColor(color);
                        is.setItemMeta(meta);
                    }
                    equipments.put(slot, is);
                }
            }
        }
    }

    public static MinionTemplate getMinionTemplate(String type, int level) {
        List<MinionTemplate> minions = mapMinions.get(type);
        return minions.stream().filter(x -> x.getLevel() == level).findAny().orElse(null);
    }

    public static MinionTemplate getMinionTemplate(MinionType type, int level) {
        return getMinionTemplate(type.name(), level);
    }

    public static MinionTemplate getMinionTemplate(ItemStack item) {
        NamespacedKey mainKey = new NamespacedKey(plugin, MinionManager.PERSISTENT_MAIN_KEY);
        NamespacedKey subKey = new NamespacedKey(plugin, MinionManager.PERSISTENT_SUB_KEY);
        NamespacedKey levelKey = new NamespacedKey(plugin, MinionManager.PERSISTENT_LEVEL_KEY);

        ItemMeta im = item.getItemMeta();
        if (!(im instanceof SkullMeta meta)) return null;

        PersistentDataContainer mainContainer = meta.getPersistentDataContainer();
        String minionType = mainContainer.get(mainKey, PersistentDataType.STRING);
        if (StringUtils.isBlank(minionType)) return null;

        PersistentDataContainer subContainer = mainContainer.get(subKey, PersistentDataType.TAG_CONTAINER);
        if (subContainer == null) return null;

        Integer level = subContainer.get(levelKey, PersistentDataType.INTEGER);
        if (level == null) return null;

        return getMinionTemplate(minionType, level);
    }

    public static MinionEntity spawnMinion(Location location, ItemStack helmet) {
        World world = location.getWorld();
        if (world == null) return null;
        // 根据手持的ItemStack获取Minion的定义模板
        MinionTemplate template = getMinionTemplate(helmet);
        if (template == null) return null;
        MinionType type = getMinionType(helmet);
        if (type == null) return null;
        // 生成ArmorStand
        ArmorStand armorStand = (ArmorStand) world.spawnEntity(location, EntityType.ARMOR_STAND);
        // 没有重力，重要，不至于脚下方块被挖了之后掉下
        armorStand.setGravity(false);
        // 看见手臂，重要，能拿工具、武器、鱼竿等
        armorStand.setArms(true);
        // 没有底板，视觉
        armorStand.setBasePlate(false);
        // 关闭所有装备被玩家置换，重要
        armorStand.addEquipmentLock(EquipmentSlot.HEAD, ArmorStand.LockType.REMOVING_OR_CHANGING);
        armorStand.addEquipmentLock(EquipmentSlot.CHEST, ArmorStand.LockType.REMOVING_OR_CHANGING);
        armorStand.addEquipmentLock(EquipmentSlot.LEGS, ArmorStand.LockType.REMOVING_OR_CHANGING);
        armorStand.addEquipmentLock(EquipmentSlot.FEET, ArmorStand.LockType.REMOVING_OR_CHANGING);
        armorStand.addEquipmentLock(EquipmentSlot.HAND, ArmorStand.LockType.REMOVING_OR_CHANGING);
        // 设置ArmorStand实体的装备
        Map<EquipmentSlot, ItemStack> equipments = template.getEquipments();
        EntityEquipment equipment = armorStand.getEquipment();
        if (equipment != null && equipments != null) {
            equipment.setItem(EquipmentSlot.HEAD, helmet);
            equipment.setItem(EquipmentSlot.CHEST, equipments.get(EquipmentSlot.CHEST));
            equipment.setItem(EquipmentSlot.LEGS, equipments.get(EquipmentSlot.LEGS));
            equipment.setItem(EquipmentSlot.FEET, equipments.get(EquipmentSlot.FEET));
            equipment.setItem(EquipmentSlot.HAND, equipments.get(EquipmentSlot.HAND));
        }
        // 返回Minion的放置记录
        MinionEntity entity = new MinionEntity(armorStand, type, template.getLevel(), location);
        entity.setUuid(armorStand.getUniqueId());
        return entity;
    }

    public static boolean isMinionType(String type) {
        MinionType[] values = MinionType.values();
        MinionType value = Arrays.stream(values).filter(x -> x.name().equalsIgnoreCase(type)).findAny().orElse(null);
        return value != null;
    }

    public static boolean isMinion(ItemStack item) {
        MinionType type = getMinionType(item);
        return type != null;
    }

    public static MinionType getMinionType(ItemStack item) {
        NamespacedKey mainKey = new NamespacedKey(plugin, MinionManager.PERSISTENT_MAIN_KEY);
        ItemMeta im = item.getItemMeta();
        if (!(im instanceof SkullMeta meta)) return null;

        PersistentDataContainer mainContainer = meta.getPersistentDataContainer();
        String minionType = mainContainer.get(mainKey, PersistentDataType.STRING);
        if (StringUtils.isBlank(minionType)) return null;

        MinionType[] types = MinionType.values();
        return Arrays.stream(types).filter(x -> x.name().equalsIgnoreCase(minionType)).findAny().orElse(null);
    }
}

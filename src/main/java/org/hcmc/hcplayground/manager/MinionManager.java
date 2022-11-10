package org.hcmc.hcplayground.manager;

import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.enums.MinionType;
import org.hcmc.hcplayground.enums.PanelSlotType;
import org.hcmc.hcplayground.model.item.ItemBase;
import org.hcmc.hcplayground.model.minion.MinionEntity;
import org.hcmc.hcplayground.model.minion.MinionPanel;
import org.hcmc.hcplayground.model.minion.MinionPanelSlot;
import org.hcmc.hcplayground.model.minion.MinionTemplate;
import org.hcmc.hcplayground.serialization.MinionTypeSerialization;
import org.hcmc.hcplayground.serialization.PanelSlotTypeSerialization;
import org.hcmc.hcplayground.utility.*;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;

public class MinionManager {
    private static final Plugin plugin = HCPlayground.getInstance();
    private static final String MINION_CONFIGURE_PATH = String.format("%s/minion",plugin.getDataFolder());
    private static final String MINION_CONTROL_PANEL_SECTION = "minion_panel";
    private static final String MINION_TEMPLATE_SECTION = "minion_template";

    public final static String PERSISTENT_MAIN_KEY = "minion";
    public final static String PERSISTENT_SUB_KEY = "content";
    public final static String PERSISTENT_LEVEL_KEY = "level";
    public final static String PERSISTENT_TYPE_KEY = "type";
    /**
     * Minion修整工作平台周期，单位: 秒
     */
    public static final int DRESSING_PERIOD = 5;
    private static final List<MinionTemplate> minions = new ArrayList<>();
    private static ConfigurationSection minionPanelSection;

    public MinionManager() {

    }

    public static void Load() throws IllegalAccessException {
        try {
            minions.clear();
            File dir = new File(MINION_CONFIGURE_PATH);
            FilenameFilter filter = new YamlFileFilter();
            String[] filenames = dir.list(filter);
            if (filenames == null) return;

            for (String file : filenames) {
                // 获取路径内每个yaml文档
                YamlConfiguration yaml = new YamlConfiguration();
                yaml.load(String.format("%s/%s", MINION_CONFIGURE_PATH, file));
                // 爪牙控制面板节段，如果定义了多个爪牙控制面板节段，只得到第一个能获取的爪牙控制面板节段
                if (minionPanelSection == null)
                    minionPanelSection = yaml.getConfigurationSection(MINION_CONTROL_PANEL_SECTION);
                // 爪牙模板定义节段
                ConfigurationSection minionTemplateSection = yaml.getConfigurationSection(MINION_TEMPLATE_SECTION);
                if (minionTemplateSection == null) continue;
                List<MinionTemplate> _minions = loadMinionTemplates(minionTemplateSection);
                minions.addAll(_minions);
            }
            ItemManager.setItemMinions(minions);
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 创建一个爪牙的控制面板模板视图
     *
     * @param title 控制面板的标题
     * @return 摆放好的Minion控制面板实例(箱子界面)
     */
    public static Inventory createBasePanel(String title, MinionEntity owner) {

        MinionPanel holder = new MinionPanel(owner);
        Inventory inv = Bukkit.createInventory(holder, 54, title);
        holder.setInventory(inv);
        // 获取在minion.yml中minion_panel的所有slots定义
        List<MinionPanelSlot> slots = Global.deserializeList(minionPanelSection, MinionPanelSlot.class);
        holder.setSlots(slots);
        // 按照slots定义摆放控制面板
        for (MinionPanelSlot slot : slots) {
            // id 的第二段表示slot的动作类型
            String[] id = slot.getId().split("\\.");
            PanelSlotType type = PanelSlotTypeSerialization.valueOf(id[1]);
            slot.setType(type == null ? PanelSlotType.INACTIVE : type);
            // 根据slots定义转换成为ItemStack
            Map<Integer, ItemStack> maps = slot.toItemStacks();
            // 摆放箱子界面
            Set<Integer> keys = maps.keySet();
            for (int key : keys) {
                inv.setItem(key, maps.get(key));
            }
        }

        return inv;
    }

    public static MinionTemplate getMinionTemplate(String type, int level) {
        MinionType t = MinionTypeSerialization.valueOf(type);
        return getMinionTemplate(t, level);
    }

    public static MinionTemplate getMinionTemplate(MinionType type, int level) {
        return minions.stream().filter(x -> x.getLevel() == level && x.getType().equals(type)).findAny().orElse(null);
    }

    public static MinionTemplate getMinionTemplate(ItemStack item) {
        NameBinaryTag tag = new NameBinaryTag(item);
        String type = tag.getStringValue(MinionManager.PERSISTENT_TYPE_KEY);
        int level = tag.getIntegerValue(MinionManager.PERSISTENT_LEVEL_KEY);
        return getMinionTemplate(type, level);
    }

    public static ItemStack getMinionStack(MinionType type, int level, int amount) {
        ItemStack is = new ItemStack(Material.PLAYER_HEAD, amount);
        MinionTemplate template = MinionManager.getMinionTemplate(type, level);
        if (template == null) return null;
        // set player head texture
        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000000");
        ItemMeta meta = PlayerHeader.setTextures(is, template.getTexture(), uuid);
        // set item stack display type
        String display = StringUtils.isBlank(template.getDisplay()) ? String.format("§4%s %s", type, level) : template.getDisplay();
        meta.setDisplayName(display);
        // set item stack lore
        List<String> lore = template.getLore();
        lore.replaceAll(x -> x.replace("%period%", String.valueOf(template.getPeriod())).replace("%storage%", String.valueOf(template.getStorageAmount())));
        meta.setLore(lore);
        // set persistent data
        NamespacedKey mainKey = new NamespacedKey(plugin, MinionManager.PERSISTENT_MAIN_KEY);
        NamespacedKey subKey = new NamespacedKey(plugin, MinionManager.PERSISTENT_SUB_KEY);
        NamespacedKey levelKey = new NamespacedKey(plugin, MinionManager.PERSISTENT_LEVEL_KEY);
        NamespacedKey typeKey = new NamespacedKey(plugin, MinionManager.PERSISTENT_TYPE_KEY);

        PersistentDataContainer mainContainer = meta.getPersistentDataContainer();
        PersistentDataContainer subContainer = mainContainer.getAdapterContext().newPersistentDataContainer();
        mainContainer.set(mainKey, PersistentDataType.STRING, template.getId());
        subContainer.set(levelKey, PersistentDataType.INTEGER, level);
        subContainer.set(typeKey, PersistentDataType.STRING, type.name());
        mainContainer.set(subKey, PersistentDataType.TAG_CONTAINER, subContainer);

        is.setItemMeta(meta);
        return is;
    }

    public static MinionEntity spawnMinion(Location location, ItemStack helmet) {
        World world = location.getWorld();
        if (world == null) return null;
        // 根据手持的ItemStack获取Minion的定义模板
        MinionTemplate template = getMinionTemplate(helmet);
        if (template == null) return null;
        MinionType type = template.getType();
        int level = template.getLevel();
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
        return new MinionEntity(armorStand, type, level);
    }

    public static boolean isMinionType(String type) {
        MinionType[] values = MinionType.values();
        MinionType value = Arrays.stream(values).filter(x -> x.name().equalsIgnoreCase(type)).findAny().orElse(null);
        return value != null;
    }

    public static boolean isMinionStack(ItemStack item) {
        MinionType type = getMinionType(item);
        return type != null;
    }

    public static List<String> getLevels(MinionType minionType) {
        List<String> levels = new ArrayList<>();
        List<MinionTemplate> templates = minions.stream().filter(x -> x.getType().equals(minionType)).toList();

        for (MinionTemplate t : templates) {
            levels.add(RomanNumber.fromInteger(t.getLevel()));
        }

        return levels;
    }

    /**
     * 获取一定义胡MinionType
     */
    public static List<String> getDeclaredTypes() {
        List<String> values = new ArrayList<>();
        for (MinionTemplate template : minions) {
            String type = template.getType().name().toLowerCase();
            if (values.stream().noneMatch(x -> x.equalsIgnoreCase(type))) values.add(type);
        }

        return values;
    }

    public static MinionType getMinionType(ItemStack item) {
        NameBinaryTag tag = new NameBinaryTag(item);
        String type = tag.getStringValue(MinionManager.PERSISTENT_TYPE_KEY);

        MinionType[] types = MinionType.values();
        return Arrays.stream(types).filter(x -> x.name().equalsIgnoreCase(type)).findAny().orElse(null);
    }

    public static MinionType getMinionType(String name) {
        MinionType[] values = MinionType.values();
        return Arrays.stream(values).filter(x -> x.name().equalsIgnoreCase(name)).findAny().orElse(null);
    }

    private static List<MinionTemplate> loadMinionTemplates(ConfigurationSection section) {
        List<MinionTemplate> templates = new ArrayList<>();
        Set<String> keys = section.getKeys(false);
        // 遍历每个MinionType
        for (String type : keys) {
            ConfigurationSection _section = section.getConfigurationSection(type);
            if (_section == null) continue;

            List<MinionTemplate> levels = Global.deserializeList(_section, MinionTemplate.class);
            // 遍历每个MinionType的每个Level
            for (MinionTemplate template : levels) {
                templates.add(template);

                String[] ss = template.getId().split("\\.");
                int level = Integer.parseInt(ss[1]);
                String id = String.format("%s.%s", type, level);

                if (template.getPeriod() <= 0)
                    Global.LogWarning(String.format("%s level %s has no define PERIOD property", type, level));

                String upgradeKey = String.format("%s.%s.upgrade", type, level);
                template.getUpgrade().clear();
                MemorySection sectionUpgrade = (MemorySection) section.get(upgradeKey);
                if (sectionUpgrade != null) {
                    Map<String, Object> mapUpgrade = sectionUpgrade.getValues(false);
                    List<ItemStack> setUpgrade = new ArrayList<>();
                    template.setUpgrade(setUpgrade);
                    for (Map.Entry<String, Object> entry : mapUpgrade.entrySet()) {
                        int amount = (int) entry.getValue();

                        ItemBase ib = ItemManager.findItemById(entry.getKey().replace("@", "."));
                        if (ib != null) ib.setAmount(amount);

                        ItemStack is = ib == null ? new ItemStack(Material.valueOf(entry.getKey().toUpperCase()), amount) : ib.toItemStack();
                        setUpgrade.add(is);
                    }
                }
                // 设置MinionType的Level
                template.setLevel(level);
                template.setId(id);
                template.setType(MinionType.valueOf(type.toUpperCase()));
                Map<EquipmentSlot, ItemStack> equipments = new HashMap<>();
                template.setEquipments(equipments);

                EquipmentSlot[] slots = EquipmentSlot.values();
                for (EquipmentSlot slot : slots) {
                    String equipKey = String.format("%s.%s.equipments.%s", type, level, slot.toString().toLowerCase());
                    String m = section.getString(equipKey + ".material");
                    String c = section.getString(equipKey + ".color");
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
        return templates;
    }
}

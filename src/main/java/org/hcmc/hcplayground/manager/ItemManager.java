package org.hcmc.hcplayground.manager;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.enums.ItemFeatureType;
import org.hcmc.hcplayground.model.item.*;
import org.hcmc.hcplayground.model.minion.MinionTemplate;
import org.hcmc.hcplayground.runnable.UpdateLoreRunnable;
import org.hcmc.hcplayground.serialization.MaterialSerialization;
import org.hcmc.hcplayground.utility.Global;
import org.hcmc.hcplayground.utility.MaterialData;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ItemManager {

    private static final JavaPlugin plugin;
    private static final List<ItemBase> items;
    private static List<Weapon> itemWeapons;
    private static List<Armor> itemArmors;
    private static List<Hand> itemHands;
    private static List<Join> itemJoins;
    private static List<Crazy> itemCrazies;
    private static List<MinionTemplate> itemMinions;

    static {
        itemWeapons = new ArrayList<>();
        itemArmors = new ArrayList<>();
        itemHands = new ArrayList<>();
        itemJoins = new ArrayList<>();
        itemCrazies = new ArrayList<>();
        itemMinions = new ArrayList<>();
        items = new ArrayList<>();
        plugin = HCPlayground.getInstance();
    }

    public ItemManager() {

    }

    public static void Load(YamlConfiguration yaml) {
        /*
         在items.yml文档中
         weapons - 武器物品
         armors - 盔甲物品
         accessories 饰物，可能会带有某些药水效果
         joins - 自定义物品，普通的没有效果的物品，比如：书本、某种附加了NBT数据的矿石等
         crazies - 疯狂物品，玩家可互动方块，比如：疯狂合成台、疯狂锻造台，疯狂附魔台等
        */
        ConfigurationSection section;
        // 获取weapons节段内容
        section = yaml.getConfigurationSection("weapons");
        if (section != null) itemWeapons = Global.deserializeList(section, Weapon.class);
        // 获取armors节段内容
        section = yaml.getConfigurationSection("armors");
        if (section != null) itemArmors = Global.deserializeList(section, Armor.class);
        // 获取hands节段内容
        section = yaml.getConfigurationSection("hands");
        if (section != null) itemHands = Global.deserializeList(section, Hand.class);
        // 获取crazies节段内容
        section = yaml.getConfigurationSection("crazies");
        if (section != null) itemCrazies = Global.deserializeList(section, Crazy.class);
        // 获取joins节段内容，需要处理作为书本的物品
        section = yaml.getConfigurationSection("joins");
        if (section != null) {
            itemJoins = Global.deserializeList(section, Join.class);
            for (Join join : itemJoins) {
                // 获取Join类物品的Id
                String[] keys = join.getId().split("\\.");
                ConfigurationSection pageSection = section.getConfigurationSection(String.format("%s.pages", keys[1]));
                if (pageSection == null) continue;
                // 获取作为书本类型的页码配置
                Set<String> indexes = pageSection.getKeys(false);
                Set<String> sorted = new TreeSet<>(Comparator.naturalOrder());
                sorted.addAll(indexes);

                for (String index : sorted) {
                    int i = Integer.parseInt(index);
                    List<String> text = pageSection.getStringList(String.format("%s.content", index));
                    text.replaceAll(x -> x.replace("&", "§"));
                    join.getPages().put(i, text);
                }
            }
        }

        items.clear();
        items.addAll(itemArmors);
        items.addAll(itemHands);
        items.addAll(itemWeapons);
        items.addAll(itemJoins);
        items.addAll(itemCrazies);
    }


    /**
     * 获取整个自定义物品列表
     */
    public static List<ItemBase> getItems() {
        return items;
    }

    public static List<ItemBase> getItems(ItemFeatureType type) {
        return items.stream().filter(x -> x.getFeatures().contains(type)).toList();
    }

    public static void setItemMinions(List<MinionTemplate> itemMinions) {
        ItemManager.itemMinions = new ArrayList<>(itemMinions);
        items.removeIf(x -> x instanceof MinionTemplate);
        items.addAll(ItemManager.itemMinions);
    }

    /**
     * 判断物品是否书本
     *
     * @param itemStack 物品实例
     * @return True: 物品是书本, False: 物品不是书本
     */
    public static boolean isBook(ItemStack itemStack) {
        ItemMeta im = itemStack.getItemMeta();
        return im instanceof BookMeta;
    }

    public static boolean isItemBase(ItemStack itemStack) {
        String id = getMainKeyName(itemStack);
        return id.equalsIgnoreCase(ItemBase.PERSISTENT_MAIN_KEY);
    }

    public static boolean isMinion(ItemStack itemStack) {
        String id = getMainKeyName(itemStack);
        return id.equalsIgnoreCase(MinionManager.PERSISTENT_MAIN_KEY);
    }

    public static List<String> getIdList() {
        List<String> list = new ArrayList<>();

        for (ItemBase ib : items) {
            list.add(ib.getId().toLowerCase());
        }

        return list;
    }

    /**
     * 创建一个id为null的ItemBase实例(普通的包含数量的ItemStack实例)
     *
     * @param material 物品的材质
     * @param amount   物品的数量
     * @return ItemBase实例
     */
    public static ItemBase createItemBase(Material material, int amount) {
        return new CraftItemBase(material, amount);
    }
    public static ItemBase createItemBase(String material, int amount) {
        return new CraftItemBase(material, amount);
    }

    public static void updateLoreOnRunnable(Player player) {
        UpdateLoreRunnable r = new UpdateLoreRunnable(player);
        // 5 * 50 = 250毫秒后执行
        r.runTaskLater(plugin, 5);
    }

    public static void updateLore(ItemStack stack) {
        ItemBase ib = getItemBase(stack);
        if (ib == null) return;
        ib.updateLore(stack);
    }

    public static List<ItemBase> getBooks() {
        return items.stream().filter(ItemBase::isWrittenBook).toList();
    }

    public static ItemBase findItemById(String id) {
        return items.stream().filter(x -> x.getId().equalsIgnoreCase(id)).findAny().orElse(null);
    }

    /**
     * 从ItemStack实例获取ItemBase实例信息
     */
    public static ItemBase getItemBase(@NotNull ItemStack itemStack) {
        String id = getItemBaseId(itemStack);
        return findItemById(id);
    }

    public static void give(CommandSender sender, String playerName, String itemId, int amount) {
        Player[] players = Bukkit.getOnlinePlayers().toArray(new Player[0]);
        Player player = Arrays.stream(players).filter(x -> x.getName().equalsIgnoreCase(playerName)).findAny().orElse(null);

        if (player == null) {
            sender.sendMessage(LanguageManager.getString("playerNotExist", null).replace("%player%", playerName));
            return;
        }
        // 可能这个判断没有用
        if (!player.isOnline()) {
            sender.sendMessage(LanguageManager.getString("playerOffLine", player).replace("%player%", playerName));
            return;
        }

        ItemBase ib = findItemById(itemId);
        ItemStack is;
        if (ib == null) {
            sender.sendMessage(LanguageManager.getString("itemNotExist", player).replace("%item%", itemId));
            return;
        }
        if (ib.isBook()) {
            is = ((Join) ib).toBook(player);
        } else {
            ib.setAmount(amount);
            is = ib.toItemStack();
        }
        player.getInventory().setItem(player.getInventory().firstEmpty(), is);
    }

    private static String getItemBaseId(ItemStack itemStack) {
        ItemMeta im = itemStack.getItemMeta();
        if (im == null) return "";
        String key = getMainKeyName(itemStack);
        if (StringUtils.isBlank(key)) return "";

        PersistentDataContainer mainContainer = im.getPersistentDataContainer();
        NamespacedKey mainKey = new NamespacedKey(plugin, key);
        return mainContainer.get(mainKey, PersistentDataType.STRING);
    }

    private static String getMainKeyName(ItemStack itemStack) {
        ItemMeta im = itemStack.getItemMeta();
        if (im == null) return "";

        PersistentDataContainer mainContainer = im.getPersistentDataContainer();
        Set<NamespacedKey> dataKeys = mainContainer.getKeys();
        String[] mainKeys = Global.getPersistentMainKeys();
        for (NamespacedKey n : dataKeys) {
            if (Arrays.stream(mainKeys).noneMatch(x -> x.equalsIgnoreCase(n.getKey()))) continue;
            return n.getKey();
        }
        return "";
    }

    private static class CraftItemBase extends org.hcmc.hcplayground.model.item.CraftItemBase {

        private int amount;
        private MaterialData material;

        public CraftItemBase(Material material, int amount) {
            this.material = new MaterialData();
            this.material.setData(material, material.name());
            this.amount = amount;
        }

        public CraftItemBase(String material, int amount) {
            Material m = MaterialSerialization.valueOf(material);
            this.material = new MaterialData();
            this.material.setData(m, m.name());
            this.amount = amount;
        }


        @Override
        public void updateAttributeLore() {

        }

        @Override
        public MaterialData getMaterial() {
            return material;
        }

        public void setMaterial(Material material) {
            this.material = new MaterialData();
            this.material.setData(material, material.name());
        }

        @Override
        public int getAmount() {
            return amount;
        }

        @Override
        public void setAmount(int amount) {
            this.amount = amount;
        }

        @Override
        public ItemStack toItemStack() {
            return new ItemStack(material.value, amount);
        }
    }
}


package org.hcmc.hcplayground.manager;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.model.item.*;
import org.hcmc.hcplayground.utility.Global;
import org.hcmc.hcplayground.utility.MaterialData;

import java.util.*;

public class ItemManager {

    private static final JavaPlugin plugin;
    private static final List<ItemBase> items;
    private static List<Weapon> itemWeapons;
    private static List<Armor> itemArmors;
    private static List<Hand> itemHands;
    private static List<Join> itemJoins;
    private static List<Crazy> itemCrazies;

    static {
        itemWeapons = new ArrayList<>();
        itemArmors = new ArrayList<>();
        itemHands = new ArrayList<>();
        itemJoins = new ArrayList<>();
        itemCrazies = new ArrayList<>();
        items = new ArrayList<>();
        plugin = HCPlayground.getPlugin();
    }

    public ItemManager() {

    }

    public static List<ItemBase> getItems() {
        return items;
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

        try {
            // 获取weapons节段内容
            section = yaml.getConfigurationSection("weapons");
            if (section != null) itemWeapons = Global.SetItemList(section, Weapon.class);
            // 获取armors节段内容
            section = yaml.getConfigurationSection("armors");
            if (section != null) itemArmors = Global.SetItemList(section, Armor.class);
            // 获取hands节段内容
            section = yaml.getConfigurationSection("hands");
            if (section != null) itemHands = Global.SetItemList(section, Hand.class);
            // 获取crazies节段内容
            section = yaml.getConfigurationSection("crazies");
            if (section != null) itemCrazies = Global.SetItemList(section, Crazy.class);
            // 获取joins节段内容，需要处理作为书本的物品
            section = yaml.getConfigurationSection("joins");
            if (section != null) {
                itemJoins = Global.SetItemList(section, Join.class);
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
                        join.pages.put(i, text);
                    }
                }
            }

            items.clear();
            items.addAll(itemArmors);
            items.addAll(itemHands);
            items.addAll(itemWeapons);
            items.addAll(itemJoins);
            items.addAll(itemCrazies);
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        }
    }

    public static List<String> getIdList() {
        List<String> list = new ArrayList<>();

        for (ItemBase ib : items) {
            list.add(ib.getId());
        }

        return list;
    }

    /**
     * 创建一个id为null的ItemBase实例
     * @param material 物品的材质
     * @param amount 物品的数量
     * @return ItemBase实例
     */
    public static ItemBase createItemBase(Material material, int amount) {
        CraftItemBase x = new CraftItemBase(amount);
        MaterialData md = new MaterialData();
        md.setData(material, material.name());
        x.setId(null);
        x.setMaterial(md);
        return x;
    }

    public static List<ItemBase> getBooks(){
        return items.stream().filter(ItemBase::isWrittenBook).toList();
    }

    public static ItemBase findItemById(String id) {
        return items.stream().filter(x -> x.getId().equalsIgnoreCase(id)).findAny().orElse(null);
    }

    public static ItemBase getItemBase(ItemStack is) {
        ItemMeta im = is.getItemMeta();
        if (im == null) return null;

        NamespacedKey mainKey = new NamespacedKey(plugin, ItemBase.PERSISTENT_MAIN_KEY);
        PersistentDataContainer mainContainer = im.getPersistentDataContainer();
        String id = mainContainer.get(mainKey, PersistentDataType.STRING);

        return findItemById(id);
    }

    public static void Give(CommandSender sender, String playerName, String itemId, int amount) {
        Player[] players = Bukkit.getOnlinePlayers().toArray(new Player[0]);
        Player player = Arrays.stream(players).filter(x -> x.getName().equalsIgnoreCase(playerName)).findAny().orElse(null);

        if (player == null) {
            sender.sendMessage(LanguageManager.getMessage("playerNotExist", null).replace("%player%", playerName));
            return;
        }
        // 可能这个判断没有用
        if (!player.isOnline()) {
            sender.sendMessage(LanguageManager.getMessage("playerOffLine", player).replace("%player%", playerName));
            return;
        }

        ItemBase ib = findItemById(itemId);
        if (ib == null) {
            sender.sendMessage(LanguageManager.getMessage("noSuchItem", player).replace("%item%", itemId));
            return;
        }
        ItemStack is = ib.toItemStack();
        is.setAmount(amount);
        player.getInventory().setItem(player.getInventory().firstEmpty(), is);
    }

    private static class CraftItemBase extends ItemBaseA {

        private final int amount;

        public CraftItemBase(int amount) {
            this.amount = amount;
        }

        @Override
        public ItemStack toItemStack() {

            return new ItemStack(this.getMaterial().value, amount);
        }
    }
}


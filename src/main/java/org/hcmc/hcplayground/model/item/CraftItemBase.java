package org.hcmc.hcplayground.model.item;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.enums.ItemFeatureType;
import org.hcmc.hcplayground.model.enchantment.EnchantmentItem;
import org.hcmc.hcplayground.utility.Global;
import org.hcmc.hcplayground.utility.MaterialData;
import org.hcmc.hcplayground.utility.RomanNumber;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 自定义物品的基础类<br>
 * 该类或者子类的属性和值将会写入PersistentData中<br>
 * 如果当前实例的id属性值为null<br>
 * 则该实例所有属性除了material和amount外都无效<br>
 * 即该实例只是一个常规的ItemStack<br>
 */
public abstract class CraftItemBase implements ItemBase {
    /**
     * 物品的displayName
     */
    @Expose
    @SerializedName(value = "name")
    protected String name = "";
    /**
     * 物品的Material，以此作为基础
     */
    @Expose
    @SerializedName(value = "material")
    protected MaterialData material;
    /**
     * 物品的叠堆数量，最大值64
     */
    @Expose
    @SerializedName(value = "amount")
    protected int amount = 1;
    /**
     * 物品是否可以被破坏<br>
     * 对于一些有耐久值入钻石剑等<br>
     * 设置为true时则不再计算耐久度<br>
     */
    @Expose
    @SerializedName(value = "unbreakable")
    protected boolean unbreakable = false;
    /**
     * 物品是否可互动，比如打开地图，吃食物等
     */
    @Expose
    @SerializedName(value = "interacted-item")
    protected boolean interactedItem = true;
    /**
     * 物品放置的方块是否可互动，比如打开标准箱子，打开标准熔炉界面等
     */
    @Expose
    @SerializedName(value = "interacted-block")
    protected boolean interactedBlock = true;
    /**
     * 物品是否带有附魔效果<br>
     */
    @Expose
    @SerializedName(value = "glowing")
    protected boolean glowing = false;
    /**
     * 物品的说明，可以用&字符代替颜色代码
     */
    @Expose
    @SerializedName(value = "lore")
    protected List<String> basicLore = new ArrayList<>();
    /**
     * 物品的标记，详细信息请参阅items.yml<br>
     */
    @Expose
    @SerializedName(value = "flags")
    protected List<ItemFlag> flags = new ArrayList<>();
    /**
     * 物品的特性，比如登陆时打开的书本，进入某个世界时给与玩家等等
     */
    @Expose
    @SerializedName(value = "features")
    protected List<ItemFeatureType> features = new ArrayList<>();
    /**
     * 该物品可以允许使用的世界列表
     */
    @Expose
    @SerializedName(value = "worlds")
    protected List<String> worlds = new ArrayList<>();
    /**
     * 物品的附魔列表
     */
    @Expose
    @SerializedName(value = "enchantments")
    protected List<EnchantmentItem> enchantments = new ArrayList<>();
    /**
     * 附加在武器上的药水效果
     */
    @Expose
    @SerializedName(value = ItemBase.PERSISTENT_POTIONS_KEY)
    protected List<PotionEffect> potions = new ArrayList<>();

    @Expose(deserialize = false)
    protected List<String> enchantLore = new ArrayList<>();
    @Expose(deserialize = false)
    protected List<String> potionLore = new ArrayList<>();
    @Expose(deserialize = false)
    protected List<String> equipmentLore = new ArrayList<>();
    @Expose(deserialize = false)
    protected List<String> attributeLore = new ArrayList<>();
    @Expose(deserialize = false)
    protected JavaPlugin plugin = HCPlayground.getInstance();
    /**
     * 物品的ID，以hccraft为命名空间写入PersistentData<br>
     * 如果id属性为null，表示当前ItemBase实例为普通的ItemStack
     */
    @Expose(deserialize = false)
    protected String id;

    @Override
    public List<EnchantmentItem> getEnchantments() {
        return enchantments;
    }

    @Override
    public void setEnchantments(List<EnchantmentItem> enchantments) {
        this.enchantments = enchantments;
    }

    @Override
    public List<String> getAttributeLore() {
        return attributeLore;
    }

    @Override
    public void setAttributeLore(List<String> attributeLore) {
        this.attributeLore = attributeLore;
    }

    @Override
    public List<String> getEquipmentLore() {
        return equipmentLore;
    }

    @Override
    public void setEquipmentLore(List<String> equipmentLore) {
        this.equipmentLore = equipmentLore;
    }

    @Override
    public List<String> getEnchantLore() {
        return enchantLore;
    }

    @Override
    public void setEnchantLore(List<String> enchantLore) {
        this.enchantLore = enchantLore;
    }

    @Override
    public List<String> getPotionLore() {
        return potionLore;
    }

    @Override
    public void setPotionLore(List<String> potionLore) {
        this.potionLore = potionLore;
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
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public MaterialData getMaterial() {
        return material;
    }

    @Override
    public void setMaterial(MaterialData material) {
        this.material = material;
    }

    @Override
    public boolean isUnbreakable() {
        return unbreakable;
    }

    @Override
    public void setUnbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
    }


    @Override
    public boolean isGlowing() {
        return glowing;
    }

    @Override
    public void setGlowing(boolean glowing) {
        this.glowing = glowing;
    }

    @Override
    public List<String> getBasicLore() {
        return basicLore;
    }

    @Override
    public void setBasicLore(List<String> basicLore) {
        this.basicLore = basicLore;
    }

    @Override
    public List<ItemFeatureType> getFeatures() {
        return features;
    }

    @Override
    public void setFeatures(List<ItemFeatureType> features) {
        this.features = features;
    }

    @Override
    public List<ItemFlag> getFlags() {
        return flags;
    }

    @Override
    public void setFlags(List<ItemFlag> flags) {
        this.flags = flags;
    }

    @Override
    public List<String> getWorlds() {
        return worlds;
    }

    @Override
    public void setWorlds(List<String> worlds) {
        this.worlds = worlds;
    }

    @Override
    public List<PotionEffect> getPotions() {
        return potions;
    }

    @Override
    public void setPotions(List<PotionEffect> potions) {
        this.potions = potions;
    }

    /**
     * 标识当前ItemBase实例是否普通的ItemStack
     */
    @Override
    public boolean isNativeItemStack(){
        return StringUtils.isBlank(id);
    }

    /**
     * 判断是否和另一个ItemBase实例相似<br>
     * @param other 另一个ItemBase实例
     * @return 如果当前ItemBase实例和被判断的ItemBase实例中其中一个包含id属性，则判断这两个ItemBase实例的id属性是否一致<br>
     * 如果当前ItemBase实例和被判断的ItemBase实例都是普通的ItemStack实例，则调用ItemStack.isSimilar(ItemStack)方法来判断是否相似
     */
    @Override
    public boolean isSimilar(@NotNull ItemBase other) {
        if (StringUtils.isBlank(id) && StringUtils.isBlank(other.getId())) {
            return toItemStack().isSimilar(other.toItemStack());
        } else {
            return StringUtils.equalsIgnoreCase(id, other.getId());
        }
    }

    /**
     * 判断玩家所在的世界是否当前ItemBase实例的可用世界
     * @param player 玩家实例
     * @return true: ItemBase实例在当前世界可用, false: ItemBase实例在当前世界不可用
     */
    @Override
    public boolean isEnabledWorld(Player player) {
        if (player.isOp()) return true;
        if (worlds.size() == 0) return true;

        String world = player.getWorld().getName();
        return worlds.stream().anyMatch(x -> x.equalsIgnoreCase(world));
    }

    @Override
    public boolean isDisabledWorld(Player player) {
        return !isEnabledWorld(player);
    }

    @Override
    public boolean isBook() {
        if (material == null || material.value == null) return false;
        ItemStack is = new ItemStack(material.value, 1);
        return is.getItemMeta() instanceof BookMeta;
    }

    @Override
    public boolean isWrittenBook() {
        if (material == null) return false;
        return material.value.equals(Material.WRITTEN_BOOK);
    }

    @Override
    public boolean isInteractedItem() {
        return interactedItem;
    }

    @Override
    public void setInteractedItem(boolean interactedItem) {
        this.interactedItem = interactedItem;
    }

    @Override
    public boolean isInteractedBlock() {
        return interactedBlock;
    }

    @Override
    public void setInteractedBlock(boolean interactedBlock) {
        this.interactedBlock = interactedBlock;
    }

    @Override
    public ItemMeta setBaseItemMeta(ItemStack itemStack) {
        for (EnchantmentItem e : enchantments) {
            itemStack.addUnsafeEnchantment(e.getItem(), e.getLevel());
        }

        ItemMeta im = itemStack.getItemMeta();
        if (im == null) return null;

        int enchant_count = itemStack.getEnchantments().size();
        int potion_count = potions.size();
        if (enchant_count >= 1) im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        if (potion_count >= 1) im.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);

        NamespacedKey mainKey = new NamespacedKey(plugin, PERSISTENT_MAIN_KEY);
        PersistentDataContainer mainContainer = im.getPersistentDataContainer();
        mainContainer.set(mainKey, PersistentDataType.STRING, id);

        updateLore(im);
        im.setDisplayName(name);
        im.setUnbreakable(unbreakable);
        im.addItemFlags(flags.toArray(new ItemFlag[0]));
        itemStack.setItemMeta(im);

        return im;
    }

    @Override
    public void updateLore(@NotNull ItemMeta meta) {
        updateAttributeLore();
        updateEnchantLore(meta);
        updatePotionLore();

        List<ItemFlag> _flags = new ArrayList<>(meta.getItemFlags().stream().toList());
        List<String> _lore = new ArrayList<>(basicLore);

        if (attributeLore.size() >= 2) {
            _lore.addAll(attributeLore);
            if (!_flags.contains(ItemFlag.HIDE_ATTRIBUTES)) _flags.add(ItemFlag.HIDE_ATTRIBUTES);
        }
        if (enchantLore.size() >= 2) {
            _lore.addAll(enchantLore);
            if (!_flags.contains(ItemFlag.HIDE_ENCHANTS)) _flags.add(ItemFlag.HIDE_ENCHANTS);
        }
        if (potionLore.size() >= 2) {
            _lore.addAll(potionLore);
            if (!_flags.contains(ItemFlag.HIDE_POTION_EFFECTS)) _flags.add(ItemFlag.HIDE_POTION_EFFECTS);
        }

        meta.addItemFlags(_flags.toArray(new ItemFlag[0]));
        meta.setLore(_lore);
    }

    @Override
    public void updateLore(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return;

        updateLore(meta);
        itemStack.setItemMeta(meta);
    }

    @Override
    public String setWeaponLore(float value, boolean isWeapon, boolean isPercentage) {
        String result;
        String colorCode;
        String sign;
        String percentage;

        colorCode = isWeapon ? "§2" : "§9";
        sign = isWeapon ? "" : "+";
        percentage = isPercentage ? "%" : "";
        float v = isPercentage ? value * 100 : value;
        result = String.format("%s %s%.2f%s", colorCode, sign, v, percentage);

        return result;
    }

    @Override
    public String toString() {
        return String.format("%s x %s", StringUtils.isBlank(name) ? material.value.name() : name, amount);
    }

    private void updateEnchantLore(ItemMeta meta) {
        enchantLore.clear();
        enchantLore.add(String.format("%s7附魔:", Global.CHAR_00A7));
        Map<Enchantment, Integer> mapEnchants = meta.getEnchants();
        Set<Enchantment> enchants = mapEnchants.keySet();
        for (Enchantment e : enchants) {
            int level = mapEnchants.get(e);
            EnchantmentItem i = new EnchantmentItem(e, level);
            enchantLore.add(String.format("%s9 %s", Global.CHAR_00A7, i.toDisplay()));
        }
    }

    private void updatePotionLore() {
        potionLore.clear();
        potionLore.add(String.format("%s7增益:", Global.CHAR_00A7));
        for (PotionEffect p : potions) {
            try {
                int amplifier = p.getAmplifier() + 1;
                String name = p.getType().getName().toUpperCase();
                String display = String.format("%s9 %s %s", Global.CHAR_00A7, name, RomanNumber.fromInteger(amplifier));
                potionLore.add(display);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}


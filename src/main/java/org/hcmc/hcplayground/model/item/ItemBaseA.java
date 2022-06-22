package org.hcmc.hcplayground.model.item;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.enums.ItemFeatureType;
import org.hcmc.hcplayground.utility.MaterialData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 自定义物品的基础类<br>
 * 该类或者子类的属性和值将会写入PersistentData中<br>
 * 如果当前实例的id属性值为null<br>
 * 则该实例所有属性除了material和amount外都无效<br>
 * 即该实例只是一个常规的ItemStack<br>
 */
public abstract class ItemBaseA implements ItemBase {
    /**
     * 物品的ID，以hccraft为命名空间写入PersistentData
     */
    @Expose
    @SerializedName(value = "id")
    private String id = "";
    /**
     * 物品的displayName
     */
    @Expose
    @SerializedName(value = "name")
    private String name = "";
    /**
     * 物品的Material，以此作为基础
     */
    @Expose
    @SerializedName(value = "material")
    private MaterialData material;
    /**
     * 物品的叠堆数量，最大值64
     */
    @Expose
    @SerializedName(value = "amount")
    private int amount = 1;
    /**
     * 物品是否可以被破坏<br>
     * 对于一些有耐久值入钻石剑等<br>
     * 设置为true时则不再计算耐久度<br>
     */
    @Expose
    @SerializedName(value = "unbreakable")
    private Boolean unbreakable = false;
    /**
     * 物品是否带有附魔效果<br>
     */
    @Expose
    @SerializedName(value = "glowing")
    private Boolean glowing = false;
    /**
     * 物品的说明，可以用&字符代替颜色代码<br>
     */
    @Expose
    @SerializedName(value = "lore")
    private String[] lore = new String[]{};
    /**
     * 物品的标记，详细信息请参阅items.yml<br>
     */
    @Expose
    @SerializedName(value = "flags")
    private ItemFlag[] flags = new ItemFlag[]{};
    /**
     * 物品的特性，比如登陆时打开的书本，进入某个世界时给与玩家等等
     */
    @Expose
    @SerializedName(value = "features")
    public ItemFeatureType[] features = new ItemFeatureType[]{};

    /*
    使用了@Expose，必须在创建Gson实例时<br>
    同时使用excludeFieldsWithoutExposeAnnotation()
    同时没有使用@Expose的字段不会被序列化和反序列化
    */
    @Expose(serialize = false, deserialize = false)
    protected JavaPlugin plugin = HCPlayground.getInstance();

    @Override
    public int getAmount() {
        return amount;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public MaterialData getMaterial() {
        return material;
    }

    @Override
    public boolean getUnbreakable() {
        return unbreakable;
    }

    @Override
    public boolean getGlowing() {
        return glowing;
    }

    @Override
    public String[] getLore() {
        return lore;
    }

    @Override
    public ItemFeatureType[] getFeature() {
        return features;
    }

    @Override
    public ItemFlag[] getFlags() {
        return flags;
    }

    public void setFeatures(ItemFeatureType[] features) {
        this.features = features;
    }

    @Override
    public void setAmount(int value) {
        amount = value;
    }

    @Override
    public void setId(String value) {
        id = value;
    }

    @Override
    public void setName(String value) {
        name = value;
    }

    @Override
    public void setMaterial(MaterialData value) {
        material = value;
    }

    @Override
    public void setUnbreakable(boolean value) {
        unbreakable = value;
    }

    @Override
    public void setGlowing(boolean value) {
        glowing = value;
    }

    @Override
    public void setLore(String[] value) {
        lore = value;
    }

    @Override
    public void setFlags(ItemFlag[] value) {
        flags = value;
    }

    public boolean isWrittenBook() {
        return material.value.equals(Material.WRITTEN_BOOK);
    }

    public ItemMeta setBaseItemMeta(ItemStack itemStack) {
        ItemMeta im = itemStack.getItemMeta();
        if (im == null) return null;

        NamespacedKey mainKey = new NamespacedKey(plugin, PERSISTENT_MAIN_KEY);
        PersistentDataContainer mainContainer = im.getPersistentDataContainer();
        mainContainer.set(mainKey, PersistentDataType.STRING, id);

        List<String> lore = new ArrayList<>(Arrays.stream(this.lore).toList());
        im.setDisplayName(name);
        im.setLore(lore);
        im.setUnbreakable(unbreakable);
        im.addItemFlags(flags);
        itemStack.setItemMeta(im);

        return im;
    }

    public String setLoreString(float value, boolean isWeapon, boolean isPercentage) {
        String result;
        String colorCode;
        String sign;
        String percentage;

        colorCode = isWeapon ? "§2" : "§9";
        sign = isWeapon ? "" : "+";
        percentage = isPercentage ? "%" : "";
        float v = isPercentage ? value * 100 : value;
        result = String.format("%s%s%.2f%s", colorCode, sign, v, percentage);

        return result;
    }
}


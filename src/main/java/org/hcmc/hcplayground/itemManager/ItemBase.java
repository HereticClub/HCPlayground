package org.hcmc.hcplayground.itemManager;


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
import org.hcmc.hcplayground.model.Global;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 自定义物品的基础类<br>
 * 该类或者子类的属性和值将会写入PersistentData中<br>
 * 如果当前实例的id属性值为null<br>
 * 则该实例所有属性除了material外都无效<br>
 * 即该实例只是一个普通的Material<br>
 */
public class ItemBase {
    /**
     * 物品的ID，以hccraft为命名空间写入PersistentData
     */
    @Expose
    @SerializedName(value = "id")
    public String id = "";
    /**
     * 物品的displayName
     */
    @Expose
    @SerializedName(value = "name")
    public String name = "";
    /**
     * 物品的Material，以此作为基础
     */
    @Expose
    @SerializedName(value = "material")
    public Material material = Material.STONE;
    /**
     * 物品是否可以被破坏<br>
     * 对于一些有耐久值入钻石剑等<br>
     * 设置为true时则不再计算耐久度<br>
     */
    @Expose
    @SerializedName(value = "unbreakable")
    public Boolean unbreakable = false;
    /**
     * 物品是否带有附魔效果<br>
     */
    @Expose
    @SerializedName(value = "glowing")
    public Boolean glowing = false;
    /**
     * 物品的说明，可以用&字符代替颜色代码<br>
     */
    @Expose
    @SerializedName(value = "lore")
    public String[] lore = new String[]{};
    /**
     * 物品的标记，详细信息请参阅items.yml<br>
     */
    @Expose
    @SerializedName(value = "flags")
    public ItemFlag[] flags = new ItemFlag[]{};

    /*
    使用了@Expose，必须在创建Gson实例时<br>
    同时使用excludeFieldsWithoutExposeAnnotation()
    同时没有使用@Expose的字段不会被序列化和反序列化
    */
    @Expose(serialize = false, deserialize = false)
    protected JavaPlugin plugin;

    public ItemBase() {
        plugin = HCPlayground.getPlugin();
    }

    protected ItemMeta SetBaseItemMeta(ItemStack is) {
        ItemMeta im = is.getItemMeta();
        if (im == null) return null;

        NamespacedKey mainKey = new NamespacedKey(plugin, Global.PERSISTENT_MAIN_KEY);
        PersistentDataContainer mainContainer = im.getPersistentDataContainer();
        mainContainer.set(mainKey, PersistentDataType.STRING, this.id);

        List<String> lores = new ArrayList<>(Arrays.stream(this.lore).toList());
        im.setDisplayName(this.name);
        im.setLore(lores);
        im.setUnbreakable(this.unbreakable);
        im.addItemFlags(this.flags);
        is.setItemMeta(im);

        return im;
    }

    protected String setColorString(float value, boolean isWeapon, boolean isPercentage) {
        String result;
        String colorCode;
        String sign;
        String percentage;

        colorCode = isWeapon ? "§2" : "§9";
        sign = isWeapon ? "" : "+";
        percentage = isPercentage ? "%" : "";
        result = String.format("%s%s%.1f%s", colorCode, sign, value, percentage);

        return result;
    }
}

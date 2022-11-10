package org.hcmc.hcplayground.model.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.hcmc.hcplayground.enums.CraftingType;

import java.util.ArrayList;
import java.util.List;

public class BanItemConfiguration {

    @Expose
    @SerializedName(value = "enchantments")
    private Enchantment[] enchantments;
    @Expose
    @SerializedName(value = "potions")
    private PotionEffect[] potionEffects;
    @Expose
    @SerializedName(value = "materials")
    private Material[] materials;
    @Expose
    @SerializedName(value = "material")
    private Material material;
    @Expose
    @SerializedName(value = "display_name")
    private String name;
    @Expose
    @SerializedName(value = "lore")
    private final List<String> lore = new ArrayList<>();
    @Expose
    @SerializedName(value = "type")
    private final CraftingType type = CraftingType.SMITHING;


    @Expose(serialize = false, deserialize = false)
    private String id;

    public BanItemConfiguration() {

    }

    public CraftingType getType() {
        return type;
    }

    public Enchantment[] getEnchantments() {
        return enchantments;
    }

    public ItemStack toBarrierItem() {
        ItemStack is = new ItemStack(material, 1);

        ItemMeta im = is.getItemMeta();
        if (im == null) return is;

        im.setDisplayName(name);
        im.setLore(lore);
        is.setItemMeta(im);

        return is;
    }
}

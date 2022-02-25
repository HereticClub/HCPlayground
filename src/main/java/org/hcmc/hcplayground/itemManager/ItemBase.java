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
import org.hcmc.hcplayground.model.Global;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface ItemBase {

    int getAmount();
    String getId();
    String getName();
    Material getMaterial();
    boolean getUnbreakable();
    boolean getGlowing();
    String[] getLore();
    ItemFlag[] getFlags();

    void setAmount(int value);
    void setId(String value);
    void setName(String value);
    void setMaterial(Material value);
    void setUnbreakable(boolean value);
    void setGlowing(boolean value);
    void setLore(String[] value);
    void setFlags(ItemFlag[] value);


    ItemMeta SetBaseItemMeta(ItemStack is);

    String setColorString(float value, boolean isWeapon, boolean isPercentage);

    ItemStack toItemStack();
}

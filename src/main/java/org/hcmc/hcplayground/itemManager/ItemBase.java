package org.hcmc.hcplayground.itemManager;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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

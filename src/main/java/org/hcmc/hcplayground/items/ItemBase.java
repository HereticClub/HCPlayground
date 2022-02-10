package org.hcmc.hcplayground.items;


import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;

public abstract class ItemBase {
    public String id = "";
    public String name = "";
    public Material material = Material.STONE;
    public Boolean unbreakable = false;
    public Boolean glowing = false;
    public String[] lore = new String[]{};
    public ItemFlag[] flags = new ItemFlag[]{};

}

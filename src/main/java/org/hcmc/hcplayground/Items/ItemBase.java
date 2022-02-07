package org.hcmc.hcplayground.Items;


import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;

public abstract class ItemBase {

    public String id = "";
    public String name = "";
    public Material material = Material.STONE;
    public Boolean unbreakable = false;
    public String[] lore = new String[]{};
    public ItemFlag[] itemFlags = new ItemFlag[]{};

}

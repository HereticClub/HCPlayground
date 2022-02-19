package org.hcmc.hcplayground.dropManager;

import com.google.gson.annotations.Expose;
import org.bukkit.Material;
import org.hcmc.hcplayground.itemManager.ItemBase;

public class DropEntity {

    @Expose
    public float rate = 0.0F;
    @Expose
    public Material block = Material.STONE;
    @Expose
    public ItemBase[] drops;
    @Expose
    public int age = 0;

    public DropEntity() {

    }
}

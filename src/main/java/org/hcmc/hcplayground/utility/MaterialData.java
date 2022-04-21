package org.hcmc.hcplayground.utility;

import org.bukkit.Material;

public class MaterialData {

    public Material value;
    public String name;

    public MaterialData() {

    }

    public MaterialData(Material material, String name) {
        value = material;
        this.name = name;
    }
}

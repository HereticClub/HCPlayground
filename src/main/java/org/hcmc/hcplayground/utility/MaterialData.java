package org.hcmc.hcplayground.utility;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.bukkit.Material;

public class MaterialData {

    @Expose
    @SerializedName(value = "value")
    public Material value = Material.AIR;
    @Expose
    @SerializedName(value = "name")
    public String name;

    public MaterialData() {

    }

    public void setData(Material material, String name) {
        value = material;
        this.name = name;
    }
}

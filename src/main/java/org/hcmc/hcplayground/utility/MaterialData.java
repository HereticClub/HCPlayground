package org.hcmc.hcplayground.utility;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.bukkit.Material;

public class MaterialData {

    @Expose
    @SerializedName(value = "value")
    public Material value;
    @Expose
    @SerializedName(value = "name")
    public String name;

    public MaterialData() {
        this.value = Material.AIR;
        this.name = "";
    }

    public MaterialData(Material material, String name) {
        setData(material, name);
    }

    public void setData(Material material, String name) {
        this.value = material;
        this.name = name;
    }
}

package org.hcmc.hcplayground.template;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class TemplateSlot {

    @Expose
    @SerializedName(value = "displayText")
    public String text;
    @Expose
    @SerializedName(value = "slot")
    public int number;
    @Expose
    @SerializedName(value = "material")
    public Material material;
    @Expose
    @SerializedName(value = "lores")
    public List<String> lores = new ArrayList<>();

    public TemplateSlot() {

    }
}

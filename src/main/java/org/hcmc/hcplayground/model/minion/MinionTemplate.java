package org.hcmc.hcplayground.model.minion;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class MinionTemplate {

    @Expose
    @SerializedName(value = "display")
    private String display;
    @Expose
    @SerializedName(value = "texture")
    private String texture;
    @Expose
    @SerializedName(value = "lore")
    private List<String> lore;

    @Expose
    @SerializedName(value = "equipments")
    private Map<EquipmentSlot, ItemStack> equipments;

    @Expose(serialize = false, deserialize = false)
    private String id;
    @Expose(serialize = false, deserialize = false)
    private int level;


    public String getId() {
        return id;
    }

    public int getLevel() {
        return level;
    }

    public List<String> getLore() {
        return lore;
    }

    public Map<EquipmentSlot, ItemStack> getEquipments() {
        return equipments;
    }

    public String getDisplay() {
        return display;
    }

    public String getTexture() {
        return texture;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setEquipments(Map<EquipmentSlot, ItemStack> equipments) {
        this.equipments = equipments;
    }

    public MinionTemplate() {

    }
}
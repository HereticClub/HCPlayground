package org.hcmc.hcplayground.model.minion;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.hcmc.hcplayground.enums.MinionCategory;
import org.hcmc.hcplayground.model.item.ItemBase;

import java.util.HashMap;
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
    @SerializedName(value = "platform")
    private Material platform;
    @Expose
    @SerializedName(value = "category")
    private MinionCategory category;
    @Expose
    @SerializedName(value = "period")
    private int period;
    @Expose
    @SerializedName(value = "equipments")
    private Map<EquipmentSlot, ItemStack> equipments = new HashMap<>();
    @Expose
    @SerializedName(value = "storage")
    private int storageAmount = 1;
    /**
     * 爪牙升级需求
     */
    @Expose(serialize = false, deserialize = false)
    private Map<ItemStack, Integer> upgrade = new HashMap<>();
    @Expose(serialize = false, deserialize = false)
    private String id;
    @Expose(serialize = false, deserialize = false)
    private int level;

    public int getStorageAmount() {
        return storageAmount;
    }

    public String getId() {
        return id;
    }

    public List<String> getLore() {
        return lore;
    }

    public String getDisplay() {
        return display;
    }

    public String getTexture() {
        return texture;
    }

    public int getPeriod() {
        return period;
    }

    public Material getPlatform() {
        return platform;
    }

    public MinionCategory getCategory() {
        return category;
    }

    public Map<ItemStack, Integer> getUpgrade() {
        return upgrade;
    }

    public void setUpgrade(Map<ItemStack, Integer> upgrade) {
        this.upgrade = upgrade;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public Map<EquipmentSlot, ItemStack> getEquipments() {
        return equipments;
    }

    public void setEquipments(Map<EquipmentSlot, ItemStack> equipments) {
        this.equipments = equipments;
    }

    public MinionTemplate() {

    }
}

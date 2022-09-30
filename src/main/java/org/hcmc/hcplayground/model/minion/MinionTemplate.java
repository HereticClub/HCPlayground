package org.hcmc.hcplayground.model.minion;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.hcmc.hcplayground.enums.MinionCategory;
import org.hcmc.hcplayground.enums.MinionType;
import org.hcmc.hcplayground.manager.MinionManager;
import org.hcmc.hcplayground.model.item.CraftItemBase;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MinionTemplate extends CraftItemBase {

    @Expose
    @SerializedName(value = "display")
    private String display;
    @Expose
    @SerializedName(value = "texture")
    private String texture;
    @Expose
    @SerializedName(value = "platform")
    private Material platform = Material.AIR;
    /**
     * 树苗，仅LUMBERJACK使用
     */
    @Expose
    @SerializedName(value = "sapling")
    private Material sapling = Material.AIR;
    /**
     * 种子，仅FARMER使用
     */
    @Expose
    @SerializedName(value = "seed")
    private Material seed = Material.AIR;
    /**
     * 幼崽，仅BUTCHER和FIGHTER使用
     */
    @Expose
    @SerializedName(value = "cubs")
    private EntityType cubs = EntityType.PLAYER;
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
     * 爪牙升级需求，包含所需要物品的升级数量
     */
    @Expose(serialize = false, deserialize = false)
    private List<ItemStack> upgrade = new ArrayList<>();

    @Expose(serialize = false, deserialize = false)
    private int level;
    @Expose(serialize = false, deserialize = false)
    private MinionType type;

    public int getStorageAmount() {
        return storageAmount;
    }

    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public Material getSeed() {
        return seed;
    }

    public Material getSapling() {
        return sapling;
    }

    public EntityType getCubs() {
        return cubs;
    }

    @Override
    public void updateAttributeLore() {

    }

    @Override
    public ItemStack toItemStack() {
        return MinionManager.getMinionStack(type, level, amount);
    }

    public MinionType getType() {
        return type;
    }

    public void setType(MinionType type) {
        this.type = type;
    }

    public List<String> getLore() {
        return basicLore;
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

    @NotNull
    public Material getPlatform() {
        return platform == null ? Material.AIR : platform;
    }

    public MinionCategory getCategory() {
        return category;
    }


    public List<ItemStack> getUpgrade() {
        return upgrade;
    }

    public void setUpgrade(List<ItemStack> upgrade) {
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

    @Override
    public String toString() {
        return id;
    }

    public MinionTemplate() {

    }
}

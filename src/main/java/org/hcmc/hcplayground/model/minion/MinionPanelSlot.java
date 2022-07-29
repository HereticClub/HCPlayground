package org.hcmc.hcplayground.model.minion;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.hcmc.hcplayground.enums.MinionPanelSlotType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MinionPanelSlot {

    @Expose
    @SerializedName(value = "slots")
    private int[] slots;
    @Expose
    @SerializedName(value = "material")
    private Material material;
    @Expose
    @SerializedName(value = "name")
    private String display;
    @Expose
    @SerializedName(value = "lore")
    private List<String> lore = new ArrayList<>();
    @Expose
    @SerializedName(value = "upgrade_template")
    private String upgradeLore;

    @Expose(serialize = false, deserialize = false)
    private String id;
    @Expose(serialize = false, deserialize = false)
    private MinionPanelSlotType type;

    public String getId() {
        return id;
    }

    public List<String> getLore() {
        return lore;
    }

    public String getDisplay() {
        return display;
    }

    public int[] getSlots() {
        return slots;
    }

    public Material getMaterial() {
        return material;
    }

    public String getUpgradeLore() {
        return upgradeLore;
    }

    public MinionPanelSlotType getType() {
        return type;
    }

    public void setType(MinionPanelSlotType type) {
        this.type = type;
    }

    public MinionPanelSlot() {

    }

    @Override
    public String toString() {
        return String.format("%s, %s", display, material);
    }

    public Map<Integer, ItemStack> toItemStacks() {
        Map<Integer, ItemStack> stacks = new HashMap<>();
        for (int slot : slots) {
            ItemStack is = new ItemStack(material, 1);
            ItemMeta meta = is.getItemMeta();
            if (meta != null) {
                if (!StringUtils.isBlank(display)) meta.setDisplayName(display);
                meta.setLore(lore);
                is.setItemMeta(meta);
            }
            stacks.put(slot, is);
        }

        return stacks;
    }
}

package org.hcmc.hcplayground.model.recipe;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.hcmc.hcplayground.enums.PanelSlotType;
import org.hcmc.hcplayground.model.item.ItemBase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CraftPanelSlot {
    @Expose
    @SerializedName(value = "slots")
    private int[] slots;
    @Expose
    @SerializedName(value = "material")
    private ItemBase material;
    @Expose
    @SerializedName(value = "name")
    private String display;
    @Expose
    @SerializedName(value = "lore")
    private List<String> lore = new ArrayList<>();

    @Expose(serialize = false, deserialize = false)
    private String id;
    @Expose(serialize = false, deserialize = false)
    private PanelSlotType type;

    public String getId() {
        return id;
    }

    public PanelSlotType getType() {
        return type;
    }

    public void setType(PanelSlotType type) {
        this.type = type;
    }

    public int[] getSlots() {
        return slots;
    }

    public void setSlots(int[] slots) {
        this.slots = slots;
    }

    public CraftPanelSlot() {

    }

    @Override
    public String toString() {
        return String.format("%s, %s, %s", display, material, type);
    }

    public Map<Integer, ItemStack> toItemStacks() {
        Map<Integer, ItemStack> stacks = new HashMap<>();
        for (int slot : slots) {
            ItemStack is = material.toItemStack();
            if (is.getType().equals(Material.AIR)) continue;

            ItemMeta meta = is.getItemMeta();
            if (meta != null) {
                if (!StringUtils.isBlank(display)) meta.setDisplayName(display);
                List<String> _lore = new ArrayList<>();
                if (meta.getLore() != null) _lore.addAll(meta.getLore());
                _lore.addAll(lore);

                meta.setLore(_lore);
                is.setItemMeta(meta);
            }
            stacks.put(slot, is);
        }

        return stacks;
    }
}

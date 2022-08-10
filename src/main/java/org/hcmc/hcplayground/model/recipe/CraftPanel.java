package org.hcmc.hcplayground.model.recipe;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CraftPanel implements InventoryHolder {

    private Inventory inventory;
    private CrazyShapedRecipe recipe;
    private List<String> previewLore = new ArrayList<>();
    private List<CraftPanelSlot> slots = new ArrayList<>();
    /**
     * 放置在箱子界面中的物品堆叠和位置<br>
     * Integer - 物品在箱子界面的位置(插槽号)<br>
     * ItemStack - 物品堆叠的实例，包含数量
     */
    private Map<Integer, ItemStack> placedIngredients = new HashMap<>();

    public CraftPanel() {

    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public Map<Integer, ItemStack> getPlacedIngredients() {
        return placedIngredients;
    }

    public void setPlacedIngredients(Map<Integer, ItemStack> placedIngredients) {
        this.placedIngredients = placedIngredients;
    }

    public List<String> getPreviewLore() {
        return previewLore;
    }

    public void setPreviewLore(List<String> previewLore) {
        this.previewLore = previewLore;
    }

    public CrazyShapedRecipe getRecipe() {
        return recipe;
    }

    public void setRecipe(CrazyShapedRecipe recipe) {
        this.recipe = recipe;
    }

    public List<CraftPanelSlot> getSlots() {
        return slots;
    }

    public void setSlots(List<CraftPanelSlot> slots) {
        this.slots = slots;
    }

    public CraftPanelSlot getPanelSlot(int slotIndex) {
        if (slots == null) return null;
        return slots.stream().filter(x -> Arrays.stream(x.getSlots()).anyMatch(y -> y == slotIndex)).findAny().orElse(null);
    }
}

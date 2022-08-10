package org.hcmc.hcplayground.runnable;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.hcmc.hcplayground.enums.PanelSlotType;
import org.hcmc.hcplayground.manager.RecipeManager;
import org.hcmc.hcplayground.model.recipe.CraftPanel;
import org.hcmc.hcplayground.model.recipe.CraftPanelSlot;
import org.hcmc.hcplayground.model.recipe.CrazyShapedRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RecipeSearchRunnable extends BukkitRunnable {

    private final Inventory inventory;

    public RecipeSearchRunnable(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public void run() {
        if (!(inventory.getHolder() instanceof CraftPanel panel)) return;
        CraftPanelSlot slot = panel.getSlots().stream().filter(x -> x.getType().equals(PanelSlotType.OUTPUT)).findAny().orElse(null);
        if (slot == null) return;

        CrazyShapedRecipe recipe = RecipeManager.getRecipe(inventory);
        panel.setRecipe(recipe);

        if (recipe == null) {
            for (Map.Entry<Integer, ItemStack> entry : slot.toItemStacks().entrySet()) {
                inventory.setItem(entry.getKey(), entry.getValue());
            }
        } else {
            ItemStack result = recipe.getPreview(panel.getPreviewLore());
            for (int i : slot.getSlots()) {
                inventory.setItem(i, result);
            }
        }
    }
}

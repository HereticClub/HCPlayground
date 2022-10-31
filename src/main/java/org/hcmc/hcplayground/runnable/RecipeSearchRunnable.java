package org.hcmc.hcplayground.runnable;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.hcmc.hcplayground.enums.PanelSlotType;
import org.hcmc.hcplayground.manager.PlayerManager;
import org.hcmc.hcplayground.manager.RecipeManager;
import org.hcmc.hcplayground.model.player.PlayerData;
import org.hcmc.hcplayground.model.recipe.CraftPanel;
import org.hcmc.hcplayground.model.recipe.CraftPanelSlot;
import org.hcmc.hcplayground.model.recipe.CrazyShapedRecipe;

import java.util.Map;

public class RecipeSearchRunnable extends BukkitRunnable {

    private final Inventory inventory;
    private final Player player;

    public RecipeSearchRunnable(Player player, Inventory inventory) {
        this.player = player;
        this.inventory = inventory;
    }

    @Override
    public void run() {
        if (!(inventory.getHolder() instanceof CraftPanel panel)) return;
        CraftPanelSlot slot = panel.getSlots().stream().filter(x -> x.getType().equals(PanelSlotType.OUTPUT)).findAny().orElse(null);
        if (slot == null) return;

        PlayerData data = PlayerManager.getPlayerData(player);
        CrazyShapedRecipe recipe = RecipeManager.getRecipe(inventory);
        panel.setRecipe(recipe);

        if (recipe == null) {
            for (Map.Entry<Integer, ItemStack> entry : slot.toItemStacks().entrySet()) {
                inventory.setItem(entry.getKey(), entry.getValue());
            }
        } else {
            ItemStack result = data.isRecipeUnlocked(recipe.getId()) ? recipe.getPreview(panel.getPreviewLore()) : RecipeManager.getRecipeLockedItem();
            for (int i : slot.getSlots()) {
                inventory.setItem(i, result);
            }
        }
    }
}

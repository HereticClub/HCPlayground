package org.hcmc.hcplayground.runnable;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.hcmc.hcplayground.manager.RecipeManager;
import org.hcmc.hcplayground.model.menu.MenuDetail;
import org.hcmc.hcplayground.model.menu.MenuItem;
import org.hcmc.hcplayground.model.recipe.CrazyShapedRecipe;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RecipeFinderRunnable extends BukkitRunnable {

    private final Inventory inventory;

    public RecipeFinderRunnable(Inventory inv) {
        inventory = inv;
    }

    @Override
    public void run() {
        int top = 54;
        int left = 54;
        int bottom = -1;
        int right = -1;
        int cornerTopLeft = 0;
        //int cornerBottomRight = 0;
        int columnLength = 0;
        int rowLength = 0;
        int index = 0;
        boolean amountNotMatch = false;

        Map<Integer, ItemStack> mapBasic = new HashMap<>();
        Map<Integer, ItemStack> mapMatrix = new HashMap<>();

        InventoryHolder holder = inventory.getHolder();
        if (!(holder instanceof MenuDetail detail)) return;
        MenuItem mi = detail.decorates.stream().filter(x -> x.result).findFirst().orElse(null);
        if (mi == null || mi.numbers == null) return;
        for (int i : mi.numbers) {
            inventory.setItem(i - 1, null);
        }

        for (int i = 0; i <= 53; i++) {
            ItemStack is = inventory.getItem(i);
            if (is == null) continue;
            if (i % 9 >= 6) continue;

            int row = i / 9;
            int column = i % 9;
            if (top > row) top = row;
            if (left > column) left = column;
            if (bottom < row) bottom = row;
            if (right < column) right = column;
            cornerTopLeft = top * 9 + left;
            //cornerBottomRight = bottom * 9 + right;
            columnLength = right - left + 1;
            rowLength = bottom - top + 1;
        }

        for (int r = 0; r < rowLength; r++) {
            for (int c = 0; c < columnLength; c++) {
                int rc = cornerTopLeft + c + r * 9;
                ItemStack is = inventory.getItem(rc);
                mapMatrix.put(index, is);
                if (is == null) {
                    mapBasic.put(index, null);
                } else {
                    ItemStack isClone = is.clone();
                    isClone.setAmount(1);
                    mapBasic.put(index, isClone);
                }
                index++;
            }
        }

        CrazyShapedRecipe recipe = RecipeManager.getRecipes().stream().filter(x -> x.getBasicMatrix().equals(mapBasic)).findFirst().orElse(null);
        if (recipe == null) return;
        Map<Integer, ItemStack> matrixOnRecipe = recipe.getMatrix();
        if (matrixOnRecipe.isEmpty()) return;

        Set<Integer> count = mapBasic.keySet();
        for (int i : count) {
            ItemStack isOnTable = mapMatrix.get(i);
            ItemStack isOnRecipe = matrixOnRecipe.get(i);

            if (isOnTable == null || isOnRecipe == null) continue;
            if (isOnTable.getAmount() >= isOnRecipe.getAmount()) continue;

            amountNotMatch = true;
            break;
        }
        if (amountNotMatch) return;

        ItemStack is = recipe.getResult();
        for (int i : mi.numbers) {
            inventory.setItem(i - 1, is);
        }
    }
}

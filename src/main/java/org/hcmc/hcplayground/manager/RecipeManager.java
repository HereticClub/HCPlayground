package org.hcmc.hcplayground.manager;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ShapedRecipe;
import org.hcmc.hcplayground.model.item.ItemBase;
import org.hcmc.hcplayground.model.recipe.CrazyShapedRecipe;
import org.hcmc.hcplayground.utility.Global;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RecipeManager {

    private static List<CrazyShapedRecipe> recipes = new ArrayList<>();

    public RecipeManager() {

    }

    public static List<CrazyShapedRecipe> getRecipes() {
        return recipes;
    }

    public static void Load(YamlConfiguration yaml) throws IllegalAccessException {
        recipes = Global.SetItemList(yaml, CrazyShapedRecipe.class);
        for (CrazyShapedRecipe r : recipes) {
            Map<Character, ItemBase> ingredients = r.getIngredients();
            List<ItemBase> itemBases = ingredients.values().stream().toList();
            if (itemBases.stream().allMatch(x -> x.getId() == null) && r.getIngredientShape().length <= 3) {
                setLegacyRecipe(r);
            } else {
                r.setMatrix();
            }
        }
    }

    private static void setLegacyRecipe(CrazyShapedRecipe crazyRecipe) {
        ShapedRecipe recipe = new ShapedRecipe(crazyRecipe.getKey(), crazyRecipe.getResult());
        recipe.shape(crazyRecipe.getIngredientShape());
        Set<Character> symbols = crazyRecipe.getIngredients().keySet();
        for (Character c : symbols) {
            ItemBase ib = crazyRecipe.getIngredients().get(c);
            recipe.setIngredient(c, ib.toItemStack().getType());
            recipe.setGroup(crazyRecipe.getGroup());
        }
        Bukkit.addRecipe(recipe);
    }
}

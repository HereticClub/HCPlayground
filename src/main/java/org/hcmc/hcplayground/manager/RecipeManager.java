package org.hcmc.hcplayground.manager;

import org.bukkit.configuration.file.YamlConfiguration;
import org.hcmc.hcplayground.model.recipe.ShapedRecipe6x6;
import org.hcmc.hcplayground.utility.Global;

import java.util.ArrayList;
import java.util.List;

public class RecipeManager {

    private static List<ShapedRecipe6x6> recipes = new ArrayList<>();

    public RecipeManager() {

    }

    public static List<ShapedRecipe6x6> getRecipes() {
        return recipes;
    }

    public static void Load(YamlConfiguration yaml) throws IllegalAccessException {
        recipes = Global.SetItemList(yaml, ShapedRecipe6x6.class);
    }
}

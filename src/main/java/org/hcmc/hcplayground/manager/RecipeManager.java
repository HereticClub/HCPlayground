package org.hcmc.hcplayground.manager;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.hcmc.hcplayground.model.item.ItemBase;
import org.hcmc.hcplayground.model.item.ItemBaseA;
import org.hcmc.hcplayground.model.recipe.ShapedRecipe6x6;
import org.hcmc.hcplayground.utility.Global;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeManager {

    private static List<ShapedRecipe6x6> Recipes = new ArrayList<>();

    public RecipeManager() {

    }

    public static void Load(YamlConfiguration yaml) throws IllegalAccessException {
        /*
        ShapedRecipe6x6 x = new ShapedRecipe6x6();
        Map<Character, String> ingredients =new HashMap<>();
        ingredients.put('A', "ingredient1");
        ingredients.put('B', "ingredient2");
        x.setIngredients(ingredients);
        Map<Character, Integer> amount = new HashMap<>();
        amount.put('A', 10);
        amount.put('B', 5);
        x.setIngredientAmount(amount);
        ItemBase item = ItemManager.Create(Material.STONE);
        x.setResult((ItemBaseA) item);

        String value = Global.GsonObject.toJson(x);
        System.out.println(value);

         */

        Recipes = Global.SetItemList(yaml, ShapedRecipe6x6.class);
    }
}

package org.hcmc.hcplayground.manager;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.model.recipe.ShapedRecipe6x6;
import org.hcmc.hcplayground.utility.Global;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecipeManager {

    private static List<ShapedRecipe6x6> Recipes = new ArrayList<>();

    public RecipeManager() {

    }

    public static void Load(YamlConfiguration yaml) throws IllegalAccessException {
        Recipes = Global.SetItemList(yaml, ShapedRecipe6x6.class);
    }
}

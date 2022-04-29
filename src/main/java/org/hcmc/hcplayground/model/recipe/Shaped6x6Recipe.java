package org.hcmc.hcplayground.model.recipe;

import com.google.common.base.Preconditions;
import org.apache.commons.lang.Validate;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Shaped6x6Recipe implements Recipe, Keyed {

    private final ItemStack result;
    private final NamespacedKey key;
    private Map<Character, RecipeChoice> ingredients = new HashMap<>();
    private String[] shape;
    private String group = "";

    public Shaped6x6Recipe(@NotNull NamespacedKey key, @NotNull ItemStack result) {
        Preconditions.checkArgument(result.getType() != Material.AIR, "Recipe must have non-AIR result.");
        this.key = key;
        this.result = result;
    }

    @NotNull
    public NamespacedKey getKey() {
        return key;
    }

    public String[] getShape() {
        return shape;
    }

    public Map<Character, RecipeChoice> getIngredients() {
        return ingredients;
    }

    @NotNull
    public String getGroup() {
        return this.group;
    }

    public void setGroup(@NotNull String value) {
        this.group = value;
    }

    @NotNull
    public Recipe shape(@NotNull String... shape) {
        Validate.notNull(shape, "Must provide a shape");
        Validate.isTrue(shape.length >= 1 && shape.length <= 6, "Crafting recipes should be 1, 2, 3, 4, 5, 6 rows, not ", shape.length);

        this.shape = shape;
        int lastLen = -1;
        HashMap<Character, RecipeChoice> newIngredients = new HashMap<>();

        for (String row : shape) {
            Validate.notNull(row, "Shape cannot have null rows");
            Validate.isTrue(row.length() >= 1 && row.length() <= 6, "Crafting rows should be 1, 2, 3, 4, 5, 6 characters, not ", row.length());
            Validate.isTrue(lastLen == -1 || lastLen == row.length(), "Crafting recipes must be rectangular");
            lastLen = row.length();

            char[] symbols = row.toCharArray();
            for (char c : symbols) {
                newIngredients.put(c, this.ingredients.get(c));
            }
        }

        this.ingredients = newIngredients;
        return this;
    }



    @NotNull
    public Recipe setIngredient(char key, @NotNull Material ingredient) {
        Validate.isTrue(this.ingredients.containsKey(key), "Symbol does not appear in the shape:", key);

        this.ingredients.put(key, new RecipeChoice.MaterialChoice(ingredient));
        return this;
    }

    @NotNull
    public Recipe setIngredient(char key, @NotNull RecipeChoice ingredient) {
        Validate.isTrue(this.ingredients.containsKey(key), "Symbol does not appear in the shape:", key);
        this.ingredients.put(key, ingredient);

        return this;
    }

    @NotNull
    public Map<Character, ItemStack> getIngredientMap() {
        HashMap<Character, ItemStack> result = new HashMap<>();

        for (Map.Entry<Character, RecipeChoice> ingredient : this.ingredients.entrySet()) {
            if (ingredient.getValue() == null) {
                result.put(ingredient.getKey(), null);
            } else {
                result.put(ingredient.getKey(), ingredient.getValue().getItemStack());
            }
        }
        return result;
    }

    @NotNull
    public Map<Character, RecipeChoice> getChoiceMap() {
        Map<Character, RecipeChoice> result = new HashMap<>();

        for (Map.Entry<Character, RecipeChoice> ingredient : this.ingredients.entrySet()) {
            if (ingredient.getValue() == null) {
                result.put(ingredient.getKey(), null);
            } else {
                result.put(ingredient.getKey(), ingredient.getValue().clone());
            }
        }

        return result;
    }

    @NotNull
    @Override
    public ItemStack getResult() {
        return result;
    }
}

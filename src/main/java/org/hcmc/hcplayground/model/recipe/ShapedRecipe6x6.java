package org.hcmc.hcplayground.model.recipe;

import com.google.common.base.Preconditions;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.hcmc.hcplayground.model.item.ItemBase;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ShapedRecipe6x6 implements Recipe {

    @Expose
    @SerializedName(value = "result")
    private final ItemBase result;
    @Expose
    @SerializedName(value = "name")
    private final NamespacedKey key;
    @Expose
    @SerializedName(value = "ingredients")
    private Map<Character, ItemStack> ingredients = new HashMap<>();
    @Expose
    @SerializedName(value = "amount")
    private Map<Character, Integer> ingredientAmount = new HashMap<>();
    @Expose
    @SerializedName(value = "shape")
    private String[] shape;
    @Expose
    @SerializedName(value = "group")
    private String group = "";

    @Expose(serialize = false, deserialize = false)
    private String id;

    public ShapedRecipe6x6(@NotNull NamespacedKey key, @NotNull ItemBase result) {
        Preconditions.checkArgument(result.toItemStack().getType() != Material.AIR, "Recipe must have non-AIR result.");

        this.key = key;
        this.result = result;
    }

    @NotNull
    public NamespacedKey getKey() {
        return key;
    }

    @NotNull
    public String[] getShape() {
        return shape;
    }

    @NotNull
    public Map<Character, ItemStack> getIngredients() {
        return ingredients;
    }

    @NotNull
    public Map<Character, Integer> getIngredientAmount() {
        return ingredientAmount;
    }

    @NotNull
    public String getGroup() {
        return this.group;
    }

    public void setGroup(@NotNull String value) {
        this.group = value;
    }

    public void shape(@NotNull String... shape) {
        Validate.notNull(shape, "Must provide a shape");
        Validate.isTrue(shape.length >= 1 && shape.length <= 6, "Crafting recipes should be 1, 2, 3, 4, 5, 6 rows, not ", shape.length);

        this.shape = shape;
        int lastLen = -1;
        HashMap<Character, ItemStack> newIngredients = new HashMap<>();

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
    }

    public void setIngredientAmount(Map<Character, Integer> ingredientAmount) {
        this.ingredientAmount = ingredientAmount;
    }

    public void setAmount(char key, int amount) {
        Validate.isTrue(this.ingredients.containsKey(key), "Symbol does not appear in the shape:", key);
        this.ingredientAmount.put(key, amount);
    }

    public void setIngredients(Map<Character, ItemStack> value) {
        this.ingredients = value;
    }

    public void setIngredient(char key, @NotNull Material ingredient, int amount) {
        Validate.isTrue(this.ingredients.containsKey(key), "Symbol does not appear in the shape:", key);
        this.ingredients.put(key, new ItemStack(ingredient, amount));
    }

    public void setIngredients(char key, ItemStack ingredient) {
        Validate.isTrue(this.ingredients.containsKey(key), "Symbol does not appear in the shape:", key);
        this.ingredients.put(key, ingredient);
    }

    public void setIngredients(char key, ItemBase ingredient) {
        Validate.isTrue(this.ingredients.containsKey(key), "Symbol does not appear in the shape:", key);
        this.ingredients.put(key, ingredient.toItemStack());
    }

    @NotNull
    @Override
    public ItemStack getResult() {
        return result.toItemStack();
    }
}

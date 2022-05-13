package org.hcmc.hcplayground.model.recipe;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.manager.ItemManager;
import org.hcmc.hcplayground.model.item.ItemBase;
import org.hcmc.hcplayground.model.item.ItemBaseA;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ShapedRecipe6x6 implements Recipe {

    @Expose
    @SerializedName(value = "result")
    private ItemBase result;
    @Expose
    @SerializedName(value = "name")
    private final NamespacedKey key;
    @Expose
    @SerializedName(value = "shape")
    private List<String> shape = new ArrayList<>();
    @Expose
    @SerializedName(value = "ingredients")
    private List<String> ingredients =new ArrayList<>();
    @Expose
    @SerializedName(value = "amount")
    private List<String> ingredientAmount = new ArrayList<>();
    @Expose
    @SerializedName(value = "group")
    private String group = "";

    @Expose(serialize = false, deserialize = false)
    private String id;

    public ShapedRecipe6x6() {
        // TODO:
        String uuid = UUID.randomUUID().toString();
        key = new NamespacedKey(HCPlayground.getPlugin(), uuid);
        result = ItemManager.createItemBase("NewRecipe", Material.STONE);

    }

    @NotNull
    public NamespacedKey getKey() {
        return key;
    }

    @NotNull
    public List<String> getShape() {
        return shape;
    }

    @NotNull
    public List<String> getIngredients() {
        return ingredients;
    }

    @NotNull
    public List<String> getIngredientAmount() {
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

        this.shape = Arrays.stream(shape).toList();
        int lastLen = -1;

        HashMap<Character, String> newIngredients = new HashMap<>();

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


    }
    /*
    public void setIngredientAmount(Map<Character, Integer> ingredientAmount) {
        this.ingredientAmount = ingredientAmount;
    }


    public void setIngredientAmount(char key, int amount) {
        Validate.isTrue(this.ingredients.containsKey(key), "Symbol does not appear in the shape:", key);
        this.ingredientAmount.put(key, amount);
    }

    public void setIngredients(Map<Character, String> value) {
        this.ingredients = value;
    }

    public void setIngredients(Character key, String ingredient) {
        Validate.isTrue(this.ingredients.containsKey(key), "Symbol does not appear in the shape:", key);
        this.ingredients.put(key, ingredient);
    }

    public void setResult(ItemBaseA item) {
        result = item;
    }*/

    @NotNull
    @Override
    public ItemStack getResult() {
        return result.toItemStack();
    }
}

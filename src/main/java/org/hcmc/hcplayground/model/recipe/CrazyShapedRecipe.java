package org.hcmc.hcplayground.model.recipe;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.Validate;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.Plugin;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.model.item.ItemBase;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * 6x6配方类
 */
public class CrazyShapedRecipe implements Recipe {
    /**
     * 成品
     */
    @Expose
    @SerializedName(value = "result")
    private ItemBase result;
    /**
     * 成品数量
     */
    @Expose
    @SerializedName(value = "result_amount")
    private int resultAmount = 1;
    /**
     * 配方名称
     */
    @Expose
    @SerializedName(value = "name")
    private NamespacedKey key;
    /**
     * 成分列表
     */
    @Expose
    @SerializedName(value = "ingredients")
    private final Map<Character, ItemBase> ingredients = new HashMap<>();
    /**
     * 成分排列形状
     */
    @Expose
    @SerializedName(value = "shape")
    private String[] ingredientShape;
    /**
     * 成分需求数量
     */
    @Expose
    @SerializedName(value = "amount")
    private final Map<Character, Integer> ingredientAmount = new HashMap<>();
    /**
     * 配方组
     */
    @Expose
    @SerializedName(value = "group")
    private final String group = "";
    // 以下变量不会参与序列化和反序列化
    private String id;
    private final Plugin plugin = HCPlayground.getInstance();
    /**
     * 成分的矩阵图(数组)，成分的数量按照实际的配置数量填充
     */
    private final Map<Integer, ItemStack> matrix = new HashMap<>();
    /**
     * 成分的矩阵图(数组)，所有成分的数量均为1
     */
    private final Map<Integer, ItemStack> basicMatrix = new HashMap<>();

    public CrazyShapedRecipe() {

    }

    public Map<Character, ItemBase> getIngredients() {
        return ingredients;
    }

    public Map<Integer, ItemStack> getMatrix() {
        return matrix;
    }

    public Map<Integer, ItemStack> getBasicMatrix() {
        return basicMatrix;
    }

    public String[] getIngredientShape() {
        return ingredientShape;
    }

    public NamespacedKey getKey() {
        return key;
    }

    public String getGroup() {
        return group;
    }

    public void setMatrix() {
        Validate.notNull(ingredientShape, "Must provide a ingredient shape, and the shape should be square.");
        Validate.isTrue(ingredientShape.length >= 1 && ingredientShape.length <= 6, "Crazy Crafting Recipes should be 1, 2, 3, 4, 5, 6 rows, not ", ingredientShape.length);

        int rowLength = -1;
        int matrixIndex = -1;
        this.matrix.clear();
        this.basicMatrix.clear();

        for (String s : ingredientShape) {
            Validate.notNull(s, "Shape can not have null rows");
            Validate.isTrue(s.length() >= 1 && s.length() <= 6, "Crafting rows should be 1, 2, 3, 4, 5, 6 character, not ", s.length());
            Validate.isTrue(rowLength == -1 || rowLength == s.length(), "Crazy Crafting Recipes should be square");
            rowLength = s.length();

            char[] symbols = s.toCharArray();
            for (char c : symbols) {
                ItemBase ib = ingredients.get(c);
                int amount = ingredientAmount.getOrDefault(c, -1);
                matrixIndex++;

                if (ib != null) {
                    ItemStack isItem = ib.toItemStack();
                    ItemStack isClone = ib.toItemStack().clone();
                    isItem.setAmount(amount);
                    isClone.setAmount(1);
                    matrix.put(matrixIndex, isItem);
                    basicMatrix.put(matrixIndex, isClone);
                } else {
                    matrix.put(matrixIndex, null);
                    basicMatrix.put(matrixIndex, null);
                }
            }
        }
    }

    @NotNull
    @Override
    public ItemStack getResult() {
        if (resultAmount <= 0) resultAmount = 1;
        if (resultAmount > 64) resultAmount = 64;

        ItemStack is = result.toItemStack();
        is.setAmount(resultAmount);
        return is;
    }
}

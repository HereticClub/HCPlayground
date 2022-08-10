package org.hcmc.hcplayground.model.recipe;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.manager.ItemManager;
import org.hcmc.hcplayground.model.item.ItemBase;
import org.jetbrains.annotations.NotNull;

import java.util.*;

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
     * 成分排列形状
     */
    @Expose
    @SerializedName(value = "shape")
    private List<String> ingredientShape = new ArrayList<>();
    /**
     * 成分列表
     */
    @Expose
    @SerializedName(value = "ingredients")
    private Map<Character, ItemBase> ingredients = new HashMap<>();
    /**
     * 成分需求数量
     */
    @Expose
    @SerializedName(value = "amount")
    private Map<Character, Integer> ingredientAmount = new HashMap<>();
    /**
     * 配方组
     */
    @Expose
    @SerializedName(value = "group")
    private final String group = "";

    // 以下变量不会参与序列化和反序列化
    @Expose(serialize = false, deserialize = false)
    private String id;
    @Expose(serialize = false, deserialize = false)
    private final Plugin plugin = HCPlayground.getInstance();
    /**
     * 配方的成分及数量的需求列表，由ingredients属性转换
     */
    private List<ItemStack> requirements = new ArrayList<>();

    public CrazyShapedRecipe() {

    }

    public Map<Character, ItemBase> getIngredients() {
        return ingredients;
    }

    public List<String> getIngredientShape() {
        return ingredientShape;
    }

    public Map<Character, Integer> getIngredientAmount() {
        return ingredientAmount;
    }

    public void setIngredients(Map<Character, ItemBase> ingredients) {
        this.ingredients = ingredients;
    }

    public List<ItemStack> getRequirements() {
        return requirements;
    }

    public void setRequirements(List<ItemStack> requirements) {
        this.requirements = requirements;
    }

    public NamespacedKey getKey() {
        return key;
    }

    public String getGroup() {
        return group;
    }

    public void prepareCrazyRecipe() {
        // 验证，成分摆放形状不能null
        int rowLength = -1;
        Validate.notNull(ingredientShape, "Must provide a ingredient shape, and the shape should be square.");
        Validate.isTrue(ingredientShape.size() >= 1 && ingredientShape.size() <= 6, "Crazy Crafting Recipes should be 1, 2, 3, 4, 5, 6 rows, not ", ingredientShape.size());

        this.requirements.clear();

        // 验证，成分摆放形状必须是矩形
        for (String line : ingredientShape) {
            Validate.notNull(line, "Shape can not have null rows");
            Validate.isTrue(line.length() >= 1 && line.length() <= 6, "Crafting rows should be 1, 2, 3, 4, 5, 6 character, not ", line.length());
            Validate.isTrue(rowLength == -1 || rowLength == line.length(), "Crazy Crafting Recipes should be square");
            rowLength = line.length();

            for (Character row : line.toCharArray()) {
                ItemBase ib = ingredients.get(row);
                if (ib == null) {
                    ib = ItemManager.createItemBase(Material.AIR, 1);
                } else {
                    ib.setAmount(ingredientAmount.get(row));
                }
                requirements.add(ib.toItemStack());
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

    public void claimAll(Player player, Inventory inventory) {
        if (!(inventory.getHolder() instanceof CraftPanel panel)) return;
        List<ItemStack> results = new ArrayList<>();
        boolean isItemEmpty = false;

        outer:
        while (!isItemEmpty) {
            // 获取已经摆放好的物品
            Map<Integer, ItemStack> placedIngredients = panel.getPlacedIngredients();
            // 检测成分的数量，少于需求则马上退出循环
            for (Map.Entry<Integer, ItemStack> entry : placedIngredients.entrySet()) {
                Map.Entry<Character, ItemBase> ee = ingredients.entrySet().stream().filter(x -> x.getValue().toItemStack().isSimilar(entry.getValue())).findAny().orElse(null);
                if (ee == null) continue;
                // 现有的物品堆叠实例
                ItemStack isExisting = entry.getValue();
                // 需求的数量
                int amountRequire = ingredientAmount.get(ee.getKey());
                // 现有的数量
                int amountExisting = isExisting.getAmount();
                // 现有数量少于配方需求数量，跳出循环
                if (!(amountExisting >= amountRequire)) break outer;
            }
            // 通过成分数量的检测后，才减去配方台里面的物品堆叠的需求数量
            for (Map.Entry<Integer, ItemStack> entry : placedIngredients.entrySet()) {
                Map.Entry<Character, ItemBase> ee = ingredients.entrySet().stream().filter(x -> x.getValue().toItemStack().isSimilar(entry.getValue())).findAny().orElse(null);
                if (ee == null) continue;

                ItemStack isExisting = entry.getValue();
                int amountRequire = ingredientAmount.get(ee.getKey());
                int amountExisting = isExisting.getAmount();
                // 减掉现有成分物品堆叠的数量
                isExisting.setAmount(amountExisting - amountRequire);
                inventory.setItem(entry.getKey(), isExisting);
                // 当物品堆叠的数量为0，则物品会被设置为AIR
                // 需要执行完最后一次成品获取，才能退出循环
                if (isExisting.getType().equals(Material.AIR) && isExisting.getAmount() <= 0) isItemEmpty = true;
            }
            // 获得配方成品及数量
            results.add(getResult());
        }
        player.getInventory().addItem(results.toArray(new ItemStack[0]));
    }

    public void claimResult(Player player, Inventory inventory) {
        // Player.getItemOnCursor()
        // 运行在runnable之外，表示获取鼠标点击箱子界面前的物品
        // 运行在runnable之内，表示获取鼠标点击箱子界面后的物品
        ItemStack cursor = player.getItemOnCursor();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!(inventory.getHolder() instanceof CraftPanel panel)) return;

                Map<Integer, ItemStack> placedIngredients = panel.getPlacedIngredients();
                for (Map.Entry<Integer, ItemStack> entry : placedIngredients.entrySet()) {
                    Map.Entry<Character, ItemBase> ee = ingredients.entrySet().stream().filter(x -> x.getValue().toItemStack().isSimilar(entry.getValue())).findAny().orElse(null);
                    if (ee == null) continue;

                    int amount = ingredientAmount.get(ee.getKey());
                    ItemStack is = entry.getValue();
                    is.setAmount(is.getAmount() - amount);
                    inventory.setItem(entry.getKey(), is);
                }

                ItemStack result = getResult();
                if (result.isSimilar(cursor)) {
                    cursor.setAmount(cursor.getAmount() + result.getAmount());
                    player.setItemOnCursor(cursor);
                } else {
                    player.getInventory().addItem(cursor);
                    player.setItemOnCursor(result);
                }
            }
        }.runTask(plugin);
    }

    public ItemStack getPreview(List<String> extraLore) {
        ItemStack is = getResult().clone();
        ItemMeta meta = is.getItemMeta();
        if (meta == null) return is;

        List<String> _lore = new ArrayList<>();
        if (meta.getLore() != null) _lore.addAll(meta.getLore());
        if (extraLore != null) _lore.addAll(extraLore);
        meta.setLore(_lore);
        is.setItemMeta(meta);

        return is;
    }
}

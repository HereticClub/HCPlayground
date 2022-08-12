package org.hcmc.hcplayground.model.mmo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.StringUtils;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.hcmc.hcplayground.manager.ItemManager;
import org.hcmc.hcplayground.manager.LanguageManager;
import org.hcmc.hcplayground.manager.RecipeManager;
import org.hcmc.hcplayground.model.item.ItemBase;
import org.hcmc.hcplayground.model.recipe.CrazyShapedRecipe;

import java.util.ArrayList;
import java.util.List;

public class MMOReward {
    @Expose
    @SerializedName(value = "money")
    private float money;
    @Expose
    @SerializedName(value = "health")
    private float health;
    @Expose
    @SerializedName(value = "armor")
    private float armor;
    @Expose
    @SerializedName(value = "damage")
    private float damage;
    @Expose
    @SerializedName(value = "critical")
    private float critical;
    @Expose
    @SerializedName(value = "critical-damage")
    private float criticalDamage;
    @Expose
    @SerializedName(value = "fortune")
    private float fortune;
    @Expose
    @SerializedName(value = "point")
    private int point;
    @Expose
    @SerializedName(value = "items")
    private List<String> itemIds = new ArrayList<>();
    @Expose
    @SerializedName(value = "recipes")
    private List<String> recipeIds = new ArrayList<>();
    @Expose
    @SerializedName(value = "particles")
    private List<String> particleIds = new ArrayList<>();
    @Expose
    @SerializedName(value = "lore")
    private List<String> lore = new ArrayList<>();

    @Expose(deserialize = false)
    private String id;
    @Expose(deserialize = false)
    private final List<ItemStack> itemStacks = new ArrayList<>();
    @Expose(deserialize = false)
    private final List<CrazyShapedRecipe> crazyRecipes = new ArrayList<>();

    public MMOReward() {

    }

    public String getId() {
        return id;
    }

    public List<String> getLore() {
        return lore;
    }

    public float getArmor() {
        return armor;
    }

    public float getCritical() {
        return critical;
    }

    public float getCriticalDamage() {
        return criticalDamage;
    }

    public float getDamage() {
        return damage;
    }

    public float getFortune() {
        return fortune;
    }

    public float getHealth() {
        return health;
    }

    public float getMoney() {
        return money;
    }

    public int getPoint() {
        return point;
    }

    public List<String> getItemIds() {
        return itemIds;
    }

    public List<String> getRecipeIds() {
        return recipeIds;
    }

    public List<String> getParticleIds() {
        return particleIds;
    }

    public List<CrazyShapedRecipe> getCrazyRecipes() {
        return crazyRecipes;
    }

    public List<ItemStack> getItemStacks() {
        return itemStacks;
    }

    public void initialize() {
        if (lore == null) lore = new ArrayList<>();
        addMoney();
        addPoint();
        addHealth();
        addArmor();
        addFortune();
        addDamage();
        addCritical();
        addCriticalDamage();
        addRecipe();
        addItemStack();
    }

    @Override
    public String toString() {
        return id;
    }

    private void addHealth() {
        if (health <= 0) return;
        lore.add(String.format("%s§a+%s", LanguageManager.getString("reword.health"), health));
    }

    private void addArmor() {
        if (armor <= 0) return;
        lore.add(String.format("%s§a+%s", LanguageManager.getString("reword.armor"), armor));
    }
    private void addDamage() {
        if (damage <= 0) return;
        lore.add(String.format("%s§a+%s", LanguageManager.getString("reword.damage"), damage));
    }
    private void addCriticalDamage() {
        if (criticalDamage <= 0) return;
        lore.add(String.format("%s§a+%s", LanguageManager.getString("reword.crit-damage"), criticalDamage));
    }
    private void addCritical() {
        if (critical <= 0) return;
        lore.add(String.format("%s§a+%s", LanguageManager.getString("reword.crit"), critical));
    }
    private void addFortune() {
        if (fortune <= 0) return;
        lore.add(String.format("%s§a+%s", LanguageManager.getString("reword.fortune"), fortune));
    }
    private void addMoney(){
        if (money <= 0) return;
        lore.add(String.format("%s§a+%s", LanguageManager.getString("reword.money"), money));
    }
    private void addPoint(){
        if (point <= 0) return;
        lore.add(String.format("%s§a+%s", LanguageManager.getString("reword.point"), point));
    }


    private void addRecipe() {
        for (String s : recipeIds) {
            CrazyShapedRecipe recipe = RecipeManager.getRecipe(s);
            if (recipe == null) continue;
            crazyRecipes.add(recipe);

            lore.add(String.format("%s%s", LanguageManager.getString("reword.recipe"), recipe.getDisplay()));
        }
    }

    private void addItemStack() {
        for (String s : itemIds) {
            String[] keys = s.split(",");
            if (keys.length <= 1) continue;
            if (!StringUtils.isNumeric(keys[1])) continue;

            String id = keys[0];
            int amount = Integer.parseInt(keys[1]);

            ItemBase ib = ItemManager.findItemById(id);
            if (ib == null) ib = ItemManager.createItemBase(id, amount);
            ItemStack itemStack = ib.toItemStack();
            itemStack.setAmount(amount);
            itemStacks.add(itemStack);

            ItemMeta meta = itemStack.getItemMeta();
            if (meta == null) continue;

            String display = StringUtils.isBlank(ib.getId()) ? itemStack.getType().name() : meta.getDisplayName();
            lore.add(String.format("§f %s §7x §a%s", display, amount));
        }
    }
}

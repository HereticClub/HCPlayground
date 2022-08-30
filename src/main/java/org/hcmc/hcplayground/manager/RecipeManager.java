package org.hcmc.hcplayground.manager;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.hcmc.hcplayground.enums.PanelSlotType;
import org.hcmc.hcplayground.model.recipe.CraftPanel;
import org.hcmc.hcplayground.model.recipe.CraftPanelSlot;
import org.hcmc.hcplayground.model.recipe.CrazyShapedRecipe;
import org.hcmc.hcplayground.serialization.PanelSlotTypeSerialization;
import org.hcmc.hcplayground.utility.Global;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class RecipeManager {

    private static List<CrazyShapedRecipe> recipes = new ArrayList<>();
    private static ConfigurationSection craftPanelSection;
    private static ConfigurationSection anvilPanelSection;
    private static ConfigurationSection enchantPanelSection;
    private static ConfigurationSection barrierItemSection;
    private static ConfigurationSection recipeLockedSection;

    private static final List<String> idList = new ArrayList<>();

    public RecipeManager() {

    }

    public static List<CrazyShapedRecipe> getRecipes() {
        return recipes;
    }

    public static void Load(YamlConfiguration yaml) throws IllegalAccessException {
        ConfigurationSection recipeSection = yaml.getConfigurationSection("recipes");
        if (recipeSection != null) recipes = Global.deserializeList(recipeSection, CrazyShapedRecipe.class);

        idList.clear();

        craftPanelSection = yaml.getConfigurationSection("crafting_panel");
        anvilPanelSection = yaml.getConfigurationSection("anvil_panel");
        enchantPanelSection = yaml.getConfigurationSection("enchanting_panel");
        barrierItemSection = yaml.getConfigurationSection("barrier_item");
        recipeLockedSection = yaml.getConfigurationSection("recipe_locked");

        for (CrazyShapedRecipe r : recipes) {
            // 全部成分都是普通Material
            // 所有成分数量=1
            // 成分形状边长<=3
            // 则添加到传统配方
            idList.add(r.getId().toLowerCase());
            if (r.isLegacy()) {
                r.setLegacyRecipe();
            } else {
                r.prepareCrazyRecipe();
            }
        }
    }

    public static List<String> getIdList() {
        return idList;
    }

    public static Inventory createEnchantingPanel() {
        ConfigurationSection buttonsSection = enchantPanelSection.getConfigurationSection("buttons");
        if (buttonsSection == null) return null;
        String title = enchantPanelSection.getString("title");
        title = StringUtils.isBlank(title) ? "&6&lCrazy &9Enchanting Table" : title.replace("&", "§");
        int size = 54;

        Inventory inventory = Bukkit.createInventory(null, size, title);
        // TODO: Create enchanting table here
        System.out.println("Create enchanting table here");

        return inventory;
    }

    public static Inventory createAnvilPanel() {
        ConfigurationSection buttonsSection = anvilPanelSection.getConfigurationSection("buttons");
        if (buttonsSection == null) return null;
        String title = anvilPanelSection.getString("title");
        title = StringUtils.isBlank(title) ? "&6&lCrazy &9Anvil Table" : title.replace("&", "§");
        int size = 54;

        Inventory inventory = Bukkit.createInventory(null, size, title);
        // TODO: Create anvil table here
        System.out.println("Create anvil table here");

        return inventory;
    }

    /**
     * 创建疯狂合成(6x6)菜单(箱子界面)
     */
    public static Inventory createCraftPanel() {
        ConfigurationSection decorationSection = craftPanelSection.getConfigurationSection("decoration");
        if (decorationSection == null) return null;
        String title = craftPanelSection.getString("title");
        title = StringUtils.isBlank(title) ? "&6&lCrazy &9Crafting Table" : title.replace("&", "§");
        int size = 54;

        CraftPanel panel = new CraftPanel();
        Inventory inventory = Bukkit.createInventory(panel, size, title);
        List<String> previewLore = craftPanelSection.getStringList("preview_extra_lore");
        List<CraftPanelSlot> slots = Global.deserializeList(decorationSection, CraftPanelSlot.class);
        previewLore.replaceAll(x -> x.replace("&", "§"));

        panel.setInventory(inventory);
        panel.setPreviewLore(previewLore);
        panel.setSlots(slots);

        for (CraftPanelSlot slot : slots) {
            // id 的第二段表示slot的动作类型
            String[] id = slot.getId().split("\\.");
            PanelSlotType type = PanelSlotTypeSerialization.resolveType(id[1]);
            slot.setType(type == null ? PanelSlotType.INACTIVE : type);
            // 根据slots定义转换成为ItemStack
            Map<Integer, ItemStack> maps = slot.toItemStacks();
            // 摆放箱子界面
            Set<Integer> keys = maps.keySet();
            for (int key : keys) {
                inventory.setItem(key, maps.get(key));
            }
        }

        return inventory;
    }

    public static boolean existRecipe(String id) {
        return recipes.stream().anyMatch(x -> x.getId().equalsIgnoreCase(id));
    }

    public static CrazyShapedRecipe getRecipe(String id) {
        return recipes.stream().filter(x -> x.getId().equalsIgnoreCase(id)).findAny().orElse(null);
    }

    public static CrazyShapedRecipe getRecipe(@NotNull Inventory inventory) {
        List<ItemStack> requirements = getRequirements(inventory);
        if (requirements.size() <= 0) return null;

        List<CrazyShapedRecipe> filterRecipes = recipes.stream().filter(x -> x.getRequirements().size() == requirements.size()).toList();
        for (CrazyShapedRecipe recipe : filterRecipes) {
            List<ItemStack> ingredients = recipe.getRequirements();
            int count = ingredients.size();
            boolean assumeFound = true;
            for (int i = 0; i < count; i++) {
                ItemStack isIngredient = ingredients.get(i);
                ItemStack isRequirement = requirements.get(i);

                boolean similar = isIngredient.isSimilar(isRequirement);
                int delta = isRequirement.getAmount() - isIngredient.getAmount();
                assumeFound = similar && delta >= 0;
                if (!assumeFound) break;
            }

            if (assumeFound) return recipe;
        }

        return null;
    }

    public static ItemStack getRecipeLockedItem() {
        String _material = recipeLockedSection.getString("material");
        String _title = recipeLockedSection.getString("name");
        List<String> _lore = recipeLockedSection.getStringList("lore");
        return createBlockedItemStack(_title, _material, _lore);
    }

    public static ItemStack getBarrierItem() {
        String _material = barrierItemSection.getString("material");
        String _title = barrierItemSection.getString("name");
        List<String> _lore = barrierItemSection.getStringList("lore");
        return createBlockedItemStack(_title, _material, _lore);
    }

    private static ItemStack createBlockedItemStack(String display, String material, List<String> lore){
        lore.replaceAll(x -> x.replace("&", "§"));

        String _material = StringUtils.isBlank(material) ? "BARRIER" : material.toUpperCase();
        String _title = StringUtils.isBlank(display) ? "§cCrazy Item" : display.replace("&", "§");

        Material m = Material.valueOf(_material);
        ItemStack result = new ItemStack(m, 1);
        ItemMeta meta = result.getItemMeta();
        if (meta == null) return result;

        meta.setDisplayName(_title);
        meta.setLore(lore);
        result.setItemMeta(meta);
        return result;
    }

    @NotNull
    private static List<ItemStack> getRequirements(@NotNull Inventory inventory) {
        if (!(inventory.getHolder() instanceof CraftPanel panel)) return new ArrayList<>();

        int top = 54;
        int left = 54;
        int bottom = -1;
        int right = -1;
        for (int i = 0; i <= 53; i++) {
            ItemStack is = inventory.getItem(i);
            if (is == null) continue;
            if (i % 9 >= 6) continue;

            int row = i / 9;
            int column = i % 9;
            if (top > row) top = row;
            if (left > column) left = column;
            if (bottom < row) bottom = row;
            if (right < column) right = column;
        }

        int columnLength = right - left + 1;
        int rowLength = bottom - top + 1;
        int cornerTopLeft = top * 9 + left;
        List<ItemStack> requirements = new ArrayList<>();
        Map<Integer, ItemStack> ingredients = new HashMap<>();
        for (int r = 0; r < rowLength; r++) {
            for (int c = 0; c < columnLength; c++) {
                int rc = cornerTopLeft + c + r * 9;

                ItemStack itemStack = inventory.getItem(rc);
                if (itemStack == null) itemStack = new ItemStack(Material.AIR, 1);
                requirements.add(itemStack);
                ingredients.put(rc, itemStack);
            }
        }
        panel.setPlacedIngredients(ingredients);
        return requirements;
    }
}

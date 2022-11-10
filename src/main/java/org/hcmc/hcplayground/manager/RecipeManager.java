package org.hcmc.hcplayground.manager;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.enums.PanelSlotType;
import org.hcmc.hcplayground.model.item.ItemBase;
import org.hcmc.hcplayground.model.recipe.CraftPanel;
import org.hcmc.hcplayground.model.recipe.CraftPanelSlot;
import org.hcmc.hcplayground.model.recipe.CrazyShapedRecipe;
import org.hcmc.hcplayground.serialization.PanelSlotTypeSerialization;
import org.hcmc.hcplayground.utility.Global;
import org.hcmc.hcplayground.utility.YamlFileFilter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;

public class RecipeManager {
    private static final Plugin plugin = HCPlayground.getInstance();
    private static final String PATH_RECIPE_CONFIGURE = String.format("%s/recipe", plugin.getDataFolder());
    private static final String SECTION_RECIPES = "recipes";
    private static final List<CrazyShapedRecipe> recipes = new ArrayList<>();
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

    public static void Load() throws IllegalAccessException {
        try {
            recipes.clear();
            idList.clear();

            File dir = new File(PATH_RECIPE_CONFIGURE);
            FilenameFilter filter = new YamlFileFilter();
            String[] filenames = dir.list(filter);
            if (filenames == null) return;
            Arrays.sort(filenames);

            for (String file : filenames) {
                // 获取路径内每个yaml文档
                YamlConfiguration yaml = new YamlConfiguration();
                yaml.load(String.format("%s/%s", PATH_RECIPE_CONFIGURE, file));
                // 以下节段只获取第一次获得的节段实例，即使在不同的yaml文档中出现多次
                if (craftPanelSection == null) craftPanelSection = yaml.getConfigurationSection("crafting_panel");
                if (anvilPanelSection == null) anvilPanelSection = yaml.getConfigurationSection("anvil_panel");
                if (enchantPanelSection == null) enchantPanelSection = yaml.getConfigurationSection("enchanting_panel");
                if (barrierItemSection == null) barrierItemSection = yaml.getConfigurationSection("barrier_item");
                if (recipeLockedSection == null) recipeLockedSection = yaml.getConfigurationSection("recipe_locked");
                // 获取recipes配置节段实例
                ConfigurationSection recipeSection = yaml.getConfigurationSection(SECTION_RECIPES);
                if (recipeSection == null) continue;
                List<CrazyShapedRecipe> _recipes = Global.deserializeList(recipeSection, CrazyShapedRecipe.class);
                recipes.addAll(_recipes);
            }

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
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
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
            PanelSlotType type = PanelSlotTypeSerialization.valueOf(id[1]);
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

    public static boolean noneMatchRecipe(String id) {
        return recipes.stream().noneMatch(x -> x.getId().equalsIgnoreCase(id));
    }

    public static CrazyShapedRecipe getRecipe(String id) {
        return recipes.stream().filter(x -> x.getId().equalsIgnoreCase(id)).findAny().orElse(null);
    }

    public static CrazyShapedRecipe getRecipe(@NotNull ItemStack fromResult) {
        ItemBase fromBase = ItemManager.getItemBase(fromResult);
        for (CrazyShapedRecipe recipe : recipes) {
            ItemBase recipeBase = ItemManager.getItemBase(recipe.getResult());
            if (recipeBase.isSimilar(fromBase)) return recipe;
        }
        return null;
    }

    public static CrazyShapedRecipe getRecipe(@NotNull Inventory inventory) {
        List<ItemStack> requirements = getRequirements(inventory);
        if (requirements.size() == 0) return null;

        List<CrazyShapedRecipe> filterRecipes = recipes.stream().filter(x -> x.getRequirements().size() == requirements.size()).toList();
        for (CrazyShapedRecipe recipe : filterRecipes) {
            List<ItemStack> ingredients = recipe.getRequirements();
            int count = ingredients.size();
            boolean assumeFound = true;

            for (int i = 0; i < count; i++) {
                ItemBase ibRequirement = ItemManager.getItemBase(requirements.get(i));
                ItemBase ibIngredient = ItemManager.getItemBase(ingredients.get(i));

                boolean similar = ibRequirement.isSimilar(ibIngredient);
                int delta = requirements.get(i).getAmount() - ingredients.get(i).getAmount();
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

    private static ItemStack createBlockedItemStack(String display, String material, List<String> lore) {
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

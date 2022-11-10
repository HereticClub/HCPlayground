package org.hcmc.hcplayground.model.menu;

import com.google.gson.annotations.Expose;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.hcmc.hcplayground.enums.RecipeType;
import org.hcmc.hcplayground.manager.*;
import org.hcmc.hcplayground.model.item.ItemBase;
import org.hcmc.hcplayground.model.mmo.MMORecipeTemplate;
import org.hcmc.hcplayground.model.player.PlayerData;
import org.hcmc.hcplayground.model.recipe.CrazyShapedRecipe;
import org.hcmc.hcplayground.serialization.RecipeTypeSerialization;

import java.util.*;

public class RecipeMenuPanel extends CraftMenuPanel {

    private static final String MENU_RECIPE_MATERIAL = "recipe_material_template";
    private static final String MENU_DECORATE_CLICKABLE = "clickable";
    private static final String MENU_DECORATE_COMMON = "common";
    private static final Map<RecipeType, List<String>> recipeTypeMapping = new HashMap<>();
    private final List<String> recipeIds = new ArrayList<>();
    @Expose(deserialize = false)
    private RecipeType type = RecipeType.UNDEFINED;
    @Expose(deserialize = false)
    private Player player;
    @Expose(deserialize = false)
    private String menuParameter;

    public RecipeMenuPanel() {

    }

    public RecipeType getType() {
        return type;
    }

    @Override
    public void OnConfigured(YamlConfiguration yaml) {

    }

    @Override
    public Inventory OnOpening(Player player, String menuParameter) {
        this.player = player;
        this.menuParameter = menuParameter;
        this.type = RecipeTypeSerialization.valueOf(menuParameter);
        CrazyShapedRecipe recipe = RecipeManager.getRecipe(menuParameter);

        // 打开的菜单被判断为配方类型列表
        switch (this.type) {
            case RECIPE_COMBAT, RECIPE_FARMING, RECIPE_FISHING, RECIPE_LUMBERING, RECIPE_MINING, RECIPE_MINION ->
                    setupInventory(player);
        }
        // 打开的菜单被判断为配方详细展示
        if (recipe != null) setupInventory(player, recipe);
        // 如果打开的菜单没有被设置实例
        if (inventory == null) return null;
        return inventory;
    }

    @Override
    public void setPage(int page) {
        super.setPage(page);
        if (player == null) return;
        OnOpening(player, menuParameter);
    }

    /**
     * 展示配方公式
     *
     * @param player 查看配方公式的玩家实例
     * @param recipe 要查看的配方实例
     */
    private void setupInventory(Player player, CrazyShapedRecipe recipe) {
        String prefix = LanguageManager.getString("recipeTitlePrefix");

        ItemStack is = recipe.getResult().clone();
        ItemMeta meta = is.getItemMeta();
        if (meta == null) return;
        String title = String.format("%s %s", prefix, meta.getDisplayName());
        inventory = createChestInventory(title, player);

        List<ItemBase> ibs = recipe.getIngredients().values().stream().toList();
        List<ItemStack> itemStacks;
        if (recipe.isLegacy()) {
            itemStacks = new ArrayList<>();
            ibs.forEach(x -> itemStacks.add(x.toItemStack()));
        } else {
            itemStacks = recipe.getRequirements();
        }

        List<String> shape = recipe.getIngredientShape();
        ItemStack result = recipe.getResult();
        inventory.setItem(34, result);
        MenuPanelSlot showcase = getDecorate(MENU_DECORATE_CLICKABLE);

        for (Map.Entry<RecipeType, List<String>> entry : recipeTypeMapping.entrySet()) {
            List<String> ids = entry.getValue();
            if (ids == null) continue;
            if (ids.stream().anyMatch(x -> x.equalsIgnoreCase(recipe.getId()))) type = entry.getKey();
        }

        for (int row = 0; row < shape.size(); row++) {
            String r = shape.get(row);
            for (int column = 0; column < r.length(); column++) {
                int slotIndex = row * 9 + column;
                int itemIndex = row * r.length() + column;
                ItemStack recipeResult = itemStacks.get(itemIndex);
                ItemStack parentResult = null;

                CrazyShapedRecipe parentRecipe = RecipeManager.getRecipe(recipeResult);
                if (parentRecipe != null) {
                    String recipeId = parentRecipe.getId().toLowerCase();
                    parentResult = setupResultMeta(parentRecipe);
                    parentResult.setAmount(recipeResult.getAmount());
                    recipeIds.add(recipeId);

                    MenuManager.registerTemplateName(recipeId, MENU_RECIPE_MATERIAL);
                    String command = String.format("[console] playgroundmenus open %s %s", recipeId, player.getName().toLowerCase());
                    showcase.addLeftCommands(slotIndex + 1, command);
                }
                inventory.setItem(slotIndex, parentResult == null ? recipeResult : parentResult);
            }
        }

        for (MenuPanelSlot slot : decorates) {
            Map<Integer, List<String>> leftCommands = slot.getLeftCommands();
            for (Map.Entry<Integer, List<String>> entry : leftCommands.entrySet()) {
                entry.getValue().replaceAll(x -> x.replace("%recipe_type%", type.name()).toLowerCase());
            }
        }
    }

    /**
     * 展示配方实例的列表
     *
     * @param player 查看配方实例列表的玩家实例
     */
    private void setupInventory(Player player) {
        MMORecipeTemplate recipe = MMOManager.getRecipeTemplate(this.type);
        if (recipe == null) return;
        String nextPage = LanguageManager.getString("menuNextPage");
        String backPage = LanguageManager.getString("menuBackPage");

        recipeIds.clear();
        recipeTypeMapping.put(type, recipeIds);
        if (inventory == null) inventory = createChestInventory(recipe.getTitle(), player);
        List<CrazyShapedRecipe> recipes = RecipeManager.getRecipes().stream().filter(x -> x.getType().equals(this.type) && !x.isLegacy()).toList();
        int recipeSize = recipes.size();
        if (recipeSize == 0) return;

        MenuPanelSlot showcase = getDecorate(MENU_DECORATE_CLICKABLE);
        MenuPanelSlot navSlot = getDecorate(MENU_DECORATE_COMMON);
        if (showcase == null) return;
        if (navSlot == null) return;

        int menuItemSize = showcase.getSlots().size();
        int maxPage = recipeSize % menuItemSize == 0 ? recipeSize / menuItemSize : recipeSize / menuItemSize + 1;

        if (page >= maxPage) {
            navSlot.clearSlotItem(inventory, 54);
            page = maxPage;
        }
        if (page <= 1) {
            navSlot.clearSlotItem(inventory, 53);
        }
        if (page >= 2) {
            int i = 53;
            int a = 1;
            navSlot.setSlotItem(inventory, i, backPage, null, Material.ARROW, a);
            navSlot.clearLeftCommands(i);
            navSlot.addLeftCommands(i, MenuPanelSlot.COMMAND_PERFORM_BACK_PAGE);
        }
        if (page < maxPage) {
            int i = 54;
            int a = 1;
            navSlot.setSlotItem(inventory, i, nextPage, null, Material.ARROW, a);
            navSlot.clearLeftCommands(i);
            navSlot.addLeftCommands(i, MenuPanelSlot.COMMAND_PERFORM_NEXT_PAGE);
        }

        for (int index = (page - 1) * menuItemSize; index < page * menuItemSize; index++) {
            int slotRawIndex = showcase.getSlots().get(index - (page - 1) * menuItemSize) - 1;
            MenuPanelSlot slot = getDecorate(slotRawIndex + 1);
            if (slot == null) continue;
            slot.clearLeftCommands(slotRawIndex + 1);
            // 清空剩余展示区域
            if (index >= recipeSize) {
                ItemStack itemStack = inventory.getItem(slotRawIndex);
                if (itemStack != null) inventory.remove(itemStack);
                continue;
            }
            // 获取配方实例及id
            CrazyShapedRecipe csr = recipes.get(index);
            String recipeId = csr.getId().toLowerCase();
            recipeIds.add(recipeId);
            // 获取配方成品并且设置显示信息
            ItemStack is = setupResultMeta(csr);
            inventory.setItem(slotRawIndex, is);

            MenuManager.registerTemplateName(recipeId, MENU_RECIPE_MATERIAL);
            String command = String.format("[console] playgroundmenus open %s %s", recipeId, player.getName().toLowerCase());
            slot.addLeftCommands(slotRawIndex + 1, command);
        }
    }

    private ItemStack setupResultMeta(CrazyShapedRecipe recipe) {
        ItemStack itemStack = recipe.getResult().clone();
        PlayerData data = PlayerManager.getPlayerData(player);
        List<String> unlocks = data.getRecipes();
        String prefix = LanguageManager.getString("recipeTitlePrefix");
        String locked = LanguageManager.getString("recipeLocked");

        ItemMeta meta = itemStack.getItemMeta();
        // 修改显示标题，说明等
        if (meta != null) {
            String display = String.format("%s %s", prefix, meta.getDisplayName());
            meta.setDisplayName(display);
            // 修改详细说明，添加[配方被锁定]信息
            List<String> lore = new ArrayList<>();
            if (unlocks.stream().noneMatch(x -> x.equalsIgnoreCase(recipe.getId()))) lore.add(locked);
            List<String> _lore = meta.getLore();
            if (_lore != null) lore.addAll(_lore);
            if (lore.size() >= 1) meta.setLore(lore);
        }
        itemStack.setItemMeta(meta);
        return itemStack;
    }
}

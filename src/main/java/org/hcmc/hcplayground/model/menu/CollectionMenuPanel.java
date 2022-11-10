package org.hcmc.hcplayground.model.menu;

import com.google.gson.annotations.Expose;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.hcmc.hcplayground.manager.MMOManager;
import org.hcmc.hcplayground.manager.MenuManager;
import org.hcmc.hcplayground.model.mmo.MMOCollectionCategory;
import org.hcmc.hcplayground.model.mmo.MMOCollectionMaterial;
import org.hcmc.hcplayground.model.mmo.MMOLevelTemplate;
import org.hcmc.hcplayground.serialization.CollectionTypeSerialization;
import org.hcmc.hcplayground.serialization.MaterialSerialization;
import org.hcmc.hcplayground.utility.Global;

import java.util.*;

public class CollectionMenuPanel extends CraftMenuPanel {
    private static final String MENU_COLLECTION_MATERIAL = "collection_material_template";
    private final Map<CollectionType, Material[]> collectionMapping = new HashMap<>();

    @Expose(deserialize = false)
    private CollectionType type = CollectionType.UNDEFINED;

    public CollectionMenuPanel() {
        collectionMapping.put(CollectionType.COLLECTION_COMBAT, MMOManager.CombatMaterials);
        collectionMapping.put(CollectionType.COLLECTION_FARMING, MMOManager.FarmingMaterials);
        collectionMapping.put(CollectionType.COLLECTION_FISHING, MMOManager.FishingMaterials);
        collectionMapping.put(CollectionType.COLLECTION_LUMBERING, MMOManager.LumberingMaterials);
        collectionMapping.put(CollectionType.COLLECTION_MINING, MMOManager.MiningMaterials);
    }

    @Override
    public void OnConfigured(YamlConfiguration yaml) {

    }

    @Override
    public Inventory OnOpening(Player player, String menuId) {
        // 假设menuId的值是collection_XXXX，其中XXXX是Material
        // 变量materialName则是从menuId中提取XXXX的值
        String prefix = "collection_";
        String materialName = menuId.substring(prefix.length()).toLowerCase();
        Material material = MaterialSerialization.valueOf(materialName);
        this.type = CollectionTypeSerialization.valueOf(menuId);

        if (!this.type.equals(CollectionType.UNDEFINED)) switch (this.type) {
            case COLLECTION_COMBAT -> setupInventory(player, MMOManager.CombatMaterials);
            case COLLECTION_FARMING -> setupInventory(player, MMOManager.FarmingMaterials);
            case COLLECTION_FISHING -> setupInventory(player, MMOManager.FishingMaterials);
            case COLLECTION_LUMBERING -> setupInventory(player, MMOManager.LumberingMaterials);
            case COLLECTION_MINING -> setupInventory(player, MMOManager.MiningMaterials);
        }

        if (!material.equals(Material.AIR)) setupInventory(player, material);
        if (inventory == null) return null;
        return inventory;
    }

    /**
     * 设置采集类型菜单实例
     *
     * @param player    使用当前菜单实例的玩家
     * @param materials 从属于当前采集类型的物品
     */
    private void setupInventory(Player player, Material[] materials) {
        MMOCollectionCategory category = getCategory(type);
        if (category == null) return;

        inventory = createChestInventory(category.getName(), player);
        Map<Integer, ItemStack> itemStacks = category.decorate(materials);
        for (Map.Entry<Integer, ItemStack> entry : itemStacks.entrySet()) {
            ItemStack itemStack = entry.getValue();
            int slotIndex = entry.getKey();
            inventory.setItem(slotIndex - 1, itemStack);

            MenuPanelSlot slot = getDecorate(slotIndex);
            String menuId = String.format("collection_%s", itemStack.getType().name().toLowerCase());
            MenuManager.registerTemplateName(menuId, MENU_COLLECTION_MATERIAL);
            String command = String.format("[console] playgroundmenus open %s %s", menuId, player.getName());
            slot.addLeftCommands(slotIndex, command);
        }
    }

    private void setupInventory(Player player, Material material) {
        MMOCollectionMaterial collectionMaterial = MMOManager.getCollectionMaterial(material);
        if (collectionMaterial == null) return;

        List<MMOLevelTemplate> levels = collectionMaterial.getUnclaimedLevels(player, material);
        String _title = StringUtils.isBlank(collectionMaterial.getName()) ? title : collectionMaterial.getName();
        inventory = Bukkit.createInventory(this, size, _title);

        CollectionType collectionType = CollectionType.UNDEFINED;
        for (Map.Entry<CollectionType, Material[]> entry : collectionMapping.entrySet()) {
            Material[] materials = entry.getValue();
            if (Arrays.asList(materials).contains(material)) collectionType = entry.getKey();
        }
        CollectionType finalCollectionType = collectionType;

        for (MenuPanelSlot slot : decorates) {
            List<String> lore = slot.getLore();
            lore.replaceAll(x -> x.replace("%levels%", String.valueOf(levels.size())));
            slot.setLore(lore);

            Map<Integer, List<String>> lefts = slot.getLeftCommands();
            for (Map.Entry<Integer, List<String>> entry : lefts.entrySet()) {
                entry.getValue().replaceAll(x -> x.replace("%collection_type%", finalCollectionType.name().toLowerCase())
                        .replace("%material%", material.name()).toLowerCase());
            }
            Map<Integer, ItemStack> itemStacks = slot.createItemStacks(player);
            for (Map.Entry<Integer, ItemStack> entry : itemStacks.entrySet()) {
                inventory.setItem(entry.getKey() - 1, entry.getValue());
            }
        }

        Map<Integer, ItemStack> itemStacks = collectionMaterial.decorate(player, material);
        for (Map.Entry<Integer, ItemStack> entry : itemStacks.entrySet()) {
            inventory.setItem(entry.getKey(), entry.getValue());
        }
    }

    private MMOCollectionCategory getCategory(CollectionType type) {
        ConfigurationSection section = yaml.getConfigurationSection(MMOManager.TEMPLATE_COLLECTION_CATEGORIES);
        if (section == null) return null;
        List<MMOCollectionCategory> categories = Global.deserializeList(section, MMOCollectionCategory.class);
        return categories.stream().filter(x -> x.getType().equals(type)).findAny().orElse(null);
    }

    public enum CollectionType {
        /**
         * 战斗收集
         */
        COLLECTION_COMBAT,
        /**
         * 农业收集
         */
        COLLECTION_FARMING,
        /**
         * 渔业收集
         */
        COLLECTION_FISHING,
        /**
         * 原木收集
         */
        COLLECTION_LUMBERING,
        /**
         * 矿产收集
         */
        COLLECTION_MINING,
        /**
         * 未定义，用作普通菜单界面
         */
        UNDEFINED,
    }
}

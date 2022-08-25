package org.hcmc.hcplayground.model.menu;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.enums.MMOType;
import org.hcmc.hcplayground.manager.*;
import org.hcmc.hcplayground.model.mmo.*;
import org.hcmc.hcplayground.model.player.PlayerData;
import org.hcmc.hcplayground.serialization.MMOLevelTypeSerialization;
import org.hcmc.hcplayground.serialization.MaterialSerialization;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MenuPanel implements InventoryHolder {

    @Expose
    @SerializedName(value = "title")
    private String title;
    @Expose
    @SerializedName(value = "inventory-type")
    private InventoryType inventoryType = InventoryType.CHEST;
    @Expose
    @SerializedName(value = "size")
    private int size = 54;
    @Expose
    @SerializedName(value = "worlds")
    private List<String> enableWorlds = new ArrayList<>();

    @Expose(deserialize = false)
    private MMOType mmoType = MMOType.UNDEFINED;
    @Expose(deserialize = false)
    private List<MenuPanelSlot> decorates = new ArrayList<>();
    @Expose(deserialize = false)
    private String id;
    @Expose(deserialize = false)
    private Inventory inventory;
    @Expose(deserialize = false)
    private JavaPlugin plugin = HCPlayground.getInstance();

    public MenuPanel() {

    }

    public MenuPanelSlot getDecorate(int slot) {
        MenuPanelSlot s = decorates.stream().filter(x -> x.getSlots().contains(slot)).findAny().orElse(null);
        return s == null ? null : (MenuPanelSlot) s.clone();
    }

    public boolean existDecorate(List<Integer> slots) {
        return decorates.stream().anyMatch(x -> new HashSet<>(x.getSlots()).containsAll(slots));
    }

    public List<MenuPanelSlot> getDecorates() {
        return decorates;
    }

    public void setDecorates(List<MenuPanelSlot> decorates) {
        this.decorates = decorates;
    }

    public String getId() {
        return id;
    }

    public int getSize() {
        return size;
    }

    public void addDecorate(MenuPanelSlot slot) {
        if (existDecorate(slot.getSlots())) return;
        decorates.add(slot);
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void close(Player player) {
        player.closeInventory();
    }

    public void open(Player player, String mmoType) {
        PlayerData data = PlayerManager.getPlayerData(player);
        String worldName = player.getWorld().getName();
        if (enableWorlds == null) enableWorlds = new ArrayList<>();
        boolean isEnabledWorld = enableWorlds.stream().anyMatch(x -> x.equalsIgnoreCase(worldName));
        if (enableWorlds.size() >= 1 && !isEnabledWorld && !player.isOp()) {
            player.sendMessage(LanguageManager.getString("menuWorldProhibited", player)
                    .replace("%world%", worldName)
                    .replace("%menu%", title)
            );
            return;
        }

        this.mmoType = MMOLevelTypeSerialization.getType(mmoType);
        System.out.println(this.mmoType);

        switch (this.mmoType) {
            case SKILL_COMBAT -> {
                int statistic = data.statisticEntityKilled(MMOManager.Monsters);
                inventory = setupSkillInventory(player, statistic);
            }
            case SKILL_FARMING -> {
                int statistic = data.statisticFarming();
                inventory = setupSkillInventory(player, statistic);
            }
            case SKILL_FISHING -> {
                int statistic = data.statisticFishingCaught();
                inventory = setupSkillInventory(player, statistic);
            }
            case SKILL_LUMBERING -> {
                int statistic = data.statisticBlockMined(MMOManager.LumberingBlocks);
                inventory = setupSkillInventory(player, statistic);
            }
            case SKILL_MINING -> {
                int statistic = data.statisticBlockMined(MMOManager.MiningBlocks);
                inventory = setupSkillInventory(player, statistic);
            }

            case SKILL_CRAFTING -> inventory = statisticSkillCrafting(player);
            case SKILL_ENCHANTING -> inventory = statisticSkillEnchanting(player);
            case SKILL_POTION -> inventory = statisticSkillPotion(player);
            case SKILL_TAMING -> inventory = statisticSkillTaming(player);

            case COLLECTION_COMBAT -> inventory = setupCollectionCategoryInventory(player, MMOManager.CombatMaterials);
            case COLLECTION_FARMING -> inventory = setupCollectionCategoryInventory(player, MMOManager.FarmingMaterials);
            case COLLECTION_FISHING -> inventory = setupCollectionCategoryInventory(player, MMOManager.FishingMaterials);
            case COLLECTION_LUMBERING -> inventory = setupCollectionCategoryInventory(player, MMOManager.LumberingMaterials);
            case COLLECTION_MINING -> inventory = setupCollectionCategoryInventory(player, MMOManager.MiningMaterials);
            default -> {
                Material material = getCollectionMaterial(mmoType);
                this.mmoType = MMOManager.getCollectionType(material);
                inventory = material != Material.AIR ? setupCollectionMaterialInventory(player, material) : switch (inventoryType) {
                    case ANVIL -> createAnvilInventory(title);
                    case CHEST -> createChestInventory(title, player);
                    default -> Bukkit.createInventory(this, inventoryType, title);
                };
            }
        }

        if (inventory == null) return;
        player.openInventory(inventory);
    }

    private @NotNull Inventory createCollectionMaterialInventory(String title, Player player, Material material) {
        String _title = StringUtils.isBlank(title) ? "" : title;
        Inventory inv = Bukkit.createInventory(this, size, _title);
        MMOType collectionType = MMOManager.getCollectionType(material);
        List<MMOLevel> levels = RewardManager.getUnclaimedLevels(player, material);

        for (MenuPanelSlot slot : decorates) {
            List<String> lore = slot.getLore();
            lore.replaceAll(x -> x.replace("%levels%", String.valueOf(levels.size())));
            slot.setLore(lore);

            Map<Integer, List<String>> lefts = slot.getLeftCommands();
            for (Map.Entry<Integer, List<String>> entry : lefts.entrySet()) {
                entry.getValue().replaceAll(x -> x.replace("%collection_type%", collectionType.name().toLowerCase())
                        .replace("%material%", material.name()).toLowerCase());
            }
            Map<Integer, ItemStack> itemStacks = slot.createItemStacks(player);
            for (Map.Entry<Integer, ItemStack> entry : itemStacks.entrySet()) {
                inv.setItem(entry.getKey() - 1, entry.getValue());
            }
        }

        return inv;
    }

    @NotNull
    private Inventory createSkillInventory(String title, Player player) {
        String _title = StringUtils.isBlank(title) ? "" : title;
        Inventory inv = Bukkit.createInventory(this, size, _title);
        List<MMOLevel> levels = RewardManager.getUnclaimedLevels(player, mmoType);

        for (MenuPanelSlot slot : decorates) {
            List<String> lore = slot.getLore();
            lore.replaceAll(x->x.replace("%levels%", String.valueOf(levels.size())));
            slot.setLore(lore);

            Map<Integer, List<String>> lefts = slot.getLeftCommands();
            for (Map.Entry<Integer, List<String>> entry : lefts.entrySet()) {
                entry.getValue().replaceAll(x -> x.replace("%skill_id%", mmoType.name().toLowerCase()));
            }

            Map<Integer, ItemStack> itemStacks = slot.createItemStacks(player);
            for (Map.Entry<Integer, ItemStack> entry : itemStacks.entrySet()) {
                inv.setItem(entry.getKey() - 1, entry.getValue());
            }
        }

        return inv;
    }

    private @NotNull Inventory createChestInventory(String title, Player player) {
        String _title = StringUtils.isBlank(title) ? "" : title;
        Inventory inv = Bukkit.createInventory(this, size, _title);

        for (MenuPanelSlot slot : decorates) {
            Map<Integer, ItemStack> itemStacks = slot.createItemStacks(player);
            for (Map.Entry<Integer, ItemStack> entry : itemStacks.entrySet()) {
                inv.setItem(entry.getKey() - 1, entry.getValue());
            }
        }

        return inv;
    }

    private Inventory statisticSkillEnchanting(Player player) {
        return null;
    }

    private Inventory statisticSkillPotion(Player player) {
        return null;
    }

    private Inventory statisticSkillCrafting(Player player) {
        return null;
    }

    private Inventory statisticSkillTaming(Player player) {
        return null;
    }

    private Material getCollectionMaterial(String collectionName) {
        if (!collectionName.startsWith("collection_")) return Material.AIR;
        String materialName = collectionName.substring(11);
        return MaterialSerialization.parse(materialName);
    }

    private Inventory setupCollectionMaterialInventory(Player player, Material material) {
        MMOCollectionMaterial collection = MMOManager.getCollectionMaterial(material);
        if (collection == null) return null;

        Map<Integer, ItemStack> itemStacks = collection.decorate(player, material);
        Inventory inventory = createCollectionMaterialInventory(collection.getName(), player, material);
        for (Map.Entry<Integer, ItemStack> entry : itemStacks.entrySet()) {
            inventory.setItem(entry.getKey(), entry.getValue());
        }

        return inventory;
    }

    private Inventory setupCollectionCategoryInventory(Player player, Material[] materials) {
        MMOCollectionCategory collection = MMOManager.getCollectionCategory(mmoType);
        if (collection == null) return null;

        Inventory inventory = createChestInventory(collection.getName(), player);
        Map<Integer, ItemStack> itemStacks = collection.decorate(materials);
        for (Map.Entry<Integer, ItemStack> entry : itemStacks.entrySet()) {
            ItemStack itemStack = entry.getValue();
            int slotIndex = entry.getKey();
            inventory.setItem(slotIndex - 1, itemStack);

            MenuPanelSlot slot = getDecorate(slotIndex);
            String command = String.format("[console] playgroundmenus open collection_%s %s", itemStack.getType(), player.getName());
            slot.addLeftCommands(slotIndex, command);
        }
        return inventory;
    }

    private Inventory setupSkillInventory(Player player, int statistic) {
        MMOSkill skill = MMOManager.getSkill(mmoType);
        if (skill == null) return null;

        Inventory inventory = createSkillInventory(skill.getName(), player);
        Map<Integer, ItemStack> itemStacks = skill.decorateLevels(statistic);
        for (Map.Entry<Integer, ItemStack> entry : itemStacks.entrySet()) {
            inventory.setItem(entry.getKey(), entry.getValue());
        }

        return inventory;
    }

    private @NotNull Inventory createAnvilInventory(String title) {
        return Bukkit.createInventory(this, inventoryType, title);
    }
}
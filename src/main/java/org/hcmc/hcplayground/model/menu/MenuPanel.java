package org.hcmc.hcplayground.model.menu;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.enums.MMOSkillType;
import org.hcmc.hcplayground.manager.LanguageManager;
import org.hcmc.hcplayground.manager.MMOSkillManager;
import org.hcmc.hcplayground.manager.MenuManager;
import org.hcmc.hcplayground.manager.PlayerManager;
import org.hcmc.hcplayground.model.mmo.MMOSkill;
import org.hcmc.hcplayground.model.player.PlayerData;
import org.hcmc.hcplayground.utility.MaterialData;
import org.hcmc.hcplayground.utility.PlayerHeaderUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MenuPanel implements InventoryHolder {

    private static final String COMMAND_ARGS_OPEN = "open";
    private static final String COMMAND_ARGS_CLOSE = "close";
    private static final String COMMAND_ARGS_LIST = "list";
    /**
     * 农业技能菜单
     */
    private static final String MENU_ID_SKILL_FARMING = "skill_farming";
    /**
     * 挖矿技能菜单
     */
    private static final String MENU_ID_SKILL_MINING = "skill_mining";
    /**
     * 战斗技能菜单
     */
    private static final String MENU_ID_SKILL_COMBAT = "skill_combat";
    /**
     * 伐木技能菜单
     */
    private static final String MENU_ID_SKILL_LOGGING = "skill_logging";
    /**
     * 钓鱼技能菜单
     */
    private static final String MENU_ID_SKILL_FISHING = "skill_fishing";
    /**
     * 附魔技能菜单
     */
    private static final String MENU_ID_SKILL_ENCHANTING = "skill_enchanting";
    /**
     * 酿造技能菜单
     */
    private static final String MENU_ID_SKILL_POTION = "skill_potion";
    /**
     * 合成技能菜单
     */
    private static final String MENU_ID_SKILL_CRAFTING = "skill_crafting";
    /**
     * 驯养技能菜单
     */
    private static final String MENU_ID_SKILL_TAMING = "skill_taming";

    @Expose
    @SerializedName(value = "title")
    private String title;
    /*
    @Expose
    @SerializedName(value = "permission")
    private String permission;

     */
    @Expose
    @SerializedName(value = "inventory-type")
    private InventoryType inventoryType = InventoryType.CHEST;
    @Expose
    @SerializedName(value = "level-type")
    private MMOSkillType levelType = MMOSkillType.UNDEFINED;
    @Expose
    @SerializedName(value = "size")
    private int size = 54;
    @Expose
    @SerializedName(value = "worlds")
    private List<String> enableWorlds = new ArrayList<>();
    /*
    @Expose
    @SerializedName(value = "aliases")
    private List<String> aliases = new ArrayList<>();

     */

    @Expose(deserialize = false)
    private List<MenuPanelSlot> decorates = new ArrayList<>();
    @Expose(serialize = false, deserialize = false)
    private String id;
    @Expose(serialize = false, deserialize = false)
    private Inventory inventory;
    @Expose(serialize = false, deserialize = false)
    private JavaPlugin plugin = HCPlayground.getInstance();

    public MenuPanel() {

    }

    public MenuPanelSlot getPanelSlot(int index) {
        return decorates.stream().filter(x -> x.getSlots().contains(index)).findAny().orElse(null);
    }

    public String getId() {
        return id;
    }

    /*
    public List<String> getAliases() {
        return aliases;
    }

    public void setAliases(List<String> aliases) {
        this.aliases = aliases;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

     */

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getSize() {
        return size;
    }

    public List<String> getEnableWorlds() {
        return enableWorlds;
    }

    public InventoryType getInventoryType() {
        return inventoryType;
    }

    public List<MenuPanelSlot> getDecorates() {
        return decorates;
    }

    public void setDecorates(List<MenuPanelSlot> decorates) {
        this.decorates = decorates;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public MenuPanelSlot getSlot(int slot) {
        return decorates.stream().filter(x -> x.getSlots().contains(slot)).findAny().orElse(null);
    }

    public void close(Player player) {
        player.closeInventory();
    }

    public void open(Player player) {

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

        inventory = switch (inventoryType) {
            case ANVIL -> createAnvilInventory(title);
            case CHEST -> createChestInventory(title, player);
            default -> Bukkit.createInventory(this, inventoryType, title);
        };
        if (levelType != null) {
            switch (levelType) {
                case SKILL_FARMING -> statisticSkillFarming(player);
                case SKILL_MINING -> statisticSkillMining(player);
                case SKILL_COMBAT -> statisticSkillCombat(player);
                case SKILL_LUMBERING -> statisticSkillLumbering(player);
                case SKILL_FISHING -> statisticSkillFishing(player);
                case SKILL_ENCHANTING -> statisticSkillEnchanting(player);
                case SKILL_POTION -> statisticSkillPotion(player);
                case SKILL_CRAFTING -> statisticSkillCrafting(player);
                case SKILL_TAMING -> statisticSkillTaming(player);
            }
        }

        player.openInventory(inventory);
    }

    private void statisticSkillFarming(Player player) {
        PlayerData data = PlayerManager.getPlayerData(player);
        int statistic = data.statisticPickupCrops();
        setupInventory(statistic);
    }

    private void statisticSkillMining(Player player) {
        PlayerData data = PlayerManager.getPlayerData(player);
        int statistic = data.statisticPickupMineral();
        setupInventory(statistic);
    }

    private void statisticSkillCombat(Player player) {
        PlayerData data = PlayerManager.getPlayerData(player);
        int statistic = data.statisticPickupCombat();
        setupInventory(statistic);
    }

    private void statisticSkillLumbering(Player player) {
        PlayerData data = PlayerManager.getPlayerData(player);
        int statistic = data.statisticPickupLumbering();
        setupInventory(statistic);
    }

    private void statisticSkillFishing(Player player) {
        PlayerData data = PlayerManager.getPlayerData(player);
        int statistic = data.statisticPickupFishing();
        setupInventory(statistic);
    }

    private void statisticSkillEnchanting(Player player) {

    }

    private void statisticSkillPotion(Player player) {

    }

    private void statisticSkillCrafting(Player player) {

    }

    private void statisticSkillTaming(Player player) {

    }

    private void setupInventory(int statistic) {
        MMOSkill skill = MMOSkillManager.getSkill(levelType);
        if (skill == null) return;
        Map<Integer, ItemStack> itemStacks = skill.resolveLevels(statistic);
        for (Map.Entry<Integer, ItemStack> entry : itemStacks.entrySet()) {
            inventory.setItem(entry.getKey(), entry.getValue());
        }
    }

    private Inventory createAnvilInventory(String title) {
        return Bukkit.createInventory(this, inventoryType, title);
    }

    private Inventory createChestInventory(String title, Player player) {
        Inventory inv = Bukkit.createInventory(this, size, title);

        for (MenuPanelSlot slot : decorates) {
            for (int number : slot.getSlots()) {
                ItemStack is;
                if (slot.getMaterial() == null) {
                    MaterialData data = new MaterialData();
                    data.setData(Material.AIR, "");
                    slot.setMaterial(data);
                }

                if (slot.getMaterial().value == null) slot.getMaterial().value = Material.STONE;
                if (!slot.getMaterial().value.equals(Material.PLAYER_HEAD)) {
                    is = new ItemStack(slot.getMaterial().value, slot.getAmount());
                } else {
                    is = slot.getCustomSkull().isEmpty() ? ResolvePlayerHead(player, slot.getMaterial()) : ResolveCustomHead(slot.getCustomSkull());
                }

                ItemMeta im = is.getItemMeta();
                if (im == null) continue;

                im.addItemFlags(slot.getFlags().toArray(new ItemFlag[0]));
                im.setLore(slot.getLore());
                if (!StringUtils.isBlank(slot.getDisplay()))
                    im.setDisplayName(slot.getDisplay().replace("%player%", player.getName()));

                if (slot.isGlowing()) {
                    im.addEnchant(Enchantment.MENDING, 1, true);
                    Set<ItemFlag> flags = im.getItemFlags();
                    if (!flags.contains(ItemFlag.HIDE_ENCHANTS)) {
                        im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    }
                }

                is.setItemMeta(im);
                inv.setItem(number - 1, is);
            }
        }

        return inv;
    }

    private ItemStack ResolvePlayerHead(Player player, MaterialData data) {
        String[] keys = data.name.split("_");
        OfflinePlayer[] offlinePlayers = plugin.getServer().getOfflinePlayers();
        ItemStack is = new ItemStack(data.value);

        if (keys.length >= 2 && keys[0].equalsIgnoreCase("head")) {
            String playerName = keys[1].replace("%player%", player.getName());
            OfflinePlayer offlinePlayer = Arrays.stream(offlinePlayers).filter(x -> Objects.requireNonNull(x.getName()).equalsIgnoreCase(playerName)).findAny().orElse(null);

            SkullMeta sm = (SkullMeta) is.getItemMeta();
            if (sm != null) sm.setOwningPlayer(offlinePlayer);
            is.setItemMeta(sm);
        }

        return is;
    }

    private static ItemStack ResolveCustomHead(String base64data) {
        ItemStack is = new ItemStack(Material.PLAYER_HEAD);

        ItemMeta im = PlayerHeaderUtil.setTextures(is, base64data);
        if (!(im instanceof SkullMeta meta)) return is;

        is.setItemMeta(meta);
        return is;
    }
}

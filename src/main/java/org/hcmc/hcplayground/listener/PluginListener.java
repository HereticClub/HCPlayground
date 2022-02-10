package org.hcmc.hcplayground.listener;

import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.hcmc.hcplayground.drops.DropManager;
import org.hcmc.hcplayground.items.ItemManager;
import org.hcmc.hcplayground.items.offhand.OffHand;
import org.hcmc.hcplayground.items.weapon.Weapon;
import org.hcmc.hcplayground.model.Global;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.scheduler.PluginRunnable;

import java.io.File;
import java.io.IOException;

public class PluginListener implements Listener {

    JavaPlugin plugin = JavaPlugin.getPlugin(HCPlayground.class);

    public PluginListener() {

    }

    /**
     * 玩家进入世界事件
     */
    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        Player p = event.getPlayer();
        String playerUuid = p.getUniqueId().toString();
        File f = new File(plugin.getDataFolder(), "profile/" + playerUuid + ".yml");

        if (!f.exists()) {
            Global.yamlPlayer = new YamlConfiguration();
        } else {
            Global.yamlPlayer = YamlConfiguration.loadConfiguration(f);
        }
    }

    /**
     * 玩家离开服务器事件
     */
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        String playerUuid = p.getUniqueId().toString();
        File f = new File(plugin.getDataFolder(), String.format("profile/%s.yml", playerUuid));

        try {
            Global.yamlPlayer.save(f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 任意 Inventory 点击事件
     */
    @EventHandler
    public void onInventoryClick1(final InventoryClickEvent event) {
        if (event.isCancelled()) return;

        Player player = (Player) event.getWhoClicked();
        Inventory inv = event.getInventory();
        ItemStack itemStack = event.getCurrentItem();

    }

    /**
     * 任意 Inventory 点击事件
     */
    @EventHandler
    public void onGetOffHandItem(final InventoryClickEvent event) {
        if (event.isCancelled()) return;

        Inventory inv = event.getClickedInventory();
        if (inv == null) return;
        if (inv.getType() != InventoryType.PLAYER) return;

        InventoryAction action = event.getAction();
        if (action != InventoryAction.PLACE_ALL && action != InventoryAction.PLACE_ONE && action != InventoryAction.PLACE_SOME)
            return;

        PluginRunnable s = new PluginRunnable(inv, PluginRunnable.ScheduleType.GetOffHandItem);
        s.runTask(plugin);
        ItemStack is = s.getIsOffHand();
        String id = getPersistentItemID(is);
        if(id == null) return;

        ItemManager.FindItemById(id, OffHand.class);

    }

    @EventHandler
    public void onBlockBreak(final BlockBreakEvent event) {
        if (event.isCancelled()) return;

        Block b = event.getBlock();
        DropManager.AdditionalDrops(b);
    }

    private String getPersistentItemID(ItemStack is) {
        if(is == null)return null;
        if (is.getItemMeta() == null) return null;

        PersistentDataContainer container = is.getItemMeta().getPersistentDataContainer();
        NamespacedKey mainKey = new NamespacedKey(plugin, Global.PERSISTENT_MAIN_KEY);

        return container.get(mainKey, PersistentDataType.STRING);
    }
}

package org.hcmc.hcplayground.Listener;

import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.hcmc.hcplayground.Drops.DropManager;
import org.hcmc.hcplayground.Model.Global;
import org.hcmc.hcplayground.HCPlayground;

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
    public void onInventoryClick(final InventoryClickEvent event) {
        if (event.isCancelled()) return;

        Player player = (Player) event.getWhoClicked();
        Inventory inv = event.getInventory();
        ItemStack itemStack = event.getCurrentItem();

    }

    @EventHandler
    public void onBlockBreak(final BlockBreakEvent event) {
        if (event.isCancelled()) return;

        Block b = event.getBlock();
        DropManager.AdditionalDrops(b);
    }
}

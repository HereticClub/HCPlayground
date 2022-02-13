package org.hcmc.hcplayground.listener;

import org.bukkit.Material;
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
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.hcmc.hcplayground.drops.DropManager;
import org.hcmc.hcplayground.event.InventoryChangedEvent;
import org.hcmc.hcplayground.items.ItemManager;
import org.hcmc.hcplayground.items.offhand.OffHand;
import org.hcmc.hcplayground.model.Global;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.scheduler.InventoryChangingRunnable;
import org.hcmc.hcplayground.scheduler.PotionEffectRunnable;

import java.io.File;
import java.io.IOException;

/*
Java 本身的编程思路就已经足够混乱
Bukkit Api在本来已经混乱的基础上添加更混乱的逻辑思路
InventoryClickEvent只能获取当前任何类型的Inventory是否被点击
所以这个事件只能获取玩家点击的是哪一类型的Inventory
但要获取被点击的ItemStack，必须要在下一个Tick(50毫秒)之后
关键点就是这下一个Tick必须通过异步线程检测
而Plugin的运作线程全部在主线程内，所以通过while loop等只能锁死在当前线程
而在异步线程的类和方法不能直接和Bukkit Api的变量互交，这就是死循环
所以就必须使用继承BukkitRunnable类来下一Tick的事件
因此又要定义很大量的Event类和Listener类来回调InventoryClickEvent的各种参数

以下就是一个典型例子
在InventoryClickEvent获取玩家点击，然后通过某个Runnable类获取下一Tick的已点击的ItemStack
在Runnable类的必须实现的run方法里面再触发自定义事件，通过该事件传递获取到的ItemStack
然后在另一个自定义Listener类里面接收这个Event的参数而得知这个ItemStack是何物
*/
public class PluginListener implements Listener {

    private final JavaPlugin plugin = HCPlayground.getPlugin();
    private static PotionEffectRunnable potionRunnable = null;
    private static BukkitTask bt = null;

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

        potionRunnable = new PotionEffectRunnable(p);
        bt = potionRunnable.runTaskTimer(plugin, 20, 200);
    }

    /**
     * 玩家离开服务器事件
     */
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        String playerUuid = p.getUniqueId().toString();
        File f = new File(plugin.getDataFolder(), String.format("profile/%s.yml", playerUuid));

        if (bt != null && potionRunnable != null) {
            bt.cancel();
            potionRunnable.cancel();
        }

        try {
            if (Global.yamlPlayer != null) Global.yamlPlayer.save(f);
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
        if (event.getCursor() == null) return;
        if (event.getCursor().getType().equals(Material.AIR)) return;

        Player player = (Player) event.getWhoClicked();
        Inventory inv = event.getInventory();
        ItemStack itemStack = event.getCurrentItem();
    }

    /**
     * 任意 Inventory 点击事件
     */
    @EventHandler
    public void onOffHandChanging(final InventoryClickEvent event) {
        if (event.isCancelled()) return;
        if (event.getCursor() == null) return;
        if (event.getCursor().getType().equals(Material.AIR)) return;

        Inventory inv = event.getClickedInventory();
        if (inv == null) return;
        if (!inv.getType().equals(InventoryType.PLAYER)) return;

        InventoryAction action = event.getAction();
        InventoryChangingRunnable s = new InventoryChangingRunnable(inv, action, EquipmentSlot.OFF_HAND);
        s.runTask(plugin);

        if (potionRunnable == null) {
            potionRunnable = new PotionEffectRunnable((Player) event.getWhoClicked());
            bt = potionRunnable.runTaskTimer(plugin, 20, 200);
        }
    }

    @EventHandler
    public void onOffHandChanged(InventoryChangedEvent event) {
        if (event.isCancelled()) return;

        ItemStack is = event.getItemStack();
        InventoryAction action = event.getAction();
        Player player = (Player) event.getWhoClicked();

        String id = getPersistentItemID(is);
        OffHand offHand = (OffHand) ItemManager.FindItemById(id, OffHand.class);

        if (offHand != null) {
            potionRunnable.setPotionEffects(offHand.potions);
        } else {
            potionRunnable.setPotionEffects(null);
        }
    }

    @EventHandler
    public void onBlockBreak(final BlockBreakEvent event) {
        if (event.isCancelled()) return;

        Block b = event.getBlock();
        DropManager.AdditionalDrops(b);
    }

    private String getPersistentItemID(ItemStack is) {
        if (is == null) return null;
        if (is.getItemMeta() == null) return null;

        PersistentDataContainer container = is.getItemMeta().getPersistentDataContainer();
        NamespacedKey mainKey = new NamespacedKey(plugin, Global.PERSISTENT_MAIN_KEY);

        return container.get(mainKey, PersistentDataType.STRING);
    }
}

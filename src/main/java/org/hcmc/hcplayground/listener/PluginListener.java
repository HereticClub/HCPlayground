package org.hcmc.hcplayground.listener;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.dropManager.DropManager;
import org.hcmc.hcplayground.event.InventoryChangedEvent;
import org.hcmc.hcplayground.itemManager.ItemManager;
import org.hcmc.hcplayground.itemManager.offhand.OffHand;
import org.hcmc.hcplayground.localization.Localization;
import org.hcmc.hcplayground.model.Global;
import org.hcmc.hcplayground.playerManager.PlayerData;
import org.hcmc.hcplayground.scheduler.InventoryChangingRunnable;
import org.hcmc.hcplayground.sqlite.SqliteManager;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;

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

    public PluginListener() {

    }

    /**
     * 玩家进入世界事件
     */
    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) throws SQLException {
        Player player = event.getPlayer();
        UUID playerUuid = player.getUniqueId();

        PlayerData playerData = new PlayerData(player);
        playerData.LoadConfig();

        if (playerData.isDBBanned()) {
            String bannedMessage = Localization.Messages.get("playerBannedMessage").replace("%player%", player.getName());
            player.kickPlayer(bannedMessage);
            return;
        }

        boolean exist = playerData.isDBExist();
        playerData.setRegister(exist);
        playerData.setLoginDTTM(new Date());

        Global.playerMap.put(playerUuid, playerData);
    }

    /**
     * 玩家离开服务器事件
     */
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) throws IOException {
        Player p = event.getPlayer();
        UUID playerUuid = p.getUniqueId();
        PlayerData playerData = Global.playerMap.get(playerUuid);

        if (playerData != null) {
            playerData.SaveConfig();
        }

        Global.playerMap.remove(playerUuid, playerData);
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

        Player player = (Player) event.getWhoClicked();
        UUID playerUuid = player.getUniqueId();
        InventoryAction action = event.getAction();
        InventoryChangingRunnable s = new InventoryChangingRunnable(inv, action, EquipmentSlot.OFF_HAND);
        s.runTask(plugin);

        PlayerData playerData = Global.playerMap.get(playerUuid);
        if (playerData == null) return;

        /*
        if (playerData.PotionTimer == null) {
            playerData.PotionTimer = new PluginRunnable(player);
            playerData.RunPotionTimer(plugin, 20, 200);
        }

         */
        Global.playerMap.replace(playerUuid, playerData);
    }

    @EventHandler
    public void onOffHandChanged(InventoryChangedEvent event) {
        if (event.isCancelled()) return;

        ItemStack is = event.getItemStack();
        Player player = (Player) event.getWhoClicked();
        UUID playerUuid = player.getUniqueId();

        String id = getPersistentItemID(is);
        OffHand offHand = (OffHand) ItemManager.FindItemById(id);

        PlayerData playerData = Global.playerMap.get(playerUuid);
        if (playerData == null) return;

        /*
        if (offHand != null) {
            playerData.PotionTimer.setPotionEffects(offHand.potions);
        } else {
            playerData.PotionTimer.setPotionEffects(null);
        }

         */
        Global.playerMap.replace(playerUuid, playerData);
    }

    @EventHandler
    private void onHandItemClick(final PlayerInteractEvent event) {

    }

    @EventHandler
    public void onBlockBroke(final BlockBreakEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material material = block.getType();

        DropManager.ExtraDrops(block);
        UUID playerUuid = player.getUniqueId();

        PlayerData playerData = Global.playerMap.get(playerUuid);
        if (playerData == null) {
            playerData = new PlayerData(player);
            Global.playerMap.put(playerUuid, playerData);
        }

        int count = playerData.BreakList.getOrDefault(material, 0);
        playerData.BreakList.put(material, count + 1);
        Global.playerMap.replace(playerUuid, playerData);

    }

    @EventHandler
    public void onBlockPlaced(final BlockPlaceEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material material = block.getType();
        UUID playerUuid = player.getUniqueId();

        PlayerData playerData = Global.playerMap.get(playerUuid);
        if (playerData == null) {
            playerData = new PlayerData(player);
            Global.playerMap.put(playerUuid, playerData);
        }

        int count = playerData.PlaceList.getOrDefault(material, 0);
        playerData.PlaceList.put(material, count + 1);
        Global.playerMap.replace(playerUuid, playerData);

    }

    private String getPersistentItemID(ItemStack is) {
        if (is == null) return null;
        if (is.getItemMeta() == null) return null;

        PersistentDataContainer container = is.getItemMeta().getPersistentDataContainer();
        NamespacedKey mainKey = new NamespacedKey(plugin, Global.PERSISTENT_MAIN_KEY);

        return container.get(mainKey, PersistentDataType.STRING);
    }
}

package org.hcmc.hcplayground.listener;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.manager.DropManager;
import org.hcmc.hcplayground.manager.LocalizationManager;
import org.hcmc.hcplayground.model.MobEntity;
import org.hcmc.hcplayground.manager.MobManager;
import org.hcmc.hcplayground.model.Global;
import org.hcmc.hcplayground.model.RandomNumber;
import org.hcmc.hcplayground.playerManager.PlayerData;

import java.io.IOException;
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

    private final static String COMMAND_LOGIN = "login";
    private final static String COMMAND_REGISTER = "register";

    private final JavaPlugin plugin = HCPlayground.getPlugin();

    public PluginListener() {

    }

    /**
     * 玩家进入服务器事件
     *
     * @param event 玩家进入服务器时触发的事件实例
     * @throws SQLException 当SQL执行操作时发生异常
     */
    @EventHandler
    public void onPlayerJoined(final PlayerJoinEvent event) throws SQLException {
        Player player = event.getPlayer();
        UUID playerUuid = player.getUniqueId();

        PlayerData playerData = Global.getPlayerData(player);
        if (playerData.isBanned()) return;

        boolean exist = playerData.Exist();
        playerData.setRegister(exist);
        playerData.setLoginDTTM(new Date());

        Global.playerMap.put(playerUuid, playerData);
    }

    /**
     * 玩家离开服务器事件
     *
     * @param event 玩家离开服务器时触发的事件实例
     * @throws IOException 当IO执行操作时发生异常
     */
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) throws IOException {
        Player player = event.getPlayer();
        UUID playerUuid = player.getUniqueId();
        PlayerData playerData = Global.getPlayerData(player);
        playerData.SaveConfig();
        Global.playerMap.remove(playerUuid, playerData);
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled()) return;

        String message = event.getMessage();
        String[] keys = message.split(" ");
        String commandText = keys[0].substring(1);
        CommandMap commandMap = Global.CommandMap;
        Command command = commandMap.getCommand(commandText);
        if (command == null) return;

        Player player = event.getPlayer();
        PlayerData playerData = Global.getPlayerData(player);
        String playerName = player.getName();

        if (!playerData.getLogin() && !command.getName().equalsIgnoreCase(COMMAND_LOGIN) && !command.getName().equalsIgnoreCase(COMMAND_REGISTER)) {
            player.sendMessage(LocalizationManager.Messages.get("playerNoLogin").replace("%player%", playerName));
            Global.LogWarning(String.format("%s try to issue command %s before login", playerName, message));
            event.setCancelled(true);
        }
    }

    /**
     * 玩家扔掉物品事件
     *
     * @param event 玩家扔掉物品时触发的事件实例
     */
    @EventHandler
    public void onItemDropped(PlayerDropItemEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        UUID playerUuid = player.getUniqueId();
        Item item = event.getItemDrop();
        Material material = item.getItemStack().getType();

        PlayerData playerData = Global.getPlayerData(player);
        int count = playerData.DropList.getOrDefault(material, 0);
        playerData.DropList.put(material, count + 1);
        Global.playerMap.replace(playerUuid, playerData);
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        // TODO: 需要实施EntityPickupItemEvent事件
    }

    /**
     * 玩家钓鱼事件
     *
     * @param event 玩家钓鱼时触发的事件实例
     */
    @EventHandler
    public void onPlayerFished(PlayerFishEvent event) {
        if (event.isCancelled()) return;
        // 获取玩家实例
        Player player = event.getPlayer();
        UUID playerUuid = player.getUniqueId();
        // 扔出鱼饵，无论扔到水里或者地上，或者其他实体身上，getCaught()都会返回null
        // 钓鱼收竿时没有任何物品被钓上来，getCaught()都会返回null
        // 仅仅当钓到任何物品时，getCaught()才会返回Item实例
        Item item = (Item) event.getCaught();
        if (item == null) return;
        // 玩家的钓鱼记录
        PlayerData playerData = Global.getPlayerData(player);
        Material material = item.getItemStack().getType();
        int count = playerData.FishingList.getOrDefault(material, 0);
        playerData.FishingList.put(material, count + 1);
        Global.playerMap.replace(playerUuid, playerData);
        // 钓鱼时的额外掉落，额外掉落物品直接放入玩家背包
        DropManager.ExtraDrops(item, player);
    }

    /**
     * 方块被破坏时触发的事件
     *
     * @param event 方块被破坏时触发的事件实例
     */
    @EventHandler
    public void onBlockBroke(final BlockBreakEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        UUID playerUuid = player.getUniqueId();
        Block block = event.getBlock();
        Material material = block.getType();

        DropManager.ExtraDrops(block);

        PlayerData playerData = Global.getPlayerData(player);
        int count = playerData.BreakList.getOrDefault(material, 0);
        playerData.BreakList.put(material, count + 1);
        Global.playerMap.replace(playerUuid, playerData);
    }

    /**
     * 方块被放置时触发的事件
     *
     * @param event 方块被放置时触发的事件实例
     */
    @EventHandler
    public void onBlockPlaced(final BlockPlaceEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material material = block.getType();
        UUID playerUuid = player.getUniqueId();

        PlayerData playerData = Global.getPlayerData(player);
        int count = playerData.PlaceList.getOrDefault(material, 0);
        playerData.PlaceList.put(material, count + 1);
        Global.playerMap.replace(playerUuid, playerData);
    }

    @EventHandler
    public void onMonsterSpawned(final EntitySpawnEvent event) {
        if (event.isCancelled()) return;

        Entity entity = event.getEntity();
        EntityType type = entity.getType();
        if (!(entity instanceof Monster monster)) return;

        MobEntity mob = MobManager.MobEntities.stream().filter(x -> x.type.equals(type)).findAny().orElse(null);
        if (mob == null) return;
        if (!RandomNumber.checkBingo(mob.spawnRate)) return;

        int health = (int) Math.round(RandomNumber.getRandomNumber(mob.minHealth, mob.maxHealth));

        AttributeInstance attributeInstance = monster.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attributeInstance != null) {
            int baseHealth = (int) attributeInstance.getBaseValue();
            attributeInstance.setBaseValue(baseHealth + health);
            monster.setHealth(baseHealth + health);
        }
        if (mob.displays.length >= 1) {
            monster.setCustomName(mob.displays[RandomNumber.getRandomNumber(mob.displays.length)]);
        }
    }

    @EventHandler
    public void onMonsterAttacked(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;

        Entity entity = event.getDamager();
        EntityType type = entity.getType();
        if (!(entity instanceof Monster)) return;

        MobEntity mob = MobManager.MobEntities.stream().filter(x -> x.type.equals(type)).findAny().orElse(null);
        if (mob == null) return;

        int damage = (int) Math.round(RandomNumber.getRandomNumber(mob.minDamage, mob.maxDamage));
        event.setDamage(event.getDamage() + damage);
    }

    @EventHandler
    public void onMonsterDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Monster)) return;

        EntityType type = entity.getType();
        Location location = entity.getLocation();

        MobEntity mob = MobManager.MobEntities.stream().filter(x -> x.type.equals(type)).findAny().orElse(null);
        if (mob == null) return;

        if (RandomNumber.checkBingo(mob.spawnRate)) {
            DropManager.ExtraDrops(location, mob.drops);
        }
    }
}

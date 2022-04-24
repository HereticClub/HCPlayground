package org.hcmc.hcplayground.listener;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.event.PlayerEquipmentChangedEvent;
import org.hcmc.hcplayground.manager.DropManager;
import org.hcmc.hcplayground.manager.LocalizationManager;
import org.hcmc.hcplayground.manager.MobManager;
import org.hcmc.hcplayground.model.MobEntity;
import org.hcmc.hcplayground.model.item.ItemBase;
import org.hcmc.hcplayground.model.menu.MenuDetail;
import org.hcmc.hcplayground.model.menu.MenuItem;
import org.hcmc.hcplayground.model.player.PlayerData;
import org.hcmc.hcplayground.scheduler.PlayerSlotRunnable;
import org.hcmc.hcplayground.utility.Global;
import org.hcmc.hcplayground.utility.NameBinaryTagResolver;
import org.hcmc.hcplayground.utility.RandomNumber;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

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
    private final static String SCOREBOARD_CRITERIA_HEALTH = "health";
    private final static String SCOREBOARD_CRITERIA_DUMMY = "dummy";

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
    public void onPlayerJoined(final PlayerJoinEvent event) throws SQLException, IllegalAccessException {
        Player player = event.getPlayer();
        PlayerData playerData = Global.getPlayerData(player);
        Global.LogMessage(String.format("\033[1;35mPlayerJoinEvent GameMode: \033[1;33m%s\033[0m", playerData.GameMode));
        // 获取玩家的登陆时间
        playerData.LoginTime = new Date().getTime() / 1000;
        if (playerData.isBanned()) return;

        boolean exist = playerData.Exist();
        playerData.setRegister(exist);
        playerData.setLoginDTTM(new Date());
        Global.setPlayerData(player, playerData);

        player.setGameMode(GameMode.SPECTATOR);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) throws IllegalAccessException {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();

        PlayerData playerData = Global.getPlayerData(player);
        if (!playerData.getLogin()) event.setCancelled(true);
    }

    /**
     * 玩家离开服务器事件
     *
     * @param event 玩家离开服务器时触发的事件实例
     * @throws IOException 当IO执行操作时发生异常
     */
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) throws IOException, IllegalAccessException {
        Player player = event.getPlayer();

        PlayerData playerData = Global.getPlayerData(player);
        Global.LogMessage(String.format("\033[1;35mPlayerQuitEvent GameMode: \033[1;33m%s\033[0m", playerData.GameMode));
        player.setGameMode(playerData.GameMode);
        playerData.SaveConfig();
        Global.removePlayerData(player, playerData);
    }

    @EventHandler
    public void onPlayerGameModeChanged(PlayerGameModeChangeEvent event) throws IllegalAccessException {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        PlayerData playerData = Global.getPlayerData(player);
        if (!playerData.getLogin()) return;

        playerData.GameMode = event.getNewGameMode();
        Global.LogMessage(String.format("\033[1;35mPlayerGameModeChangeEvent GameMode: \033[1;33m%s\033[0m", playerData.GameMode));

        Global.setPlayerData(player, playerData);
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) throws IllegalAccessException {
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
            player.sendMessage(LocalizationManager.getMessage("playerNoLogin", player).replace("%player%", playerName));
            Global.LogWarning(String.format("%s tries to issue command %s before login", playerName, message));
            event.setCancelled(true);
        }
    }

    /**
     * 玩家钓鱼事件
     *
     * @param event 玩家钓鱼时触发的事件实例
     */
    @EventHandler
    public void onPlayerFished(PlayerFishEvent event) throws IllegalAccessException {
        if (event.isCancelled()) return;
        // 获取玩家实例
        Player player = event.getPlayer();
        // 扔出鱼饵，无论扔到水里或者地上，或者其他实体身上，getCaught()都会返回null
        // 钓鱼收竿时没有任何物品被钓上来，getCaught()都会返回null
        // 仅仅当钓到任何物品时，getCaught()才会返回Item实例
        Item item = (Item) event.getCaught();
        if (item == null) return;
        // 获取钓到的物品及数量
        ItemStack is = item.getItemStack();
        Material material = is.getType();
        int amount = is.getAmount();
        // 玩家的钓鱼记录
        PlayerData playerData = Global.getPlayerData(player);
        int count = playerData.FishingList.getOrDefault(material, 0);
        playerData.FishingList.put(material, count + amount);
        Global.setPlayerData(player, playerData);
        // 钓鱼时的额外掉落，额外掉落物品直接放入玩家背包
        DropManager.ExtraDrops(item, player);
    }

    @EventHandler
    private void onPlayerEquipmentChanged(PlayerEquipmentChangedEvent event) throws IllegalAccessException {
        Map<EquipmentSlot, ItemStack> equipments = event.getEquipments();
        Player player = event.getPlayer();
        Set<EquipmentSlot> slots = equipments.keySet();

        float health = 0;
        float armor = 0;
        float recover = 0;
        float armorToughness = 0;
        float knockBackResistance = 0;
        float movementSpeed = 0;
        // 检查玩家的装备栏和副手物品
        for (EquipmentSlot e : slots) {
            // 忽略没装备的物品
            ItemStack is = equipments.get(e);
            if (is == null) continue;
            // 忽略没有ItemMeta的物品
            ItemMeta im = is.getItemMeta();
            if (im == null) continue;
            // 获取玩家身上所有装备和副手物品的额外数值
            NameBinaryTagResolver nbt = new NameBinaryTagResolver(is);
            health += nbt.getFloatValue(ItemBase.PERSISTENT_HEALTH_KEY);
            armor += nbt.getFloatValue(ItemBase.PERSISTENT_ARMOR_KEY);
            recover += nbt.getFloatValue(ItemBase.PERSISTENT_RECOVER_KEY);
            armorToughness += nbt.getFloatValue(ItemBase.PERSISTENT_ARMOR_TOUGHNESS_KEY);
            knockBackResistance += nbt.getFloatValue(ItemBase.PERSISTENT_KNOCKBACK_RESISTANCE_KEY);
            movementSpeed += nbt.getFloatValue(ItemBase.PERSISTENT_MOVEMENT_SPEED_KEY);
        }

        PlayerData data = Global.getPlayerData(player);
        data.setTotalHealth(health);
        data.setTotalArmor(armor);
        data.setTotalRecover(recover);
        data.setTotalArmorToughness(armorToughness);
        data.setTotalKnockBackResistance(knockBackResistance);
        data.setTotalMovementSpeed(movementSpeed);
        Global.setPlayerData(player, data);
    }

    /**
     * 玩家扔掉物品事件
     *
     * @param event 玩家扔掉物品时触发的事件实例
     */
    @EventHandler
    public void onItemDropped(PlayerDropItemEvent event) throws IllegalAccessException {
        if (event.isCancelled()) return;
        // 获取玩家及UUID
        Player player = event.getPlayer();
        // 获取玩家扔出去的物品及数量
        Item item = event.getItemDrop();
        ItemStack is = item.getItemStack();
        int amount = is.getAmount();
        Material material = item.getItemStack().getType();
        // 记录玩家数据
        PlayerData playerData = Global.getPlayerData(player);
        int count = playerData.DropList.getOrDefault(material, 0);
        playerData.DropList.put(material, count + amount);
        Global.setPlayerData(player, playerData);
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) throws IllegalAccessException {
        if (event.isCancelled()) return;
        // 获取拾取物品的生物
        LivingEntity entity = event.getEntity();
        // 获取拾取的物品，类型及数量
        Item item = event.getItem();
        ItemStack is = item.getItemStack();
        Material material = is.getType();
        int amount = is.getAmount();

        // 记录玩家拾取物品的数据
        if (!(entity instanceof Player player)) return;
        PlayerData playerData = Global.getPlayerData(player);
        int count = playerData.PickupList.getOrDefault(material, 0);
        playerData.PickupList.put(material, count + amount);
        Global.setPlayerData(player, playerData);
    }

    /**
     * 方块被破坏时触发的事件
     *
     * @param event 方块被破坏时触发的事件实例
     */
    @EventHandler
    public void onBlockBroke(final BlockBreakEvent event) throws IllegalAccessException {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material material = block.getType();

        DropManager.ExtraDrops(block);

        PlayerData playerData = Global.getPlayerData(player);
        int count = playerData.BreakList.getOrDefault(material, 0);
        playerData.BreakList.put(material, count + 1);
        Global.setPlayerData(player, playerData);
    }

    /**
     * 方块被放置时触发的事件
     *
     * @param event 方块被放置时触发的事件实例
     */
    @EventHandler
    public void onBlockPlaced(final BlockPlaceEvent event) throws IllegalAccessException {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material material = block.getType();

        PlayerData playerData = Global.getPlayerData(player);
        int count = playerData.PlaceList.getOrDefault(material, 0);
        playerData.PlaceList.put(material, count + 1);
        Global.setPlayerData(player, playerData);
    }

    @EventHandler
    public void onMonsterSpawned(final EntitySpawnEvent event) {
        if (event.isCancelled()) return;

        String display = "";
        String prefix = "";
        Entity entity = event.getEntity();
        EntityType type = entity.getType();
        if (!(entity instanceof Monster monster)) return;

        MobEntity mobEntity = MobManager.MobEntities.stream().filter(x -> x.type.equals(type)).findAny().orElse(null);
        if (mobEntity == null) return;
        if (!RandomNumber.checkBingo(mobEntity.spawnRate)) return;

        int randomHealth = (int) Math.round(RandomNumber.getRandomNumber(mobEntity.minHealth, mobEntity.maxHealth));
        int monsterHealth = 0;
        AttributeInstance attributeInstance = monster.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attributeInstance != null) {
            int baseHealth = (int) attributeInstance.getBaseValue();
            monsterHealth = baseHealth + randomHealth;
            attributeInstance.setBaseValue(monsterHealth);
            monster.setHealth(baseHealth + randomHealth);
        }

        if (mobEntity.displays != null && mobEntity.displays.length >= 1) {
            display = mobEntity.displays[RandomNumber.getRandomNumber(mobEntity.displays.length)];
        }
        if (mobEntity.prefix != null && mobEntity.prefix.length >= 1) {
            prefix = mobEntity.prefix[RandomNumber.getRandomNumber(mobEntity.prefix.length)];
        }
        if (prefix.length() >= 1 && display.length() >= 1) {
            String customName = String.format("%s%s", prefix, display);
            monster.setCustomName(customName);
        }


        /*
        TODO: 需要实施在生物的名字下方显示生命值

        monster.addScoreboardTag(monster.getUniqueId().toString());
        Player[] players = plugin.getServer().getOnlinePlayers().toArray(new Player[0]);
        Scoreboard sb = Global.HealthScoreboard;
        Objective objective = sb.registerNewObjective(monster.getUniqueId().toString(), SCOREBOARD_CRITERIA_DUMMY, ChatColor.RED + "❤");
        objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
        //objective.setRenderType(RenderType.HEARTS);
        objective.getScore("").setScore(monsterHealth);

        for (Player p : players) {
            p.setScoreboard(sb);
        }

        Player player = plugin.getServer().getPlayer("TerryNG9527");
        if (player == null) return;
        Location location = player.getLocation();
        location.setX(location.getX() + 5);
        monster.teleport(location);

        location.setY(location.getY() + 0.7);
        ArmorStand armorStand=monster.getWorld().spawn(location, ArmorStand.class);
        armorStand.setCustomNameVisible(true);
        armorStand.setCustomName(String.valueOf(monsterHealth));
        armorStand.setSmall(true);
        armorStand.setGravity(false);

         */


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
    public void onEntityDeath(EntityDeathEvent event) throws IllegalAccessException {
        // 获取死亡的生物实例
        LivingEntity entity = event.getEntity();
        // 获取生物的类型及位置
        EntityType type = entity.getType();
        Location location = entity.getLocation();

        Scoreboard sb = Global.HealthScoreboard;
        Objective objective = sb.getObjective(entity.getUniqueId().toString());
        if (objective != null) objective.unregister();
        // 获取生物的额外掉落列表及掉落概率
        MobEntity mob = MobManager.MobEntities.stream().filter(x -> x.type.equals(type)).findAny().orElse(null);
        if (mob != null && RandomNumber.checkBingo(mob.spawnRate)) {
            DropManager.ExtraDrops(location, mob.drops);
        }
        // 记录玩家杀死生物的数量
        Player player = entity.getKiller();
        if (player == null) return;
        PlayerData playerData = Global.getPlayerData(player);
        int count = playerData.KillMobList.getOrDefault(type, 0);
        playerData.KillMobList.put(type, count + 1);
        Global.setPlayerData(player, playerData);
    }

    @EventHandler
    public void onEquipmentSlotClicked(InventoryClickEvent event) {
        if (event.isCancelled()) return;
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof Player player)) return;
        InventoryType.SlotType slotType = event.getSlotType();
        int slot = event.getSlot();
        if(slotType.equals(InventoryType.SlotType.CONTAINER)) return;
        if(slotType.equals(InventoryType.SlotType.RESULT)) return;
        if(slotType.equals(InventoryType.SlotType.CRAFTING)) return;
        // Slot number 40 on Quick bar is offhand
        if(slotType.equals(InventoryType.SlotType.QUICKBAR) && slot != 40) return;

        PlayerSlotRunnable runnable = new PlayerSlotRunnable(player);
        runnable.runTask(plugin);
    }

    @EventHandler
    public void onMenuClicked(InventoryClickEvent event) {
        if (event.isCancelled()) return;
        // 获取打开的箱子界面并且当前箱子是否属于InventoryDetail实例
        Inventory inv = event.getInventory();
        InventoryHolder holder = inv.getHolder();
        HumanEntity human = event.getWhoClicked();
        // 检测是否打开了属于InventoryDetail实例创建的箱子
        if (!(holder instanceof MenuDetail detail)) return;
        // 检测点击箱子界面的实体是否为玩家
        if (!(human instanceof Player player)) return;
        // 检测玩家点击的箱子界面是否属于玩家的背包或快捷栏
        // 如果使用了鼠标单击则忽略事件
        // 如果使用了SHIFT+鼠标点击则取消事件
        Inventory pInv = event.getClickedInventory();
        boolean isPlayerInventory = pInv instanceof PlayerInventory;
        if (isPlayerInventory) {
            if (event.isShiftClick()) event.setCancelled(true);
            return;
        }
        // 获得玩家点击箱子中某个格子的InventorySlot实例
        // 检测当前点击的格子位置是否可放可拿
        int index = event.getSlot();
        MenuItem slot = detail.getSlot(index + 1);
        if (slot == null) {
            event.setCancelled(true);
            return;
        }

        List<String> commands = new ArrayList<>();
        ClickType clickType = event.getClick();
        if (clickType.equals(ClickType.LEFT)) commands = slot.leftCommands;
        if (clickType.equals(ClickType.RIGHT)) commands = slot.rightCommands;

        for (String s : commands) {
            int firstSpace = s.indexOf(" ");
            String key = firstSpace >= 1 ? s.substring(0, firstSpace) : s;
            String command = firstSpace >= 1 ? s.substring(firstSpace + 1) : s;
            command = command.replace("%player%", player.getName()).trim();
            if (key.contains("[console]") || !key.contains("[") || !key.contains("]")) {
                runConsoleCommand(command, player);
            }
            if (key.contains("[player]")) {
                runPlayerCommand(command, player);
            }
        }


        if (!slot.draggable || !slot.droppable) event.setCancelled(true);
    }

    private void runConsoleCommand(String command, Player player) {
        ConsoleCommandSender sender = Bukkit.getConsoleSender();
        Bukkit.dispatchCommand(sender, command);

        Global.LogMessage(String.format("%s issued a console command: %s", player.getName(), command));
    }

    private void runPlayerCommand(String command, Player player) {
        player.performCommand(command);
        Global.LogMessage(String.format("%s issued a player command: %s", player.getName(), command));
    }
}

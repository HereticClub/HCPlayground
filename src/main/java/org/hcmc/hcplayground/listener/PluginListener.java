package org.hcmc.hcplayground.listener;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.enums.RecipeType;
import org.hcmc.hcplayground.event.PlayerEquipmentChangedEvent;
import org.hcmc.hcplayground.manager.*;
import org.hcmc.hcplayground.model.MobEntity;
import org.hcmc.hcplayground.model.config.BanConfiguration;
import org.hcmc.hcplayground.model.menu.MenuDetail;
import org.hcmc.hcplayground.model.menu.MenuItem;
import org.hcmc.hcplayground.model.player.PlayerData;
import org.hcmc.hcplayground.scheduler.EquipmentMonitorRunnable;
import org.hcmc.hcplayground.utility.Global;
import org.hcmc.hcplayground.utility.RandomNumber;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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

    private final static int EQUIPMENT_SLOT_OFFHAND = 40;
    private final static int EQUIPMENT_SLOT_HELMET = 39;

    private final JavaPlugin plugin = HCPlayground.getPlugin();

    public PluginListener() {

    }

    /**
     * 玩家进入服务器事件<br>
     * 当玩家第一次进入服务器，需要执行/register password password以注册<br>
     * 后续的进入服务器，需要执行/login password以登陆
     *
     * @param event 玩家进入服务器事件
     * @throws SQLException 当SQL执行操作时发生异常
     */
    @EventHandler
    private void onPlayerJoined(final PlayerJoinEvent event) throws SQLException, IllegalAccessException {
        // 获取登陆玩家实例
        Player player = event.getPlayer();
        // 获取玩家实例的附加数据
        PlayerData playerData = PlayerManager.getPlayerData(player);
        // 判断玩家是否被禁止进入服务器
        if (playerData.isBanned()) return;
        // 获取玩家是否已经注册到服务器
        // 用于显示提醒登陆或者提醒注册
        boolean exist = playerData.Exist();
        playerData.setRegister(exist);
        // 获取并记录玩家的登陆时间
        playerData.loginTimeStamp = new Date().getTime() / 1000;
        playerData.setLoginTime(new Date());
        // 获取玩家身上的附加属性
        // 任务new EquipmentMonitorRunnable(player).runTask(plugin)
        // 已经执行了一次PlayerManager.setPlayerData(player, playerData)
        // 所以不需要在这里再次执行
        new EquipmentMonitorRunnable(player).runTask(plugin);
        // 在成功登陆前，把玩家的游戏模式设置为SPECTATOR，防止被怪物攻击
        // 在成功登陆后，把玩家的游戏模式设置为上次退出服务器时的游戏模式
        //Global.LogMessage(String.format("\033[1;35mPlayerJoinEvent GameMode: \033[1;33m%s\033[0m", playerData.GameMode));
        player.setGameMode(GameMode.SPECTATOR);
    }

    /**
     * 玩家移动事件<br>
     * 当玩家没有正式登陆前，禁止移动
     *
     * @param event 玩家移动事件
     * @throws IllegalAccessException IllegalAccessException
     */
    @EventHandler
    private void onPlayerMove(PlayerMoveEvent event) throws IllegalAccessException {
        if (event.isCancelled()) return;
        // 获取玩家实例
        Player player = event.getPlayer();
        // 获取玩家附加数据实例
        PlayerData playerData = PlayerManager.getPlayerData(player);
        // 在玩家成功登陆前，禁止玩家移动
        if (!playerData.getLogin()) event.setCancelled(true);
    }

    /**
     * 玩家离开服务器事件<br>
     * 保存玩家所有数据
     *
     * @param event 玩家离开服务器事件
     * @throws IOException 当IO执行操作时发生异常
     */
    @EventHandler
    private void onPlayerLeave(PlayerQuitEvent event) throws IOException, IllegalAccessException {
        // 获得玩家实例
        Player player = event.getPlayer();
        PlayerData playerData = PlayerManager.getPlayerData(player);
        //Global.LogMessage(String.format("\033[1;35mPlayerQuitEvent GameMode: \033[1;33m%s\033[0m", playerData.GameMode));
        player.setGameMode(playerData.GameMode);
        playerData.SaveConfig();
        PlayerManager.removePlayerData(player, playerData);
    }

    @EventHandler
    private void onPlayerGameModeChanged(PlayerGameModeChangeEvent event) throws IllegalAccessException {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        PlayerData playerData = PlayerManager.getPlayerData(player);
        if (!playerData.getLogin()) return;

        playerData.GameMode = event.getNewGameMode();
        //Global.LogMessage(String.format("\033[1;35mPlayerGameModeChangeEvent GameMode: \033[1;33m%s\033[0m", playerData.GameMode));
        PlayerManager.setPlayerData(player, playerData);
    }

    @EventHandler
    private void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) throws IllegalAccessException {
        if (event.isCancelled()) return;

        String message = event.getMessage();
        String[] keys = message.split(" ");
        String commandText = keys[0].substring(1);
        CommandMap commandMap = Global.CommandMap;
        Command command = commandMap.getCommand(commandText);
        if (command == null) return;

        Player player = event.getPlayer();
        PlayerData playerData = PlayerManager.getPlayerData(player);
        String playerName = player.getName();

        if (!playerData.getLogin() && !command.getName().equalsIgnoreCase(COMMAND_LOGIN) && !command.getName().equalsIgnoreCase(COMMAND_REGISTER)) {
            player.sendMessage(LocalizationManager.getMessage("playerNoLogin", player).replace("%player%", playerName));
            Global.LogWarning(String.format("%s tries to issue command %s before login", playerName, message));
            event.setCancelled(true);
        }
    }

    /**
     * 玩家钓鱼事件<br>
     * 记录玩家的钓鱼数量
     *
     * @param event 玩家钓鱼时触发的事件实例
     */
    @EventHandler
    private void onPlayerFished(PlayerFishEvent event) throws IllegalAccessException {
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
        PlayerData playerData = PlayerManager.getPlayerData(player);
        int count = playerData.FishingList.getOrDefault(material, 0);
        playerData.FishingList.put(material, count + amount);
        PlayerManager.setPlayerData(player, playerData);
        // 钓鱼时的额外掉落，额外掉落物品直接放入玩家背包
        DropManager.ExtraDrops(item, player);
    }

    @EventHandler
    private void onPlayerHandItemChanged(PlayerItemHeldEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();

        EquipmentMonitorRunnable runnable = new EquipmentMonitorRunnable(player);
        runnable.runTask(plugin);
    }

    @EventHandler
    private void onPlayerEnchanting(PrepareAnvilEvent event) {
        AnvilInventory inv = event.getInventory();
        BanConfiguration banItem = BanItemManager.getBanItem(RecipeType.SMITHING);

        ItemStack item1 = inv.getItem(0);
        boolean hasEnchant1 = BanItemManager.checkEnchantments(item1, banItem.getEnchantments());
        ItemStack item2 = inv.getItem(1);
        boolean hasEnchant2 = BanItemManager.checkEnchantments(item2, banItem.getEnchantments());

        if (hasEnchant1 || hasEnchant2) {
            inv.setRepairCost((int) Math.pow(2, 31) - 1);
            ItemStack is = banItem.toBarrierItem();
            event.setResult(is);
        }
    }


    @EventHandler
    private void onPlayerAnvilEnchanted(InventoryClickEvent event) {
        if (event.isCancelled()) return;
        Inventory inv = event.getInventory();
        if (!(inv instanceof AnvilInventory anvil)) return;
        HumanEntity human = event.getWhoClicked();
        if (!(human instanceof Player player)) return;
        InventoryType.SlotType slotType = event.getSlotType();
        if (!slotType.equals(InventoryType.SlotType.RESULT)) return;

        String name = player.getName();
        /*
         TODO: 需要实施以下功能
          当玩家尝试使用铁砧附魔经验修补时，显示附魔失败信息，并且发出声音
        */
    }


    @EventHandler
    private void onPlayerEquipmentChanged(PlayerEquipmentChangedEvent event) throws IllegalAccessException {
        Map<EquipmentSlot, ItemStack> equipments = event.getEquipments();
        Player player = event.getPlayer();

        List<ItemStack> itemStacks = new ArrayList<>(equipments.values().stream().toList());
        PlayerManager.getEquipmentData(player, itemStacks.toArray(new ItemStack[0]));
    }

    /**
     * 玩家扔掉物品事件
     *
     * @param event 玩家扔掉物品时触发的事件实例
     */
    @EventHandler
    private void onItemDropped(PlayerDropItemEvent event) throws IllegalAccessException {
        if (event.isCancelled()) return;
        // 获取玩家及UUID
        Player player = event.getPlayer();
        // 获取玩家扔出去的物品及数量
        Item item = event.getItemDrop();
        ItemStack is = item.getItemStack();
        int amount = is.getAmount();
        Material material = item.getItemStack().getType();
        // 记录玩家数据
        PlayerData playerData = PlayerManager.getPlayerData(player);
        int count = playerData.DropList.getOrDefault(material, 0);
        playerData.DropList.put(material, count + amount);
        PlayerManager.setPlayerData(player, playerData);
    }

    @EventHandler
    private void onItemPickup(EntityPickupItemEvent event) throws IllegalAccessException {
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
        PlayerData playerData = PlayerManager.getPlayerData(player);
        int count = playerData.PickupList.getOrDefault(material, 0);
        playerData.PickupList.put(material, count + amount);
        PlayerManager.setPlayerData(player, playerData);
    }

    /**
     * 方块被破坏时触发的事件
     *
     * @param event 方块被破坏时触发的事件实例
     */
    @EventHandler
    private void onBlockBroken(final BlockBreakEvent event) throws IllegalAccessException {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material material = block.getType();

        DropManager.ExtraDrops(block);

        PlayerData playerData = PlayerManager.getPlayerData(player);
        int count = playerData.BreakList.getOrDefault(material, 0);
        playerData.BreakList.put(material, count + 1);
        PlayerManager.setPlayerData(player, playerData);
    }

    /**
     * 方块被放置时触发的事件
     *
     * @param event 方块被放置时触发的事件实例
     */
    @EventHandler
    private void onBlockPlaced(final BlockPlaceEvent event) throws IllegalAccessException {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material material = block.getType();

        PlayerData playerData = PlayerManager.getPlayerData(player);
        int count = playerData.PlaceList.getOrDefault(material, 0);
        playerData.PlaceList.put(material, count + 1);
        PlayerManager.setPlayerData(player, playerData);
    }

    @EventHandler
    private void onMonsterSpawned(final EntitySpawnEvent event) {
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
        AttributeInstance attributeInstance = monster.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attributeInstance != null) {
            int baseHealth = (int) attributeInstance.getBaseValue();
            int monsterHealth = baseHealth + randomHealth;
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
    private void onMonsterAttacked(EntityDamageByEntityEvent event) {
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
    private void onEntityDeath(EntityDeathEvent event) throws IllegalAccessException {
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
        PlayerData playerData = PlayerManager.getPlayerData(player);
        int count = playerData.KillMobList.getOrDefault(type, 0);
        playerData.KillMobList.put(type, count + 1);
        PlayerManager.setPlayerData(player, playerData);
    }

    @EventHandler
    private void onEquipmentSlotClicked(InventoryClickEvent event) {
        /*
         slot number
         40 - offhand
         39 - helmet
         38 - chest
         37 - leggings
         36 - boots
        */
        if (event.isCancelled()) return;
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof Player player)) return;
        InventoryType.SlotType slotType = event.getSlotType();
        int slot = event.getSlot();
        if (slotType.equals(InventoryType.SlotType.CONTAINER)) return;
        if (slotType.equals(InventoryType.SlotType.RESULT)) return;
        if (slotType.equals(InventoryType.SlotType.CRAFTING)) return;
        // Slot number 40 on Quick bar is offhand
        if (slotType.equals(InventoryType.SlotType.QUICKBAR) && slot != EQUIPMENT_SLOT_OFFHAND) return;

        ItemStack isCursor = event.getCursor();
        ItemStack isCurrent = event.getCurrentItem();
        // Slot number 39 on Quick bar is helmet
        if (slot == EQUIPMENT_SLOT_HELMET) {
            player.getInventory().setItem(slot, isCursor);
            event.setResult(Event.Result.DENY);
            player.setItemOnCursor(isCurrent);
        }

        EquipmentMonitorRunnable runnable = new EquipmentMonitorRunnable(player);
        runnable.runTask(plugin);
    }

    @EventHandler
    private void onMenuClicked(InventoryClickEvent event) {
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

    private void onItemCrafting(PrepareItemCraftEvent event) {

        Recipe recipe = event.getRecipe();

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

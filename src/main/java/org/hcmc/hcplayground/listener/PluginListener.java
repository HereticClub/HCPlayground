package org.hcmc.hcplayground.listener;

import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.enums.CrazyBlockType;
import org.hcmc.hcplayground.enums.RecipeType;
import org.hcmc.hcplayground.event.PlayerEquipmentChangedEvent;
import org.hcmc.hcplayground.event.WorldMorningEvent;
import org.hcmc.hcplayground.manager.*;
import org.hcmc.hcplayground.model.recipe.CrazyBlockRecord;
import org.hcmc.hcplayground.model.mob.MobEntity;
import org.hcmc.hcplayground.model.command.CommandItem;
import org.hcmc.hcplayground.model.config.BanItemConfiguration;
import org.hcmc.hcplayground.model.item.Crazy;
import org.hcmc.hcplayground.model.item.ItemBase;
import org.hcmc.hcplayground.model.menu.MenuDetail;
import org.hcmc.hcplayground.model.menu.MenuItem;
import org.hcmc.hcplayground.model.player.PlayerData;
import org.hcmc.hcplayground.runnable.EquipmentMonitorRunnable;
import org.hcmc.hcplayground.runnable.RecipeFinderRunnable;
import org.hcmc.hcplayground.sqlite.table.BanPlayerDetail;
import org.hcmc.hcplayground.utility.Global;
import org.hcmc.hcplayground.utility.RandomNumber;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.*;

public class PluginListener implements Listener {
    private final static String COMMAND_LOGIN = "login";
    private final static String COMMAND_REGISTER = "register";
    private final static String SCOREBOARD_CRITERIA_HEALTH = "health";
    private final static String SCOREBOARD_CRITERIA_DUMMY = "dummy";

    private final static int EQUIPMENT_SLOT_OFFHAND = 40;
    private final static int EQUIPMENT_SLOT_HELMET = 39;

    private final JavaPlugin plugin = HCPlayground.getInstance();

    public PluginListener() {

    }

    /**
     * 玩家进入服务器事件<br>
     * 当玩家第一次进入服务器，需要执行/register password password以注册<br>
     * 后续的进入服务器，需要执行/login password以登陆
     *
     * @param event 玩家进入服务器事件
     */
    @EventHandler
    private void onPlayerJoined(final PlayerJoinEvent event) throws SQLException, IllegalAccessException, IOException, InvalidConfigurationException {
        // 获取登陆玩家实例
        Player player = event.getPlayer();
        World world = player.getWorld();
        // 获取玩家实例的附加数据
        PlayerData data = PlayerManager.getPlayerData(player);
        // 进入服务器马上显示计分板
        data.ShowSidebar(world.getName());
        // 判断玩家是否被禁止进入服务器
        BanPlayerDetail detail = data.isBanned();
        if (detail != null) {
            DateFormat df = DateFormat.getDateInstance(DateFormat.FULL, Locale.CHINA);
            DateFormat tf = DateFormat.getTimeInstance(DateFormat.FULL, Locale.CHINA);
            String banDateTime = String.format("%s %s", df.format(detail.banDate), tf.format(detail.banDate));
            String bannedMessage = LanguageManager.getString("playerBannedMessage", player)
                    .replace("%player%", player.getName())
                    .replace("%master%", detail.masterName)
                    .replace("%reason%", detail.message)
                    .replace("%banDate%", banDateTime);
            player.kickPlayer(bannedMessage);
            return;
        }
        // 获取玩家是否已经注册到服务器
        // 用于显示提醒登陆或者提醒注册
        boolean register = data.isRegister();
        data.setRegister(register);
        // 获取并记录玩家的登陆时间
        data.loginTimeStamp = new Date().getTime() / 1000;
        data.setLoginTime(new Date());
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
     */
    @EventHandler
    private void onPlayerMove(PlayerMoveEvent event) throws IllegalAccessException, IOException, InvalidConfigurationException {
        if (event.isCancelled()) return;
        // 获取玩家实例
        Player player = event.getPlayer();
        // 获取玩家附加数据实例
        PlayerData data = PlayerManager.getPlayerData(player);
        // 在玩家成功登陆前，禁止玩家移动
        if (!data.isLogin()) {
            event.setCancelled(true);
            return;
        }
        data.designer.EdgeDetection(player.getLocation());
    }

    /**
     * 玩家离开服务器事件<br>
     * 保存玩家所有数据
     *
     * @param event 玩家离开服务器事件
     */
    @EventHandler
    private void onPlayerLeave(PlayerQuitEvent event) throws IOException, IllegalAccessException, InvalidConfigurationException {
        // 获得玩家实例
        Player player = event.getPlayer();
        PlayerData data = PlayerManager.getPlayerData(player);
        //Global.LogMessage(String.format("\033[1;35mPlayerQuitEvent GameMode: \033[1;33m%s\033[0m", playerData.GameMode));

        BukkitTask task = data.designer.getLeaveTask();
        if(task != null && !task.isCancelled()) task.cancel();

        player.setGameMode(data.getGameMode());
        data.SaveConfig();
        PlayerManager.removePlayerData(player, data);
    }

    /**
     * 玩家的游戏模式被改变事件
     */
    @EventHandler
    private void onPlayerGameModeChanged(PlayerGameModeChangeEvent event) throws IllegalAccessException, IOException, InvalidConfigurationException {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        PlayerData data = PlayerManager.getPlayerData(player);
        if (!data.isLogin()) return;

        data.setGameMode(event.getNewGameMode());
        //Global.LogMessage(String.format("\033[1;35mPlayerGameModeChangeEvent GameMode: \033[1;33m%s\033[0m", playerData.getGameMode()));
        PlayerManager.setPlayerData(player, data);
    }

    /**
     * 玩家发出的指令的预处理事件<br>
     * 在玩家成功登陆到游戏前，禁止执行除了/login, /register之外的任何指令
     */
    @EventHandler
    private void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) throws IllegalAccessException, IOException, InvalidConfigurationException {
        if (event.isCancelled()) return;

        String message = event.getMessage();
        String[] keys = message.split(" ");
        String commandText = keys[0].substring(1);
        CommandMap commandMap = Global.CommandMap;
        Command command = commandMap.getCommand(commandText);
        if (command == null) return;

        Player player = event.getPlayer();
        PlayerData data = PlayerManager.getPlayerData(player);
        String playerName = player.getName();
        // 无论任何人包括op玩家，都必须先登录，才能执行任何其他命令
        if (!data.isLogin() && !command.getName().equalsIgnoreCase(COMMAND_LOGIN) && !command.getName().equalsIgnoreCase(COMMAND_REGISTER)) {
            player.sendMessage(LanguageManager.getString("playerNoLogin", player).replace("%player%", playerName));
            Global.LogWarning(String.format("%s tries to issue command %s before login", playerName, message));
            event.setCancelled(true);
            return;
        }
        // op玩家可以绕过除登陆外的限制
        if (player.isOp()) return;
        // 跑酷赛道设计状态下，非op玩家禁止执行除/course外的任何指令
        if (data.isCourseDesigning && data.isLogin() && !command.getName().equalsIgnoreCase(CommandItem.COMMAND_COURSE)) {
            player.sendMessage(LanguageManager.getString("courseDenyCommandOnDesign", player));
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
    private void onPlayerFished(PlayerFishEvent event) throws IllegalAccessException, IOException, InvalidConfigurationException {
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

    /**
     * 玩家主副手的物品改变后的事件
     */
    @EventHandler
    private void onPlayerHandItemChanged(PlayerItemHeldEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();

        EquipmentMonitorRunnable runnable = new EquipmentMonitorRunnable(player);
        runnable.runTask(plugin);
    }

    /**
     * 玩家右键点击盔甲架事件
     */
    @EventHandler
    private void onArmorStandClicked(PlayerInteractAtEntityEvent event) {
        if (event.isCancelled()) return;
        Entity entity = event.getRightClicked();
        if(!(entity instanceof ArmorStand)) return;
        System.out.println(entity);
        // TODO: 实施取消玩家右键点击作为minion的盔甲架事件
    }

    @EventHandler
    private void onCrazyBlockClicked(PlayerInteractEvent event) throws IOException, IllegalAccessException, InvalidConfigurationException {
        Block block = event.getClickedBlock();
        Player player = event.getPlayer();
        Action action = event.getAction();
        PlayerData data = PlayerManager.getPlayerData(player);
        String playerName = player.getName();
        // 检测block是否null，比如右键点击了空气方块
        if (block == null) return;
        // 无论任何人包括op玩家，都必须先登录，才能进行任何互动
        if (!data.isLogin()) {
            player.sendMessage(LanguageManager.getString("playerNoLogin", player).replace("%player%", playerName));
            event.setCancelled(true);
            return;
        }
        // 跑酷赛道的方块互动检测
        boolean allowInteract = data.designer.InteractDetection(block);
        if (!allowInteract) {
            player.sendMessage(LanguageManager.getString("courseNoPermission", player));
            event.setCancelled(true);
            return;
        }
        // 获取玩家的潜行状态
        boolean sneaking = player.isSneaking();
        // 检测玩家是否sneaking(潜行)状态
        if (sneaking) return;
        // 非右键点击无效
        if (!action.equals(Action.RIGHT_CLICK_BLOCK)) return;
        // 获取自定义可放置方块的记录信息
        CrazyBlockRecord record = RecordManager.findCrazyRecord(block.getLocation());
        if (record == null) return;
        // 获取自定义可放置方块的物品信息
        Crazy crazyItem = (Crazy) ItemManager.findItemById(record.getName());
        if (crazyItem == null) return;
        // 设置执行指令
        String crazyCommand = "";
        if (crazyItem.getType().equals(CrazyBlockType.CRAZY_CRAFTING_TABLE)) {
            crazyCommand = String.format("%s %s", CommandItem.COMMAND_CRAZY, CommandItem.COMMAND_CRAZY_CRAFTING);
        }
        if (crazyItem.getType().equals(CrazyBlockType.CRAZY_ENCHANTING_TABLE)) {
            crazyCommand = String.format("%s %s", CommandItem.COMMAND_CRAZY, CommandItem.COMMAND_CRAZY_ENCHANTING);
        }
        if (crazyItem.getType().equals(CrazyBlockType.CRAZY_ANVIL)) {
            crazyCommand = String.format("%s %s", CommandItem.COMMAND_CRAZY, CommandItem.COMMAND_CRAZY_ANVIL);
        }
        // 执行指令，并且取消当前事件(防止弹出系统界面)
        if (!StringUtils.isEmpty(crazyCommand)) {
            runPlayerCommand(crazyCommand, player);
            event.setCancelled(true);
        }
    }

    /**
     * 玩家在使用铁砧为物品(武器或盔甲)附魔时的预处理事件<br>
     * 禁止玩家使用普通铁砧为物品(武器或盔甲)附魔经验修复
     */
    @EventHandler
    private void onPlayerEnchanting(PrepareAnvilEvent event) {
        AnvilInventory inv = event.getInventory();
        BanItemConfiguration banItem = BanItemManager.getBanItem(RecipeType.SMITHING);

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

    /**
     * 玩家的盔甲栏物品被改变后的事件
     *
     * @param event
     * @throws IllegalAccessException
     */
    @EventHandler
    private void onPlayerEquipmentChanged(PlayerEquipmentChangedEvent event) throws IllegalAccessException, IOException, InvalidConfigurationException {
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
    private void onItemDropped(PlayerDropItemEvent event) throws IllegalAccessException, IOException, InvalidConfigurationException {
        if (event.isCancelled()) return;
        // 获取玩家及UUID
        Player player = event.getPlayer();
        PlayerData data = PlayerManager.getPlayerData(player);
        String playerName = player.getName();
        // 无论任何人包括op玩家，都必须先登录，才能扔掉些什么
        if (!data.isLogin()) {
            player.sendMessage(LanguageManager.getString("playerNoLogin", player).replace("%player%", playerName));
            event.setCancelled(true);
            return;
        }
        // 获取玩家扔出去的物品及数量
        Item item = event.getItemDrop();
        ItemStack is = item.getItemStack();
        int amount = is.getAmount();
        Material material = item.getItemStack().getType();
        // 记录玩家数据
        int count = data.DropList.getOrDefault(material, 0);
        data.DropList.put(material, count + amount);
        PlayerManager.setPlayerData(player, data);
    }

    @EventHandler
    private void onPlayerChangedWorld(PlayerChangedWorldEvent event) throws IOException, IllegalAccessException, InvalidConfigurationException {
        Player player = event.getPlayer();
        PlayerData data = PlayerManager.getPlayerData(player);
        World world = player.getWorld();

        data.ShowSidebar(world.getName());
        PlayerManager.setPlayerData(player, data);
    }

    /**
     * 生物(包括玩家)拾起物品事件
     */
    @EventHandler
    private void onItemPickup(EntityPickupItemEvent event) throws IllegalAccessException, IOException, InvalidConfigurationException {
        if (event.isCancelled()) return;
        // 获取拾取物品的生物，并且忽略非人类拾取事件
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Player player)) return;
        // 获取玩家数据
        PlayerData data = PlayerManager.getPlayerData(player);
        String playerName = player.getName();
        // 无论任何人包括op玩家，都必须先登录，才能拾取些什么
        if (!data.isLogin()) {
            player.sendMessage(LanguageManager.getString("playerNoLogin", player).replace("%player%", playerName));
            event.setCancelled(true);
            return;
        }
        // 获取拾取的物品，类型及数量
        Item item = event.getItem();
        ItemStack is = item.getItemStack();
        Material material = is.getType();
        int amount = is.getAmount();
        // 记录玩家拾取物品的数据
        int count = data.PickupList.getOrDefault(material, 0);
        data.PickupList.put(material, count + amount);
        PlayerManager.setPlayerData(player, data);
    }

    /**
     * 实体生物生成后事件
     */
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
         */
    }

    /**
     * 玩家尝试左键点击一个盔甲架事件
     */
    @EventHandler
    private void onMinionDamaged(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        Entity entity = event.getEntity();
        if (!(entity instanceof ArmorStand)) return;

        event.setCancelled(true);
        //TODO: 实施取消作为minion的盔甲架的伤害
    }

    /**
     * 实体攻击另一个实体事件
     */
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

    /**
     * 实体死亡事件
     */
    @EventHandler
    private void onEntityDeath(EntityDeathEvent event) throws IllegalAccessException, IOException, InvalidConfigurationException {
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

    /**
     * 方块被破坏时触发的事件
     *
     * @param event 方块被破坏时触发的事件实例
     */
    @EventHandler
    private void onBlockBroken(final BlockBreakEvent event) throws IllegalAccessException, IOException, InvalidConfigurationException {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        PlayerData data = PlayerManager.getPlayerData(player);
        Block block = event.getBlock();
        Material material = block.getType();
        // 检测玩家是否在跑酷设计状态，并且超出了跑酷设计范围
        if (!data.designer.RangeDetection(block.getLocation())) {
            event.setCancelled(true);
            return;
        }
        // 破坏方块时检测额外掉落
        DropManager.ExtraDrops(block);
        // 记录破坏方块的种类和数量
        int count = data.BreakList.getOrDefault(material, 0);
        data.BreakList.put(material, count + 1);
        PlayerManager.setPlayerData(player, data);
        // 记录疯狂方块的位置
        CrazyBlockRecord record = RecordManager.findCrazyRecord(block.getLocation());
        if (record == null) return;
        Crazy ib = (Crazy) ItemManager.findItemById(record.getName());
        if (ib == null) return;
        // 如果被破坏的方块是疯狂方块，则取消掉落，改为掉落疯狂方块相应的物品
        event.setDropItems(false);
        block.getWorld().dropItemNaturally(block.getLocation(), ib.toItemStack());
        // 移除疯狂方块的摆放记录
        RecordManager.removeCrazyRecord(record);
    }

    /**
     * 方块被放置时触发的事件
     *
     * @param event 方块被放置时触发的事件实例
     */
    @EventHandler
    private void onBlockPlaced(final BlockPlaceEvent event) throws IllegalAccessException, IOException, InvalidConfigurationException {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        PlayerData data = PlayerManager.getPlayerData(player);
        Block block = event.getBlock();
        Material material = block.getType();
        World world = block.getWorld();
        Location location = block.getLocation();
        ItemStack is = event.getItemInHand().clone();
        // 检测玩家是否在跑酷设计状态，并且超出了跑酷设计范围
        if (!data.designer.RangeDetection(block.getLocation())) {
            event.setCancelled(true);
            return;
        }
        if (MinionManager.isMinion(is)) {
            is.setAmount(1);
            block.setType(Material.AIR);
            Location asLocation = new Location(location.getWorld(), (int) location.getX(), (int) location.getY(), (int) location.getZ());
            asLocation.add(0.5, 0, 0.5);
            ArmorStand armorStand = (ArmorStand) world.spawnEntity(asLocation, EntityType.ARMOR_STAND);
            //armorStand.setMarker(true);
            armorStand.setBasePlate(false);
            armorStand.setGravity(true);
            armorStand.setArms(true);
            EntityEquipment equipment = armorStand.getEquipment();
            if (equipment != null) {
                equipment.setItem(EquipmentSlot.HEAD, is);
                equipment.setItem(EquipmentSlot.CHEST, new ItemStack(Material.DIAMOND_CHESTPLATE));
                equipment.setItem(EquipmentSlot.LEGS, new ItemStack(Material.DIAMOND_LEGGINGS));
                equipment.setItem(EquipmentSlot.FEET, new ItemStack(Material.DIAMOND_BOOTS));
                equipment.setItem(EquipmentSlot.HAND, new ItemStack(Material.DIAMOND_SWORD));
            }
        }

        // 计数玩家的各种方块摆放
        int count = data.PlaceList.getOrDefault(material, 0);
        data.PlaceList.put(material, count + 1);
        PlayerManager.setPlayerData(player, data);
        // 自定义可放置方块的摆放记录
        ItemBase ib = ItemManager.getItemBase(is);
        if (ib instanceof Crazy crazy) {
            CrazyBlockRecord record = new CrazyBlockRecord(crazy.getId(), block.getLocation());
            RecordManager.addCrazyRecord(record);
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
    private void onMenuClosed(InventoryCloseEvent event) {
        // 获取打开的箱子界面并且当前箱子是否属于InventoryDetail实例
        Inventory inv = event.getInventory();
        HumanEntity human = event.getPlayer();
        InventoryHolder holder = inv.getHolder();
        // 检测是否打开了属于InventoryDetail实例创建的箱子
        if (!(holder instanceof MenuDetail detail)) return;
        // 检测点击箱子界面的实体是否为玩家
        if (!(human instanceof Player player)) return;
        // 检测箱子界面的每个格子
        for (int i = 0; i < 54; i++) {
            // 获取格子的物品及数量
            ItemStack is = inv.getItem(i);
            // 获取格子的额外信息
            MenuItem mi = detail.getSlot(i + 1);
            // 格子内没有物品或者物品没有额外信息则忽略
            if (is == null) continue;
            if (mi == null) continue;
            // 除了成品输出格子外，如果格子设置为可放入或者可拿取，则返还格子的物品给玩家
            if ((mi.draggable || mi.droppable) && !mi.result) player.getInventory().addItem(is);
        }
    }

    @EventHandler
    private void onMenuOrRecipeClicked(InventoryClickEvent event) {
        if (event.isCancelled()) return;
        // 获取打开的箱子界面并且当前箱子是否属于InventoryDetail实例
        Inventory inv = event.getInventory();
        InventoryHolder holder = inv.getHolder();
        HumanEntity human = event.getWhoClicked();
        ClickType clickType = event.getClick();
        // 检测是否打开了属于InventoryDetail实例创建的箱子
        if (!(holder instanceof MenuDetail detail)) return;
        // 检测点击箱子界面的实体是否为玩家
        if (!(human instanceof Player player)) return;
        // 检测玩家点击的箱子界面是否属于玩家的背包或快捷栏
        Inventory pInv = event.getClickedInventory();
        if (pInv instanceof PlayerInventory && !clickType.isShiftClick()) return;
        // 获得玩家点击箱子中某个格子的InventorySlot实例
        int slotIndex = event.getSlot();
        MenuItem slot = detail.getSlot(slotIndex + 1);
        if (slot == null) {
            event.setCancelled(true);
            return;
        }
        // 执行自定义菜单的鼠标点击指令
        List<String> commands = new ArrayList<>();
        // 获取鼠标点击类型，鼠标的左键，右键，中建点击
        if (clickType.equals(ClickType.LEFT)) commands = slot.leftCommands;
        if (clickType.equals(ClickType.RIGHT)) commands = slot.rightCommands;
        runMenuItemCommand(commands, player);
        // 检查自定义合成公式并且在输入格子展示合成物品，让玩家拿取
        // TODO: 检查自定义合成公式并且在输入格子展示合成物品，让玩家拿取
        RecipeFinderRunnable finder = new RecipeFinderRunnable(inv);
        finder.runTask(plugin);

        // 过滤不可放置格子和不可拿取格子的动作
        InventoryAction action = event.getAction();
        boolean placeFlag = action.equals(InventoryAction.PLACE_ALL) || action.equals(InventoryAction.PLACE_ONE) || action.equals(InventoryAction.PLACE_SOME) || event.isShiftClick();
        boolean pickupFlag = action.equals(InventoryAction.PICKUP_ALL) || action.equals(InventoryAction.PICKUP_ONE) || action.equals(InventoryAction.PICKUP_HALF) || event.isShiftClick();
        if (action.equals(InventoryAction.SWAP_WITH_CURSOR)) event.setCancelled(true);
        if (!slot.droppable && placeFlag) event.setCancelled(true);
        if (!slot.draggable && pickupFlag) event.setCancelled(true);
    }

    @EventHandler
    private void onItemDragging(InventoryDragEvent event) {
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
        Set<Integer> slots = event.getRawSlots();
        for (Integer index : slots) {
            if (index >= 54) continue;
            MenuItem slot = detail.getSlot(index + 1);
            if (slot == null || !slot.droppable) {
                event.setCancelled(true);
                break;
            }
        }
        RecipeFinderRunnable finder = new RecipeFinderRunnable(inv);
        finder.runTask(plugin);
    }

    /**
     * Morning call
     */
    @EventHandler
    private void onWorldMorning(WorldMorningEvent event) {
        World world = event.getWorld();
        Player[] players = Bukkit.getOnlinePlayers().toArray(new Player[0]);

        for (Player player : players) {
            if (player.getWorld().equals(world))
                player.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_DEATH, 1f, 1f);
        }
    }

    private void runMenuItemCommand(List<String> commands, Player player) {
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

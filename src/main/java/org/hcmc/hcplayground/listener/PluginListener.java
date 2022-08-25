package org.hcmc.hcplayground.listener;

import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
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
import org.hcmc.hcplayground.enums.PanelSlotType;
import org.hcmc.hcplayground.enums.RecipeType;
import org.hcmc.hcplayground.event.PlayerEquipmentChangedEvent;
import org.hcmc.hcplayground.event.WorldMorningEvent;
import org.hcmc.hcplayground.manager.*;
import org.hcmc.hcplayground.model.item.*;
import org.hcmc.hcplayground.model.menu.MenuPanel;
import org.hcmc.hcplayground.model.menu.MenuPanelSlot;
import org.hcmc.hcplayground.model.minion.MinionEntity;
import org.hcmc.hcplayground.model.minion.MinionPanel;
import org.hcmc.hcplayground.model.minion.MinionPanelSlot;
import org.hcmc.hcplayground.model.recipe.CraftPanel;
import org.hcmc.hcplayground.model.recipe.CraftPanelSlot;
import org.hcmc.hcplayground.model.recipe.CrazyShapedRecipe;
import org.hcmc.hcplayground.model.recipe.HCItemBlockRecord;
import org.hcmc.hcplayground.model.mob.MobEntity;
import org.hcmc.hcplayground.model.command.CommandItem;
import org.hcmc.hcplayground.model.config.BanItemConfiguration;
import org.hcmc.hcplayground.model.player.PlayerData;
import org.hcmc.hcplayground.runnable.EquipmentMonitorRunnable;
import org.hcmc.hcplayground.runnable.RecipeSearchRunnable;
import org.hcmc.hcplayground.runnable.StatisticPickupDecrement;
import org.hcmc.hcplayground.sqlite.table.BanPlayerDetail;
import org.hcmc.hcplayground.utility.Global;
import org.hcmc.hcplayground.utility.RandomNumber;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.*;

public class PluginListener implements Listener {
    private final static String COMMAND_LOGIN = "login";
    private final static String COMMAND_REGISTER = "register";
    private final static String COMMAND_ENCHANT = "enchant";

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
    private void onPlayerJoined(final PlayerJoinEvent event) throws SQLException {
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
        data.setLoginTimeStamp(new Date().getTime() / 1000);
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
    private void onPlayerMove(PlayerMoveEvent event) {
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
        data.getDesigner().EdgeDetection(player.getLocation());
        PlayerManager.setPlayerData(player, data);
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

        BukkitTask task = data.getDesigner().getLeaveTask();
        if(task != null && !task.isCancelled()) task.cancel();

        player.setGameMode(data.getGameMode());
        PlayerManager.SaveConfig(player);
        PlayerManager.removePlayerData(player, data);
    }

    /**
     * 玩家的游戏模式被改变事件
     */
    @EventHandler
    private void onPlayerGameModeChanged(PlayerGameModeChangeEvent event) {
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
    private void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled()) return;

        String message = event.getMessage();
        String[] keys = message.split(" ");
        String commandText = keys[0].substring(1);
        CommandMap commandMap = Global.getCommandMap();
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
        // 跑酷赛道设计状态下，非op玩家禁止执行除/course外的任何指令
        if (data.isDesignMode() && !player.isOp() && !command.getName().equalsIgnoreCase(CommandItem.COMMAND_COURSE)) {
            player.sendMessage(LanguageManager.getString("courseDenyCommandOnDesign", player));
            event.setCancelled(true);
        }
        // 使用/enchant指令为自定义物品附魔后，更新物品的说明
        // 必须使用runnable方式
        if (command.getName().equalsIgnoreCase(COMMAND_ENCHANT)) {
            ItemManager.updateLoreOnRunnable(player);
        }
    }

    /**
     * 玩家钓鱼事件<br>
     * 记录玩家的钓鱼数量
     *
     * @param event 玩家钓鱼时触发的事件实例
     */
    @EventHandler
    private void onPlayerFished(PlayerFishEvent event) {
        if (event.isCancelled()) return;
        // 获取玩家实例
        Player player = event.getPlayer();
        // 扔出鱼饵，无论扔到水里或者地上，或者其他实体身上，getCaught()都会返回null
        // 钓鱼收竿时没有任何物品被钓上来，getCaught()都会返回null
        // 仅仅当钓到任何物品时，getCaught()才会返回Item实例
        Item item = (Item) event.getCaught();
        if (item == null) return;
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
    private void onMinionPanelOpened(PlayerInteractAtEntityEvent event) {
        if (event.isCancelled()) return;
        // 获取被点击的Entity实体
        Entity entity = event.getRightClicked();
        Player player = event.getPlayer();
        // 判断非ArmorStand
        if (!(entity instanceof ArmorStand armorStand)) return;
        MinionEntity minion = RecordManager.getMinionRecord(armorStand.getUniqueId());
        if (minion == null) return;
        player.openInventory(minion.openControlPanel());
        minion.refreshSack();
    }


    @EventHandler
    private void onItemInteracted(PlayerInteractEvent event) {
        ItemStack is = event.getItem();
        Player player = event.getPlayer();
        Action action = event.getAction();
        EquipmentSlot slot = event.getHand();
        String worldName = player.getWorld().getName();
        List<String> commands = new ArrayList<>();
        if (is == null) return;
        // PlayerInteractEvent分别用HAND和OFF_HAND触发2次
        // 如果使用右键点击，则需要过滤使用OFF_HAND触发的事件
        if (Objects.equals(slot, EquipmentSlot.OFF_HAND)) {
            // 不能简单返回，必须取消事件，否则物品会被使用，比如地图会被打开
            event.setUseItemInHand(Event.Result.DENY);
            return;
        }

        ItemBase ib = ItemManager.getItemBase(is);
        if (!(ib instanceof Join join)) return;
        if (!join.isInteractedItem()) {
            event.setUseItemInHand(Event.Result.DENY);
        }
        // 过滤自定义方块在不可用世界
        if (join.isDisabledWorld(player)) {
            player.sendMessage(LanguageManager.getString("no-world-interact", player).replace("%world%", worldName));
            return;
        }

        if (Objects.equals(action, Action.RIGHT_CLICK_BLOCK) || Objects.equals(action, Action.RIGHT_CLICK_AIR)) {
            commands.addAll(join.getMainHandRightClicks());
        }
        if (Objects.equals(action, Action.LEFT_CLICK_BLOCK) || Objects.equals(action, Action.LEFT_CLICK_AIR)) {
            commands.addAll(join.getMainHandLeftClicks());
        }
        prepareCommandList(commands, player);
    }

    @EventHandler
    private void onCrazyBlockClicked(PlayerInteractEvent event)  {
        Block block = event.getClickedBlock();
        Player player = event.getPlayer();
        Action action = event.getAction();
        PlayerData data = PlayerManager.getPlayerData(player);
        String playerName = player.getName();
        String worldName = player.getWorld().getName();
        // PlayerInteractEvent分别用HAND和OFF_HAND触发2次
        // 如果使用右键点击，则需要过滤使用OFF_HAND触发的事件
        EquipmentSlot slot = event.getHand();
        if (Objects.equals(slot, EquipmentSlot.OFF_HAND)) return;
        // 检测block是否null，比如右键点击了空气方块
        if (block == null) return;
        // 无论任何人包括op玩家，都必须先登录，才能进行任何互动
        if (!data.isLogin()) {
            player.sendMessage(LanguageManager.getString("playerNoLogin", player).replace("%player%", playerName));
            event.setCancelled(true);
            return;
        }
        // 跑酷赛道的方块互动检测
        boolean allowInteract = data.getDesigner().InteractDetection(block);
        if (!allowInteract) {
            player.sendMessage(LanguageManager.getString("courseNoPermission", player));
            event.setCancelled(true);
            return;
        }
        // 获取玩家的潜行状态
        boolean sneaking = player.isSneaking();
        // 检测玩家是否sneaking(潜行)状态
        // 玩家尝试在可互动方块上放置其他方块
        // 潜行+右键=放置方块
        if (sneaking) return;
        // 非右键点击无效
        if (!action.equals(Action.RIGHT_CLICK_BLOCK)) return;
        // 获取自定义可放置方块的记录信息
        HCItemBlockRecord record = RecordManager.getHCItemRecord(block.getLocation());
        if (record == null) return;
        // 获取自定义可放置方块的物品信息
        ItemBase ib = ItemManager.findItemById(record.getName());
        if (!(ib instanceof Crazy crazyItem)) return;
        // 判断自定义方块是否在不可用世界
        if (crazyItem.isDisabledWorld(player)) {
            player.sendMessage(LanguageManager.getString("no-world-interact", player).replace("%world%", worldName));
            return;
        }
        // 判断自定义方块是否可常规互动，比如吃，打开地图等
        if (!crazyItem.isInteractedBlock()) event.setUseInteractedBlock(Event.Result.DENY);
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
            CommandManager.runPlayerCommand(crazyCommand, player);
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

    @EventHandler
    private void onItemCrafting(PrepareItemCraftEvent event) {
        if (event.getRecipe() == null) return;
        CraftingInventory inventory = event.getInventory();
        ItemStack[] itemStacks = inventory.getMatrix();

        for (ItemStack itemStack : itemStacks) {
            if (itemStack == null) continue;
            ItemBase ib = ItemManager.getItemBase(itemStack);
            if (ib == null) continue;

            ItemStack result = RecipeManager.getBarrierItem();
            inventory.setResult(result);
            break;
        }
    }

    @EventHandler
    private void onItemCrafted(CraftItemEvent event) {
        if (event.isCancelled()) return;
        CraftingInventory inventory = event.getInventory();
        ItemStack[] itemStacks = inventory.getMatrix();

        for (ItemStack itemStack : itemStacks) {
            if (itemStack == null) continue;
            ItemBase ib = ItemManager.getItemBase(itemStack);
            if (ib == null) continue;

            event.setCancelled(true);
            break;
        }
    }

    /**
     * 玩家的盔甲栏物品被改变后的事件
     */
    @EventHandler
    private void onPlayerEquipmentChanged(PlayerEquipmentChangedEvent event) {
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
    private void onItemDropped(PlayerDropItemEvent event) {
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
        PlayerManager.setPlayerData(player, data);
    }

    @EventHandler
    private void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
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
    private void onItemPickup(EntityPickupItemEvent event) {
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
        // 获取掉落物品
        Item item = event.getItem();
        // 物品的掉落者
        UUID thrower = item.getThrower();
        if (thrower == null) return;
        // 拾取任何玩家(包含自己)扔出来的物品不在统计内
        boolean isPlayer = Arrays.stream(Bukkit.getOfflinePlayers()).anyMatch(x -> x.getUniqueId().equals(thrower));
        if (isPlayer) {
            StatisticPickupDecrement decrement = new StatisticPickupDecrement(player, item.getItemStack());
            decrement.runTask(plugin);
        }
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

        int randomHealth = (int) Math.round(RandomNumber.getRandomDouble(mobEntity.minHealth, mobEntity.maxHealth));
        AttributeInstance attributeInstance = monster.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attributeInstance != null) {
            int baseHealth = (int) attributeInstance.getBaseValue();
            int monsterHealth = baseHealth + randomHealth;
            attributeInstance.setBaseValue(monsterHealth);
            monster.setHealth(baseHealth + randomHealth);
        }

        if (mobEntity.displays != null && mobEntity.displays.length >= 1) {
            display = mobEntity.displays[RandomNumber.getRandomInteger(mobEntity.displays.length)];
        }
        if (mobEntity.prefix != null && mobEntity.prefix.length >= 1) {
            prefix = mobEntity.prefix[RandomNumber.getRandomInteger(mobEntity.prefix.length)];
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

        int damage = (int) Math.round(RandomNumber.getRandomDouble(mob.minDamage, mob.maxDamage));
        event.setDamage(event.getDamage() + damage);
    }

    /**
     * Cancel an entity try to damage on an armorstand(Minion)
     */
    @EventHandler
    private void onDamageArmorstand(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        Entity entity = event.getEntity();
        boolean exist = RecordManager.existMinionRecord(entity.getUniqueId());
        if (exist) {
            event.setCancelled(true);
        }
    }

    /**
     * 实体死亡事件
     */
    @EventHandler
    private void onEntityDeath(EntityDeathEvent event) {
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
    }

    /**
     * 方块被破坏时触发的事件
     * @param event 方块被破坏时触发的事件实例
     */
    @EventHandler
    private void onBlockBroken(final @NotNull BlockBreakEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        PlayerData data = PlayerManager.getPlayerData(player);
        Block block = event.getBlock();
        // 检测玩家是否在跑酷设计状态，并且超出了跑酷设计范围
        if (data.getDesigner().isOutOfRange(block.getLocation())) {
            event.setCancelled(true);
            return;
        }
        // 破坏方块时检测额外掉落
        DropManager.ExtraDrops(block);
        // 获取可摆放的自定义方块的记录位置
        HCItemBlockRecord record = RecordManager.getHCItemRecord(block.getLocation());
        if (record == null) return;
        // 根据记录获取ItemBase实例
        ItemBase ib = ItemManager.findItemById(record.getName());
        if (ib != null) {
            // 如果被破坏的方块是疯狂方块，则取消掉落，改为掉落疯狂方块相应的物品
            event.setDropItems(false);
            block.getWorld().dropItemNaturally(block.getLocation(), ib.toItemStack());
            // 移除疯狂方块的摆放记录
            RecordManager.removeHCItemRecord(record);
        }
    }

    /**
     * 方块被放置时触发的事件
     *
     * @param event 方块被放置时触发的事件实例
     */
    @EventHandler
    private void onBlockPlaced(final BlockPlaceEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        PlayerData data = PlayerManager.getPlayerData(player);
        Block block = event.getBlock();
        String worldName = player.getWorld().getName();
        // 被摆放方块的位置
        Location location = block.getLocation();
        // MainHandItem, 摆放方块时主手拿着的物品
        // 从行为上，当方块被摆放后，MainHandItem就已经被消灭
        // 因此在此处MainHandItem只能被某些判断逻辑使用
        // 而不是对MainHandItem进行处理，比如更改其材质，数量等
        ItemStack MainHandItem = event.getItemInHand().clone();
        // 检测玩家是否在跑酷设计状态，并且超出了跑酷设计范围
        if (data.getDesigner().isOutOfRange(block.getLocation())) {
            event.setCancelled(true);
            return;
        }
        if (MinionManager.isMinionStack(MainHandItem)) {
            MainHandItem.setAmount(1);
            block.setType(Material.AIR);

            Location minionLocation = new Location(location.getWorld(), (int) location.getX(), (int) location.getY(), (int) location.getZ());
            minionLocation.add(0.5, 0, 0.5);
            minionLocation.setDirection(player.getLocation().getDirection().multiply(-1));
            MinionEntity record = MinionManager.spawnMinion(minionLocation, MainHandItem);
            if (record == null) return;
            // 放置该Minion的玩家
            record.setOwner(player.getUniqueId());
            RecordManager.addMinionRecord(record);
        }
        // 自定义可放置方块的摆放记录
        ItemBase ib = ItemManager.getItemBase(MainHandItem);
        if (ib != null) {
            if (ib.isDisabledWorld(player)) {
                player.sendMessage(LanguageManager.getString("no-world-interact", player).replace("%world%", worldName));
                event.setCancelled(true);
                return;
            } else {
                HCItemBlockRecord record = new HCItemBlockRecord(ib.getId(), block.getLocation());
                RecordManager.addHCItemRecord(record);
            }
        }

        PlayerManager.setPlayerData(player, data);
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
    private void onMenuPanelClicked(InventoryClickEvent event) {
        if (event.isCancelled()) return;
        Inventory inventory = event.getInventory();
        InventoryAction action = event.getAction();
        ClickType click = event.getClick();

        int rawSlotIndex = event.getRawSlot();
        if (!(inventory.getHolder() instanceof MenuPanel panel)) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (rawSlotIndex >= 54) {
            if (action.equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) event.setCancelled(true);
            return;
        }

        MenuPanelSlot slot = panel.getDecorate(rawSlotIndex + 1);
        if (slot == null) {
            event.setCancelled(true);
            return;
        }

        if (click.isLeftClick()) slot.runLeftClickCommands(player, rawSlotIndex + 1);
        if (click.isRightClick()) slot.runRightClickCommands(player, rawSlotIndex + 1);

        event.setCancelled(true);
    }

    @EventHandler
    private void onMinionPanelClicked(InventoryClickEvent event) {
        if (event.isCancelled()) return;
        Inventory inventory = event.getInventory();
        ItemStack current = event.getCurrentItem();
        if (!(inventory.getHolder() instanceof MinionPanel panel)) return;
        MinionEntity minion = panel.getMinion();
        if (!(event.getWhoClicked() instanceof Player player)) return;
        // 判断点击了箱子界面以外
        // 判断是否SHIFT+CLICK
        // 判断点击了空白格子
        if (event.getRawSlot() >= 54 || event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY) || current == null) {
            event.setCancelled(true);
            return;
        }

        // 拿一组
        if (minion.isItemInSack(current)) panel.pickone(player, current);

        List<MinionPanelSlot> slots = panel.getSlots();
        int rawIndex = event.getRawSlot();
        MinionPanelSlot slot = slots.stream().filter(x -> Arrays.stream(x.getSlots()).anyMatch(y -> y == rawIndex)).findAny().orElse(null);
        if (slot == null) return;

        switch (slot.getType()) {
            case PERKS -> panel.removePerksDevice();
            case SMELT -> panel.removeSmeltDevice();
            case ENERGY -> panel.removeEnergyDevice();
            case COMPACT -> panel.removeCompactDevice();
            case PICKUP -> panel.pickup(player);
            case RECLAIM -> panel.Reclaim(player);
            case UPGRADE -> panel.Upgrade(player);
        }

        event.setCancelled(true);
    }

    @EventHandler
    private void onCrazyCrafting(InventoryClickEvent event) {
        if (event.isCancelled()) return;
        Inventory inventory = event.getInventory();
        InventoryAction action = event.getAction();

        if (!(inventory.getHolder() instanceof CraftPanel panel)) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        int rawSlot = event.getRawSlot();
        CraftPanelSlot slot = panel.getPanelSlot(rawSlot);
        CrazyShapedRecipe recipe = panel.getRecipe();
        PlayerData data = PlayerManager.getPlayerData(player);

        // 检测玩家使用鼠标Double Click叠堆物品
        // 不允许叠堆配方的成品输出
        // 叠堆和配方有关的物品后，需要重新检测配方
        if (rawSlot >= 54) {
            if (action.equals(InventoryAction.COLLECT_TO_CURSOR)) {
                CraftPanelSlot slotStorage = panel.getSlots().stream().filter(x -> x.getType().equals(PanelSlotType.STORAGE)).findAny().orElse(null);
                if (slotStorage == null) return;
                List<ItemStack> stacksStorage = new ArrayList<>();
                for (int i : slotStorage.getSlots()) {
                    stacksStorage.add(inventory.getItem(i));
                }
                if (stacksStorage.stream().anyMatch(x -> x != null && x.isSimilar(event.getCursor()))) {
                    RecipeSearchRunnable finder = new RecipeSearchRunnable(player, inventory);
                    finder.runTask(plugin);
                }
            }
            if (action.equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
                RecipeSearchRunnable finder = new RecipeSearchRunnable(player, inventory);
                finder.runTask(plugin);
            }
            return;
        }
        // 点击了没有定义的插槽，或者点击了INACTIVE类型的插槽，取消事件
        if (slot == null || slot.getType().equals(PanelSlotType.INACTIVE)) {
            event.setCancelled(true);
            return;
        }
        // 当有配方成品输出，并且点击了成品输出槽
        if (slot.getType().equals(PanelSlotType.OUTPUT)) {
            if (recipe == null || !data.existRecipe(recipe.getId())) {
                event.setCancelled(true);
                return;
            }

            if (action.equals(InventoryAction.PICKUP_ALL)
                    || action.equals(InventoryAction.PICKUP_SOME)
                    || action.equals(InventoryAction.PICKUP_ONE)
                    || action.equals(InventoryAction.PICKUP_HALF)
                    || action.equals(InventoryAction.SWAP_WITH_CURSOR)) {
                recipe.claimResult(player, inventory);
            }
            if (action.equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
                recipe.claimAll(player, inventory);
            }
            event.setCancelled(true);
        }

        RecipeSearchRunnable search = new RecipeSearchRunnable(player, inventory);
        search.runTask(plugin);
    }

    @EventHandler
    private void onCraftPanelClosed(InventoryCloseEvent event) {
        // 获取打开的箱子界面并且当前箱子是否属于CraftPanel实例
        Inventory inventory = event.getInventory();
        // 检测是否打开了属于CraftPanel实例创建的箱子
        if (!(inventory.getHolder() instanceof CraftPanel panel)) return;
        // 检测点击箱子界面的实体是否为玩家
        if (!(event.getPlayer() instanceof Player player)) return;
        // 检测箱子界面的每个格子
        CraftPanelSlot slot = panel.getSlots().stream().filter(x -> x.getType().equals(PanelSlotType.STORAGE)).findAny().orElse(null);
        if (slot == null) return;

        List<ItemStack> inChest = new ArrayList<>();
        for (int index : slot.getSlots()) {
            ItemStack stack = inventory.getItem(index);
            if (stack == null) continue;

            inChest.add(stack);
        }

        List<ItemStack> remainder = player.getInventory().addItem(inChest.toArray(new ItemStack[0])).values().stream().toList();
        for (ItemStack itemStack : remainder) {
            player.getWorld().dropItemNaturally(player.getLocation(), itemStack);
        }
    }

    @EventHandler
    private void onItemDragging(InventoryDragEvent event) {
        if (event.isCancelled()) return;
        // 获取打开的箱子界面并且当前箱子是否属于InventoryDetail实例
        Inventory inv = event.getInventory();
        InventoryHolder holder = inv.getHolder();
        HumanEntity human = event.getWhoClicked();
        // 如果是Minion的控制面板，取消事件并且返回
        if ((holder instanceof MinionPanel)) {
            event.setCancelled(true);
            return;
        }
        if ((holder instanceof MenuPanel) && event.getRawSlots().stream().anyMatch(x -> x <= 53)) {
            event.setCancelled(true);
            return;
        }
        // 检测是否打开了属于InventoryDetail实例创建的箱子
        if (!(holder instanceof CraftPanel panel)) return;
        // 检测点击箱子界面的实体是否为玩家
        if (!(human instanceof Player player)) return;
        // 检测玩家点击的箱子界面是否属于玩家的背包或快捷栏
        CraftPanelSlot slot = panel.getSlots().stream().filter(x -> x.getType().equals(PanelSlotType.OUTPUT)).findAny().orElse(null);
        if (slot == null) return;
        if (event.getRawSlots().stream().anyMatch(x -> Arrays.stream(slot.getSlots()).anyMatch(y -> y == x))) {
            event.setCancelled(true);
            return;
        }

        RecipeSearchRunnable finder = new RecipeSearchRunnable(player, inv);
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

    private void prepareCommandList(List<String> commands, Player player) {
        for (String s : commands) {
            int firstSpace = s.indexOf(" ");
            String key = firstSpace >= 1 ? s.substring(0, firstSpace) : s;
            String command = firstSpace >= 1 ? s.substring(firstSpace + 1) : s;
            command = command.replace("%player%", player.getName()).trim();
            if (key.contains("[console]") || !key.contains("[") || !key.contains("]")) {
                CommandManager.runConsoleCommand(command, player);
            }
            if (key.contains("[player]")) {
                CommandManager.runPlayerCommand(command, player);
            }
        }
    }
}

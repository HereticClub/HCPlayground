package org.hcmc.hcplayground.utility;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sk89q.worldguard.WorldGuard;
import me.clip.placeholderapi.PlaceholderAPI;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.*;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.util.Vector;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.enums.*;
import org.hcmc.hcplayground.manager.*;
import org.hcmc.hcplayground.model.config.AuthmeConfiguration;
import org.hcmc.hcplayground.model.config.CourseConfiguration;
import org.hcmc.hcplayground.model.config.PotionConfiguration;
import org.hcmc.hcplayground.model.enchantment.EnchantmentItem;
import org.hcmc.hcplayground.model.item.ItemBase;
import org.hcmc.hcplayground.model.menu.MenuPanelSlot;
import org.hcmc.hcplayground.model.player.PlayerData;
import org.hcmc.hcplayground.runnable.PluginRunnable;
import org.hcmc.hcplayground.serialization.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;

/**
 * @Desciption Store global static variants and methods
 */
public final class Global {
    private final static String[] childrenFolders;
    /**
     * 可配置的配置文档<br>
     * 从本地的插件文件夹加载<br>
     * 如果不存在则从资源区复制<br>
     * 但永远不会从资源文档更新<br>
     */
    private final static String[] ymlConfigurable;
    /**
     * 可迁移的配置文档<br>
     * 每次插件启动，先检测资源区和本地文件夹的差异
     * 将差异更新到本地的配置文档，再加载本地配置文档<br>
     */
    private final static String[] ymlMigratable;
    /**
     * 仅从资源加载配置文档
     * 永远不会将资源文档复制到本地
     */
    private final static String[] ymlResources;
    private final static JavaPlugin plugin;

    private final static Type mapCharInteger = new TypeToken<Map<Character, Integer>>() {
    }.getType();
    private final static Type mapCharItemBase = new TypeToken<Map<Character, ItemBase>>() {
    }.getType();
    private final static Type listMenuItem=new TypeToken<List<MenuPanelSlot>>(){}.getType();

    public final static char CHAR_00A7 = '\u00a7';

    private final static String CONFIG_AUTHME = "authme";
    private final static String CONFIG_POTION = "potion";
    private final static String CONFIG_BAN_ITEM = "banitem";
    private final static String CONFIG_PARKOUR = "parkouradmin";
    private final static String FIELD_NAME_COMMANDMAP = "commandMap";
    private final static String DATE_TIME_FORMAT = "yyyy/MM/dd HH:mm:ss";

    private final static String FOLDER_DEBUG = "debug";
    private final static String FOLDER_PROFILE = "profile";
    private final static String FOLDER_DATABASE = "database";
    private final static String FOLDER_DESIGNER = "designer";
    private final static String FOLDER_STORAGE = "storage";
    private final static String FILE_CONFIG = "config.yml";
    private final static String FILE_ITEMS = "items.yml";
    private final static String FILE_DROPS = "drops.yml";
    private final static String FILE_MESSAGES = "messages.yml";
    private final static String FILE_COMMANDS = "command.yml";
    private final static String FILE_MENU = "menu.yml";
    private final static String FILE_PERMISSION = "permission.yml";
    private final static String FILE_MOBS = "mobs.yml";
    private final static String FILE_BROADCAST = "broadcast.yml";
    private final static String FILE_CLEARLAG = "clearlag.yml";
    private final static String FILE_RECIPE = "recipe.yml";
    private final static String FILE_CCMD = "ccmd.yml";
    private final static String FILE_SIDEBAR = "scoreboard.yml";
    private final static String FILE_HOLOGRAM = "hologram.yml";
    private final static String FILE_MINION = "minion.yml";
    private final static String FILE_MMO_LEVEL = "level.yml";
    private final static String FILE_MMO_REWARD = "reward.yml";
    public final static String FILE_COURSE = "database/course.yml";
    public final static String FILE_RECORD_MINION = "database/minions.json";
    public final static String FILE_RECORD_BLOCK = "database/blocks.json";

    public static PluginRunnable runnable;
    public static Map<String, YamlConfiguration> yamlMap;

    public static Gson GsonObject;
    public static Scoreboard HealthScoreboard;
    public static CourseConfiguration course = null;
    public static AuthmeConfiguration authme = null;
    public static PotionConfiguration potion = null;
    public static Connection Sqlite = null;
    public static WorldGuard worldGuardApi = null;
    public static Economy economyApi = null;
    public static Chat chatApi = null;
    public static Permission permissionApi = null;

    static {
        plugin = JavaPlugin.getPlugin(HCPlayground.class);
        runnable = new PluginRunnable();
        yamlMap = new HashMap<>();
        HealthScoreboard = CreateScoreboard();
        childrenFolders = new String[]{
                FOLDER_DEBUG,
                FOLDER_DATABASE,
                FOLDER_DESIGNER,
                FOLDER_PROFILE,
                FOLDER_STORAGE,
        };
        ymlMigratable = new String[]{
                FILE_CLEARLAG,
                FILE_CONFIG,
                FILE_MESSAGES,
        };
        ymlConfigurable = new String[]{
                FILE_BROADCAST,
                FILE_CCMD,
                FILE_COURSE,
                FILE_DROPS,
                FILE_HOLOGRAM,
                FILE_ITEMS,
                FILE_MENU,
                FILE_MINION,
                FILE_MOBS,
                FILE_RECIPE,
                FILE_SIDEBAR,
                FILE_MMO_LEVEL,
                FILE_MMO_REWARD,
        };
        ymlResources = new String[]{
                FILE_COMMANDS,
                FILE_PERMISSION,
        };

        GsonObject = new GsonBuilder()
                .disableHtmlEscaping()
                .enableComplexMapKeySerialization()
                .excludeFieldsWithoutExposeAnnotation()
                .registerTypeAdapter(CcmdActionType.class, new CcmdActionTypeSerialization())
                .registerTypeAdapter(CompareType.class, new CompareTypeSerialization())
                .registerTypeAdapter(CrazyBlockType.class, new CrazyTypeSerialization())
                .registerTypeAdapter(Enchantment.class, new EnchantmentSerialization())
                .registerTypeAdapter(EnchantmentItem.class, new EnchantmentItemSerialization())
                .registerTypeAdapter(EntityType.class, new EntityTypeSerialization())
                .registerTypeAdapter(EquipmentSlot.class, new EquipmentSlotSerialization())
                .registerTypeAdapter(GameMode.class, new GameModeSerialization())
                .registerTypeAdapter(InventoryType.class, new InventoryTypeSerialization())
                .registerTypeAdapter(ItemBase.class, new ItemBaseSerialization())
                .registerTypeAdapter(ItemFeatureType.class, new ItemFeatureTypeSerialization())
                .registerTypeAdapter(ItemFlag.class, new ItemFlagsSerialization())
                .registerTypeAdapter(ItemStack.class, new ItemStackSerialization())
                .registerTypeAdapter(listMenuItem, new MenuPanelSlotSerialization())
                .registerTypeAdapter(Location.class, new LocationSerialization())
                .registerTypeAdapter(mapCharInteger, new MapCharIntegerSerialization())
                .registerTypeAdapter(mapCharItemBase, new MapCharItemBaseSerialization())
                .registerTypeAdapter(Material.class, new MaterialSerialization())
                .registerTypeAdapter(MaterialData.class, new MaterialDataSerialization())
                .registerTypeAdapter(MinionCategory.class, new MinionCategorySerialization())
                .registerTypeAdapter(MinionType.class, new MinionTypeSerialization())
                .registerTypeAdapter(MMOSkillType.class, new MMOLevelTypeSerialization())
                .registerTypeAdapter(NamespacedKey.class, new NamespacedKeySerialization())
                .registerTypeAdapter(OperatorType.class, new OperatorTypeSerialization())
                .registerTypeAdapter(PanelSlotType.class, new PanelSlotTypeSerialization())
                .registerTypeAdapter(PermissionDefault.class, new PermissionDefaultSerialization())
                .registerTypeAdapter(PotionEffect.class, new PotionEffectSerialization())
                .registerTypeAdapter(RecipeType.class, new RecipeTypeSerialization())
                .registerTypeAdapter(Sound.class, new SoundSerialization())
                .serializeNulls()
                .setDateFormat(DATE_TIME_FORMAT)
                .setPrettyPrinting()
                .create();
    }

    public static void ReloadConfiguration() throws IllegalAccessException, NoSuchFieldException, SQLException, NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        // 创建插件所需要的子目录
        InitialChildrenFolders();
        // 合并所有Yml格式文档到插件目录
        // 兼容前版本的配置，并且添加新版本的配置
        SaveYamlResource();
        // 加载插件的基本设置config.yml
        LoadPluginConfig();
        // 从yml格式文档加载配置到实例，必须按照指定的加载顺序
        // 1.加载本地化文档，所有加载项的依赖，必须最优先加载
        LanguageManager.Load(getYamlConfiguration(FILE_MESSAGES));
        // 2.加载权限列表
        PermissionManager.Load(getYamlConfiguration(FILE_PERMISSION));
        // 3.加载指令
        CommandManager.Load(getYamlConfiguration(FILE_COMMANDS));
        // 4.加载自定义物品
        // 无依赖，可优先加载
        ItemManager.Load(getYamlConfiguration(FILE_ITEMS));
        // 5.加载爪牙模板配置
        // 无依赖，可优先加载
        MinionManager.Load(getYamlConfiguration(FILE_MINION));
        // 6.加载跑酷赛道信息
        // 无依赖，可优先加载
        CourseManager.Load(getYamlConfiguration(FILE_COURSE));
        // 7.加载各种菜单(箱子)模板
        // 无依赖，可优先加载
        MenuManager.Load(getYamlConfiguration(FILE_MENU));
        // 8.加载自定义命令列表
        // 无依赖，可优先加载
        CcmdManager.Load(getYamlConfiguration(FILE_CCMD));
        // 9.加载计分板定义列表
        // 无依赖，可优先加载
        SidebarManager.Load(getYamlConfiguration(FILE_SIDEBAR));
        // 10.加载随机公告消息列表
        // 无依赖，可优先加载
        BroadcastManager.Load(getYamlConfiguration(FILE_BROADCAST));
        // 11.加载清除垃圾物品设置
        // 无依赖，可优先加载
        ClearLagManager.Load(getYamlConfiguration(FILE_CLEARLAG));
        // 12.加载破坏方块的自定义掉落列表，可掉落自定义物品
        // 依赖ItemManager
        DropManager.Load(getYamlConfiguration(FILE_DROPS));
        // 13.加载各种可生成的生物列表
        // 依赖ItemManager
        MobManager.Load(getYamlConfiguration(FILE_MOBS));
        // 14.加载配方列表
        // 依赖ItemManager
        RecipeManager.Load(getYamlConfiguration(FILE_RECIPE));
        // 15.加载奖励配置
        // 依赖ItemManager, RecipeManager
        RewardManager.Load(getYamlConfiguration(FILE_MMO_REWARD));
        // 16.加载等级设置列表
        // 依赖ItemManager, RecipeManager, RewardManager
        MMOSkillManager.Load(getYamlConfiguration(FILE_MMO_LEVEL));
        // 101.加载自定义可放置方块的摆放记录
        // 依赖ItemManager, MinionManager
        RecordManager.Load();
        // 201.加载漂浮字体定义列表
        HologramManager.Load(getYamlConfiguration(FILE_HOLOGRAM));
    }

    /**
     * 清理所有正在执行的对象，特别是所有继承于BukkitRunnable的对象<br>
     * 在执行/reload指令或者插件退出时都需要执行该方法
     */
    public static void Dispose() throws SQLException, IOException, IllegalAccessException, InvalidConfigurationException {
        // 保存所有在线玩家的数据
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            PlayerData pd = PlayerManager.getPlayerData(player);
            pd.SaveConfig();
        }
        LogMessage("Online player data saved");
        // 清除内存
        PlayerManager.purgePlayerData();
        LogMessage("Online player data purged");
        RecordManager.Save();
        LogMessage("Specific blocks saved");
        yamlMap.clear();
        // 关闭sqlite数据库
        if (!Sqlite.isClosed()) Sqlite.close();
        LogMessage("Sqlite database closed");
        // 停止全局runnable线程
        if (!runnable.isCancelled()) runnable.cancel();
        LogMessage("Global runnable thread stopped");
    }

    /**
     * 获取section节段内容，使用Gson反序列到T对象，并且设置placeholder，然后返回T对象
     */
    public static <T> T deserialize(@NotNull ConfigurationSection section, @NotNull Player player, @NotNull Class<T> tClass) {
        T item = null;

        try {
            String ClassName = tClass.getSimpleName();
            String value = GsonObject.toJson((section).getValues(false)).replace('&', '§');
            value = PlaceholderAPI.setPlaceholders(player, value);
            item = GsonObject.fromJson(value, tClass);

            Class<?> findClass = tClass;
            Field fieldId = null;
            while (findClass != null) {
                Field[] fields = findClass.getDeclaredFields();
                fieldId = Arrays.stream(fields).filter(x -> x.getName().equalsIgnoreCase("id")).findAny().orElse(null);
                if (fieldId != null) break;
                findClass = findClass.getSuperclass();
            }

            if (fieldId != null) {
                fieldId.setAccessible(true);
                fieldId.set(item, String.format("%s.%s", ClassName, section.getName()));
            }

        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return item;
    }

    @NotNull
    public static <T> List<T> deserializeList(@NotNull ConfigurationSection section, @NotNull Player player, @NotNull Class<T> tClass) {
        Set<String> keys = section.getKeys(false);
        String ClassName = tClass.getSimpleName();
        List<T> list = new ArrayList<>();

        try {
            for (String s : keys) {
                ConfigurationSection itemSection = section.getConfigurationSection(s);
                if (itemSection == null) continue;
                String value = GsonObject.toJson(itemSection.getValues(false)).replace('&', '§');
                value = PlaceholderAPI.setPlaceholders(player, value);
                //System.out.println(value);

                T item = GsonObject.fromJson(value, tClass);
                Class<?> findClass = tClass;
                Field fieldId = null;
                while (findClass != null) {
                    Field[] fields = findClass.getDeclaredFields();
                    fieldId = Arrays.stream(fields).filter(x -> x.getName().equalsIgnoreCase("id")).findAny().orElse(null);
                    if (fieldId != null) break;
                    findClass = findClass.getSuperclass();
                }

                if (fieldId != null) {
                    fieldId.setAccessible(true);
                    fieldId.set(item, String.format("%s.%s", ClassName, s));
                }
                list.add(item);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return list;
    }
    /**
     * 获取section内所有子节段，使用Gson反序列到每一个T对象，然后返回List&lt;T&gt;数组
     */
    @NotNull
    public static <T> List<T> deserializeList(ConfigurationSection section, Class<T> tClass) {
        Set<String> keys = section.getKeys(false);
        String ClassName = tClass.getSimpleName();
        List<T> list = new ArrayList<>();

        try {
            for (String s : keys) {
                ConfigurationSection itemSection = section.getConfigurationSection(s);
                if (itemSection == null) continue;
                String value = GsonObject.toJson(itemSection.getValues(false)).replace('&', '§');
                //System.out.println(value);

                T item = GsonObject.fromJson(value, tClass);
                Class<?> findClass = tClass;
                Field fieldId = null;
                while (findClass != null) {
                    Field[] fields = findClass.getDeclaredFields();
                    fieldId = Arrays.stream(fields).filter(x -> x.getName().equalsIgnoreCase("id")).findAny().orElse(null);
                    if (fieldId != null) break;
                    findClass = findClass.getSuperclass();
                }

                if (fieldId != null) {
                    fieldId.setAccessible(true);
                    fieldId.set(item, String.format("%s.%s", ClassName, s));
                }
                list.add(item);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * 获取yml文档内所有子节段，使用Gson反序列到每一个T对象，然后返回List&lt;T&gt;数组
     */
    @NotNull
    public static <T> List<T> deserializeList(YamlConfiguration yaml, Class<T> tClass) {
        Set<String> keys = yaml.getKeys(false);
        List<T> list = new ArrayList<>();

        try {
            for (String s : keys) {
                ConfigurationSection itemSection = yaml.getConfigurationSection(s);
                if (itemSection == null) continue;
                String value = GsonObject.toJson(itemSection.getValues(false)).replace('&', '§');
                //System.out.println(value);

                T item = GsonObject.fromJson(value, tClass);
                Field fieldId = Arrays.stream(tClass.getDeclaredFields()).filter(x -> x.getName().equalsIgnoreCase("id")).findAny().orElse(null);
                if (fieldId != null) {
                    fieldId.setAccessible(true);
                    fieldId.set(item, s);
                }
                list.add(item);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * 验证并且注册WorldGuard插件
     */
    public static void ValidWorldGuardPlugin() {
        Plugin p = plugin.getServer().getPluginManager().getPlugin("WorldGuard");
        if (p == null) {
            LogWarning("WorldGuard not found :(");
        } else {
            worldGuardApi = WorldGuard.getInstance();
            LogMessage(String.format("Found WorldGuard, version: %s", p.getDescription().getVersion()));
        }
    }

    /**
     * 验证并且注册Vault插件
     */
    public static void ValidVaultPlugin() {
        Plugin p = plugin.getServer().getPluginManager().getPlugin("Vault");
        if (p == null) {
            LogWarning("Vault not found :(");
        } else {
            SetVaultChat();
            SetVaultEconomy();
            SetVaultPermission();
            LogMessage(String.format("Found Vault, version: %s", p.getDescription().getVersion()));
        }
    }

    public static void ValidParkourPlugin() {
        Plugin pa = plugin.getServer().getPluginManager().getPlugin("Parkour");
        if (pa == null) {
            LogWarning("Parkour not found :(");
        } else {
            LogMessage(String.format("Found Parkour, version: %s", pa.getDescription().getVersion()));
        }
    }

    public static void InitialChildrenFolders() {

        File dataFolder = plugin.getDataFolder();

        try {
            if (!dataFolder.exists()) {
                Files.createDirectory(dataFolder.toPath());
            }

            for (String s : childrenFolders) {
                File f = new File(String.format("%s/%s", dataFolder, s));
                if (!f.exists()) {
                    Files.createDirectory(f.toPath());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get YamlConfiguration instance special by filename parameter
     *
     * @param filename The .yml format file without path
     * @return The instance of YamlConfiguration was loaded by filename parameter
     */
    public static YamlConfiguration getYamlConfiguration(String filename) {
        return yamlMap.get(filename);
    }

    /**
     * 从config.yml加载插件的基本设置
     */
    private static void LoadPluginConfig() throws IllegalAccessException {
        String value;
        ConfigurationSection section;
        YamlConfiguration config = getYamlConfiguration(FILE_CONFIG);

        section = config.getConfigurationSection(CONFIG_AUTHME);
        if (section != null) {
            value = GsonObject.toJson(section.getValues(false));
            authme = GsonObject.fromJson(value, AuthmeConfiguration.class);
        }
        section = config.getConfigurationSection(CONFIG_POTION);
        if (section != null) {
            value = GsonObject.toJson(section.getValues(false));
            potion = GsonObject.fromJson(value, PotionConfiguration.class);
        }
        section = config.getConfigurationSection(CONFIG_BAN_ITEM);
        BanItemManager.Load(section);
        section = config.getConfigurationSection(CONFIG_PARKOUR);
        if (section != null) {
            value = GsonObject.toJson(section.getValues(false));
            course = GsonObject.fromJson(value, CourseConfiguration.class);
        }
    }

    /**
     * 复制所有Yml资源文档到插件目录
     * 加载所有Yml资源文档
     * 创建全局Map<String, YamlConfiguration>对象
     */
    public static void SaveYamlResource() throws IOException {

        yamlMap.clear();
        // 只加载资源区的配置文档
        // 不会复制到本地插件文件夹
        for (String s : ymlResources) {
            yamlMap.put(s, loadResource(s));
        }
        // 对比资源区和插件文件夹的配置文档
        // 将差异复制到插件文件夹的配置文档
        for (String s : ymlMigratable) {
            String ext = s.substring(s.length() - 3);
            if (!ext.equalsIgnoreCase("yml")) {
                plugin.saveResource(s, false);
            } else {
                YamlConfiguration yaml = migrateConfiguration(s);
                yaml.save(String.format("%s/%s", plugin.getDataFolder(), s));
                yamlMap.put(s, yaml);
            }
        }
        // 加载插件文件夹的配置文档
        // 如果不存在则先从资源区复制
        for (String s : ymlConfigurable) {
            File f = new File(String.format("%s/%s", plugin.getDataFolder(), s));
            if (plugin.getResource(s) != null && !f.exists()) plugin.saveResource(s, false);

            yamlMap.put(s, YamlConfiguration.loadConfiguration(f));
        }
    }

    /**
     * 获取本插件名称
     */
    public static String PluginName() {
        return plugin.getName();
    }

    /**
     * 写记录信息到记录器
     *
     * @param message 要记录的信息
     */
    public static void LogMessage(String message) {
        plugin.getLogger().info(message);
    }

    public static void LogWarning(String message) {
        plugin.getLogger().log(Level.WARNING, message);
    }
    public static void LogDebug(Date date, String className, String methodMand, Level level, String... messages) {

        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
            String filename = String.format("%s/%s/debug-%s.log", plugin.getDataFolder(), FOLDER_DEBUG, df.format(date));
            Path path = Paths.get(filename);

            List<String> lines = new ArrayList<>();
            for (String line : messages) {
                lines.add(String.format("[%s, %s, %s, %s] %s", date, className, methodMand, level.getLocalizedName(), line));
            }
            if (!Files.exists(path)) Files.createFile(path);
            Files.write(path, lines, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getDateFormat(Date date, int format, Locale locale) {
        String dateFormat;

        DateFormat df = DateFormat.getDateInstance(format, locale);
        DateFormat tf = DateFormat.getTimeInstance(format, locale);
        dateFormat = String.format("%s %s", df.format(date), tf.format(date));

        return dateFormat;
    }

    public static Location LookAt(Location source, Location target) {
        Vector v = source.toVector().subtract(target.toVector());
        Location _s = source.clone();

        double x = v.getX();
        double y = v.getY();
        double z = v.getZ();

        double dXZ = Math.sqrt(x * x + z * z);
        double dY = Math.sqrt(dXZ * dXZ + y * y);

        double newYaw = Math.acos(x / dXZ) * 180 / Math.PI;
        double newPitch = Math.acos(y / dY) * 180 / Math.PI - 90;
        if (z < 0.0)
            newYaw = newYaw + Math.abs(180 - newYaw) * 2;
        newYaw = (newYaw - 90);

        _s.setYaw((float) newYaw);
        _s.setPitch((float) newPitch);

        return _s;
    }

    public static CommandMap getCommandMap() {
        CommandMap commandMap = null;

        try {
            // 获取Bukkit.Server.CommandMap字段
            Field fieldCommandMap = Bukkit.getServer().getClass().getDeclaredField(FIELD_NAME_COMMANDMAP);
            // 设置CommandMap字段为可访问
            fieldCommandMap.setAccessible(true);
            // 从CommandMap字段获取CommandMap实例
            commandMap = (CommandMap) fieldCommandMap.get(Bukkit.getServer());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return commandMap;
    }

    private static void SetVaultEconomy() {
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return;

        economyApi = rsp.getProvider();
    }

    private static void SetVaultChat() {
        RegisteredServiceProvider<Chat> rsp = plugin.getServer().getServicesManager().getRegistration(Chat.class);
        if (rsp == null) return;

        chatApi = rsp.getProvider();
    }

    private static void SetVaultPermission() {
        RegisteredServiceProvider<Permission> rsp = plugin.getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp == null) return;

        permissionApi = rsp.getProvider();
    }

    private static YamlConfiguration loadResource(String filename) {
        YamlConfiguration yaml;
        InputStream stream = plugin.getResource(filename);
        if (stream == null) {
            yaml = new YamlConfiguration();
        } else {
            InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
            yaml = YamlConfiguration.loadConfiguration(reader);
        }
        return yaml;
    }

    @NotNull
    private static YamlConfiguration migrateConfiguration(String filename) {
        YamlConfiguration yamlResource, yamlPlugin;
        // 从资源区加载配置文档
        yamlResource = loadResource(filename);
        // 从本地插件文件夹加载配置文档
        File f = new File(String.format("%s/%s", plugin.getDataFolder(), filename));
        yamlPlugin = YamlConfiguration.loadConfiguration(f);

        Set<String> keysResource = yamlResource.getKeys(true);
        Set<String> keysPlugin = yamlPlugin.getKeys(true);

        for (String key : keysResource) {
            List<String> comments = yamlResource.getComments(key);
            Object obj = yamlResource.get(key);

            String exist = keysPlugin.stream().filter(x -> x.equalsIgnoreCase(key)).findAny().orElse(null);
            if (exist == null) {
                yamlPlugin.set(key, obj);
                yamlPlugin.setComments(key, comments);
            }
        }
        return yamlPlugin;
    }

    private static Scoreboard CreateScoreboard() {
        ScoreboardManager sm = plugin.getServer().getScoreboardManager();
        if (sm == null) return null;

        Scoreboard scoreboard = sm.getNewScoreboard();
        Set<Objective> objectives = scoreboard.getObjectives();
        for (Objective o : objectives) {
            o.unregister();
        }
        return scoreboard;
    }
}

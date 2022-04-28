package org.hcmc.hcplayground.utility;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sk89q.worldguard.WorldGuard;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.deserializer.*;
import org.hcmc.hcplayground.enums.RecipeType;
import org.hcmc.hcplayground.manager.BanItemManager;
import org.hcmc.hcplayground.model.config.AuthmeConfiguration;
import org.hcmc.hcplayground.model.config.PotionConfiguration;
import org.hcmc.hcplayground.model.item.ItemBaseA;
import org.hcmc.hcplayground.model.player.PlayerData;
import org.hcmc.hcplayground.manager.PlayerManager;
import org.hcmc.hcplayground.scheduler.PluginRunnable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 * @Desciption Store global static variants and methods
 */
public final class Global {
    private final static String[] ymlFilenames;
    private final static JavaPlugin plugin;

    public final static String CONFIG_AUTHME = "authme";
    public final static String CONFIG_POTION = "potion";
    public final static String CONFIG_BAN_ITEM = "banitem";
    public final static String FIELD_NAME_COMMANDMAP = "commandMap";
    public final static Pattern patternNumber = Pattern.compile("-?\\d+(\\.\\d+)?");

    public static PluginRunnable runnable;
    public static Map<String, YamlConfiguration> yamlMap;
    public static Gson GsonObject;
    public static Scoreboard HealthScoreboard;
    public static AuthmeConfiguration authme = null;
    public static PotionConfiguration potion = null;
    public static Connection Sqlite = null;
    public static WorldGuard WorldGuardApi = null;
    public static Economy EconomyApi = null;
    public static Chat ChatApi = null;
    public static Permission PermissionApi = null;
    public static CommandMap CommandMap = null;


    static {
        plugin = JavaPlugin.getPlugin(HCPlayground.class);
        runnable = new PluginRunnable();
        yamlMap = new HashMap<>();
        HealthScoreboard = CreateScoreboard();
        ymlFilenames = new String[]{
                "config.yml",
                "items.yml",
                "drops.yml",
                "messages.yml",
                "levels.yml",
                "command.yml",
                "menu.yml",
                "permission.yml",
                "mobs.yml",
                "broadcast.yml",
                "clearlag.yml",
                "database/hcdb.db",
        };

        GsonObject = new GsonBuilder()
                .disableHtmlEscaping()
                .enableComplexMapKeySerialization()
                .excludeFieldsWithoutExposeAnnotation()
                .registerTypeAdapter(RecipeType.class, new BanItemTypeDeserializer())
                .registerTypeAdapter(Enchantment.class, new EnchantmentDeserializer())
                .registerTypeAdapter(EntityType.class, new EntityTypeDeserializer())
                .registerTypeAdapter(EquipmentSlot.class, new EquipmentSlotDeserializer())
                .registerTypeAdapter(InventoryType.class, new InventoryTypeDeserializer())
                .registerTypeAdapter(ItemBaseA.class, new ItemBaseDeserializer())
                .registerTypeAdapter(ItemFlag.class, new ItemFlagsDeserializer())
                .registerTypeAdapter(MaterialData.class, new MaterialDeserializer())
                .registerTypeAdapter(PotionEffect.class, new PotionEffectDeserializer())
                .registerTypeAdapter(PermissionDefault.class, new PermissionDefaultDeserializer())
                .serializeNulls()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .setPrettyPrinting()
                .create();

        try {
            CommandMap = CreateCommandMap();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 清理所有正在执行的对象，特别是所有继承于BukkitRunnable的对象<br>
     * 在执行/reload指令或者插件退出时都需要执行该方法
     */
    public static void Dispose() throws SQLException, IOException, IllegalAccessException {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            PlayerData pd = PlayerManager.getPlayerData(player);
            pd.SaveConfig();
        }
        runnable.cancel();
        PlayerManager.clearAllPlayerData();
        yamlMap.clear();
        if (!Sqlite.isClosed()) Sqlite.close();
    }

    /**
     * 从config.yml加载插件的基本设置
     */
    public static void LoadConfig() throws IllegalAccessException {
        String value;
        ConfigurationSection section;
        YamlConfiguration config = getYamlConfiguration("config.yml");

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
    }

    /**
     * 获取section内所有子节段，利用Gson反序列到每一个T对象，然后返回List&lt;T&gt;数组
     */
    public static <T> List<T> SetItemList(ConfigurationSection section, Class<T> tClass) throws IllegalAccessException {
        Set<String> keys = section.getKeys(false);
        String ClassName = tClass.getSimpleName();
        List<T> list = new ArrayList<>();

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

        return list;
    }

    /**
     * 获取yml文档内所有子节段，利用Gson反序列到每一个T对象，然后返回List&lt;T&gt;数组
     */
    public static <T> List<T> SetItemList(YamlConfiguration yaml, Class<T> tClass) throws IllegalAccessException {
        Set<String> keys = yaml.getKeys(false);
        List<T> list = new ArrayList<>();

        for (String s : keys) {
            ConfigurationSection itemSection = yaml.getConfigurationSection(s);
            if (itemSection == null) continue;
            String value = GsonObject.toJson(itemSection.getValues(false)).replace('&', '§');
            //System.out.println(value);

            T item = GsonObject.fromJson(value, tClass);
            Field fieldId = Arrays.stream(tClass.getFields()).filter(x -> x.getName().equalsIgnoreCase("id")).findAny().orElse(null);
            if (fieldId != null) fieldId.set(item, s);
            list.add(item);
        }

        return list;
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
     * 验证并且注册WorldGuard插件
     */
    public static void ValidWorldGuardPlugin() {
        Plugin p = plugin.getServer().getPluginManager().getPlugin("WorldGuard");
        if (p == null) {
            LogWarning("WorldGuard not found :(");
        } else {
            WorldGuardApi = WorldGuard.getInstance();
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

    /**
     * 复制所有Yml资源文档到插件目录
     * 加载所有Yml资源文档
     * 创建全局Map<String, YamlConfiguration>对象
     */
    public static void SaveYamlResource() throws IOException {

        yamlMap.clear();

        for (String s : ymlFilenames) {
            String ext = s.substring(s.length() - 3);

            if (!ext.equalsIgnoreCase("yml")) {
                plugin.saveResource(s, false);
            } else {
                YamlConfiguration yaml = MigrateConfiguration(s);
                if (yaml == null) continue;

                yaml.save(String.format("%s/%s", plugin.getDataFolder(), s));
                yamlMap.put(s, yaml);
            }
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

    public static String getDateFormat(Date date, int format, Locale locale) {
        String dateFormat;

        DateFormat df = DateFormat.getDateInstance(format, locale);
        DateFormat tf = DateFormat.getTimeInstance(format, locale);
        dateFormat = String.format("%s %s", df.format(date), tf.format(date));

        return dateFormat;
    }

    private static void SetVaultEconomy() {
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return;

        EconomyApi = rsp.getProvider();
    }

    private static void SetVaultChat() {
        RegisteredServiceProvider<Chat> rsp = plugin.getServer().getServicesManager().getRegistration(Chat.class);
        if (rsp == null) return;

        ChatApi = rsp.getProvider();
    }

    private static void SetVaultPermission() {
        RegisteredServiceProvider<Permission> rsp = plugin.getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp == null) return;

        PermissionApi = rsp.getProvider();
    }

    private static YamlConfiguration MigrateConfiguration(String filename) {
        YamlConfiguration yamlResource, yamlPlugin;

        InputStream stream = plugin.getResource(filename);
        if (stream == null) return null;
        InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
        yamlResource = YamlConfiguration.loadConfiguration(reader);

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

    private static CommandMap CreateCommandMap() throws NoSuchFieldException, IllegalAccessException {
        // 获取Bukkit.Server.CommandMap字段
        Field fieldCommandMap = plugin.getServer().getClass().getDeclaredField(FIELD_NAME_COMMANDMAP);
        // 设置CommandMap字段为可访问
        fieldCommandMap.setAccessible(true);
        // 从CommandMap字段获取CommandMap实例
        return (CommandMap) fieldCommandMap.get(plugin.getServer());
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

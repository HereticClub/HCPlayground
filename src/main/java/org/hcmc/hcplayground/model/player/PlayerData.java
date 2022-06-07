package org.hcmc.hcplayground.model.player;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.enums.PlayerBannedState;
import org.hcmc.hcplayground.manager.LocalizationManager;
import org.hcmc.hcplayground.sqlite.SqliteManager;
import org.hcmc.hcplayground.sqlite.table.BanPlayerDetail;
import org.hcmc.hcplayground.utility.Global;
import org.jetbrains.annotations.NotNull;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.*;

public class PlayerData {
    /*
    Key:
    如果是普通方块比如麦子等
    则保存保存该方块的Material
    如果含有PersistentData，则保存其Id
    Value:
    破快或放置该方块的总数量
    */
    private static final String Section_Key_BreakList = "breakList";
    private static final String Section_Key_PlaceList = "placeList";
    private static final String Section_Key_FishingList = "fishingList";
    private static final String Section_Key_DropList = "dropList";
    private static final String Section_Key_PickupList = "pickupList";
    private static final String Section_Key_KillMobList = "killMobList";
    private static final String Section_Key_CcmdCooldownList = "ccmdCooldownList";
    private static final String Section_Key_Parkour = "parkour";
    private static final String Section_Key_Parkour_Design = "parkour.design";
    private static final String Section_Key_Parkour_List = "parkour.list";
    private static final String GAMEPROFILE_PROPERTY_TEXTURES = "textures";



    private static final String TYPE_JAVA_UTIL_MAP = "java.util.Map";
    public static final double BASE_HEALTH = 20.0F;
    public static final double BASE_MAX_HEALTH = 20.0F;
    public static final double BASE_CRITICAL = 0.05F;
    public static final double BASE_CRITICAL_DAMAGE = 1.5F;
    public static final double BASE_ARMOR = 0;
    public static final double BASE_ATTACK_REACH = 0;
    public static final double BASE_BLOOD_SUCKING = 0;
    public static final double BASE_RECOVER = 0;
    public static final double BASE_INTELLIGENCE = 0;
    public static final double BASE_DIGGING_SPEED = 0;
    public static final double BASE_LOGGING_SPEED = 0;
    /**
     * 玩家档案文档的各种记录名称列表
     */
    private final String[] SectionKeys = new String[]{
            Section_Key_BreakList,
            Section_Key_PlaceList,
            Section_Key_DropList,
            Section_Key_PickupList,
            Section_Key_KillMobList,
            Section_Key_FishingList,
            Section_Key_CcmdCooldownList,
    };

    // 破坏方块记录
    @Expose
    @SerializedName(value = Section_Key_BreakList)
    public Map<Material, Integer> BreakList = new HashMap<>();
    // 摆放方块记录
    @Expose
    @SerializedName(value = Section_Key_PlaceList)
    public Map<Material, Integer> PlaceList = new HashMap<>();
    // 钓鱼记录
    @Expose
    @SerializedName(value = Section_Key_FishingList)
    public Map<Material, Integer> FishingList = new HashMap<>();
    // 扔掉物品记录
    @Expose
    @SerializedName(value = Section_Key_DropList)
    public Map<Material, Integer> DropList = new HashMap<>();
    // 拾取物品记录
    @Expose
    @SerializedName(value = Section_Key_PickupList)
    public Map<Material, Integer> PickupList = new HashMap<>();
    // 杀掉生物记录
    @Expose
    @SerializedName(value = Section_Key_KillMobList)
    public Map<EntityType, Integer> KillMobList = new HashMap<>();
    // Custom Command cooldown
    @Expose
    @SerializedName(value = Section_Key_CcmdCooldownList)
    public Map<String, Double> CcmdCooldownList = new HashMap<>();
    @Expose
    @SerializedName(value = "parkour.design")
    public boolean isCourseDesigning = false;
    @Expose
    @SerializedName(value = "parkour.list")
    public List<String> courseIds = new ArrayList<>();
    public PermissionAttachment attachment;
    /**
     * 玩家的背包和装备物品的记录
     */
    public CourseDesigner designer;

    /**
     * 玩家在runnable线程的时间检查点，初始化为登陆时间
     * 通常不会更改这个属性的值
     */
    public long loginTimeStamp = 0;
    /**
     * 记录玩家登陆时的游戏模式
     */
    private GameMode gameMode;
    /**
     * 玩家的Player实例
     */
    private final Player player;
    /**
     * 玩家的UUID
     */
    private final UUID uuid;
    /**
     * 玩家名称
     */
    private final String name;
    /**
     * 玩家是否已经使用/login指令登陆到服务器
     */
    private boolean isLogin;
    /**
     * 玩家是否已经使用/register指令注册到服务器
     */
    private boolean isRegister;
    /**
     * 玩家最近的登陆时间
     */
    private Date loginTime = new Date();
    /**
     * 本插件的实例
     */
    private final JavaPlugin plugin = HCPlayground.getPlugin();
    private GameProfile profile;

    private double totalArmor = BASE_ARMOR;
    private double totalAttackReach = BASE_ATTACK_REACH;
    private double totalBloodSucking = BASE_BLOOD_SUCKING;
    private double totalCritical = BASE_CRITICAL;
    private double totalCriticalDamage = BASE_CRITICAL_DAMAGE;
    private double maxHealth = BASE_MAX_HEALTH;
    private double totalRecover = BASE_RECOVER;
    private double totalIntelligence = BASE_INTELLIGENCE;
    private double totalDiggingSpeed = BASE_DIGGING_SPEED;
    private double totalLoggingSpeed = BASE_LOGGING_SPEED;

    public PlayerData(Player player) {
        this.player = player;

        name = player.getName();
        uuid = player.getUniqueId();
        designer = new CourseDesigner(this);
    }

    public double getCurrentHealth() {
        double currentHealth = (float) player.getHealth();
        if (currentHealth >= maxHealth) player.setHealth(maxHealth);
        return currentHealth;
    }

    // 获取玩家当前生命值
    public double getMaxHealth() {
        maxHealth = getGenericAttribute(Attribute.GENERIC_MAX_HEALTH, BASE_HEALTH);
        return maxHealth;
    }

    // 获取玩家当前护甲值
    public double getTotalArmor() {
        return totalArmor;
    }

    // 获取玩家当前移动速度
    public double getTotalMovementSpeed() {
        return getGenericAttribute(Attribute.GENERIC_MOVEMENT_SPEED, 0);
    }

    public double getTotalArmorToughness() {
        return getGenericAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS, 0);
    }

    public double getTotalKnockBackResistance() {
        return getGenericAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE, 0);
    }

    public double getTotalLuck() {
        return getGenericAttribute(Attribute.GENERIC_LUCK, 0);
    }

    public double getTotalAttackDamage() {
        return getGenericAttribute(Attribute.GENERIC_ATTACK_DAMAGE, 0);
    }

    public double getTotalAttackSpeed() {
        return getGenericAttribute(Attribute.GENERIC_ATTACK_SPEED, 0);
    }

    public double getTotalRecover() {
        return totalRecover;
    }

    public double getTotalBloodSucking() {
        return totalBloodSucking;
    }

    public double getTotalCritical() {
        return totalCritical;
    }

    public double getTotalCriticalDamage() {
        return totalCriticalDamage;
    }

    public double getTotalAttackReach() {
        return totalAttackReach;
    }

    public double getTotalIntelligence() {
        return totalIntelligence;
    }

    public double getTotalDiggingSpeed() {
        return totalDiggingSpeed;
    }

    public double getTotalLoggingSpeed() {
        return totalLoggingSpeed;
    }

    public GameProfile setHeadTextures(String base64Value) {
        if (profile == null) profile = new GameProfile(UUID.randomUUID(), null);
        PropertyMap pm = profile.getProperties();
        Property pp = new Property(GAMEPROFILE_PROPERTY_TEXTURES, base64Value);
        pm.put(GAMEPROFILE_PROPERTY_TEXTURES, pp);

        return profile;
    }

    public void setTotalLoggingSpeed(double value) {
        totalLoggingSpeed = value;
    }

    public void setTotalDiggingSpeed(double value) {
        totalDiggingSpeed = value;
    }

    public void setTotalIntelligence(double value) {
        totalIntelligence = value;
    }

    public void setTotalArmor(double value) {
        totalArmor = value;
    }

    public void setTotalCriticalDamage(double value) {
        totalCriticalDamage = value;
    }

    public void setTotalAttackReach(double value) {
        totalAttackReach = value;
    }

    public void setTotalCritical(double value) {
        totalCritical = value;
    }

    public void setTotalBloodSucking(double value) {
        totalBloodSucking = value;
    }

    public void setTotalRecover(double value) {
        totalRecover = value;
    }

    public boolean getLogin() {
        return isLogin;
    }

    public void setLogin(boolean value) {
        isLogin = value;
    }

    public Date getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(Date value) {
        loginTime = value;
    }

    public boolean getRegister() {
        return isRegister;
    }

    public void setRegister(boolean value) {
        isRegister = value;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    @NotNull
    public Player getPlayer() {
        return player;
    }

    public org.bukkit.GameMode getGameMode() {
        return gameMode;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public boolean Exist() throws SQLException {
        return SqliteManager.PlayerExist(player);
    }

    public boolean isBanned() throws SQLException {
        BanPlayerDetail detail = SqliteManager.isPlayerBanned(player);
        if (detail == null) return false;

        DateFormat df = DateFormat.getDateInstance(DateFormat.FULL, Locale.CHINA);
        DateFormat tf = DateFormat.getTimeInstance(DateFormat.FULL, Locale.CHINA);
        String masterName = detail.masterName;
        String reason = detail.message;
        Date banDate = detail.banDate;
        String banDateTime = String.format("%s %s", df.format(banDate), tf.format(banDate));
        String bannedMessage = LocalizationManager.getMessage("playerBannedMessage", player)
                .replace("%player%", name)
                .replace("%master%", masterName)
                .replace("%reason%", reason)
                .replace("%banDate%", banDateTime);
        player.kickPlayer(bannedMessage);

        return detail.isBanned;
    }

    public boolean Register(String password) throws SQLException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException {

        boolean register = SqliteManager.PlayerRegister(player, password);
        if (!register) {
            player.sendMessage(LocalizationManager.getMessage("playerRegisterExist", player)
                    .replace("%player%", name));
        } else {
            isLogin = true;
            //Global.LogMessage(String.format("\033[1;35mPlayer register gameMode: \033[1;33m%s\033[0m", gameMode));
            player.setGameMode(gameMode);
            plugin.getServer().broadcastMessage(LocalizationManager.getMessage("playerRegisterWelcome", player)
                    .replace("%player%", name));
        }

        return register;
    }

    public boolean Unregister(String password) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException, SQLException {

        boolean unregister = SqliteManager.PlayerUnregister(player, password);
        if (!unregister) {
            player.sendMessage(LocalizationManager.getMessage("playerURPasswordNotRight", player).replace("%player%", name));
        } else {
            player.kickPlayer(LocalizationManager.getMessage("playerUnregistered", player).replace("%player%", name));
        }

        return unregister;
    }

    public boolean Login(String password) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException, SQLException {

        if (isLogin) {
            player.sendMessage(LocalizationManager.getMessage("playerHasLogin", player).replace("%player%", name));
            return false;
        }
        isLogin = SqliteManager.PlayerLogin(player, password);
        if (!isLogin) {
            player.sendMessage(LocalizationManager.getMessage("playerLoginFailed", player).replace("%player%", name));
        } else {
            //Global.LogMessage(String.format("\033[1;35mPlayer Login gameMode: \033[1;33m%s\033[0m", gameMode));
            player.setGameMode(gameMode);
            player.sendMessage(LocalizationManager.getMessage("playerLoginWelcome", player).replace("&", "§").replace("%player%", name));
        }

        return isLogin;
    }

    public boolean ChangePassword(String oldPassword, String newPassword) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException, SQLException {

        boolean exist = SqliteManager.CheckPassword(player, oldPassword);
        if (!exist) {
            player.sendMessage(LocalizationManager.getMessage("playerOldPasswordNotRight", player).replace("%player%", name));
            return false;
        }

        boolean changed = SqliteManager.ChangePassword(player, newPassword);
        if (!changed) {
            player.sendMessage(LocalizationManager.getMessage("systemError", player).replace("%player%", name));
        } else {
            player.sendMessage(LocalizationManager.getMessage("playerPasswordChanged", player).replace("%player%", name));
        }

        return changed;
    }

    public void BanPlayer(String targetPlayer, String reason) throws SQLException {
        PlayerBannedState state = SqliteManager.BanPlayer(player, targetPlayer, reason);
        Player[] players = plugin.getServer().getOnlinePlayers().toArray(new Player[0]);
        Player target = Arrays.stream(players).filter(x -> x.getName().equalsIgnoreCase(targetPlayer)).findAny().orElse(null);

        java.util.Date banDate = new Date();
        DateFormat df = DateFormat.getDateInstance(DateFormat.FULL, Locale.CHINA);
        DateFormat tf = DateFormat.getTimeInstance(DateFormat.FULL, Locale.CHINA);
        String banDateTime = String.format("%s %s", df.format(banDate), tf.format(banDate));

        switch (state) {
            case Player_Not_Exist ->
                    player.sendMessage(LocalizationManager.getMessage("playerNotExist", player).replace("%player%", targetPlayer));
            case Player_Banned -> {
                if (target != null) {
                    target.kickPlayer(LocalizationManager.getMessage("playerBannedMessage", player)
                            .replace("%player%", targetPlayer)
                            .replace("%master%", name)
                            .replace("%reason%", reason)
                            .replace("%banDate%", banDateTime));
                }
                player.sendMessage(LocalizationManager.getMessage("playerBanned", player).replace("%player%", targetPlayer));
            }
            case Player_Unbanned ->
                    player.sendMessage(LocalizationManager.getMessage("playerUnBanned", player).replace("%player%", targetPlayer));
        }
    }

    public void LoadConfig() throws IllegalAccessException {
        UUID playerUuid = player.getUniqueId();
        File f = new File(plugin.getDataFolder(), String.format("profile/%s.yml", playerUuid));
        // 加载所有Map<Material, Integer>类型的玩家记录
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(f);
        Yaml2Map(yaml);
    }

    public void SaveConfig() throws IOException, IllegalAccessException, InvalidConfigurationException {
        UUID playerUuid = player.getUniqueId();
        File f = new File(plugin.getDataFolder(), String.format("profile/%s.yml", playerUuid));
        // 转换所有Map<Material, Integer>类型的玩家记录，成为Yaml格式字符串
        YamlConfiguration yaml = Map2Yaml();
        yaml.save(f);
    }

    /**
     * 将玩家档案内所有记录数据加载到类型为Map&lt;?, ?&gt;的属性
     */
    private void Yaml2Map(YamlConfiguration yaml) throws IllegalAccessException {
        // 循环检测SectionKeys字段内的字符串列表
        for (String s : SectionKeys) {
            // 根据字符串获取当前对象的属性的实例
            Field[] fields = this.getClass().getFields();
            Field field = Arrays.stream(fields).filter(x -> x.getName().equalsIgnoreCase(s)).findAny().orElse(null);
            if (field == null) continue;
            // 过滤类型不是Map<?, ?>的属性
            ParameterizedType pType = (ParameterizedType) field.getGenericType();
            String name = pType.getRawType().getTypeName();
            if (!name.equalsIgnoreCase(TYPE_JAVA_UTIL_MAP)) continue;
            // 根据字符串获取yaml格式文档内相应的节段及内容
            ConfigurationSection section = yaml.getConfigurationSection(s);
            if (section == null) continue;
            // 使用Gson将Yaml内容转换到类型为Map<?, ?>的属性
            String data = Global.GsonObject.toJson(section.getValues(false));
            Type mapType = new TypeToken<Map<?, ?>>() {
            }.getType();
            Map<?, ?> value = Global.GsonObject.fromJson(data, mapType);
            field.set(this, value);
        }
        // 加载玩家的其他数据记录或者状态
        isCourseDesigning = yaml.getBoolean(Section_Key_Parkour_Design);
        courseIds = yaml.getStringList(Section_Key_Parkour_List);
    }

    /**
     * 将所有类型为Map&lt;?, ?&gt;的属性保存到yaml实例
     *
     * @return YamlConfiguration实例
     */
    private YamlConfiguration Map2Yaml() throws InvalidConfigurationException {
        YamlConfiguration yaml = new YamlConfiguration();
        String v = Global.GsonObject.toJson(this);
        yaml.loadFromString(v);
        return yaml;
    }

    private double getGenericAttribute(Attribute attribute, double defaultValue) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) return defaultValue;

        return instance.getValue();
    }
}

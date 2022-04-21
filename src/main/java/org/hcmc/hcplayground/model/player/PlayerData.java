package org.hcmc.hcplayground.model.player;

import com.google.gson.reflect.TypeToken;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.enums.PlayerBannedState;
import org.hcmc.hcplayground.manager.LocalizationManager;
import org.hcmc.hcplayground.sqlite.SqliteManager;
import org.hcmc.hcplayground.sqlite.table.BanPlayerDetail;
import org.hcmc.hcplayground.utility.Global;

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

    private static final String TYPE_JAVA_UTIL_MAP = "java.util.Map";
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
    };
    // 破坏方块记录
    public Map<Material, Integer> BreakList = new HashMap<>();
    // 摆放方块记录
    public Map<Material, Integer> PlaceList = new HashMap<>();
    // 钓鱼记录
    public Map<Material, Integer> FishingList = new HashMap<>();
    // 扔掉物品记录
    public Map<Material, Integer> DropList = new HashMap<>();
    // 拾取物品记录
    public Map<Material, Integer> PickupList = new HashMap<>();
    // 杀掉生物记录
    public Map<EntityType, Integer> KillMobList = new HashMap<>();
    /**
     * 玩家在runnable线程的时间检查点，初始化为登陆时间
     * 通常不会更改这个属性的值
     */
    public long CheckpointTime = 0;
    /**
     * 记录玩家登陆时的游戏模式
     */
    public GameMode GameMode;
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
    private Date loginDTTM = new Date();
    /**
     * 本插件的实例
     */
    private final JavaPlugin plugin = HCPlayground.getPlugin();

    public PlayerData(Player player) {
        this.player = player;

        name = player.getName();
        uuid = player.getUniqueId();
    }

    public boolean getLogin() {
        return isLogin;
    }

    public void setLogin(boolean value) {
        isLogin = value;
    }

    public Date getLoginDTTM() {
        return loginDTTM;
    }

    public void setLoginDTTM(Date value) {
        loginDTTM = value;
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
        String bannedMessage = LocalizationManager.Messages.get("playerBannedMessage")
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
            player.sendMessage(LocalizationManager.Messages.get("playerRegisterExist").replace("%player%", name));
        } else {
            isLogin = true;
            player.setGameMode(GameMode);
            plugin.getServer().broadcastMessage(LocalizationManager.Messages.get("playerRegisterWelcome").replace("%player%", name));
        }

        return register;
    }

    public boolean Unregister(String password) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException, SQLException {

        boolean unregister = SqliteManager.PlayerUnregister(player, password);
        if (!unregister) {
            player.sendMessage(LocalizationManager.Messages.get("playerURPasswordNotRight").replace("%player%", name));
        } else {
            player.kickPlayer(LocalizationManager.Messages.get("playerUnregistered").replace("%player%", name));
        }

        return unregister;
    }

    public boolean Login(String password) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException, SQLException {

        if (isLogin) {
            player.sendMessage(LocalizationManager.Messages.get("playerHasLogin").replace("%player%", name));
            return false;
        }
        isLogin = SqliteManager.PlayerLogin(player, password);
        if (!isLogin) {
            player.sendMessage(LocalizationManager.Messages.get("playerLoginFailed").replace("%player%", name));
        } else {
            player.setGameMode(GameMode);
            player.sendMessage(LocalizationManager.Messages.get("playerLoginWelcome").replace("&", "§").replace("%player%", name));
        }

        return isLogin;
    }

    public boolean ChangePassword(String oldPassword, String newPassword) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException, SQLException {

        boolean exist = SqliteManager.CheckPassword(player, oldPassword);
        if (!exist) {
            player.sendMessage(LocalizationManager.Messages.get("playerOldPasswordNotRight").replace("%player%", name));
            return false;
        }

        boolean changed = SqliteManager.ChangePassword(player, newPassword);
        if (!changed) {
            player.sendMessage(LocalizationManager.Messages.get("systemError").replace("%player%", name));
        } else {
            player.sendMessage(LocalizationManager.Messages.get("playerPasswordChanged").replace("%player%", name));
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
            case Player_Not_Exist -> player.sendMessage(LocalizationManager.Messages.get("playerNotExist").replace("%player%", targetPlayer));
            case Player_Banned -> {
                if (target != null) {
                    target.kickPlayer(LocalizationManager.Messages.get("playerBannedMessage")
                            .replace("%player%", targetPlayer)
                            .replace("%master%", name)
                            .replace("%reason%", reason)
                            .replace("%banDate%", banDateTime));
                }
                player.sendMessage(LocalizationManager.Messages.get("playerBanned").replace("%player%", targetPlayer));
            }
            case Player_Unbanned -> player.sendMessage(LocalizationManager.Messages.get("playerUnBanned").replace("%player%", targetPlayer));
        }
    }

    public void LoadConfig() throws IllegalAccessException {
        UUID playerUuid = player.getUniqueId();
        File f = new File(plugin.getDataFolder(), String.format("profile/%s.yml", playerUuid));

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(f);
        Yaml2Map(yaml);
    }

    public void SaveConfig() throws IOException, IllegalAccessException {
        UUID playerUuid = player.getUniqueId();
        File f = new File(plugin.getDataFolder(), String.format("profile/%s.yml", playerUuid));

        Map2Yaml().save(f);
    }

    /**
     * 将玩家档案内所有记录数据加载到类型为Map&lt;?, ?&gt;的属性
     *
     * @param yaml
     * @throws IllegalAccessException
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
    }

    /**
     * 将所有类型为Map&lt;?, ?&gt;的属性保存到yaml实例
     *
     * @return YamlConfiguration实例
     * @throws IllegalAccessException
     */
    private YamlConfiguration Map2Yaml() throws IllegalAccessException {
        YamlConfiguration yaml = new YamlConfiguration();

        for (String s : SectionKeys) {
            Field[] fields = this.getClass().getFields();
            Field field = Arrays.stream(fields).filter(x -> x.getName().equalsIgnoreCase(s)).findAny().orElse(null);
            if (field == null) continue;

            ParameterizedType pType = (ParameterizedType) field.getGenericType();
            String name = pType.getRawType().getTypeName();
            if (!name.equalsIgnoreCase(TYPE_JAVA_UTIL_MAP)) continue;

            Map<?, ?> value = (Map<?, ?>) field.get(this);
            yaml.createSection(s, value);
        }

        return yaml;
    }
}

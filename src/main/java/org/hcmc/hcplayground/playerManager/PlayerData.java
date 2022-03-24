package org.hcmc.hcplayground.playerManager;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.flywaydb.core.internal.util.TimeFormat;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.localization.Localization;
import org.hcmc.hcplayground.model.AesAlgorithm;
import org.hcmc.hcplayground.model.Global;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;

public class PlayerData {
    /*
    BreakList - 玩家破坏方块的数据
    PlaceList - 玩家放置方块的数据
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
    // 破坏方块记录
    @SerializedName(value = Section_Key_BreakList)
    @Expose
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
    /**
     * 玩家在进入服务器后登陆前或注册前的信息提醒的时间检查点
     */
    @Expose(serialize = false, deserialize = false)
    public long remindCheckpoint = 0;

    @Expose(serialize = false, deserialize = false)
    private final Player player;
    @Expose(serialize = false, deserialize = false)
    private final JavaPlugin plugin = HCPlayground.getPlugin();

    @Expose(serialize = false, deserialize = false)
    private final UUID uuid;
    @Expose(serialize = false, deserialize = false)
    private final String name;
    @Expose(serialize = false, deserialize = false)
    private boolean isLogin;
    @Expose(serialize = false, deserialize = false)
    private boolean isRegister;
    @Expose(serialize = false, deserialize = false)
    private Date loginDTTM = new Date();

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

    public boolean isDBExist() throws SQLException {
        String commandText = String.format("select * from player where uuid = '%s'", uuid);
        Statement statement = Global.Sqlite.createStatement();
        ResultSet resultSet = statement.executeQuery(commandText);
        boolean exist = resultSet.next();

        statement.close();
        resultSet.close();

        return exist;
    }

    public boolean isDBBanned() throws SQLException {
        String commandText = String.format("select * from banDetails where playerId = '%s'", uuid);
        Statement statement = Global.Sqlite.createStatement();
        ResultSet resultSet = statement.executeQuery(commandText);

        boolean exist = resultSet.next();
        DateFormat df = DateFormat.getDateInstance(DateFormat.FULL, Locale.CHINA);
        DateFormat tf = DateFormat.getTimeInstance(DateFormat.FULL, Locale.CHINA);

        if (exist) {
            String masterName = resultSet.getString("masterName");
            String reason = resultSet.getString("message");
            Date banDate = resultSet.getDate("banDTTM");
            String banDateTime = String.format("%s %s", df.format(banDate), tf.format(banDate));

            String bannedMessage = Localization.Messages.get("playerBannedMessage").replace("%player%", name).replace("%master%", masterName).replace("%reason%", reason).replace("%banDate%", banDateTime);
            player.kickPlayer(bannedMessage);
        }

        resultSet.close();
        statement.close();

        return exist;
    }

    public boolean DBCreate(String password) throws SQLException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException {
        String key = uuid.toString().replace("-", "");
        String aesPassword = AesAlgorithm.Encrypt(key, password);

        String commandText = String.format("insert or ignore into player (uuid,name,password) values ('%s','%s','%s')", uuid, name, aesPassword);
        Statement statement = Global.Sqlite.createStatement();
        int count = statement.executeUpdate(commandText);
        statement.close();

        if (count == 0) {
            player.sendMessage(Localization.Messages.get("playerRegisterExist").replace("%player%", name));
        } else {
            isLogin = true;
            plugin.getServer().broadcastMessage(Localization.Messages.get("playerRegisterWelcome").replace("%player%", name));
            player.sendMessage(Localization.Messages.get("playerLoginMotd").replace("&", "§").replace("%player%", name));
        }

        return count != 0;
    }

    public boolean DBRemove(String password) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException, SQLException {
        String key = uuid.toString().replace("-", "");
        String aesPassword = AesAlgorithm.Encrypt(key, password);
        String commandText = String.format("delete from player where uuid = '%s' and password = '%s'", uuid, aesPassword);
        Statement statement = Global.Sqlite.createStatement();
        int count = statement.executeUpdate(commandText);
        statement.close();

        if (count == 0) {
            player.sendMessage(Localization.Messages.get("playerURPasswordNotRight").replace("%player%", name));
            return false;
        } else {
            player.kickPlayer(Localization.Messages.get("playerUnregistered").replace("%player%", name));
            return true;
        }
    }

    public boolean DBLogin(String password) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException, SQLException {
        if (isLogin) {
            player.sendMessage(Localization.Messages.get("playerHasLogin").replace("%player%", name));
            return false;
        }

        String key = uuid.toString().replace("-", "");
        String aesPassword = AesAlgorithm.Encrypt(key, password);
        String commandText = String.format("select * from player where name = '%s' and password = '%s'", name, aesPassword);
        Statement statement = Global.Sqlite.createStatement();
        ResultSet resultSet = statement.executeQuery(commandText);
        isLogin = resultSet.next();
        if (!isLogin) {
            player.sendMessage(Localization.Messages.get("playerLoginFailed").replace("%player%", name));
            return false;
        }

        player.sendMessage(Localization.Messages.get("playerLoginMotd").replace("&", "§").replace("%player%", name));
        resultSet.close();
        statement.close();

        return isLogin;
    }

    public boolean DBChangePassword(String oldPassword, String newPassword) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException, SQLException {
        String key = uuid.toString().replace("-", "");
        String aesOldPassword = AesAlgorithm.Encrypt(key, oldPassword);
        String aesNewPassword = AesAlgorithm.Encrypt(key, newPassword);

        String commandText = String.format("select * from player where name = '%s' and password = '%s'", name, aesOldPassword);
        Statement statement = Global.Sqlite.createStatement();
        ResultSet resultSet = statement.executeQuery(commandText);
        boolean exist = resultSet.next();
        if (!exist) {
            player.sendMessage(Localization.Messages.get("playerOldPasswordNotRight").replace("%player%", name));
            return false;
        }

        commandText = String.format("update player set password = '%s' where uuid = '%s'", aesNewPassword, uuid);
        statement = Global.Sqlite.createStatement();
        int count = statement.executeUpdate(commandText);
        if (count == 0) {
            player.sendMessage(Localization.Messages.get("playerChangePasswordError").replace("%player%", name));
        } else {
            player.sendMessage(Localization.Messages.get("playerPasswordChanged").replace("%player%", name));
        }

        resultSet.close();
        statement.close();
        return count != 0;
    }

    public void DBBanPlayer(String targetPlayer, String reason) throws SQLException {
        boolean isBan = !reason.equalsIgnoreCase("u");
        Date banDate = new Date();
        DateFormat df = DateFormat.getDateInstance(DateFormat.FULL, Locale.CHINA);
        DateFormat tf = DateFormat.getTimeInstance(DateFormat.FULL, Locale.CHINA);
        Statement statement = Global.Sqlite.createStatement();
        String banDateTime = String.format("%s %s", df.format(banDate), tf.format(banDate));

        String commandText;
        UUID targetUuid;
        Player target;

        OfflinePlayer[] offlinePlayers = plugin.getServer().getOfflinePlayers();
        OfflinePlayer o = Arrays.stream(offlinePlayers).filter(x -> Objects.requireNonNull(x.getName()).equalsIgnoreCase(targetPlayer)).findAny().orElse(null);
        if (o == null) {
            player.sendMessage(Localization.Messages.get("playerNotExist").replace("%player%", targetPlayer));
            return;
        }
        target = o.getPlayer();
        targetUuid = o.getUniqueId();

        if (isBan) {
            if (target != null && target.isOnline()) {
                target.kickPlayer(Localization.Messages.get("playerBannedMessage").replace("%player%", targetPlayer).replace("%master%", name).replace("%reason%", reason).replace("%banDate%", banDateTime));
            }
            commandText = String.format("insert into banRecord (id,masterId,playerId,message) values ('%s','%s','%s','%s')", UUID.randomUUID(), uuid, targetUuid, reason);
            statement.executeUpdate(commandText);
            player.sendMessage(Localization.Messages.get("playerBanned").replace("%player%", targetPlayer));
        } else {
            player.sendMessage(Localization.Messages.get("playerUnBanned").replace("%player%", targetPlayer));
        }

        commandText = String.format("update player set isBanned = '%s' where uuid = '%s'", isBan, targetUuid);
        statement.executeUpdate(commandText);

        statement.close();
    }

    public void LoadConfig() {
        UUID playerUuid = player.getUniqueId();
        ConfigurationSection section;
        String sectionValue;
        Type mapType = new TypeToken<Map<Material, Integer>>() {
        }.getType();
        File f = new File(plugin.getDataFolder(), String.format("profile/%s.yml", playerUuid));

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(f);
        section = yaml.getConfigurationSection(Section_Key_BreakList);
        if (section != null) {
            sectionValue = Global.GsonObject.toJson(section.getValues(false));
            BreakList = Global.GsonObject.fromJson(sectionValue, mapType);
        }
        section = yaml.getConfigurationSection(Section_Key_PlaceList);
        if (section != null) {
            sectionValue = Global.GsonObject.toJson(section.getValues(false));
            PlaceList = Global.GsonObject.fromJson(sectionValue, mapType);
        }
        section = yaml.getConfigurationSection(Section_Key_FishingList);
        if (section != null) {
            sectionValue = Global.GsonObject.toJson((section.getValues(false)));
            FishingList = Global.GsonObject.fromJson(sectionValue, mapType);
        }
        section = yaml.getConfigurationSection(Section_Key_DropList);
        if (section != null) {
            sectionValue = Global.GsonObject.toJson((section.getValues(false)));
            DropList = Global.GsonObject.fromJson(sectionValue, mapType);
        }
    }

    public void SaveConfig() throws IOException {
        UUID playerUuid = player.getUniqueId();
        File f = new File(plugin.getDataFolder(), String.format("profile/%s.yml", playerUuid));

        toYaml().save(f);
    }

    private YamlConfiguration toYaml() {
        YamlConfiguration yaml = new YamlConfiguration();

        yaml.createSection(Section_Key_BreakList, BreakList);
        yaml.createSection(Section_Key_PlaceList, PlaceList);
        yaml.createSection(Section_Key_FishingList, FishingList);
        yaml.createSection(Section_Key_DropList, DropList);

        return yaml;
    }
}

package org.hcmc.hcplayground.playerManager;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.enums.PlayerBannedState;
import org.hcmc.hcplayground.manager.LocalizationManager;
import org.hcmc.hcplayground.model.BanPlayerDetail;
import org.hcmc.hcplayground.model.Global;
import org.hcmc.hcplayground.sqlite.SqliteManager;

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
import java.sql.SQLException;
import java.text.DateFormat;
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
     * 玩家在runnable线程的时间检查点，初始化为登陆时间
     * 通常不会更改这个属性的值
     */
    @Expose(serialize = false, deserialize = false)
    public long CheckpointTime = 0;

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
            plugin.getServer().broadcastMessage(LocalizationManager.Messages.get("playerRegisterWelcome").replace("%player%", name));
            //player.sendMessage(LocalizationManager.Messages.get("playerLoginMotd").replace("&", "§").replace("%player%", name));
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
            player.sendMessage(LocalizationManager.Messages.get("playerRegisterWelcome").replace("&", "§").replace("%player%", name));
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
            player.sendMessage(LocalizationManager.Messages.get("playerChangePasswordError").replace("%player%", name));
        } else {
            player.sendMessage(LocalizationManager.Messages.get("playerPasswordChanged").replace("%player%", name));
        }

        return changed;
    }

    public void BanPlayer(String targetPlayer, String reason) throws SQLException {
        PlayerBannedState state = SqliteManager.BanPlayer(player, targetPlayer, reason);
        Player[] players = plugin.getServer().getOnlinePlayers().toArray(new Player[0]);
        Player target = Arrays.stream(players).filter(x->x.getName().equalsIgnoreCase(targetPlayer)).findAny().orElse(null);

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

package org.hcmc.hcplayground.sqlite;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.annotation.SqliteColumn;
import org.hcmc.hcplayground.annotation.SqliteColumnIgnore;
import org.hcmc.hcplayground.enums.PlayerBannedState;
import org.hcmc.hcplayground.model.AesAlgorithm;
import org.hcmc.hcplayground.model.BanPlayerDetail;
import org.hcmc.hcplayground.model.Global;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.lang.reflect.Field;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;
import java.util.*;

public class SqliteManager {

    private static final JavaPlugin plugin = HCPlayground.getPlugin();
    private static Connection connection;

    public SqliteManager() {

    }

    public static Connection CreateSqliteConnection() throws SQLException {
        String url = String.format("jdbc:sqlite:%s/database/hcdb.db", plugin.getDataFolder());
        connection = DriverManager.getConnection(url);
        return connection;
    }

    public static BanPlayerDetail isPlayerBanned(Player player) throws SQLException {
        BanPlayerDetail detail;

        String commandText = String.format("select * from banDetails where playerId = '%s'", player.getUniqueId());
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(commandText);

        boolean exist = resultSet.next();
        if (!exist) return null;
        detail = ResultSetSerializer(resultSet, BanPlayerDetail.class);
        /*
        detail.isBanned = resultSet.getBoolean("isBanned");
        detail.banDate = resultSet.getDate("banDTTM");
        detail.playerName = resultSet.getString("playerName");
        detail.masterName = resultSet.getString("masterName");
        detail.playerUuid = UUID.fromString(resultSet.getString("playerId"));
        detail.masterUuid = UUID.fromString(resultSet.getString("masterId"));
        detail.message = resultSet.getString("message");

         */

        resultSet.close();
        statement.close();

        return detail;
    }

    public static boolean PlayerRegister(Player player, String password) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException, SQLException {
        UUID uuid = player.getUniqueId();
        String hyphen = "-";
        String empty = "";

        String key = uuid.toString().replace(hyphen, empty);
        String aesPassword = AesAlgorithm.Encrypt(key, password);

        String commandText = String.format("insert or ignore into player (uuid,name,password) values ('%s','%s','%s')", uuid, player.getName(), aesPassword);
        Statement statement = connection.createStatement();
        int count = statement.executeUpdate(commandText);
        statement.close();

        return count != 0;
    }

    public static boolean PlayerUnregister(Player player, String password) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException, SQLException {
        UUID uuid = player.getUniqueId();

        String key = uuid.toString().replace("-", "");
        String aesPassword = AesAlgorithm.Encrypt(key, password);
        String commandText = String.format("delete from player where uuid = '%s' and password = '%s'", uuid, aesPassword);
        Statement statement = connection.createStatement();
        int count = statement.executeUpdate(commandText);
        statement.close();

        return count != 0;
    }

    public static boolean PlayerExist(Player player) throws SQLException {
        UUID uuid = player.getUniqueId();
        String commandText = String.format("select * from player where uuid = '%s'", uuid);
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(commandText);

        boolean exist = resultSet.next();

        statement.close();
        resultSet.close();

        return exist;
    }

    public static boolean CheckPassword(Player player, String password) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException, SQLException {
        UUID uuid = player.getUniqueId();

        String key = uuid.toString().replace("-", "");
        String aesPassword = AesAlgorithm.Encrypt(key, password);
        String commandText = String.format("select * from player where uuid = '%s' and password = '%s'", uuid, aesPassword);
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(commandText);

        boolean checked = resultSet.next();

        resultSet.close();
        statement.close();

        return checked;
    }

    public static boolean ChangePassword(Player player, String newPassword ) throws SQLException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException {
        UUID uuid=player.getUniqueId();
        String key = uuid.toString().replace("-", "");
        String aesNewPassword = AesAlgorithm.Encrypt(key, newPassword);
        Statement statement = connection.createStatement();
        String commandText = String.format("update player set password = '%s' where uuid = '%s'", aesNewPassword, uuid);

        int count = statement.executeUpdate(commandText);

        statement.close();
        return count != 0;
    }

    public static boolean PlayerLogin(Player player, String password) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException, SQLException {
        UUID uuid = player.getUniqueId();
        String name = player.getName();

        String key = uuid.toString().replace("-", "");
        String aesPassword = AesAlgorithm.Encrypt(key, password);
        String commandText = String.format("select * from player where name = '%s' and password = '%s'", name, aesPassword);
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(commandText);

        boolean login = resultSet.next();

        resultSet.close();
        statement.close();

        return login;
    }

    public static PlayerBannedState BanPlayer(Player master, String targetName, String reason) throws SQLException {
        boolean banAction = !reason.equalsIgnoreCase("u");
        PlayerBannedState state;

        Statement statement = connection.createStatement();
        String commandText;

        OfflinePlayer[] offlinePlayers = plugin.getServer().getOfflinePlayers();
        OfflinePlayer offlinePlayer = Arrays.stream(offlinePlayers).filter(x -> Objects.requireNonNull(x.getName()).equalsIgnoreCase(targetName)).findAny().orElse(null);
        if (offlinePlayer == null) return PlayerBannedState.Player_Not_Exist;
        UUID targetUuid = offlinePlayer.getUniqueId();
        UUID masterUuid = master.getUniqueId();

        if (banAction) {
            commandText = String.format("insert into banRecord (id,masterId,playerId,message) values ('%s','%s','%s','%s')", UUID.randomUUID(), masterUuid, targetUuid, reason);
            statement.executeUpdate(commandText);
            state = PlayerBannedState.Player_Banned;
        } else {
            state = PlayerBannedState.Player_Unbanned;
        }

        commandText = String.format("update player set isBanned = '%s' where uuid = '%s'", banAction, targetUuid);
        statement.executeUpdate(commandText);

        statement.close();
        return state;
    }

    private static <T> T ResultSetSerializer(ResultSet resultSet, Class<T> tClass) throws SQLException {
        T result;
        String columnName;
        Map<String, Object> data =new HashMap<>();

        Field[] fields = tClass.getDeclaredFields();
        for (Field f : fields) {
            SqliteColumn aColumn = f.getDeclaredAnnotation(SqliteColumn.class);
            SqliteColumnIgnore aIgnore = f.getDeclaredAnnotation(SqliteColumnIgnore.class);

            if (aIgnore != null) {
                continue;
            }

            if (aColumn != null) {
                columnName = aColumn.name();
            } else {
                columnName = f.getName();
            }

            Object obj = resultSet.getObject(columnName);
            data.put(columnName, obj);
        }

        String json = Global.GsonObject.toJson(data);
        result = Global.GsonObject.fromJson(json, tClass);

        return result;
    }
}

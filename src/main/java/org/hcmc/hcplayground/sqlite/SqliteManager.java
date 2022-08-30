package org.hcmc.hcplayground.sqlite;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.annotation.SqliteColumn;
import org.hcmc.hcplayground.annotation.SqliteColumnIgnore;
import org.hcmc.hcplayground.enums.PlayerBannedState;
import org.hcmc.hcplayground.sqlite.table.BanPlayerDetail;
import org.hcmc.hcplayground.utility.AesAlgorithm;
import org.hcmc.hcplayground.utility.Global;
import org.jetbrains.annotations.NotNull;

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

    private static final String SQLITE_CONNECTION_STRING = "jdbc:sqlite:%s/database/hcdb.db";
    private static final JavaPlugin plugin = HCPlayground.getInstance();
    private static Connection connection;

    public SqliteManager() {

    }

    public static Connection CreateSqliteConnection() throws SQLException {
        String url = String.format(SQLITE_CONNECTION_STRING, plugin.getDataFolder());
        connection = DriverManager.getConnection(url);
        return connection;
    }

    public static List<UUID> getArmorStandIdList(String group) throws SQLException {
        String commandText = String.format("select * from hologram where [group] = '%s'", group);
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(commandText);

        List<UUID> idList = new ArrayList<>();

        while (resultSet.next()) {
            Object id = resultSet.getObject("id");
            UUID uuid = UUID.fromString(id.toString());
            idList.add(uuid);
        }

        resultSet.close();
        statement.close();
        return idList;
    }

    public static void clearArmorStandRecord(String group) throws SQLException {
        String commandText = String.format("delete from hologram where [group] = '%s'", group);
        Statement statement = connection.createStatement();
        statement.executeUpdate(commandText);
        statement.close();
    }

    public static void insertArmorStandId(UUID uuid, String group) throws SQLException {
        String commandText = String.format("insert into hologram (id, [group]) values ('%s', '%s')", uuid, group);
        Statement statement = connection.createStatement();
        statement.executeUpdate(commandText);
        statement.close();
    }

    public static BanPlayerDetail getBanDetail(Player player) throws SQLException {
        BanPlayerDetail detail;

        String commandText = String.format("select * from banDetails where playerId = '%s'", player.getUniqueId());
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(commandText);

        boolean exist = resultSet.next();
        if (!exist) return null;
        detail = ResultSetSerializer(resultSet, BanPlayerDetail.class);
        resultSet.close();
        statement.close();

        return detail;
    }

    public static boolean doPlayerRegister(Player player, String password) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException, SQLException {
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

    public static boolean doPlayerUnregister(Player player, String password) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException, SQLException {
        UUID uuid = player.getUniqueId();

        String key = uuid.toString().replace("-", "");
        String aesPassword = AesAlgorithm.Encrypt(key, password);
        String commandText = String.format("delete from player where uuid = '%s' and password = '%s'", uuid, aesPassword);
        Statement statement = connection.createStatement();
        int count = statement.executeUpdate(commandText);
        statement.close();

        return count != 0;
    }

    public static boolean isPlayerRegister(Player player) throws SQLException {
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

    public static boolean ChangePassword(Player player, String newPassword) throws SQLException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException {
        UUID uuid = player.getUniqueId();
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
        Statement statement = connection.createStatement();
        String commandText;

        OfflinePlayer[] offlinePlayers = plugin.getServer().getOfflinePlayers();
        OfflinePlayer offlinePlayer = Arrays.stream(offlinePlayers).filter(x -> Objects.requireNonNull(x.getName()).equalsIgnoreCase(targetName)).findAny().orElse(null);
        if (offlinePlayer == null) return PlayerBannedState.Player_Not_Exist;
        UUID targetUuid = offlinePlayer.getUniqueId();
        UUID masterUuid = master.getUniqueId();

        commandText = String.format("insert into banRecord (id,masterId,playerId,message) values ('%s','%s','%s','%s')", UUID.randomUUID(), masterUuid, targetUuid, reason);
        statement.executeUpdate(commandText);
        commandText = String.format("update player set getBanDetail = 'true' where uuid = '%s'", targetUuid);
        statement.executeUpdate(commandText);
        statement.close();

        return PlayerBannedState.Player_Banned;
    }

    public static PlayerBannedState UnBanPlayer(@NotNull String targetName) throws SQLException {
        Statement statement = connection.createStatement();
        String commandText;

        OfflinePlayer[] offlines = plugin.getServer().getOfflinePlayers();
        OfflinePlayer target = Arrays.stream(offlines).filter(x -> Objects.requireNonNull(x.getName()).equalsIgnoreCase(targetName)).findAny().orElse(null);
        if (target == null) return PlayerBannedState.Player_Not_Exist;

        UUID targetUuid = target.getUniqueId();
        commandText = String.format("update player set getBanDetail = 'false' where uuid = '%s'", targetUuid);
        statement.executeUpdate(commandText);
        statement.close();

        return PlayerBannedState.Player_Unbanned;
    }

    private static <T> T ResultSetSerializer(ResultSet resultSet, Class<T> tClass) throws SQLException {
        T result;
        String columnName;
        Map<String, Object> data = new HashMap<>();

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
        //Global.LogMessage(json);
        result = Global.GsonObject.fromJson(json, tClass);

        return result;
    }
}

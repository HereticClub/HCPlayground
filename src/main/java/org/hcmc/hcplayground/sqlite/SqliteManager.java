package org.hcmc.hcplayground.sqlite;

import org.bukkit.plugin.java.JavaPlugin;
import org.hcmc.hcplayground.HCPlayground;

import javax.crypto.KeyGenerator;
import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.UUID;

public class SqliteManager {

    private final JavaPlugin plugin = HCPlayground.getPlugin();
    private final Connection connection;

    public SqliteManager() throws SQLException {

        String url = String.format("jdbc:sqlite:%s/database/hcdb.db", plugin.getDataFolder());
        connection = DriverManager.getConnection(url);
    }

    public void Disconnect() throws SQLException {
        if (!connection.isClosed()) connection.close();
    }

    public boolean isPlayerExist(UUID playerUuid) throws SQLException {
        String commandText =String.format("select * from player where uuid = '%s'", playerUuid);
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(commandText);

        return resultSet.next();
    }

    private String Encrypt(String key, String raw) throws NoSuchAlgorithmException {
        // TODO: make security password
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256);

        return "";
    }
}

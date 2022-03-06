package org.hcmc.hcplayground.sqlite;

import org.bukkit.plugin.java.JavaPlugin;
import org.hcmc.hcplayground.HCPlayground;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqliteManager {

    private final JavaPlugin plugin = HCPlayground.getPlugin();
    private final Connection connection;

    public SqliteManager() throws SQLException {

        String url = String.format("jdbc:sqlite:%s/database/hcdb.db", plugin.getDataFolder());
        connection = DriverManager.getConnection(url);
    }

    public void Disconnect() throws SQLException {
        if(!connection.isClosed()) connection.close();
    }
}

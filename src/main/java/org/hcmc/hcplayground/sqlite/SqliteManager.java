package org.hcmc.hcplayground.sqlite;

import org.bukkit.plugin.java.JavaPlugin;
import org.hcmc.hcplayground.HCPlayground;

import java.sql.*;

public class SqliteManager {

    private static final JavaPlugin plugin = HCPlayground.getPlugin();

    public SqliteManager() {

    }

    public static Connection CreateSqliteConnection() throws SQLException {
        String url = String.format("jdbc:sqlite:%s/database/hcdb.db", plugin.getDataFolder());
        return DriverManager.getConnection(url);
    }
}

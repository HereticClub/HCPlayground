package org.hcmc.hcplayground;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.hcmc.hcplayground.expansion.HCPluginExpansion;
import org.hcmc.hcplayground.filter.ConsoleLog4jFilter;
import org.hcmc.hcplayground.listener.PluginListener;
import org.hcmc.hcplayground.manager.*;
import org.hcmc.hcplayground.manager.PermissionManager;
import org.hcmc.hcplayground.model.player.PlayerData;
import org.hcmc.hcplayground.runnable.EquipmentMonitorRunnable;
import org.hcmc.hcplayground.sqlite.SqliteManager;
import org.hcmc.hcplayground.utility.Global;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;

public class HCPlayground extends JavaPlugin {

    private static HCPlayground instance;
    private static BukkitTask task;

    public HCPlayground() {

    }

    public static HCPlayground getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        try {
            // 连接和加载Sqlite数据库
            Global.Sqlite = SqliteManager.CreateSqliteConnection();
            // 重新加载所有yml文档
            Global.ReloadConfiguration();
            // 以下代码不需要在ReloadPlugin()中执行，只需要在插件启用时执行一次
            // 启动runnable线程，每秒循环执行一次
            task = Global.runnable.runTaskTimer(this, 20, 20);
            // 注册Listener
            getServer().getPluginManager().registerEvents(new PluginListener(), this);
            //getServer().getMessenger().registerIncomingPluginChannel(this, "HOLOGRAM", new HologramListener());
            //getServer().getMessenger().registerOutgoingPluginChannel(this, "HOLOGRAM");
            // 验证并且注册所依赖的Plugin
            Global.ValidWorldGuardPlugin();
            Global.ValidVaultPlugin();
            Global.ValidParkourPlugin();
            // 执行/reload指令后，重新加载所有玩家数据
            // 假定每个在线玩家已经注册并且已经登陆
            for (Player p : getServer().getOnlinePlayers()) {
                PlayerData pd = PlayerManager.getPlayerData(p);
                // 强制设置在线玩家已经注册并且已经登陆
                pd.setRegister(true);
                pd.setLogin(true);
                // 获取玩家身上的附加属性
                // 任务new EquipmentMonitorRunnable(player).runTask(plugin)
                // 已经执行了一次PlayerManager.setPlayerData(player, playerData)
                // 所以不需要在这里再次执行
                new EquipmentMonitorRunnable(p).runTask(getInstance());
            }
            //ConsoleLegacyFilter.RegisterFilter(getLogger());
            ConsoleLog4jFilter.RegisterFilter();
            HCPluginExpansion.RegisterExpansion();
        } catch (IllegalAccessException | NoSuchFieldException | SQLException | NoSuchAlgorithmException |
                 InvalidKeySpecException | IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();

        try {
            // 停止runnable线程
            if (task != null) task.cancel();
            // 注销插件，保存所有在线玩家数据，断开可Sqlite的连接，清空所有Map对象
            Global.Dispose();
            HCPluginExpansion.UnregisterExpansion();
            Global.LogMessage(String.format("%s has been disabled.", this.getName()));
        } catch (SQLException | IOException | IllegalAccessException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLoad() {
        instance = this;
        super.onLoad();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        return false;
    }
}

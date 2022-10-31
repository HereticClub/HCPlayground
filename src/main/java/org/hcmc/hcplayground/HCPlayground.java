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
import org.hcmc.hcplayground.manager.PlayerManager;
import org.hcmc.hcplayground.model.player.PlayerData;
import org.hcmc.hcplayground.runnable.EquipmentMonitorRunnable;
import org.hcmc.hcplayground.sqlite.SqliteManager;
import org.hcmc.hcplayground.utility.Global;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;

/*
终端显示字体背景和字体颜色等使用用法
示例       :       \033[0;30;41m
\033      :       ESC字符的ASCII代码，固定值，必填
[         :       左中括号，分隔符，必填
0;30;41m  :       颜色代码，如果该部分使用0m代码，表示清除字体颜色效果

该代码分3部分，中间用分号作为分隔符，最后用小写字母m结尾
第一个数字，0：正常，1：高亮，4：下划线，5：闪烁，7：反白，8：隐藏
第二和第三个数字，分别代表字体的颜色和字体的背景颜色，不分顺序
从30 - 39代表字体颜色，从40 - 49代表字体的背景颜色
示例： 1;40;32m，表示字体颜色为32，背景色为40，高亮显示
示例： 0;33;41m，表示字体颜色为33，背景色为41，暗色显示
示例： 0m，表示清除所有字体效果，用传统的黑底白字显示

颜色数值代码
字色              背景             颜色
---------------------------------------
30                40              黑色
31                41              紅色
32                42              綠色
33                43              黃色
34                44              藍色
35                45              紫紅色
36                46              青藍色
37                47              白色
 */


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
        try {
            instance = this;
            // 连接和加载Sqlite数据库
            Global.Sqlite = SqliteManager.CreateSqliteConnection();
            Global.LogMessage("Sqlite database connected");
            // 重新加载所有yml文档
            Global.ReloadConfiguration();
            Global.LogMessage("All configuration files loaded");
            // 以下代码不需要在ReloadPlugin()中执行，只需要在插件启用时执行一次
            // 启动全局runnable线程，每秒循环执行一次
            task = Global.runnable.runTaskTimer(this, 20, 2);
            Global.LogMessage("A global timer has been startup, refresh per second");
            // 注册Listener
            getServer().getPluginManager().registerEvents(new PluginListener(), this);
            Global.LogMessage("A global event listener has been startup");
            //getServer().getMessenger().registerIncomingPluginChannel(this, "HOLOGRAM", new HologramListener());
            //getServer().getMessenger().registerOutgoingPluginChannel(this, "HOLOGRAM");
            // 验证并且注册所依赖的Plugin
            Global.ValidWorldGuardPlugin();
            Global.ValidVaultPlugin();
            Global.ValidParkourPlugin();
            // 执行/reload指令后，重新加载所有玩家数据
            // 假定每个在线玩家已经注册并且已经登陆
            for (Player p : getServer().getOnlinePlayers()) {
                Global.LogMessage(String.format("Initialize online player data for - %s", p.getName()));
                PlayerData data = PlayerManager.getPlayerData(p);
                // 强制设置在线玩家已经注册并且已经登陆
                data.setRegister(true);
                data.setLogin(true);
                // 获取玩家身上的附加属性
                // 任务new EquipmentMonitorRunnable(player).runTask(plugin)
                // 已经执行了一次PlayerManager.setPlayerData(player, playerData)
                // 所以不需要在这里再次执行
                new EquipmentMonitorRunnable(p).runTask(getInstance());
            }
            //ConsoleLegacyFilter.RegisterFilter(getLogger());
            ConsoleLog4jFilter.RegisterFilter();
            HCPluginExpansion.RegisterExpansion();
            Global.LogMessage(String.format("\033[1;32m%s v%s enabled\033[0m", this.getName(), this.getDescription().getVersion()));
        } catch (IllegalAccessException | NoSuchFieldException | SQLException | NoSuchAlgorithmException |
                 InvalidKeySpecException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();

        try {
            // 停止runnable线程
            if (task != null) task.cancel();
            Global.LogMessage("Global runnable task stopped");
            // 注销插件，保存所有在线玩家数据，断开可Sqlite的连接，清空所有Map对象
            Global.Dispose();
            HCPluginExpansion.UnregisterExpansion();
            Global.LogMessage(String.format("%s placeholder expansion unregistered", getName()));
            Global.LogMessage(String.format("%s has been disabled.", this.getName()));
        } catch (SQLException | IOException | IllegalAccessException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        return false;
    }
}

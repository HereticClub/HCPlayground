package org.hcmc.hcplayground;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.hcmc.hcplayground.filter.ConsoleLog4jFilter;
import org.hcmc.hcplayground.listener.PluginListener;
import org.hcmc.hcplayground.manager.*;
import org.hcmc.hcplayground.model.permission.PermissionManager;
import org.hcmc.hcplayground.model.player.PlayerData;
import org.hcmc.hcplayground.model.template.TemplateManager;
import org.hcmc.hcplayground.sqlite.SqliteManager;
import org.hcmc.hcplayground.utility.Global;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;

public class HCPlayground extends JavaPlugin {

    private static HCPlayground instance;
    private static BukkitTask task;

    public HCPlayground() {

    }

    @Override
    public void onEnable() {
        super.onEnable();

        try {
            // 重新加载所有yml文档
            ReloadConfiguration();
            // 以下代码不需要在ReloadPlugin()中执行，只需要在插件启用时执行一次
            // 启动runnable线程，每秒循环执行一次
            task = Global.runnable.runTaskTimer(this, 20, 20);
            // 注册Listener
            getServer().getPluginManager().registerEvents(new PluginListener(), this);
            // 连接和加载Sqlite数据库
            Global.Sqlite = SqliteManager.CreateSqliteConnection();
            // 验证并且注册所依赖的Plugin
            Global.ValidWorldGuardPlugin();
            Global.ValidVaultPlugin();
            // 重新加载所有玩家数据
            // 假定每个在线玩家已经注册并且已经登陆
            for (Player p : getServer().getOnlinePlayers()) {
                PlayerData pd = Global.getPlayerData(p);
                pd.setRegister(true);
                pd.setLogin(true);
                Global.playerMap.put(p.getUniqueId(), pd);
            }
            ConsoleLog4jFilter.RegisterFilter();
        } catch (IllegalAccessException | NoSuchFieldException | SQLException | NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();

        try {
            // 停止runnable线程
            task.cancel();
            // 注销插件，保存所有在线玩家数据，断开可Sqlite的连接，清空所有Map对象
            Global.Dispose();
            Global.LogMessage(String.format("%s has been disabled", this.getName()));
        } catch (SQLException | IOException | IllegalAccessException e) {
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
        System.out.println(label);
        return false;
    }


    public static HCPlayground getInstance() {
        return instance;
    }

    public static JavaPlugin getPlugin() {
        return getPlugin(instance.getClass());
    }

    private void InitialChildrenFolders() {
        String[] childrenFolders = new String[]{"profile", "database"};

        if (!getDataFolder().exists()) getDataFolder().mkdir();

        for (String s : childrenFolders) {
            File f = new File(String.format("%s/%s", getDataFolder(), s));
            boolean flag = f.mkdir();
        }
    }

    public void ReloadConfiguration() throws IllegalAccessException, NoSuchFieldException, SQLException, NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        // 创建插件所需要的子目录
        InitialChildrenFolders();
        // 合并所有Yml格式文档到插件目录
        // 兼容前版本的配置，并且添加新版本的配置
        Global.SaveYamlResource();
        // 加载插件的基本设置config.yml
        Global.LoadConfig();
        // 从yml格式文档加载配置到实例，必须按照指定的加载顺序
        // 1.加载本地化文档
        LocalizationManager.Load(Global.getYamlConfiguration("messages.yml"));
        // 2.加载权限列表
        PermissionManager.Load(Global.getYamlConfiguration("permission.yml"));
        // 3.加载指令
        CommandManager.Load(Global.getYamlConfiguration("command.yml"));
        // 4.加载自定义物品
        ItemManager.Load(Global.getYamlConfiguration("items.yml"));
        // 5.加载破坏方块的自定义掉落列表，可掉落自定义物品
        DropManager.Load(Global.getYamlConfiguration("drops.yml"));
        // 6.加载等级设置列表
        LevelManager.Load(Global.getYamlConfiguration("levels.yml"));
        // 7.加载各种菜单(箱子)模板
        TemplateManager.Load(Global.getYamlConfiguration("inventoryTemplate.yml"));
        // 8.加载各种可生成的生物列表
        MobManager.Load(Global.getYamlConfiguration("mobs.yml"));
        // 9.加载随机公告消息列表
        BroadcastManager.Load(Global.getYamlConfiguration("broadcast.yml"));
        // 10.加载清除垃圾物品设置
        ClearLagManager.Load(Global.getYamlConfiguration("clearlag.yml"));
    }
}

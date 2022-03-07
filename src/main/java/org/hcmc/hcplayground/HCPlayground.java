package org.hcmc.hcplayground;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.hcmc.hcplayground.command.CommandManager;
import org.hcmc.hcplayground.dropManager.DropManager;
import org.hcmc.hcplayground.itemManager.ItemManager;
import org.hcmc.hcplayground.level.LevelManager;
import org.hcmc.hcplayground.listener.PluginListener;
import org.hcmc.hcplayground.localization.Localization;
import org.hcmc.hcplayground.model.Global;
import org.hcmc.hcplayground.permission.PermissionManager;
import org.hcmc.hcplayground.scheduler.PluginRunnable;
import org.hcmc.hcplayground.sqlite.SqliteManager;
import org.hcmc.hcplayground.template.TemplateManager;
import org.jetbrains.annotations.NotNull;

import java.io.File;
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
            ReloadPlugin();
        } catch (IllegalAccessException | NoSuchFieldException | SQLException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();

        try {
            task.cancel();
            Global.Dispose();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLoad() {
        instance = this;
        super.onLoad();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            System.out.println("Console send a command");
        }
        if (sender instanceof Player) {
            System.out.println("Player send a command");
        }

        return true;
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

    private void ReloadPlugin() throws IllegalAccessException, NoSuchFieldException, SQLException, NoSuchAlgorithmException, InvalidKeySpecException {
        // 创建插件所需要的子目录
        InitialChildrenFolders();
        // 复制并且加载所有Yml格式文档到插件目录
        Global.SaveYamlResource();
        // 本地化对象必须在最开始运行
        Localization.Load(Global.getYamlConfiguration("messages.yml"));
        PermissionManager.Load(Global.getYamlConfiguration("permission.yml"));
        CommandManager.Load(Global.getYamlConfiguration("command.yml"));
        DropManager.Load(Global.getYamlConfiguration("drops.yml"));
        ItemManager.Load(Global.getYamlConfiguration("items.yml"));
        LevelManager.Load(Global.getYamlConfiguration("levels.yml"));
        TemplateManager.Load(Global.getYamlConfiguration("inventoryTemplate.yml"));
        // 连接和加载Sqlite数据库
        Global.Sqlite = new SqliteManager();
        // 验证并且注册所依赖的Plugin
        Global.ValidWorldGuardPlugin();
        Global.ValidVaultPlugin();

        task = Global.runnable.runTaskTimer(this, 20, 20);

        // 注册Listener
        getServer().getPluginManager().registerEvents(new PluginListener(), this);
    }
}

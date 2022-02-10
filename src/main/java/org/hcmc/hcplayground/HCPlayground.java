package org.hcmc.hcplayground;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.hcmc.hcplayground.drops.DropManager;
import org.hcmc.hcplayground.items.ItemManager;
import org.hcmc.hcplayground.listener.PluginListener;
import org.hcmc.hcplayground.model.Global;
import org.hcmc.hcplayground.tabCompleter.QuartermasterTabCompleter;

import java.io.File;

public class HCPlayground extends JavaPlugin {

    private final HCPlayground instance;

    public HCPlayground() {
        /* TODO: Initialize plugin here*/
        instance = this;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        ReloadPlugin();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            System.out.println("Console send a command");
        }
        if (sender instanceof Player) {
            System.out.println("Player send a command");
        }

        return true;
    }

    public HCPlayground getInstance() {
        return instance;
    }

    private void InitialChildrenFolders() {
        String[] childrenFolders = new String[]{"profile"};

        for (String s : childrenFolders) {
            File f = new File(getDataFolder(), s);
            boolean flag = f.mkdir();
        }
    }

    private void ReloadPlugin(){

        InitialChildrenFolders();

        // 复制并且加载所有Yml格式文档到插件目录
        Global.SaveYamlResource();
        ItemManager.Load(Global.getYamlConfiguration("items.yml"));
        DropManager.Load(Global.getYamlConfiguration("drops.yml"));

        // 验证并且注册所依赖的Plugin
        Global.ValidWorldGuardPlugin();
        Global.ValidVaultPlugin();

        // 注册Command
        getCommand("quartermaster").setExecutor(new ItemManager());
        getCommand("quartermaster").setTabCompleter(new QuartermasterTabCompleter());

        // 注册Listener
        getServer().getPluginManager().registerEvents(new PluginListener(), this);
    }
}

package org.hcmc.hcplayground;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.hcmc.hcplayground.deserializer.EquipmentSlotDeserializer;
import org.hcmc.hcplayground.deserializer.ItemFlagsDeserializer;
import org.hcmc.hcplayground.deserializer.MaterialDeserializer;
import org.hcmc.hcplayground.deserializer.PotionEffectDeserializer;
import org.hcmc.hcplayground.dropManager.DropManager;
import org.hcmc.hcplayground.itemManager.ItemManager;
import org.hcmc.hcplayground.itemManager.weapon.Weapon;
import org.hcmc.hcplayground.listener.PluginListener;
import org.hcmc.hcplayground.model.Global;
import org.hcmc.hcplayground.tabCompleter.QuartermasterTabCompleter;

import java.io.File;

public class HCPlayground extends JavaPlugin {

    private static HCPlayground instance;

    public HCPlayground() {

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
        instance = this;
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

    public static HCPlayground getInstance() {
        return instance;
    }

    public static JavaPlugin getPlugin() {
        return getPlugin(instance.getClass());
    }

    private void InitialChildrenFolders() {
        String[] childrenFolders = new String[]{"profile"};

        for (String s : childrenFolders) {
            File f = new File(getDataFolder(), s);
            boolean flag = f.mkdir();
        }
    }

    private void ReloadPlugin() {
        // 创建插件所需要的子目录
        InitialChildrenFolders();

        // 复制并且加载所有Yml格式文档到插件目录
        Global.SaveYamlResource();
        ItemManager.Load(Global.getYamlConfiguration("items.yml"));
        DropManager.Load(Global.getYamlConfiguration("drops.yml"));

        // 验证并且注册所依赖的Plugin
        Global.ValidWorldGuardPlugin();
        Global.ValidVaultPlugin();

        // 注册Command
        PluginCommand qmCommand = getCommand("quartermaster");
        if (qmCommand != null) {
            qmCommand.setExecutor(new ItemManager());
            qmCommand.setTabCompleter(new QuartermasterTabCompleter());
        }

        // 注册Listener
        getServer().getPluginManager().registerEvents(new PluginListener(), this);
    }
}

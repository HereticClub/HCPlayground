package org.hcmc.hcplayground.command;

import org.bukkit.command.CommandMap;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.model.Global;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class CommandManager {

    public static List<CommandItem> Commands = new ArrayList<>();
    private static final JavaPlugin plugin = HCPlayground.getPlugin();

    public CommandManager() {

    }

    public static void Load(YamlConfiguration yaml) throws IllegalAccessException, NoSuchFieldException {
        // 从command.yml获取可执行的指令集
        Commands = Global.SetItemList(yaml, CommandItem.class);
        // 获取Bukkit.Server.CommandMap字段
        Field fieldCommandMap = plugin.getServer().getClass().getDeclaredField("commandMap");
        // 设置CommandMap字段为可访问
        fieldCommandMap.setAccessible(true);
        // 从CommandMap字段获取CommandMap实例
        CommandMap commandMap = (CommandMap) fieldCommandMap.get(plugin.getServer());
        // 注册自定义命令，这些命令无需在plugin.yml中定义
        for (CommandItem item : Commands) {
            item.Enroll(commandMap);
        }
    }
}

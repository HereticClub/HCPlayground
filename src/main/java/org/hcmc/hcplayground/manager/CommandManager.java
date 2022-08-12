package org.hcmc.hcplayground.manager;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.model.command.CommandArgument;
import org.hcmc.hcplayground.model.command.CommandItem;
import org.hcmc.hcplayground.utility.Global;

import java.util.ArrayList;
import java.util.List;

public class CommandManager {

    public static List<CommandItem> Commands = new ArrayList<>();
    private static final JavaPlugin plugin = HCPlayground.getInstance();

    public CommandManager() {

    }

    public static void Load(YamlConfiguration yaml) throws IllegalAccessException, NoSuchFieldException {
        // 从command.yml获取可执行的指令集
        Commands = Global.deserializeList(yaml, CommandItem.class);
        // 从CommandMap字段获取CommandMap实例
        CommandMap commandMap = Global.getCommandMap();
        // 注册自定义命令，这些命令无需在plugin.yml中定义
        for (CommandItem item : Commands) {
            item.Enroll(commandMap);

            String path = String.format("%s.args", item.id);
            ConfigurationSection section = yaml.getConfigurationSection(path);
            if (section == null) continue;
            item.args = Global.deserializeList(section, CommandArgument.class);
        }
    }

    public static void runConsoleCommand(String command, Player player) {
        ConsoleCommandSender sender = Bukkit.getConsoleSender();

        String _command = PlaceholderAPI.setPlaceholders(player, command);
        Bukkit.dispatchCommand(sender, _command);
        Global.LogMessage(String.format("%s issued a console command: %s", player.getName(), _command));
    }

    public static void runPlayerCommand(String command, Player player) {
        String _command = PlaceholderAPI.setPlaceholders(player, command);
        player.performCommand(_command);
        Global.LogMessage(String.format("%s issued a player command: %s", player.getName(), _command));
    }
}

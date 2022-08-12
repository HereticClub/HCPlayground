package org.hcmc.hcplayground.filter;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.hcmc.hcplayground.model.command.CommandItem;
import org.hcmc.hcplayground.utility.Global;

import java.util.Arrays;
import java.util.logging.Filter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class ConsoleLegacyFilter implements Filter {

    public ConsoleLegacyFilter() {

    }

    public static void RegisterFilter(Logger logger) {
        ConsoleLegacyFilter filter = new ConsoleLegacyFilter();
        Filter ff = logger.getFilter();
        //System.out.println(("\033[1;31mRegister: " + logger.getName() + "\033[0m"));

        logger.setFilter(filter);
        Logger.getLogger("Minecraft").setFilter(filter);
        Logger.getLogger("Console").setFilter(filter);
        Bukkit.getLogger().setFilter(filter);
        Bukkit.getServer().getLogger().setFilter(filter);
    }

    @Override
    public boolean isLoggable(LogRecord record) {
        if (record == null) return true;

        //System.out.println(("\033[1;35misLoggable: " + record.getLoggerName() + "\033[0m"));

        String message = record.getMessage();
        return ValidateMessage(message);
    }

    private boolean ValidateMessage(String message) {
        if (message == null) return true;
        String[] keys = message.split(" ");

        String commandText = Arrays.stream(keys).filter(x -> x.substring(0, 1).equalsIgnoreCase("/")).findAny().orElse(null);
        if (commandText == null) return true;

        commandText = commandText.substring(1);
        Command command = Global.getCommandMap().getCommand(commandText);
        if (command == null) return true;

        if (command.getName().equalsIgnoreCase(CommandItem.COMMAND_LOGIN)) return false;
        if (command.getName().equalsIgnoreCase(CommandItem.COMMAND_REGISTER)) return false;
        if (command.getName().equalsIgnoreCase(CommandItem.COMMAND_UNREGISTER)) return false;
        return !command.getName().equalsIgnoreCase(CommandItem.COMMAND_CHANGE_PASSWORD);
    }
}

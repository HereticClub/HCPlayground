package org.hcmc.hcplayground.filter;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;
import org.bukkit.command.Command;
import org.hcmc.hcplayground.model.command.CommandItem;
import org.hcmc.hcplayground.utility.Global;

import java.util.Arrays;

public class ConsoleLog4jFilter extends AbstractFilter {

    public ConsoleLog4jFilter() {

    }

    public static void RegisterFilter() {
        Logger logger = (Logger) LogManager.getRootLogger();
        ConsoleLog4jFilter filter = new ConsoleLog4jFilter();
        logger.addFilter(filter);
    }

    @Override
    public Result filter(LogEvent event) {
        if (event == null) return Result.NEUTRAL;

        Message message = event.getMessage();
        if (message == null) return Result.NEUTRAL;

        return ValidateMessage(message.getFormattedMessage());
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Message msg, Throwable t) {
        if (msg == null) return Result.NEUTRAL;
        return ValidateMessage(msg.getFormattedMessage());
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String msg, Object... params) {
        return ValidateMessage(msg);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Object msg, Throwable t) {
        String candidate = null;
        if (msg != null) {
            candidate = msg.toString();
        }
        return ValidateMessage(candidate);
    }

    private Result ValidateMessage(String message) {
        if (message == null) return Result.NEUTRAL;
        String[] keys = message.split(" ");

        String commandText = Arrays.stream(keys).filter(x -> x.substring(0, 1).equalsIgnoreCase("/")).findAny().orElse(null);
        if (commandText == null) return Result.NEUTRAL;

        commandText = commandText.substring(1);
        Command command = Global.CommandMap.getCommand(commandText);
        if (command == null) return Result.NEUTRAL;

        if (command.getName().equalsIgnoreCase(CommandItem.COMMAND_LOGIN)) return Result.DENY;
        if (command.getName().equalsIgnoreCase(CommandItem.COMMAND_REGISTER)) return Result.DENY;
        if (command.getName().equalsIgnoreCase(CommandItem.COMMAND_UNREGISTER)) return Result.DENY;
        if (command.getName().equalsIgnoreCase(CommandItem.COMMAND_CHANGE_PASSWORD)) return Result.DENY;

        return Result.NEUTRAL;
    }
}

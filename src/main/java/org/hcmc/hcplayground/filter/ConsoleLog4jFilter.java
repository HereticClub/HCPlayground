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
import java.util.List;

public class ConsoleLog4jFilter extends AbstractFilter {

    private static final long serialVersionUID = -5594073755007974254L;

    public ConsoleLog4jFilter() {

    }

    public static void RegisterFilter() {
        ConsoleLog4jFilter filter = new ConsoleLog4jFilter();

        Logger logger = (Logger) LogManager.getRootLogger();
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
        List<String> keys = Arrays.stream(message.split(" ")).toList();

        String commandText = keys.stream().filter(x -> x.length() >= 1 && x.substring(0, 1).equalsIgnoreCase("/")).findAny().orElse(null);
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

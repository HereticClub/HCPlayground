package org.hcmc.hcplayground.handler;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

public class PermissionHandler {


    public PermissionHandler() {

    }

    public static boolean HasPermission(CommandSender sender, String permission) {
        if (sender instanceof ConsoleCommandSender) return true;
        if (sender.isOp()) return true;
        /*
        if (sender.hasPermission("hcplayground.*")) return true;
        if (sender.hasPermission("hcplayground.all")) return true;
        if (sender.hasPermission("quartermaster.*")) return true;
        if (sender.hasPermission("quartermaster.all")) return true;
         */
        return sender.hasPermission(permission);
    }
}

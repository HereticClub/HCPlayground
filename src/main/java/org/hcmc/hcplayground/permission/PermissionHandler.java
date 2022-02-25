package org.hcmc.hcplayground.permission;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

public class PermissionHandler {


    public PermissionHandler() {

    }

    public static boolean HasPermission(CommandSender sender, String permission) {
        if (sender instanceof ConsoleCommandSender) return true;
        if (sender.isOp()) return true;

        return sender.hasPermission(permission);
    }
}

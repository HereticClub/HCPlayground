package org.hcmc.hcplayground.TabCompleter;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.hcmc.hcplayground.Handler.PermissionHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class QuartermasterTabCompleter implements TabCompleter {

    public QuartermasterTabCompleter() {

    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> tabs = new ArrayList<>();

        if (args.length == 1 && PermissionHandler.HasPermission(sender, "quartermaster.weapon")) {
            tabs.add("weapon");
        }
        if (args.length == 1 && PermissionHandler.HasPermission(sender, "quartermaster.armor")) {
            tabs.add("armor");
        }
        if (args.length == 1 && PermissionHandler.HasPermission(sender, "quartermaster.off-hand")) {
            tabs.add("off-hand");
        }
        if (args.length == 1 && PermissionHandler.HasPermission(sender, "quartermaster.accessory")) {
            tabs.add("accessory");
        }
        if (args.length == 1 && PermissionHandler.HasPermission(sender, "quartermaster.gui")) {
            tabs.add("gui");
        }
        return tabs;
    }
}

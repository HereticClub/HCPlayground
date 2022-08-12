package org.hcmc.hcplayground.manager;

import org.bukkit.command.CommandMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.hcmc.hcplayground.model.ccmd.CcmdAction;
import org.hcmc.hcplayground.model.ccmd.CcmdItem;
import org.hcmc.hcplayground.utility.Global;

import java.util.ArrayList;
import java.util.List;

public class CcmdManager {

    private static List<CcmdItem> commands = new ArrayList<>();

    public CcmdManager() {

    }

    public static void Load(YamlConfiguration yaml) throws IllegalAccessException {
        CommandMap commandMap = Global.getCommandMap();

        commands = Global.deserializeList(yaml, CcmdItem.class);
        for (CcmdItem cc : commands) {
            cc.Enroll(commandMap);

            String path = String.format("%s.actions", cc.getId());
            ConfigurationSection section = yaml.getConfigurationSection(path);
            if (section == null) continue;
            cc.actions = Global.deserializeList(section, CcmdAction.class);
        }
    }

    public static List<CcmdItem> getCommands() {
        return commands;
    }
}

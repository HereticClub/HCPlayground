package org.hcmc.hcplayground.manager;

import com.google.gson.reflect.TypeToken;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.hcmc.hcplayground.model.ccmd.CcmdAction;
import org.hcmc.hcplayground.model.ccmd.CcmdItem;
import org.hcmc.hcplayground.model.command.CommandItem;
import org.hcmc.hcplayground.model.item.ItemBase;
import org.hcmc.hcplayground.utility.Global;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CcmdManager {

    private static List<CcmdItem> commands = new ArrayList<>();

    public CcmdManager() {

    }

    public static void Load(YamlConfiguration yaml) throws IllegalAccessException {
        CommandMap commandMap = Global.CommandMap;

        commands = Global.SetItemList(yaml, CcmdItem.class);
        for (CcmdItem cc : commands) {
            cc.Enroll(commandMap);

            String path = String.format("%s.actions", cc.id);
            ConfigurationSection section = yaml.getConfigurationSection(path);
            if (section == null) continue;
            cc.actions = Global.SetItemList(section, CcmdAction.class);
        }
    }

    public static List<CcmdItem> getCommands() {
        return commands;
    }
}

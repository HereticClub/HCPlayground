package org.hcmc.hcplayground.manager;

import com.google.gson.annotations.Expose;
import me.clip.placeholderapi.PlaceholderAPI;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.command.defaults.PluginsCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.model.menu.MenuPanel;
import org.hcmc.hcplayground.model.menu.MenuPanelSlot;
import org.hcmc.hcplayground.model.menu.SlotClickCondition;
import org.hcmc.hcplayground.utility.Global;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;


public class MenuManager extends BukkitCommand {

    @Expose(serialize = false, deserialize = false)
    private static Plugin plugin = HCPlayground.getInstance();
    @Expose(serialize = false, deserialize = false)
    private static List<String> idList = new ArrayList<>();
    @Expose(serialize = false, deserialize = false)
    private static YamlConfiguration yaml;

    public static void Load(YamlConfiguration yaml) {
        MenuManager.yaml = yaml;
        idList = yaml.getKeys(false).stream().toList();
        CommandMap commandMap = Global.getCommandMap();

        for (String id : idList) {
            Command c = commandMap.getCommand(id);
            if (c != null) c.unregister(commandMap);
            MenuManager menuCommand = new MenuManager(id);
            if (!menuCommand.preregister(id)) continue;
            commandMap.register(plugin.getName(), menuCommand);
        }
    }

    public static List<String> getIdList() {
        return idList;
    }

    public MenuManager(String name) {
        super(name);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String command, @NotNull String[] strings) {

        try {
            Player player = (Player) sender;
            if (!StringUtils.isBlank(getPermission()) && !player.hasPermission(getPermission()) && !player.isOp()) {
                player.sendMessage(LanguageManager.getString("permission-message").replace("%permission%", getPermission()));
                return false;
            }

            MenuPanel menu = MenuManager.getMenuPanel(getName(), player);
            if (menu == null) return false;
            menu.open(player);
        } catch (InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }

        return true;
    }

    public static MenuPanel getMenuPanel(@NotNull String menuId, @NotNull Player player) throws InvalidConfigurationException {

        String decoratesPath = String.format("%s.decorates", menuId);
        ConfigurationSection section = yaml.getConfigurationSection(menuId);
        if (section == null) return null;
        MenuPanel menu = Global.deserialize(section, player, MenuPanel.class);

        ConfigurationSection sectionDecorates = yaml.getConfigurationSection(decoratesPath);
        if (sectionDecorates == null) return null;
        List<MenuPanelSlot> slots = Global.deserializeList(sectionDecorates, player, MenuPanelSlot.class);

        menu.setDecorates(slots);
        for (MenuPanelSlot slot : slots) {
            String slotId = slot.getId().split("\\.")[1];
            String leftCondPath = String.format("%s.decorates.%s.left_conditions.conditions", menuId, slotId);
            String rightCondPath = String.format("%s.decorates.%s.right_conditions.conditions", menuId, slotId);
            String leftDenyPath = String.format("%s.decorates.%s.left_conditions", menuId, slotId);
            String rightDenyPath = String.format("%s.decorates.%s.right_conditions", menuId, slotId);
            ConfigurationSection leftCondSection = yaml.getConfigurationSection(leftCondPath);
            ConfigurationSection rightCondSection = yaml.getConfigurationSection(rightCondPath);
            ConfigurationSection leftDenySection = yaml.getConfigurationSection(leftDenyPath);
            ConfigurationSection rightDenySection = yaml.getConfigurationSection(rightDenyPath);

            if (leftDenySection != null) {
                List<String> leftDeny = leftDenySection.getStringList("deny-message");
                leftDeny.replaceAll(x -> PlaceholderAPI.setPlaceholders(player, x).replace('&', '§'));
                slot.setLeftDenyMessage(leftDeny);
            }
            if (rightDenySection != null) {
                List<String> rightDeny = rightDenySection.getStringList("deny-message");
                rightDeny.replaceAll(x -> PlaceholderAPI.setPlaceholders(player, x).replace('&', '§'));
                slot.setRightDenyMessage(rightDeny);
            }
            if (leftCondSection != null)
                slot.setLeftConditions(Global.deserializeList(leftCondSection, player, SlotClickCondition.class));
            if (rightCondSection != null)
                slot.setRightConditions(Global.deserializeList(rightCondSection, player, SlotClickCondition.class));
        }
        return menu;
    }

    private boolean preregister(@NotNull String menuId) {
        String aliasesPath = String.format("%s.aliases", menuId);
        String permissionPath = String.format("%s.permission", menuId);
        String registerPath = String.format("%s.register", menuId);

        List<String> aliases = yaml.getStringList(aliasesPath);
        String permission = yaml.getString(permissionPath);
        boolean register = yaml.getBoolean(registerPath);

        if (!register) return false;
        if (permission == null) permission = "";
        // 以下两个属性必须设置为指令的名称
        this.setLabel(menuId);
        this.setName(menuId);
        // 以下所有属性值都不能为null
        if (!StringUtils.isBlank(permission)) this.setPermission(permission);
        this.setAliases(aliases);
        this.setPermissionMessage(LanguageManager.getString("permission-message").replace("%permission%", permission));
        this.setUsage(LanguageManager.getString(String.format("%s.usage", menuId)));
        this.setDescription(LanguageManager.getString(String.format("%s.description", menuId)));
        return true;
    }
}

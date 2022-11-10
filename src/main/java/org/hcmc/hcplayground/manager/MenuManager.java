package org.hcmc.hcplayground.manager;

import com.google.gson.annotations.Expose;
import com.google.gson.reflect.TypeToken;
import me.clip.placeholderapi.PlaceholderAPI;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.model.menu.*;
import org.hcmc.hcplayground.utility.Global;
import org.hcmc.hcplayground.utility.YamlFileFilter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;


public class MenuManager extends BukkitCommand {

    @Expose(serialize = false, deserialize = false)
    private static Plugin plugin = HCPlayground.getInstance();
    @Expose(serialize = false, deserialize = false)
    private static List<String> idList = new ArrayList<>();
    private static final Map<String, YamlConfiguration> mapYaml = new HashMap<>();
    /**
     * 菜单模板注册表，以菜单模板代替菜单实例<br>
     * String - 菜单实例id<br>
     * String - 菜单模板id<br>
     */
    private static final Map<String, String> menuRegister = new HashMap<>();

    public static void Load() {
        try {
            mapYaml.clear();
            idList.clear();

            CommandMap commandMap = Global.getCommandMap();
            File dir = new File(String.format("%s/menu", plugin.getDataFolder()));
            FilenameFilter filter = new YamlFileFilter();
            String[] filenames = dir.list(filter);
            if (filenames == null) return;

            for (String file : filenames) {
                YamlConfiguration yaml = new YamlConfiguration();
                yaml.load(String.format("%s/menu/%s", plugin.getDataFolder(), file));
                Set<String> keys = yaml.getKeys(false);
                for (String id : keys) {
                    idList.add(id);
                    mapYaml.put(id, yaml);

                    Command c = commandMap.getCommand(id);
                    if (c != null) c.unregister(commandMap);
                    MenuManager menuCommand = new MenuManager(id);
                    if (!menuCommand.preregister(id)) continue;
                    commandMap.register(plugin.getName(), menuCommand);
                }
            }
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public static void registerTemplateName(String menuId, String templateName) {
        if (!menuRegister.containsKey(menuId)) menuRegister.put(menuId, templateName);
    }

    public static String getTemplateName(String menuId) {
        return menuRegister.getOrDefault(menuId, menuId);
    }

    public static Map<String, String> getMenuRegister() {
        return menuRegister;
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
            // 显示目标玩家没有权限使用菜单信息
            if (!StringUtils.isBlank(getPermission()) && !player.hasPermission(getPermission()) && !player.isOp()) {
                player.sendMessage(LanguageManager.getString("no-permission").replace("%permission%", getPermission()));
                return false;
            }
            // 获取菜单名称和实例
            String menuName = getName();
            String menuId = MenuManager.getTemplateName(menuName);
            MenuPanel menu = MenuManager.getMenuPanel(menuId, player);
            // 显示菜单实例不存在信息
            if (menu == null) {
                player.sendMessage(LanguageManager.getString("menuNotExist").replace("%menu%", menuName));
                return false;
            }
            // 显示菜单在当前世界不可使用信息
            if (menu.isDisabled(player)) {
                player.sendMessage(LanguageManager.getString("menuWorldProhibited", player)
                        .replace("%world%", player.getWorld().getName())
                        .replace("%menu%", menu.getTitle())
                );
                return false;
            }

            Inventory inventory = menu.OnOpening(player, menuName);
            if (inventory != null) player.openInventory(inventory);
        } catch (InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public static MenuPanel getMenuPanel(@NotNull String menuId, @NotNull Player player) throws InvalidConfigurationException {
        YamlConfiguration yaml = mapYaml.get(menuId);
        if (yaml == null) return null;
        // 如果当前菜单实例采用了菜单模板指向
        String templateName = yaml.getString(String.format("%s.menu-template", menuId));
        if (!StringUtils.isBlank(templateName)) {
            menuId = templateName;
            yaml = mapYaml.get(templateName);
        }
        // 获取菜单的yml配置节段
        ConfigurationSection section = yaml.getConfigurationSection(menuId);
        if (section == null) return null;
        // 获取当前菜单是否由特别类设置，并且返回这个类的菜单实例
        String className = section.getString("class-name");
        Class<? extends MenuPanel> cls = getMenuClass(className);
        MenuPanel menu = Global.deserialize(section, player, cls);

        String decoratesPath = String.format("%s.decorates", menuId);
        ConfigurationSection decoratesSection = yaml.getConfigurationSection(decoratesPath);
        if (decoratesSection == null) return null;
        List<MenuPanelSlot> decorates = Global.deserializeList(decoratesSection, player, MenuPanelSlot.class);
        menu.setDecorates(decorates);

        Type mapType = new TypeToken<Map<Integer, List<String>>>() {
        }.getType();
        for (MenuPanelSlot slot : decorates) {
            slot.setOwningMenu(menu);
            String slotId = slot.getId().split("\\.")[1];
            String leftClickPath = String.format("%s.decorates.%s.left_click", menuId, slotId);
            String rightClickPath = String.format("%s.decorates.%s.right_click", menuId, slotId);
            String leftCondPath = String.format("%s.decorates.%s.left_conditions.conditions", menuId, slotId);
            String rightCondPath = String.format("%s.decorates.%s.right_conditions.conditions", menuId, slotId);
            String leftDenyPath = String.format("%s.decorates.%s.left_conditions", menuId, slotId);
            String rightDenyPath = String.format("%s.decorates.%s.right_conditions", menuId, slotId);
            ConfigurationSection leftCondSection = yaml.getConfigurationSection(leftCondPath);
            ConfigurationSection rightCondSection = yaml.getConfigurationSection(rightCondPath);
            ConfigurationSection leftDenySection = yaml.getConfigurationSection(leftDenyPath);
            ConfigurationSection rightDenySection = yaml.getConfigurationSection(rightDenyPath);
            ConfigurationSection leftClickSection = yaml.getConfigurationSection(leftClickPath);
            ConfigurationSection rightClickSection = yaml.getConfigurationSection(rightClickPath);
            // 左键点击指令
            if (leftClickSection != null) {
                String value = Global.GsonObject.toJson(leftClickSection.getValues(false));
                Map<Integer, List<String>> mapCommands = Global.GsonObject.fromJson(value, mapType);
                slot.setLeftCommands(mapCommands);
            }
            // 右键点击指令
            if (rightClickSection != null) {
                String value = Global.GsonObject.toJson(rightClickSection.getValues(false));
                Map<Integer, List<String>> mapCommands = Global.GsonObject.fromJson(value, mapType);
                slot.setRightCommands(mapCommands);
            }
            // 左键点击拒绝
            if (leftDenySection != null) {
                List<String> leftDeny = leftDenySection.getStringList("deny-message");
                leftDeny.replaceAll(x -> PlaceholderAPI.setPlaceholders(player, x).replace('&', '§'));
                slot.setLeftDenyMessage(leftDeny);
            }
            // 右键点击拒绝
            if (rightDenySection != null) {
                List<String> rightDeny = rightDenySection.getStringList("deny-message");
                rightDeny.replaceAll(x -> PlaceholderAPI.setPlaceholders(player, x).replace('&', '§'));
                slot.setRightDenyMessage(rightDeny);
            }
            // 左键点击条件
            if (leftCondSection != null)
                slot.setLeftConditions(Global.deserializeList(leftCondSection, player, SlotClickCondition.class));
            // 右键点击条件
            if (rightCondSection != null)
                slot.setRightConditions(Global.deserializeList(rightCondSection, player, SlotClickCondition.class));
        }
        return menu;
    }

    private static Class<? extends MenuPanel> getMenuClass(String className) {

        try {
            if (StringUtils.isBlank(className)) return LegacyMenuPanel.class;
            Class<? extends MenuPanel> menuClass = Class.forName(className).asSubclass(MenuPanel.class);
            return MenuPanel.class.isAssignableFrom(menuClass) ? menuClass : LegacyMenuPanel.class;
        } catch (Exception e) {
            return LegacyMenuPanel.class;
        }
    }

    private boolean preregister(@NotNull String menuId) {
        YamlConfiguration yaml = mapYaml.get(menuId);

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
        this.setPermissionMessage(LanguageManager.getString("no-permission").replace("%permission%", permission));
        this.setUsage(LanguageManager.getString(String.format("%s.usage", menuId)));
        this.setDescription(LanguageManager.getString(String.format("%s.description", menuId)));
        return true;
    }
}

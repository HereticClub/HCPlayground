package org.hcmc.hcplayground.manager;

import com.google.gson.annotations.Expose;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.model.menu.MenuPanel;
import org.hcmc.hcplayground.model.menu.MenuPanelSlot;
import org.hcmc.hcplayground.utility.Global;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MenuManager {

    @Expose(serialize = false, deserialize = false)
    private static List<MenuPanel> menuPanels = new ArrayList<>();
    @Expose(serialize = false, deserialize = false)
    private static Plugin plugin = HCPlayground.getInstance();
    @Expose(serialize = false, deserialize = false)
    private static List<String> idList = new ArrayList<>();

    public MenuManager() {

    }

    public static void Load(YamlConfiguration yaml) throws IllegalAccessException {
        CommandMap commandMap = Global.CommandMap;
        idList.clear();
        menuPanels.clear();
        menuPanels = Global.SetItemList(yaml, MenuPanel.class);

        for (MenuPanel menu : menuPanels) {
            idList.add(menu.getId());

            String path = String.format("%s.decorates", menu.getId());
            ConfigurationSection section = yaml.getConfigurationSection(path);
            if (section == null) continue;
            menu.setDecorates(Global.SetItemList(section, MenuPanelSlot.class));
            menu.initialCommand();

            Command c = commandMap.getCommand(menu.getName());
            if (c != null) System.out.println(c.unregister(commandMap));

            commandMap.register(plugin.getName(), menu);
            menu.register(commandMap);
        }
    }

    public static MenuPanel getMenuPanel(@NotNull String id, @NotNull Player player) throws InvalidConfigurationException {
        MenuPanel menu = menuPanels.stream().filter(x -> x.getId().equalsIgnoreCase(id)).findAny().orElse(null);
        if (menu == null) return null;

        menu.setDecorates(setPlaceholders(menu, player));
        return menu;
    }

    public static List<MenuPanelSlot> setPlaceholders(MenuPanel menu, Player player) throws InvalidConfigurationException {
        // 设置标题和每个获取每个PanelSlot实例
        menu.setTitle(menu.getTitle().replace("%player%", player.getName()).replace("%menu_id%", menu.getId()));
        Map<String, MenuPanelSlot> mapSlots = new HashMap<>();
        Map<String, Map<String, MenuPanelSlot>> mapMapSlots = new HashMap<>();
        for (MenuPanelSlot slot : menu.getDecorates()) {
            String[] keys = slot.getId().split("\\.");
            mapSlots.put(keys[1], slot);
        }
        mapMapSlots.put("decorates", mapSlots);

        // 序列化成为Json字符串
        String value = Global.GsonObject.toJson(mapMapSlots).replace('&', '§').replace("%player%", player.getName());
        // set placeholder
        value = PlaceholderAPI.setPlaceholders(player, value);
        // 加载成为Yaml实例
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.loadFromString(value);
        ConfigurationSection section = yaml.getConfigurationSection("decorates");
        if (section == null) return new ArrayList<>();
        // 反序列化
        return Global.SetItemList(section, MenuPanelSlot.class);
    }

    public static List<MenuPanel> getMenuPanels() {
        return menuPanels;
    }

    public static List<String> getIdList() {
        return idList;
    }
}

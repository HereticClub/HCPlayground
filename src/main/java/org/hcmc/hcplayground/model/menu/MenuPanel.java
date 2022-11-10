package org.hcmc.hcplayground.model.menu;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface MenuPanel extends InventoryHolder {

    MenuPanelSlot getDecorate(int slot);

    MenuPanelSlot getDecorate(String name);

    boolean existDecorate(List<Integer> slots);

    List<MenuPanelSlot> getDecorates();

    void setDecorates(List<MenuPanelSlot> decorates);

    String getId();

    void setId(String value);

    int getPage();

    void setPage(int page);

    int getSize();

    String getTitle();

    boolean isDisabled(Player player);

    @NotNull Inventory getInventory();

    void setInventory(Inventory inventory);

    void OnConfigured(YamlConfiguration yaml);

    Inventory OnOpening(Player player, String menuId);

    void OnClosed(Player player, String menuId);
}

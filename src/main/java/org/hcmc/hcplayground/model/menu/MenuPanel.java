package org.hcmc.hcplayground.model.menu;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface MenuPanel extends InventoryHolder {

    MenuPanelSlot getDecorate(int slot);
    boolean existDecorate(List<Integer> slots);
    List<MenuPanelSlot> getDecorates();
    void setDecorates(List<MenuPanelSlot> decorates);
    String getId();
    int getSize();
    @NotNull Inventory getInventory();
    void setInventory(Inventory inventory);
    void open(Player player, String mmoType);
    void close(Player player);
}

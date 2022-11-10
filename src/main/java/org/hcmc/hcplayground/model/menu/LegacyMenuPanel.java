package org.hcmc.hcplayground.model.menu;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class LegacyMenuPanel extends CraftMenuPanel {

    private YamlConfiguration yaml;

    public LegacyMenuPanel() {

    }

    @Override
    public void OnConfigured(YamlConfiguration yaml) {
        this.yaml = yaml;
    }

    @Override
    public Inventory OnOpening(Player player, String mmoType) {
        inventory = switch (inventoryType) {
            case ANVIL -> createAnvilInventory(title);
            case CHEST -> createChestInventory(title, player);
            default -> Bukkit.createInventory(this, inventoryType, title);
        };

        return inventory;
    }
}

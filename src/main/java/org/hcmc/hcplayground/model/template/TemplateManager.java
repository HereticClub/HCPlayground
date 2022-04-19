package org.hcmc.hcplayground.model.template;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.hcmc.hcplayground.utility.Global;

import java.util.ArrayList;
import java.util.List;

public class TemplateManager {

    public static List<TemplateItem> InventoryTemplates = new ArrayList<>();


    public TemplateManager() {

    }

    public static void Load(YamlConfiguration yaml) throws IllegalAccessException {
        InventoryTemplates = Global.SetItemList(yaml, TemplateItem.class);
    }

    public static Inventory CreateInventory(String TemplateId, String title) {
        TemplateItem item = InventoryTemplates.stream().filter(x -> x.id.equalsIgnoreCase(TemplateId)).findAny().orElse(null);
        if (item == null) return Bukkit.createInventory(null, InventoryType.CHEST, title);

        return switch (item.type) {
            case ANVIL -> createAnvilInventory(item, title);
            case CHEST -> createChestInventory(item, title);
            default -> Bukkit.createInventory(null, item.type, item.title);
        };
    }

    private static Inventory createAnvilInventory(TemplateItem item, String newTitle) {
        String title = (newTitle == null || newTitle.equalsIgnoreCase("")) ? item.title : newTitle;

        return Bukkit.createInventory(null, item.type, title);
    }

    private static Inventory createChestInventory(TemplateItem item, String newTitle) {
        String title = (newTitle == null || newTitle.equalsIgnoreCase("")) ? item.title : newTitle;

        Inventory inv = Bukkit.createInventory(null, item.size, title);
        for (TemplateSlot d : item.decorates) {
            ItemStack is = new ItemStack(d.material);
            ItemMeta im = is.getItemMeta();
            if (im == null) continue;

            im.setLore(d.lores);
            im.setDisplayName(d.text);
            is.setItemMeta(im);

            inv.setItem(d.number - 1, is);
        }

        return inv;
    }
}

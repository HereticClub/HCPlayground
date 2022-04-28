package org.hcmc.hcplayground.manager;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.hcmc.hcplayground.enums.RecipeType;
import org.hcmc.hcplayground.model.config.BanConfiguration;
import org.hcmc.hcplayground.utility.Global;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class BanItemManager {

    public static List<BanConfiguration> BanConfigs;

    public BanItemManager() {

    }

    public static void Load(ConfigurationSection section) throws IllegalAccessException {
        BanConfigs = Global.SetItemList(section, BanConfiguration.class);
    }

    public static BanConfiguration getBanItem(RecipeType type) {
        return BanConfigs.stream().filter(x -> x.getType().equals(type)).findAny().orElse(null);
    }

    public static boolean checkEnchantments(ItemStack itemStack, Enchantment[] enchantments) {
        if (itemStack == null) return false;
        Map<Enchantment, Integer> existEnchantments;
        boolean found = false;

        ItemMeta im = itemStack.getItemMeta();
        if (im instanceof EnchantmentStorageMeta storage) {
            existEnchantments = storage.getStoredEnchants();
        } else {
            existEnchantments = itemStack.getEnchantments();
        }

        Set<Enchantment> keys = existEnchantments.keySet();
        for (Enchantment e : enchantments) {
            found = keys.contains(e);
            if (found) break;
        }

        return found;
    }
}

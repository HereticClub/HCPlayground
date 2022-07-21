package org.hcmc.hcplayground.model.enchantment;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.utility.RomanNumber;

import javax.xml.stream.events.Namespace;
import java.util.Arrays;

public class EnchantmentItem {

    private int level;
    private Enchantment item;

    public EnchantmentItem() {
        item = Enchantment.DURABILITY;
        level = 1;
    }

    public EnchantmentItem(Enchantment item, int level) {
        this.item = item;
        this.level = level;
    }

    public EnchantmentItem(String item, int level) {
        Enchantment[] enchantments = Enchantment.values();

        this.item = Arrays.stream(enchantments).filter(x -> x.getKey().getKey().equalsIgnoreCase(item.trim())).findAny().orElse(null);
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public Enchantment getItem() {
        return item;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setItem(Enchantment item) {
        this.item = item;
    }

    public String getName() {
        return item.getKey().getKey().toUpperCase();
    }

    public String toDisplay() {
        try {
            String roman = RomanNumber.fromInteger(level);
            return String.format("%s %s", getName(), roman);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return String.format("%s, %s", getName(), level);
    }
}

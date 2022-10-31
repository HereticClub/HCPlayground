package org.hcmc.hcplayground.model.enchantment;

import org.bukkit.enchantments.Enchantment;
import org.hcmc.hcplayground.utility.RomanNumber;

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

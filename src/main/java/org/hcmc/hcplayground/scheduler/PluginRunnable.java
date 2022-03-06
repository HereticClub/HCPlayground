package org.hcmc.hcplayground.scheduler;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.Collection;

public class PluginRunnable extends BukkitRunnable {

    private Player player;
    private PotionEffect[] effects;

    public PluginRunnable() {

    }

    public void setPotionEffects(PotionEffect[] effects) {
        this.effects = effects;
    }

    @Override
    public void run() {

    }

    private void setPlayerPotionEffects() {
        Collection<PotionEffect> potions = player.getActivePotionEffects();
        if (effects == null) {
            removePlayerPotionEffects();
        } else {
            player.addPotionEffects(Arrays.asList(effects));
        }
    }

    private void removePlayerPotionEffects() {
        Collection<PotionEffect> potions = player.getActivePotionEffects();
        for (PotionEffect pe : potions) {
            player.removePotionEffect(pe.getType());
        }
    }
}

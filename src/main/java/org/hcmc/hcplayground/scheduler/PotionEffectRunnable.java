package org.hcmc.hcplayground.scheduler;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.Collection;

public class PotionEffectRunnable extends BukkitRunnable {

    private final Player player;
    private PotionEffect[] effects;

    public PotionEffectRunnable(Player player) {
        this.player = player;
    }

    public void setPotionEffects(PotionEffect[] effects) {
        this.effects = effects;
    }

    @Override
    public void run() {
        setPlayerPotionEffects();
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

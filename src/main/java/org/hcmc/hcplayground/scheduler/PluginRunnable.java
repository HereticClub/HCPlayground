package org.hcmc.hcplayground.scheduler;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.model.Global;
import org.hcmc.hcplayground.playerManager.PlayerData;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;

public class PluginRunnable extends BukkitRunnable {

    private PotionEffect[] effects;
    private JavaPlugin plugin = HCPlayground.getPlugin();
    private boolean playerExist = true;

    public PluginRunnable() {

    }

    public void setPotionEffects(PotionEffect[] effects) {
        this.effects = effects;
    }

    @Override
    public void run() {
        doBukkitTask();
    }

    private void doBukkitTask() {
        for (PlayerData pd : Global.playerMap.values()) {
            if (pd.getLogin()) continue;
            boolean register = pd.getRegister();

            if (register) {
                doRemindLogin(pd);
            } else {
                doRemindRegister(pd);
            }
        }
    }

    private void doRemindRegister(PlayerData pd) {
        LocalDateTime loginDTTM = pd.getLoginDTTM();
        Player player = plugin.getServer().getPlayer(pd.getUuid());
        if(player == null) return;

        player.sendMessage("Register");
    }

    private void doRemindLogin(PlayerData pd) {
        LocalDateTime loginDTTM = pd.getLoginDTTM();
        Player player = plugin.getServer().getPlayer(pd.getUuid());
        if(player == null) return;


        player.sendMessage("Login");
    }

    private void setPlayerPotionEffects(PlayerData pd) {
        Player player = plugin.getServer().getPlayer(pd.getUuid());
        if(player == null) return;

        Collection<PotionEffect> potions = player.getActivePotionEffects();
        if (effects == null) {
            removePlayerPotionEffects(player);
        } else {
            player.addPotionEffects(Arrays.asList(effects));
        }
    }

    private void removePlayerPotionEffects(Player player) {

        Collection<PotionEffect> potions = player.getActivePotionEffects();
        for (PotionEffect pe : potions) {
            player.removePotionEffect(pe.getType());
        }
    }
}

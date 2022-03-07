package org.hcmc.hcplayground.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.localization.Localization;
import org.hcmc.hcplayground.model.Global;
import org.hcmc.hcplayground.playerManager.PlayerData;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

public class PluginRunnable extends BukkitRunnable {

    private PotionEffect[] effects;
    private final JavaPlugin plugin = HCPlayground.getPlugin();

    public PluginRunnable() {

    }

    @Override
    public void run() {
        doBukkitTask();
    }

    private void doBukkitTask() {
        for (PlayerData pd : Global.playerMap.values()) {
            boolean isLogin = pd.getLogin();
            boolean register = pd.getRegister();

            if (register && !isLogin) doRemindLogin(pd);
            if (!register && !isLogin) doRemindRegister(pd);
        }
    }

    private void doRemindRegister(PlayerData pd) {
        long currentSeconds = new Date().getTime() / 1000;
        long loginSeconds = pd.getLoginDTTM().getTime() / 1000;

        if (pd.remindCheckpoint == 0) pd.remindCheckpoint = currentSeconds;

        Player player = plugin.getServer().getPlayer(pd.getUuid());
        if (player == null) return;

        if (currentSeconds - pd.remindCheckpoint >= Global.authme.remainInterval) {
            long remain = Global.authme.timeout - (currentSeconds - loginSeconds);
            player.sendMessage(Localization.Messages.get("playerRegisterRemind").replace("%remain%", String.valueOf(remain)));
            pd.remindCheckpoint = currentSeconds;
        }
        if (currentSeconds - loginSeconds >= Global.authme.timeout) {
            player.kickPlayer(Localization.Messages.get("playerRegisterTimeout").replace("%player%", player.getName()));
        }
    }

    private void doRemindLogin(PlayerData pd) {

        long currentSeconds = new Date().getTime() / 1000;

        if (pd.remindCheckpoint == 0) pd.remindCheckpoint = currentSeconds;

        Player player = plugin.getServer().getPlayer(pd.getUuid());
        if(player == null) return;

        if (currentSeconds - pd.remindCheckpoint >= 5) {
            player.sendMessage("Login please");
            pd.remindCheckpoint = currentSeconds;
        }
    }

    public void setPotionEffects(PotionEffect[] effects) {
        this.effects = effects;
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

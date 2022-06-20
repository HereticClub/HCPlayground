package org.hcmc.hcplayground.listener;

import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

public class HologramListener implements PluginMessageListener {

    public HologramListener() {

    }

    @Override
    public void onPluginMessageReceived(@NotNull String s, @NotNull Player player, @NotNull byte[] bytes) {
        String d = String.format("PluginMessageReceived: %s -> %s", player.getName(), s);
        System.out.println(d);
    }
}

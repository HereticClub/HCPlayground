package org.hcmc.hcplayground.runnable;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.hcmc.hcplayground.enums.CcmdActionType;
import org.hcmc.hcplayground.model.ccmd.CcmdAction;

import java.util.Date;
import java.util.List;

public class CcmdActionRunnable extends BukkitRunnable {

    private final List<CcmdAction> actions;
    private final Player player;
    private int index;
    private final int count;
    private boolean waiting = false;

    private Date lastTime;

    public CcmdActionRunnable(Player player, List<CcmdAction> actions) {
        this.actions = actions;
        this.player = player;
        count = actions.size();
    }

    @Override
    public void run() {
        if (index >= count) {
            this.cancel();
            return;
        }

        CcmdAction action = actions.get(index);
        if (action.getType().equals(CcmdActionType.Wait) && !waiting) {
            lastTime = new Date();
        }
        perform(action);
    }

    private void perform(CcmdAction action) {
        switch (action.getType()) {
            case Wait -> {
                waiting = true;
                Date current = new Date();
                long diff = current.getTime() - lastTime.getTime();
                if (diff >= action.getDuration() * 1000) {
                    waiting = false;
                    index++;
                }
            }
            case Message -> {
                index++;
                waiting = false;

                player.sendMessage(action.getText());
                player.playSound(player.getLocation(), action.getSound(), 10, 1);
            }
            case Teleport -> {
                index++;
                waiting = false;

                Location l = action.getLocation();
                if (l.getWorld() == null) break;
                player.teleport(action.getLocation());
            }
        }
    }
}

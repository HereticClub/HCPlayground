package org.hcmc.hcplayground.scheduler;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.itemManager.IItemBase;
import org.hcmc.hcplayground.itemManager.ItemManager;
import org.hcmc.hcplayground.localization.Localization;
import org.hcmc.hcplayground.model.Global;
import org.hcmc.hcplayground.playerManager.PlayerData;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class PluginRunnable extends BukkitRunnable {

    private PotionEffect[] effects;
    private final JavaPlugin plugin;

    // 自1970年1月1日开始，至今的总秒数
    private long totalSeconds;


    public PluginRunnable() {
        plugin = HCPlayground.getPlugin();
    }

    @Override
    public void run() {
        try {
            doBukkitTask();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void doBukkitTask() throws NoSuchFieldException, IllegalAccessException {
        for (PlayerData pd : Global.playerMap.values()) {
            // 每秒更新自1970年1月1日开始至今的总秒数
            totalSeconds = new Date().getTime() / 1000;
            // 初始化每玩家的检查点时间
            if (pd.CheckpointTime == 0) pd.CheckpointTime = totalSeconds;

            doRemindLogin(pd);
            doPotionEffect(pd);
        }
    }

    /**
     * 检测和激活玩家身上装备的药水效果
     *
     * @param pd
     * @throws IllegalAccessException
     */
    private void doPotionEffect(PlayerData pd) throws IllegalAccessException {
        int interval = (int) (totalSeconds - pd.CheckpointTime) % Global.offhandPotionEffect.refreshInterval + 1;
        if (interval < Global.offhandPotionEffect.refreshInterval) return;

        Player player = plugin.getServer().getPlayer(pd.getUuid());
        if (player == null) return;

        List<ItemStack> items = new ArrayList<>(Arrays.stream(player.getInventory().getArmorContents()).toList());
        items.add(player.getInventory().getItemInMainHand());
        items.add(player.getInventory().getItemInOffHand());

        for (ItemStack is : items) {
            if (is == null) continue;
            ItemMeta im = is.getItemMeta();
            if (im == null) continue;

            PersistentDataContainer mainContainer = im.getPersistentDataContainer();
            NamespacedKey mainKey = new NamespacedKey(plugin, Global.PERSISTENT_MAIN_KEY);
            String id = mainContainer.get(mainKey, PersistentDataType.STRING);
            if (id == null) continue;

            IItemBase itemX = ItemManager.FindItemById(id);
            if (itemX == null) continue;

            Field[] fields = itemX.getClass().getFields();
            Field field = Arrays.stream(fields).filter(x -> x.getName().equalsIgnoreCase("potions")).findAny().orElse(null);
            if (field == null) continue;
            Object obj = field.get(itemX);
            if (!(obj instanceof PotionEffect[] potions)) continue;

            player.addPotionEffects(Arrays.asList(potions));
        }
    }

    private void doRemindLogin(PlayerData pd) {
        // 自玩家登录时间开始，至今的总秒数
        long loginSeconds = pd.getLoginDTTM().getTime() / 1000;
        // 获取玩家是否已经登录
        boolean isLogin = pd.getLogin();
        // 获取玩家是否已经注册
        boolean isRegister = pd.getRegister();
        // 如果玩家已经登录，则不需要再提醒登录操作
        if (isLogin) return;

        Player player = plugin.getServer().getPlayer(pd.getUuid());
        if (player == null) return;

        int interval = (int) (totalSeconds - pd.CheckpointTime) % Global.authme.remainInterval + 1;
        if (interval >= Global.authme.remainInterval) {
            long remain = Global.authme.timeout - (totalSeconds - loginSeconds);
            if (!isRegister) {
                player.sendMessage(Localization.Messages.get("playerRegisterRemind").replace("%remain%", String.valueOf(remain)));
            } else {
                player.sendMessage(Localization.Messages.get("playerLoginRemind").replace("%remain%", String.valueOf(remain)));
            }
        }
        if (totalSeconds - loginSeconds >= Global.authme.timeout) {
            if (!isRegister) {
                player.kickPlayer(Localization.Messages.get("playerRegisterTimeout").replace("%player%", player.getName()));
            } else {
                player.kickPlayer(Localization.Messages.get("playerLoginTimeout").replace("%player%", player.getName()));
            }
        }

        Global.playerMap.put(pd.getUuid(), pd);
    }
}

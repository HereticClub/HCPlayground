package org.hcmc.hcplayground.scheduler;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.manager.*;
import org.hcmc.hcplayground.model.item.ItemBase;
import org.hcmc.hcplayground.model.player.PlayerData;
import org.hcmc.hcplayground.utility.Global;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class PluginRunnable extends BukkitRunnable {

    private PotionEffect[] effects;
    private final JavaPlugin plugin;

    // 上一次发送随机公告的时间
    private Date lastBroadcastTime = new Date();
    private Date lastClearLagTime = new Date();

    // 自1970年1月1日开始，至今的总秒数
    private long totalSeconds;


    public PluginRunnable() {
        plugin = HCPlayground.getPlugin();
    }

    @Override
    public void run() {
        try {
            doOnlinePlayerTask();
            doBroadcastTask();
            doClearLag();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void doClearLag() {
        long delta = new Date().getTime() / 1000 - lastClearLagTime.getTime() / 1000;
        int remainSecond = ClearLagManager.Interval - (int) delta;
        int clearedItem;
        String msg = ClearLagManager.Remind.get(remainSecond);
        if (msg != null) {
            plugin.getServer().broadcastMessage(msg.replace("%remain%", String.valueOf(remainSecond)));
        }

        if (delta <= ClearLagManager.Interval) return;
        lastClearLagTime = new Date();
        clearedItem = 0;

        List<World> worlds = plugin.getServer().getWorlds();
        for (World w : worlds) {
            List<Entity> entities = w.getEntities();
            List<Entity> dropped = entities.stream().filter(x -> ClearLagManager.Types.contains(x.getType())).toList();
            for (Entity item : dropped) {
                item.remove();
                clearedItem++;
            }
        }
        plugin.getServer().broadcastMessage(ClearLagManager.ClearMessage.replace("%removed%", String.valueOf(clearedItem)));
    }

    private void doBroadcastTask() {
        long delta = new Date().getTime() / 1000 - lastBroadcastTime.getTime() / 1000;
        if (delta <= BroadcastManager.Interval) return;

        lastBroadcastTime = new Date();
        List<String> msg = BroadcastManager.RandomMessage();
        for (String s : msg) {
            plugin.getServer().broadcastMessage(s);
        }
    }

    private void doOnlinePlayerTask() throws NoSuchFieldException, IllegalAccessException {
        // 获取所有在线玩家实例
        Player[] players = plugin.getServer().getOnlinePlayers().toArray(new Player[0]);
        // 每秒更新自1970年1月1日开始至今的总秒数
        totalSeconds = new Date().getTime() / 1000;
        for (Player player : players) {
            PlayerData pd = PlayerManager.getPlayerData(player);

            doRemindLogin(pd);
            doPotionEffect(pd);
            doShowActionBar(pd);
        }
    }

    private void doShowActionBar(PlayerData pd) {
        int interval = (int) (totalSeconds - pd.loginTimeStamp) % Global.potion.refreshInterval + 1;
        if (interval < 2) return;

        double maxHealth = pd.getMaxHealth();
        double currentHealth = pd.getCurrentHealth();
        double totalArmor = pd.getTotalArmor();
        double totalAttackDamage = pd.getTotalAttackDamage();
        double totalCritical = pd.getTotalCritical();
        double totalCriticalDamage = pd.getTotalCriticalDamage();


        //String value = String.format("§c生命: §e%.1f§7/§e%.1f §b护甲: §e%.1f §a攻击: §e%.1f §6暴击: §e%.1f%% §5爆伤: §e%.1f%%", currentHealth, maxHealth, totalArmor, totalAttackDamage, totalCritical * 100, totalCriticalDamage * 100);

        Player player = pd.getPlayer();
        String value = LocalizationManager.getMessage("playerActionBar", player);
        BaseComponent baseComponent = new TextComponent(value);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, baseComponent);
    }

    /**
     * 检测和激活玩家身上装备的药水效果
     *
     * @param pd
     * @throws IllegalAccessException
     */
    private void doPotionEffect(PlayerData pd) throws IllegalAccessException {
        int interval = (int) (totalSeconds - pd.loginTimeStamp) % Global.potion.refreshInterval + 1;
        if (interval < Global.potion.refreshInterval) return;

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
            NamespacedKey mainKey = new NamespacedKey(plugin, ItemBase.PERSISTENT_MAIN_KEY);
            String id = mainContainer.get(mainKey, PersistentDataType.STRING);
            if (id == null) continue;

            ItemBase itemX = ItemManager.FindItemById(id);
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
        long loginSeconds = pd.getLoginTime().getTime() / 1000;
        // 获取玩家是否已经登录
        boolean isLogin = pd.getLogin();
        // 获取玩家是否已经注册
        boolean isRegister = pd.getRegister();
        // 如果玩家已经登录，则不需要再提醒登录操作
        if (isLogin) return;

        Player player = plugin.getServer().getPlayer(pd.getUuid());
        if (player == null) return;

        int interval = (int) (totalSeconds - pd.loginTimeStamp) % Global.authme.remainInterval + 1;
        if (interval >= Global.authme.remainInterval) {
            long remain = Global.authme.timeout - (totalSeconds - loginSeconds);
            if (!isRegister) {
                player.sendMessage(LocalizationManager.getMessage("playerRegisterRemind", player).replace("%remain%", String.valueOf(remain)));
            } else {
                player.sendMessage(LocalizationManager.getMessage("playerLoginRemind", player).replace("%remain%", String.valueOf(remain)));
            }
        }
        if (totalSeconds - loginSeconds >= Global.authme.timeout) {
            if (!isRegister) {
                player.kickPlayer(LocalizationManager.getMessage("playerRegisterTimeout", player).replace("%player%", player.getName()));
            } else {
                player.kickPlayer(LocalizationManager.getMessage("playerLoginTimeout", player).replace("%player%", player.getName()));
            }
        }
    }
}

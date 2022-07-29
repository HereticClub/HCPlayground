package org.hcmc.hcplayground.runnable;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
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
import org.hcmc.hcplayground.event.WorldMorningEvent;
import org.hcmc.hcplayground.manager.*;
import org.hcmc.hcplayground.model.item.ItemBase;
import org.hcmc.hcplayground.model.minion.MinionEntity;
import org.hcmc.hcplayground.model.minion.MinionTemplate;
import org.hcmc.hcplayground.model.player.PlayerData;
import org.hcmc.hcplayground.utility.Global;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

public class PluginRunnable extends BukkitRunnable {

    private static final int CPU_TIMER = 100;
    private final JavaPlugin plugin;
    // 上一次发送随机公告的时间
    private Date lastBroadcastTime = new Date();
    private Date lastClearLagTime = new Date();

    // 自1970年1月1日开始，至今的总秒数
    private long totalSeconds;


    public PluginRunnable() {
        plugin = HCPlayground.getInstance();
    }

    // 因为该runnable每100毫秒执行一次
    // 因此只需要判断: 当前秒数(毫秒) % 间隔时间秒数(毫秒) <= 100
    // 间隔时间秒数(毫秒)必须大于100
    @Override
    public void run() {
        try {
            // 每1秒钟求余
            double delta1s = new Date().getTime() % 1000;
            // 每3秒钟求余
            double delta5s = new Date().getTime() % (MinionManager.DRESSING_PERIOD * 1000);
            // 每10分钟求余
            double delta10m = new Date().getTime() % (RecordManager.ARCHIVE_PERIOD * 1000);

            // 以下方法每100毫秒运行1次
            doMinionAcquire();
            // 以下方法每秒运行1次
            if (delta1s <= CPU_TIMER) {
                doOnlinePlayerTask();
                doBroadcastTask();
                doClearLag();
                doTrackWorldTime();
            }
            // 以下方法每5秒运行1次
            if (delta5s <= CPU_TIMER) {
                doMinionDressingPlatform();
            }
            // 以下方法每10分钟运行1次
            if (delta10m <= CPU_TIMER) {
                doRecordArchive();
            }
        } catch (NoSuchFieldException | IllegalAccessException | InvalidConfigurationException | IOException e) {
            e.printStackTrace();
        }
    }

    private void doRecordArchive() throws IOException {
        RecordManager.Save();
    }

    private void doMinionAcquire() {
        List<MinionEntity> minions = RecordManager.getMinionRecords();
        for (MinionEntity entity : minions) {
            if (entity.getType() == null) continue;

            long diff = new Date().getTime() / 1000 - entity.getLastAcquireTime().getTime() / 1000;
            MinionTemplate template = MinionManager.getMinionTemplate(entity.getType(), entity.getLevel());
            if (template == null || diff <= template.getPeriod()) continue;

            new MinionAcquireRunnable(entity, template).runTaskLater(plugin, 10);
            entity.setLastAcquireTime(new Date());
        }
    }

    @Override
    public boolean isCancelled() {
        return super.isCancelled();
    }

    @Override
    public void cancel() {
        super.cancel();
    }

    private void doMinionDressingPlatform() {
        for (MinionEntity minion : RecordManager.getMinionRecords()) {
            minion.dressingPlatform();
        }
    }

    private void doTrackWorldTime() {
        List<World> worlds = Bukkit.getWorlds();
        for (World w : worlds) {
            long gameTime = w.getTime();
            if (gameTime <= 19) {
                WorldMorningEvent event = new WorldMorningEvent(w, gameTime);
                Bukkit.getPluginManager().callEvent(event);
            }

            /* DO NO DELETE, World time algorithm
            long hour = gameTime / 1000 + 6;
            long minute = gameTime % 1000 * 60 / 1000;
             */
        }
    }

    private void doClearLag() {
        long delta = new Date().getTime() / 1000 - lastClearLagTime.getTime() / 1000;
        int remainSecond = ClearLagManager.Interval - (int) delta;
        int clearedCount;
        String msg = ClearLagManager.Remind.get(remainSecond);
        if (msg != null) {
            plugin.getServer().broadcastMessage(msg.replace("%remain%", String.valueOf(remainSecond)));
        }

        if (delta <= ClearLagManager.Interval) return;
        lastClearLagTime = new Date();
        clearedCount = 0;

        List<World> worlds = plugin.getServer().getWorlds();
        for (World w : worlds) {
            List<Entity> entities = w.getEntities();
            List<Entity> dropped = entities.stream().filter(x -> ClearLagManager.Types.contains(x.getType())).toList();
            clearedCount += dropped.size();
            for (Entity item : dropped) {
                item.remove();
            }

        }
        plugin.getServer().broadcastMessage(ClearLagManager.ClearMessage.replace("%removed%", String.valueOf(clearedCount)));
    }

    private void doBroadcastTask() {
        long delta = new Date().getTime() / 1000 - lastBroadcastTime.getTime() / 1000;
        if (delta <= BroadcastManager.Interval) return;

        lastBroadcastTime = new Date();
        List<String> msg = BroadcastManager.randomMessage();
        for (String s : msg) {
            plugin.getServer().broadcastMessage(s);
        }
    }

    private void doOnlinePlayerTask() throws NoSuchFieldException, IllegalAccessException, IOException, InvalidConfigurationException {
        // 获取所有在线玩家实例
        Player[] players = plugin.getServer().getOnlinePlayers().toArray(new Player[0]);
        // 每秒更新自1970年1月1日开始至今的总秒数
        totalSeconds = new Date().getTime() / 1000;
        for (Player player : players) {
            PlayerData data = PlayerManager.getPlayerData(player);

            doRemindLogin(data);
            doPotionEffect(data);
            doShowActionBar(data);
            doUpdateSidebar(data);
        }
    }

    /*
    TODO: Hologram, 需要实施漂浮字体内容更新功能
    private void doUpdateHologram(Player player) {
        List<HologramItem> holograms = HologramManager.getHolograms();
        for (HologramItem holo : holograms) {
            holo.update(player);
        }
    }
     */

    private void doUpdateSidebar(PlayerData data) {
        data.UpdateSidebar();
    }

    private void doShowActionBar(PlayerData pd) {
        int interval = (int) (totalSeconds - pd.loginTimeStamp) % Global.potion.refreshInterval + 1;
        if (interval < 2) return;

        Player player = pd.getPlayer();
        String value = LanguageManager.getString("playerActionBar", player);
        BaseComponent baseComponent = new TextComponent(value);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, baseComponent);
    }

    /**
     * 检测和激活玩家身上装备的药水效果
     */
    private void doPotionEffect(PlayerData pd) {
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

            ItemBase itemX = ItemManager.findItemById(id);
            if (itemX == null) continue;
            player.addPotionEffects(itemX.getPotions());
        }
    }

    private void doRemindLogin(PlayerData pd) {
        // 自玩家登录时间开始，至今的总秒数
        long loginSeconds = pd.getLoginTime().getTime() / 1000;
        // 获取玩家是否已经登录
        boolean isLogin = pd.isLogin();
        // 获取玩家是否已经注册
        boolean isRegister = pd.getRegister();
        // 如果玩家已经登录，则不需要再提醒登录操作
        if (isLogin) return;

        Player player = pd.getPlayer();
        int interval = (int) (totalSeconds - pd.loginTimeStamp) % Global.authme.remainInterval + 1;
        if (interval >= Global.authme.remainInterval) {
            long remain = Global.authme.timeout - (totalSeconds - loginSeconds);
            if (!isRegister) {
                player.sendMessage(LanguageManager.getString("playerRegisterRemind", player).replace("%remain%", String.valueOf(remain)));
            } else {
                player.sendMessage(LanguageManager.getString("playerLoginRemind", player).replace("%remain%", String.valueOf(remain)));
            }
        }
        if (totalSeconds - loginSeconds >= Global.authme.timeout) {
            if (!isRegister) {
                player.kickPlayer(LanguageManager.getString("playerRegisterTimeout", player).replace("%player%", player.getName()));
            } else {
                player.kickPlayer(LanguageManager.getString("playerLoginTimeout", player).replace("%player%", player.getName()));
            }
        }
    }
}

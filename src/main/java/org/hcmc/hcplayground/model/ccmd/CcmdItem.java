package org.hcmc.hcplayground.model.ccmd;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.manager.LanguageManager;
import org.hcmc.hcplayground.manager.PlayerManager;
import org.hcmc.hcplayground.model.player.PlayerData;
import org.hcmc.hcplayground.runnable.CcmdActionRunnable;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

public class CcmdItem extends Command {

    private Plugin plugin = HCPlayground.getInstance();
    public String id;

    @Expose
    @SerializedName(value = "permission")
    public String permission;
    @Expose
    @SerializedName(value = "usage")
    public String usage;
    @Expose
    @SerializedName(value = "cooldown")
    public int cooldown;
    @Expose
    @SerializedName(value = "aliases")
    public List<String> aliases = new ArrayList<>();
    @Expose
    @SerializedName(value = "args")
    public List<String> args = new ArrayList<>();
    @Expose
    @SerializedName(value = "worlds")
    public List<String> worlds = new ArrayList<>();

    public List<CcmdAction> actions = new ArrayList<>();

    public CcmdItem(@NotNull String name) {
        super(name);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        try {
            return RunCustomCommand(sender, args);
        } catch (IOException | IllegalAccessException | InvalidConfigurationException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 向Bukkit核心注册命令
     */
    public void Enroll(@NotNull CommandMap commandMap) {
        if (plugin == null) plugin = HCPlayground.getInstance();

        setCommandMessage();
        commandMap.register(plugin.getName(), this);
    }

    private boolean RunCustomCommand(CommandSender sender, @NotNull String[] args) throws IOException, IllegalAccessException, InvalidConfigurationException, ParseException {
        // 自定义命令必须玩家执行
        if (!(sender instanceof Player player)) {
            sender.sendMessage(LanguageManager.getString("player-message", sender).replace("%command%", id));
            return false;
        }
        // 检测指令的可用世界列表，op玩家绕过检测
        if (worlds.size() >= 1 && !player.isOp()) {
            String w = player.getWorld().getName();
            if (worlds.stream().noneMatch(x -> x.equalsIgnoreCase(w))) return false;
        }
        if (!player.isOp() && !this.testPermission(sender)) return false;
        PlayerData data = PlayerManager.getPlayerData(player);

        double lastTime = data.CcmdCooldownList.containsKey(id) ? data.CcmdCooldownList.get(id) : new Date(0).getTime();
        double current = new Date().getTime();
        double diff = current - lastTime;
        if (diff <= cooldown * 1000L) {
            int remain = (int) ((cooldown * 1000L - diff) / 1000);
            player.sendMessage(LanguageManager.getString("customCommandCooldown", player).replace("%command%", id).replace("%remain%", String.valueOf(remain)));
            return false;
        }
        // 执行actions
        CcmdActionRunnable r = new CcmdActionRunnable(player, actions);
        r.runTaskTimer(plugin, 0, 2);

        lastTime = new Date().getTime();
        data.CcmdCooldownList.put(id, lastTime);
        PlayerManager.setPlayerData(player, data);
        return true;
    }

    private void setCommandMessage() {
        // 以下两个属性必须设置为指令的名称
        this.setLabel(this.id);
        this.setName(this.id);
        // 以下所有属性值都不能为null
        if (aliases == null) aliases = new ArrayList<>();
        if (args == null) args = new ArrayList<>();
        if (worlds == null) worlds = new ArrayList<>();

        if (!StringUtils.isBlank(this.permission)) this.setPermission(this.permission);
        this.setAliases(this.aliases);
        this.setPermissionMessage(LanguageManager.getString("permission-message", null).replace("%permission%", permission));
        this.setUsage(this.usage);
        this.setDescription(this.description);
    }
}

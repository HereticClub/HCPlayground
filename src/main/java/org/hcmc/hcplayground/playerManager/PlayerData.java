package org.hcmc.hcplayground.playerManager;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.model.Global;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerData {
    /*
    BreakList - 玩家破坏方块的数据
    PlaceList - 玩家放置方块的数据
    Key:
    如果是普通方块比如麦子等
    则保存保存该方块的Material
    如果含有PersistentData，则保存其Id
    Value:
    破快或放置该方块的总数量
    */
    private static final String Section_Key_BreakList = "breakList";
    private static final String Section_Key_PlaceList = "placeList";

    @SerializedName(value = "breakList")
    @Expose
    public Map<Material, Integer> BreakList = new HashMap<>();
    @Expose
    @SerializedName(value = "placeList")
    public Map<Material, Integer> PlaceList = new HashMap<>();

    @Expose(serialize = false, deserialize = false)
    private final Player player;
    @Expose(serialize = false, deserialize = false)
    private final JavaPlugin plugin = HCPlayground.getPlugin();

    @Expose(serialize = false, deserialize = false)
    private final UUID uuid;
    @Expose(serialize = false, deserialize = false)
    private final String name;
    @Expose(serialize = false, deserialize = false)
    private boolean isLogin;
    @Expose(serialize = false, deserialize = false)
    private boolean isRegister;
    @Expose(serialize = false, deserialize = false)
    private LocalDateTime loginDTTM;
    /*
    @Expose(serialize = false, deserialize = false)
    public PotionEffectRunnable PotionTimer;
    @Expose(serialize = false, deserialize = false)
    public BukkitTask PotionTask;

     */

    //private boolean isScheduled = false;

    public PlayerData(Player player) {
        this.player = player;
        //this.PotionTimer = new PotionEffectRunnable(player);

        name = player.getName();
        uuid = player.getUniqueId();
    }

    public boolean getLogin() {
        return isLogin;
    }

    public void setLogin(boolean value) {
        isLogin = value;
    }

    public LocalDateTime getLoginDTTM() {
        return loginDTTM;
    }

    public void setLoginDTTM(LocalDateTime value) {
        loginDTTM = value;
    }

    public boolean getRegister() {
        return isRegister;
    }

    public void setRegister(boolean value) {
        isRegister = value;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public boolean checkLogin() {

        return false;
    }

    /*
    public void RunPotionTimer(JavaPlugin plugin, long delay, long period) {
        if (isScheduled) return;
        this.PotionTask = PotionTimer.runTaskTimer(plugin, delay, period);
        isScheduled = true;
    }

    public void CancelPotionTimer() {
        if (!isScheduled) return;
        if (PotionTask != null) PotionTask.cancel();
        if (PotionTimer != null) PotionTimer.cancel();
        isScheduled = false;
    }

     */

    public YamlConfiguration toYaml() {
        YamlConfiguration yaml = new YamlConfiguration();

        yaml.createSection("breakList", BreakList);
        yaml.createSection("placeList", PlaceList);

        return yaml;
    }

    public void LoadConfig() {
        UUID playerUuid = player.getUniqueId();
        ConfigurationSection breakSection, placeSection;
        String sectionValue;
        Type mapType = new TypeToken<Map<Material, Integer>>() {
        }.getType();
        File f = new File(plugin.getDataFolder(), String.format("profile/%s.yml", playerUuid));

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(f);
        breakSection = yaml.getConfigurationSection(Section_Key_BreakList);
        if (breakSection != null) {
            sectionValue = Global.GsonObject.toJson(breakSection.getValues(false));
            BreakList = Global.GsonObject.fromJson(sectionValue, mapType);
        }
        placeSection = yaml.getConfigurationSection(Section_Key_PlaceList);
        if (placeSection != null) {
            sectionValue = Global.GsonObject.toJson(placeSection.getValues(false));
            PlaceList = Global.GsonObject.fromJson(sectionValue, mapType);
        }
    }

    public void SaveConfig() throws IOException {
        UUID playerUuid = player.getUniqueId();
        File f = new File(plugin.getDataFolder(), String.format("profile/%s.yml", playerUuid));

        toYaml().save(f);

    }
}

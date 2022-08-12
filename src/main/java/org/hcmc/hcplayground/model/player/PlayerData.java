package org.hcmc.hcplayground.model.player;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.enums.ItemFeatureType;
import org.hcmc.hcplayground.enums.PlayerBannedState;
import org.hcmc.hcplayground.manager.ItemManager;
import org.hcmc.hcplayground.manager.LanguageManager;
import org.hcmc.hcplayground.manager.MMOSkillManager;
import org.hcmc.hcplayground.manager.SidebarManager;
import org.hcmc.hcplayground.model.item.ItemBase;
import org.hcmc.hcplayground.model.item.Join;
import org.hcmc.hcplayground.model.scoreboard.ScoreboardItem;
import org.hcmc.hcplayground.sqlite.SqliteManager;
import org.hcmc.hcplayground.sqlite.table.BanPlayerDetail;
import org.hcmc.hcplayground.utility.Global;
import org.jetbrains.annotations.NotNull;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.*;

public class PlayerData {
    private static final String Section_Key_CcmdCooldownList = "ccmdCooldownList";
    private static final String Section_Key_Parkour_Design = "parkour.design";
    private static final String Section_Key_Parkour_List = "parkour.list";
    private static final String TYPE_JAVA_UTIL_MAP = "java.util.Map";
    public static final String ECONOMY_BALANCE_KEY = "balance";
    public static final double BASE_HEALTH = 20.0F;
    public static final double BASE_MAX_HEALTH = 20.0F;
    public static final double BASE_CRITICAL = 0.05F;
    public static final double BASE_CRITICAL_DAMAGE = 1.5F;
    public static final double BASE_ARMOR = 0;
    public static final double BASE_ATTACK_REACH = 0;
    public static final double BASE_BLOOD_SUCKING = 0;
    public static final double BASE_RECOVER = 0;
    public static final double BASE_INTELLIGENCE = 0;
    public static final double BASE_DIGGING_SPEED = 0;
    public static final double BASE_LOGGING_SPEED = 0;
    /**
     * 玩家档案文档的各种记录名称列表
     */
    private final String[] SectionKeys = new String[]{
            Section_Key_CcmdCooldownList,
    };

    /**
     * Custom Command with cooldown-able
     */
    @Expose
    @SerializedName(value = Section_Key_CcmdCooldownList)
    public Map<String, Double> CcmdCooldownList = new HashMap<>();
    @Expose
    @SerializedName(value = Section_Key_Parkour_Design)
    public boolean isCourseDesigning = false;
    @Expose
    @SerializedName(value = Section_Key_Parkour_List)
    public List<String> courseIds = new ArrayList<>();
    /**
     * 玩家的背包和装备物品的记录
     */
    public CourseDesigner designer;
    /**
     * 玩家在runnable线程的时间检查点，初始化为登陆时间
     * 通常不会更改这个属性的值
     */
    public long loginTimeStamp = 0;
    /**
     * 本插件实例
     */
    private final JavaPlugin plugin = HCPlayground.getInstance();
    /**
     * 记录玩家登陆时的游戏模式
     */
    private GameMode gameMode;
    /**
     * 玩家的Player实例
     */
    private final Player player;
    /**
     * 玩家的OfflinePlayer实例
     */
    private final OfflinePlayer offline;
    /**
     * 玩家的UUID
     */
    private final UUID uuid;
    /**
     * 玩家名称
     */
    private final String name;
    /**
     * 玩家是否已经使用/login指令登陆到服务器
     */
    private boolean login;
    /**
     * 玩家是否已经使用/register指令注册到服务器
     */
    private boolean register;
    private final PermissionAttachment attachment;
    private Date loginTime = new Date();
    private ScoreboardItem sidebar;
    private double totalArmor = BASE_ARMOR;
    private double totalAttackReach = BASE_ATTACK_REACH;
    private double totalBloodSucking = BASE_BLOOD_SUCKING;
    private double totalCritical = BASE_CRITICAL;
    private double totalCriticalDamage = BASE_CRITICAL_DAMAGE;
    private double maxHealth = BASE_MAX_HEALTH;
    private double totalRecover = BASE_RECOVER;
    private double totalIntelligence = BASE_INTELLIGENCE;
    private double totalDiggingSpeed = BASE_DIGGING_SPEED;
    private double totalLoggingSpeed = BASE_LOGGING_SPEED;

    public PlayerData(Player player) {
        this.player = player;

        name = player.getName();
        uuid = player.getUniqueId();
        gameMode = player.getGameMode();
        attachment = player.addAttachment(plugin);
        designer = new CourseDesigner(this);
        offline = Bukkit.getOfflinePlayer(uuid);

        LoadConfig();
    }

    public PermissionAttachment getAttachment() {
        return attachment;
    }

    /**
     * 获取当前玩家的金钱
     * @return 玩家拥有的金钱
     */
    public double getBalance() {
        return Global.economyApi.getBalance(offline);
    }

    /**
     * 给予玩家的钱
     * @param money 给予玩家钱的数量
     */
    public void deposit(double money) {
        EconomyResponse response = Global.economyApi.depositPlayer(player, money);
    }

    /**
     * 拿走玩家的钱
     * @param money 拿走玩家钱的数量
     */
    public void withdraw(double money) {
        EconomyResponse response = Global.economyApi.withdrawPlayer(player, money);
    }

    public double getCurrentHealth() {
        double currentHealth = (float) player.getHealth();
        if (currentHealth >= maxHealth) player.setHealth(maxHealth);
        return currentHealth;
    }

    // 获取玩家当前生命值
    public double getMaxHealth() {
        maxHealth = getGenericAttribute(Attribute.GENERIC_MAX_HEALTH, BASE_HEALTH);
        return maxHealth;
    }

    // 获取玩家当前护甲值
    public double getTotalArmor() {
        return totalArmor;
    }

    // 获取玩家当前移动速度
    public double getTotalMovementSpeed() {
        return getGenericAttribute(Attribute.GENERIC_MOVEMENT_SPEED, 0);
    }

    public double getTotalArmorToughness() {
        return getGenericAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS, 0);
    }

    public double getTotalKnockBackResistance() {
        return getGenericAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE, 0);
    }

    public double getTotalLuck() {
        return getGenericAttribute(Attribute.GENERIC_LUCK, 0);
    }

    public double getTotalAttackDamage() {
        return getGenericAttribute(Attribute.GENERIC_ATTACK_DAMAGE, 0);
    }

    public double getTotalAttackSpeed() {
        return getGenericAttribute(Attribute.GENERIC_ATTACK_SPEED, 0);
    }

    public double getTotalRecover() {
        return totalRecover;
    }

    public double getTotalBloodSucking() {
        return totalBloodSucking;
    }

    public double getTotalCritical() {
        return totalCritical;
    }

    public double getTotalCriticalDamage() {
        return totalCriticalDamage;
    }

    public double getTotalAttackReach() {
        return totalAttackReach;
    }

    public double getTotalIntelligence() {
        return totalIntelligence;
    }

    public double getTotalDiggingSpeed() {
        return totalDiggingSpeed;
    }

    public double getTotalLoggingSpeed() {
        return totalLoggingSpeed;
    }

    public void setTotalLoggingSpeed(double value) {
        totalLoggingSpeed = value;
    }

    public void setTotalDiggingSpeed(double value) {
        totalDiggingSpeed = value;
    }

    public void setTotalIntelligence(double value) {
        totalIntelligence = value;
    }

    public void setTotalArmor(double value) {
        totalArmor = value;
    }

    public void setTotalCriticalDamage(double value) {
        totalCriticalDamage = value;
    }

    public void setTotalAttackReach(double value) {
        totalAttackReach = value;
    }

    public void setTotalCritical(double value) {
        totalCritical = value;
    }

    public void setTotalBloodSucking(double value) {
        totalBloodSucking = value;
    }

    public void setTotalRecover(double value) {
        totalRecover = value;
    }

    public boolean isLogin() {
        return login;
    }

    public void setLogin(boolean value) {
        login = value;
    }

    public Date getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(Date value) {
        loginTime = value;
    }

    public boolean getRegister() {
        return register;
    }

    public void setRegister(boolean value) {
        register = value;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    @NotNull
    public Player getPlayer() {
        return player;
    }

    public org.bukkit.GameMode getGameMode() {
        return gameMode;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public boolean isRegister() throws SQLException {
        return SqliteManager.isPlayerRegister(player);
    }

    public BanPlayerDetail isBanned() throws SQLException {
        return SqliteManager.isPlayerBanned(player);
    }

    public boolean Register(String password) throws SQLException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException {

        boolean register = SqliteManager.PlayerRegister(player, password);
        if (!register) {
            player.sendMessage(LanguageManager.getString("playerRegisterExist", player)
                    .replace("%player%", name));
        } else {
            login = true;
            //Global.LogMessage(String.format("\033[1;35mPlayer register gameMode: \033[1;33m%s\033[0m", gameMode));
            player.setGameMode(gameMode);
            plugin.getServer().broadcastMessage(LanguageManager.getString("playerRegisterWelcome", player)
                    .replace("%player%", name));

            doLoginSuccess();
        }

        return register;
    }

    public boolean Unregister(String password) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException, SQLException {

        boolean unregister = SqliteManager.PlayerUnregister(player, password);
        if (!unregister) {
            player.sendMessage(LanguageManager.getString("playerURPasswordNotRight", player).replace("%player%", name));
        } else {
            player.kickPlayer(LanguageManager.getString("playerUnregistered", player).replace("%player%", name));
        }

        return unregister;
    }

    /**
     * 玩家登陆，如果之前玩家已经登陆成功，之后再调用这个函数，则直接判断玩家无需要再登陆
     *
     * @param password 玩家的密码
     * @return True: 玩家登陆成功，False: 玩家登陆失败
     */
    public boolean Login(String password) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException, SQLException {

        if (login) {
            player.sendMessage(LanguageManager.getString("playerHasLogin", player).replace("%player%", name));
            return false;
        }
        login = SqliteManager.PlayerLogin(player, password);
        if (!login) {
            player.sendMessage(LanguageManager.getString("playerLoginFailed", player).replace("%player%", name));
        } else {
            //Global.LogMessage(String.format("\033[1;35mPlayer Login gameMode: \033[1;33m%s\033[0m", gameMode));
            player.setGameMode(gameMode);
            player.sendMessage(LanguageManager.getString("playerLoginWelcome", player)
                    .replace("%player%", name));

            doLoginSuccess();
        }

        return login;
    }

    public boolean ChangePassword(String oldPassword, String newPassword) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException, SQLException {

        boolean exist = SqliteManager.CheckPassword(player, oldPassword);
        if (!exist) {
            player.sendMessage(LanguageManager.getString("playerOldPasswordNotRight", player).replace("%player%", name));
            return false;
        }

        boolean changed = SqliteManager.ChangePassword(player, newPassword);
        if (!changed) {
            player.sendMessage(LanguageManager.getString("systemError", player).replace("%player%", name));
        } else {
            player.sendMessage(LanguageManager.getString("playerPasswordChanged", player).replace("%player%", name));
        }

        return changed;
    }

    public void UnBanPlayer(String targetPlayer) throws SQLException {
        PlayerBannedState state = SqliteManager.UnBanPlayer(targetPlayer);
        switch (state) {
            case Player_Not_Exist ->
                    player.sendMessage(LanguageManager.getString("playerNotExist", player).replace("%player%", targetPlayer));
            case Player_Unbanned ->
                    player.sendMessage(LanguageManager.getString("playerUnBanned", player).replace("%player%", targetPlayer));
        }
    }

    public void BanPlayer(String targetPlayer, String reason) throws SQLException {
        PlayerBannedState state = SqliteManager.BanPlayer(player, targetPlayer, reason);
        Player[] players = plugin.getServer().getOnlinePlayers().toArray(new Player[0]);
        Player target = Arrays.stream(players).filter(x -> x.getName().equalsIgnoreCase(targetPlayer)).findAny().orElse(null);

        java.util.Date banDate = new Date();
        DateFormat df = DateFormat.getDateInstance(DateFormat.FULL, Locale.CHINA);
        DateFormat tf = DateFormat.getTimeInstance(DateFormat.FULL, Locale.CHINA);
        String banDateTime = String.format("%s %s", df.format(banDate), tf.format(banDate));

        switch (state) {
            case Player_Not_Exist ->
                    player.sendMessage(LanguageManager.getString("playerNotExist", player).replace("%player%", targetPlayer));
            case Player_Banned -> {
                if (target != null) {
                    target.kickPlayer(LanguageManager.getString("playerBannedMessage", player)
                            .replace("%player%", targetPlayer)
                            .replace("%master%", name)
                            .replace("%reason%", reason)
                            .replace("%banDate%", banDateTime));
                }
                player.sendMessage(LanguageManager.getString("playerBanned", player).replace("%player%", targetPlayer));
            }
        }
    }

    public void ShowSidebar(String world) {
        ScoreboardItem sidebar = SidebarManager.getItemByWorld(world);
        this.sidebar = sidebar;
        if (sidebar == null) return;
        sidebar.display(player);
    }

    public void UpdateSidebar() {
        if (this.sidebar == null) return;
        sidebar.Update(player);
    }

    public void LoadConfig() {
        UUID playerUuid = player.getUniqueId();
        File f = new File(plugin.getDataFolder(), String.format("profile/%s.yml", playerUuid));
        // 加载所有Map<Material, Integer>类型的玩家记录
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(f);
        Yaml2Map(yaml);
    }

    public void SaveConfig() throws IOException {
        UUID playerUuid = player.getUniqueId();
        File f = new File(plugin.getDataFolder(), String.format("profile/%s.yml", playerUuid));
        // 转换所有Map<Material, Integer>类型的玩家记录，成为Yaml格式字符串
        YamlConfiguration yaml = Map2Yaml();
        yaml.save(f);
    }

    public int statisticPickupFeather() {
        return player.getStatistic(Statistic.PICKUP, Material.FEATHER);
    }
    public int statisticPickupLeather() {
        return player.getStatistic(Statistic.PICKUP, Material.LEATHER);
    }
    public int statisticPickupMutton() {
        return player.getStatistic(Statistic.PICKUP, Material.MUTTON);
    }
    public int statisticPickupRabbitHide() {
        return player.getStatistic(Statistic.PICKUP, Material.RABBIT_HIDE);
    }
    public int statisticPickupRabbitFoot() {
        return player.getStatistic(Statistic.PICKUP, Material.RABBIT_FOOT);
    }
    public int statisticPickupBeef() {
        return player.getStatistic(Statistic.PICKUP, Material.BEEF);
    }
    public int statisticPickupChicken() {
        return player.getStatistic(Statistic.PICKUP, Material.CHICKEN);
    }
    public int statisticPickupPorkchop() {
        return player.getStatistic(Statistic.PICKUP, Material.PORKCHOP);
    }
    public int statisticPickupRabbit() {
        return player.getStatistic(Statistic.PICKUP, Material.RABBIT);
    }
    public int statisticPickupCactus() {
        return player.getStatistic(Statistic.PICKUP, Material.CACTUS);
    }
    public int statisticPickupCarrot() {
        return player.getStatistic(Statistic.PICKUP, Material.CARROT);
    }
    public int statisticPickupCocoaBeans() {
        return player.getStatistic(Statistic.PICKUP, Material.COCOA_BEANS);
    }
    public int statisticPickupMelon() {
        return player.getStatistic(Statistic.PICKUP, Material.MELON);
    }
    public int statisticPickupRedMushroom() {
        return player.getStatistic(Statistic.PICKUP, Material.RED_MUSHROOM);
    }
    public int statisticPickupBrownMushroom() {
        return player.getStatistic(Statistic.PICKUP, Material.BROWN_MUSHROOM);
    }
    public int statisticPickupNetherWart() {
        return player.getStatistic(Statistic.PICKUP, Material.NETHER_WART);
    }
    public int statisticPickupPotato() {
        return player.getStatistic(Statistic.PICKUP, Material.POTATO);
    }
    public int statisticPickupPumpkin() {
        return player.getStatistic(Statistic.PICKUP, Material.PUMPKIN);
    }
    public int statisticPickupSugarCane() {
        return player.getStatistic(Statistic.PICKUP, Material.SUGAR_CANE);
    }
    public int statisticPickupWheat() {
        return player.getStatistic(Statistic.PICKUP, Material.WHEAT);
    }
    public int statisticPickupWheatSeeds() {
        return player.getStatistic(Statistic.PICKUP, Material.WHEAT_SEEDS);
    }
    public int statisticPickupCoal() {
        return player.getStatistic(Statistic.PICKUP, Material.COAL);
    }
    public int statisticPickupCobblestone() {
        return player.getStatistic(Statistic.PICKUP, Material.COBBLESTONE);
    }
    public int statisticPickupDiamond() {
        return player.getStatistic(Statistic.PICKUP, Material.DIAMOND);
    }
    public int statisticPickupEmerald() {
        return player.getStatistic(Statistic.PICKUP, Material.EMERALD);
    }
    public int statisticPickupEndStone() {
        return player.getStatistic(Statistic.PICKUP, Material.END_STONE);
    }
    public int statisticPickupGlowStoneDust() {
        return player.getStatistic(Statistic.PICKUP, Material.GLOWSTONE_DUST);
    }
    public int statisticPickupGoldIngot() {
        return player.getStatistic(Statistic.PICKUP, Material.GOLD_INGOT);
    }
    public int statisticPickupIronIngot() {
        return player.getStatistic(Statistic.PICKUP, Material.IRON_INGOT);
    }
    public int statisticPickupGravel() {
        return player.getStatistic(Statistic.PICKUP, Material.GRAVEL);
    }
    public int statisticPickupIce() {
        return player.getStatistic(Statistic.PICKUP, Material.ICE);
    }
    public int statisticPickupLapisLazuli() {
        return player.getStatistic(Statistic.PICKUP, Material.LAPIS_LAZULI);
    }
    public int statisticPickupQuartz() {
        return player.getStatistic(Statistic.PICKUP, Material.QUARTZ);
    }
    public int statisticPickupNetherrack() {
        return player.getStatistic(Statistic.PICKUP, Material.NETHERRACK);
    }
    public int statisticPickupObsidian() {
        return player.getStatistic(Statistic.PICKUP, Material.OBSIDIAN);
    }
    public int statisticPickupRedSend() {
        return player.getStatistic(Statistic.PICKUP, Material.RED_SAND);
    }
    public int statisticPickupSand() {
        return player.getStatistic(Statistic.PICKUP, Material.SAND);
    }
    public int statisticPickupRedstone() {
        return player.getStatistic(Statistic.PICKUP, Material.REDSTONE);
    }
    public int statisticPickupAcaciaLog() {
        return player.getStatistic(Statistic.PICKUP, Material.ACACIA_LOG);
    }
    public int statisticPickupBirchLog() {
        return player.getStatistic(Statistic.PICKUP, Material.BIRCH_LOG);
    }
    public int statisticPickupDarkOakLog() {
        return player.getStatistic(Statistic.PICKUP, Material.DARK_OAK_LOG);
    }
    public int statisticPickupJungleLog() {
        return player.getStatistic(Statistic.PICKUP, Material.JUNGLE_LOG);
    }
    public int statisticPickupMangroveLog() {
        return player.getStatistic(Statistic.PICKUP, Material.MANGROVE_LOG);
    }
    public int statisticPickupOakLog() {
        return player.getStatistic(Statistic.PICKUP, Material.OAK_LOG);
    }
    public int statisticPickupSpruceLog() {
        return player.getStatistic(Statistic.PICKUP, Material.SPRUCE_LOG);
    }
    public int statisticPickupCrimsonStem() {
        return player.getStatistic(Statistic.PICKUP, Material.CRIMSON_STEM);
    }
    public int statisticPickupWarpedStem() {
        return player.getStatistic(Statistic.PICKUP, Material.WARPED_STEM);
    }
    public int statisticPickupAcaciaSapling() {
        return player.getStatistic(Statistic.PICKUP, Material.ACACIA_SAPLING);
    }
    public int statisticPickupBirchSapling() {
        return player.getStatistic(Statistic.PICKUP, Material.BIRCH_SAPLING);
    }
    public int statisticPickupDarkOakSapling() {
        return player.getStatistic(Statistic.PICKUP, Material.DARK_OAK_SAPLING);
    }
    public int statisticPickupJungleSapling() {
        return player.getStatistic(Statistic.PICKUP, Material.JUNGLE_SAPLING);
    }
    public int statisticPickupMangrovePropagule() {
        return player.getStatistic(Statistic.PICKUP, Material.MANGROVE_PROPAGULE);
    }
    public int statisticPickupOakSapling() {
        return player.getStatistic(Statistic.PICKUP, Material.OAK_SAPLING);
    }
    public int statisticPickupSpruceSapling() {
        return player.getStatistic(Statistic.PICKUP, Material.SPRUCE_SAPLING);
    }
    public int statisticPickupApple() {
        return player.getStatistic(Statistic.PICKUP, Material.APPLE);
    }
    public int statisticPickupTropicalFish() {
        return player.getStatistic(Statistic.PICKUP, Material.TROPICAL_FISH);
    }
    public int statisticPickupInkSac() {
        return player.getStatistic(Statistic.PICKUP, Material.INK_SAC);
    }
    public int statisticPickupLilyPad() {
        return player.getStatistic(Statistic.PICKUP, Material.LILY_PAD);
    }
    public int statisticPickupPrismarineCrystals() {
        return player.getStatistic(Statistic.PICKUP, Material.PRISMARINE_CRYSTALS);
    }
    public int statisticPickupPrismarineShard() {
        return player.getStatistic(Statistic.PICKUP, Material.PRISMARINE_SHARD);
    }
    public int statisticPickupPufferFish() {
        return player.getStatistic(Statistic.PICKUP, Material.PUFFERFISH);
    }
    public int statisticPickupCod() {
        return player.getStatistic(Statistic.PICKUP, Material.COD);
    }
    public int statisticPickupSalmon() {
        return player.getStatistic(Statistic.PICKUP, Material.SALMON);
    }
    public int statisticPickupSponge() {
        return player.getStatistic(Statistic.PICKUP, Material.SPONGE);
    }
    public int statisticPickupClay() {
        return player.getStatistic(Statistic.PICKUP, Material.CLAY);
    }
    public int statisticPickupBlazeRod() {
        return player.getStatistic(Statistic.PICKUP, Material.BLAZE_ROD);
    }
    public int statisticPickupBone() {
        return player.getStatistic(Statistic.PICKUP, Material.BONE);
    }
    public int statisticPickupEnderPearl() {
        return player.getStatistic(Statistic.PICKUP, Material.ENDER_PEARL);
    }
    public int statisticPickupGhastTear() {
        return player.getStatistic(Statistic.PICKUP, Material.GHAST_TEAR);
    }
    public int statisticPickupGunpowder() {
        return player.getStatistic(Statistic.PICKUP, Material.GUNPOWDER);
    }
    public int statisticPickupMagmaCream() {
        return player.getStatistic(Statistic.PICKUP, Material.MAGMA_CREAM);
    }
    public int statisticPickupRottenFlesh() {
        return player.getStatistic(Statistic.PICKUP, Material.ROTTEN_FLESH);
    }
    public int statisticPickupSlimeBall() {
        return player.getStatistic(Statistic.PICKUP, Material.SLIME_BALL);
    }
    public int statisticPickupSpiderEye() {
        return player.getStatistic(Statistic.PICKUP, Material.SPIDER_EYE);
    }
    public int statisticPickupString() {
        return player.getStatistic(Statistic.PICKUP, Material.STRING);
    }

    public int statisticPickupCombat() {
        int total = 0;

        for (Material m : MMOSkillManager.combatStatistics) {
            total += player.getStatistic(Statistic.PICKUP, m);
        }
        return total;
    }

    public int statisticPickupFishing() {
        int total = 0;
        for (Material m : MMOSkillManager.fishingStatistics) {
            total += player.getStatistic(Statistic.PICKUP, m);
        }
        return total;
    }

    public int statisticPickupLumbering() {
        int total = 0;
        for (Material m : MMOSkillManager.lumberingStatistics) {
            total += player.getStatistic(Statistic.PICKUP, m);
        }
        return total;
    }

    /**
     * 拾取矿物的总数量
     */
    public int statisticPickupMineral() {
        int total = 0;
        for (Material m : MMOSkillManager.miningStatistics) {
            total += player.getStatistic(Statistic.PICKUP, m);
        }
        return total;
    }

    /**
     * 拾取农作物及家禽产物的总数量<br>
     * TODO: 需要实施家禽产物的总数量
     */
    public int statisticPickupCrops() {
        int total = 0;
        for (Material m : MMOSkillManager.farmingStatistics) {
            total += player.getStatistic(Statistic.PICKUP, m);
        }

        return total;
    }

    private void doLoginSuccess() {
        List<ItemBase> books = ItemManager.getBooks();
        for (ItemBase ib : books) {
            if (!(ib instanceof Join join)) continue;
            if (!ib.getFeatures().contains(ItemFeatureType.OPEN_BOOK_ON_JOIN)) continue;
            player.openBook(join.toBook(player));
        }

        if (!player.hasPlayedBefore()) {
            List<ItemBase> firstJoins = ItemManager.getItems(ItemFeatureType.FIRST_JOIN);
            for (ItemBase ib : firstJoins) {
                player.getInventory().addItem(ib.toItemStack());
            }
        }
    }

    /**
     * 将玩家档案内所有记录数据加载到类型为Map&lt;?, ?&gt;的属性
     */
    private void Yaml2Map(YamlConfiguration yaml) {

        try {
            // 循环检测SectionKeys字段内的字符串列表
            for (String s : SectionKeys) {
                // 根据字符串获取当前对象的属性的实例
                Field[] fields = this.getClass().getFields();
                Field field = Arrays.stream(fields).filter(x -> x.getName().equalsIgnoreCase(s)).findAny().orElse(null);
                if (field == null) continue;
                // 过滤类型不是Map<?, ?>的属性
                ParameterizedType pType = (ParameterizedType) field.getGenericType();
                String name = pType.getRawType().getTypeName();
                if (!name.equalsIgnoreCase(TYPE_JAVA_UTIL_MAP)) continue;
                // 根据字符串获取yaml格式文档内相应的节段及内容
                ConfigurationSection section = yaml.getConfigurationSection(s);
                if (section == null) continue;
                // 使用Gson将Yaml内容转换到类型为Map<?, ?>的属性
                String data = Global.GsonObject.toJson(section.getValues(false));
                Type mapType = new TypeToken<Map<?, ?>>() {
                }.getType();
                Map<?, ?> value = Global.GsonObject.fromJson(data, mapType);
                field.set(this, value);
            }
            // 加载玩家的其他数据记录或者状态
            isCourseDesigning = yaml.getBoolean(Section_Key_Parkour_Design);
            courseIds = yaml.getStringList(Section_Key_Parkour_List);
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 将所有类型为Map&lt;?, ?&gt;的属性保存到yaml实例
     *
     * @return YamlConfiguration实例
     */
    private YamlConfiguration Map2Yaml() {

        try {
            YamlConfiguration yaml = new YamlConfiguration();
            String v = Global.GsonObject.toJson(this);
            yaml.loadFromString(v);
            return yaml;
        } catch (InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    private double getGenericAttribute(Attribute attribute, double defaultValue) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) return defaultValue;

        return instance.getValue();
    }
}

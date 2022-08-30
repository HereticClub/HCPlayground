package org.hcmc.hcplayground.model.player;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.enums.ItemFeatureType;
import org.hcmc.hcplayground.enums.MMOType;
import org.hcmc.hcplayground.enums.PlayerBannedState;
import org.hcmc.hcplayground.manager.*;
import org.hcmc.hcplayground.model.armorset.ArmorSetEffect;
import org.hcmc.hcplayground.model.item.ItemBase;
import org.hcmc.hcplayground.model.item.Join;
import org.hcmc.hcplayground.model.scoreboard.ScoreboardItem;
import org.hcmc.hcplayground.sqlite.SqliteManager;
import org.hcmc.hcplayground.sqlite.table.BanPlayerDetail;
import org.hcmc.hcplayground.utility.Global;
import org.hcmc.hcplayground.utility.NameBinaryTag;
import org.jetbrains.annotations.NotNull;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class PlayerData {
    /**
     * 玩家血量压缩边缘等级<br>
     * 右边数值代表玩家血量，当小于某个等级的数值，则按相应左边数值的压缩血量(♥数量)显示<br>
     * Double - 血量压缩值<br>
     * Double - 血量等级值<br>
     * 例如: 当前玩家血量555，在表中表示小于610，则按照30来显示(♥数量)压缩血量<br>
     */
    private static final Map<Double, Double> scaleEdges = new HashMap<>();
    private static final String Section_Key_CcmdCooldownList = "ccmdCooldown";
    private static final String Section_Key_Course_Design = "courseDesignMode";
    private static final String Section_Key_Course_Owned = "courseOwned";
    private static final String Section_Key_Claimed_Skill_Level = "claimedSkillLevel";
    private static final String Section_Key_Claimed_Collection_Level = "claimedCollectionLevel";
    private static final String Section_Key_Recipes = "recipes";
    private static final String Section_Key_Activated_Armor_Sets= "activatedArmorSets";
    public static final String ECONOMY_BALANCE_KEY = "balance";
    public static final double BASE_HEALTH = 20.0F;
    public static final double BASE_CRITICAL = 0.05F;
    public static final double BASE_CRITICAL_DAMAGE = 1.5F;
    public static final double BASE_ARMOR = 0;
    public static final double BASE_ARMOR_TOUGHNESS = 0;
    public static final double BASE_ATTACK_REACH = 0;
    public static final double BASE_BLOOD_SUCKING = 0;
    public static final double BASE_RECOVER = 0;
    public static final double BASE_INTELLIGENCE = 0;
    public static final double BASE_DIGGING_SPEED = 0;
    public static final double BASE_LOGGING_SPEED = 0;

    static {
        scaleEdges.put(20.0, 100.0);
        scaleEdges.put(22.0, 300.0);
        scaleEdges.put(24.0, 500.0);
        scaleEdges.put(26.0, 700.0);
        scaleEdges.put(28.0, 900.0);
        scaleEdges.put(30.0, 1100.0);
        scaleEdges.put(32.0, 1300.0);
        scaleEdges.put(34.0, 1500.0);
        scaleEdges.put(36.0, 1700.0);
        scaleEdges.put(38.0, 1900.0);
        scaleEdges.put(40.0, 9999.0);
    }

    /**
     * 玩家护甲值
     */
    @Expose
    @SerializedName(value = "armor")
    private double baseArmor = BASE_ARMOR;
    /**
     * 玩家盔甲韧性值
     */
    @Expose
    @SerializedName(value = "armor_toughness")
    private double baseArmorToughness = BASE_ARMOR_TOUGHNESS;
    /**
     * 攻击距离
     */
    @Expose
    @SerializedName(value = "attack_reach")
    private double baseAttackReach = BASE_ATTACK_REACH;
    /**
     * 吸血效果
     */
    @Expose
    @SerializedName(value = "blood_sucking")
    private double baseBloodSucking = BASE_BLOOD_SUCKING;
    /**
     * 暴击率
     */
    @Expose
    @SerializedName(value = "critical")
    private double baseCritical = BASE_CRITICAL;
    /**
     * 暴击伤害
     */
    @Expose
    @SerializedName(value = "critical_damage")
    private double baseCriticalDamage = BASE_CRITICAL_DAMAGE;
    /**
     * 生命恢复，每x秒恢复y血量
     */
    @Expose
    @SerializedName(value = "recover")
    private double baseRecover = BASE_RECOVER;
    /**
     * 智力
     */
    @Expose
    @SerializedName(value = "intelligence")
    private double baseIntelligence = BASE_INTELLIGENCE;
    /**
     * 挖掘速度，影响破坏矿物方块的速度
     */
    @Expose
    @SerializedName(value = "digging_speed")
    private double baseDiggingSpeed = BASE_DIGGING_SPEED;
    /**
     * 伐木速度，影响破坏木头方块的速度
     */
    @Expose
    @SerializedName(value = "logging_speed")
    private double baseLoggingSpeed = BASE_LOGGING_SPEED;
    /**
     * 表示是否在跑酷赛道设计模式
     */
    @Expose
    @SerializedName(value = Section_Key_Course_Design)
    private boolean designMode = false;
    /**
     * 自定义命令的冷却剩余时间<br>
     * String - 自定义命令<br>
     * Double - 剩余的冷却时间<br>
     */
    @Expose
    @SerializedName(value = Section_Key_CcmdCooldownList)
    private Map<String, Double> ccmdCooldown = new HashMap<>();
    /**
     * 当前玩家所拥有的跑酷赛道列表
     */
    @Expose
    @SerializedName(value = Section_Key_Course_Owned)
    private List<String> courseOwned = new ArrayList<>();
    /**
     * 当前玩家已经解锁的疯狂菜单(id)
     */
    @Expose
    @SerializedName(value = Section_Key_Recipes)
    private List<String> recipes = new ArrayList<>();
    /**
     * 记录已经激活的套装效果
     */
    @Expose
    @SerializedName(value = Section_Key_Activated_Armor_Sets)
    private Map<String, List<Integer>> activatedArmorSetEffects = new HashMap<>();
    /**
     * 已领取的技能等级奖励<br>
     * MMOType - 技能类型<br>
     * Integer - 已领取的技能等级<br>
     */
    @Expose
    @SerializedName(value = Section_Key_Claimed_Skill_Level)
    private Map<MMOType, Integer> claimedSkillLevel = new HashMap<>();
    /**
     * 已领取的技能等级奖励<br>
     * MMOType - 技能类型<br>
     * Integer - 已领取的技能等级<br>
     */
    @Expose
    @SerializedName(value = Section_Key_Claimed_Collection_Level)
    private Map<Material, Integer> claimedCollectionLevel = new HashMap<>();
    /**
     * 来自装备或物品的额外护甲值
     */
    private double extraArmor;
    /**
     * 来自装备或物品的额外盔甲韧性值
     */
    private double extraArmorToughness;
    /**
     * 来自装备或物品的额外攻击距离
     */
    private double extraAttackReach;
    /**
     * 来自装备或物品的额外吸血效果
     */
    private double extraBloodSucking;
    /**
     * 来自装备或物品的额外暴击率
     */
    private double extraCritical;
    /**
     * 来自装备或物品的额外暴击伤害
     */
    private double extraCriticalDamage;
    /**
     * 来自装备或物品的额外恢复速度
     */
    private double extraRecover;
    /**
     * 来自装备或物品的额外智力值
     */
    private double extraIntelligence;
    /**
     * 来自装备或物品的额外矿物方块破坏速度
     */
    private double extraDiggingSpeed;
    /**
     * 来自装备或物品的额外木头方块破坏速度
     */
    private double extraLoggingSpeed;
    /**
     * 玩家的背包和装备物品的记录
     */
    private CourseDesigner designer;
    /**
     * 玩家在runnable线程的时间检查点，初始化为登陆时间
     * 通常不会更改这个属性的值
     */
    private long loginTimeStamp;
    /**
     * 本插件实例
     */
    private JavaPlugin plugin;
    /**
     * 记录玩家登陆时的游戏模式
     */
    private GameMode gameMode;
    /**
     * 玩家的Player实例
     */
    private Player player;
    /**
     * 玩家的OfflinePlayer实例
     */
    private OfflinePlayer offline;
    /**
     * 玩家的UUID
     */
    private UUID uuid;
    /**
     * 玩家名称
     */
    private String name;
    /**
     * 玩家是否已经使用/login指令登陆到服务器
     */
    private boolean login;
    /**
     * 玩家是否已经使用/register指令注册到服务器
     */
    private boolean register;
    private PermissionAttachment attachment;
    private Date loginTime = new Date();
    private ScoreboardItem sidebar;

    public PlayerData(Player player) {
        initialize(player);
    }

    public void initialize(Player player) {
        this.player = player;
        plugin = HCPlayground.getInstance();
        name = player.getName();
        uuid = player.getUniqueId();
        loginTimeStamp = 0;
        loginTime = new Date();
        gameMode = player.getGameMode();
        attachment = player.addAttachment(plugin);
        designer = new CourseDesigner(this);
        offline = Bukkit.getOfflinePlayer(uuid);

        if (baseCritical <= 0) baseCritical = 0.05;
        if (baseCriticalDamage <= 0) baseCriticalDamage = 1.5;
        if (baseIntelligence <= 0) baseIntelligence = 1.0;
        if (recipes == null) recipes = new ArrayList<>();
        if (activatedArmorSetEffects == null) activatedArmorSetEffects = new HashMap<>();

        setPlayerScale();
    }

    /**
     * 不能直接使用getClaimedSkillLevel().xxx直接进行工作
     * @return claimedSkillLevel属性的副本
     */
    @NotNull
    public Map<MMOType, Integer> getClaimedSkillLevel() {
        return claimedSkillLevel == null ? new HashMap<>() : new HashMap<>(claimedSkillLevel);
    }

    public void setClaimedSkillLevel(@NotNull Map<MMOType, Integer> claimedSkillLevel) {
        this.claimedSkillLevel = new HashMap<>(claimedSkillLevel);
    }

    @NotNull
    public Map<Material, Integer> getClaimedCollectionLevel() {
        return claimedCollectionLevel == null ? new HashMap<>() : new HashMap<>(claimedCollectionLevel);
    }

    public void setClaimedCollectionLevel(@NotNull Map<Material, Integer> claimedCollectionLevel) {
        this.claimedCollectionLevel = new HashMap<>(claimedCollectionLevel);
    }

    public CourseDesigner getDesigner() {
        return designer;
    }

    public long getLoginTimeStamp() {
        return loginTimeStamp;
    }

    public void setLoginTimeStamp(long loginTimeStamp) {
        this.loginTimeStamp = loginTimeStamp;
    }

    public List<String> getCourseOwned() {
        return courseOwned;
    }

    public Map<String, Double> getCcmdCooldown() {
        return ccmdCooldown;
    }

    public List<String> getRecipes() {
        return recipes;
    }

    public boolean unlockRecipe(String recipeId) {
        // 检查是否已经解锁
        if (existRecipe(recipeId)) return false;
        // 检查是否存在配方加载项中
        if (!RecipeManager.existRecipe(recipeId)) return false;
        // 为玩家解锁配方
        recipes.add(recipeId);
        return true;
    }

    public boolean removeRecipe(String recipeId) {
        // 检查是否已经解锁
        if (!existRecipe(recipeId)) return false;
        // 为玩家解锁配方
        recipes.remove(recipeId);
        return true;
    }

    public void unlockRecipe(String ... recipeIds) {
        for (String recipe : recipeIds) {
            // 检查是否已经解锁
            if (existRecipe(recipe)) continue;
            // 检查是否存在配方加载项中
            if (!RecipeManager.existRecipe(recipe)) continue;
            // 为玩家解锁配方
            recipes.add(recipe);
        }
    }

    public boolean existRecipe(String recipeId) {
        return recipes.stream().anyMatch(x -> x.equalsIgnoreCase(recipeId));
    }

    public boolean isDesignMode() {
        return designMode;
    }

    public void setDesignMode(boolean designMode) {
        this.designMode = designMode;
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
     * 玩家从银行提款
     * @param money 提款的数量
     */
    public void deposit(double money) {
        EconomyResponse response = Global.economyApi.depositPlayer(player, money);
        if (!response.type.equals(EconomyResponse.ResponseType.SUCCESS)) Global.LogWarning(response.errorMessage);
    }

    /**
     * 玩家存款到银行
     * @param money 存款的数量
     */
    public void withdraw(double money) {
        EconomyResponse response = Global.economyApi.withdrawPlayer(player, money);
        if (!response.type.equals(EconomyResponse.ResponseType.SUCCESS)) Global.LogWarning(response.errorMessage);
    }

    public Map<String, List<Integer>> getActivatedArmorSetEffects() {
        return new HashMap<>(activatedArmorSetEffects);
    }

    public void setActivatedArmorSetEffects(Map<String, List<Integer>> activatedArmorSetEffects) {
        this.activatedArmorSetEffects = new HashMap<>(activatedArmorSetEffects);
    }

    public double getExtraArmor() {
        return extraArmor;
    }

    public void setExtraArmor(double extraArmor) {
        this.extraArmor = extraArmor;
    }

    public double getExtraArmorToughness() {
        return extraArmorToughness;
    }

    public void setExtraArmorToughness(double extraArmorToughness) {
        this.extraArmorToughness = extraArmorToughness;
    }

    public double getExtraAttackReach() {
        return extraAttackReach;
    }

    public void setExtraAttackReach(double extraAttackReach) {
        this.extraAttackReach = extraAttackReach;
    }

    public double getExtraBloodSucking() {
        return extraBloodSucking;
    }

    public void setExtraBloodSucking(double extraBloodSucking) {
        this.extraBloodSucking = extraBloodSucking;
    }

    public double getExtraCritical() {
        return extraCritical;
    }

    public void setExtraCritical(double extraCritical) {
        this.extraCritical = extraCritical;
    }

    public double getExtraCriticalDamage() {
        return extraCriticalDamage;
    }

    public void setExtraCriticalDamage(double extraCriticalDamage) {
        this.extraCriticalDamage = extraCriticalDamage;
    }

    public double getExtraDiggingSpeed() {
        return extraDiggingSpeed;
    }

    public void setExtraDiggingSpeed(double extraDiggingSpeed) {
        this.extraDiggingSpeed = extraDiggingSpeed;
    }

    public double getExtraIntelligence() {
        return extraIntelligence;
    }

    public void setExtraIntelligence(double extraIntelligence) {
        this.extraIntelligence = extraIntelligence;
    }

    public double getExtraLoggingSpeed() {
        return extraLoggingSpeed;
    }

    public void setExtraLoggingSpeed(double extraLoggingSpeed) {
        this.extraLoggingSpeed = extraLoggingSpeed;
    }

    public double getExtraRecover() {
        return extraRecover;
    }

    public void setExtraRecover(double extraRecover) {
        this.extraRecover = extraRecover;
    }

    public double getLiveHealth() {
        double liveHealth = (float) player.getHealth();
        double maxHealth = getMaxHealth();

        if (liveHealth >= maxHealth) player.setHealth(maxHealth);
        return liveHealth;
    }

    // 获取玩家当前生命值
    public double getMaxHealth() {
        return getMaxAttribute(Attribute.GENERIC_MAX_HEALTH, BASE_HEALTH);
    }

    public double getBaseHealth() {
        return getBaseAttribute(Attribute.GENERIC_MAX_HEALTH, BASE_HEALTH);
    }

    public void setBaseHealth(double maxHealth) {
        setBaseAttribute(Attribute.GENERIC_MAX_HEALTH, maxHealth);
        setPlayerScale();
    }

    public void increaseBaseHealth(double health) {
        double _health = getBaseHealth();
        setBaseHealth(health + _health);
    }

    public void decreaseBaseHealth(double health) {
        double _health = getBaseHealth();
        setBaseHealth(_health - health);
    }

    public double getBaseMovementSpeed() {
        return getBaseAttribute(Attribute.GENERIC_MOVEMENT_SPEED, 0);
    }

    public double getMaxMovementSpeed() {
        return getMaxAttribute(Attribute.GENERIC_MOVEMENT_SPEED, 0);
    }

    public void setBaseMovementSpeed(double value) {
        setBaseAttribute(Attribute.GENERIC_MOVEMENT_SPEED, value);
    }

    public void increaseBaseMovementSpeed(double value) {
        double _value = getBaseMovementSpeed();
        setBaseMovementSpeed(value + _value);
    }

    public void decreaseBaseMovementSpeed(double value) {
        double _value = getBaseMovementSpeed();
        setBaseMovementSpeed(_value - value);
    }

    public double getBaseKnockBackResistance() {
        return getBaseAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE, 0);
    }

    public double getMaxKnockBackResistance(){
        return getMaxAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE, 0);
    }

    public void setBaseKnockBackResistance(double value) {
        setBaseAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE, value);
    }

    public void increaseBaseKnockBackResistance(double value) {
        double _value = getBaseKnockBackResistance();
        setBaseKnockBackResistance(value + _value);
    }

    public void decreaseBaseKnockBackResistance(double value){
        double _value = getBaseKnockBackResistance();
        setBaseKnockBackResistance(_value - value);
    }

    public double getBaseLuck() {
        return getBaseAttribute(Attribute.GENERIC_LUCK, 0);
    }

    public double getMaxLuck() {
        return getMaxAttribute(Attribute.GENERIC_LUCK, 0);
    }

    public void setBaseLuck(double value) {
        setBaseAttribute(Attribute.GENERIC_LUCK, value);
    }

    public void increaseBaseLuck(double value) {
        double _value = getBaseLuck();
        setBaseLuck(value + _value);
    }

    public void decreaseBaseLuck(double value) {
        double _value = getBaseLuck();
        setBaseLuck(_value - value);
    }

    public double getBaseAttackDamage() {
        return getBaseAttribute(Attribute.GENERIC_ATTACK_DAMAGE, 0);
    }

    public double getMaxAttackDamage() {
        return getMaxAttribute(Attribute.GENERIC_ATTACK_DAMAGE, 0);
    }

    public void setBaseAttackDamage(double value) {
        setBaseAttribute(Attribute.GENERIC_ATTACK_DAMAGE, value);
    }

    public void increaseBaseAttackDamage(double value) {
        double _value = getBaseAttackDamage();
        setBaseAttackDamage(value + _value);
    }

    public void decreaseBaseAttackDamage(double value) {
        double _value = getBaseAttackDamage();
        setBaseAttackDamage(_value - value);
    }

    public double getBaseAttackSpeed() {
        return getBaseAttribute(Attribute.GENERIC_ATTACK_SPEED, 0);
    }

    public double getMaxAttackSpeed() {
        return getMaxAttribute(Attribute.GENERIC_ATTACK_SPEED, 0);
    }

    public void setBaseAttackSpeed(double value) {
        setBaseAttribute(Attribute.GENERIC_ATTACK_SPEED, value);
    }

    public void increaseBaseAttackSpeed(double value) {
        double _value = getBaseAttackSpeed();
        setBaseAttackSpeed(value + _value);
    }

    public void decreaseBaseAttackSpeed(double value) {
        double _value = getBaseAttackSpeed();
        setBaseAttackSpeed(_value - value);
    }

    // 获取玩家当前护甲值
    public double getBaseArmor() {
        return baseArmor;
    }

    public void setBaseArmor(double value) {
        baseArmor = value;
    }

    public void increaseBaseArmor(double value) {
        double _value = getBaseArmor();
        setBaseArmor(value + _value);
    }

    public void decreaseBaseArmor(double value) {
        double _value = getBaseArmor();
        setBaseArmor(_value - value);
    }

    public double getMaxArmor() {
        return baseArmor + extraArmor;
    }

    public double getBaseArmorToughness() {
        return baseArmorToughness;
    }

    public void setBaseArmorToughness(double value) {
        baseArmorToughness = value;
    }

    public void increaseBaseArmorToughness(double value) {
        double _value = getBaseArmorToughness();
        setBaseArmorToughness(value + _value);
    }

    public void decreaseBaseArmorToughness(double value) {
        double _value = getBaseArmorToughness();
        setBaseArmorToughness(_value - value);
    }

    public double getMaxArmorToughness() {
        return baseArmorToughness + extraArmorToughness;
    }

    public double getBaseCritical() {
        return baseCritical;
    }

    public void setBaseCritical(double value) {
        baseCritical = value;
    }

    public void increaseBaseCritical(double value) {
        double _value = getBaseCritical();
        setBaseCritical(value + _value);
    }

    public void decreaseBaseCritical(double value) {
        double _value = getBaseCritical();
        setBaseCritical(_value - value);
    }

    public double getMaxCritical() {
        return baseCritical + extraCritical;
    }

    public double getBaseCriticalDamage() {
        return baseCriticalDamage;
    }

    public void setBaseCriticalDamage(double value) {
        baseCriticalDamage = value;
    }

    public void increaseBaseCriticalDamage(double value) {
        double _value = getBaseCriticalDamage();
        setBaseCriticalDamage(value + _value);
    }

    public void decreaseBaseCriticalDamage(double value) {
        double _value = getBaseCriticalDamage();
        setBaseCriticalDamage(_value - value);
    }

    public double getMaxCriticalDamage() {
        return baseCriticalDamage + extraCriticalDamage;
    }

    public double getBaseAttackReach() {
        return baseAttackReach;
    }

    public void setBaseAttackReach(double value) {
        baseAttackReach = value;
    }

    public void increaseBaseAttackReach(double value) {
        double _value = getBaseAttackReach();
        setBaseAttackReach(value + _value);
    }

    public void decreaseBaseAttackReach(double value) {
        double _value = getBaseAttackReach();
        setBaseAttackReach(_value - value);
    }

    public double getMaxAttackReach() {
        return baseAttackReach + extraAttackReach;
    }

    public double getBaseIntelligence() {
        return baseIntelligence;
    }

    public void setBaseIntelligence(double value) {
        baseIntelligence = value;
    }

    public void increaseBaseIntelligence(double value) {
        double _value = getBaseIntelligence();
        setBaseIntelligence(value + _value);
    }

    public void decreaseBaseIntelligence(double value) {
        double _value = getBaseIntelligence();
        setBaseIntelligence(_value - value);
    }

    public double getMaxIntelligence() {
        return baseIntelligence + extraIntelligence;
    }

    public double getBaseDiggingSpeed() {
        return baseDiggingSpeed;
    }

    public void setBaseDiggingSpeed(double value) {
        baseDiggingSpeed = value;
    }

    public void increaseBaseDiggingSpeed(double value) {
        double _value = getBaseDiggingSpeed();
        setBaseDiggingSpeed(value + _value);
    }

    public void decreaseBaseDiggingSpeed(double value) {
        double _value = getBaseDiggingSpeed();
        setBaseDiggingSpeed(_value - value);
    }

    public double getMaxDiggingSpeed() {
        return baseDiggingSpeed + extraDiggingSpeed;
    }

    public double getBaseLoggingSpeed() {
        return baseLoggingSpeed;
    }

    public void setBaseLoggingSpeed(double value) {
        baseLoggingSpeed = value;
    }

    public void increaseBaseLoggingSpeed(double value) {
        double _value = getBaseLoggingSpeed();
        setBaseLoggingSpeed(value + _value);
    }

    public void decreaseBaseLoggingSpeed(double value) {
        double _value = getBaseLoggingSpeed();
        setBaseLoggingSpeed(_value - value);
    }

    public double getMaxLoggingSpeed() {
        return baseLoggingSpeed + extraLoggingSpeed;
    }

    public void setBaseBloodSucking(double value) {
        baseBloodSucking = value;
    }

    public double getBaseBloodSucking() {
        return baseBloodSucking;
    }

    public void increaseBaseBloodSucking(double value) {
        double _value = getBaseBloodSucking();
        setBaseBloodSucking(value + _value);
    }

    public void decreaseBaseBloodSucking(double value) {
        double _value = getBaseBloodSucking();
        setBaseBloodSucking(_value - value);
    }

    public double getMaxBloodSucking() {
        return baseBloodSucking + extraBloodSucking;
    }

    public double getBaseRecover() {
        return baseRecover;
    }

    public void setBaseRecover(double value) {
        baseRecover = value;
    }

    public void increaseBaseRecover(double value) {
        double _value = getBaseRecover();
        setBaseRecover(value + _value);
    }

    public void decreaseBaseRecover(double value) {
        double _value = getBaseRecover();
        setBaseRecover(_value - value);
    }

    public double getMaxRecover() {
        return baseRecover + extraRecover;
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

    public void checkRegister() {
        try {
            register = SqliteManager.isPlayerRegister(player);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isRegister() {
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

    public BanPlayerDetail getBanDetail() throws SQLException {
        return SqliteManager.getBanDetail(player);
    }

    public boolean Register(String password) throws SQLException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException {

        boolean register = SqliteManager.doPlayerRegister(player, password);
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

        boolean unregister = SqliteManager.doPlayerUnregister(player, password);
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
            player.sendMessage(LanguageManager.getString("systemCriticalError", player).replace("%player%", name));
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

    /**
     * 敌对生物的击杀统计
     * @return 敌对生物击杀统计数值
     */
    public int statisticEntityKilled(EntityType[] types) {
        int total = 0;
        for (EntityType type : types) {
            total += player.getStatistic(Statistic.KILL_ENTITY, type);
        }
        return total;
    }

    /**
     * 矿物类方块的破坏统计
     * @param materials 矿物类Block的材质
     * @return 已破坏矿物类Block的统计数量
     */
    public int statisticBlockMined(Material[] materials){
        int total = 0;
        for (Material material : materials) {
            total += player.getStatistic(Statistic.MINE_BLOCK, material);
        }
        return total;
    }

    /**
     * 农作物种植及家禽饲养统计
     * @return 家禽Entity的击杀数量与农作物Block的破坏数量的总和
     */
    public int statisticFarming() {
        return statisticEntityKilled(MMOManager.Poultry) + statisticBlockMined(MMOManager.FarmingBlocks);
    }

    /**
     * 钓鱼统计
     * @return 钓鱼统计数值
     */
    public int statisticFishingCaught() {
        return player.getStatistic(Statistic.FISH_CAUGHT);
    }

    public int getStatisticSkill(MMOType type) {
        return switch (type) {
            case SKILL_LUMBERING -> statisticBlockMined(MMOManager.LumberingBlocks);
            case SKILL_FISHING -> statisticFishingCaught();
            case SKILL_FARMING -> statisticFarming();
            case SKILL_MINING -> statisticBlockMined(MMOManager.MiningBlocks);
            case SKILL_COMBAT -> statisticEntityKilled(MMOManager.Monsters);
            default -> 0;
        };
    }

    /**
     * Call this function while player change their equipment every time<br>
     * On EquipmentMonitorRunnable Class<br>
     */
    public void getExtraDataInEquipments() {
        PlayerInventory inv = player.getInventory();
        List<ItemStack> itemStacks = new ArrayList<>(Arrays.stream(inv.getArmorContents()).toList());
        itemStacks.add(inv.getItemInMainHand());
        itemStacks.add(inv.getItemInOffHand());
        setPlayerScale();
        Map<String, Integer> mapAmount = new HashMap<>();

        this.extraArmor = 0;
        this.extraArmorToughness = 0;
        this.extraAttackReach = 0;
        this.extraBloodSucking = 0;
        this.extraCritical = 0;
        this.extraCriticalDamage = 0;
        this.extraRecover = 0;
        this.extraIntelligence = 0;
        this.extraDiggingSpeed = 0;
        this.extraLoggingSpeed = 0;

        // 检查玩家的装备栏和副手物品
        for (ItemStack is : itemStacks) {
            // 忽略没有装备的物品
            if (is == null) continue;
            // 获取玩家身上所有装备和副手物品的额外数值
            NameBinaryTag nbt = new NameBinaryTag(is);
            // 忽略没有ItemMeta的物品
            ItemMeta im = is.getItemMeta();
            if (im == null) continue;
            // 获取套装效果名称和拥有相同套装名称的装备数量
            String armorSetName = nbt.getStringValue(ItemBase.PERSISTENT_ARMOR_SET_KEY);
            int armorAmount = mapAmount.getOrDefault(armorSetName, 0);
            armorAmount++;
            mapAmount.put(armorSetName, armorAmount);
            // Minecraft直接支持的玩家基本属性
            extraArmor += nbt.getFloatValue(ItemBase.PERSISTENT_ARMOR_KEY);
            extraArmorToughness += nbt.getFloatValue(ItemBase.PERSISTENT_ARMOR_TOUGHNESS_KEY);
            // Minecraft不支持的玩家基本属性
            extraRecover += nbt.getFloatValue(ItemBase.PERSISTENT_RECOVER_KEY);
            extraBloodSucking += nbt.getFloatValue(ItemBase.PERSISTENT_BLOOD_SUCKING_KEY);
            extraCritical += nbt.getFloatValue(ItemBase.PERSISTENT_CRITICAL_KEY);
            extraCriticalDamage += nbt.getFloatValue(ItemBase.PERSISTENT_CRITICAL_DAMAGE_KEY);
            extraIntelligence += nbt.getFloatValue(ItemBase.PERSISTENT_INTELLIGENCE);
            extraDiggingSpeed += nbt.getFloatValue(ItemBase.PERSISTENT_DIGGING_SPEED);
            extraLoggingSpeed += nbt.getFloatValue(ItemBase.PERSISTENT_LOGGING_SPEED);
            // Minecraft实验性玩家基本属性，当前版本还不支持
            extraAttackReach += nbt.getFloatValue(ItemBase.PERSISTENT_ATTACK_REACH_KEY);
        }

        Map<String, List<Integer>> newArmorSetEffects = new HashMap<>();
        for (Map.Entry<String, Integer> entry : mapAmount.entrySet()) {
            String name = entry.getKey();
            ArmorSetEffect effect = ArmorSetManager.getArmorSetEffect(name);
            if (effect == null) continue;
            List<Integer> _amount = effect.getAmountActivated(entry.getValue());
            if (_amount.size() <= 0)
                newArmorSetEffects.remove(name);
            else
                newArmorSetEffects.put(name, _amount);
        }
        ArmorSetManager.applyArmorSetEffect(player, newArmorSetEffects);
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

    private void setPlayerScale() {
        double health = getMaxHealth();
        Map<Double, Double> filter = scaleEdges.entrySet().stream().filter(x -> x.getValue() >= health).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        double scale = filter.keySet().stream().min(Comparator.comparing(Double::doubleValue)).orElse(20.0);
        double playerScale = player.getHealthScale();
        if (scale != playerScale) player.setHealthScale(scale);
    }

    private double getBaseAttribute(Attribute attribute, double defaultValue) {
        AttributeInstance instance = player.getAttribute(attribute);
        return instance == null ? defaultValue : instance.getBaseValue();
    }

    private double getMaxAttribute(Attribute attribute, double defaultValue) {
        AttributeInstance instance = player.getAttribute(attribute);
        return instance == null ? defaultValue : instance.getValue();
    }

    private void setBaseAttribute(Attribute attribute, double value) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) return;
        instance.setBaseValue(value);
    }
}

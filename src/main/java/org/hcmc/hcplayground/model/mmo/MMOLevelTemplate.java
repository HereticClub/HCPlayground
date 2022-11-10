package org.hcmc.hcplayground.model.mmo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.hcmc.hcplayground.manager.*;
import org.hcmc.hcplayground.model.item.ItemBase;
import org.hcmc.hcplayground.model.menu.SkillMenuPanel;
import org.hcmc.hcplayground.model.player.PlayerData;
import org.hcmc.hcplayground.model.recipe.CrazyShapedRecipe;
import org.hcmc.hcplayground.serialization.MaterialSerialization;
import org.hcmc.hcplayground.serialization.SkillTypeSerialization;
import org.hcmc.hcplayground.utility.RomanNumber;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MMOLevelTemplate {
    /**
     * 技能等级的显示名称
     */
    @Expose
    @SerializedName(value = "display")
    private String display;
    /**
     * 已达成等级的显示物品，通常是绿色玻璃板
     */
    @Expose
    @SerializedName(value = "reached")
    private Material reachedMaterial = null;
    /**
     * 正在进行的等级的显示物品，通常是黄色玻璃板
     */
    @Expose
    @SerializedName(value = "inprogress")
    private Material currentMaterial = null;
    /**
     * 未达成当前等级时显示的物品
     */
    @Expose
    @SerializedName(value = "unreached")
    private Material unreachedMaterial = null;
    /**
     * 技能等级所在菜单界面的插槽号，以1为开始，最大值: 54
     */
    @Expose
    @SerializedName(value = "slot")
    private int slot;
    /**
     * 表示显示物品的数量
     */
    @Expose
    @SerializedName(value = "amount")
    private int amount = -1;
    /**
     * 通过当前等级的阈值，这是累进数值
     */
    @Expose
    @SerializedName(value = "threshold")
    private int threshold;

    /**
     * 等级说明
     */
    @Expose
    @SerializedName(value = "lore")
    private List<String> lore = new ArrayList<>();

    /**
     * 收集物品或技能的奖励对应列表<br>
     * String - 奖励类型，如果当前等级实例属于技能类型，则值必须是MMOType的值之一，如果属于物品收集类型，则值必须是Material的值之一<br>
     * {@code List<String>} - 奖励id列表
     */
    @Expose(deserialize = false)
    private Map<String, List<String>> rewards = new HashMap<>();
    /**
     * 等级id，包含等级数值
     */
    @Expose(deserialize = false)
    private String id;

    /**
     * 等级数值，由id拆解
     */
    private int level = -1;
    private double knockBack = 0;
    private double armor = 0;
    private double armorToughness = 0;
    private double attackDamage = 0;
    private double attackSpeed = 0;
    private double attackReach = 0;
    private double bloodSucking = 0;
    private double critical = 0;
    private double criticalDamage = 0;
    private double fortune = 0;
    private double health = 0;
    private double intelligence = 0;
    private double money = 0;
    private int point = 0;
    private double recover = 0;
    private double speed = 0;
    private double diggingSpeed = 0;
    private double loggingSpeed = 0;
    /**
     * 可解锁的疯狂配方列表
     */
    private final List<String> recipeIdList = new ArrayList<>();
    /**
     * 奖励项目的物品列表，包含数量<br>
     * String - ItemBase id，也可以是Material<br>
     * Integer - 数量<br>
     */
    private final Map<String, Integer> itemIdList = new HashMap<>();
    /**
     * 可解锁的粒子效果列表
     */
    private final List<String> particleIdList = new ArrayList<>();
    /**
     * 奖励项目的物品实例，由itemIdList属性转换
     */
    private final List<ItemStack> itemStacks = new ArrayList<>();

    public int getLevel() {
        return level;
    }

    public String getId() {
        return id;
    }

    public Map<String, List<String>> getRewards() {
        return new HashMap<>(rewards);
    }

    public void setRewards(Map<String, List<String>> rewards) {
        this.rewards = new HashMap<>(rewards);
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public int getThreshold() {
        return threshold;
    }

    public String getDisplay() {
        return display;
    }

    public Material getCurrentMaterial() {
        return currentMaterial;
    }

    public Material getReachedMaterial() {
        return reachedMaterial;
    }

    public Material getUnreachedMaterial() {
        return unreachedMaterial;
    }

    @NotNull
    public List<String> getLore() {
        List<String> _lore = new ArrayList<>(getRewardLore());
        _lore.addAll(lore);
        return _lore;
    }

    public void setLore(List<String> lore) {
        this.lore = new ArrayList<>(lore);
    }

    public String getName() {
        String[] keys = id.split("\\.");
        return keys[1];
    }

    public MMOLevelTemplate() {

    }

    /**
     * 初始化等级实例
     * @param rewardType 奖励类型，必须是MMOType或者Material的值之一
     * @param display 箱子界面上当前等级实例在插槽上的显示名称
     */
    public void initialize(int level, String rewardType, String display) {
        this.level = level;
        armor = 0;
        armorToughness = 0;
        attackDamage = 0;
        attackSpeed = 0;
        attackReach = 0;
        bloodSucking = 0;
        critical = 0;
        criticalDamage = 0;
        fortune = 0;
        health = 0;
        knockBack = 0;
        intelligence = 0;
        money = 0;
        point = 0;
        recover = 0;
        speed = 0;
        diggingSpeed = 0;
        loggingSpeed = 0;

        itemStacks.clear();
        itemIdList.clear();
        recipeIdList.clear();
        particleIdList.clear();

        if (amount <= 0) amount = 1;
        if (lore == null) lore = new ArrayList<>();
        if (reachedMaterial == null) reachedMaterial = Material.LIME_STAINED_GLASS_PANE;
        if (currentMaterial == null) currentMaterial = Material.YELLOW_STAINED_GLASS_PANE;
        if (unreachedMaterial == null) unreachedMaterial = Material.RED_STAINED_GLASS_PANE;
        if (!StringUtils.isBlank(display) && StringUtils.isBlank(this.display))
            this.display = String.format("%s %s", display, RomanNumber.fromInteger(level));

        String _rewardType = rewardType.toLowerCase();
        SkillMenuPanel.SkillType type = SkillTypeSerialization.valueOf(_rewardType);
        Material material = MaterialSerialization.valueOf(_rewardType);

        List<String> _rewards = new ArrayList<>();
        if (!type.equals(SkillMenuPanel.SkillType.UNDEFINED) && rewards.containsKey(_rewardType)) _rewards = rewards.get(_rewardType);
        if (!material.equals(Material.AIR) && rewards.containsKey(_rewardType)) _rewards = rewards.get(_rewardType);

        for (String rewardId : _rewards) {
            MMOReward reward = RewardManager.getReward(rewardId);
            if (reward != null) calculate(reward);
        }
    }

    public void reward(Player player) {
        PlayerData data = PlayerManager.getPlayerData(player);

        if (armor > 0) data.increaseBaseArmor(armor);
        if (armorToughness > 0) data.increaseBaseArmorToughness(armorToughness);
        if (attackDamage > 0) data.increaseBaseAttackDamage(attackDamage);
        if (attackSpeed > 0) data.increaseBaseAttackSpeed(attackSpeed);
        if (attackReach > 0) data.increaseBaseAttackReach(attackReach);
        if (bloodSucking > 0) data.increaseBaseBloodSucking(bloodSucking);
        if (critical > 0) data.increaseBaseCritical(critical);
        if (criticalDamage > 0) data.increaseBaseCriticalDamage(criticalDamage);
        if (diggingSpeed > 0) data.increaseBaseDiggingSpeed(diggingSpeed);
        if (fortune > 0) data.increaseBaseLuck(fortune);
        if (health > 0) data.increaseBaseHealth(health);
        if (intelligence > 0) data.increaseBaseIntelligence(intelligence);
        if (knockBack > 0) data.increaseBaseKnockBackResistance(knockBack);
        if (loggingSpeed > 0) data.increaseBaseLoggingSpeed(loggingSpeed);
        if (money > 0) data.deposit(money);
        if (recover > 0) data.increaseBaseRecover(recover);
        if (speed > 0) data.increaseBaseMovementSpeed(speed);

        data.unlockRecipe(recipeIdList.toArray(new String[0]));

        for (Map.Entry<String, Integer> entry : itemIdList.entrySet()) {
            ItemBase ib = ItemManager.findItemById(entry.getKey());
            if (ib == null) ib = ItemManager.createItemBase(entry.getKey(), entry.getValue());
            ib.setAmount(entry.getValue());
            itemStacks.add(ib.toItemStack());
        }
        Map<Integer, ItemStack> rewardItemStacks = player.getInventory().addItem(itemStacks.toArray(new ItemStack[0]));
        for (Map.Entry<Integer, ItemStack> entry : rewardItemStacks.entrySet()) {
            player.getWorld().dropItemNaturally(player.getLocation(), entry.getValue());
        }
    }

    /**
     * flag<br>
     * 0: reached<br>
     * 1: in-progress<br>
     * 2: unreached<br>
     */
    public ItemStack setupItemStack(String levelTemplate, int statistic, int flag) {
        MMOLevelTemplate _levelTemplate = MMOManager.getLevelTemplate(levelTemplate);
        // 以下代码必须运行在等级奖励计算后
        if (_levelTemplate != null) {
            if (_levelTemplate.getAmount() >= 1) amount = _levelTemplate.getAmount();
            if (_levelTemplate.getCurrentMaterial() != null)
                currentMaterial = _levelTemplate.getCurrentMaterial();
            if (_levelTemplate.getReachedMaterial() != null)
                reachedMaterial = _levelTemplate.getReachedMaterial();
            if (_levelTemplate.getUnreachedMaterial() != null)
                unreachedMaterial = _levelTemplate.getUnreachedMaterial();
        }

        ItemStack itemStack = switch (flag) {
            case 0 -> new ItemStack(reachedMaterial, amount);
            case 1 -> new ItemStack(currentMaterial, amount);
            case 2 -> new ItemStack(unreachedMaterial, amount);
            default -> new ItemStack(Material.AIR, amount);
        };

        StringBuilder pass = new StringBuilder();
        StringBuilder left = new StringBuilder();
        int progressBarCount = 20;
        float percent = (float) statistic / threshold * 100;
        int progress = (int) percent / 5;

        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return itemStack;
        meta.setDisplayName(display);
        // 制作特定的lore显示 - start
        // 必须使用getLore()方法，而非直接使用lore属性
        List<String> _lore = new ArrayList<>(getLore());
        if (flag == 1) {
            if (_levelTemplate != null) _lore.addAll(_levelTemplate.getLore());
            pass.append("=".repeat(Math.max(0, progress)));
            left.append("-".repeat(progressBarCount - progress));
            String line = String.format("§7 §a§m%s§e§m%s", pass, left);
            _lore.add(line);
        }
        if (flag == 2) {
            if (_levelTemplate != null) _lore.addAll(_levelTemplate.getLore());
        }

        _lore.replaceAll(x -> x.replace("%skill_points%", String.valueOf(statistic))
                .replace("%threshold%", String.valueOf(threshold))
                .replace("%percent%", String.format("%.2f%%", percent)));
        meta.setLore(_lore);
        // 制作特定的lore显示 - end
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    @Override
    public String toString() {
        return String.format("Level x %d, Threshold x %d", level, threshold);
    }

    private void calculate(MMOReward reward) {
        armor += reward.getArmor();
        armorToughness += reward.getArmorToughness();
        attackDamage += reward.getAttackDamage();
        attackSpeed += reward.getAttackSpeed();
        attackReach += reward.getAttackReach();
        bloodSucking += reward.getBloodSucking();
        critical += reward.getCritical();
        criticalDamage += reward.getCriticalDamage();
        fortune += reward.getFortune();
        health += reward.getHealth();
        intelligence += reward.getIntelligence();
        knockBack += reward.getKnockBack();
        money += reward.getMoney();
        point += reward.getPoint();
        recover += reward.getRecover();
        speed += reward.getSpeed();
        diggingSpeed += reward.getDiggingSpeed();
        loggingSpeed += reward.getLoggingSpeed();

        for (String s : reward.getRecipeIds()) {
            if (recipeIdList.stream().noneMatch(x -> x.equalsIgnoreCase(s))) recipeIdList.add(s);
        }
        for (String s : reward.getItemIds()) {
            List<String> keys = Arrays.stream(s.split(",")).map(String::trim).toList();
            if (keys.size() <= 1) continue;
            if (!StringUtils.isNumeric(keys.get(1))) continue;

            String id = keys.get(0);
            int amount = Integer.parseInt(keys.get(1));
            if (itemIdList.containsKey(id)) {
                int _amount = itemIdList.get(id);
                itemIdList.replace(id, amount + _amount);
            } else {
                itemIdList.put(id, amount);
            }
        }
        for (String s : reward.getParticleIds()) {
            if (particleIdList.stream().noneMatch(x -> x.equalsIgnoreCase(s))) particleIdList.add(s);
        }
    }

    private List<String> getRewardLore() {
        List<String> _lore = new ArrayList<>();

        if (health > 0) _lore.add(String.format("%s§a+%.1f", LanguageManager.getString("reword.health"), health));
        if (armor > 0) _lore.add(String.format("%s§a+%.1f", LanguageManager.getString("reword.armor"), armor));
        if (attackDamage > 0)
            _lore.add(String.format("%s§a+%.1f", LanguageManager.getString("reword.attack-damage"), attackDamage));
        if (critical > 0) _lore.add(String.format("%s§a+%.3f", LanguageManager.getString("reword.crit"), critical));
        if (criticalDamage > 0)
            _lore.add(String.format("%s§a+%.2f", LanguageManager.getString("reword.crit-damage"), criticalDamage));
        if (intelligence > 0)
            _lore.add(String.format("%s§a+%.1f", LanguageManager.getString("reword.intelligence"), intelligence));
        if (attackSpeed > 0)
            _lore.add(String.format("%s§a+%.1f", LanguageManager.getString("reword.attack-speed"), attackSpeed));
        if (armorToughness > 0)
            _lore.add(String.format("%s§a+%.1f", LanguageManager.getString("reword.armor-toughness"), armorToughness));
        if (speed > 0) _lore.add(String.format("%s§a+%.1f", LanguageManager.getString("reword.speed"), armor));
        if (recover > 0) _lore.add(String.format("%s§a+%.1f", LanguageManager.getString("reword.recover"), recover));
        if (fortune > 0) _lore.add(String.format("%s§a+%.1f", LanguageManager.getString("reword.fortune"), fortune));
        if (knockBack > 0)
            _lore.add(String.format("%s§a+%.1f", LanguageManager.getString("reword.knock-back"), knockBack));
        if (bloodSucking > 0)
            _lore.add(String.format("%s§a+%.1f", LanguageManager.getString("reword.blood-sucking"), bloodSucking));
        if (attackReach > 0)
            _lore.add(String.format("%s§a+%.1f", LanguageManager.getString("reword.attack-reach"), attackReach));
        if (diggingSpeed > 0)
            _lore.add(String.format("%s§a+%.1f", LanguageManager.getString("reword.digging-speed"), diggingSpeed));
        if (loggingSpeed > 0)
            _lore.add(String.format("%s§a+%.1f", LanguageManager.getString("reword.logging-speed"), loggingSpeed));
        if (money > 0) _lore.add(String.format("%s§a+%.1f", LanguageManager.getString("reword.money"), money));
        if (point > 0) _lore.add(String.format("%s§a+%d", LanguageManager.getString("reword.point"), point));

        for (String s : recipeIdList) {
            CrazyShapedRecipe recipe = RecipeManager.getRecipe(s);
            if (recipe == null) continue;
            ItemStack is = recipe.getResult();
            ItemMeta meta = is.getItemMeta();
            String display = meta == null ? is.getType().name() : meta.getDisplayName();
            _lore.add(String.format("%s%s", LanguageManager.getString("reword.recipe"), display));
        }
        for (Map.Entry<String, Integer> entry : itemIdList.entrySet()) {
            String id = entry.getKey();
            Integer amount = entry.getValue();
            ItemBase ib = ItemManager.findItemById(id);
            Material material = MaterialSerialization.valueOf(id);
            ItemStack itemStack = ib == null ? new ItemStack(material, amount) : ib.toItemStack();
            itemStack.setAmount(amount);
            ItemMeta meta = itemStack.getItemMeta();
            String display = !ItemManager.isItemBase(itemStack) || meta == null ? itemStack.getType().name() : meta.getDisplayName();
            _lore.add(String.format("§f %s §7x §a%d", display, itemStack.getAmount()));
        }

        if (_lore.size() >= 1) _lore.addAll(0, LanguageManager.getStringList("reword.lore"));
        return _lore;
    }
}

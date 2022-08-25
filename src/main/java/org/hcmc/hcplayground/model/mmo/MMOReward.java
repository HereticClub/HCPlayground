package org.hcmc.hcplayground.model.mmo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class MMOReward {
    @Expose
    @SerializedName(value = "money")
    private float money;
    @Expose
    @SerializedName(value = "health")
    private float health;
    @Expose
    @SerializedName(value = "armor")
    private float armor;
    @Expose
    @SerializedName(value = "armor-toughness")
    private float armorToughness;
    @Expose
    @SerializedName(value = "blood-sucking")
    private float bloodSucking;
    @Expose
    @SerializedName(value = "speed")
    private float speed;
    @Expose
    @SerializedName(value = "attack-damage")
    private float attackDamage;
    @Expose
    @SerializedName(value = "attack-speed")
    private float attackSpeed;
    @Expose
    @SerializedName(value = "attack-reach")
    private float attackReach;
    @Expose
    @SerializedName(value = "critical")
    private float critical;
    @Expose
    @SerializedName(value = "critical-damage")
    private float criticalDamage;
    @Expose
    @SerializedName(value = "fortune")
    private float fortune;
    @Expose
    @SerializedName(value = "knock-back")
    private float knockBack;
    @Expose
    @SerializedName(value = "intelligence")
    private float intelligence;
    @Expose
    @SerializedName(value = "digging-speed")
    private float diggingSpeed;
    @Expose
    @SerializedName(value = "logging-speed")
    private float loggingSpeed;
    @Expose
    @SerializedName(value = "recover")
    private float recover;
    @Expose
    @SerializedName(value = "point")
    private int point;
    @Expose
    @SerializedName(value = "items")
    private List<String> itemIds = new ArrayList<>();
    @Expose
    @SerializedName(value = "recipes")
    private List<String> recipeIds = new ArrayList<>();
    @Expose
    @SerializedName(value = "particles")
    private List<String> particleIds = new ArrayList<>();

    @Expose(deserialize = false)
    private String id;

    public MMOReward() {

    }

    public String getId() {
        return id;
    }

    public float getAttackSpeed() {
        return attackSpeed;
    }

    public float getAttackDamage() {
        return attackDamage;
    }

    public float getArmor() {
        return armor;
    }

    public float getHealth() {
        return health;
    }

    public float getFortune() {
        return fortune;
    }

    public float getCritical() {
        return critical;
    }

    public float getCriticalDamage() {
        return criticalDamage;
    }

    public float getMoney() {
        return money;
    }

    public int getPoint() {
        return point;
    }

    public float getKnockBack() {
        return knockBack;
    }

    public float getSpeed() {
        return speed;
    }

    public List<String> getRecipeIds() {
        return recipeIds;
    }

    public List<String> getParticleIds() {
        return particleIds;
    }

    public List<String> getItemIds() {
        return itemIds;
    }

    public float getArmorToughness() {
        return armorToughness;
    }

    public float getIntelligence() {
        return intelligence;
    }

    public float getAttackReach() {
        return attackReach;
    }

    public float getDiggingSpeed() {
        return diggingSpeed;
    }

    public float getLoggingSpeed() {
        return loggingSpeed;
    }

    public float getBloodSucking() {
        return bloodSucking;
    }

    public float getRecover() {
        return recover;
    }

    @Override
    public String toString() {
        return id;
    }

}

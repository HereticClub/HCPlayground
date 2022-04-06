package org.hcmc.hcplayground.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.bukkit.entity.EntityType;

public class MobEntity {

    @Expose
    @SerializedName(value = "minHealth")
    public double minHealth = 0;
    @Expose
    @SerializedName(value = "maxHealth")
    public double maxHealth = 0;
    @Expose
    @SerializedName(value = "minDamage")
    public double minDamage = 0;
    @Expose
    @SerializedName(value = "maxDamage")
    public double maxDamage = 0;
    @Expose
    @SerializedName(value = "type")
    public EntityType type = EntityType.ZOMBIE;
    @Expose
    @SerializedName(value = "displays")
    public String[] displays;
    @Expose
    @SerializedName(value = "drops")
    public ItemBaseA[] drops;
    @Expose
    @SerializedName(value = "spawnRate")
    public float spawnRate;
    @Expose
    @SerializedName(value = "dropRate")
    public float dropRate;
    @Expose
    @SerializedName(value = "id")
    public String id;


    public MobEntity() {

    }
}

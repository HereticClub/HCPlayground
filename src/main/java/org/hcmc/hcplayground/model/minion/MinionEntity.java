package org.hcmc.hcplayground.model.minion;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Pose;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.hcmc.hcplayground.enums.MinionType;
import org.hcmc.hcplayground.manager.MinionManager;
import org.hcmc.hcplayground.utility.Global;
import org.hcmc.hcplayground.utility.RandomNumber;

import java.util.*;

public class MinionEntity {

    @Expose
    @SerializedName(value = "type")
    private String type;
    @Expose
    @SerializedName(value = "level")
    private int level;
    @Expose
    @SerializedName(value = "world")
    private String world;
    @Expose
    @SerializedName(value = "x")
    private double x;
    @Expose
    @SerializedName(value = "y")
    private double y;
    @Expose
    @SerializedName(value = "z")
    private double z;
    @Expose
    @SerializedName(value = "pitch")
    private float pitch;
    @Expose
    @SerializedName(value = "yaw")
    private float yaw;
    @Expose
    @SerializedName(value = "sack")
    private Map<Material, Integer> sack = new HashMap<>();
    @Expose
    @SerializedName(value = "id")
    private UUID uuid;

    @Expose(serialize = false, deserialize = false)
    private String id;
    @Expose(serialize = false, deserialize = false)
    private Location location;
    @Expose(serialize = false, deserialize = false)
    private List<Location> platform = new ArrayList<>();
    @Expose(serialize = false, deserialize = false)
    private Date lastAcquireTime = new Date();
    @Expose(serialize = false, deserialize = false)
    private Entity armorStand;

    public MinionEntity() {

    }

    public MinionEntity(Entity armorStand, MinionType type, int level, Location location) {
        this.type = type.name();
        this.level = level;
        this.armorStand = armorStand;

        x = location.getX();
        y = location.getY();
        z = location.getZ();
        pitch = location.getPitch();
        yaw = location.getYaw();
        this.location = location;

        World w = location.getWorld();
        if (w == null) return;
        world = w.getName();

        initialPlatform();
    }

    public void initialPlatform() {
        if (location == null) location = getLocation();
        platform.clear();
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (x == 0 && z == 0) continue;
                Location l = new Location(location.getWorld(), location.getX() + x, location.getY() - 1, location.getZ() + z);
                platform.add(l);
            }
        }
    }

    public void dressingPlatform() {
        MinionTemplate template = MinionManager.getMinionTemplate(type, level);
        if (template == null || template.getRequirement() == null) return;

        List<Block> blocks = new ArrayList<>();
        for (Location l : platform) {
            Block b = l.getBlock();
            blocks.add(b);
        }

        List<Block> filterBlocks = blocks.stream().filter(x -> !x.getType().equals(template.getRequirement())).toList();
        int size = filterBlocks.size();
        if (size <= 0) return;

        int rnd = RandomNumber.getRandomInteger(size);
        filterBlocks.get(rnd).setType(template.getRequirement());
    }

    public Date getLastAcquireTime() {
        return lastAcquireTime;
    }

    public void setLastAcquireTime(Date lastAcquireTime) {
        this.lastAcquireTime = lastAcquireTime;
    }

    public String getId() {
        return id;
    }

    public Map<Material, Integer> getSack() {
        return sack;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public List<Location> getPlatform() {
        return platform;
    }

    public void setPlatform(List<Location> platform) {
        this.platform = platform;
    }

    public Entity getArmorStand() {
        return armorStand;
    }

    public void setArmorStand(Entity armorStand) {
        this.armorStand = armorStand;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public String getWorld() {
        return world;
    }

    public float getPitch() {
        return pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public Location getLocation() {
        World w = Bukkit.getWorld(world);
        return new Location(w, x, y, z, yaw, pitch);
    }

    public int getLevel() {
        return level;
    }

    public MinionType getType() {
        MinionType[] values = MinionType.values();
        return Arrays.stream(values).filter(x -> x.name().equalsIgnoreCase(this.type)).findAny().orElse(null);
    }
}

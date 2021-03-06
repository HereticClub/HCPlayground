package org.hcmc.hcplayground.model.minion;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.hcmc.hcplayground.enums.MinionPanelSlotType;
import org.hcmc.hcplayground.enums.MinionType;
import org.hcmc.hcplayground.manager.ItemManager;
import org.hcmc.hcplayground.manager.LanguageManager;
import org.hcmc.hcplayground.manager.MinionManager;
import org.hcmc.hcplayground.model.item.ItemBase;
import org.hcmc.hcplayground.utility.RandomNumber;
import org.hcmc.hcplayground.utility.RomanNumber;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MinionEntity {

    @Expose
    @SerializedName(value = "type")
    private MinionType type;
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
    @Expose
    @SerializedName(value = "owner")
    private UUID owner;

    @Expose(serialize = false, deserialize = false)
    private String id;
    @Expose(serialize = false, deserialize = false)
    private Location location;
    @Expose(serialize = false, deserialize = false)
    private List<Location> platform = new ArrayList<>();
    @Expose(serialize = false, deserialize = false)
    private Date lastAcquireTime = new Date();
    @Expose(serialize = false, deserialize = false)
    private ArmorStand armorStand;
    @Expose(serialize = false, deserialize = false)
    private Inventory inventory;

    public MinionEntity() {

    }

    public MinionEntity(ArmorStand armorStand, MinionType type, int level, Location location) {
        this.type = type;
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
        if (template == null || template.getPlatform() == null) return;

        List<Block> blocks = new ArrayList<>();
        for (Location l : platform) {
            Block b = l.getBlock();
            blocks.add(b);
        }

        List<Block> filterBlocks = blocks.stream().filter(x -> !x.getType().equals(template.getPlatform())).toList();
        int size = filterBlocks.size();
        if (size <= 0) return;

        int rnd = RandomNumber.getRandomInteger(size);
        filterBlocks.get(rnd).setType(template.getPlatform());
    }

    public void reduceItemInSack(@NotNull ItemStack... itemStacks) {
        for (ItemStack is : itemStacks) {
            Material material = is.getType();
            int amount = is.getAmount();
            reduceItemInSack(material, amount);
        }
    }

    public void reduceItemInSack(Material material, int amount) {
        if (!sack.containsKey(material)) return;
        int rest = sack.get(material) - amount;
        sack.replace(material, Math.max(rest, 0));
    }

    public void setItemInSack(ItemStack... itemStacks){
        for (ItemStack is : itemStacks) {
            Material material = is.getType();
            int amount = is.getAmount();
            setItemInSack(material, amount);
        }
    }

    public void setItemInSack(Material material, int amount) {
        if (!sack.containsKey(material)) return;
        sack.replace(material, amount);
    }

    public void refreshSack() {
        // ???????????????????????????
        MinionTemplate template = MinionManager.getMinionTemplate(type, level);
        if (template == null) return;
        // ?????????????????????????????????
        int storageAmount = template.getStorageAmount();
        // ??????????????????????????????????????????????????????
        List<ItemStack> acquired = new ArrayList<>();
        Map<Material, Boolean> check = new HashMap<>();
        // ??????????????????????????????(sack???????????????)
        Map<Material, Integer> remainder = new HashMap<>();
        Set<Material> keys = sack.keySet();
        for (Material m : keys) {
            remainder.put(m, sack.get(m));
        }
        // ???????????????????????????????????????????????????
        outer:
        do {
            // ???????????????????????????????????????
            for (Material m : keys) {
                // ?????????????????????
                int restAmount = remainder.get(m);
                // ?????????????????????????????????????????????????????????????????????????????????
                int count = Math.min(restAmount, m.getMaxStackSize());
                // ???????????????0???????????????????????????????????????
                if (restAmount <= 0) {
                    check.put(m, true);
                    continue;
                }
                // ?????????????????????
                acquired.add(new ItemStack(m, count));
                // ????????????????????????????????????
                remainder.replace(m, restAmount - count);
                // ??????????????????????????????????????????????????????
                if (acquired.size() >= storageAmount) break outer;
            }
        } while (check.size() != keys.size());
        // ???????????????????????????????????????????????????
        for (Material m : keys) {
            sack.replace(m, Math.abs(sack.get(m) - remainder.get(m)));
        }
        // ????????????????????????
        if (inventory == null) return;
        if (!(inventory.getHolder() instanceof MinionPanel panel)) return;
        MinionPanelSlot slot = panel.getSlots().stream().filter(x -> x.getType().equals(MinionPanelSlotType.STORAGE)).findAny().orElse(null);
        if (slot == null) return;
        for (int i = 0; i < storageAmount; i++) {
            ItemStack air = new ItemStack(Material.AIR, 1);
            inventory.setItem(slot.getSlots()[i], air);
        }
        inventory.addItem(acquired.toArray(new ItemStack[0]));
    }

    /**
     * ???????????????????????????????????????
     */
    public Inventory openControlPanel() {
        // ?????????????????????????????????????????????
        int storage = 1;
        String title = "";
        MinionTemplate template = MinionManager.getMinionTemplate(type, level);
        if (template == null) return null;
        title = template.getDisplay();
        storage = template.getStorageAmount();

        // ???????????????????????????
        Inventory inventory = MinionManager.createBasePanel(title, this);
        if (!(inventory.getHolder() instanceof MinionPanel panel)) return null;
        // ????????????????????????
        MinionPanelSlot slotStorage = panel.getSlots().stream().filter(x -> x.getType().equals(MinionPanelSlotType.STORAGE)).findAny().orElse(null);
        if (slotStorage == null) return null;
        int[] slotIndexes = slotStorage.getSlots();
        Arrays.sort(slotIndexes);
        // ???????????????????????????
        for (int i = 0; i < storage; i++) {
            inventory.setItem(slotIndexes[i], new ItemStack(Material.AIR, 1));
        }
        // ???????????????????????????????????????
        MinionPanelSlot slotUpgrade = panel.getSlots().stream().filter(x -> x.getType().equals(MinionPanelSlotType.UPGRADE)).findAny().orElse(null);
        if (slotUpgrade == null) return null;
        MinionTemplate nextLevel = MinionManager.getMinionTemplate(type, level + 1);
        for (int i : slotUpgrade.getSlots()) {
            // ???????????????????????????
            ItemStack itemStack = inventory.getItem(i);
            if (itemStack == null) continue;
            ItemMeta meta = itemStack.getItemMeta();
            if (meta == null) continue;
            List<String> lore = meta.getLore();
            if (lore == null) continue;
            // ?????????????????????null???????????????????????????????????????
            if (nextLevel == null) {
                lore = LanguageManager.getStringList("minionMaxLevel");
                lore.replaceAll(x -> x.replace("%current_level%", RomanNumber.fromInteger(level))
                        .replace("%current_period%", String.valueOf(template.getPeriod()))
                        .replace("%current_storage%", String.valueOf(template.getStorageAmount())));
            } else {
                lore.replaceAll(x -> x.replace("%current_level%", RomanNumber.fromInteger(level))
                        .replace("%next_level%", RomanNumber.fromInteger(level + 1))
                        .replace("%current_period%", String.valueOf(template.getPeriod()))
                        .replace("%next_period%", String.valueOf(nextLevel.getPeriod()))
                        .replace("%current_storage%", String.valueOf(template.getStorageAmount()))
                        .replace("%next_storage%", String.valueOf(nextLevel.getStorageAmount())));

                String upgradeLore = slotUpgrade.getUpgradeLore();
                Map<ItemStack, Integer> upgrade = template.getUpgrade();
                for (Map.Entry<ItemStack, Integer> entry : upgrade.entrySet()) {
                    ItemStack isUpgrade = entry.getKey();
                    ItemBase ib = ItemManager.getItemBase(isUpgrade);
                    lore.add(upgradeLore.replace("%name%", ib == null ? isUpgrade.getType().name() : ib.getName())
                            .replace("%amount%", String.valueOf(entry.getValue())));
                }
            }

            meta.setLore(lore);
            itemStack.setItemMeta(meta);
        }

        // ?????????????????????ItemStack????????????player_head
        ItemStack helmet = MinionManager.getMinion(type, level, 1);
        inventory.setItem(4, helmet);

        this.inventory = inventory;
        return inventory;
    }

    public boolean isItemInSack(ItemStack itemStack) {
        if (itemStack == null) return false;
        Material material = itemStack.getType();
        return sack.containsKey(material);
    }

    public ItemStack[] getItemInSack() {
        List<ItemStack> itemStacks = new ArrayList<>();
        for (Material m : sack.keySet()) {
            itemStacks.add(new ItemStack(m, sack.get(m)));
        }
        return itemStacks.toArray(new ItemStack[0]);
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

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
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

    public ArmorStand getArmorStand() {
        return armorStand;
    }

    public void setArmorStand(ArmorStand armorStand) {
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
        return type;
    }
}

package org.hcmc.hcplayground.dropManager;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.hcmc.hcplayground.itemManager.ItemBaseA;
import org.hcmc.hcplayground.itemManager.armor.Armor;
import org.hcmc.hcplayground.itemManager.offhand.OffHand;
import org.hcmc.hcplayground.itemManager.weapon.Weapon;
import org.hcmc.hcplayground.model.Global;
import org.hcmc.hcplayground.model.RandomNumber;

import java.util.ArrayList;
import java.util.List;

public class DropManager {

    private static List<DropItem> dropEntities;

    static {
        dropEntities = new ArrayList<>();
    }

    public DropManager() {

    }

    public static DropItem Find(Material material) {
        return dropEntities.stream().filter(x -> x.block.equals(material)).findAny().orElse(null);
    }

    public static List<DropItem> getDropEntities() {
        return dropEntities;
    }

    public static void Load(YamlConfiguration yaml) throws IllegalAccessException {
        // 在drops.yml文档里获取blocks节段
        ConfigurationSection section = yaml.getConfigurationSection("blocks");
        if (section == null) return;
        // 获取额外掉落物品列表
        dropEntities = Global.SetItemList(section, DropItem.class);
    }

    public static void ExtraDrops(Block b) {
        BlockData bd = b.getBlockData();
        World w = b.getWorld();
        Location l = b.getLocation();
        DropItem de = Find(b.getType());

        if (de == null) return;
        if (bd instanceof Ageable) if (((Ageable) bd).getAge() < de.age) return;
        System.out.printf("BlockData: %s", bd.getClass());

        if (RandomNumber.checkBingo(de.rate)) {
            ItemStack is = null;
            for (ItemBaseA ib : de.drops) {
                if (ib.getId() == null) {
                    is = new ItemStack(ib.getMaterial());
                } else {
                    if (ib instanceof Weapon) is = ((Weapon) ib).toItemStack();
                    if (ib instanceof Armor) is = ((Armor) ib).toItemStack();
                    if (ib instanceof OffHand) is = ((OffHand) ib).toItemStack();
                }
                if (is != null) w.dropItemNaturally(l, is);
            }
        }
    }
}

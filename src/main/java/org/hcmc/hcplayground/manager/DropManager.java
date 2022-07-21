package org.hcmc.hcplayground.manager;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.hcmc.hcplayground.model.item.DropItem;
import org.hcmc.hcplayground.model.item.ItemBase;
import org.hcmc.hcplayground.model.item.CraftItemBase;
import org.hcmc.hcplayground.utility.Global;
import org.hcmc.hcplayground.utility.RandomNumber;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DropManager {

    private static List<DropItem> dropItemList;

    static {
        dropItemList = new ArrayList<>();
    }

    public DropManager() {

    }

    public static DropItem Find(Material material) {
        return dropItemList.stream().filter(x -> Arrays.asList(x.materials).contains(material)).findFirst().orElse(null);
    }

    public static List<DropItem> getDropBreakingList() {
        return dropItemList;
    }

    public static void Load(YamlConfiguration yaml) throws IllegalAccessException {
        // 在drops.yml文档里获取blocks节段
        ConfigurationSection section = yaml.getConfigurationSection("dropList");
        if (section == null) return;
        // 获取额外掉落物品列表
        dropItemList = Global.SetItemList(section, DropItem.class);
    }

    // 钓鱼时的额外掉落
    public static void ExtraDrops(Item item, Player player) {
        Material material = item.getItemStack().getType();

        DropItem de = Find(material);
        if (de == null) return;

        boolean checkBingo = RandomNumber.checkBingo(de.rate);
        if (checkBingo) {
            ItemStack is;
            for (ItemBase ib : de.drops) {
                is = ib.getId() == null ? new ItemStack(ib.getMaterial().value) : ib.toItemStack();
                player.getInventory().addItem(is);
            }
        }
    }

    // 破坏方块时的额外掉落
    public static void ExtraDrops(Block b) {
        BlockData bd = b.getBlockData();
        World w = b.getWorld();
        Location l = b.getLocation();
        DropItem de = Find(b.getType());

        if (de == null) return;
        if (bd instanceof Ageable) if (((Ageable) bd).getAge() < de.age) return;

        boolean checkBingo = RandomNumber.checkBingo(de.rate);
        if (!checkBingo) return;

        ItemStack is;
        for (ItemBase ib : de.drops) {
            if (ib.getId() == null) {
                is = new ItemStack(ib.getMaterial().value);
            } else {
                is = ib.toItemStack();
            }
            w.dropItemNaturally(l, is);
        }
    }

    public static void ExtraDrops(Location location, CraftItemBase[] itemBases) {
        ItemStack is;
        World world = location.getWorld();
        if (world == null) return;

        for (CraftItemBase ib : itemBases) {
            if (ib.getId() == null) {
                is = new ItemStack(ib.getMaterial().value);
            } else {
                is = ib.toItemStack();
            }
            world.dropItemNaturally(location, is);
        }
    }
}

package org.hcmc.hcplayground.Drops;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.hcmc.hcplayground.Deserializer.MaterialDeserializer;
import org.hcmc.hcplayground.Model.RandomNumber;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DropManager {

    private static List<DropEntity> dropEntities;

    static {
        dropEntities = new ArrayList<>();
    }

    public DropManager() {

    }

    public static DropEntity Find(Material material) {
        return dropEntities.stream().filter(x -> x.block.equals(material)).findAny().orElse(null);
    }

    public static List<DropEntity> getDropEntities() {
        return dropEntities;
    }

    public static void Load(YamlConfiguration yaml) {
        // 在items.yml文档里获取items节段
        ConfigurationSection section = yaml.getConfigurationSection("blocks");
        Set<String> itemKeys = section.getKeys(false);

        Gson gson = new GsonBuilder()
                .enableComplexMapKeySerialization()
                .disableHtmlEscaping()
                .registerTypeAdapter(Material.class, new MaterialDeserializer())
                .create();

        dropEntities.clear();
        for (String s : itemKeys) {
            ConfigurationSection itemSection = section.getConfigurationSection(s);
            String Value = gson.toJson(itemSection.getValues(false)).replace('&', '§');
            DropEntity de = gson.fromJson(Value, DropEntity.class);

            dropEntities.add(de);
        }
    }

    public static void AdditionalDrops(Block b){
        BlockData bd = b.getBlockData();
        World w = b.getWorld();
        Location l = b.getLocation();
        DropEntity de = Find(b.getType());

        if (de == null) return;
        if (bd instanceof Ageable) if (((Ageable) bd).getAge() < de.age) return;

        if (RandomNumber.checkBingo(de.rate)) {
            for (Material m : de.drops) {
                ItemStack is = new ItemStack(m);
                w.dropItemNaturally(l, is);
            }
        }
    }
}

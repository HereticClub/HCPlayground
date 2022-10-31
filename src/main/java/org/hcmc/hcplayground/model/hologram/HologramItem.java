package org.hcmc.hcplayground.model.hologram;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.hcmc.hcplayground.sqlite.SqliteManager;

import java.sql.SQLException;
import java.util.*;

public class HologramItem {

    @Expose
    @SerializedName(value = "location")
    private Location location;
    @Expose
    @SerializedName(value = "text")
    private List<String> text = new ArrayList<>();
    @Expose
    @SerializedName(value = "is_template")
    private boolean template;

    @Expose(serialize = false, deserialize = false)
    private String id;
    private final Map<Integer, ArmorStand> mapArmorStand = new HashMap<>();

    public HologramItem() {

    }

    public void create() throws SQLException {
        World world = location.getWorld();
        if (world == null) return;

        mapArmorStand.clear();
        List<UUID> armorStandId = SqliteManager.getArmorStandIdList(id);
        SqliteManager.clearArmorStandRecord(id);

        for (UUID uuid : armorStandId) {
            ArmorStand armorStand = (ArmorStand) Bukkit.getEntity(uuid);
            if (armorStand == null) continue;
            armorStand.remove();
        }

        for (int index = 0; index <= text.size() - 1; index++) {
            ArmorStand armorStand = (ArmorStand) world.spawnEntity(location.add(0, index * -0.3, 0), EntityType.ARMOR_STAND);
            armorStand.setVisible(false);
            armorStand.setCustomName("§7");
            armorStand.setCustomNameVisible(true);
            armorStand.setSmall(true);
            armorStand.setGravity(false);
            mapArmorStand.put(index, armorStand);

            SqliteManager.insertArmorStandId(armorStand.getUniqueId(), id);
        }
    }

    public void update(Player player) {
        for (int index = 0; index <= text.size() - 1; index++) {
            String line = text.get(index);
            String data = PlaceholderAPI.setPlaceholders(player, line);

            ArmorStand armorStand = mapArmorStand.get(index);
            armorStand.setCustomName(data);
        }
    }

    public void addLine(String line) {

    }

    public void updateLine(String line, int index) {

    }
}

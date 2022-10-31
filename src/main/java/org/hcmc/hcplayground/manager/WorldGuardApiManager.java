package org.hcmc.hcplayground.manager;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.hcmc.hcplayground.utility.Global;

import java.util.Map;
import java.util.Set;

public class WorldGuardApiManager {

    public WorldGuardApiManager() {

    }

    public static void addMember(Player player, World world) {
        RegionContainer container = Global.worldGuardApi.getPlatform().getRegionContainer();
        com.sk89q.worldedit.world.World w = BukkitAdapter.adapt(world);
        RegionManager manager = container.get(w);
        if (manager == null) return;

        Map<String, ProtectedRegion> map = manager.getRegions();
        Set<String> keys = map.keySet();
        for (String s : keys) {
            ProtectedRegion region = map.get(s);
            DefaultDomain domain = region.getMembers();

            domain.addPlayer(player.getUniqueId());
        }
    }

    public static void removeMember(Player player, World world) {
        RegionContainer container = Global.worldGuardApi.getPlatform().getRegionContainer();
        com.sk89q.worldedit.world.World w = BukkitAdapter.adapt(world);
        RegionManager manager = container.get(w);
        if (manager == null) return;

        Map<String, ProtectedRegion> map = manager.getRegions();
        Set<String> keys = map.keySet();
        for (String s : keys) {
            ProtectedRegion region = map.get(s);
            DefaultDomain domain = region.getMembers();

            domain.removePlayer(player.getUniqueId());
        }
    }
}

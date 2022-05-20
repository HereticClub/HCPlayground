package org.hcmc.hcplayground.model.player;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.annotations.Expose;
import com.google.gson.reflect.TypeToken;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.utility.Global;

import javax.swing.*;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PlayerParkourStorage {
    @Expose
    private Map<EquipmentSlot, ItemStack> equipments = new HashMap<>();
    @Expose
    private Map<Integer, ItemStack> contents = new HashMap<>();
    @Expose
    private boolean parkourSetting = false;

    @Expose(serialize = false, deserialize = false)
    private final Player player;
    private final Plugin plugin;

    public PlayerParkourStorage(Player player) {
        this.player = player;
        plugin = HCPlayground.getPlugin();
    }

    public Map<EquipmentSlot, ItemStack> getEquipments() {
        return equipments;
    }

    public Map<Integer, ItemStack> getContents() {
        return contents;
    }

    public boolean isParkourSetting() {
        return parkourSetting;
    }

    /**
     * 获取玩家身上所有物品并且放入到储存器，包括背包和装备栏，主副手等物品，并且以json格式保存到玩家文档
     */
    public void obtainStorage() throws IOException, ClassNotFoundException {
        obtainEquipments();
        obtainContents();
        parkourSetting = true;

        saveFile();
    }

    /**
     * 将储存器的物品发回给玩家，包括背包和装备栏，主副手等物品，然后清空玩家储存文档
     *
     * @throws IOException
     */
    public void replaceStorage() throws IOException, ClassNotFoundException {
        loadFile();
        replaceEquipments();
        replaceContents();

        parkourSetting = false;
        this.contents.clear();
        this.equipments.clear();
        saveFile();
    }

    private void saveFile() throws IOException {
        UUID uuid = player.getUniqueId();
        String file = String.format("%s/storage/%s.parkour.json", plugin.getDataFolder(), uuid);
        String value = Global.GsonObject.toJson(this, PlayerParkourStorage.class);

        Type t1 = new TypeToken<Map<EquipmentSlot, ItemStack>>(){}.getType();
        Type t2 = new TypeToken<Map<Integer, ItemStack>>(){}.getType();

        String v1 = Global.GsonObject.toJson(this.equipments, t1);
        String v2 = Global.GsonObject.toJson(this.contents, t2);
        System.out.println(v1);
        System.out.println(v2);

        Path path = Paths.get(file);
        Files.writeString(path, value, Charset.defaultCharset());
    }

    private void loadFile() throws IOException {
        UUID uuid = player.getUniqueId();
        String file = String.format("%s/storage/%s.parkour.json", plugin.getDataFolder(), uuid);
        //ObjectMapper mapper = new ObjectMapper();
        Path path = Paths.get(file);
        PlayerParkourStorage storage;

        String value = Files.readString(path, Charset.defaultCharset());
        storage = Global.GsonObject.fromJson(value, this.getClass());
        //storage = mapper.readValue(value, PlayerParkourStorage.class);

        this.equipments = storage.getEquipments();
        this.contents = storage.getContents();
        this.parkourSetting = storage.isParkourSetting();
    }

    private void obtainEquipments() {
        PlayerInventory inv = player.getInventory();
        EquipmentSlot[] slots = EquipmentSlot.values();
        equipments.clear();

        for (EquipmentSlot s : slots) {
            ItemStack is = inv.getItem(s);
            equipments.put(s, is);
        }
    }

    private void obtainContents() {
        PlayerInventory inv = player.getInventory();
        contents.clear();

        for (int index = 0; index < 36; index++) {
            ItemStack is = inv.getItem(index);
            if (is == null) continue;

            contents.put(index, is);
        }
    }

    private void replaceContents() {
        PlayerInventory inv = player.getInventory();
        Set<Integer> index = contents.keySet();

        for (int i : index) {
            ItemStack is = contents.get(i);
            inv.setItem(i, is);
        }
    }

    private void replaceEquipments() {
        PlayerInventory inv = player.getInventory();
        EquipmentSlot[] slots = EquipmentSlot.values();

        for (EquipmentSlot s : slots) {
            ItemStack is = equipments.get(s);
            inv.setItem(s, is);
        }
    }
}

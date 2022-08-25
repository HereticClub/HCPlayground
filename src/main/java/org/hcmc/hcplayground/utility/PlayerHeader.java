package org.hcmc.hcplayground.utility;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.UUID;

public class PlayerHeader {

    private static final String GAME_PROFILE_PROPERTY_TEXTURES = "textures";
    private final static String SKULL_META_FIELD_PROFILE = "profile";

    public PlayerHeader() {

    }

    public static ItemMeta setTextures(ItemStack item, String base64Value) {
        ItemMeta im = item.getItemMeta();
        if (!(im instanceof SkullMeta meta)) return im;

        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        PropertyMap pm = profile.getProperties();
        Property pp = new Property(GAME_PROFILE_PROPERTY_TEXTURES, base64Value);
        pm.put(GAME_PROFILE_PROPERTY_TEXTURES, pp);

        try {
            Field field = meta.getClass().getDeclaredField(SKULL_META_FIELD_PROFILE);
            field.setAccessible(true);
            field.set(meta, profile);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return meta;
    }
}

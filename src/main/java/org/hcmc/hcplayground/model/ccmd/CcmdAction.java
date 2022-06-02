package org.hcmc.hcplayground.model.ccmd;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.hcmc.hcplayground.enums.CcmdActionType;

public class CcmdAction {

    @Expose
    @SerializedName(value = "text")
    private String text;
    @Expose
    @SerializedName(value = "duration")
    private long duration;
    @Expose
    @SerializedName(value = "location")
    private Location location;
    @Expose
    @SerializedName(value = "sound")
    private Sound sound;
    @Expose
    @SerializedName(value = "type")
    private CcmdActionType type;

    @Expose(serialize = false, deserialize = false)
    private String id;

    public CcmdAction() {

    }

    public String getId() {
        return id;
    }

    public Location getLocation() {
        return location;
    }

    public long getDuration() {
        return duration;
    }

    public Sound getSound() {
        return sound;
    }

    public String getText() {
        return text;
    }

    public CcmdActionType getType() {
        return type;
    }
}

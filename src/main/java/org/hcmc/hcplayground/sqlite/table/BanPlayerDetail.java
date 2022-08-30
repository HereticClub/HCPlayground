package org.hcmc.hcplayground.sqlite.table;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.hcmc.hcplayground.annotation.SqliteColumn;

import java.util.Date;
import java.util.UUID;

public class BanPlayerDetail {

    @Expose
    @SerializedName(value = "playerId")
    @SqliteColumn(name = "playerId")
    public UUID playerUuid;

    @Expose
    @SerializedName(value = "masterId")
    @SqliteColumn(name = "masterId")
    public UUID masterUuid;

    @Expose
    @SerializedName(value = "message")
    @SqliteColumn(name = "message")
    public String message;

    @Expose
    @SerializedName(value = "playerName")
    @SqliteColumn(name = "playerName")
    public String playerName;

    @Expose
    @SerializedName(value = "masterName")
    @SqliteColumn(name = "masterName")
    public String masterName;

    @Expose
    @SerializedName(value = "banDate")
    @SqliteColumn(name = "banDate")
    public Date banDate;

    @Expose
    @SerializedName(value = "getBanDetail")
    @SqliteColumn(name = "getBanDetail")
    public boolean isBanned;

    public BanPlayerDetail() {

    }
}

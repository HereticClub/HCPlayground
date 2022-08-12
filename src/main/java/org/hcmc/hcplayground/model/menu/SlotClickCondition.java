package org.hcmc.hcplayground.model.menu;

import com.comphenix.protocol.PacketType;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import me.clip.placeholderapi.PlaceholderAPI;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.hcmc.hcplayground.enums.CompareType;
import org.hcmc.hcplayground.enums.OperatorType;
import org.hcmc.hcplayground.manager.LanguageManager;
import org.hcmc.hcplayground.utility.Global;

import java.util.Date;
import java.util.logging.Level;

public class SlotClickCondition {

    @Expose
    @SerializedName(value = "compare-type")
    private CompareType compareType;
    @Expose
    @SerializedName(value = "operator-type")
    private OperatorType operatorType;
    @Expose
    @SerializedName(value = "target-value")
    private String targetValue;
    @Expose
    @SerializedName(value = "source-value")
    private String sourceValue;

    @Expose(serialize = false, deserialize = false)
    private String id;

    public String getId() {
        return id;
    }

    public CompareType getCompareType() {
        return compareType;
    }

    public OperatorType getOperatorType() {
        return operatorType;
    }

    public String getSourceValue() {
        return sourceValue;
    }

    public String getTargetValue() {
        return targetValue;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCompareType(CompareType compareType) {
        this.compareType = compareType;
    }

    public void setOperatorType(OperatorType operatorType) {
        this.operatorType = operatorType;
    }

    public void setSourceValue(String sourceValue) {
        this.sourceValue = sourceValue;
    }

    public void setTargetValue(String targetValue) {
        this.targetValue = targetValue;
    }

    public SlotClickCondition() {

    }

    public boolean getResult(Player player) {

        Global.LogDebug(new Date(), this.getClass().getSimpleName(), "getResult", Level.INFO, toString());

        switch (compareType) {
            case COMPARE_PERMISSION -> {
                switch (operatorType) {
                    case EQUAL -> {
                        return player.hasPermission(sourceValue);
                    }
                    case NOT_EQUAL -> {
                        return !player.hasPermission(sourceValue);
                    }
                }
            }
            case COMPARE_STRING -> {
                switch (operatorType) {
                    case EQUAL -> {
                        return sourceValue.equalsIgnoreCase(targetValue);
                    }
                    case NOT_EQUAL -> {
                        return !sourceValue.equalsIgnoreCase(targetValue);
                    }
                }
            }
            case COMPARE_BOOLEAN -> {
                boolean tmpSource = Boolean.parseBoolean(sourceValue);
                boolean tmpTarget = Boolean.parseBoolean(targetValue);
                switch (operatorType) {
                    case EQUAL -> {
                        return tmpSource == tmpTarget;
                    }
                    case NOT_EQUAL -> {
                        return tmpSource != tmpTarget;
                    }
                }
            }
            case COMPARE_NUMERIC -> {
                if (!StringUtils.isNumeric(sourceValue) || !StringUtils.isNumeric(targetValue)) {
                    Global.LogMessage("Menu slot click condition judgment fatal error!");
                    Global.LogWarning(LanguageManager.getString("numberFormatInvalid").replace("%numeric%", String.format("source: %s target: %s", sourceValue, targetValue)));
                    return false;
                }
                double tmpSource = Double.parseDouble(sourceValue);
                double tmpTarget = Double.parseDouble(targetValue);
                switch (operatorType) {
                    case EQUAL -> {
                        return tmpSource == tmpTarget;
                    }
                    case NOT_EQUAL -> {
                        return tmpSource != tmpTarget;
                    }
                    case GREATER -> {
                        return tmpSource > tmpTarget;
                    }
                    case LESS -> {
                        return tmpSource < tmpTarget;
                    }
                    case GREATER_AND_EQUAL -> {
                        return tmpSource >= tmpTarget;
                    }
                    case LESS_AND_EQUAL -> {
                        return tmpSource <= tmpTarget;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("%s: %s %s %s", compareType, sourceValue, operatorType, targetValue);
    }
}

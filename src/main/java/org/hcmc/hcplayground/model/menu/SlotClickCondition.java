package org.hcmc.hcplayground.model.menu;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.hcmc.hcplayground.enums.CompareType;
import org.hcmc.hcplayground.enums.OperatorType;
import org.hcmc.hcplayground.manager.LanguageManager;
import org.hcmc.hcplayground.utility.Global;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
    private Object targetValue;
    @Expose
    @SerializedName(value = "source-value")
    private String sourceValue;

    @Expose(serialize = false, deserialize = false)
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
                        return sourceValue.equalsIgnoreCase(String.valueOf(targetValue));
                    }
                    case NOT_EQUAL -> {
                        return !sourceValue.equalsIgnoreCase(String.valueOf(targetValue));
                    }
                }
            }
            case COMPARE_BOOLEAN -> {
                boolean tmpSource = Boolean.parseBoolean(sourceValue);
                boolean tmpTarget = Boolean.parseBoolean(String.valueOf(targetValue));
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
                if (!StringUtils.isNumeric(sourceValue) || !StringUtils.isNumeric(String.valueOf(targetValue))) {
                    Global.LogMessage("Menu slot click condition judgment fatal error!");
                    Global.LogWarning(LanguageManager.getString("numberFormatInvalid").replace("%numeric%", String.format("source: %s target: %s", sourceValue, targetValue)));
                    return false;
                }
                double tmpSource = Double.parseDouble(sourceValue);
                double tmpTarget = Double.parseDouble(String.valueOf(targetValue));
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
            case COMPARE_STRING_LIST -> {
                List<String> values = new ArrayList<>();
                if (!(targetValue instanceof List<?>)) return false;
                for (Object o : (List<?>) targetValue) {
                    values.add(String.valueOf(o));
                }

                switch (operatorType) {
                    case CONTAINED -> {
                        return values.contains(sourceValue);
                    }
                    case NOT_CONTAINED -> {
                        return !values.contains(sourceValue);
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

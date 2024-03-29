package org.hcmc.hcplayground.model.mmo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.hcmc.hcplayground.enums.RecipeType;
import org.hcmc.hcplayground.model.menu.RecipeMenuPanel;

public class MMORecipeTemplate {
    @Expose
    @SerializedName(value = "title")
    private String title;
    @Expose
    @SerializedName(value = "type")
    private RecipeType type;

    @Expose(deserialize = false)
    private String id;

    public String getTitle() {
        return title;
    }

    public RecipeType getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public MMORecipeTemplate() {

    }
}

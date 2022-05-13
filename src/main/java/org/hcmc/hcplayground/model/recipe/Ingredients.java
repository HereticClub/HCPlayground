package org.hcmc.hcplayground.model.recipe;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

public class Ingredients {
    @Expose
    private final Map<Character, String > values = new HashMap<>();
    @Expose
    public final String A = "";

    public Map<Character, String> getValues() {
        return values;
    }

    public Ingredients(){

    }
}

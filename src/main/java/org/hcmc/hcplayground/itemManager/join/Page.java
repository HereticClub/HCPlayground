package org.hcmc.hcplayground.itemManager.join;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Page {
    /**
     * 页码
     */
    @Expose
    @SerializedName(value = "number")
    public int Number = 0;
    /**
     * 页内容
     */
    @Expose
    @SerializedName(value = "content")
    public List<String> Content = new ArrayList<>();

    public Page() {

    }
}

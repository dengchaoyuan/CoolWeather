package com.dcy.coolweather.gson;

import com.google.gson.annotations.SerializedName;

public class Now {

    @SerializedName("tmp")
    public String tmp;

    @SerializedName("cond")
    public NowCond nowCond;

    public class NowCond{
        @SerializedName("txt")
        public String txt;
    }
}

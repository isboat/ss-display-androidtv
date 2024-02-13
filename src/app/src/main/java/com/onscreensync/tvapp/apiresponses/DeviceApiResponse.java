package com.onscreensync.tvapp.apiresponses;

import com.google.gson.annotations.SerializedName;

public class DeviceApiResponse {
    @SerializedName("deviceName")
    private String name;

    @SerializedName("tenantId")
    private String tenantId;

    @SerializedName("id")
    private String id;

    public String getName() {
        return name;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getId() {
        return id;
    }
}

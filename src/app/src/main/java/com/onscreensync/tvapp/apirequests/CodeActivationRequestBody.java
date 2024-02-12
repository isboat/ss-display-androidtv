package com.onscreensync.tvapp.apirequests;

public class CodeActivationRequestBody {

    public CodeActivationRequestBody(String clientId, String grantType)
    {
        this.clientId = clientId;
        this.grantType = grantType;
    }
    private String clientId;
    private String grantType;

    public String getGrantType() {
        return grantType;
    }

    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}

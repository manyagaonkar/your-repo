package com.example.webhooker.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GenerateResponse {
    private String webhook;
    private String accessToken;
    public GenerateResponse() {}
    @JsonProperty("webhook")
    public String getWebhook() { return webhook; }
    public void setWebhook(String webhook) { this.webhook = webhook; }
    @JsonProperty("accessToken")
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
}

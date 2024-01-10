package de.teamA.SWT.entities.reqres;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthenticationCodeResponse {

    @JsonProperty("access_token")
    public String accessToken;

    @JsonProperty("token_type")
    public String tokenType;

    @JsonProperty("refresh_token")
    public String refreshToken;

    @JsonProperty("scope")
    public String scope;

    @JsonProperty("created_at")
    public Double createdAt;

    @JsonProperty("id_token")
    public String idToken;

}

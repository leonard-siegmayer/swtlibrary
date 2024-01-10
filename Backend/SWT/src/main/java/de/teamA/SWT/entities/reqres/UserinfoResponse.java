package de.teamA.SWT.entities.reqres;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserinfoResponse {

    @JsonProperty("sub")
    public String sub;

    @JsonProperty("name")
    public String name;

    @JsonProperty("nickname")
    public String nickname;

    @JsonProperty("email")
    public String email;

    @JsonProperty("email_verified")
    public boolean emailIsVerified;

    @JsonProperty("picture")
    public String pictureUrl;

    @JsonProperty("profile")
    public String gitlabProfileUrl;

}

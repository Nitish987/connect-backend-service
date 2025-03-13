package com.conceptune.connect.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthToken {

    @JsonProperty("_id")
    private String userId;

    @JsonProperty("_at")
    private String accessToken;

    @JsonProperty("_rt")
    private String refreshToken;

    @JsonProperty("_lst")
    private String loginStateToken;

    @JsonProperty("_fat")
    private String firebaseAuthToken;

    @JsonProperty("_new")
    private Boolean newUser;
}

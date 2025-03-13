package com.conceptune.connect.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NewUserToken {

    @JsonProperty("_t")
    private String token;

    @JsonProperty("_new")
    private Boolean newUser;
}

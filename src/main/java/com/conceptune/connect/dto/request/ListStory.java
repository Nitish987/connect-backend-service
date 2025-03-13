package com.conceptune.connect.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ListStory {

    @NotNull(message = "User-ids required.")
    private List<String> userIds;
}

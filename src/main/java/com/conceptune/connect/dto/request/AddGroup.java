package com.conceptune.connect.dto.request;

import com.conceptune.connect.utils.Regex;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

@Data
public class AddGroup {

    @NotNull(message = "Group name required")
    @Pattern(regexp = Regex.NO_HTML)
    private String name;

    @NotNull(message = "Group member required.")
    private List<String> memberUserIds;
}

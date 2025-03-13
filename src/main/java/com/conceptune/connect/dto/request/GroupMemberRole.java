package com.conceptune.connect.dto.request;

import com.conceptune.connect.utils.Regex;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class GroupMemberRole {

    @NotNull(message = "Role is required.")
    @Pattern(regexp = Regex.NO_HTML, message = "Invalid role specified.")
    @Pattern(regexp = "^(ADMIN|MEMBER)$", message = "Invalid role specified.")
    private String role;

    @NotNull(message = "UserId is required.")
    @Pattern(regexp = Regex.NO_HTML, message = "Invalid userId specified.")
    private String userId;
}

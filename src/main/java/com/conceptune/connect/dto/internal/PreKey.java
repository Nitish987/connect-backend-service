package com.conceptune.connect.dto.internal;

import com.conceptune.connect.utils.Regex;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PreKey {

    @NotNull(message = "Pre-key Id required")
    private Long id;

    @NotNull(message = "Registration Id required.")
    @Pattern(regexp = Regex.NO_HTML)
    private String key;
}

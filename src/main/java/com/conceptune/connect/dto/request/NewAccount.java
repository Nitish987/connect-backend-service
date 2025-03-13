package com.conceptune.connect.dto.request;

import com.conceptune.connect.utils.Regex;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class NewAccount {

    @NotNull(message = "Required Name.")
    @Pattern(regexp = Regex.NO_HTML)
    private String name;
}

package com.conceptune.connect.dto.request;

import com.conceptune.connect.utils.Regex;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AddStory {

    @NotNull(message = "Story Type required.")
    @Pattern(regexp = Regex.NO_HTML)
    private String type;

    @NotNull(message = "Story Content required.")
    @Pattern(regexp = Regex.NO_HTML)
    private String content;
}

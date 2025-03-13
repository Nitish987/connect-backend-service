package com.conceptune.connect.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class Phones {

    @NotNull(message = "Phone hashes cannot be null or empty.")
    private List<String> hashes;
}

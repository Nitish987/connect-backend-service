package com.conceptune.connect.broker.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class MultiMessage {

    @NotNull(message = "Messages required.")
    private List<Message> messages;
}

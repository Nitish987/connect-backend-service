package com.conceptune.connect.broker.dto;

import com.conceptune.connect.utils.Regex;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.json.JSONObject;

import java.util.Map;

@Data
public class Message {

    @NotNull(message = "Message Id required.")
    @Pattern(regexp = Regex.NO_HTML)
    private String id;

    @NotNull(message = "Message senderId required.")
    @Pattern(regexp = Regex.NO_HTML)
    private String senderId;

    @NotNull(message = "Message recipientId required.")
    @Pattern(regexp = Regex.NO_HTML)
    private String recipientId;

    @NotNull(message = "Message connectId required.")
    @Pattern(regexp = Regex.NO_HTML)
    private String connectId;

    @NotNull(message = "Message connectType required.")
    @Pattern(regexp = Regex.NO_HTML)
    private String connectType;

    @NotNull(message = "Message status required.")
    @Pattern(regexp = Regex.NO_HTML)
    private String status;

    @NotNull(message = "Message contentType required.")
    @Pattern(regexp = Regex.NO_HTML)
    private String contentType;

    @NotNull(message = "Message content required.")
    @Pattern(regexp = Regex.NO_HTML)
    private String content;

    @NotNull(message = "Message time required.")
    private Long time;

    public Map<String, Object> toMap() {
        return new JSONObject(this).toMap();
    }

    public String toJsonString() {
        return new JSONObject(this).toString();
    }
}

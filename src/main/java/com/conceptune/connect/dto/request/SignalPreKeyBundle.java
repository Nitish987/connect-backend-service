package com.conceptune.connect.dto.request;

import com.conceptune.connect.utils.Regex;
import com.conceptune.connect.dto.internal.PreKey;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

@Data
public class SignalPreKeyBundle {

    @NotNull(message = "Device Id required.")
    private Long deviceId;

    @NotNull(message = "Registration Id required.")
    private Long registrationId;

    @NotNull(message = "Signed Pre-key required.")
    @Pattern(regexp = Regex.NO_HTML)
    private String signedPreKey;

    @NotNull(message = "Signed Signature required.")
    @Pattern(regexp = Regex.NO_HTML)
    private String signature;

    @NotNull(message = "Identity Key required.")
    @Pattern(regexp = Regex.NO_HTML)
    private String identityKey;

    @NotNull(message = "Identity Key required.")
    private List<PreKey> preKeys;
}

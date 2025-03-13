package com.conceptune.connect.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OtpPayload {

    @NotNull(message = "Otp Required.")
    @Size(min = 6, max = 6, message = "Invalid OTP.")
    private String otp;
}

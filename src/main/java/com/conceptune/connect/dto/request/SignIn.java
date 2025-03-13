package com.conceptune.connect.dto.request;

import com.conceptune.connect.utils.Regex;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignIn {

    @NotNull(message = "Country required.")
    @Pattern(regexp = Regex.NO_HTML)
    private String country;

    @NotNull(message = "Country code required.")
    @Size(min = 2, max = 5, message = "Invalid country code.")
    private String countryCode;

    @NotNull(message = "Phone number required.")
    @Size(min = 10, max = 15, message = "Invalid phone number.")
    @Pattern(regexp = Regex.ONLY_NUMBERS, message = "Invalid Phone number.")
    private String phone;

    @NotNull(message = "Message token required.")
    @Pattern(regexp = Regex.NO_HTML, message = "Invalid message token.")
    private String messageToken;
}

package com.conceptune.connect.dto.response;

import lombok.Data;

@Data
public class Contact {
    private String id;
    private String name;
    private String username;
    private String country;
    private String countryCode;
    private String hash;
    private String phone;
    private String title;
    private String photo;
}

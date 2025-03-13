package com.conceptune.connect.utils;

public class Regex {

    public static final String NO_HTML = "^(?!.*<[^>]+>).*";
    public static final String VALID_PASSWORD = "(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>?/]*";
    public static final String ONLY_NUMBERS = "^\\d+$";
    public static final String VALID_HTTP_URL = "^(https?:\\/\\/)?([a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})(:[0-9]{1,5})?(\\/[^\\s]*)?$";
}

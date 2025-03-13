package com.conceptune.connect.utils;

import lombok.Data;
import lombok.AllArgsConstructor;
import org.json.JSONObject;

import java.util.Map;

@Data
@AllArgsConstructor
public class Response<T> {
    private boolean success;
    private String message;
    private T data;

    public static <T> Response<T> success(String message, T data) {
        return new Response<>(true, message, data);
    }

    public static Response<?> success(String message) {
        return new Response<>(true, message, null);
    }

    public static Response<?> token(String token) {
        return new Response<>(true, "", Map.of("_t", token));
    }

    public static Response<?> error(String message) {
        return new Response<>(false, message, null);
    }

    public static <T> Response<T> error(String message, T data) {
        return new Response<>(false, message, data);
    }

    public String toJsonString() {
        return new JSONObject(this).toString();
    }
}

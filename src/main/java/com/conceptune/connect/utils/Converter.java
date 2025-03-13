package com.conceptune.connect.utils;

import java.util.Arrays;
import java.util.stream.Collectors;

public class Converter {

    public static String toTitleCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        return Arrays.stream(input.split(" ")).map(word -> word.isEmpty() ? word : Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase()).collect(Collectors.joining(" "));
    }
}

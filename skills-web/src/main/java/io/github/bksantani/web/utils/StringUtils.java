package io.github.bksantani.web.utils;

import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public class StringUtils {

    public static String valueOrEmptyString(String value) {
        return value == null ? "" : value;
    }
}

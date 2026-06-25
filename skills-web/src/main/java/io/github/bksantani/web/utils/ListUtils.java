package io.github.bksantani.web.utils;

import static lombok.AccessLevel.PRIVATE;

import java.util.Collections;
import java.util.List;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public class ListUtils {

    public static <T> List<T> responseOrEmptyList(List<T> value) {
        return value == null ? Collections.emptyList() : value;
    }
}

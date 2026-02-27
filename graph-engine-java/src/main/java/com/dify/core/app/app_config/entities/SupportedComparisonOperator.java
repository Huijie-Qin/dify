package com.dify.core.app.app_config.entities;

import java.util.Arrays;
import lombok.Getter;

@Getter
public enum SupportedComparisonOperator {
    CONTAINS("contains"),
    NOT_CONTAINS("not contains"),
    START_WITH("start with"),
    END_WITH("end with"),
    IS("is"),
    IS_NOT("is not"),
    EMPTY("empty"),
    NOT_EMPTY("not empty"),
    IN("in"),
    NOT_IN("not in"),
    EQUAL("="),
    NOT_EQUAL("≠"),
    GREATER_THAN(">"),
    LESS_THAN("<"),
    GREATER_OR_EQUAL("≥"),
    LESS_OR_EQUAL("≤"),
    BEFORE("before"),
    AFTER("after");

    private final String value;

    SupportedComparisonOperator(String value) {
        this.value = value;
    }

    public static SupportedComparisonOperator valueOfLabel(String value) {
        return Arrays.stream(values())
                .filter(item -> item.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("invalid comparison operator value " + value));
    }
}

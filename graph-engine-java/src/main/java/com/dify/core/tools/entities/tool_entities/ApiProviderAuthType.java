package com.dify.core.tools.entities.tool_entities;

import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public enum ApiProviderAuthType {
    NONE("none"),
    API_KEY_HEADER("api_key_header"),
    API_KEY_QUERY("api_key_query");

    private final String value;

    ApiProviderAuthType(String value) {
        this.value = value;
    }

    public static ApiProviderAuthType valueOfLabel(String value) {
        String v = value == null ? "" : value.trim().toLowerCase();
        if ("api_key".equals(v)) {
            v = API_KEY_HEADER.value;
        }
        final String normalized = v;
        return Arrays.stream(values())
                .filter(mode -> mode.value.equals(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "invalid mode value '" + value + "', expected one of: "
                                + Arrays.stream(values()).map(ApiProviderAuthType::getValue).collect(Collectors.joining(", "))));
    }
}

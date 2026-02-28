package com.dify.core.tools.entities.tool_entities;

import java.util.Arrays;
import lombok.Getter;

@Getter
public enum ApiProviderSchemaType {
    OPENAPI("openapi"),
    SWAGGER("swagger"),
    OPENAI_PLUGIN("openai_plugin"),
    OPENAI_ACTIONS("openai_actions");

    private final String value;

    ApiProviderSchemaType(String value) {
        this.value = value;
    }

    public static ApiProviderSchemaType valueOfLabel(String value) {
        return Arrays.stream(values())
                .filter(mode -> mode.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("invalid mode value " + value));
    }
}

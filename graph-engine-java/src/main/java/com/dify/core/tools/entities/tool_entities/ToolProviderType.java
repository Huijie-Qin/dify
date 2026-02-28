package com.dify.core.tools.entities.tool_entities;

import java.util.Arrays;
import lombok.Getter;

@Getter
public enum ToolProviderType {
    PLUGIN("plugin"),
    BUILT_IN("builtin"),
    WORKFLOW("workflow"),
    API("api"),
    APP("app"),
    DATASET_RETRIEVAL("dataset-retrieval"),
    MCP("mcp");

    private final String value;

    ToolProviderType(String value) {
        this.value = value;
    }

    public static ToolProviderType valueOfLabel(String value) {
        return Arrays.stream(values())
                .filter(mode -> mode.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("invalid mode value " + value));
    }
}

package com.dify.core.plugin.entities.parameters;

import lombok.Getter;

@Getter
public enum MCPServerParameterType {
    ARRAY("array"),
    OBJECT("object");

    private final String value;

    MCPServerParameterType(String value) {
        this.value = value;
    }
}

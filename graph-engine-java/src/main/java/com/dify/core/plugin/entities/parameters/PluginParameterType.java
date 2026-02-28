package com.dify.core.plugin.entities.parameters;

import lombok.Getter;

@Getter
public enum PluginParameterType {
    STRING("string"),
    NUMBER("number"),
    BOOLEAN("boolean"),
    SELECT("select"),
    SECRET_INPUT("secret-input"),
    FILE("file"),
    FILES("files"),
    APP_SELECTOR("app-selector"),
    MODEL_SELECTOR("model-selector"),
    TOOLS_SELECTOR("tools-selector"),
    ANY("any"),
    DYNAMIC_SELECT("dynamic-select"),
    CHECKBOX("checkbox"),
    SYSTEM_FILES("system-files"),
    ARRAY("array"),
    OBJECT("object");

    private final String value;

    PluginParameterType(String value) {
        this.value = value;
    }
}

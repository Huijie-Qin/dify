package com.dify.core.app.app_config.entities;

import java.util.Arrays;
import lombok.Getter;

@Getter
public enum VariableEntityType {
    TEXT_INPUT("text-input"),
    SELECT("select"),
    PARAGRAPH("paragraph"),
    NUMBER("number"),
    EXTERNAL_DATA_TOOL("external_data_tool"),
    FILE("file"),
    FILE_LIST("file-list"),
    CHECKBOX("checkbox"),
    JSON_OBJECT("json_object");

    private final String value;

    VariableEntityType(String value) {
        this.value = value;
    }

    public static VariableEntityType valueOfLabel(String value) {
        return Arrays.stream(values())
                .filter(item -> item.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("invalid variable type value " + value));
    }
}

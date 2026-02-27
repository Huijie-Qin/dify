package com.dify.core.app.app_config.entities;

import com.dify.core.workflow.file.FileTransferMethod;
import com.dify.core.workflow.file.FileType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class VariableEntity {
    private String variable;
    private String label;

    @Builder.Default
    private String description = "";

    private VariableEntityType type;

    @Builder.Default
    private boolean required = false;

    @Builder.Default
    private boolean hide = false;

    private Object defaultValue;
    private Integer maxLength;

    @Builder.Default
    private List<String> options = new ArrayList<>();

    @Builder.Default
    private List<FileType> allowedFileTypes = new ArrayList<>();

    @Builder.Default
    private List<String> allowedFileExtensions = new ArrayList<>();

    @Builder.Default
    private List<FileTransferMethod> allowedFileUploadMethods = new ArrayList<>();

    private Map<String, Object> jsonSchema;

    public void setDescription(String description) {
        this.description = description == null ? "" : description;
    }

    public void setOptions(List<String> options) {
        this.options = options == null ? new ArrayList<>() : options;
    }

    public static VariableEntity modelValidate(Map<String, Object> payload) {
        VariableEntity entity = new VariableEntity();
        entity.setVariable((String) payload.get("variable"));
        entity.setLabel((String) payload.get("label"));
        entity.setDescription((String) payload.get("description"));
        Object type = payload.get("type");
        if (type instanceof VariableEntityType variableEntityType) {
            entity.setType(variableEntityType);
        } else if (type != null) {
            entity.setType(VariableEntityType.valueOfLabel(String.valueOf(type)));
        }
        Object required = payload.get("required");
        entity.setRequired(required instanceof Boolean b && b);
        Object hide = payload.get("hide");
        entity.setHide(hide instanceof Boolean b && b);
        entity.setDefaultValue(payload.get("default"));
        Object maxLength = payload.get("max_length");
        if (maxLength instanceof Number number) {
            entity.setMaxLength(number.intValue());
        }
        Object options = payload.get("options");
        if (options instanceof List<?> list) {
            entity.setOptions(list.stream().map(String::valueOf).toList());
        }
        Object schema = payload.get("json_schema");
        if (schema instanceof Map<?, ?> map) {
            entity.setJsonSchema((Map<String, Object>) map);
        }
        return entity;
    }
}

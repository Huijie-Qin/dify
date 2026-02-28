package com.dify.core.tools.entities.tool_entities;

import com.dify.core.plugin.entities.parameters.PluginParameter;
import com.dify.core.plugin.entities.parameters.PluginParameterOption;
import com.dify.core.plugin.entities.parameters.PluginParameterType;
import com.dify.core.plugin.entities.parameters.PluginParameterUtils;
import com.dify.core.tools.entities.common_entities.I18nObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ToolParameter extends PluginParameter {

    private ToolParameterType type;
    private I18nObject humanDescription;
    private ToolParameterForm form;
    private String llmDescription;
    private Map<String, Object> inputSchema;

    public static ToolParameter getSimpleInstance(
            String name,
            String llmDescription,
            ToolParameterType typ,
            boolean required,
            List<String> options) {
        List<PluginParameterOption> optionObjects = new ArrayList<>();
        if (options != null) {
            for (String option : options) {
                optionObjects.add(PluginParameterOption.builder()
                        .value(option)
                        .label(new I18nObject(option, option))
                        .build());
            }
        }

        ToolParameter parameter = new ToolParameter();
        parameter.setName(name);
        parameter.setLabel(new I18nObject("", ""));
        parameter.setPlaceholder(null);
        parameter.setHumanDescription(new I18nObject("", ""));
        parameter.setType(typ);
        parameter.setForm(ToolParameterForm.LLM);
        parameter.setLlmDescription(llmDescription);
        parameter.setRequired(required);
        parameter.setOptions(optionObjects);
        return parameter;
    }

    public Object initFrontendParameter(Object value) {
        return PluginParameterUtils.initFrontendParameter(this, this.type.value, value);
    }

    @Getter
    public enum ToolParameterType {
        STRING(PluginParameterType.STRING.getValue()),
        NUMBER(PluginParameterType.NUMBER.getValue()),
        BOOLEAN(PluginParameterType.BOOLEAN.getValue()),
        SELECT(PluginParameterType.SELECT.getValue()),
        SECRET_INPUT(PluginParameterType.SECRET_INPUT.getValue()),
        FILE(PluginParameterType.FILE.getValue()),
        FILES(PluginParameterType.FILES.getValue()),
        CHECKBOX(PluginParameterType.CHECKBOX.getValue()),
        APP_SELECTOR(PluginParameterType.APP_SELECTOR.getValue()),
        MODEL_SELECTOR(PluginParameterType.MODEL_SELECTOR.getValue()),
        ANY(PluginParameterType.ANY.getValue()),
        DYNAMIC_SELECT(PluginParameterType.DYNAMIC_SELECT.getValue()),
        ARRAY("array"),
        OBJECT("object"),
        SYSTEM_FILES(PluginParameterType.SYSTEM_FILES.getValue());

        private final String value;

        ToolParameterType(String value) {
            this.value = value;
        }

        public String asNormalType() {
            return PluginParameterUtils.asNormalType(value);
        }

        public Object castValue(Object value) {
            return PluginParameterUtils.castParameterValue(this.value, value);
        }
    }

    public enum ToolParameterForm {
        SCHEMA,
        FORM,
        LLM
    }
}

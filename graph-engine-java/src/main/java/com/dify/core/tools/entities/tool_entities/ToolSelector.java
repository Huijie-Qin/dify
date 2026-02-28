package com.dify.core.tools.entities.tool_entities;

import com.dify.core.plugin.entities.parameters.PluginParameterOption;
import com.dify.core.tools.entities.constants.ToolSelectorModelIdentity;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolSelector {
    @Builder.Default
    private String difyModelIdentity = ToolSelectorModelIdentity.TOOL_SELECTOR_MODEL_IDENTITY;

    private String providerId;
    private String credentialId;
    private String toolName;
    private String toolDescription;
    private Map<String, Object> toolConfiguration;
    private Map<String, Parameter> toolParameters;

    public Map<String, Object> toPluginParameter() {
        Map<String, Object> result = new HashMap<>();
        result.put("dify_model_identity", difyModelIdentity);
        result.put("provider_id", providerId);
        result.put("credential_id", credentialId);
        result.put("tool_name", toolName);
        result.put("tool_description", toolDescription);
        result.put("tool_configuration", toolConfiguration);
        result.put("tool_parameters", toolParameters);
        return result;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Parameter {
        private String name;
        private ToolParameter.ToolParameterType type;
        private boolean required;
        private String description;
        private Object defaultValue;
        private java.util.List<PluginParameterOption> options;
    }
}

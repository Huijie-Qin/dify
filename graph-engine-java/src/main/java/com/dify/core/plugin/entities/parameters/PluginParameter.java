package com.dify.core.plugin.entities.parameters;

import com.dify.core.tools.entities.common_entities.I18nObject;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PluginParameter {
    private String name;
    private I18nObject label;
    private I18nObject placeholder;
    private Boolean required;
    private Object defaultValue;
    @Builder.Default
    private List<PluginParameterOption> options = new ArrayList<>();
}

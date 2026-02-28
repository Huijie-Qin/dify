package com.dify.core.plugin.entities.parameters;

import com.dify.core.tools.entities.common_entities.I18nObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PluginParameterOption {
    private String value;
    private I18nObject label;
    private String icon;
}

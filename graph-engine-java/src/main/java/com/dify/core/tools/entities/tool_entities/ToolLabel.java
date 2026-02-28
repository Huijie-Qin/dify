package com.dify.core.tools.entities.tool_entities;

import com.dify.core.tools.entities.common_entities.I18nObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolLabel {
    private String name;
    private I18nObject label;
    private String icon;
}

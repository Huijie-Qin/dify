package com.dify.core.tools.entities.tool_entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowToolParameterConfiguration {
    private String name;
    private String description;
    private ToolParameter.ToolParameterForm form;
}

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
public class ToolDescription {
    private I18nObject human;
    private String llm;
}

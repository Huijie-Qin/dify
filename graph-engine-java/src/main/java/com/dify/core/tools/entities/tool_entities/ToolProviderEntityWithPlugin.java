package com.dify.core.tools.entities.tool_entities;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ToolProviderEntityWithPlugin extends ToolProviderEntity {
    private List<ToolEntity> tools = new ArrayList<>();
}

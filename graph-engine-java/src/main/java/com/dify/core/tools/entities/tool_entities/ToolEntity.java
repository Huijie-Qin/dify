package com.dify.core.tools.entities.tool_entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolEntity {
    private ToolIdentity identity;

    @Builder.Default
    private List<ToolParameter> parameters = new ArrayList<>();

    private ToolDescription description;

    @Builder.Default
    private Map<String, Object> outputSchema = new HashMap<>();

    @Builder.Default
    private boolean hasRuntimeParameters = false;

    public ToolEntity copy() {
        ToolEntity copied = new ToolEntity();
        copied.setIdentity(identity);
        copied.setParameters(new ArrayList<>(parameters));
        copied.setDescription(description);
        copied.setOutputSchema(new HashMap<>(outputSchema));
        copied.setHasRuntimeParameters(hasRuntimeParameters);
        return copied;
    }
}

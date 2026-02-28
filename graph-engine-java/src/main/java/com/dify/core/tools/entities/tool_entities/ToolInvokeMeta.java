package com.dify.core.tools.entities.tool_entities;

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
public class ToolInvokeMeta {
    private double timeCost;
    private String error;
    private Map<String, Object> toolConfig;

    public static ToolInvokeMeta empty() {
        return new ToolInvokeMeta(0.0, null, new HashMap<>());
    }

    public static ToolInvokeMeta errorInstance(String error) {
        return new ToolInvokeMeta(0.0, error, new HashMap<>());
    }

    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("time_cost", timeCost);
        result.put("error", error);
        result.put("tool_config", toolConfig);
        return result;
    }
}

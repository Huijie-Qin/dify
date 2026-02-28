package com.dify.core.app.app_config.entities;

import com.dify.core.model_runtime.entities.llm_entities.LLMMode;
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
public class ModelConfig {
    private String provider;
    private String name;
    private LLMMode mode;

    @Builder.Default
    private Map<String, Object> completionParams = new HashMap<>();
}

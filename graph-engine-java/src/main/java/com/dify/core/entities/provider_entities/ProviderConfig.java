package com.dify.core.entities.provider_entities;

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
public class ProviderConfig {
    private String name;
    private String type;
    @Builder.Default
    private Map<String, Object> extra = new HashMap<>();
}

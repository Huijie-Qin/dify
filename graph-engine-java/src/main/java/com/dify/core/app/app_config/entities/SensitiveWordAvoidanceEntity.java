package com.dify.core.app.app_config.entities;

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
public class SensitiveWordAvoidanceEntity {
    private String type;

    @Builder.Default
    private Map<String, Object> config = new HashMap<>();
}

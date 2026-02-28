package com.dify.core.app.app_config.entities;

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
public class ModelConfigEntity {
    private String provider;
    private String model;
    private String mode;

    @Builder.Default
    private Map<String, Object> parameters = new HashMap<>();

    @Builder.Default
    private List<String> stop = new ArrayList<>();
}

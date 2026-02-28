package com.dify.core.app.app_config.entities;

import com.dify.models.model.AppMode;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppConfig {
    private String tenantId;
    private String appId;
    private AppMode appMode;
    private AppAdditionalFeatures additionalFeatures;

    @Builder.Default
    private List<VariableEntity> variables = new ArrayList<>();

    private SensitiveWordAvoidanceEntity sensitiveWordAvoidance;
}

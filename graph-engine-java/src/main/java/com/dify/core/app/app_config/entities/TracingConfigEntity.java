package com.dify.core.app.app_config.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TracingConfigEntity {
    private boolean enabled;
    private String tracingProvider;
}

package com.dify.core.app.app_config.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdvancedCompletionPromptTemplateEntity {

    private String prompt;
    private RolePrefixEntity rolePrefix;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RolePrefixEntity {
        private String user;
        private String assistant;
    }
}

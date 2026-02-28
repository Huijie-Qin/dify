package com.dify.core.app.app_config.entities;

import com.dify.core.model_runtime.entities.message_entities.PromptMessageRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdvancedChatMessageEntity {
    private String text;
    private PromptMessageRole role;
}

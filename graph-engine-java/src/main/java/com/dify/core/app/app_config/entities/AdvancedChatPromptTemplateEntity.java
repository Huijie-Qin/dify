package com.dify.core.app.app_config.entities;

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
public class AdvancedChatPromptTemplateEntity {
    @Builder.Default
    private List<AdvancedChatMessageEntity> messages = new ArrayList<>();
}

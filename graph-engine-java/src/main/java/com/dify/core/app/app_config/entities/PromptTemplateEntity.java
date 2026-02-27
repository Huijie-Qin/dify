package com.dify.core.app.app_config.entities;

import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptTemplateEntity {

    private PromptType promptType;
    private String simplePromptTemplate;
    private AdvancedChatPromptTemplateEntity advancedChatPromptTemplate;
    private AdvancedCompletionPromptTemplateEntity advancedCompletionPromptTemplate;

    @Getter
    public enum PromptType {
        SIMPLE("simple"),
        ADVANCED("advanced");

        private final String value;

        PromptType(String value) {
            this.value = value;
        }

        public static PromptType valueOfLabel(String value) {
            return Arrays.stream(values())
                    .filter(mode -> mode.value.equals(value))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("invalid prompt type value " + value));
        }
    }
}

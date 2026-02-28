package com.dify.core.tools.entities.tool_entities;

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
public class ToolInvokeMessage {
    private TextMessage text;
    private JsonMessage json;
    private BlobMessage blob;
    private BlobChunkMessage blobChunk;
    private FileMessage file;
    private VariableMessage variable;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TextMessage { private String text; }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JsonMessage {
        private Object jsonObject;
        @Builder.Default
        private boolean suppressOutput = false;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BlobMessage { private byte[] blob; }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BlobChunkMessage {
        private String id;
        private int sequence;
        private int totalLength;
        private byte[] blob;
        private boolean end;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileMessage {
        @Builder.Default
        private String fileMarker = "file_marker";
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VariableMessage {
        private String variableName;
        private Object variableValue;
        @Builder.Default
        private boolean stream = false;

        public void setVariableValue(Object variableValue) {
            if (variableValue == null || variableValue instanceof String || variableValue instanceof Number || variableValue instanceof Boolean || variableValue instanceof Map<?, ?> || variableValue instanceof List<?>) {
                this.variableValue = variableValue;
                return;
            }
            this.variableValue = String.valueOf(variableValue);
        }
    }
}

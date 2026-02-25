package com.dify.api.models;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Java translation for api/models/tools.py.
 */
public class tools {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Data
    @Builder
    public static class ToolOAuthSystemClient {
        private String id;
        private String pluginId;
        private String provider;
        private String encryptedOauthParams;

        public static ToolOAuthSystemClient create(String pluginId, String provider, String encryptedOauthParams) {
            return ToolOAuthSystemClient.builder()
                    .id(UUID.randomUUID().toString())
                    .pluginId(pluginId)
                    .provider(provider)
                    .encryptedOauthParams(encryptedOauthParams)
                    .build();
        }
    }

    @Data
    @Builder
    public static class ToolOAuthTenantClient {
        private String id;
        private String tenantId;
        private String pluginId;
        private String provider;
        private boolean enabled;
        private String encryptedOauthParams;

        public Map<String, Object> getOauthParams() {
            return readMap(encryptedOauthParams);
        }
    }

    @Data
    @Builder
    public static class BuiltinToolProvider {
        private String id;
        private String name;
        private String tenantId;
        private String userId;
        private String provider;
        private String encryptedCredentials;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private boolean isDefault;
        private String credentialType;
        private long expiresAt;

        public Map<String, Object> getCredentials() {
            return readMap(encryptedCredentials);
        }
    }

    @Data
    @Builder
    public static class ApiToolProvider {
        private String id;
        private String name;
        private String icon;
        private String schema;
        private String schemaTypeStr;
        private String userId;
        private String tenantId;
        private String toolsStr;
        private String credentialsStr;
        private String privacyPolicy;
        private String customDisclaimer;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public String getSchemaType() {
            return schemaTypeStr;
        }

        public List<Map<String, Object>> getTools() {
            return readList(toolsStr);
        }

        public Map<String, Object> getCredentials() {
            return readMap(credentialsStr);
        }
    }

    @Data
    @Builder
    public static class ToolLabelBinding {
        private String id;
        private String toolId;
        private String toolType;
        private String labelName;
    }

    @Data
    @Builder
    public static class WorkflowToolProvider {
        private String id;
        private String name;
        private String label;
        private String icon;
        private String appId;
        private String version;
        private String userId;
        private String tenantId;
        private String description;
        private String parameterConfiguration;
        private String privacyPolicy;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public List<Map<String, Object>> getParameterConfigurations() {
            return readList(parameterConfiguration);
        }
    }

    @Data
    @Builder
    public static class MCPToolProvider {
        private String id;
        private String name;
        private String serverIdentifier;
        private String serverUrl;
        private String serverUrlHash;
        private String icon;
        private String tenantId;
        private String userId;
        private String encryptedCredentials;
        private boolean authed;
        private String tools;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Map<String, Object> getCredentials() {
            return readMap(encryptedCredentials);
        }

        public List<Map<String, Object>> getToolsList() {
            return readList(tools);
        }
    }

    private static Map<String, Object> readMap(String raw) {
        try {
            if (raw == null || raw.isBlank()) {
                return Map.of();
            }
            return MAPPER.readValue(raw, new TypeReference<>() {});
        } catch (Exception e) {
            throw new IllegalArgumentException("json parse error", e);
        }
    }

    private static List<Map<String, Object>> readList(String raw) {
        try {
            if (raw == null || raw.isBlank()) {
                return List.of();
            }
            return MAPPER.readValue(raw, new TypeReference<>() {});
        } catch (Exception e) {
            throw new IllegalArgumentException("json parse error", e);
        }
    }
}

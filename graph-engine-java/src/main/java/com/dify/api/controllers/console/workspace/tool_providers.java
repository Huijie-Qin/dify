package com.dify.api.controllers.console.workspace;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Java translation for api/controllers/console/workspace/tool_providers.py.
 * This class keeps the same module name and contains payload/query DTOs plus shared validators.
 */
public class tool_providers {

    public static boolean isValidUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        try {
            URI parsed = URI.create(url);
            String scheme = parsed.getScheme();
            return parsed.getHost() != null && ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme));
        } catch (Exception e) {
            return false;
        }
    }

    public static String validateUuid(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        UUID.fromString(value);
        return value;
    }

    public static String validateAlphaNumeric(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        if (!value.matches("^[a-zA-Z0-9_\\-]+$")) {
            throw new IllegalArgumentException(fieldName + " must be alphanumeric/underscore/hyphen");
        }
        return value;
    }

    @Data
    @NoArgsConstructor
    public static class ToolProviderListQuery {
        private String type;
    }

    @Data
    @NoArgsConstructor
    public static class BuiltinToolCredentialDeletePayload {
        private String credentialId;
    }

    @Data
    @NoArgsConstructor
    public static class BuiltinToolAddPayload {
        private Map<String, Object> credentials;
        private String name;
        private String type;
    }

    @Data
    @NoArgsConstructor
    public static class BuiltinToolUpdatePayload {
        private String credentialId;
        private Map<String, Object> credentials;
        private String name;
    }

    @Data
    @NoArgsConstructor
    public static class ApiToolProviderBasePayload {
        private Map<String, Object> credentials;
        private String schemaType;
        private String schema;
        private String provider;
        private Map<String, Object> icon;
        private String privacyPolicy;
        private List<String> labels;
        private String customDisclaimer = "";
    }

    @Data
    @NoArgsConstructor
    public static class ApiToolProviderAddPayload extends ApiToolProviderBasePayload {
    }

    @Data
    @NoArgsConstructor
    public static class ApiToolProviderUpdatePayload extends ApiToolProviderBasePayload {
        private String originalProvider;
    }

    @Data
    @NoArgsConstructor
    public static class WorkflowToolBasePayload {
        private String name;
        private String label;
        private String description;
        private Map<String, Object> icon;
        private List<Map<String, Object>> parameters;
        private String privacyPolicy = "";
        private List<String> labels;

        public void validate() {
            validateAlphaNumeric(name, "name");
        }
    }

    @Data
    @NoArgsConstructor
    public static class WorkflowToolCreatePayload extends WorkflowToolBasePayload {
        private String workflowAppId;

        @Override
        public void validate() {
            super.validate();
            validateUuid(workflowAppId, "workflowAppId");
        }
    }

    @Data
    @NoArgsConstructor
    public static class WorkflowToolUpdatePayload extends WorkflowToolBasePayload {
        private String workflowToolId;

        @Override
        public void validate() {
            super.validate();
            validateUuid(workflowToolId, "workflowToolId");
        }
    }

    @Data
    @NoArgsConstructor
    public static class WorkflowToolDeletePayload {
        private String workflowToolId;

        public void validate() {
            validateUuid(workflowToolId, "workflowToolId");
        }
    }

    @Data
    @NoArgsConstructor
    public static class WorkflowToolGetQuery {
        private String workflowToolId;
        private String workflowAppId;

        public void validate() {
            if (workflowToolId != null) {
                validateUuid(workflowToolId, "workflowToolId");
            }
            if (workflowAppId != null) {
                validateUuid(workflowAppId, "workflowAppId");
            }
            if (Objects.isNull(workflowToolId) && Objects.isNull(workflowAppId)) {
                throw new IllegalArgumentException("workflowToolId or workflowAppId is required");
            }
        }
    }

    @Data
    @NoArgsConstructor
    public static class MCPProviderBasePayload {
        private String serverUrl;
        private String name;
        private String icon;
        private String iconType;
        private String iconBackground = "";
        private String serverIdentifier;
        private Map<String, Object> configuration;
        private Map<String, Object> headers;
        private Map<String, Object> authentication;
    }

    @Data
    @NoArgsConstructor
    public static class MCPProviderCreatePayload extends MCPProviderBasePayload {
    }

    @Data
    @NoArgsConstructor
    public static class MCPProviderUpdatePayload extends MCPProviderBasePayload {
        private String providerId;
    }
}

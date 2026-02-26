package com.dify.api.models;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Java translation for api/models/workflow.py.
 */
public class workflow {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public enum WorkflowType {
        WORKFLOW("workflow"),
        CHAT("chat"),
        RAG_PIPELINE("rag-pipeline");

        private final String value;

        WorkflowType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static WorkflowType valueOfValue(String value) {
            for (WorkflowType type : values()) {
                if (type.value.equals(value)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("invalid workflow type value " + value);
        }

        public static WorkflowType fromAppMode(String appMode) {
            return "workflow".equals(appMode) ? WORKFLOW : CHAT;
        }
    }

    @Data
    public static class WorkflowEntity {
        public static final String VERSION_DRAFT = "draft";

        private String id;
        private String tenantId;
        private String appId;
        private String type;
        private String version;
        private String markedName = "";
        private String markedComment = "";
        private String graph;
        private String features;
        private String createdBy;
        private LocalDateTime createdAt;
        private String updatedBy;
        private LocalDateTime updatedAt;
        private String environmentVariables = "{}";
        private String conversationVariables = "{}";
        private String ragPipelineVariables = "{}";

        public static WorkflowEntity newWorkflow(
                String tenantId,
                String appId,
                String type,
                String version,
                String graph,
                String features,
                String createdBy,
                List<Map<String, Object>> environmentVariables,
                List<Map<String, Object>> conversationVariables,
                List<Map<String, Object>> ragPipelineVariables,
                String markedName,
                String markedComment
        ) {
            WorkflowEntity workflow = new WorkflowEntity();
            workflow.id = UUID.randomUUID().toString();
            workflow.tenantId = tenantId;
            workflow.appId = appId;
            workflow.type = type;
            workflow.version = version;
            workflow.graph = graph;
            workflow.features = features;
            workflow.createdBy = createdBy;
            workflow.environmentVariables = writeJson(environmentVariables);
            workflow.conversationVariables = writeJson(conversationVariables);
            workflow.ragPipelineVariables = writeJson(ragPipelineVariables);
            workflow.markedName = markedName == null ? "" : markedName;
            workflow.markedComment = markedComment == null ? "" : markedComment;
            workflow.createdAt = LocalDateTime.now();
            workflow.updatedAt = workflow.createdAt;
            return workflow;
        }

        public Map<String, Object> getGraphDict() {
            return readMap(graph, "graph");
        }

        public Map<String, Object> getFeaturesDict() {
            return readMap(features, "features");
        }

        public Map<String, Object> getNodeConfigById(String nodeId) {
            Map<String, Object> workflowGraph = getGraphDict();
            Object nodesObj = workflowGraph.get("nodes");
            if (!(nodesObj instanceof List<?> nodes)) {
                throw new IllegalArgumentException("nodes not found in workflow graph");
            }

            for (Object nodeObj : nodes) {
                if (nodeObj instanceof Map<?, ?> rawNode) {
                    Object idObj = rawNode.get("id");
                    if (nodeId.equals(idObj)) {
                        return (Map<String, Object>) rawNode;
                    }
                }
            }
            throw new NoSuchElementException("Node not found: " + nodeId);
        }

        public String getNodeTypeFromNodeConfig(Map<String, Object> nodeConfig) {
            Object dataObj = nodeConfig.get("data");
            if (dataObj instanceof Map<?, ?> data) {
                Object typeObj = data.get("type");
                return typeObj == null ? null : String.valueOf(typeObj);
            }
            return null;
        }

        public Map<String, String> getEnclosingNodeTypeAndId(Map<String, Object> nodeConfig) {
            boolean inLoop = Boolean.TRUE.equals(nodeConfig.get("isInLoop"));
            boolean inIteration = Boolean.TRUE.equals(nodeConfig.get("isInIteration"));

            if (inLoop) {
                Object loopId = nodeConfig.get("loop_id");
                if (loopId == null) {
                    throw new IllegalArgumentException("invalid graph");
                }
                return Map.of("type", "loop", "id", String.valueOf(loopId));
            }
            if (inIteration) {
                Object iterationId = nodeConfig.get("iteration_id");
                if (iterationId == null) {
                    throw new IllegalArgumentException("invalid graph");
                }
                return Map.of("type", "iteration", "id", String.valueOf(iterationId));
            }
            return null;
        }

        private static Map<String, Object> readMap(String value, String fieldName) {
            try {
                if (value == null || value.isBlank()) {
                    return Map.of();
                }
                return MAPPER.readValue(value, new TypeReference<>() {});
            } catch (Exception e) {
                throw new IllegalArgumentException("invalid json in " + fieldName, e);
            }
        }

        private static String writeJson(Object data) {
            try {
                return MAPPER.writeValueAsString(data == null ? List.of() : data);
            } catch (Exception e) {
                throw new IllegalArgumentException("json encode failed", e);
            }
        }
    }
}

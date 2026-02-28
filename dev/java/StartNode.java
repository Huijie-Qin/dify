package dev.java;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Java translation of Dify's StartNode runtime behavior.
 *
 * <p>Behavior parity with api/core/workflow/nodes/start/start_node.py:
 * <ul>
 *   <li>Read user inputs from variable pool.</li>
 *   <li>Validate + normalize JSON_OBJECT typed variables.</li>
 *   <li>Expose system variables as prefixed outputs: sys.{@code <key>}.</li>
 *   <li>Return inputs/outputs with SUCCEEDED status.</li>
 * </ul>
 */
public final class StartNode {

    public static final String SYSTEM_VARIABLE_NODE_ID = "sys";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public NodeRunResult run(StartNodeData nodeData, VariablePool variablePool) {
        Map<String, Object> nodeInputs = new HashMap<>(variablePool.userInputs());
        validateAndNormalizeJsonObjectInputs(nodeData, nodeInputs);

        Map<String, Object> systemInputs = variablePool.systemVariables();
        for (Map.Entry<String, Object> entry : systemInputs.entrySet()) {
            nodeInputs.put(SYSTEM_VARIABLE_NODE_ID + "." + entry.getKey(), entry.getValue());
        }

        Map<String, Object> outputs = new HashMap<>(nodeInputs);
        return new NodeRunResult(WorkflowNodeExecutionStatus.SUCCEEDED, nodeInputs, outputs);
    }

    private void validateAndNormalizeJsonObjectInputs(StartNodeData nodeData, Map<String, Object> nodeInputs) {
        for (VariableEntity variable : nodeData.variables()) {
            if (variable.type() != VariableEntityType.JSON_OBJECT) {
                continue;
            }

            String key = variable.variable();
            Object value = nodeInputs.get(key);

            if (value == null && variable.required()) {
                throw new IllegalArgumentException(key + " is required in input form");
            }

            if (isFalsy(value)) {
                continue;
            }

            if (!(value instanceof Map<?, ?> mapValue)) {
                throw new IllegalArgumentException("JSON object for '" + key + "' must be an object");
            }

            // Normalize to map for downstream consistency (same as Python implementation intent).
            Map<String, Object> normalized = new HashMap<>();
            for (Map.Entry<?, ?> entry : mapValue.entrySet()) {
                normalized.put(String.valueOf(entry.getKey()), entry.getValue());
            }
            nodeInputs.put(key, normalized);

            Map<String, Object> schema = variable.jsonSchema();
            if (schema == null || schema.isEmpty()) {
                continue;
            }

            validateJsonSchema(key, normalized, schema);
        }
    }

    private static void validateJsonSchema(String key, Map<String, Object> value, Map<String, Object> schema) {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
        JsonNode schemaNode = MAPPER.valueToTree(schema);
        JsonNode valueNode = MAPPER.valueToTree(value);
        JsonSchema jsonSchema = factory.getSchema(schemaNode);

        Set<ValidationMessage> errors = jsonSchema.validate(valueNode);
        if (!errors.isEmpty()) {
            ValidationMessage firstError = errors.iterator().next();
            throw new IllegalArgumentException(
                    "JSON object for '" + key + "' does not match schema: " + firstError.getMessage());
        }
    }

    private static boolean isFalsy(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof String s) {
            return s.isEmpty();
        }
        if (value instanceof Map<?, ?> m) {
            return m.isEmpty();
        }
        if (value instanceof List<?> l) {
            return l.isEmpty();
        }
        return false;
    }

    public enum VariableEntityType {
        TEXT_INPUT,
        SELECT,
        PARAGRAPH,
        NUMBER,
        EXTERNAL_DATA_TOOL,
        FILE,
        FILE_LIST,
        CHECKBOX,
        JSON_OBJECT
    }

    public enum WorkflowNodeExecutionStatus {
        SUCCEEDED,
        FAILED
    }

    public record VariableEntity(
            String variable,
            VariableEntityType type,
            boolean required,
            Map<String, Object> jsonSchema) {

        public VariableEntity {
            Objects.requireNonNull(variable, "variable must not be null");
            Objects.requireNonNull(type, "type must not be null");
            jsonSchema = jsonSchema == null ? null : Map.copyOf(jsonSchema);
        }
    }

    public record StartNodeData(List<VariableEntity> variables) {
        public StartNodeData {
            variables = variables == null ? List.of() : List.copyOf(variables);
        }
    }

    public record NodeRunResult(
            WorkflowNodeExecutionStatus status,
            Map<String, Object> inputs,
            Map<String, Object> outputs) {

        public NodeRunResult {
            Objects.requireNonNull(status, "status must not be null");
            inputs = inputs == null ? Collections.emptyMap() : Map.copyOf(inputs);
            outputs = outputs == null ? Collections.emptyMap() : Map.copyOf(outputs);
        }
    }

    public interface VariablePool {
        Map<String, Object> userInputs();

        Map<String, Object> systemVariables();
    }
}

package com.dify.core.tools.utils.workflow_configuration_sync;

import com.dify.core.app.app_config.entities.VariableEntity;
import com.dify.core.tools.entities.tool_entities.WorkflowToolParameterConfiguration;
import com.dify.core.tools.errors.WorkflowToolHumanInputNotSupportedError;
import com.dify.core.workflow.nodes.base.entities.OutputVariableEntity;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class WorkflowToolConfigurationUtils {

    private WorkflowToolConfigurationUtils() {}

    @SuppressWarnings("unchecked")
    public static List<VariableEntity> getWorkflowGraphVariables(Map<String, Object> graph) {
        List<Map<String, Object>> nodes = (List<Map<String, Object>>) graph.getOrDefault("nodes", List.of());
        Map<String, Object> startNode = null;

        for (Map<String, Object> node : nodes) {
            Map<String, Object> data = castMap(node.get("data"));
            if (data != null && Objects.equals(data.get("type"), "start")) {
                startNode = node;
                break;
            }
        }

        if (startNode == null) {
            return List.of();
        }

        Map<String, Object> startData = castMap(startNode.get("data"));
        if (startData == null) {
            return List.of();
        }

        List<Map<String, Object>> variables = (List<Map<String, Object>>) startData.getOrDefault("variables", List.of());
        List<VariableEntity> result = new ArrayList<>(variables.size());
        for (Map<String, Object> variable : variables) {
            result.add(VariableEntity.modelValidate(variable));
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    public static List<OutputVariableEntity> getWorkflowGraphOutput(Map<String, Object> graph) {
        List<Map<String, Object>> nodes = (List<Map<String, Object>>) graph.getOrDefault("nodes", List.of());
        Map<String, OutputVariableEntity> outputsByVariable = new LinkedHashMap<>();
        List<String> variableOrder = new ArrayList<>();

        for (Map<String, Object> node : nodes) {
            Map<String, Object> data = castMap(node.get("data"));
            if (data == null || !Objects.equals(data.get("type"), "end")) {
                continue;
            }

            List<Map<String, Object>> outputs = (List<Map<String, Object>>) data.getOrDefault("outputs", List.of());
            for (Map<String, Object> output : outputs) {
                OutputVariableEntity entity = OutputVariableEntity.modelValidate(output);
                String variable = entity.getVariable();
                if (!variableOrder.contains(variable)) {
                    variableOrder.add(variable);
                }

                // Later end nodes override duplicated variable definitions.
                outputsByVariable.put(variable, entity);
            }
        }

        List<OutputVariableEntity> result = new ArrayList<>(variableOrder.size());
        for (String variable : variableOrder) {
            result.add(outputsByVariable.get(variable));
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    public static void ensureNoHumanInputNodes(Map<String, Object> graph) {
        List<Map<String, Object>> nodes = (List<Map<String, Object>>) graph.getOrDefault("nodes", List.of());
        for (Map<String, Object> node : nodes) {
            Map<String, Object> data = castMap(node.get("data"));
            if (data != null && Objects.equals(data.get("type"), "human-input")) {
                throw new WorkflowToolHumanInputNotSupportedError();
            }
        }
    }

    public static void checkIsSynced(
            List<VariableEntity> variables, List<WorkflowToolParameterConfiguration> toolConfigurations) {
        List<String> variableNames = variables.stream().map(VariableEntity::getVariable).toList();

        if (toolConfigurations.size() != variables.size()) {
            throw new IllegalArgumentException("parameter configuration mismatch, please republish the tool to update");
        }

        for (WorkflowToolParameterConfiguration parameter : toolConfigurations) {
            if (!variableNames.contains(parameter.getName())) {
                throw new IllegalArgumentException("parameter configuration mismatch, please republish the tool to update");
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return null;
    }
}

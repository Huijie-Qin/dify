package com.dify.api.core.tools.workflow_as_tool;

import com.dify.api.models.tools;
import com.dify.api.models.workflow;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Default implementation for {@link provider.ProviderGateway}.
 *
 * <p>This implementation keeps repository/data-access dependencies abstract through nested interfaces,
 * and provides concrete graph parsing logic for:
 * <ul>
 *   <li>workflow input variables (used by provider#getDbProviderTool)</li>
 *   <li>workflow output schema extraction</li>
 * </ul>
 */
@RequiredArgsConstructor
public class provider_gateway_impl implements provider.ProviderGateway {

    private final AppGateway appGateway;
    private final AccountGateway accountGateway;
    private final WorkflowGateway workflowGateway;
    private final WorkflowToolProviderGateway workflowToolProviderGateway;

    @Override
    public Optional<provider.App> getApp(String appId) {
        return appGateway.getApp(appId);
    }

    @Override
    public Optional<provider.Account> getAccount(String accountId) {
        return accountGateway.getAccount(accountId);
    }

    @Override
    public Optional<workflow.WorkflowEntity> findWorkflowByAppIdAndVersion(String appId, String version) {
        return workflowGateway.findWorkflowByAppIdAndVersion(appId, version);
    }

    @Override
    public Optional<tools.WorkflowToolProvider> findWorkflowToolProvider(String tenantId, String providerId) {
        return workflowToolProviderGateway.findWorkflowToolProvider(tenantId, providerId);
    }

    @Override
    public List<provider.VariableEntity> getWorkflowGraphVariables(Map<String, Object> graph) {
        if (graph == null || graph.isEmpty()) {
            return List.of();
        }

        List<Map<String, Object>> nodes = getMapList(graph.get("nodes"));
        if (nodes.isEmpty()) {
            return List.of();
        }

        Map<String, Object> startNode = findStartNode(nodes);
        if (startNode == null) {
            return List.of();
        }

        Map<String, Object> data = getMap(startNode.get("data"));
        if (data.isEmpty()) {
            return List.of();
        }

        // Dify workflow start node usually stores inputs in "variables".
        List<Map<String, Object>> variableMaps = getMapList(data.get("variables"));
        if (variableMaps.isEmpty()) {
            // Some graph variants may use "inputs" as the field name.
            variableMaps = getMapList(data.get("inputs"));
        }

        List<provider.VariableEntity> result = new ArrayList<>();
        for (Map<String, Object> variableMap : variableMaps) {
            String variableName = asString(variableMap.get("variable"));
            if (variableName == null || variableName.isBlank()) {
                variableName = asString(variableMap.get("name"));
            }
            if (variableName == null || variableName.isBlank()) {
                continue;
            }

            provider.VariableEntity variableEntity = new provider.VariableEntity();
            variableEntity.setVariable(variableName);
            variableEntity.setLabel(defaultString(asString(variableMap.get("label")), variableName));
            variableEntity.setType(defaultString(asString(variableMap.get("type")), "text-input"));
            variableEntity.setRequired(Boolean.TRUE.equals(variableMap.get("required")));
            variableEntity.setDefaultValue(variableMap.get("default"));
            variableEntity.setOptions(getStringList(variableMap.get("options")));
            result.add(variableEntity);
        }

        return result;
    }

    @Override
    public List<provider.WorkflowOutput> getWorkflowGraphOutput(Map<String, Object> graph) {
        if (graph == null || graph.isEmpty()) {
            return List.of();
        }

        List<Map<String, Object>> nodes = getMapList(graph.get("nodes"));
        if (nodes.isEmpty()) {
            return List.of();
        }

        List<provider.WorkflowOutput> outputs = new ArrayList<>();
        for (Map<String, Object> node : nodes) {
            Map<String, Object> data = getMap(node.get("data"));
            String nodeType = asString(data.get("type"));
            if (!"end".equals(nodeType) && !"answer".equals(nodeType)) {
                continue;
            }

            List<Map<String, Object>> outputItems = getMapList(data.get("outputs"));
            for (Map<String, Object> outputItem : outputItems) {
                String variable = asString(outputItem.get("name"));
                if (variable == null || variable.isBlank()) {
                    variable = asString(outputItem.get("variable"));
                }
                if (variable == null || variable.isBlank()) {
                    continue;
                }

                provider.WorkflowOutput workflowOutput = new provider.WorkflowOutput();
                workflowOutput.setVariable(variable);
                workflowOutput.setValueType(defaultString(asString(outputItem.get("type")), "string"));
                outputs.add(workflowOutput);
            }
        }

        return outputs;
    }

    private static Map<String, Object> findStartNode(List<Map<String, Object>> nodes) {
        for (Map<String, Object> node : nodes) {
            Map<String, Object> data = getMap(node.get("data"));
            if ("start".equals(asString(data.get("type")))) {
                return node;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> getMap(Object obj) {
        if (obj instanceof Map<?, ?> raw) {
            return (Map<String, Object>) raw;
        }
        return Collections.emptyMap();
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> getMapList(Object obj) {
        if (!(obj instanceof List<?> list)) {
            return List.of();
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map<?, ?> raw) {
                result.add((Map<String, Object>) raw);
            }
        }
        return result;
    }

    private static List<String> getStringList(Object obj) {
        if (!(obj instanceof List<?> list)) {
            return List.of();
        }

        List<String> result = new ArrayList<>();
        for (Object item : list) {
            if (item != null) {
                result.add(String.valueOf(item));
            }
        }
        return result;
    }

    private static String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static String defaultString(String value, String defaultValue) {
        return (value == null || value.isBlank()) ? defaultValue : value;
    }

    public interface AppGateway {
        Optional<provider.App> getApp(String appId);
    }

    public interface AccountGateway {
        Optional<provider.Account> getAccount(String accountId);
    }

    public interface WorkflowGateway {
        Optional<workflow.WorkflowEntity> findWorkflowByAppIdAndVersion(String appId, String version);
    }

    public interface WorkflowToolProviderGateway {
        Optional<tools.WorkflowToolProvider> findWorkflowToolProvider(String tenantId, String providerId);
    }
}

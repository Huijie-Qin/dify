package com.dify.api.core.tools.workflow_as_tool;

import com.dify.api.models.tools;
import com.dify.api.models.workflow;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Java translation for api/core/tools/workflow_as_tool/provider.py.
 */
@Data
@RequiredArgsConstructor
public class provider {


    private final String providerId;
    private List<WorkflowTool> tools = new ArrayList<>();

    public static final Map<String, String> variableToParameterTypeMapping = Map.of(
            "text-input", "string",
            "paragraph", "string",
            "select", "select",
            "number", "number",
            "checkbox", "boolean",
            "file", "file",
            "file-list", "files"
    );

    public static provider fromDb(tools.WorkflowToolProvider dbProvider, ProviderGateway gateway) {
        App app = gateway.getApp(dbProvider.getAppId()).orElseThrow(() -> new IllegalArgumentException("app not found"));
        Account user = dbProvider.getUserId() == null ? null : gateway.getAccount(dbProvider.getUserId()).orElse(null);

        provider controller = new provider(dbProvider.getId());
        controller.tools = List.of(controller.getDbProviderTool(dbProvider, app, gateway, user));
        return controller;
    }

    public WorkflowTool getDbProviderTool(
            tools.WorkflowToolProvider dbProvider,
            App app,
            ProviderGateway gateway,
            Account user
    ) {
        workflow.WorkflowEntity workflowEntity = gateway.findWorkflowByAppIdAndVersion(dbProvider.getAppId(), dbProvider.getVersion())
                .orElseThrow(() -> new IllegalArgumentException("workflow not found"));

        Map<String, Object> graph = workflowEntity.getGraphDict();
        Map<String, Object> features = workflowEntity.getFeaturesDict();

        List<Map<String, Object>> parameters = dbProvider.getParameterConfigurations();
        List<VariableEntity> variables = gateway.getWorkflowGraphVariables(graph);

        List<ToolParameter> workflowToolParameters = new ArrayList<>();
        for (Map<String, Object> parameter : parameters) {
            String parameterName = String.valueOf(parameter.get("name"));
            String parameterDescription = String.valueOf(parameter.getOrDefault("description", ""));
            String parameterForm = String.valueOf(parameter.getOrDefault("form", "llm"));

            VariableEntity variable = variables.stream().filter(item -> item.getVariable().equals(parameterName)).findFirst().orElse(null);
            if (variable != null) {
                String parameterType = variableToParameterTypeMapping.get(variable.getType());
                if (parameterType == null) {
                    throw new IllegalArgumentException("unsupported variable type " + variable.getType());
                }

                List<Map<String, String>> options = new ArrayList<>();
                if ("select".equals(variable.getType()) && variable.getOptions() != null) {
                    for (String option : variable.getOptions()) {
                        options.add(Map.of("value", option, "label", option));
                    }
                }

                workflowToolParameters.add(ToolParameter.builder()
                        .name(parameterName)
                        .label(variable.getLabel())
                        .humanDescription(parameterDescription)
                        .type(parameterType)
                        .form(parameterForm)
                        .llmDescription(parameterDescription)
                        .required(variable.isRequired())
                        .defaultValue(variable.getDefaultValue())
                        .options(options)
                        .placeholder("")
                        .build());
            } else if (Boolean.TRUE.equals(features.get("file_upload"))) {
                workflowToolParameters.add(ToolParameter.builder()
                        .name(parameterName)
                        .label(parameterName)
                        .humanDescription(parameterDescription)
                        .type("system-files")
                        .llmDescription(parameterDescription)
                        .required(false)
                        .form(parameterForm)
                        .placeholder("")
                        .build());
            } else {
                throw new IllegalArgumentException("variable not found");
            }
        }

        List<WorkflowOutput> outputs = gateway.getWorkflowGraphOutput(graph);
        Map<String, Object> properties = new HashMap<>();
        for (WorkflowOutput output : outputs) {
            if (!List.of("json", "text", "files").contains(output.getVariable())) {
                properties.put(output.getVariable(), Map.of("type", output.getValueType(), "description", ""));
            }
        }

        Map<String, Object> outputSchema = Map.of("type", "object", "properties", properties);
        return WorkflowTool.builder()
                .workflowAsToolId(dbProvider.getId())
                .name(dbProvider.getName())
                .label(dbProvider.getLabel())
                .provider(this.providerId)
                .description(dbProvider.getDescription())
                .icon(dbProvider.getIcon())
                .parameters(workflowToolParameters)
                .outputSchema(outputSchema)
                .workflowAppId(app.getId())
                .workflowEntities(Map.of("app", app, "workflow", workflowEntity))
                .version(dbProvider.getVersion())
                .workflowCallDepth(0)
                .build();
    }

    public List<WorkflowTool> getTools(String tenantId, ProviderGateway gateway) {
        if (tools != null && !tools.isEmpty()) {
            return tools;
        }
        tools.WorkflowToolProvider dbProvider = gateway.findWorkflowToolProvider(tenantId, providerId).orElse(null);
        if (dbProvider == null) {
            return List.of();
        }

        App app = gateway.getApp(dbProvider.getAppId()).orElseThrow(() -> new IllegalArgumentException("app not found"));
        Account user = dbProvider.getUserId() == null ? null : gateway.getAccount(dbProvider.getUserId()).orElse(null);
        this.tools = List.of(this.getDbProviderTool(dbProvider, app, gateway, user));
        return this.tools;
    }

    public WorkflowTool getTool(String toolName) {
        if (tools == null) {
            return null;
        }
        return tools.stream().filter(tool -> tool.getName().equals(toolName)).findFirst().orElse(null);
    }

    @Data
    @lombok.Builder
    public static class WorkflowTool {
        private String workflowAsToolId;
        private String name;
        private String label;
        private String provider;
        private String description;
        private String icon;
        private List<ToolParameter> parameters;
        private Map<String, Object> outputSchema;
        private String workflowAppId;
        private Map<String, Object> workflowEntities;
        private String version;
        private int workflowCallDepth;
    }

    @Data
    @lombok.Builder
    public static class ToolParameter {
        private String name;
        private String label;
        private String humanDescription;
        private String type;
        private String form;
        private String llmDescription;
        private boolean required;
        private Object defaultValue;
        private List<Map<String, String>> options;
        private String placeholder;
    }

    @Data
    public static class VariableEntity {
        private String variable;
        private String label;
        private String type;
        private boolean required;
        private Object defaultValue;
        private List<String> options;
    }

    @Data
    public static class WorkflowOutput {
        private String variable;
        private String valueType;
    }

    public interface ProviderGateway {
        Optional<App> getApp(String appId);

        Optional<Account> getAccount(String accountId);

        Optional<workflow.WorkflowEntity> findWorkflowByAppIdAndVersion(String appId, String version);

        Optional<tools.WorkflowToolProvider> findWorkflowToolProvider(String tenantId, String providerId);

        List<VariableEntity> getWorkflowGraphVariables(Map<String, Object> graph);

        List<WorkflowOutput> getWorkflowGraphOutput(Map<String, Object> graph);
    }

    public interface App {
        String getId();
    }

    public interface Account {
        String getName();
    }
}

package com.dify.core.tools.workflow_as_tool;

import com.dify.core.app.app_config.entities.VariableEntity;
import com.dify.core.app.app_config.entities.VariableEntityType;
import com.dify.core.app.apps.workflow.app_config_manager.WorkflowAppConfigManager;
import com.dify.core.db.session_factory.SessionFactory;
import com.dify.core.plugin.entities.parameters.PluginParameterOption;
import com.dify.core.tools.__base.tool_provider.ToolProviderController;
import com.dify.core.tools.__base.tool_runtime.ToolRuntime;
import com.dify.core.tools.entities.common_entities.I18nObject;
import com.dify.core.tools.entities.tool_entities.ToolDescription;
import com.dify.core.tools.entities.tool_entities.ToolEntity;
import com.dify.core.tools.entities.tool_entities.ToolIdentity;
import com.dify.core.tools.entities.tool_entities.ToolParameter;
import com.dify.core.tools.entities.tool_entities.ToolProviderEntity;
import com.dify.core.tools.entities.tool_entities.ToolProviderIdentity;
import com.dify.core.tools.entities.tool_entities.ToolProviderType;
import com.dify.core.tools.utils.workflow_configuration_sync.WorkflowToolConfigurationUtils;
import com.dify.extensions.ext_database.DB;
import com.dify.models.account.Account;
import com.dify.models.model.App;
import com.dify.models.model.AppMode;
import com.dify.models.tools.WorkflowToolProvider;
import com.dify.models.workflow.Workflow;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkflowToolProviderController extends ToolProviderController {

    private static final Map<VariableEntityType, ToolParameter.ToolParameterType> VARIABLE_TO_PARAMETER_TYPE_MAPPING =
            new EnumMap<>(VariableEntityType.class);

    static {
        VARIABLE_TO_PARAMETER_TYPE_MAPPING.put(VariableEntityType.TEXT_INPUT, ToolParameter.ToolParameterType.STRING);
        VARIABLE_TO_PARAMETER_TYPE_MAPPING.put(VariableEntityType.PARAGRAPH, ToolParameter.ToolParameterType.STRING);
        VARIABLE_TO_PARAMETER_TYPE_MAPPING.put(VariableEntityType.SELECT, ToolParameter.ToolParameterType.SELECT);
        VARIABLE_TO_PARAMETER_TYPE_MAPPING.put(VariableEntityType.NUMBER, ToolParameter.ToolParameterType.NUMBER);
        VARIABLE_TO_PARAMETER_TYPE_MAPPING.put(VariableEntityType.CHECKBOX, ToolParameter.ToolParameterType.BOOLEAN);
        VARIABLE_TO_PARAMETER_TYPE_MAPPING.put(VariableEntityType.FILE, ToolParameter.ToolParameterType.FILE);
        VARIABLE_TO_PARAMETER_TYPE_MAPPING.put(VariableEntityType.FILE_LIST, ToolParameter.ToolParameterType.FILES);
    }

    private String providerId;
    private List<WorkflowTool> tools = new ArrayList<>();

    public WorkflowToolProviderController(ToolProviderEntity entity, String providerId) {
        super(entity);
        this.providerId = providerId;
    }

    public static WorkflowToolProviderController fromDb(WorkflowToolProvider dbProvider) {
        try (var session = SessionFactory.createSession()) {
            var app = session.get(App.class, dbProvider.getAppId());
            if (app == null) {
                throw new IllegalArgumentException("app not found");
            }

            Account user = dbProvider.getUserId() == null ? null : session.get(Account.class, dbProvider.getUserId());
            var controller = new WorkflowToolProviderController(
                    ToolProviderEntity.builder()
                            .identity(ToolProviderIdentity.builder()
                                    .author(user == null ? "" : user.getName())
                                    .name(dbProvider.getLabel())
                                    .label(new I18nObject(dbProvider.getLabel(), dbProvider.getLabel()))
                                    .description(new I18nObject(dbProvider.getDescription(), dbProvider.getDescription()))
                                    .icon(dbProvider.getIcon())
                                    .build())
                            .credentialsSchema(new ArrayList<>())
                            .pluginId(null)
                            .build(),
                    dbProvider.getId());

            controller.tools = List.of(controller.getDbProviderTool(dbProvider, app, session, user));
            return controller;
        }
    }

    @Override
    public ToolProviderType getProviderType() {
        return ToolProviderType.WORKFLOW;
    }

    public WorkflowTool getDbProviderTool(WorkflowToolProvider dbProvider, App app, Object session, Account user) {
        Workflow workflow = SessionFactory.queryFirstWorkflow(session, dbProvider.getAppId(), dbProvider.getVersion());
        if (workflow == null) {
            throw new IllegalArgumentException("workflow not found");
        }

        Map<String, Object> graph = workflow.getGraphDict();
        Map<String, Object> featuresDict = workflow.getFeaturesDict();
        var features = WorkflowAppConfigManager.convertFeatures(featuresDict, AppMode.WORKFLOW);

        var parameters = dbProvider.getParameterConfigurations();
        var variables = WorkflowToolConfigurationUtils.getWorkflowGraphVariables(graph);

        List<ToolParameter> workflowToolParameters = new ArrayList<>();
        for (var parameter : parameters) {
            VariableEntity variable = fetchWorkflowVariable(variables, parameter.getName()).orElse(null);
            if (variable != null) {
                ToolParameter.ToolParameterType parameterType = VARIABLE_TO_PARAMETER_TYPE_MAPPING.get(variable.getType());
                if (parameterType == null) {
                    throw new IllegalArgumentException("unsupported variable type " + variable.getType());
                }

                List<PluginParameterOption> options = new ArrayList<>();
                if (variable.getType() == VariableEntityType.SELECT && variable.getOptions() != null) {
                    for (String option : variable.getOptions()) {
                        options.add(new PluginParameterOption(option, new I18nObject(option, option)));
                    }
                }

                workflowToolParameters.add(ToolParameter.builder()
                        .name(parameter.getName())
                        .label(new I18nObject(variable.getLabel(), variable.getLabel()))
                        .humanDescription(new I18nObject(parameter.getDescription(), parameter.getDescription()))
                        .type(parameterType)
                        .form(parameter.getForm())
                        .llmDescription(parameter.getDescription())
                        .required(variable.isRequired())
                        .defaultValue(variable.getDefaultValue())
                        .options(options)
                        .placeholder(new I18nObject("", ""))
                        .build());
            } else if (features.isFileUpload()) {
                workflowToolParameters.add(ToolParameter.builder()
                        .name(parameter.getName())
                        .label(new I18nObject(parameter.getName(), parameter.getName()))
                        .humanDescription(new I18nObject(parameter.getDescription(), parameter.getDescription()))
                        .type(ToolParameter.ToolParameterType.SYSTEM_FILES)
                        .llmDescription(parameter.getDescription())
                        .required(false)
                        .form(parameter.getForm())
                        .placeholder(new I18nObject("", ""))
                        .build());
            } else {
                throw new IllegalArgumentException("variable not found");
            }
        }

        var outputs = WorkflowToolConfigurationUtils.getWorkflowGraphOutput(graph);
        var reservedKeys = java.util.Set.of("json", "text", "files");
        Map<String, Object> properties = new HashMap<>();
        for (var output : outputs) {
            if (!reservedKeys.contains(output.getVariable())) {
                properties.put(output.getVariable(), Map.of("type", output.getValueType(), "description", ""));
            }
        }

        return new WorkflowTool(
                app.getId(),
                dbProvider.getId(),
                dbProvider.getVersion(),
                Map.of("app", app, "workflow", workflow),
                0,
                ToolEntity.builder()
                        .identity(ToolIdentity.builder()
                                .author(user == null ? "" : user.getName())
                                .name(dbProvider.getName())
                                .label(new I18nObject(dbProvider.getLabel(), dbProvider.getLabel()))
                                .provider(providerId)
                                .icon(dbProvider.getIcon())
                                .build())
                        .description(ToolDescription.builder()
                                .human(new I18nObject(dbProvider.getDescription(), dbProvider.getDescription()))
                                .llm(dbProvider.getDescription())
                                .build())
                        .parameters(workflowToolParameters)
                        .outputSchema(Map.of("type", "object", "properties", properties))
                        .build(),
                ToolRuntime.builder().tenantId(dbProvider.getTenantId()).build(),
                dbProvider.getLabel());
    }

    @Override
    public List<WorkflowTool> getTools(String tenantId) {
        if (tools != null && !tools.isEmpty()) {
            return tools;
        }

        try (var session = DB.openSession()) {
            WorkflowToolProvider dbProvider = DB.queryWorkflowToolProvider(session, tenantId, providerId);
            if (dbProvider == null) {
                return new ArrayList<>();
            }

            App app = session.get(App.class, dbProvider.getAppId());
            if (app == null) {
                throw new IllegalArgumentException("app not found");
            }

            Account user = dbProvider.getUserId() == null ? null : session.get(Account.class, dbProvider.getUserId());
            tools = List.of(getDbProviderTool(dbProvider, app, session, user));
            return tools;
        }
    }

    @Override
    public WorkflowTool getTool(String toolName) {
        if (tools == null) {
            return null;
        }

        for (WorkflowTool tool : tools) {
            if (Objects.equals(tool.getEntity().getIdentity().getName(), toolName)) {
                return tool;
            }
        }

        return null;
    }

    private Optional<VariableEntity> fetchWorkflowVariable(List<VariableEntity> variables, String variableName) {
        return variables.stream().filter(variable -> Objects.equals(variable.getVariable(), variableName)).findFirst();
    }
}

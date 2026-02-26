package com.dify.core.tools.workflow_as_tool;

import com.dify.core.db.session_factory.SessionFactory;
import com.dify.core.model_runtime.entities.llm_entities.LLMUsage;
import com.dify.core.model_runtime.entities.llm_entities.LLMUsageMetadata;
import com.dify.core.tools.__base.tool.Tool;
import com.dify.core.tools.__base.tool_runtime.ToolRuntime;
import com.dify.core.tools.entities.tool_entities.ToolEntity;
import com.dify.core.tools.entities.tool_entities.ToolInvokeMessage;
import com.dify.core.tools.entities.tool_entities.ToolParameter;
import com.dify.core.tools.entities.tool_entities.ToolProviderType;
import com.dify.core.tools.errors.ToolInvokeError;
import com.dify.core.workflow.file.File;
import com.dify.core.workflow.file.FileTransferMethod;
import com.dify.factories.file_factory.FileFactory;
import com.dify.models.Account;
import com.dify.models.Tenant;
import com.dify.models.model.App;
import com.dify.models.model.EndUser;
import com.dify.models.workflow.Workflow;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class WorkflowTool extends Tool {

    private static final String FILE_MODEL_IDENTITY = "__dify__file__";

    private final String workflowAppId;
    private final String workflowAsToolId;
    private final String version;
    private final Map<String, Object> workflowEntities;
    private final Integer workflowCallDepth;
    private final String label;
    private LLMUsage latestUsage = LLMUsage.emptyUsage();

    public WorkflowTool(
            String workflowAppId,
            String workflowAsToolId,
            String version,
            Map<String, Object> workflowEntities,
            Integer workflowCallDepth,
            ToolEntity entity,
            ToolRuntime runtime,
            String label) {
        super(entity, runtime);
        this.workflowAppId = workflowAppId;
        this.workflowAsToolId = workflowAsToolId;
        this.version = version;
        this.workflowEntities = workflowEntities;
        this.workflowCallDepth = workflowCallDepth;
        this.label = label == null ? "Workflow" : label;
    }

    @Override
    public ToolProviderType toolProviderType() {
        return ToolProviderType.WORKFLOW;
    }

    @Override
    protected Iterable<ToolInvokeMessage> invoke(
            String userId,
            Map<String, Object> toolParameters,
            String conversationId,
            String appId,
            String messageId) {
        App app = getApp(workflowAppId);
        Workflow workflow = getWorkflow(workflowAppId, version);

        var transformed = transformArgs(toolParameters);
        Map<String, Object> transformedParameters = transformed.parameters;
        List<Map<String, Object>> files = transformed.files;

        var generator = new com.dify.core.app.apps.workflow.app_generator.WorkflowAppGenerator();
        if (getRuntime() == null || getRuntime().getInvokeFrom() == null) {
            throw new ToolInvokeError("Tool runtime invokeFrom is required");
        }

        Object user = resolveUser(userId);
        if (user == null) {
            throw new ToolInvokeError("User not found");
        }

        latestUsage = LLMUsage.emptyUsage();
        Map<String, Object> result = generator.generate(
                app,
                workflow,
                user,
                Map.of("inputs", transformedParameters, "files", files),
                getRuntime().getInvokeFrom(),
                false,
                workflowCallDepth + 1,
                null);

        Map<String, Object> data = castMap(result.getOrDefault("data", new HashMap<>()));
        Object error = data.get("error");
        if (error != null) {
            throw new ToolInvokeError(String.valueOf(error));
        }

        List<ToolInvokeMessage> messages = new ArrayList<>();
        Map<String, Object> outputs = castMap(data.get("outputs"));
        if (outputs == null) {
            outputs = new HashMap<>();
        } else {
            var extractResult = extractFiles(outputs);
            outputs = extractResult.result;
            for (File file : extractResult.files) {
                messages.add(createFileMessage(file));
            }
        }

        for (Map.Entry<String, Object> entry : outputs.entrySet()) {
            if (!java.util.Set.of("text", "json", "files").contains(entry.getKey())) {
                messages.add(createVariableMessage(entry.getKey(), entry.getValue()));
            }
        }

        latestUsage = deriveUsageFromResult(data);
        messages.add(createTextMessage(new com.fasterxml.jackson.databind.ObjectMapper().valueToTree(outputs).toString()));
        messages.add(createJsonMessage(outputs, true));

        return messages;
    }

    public static LLMUsage deriveUsageFromResult(Map<String, Object> data) {
        Map<String, Object> usageDict = extractUsageDict(data);
        if (usageDict != null) {
            return LLMUsage.fromMetadata(LLMUsageMetadata.fromMap(usageDict));
        }

        Object totalTokens = data.get("total_tokens");
        Object totalPrice = data.get("total_price");
        if (totalTokens == null && totalPrice == null) {
            return LLMUsage.emptyUsage();
        }

        Map<String, Object> usageMetadata = new HashMap<>();
        if (totalTokens != null) {
            try {
                usageMetadata.put("total_tokens", Integer.parseInt(String.valueOf(totalTokens)));
            } catch (NumberFormatException ignore) {
                // keep aligned with Python behavior: silently ignore parsing failure
            }
        }

        if (totalPrice != null) {
            usageMetadata.put("total_price", String.valueOf(totalPrice));
        }

        Object currency = data.get("currency");
        if (currency != null) {
            usageMetadata.put("currency", currency);
        }

        if (usageMetadata.isEmpty()) {
            return LLMUsage.emptyUsage();
        }

        return LLMUsage.fromMetadata(LLMUsageMetadata.fromMap(usageMetadata));
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> extractUsageDict(Map<String, Object> payload) {
        Object usageCandidate = payload.get("usage");
        if (usageCandidate instanceof Map<?, ?> usageMap) {
            return (Map<String, Object>) usageMap;
        }

        Object metadataCandidate = payload.get("metadata");
        if (metadataCandidate instanceof Map<?, ?> metadataMap) {
            Object metadataUsageCandidate = ((Map<String, Object>) metadataMap).get("usage");
            if (metadataUsageCandidate instanceof Map<?, ?> metadataUsageMap) {
                return (Map<String, Object>) metadataUsageMap;
            }
        }

        for (Object value : payload.values()) {
            if (value instanceof Map<?, ?> mapValue) {
                Map<String, Object> found = extractUsageDict((Map<String, Object>) mapValue);
                if (found != null) {
                    return found;
                }
            } else if (value instanceof List<?> listValue) {
                for (Object item : listValue) {
                    if (item instanceof Map<?, ?> mapItem) {
                        Map<String, Object> found = extractUsageDict((Map<String, Object>) mapItem);
                        if (found != null) {
                            return found;
                        }
                    }
                }
            }
        }

        return null;
    }

    public WorkflowTool forkToolRuntime(ToolRuntime runtime) {
        return new WorkflowTool(
                workflowAppId,
                workflowAsToolId,
                version,
                workflowEntities,
                workflowCallDepth,
                getEntity().copy(),
                runtime,
                label);
    }

    private Object resolveUser(String userId) {
        return resolveUserFromDatabase(userId);
    }

    private Object resolveUserFromDatabase(String userId) {
        try (var session = SessionFactory.createSession()) {
            Tenant tenant = session.scalar(Tenant.class, getRuntime().getTenantId());
            if (tenant == null) {
                return null;
            }

            Account user = session.scalar(Account.class, userId);
            if (user != null) {
                user.setCurrentTenant(tenant);
                session.expunge(user);
                return user;
            }

            EndUser endUser = session.scalar(EndUser.class, userId, tenant.getId());
            if (endUser != null) {
                session.expunge(endUser);
                return endUser;
            }

            return null;
        }
    }

    private Workflow getWorkflow(String appId, String version) {
        try (var session = SessionFactory.createSession()) {
            Workflow workflow;
            if (version == null || version.isBlank()) {
                workflow = session.latestPublishedWorkflow(appId);
            } else {
                workflow = session.workflowByVersion(appId, version);
            }

            if (workflow == null) {
                throw new IllegalArgumentException("workflow not found or not published");
            }

            session.expunge(workflow);
            return workflow;
        }
    }

    private App getApp(String appId) {
        try (var session = SessionFactory.createSession()) {
            App app = session.scalar(App.class, appId);
            if (app == null) {
                throw new IllegalArgumentException("app not found");
            }

            session.expunge(app);
            return app;
        }
    }

    private TransformResult transformArgs(Map<String, Object> toolParameters) {
        var parameterRules = getMergedRuntimeParameters();
        Map<String, Object> parametersResult = new HashMap<>();
        List<Map<String, Object>> files = new ArrayList<>();

        for (ToolParameter parameter : parameterRules) {
            if (parameter.getType() == ToolParameter.ToolParameterType.SYSTEM_FILES) {
                Object fileObj = toolParameters.get(parameter.getName());
                if (fileObj instanceof List<?> rawFiles) {
                    try {
                        for (Object item : rawFiles) {
                            File file = File.modelValidate(item);
                            Map<String, Object> fileDict = new HashMap<>();
                            fileDict.put("transfer_method", file.getTransferMethod().getValue());
                            fileDict.put("type", file.getType().getValue());

                            if (file.getTransferMethod() == FileTransferMethod.TOOL_FILE) {
                                fileDict.put("tool_file_id", file.getRelatedId());
                            } else if (file.getTransferMethod() == FileTransferMethod.LOCAL_FILE) {
                                fileDict.put("upload_file_id", file.getRelatedId());
                            } else if (file.getTransferMethod() == FileTransferMethod.REMOTE_URL) {
                                fileDict.put("url", file.generateUrl());
                            }

                            files.add(fileDict);
                        }
                    } catch (Exception exception) {
                        log.error("Failed to transform file {}", fileObj, exception);
                    }
                }
            } else {
                parametersResult.put(parameter.getName(), toolParameters.get(parameter.getName()));
            }
        }

        return new TransformResult(parametersResult, files);
    }

    private ExtractFileResult extractFiles(Map<String, Object> outputs) {
        List<File> files = new ArrayList<>();
        Map<String, Object> result = new HashMap<>();

        for (Map.Entry<String, Object> entry : outputs.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof List<?> listValue) {
                for (Object item : listValue) {
                    if (item instanceof Map<?, ?> mapItem
                            && Objects.equals(((Map<?, ?>) item).get("dify_model_identity"), FILE_MODEL_IDENTITY)) {
                        Map<String, Object> itemMap = updateFileMapping(castMap(mapItem));
                        File file = FileFactory.buildFromMapping(itemMap, String.valueOf(getRuntime().getTenantId()));
                        files.add(file);
                    }
                }
            } else if (value instanceof Map<?, ?> mapValue
                    && Objects.equals(mapValue.get("dify_model_identity"), FILE_MODEL_IDENTITY)) {
                Map<String, Object> valueMap = updateFileMapping(castMap(mapValue));
                File file = FileFactory.buildFromMapping(valueMap, String.valueOf(getRuntime().getTenantId()));
                files.add(file);
                value = valueMap;
            }

            result.put(entry.getKey(), value);
        }

        return new ExtractFileResult(result, files);
    }

    private Map<String, Object> updateFileMapping(Map<String, Object> fileDict) {
        FileTransferMethod transferMethod = FileTransferMethod.valueOfLabel((String) fileDict.get("transfer_method"));
        if (transferMethod == FileTransferMethod.TOOL_FILE) {
            fileDict.put("tool_file_id", fileDict.get("related_id"));
        } else if (transferMethod == FileTransferMethod.LOCAL_FILE) {
            fileDict.put("upload_file_id", fileDict.get("related_id"));
        }

        return fileDict;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return null;
    }

    private record TransformResult(Map<String, Object> parameters, List<Map<String, Object>> files) {}

    private record ExtractFileResult(Map<String, Object> result, List<File> files) {}
}

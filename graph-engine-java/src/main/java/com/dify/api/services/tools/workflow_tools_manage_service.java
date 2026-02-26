package com.dify.api.services.tools;

import com.dify.api.models.tools;
import com.dify.api.models.workflow;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Java translation for api/services/tools/workflow_tools_manage_service.py.
 */
@RequiredArgsConstructor
public class workflow_tools_manage_service {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final WorkflowToolProviderRepository workflowToolProviderRepository;
    private final AppRepository appRepository;
    private final WorkflowProviderControllerFactory providerControllerFactory;
    private final ToolLabelManagerAdapter toolLabelManagerAdapter;
    private final ToolTransformServiceAdapter toolTransformServiceAdapter;

    public Map<String, String> createWorkflowTool(
            String userId,
            String tenantId,
            String workflowAppId,
            String name,
            String label,
            Map<String, Object> icon,
            String description,
            List<Map<String, Object>> parameters,
            String privacyPolicy,
            List<String> labels
    ) {
        Optional<tools.WorkflowToolProvider> existing = workflowToolProviderRepository
                .findByTenantIdAndNameOrAppId(tenantId, name, workflowAppId);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Tool with name " + name + " or appId " + workflowAppId + " already exists");
        }

        App app = appRepository.findByIdAndTenantId(workflowAppId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("App " + workflowAppId + " not found"));

        workflow.WorkflowEntity workflowEntity = app.getWorkflow();
        if (workflowEntity == null) {
            throw new IllegalArgumentException("Workflow not found for app " + workflowAppId);
        }

        tools.WorkflowToolProvider provider = tools.WorkflowToolProvider.builder()
                .tenantId(tenantId)
                .userId(userId)
                .appId(workflowAppId)
                .name(name)
                .label(label)
                .icon(writeJson(icon))
                .description(description)
                .parameterConfiguration(writeJson(parameters))
                .privacyPolicy(privacyPolicy == null ? "" : privacyPolicy)
                .version(workflowEntity.getVersion())
                .build();

        providerControllerFactory.fromDb(provider);
        workflowToolProviderRepository.save(provider);

        if (labels != null) {
            toolLabelManagerAdapter.updateToolLabels(toolTransformServiceAdapter.workflowProviderToController(provider), labels);
        }
        return Map.of("result", "success");
    }

    public Map<String, String> updateWorkflowTool(
            String userId,
            String tenantId,
            String workflowToolId,
            String name,
            String label,
            Map<String, Object> icon,
            String description,
            List<Map<String, Object>> parameters,
            String privacyPolicy,
            List<String> labels
    ) {
        Optional<tools.WorkflowToolProvider> existing = workflowToolProviderRepository
                .findByTenantIdAndNameAndIdNot(tenantId, name, workflowToolId);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Tool with name " + name + " already exists");
        }

        tools.WorkflowToolProvider provider = workflowToolProviderRepository.findByTenantIdAndId(tenantId, workflowToolId)
                .orElseThrow(() -> new IllegalArgumentException("Tool " + workflowToolId + " not found"));

        App app = appRepository.findByIdAndTenantId(provider.getAppId(), tenantId)
                .orElseThrow(() -> new IllegalArgumentException("App " + provider.getAppId() + " not found"));

        workflow.WorkflowEntity workflowEntity = app.getWorkflow();
        if (workflowEntity == null) {
            throw new IllegalArgumentException("Workflow not found for app " + provider.getAppId());
        }

        provider.setName(name);
        provider.setLabel(label);
        provider.setIcon(writeJson(icon));
        provider.setDescription(description);
        provider.setParameterConfiguration(writeJson(parameters));
        provider.setPrivacyPolicy(privacyPolicy == null ? "" : privacyPolicy);
        provider.setVersion(workflowEntity.getVersion());
        provider.setUpdatedAt(LocalDateTime.now());

        providerControllerFactory.fromDb(provider);
        workflowToolProviderRepository.save(provider);

        if (labels != null) {
            toolLabelManagerAdapter.updateToolLabels(toolTransformServiceAdapter.workflowProviderToController(provider), labels);
        }
        return Map.of("result", "success");
    }

    public List<Object> listTenantWorkflowTools(String userId, String tenantId) {
        List<tools.WorkflowToolProvider> dbTools = workflowToolProviderRepository.findByTenantId(tenantId);
        Map<String, String> providerIdToAppId = dbTools.stream().collect(Collectors.toMap(tools.WorkflowToolProvider::getId, tools.WorkflowToolProvider::getAppId));

        List<Object> controllers = new ArrayList<>();
        for (tools.WorkflowToolProvider provider : dbTools) {
            try {
                controllers.add(toolTransformServiceAdapter.workflowProviderToController(provider));
            } catch (Exception ignored) {
            }
        }

        Map<String, List<String>> labels = toolLabelManagerAdapter.getToolsLabels(controllers);
        List<Object> result = new ArrayList<>();

        for (Object tool : controllers) {
            String providerId = toolTransformServiceAdapter.getProviderId(tool);
            Object userProvider = toolTransformServiceAdapter.workflowProviderToUserProvider(
                    tool,
                    labels.getOrDefault(providerId, List.of()),
                    providerIdToAppId.get(providerId)
            );
            toolTransformServiceAdapter.repackProvider(tenantId, userProvider);
            toolTransformServiceAdapter.attachSingleTool(userProvider, tool, labels.getOrDefault(providerId, List.of()), tenantId);
            result.add(userProvider);
        }

        return result;
    }

    public Map<String, String> deleteWorkflowTool(String userId, String tenantId, String workflowToolId) {
        workflowToolProviderRepository.deleteByTenantIdAndId(tenantId, workflowToolId);
        return Map.of("result", "success");
    }

    public Map<String, Object> getWorkflowToolByToolId(String userId, String tenantId, String workflowToolId) {
        tools.WorkflowToolProvider dbTool = workflowToolProviderRepository.findByTenantIdAndId(tenantId, workflowToolId)
                .orElseThrow(() -> new IllegalArgumentException("Tool not found"));
        return getWorkflowTool(tenantId, dbTool);
    }

    public Map<String, Object> getWorkflowToolByAppId(String userId, String tenantId, String workflowAppId) {
        tools.WorkflowToolProvider dbTool = workflowToolProviderRepository.findByTenantIdAndAppId(tenantId, workflowAppId)
                .orElseThrow(() -> new IllegalArgumentException("Tool not found"));
        return getWorkflowTool(tenantId, dbTool);
    }

    public Map<String, Object> getWorkflowTool(String tenantId, tools.WorkflowToolProvider dbTool) {
        App workflowApp = appRepository.findByIdAndTenantId(dbTool.getAppId(), dbTool.getTenantId())
                .orElseThrow(() -> new IllegalArgumentException("App " + dbTool.getAppId() + " not found"));

        workflow.WorkflowEntity workflowEntity = workflowApp.getWorkflow();
        if (workflowEntity == null) {
            throw new IllegalArgumentException("Workflow not found");
        }

        Object toolController = toolTransformServiceAdapter.workflowProviderToController(dbTool);
        Object workflowTool = toolTransformServiceAdapter.firstWorkflowTool(toolController, tenantId);
        Map<String, Object> toolEntity = toolTransformServiceAdapter.getToolEntity(workflowTool);

        Map<String, Object> result = new HashMap<>();
        result.put("name", dbTool.getName());
        result.put("label", dbTool.getLabel());
        result.put("workflowToolId", dbTool.getId());
        result.put("workflowAppId", dbTool.getAppId());
        result.put("icon", readJsonMap(dbTool.getIcon()));
        result.put("description", dbTool.getDescription());
        result.put("parameters", dbTool.getParameterConfigurations());
        result.put("outputSchema", toolEntity.get("outputSchema"));
        result.put("tool", toolTransformServiceAdapter.convertToolEntityToApiEntity(workflowTool, toolLabelManagerAdapter.getToolLabels(toolController), tenantId));
        result.put("synced", workflowEntity.getVersion().equals(dbTool.getVersion()));
        result.put("privacyPolicy", dbTool.getPrivacyPolicy());
        return result;
    }

    private static String writeJson(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalArgumentException("json encode failed", e);
        }
    }

    private static Map<String, Object> readJsonMap(String value) {
        try {
            return MAPPER.readValue(value, new TypeReference<>() {});
        } catch (Exception e) {
            throw new IllegalArgumentException("json decode failed", e);
        }
    }

    public interface WorkflowToolProviderRepository {
        Optional<tools.WorkflowToolProvider> findByTenantIdAndNameOrAppId(String tenantId, String name, String appId);

        Optional<tools.WorkflowToolProvider> findByTenantIdAndNameAndIdNot(String tenantId, String name, String workflowToolId);

        Optional<tools.WorkflowToolProvider> findByTenantIdAndId(String tenantId, String workflowToolId);

        Optional<tools.WorkflowToolProvider> findByTenantIdAndAppId(String tenantId, String appId);

        List<tools.WorkflowToolProvider> findByTenantId(String tenantId);

        void save(tools.WorkflowToolProvider provider);

        void deleteByTenantIdAndId(String tenantId, String workflowToolId);
    }

    public interface AppRepository {
        Optional<App> findByIdAndTenantId(String appId, String tenantId);
    }

    public interface WorkflowProviderControllerFactory {
        Object fromDb(tools.WorkflowToolProvider provider);
    }

    public interface ToolLabelManagerAdapter {
        void updateToolLabels(Object toolProviderController, List<String> labels);

        Map<String, List<String>> getToolsLabels(List<Object> tools);

        List<String> getToolLabels(Object toolProviderController);
    }

    public interface ToolTransformServiceAdapter {
        Object workflowProviderToController(tools.WorkflowToolProvider provider);

        String getProviderId(Object providerController);

        Object workflowProviderToUserProvider(Object providerController, List<String> labels, String workflowAppId);

        void repackProvider(String tenantId, Object provider);

        void attachSingleTool(Object userProvider, Object providerController, List<String> labels, String tenantId);

        Object firstWorkflowTool(Object providerController, String tenantId);

        Map<String, Object> getToolEntity(Object workflowTool);

        Object convertToolEntityToApiEntity(Object workflowTool, List<String> labels, String tenantId);
    }

    public interface App {
        workflow.WorkflowEntity getWorkflow();
    }
}

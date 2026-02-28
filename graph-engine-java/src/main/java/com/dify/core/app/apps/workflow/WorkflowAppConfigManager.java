package com.dify.core.app.apps.workflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Java rewrite of api/core/app/apps/workflow/app_config_manager.py.
 */
public final class WorkflowAppConfigManager {

    private WorkflowAppConfigManager() {
    }

    public static WorkflowAppConfig getAppConfig(App appModel, Workflow workflow) {
        Map<String, Object> featuresDict = workflow.getFeaturesDict();
        Object appMode = AppMode.valueOfMode(appModel.getMode());

        WorkflowAppConfig appConfig = new WorkflowAppConfig();
        appConfig.setTenantId(appModel.getTenantId());
        appConfig.setAppId(appModel.getId());
        appConfig.setAppMode(appMode);
        appConfig.setWorkflowId(workflow.getId());
        appConfig.setSensitiveWordAvoidance(SensitiveWordAvoidanceConfigManager.convert(featuresDict));
        appConfig.setVariables(WorkflowVariablesConfigManager.convert(workflow));
        appConfig.setAdditionalFeatures(BaseAppConfigManager.convertFeatures(featuresDict, appMode));
        return appConfig;
    }

    public static Map<String, Object> configValidate(
            String tenantId,
            Map<String, Object> config,
            boolean onlyStructureValidate
    ) {
        List<String> relatedConfigKeys = new ArrayList<>();

        ConfigValidationResult fileUpload = FileUploadConfigManager.validateAndSetDefaults(config);
        config = fileUpload.getConfig();
        relatedConfigKeys.addAll(fileUpload.getRelatedConfigKeys());

        ConfigValidationResult tts = TextToSpeechConfigManager.validateAndSetDefaults(config);
        config = tts.getConfig();
        relatedConfigKeys.addAll(tts.getRelatedConfigKeys());

        ConfigValidationResult moderation = SensitiveWordAvoidanceConfigManager.validateAndSetDefaults(
                tenantId,
                config,
                onlyStructureValidate
        );
        config = moderation.getConfig();
        relatedConfigKeys.addAll(moderation.getRelatedConfigKeys());

        Set<String> deduplicated = new HashSet<>(relatedConfigKeys);
        Map<String, Object> filteredConfig = new HashMap<>();
        for (String key : deduplicated) {
            filteredConfig.put(key, config.get(key));
        }
        return filteredConfig;
    }

    // Placeholder dependency contracts to keep this module self-contained.
    public interface App {
        String getTenantId();

        String getId();

        String getMode();
    }

    public interface Workflow {
        Map<String, Object> getFeaturesDict();

        String getId();
    }

    public interface ConfigValidationResult {
        Map<String, Object> getConfig();

        List<String> getRelatedConfigKeys();
    }

    public static final class AppMode {
        private AppMode() {
        }

        public static Object valueOfMode(String mode) {
            return mode;
        }
    }

    public static final class BaseAppConfigManager {
        private BaseAppConfigManager() {
        }

        public static Map<String, Object> convertFeatures(Map<String, Object> featuresDict, Object appMode) {
            return new HashMap<>(featuresDict);
        }
    }

    public static final class SensitiveWordAvoidanceConfigManager {
        private SensitiveWordAvoidanceConfigManager() {
        }

        public static Object convert(Map<String, Object> config) {
            return config.get("sensitive_word_avoidance");
        }

        public static ConfigValidationResult validateAndSetDefaults(String tenantId, Map<String, Object> config,
                                                                    boolean onlyStructureValidate) {
            return new BasicValidationResult(config, List.of("sensitive_word_avoidance"));
        }
    }

    public static final class FileUploadConfigManager {
        private FileUploadConfigManager() {
        }

        public static ConfigValidationResult validateAndSetDefaults(Map<String, Object> config) {
            return new BasicValidationResult(config, List.of("file_upload"));
        }
    }

    public static final class TextToSpeechConfigManager {
        private TextToSpeechConfigManager() {
        }

        public static ConfigValidationResult validateAndSetDefaults(Map<String, Object> config) {
            return new BasicValidationResult(config, List.of("text_to_speech"));
        }
    }

    public static final class WorkflowVariablesConfigManager {
        private WorkflowVariablesConfigManager() {
        }

        public static Object convert(Workflow workflow) {
            return workflow;
        }
    }

    private record BasicValidationResult(Map<String, Object> config, List<String> relatedConfigKeys)
            implements ConfigValidationResult {
        @Override
        public Map<String, Object> getConfig() {
            return config;
        }

        @Override
        public List<String> getRelatedConfigKeys() {
            return relatedConfigKeys;
        }
    }
}

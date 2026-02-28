package com.dify.core.app.apps.workflow;

import java.util.Map;

public class WorkflowAppConfig {
    private String tenantId;
    private String appId;
    private Object appMode;
    private String workflowId;
    private Object sensitiveWordAvoidance;
    private Object variables;
    private Map<String, Object> additionalFeatures;

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }
    public Object getAppMode() { return appMode; }
    public void setAppMode(Object appMode) { this.appMode = appMode; }
    public String getWorkflowId() { return workflowId; }
    public void setWorkflowId(String workflowId) { this.workflowId = workflowId; }
    public Object getSensitiveWordAvoidance() { return sensitiveWordAvoidance; }
    public void setSensitiveWordAvoidance(Object sensitiveWordAvoidance) { this.sensitiveWordAvoidance = sensitiveWordAvoidance; }
    public Object getVariables() { return variables; }
    public void setVariables(Object variables) { this.variables = variables; }
    public Map<String, Object> getAdditionalFeatures() { return additionalFeatures; }
    public void setAdditionalFeatures(Map<String, Object> additionalFeatures) { this.additionalFeatures = additionalFeatures; }
}

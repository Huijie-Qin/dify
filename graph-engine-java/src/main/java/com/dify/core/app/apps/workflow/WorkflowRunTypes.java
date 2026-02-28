package com.dify.core.app.apps.workflow;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class WorkflowRunTypes {
    private WorkflowRunTypes() {}

    public enum InvokeFrom { DEBUGGER }

    public static Object resolveUserFrom(Object invokeFrom) { return invokeFrom; }

    public static Graph initGraph(Map<String, Object> graphConfig, GraphRuntimeState graphRuntimeState, String workflowId,
                                  String tenantId, String userId, Object userFrom, Object invokeFrom, String rootNodeId) {
        return new Graph();
    }

    public static SingleNodeExecutionPrepared prepareSingleNodeExecution(Workflow workflow,
                                                                         boolean singleIterationRun,
                                                                         boolean singleLoopRun) {
        VariablePool variablePool = new VariablePool(null, Collections.emptyMap(), Collections.emptyMap(), List.of());
        return new SingleNodeExecutionPrepared(new Graph(), variablePool, new GraphRuntimeState(variablePool));
    }

    public static void handleEvent(WorkflowEntry workflowEntry, Object event) {
    }

    public interface VariableLoader {}
    public interface WorkflowExecutionRepository {}
    public interface WorkflowNodeExecutionRepository {}
    public interface GraphEngineLayer {}

    public static class Workflow {
        private final String id;
        private final String tenantId;
        private final String appId;
        private final Map<String, Object> graphDict;
        private final Map<String, Object> environmentVariables;

        public Workflow(String id, String tenantId, String appId, Map<String, Object> graphDict, Map<String, Object> environmentVariables) {
            this.id = id; this.tenantId = tenantId; this.appId = appId; this.graphDict = graphDict; this.environmentVariables = environmentVariables;
        }
        public String getId() { return id; }
        public String getTenantId() { return tenantId; }
        public String getAppId() { return appId; }
        public Map<String, Object> getGraphDict() { return graphDict; }
        public Map<String, Object> getEnvironmentVariables() { return environmentVariables; }
    }

    public static class AppConfig {
        private final String appId;
        private final String workflowId;
        public AppConfig(String appId, String workflowId) { this.appId = appId; this.workflowId = workflowId; }
        public String getAppId() { return appId; }
        public String getWorkflowId() { return workflowId; }
    }

    public static class WorkflowAppGenerateEntity {
        private final AppConfig appConfig;
        private final Object invokeFrom;
        private final boolean singleIterationRun;
        private final boolean singleLoopRun;
        private final Map<String, Object> inputs;
        private final List<Object> files;
        private final String workflowExecutionId;
        private final String userId;
        private final int callDepth;
        private final String taskId;

        public WorkflowAppGenerateEntity(AppConfig appConfig, Object invokeFrom, boolean singleIterationRun, boolean singleLoopRun,
                                         Map<String, Object> inputs, List<Object> files, String workflowExecutionId,
                                         String userId, int callDepth, String taskId) {
            this.appConfig = appConfig;
            this.invokeFrom = invokeFrom;
            this.singleIterationRun = singleIterationRun;
            this.singleLoopRun = singleLoopRun;
            this.inputs = inputs;
            this.files = files;
            this.workflowExecutionId = workflowExecutionId;
            this.userId = userId;
            this.callDepth = callDepth;
            this.taskId = taskId;
        }

        public AppConfig getAppConfig() { return appConfig; }
        public Object getInvokeFrom() { return invokeFrom; }
        public boolean isSingleIterationRun() { return singleIterationRun; }
        public boolean isSingleLoopRun() { return singleLoopRun; }
        public Map<String, Object> getInputs() { return inputs; }
        public List<Object> getFiles() { return files; }
        public String getWorkflowExecutionId() { return workflowExecutionId; }
        public String getUserId() { return userId; }
        public int getCallDepth() { return callDepth; }
        public String getTaskId() { return taskId; }
    }

    public record SystemVariable(List<Object> files, String userId, String appId, long timestamp,
                                 String workflowId, String workflowExecutionId) {}

    public record VariablePool(SystemVariable systemVariables, Map<String, Object> userInputs,
                               Map<String, Object> environmentVariables, List<Object> conversationVariables) {}

    public static class GraphRuntimeState {
        private final VariablePool variablePool;
        public GraphRuntimeState(VariablePool variablePool) { this.variablePool = variablePool; }
        public VariablePool getVariablePool() { return variablePool; }
    }

    public static class Graph {}

    public record SingleNodeExecutionPrepared(Graph graph, VariablePool variablePool, GraphRuntimeState graphRuntimeState) {}

    public static class WorkflowPersistenceLayer implements GraphEngineLayer {
        public WorkflowPersistenceLayer(WorkflowAppGenerateEntity applicationGenerateEntity,
                                        Workflow workflow,
                                        WorkflowExecutionRepository workflowExecutionRepository,
                                        WorkflowNodeExecutionRepository workflowNodeExecutionRepository) {}
    }

    public static class WorkflowEntry {
        public WorkflowEntry(String tenantId, String appId, String workflowId, Graph graph, Map<String, Object> graphConfig,
                             String userId, Object userFrom, Object invokeFrom, int callDepth, VariablePool variablePool,
                             GraphRuntimeState graphRuntimeState, String commandChannel) {
        }

        public void layer(GraphEngineLayer layer) {
        }

        public Iterable<Object> run() { return List.of(); }
    }
}

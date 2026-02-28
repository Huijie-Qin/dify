package com.dify.core.app.apps.workflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Java rewrite of api/core/app/apps/workflow/app_runner.py.
 */
public class WorkflowAppRunner {

    private final WorkflowRunTypes.WorkflowAppGenerateEntity applicationGenerateEntity;
    private final WorkflowQueueSupport.AppQueueManager queueManager;
    private final WorkflowRunTypes.VariableLoader variableLoader;
    private final WorkflowRunTypes.Workflow workflow;
    private final String sysUserId;
    private final String rootNodeId;
    private final WorkflowRunTypes.WorkflowExecutionRepository workflowExecutionRepository;
    private final WorkflowRunTypes.WorkflowNodeExecutionRepository workflowNodeExecutionRepository;
    private final List<WorkflowRunTypes.GraphEngineLayer> graphEngineLayers;
    private final WorkflowRunTypes.GraphRuntimeState resumeGraphRuntimeState;

    public WorkflowAppRunner(
            WorkflowRunTypes.WorkflowAppGenerateEntity applicationGenerateEntity,
            WorkflowQueueSupport.AppQueueManager queueManager,
            WorkflowRunTypes.VariableLoader variableLoader,
            WorkflowRunTypes.Workflow workflow,
            String systemUserId,
            String rootNodeId,
            WorkflowRunTypes.WorkflowExecutionRepository workflowExecutionRepository,
            WorkflowRunTypes.WorkflowNodeExecutionRepository workflowNodeExecutionRepository,
            List<WorkflowRunTypes.GraphEngineLayer> graphEngineLayers,
            WorkflowRunTypes.GraphRuntimeState graphRuntimeState
    ) {
        this.applicationGenerateEntity = applicationGenerateEntity;
        this.queueManager = queueManager;
        this.variableLoader = variableLoader;
        this.workflow = workflow;
        this.sysUserId = systemUserId;
        this.rootNodeId = rootNodeId;
        this.workflowExecutionRepository = workflowExecutionRepository;
        this.workflowNodeExecutionRepository = workflowNodeExecutionRepository;
        this.graphEngineLayers = graphEngineLayers == null ? List.of() : graphEngineLayers;
        this.resumeGraphRuntimeState = graphRuntimeState;
    }

    public void run() {
        Object invokeFrom = applicationGenerateEntity.getInvokeFrom();
        if (applicationGenerateEntity.isSingleIterationRun() || applicationGenerateEntity.isSingleLoopRun()) {
            invokeFrom = WorkflowRunTypes.InvokeFrom.DEBUGGER;
        }
        Object userFrom = WorkflowRunTypes.resolveUserFrom(invokeFrom);

        WorkflowRunTypes.GraphRuntimeState graphRuntimeState = resumeGraphRuntimeState;
        WorkflowRunTypes.VariablePool variablePool;
        WorkflowRunTypes.Graph graph;

        if (graphRuntimeState != null) {
            variablePool = graphRuntimeState.getVariablePool();
            graph = WorkflowRunTypes.initGraph(workflow.getGraphDict(), graphRuntimeState, workflow.getId(), workflow.getTenantId(),
                    applicationGenerateEntity.getUserId(), userFrom, invokeFrom, rootNodeId);
        } else if (applicationGenerateEntity.isSingleIterationRun() || applicationGenerateEntity.isSingleLoopRun()) {
            WorkflowRunTypes.SingleNodeExecutionPrepared prepared = WorkflowRunTypes.prepareSingleNodeExecution(
                    workflow,
                    applicationGenerateEntity.isSingleIterationRun(),
                    applicationGenerateEntity.isSingleLoopRun()
            );
            graph = prepared.graph();
            variablePool = prepared.variablePool();
            graphRuntimeState = prepared.graphRuntimeState();
        } else {
            WorkflowRunTypes.SystemVariable systemInputs = new WorkflowRunTypes.SystemVariable(
                    applicationGenerateEntity.getFiles(),
                    sysUserId,
                    applicationGenerateEntity.getAppConfig().getAppId(),
                    System.currentTimeMillis() / 1000,
                    applicationGenerateEntity.getAppConfig().getWorkflowId(),
                    applicationGenerateEntity.getWorkflowExecutionId()
            );
            variablePool = new WorkflowRunTypes.VariablePool(systemInputs, applicationGenerateEntity.getInputs(),
                    workflow.getEnvironmentVariables(), List.of());
            graphRuntimeState = new WorkflowRunTypes.GraphRuntimeState(variablePool);
            graph = WorkflowRunTypes.initGraph(workflow.getGraphDict(), graphRuntimeState, workflow.getId(), workflow.getTenantId(),
                    applicationGenerateEntity.getUserId(), userFrom, invokeFrom, rootNodeId);
        }

        queueManager.getQueue().add(graphRuntimeState);

        WorkflowRunTypes.WorkflowEntry workflowEntry = new WorkflowRunTypes.WorkflowEntry(
                workflow.getTenantId(), workflow.getAppId(), workflow.getId(), graph, workflow.getGraphDict(),
                applicationGenerateEntity.getUserId(), userFrom, invokeFrom, applicationGenerateEntity.getCallDepth(),
                variablePool, graphRuntimeState, "workflow:" + applicationGenerateEntity.getTaskId() + ":commands"
        );

        workflowEntry.layer(new WorkflowRunTypes.WorkflowPersistenceLayer(
                applicationGenerateEntity,
                workflow,
                workflowExecutionRepository,
                workflowNodeExecutionRepository
        ));

        for (WorkflowRunTypes.GraphEngineLayer layer : graphEngineLayers) {
            workflowEntry.layer(layer);
        }

        for (Object event : workflowEntry.run()) {
            WorkflowRunTypes.handleEvent(workflowEntry, event);
        }
    }
}

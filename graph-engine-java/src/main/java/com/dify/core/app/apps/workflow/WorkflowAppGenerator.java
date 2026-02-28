package com.dify.core.app.apps.workflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Java rewrite of api/core/app/apps/workflow/app_generator.py.
 */
public class WorkflowAppGenerator {

    public boolean shouldPrepareUserInputs(WorkflowRunTypes.WorkflowAppGenerateEntity entity) {
        return !entity.isSingleIterationRun() && !entity.isSingleLoopRun();
    }

    public Object generate(
            WorkflowRunTypes.Workflow appModel,
            WorkflowRunTypes.Workflow workflow,
            Object user,
            WorkflowRunTypes.WorkflowAppGenerateEntity applicationGenerateEntity,
            boolean streaming,
            String rootNodeId,
            List<WorkflowRunTypes.GraphEngineLayer> graphEngineLayers
    ) {
        return generateInternal(
                appModel,
                workflow,
                user,
                applicationGenerateEntity,
                applicationGenerateEntity.getInvokeFrom(),
                streaming,
                rootNodeId,
                graphEngineLayers,
                null
        );
    }

    public Object resume(
            WorkflowRunTypes.Workflow appModel,
            WorkflowRunTypes.Workflow workflow,
            Object user,
            WorkflowRunTypes.WorkflowAppGenerateEntity applicationGenerateEntity,
            WorkflowRunTypes.GraphRuntimeState graphRuntimeState,
            WorkflowRunTypes.WorkflowExecutionRepository workflowExecutionRepository,
            WorkflowRunTypes.WorkflowNodeExecutionRepository workflowNodeExecutionRepository,
            List<WorkflowRunTypes.GraphEngineLayer> graphEngineLayers
    ) {
        return generateInternal(
                appModel,
                workflow,
                user,
                applicationGenerateEntity,
                applicationGenerateEntity.getInvokeFrom(),
                applicationGenerateEntity.isSingleIterationRun(),
                null,
                graphEngineLayers,
                graphRuntimeState
        );
    }

    private Object generateInternal(
            WorkflowRunTypes.Workflow appModel,
            WorkflowRunTypes.Workflow workflow,
            Object user,
            WorkflowRunTypes.WorkflowAppGenerateEntity applicationGenerateEntity,
            Object invokeFrom,
            boolean streaming,
            String rootNodeId,
            List<WorkflowRunTypes.GraphEngineLayer> graphEngineLayers,
            WorkflowRunTypes.GraphRuntimeState graphRuntimeState
    ) {
        WorkflowAppQueueManager queueManager = new WorkflowAppQueueManager(
                applicationGenerateEntity.getTaskId(),
                applicationGenerateEntity.getUserId(),
                applicationGenerateEntity.getInvokeFrom(),
                "workflow"
        );

        WorkflowAppRunner appRunner = new WorkflowAppRunner(
                applicationGenerateEntity,
                queueManager,
                new DummyVariableLoader(),
                workflow,
                applicationGenerateEntity.getUserId(),
                rootNodeId,
                new DummyWorkflowExecutionRepository(),
                new DummyWorkflowNodeExecutionRepository(),
                graphEngineLayers == null ? new ArrayList<>() : graphEngineLayers,
                graphRuntimeState
        );

        Thread workerThread = new Thread(appRunner::run);
        workerThread.start();

        WorkflowAppGenerateTaskPipeline pipeline = new WorkflowAppGenerateTaskPipeline(applicationGenerateEntity, queueManager);
        Object response = pipeline.process(streaming);
        handleResponse(workerThread);
        return response;
    }

    public Object singleIterationGenerate(Map<String, Object> payload) {
        return payload;
    }

    public Object singleLoopGenerate(Map<String, Object> payload) {
        return payload;
    }

    private void handleResponse(Thread workerThread) {
        try {
            workerThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Worker thread interrupted", e);
        }
    }

    private static class DummyVariableLoader implements WorkflowRunTypes.VariableLoader {
    }

    private static class DummyWorkflowExecutionRepository implements WorkflowRunTypes.WorkflowExecutionRepository {
    }

    private static class DummyWorkflowNodeExecutionRepository implements WorkflowRunTypes.WorkflowNodeExecutionRepository {
    }
}

package com.dify.core.app.apps.workflow;

/**
 * Java rewrite of api/core/app/apps/workflow/app_queue_manager.py.
 */
public class WorkflowAppQueueManager extends WorkflowQueueSupport.AppQueueManager {

    private final String appMode;

    public WorkflowAppQueueManager(String taskId, String userId, Object invokeFrom, String appMode) {
        super(taskId, userId, invokeFrom);
        this.appMode = appMode;
    }

    @Override
    protected void publish(Object event, PublishFrom publishFrom) {
        WorkflowQueueSupport.WorkflowQueueMessage message =
                new WorkflowQueueSupport.WorkflowQueueMessage(getTaskId(), appMode, event);
        getQueue().add(message);

        if (event instanceof WorkflowQueueSupport.QueueStopEvent
                || event instanceof WorkflowQueueSupport.QueueErrorEvent
                || event instanceof WorkflowQueueSupport.QueueMessageEndEvent
                || event instanceof WorkflowQueueSupport.QueueWorkflowSucceededEvent
                || event instanceof WorkflowQueueSupport.QueueWorkflowFailedEvent
                || event instanceof WorkflowQueueSupport.QueueWorkflowPartialSuccessEvent) {
            stopListen();
        }

        if (publishFrom == PublishFrom.APPLICATION_MANAGER && isStopped()) {
            throw new WorkflowQueueSupport.GenerateTaskStoppedError();
        }
    }

    public enum PublishFrom {
        APPLICATION_MANAGER,
        WORKFLOW_RUNNER
    }
}

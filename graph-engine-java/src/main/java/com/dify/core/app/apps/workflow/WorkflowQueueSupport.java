package com.dify.core.app.apps.workflow;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class WorkflowQueueSupport {
    private WorkflowQueueSupport() {
    }

    public abstract static class AppQueueManager {
        private final String taskId;
        private final String userId;
        private final Object invokeFrom;
        private final Queue<Object> queue = new ConcurrentLinkedQueue<>();
        private volatile boolean stopped;

        protected AppQueueManager(String taskId, String userId, Object invokeFrom) {
            this.taskId = taskId;
            this.userId = userId;
            this.invokeFrom = invokeFrom;
        }

        protected abstract void publish(Object event, WorkflowAppQueueManager.PublishFrom publishFrom);

        public String getTaskId() { return taskId; }
        public String getUserId() { return userId; }
        public Object getInvokeFrom() { return invokeFrom; }
        public Queue<Object> getQueue() { return queue; }

        public void stopListen() {
            this.stopped = true;
        }

        public boolean isStopped() {
            return stopped;
        }
    }

    public static class WorkflowQueueMessage {
        public final String taskId;
        public final String appMode;
        public final Object event;

        public WorkflowQueueMessage(String taskId, String appMode, Object event) {
            this.taskId = taskId;
            this.appMode = appMode;
            this.event = event;
        }
    }

    public static class GenerateTaskStoppedError extends RuntimeException {
    }

    public static class QueueStopEvent {}
    public static class QueueErrorEvent {}
    public static class QueueMessageEndEvent {}
    public static class QueueWorkflowSucceededEvent {}
    public static class QueueWorkflowFailedEvent {}
    public static class QueueWorkflowPartialSuccessEvent {}
}

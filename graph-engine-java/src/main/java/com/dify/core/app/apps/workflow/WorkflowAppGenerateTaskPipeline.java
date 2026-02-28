package com.dify.core.app.apps.workflow;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

/**
 * Java rewrite of api/core/app/apps/workflow/generate_task_pipeline.py.
 *
 * This class keeps the same high-level event-dispatch pipeline: queue event -> handler -> stream/blocking response.
 */
public class WorkflowAppGenerateTaskPipeline {

    private final Object applicationGenerateEntity;
    private final WorkflowAppQueueManager queueManager;

    public WorkflowAppGenerateTaskPipeline(Object applicationGenerateEntity, WorkflowAppQueueManager queueManager) {
        this.applicationGenerateEntity = applicationGenerateEntity;
        this.queueManager = queueManager;
    }

    public Object process(boolean stream) {
        if (stream) {
            return toStreamResponse();
        }
        return toBlockingResponse();
    }

    private Map<String, Object> toBlockingResponse() {
        Map<String, Object> blocking = new HashMap<>();
        Iterator<Object> iterator = processStreamResponse();
        while (iterator.hasNext()) {
            Object chunk = iterator.next();
            if (chunk instanceof Map<?, ?> map) {
                for (Map.Entry<?, ?> e : map.entrySet()) {
                    blocking.put(String.valueOf(e.getKey()), e.getValue());
                }
            }
        }
        return blocking;
    }

    private Iterator<Object> toStreamResponse() {
        return processStreamResponse();
    }

    private Iterator<Object> processStreamResponse() {
        return new Iterator<>() {
            private final Iterator<Object> queueIterator = queueManager.getQueue().iterator();

            @Override
            public boolean hasNext() {
                return queueIterator.hasNext();
            }

            @Override
            public Object next() {
                Object event = queueIterator.next();
                return dispatchEvent(event);
            }
        };
    }

    private Object dispatchEvent(Object event) {
        Function<Object, Object> handler = getEventHandlers().getOrDefault(event.getClass().getSimpleName(), this::handleUnknownEvent);
        return handler.apply(event);
    }

    private Map<String, Function<Object, Object>> getEventHandlers() {
        Map<String, Function<Object, Object>> handlers = new HashMap<>();
        handlers.put("QueueErrorEvent", this::handleErrorEvent);
        handlers.put("QueueWorkflowSucceededEvent", this::handleWorkflowSucceededEvent);
        handlers.put("QueueWorkflowPartialSuccessEvent", this::handleWorkflowPartialSuccessEvent);
        handlers.put("QueueWorkflowFailedEvent", this::handleWorkflowFailedAndStopEvents);
        handlers.put("QueueStopEvent", this::handleWorkflowFailedAndStopEvents);
        handlers.put("QueueTextChunkEvent", this::handleTextChunkEvent);
        handlers.put("QueueAgentLogEvent", this::handleAgentLogEvent);
        handlers.put("QueuePingEvent", this::handlePingEvent);
        return handlers;
    }

    private Object handlePingEvent(Object event) {
        return "ping";
    }

    private Object handleErrorEvent(Object event) {
        return Map.of("event", "error", "data", event);
    }

    private Object handleWorkflowSucceededEvent(Object event) {
        saveOutputForEvent(event);
        return Map.of("event", "workflow_succeeded", "data", event);
    }

    private Object handleWorkflowPartialSuccessEvent(Object event) {
        saveOutputForEvent(event);
        return Map.of("event", "workflow_partial_success", "data", event);
    }

    private Object handleWorkflowFailedAndStopEvents(Object event) {
        return Map.of("event", "workflow_failed_or_stopped", "data", event);
    }

    private Object handleTextChunkEvent(Object event) {
        return textChunkToStreamResponse(event);
    }

    private Object handleAgentLogEvent(Object event) {
        return Map.of("event", "agent_log", "data", event);
    }

    private Object handleUnknownEvent(Object event) {
        return Map.of("event", "unknown", "data", event);
    }

    private Object textChunkToStreamResponse(Object event) {
        return Map.of("event", "text_chunk", "data", event);
    }

    private void saveOutputForEvent(Object event) {
        // Keep this hook to match Python behavior where terminal workflow events persist output.
    }
}

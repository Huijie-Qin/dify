package com.dify.core.app.apps.workflow;

import java.util.HashMap;
import java.util.Map;

public final class WorkflowAppStreamTypes {
    private WorkflowAppStreamTypes() {
    }

    public static class WorkflowAppBlockingResponse {
        private final Map<String, Object> payload;

        public WorkflowAppBlockingResponse(Map<String, Object> payload) {
            this.payload = payload;
        }

        public Map<String, Object> modelDump() { return payload; }
    }

    public static class WorkflowAppStreamResponse {
        private final String workflowRunId;
        private final BaseStreamResponse streamResponse;

        public WorkflowAppStreamResponse(String workflowRunId, BaseStreamResponse streamResponse) {
            this.workflowRunId = workflowRunId;
            this.streamResponse = streamResponse;
        }

        public String getWorkflowRunId() { return workflowRunId; }
        public BaseStreamResponse getStreamResponse() { return streamResponse; }
    }

    public interface BaseStreamResponse {
        String getEvent();

        default Map<String, Object> modelDump() { return new HashMap<>(); }

        default Map<String, Object> toIgnoreDetailMap() { return modelDump(); }
    }

    public static class PingStreamResponse implements BaseStreamResponse {
        public String getEvent() { return "ping"; }
    }

    public static class ErrorStreamResponse implements BaseStreamResponse {
        private final String event;
        private final Throwable err;

        public ErrorStreamResponse(String event, Throwable err) {
            this.event = event;
            this.err = err;
        }

        public String getEvent() { return event; }
        public Throwable getErr() { return err; }
    }

    public static class NodeStartStreamResponse implements BaseStreamResponse {
        private final String event;

        public NodeStartStreamResponse(String event) { this.event = event; }
        public String getEvent() { return event; }
    }

    public static class NodeFinishStreamResponse implements BaseStreamResponse {
        private final String event;

        public NodeFinishStreamResponse(String event) { this.event = event; }
        public String getEvent() { return event; }
    }
}

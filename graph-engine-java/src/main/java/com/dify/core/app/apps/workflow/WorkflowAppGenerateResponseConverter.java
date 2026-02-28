package com.dify.core.app.apps.workflow;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Java rewrite of api/core/app/apps/workflow/generate_response_converter.py.
 */
public final class WorkflowAppGenerateResponseConverter {

    private WorkflowAppGenerateResponseConverter() {
    }

    public static Map<String, Object> convertBlockingFullResponse(WorkflowAppStreamTypes.WorkflowAppBlockingResponse blockingResponse) {
        return blockingResponse.modelDump();
    }

    public static Map<String, Object> convertBlockingSimpleResponse(
            WorkflowAppStreamTypes.WorkflowAppBlockingResponse blockingResponse
    ) {
        return convertBlockingFullResponse(blockingResponse);
    }

    public static Iterator<Object> convertStreamFullResponse(Iterator<WorkflowAppStreamTypes.WorkflowAppStreamResponse> streamResponse) {
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return streamResponse.hasNext();
            }

            @Override
            public Object next() {
                if (!streamResponse.hasNext()) {
                    throw new NoSuchElementException();
                }

                WorkflowAppStreamTypes.WorkflowAppStreamResponse chunk = streamResponse.next();
                WorkflowAppStreamTypes.BaseStreamResponse sub = chunk.getStreamResponse();

                if (sub instanceof WorkflowAppStreamTypes.PingStreamResponse) {
                    return "ping";
                }

                Map<String, Object> responseChunk = new HashMap<>();
                responseChunk.put("event", sub.getEvent());
                responseChunk.put("workflow_run_id", chunk.getWorkflowRunId());

                if (sub instanceof WorkflowAppStreamTypes.ErrorStreamResponse errorStreamResponse) {
                    responseChunk.putAll(errorToStreamResponse(errorStreamResponse.getErr()));
                } else {
                    responseChunk.putAll(sub.modelDump());
                }
                return responseChunk;
            }
        };
    }

    public static Iterator<Object> convertStreamSimpleResponse(Iterator<WorkflowAppStreamTypes.WorkflowAppStreamResponse> streamResponse) {
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return streamResponse.hasNext();
            }

            @Override
            public Object next() {
                if (!streamResponse.hasNext()) {
                    throw new NoSuchElementException();
                }

                WorkflowAppStreamTypes.WorkflowAppStreamResponse chunk = streamResponse.next();
                WorkflowAppStreamTypes.BaseStreamResponse sub = chunk.getStreamResponse();

                if (sub instanceof WorkflowAppStreamTypes.PingStreamResponse) {
                    return "ping";
                }

                Map<String, Object> responseChunk = new HashMap<>();
                responseChunk.put("event", sub.getEvent());
                responseChunk.put("workflow_run_id", chunk.getWorkflowRunId());

                if (sub instanceof WorkflowAppStreamTypes.ErrorStreamResponse errorStreamResponse) {
                    responseChunk.putAll(errorToStreamResponse(errorStreamResponse.getErr()));
                } else if (sub instanceof WorkflowAppStreamTypes.NodeStartStreamResponse
                        || sub instanceof WorkflowAppStreamTypes.NodeFinishStreamResponse) {
                    responseChunk.putAll(sub.toIgnoreDetailMap());
                } else {
                    responseChunk.putAll(sub.modelDump());
                }
                return responseChunk;
            }
        };
    }

    private static Map<String, Object> errorToStreamResponse(Throwable err) {
        Map<String, Object> error = new HashMap<>();
        error.put("message", err.getMessage());
        error.put("type", err.getClass().getSimpleName());
        return error;
    }
}

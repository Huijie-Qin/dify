package com.dify.core.app.apps.workflow;

/**
 * Java rewrite of api/core/app/apps/workflow/errors.py.
 */
public class WorkflowPausedInBlockingModeError extends RuntimeException {

    private static final String ERROR_CODE = "workflow_paused_in_blocking_mode";
    private static final int CODE = 400;

    public WorkflowPausedInBlockingModeError() {
        super("Workflow execution paused for human input; blocking response mode is not supported.");
    }

    public String getErrorCode() {
        return ERROR_CODE;
    }

    public int getCode() {
        return CODE;
    }
}

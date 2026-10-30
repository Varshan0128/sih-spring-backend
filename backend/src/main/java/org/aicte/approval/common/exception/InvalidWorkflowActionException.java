package org.aicte.approval.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidWorkflowActionException extends RuntimeException {
    public InvalidWorkflowActionException(String message) {
        super(message);
    }
}

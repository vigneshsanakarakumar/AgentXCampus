package com.agentx.campus.exception;

public class AccountPendingApprovalException extends RuntimeException {
    public AccountPendingApprovalException(String message) {
        super(message);
    }
}

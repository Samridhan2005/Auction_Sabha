package com.cts.mfrp.au.dto;

import com.cts.mfrp.au.model.VerifierApplication;

public class VerifierApprovalResponse {
    private VerifierApplication application;
    private String tempPassword;
    private boolean emailSent;

    public VerifierApprovalResponse(VerifierApplication application, String tempPassword, boolean emailSent) {
        this.application = application;
        this.tempPassword = tempPassword;
        this.emailSent = emailSent;
    }

    public VerifierApplication getApplication() { return application; }
    public String getTempPassword() { return tempPassword; }
    public boolean isEmailSent() { return emailSent; }
}

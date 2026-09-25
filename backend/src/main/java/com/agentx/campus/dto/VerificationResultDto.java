package com.agentx.campus.dto;

import java.util.ArrayList;
import java.util.List;

public class VerificationResultDto {
    private boolean verified;
    private String status; // VERIFIED, REJECTED, REQUIRES_HUMAN_APPROVAL
    private String message;
    private List<String> checksPerformed = new ArrayList<>();
    private String verifiedBy;

    public VerificationResultDto() {}

    public VerificationResultDto(boolean verified, String status, String message, String verifiedBy) {
        this.verified = verified;
        this.status = status;
        this.message = message;
        this.verifiedBy = verifiedBy;
    }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public List<String> getChecksPerformed() { return checksPerformed; }
    public void setChecksPerformed(List<String> checksPerformed) { this.checksPerformed = checksPerformed; }

    public String getVerifiedBy() { return verifiedBy; }
    public void setVerifiedBy(String verifiedBy) { this.verifiedBy = verifiedBy; }

    public void addCheck(String check) {
        this.checksPerformed.add(check);
    }
}

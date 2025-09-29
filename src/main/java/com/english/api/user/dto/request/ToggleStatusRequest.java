
package com.english.api.user.dto.request;

public class ToggleStatusRequest {
    private String lockReason;

    public String getLockReason() { return lockReason; }
    public void setLockReason(String lockReason) { this.lockReason = lockReason; }
}

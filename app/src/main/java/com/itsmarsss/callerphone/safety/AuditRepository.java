package com.itsmarsss.callerphone.safety;

public interface AuditRepository {
    void append(AuditEvent event);
}

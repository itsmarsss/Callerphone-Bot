package com.itsmarsss.callerphone.call.service;

import com.itsmarsss.callerphone.call.model.CallSession;

public record CallResult(Status status, CallSession session, int queuePosition, int queueSize, String message) {
    public enum Status {
        QUEUED,
        ALREADY_QUEUED,
        MATCHED,
        CONFLICT,
        FAILED
    }

    public static CallResult queued(int pos, int size) {
        return new CallResult(Status.QUEUED, null, pos, size, null);
    }

    public static CallResult alreadyQueued(int pos, int size) {
        return new CallResult(Status.ALREADY_QUEUED, null, pos, size, null);
    }

    public static CallResult matched(CallSession session) {
        return new CallResult(Status.MATCHED, session, 0, 0, null);
    }

    public static CallResult conflict() {
        return new CallResult(Status.CONFLICT, null, 0, 0, null);
    }

    public static CallResult failed(String message) {
        return new CallResult(Status.FAILED, null, 0, 0, message);
    }
}

package com.itsmarsss.callerphone.msginbottle;

public enum MIBStatus {

    ERROR,
    SENT,
    DELETED,
    RATE_LIMITED,
    NOT_FOUND,
    /** Bottle already has {@link com.itsmarsss.callerphone.Constants#MIB_MAX_PAGES} pages. */
    THREAD_FULL

}

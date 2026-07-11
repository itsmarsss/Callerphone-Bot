package com.itsmarsss.callerphone.identity;

import java.util.List;

public interface ConsentRepository {
    void append(ConsentEvent event);

    List<ConsentEvent> findByUser(String userId, int limit);
}

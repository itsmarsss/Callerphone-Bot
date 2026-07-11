package com.itsmarsss.callerphone.match.repository;

import com.itsmarsss.callerphone.match.model.MatchMessage;

import java.util.List;

public interface MatchMessageRepository {
    void save(MatchMessage message);

    List<MatchMessage> findByConversation(String conversationId, int limit);
}

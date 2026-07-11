package com.itsmarsss.callerphone.match.repository;

import com.itsmarsss.callerphone.match.model.MatchConversation;

import java.util.List;
import java.util.Optional;

public interface MatchConversationRepository {
    Optional<MatchConversation> findById(String conversationId);

    Optional<MatchConversation> findByMatchId(String matchId);

    void save(MatchConversation conversation);

    List<MatchConversation> findActiveByUserId(String userId);

    long countActiveByUserId(String userId);
}

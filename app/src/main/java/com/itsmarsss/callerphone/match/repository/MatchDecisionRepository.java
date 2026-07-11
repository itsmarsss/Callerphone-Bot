package com.itsmarsss.callerphone.match.repository;

import com.itsmarsss.callerphone.match.model.DecisionType;
import com.itsmarsss.callerphone.match.model.MatchDecision;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface MatchDecisionRepository {
    void upsert(MatchDecision decision);

    Optional<MatchDecision> find(String viewerId, String subjectId);

    boolean hasActiveDecision(String viewerId, String subjectId);

    Set<String> findSubjectIdsForViewer(String viewerId);

    List<MatchDecision> findIncomingInterested(String subjectId, int limit);

    Optional<MatchDecision> findReciprocalInterest(String firstUserId, String secondUserId);
}

package com.itsmarsss.callerphone.match.repository;

import com.itsmarsss.callerphone.match.model.Match;

import java.util.List;
import java.util.Optional;

public interface MatchRepository {
    Optional<Match> findById(String matchId);

    Optional<Match> findByPairKey(String pairKey);

    Optional<Match> createIfAbsent(Match match);

    void save(Match match);

    List<Match> findActiveByUserId(String userId);
}

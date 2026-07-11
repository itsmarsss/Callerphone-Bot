package com.itsmarsss.callerphone.identity;

import java.util.List;
import java.util.Optional;

public interface MatchUserRepository {
    Optional<MatchUser> findById(String userId);

    void save(MatchUser user);

    default List<MatchUser> findDigestOptIn(int limit) {
        return List.of();
    }
}

package com.itsmarsss.callerphone.match.repository;

import com.itsmarsss.callerphone.identity.AgeCohort;
import com.itsmarsss.callerphone.match.model.MatchProfile;
import com.itsmarsss.callerphone.match.model.ProfileState;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface MatchProfileRepository {
    Optional<MatchProfile> findByUserId(String userId);

    void save(MatchProfile profile);

    List<MatchProfile> findCandidates(CandidateQuery query);

    List<MatchProfile> findByState(ProfileState state, int limit);

    record CandidateQuery(
            String viewerId,
            AgeCohort ageCohort,
            Set<String> excludeUserIds,
            int limit
    ) {
    }
}

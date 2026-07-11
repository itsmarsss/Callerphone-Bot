package com.itsmarsss.callerphone.safety;

import java.util.List;

public interface SanctionRepository {
    void save(Sanction sanction);

    boolean hasActiveSanction(String userId, String product);

    List<Sanction> findActive(String userId);
}

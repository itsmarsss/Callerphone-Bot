package com.itsmarsss.callerphone.safety;

public interface BlockRepository {
    boolean existsEitherDirection(String firstUserId, String secondUserId, String product);

    void create(Block block);
}

package com.itsmarsss.callerphone.experience;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ControlMessageStoreTest {

    @Test
    void storesAndRemovesLobbyKeys() {
        ControlMessageStore store = ControlMessageStore.get();
        String key = ControlMessageStore.callLobbyKey("chan-1");
        store.remove(key);
        assertTrue(store.get(key).isEmpty());
        store.put(key, "msg-99");
        assertEquals("msg-99", store.get(key).orElseThrow());
        store.remove(key);
        assertTrue(store.get(key).isEmpty());
    }

    @Test
    void ignoresBlankKeys() {
        ControlMessageStore store = ControlMessageStore.get();
        store.put(" ", "x");
        store.put(null, "x");
        store.put("k", "");
        assertTrue(store.get(" ").isEmpty());
    }

    @Test
    void keyHelpersAreStable() {
        assertEquals("call:lobby:c1", ControlMessageStore.callLobbyKey("c1"));
        assertEquals("match:discover:u1", ControlMessageStore.discoverKey("u1"));
        assertEquals("match:onboard:u1", ControlMessageStore.onboardingKey("u1"));
    }
}

package com.itsmarsss.callerphone.msginbottle;

import com.itsmarsss.callerphone.msginbottle.entities.Bottle;
import com.itsmarsss.callerphone.msginbottle.entities.Page;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class MessageInBottleIdentityTest {

    @Test
    void identityLockReturnsNullForNewAuthor() {
        Bottle bottle = bottleWithPage("user-a", true);
        assertNull(MessageInBottle.identityLockForAuthor(bottle, "user-b"));
    }

    @Test
    void identityLockMatchesFirstPageForAuthor() {
        ArrayList<Page> pages = new ArrayList<>();
        pages.add(new Page(0, "user-a", "hello", false, 1L));
        pages.add(new Page(1, "user-b", "hi", true, 2L));
        pages.add(new Page(2, "user-a", "again", true, 3L)); // later page differs; lock uses first
        Bottle bottle = new Bottle("b1", pages);
        assertEquals(Boolean.FALSE, MessageInBottle.identityLockForAuthor(bottle, "user-a"));
        assertEquals(Boolean.TRUE, MessageInBottle.identityLockForAuthor(bottle, "user-b"));
    }

    @Test
    void isFullRespectsMaxPages() {
        ArrayList<Page> pages = new ArrayList<>();
        for (int i = 0; i < com.itsmarsss.callerphone.Constants.MIB_MAX_PAGES; i++) {
            pages.add(new Page(i, "u", "m" + i, false, i));
        }
        assertTrue(MessageInBottle.isFull(new Bottle("full", pages)));
        pages.remove(pages.size() - 1);
        assertFalse(MessageInBottle.isFull(new Bottle("open", pages)));
    }

    private static Bottle bottleWithPage(String author, boolean signed) {
        ArrayList<Page> pages = new ArrayList<>();
        pages.add(new Page(0, author, "msg", signed, 1L));
        return new Bottle("b", pages);
    }
}

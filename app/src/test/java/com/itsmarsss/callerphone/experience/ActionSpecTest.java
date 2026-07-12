package com.itsmarsss.callerphone.experience;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ActionSpecTest {

    @Test
    void linkUsesUrlAsComponentId() {
        ActionSpec link = ActionSpec.link("https://example.com/invite", "Invite");
        assertEquals(ActionSpec.Style.LINK, link.style());
        assertEquals("https://example.com/invite", link.componentId());
        assertEquals("Invite", link.label());
    }

    @Test
    void asDisabledPreservesStyle() {
        ActionSpec disabled = ActionSpec.primary("m-v1-x-_", "Go").asDisabled();
        assertTrue(disabled.disabled());
        assertEquals(ActionSpec.Style.PRIMARY, disabled.style());
    }
}

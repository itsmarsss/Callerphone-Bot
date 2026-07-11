package com.itsmarsss.callerphone.safety;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportCategoryTest {
    @Test
    void urgentCategories() {
        assertTrue(ReportCategory.GROOMING.urgent());
        assertTrue(ReportCategory.AGE_MISREPRESENTATION.urgent());
        assertFalse(ReportCategory.SPAM.urgent());
        assertTrue(ReportCategory.from("age_lie").isPresent());
    }
}

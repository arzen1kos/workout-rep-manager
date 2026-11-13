package app.reppin.core.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ExerciseTest {

    @Test
    void cleansTheName() {
        Exercise e = new Exercise("x1", "  Russian   twist ", 100);
        assertEquals("Russian twist", e.name());
        assertThrows(IllegalArgumentException.class, () -> new Exercise("x2", "  ", 100));
    }

    @Test
    void urlMustBeHttpOrHttpsAndIsOptional() {
        Exercise e = new Exercise("x1", "Plank", 100);
        e.setInstructionUrl("https://example.com/plank", 200);
        assertEquals("https://example.com/plank", e.instructionUrl());
        e.setInstructionUrl("   ", 300);
        assertNull(e.instructionUrl());
        assertThrows(IllegalArgumentException.class, () -> e.setInstructionUrl("javascript:alert(1)", 400));
        assertThrows(IllegalArgumentException.class, () -> e.setInstructionUrl("ftp://example.com", 400));
        assertThrows(IllegalArgumentException.class, () -> e.setInstructionUrl("https://", 400));
        assertThrows(IllegalArgumentException.class, () -> e.setInstructionUrl("https://a b.com", 400));
    }

    @Test
    void archiveInsteadOfDelete() {
        Exercise e = new Exercise("x1", "Plank", 100);
        e.archive(500);
        assertTrue(e.isArchived());
        assertEquals(500, e.archivedAtMs());
        assertEquals(500, e.updatedAtMs());
        e.unarchive(600);
        assertFalse(e.isArchived());
    }

    @Test
    void duplicateCopiesFieldsWithNewIdAndCopyName() {
        Exercise e = new Exercise("x1", "Plank", 100);
        e.setDescription("Hold a straight line", 110);
        e.addTag("tag-core", 120);
        e.setLastUsedSpec(WorkSpec.duration(60_000), 130);
        e.archive(140);
        Exercise copy = e.duplicate("x2", 200);
        assertEquals("x2", copy.id());
        assertEquals("Plank (copy)", copy.name());
        assertEquals("Hold a straight line", copy.description());
        assertTrue(copy.tagIds().contains("tag-core"));
        assertEquals(WorkSpec.duration(60_000), copy.lastUsedSpec());
        assertFalse(copy.isArchived());
        assertEquals(200, copy.createdAtMs());
    }

    @Test
    void duplicateKeepsTheNameWithinTheLimit() {
        Exercise e = new Exercise("x1", "n".repeat(Names.MAX_NAME), 100);
        assertEquals(Names.MAX_NAME, e.duplicate("x2", 200).name().length());
    }
}

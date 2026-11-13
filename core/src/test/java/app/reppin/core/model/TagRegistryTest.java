package app.reppin.core.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TagRegistryTest {

    @Test
    void namesAreUniquePerScopeIgnoringCaseAndSpacing() {
        TagRegistry tags = new TagRegistry();
        tags.create("t1", "Core", TagScope.EXERCISE);
        assertThrows(TagNameTakenException.class, () -> tags.create("t2", "  core  ", TagScope.EXERCISE));
        assertThrows(TagNameTakenException.class, () -> tags.create("t3", "CORE", TagScope.EXERCISE));
    }

    @Test
    void sameNameInAnotherScopeIsADifferentTag() {
        TagRegistry tags = new TagRegistry();
        Tag exercise = tags.create("t1", "Core", TagScope.EXERCISE);
        Tag routine = tags.create("t2", "Core", TagScope.ROUTINE);
        assertTrue(exercise != routine);
        assertEquals("t1", tags.find("core", TagScope.EXERCISE).id());
        assertEquals("t2", tags.find("core", TagScope.ROUTINE).id());
    }

    @Test
    void renameChecksEveryScopeOfTheTag() {
        TagRegistry tags = new TagRegistry();
        tags.create("t1", "Push", TagScope.EXERCISE);
        tags.create("t2", "Pull", TagScope.EXERCISE);
        assertThrows(TagNameTakenException.class, () -> tags.rename("t2", "push"));
        tags.rename("t2", "Pull day"); // free name works
        assertEquals("Pull day", tags.get("t2").displayName());
    }

    @Test
    void linkMergesIntoOneSharedTag() {
        TagRegistry tags = new TagRegistry();
        tags.create("e1", "Core", TagScope.EXERCISE);
        tags.create("r1", "Core", TagScope.ROUTINE);
        TagRegistry.LinkResult result = tags.link("e1", "r1", true);
        assertEquals("e1", result.survivorId());
        assertEquals("r1", result.removedId());
        assertNull(tags.get("r1"));
        Tag shared = tags.get("e1");
        assertTrue(shared.hasScope(TagScope.EXERCISE) && shared.hasScope(TagScope.ROUTINE));
        assertEquals(shared, tags.find("core", TagScope.ROUTINE));
    }

    @Test
    void linkCanAdoptTheRoutineTagName() {
        TagRegistry tags = new TagRegistry();
        tags.create("e1", "Abs", TagScope.EXERCISE);
        tags.create("r1", "Core", TagScope.ROUTINE);
        tags.link("e1", "r1", false);
        assertEquals("Core", tags.get("e1").displayName());
    }

    @Test
    void linkRefusesWhenTheNameCollidesElsewhere() {
        TagRegistry tags = new TagRegistry();
        tags.create("e1", "Core", TagScope.EXERCISE);
        tags.create("r1", "Abs", TagScope.ROUTINE);
        tags.create("r2", "Core", TagScope.ROUTINE); // another routine tag already uses "Core"
        assertThrows(TagNameTakenException.class, () -> tags.link("e1", "r1", true));
        assertNotNull(tags.get("r1")); // nothing was changed
    }

    @Test
    void unlinkSplitsAgain() {
        TagRegistry tags = new TagRegistry();
        tags.create("e1", "Core", TagScope.EXERCISE);
        tags.create("r1", "Core", TagScope.ROUTINE);
        tags.link("e1", "r1", true);
        Tag routineSide = tags.unlink("e1", "r2");
        assertEquals("r2", routineSide.id());
        assertTrue(routineSide.hasScope(TagScope.ROUTINE) && !routineSide.hasScope(TagScope.EXERCISE));
        assertTrue(!tags.get("e1").hasScope(TagScope.ROUTINE));
    }

    @Test
    void rejectsBlankAndOverlongNames() {
        TagRegistry tags = new TagRegistry();
        assertThrows(IllegalArgumentException.class, () -> tags.create("t1", "   ", TagScope.EXERCISE));
        assertThrows(IllegalArgumentException.class,
                () -> tags.create("t2", "x".repeat(Names.MAX_TAG + 1), TagScope.EXERCISE));
    }
}

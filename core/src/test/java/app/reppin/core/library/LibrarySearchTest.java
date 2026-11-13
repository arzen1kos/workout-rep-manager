package app.reppin.core.library;

import static org.junit.jupiter.api.Assertions.assertEquals;

import app.reppin.core.model.Exercise;
import app.reppin.core.model.TagRegistry;
import app.reppin.core.model.TagScope;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LibrarySearchTest {

    private TagRegistry tags;
    private List<Exercise> all;

    @BeforeEach
    void setUp() {
        tags = new TagRegistry();
        tags.create("core", "Core", TagScope.EXERCISE);
        tags.create("shoulder", "Shoulder", TagScope.EXERCISE);

        Exercise twist = new Exercise("1", "Russian twist", 1);
        twist.addTag("core", 1);
        Exercise plank = new Exercise("2", "Plank", 1);
        plank.setDescription("Hold a straight line", 1);
        plank.addTag("core", 1);
        plank.addTag("shoulder", 1);
        Exercise press = new Exercise("3", "Overhead press", 1);
        press.addTag("shoulder", 1);
        Exercise oldMove = new Exercise("4", "Old move", 1);
        oldMove.addTag("core", 1);
        oldMove.archive(2);

        all = new ArrayList<>();
        all.add(twist);
        all.add(plank);
        all.add(press);
        all.add(oldMove);
    }

    private static List<String> ids(List<Exercise> list) {
        List<String> ids = new ArrayList<>();
        for (Exercise e : list) {
            ids.add(e.id());
        }
        return ids;
    }

    @Test
    void noFilterReturnsActiveCardsSortedByName() {
        assertEquals(List.of("3", "2", "1"), ids(LibrarySearch.search(all, tags, new LibraryQuery())));
    }

    @Test
    void archivedCardsAppearOnlyWhenAsked() {
        List<Exercise> result = LibrarySearch.search(all, tags, new LibraryQuery().includeArchived(true));
        assertEquals(4, result.size());
    }

    @Test
    void textMatchesNameDescriptionAndTagNames() {
        assertEquals(List.of("1"), ids(LibrarySearch.search(all, tags, new LibraryQuery().text("RUSSIAN"))));
        assertEquals(List.of("2"), ids(LibrarySearch.search(all, tags, new LibraryQuery().text("straight"))));
        assertEquals(List.of("3", "2"), ids(LibrarySearch.search(all, tags, new LibraryQuery().text("shoulder"))));
    }

    @Test
    void everyWordMustMatch() {
        assertEquals(List.of("2"), ids(LibrarySearch.search(all, tags, new LibraryQuery().text("plank  core"))));
        assertEquals(List.of(), ids(LibrarySearch.search(all, tags, new LibraryQuery().text("plank twist"))));
    }

    @Test
    void tagFilterIsAndByDefaultAndOrOnRequest() {
        LibraryQuery both = new LibraryQuery().requireTag("core").requireTag("shoulder");
        assertEquals(List.of("2"), ids(LibrarySearch.search(all, tags, both)));
        LibraryQuery either = new LibraryQuery().requireTag("core").requireTag("shoulder").matchAnyTag(true);
        assertEquals(List.of("3", "2", "1"), ids(LibrarySearch.search(all, tags, either)));
    }
}

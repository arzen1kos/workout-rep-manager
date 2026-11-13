package app.reppin.core.library;

import app.reppin.core.model.Exercise;
import app.reppin.core.model.Names;
import app.reppin.core.model.Tag;
import app.reppin.core.model.TagRegistry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Pure search over exercise cards: every whitespace-separated word of the text query must appear
 * in the name, description or one of the tag names (case-insensitive). Results are ordered by
 * name, then id, so the UI list is stable.
 */
public final class LibrarySearch {

    private LibrarySearch() {
    }

    public static List<Exercise> search(Collection<Exercise> all, TagRegistry tags, LibraryQuery query) {
        String[] words = splitWords(query.text());
        List<Exercise> result = new ArrayList<>();
        for (Exercise exercise : all) {
            if (exercise.isArchived() && !query.includeArchived()) {
                continue;
            }
            if (!matchesTags(exercise, query)) {
                continue;
            }
            if (words.length > 0 && !matchesText(exercise, tags, words)) {
                continue;
            }
            result.add(exercise);
        }
        Collections.sort(result, new Comparator<Exercise>() {
            @Override
            public int compare(Exercise a, Exercise b) {
                int byName = Names.normalize(a.name()).compareTo(Names.normalize(b.name()));
                return byName != 0 ? byName : a.id().compareTo(b.id());
            }
        });
        return result;
    }

    private static boolean matchesTags(Exercise exercise, LibraryQuery query) {
        if (query.tagIds().isEmpty()) {
            return true;
        }
        if (query.matchAnyTag()) {
            for (String id : query.tagIds()) {
                if (exercise.tagIds().contains(id)) {
                    return true;
                }
            }
            return false;
        }
        return exercise.tagIds().containsAll(query.tagIds());
    }

    private static boolean matchesText(Exercise exercise, TagRegistry tags, String[] words) {
        StringBuilder haystack = new StringBuilder(exercise.name());
        if (exercise.description() != null) {
            haystack.append(' ').append(exercise.description());
        }
        for (String tagId : exercise.tagIds()) {
            Tag tag = tags.get(tagId);
            if (tag != null) {
                haystack.append(' ').append(tag.displayName());
            }
        }
        String text = Names.normalize(haystack.toString());
        for (String word : words) {
            if (!text.contains(word)) {
                return false;
            }
        }
        return true;
    }

    private static String[] splitWords(String raw) {
        List<String> words = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (Character.isWhitespace(c)) {
                if (current.length() > 0) {
                    words.add(Names.normalize(current.toString()));
                    current.setLength(0);
                }
            } else {
                current.append(c);
            }
        }
        if (current.length() > 0) {
            words.add(Names.normalize(current.toString()));
        }
        return words.toArray(new String[0]);
    }
}

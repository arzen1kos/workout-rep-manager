package app.reppin.core.model;

import java.util.Locale;

/**
 * Input cleaning and normalisation shared by the model. TeaVM-safe: no regex, no java.net.
 *
 * <p>TODO(M1 spike): add Unicode normalisation (NFKC) once TeaVM support is verified; until then
 * names are compared after trim, whitespace collapse and lower-casing only.
 */
public final class Names {

    public static final int MAX_NAME = 80;
    public static final int MAX_TAG = 40;
    public static final int MAX_DESCRIPTION = 2_000;
    public static final int MAX_URL = 2_048;

    private Names() {
    }

    /** Trims and collapses whitespace runs to one space. Rejects null, empty and over-long values. */
    public static String clean(String raw, int maxLength, String what) {
        if (raw == null) {
            throw new IllegalArgumentException(what + " is required");
        }
        StringBuilder sb = new StringBuilder(raw.length());
        boolean pendingSpace = false;
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (Character.isWhitespace(c)) {
                pendingSpace = sb.length() > 0;
            } else {
                if (pendingSpace) {
                    sb.append(' ');
                    pendingSpace = false;
                }
                sb.append(c);
            }
        }
        if (sb.length() == 0) {
            throw new IllegalArgumentException(what + " must not be empty");
        }
        if (sb.length() > maxLength) {
            throw new IllegalArgumentException(what + " must be at most " + maxLength + " characters");
        }
        return sb.toString();
    }

    /** Key used for uniqueness checks and search. */
    public static String normalize(String cleaned) {
        return cleaned.toLowerCase(Locale.ROOT);
    }

    /** Optional free text: blank becomes null, otherwise trimmed and length-limited. */
    public static String optionalText(String raw, int maxLength, String what) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.length() > maxLength) {
            throw new IllegalArgumentException(what + " must be at most " + maxLength + " characters");
        }
        return trimmed;
    }

    /** Optional http(s) URL (SPEC LIB-1). Blank becomes null. The app never fetches it. */
    public static String optionalUrl(String raw) {
        String url = optionalText(raw, MAX_URL, "URL");
        if (url == null) {
            return null;
        }
        int schemeLength;
        if (url.regionMatches(true, 0, "https://", 0, 8)) {
            schemeLength = 8;
        } else if (url.regionMatches(true, 0, "http://", 0, 7)) {
            schemeLength = 7;
        } else {
            throw new IllegalArgumentException("URL must start with http:// or https://");
        }
        if (url.length() == schemeLength) {
            throw new IllegalArgumentException("URL has no host");
        }
        for (int i = 0; i < url.length(); i++) {
            if (Character.isWhitespace(url.charAt(i))) {
                throw new IllegalArgumentException("URL must not contain spaces");
            }
        }
        return url;
    }
}

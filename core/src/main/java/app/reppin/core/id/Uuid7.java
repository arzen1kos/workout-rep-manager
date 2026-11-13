package app.reppin.core.id;

/**
 * UUIDv7 (RFC 9562): 48-bit Unix-ms timestamp, version 7, 74 random bits. Ids sort by creation
 * time as plain strings, which keeps IndexedDB keys roughly chronological.
 *
 * <p>Ids created in the same millisecond are unique (random) but not ordered among themselves.
 * Written with long arithmetic only, so it is TeaVM-safe.
 */
public final class Uuid7 {

    private static final char[] HEX = "0123456789abcdef".toCharArray();

    private Uuid7() {
    }

    public static String generate(long unixMs, RandomSource random) {
        if (unixMs < 0 || unixMs >= (1L << 48)) {
            throw new IllegalArgumentException("timestamp must fit in 48 bits");
        }
        long randA = random.nextLong();
        long randB = random.nextLong();
        long msb = (unixMs << 16) | 0x7000L | (randA & 0x0FFFL);
        long lsb = (randB & 0x3FFFFFFFFFFFFFFFL) | 0x8000000000000000L;

        StringBuilder sb = new StringBuilder(36);
        appendHex(sb, msb >>> 32, 8);
        sb.append('-');
        appendHex(sb, msb >>> 16, 4);
        sb.append('-');
        appendHex(sb, msb, 4);
        sb.append('-');
        appendHex(sb, lsb >>> 48, 4);
        sb.append('-');
        appendHex(sb, lsb, 12);
        return sb.toString();
    }

    public static boolean isValid(String id) {
        if (id == null || id.length() != 36) {
            return false;
        }
        for (int i = 0; i < 36; i++) {
            char c = id.charAt(i);
            if (i == 8 || i == 13 || i == 18 || i == 23) {
                if (c != '-') {
                    return false;
                }
            } else if (hexValue(c) < 0) {
                return false;
            }
        }
        char variant = id.charAt(19);
        return id.charAt(14) == '7' && (variant == '8' || variant == '9' || variant == 'a' || variant == 'b');
    }

    /** Extracts the embedded Unix-ms timestamp. The id must be valid. */
    public static long timestampMs(String id) {
        if (!isValid(id)) {
            throw new IllegalArgumentException("not a UUIDv7: " + id);
        }
        long value = 0;
        for (int i = 0; i < 13; i++) {
            if (i == 8) {
                continue; // skip the hyphen
            }
            value = (value << 4) | hexValue(id.charAt(i));
        }
        return value;
    }

    private static void appendHex(StringBuilder sb, long value, int digits) {
        for (int i = digits - 1; i >= 0; i--) {
            sb.append(HEX[(int) ((value >>> (4 * i)) & 0xF)]);
        }
    }

    private static int hexValue(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        if (c >= 'a' && c <= 'f') {
            return c - 'a' + 10;
        }
        return -1;
    }
}

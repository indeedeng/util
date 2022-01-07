package com.indeed.util.core;

import javax.annotation.Nullable;
import java.util.Arrays;

/**
 * Represents a multi-part version number internally as a 64-bit integer for fast comparison. Can
 * represent a numeric version number with up to four components:
 * majorVersion.minorVersion.patchVersion.buildNumber.
 *
 * <ul>
 *   <li>majorVersion can be a positive integer between 0 and 32767 (inclusive)
 *   <li>minorVersion can be a positive integer between 0 and 65535 (inclusive)
 *   <li>patchVersion can be a positive integer between 0 and 65535 (inclusive)
 *   <li>buildNumber can be a positive integer between 0 and 65535 (inclusive)
 * </ul>
 *
 * <p>Supports comparison using wild-card expressions, using the special character 'x'. The wild
 * card character can only be using at the end of a version string. <code>
 *     assert ReleaseVersion.fromString("1.0").compareTo(ReleaseVersion.fromString("1.1.x") == 1;
 *     assert ReleaseVersion.fromString("1.0.x").compareTo(ReleaseVersion.fromString("1.0.0.1") == 0;
 *     assert ReleaseVersion.fromString("1.0.0.1").compareTo(ReleaseVersion.fromString("1.0.x") == 0;
 *     assert ReleaseVersion.fromString("1.0.x").compareTo(ReleaseVersion.fromString("1.0.0.x") == 0;
 * </code>
 *
 * <p>Supports interpreting a version string. The version must be fully qualified (e.g. "1.0.0.0")
 * or end with a wild card ("x") to be parsed correctly.
 *
 * <p>When using the Builder, you can use the wild card behavior by calling setMatchPrecision with
 * the lowest level to use in comparison. Default match matchPrecision is BUILD. <code>
 *     ReleaseVersion lhs = newBuilder().setMajorVersion(1).build(); // equivalent to fromString("1.0.0.0")
 *     ReleaseVersion rhs = newBuilder().setMajorVersion(1).setMinorVersion(1).build(); // equivalent to fromString("1.1.0.0")
 *     assert lhs.compareTo(rhs) == -1;
 *     lhs = newBuilder().setMajorVersion(1).setMatchPrecision(MAJOR).build(); // equivalent to fromString("1.x")
 *     assert lhs.compareTo(rhs) == 0;
 * </code>
 *
 * @author jack@indeed.com (Jack Humphrey)
 */
public class ReleaseVersion implements Comparable<ReleaseVersion> {

    private static final char WILDCARD = 'x';

    private static boolean isWildcard(@Nullable String versionComponent) {
        return versionComponent != null
                && versionComponent.length() == 1
                && WILDCARD == versionComponent.charAt(0);
    }

    private final long version;
    private final MatchPrecision matchPrecision;

    public enum MatchPrecision {
        MAJOR(1, 0x7FFF000000000000L),
        MINOR(2, 0x7FFFFFFF00000000L),
        PATCH(3, 0x7FFFFFFFFFFF0000L),
        BUILD(4, 0x7FFFFFFFFFFFFFFFL);

        int length;
        long mask;

        MatchPrecision(int length, long mask) {
            this.length = length;
            this.mask = mask;
        }

        static MatchPrecision forLength(int length) {
            switch (length) {
                case 0:
                case 1:
                    return MAJOR;
                case 2:
                    return MINOR;
                case 3:
                    return PATCH;
                case 4:
                    return BUILD;
            }
            return null;
        }
    }

    private ReleaseVersion(
            short majorVersion,
            int minorVersion,
            int patchVersion,
            int buildNumber,
            MatchPrecision matchPrecision) {
        version =
                ((long) majorVersion << 48)
                        | ((long) minorVersion << 32)
                        | ((long) patchVersion << 16)
                        | (long) buildNumber;
        this.matchPrecision = matchPrecision;
    }

    public MatchPrecision getMatchPrecision() {
        return matchPrecision;
    }

    @Override
    public String toString() {
        final short majorVersion = (short) ((version >> 48) & 0x7FFF);
        final int minorVersion = (int) (version >> 32) & 0xFFFF;
        final int patchVersion = (int) (version >> 16) & 0xFFFF;
        final int buildNumber = (int) (version & 0xFFFF);
        final StringBuilder b = new StringBuilder();
        b.append(Short.toString(majorVersion)).append('.');
        if (matchPrecision == MatchPrecision.MAJOR) {
            b.append(WILDCARD);
        } else {
            b.append(Integer.toString(minorVersion)).append('.');
            if (matchPrecision == MatchPrecision.MINOR) {
                b.append(WILDCARD);
            } else {
                b.append(Integer.toString(patchVersion)).append('.');
                if (matchPrecision == MatchPrecision.PATCH) {
                    b.append(WILDCARD);
                } else {
                    b.append(Integer.toString(buildNumber));
                }
            }
        }
        return b.toString();
    }

    @Override
    public int compareTo(ReleaseVersion other) {
        final MatchPrecision minPrecision =
                MatchPrecision.forLength(
                        Math.min(matchPrecision.length, other.matchPrecision.length));

        final long lhs = version & minPrecision.mask;
        final long rhs = other.version & minPrecision.mask;
        return (lhs < rhs ? -1 : (lhs == rhs ? 0 : 1));
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof ReleaseVersion) {
            return compareTo((ReleaseVersion) o) == 0;
        }
        return false;
    }

    /**
     * Turn a version string into an object. Enforces strict interpretation: string must either be
     * fully qualified (x.x.x.x) or use a wildcard (like 3.2.x) or an IllegalArgumentException will
     * be thrown. All numbers must parse as integers or a NumberFormatException will be thrown. This
     * method is typically for use in code when dealing with explicit string literals.
     *
     * @param versionString fully-qualified or wild-card version string
     * @return The parsed version of the provided {@code versionString}
     * @throws IllegalArgumentException if version string is not fully-qualified or a wild card, or
     *     if a version number does not parse as an integer
     */
    public static ReleaseVersion fromString(String versionString) {
        return new Builder().fromString(versionString, false).build();
    }

    /**
     * Turn a version string into an object. Does lenient parsing: "1.2.rc1" will turn into
     * "1.2.0.0", for example. Will not throw {@link IllegalArgumentException}.
     *
     * @param versionString The version string that we should attempt to parse into a release
     *     version.
     * @param defaultVersion The default version to use in the event parsing of versionString fails.
     * @return The parsed ReleaseVersion or the defaultVersion if parsing failed.
     */
    public static ReleaseVersion fromStringSafely(
            String versionString, ReleaseVersion defaultVersion) {
        try {
            return new Builder().fromString(versionString, true).build();
        } catch (Exception e) {
            return defaultVersion;
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final int MAX_MAJOR_VERSION = Short.MAX_VALUE;
    public static final int MAX_MINOR_VERSION = (Short.MAX_VALUE << 1) + 1;
    public static final int MAX_PATCH_VERSION = (Short.MAX_VALUE << 1) + 1;
    public static final int MAX_BUILD_NUMBER = (Short.MAX_VALUE << 1) + 1;

    public static final class Builder {
        private short majorVersion;
        private int minorVersion;
        private int patchVersion;
        private int buildNumber;
        private MatchPrecision matchPrecision = MatchPrecision.BUILD;

        public Builder setMatchPrecision(MatchPrecision matchPrecision) {
            this.matchPrecision = matchPrecision;
            return Builder.this;
        }

        public Builder setMajorVersion(int majorVersion) {
            if (majorVersion > MAX_MAJOR_VERSION || majorVersion < 0) {
                throw new IllegalArgumentException("invalid major version " + majorVersion);
            }
            this.majorVersion = (short) majorVersion;
            return Builder.this;
        }

        public Builder setMinorVersion(int minorVersion) {
            if (minorVersion > MAX_MINOR_VERSION || minorVersion < 0) {
                throw new IllegalArgumentException("invalid minor version " + minorVersion);
            }
            this.minorVersion = minorVersion;
            return Builder.this;
        }

        public Builder setPatchVersion(int patchVersion) {
            if (patchVersion > MAX_PATCH_VERSION || patchVersion < 0) {
                throw new IllegalArgumentException("invalid patch version " + patchVersion);
            }
            this.patchVersion = patchVersion;
            return Builder.this;
        }

        public Builder setBuildNumber(int buildNumber) {
            if (buildNumber > MAX_BUILD_NUMBER || buildNumber < 0) {
                throw new IllegalArgumentException("invalid build number " + buildNumber);
            }
            this.buildNumber = buildNumber;
            return Builder.this;
        }

        /**
         * @param versionString The string representation of a release version.
         * @param lenient if false, will throw exceptions if not proper version string.
         * @return The builder class so we can update components of the version before building.
         * @throws IllegalArgumentException if version string is invalid and !lenient (if it can't
         *     parse a major version, will throw this exception even if lenient.
         */
        public Builder fromString(String versionString, boolean lenient) {
            String[] parts = versionString.split("\\.");
            if (parts.length > 0 && isWildcard(parts[parts.length - 1])) {
                parts = Arrays.copyOfRange(parts, 0, parts.length - 1);
                if (!lenient && parts.length == 0) {
                    throw new IllegalArgumentException("Invalid version string: " + versionString);
                }
            } else if (!lenient && parts.length < 4) {
                // Will allow parts.length > 4 and discard the extra parts
                throw new IllegalArgumentException(
                        "ReleaseVersion string must have 4 numbers or end in "
                                + WILDCARD
                                + ": "
                                + versionString);
            }

            // this part can't be lenient
            setMajorVersion(parts.length > 0 ? Integer.parseInt(parts[0]) : 0);

            try {
                setMinorVersion(parts.length > 1 ? Integer.parseInt(parts[1]) : 0);
                setPatchVersion(parts.length > 2 ? Integer.parseInt(parts[2]) : 0);
                setBuildNumber(parts.length > 3 ? Integer.parseInt(parts[3]) : 0);
            } catch (IllegalArgumentException e) {
                if (!lenient) {
                    throw e;
                }
            }

            if (lenient) {
                // when lenient, we assume full precision
                setMatchPrecision(MatchPrecision.BUILD);
            } else {
                setMatchPrecision(MatchPrecision.forLength(Math.min(parts.length, 4)));
            }
            return Builder.this;
        }

        public ReleaseVersion build() {
            return new ReleaseVersion(
                    majorVersion, minorVersion, patchVersion, buildNumber, matchPrecision);
        }
    }
}

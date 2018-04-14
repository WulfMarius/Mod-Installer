package me.wulfmarius.modinstaller;

import java.util.function.*;

public class VersionRequirement {

    private Predicate<Version> predicate;

    public static VersionRequirement parse(String version) {
        VersionRequirement result = new VersionRequirement();

        if (version.startsWith("^")) {
            Version parsedVersion = Version.parse(version.substring(1));
            result.predicate = VersionComparison.min(parsedVersion).and(VersionComparison.max(parsedVersion.nextMajor()));
        } else {
            Version parsedVersion = Version.parse(version.substring(0));
            result.predicate = VersionComparison.equals(parsedVersion);
        }

        return result;
    }

    public boolean isSatisfiedBy(Version version) {
        return this.predicate.test(version);
    }

    public static class VersionComparison implements Predicate<Version> {

        private final Version expectedVersion;
        private final BiFunction<Version, Version, Boolean> test;

        private VersionComparison(Version expectedVersion, BiFunction<Version, Version, Boolean> test) {
            super();
            this.expectedVersion = expectedVersion;
            this.test = test;
        }

        public static VersionComparison equals(Version expectedVersion) {
            return new VersionComparison(expectedVersion, (o1, o2) -> o1.compareTo(o2) == 0);
        }

        public static VersionComparison max(Version expectedVersion) {
            return new VersionComparison(expectedVersion, (o1, o2) -> o1.compareTo(o2) >= 0);
        }

        public static VersionComparison min(Version expectedVersion) {
            return new VersionComparison(expectedVersion, (o1, o2) -> o1.compareTo(o2) <= 0);
        }

        @Override
        public boolean test(Version version) {
            return this.test.apply(this.expectedVersion, version);
        }
    }
}

package me.wulfmarius.modinstaller;

import static me.wulfmarius.modinstaller.utils.StringUtils.trimToEmpty;

import java.util.Comparator;
import java.util.regex.*;

import org.springframework.util.StringUtils;

public class Version implements Comparable<Version> {

    public static final String VERSION_UNKNOWN = "UNKNOWN";

    // an extension to the semver.org pattern to accomodate UModTld
    private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+)(?:\\.(\\d+)(?:\\.(\\d+))?)?(\\w+)?(?:-(\\w+))?");

    private static final Comparator<Version> COMPARATOR = Comparator.comparingInt(Version::getMajor)
            .thenComparingInt(Version::getMinor).thenComparingInt(Version::getPatch)
            .thenComparing(Version::getSpecial, Version::compareSpecial)
            .thenComparing(Version::getPrelease, Version::comparePrerelease);

    private int major;
    private int minor;
    private int patch;
    private String prelease;

    private String special;

    public static int compare(Version v1, Version v2) {
        return COMPARATOR.compare(v1, v2);
    }

    public static Version parse(String version) {
        if (version.startsWith("v") || version.startsWith("V")) {
            return parse(version.substring(1));
        }

        Matcher matcher = VERSION_PATTERN.matcher(version);
        if (matcher.matches()) {
            Version result = new Version();
            result.major = Integer.parseInt(matcher.group(1));

            if (!StringUtils.isEmpty(matcher.group(2))) {
                result.minor = Integer.parseInt(matcher.group(2));
            }

            if (!StringUtils.isEmpty(matcher.group(3))) {
                result.patch = Integer.parseInt(matcher.group(3));
            }

            result.special = trimToEmpty(matcher.group(4));
            result.prelease = trimToEmpty(matcher.group(5));
            return result;
        }

        throw new IllegalArgumentException("Unsupported version format: " + version);
    }

    private static int comparePrerelease(String prerelease1, String prerelease2) {
        if (prerelease1.equals(prerelease2)) {
            return 0;
        }

        if (prerelease1.isEmpty()) {
            return 1;
        }

        if (prerelease2.isEmpty()) {
            return -1;
        }

        return prerelease1.compareTo(prerelease2);
    }

    private static int compareSpecial(String special1, String special2) {
        if (special1.equals(special2)) {
            return 0;
        }

        if (special1.isEmpty()) {
            return -1;
        }

        if (special2.isEmpty()) {
            return 1;
        }

        return special1.compareTo(special2);
    }

    @Override
    public int compareTo(Version other) {
        return compare(this, other);
    }

    public int getMajor() {
        return this.major;
    }

    public int getMinor() {
        return this.minor;
    }

    public int getPatch() {
        return this.patch;
    }

    public String getPrelease() {
        return this.prelease;
    }

    public String getSpecial() {
        return this.special;
    }

    public boolean hasSameMajor(Version other) {
        return this.major == other.major;
    }

    public boolean hasSameMinor(Version other) {
        return this.minor == other.minor;
    }

    public boolean hasSamePatch(Version other) {
        return this.patch == other.patch;
    }

    public boolean hasSamePrelease(Version other) {
        if (this.prelease == null) {
            return other.prelease == null;
        }

        return this.prelease.equals(other.prelease);
    }

    public Version nextMajor() {
        Version result = new Version();

        result.major = this.major + 1;
        result.minor = 0;
        result.patch = 0;
        result.prelease = "";

        return result;
    }

    public void setMajor(int major) {
        this.major = major;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }

    public void setPatch(int patch) {
        this.patch = patch;
    }

    public void setPrelease(String prelease) {
        this.prelease = prelease;
    }
}

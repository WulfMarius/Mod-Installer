package me.wulfmarius.modinstaller;

import java.util.Optional;

public class Version implements Comparable<Version> {

    private int major;
    private int minor;
    private int patch;
    private String prelease;

    public static int compare(Version v1, Version v2) {
        return Optional.of(Integer.compare(v1.major, v2.major))
                .map(result -> result == 0 ? Integer.compare(v1.minor, v2.minor) : result)
                .map(result -> result == 0 ? Integer.compare(v1.patch, v2.patch) : result)
                .map(result -> result == 0 ? v1.prelease.compareTo(v2.prelease) : result).orElse(0);
    }

    public static Version parse(String version) {
        if (version.startsWith("v") || version.startsWith("V")) {
            return parse(version.substring(1));
        }

        Version result = new Version();

        String[] parts = version.split("\\.", 4);
        if (parts.length > 0) {
            result.major = Integer.parseInt(parts[0]);
        }

        if (parts.length > 1) {
            result.minor = Integer.parseInt(parts[1]);
        }

        if (parts.length > 2) {
            result.patch = Integer.parseInt(parts[2]);
        }

        if (parts.length > 3) {
            result.prelease = parts[3];
        } else {
            result.prelease = "";
        }

        return result;
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

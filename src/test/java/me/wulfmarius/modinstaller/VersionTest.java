package me.wulfmarius.modinstaller;

import static org.junit.Assert.assertEquals;

import java.util.*;

import org.junit.Test;

public class VersionTest {

    @Test
    public void semverPreleaseFullVersion() {
        Version version = Version.parse("1.2.3-pre");
        assertEquals(1, version.getMajor());
        assertEquals(2, version.getMinor());
        assertEquals(3, version.getPatch());
        assertEquals("", version.getSpecial());
        assertEquals("pre", version.getPrelease());
    }

    @Test
    public void semverPreleaseMajorMinorVersion() {
        Version version = Version.parse("1.2-pre");
        assertEquals(1, version.getMajor());
        assertEquals(2, version.getMinor());
        assertEquals(0, version.getPatch());
        assertEquals("", version.getSpecial());
        assertEquals("pre", version.getPrelease());
    }

    @Test
    public void semverPreleaseMajorVersion() {
        Version version = Version.parse("1-pre");
        assertEquals(1, version.getMajor());
        assertEquals(0, version.getMinor());
        assertEquals(0, version.getPatch());
        assertEquals("", version.getSpecial());
        assertEquals("pre", version.getPrelease());
    }

    @Test
    public void semverSimpleFullVersion() {
        Version version = Version.parse("1.2.3");
        assertEquals(1, version.getMajor());
        assertEquals(2, version.getMinor());
        assertEquals(3, version.getPatch());
        assertEquals("", version.getSpecial());
        assertEquals("", version.getPrelease());
    }

    @Test
    public void semverSimpleMajorMinorVersion() {
        Version version = Version.parse("1.2");
        assertEquals(1, version.getMajor());
        assertEquals(2, version.getMinor());
        assertEquals(0, version.getPatch());
        assertEquals("", version.getSpecial());
        assertEquals("", version.getPrelease());
    }

    @Test
    public void semverSimpleMajorVersion() {
        Version version = Version.parse("1");
        assertEquals(1, version.getMajor());
        assertEquals(0, version.getMinor());
        assertEquals(0, version.getPatch());
        assertEquals("", version.getSpecial());
        assertEquals("", version.getPrelease());
    }

    @Test
    public void semverSorting() {
        Version version_0_5_0 = Version.parse("0.5");
        Version version_1_0_0 = Version.parse("1");
        Version version_1_0_1 = Version.parse("1.0.1");
        Version version_1_1_0_pre = Version.parse("1.1-pre");
        Version version_1_1_0 = Version.parse("1.1");

        List<Version> versions = Arrays.asList(version_0_5_0, version_1_0_0, version_1_0_1, version_1_1_0, version_1_1_0_pre);
        Collections.shuffle(versions);
        Collections.sort(versions, Version::compare);

        assertEquals(version_0_5_0, versions.get(0));
        assertEquals(version_1_0_0, versions.get(1));
        assertEquals(version_1_0_1, versions.get(2));
        assertEquals(version_1_1_0_pre, versions.get(3));
        assertEquals(version_1_1_0, versions.get(4));
    }

    @Test
    public void specialSorting() {
        Version version_1_5 = Version.parse("1.5");
        Version version_1_6c = Version.parse("1.6c");
        Version version_1_6e = Version.parse("1.6e");
        Version version_1_6f = Version.parse("1.6f");
        Version version_1_7 = Version.parse("1.7");
        Version version_1_7a = Version.parse("1.7a");

        List<Version> versions = Arrays.asList(version_1_5, version_1_6c, version_1_6e, version_1_6f, version_1_7, version_1_7a);
        Collections.shuffle(versions);
        Collections.sort(versions, Version::compare);

        assertEquals(version_1_5, versions.get(0));
        assertEquals(version_1_6c, versions.get(1));
        assertEquals(version_1_6e, versions.get(2));
        assertEquals(version_1_6f, versions.get(3));
        assertEquals(version_1_7, versions.get(4));
        assertEquals(version_1_7a, versions.get(5));
    }

    @Test
    public void specialVersion() {
        Version version = Version.parse("1.7a");
        assertEquals(1, version.getMajor());
        assertEquals(7, version.getMinor());
        assertEquals(0, version.getPatch());
        assertEquals("a", version.getSpecial());
        assertEquals("", version.getPrelease());
    }

}

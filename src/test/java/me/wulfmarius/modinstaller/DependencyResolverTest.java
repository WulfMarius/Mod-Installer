package me.wulfmarius.modinstaller;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;

import org.junit.*;
import org.springframework.util.FileSystemUtils;

import me.wulfmarius.modinstaller.repository.*;

public class DependencyResolverTest {

    private DependencyResolver resolver;
    private Repository repository;
    private Installations installations;

    private static void assertMatches(Installations actual, ModDefinition... expected) {
        assertEquals(expected.length, actual.getSize());

        for (ModDefinition eachExpected : expected) {
            assertTrue(actual.contains(eachExpected));
        }
    }

    private static void assertMatches(ModDefinitions actual, ModDefinition... expected) {
        assertEquals("Expected: " + Arrays.toString(expected) + ", Actual: " + actual, expected.length, actual.getSize());

        for (ModDefinition eachExpected : expected) {
            assertTrue(actual.contains(eachExpected));
        }
    }

    private static void assertMatches(ModDependencies actual, ModDependency... expected) {
        assertEquals(expected.length, actual.getSize());

        for (ModDependency eachExpected : expected) {
            assertTrue(actual.contains(eachExpected));
        }
    }

    private static ModDependency createDependency(String name, String version) {
        ModDependency result = new ModDependency();

        result.setName(name);
        result.setVersion(version);

        return result;
    }

    private static Installation createInstallation(ModDefinition modDefinition) {
        Installation installation = new Installation();

        installation.setName(modDefinition.getName());
        installation.setVersion(modDefinition.getVersion());

        return installation;
    }

    @Before
    public void before() throws IOException {
        Path path = Paths.get("./target/resolver-test");
        FileSystemUtils.deleteRecursively(path);

        this.repository = new Repository(path);
        this.repository.initialize();
        this.repository.registerSource("./src/test/resources/mod-a.json");
        this.repository.registerSource("./src/test/resources/mod-b.json");
        this.repository.registerSource("./src/test/resources/mod-c.json");
        this.repository.registerSource("./src/test/resources/mod-d.json");
        this.repository.registerSource("./src/test/resources/mod-e.json");

        this.installations = new Installations();

        this.resolver = new DependencyResolver(this.repository, this.installations);
    }

    @Test
    public void install() {
        ModDefinition a10 = this.repository.getModDefinition("A", "1.0.0").get();

        Resolution resolution = this.resolver.resolve(a10);

        assertFalse(resolution.isErroneous());
        assertMatches(resolution.getInstall(), a10);
        assertMatches(resolution.getUninstall());
    }

    @Test
    public void installWithConflict() {
        ModDefinition a12 = this.repository.getModDefinition("A", "1.2.0").get();
        ModDefinition b11 = this.repository.getModDefinition("B", "1.1.0").get();
        ModDefinition c10 = this.repository.getModDefinition("C", "1.0.0").get();

        this.installations.addInstallation(createInstallation(a12));
        this.installations.addInstallation(createInstallation(c10));

        Resolution resolution = this.resolver.resolve(b11);

        assertTrue(resolution.isErroneous());
        assertFalse(resolution.hasMissingDependencies());
        assertTrue(resolution.hasUnresolvableDependencies());

        assertMatches(resolution.getUnresolvableDependencies(), createDependency("C", "1.0.0"), createDependency("C", "^1.1.0"));
    }

    @Test
    public void installWithDependencies() {
        ModDefinition a12 = this.repository.getModDefinition("A", "1.2.0").get();
        ModDefinition c1 = this.repository.getModDefinition("C", "1.0.0").get();

        Resolution resolution = this.resolver.resolve(a12);

        assertFalse(resolution.isErroneous());
        assertMatches(resolution.getInstall(), a12, c1);
        assertMatches(resolution.getUninstall());
    }

    @Test
    public void installWithExistingDependencies() {
        ModDefinition a13 = this.repository.getModDefinition("A", "1.3.0").get();
        ModDefinition b11 = this.repository.getModDefinition("B", "1.1.0").get();
        ModDefinition c11 = this.repository.getModDefinition("C", "1.1.0").get();

        this.installations.addInstallation(createInstallation(a13));
        this.installations.addInstallation(createInstallation(c11));

        Resolution resolution = this.resolver.resolve(b11);

        assertFalse(resolution.isErroneous());
        assertMatches(resolution.getInstall(), b11);
        assertMatches(resolution.getUninstall());
    }

    @Test
    public void update() {
        ModDefinition a1 = this.repository.getModDefinition("A", "1.0.0").get();
        ModDefinition a11 = this.repository.getModDefinition("A", "1.1.0").get();

        this.installations.addInstallation(createInstallation(a1));

        Resolution resolution = this.resolver.resolve(a11);

        assertFalse(resolution.isErroneous());
        assertMatches(resolution.getInstall(), a11);
        assertMatches(resolution.getUninstall(), a1);
    }

    @Test
    public void updateDependency() {
        ModDefinition a13 = this.repository.getModDefinition("A", "1.3.0").get();
        ModDefinition c11 = this.repository.getModDefinition("C", "1.1.0").get();
        ModDefinition c12 = this.repository.getModDefinition("C", "1.2.0").get();

        this.installations.addInstallation(createInstallation(a13));
        this.installations.addInstallation(createInstallation(c11));

        Resolution resolution = this.resolver.resolve(c12);

        assertFalse(resolution.isErroneous());
        assertMatches(resolution.getInstall(), c12);
        assertMatches(resolution.getUninstall(), c11);
    }

    @Test
    public void updateWithConflict() {
        ModDefinition a12 = this.repository.getModDefinition("A", "1.2.0").get();
        ModDefinition b10 = this.repository.getModDefinition("B", "1.0.0").get();
        ModDefinition b11 = this.repository.getModDefinition("B", "1.1.0").get();
        ModDefinition c10 = this.repository.getModDefinition("C", "1.0.0").get();

        this.installations.addInstallation(createInstallation(a12));
        this.installations.addInstallation(createInstallation(b10));
        this.installations.addInstallation(createInstallation(c10));

        Resolution resolution = this.resolver.resolve(b11);

        assertTrue(resolution.isErroneous());
        assertFalse(resolution.hasMissingDependencies());
        assertTrue(resolution.hasUnresolvableDependencies());

        assertMatches(resolution.getUnresolvableDependencies(), createDependency("C", "1.0.0"), createDependency("C", "^1.1.0"));
    }

    @Test
    public void updateWithDependencies() {
        ModDefinition a12 = this.repository.getModDefinition("A", "1.2.0").get();
        ModDefinition a13 = this.repository.getModDefinition("A", "1.3.0").get();
        ModDefinition c10 = this.repository.getModDefinition("C", "1.0.0").get();
        ModDefinition c13 = this.repository.getModDefinition("C", "1.3.0").get();

        this.installations.addInstallation(createInstallation(a12));
        this.installations.addInstallation(createInstallation(c10));

        Resolution resolution = this.resolver.resolve(a13);

        assertFalse(resolution.isErroneous());
        assertMatches(resolution.getInstall(), a13, c13);
        assertMatches(resolution.getUninstall(), a12, c10);
    }

    @Test
    public void updateWithDependencies2() {
        ModDefinition a12 = this.repository.getModDefinition("A", "1.2.0").get();
        ModDefinition a13 = this.repository.getModDefinition("A", "1.3.0").get();
        ModDefinition c1 = this.repository.getModDefinition("C", "1.0.0").get();
        ModDefinition c13 = this.repository.getModDefinition("C", "1.3.0").get();

        this.installations.addInstallation(createInstallation(a12));
        this.installations.addInstallation(createInstallation(c1));

        Resolution resolution = this.resolver.resolve(a13);

        assertFalse(resolution.isErroneous());
        assertMatches(resolution.getInstall(), a13, c13);
        assertMatches(resolution.getUninstall(), a12, c1);
    }

    @Test
    public void updateWithExistingDependencies() {
        ModDefinition b10 = this.repository.getModDefinition("B", "1.0.0").get();
        ModDefinition c11 = this.repository.getModDefinition("C", "1.1.0").get();

        this.installations.addInstallation(createInstallation(c11));

        Resolution resolution = this.resolver.resolve(b10);

        assertFalse(resolution.isErroneous());
        assertMatches(resolution.getInstall(), b10);
        assertMatches(resolution.getUninstall());
    }

    @Test
    public void updateWithExistingTransitiveDependencies() {
        ModDefinition a13 = this.repository.getModDefinition("A", "1.3.0").get();
        ModDefinition b10 = this.repository.getModDefinition("B", "1.0.0").get();
        ModDefinition c10 = this.repository.getModDefinition("C", "1.0.0").get();
        ModDefinition c13 = this.repository.getModDefinition("C", "1.3.0").get();
        ModDefinition d20 = this.repository.getModDefinition("D", "2.0.0").get();

        this.installations.addInstallation(createInstallation(a13));
        this.installations.addInstallation(createInstallation(b10));
        this.installations.addInstallation(createInstallation(c10));

        Resolution resolution = this.resolver.resolve(d20);

        assertFalse(resolution.isErroneous());
        assertMatches(resolution.getInstall(), d20, c13);
        assertMatches(resolution.getUninstall(), c10);
    }

    @Test
    public void updateWithMissingDefinition() {
        ModDefinition c10 = this.repository.getModDefinition("C", "1.0.0").get();

        ModDefinition c01 = new ModDefinition();
        c01.setName("C");
        c01.setVersion("0.1.0");
        this.installations.addInstallation(createInstallation(c01));
        Resolution resolution = this.resolver.resolve(c10);

        assertFalse(resolution.isErroneous());
        assertMatches(resolution.getInstall(), c10);
        assertMatches(resolution.getUninstall(), c01);
    }

    @Test
    public void updateWithTransitiveDependencies() {
        ModDefinition c13 = this.repository.getModDefinition("C", "1.3.0").get();
        ModDefinition d24 = this.repository.getModDefinition("D", "2.4.0").get();
        ModDefinition e20 = this.repository.getModDefinition("E", "2.0.0").get();

        Resolution resolution = this.resolver.resolve(e20);

        assertFalse(resolution.isErroneous());
        assertMatches(resolution.getInstall(), e20, d24, c13);
        assertMatches(resolution.getUninstall());
    }
}

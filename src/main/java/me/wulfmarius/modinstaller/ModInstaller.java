package me.wulfmarius.modinstaller;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.*;
import java.util.zip.ZipFile;

import org.springframework.util.StringUtils;

import me.wulfmarius.modinstaller.ProgressListener.StepType;
import me.wulfmarius.modinstaller.repository.*;
import me.wulfmarius.modinstaller.rest.RestClient;
import me.wulfmarius.modinstaller.update.*;
import me.wulfmarius.modinstaller.utils.JsonUtils;

public class ModInstaller {

    public static final String VERSION = "0.2.2";

    private final Path basePath;
    private final Repository repository;
    private final Installations installations = new Installations();
    private final InstallationsChangedListeners installationsChangedListeners = new InstallationsChangedListeners();
    private final ProgressListeners progressListeners = new ProgressListeners();
    private UpdateChecker updateChecker;

    public ModInstaller(Path basePath) {
        super();

        this.basePath = basePath;
        try {
            Files.createDirectories(basePath);
        } catch (IOException e) {
            throw new ModInstallerException("Could not create base path " + basePath + ".", e);
        }

        this.repository = new Repository(basePath.resolve("repository"));
    }

    private static boolean isNonEmptyDirectory(Path path) {
        if (!Files.exists(path)) {
            return false;
        }

        if (!Files.isDirectory(path)) {
            return false;
        }

        try (Stream<Path> stream = Files.list(path)) {
            return stream.count() > 0;
        } catch (IOException e) {
            throw new ModInstallerException("Failed to list files in directory " + path + ".", e);
        }
    }

    public void addInstallationsChangedListener(InstallationsChangedListener listener) {
        this.installationsChangedListeners.addListener(listener);
    }

    public void addProgressListener(ProgressListener listener) {
        this.progressListeners.addListener(listener);
        this.repository.addProgressListener(listener);
    }

    public void addSourcesChangedListener(SourcesChangedListener listener) {
        this.repository.addSourcesChangedListener(listener);
    }

    public boolean areOtherVersionsPresent() {
        return this.updateChecker.areOtherVersionsPresent();
    }

    public Installations getInstallations() {
        return this.installations;
    }

    public String getInstalledVersion(String name) {
        List<Installation> modInstallations = this.installations.getInstallations(name);
        if (modInstallations.isEmpty()) {
            return null;
        }

        return modInstallations.stream().map(Installation::getVersion).collect(Collectors.joining(", "));
    }

    public Optional<ModDefinition> getModDefinition(String name, String version) {
        return this.repository.getModDefinition(name, version);
    }

    public List<ModDefinition> getModDefinitions(String name) {
        return this.repository.getModDefinitions(name);
    }

    public Path[] getOtherVersions() {
        return this.updateChecker.getOtherVersions();
    }

    public List<ModDefinition> getRequired(ModDefinition modDefinition, Installations presentInstallations) {
        List<ModDefinition> result = new ArrayList<ModDefinition>();

        ModDefinitions installed = new ModDefinitions();
        presentInstallations.stream().map(installation -> this.getModDefinition(installation.getName(), installation.getVersion()))
                .filter(Optional::isPresent).map(Optional::get).forEach(installed::addModDefinition);

        installed.addModDefinition(modDefinition);

        while (true) {
            Map<String, List<ModDependency>> missingDependencies = installed.getMissingDependencies();
            if (missingDependencies.isEmpty()) {
                break;
            }

            for (Entry<String, List<ModDependency>> eachEntry : missingDependencies.entrySet()) {
                List<ModDependency> value = eachEntry.getValue();

                DependencyResolution resolution = this.resolve(value);
                if (resolution.getAvailable().isEmpty()) {
                    throw new MissingDependencyException(
                            "Could not resolve dependency to " + eachEntry.getKey() + ". No matching version found.",
                            resolution.getRequested());
                }

                if (resolution.getBestMatch() == null) {
                    throw new MissingDependencyException("Could not resolve dependency to " + eachEntry.getKey()
                            + ". No available version satisfies all dependencies.", resolution.getRequested());
                }

                result.add(resolution.getBestMatch());
                installed.addModDefinition(resolution.getBestMatch());
            }
        }

        return result;
    }

    public List<ModDefinition> getRequiredBy(ModDefinition modDefinition) {
        return this.getSources().stream().flatMap(Source::getLatestVersions)
                .filter(modDependency -> modDependency.dependsOn(modDefinition.getName())).collect(Collectors.toList());
    }

    public List<ModDefinition> getRequires(ModDefinition modDefinition) {
        return this.getRequired(modDefinition, new Installations());
    }

    public Sources getSources() {
        return this.repository.getSources();
    }

    public UpdateState getUpdateState() {
        return this.updateChecker.getState();
    }

    public boolean hasDownloadedNewVersion() {
        return this.updateChecker.hasDownloadedNewVersion();
    }

    public void initialize() {
        this.repository.initialize();

        Installations savedInstallations = this.readInstallations();
        if (!savedInstallations.isEmpty()) {
            this.installations.addInstallations(savedInstallations);
            this.installationsChangedListeners.changed();
        }

        this.updateChecker = new UpdateChecker(new RestClient());
        this.updateChecker.findLatestVersion();
        this.updateChecker.findOtherVersions(this.basePath.getParent());
    }

    public void install(ModDefinition modDefinition) {
        this.progressListeners.started("Installing " + modDefinition.getDisplayName());
        try {
            this.performInstall(modDefinition);
            this.writeInstallations();
        } catch (Exception e) {
            this.progressListeners.detail(e.toString());
            this.progressListeners.stepProgress(1, 1);
        } finally {
            this.progressListeners.finished();
        }
    }

    public boolean isAnyVersionInstalled(ModDefinition modDefinition) {
        return !this.installations.getInstallations(modDefinition.getName()).isEmpty();
    }

    public boolean isExactVersionInstalled(ModDefinition modDefinition) {
        return this.installations.contains(modDefinition);
    }

    public boolean isNewVersionAvailable() {
        return this.updateChecker.isNewVersionAvailable(VERSION);
    }

    public boolean isNoVersionInstalled(ModDefinition modDefinition) {
        return this.installations.getInstallations(modDefinition.getName()).isEmpty();
    }

    public boolean isOtherVersionInstalled(ModDefinition modDefinition) {
        return this.isAnyVersionInstalled(modDefinition) && !this.isExactVersionInstalled(modDefinition);
    }

    public boolean isRequiredByInstallation(ModDefinition modDefinition) {
        return this.getRequiredBy(modDefinition).stream().filter(this::isAnyVersionInstalled).count() > 0;
    }

    public void prepareUpdate() {
        this.progressListeners.started("Downloading version " + this.updateChecker.getLatestVersion());
        try {
            this.updateChecker.downloadNewVersion(this.basePath.getParent(), this.progressListeners);
            this.progressListeners.finished();
            this.progressListeners.detail("\nClose this window to start the new version.");
        } catch (Exception e) {
            this.progressListeners.detail("ERROR: " + e.toString());
            this.progressListeners.finished();
        }
    }

    public void refreshSources() {
        this.repository.refreshSources();
    }

    public void registerSource(String definition) {
        this.repository.registerSource(definition);
    }

    public void removeListener(InstallationsChangedListener listener) {
        this.installationsChangedListeners.removeListener(listener);
    }

    public void removeListener(SourcesChangedListener listener) {
        this.repository.removeSourcesChangedListener(listener);
    }

    public void removeProgressListener(ProgressListener listener) {
        this.progressListeners.removeListener(listener);
        this.repository.removeProgressListener(listener);
    }

    public void startUpdate() throws IOException {
        this.updateChecker.startNewVersion();
    }

    public void uninstallAll(String name) {
        this.progressListeners.started("Uninstalling " + name);

        try {
            this.installations.getInstallations(name).forEach(this::uninstall);
            this.writeInstallations();
        } finally {
            this.progressListeners.finished();
        }
    }

    private boolean canBeDeleted(Path path, String asset) {
        if (this.installations.getInstallationsWithAsset(asset).size() > 1) {
            return false;
        }

        if (isNonEmptyDirectory(path)) {
            return false;
        }

        if (Files.notExists(path)) {
            return false;
        }

        return true;
    }

    private String copyAsset(InputStream inputStream, String relativePath, Path targetDirectory) {
        Path targetPath = targetDirectory.resolve(relativePath);
        this.progressListeners.detail(relativePath);

        try {
            Files.createDirectories(targetPath.getParent());
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            return relativePath;
        } catch (IOException e) {
            throw new ModInstallerException("Failed to install asset " + relativePath + ".", e);
        }
    }

    private String copyAsset(Path sourcePath, Path targetDirectory) {
        try (InputStream inputStream = Files.newInputStream(sourcePath)) {
            return this.copyAsset(inputStream, sourcePath.getFileName().toString(), targetDirectory);
        } catch (IOException e) {
            throw new ModInstallerException("Failed not install asset " + sourcePath + ".", e);
        }
    }

    private String createDirectory(String relativePath, Path targetDirectory) {
        Path targetPath = targetDirectory.resolve(relativePath);
        if (Files.exists(targetPath)) {
            return relativePath;
        }

        this.progressListeners.detail(relativePath);

        try {
            Files.createDirectories(targetPath);
            return relativePath;
        } catch (IOException e) {
            throw new ModInstallerException("Failed to install asset " + relativePath + ".", e);
        }
    }

    private void deleteAssets(List<String> assets, Path targetDirectory) {
        int deletedCount = 0;

        while (true) {
            boolean deleted = false;

            for (String eachAsset : assets) {
                Path targetPath = targetDirectory.resolve(eachAsset);
                if (!this.canBeDeleted(targetPath, eachAsset)) {
                    continue;
                }

                try {
                    this.progressListeners.detail(eachAsset);
                    Files.deleteIfExists(targetPath);
                    deleted = true;
                    deletedCount++;
                    this.progressListeners.stepProgress(deletedCount, assets.size());
                } catch (IOException e) {
                    throw new ModInstallerException("Failed to delete asset " + eachAsset, e);
                }
            }

            if (!deleted) {
                break;
            }
        }

        this.progressListeners.stepProgress(assets.size(), assets.size());
    }

    private Path getInstallationsPath() {
        return this.basePath.resolve("installations.json");
    }

    private Path getModsDirectory() {
        return this.basePath.resolveSibling("mods");
    }

    private void installAsset(ModDefinition modDefinition, Asset asset, Installation installation) {
        Path modsDirectory = this.getModsDirectory();

        Path sourcePath = this.repository.getAssetPath(modDefinition, asset);
        Path targetDirectory = modsDirectory;
        if (!StringUtils.isEmpty(asset.getTargetDirectory())) {
            targetDirectory.resolve(asset.getTargetDirectory());
        }

        try {
            if (!"zip".equals(asset.getType()) && !asset.getUrl().endsWith(".zip")) {
                installation.addAsset(this.copyAsset(sourcePath, targetDirectory));
                return;
            }

            try (ZipFile zipFile = new ZipFile(sourcePath.toFile())) {
                zipFile.stream().forEach(entry -> {
                    Path entryPath = Paths.get(entry.getName());
                    if (!StringUtils.isEmpty(asset.getZipDirectory())) {
                        entryPath = Paths.get(asset.getZipDirectory()).relativize(entryPath);
                    }

                    if (entryPath.startsWith("..") || entryPath.getFileName().toString().equals("")) {
                        return;
                    }

                    if (entry.isDirectory()) {
                        installation.addAsset(this.createDirectory(entryPath.toString(), targetDirectory));
                        return;
                    }

                    try {
                        installation.addAsset(this.copyAsset(zipFile.getInputStream(entry), entryPath.toString(), targetDirectory));
                    } catch (IOException e) {
                        throw new ModInstallerException("Could not extract " + entry.getName() + " from " + asset + ".", e);
                    }
                });
            }
        } catch (IOException e) {
            throw new ModInstallerException("Could not install asset " + asset.getUrl() + ".", e);
        }
    }

    private Installation installAssets(ModDefinition modDefinition) {
        Installation installation = new Installation();
        installation.setName(modDefinition.getName());
        installation.setVersion(modDefinition.getVersion());
        installation.setSourceDefinition(modDefinition.getSourceDefinition());

        this.repository.downloadAssets(modDefinition);
        Asset[] assets = modDefinition.getAssets();

        this.progressListeners.stepStarted(modDefinition.getDisplayName(), StepType.INSTALL);
        this.progressListeners.stepProgress(0, assets.length);

        for (int i = 0; i < assets.length; i++) {
            this.installAsset(modDefinition, assets[i], installation);
            this.progressListeners.stepProgress(i + 1, assets.length);
        }

        return installation;
    }

    private void performInstall(ModDefinition modDefinition) {
        if (this.installations.contains(modDefinition)) {
            return;
        }

        List<ModDefinition> required = this.getRequired(modDefinition, this.installations);
        for (ModDefinition eachRequired : required) {
            this.performInstall(eachRequired);
        }

        List<Installation> installedVersions = this.installations.getInstallations(modDefinition.getName());
        for (Installation eachInstalledVersion : installedVersions) {
            this.uninstall(eachInstalledVersion);
        }

        Installation installation = this.installAssets(modDefinition);
        this.installations.addInstallation(installation);
    }

    private Installations readInstallations() {
        try {
            Path installedPath = this.getInstallationsPath();
            if (Files.exists(installedPath)) {
                return JsonUtils.deserialize(installedPath, Installations.class);
            }
        } catch (IOException e) {
            throw new ModInstallerException("Failed to read installed mod definitions.", e);
        }

        return new Installations();
    }

    private DependencyResolution resolve(List<ModDependency> dependencies) {
        return this.repository.resolve(dependencies);
    }

    private void uninstall(Installation installation) {
        if (!this.installations.contains(installation)) {
            throw new ModInstallerException("Requested mod is not installed.");
        }

        this.progressListeners.stepStarted(installation.getDisplayName(), StepType.UNINSTALL);
        this.deleteAssets(installation.getAssets(), this.getModsDirectory());

        this.installations.remove(installation);
    }

    private void writeInstallations() {
        try {
            JsonUtils.serialize(this.getInstallationsPath(), this.installations);
            this.installationsChangedListeners.changed();
        } catch (IOException e) {
            throw new ModInstallerException("Could not save installed mods.", e);
        }
    }
}
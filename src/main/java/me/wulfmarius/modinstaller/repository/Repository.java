package me.wulfmarius.modinstaller.repository;

import static me.wulfmarius.modinstaller.repository.SourceFactory.PARAMETER_ETAG;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.http.*;
import org.springframework.util.StringUtils;

import me.wulfmarius.modinstaller.*;
import me.wulfmarius.modinstaller.ProgressListener.StepType;
import me.wulfmarius.modinstaller.repository.source.*;
import me.wulfmarius.modinstaller.rest.RestClient;
import me.wulfmarius.modinstaller.utils.JsonUtils;

public class Repository {

    private static final String SNAPSHOT_URL = "https://raw.githubusercontent.com/WulfMarius/Mod-Installer/master/src/main/resources/default-sources.json";

    private final Path basePath;

    private final Sources sources = new Sources();
    private final List<SourceFactory> sourceFactories = new ArrayList<>();
    private final ProgressListeners progressListeners = new ProgressListeners();
    private final SourcesChangedListeners sourcesChangedListeners = new SourcesChangedListeners();

    public Repository(Path basePath) {
        super();

        this.basePath = basePath;
        try {
            Files.createDirectories(basePath);
        } catch (IOException e) {
            throw new RepositoryException("Could not create base path " + basePath + ".", e);
        }
    }

    public static String getFileName(Asset asset) {
        String url = asset.getUrl();

        int index = url.indexOf("?");
        if (index != -1) {
            url = url.substring(0, index);
        }

        index = url.lastIndexOf('/');
        if (index == url.length() - 1) {
            url = url.substring(0, url.length() - 1);
            index = url.lastIndexOf('/');
        }

        if (index != -1) {
            url = url.substring(index + 1);
        }

        if (StringUtils.isEmpty(asset.getType())) {
            return url;
        }

        return url;
    }

    public void addProgressListener(ProgressListener listener) {
        this.progressListeners.addListener(listener);
    }

    public void addSourcesChangedListener(SourcesChangedListener listener) {
        this.sourcesChangedListeners.addListener(listener);
    }

    public void downloadAssets(ModDefinition modDefinition) {
        Asset[] assets = modDefinition.getAssets();

        for (Asset eachAsset : assets) {
            Path assetPath = this.getAssetPath(modDefinition, eachAsset);
            if (Files.notExists(assetPath)) {
                RestClient.getInstance().downloadAsset(eachAsset.getUrl(), assetPath, this.progressListeners);
            }
        }
    }

    public Path getAssetPath(ModDefinition modDefinition, Asset asset) {
        return this.basePath.resolve(modDefinition.getName()).resolve(modDefinition.getVersion()).resolve(getFileName(asset));
    }

    public List<ModDefinition> getLatestVersions() {
        return this.getSources().stream().flatMap(Source::getLatestVersions).collect(Collectors.toList());
    }

    public ModDefinitions getMatching(ModDependency modDependency) {
        ModDefinitions result = new ModDefinitions();

        for (Source eachSource : this.sources) {
            result.addModDefinitions(eachSource.getMatchingDefinitions(modDependency));
        }

        return result;
    }

    public Optional<ModDefinition> getModDefinition(String name, String version) {
        for (Source eachSource : this.sources) {
            Optional<ModDefinition> modDefinition = eachSource.getModDefinition(name, version);
            if (modDefinition.isPresent()) {
                return modDefinition;
            }
        }

        return Optional.empty();
    }

    public List<ModDefinition> getModDefinitions(String name) {
        return this.sources.stream()
                .flatMap(Source::getModDefinitionStream)
                .filter(modDefinition -> modDefinition.getName().equals(name))
                .sorted(ModDefinition::latest)
                .collect(Collectors.toList());
    }

    public Sources getSources() {
        return this.sources;
    }

    public void initialize() {
        this.sourceFactories.add(new GithubSourceFactory(RestClient.getInstance()));
        this.sourceFactories.add(new DirectSourceFactory(RestClient.getInstance()));
        this.sourceFactories.add(new FileSourceFactory());

        Sources savedSources = this.readSources();
        if (!savedSources.isEmpty()) {
            this.sources.addSources(savedSources);
            this.sources.setLastUpdate(savedSources.getLastUpdate());
            this.sources.setSnapshotETag(savedSources.getSnapshotETag());
            this.sourcesChangedListeners.changed();
        }
    }

    public void invalidateSources() {
        for (Source eachSource : this.sources) {
            eachSource.removeParameter(SourceFactory.PARAMETER_ETAG);
        }
    }

    public void refreshSnapshot() {
        ResponseEntity<String> response = RestClient.getInstance().fetch(SNAPSHOT_URL, this.sources.getSnapshotETag());

        if (response.getStatusCode() == HttpStatus.NOT_MODIFIED) {
            return;
        }

        if (!response.getStatusCode().is2xxSuccessful()) {
            this.progressListeners.error("Could not find snapshot: " + SNAPSHOT_URL + " " + response.getStatusCodeValue() + "/"
                    + response.getStatusCode().getReasonPhrase());
            return;
        }

        Sources snapshot = RestClient.getInstance().deserialize(response, Sources.class, Sources::new);
        this.applySnapshot(snapshot);

        this.sources.setSnapshotETag(response.getHeaders().getETag());
        this.writeSources();
    }

    public void refreshSources() {
        String changes = null;

        try {
            this.progressListeners.started("Refreshing Sources");

            List<ModDefinition> previousLatestVersions = this.getLatestVersions();

            this.performRefreshSources();

            List<ModDefinition> currentLatestVersions = this.getLatestVersions();
            currentLatestVersions.removeAll(previousLatestVersions);
            if (!currentLatestVersions.isEmpty()) {
                changes = currentLatestVersions.stream().map(ModDefinition::getName).collect(
                        Collectors.joining("\n\t", "\n\nThe following mods were added/updated:\n\t", "\n"));
            } else {
                changes = "\n\nNo changes found";
            }
        } catch (AbortException e) {
            this.progressListeners.error(e.getMessage());
            this.progressListeners.detail("Aborting now.");
        } finally {
            this.writeSources();
            this.progressListeners.finished(changes);
        }
    }

    public void registerSource(String definition) {
        try {
            this.progressListeners.started("Add " + definition);
            this.performRegisterSource(definition);
        } catch (AbortException e) {
            this.progressListeners.error(e.getMessage());
            this.progressListeners.detail("Aborting now.");
        } finally {
            this.writeSources();
            this.progressListeners.stepProgress(1, 1);
            this.progressListeners.finished();
        }
    }

    public void removeProgressListener(ProgressListener listener) {
        this.progressListeners.removeListener(listener);
    }

    public void removeSourcesChangedListener(SourcesChangedListener listener) {
        this.sourcesChangedListeners.removeListener(listener);
    }

    private void addSource(Source source) {
        this.sources.addSource(source);
    }

    private void applySnapshot(Sources snapshot) {
        for (Source eachSnapshotSource : snapshot) {
            if (!this.sources.contains(eachSnapshotSource.getDefinition())) {
                this.progressListeners.detail("Adding " + eachSnapshotSource.getDefinition());
                this.addSource(eachSnapshotSource);
                continue;
            }

            Date now = new Date(0);
            this.sources.stream()
                    .filter(eachSource -> eachSource.getDefinition().equals(eachSnapshotSource.getDefinition()))
                    .filter(eachSource -> !eachSource.getParameter(PARAMETER_ETAG).equals(eachSnapshotSource.getParameter(PARAMETER_ETAG)))
                    .filter(eachSource -> !eachSource.getLastUpdated().orElse(now).after(eachSnapshotSource.getLastUpdated().orElse(now)))
                    .findFirst()
                    .ifPresent(eachSource -> {
                        this.progressListeners.detail("Updating " + eachSource.getDefinition());
                        eachSource.update(eachSnapshotSource);
                    });
        }
    }

    private Source createSource(String sourceDefinition, Map<String, String> parameters) {
        for (SourceFactory eachSourceFactory : this.sourceFactories) {
            if (!eachSourceFactory.isSupportedSource(sourceDefinition)) {
                continue;
            }

            this.progressListeners.detail("Loading definition");
            return eachSourceFactory.create(sourceDefinition, parameters);
        }

        throw new SourceException("Unsupported source '" + sourceDefinition + "'.");
    }

    private Path getSourcesPath() {
        return this.basePath.resolve("sources.json");
    }

    private void performRefreshSources() {
        int refreshed = 0;
        int total = this.sources.size();

        for (int i = 0; i < total; i++) {
            try {
                this.refreshSource(this.sources.getSources().get(i));
            } catch (AbortException e) {
                throw e;
            } catch (Exception e) {
                this.progressListeners.error(e.getMessage());
            }
            this.progressListeners.stepProgress(++refreshed, total);
        }
    }

    private void performRegisterSource(String definition) {
        this.progressListeners.stepStarted(definition, StepType.ADD);

        if (this.sources.contains(definition)) {
            this.progressListeners.detail("Already present.");
            return;
        }

        try {
            Source source = this.createSource(definition, Collections.emptyMap());
            this.registerDefinitions(source);
            this.addSource(source);
        } catch (AbortException e) {
            throw e;
        } catch (RuntimeException e) {
            this.progressListeners.detail("Could not register source: " + e);
        }
    }

    private Sources readSources() {
        try {
            Path sourcesPath = this.getSourcesPath();
            if (Files.exists(sourcesPath)) {
                return JsonUtils.deserialize(sourcesPath, Sources.class);
            }
        } catch (IOException e) {
            throw new RepositoryException("Failed to read sources.", e);
        }

        try (InputStream inputStream = this.getClass().getResourceAsStream("/default-sources.json")) {
            return JsonUtils.deserialize(inputStream, Sources.class);
        } catch (IOException e) {
            // ignore
        }

        return new Sources();
    }

    private boolean refreshSource(Source source) {
        this.progressListeners.stepStarted(source.getDefinition(), StepType.REFRESH);

        Source refreshedSource = this.createSource(source.getDefinition(), source.getParameters());
        if (refreshedSource.hasParameterValue(SourceFactory.PARAMETER_UNMODIFIED, "true")) {
            this.progressListeners.detail("Unmodified");

            // it may be possible that this source's definitions were not added last time (because of an error)
            // so retry to register them now
            this.registerDefinitions(source);

            return false;
        }

        source.update(refreshedSource);
        this.registerDefinitions(refreshedSource);

        this.progressListeners.detail("Updated");
        return true;
    }

    private void registerDefinitions(Source source) {
        String[] definitions = source.getDefinitions();
        if (definitions == null) {
            return;
        }

        for (String eachDefinition : definitions) {
            if (this.sources.contains(eachDefinition)) {
                continue;
            }

            this.performRegisterSource(eachDefinition);
        }
    }

    private void writeSources() {
        try {
            this.sources.setLastUpdate(new Date());
            JsonUtils.serialize(this.getSourcesPath(), this.sources);
            this.sourcesChangedListeners.changed();
        } catch (IOException e) {
            this.progressListeners.error("Could not save sources: " + e);
        }
    }
}

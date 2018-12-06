package me.wulfmarius.modinstaller.repository;

import java.io.IOException;
import java.nio.file.*;
import java.time.ZoneId;
import java.time.format.*;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.util.StringUtils;

import me.wulfmarius.modinstaller.*;
import me.wulfmarius.modinstaller.ProgressListener.StepType;
import me.wulfmarius.modinstaller.repository.source.*;
import me.wulfmarius.modinstaller.rest.RestClient;
import me.wulfmarius.modinstaller.utils.JsonUtils;

public class Repository {

    private final Path basePath;

    private final Sources sources = new Sources();
    private final List<SourceFactory> sourceFactories = new ArrayList<SourceFactory>();
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
        return this.sources.stream().flatMap(Source::getModDefinitionStream)
                .filter(modDefinition -> modDefinition.getName().equals(name)).sorted(ModDefinition::compare)
                .collect(Collectors.toList());
    }

    public Sources getSources() {
        return this.sources;
    }

    public void initialize() {
        this.sourceFactories.add(new GithubSourceFactory(RestClient.getInstance()));
        this.sourceFactories.add(new DirectSourceFactory(RestClient.getInstance()));

        Sources savedSources = this.readSources();
        if (!savedSources.isEmpty()) {
            this.sources.addSources(savedSources);
            this.sources.setLastUpdate(savedSources.getLastUpdate());
            this.sourcesChangedListeners.changed();
        }
    }

    public void invalidateSources() {
        for (Source eachSource : this.sources) {
            eachSource.removeParameter(SourceFactory.PARAMETER_ETAG);
        }
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
                changes = currentLatestVersions.stream().map(ModDefinition::getName)
                        .collect(Collectors.joining("\n\t", "\n\nThe following mods were added/updated:\n\t", "\n"));
            } else {
                changes = "\n\nNo changes found";
            }
        } catch (RateLimitException e) {
            this.progressListeners.detail("RATE LIMIT REACHED. Please try again after "
                    + DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM).format(e.getReset().atZone(ZoneId.systemDefault())));
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
        } catch (RateLimitException e) {
            this.progressListeners.detail("RATE LIMIT REACHED. Please try again after "
                    + DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM).format(e.getReset().atZone(ZoneId.systemDefault())));
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

    public DependencyResolution resolve(List<ModDependency> dependencies) {
        for (Source eachSource : this.sources) {
            DependencyResolution resolution = eachSource.getModDefinitions().resolve(dependencies);
            if (resolution.isResolved()) {
                return resolution;
            }
        }

        DependencyResolution result = new DependencyResolution();
        result.setDependencies(dependencies);
        return result;
    }

    private void addSource(Source source) {
        this.sources.addSource(source);
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
            } catch (Exception e) {
                this.progressListeners.detail(e.getMessage());
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
        } catch (RateLimitException e) {
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
            this.progressListeners.detail("Could not save sources: " + e);
        }
    }
}
package me.wulfmarius.modinstaller.update;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.stream.Stream;

import org.springframework.http.ResponseEntity;

import me.wulfmarius.modinstaller.*;
import me.wulfmarius.modinstaller.repository.source.*;
import me.wulfmarius.modinstaller.rest.RestClient;
import me.wulfmarius.modinstaller.utils.JsonUtils;

public class UpdateChecker {

    private static final String LATEST_RELEASE_URL = "https://api.github.com/repos/WulfMarius/Mod-Installer/releases/latest";

    private final Path basePath;
    private final RestClient restClient;
    private final UpdateState state;

    private Path downloadPath;
    private Path[] otherVersions;

    public UpdateChecker(Path basePath, RestClient restClient) {
        super();

        this.basePath = basePath;
        this.restClient = restClient;
        this.state = this.readState();
    }

    public boolean areOtherVersionsPresent() {
        return this.otherVersions != null && this.otherVersions.length > 0;
    }

    public void downloadNewVersion(Path directory, ProgressListeners progressListeners) {
        this.downloadPath = directory.resolve(this.getState().getAsset().getName());
        this.restClient.downloadAsset(this.state.getAsset().getDownloadUrl(), this.downloadPath, progressListeners);
    }

    public void findLatestVersion() {
        if (!this.canCheckForNewVersion()) {
            return;
        }

        ResponseEntity<String> response = this.restClient.fetch(LATEST_RELEASE_URL, this.state.getEtag());
        if (response.getStatusCode().is2xxSuccessful()) {
            GithubRelease latestRelease = this.restClient.deserialize(response, GithubRelease.class, GithubRelease::new);
            this.state.setLatestVersion(latestRelease.getName());

            GithubAsset[] assets = latestRelease.getAssets();
            if (assets != null && assets.length == 1) {
                this.state.setAsset(assets[0]);
            } else {
                this.state.setAsset(null);
            }
            this.state.setReleaseUrl(latestRelease.getUrl());
        }

        this.state.setEtag(response.getHeaders().getETag());
        this.state.setChecked(new Date());
    }

    public void findOtherVersions() {
        Path currentJar = this.getCurrentJarPath();

        try (Stream<Path> stream = Files.list(this.basePath)) {
            this.otherVersions = stream.filter(Files::isRegularFile)
                    .filter(file -> !file.getFileName().equals(currentJar))
                    .filter(file -> file.getFileName().toString().startsWith("mod-installer-")
                            && file.getFileName().toString().endsWith(".jar"))
                    .toArray(Path[]::new);
        } catch (IOException e) {
            this.otherVersions = new Path[0];
        }
    }

    public String getDownloadURL() {
        return this.state.getReleaseUrl();
    }

    public String getLatestVersion() {
        return this.state.getLatestVersion();
    }

    public Path[] getOtherVersions() {
        return this.otherVersions;
    }

    public UpdateState getState() {
        return this.state;
    }

    public boolean hasDownloadedNewVersion() {
        return this.downloadPath != null && Files.exists(this.downloadPath);
    }

    public void initialize() {
        if (System.getProperty("SKIP_SELF_UPDATE_CHECK") != null) {
            this.state.setLatestVersion(null);
            return;
        }

        this.findLatestVersion();
        this.findOtherVersions();

        this.writeState();
    }

    public boolean isNewVersionAvailable(String version) {
        if (this.state.getLatestVersion() == null) {
            return false;
        }

        return Version.parse(this.getLatestVersion()).compareTo(Version.parse(version)) > 0;
    }

    public void startNewVersion() throws IOException {
        new ProcessBuilder("java", "-jar", this.downloadPath.getFileName().toString()).inheritIO().start();
    }

    private boolean canCheckForNewVersion() {
        if (this.state.getChecked() == null) {
            return true;
        }

        return this.state.getChecked().toInstant().plus(5, ChronoUnit.MINUTES).isBefore(Instant.now());
    }

    private Path getCurrentJarPath() {
        try {
            return Paths.get(UpdateChecker.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getFileName();
        } catch (URISyntaxException e) {
            return null;
        }
    }

    private Path getUpdateCheckStatePath() {
        return Paths.get(System.getProperty("java.io.tmpdir"), "tld-mod-installer-update-check-state.json");
    }

    private UpdateState readState() {
        try {
            Path path = this.getUpdateCheckStatePath();
            if (Files.exists(path)) {
                return JsonUtils.deserialize(path, UpdateState.class);
            }
        } catch (IOException e) {
            // ignore
        }

        return new UpdateState();
    }

    private void writeState() {
        try {
            Path path = this.getUpdateCheckStatePath();
            Files.createDirectories(path.getParent());
            JsonUtils.serialize(path, this.state);
        } catch (IOException e) {
            // ignore
        }
    }
}

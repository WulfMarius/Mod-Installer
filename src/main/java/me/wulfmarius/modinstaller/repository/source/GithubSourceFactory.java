package me.wulfmarius.modinstaller.repository.source;

import java.text.MessageFormat;
import java.util.*;
import java.util.regex.*;

import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;

import me.wulfmarius.modinstaller.*;
import me.wulfmarius.modinstaller.repository.SourceDescription;
import me.wulfmarius.modinstaller.rest.RestClient;

public class GithubSourceFactory extends AbstractSourceFactory {

    public static final String PARAMETER_USER = "user";

    private static final Pattern SOURCE_PATTERN = Pattern.compile("\\Qhttps://github.com/\\E([A-Z0-9-]+)/([A-Z0-9-_]+)/?",
            Pattern.CASE_INSENSITIVE);

    public GithubSourceFactory(RestClient restClient) {
        super(restClient);
    }

    @Override
    public boolean isSupportedSource(String sourceDefinition) {
        return SOURCE_PATTERN.matcher(sourceDefinition).matches();
    }

    @Override
    protected String getDefinitionsUrl(String sourceDefinition) {
        Matcher matcher = SOURCE_PATTERN.matcher(sourceDefinition);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Unsupported source definition " + sourceDefinition);
        }

        return MessageFormat.format("https://raw.githubusercontent.com/{0}/{1}/master/mod-installer-description.json",
                matcher.group(1),
                matcher.group(2));
    }

    protected GithubRelease[] getGithubReleases(String definition) {
        String url = definition.replace("//github.com/", "//api.github.com/repos/") + "/releases";
        ResponseEntity<String> response = this.restClient.fetch(url, null);
        return this.restClient.deserialize(response, GithubRelease[].class, () -> new GithubRelease[0]);
    }

    @Override
    protected void postProcessSourceDescription(String definition, SourceDescription sourceDescription) {
        super.postProcessSourceDescription(definition, sourceDescription);

        ModDefinition[] releases = sourceDescription.getReleases();
        if (releases == null) {
            return;
        }

        ReleaseProvider releaseProvider = new ReleaseProvider(definition);

        for (ModDefinition eachRelease : releases) {
            if (StringUtils.isEmpty(eachRelease.getUrl())) {
                eachRelease.setUrl(releaseProvider.getRelease(eachRelease.getVersion()).map(GithubRelease::getUrl).orElse(
                        definition + "/releases/tag/" + eachRelease.getVersion()));
            }

            if (StringUtils.isEmpty(eachRelease.getChanges())) {
                eachRelease.setChanges(releaseProvider.getRelease(eachRelease.getVersion()).map(GithubRelease::getBody).orElse(""));
            }

            if (eachRelease.getAssets() == null || eachRelease.getAssets().length == 0) {
                eachRelease.setAssets(releaseProvider.getRelease(eachRelease.getVersion())
                        .map(GithubRelease::getAssets)
                        .map(assets -> Arrays.stream(assets).map(GithubAsset::toAsset).toArray(Asset[]::new))
                        .orElse(new Asset[0]));
            }

            if (eachRelease.getReleaseDate() == null) {
                eachRelease.setReleaseDate(
                        releaseProvider.getRelease(eachRelease.getVersion()).map(GithubRelease::getDate).orElse(new Date()));
            }

            if (eachRelease.getAuthor() == null) {
                eachRelease.setAuthor(releaseProvider.getRelease(eachRelease.getVersion())
                        .map(GithubRelease::getAuthor)
                        .map(GithubAuthor::getLogin)
                        .orElse(sourceDescription.getAuthor()));
            }

            if (eachRelease.getAuthor() == null) {
                Matcher matcher = SOURCE_PATTERN.matcher(definition);
                if (matcher.matches()) {
                    eachRelease.setAuthor(matcher.group(1));
                }
            }
        }
    }

    private class ReleaseProvider {

        private final String definition;
        private GithubRelease[] githubReleases = null;

        public ReleaseProvider(String definition) {
            super();
            this.definition = definition;
        }

        public Optional<GithubRelease> getRelease(String version) {
            if (this.githubReleases == null) {
                this.githubReleases = GithubSourceFactory.this.getGithubReleases(this.definition);
            }

            return Arrays.stream(this.githubReleases).filter(release -> release.hasMatchingTag(version)).findFirst();
        }
    }
}

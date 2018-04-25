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

    private static final Pattern SOURCE_PATTERN = Pattern.compile("\\Qhttps://github.com/\\E([A-Z0-9-]+)/([A-Z0-9-]+)",
            Pattern.CASE_INSENSITIVE);

    public GithubSourceFactory(RestClient restClient) {
        super(restClient);
    }

    private static Optional<GithubRelease> getRelease(GithubRelease[] releases, String version) {
        return Arrays.stream(releases).filter(release -> release.getName().equals(version)).findFirst();
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
                matcher.group(1), matcher.group(2));
    }

    @Override
    protected void postProcessSourceDescription(String definition, SourceDescription sourceDescription) {
        super.postProcessSourceDescription(definition, sourceDescription);

        ModDefinition[] releases = sourceDescription.getReleases();
        if (releases == null) {
            return;
        }

        GithubRelease[] githubReleases = null;

        for (ModDefinition eachRelease : releases) {
            if (StringUtils.isEmpty(eachRelease.getUrl())) {
                if (githubReleases == null) {
                    githubReleases = this.getGithubReleases(definition);
                }

                Optional<GithubRelease> optional = getRelease(githubReleases, eachRelease.getVersion());
                eachRelease
                        .setUrl(optional.map(GithubRelease::getUrl).orElse(definition + "/releases/tag/" + eachRelease.getVersion()));
            }

            if (StringUtils.isEmpty(eachRelease.getChanges())) {
                if (githubReleases == null) {
                    githubReleases = this.getGithubReleases(definition);
                }

                Optional<GithubRelease> optional = getRelease(githubReleases, eachRelease.getVersion());
                eachRelease.setChanges(optional.map(GithubRelease::getBody).orElse(""));
            }

            if (eachRelease.getAssets() == null || eachRelease.getAssets().length == 0) {
                if (githubReleases == null) {
                    githubReleases = this.getGithubReleases(definition);
                }

                Optional<GithubRelease> optional = getRelease(githubReleases, eachRelease.getVersion());
                eachRelease.setAssets(optional.map(GithubRelease::getAssets)
                        .map(assets -> Arrays.stream(assets).map(GithubAsset::toAsset).toArray(Asset[]::new)).orElse(new Asset[0]));
            }
        }
    }

    private GithubRelease[] getGithubReleases(String definition) {
        String url = definition.replace("//github.com/", "//api.github.com/repos/") + "/releases";
        ResponseEntity<String> response = this.restClient.fetch(url, null);
        return this.restClient.deserialize(response, GithubRelease[].class, () -> new GithubRelease[0]);
    }
}

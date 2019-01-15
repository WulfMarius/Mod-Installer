package me.wulfmarius.modinstaller.repository.source;

import me.wulfmarius.modinstaller.ModDefinition;
import me.wulfmarius.modinstaller.repository.SourceDescription;
import me.wulfmarius.modinstaller.rest.RestClient;

public class DirectSourceFactory extends AbstractSourceFactory {

    public DirectSourceFactory(RestClient restClient) {
        super(restClient);
    }

    @Override
    public boolean isSupportedSource(String definition) {
        return definition.startsWith("http://") || definition.startsWith("https://");
    }

    @Override
    protected String getDefinitionsUrl(String sourceDefinition) {
        return sourceDefinition;
    }

    @Override
    protected void postProcessSourceDescription(String definition, SourceDescription sourceDescription) {
        super.postProcessSourceDescription(definition, sourceDescription);

        ModDefinition[] releases = sourceDescription.getReleases();
        if (releases == null) {
            return;
        }

        for (ModDefinition eachRelease : releases) {
            if (eachRelease.getAuthor() == null) {
                eachRelease.setAuthor(sourceDescription.getAuthor());
            }
        }
    }
}

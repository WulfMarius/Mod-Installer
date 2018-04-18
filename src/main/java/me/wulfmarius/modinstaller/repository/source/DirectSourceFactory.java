package me.wulfmarius.modinstaller.repository.source;

import me.wulfmarius.modinstaller.rest.RestClient;

public class DirectSourceFactory extends AbstractSourceFactory {

    public DirectSourceFactory(RestClient restClient) {
        super(restClient);
    }

    @Override
    public boolean isSupportedSource(String definition) {
        return true;
    }

    @Override
    protected String getDefinitionsUrl(String sourceDefinition) {
        return sourceDefinition;
    }
}

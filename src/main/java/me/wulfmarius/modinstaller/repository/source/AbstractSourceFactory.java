package me.wulfmarius.modinstaller.repository.source;

import java.util.Map;

import me.wulfmarius.modinstaller.repository.*;

public abstract class AbstractSourceFactory implements SourceFactory {

    protected RestClient restClient;

    protected AbstractSourceFactory() {
        super();

        this.restClient = new RestClient();
    }

    @Override
    public final Source create(String definition, Map<String, ?> parameters) {
        if (!this.isSupportedSource(definition)) {
            throw new IllegalArgumentException("Unsupported source definition " + definition);
        }

        SourceDescription sourceDescription = this.getSourceDescription(definition, parameters);
        return Source.from(definition, sourceDescription);
    }

    protected abstract String getDefinitionsUrl(String sourceDefinition);

    protected SourceDescription getSourceDescription(String sourceDefinition, Map<String, ?> parameter) {
        String url = this.getDefinitionsUrl(sourceDefinition);
        return this.restClient.getSourceDescription(url, parameter);
    }
}
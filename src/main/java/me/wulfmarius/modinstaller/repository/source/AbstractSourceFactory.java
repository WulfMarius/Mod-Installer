package me.wulfmarius.modinstaller.repository.source;

import java.util.Map;

import org.springframework.http.*;
import org.springframework.util.StringUtils;

import me.wulfmarius.modinstaller.repository.*;
import me.wulfmarius.modinstaller.rest.RestClient;

public abstract class AbstractSourceFactory implements SourceFactory {

    protected final RestClient restClient;

    protected AbstractSourceFactory(RestClient restClient) {
        super();
        this.restClient = restClient;
    }

    @Override
    public final Source create(String definition, Map<String, String> parameters) {
        if (!this.isSupportedSource(definition)) {
            throw new IllegalArgumentException("Unsupported source definition " + definition);
        }

        SourceDescription sourceDescription = this.getSourceDescription(definition, parameters);
        this.postProcessSourceDescription(definition, sourceDescription);
        return Source.from(definition, sourceDescription);
    }

    protected abstract String getDefinitionsUrl(String sourceDefinition);

    protected SourceDescription getSourceDescription(String sourceDefinition, Map<String, String> parameters) {
        String url = this.getDefinitionsUrl(sourceDefinition);

        ResponseEntity<String> response = this.restClient.fetch(url, parameters.get(HttpHeaders.ETAG));
        SourceDescription result = this.restClient.deserialize(response, SourceDescription.class,
                this::createUnmodifiedSourceDescription);
        result.setParameter(HttpHeaders.ETAG, response.getHeaders().getETag());

        return result;
    }

    protected void postProcessSourceDescription(String definition, SourceDescription sourceDescription) {
        if (StringUtils.isEmpty(sourceDescription.getUrl())) {
            sourceDescription.setUrl(definition);
        }
    }

    private SourceDescription createUnmodifiedSourceDescription() {
        SourceDescription result = new SourceDescription();
        result.setParameter("Unmodified", "true");
        return result;
    }
}
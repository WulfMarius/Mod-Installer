package me.wulfmarius.modinstaller.repository.source;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

import me.wulfmarius.modinstaller.repository.*;
import me.wulfmarius.modinstaller.utils.JsonUtils;

public class FileSourceFactory extends AbstractSourceFactory {

    public FileSourceFactory() {
        super(null);
    }

    @Override
    public boolean isSupportedSource(String sourceDefinition) {
        return true;
    }

    @Override
    protected String getDefinitionsUrl(String sourceDefinition) {
        return null;
    }

    @Override
    protected SourceDescription getSourceDescription(String sourceDefinition, Map<String, String> parameters) {
        try {
            return JsonUtils.deserialize(Paths.get(sourceDefinition), SourceDescription.class);
        } catch (IOException e) {
            throw new SourceException("Could not read source description: " + e.getMessage());
        }
    }
}

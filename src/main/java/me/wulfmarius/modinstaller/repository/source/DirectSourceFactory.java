package me.wulfmarius.modinstaller.repository.source;

public class DirectSourceFactory extends AbstractSourceFactory {

    @Override
    public boolean isSupportedSource(String definition) {
        return true;
    }

    @Override
    protected String getDefinitionsUrl(String sourceDefinition) {
        return sourceDefinition;
    }
}

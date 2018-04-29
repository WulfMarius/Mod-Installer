package me.wulfmarius.modinstaller.repository;

import java.util.Map;

public interface SourceFactory {

    String PARAMETER_UNMODIFIED = "unmodified";
    String PARAMETER_ETAG = "etag";
    String PARAMETER_VERSION = "version";

    Source create(String sourceDefinition, Map<String, String> parameters);

    boolean isSupportedSource(String sourceDefinition);

}

package me.wulfmarius.modinstaller.repository.source;

import java.text.MessageFormat;
import java.util.regex.*;

public class GithubSourceFactory extends AbstractSourceFactory {

    public static final String PARAMETER_USER = "user";
    public static final String PARAMETER_REPOSITORY = "repository";

    private static final Pattern SOURCE_PATTERN = Pattern.compile("\\Qhttps://github.com/\\E([A-Z0-9-]+)/([A-Z0-9-]+)",
            Pattern.CASE_INSENSITIVE);

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
}

package me.wulfmarius.modinstaller.utils;

import java.io.*;
import java.nio.file.*;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.*;

public class JsonUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.setSerializationInclusion(Include.NON_NULL);
        OBJECT_MAPPER.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
        OBJECT_MAPPER.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        OBJECT_MAPPER.configure(SerializationFeature.INDENT_OUTPUT, true);
        OBJECT_MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static <T> T deserialize(InputStream inputStream, Class<T> type) throws IOException {
        return OBJECT_MAPPER.readValue(inputStream, type);
    }

    public static <T> T deserialize(Path path, Class<T> type) throws IOException {
        try (InputStream inputStream = Files.newInputStream(path)) {
            return deserialize(inputStream, type);
        }
    }

    public static <T> T deserialize(String content, Class<T> type) throws IOException {
        return OBJECT_MAPPER.readValue(content, type);
    }

    public static void serialize(Path path, Object value) throws IOException {
        try (OutputStream outputStream = Files.newOutputStream(path)) {
            OBJECT_MAPPER.writeValue(outputStream, value);
        }
    }
}

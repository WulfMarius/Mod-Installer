package me.wulfmarius.modinstaller.repository;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.regex.*;

import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.ResponseExtractor;

import me.wulfmarius.modinstaller.ProgressListeners;
import me.wulfmarius.modinstaller.utils.StringUtils;

public class DownloadResponseExtractor implements ResponseExtractor<String> {

    private final Path targetFile;
    private final ProgressListeners progressListeners;

    public DownloadResponseExtractor(Path targetFile, ProgressListeners progressListeners) {
        super();

        this.targetFile = targetFile;
        this.progressListeners = progressListeners;
    }

    private static String getBody(ClientHttpResponse response) throws IOException {
        return StreamUtils.copyToString(response.getBody(), getContentTypeCharset(response.getHeaders().getContentType()));
    }

    private static Charset getContentTypeCharset(@Nullable MediaType contentType) {
        if (contentType != null && contentType.getCharset() != null) {
            return contentType.getCharset();
        }

        return StandardCharsets.ISO_8859_1;
    }

    @Override
    public String extractData(ClientHttpResponse response) throws IOException {
        MediaType contentType = response.getHeaders().getContentType();
        if (contentType.isCompatibleWith(MediaType.TEXT_HTML)) {
            String body = getBody(response);
            Pattern pattern = Pattern.compile("\\Qwindow.location.href=\"\\E(\\Qhttp://www.moddb.com/downloads/\\E.*?)\"",
                    Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(body);
            if (matcher.find()) {
                return matcher.group(1);
            }

            throw new SourceException("Received unexpected text/html response.");
        }

        long contentLength = response.getHeaders().getContentLength();
        this.progressListeners.detail(StringUtils.formatByteCount(contentLength));

        Files.createDirectories(this.targetFile.getParent());
        long copied = 0;
        this.progressListeners.stepProgress((int) copied, (int) contentLength);

        byte[] buffer = new byte[4096];
        try (InputStream inputStream = response.getBody(); OutputStream outputStream = Files.newOutputStream(this.targetFile)) {
            while (true) {
                int count = inputStream.read(buffer);
                if (count == -1) {
                    break;
                }

                copied += count;
                outputStream.write(buffer, 0, count);
                this.progressListeners.stepProgress((int) copied, (int) contentLength);
            }
        }

        return null;
    }
}

package me.wulfmarius.modinstaller.rest;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;
import java.util.function.Supplier;

import org.springframework.http.*;
import org.springframework.http.client.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.StringUtils;
import org.springframework.web.client.*;

import me.wulfmarius.modinstaller.ProgressListener.StepType;
import me.wulfmarius.modinstaller.ProgressListeners;
import me.wulfmarius.modinstaller.repository.*;
import me.wulfmarius.modinstaller.utils.JsonUtils;

public class RestClient {

    private static final RestClient INSTANCE = new RestClient();

    private RestTemplate restTemplate;
    private Instant rateLimitReset;

    private RestClient() {
        super();

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(10000);
        requestFactory.setReadTimeout(10000);

        this.restTemplate = new RestTemplate(requestFactory);
        this.restTemplate.setMessageConverters(Arrays.asList(new StringHttpMessageConverter()));
    }

    public static RestClient getInstance() {
        return INSTANCE;
    }

    public <T> T deserialize(ResponseEntity<String> response, Class<T> type, Supplier<T> unmodifiedSupplier) {
        if (HttpStatus.NOT_MODIFIED.equals(response.getStatusCode())) {
            return unmodifiedSupplier.get();
        }

        if (HttpStatus.NOT_FOUND.equals(response.getStatusCode())) {
            return null;
        }

        try {
            String json = response.getBody();
            if (json.startsWith("\uFEFF")) {
                json = json.substring(1);
            }

            if (json.startsWith("\u00EF\u00BB\u00BF")) {
                json = json.substring(3);
            }

            return JsonUtils.deserialize(json, type);
        } catch (IOException e) {
            throw new SourceException("Could not deserialize: " + e.getMessage(), e);
        }
    }

    public void downloadAsset(String url, Path assetPath, ProgressListeners progressListeners) {
        progressListeners.stepStarted(url, StepType.DOWNLOAD);

        String redirectURL = url;
        while (redirectURL != null) {
            redirectURL = this.restTemplate.execute(redirectURL, HttpMethod.GET, this::prepareRequest,
                    new DownloadResponseExtractor(assetPath, progressListeners));
        }
    }

    public ResponseEntity<String> fetch(String url, String etag) {
        if (this.isRateLimitReached()) {
            throw new RateLimitException(this.rateLimitReset);
        }

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setIfNoneMatch(etag);

        try {
            ResponseEntity<String> responseEntity = this.restTemplate.exchange(url, HttpMethod.GET,
                    new HttpEntity<Object>(requestHeaders), String.class);
            this.handleRateLimit(responseEntity.getHeaders());
            return responseEntity;
        } catch (HttpClientErrorException e) {
            this.handleRateLimit(e.getResponseHeaders());
            return new ResponseEntity<>(e.getStatusText(), e.getStatusCode());
        } catch (Exception e) {
            throw new SourceException("Could not fetch from " + url + ": " + e.getMessage(), e);
        }
    }

    private void handleRateLimit(HttpHeaders headers) {
        String remaining = headers.getFirst("X-RateLimit-Remaining");
        if (StringUtils.isEmpty(remaining) || !remaining.equals("0")) {
            return;
        }

        String reset = headers.getFirst("X-RateLimit-Reset");
        if (StringUtils.isEmpty(reset)) {
            return;
        }

        this.rateLimitReset = Instant.ofEpochSecond(Long.parseLong(reset));
    }

    private boolean isRateLimitReached() {
        if (this.rateLimitReset == null) {
            return false;
        }

        if (this.rateLimitReset.isBefore(Instant.now())) {
            this.rateLimitReset = null;
            return false;
        }

        return true;
    }

    private void prepareRequest(@SuppressWarnings("unused") ClientHttpRequest request) {
        // nothing to do
    }
}

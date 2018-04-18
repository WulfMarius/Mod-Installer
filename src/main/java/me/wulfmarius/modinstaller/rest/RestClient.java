package me.wulfmarius.modinstaller.rest;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.Supplier;

import org.springframework.http.*;
import org.springframework.http.client.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.*;

import me.wulfmarius.modinstaller.ProgressListener.StepType;
import me.wulfmarius.modinstaller.ProgressListeners;
import me.wulfmarius.modinstaller.repository.*;
import me.wulfmarius.modinstaller.utils.JsonUtils;

public class RestClient {

    private RestTemplate restTemplate;

    public RestClient() {
        super();

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(10000);
        requestFactory.setReadTimeout(10000);

        this.restTemplate = new RestTemplate(requestFactory);
        this.restTemplate.setMessageConverters(Arrays.asList(new StringHttpMessageConverter()));
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
            if (json.charAt(0) == '\uFEFF') {
                json = json.substring(1);
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
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setIfNoneMatch(etag);

        try {
            return this.restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<Object>(requestHeaders), String.class);
        } catch (HttpClientErrorException e) {
            return new ResponseEntity<>(e.getStatusText(), e.getStatusCode());
        } catch (RestClientException e) {
            throw new SourceException("Could not fetch from " + url + ": " + e.getMessage(), e);
        }
    }

    private void prepareRequest(@SuppressWarnings("unused") ClientHttpRequest request) {
        // nothing to do
    }

}

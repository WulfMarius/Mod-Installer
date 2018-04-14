package me.wulfmarius.modinstaller.repository.source;

import java.io.IOException;
import java.util.*;

import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import me.wulfmarius.modinstaller.repository.*;

public class RestClient {

    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;

    public RestClient() {
        super();

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(10000);
        requestFactory.setReadTimeout(10000);

        this.restTemplate = new RestTemplate(requestFactory);
        this.restTemplate.setMessageConverters(Arrays.asList(new StringHttpMessageConverter()));

        this.objectMapper = new ObjectMapper();
    }

    public SourceDescription createUnmodifiedSourceDescription() {
        SourceDescription result = new SourceDescription();
        result.setParameter("Unmodified", "true");
        return result;
    }

    public SourceDescription getSourceDescription(String url, Map<String, ?> parameters) {
        String previousETag = (String) parameters.get(HttpHeaders.ETAG);

        ResponseEntity<String> response = this.loadContent(url, previousETag);

        SourceDescription result = this.deserializeSourceDescription(response);
        String currentETag = response.getHeaders().getETag();
        result.setParameter(HttpHeaders.ETAG, currentETag);
        return result;
    }

    public ResponseEntity<String> loadContent(String url, String etag) {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setIfNoneMatch(etag);

        try {
            return this.restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<Object>(requestHeaders), String.class);
        } catch (RestClientException e) {
            throw new SourceException("Could not load source descriptions from " + url + ": " + e.getMessage(), e);
        }
    }

    private SourceDescription deserializeSourceDescription(ResponseEntity<String> response) {
        if (response.getStatusCodeValue() == 304) {
            return this.createUnmodifiedSourceDescription();
        }

        try {
            String json = response.getBody();
            if (json.charAt(0) == '\uFEFF') {
                json = json.substring(1);
            }

            return this.objectMapper.readValue(json, SourceDescription.class);
        } catch (IOException e) {
            throw new SourceException("Could not deserialize source description: " + e.getMessage(), e);
        }
    }
}

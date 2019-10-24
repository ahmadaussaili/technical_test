package com.tech.rideways.service.utils;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.Map;

@Component
public class Client {

    @Autowired
    private RestTemplate restTemplate;

    /**
     * Makes a GET request to the specified URL.
     *
     * @param base_url
     * @param path
     * @param params
     * @return response
     */
    public ResponseEntity<String> get(String base_url, String path, Map<String, String> params) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(base_url);
            builder.path(path);
            for (Map.Entry<String, String> param : params.entrySet()) {
                builder.queryParam(param.getKey(), param.getValue());
            }

            HttpEntity entity = new HttpEntity<>(headers);

            // Execute the get request.
            ResponseEntity<String> response = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);
            return response;

        }
        catch (HttpClientErrorException clientErrorException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request to " + path + "'s API");
        }
        catch (HttpServerErrorException serverUnavailableException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(path + "'s API is currently unavailable");
        }
        catch (Exception exception) {
            // Timeout of 2 seconds.
            return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(path + "'s API timed out.");
        }
    }
}

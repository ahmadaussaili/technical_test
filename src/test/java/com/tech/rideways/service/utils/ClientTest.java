package com.tech.rideways.service.utils;

import com.tech.rideways.service.RideService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class ClientTest {

    private static final String TAXI_API_URL = "https://techtest.rideways.com";
    private static final String DAVE_PATH = "dave";

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private RideService rideService;

    @InjectMocks
    private Client client;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void get_whenApiCallIsSuccessful_return200WithValidPayload() throws IOException {

        // arrange
        String fixturePath =
                new File("src/test/java/com/tech/rideways/service/fixtures/ride_response_payload.json").getAbsolutePath();
        String expectedPayload = Files.readAllLines(Paths.get(fixturePath), StandardCharsets.UTF_8)
                .stream()
                .collect(Collectors.joining());

        // construct the request headers and url
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity entity = new HttpEntity<>(headers);

        String url = TAXI_API_URL + "/" + DAVE_PATH;
        when(restTemplate.exchange(url, HttpMethod.GET, entity, String.class)).thenReturn(ResponseEntity.ok(expectedPayload));

        // act
        ResponseEntity<String> response = client.get(TAXI_API_URL, DAVE_PATH, new HashMap<>());

        // assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedPayload, response.getBody());
    }

    @Test(expected = Exception.class)
    public void get_whenApiCallTimeouts_return408() {

        // arrange - construct the request headers and url
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity entity = new HttpEntity<>(headers);

        String url = TAXI_API_URL + "/" + DAVE_PATH;
        when(restTemplate.exchange(url, HttpMethod.GET, entity, String.class)).thenThrow(Exception.class);

        // act
        ResponseEntity<String> response = client.get(TAXI_API_URL, DAVE_PATH, new HashMap<>());

        // assert handled in the annotation
    }

    @Test
    public void get_whenServiceIsUnavailable_return500() throws IOException {

        // arrange
        String fixturePath =
                new File("src/test/java/com/tech/rideways/service/fixtures/ride_response_payload.json").getAbsolutePath();
        String expectedPayload = Files.readAllLines(Paths.get(fixturePath), StandardCharsets.UTF_8)
                .stream()
                .collect(Collectors.joining());

        // construct the request headers and url
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity entity = new HttpEntity<>(headers);

        String url = TAXI_API_URL + "/" + DAVE_PATH;
        when(restTemplate.exchange(url, HttpMethod.GET, entity, String.class))
                .thenReturn(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(""));

        // act
        ResponseEntity<String> response = client.get(TAXI_API_URL, DAVE_PATH, new HashMap<>());

        // assert
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
    }
}

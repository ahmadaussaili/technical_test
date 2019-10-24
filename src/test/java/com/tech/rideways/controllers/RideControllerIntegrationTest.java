package com.tech.rideways.controllers;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.tech.rideways.Application;
import com.tech.rideways.config.AppConfig;
import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = AppConfig.class)
@SpringBootTest(classes = {Application.class, ServletWebServerFactoryAutoConfiguration.class},
                webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class RideControllerIntegrationTest {

    @Value("${server.port}")
    private int port;

    @Value("${wiremock.port}")
    private int wiremockPort;

    // Testing variables
    private static final String PICK_UP = "51.470020,-0.454295";
    private static final String DROP_OFF = "53.470020,-0.454295";

    private static final String DAVE_PATH = "dave";
    private static final String ERIC_PATH = "eric";
    private static final String JEFF_PATH = "jeff";

    // External server URL paths
    private static final String EXTERNAL_API_PATH = "/external";
    private static final String EXTERNAL_DAVE_PATH = "/external/dave";
    private static final String EXTERNAL_ERIC_PATH = "/external/eric";
    private static final String EXTERNAL_JEFF_PATH = "/external/jeff";

    // Local server URLs
    private String localUrl;
    private String localDaveUrl;

    // Fixtures
    private String daveResponse;
    private String ericResponse;
    private String jeffResponse;
    // The response from calling all the APIs and filtering them by the cheapest supplier.
    private String filteredApiResponse;
    // The response from calling all the APIs and filtering them by the cheapest supplier and number of passengers
    private String filteredApiResponseByPassengers;
    private String daveOptionList;
    private String daveOptionListFilteredByPassengers;

    private final TestRestTemplate template = new TestRestTemplate();

    private HttpHeaders headers = new HttpHeaders();

    private WireMockServer mockServer;

    @Before
    public void setup() throws IOException {

        // Initialise local URLs
        this.localUrl = "http://localhost:" + port + "/ride";
        this.localDaveUrl = localUrl + "/" + DAVE_PATH;

        // Initialise fixtures
        String daveFixturePath =
                new File("src/test/java/com/tech/rideways/controllers/fixtures/dave_api_response.json").getAbsolutePath();
        daveResponse = Files.readAllLines(Paths.get(daveFixturePath), StandardCharsets.UTF_8)
                .stream()
                .collect(Collectors.joining());

        String ericFixturePath =
                new File("src/test/java/com/tech/rideways/controllers/fixtures/eric_api_response.json").getAbsolutePath();
        ericResponse = Files.readAllLines(Paths.get(ericFixturePath), StandardCharsets.UTF_8)
                .stream()
                .collect(Collectors.joining());

        String jeffFixturePath =
                new File("src/test/java/com/tech/rideways/controllers/fixtures/jeff_api_response.json").getAbsolutePath();
        jeffResponse = Files.readAllLines(Paths.get(jeffFixturePath), StandardCharsets.UTF_8)
                .stream()
                .collect(Collectors.joining());

        String filteredApiFixturePath =
                new File("src/test/java/com/tech/rideways/controllers/fixtures/filtered_api_response.json").getAbsolutePath();
        filteredApiResponse = Files.readAllLines(Paths.get(filteredApiFixturePath), StandardCharsets.UTF_8)
                .stream()
                .collect(Collectors.joining());
        String daveOptionListFixturePath =
                new File("src/test/java/com/tech/rideways/controllers/fixtures/dave_option_list.json").getAbsolutePath();
        daveOptionList = Files.readAllLines(Paths.get(daveOptionListFixturePath), StandardCharsets.UTF_8)
                .stream()
                .collect(Collectors.joining());
        String daveOptionListFilteredByPassengersFixturePath =
                new File("src/test/java/com/tech/rideways/controllers/fixtures/dave_option_list_filtered_by_passengers.json").getAbsolutePath();
        daveOptionListFilteredByPassengers = Files.readAllLines(Paths.get(daveOptionListFilteredByPassengersFixturePath), StandardCharsets.UTF_8)
                .stream()
                .collect(Collectors.joining());
        String filteredApiResponseByPassengersFixturePath =
                new File("src/test/java/com/tech/rideways/controllers/fixtures/filtered_api_response_by_passengers.json").getAbsolutePath();
        filteredApiResponseByPassengers = Files.readAllLines(Paths.get(filteredApiResponseByPassengersFixturePath), StandardCharsets.UTF_8)
                .stream()
                .collect(Collectors.joining());

        // Set accepted media type
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        // Initialise mock server
        mockServer = new WireMockServer(options().port(wiremockPort));

        mockServer.start();
    }

    @Before
    public void resetWiremockServer() {
        mockServer.resetAll();
    }

    @After
    public void stopWiremockServer() {
        mockServer.stop();
    }

    // TESTING /ride

    @Test
    public void getRideIndex_whenInternalServerError_then200IsReturned() {

        // arrange
        stubExternalApi_InternalServerError();

        HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);
        String url = localUrl + "?pickup=" + PICK_UP + "&dropoff=" + DROP_OFF;

        // act
        ResponseEntity<String> response = template.exchange(url, HttpMethod.GET, httpEntity, String.class);

        // assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void getRideIndex_whenInternalServerError_thenMediaTypeIsJSON() {

        // arrange
        stubExternalApi_InternalServerError();

        HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);
        String url = localUrl + "?pickup=" + PICK_UP + "&dropoff=" + DROP_OFF;

        // act
        ResponseEntity<String> response = template.exchange(url, HttpMethod.GET, httpEntity, String.class);

        // assert
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
    }

    @Test
    public void getRideIndex_whenInternalServerError_thenContentIsEmptyList() throws JSONException {

        // arrange
        stubExternalApi_InternalServerError();

        HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);
        String url = localUrl + "?pickup=" + PICK_UP + "&dropoff=" + DROP_OFF;

        // act
        ResponseEntity<String> response = template.exchange(url, HttpMethod.GET, httpEntity, String.class);

        // assert
        JSONAssert.assertEquals("[]", response.getBody(), true);
    }

    @Test
    public void getRideIndex_whenRequestTimeout_then200IsReturned() {

        // arrange
        stubExternalApi_RequestTimeout();

        HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);
        String url = localUrl + "?pickup=" + PICK_UP + "&dropoff=" + DROP_OFF;

        // act
        ResponseEntity<String> response = template.exchange(url, HttpMethod.GET, httpEntity, String.class);

        // assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void getRideIndex_whenRequestTimeout_thenMediaTypeIsJSON() {

        // arrange
        stubExternalApi_RequestTimeout();

        HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);
        String url = localUrl + "?pickup=" + PICK_UP + "&dropoff=" + DROP_OFF;

        // act
        ResponseEntity<String> response = template.exchange(url, HttpMethod.GET, httpEntity, String.class);

        // assert
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
    }

    @Test
    public void getRideIndex_whenRequestTimeout_thenContentIsEmptyList() throws JSONException {

        // arrange
        stubExternalApi_RequestTimeout();

        HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);
        String url = localUrl + "?pickup=" + PICK_UP + "&dropoff=" + DROP_OFF;

        // act
        ResponseEntity<String> response = template.exchange(url, HttpMethod.GET, httpEntity, String.class);

        // assert
        JSONAssert.assertEquals("[]", response.getBody(), true);
    }

    @Test
    public void getRideIndex_whenMissingParameter_thenBadRequestIsReturned() {

        // arrange
        HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);
        String url = localUrl + "?pickup=" + PICK_UP;

        // act
        ResponseEntity<String> response = template.exchange(url, HttpMethod.GET, httpEntity, String.class);

        // assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void getRideIndex_whenMissingParameter_thenMediaTypeIsJSON() {

        // arrange
        HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);
        String url = localUrl + "?pickup=" + PICK_UP;

        // act
        ResponseEntity<String> response = template.exchange(url, HttpMethod.GET, httpEntity, String.class);

        // assert
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
    }

    @Test
    public void getRideIndex_whenInvalidParameter_thenBadRequestIsReturned() {

        // arrange
        HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);
        String url = localUrl + "?pickup=" + PICK_UP + "&dropoff=50";

        // act
        ResponseEntity<String> response = template.exchange(url, HttpMethod.GET, httpEntity, String.class);

        // assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void getRideIndex_whenInvalidParameter_thenMediaTypeIsJSON() {

        // arrange
        HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);
        String url = localUrl + "?pickup=" + PICK_UP + "&dropoff=50";

        // act
        ResponseEntity<String> response = template.exchange(url, HttpMethod.GET, httpEntity, String.class);

        // assert
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
    }

    @Test
    public void getRideIndex_whenSuccessfulRequest_then200isReturned() {

        // arrange
        stubExternalApi_ValidResponse();

        HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);
        String url = localUrl + "?pickup=" + PICK_UP + "&dropoff=" + DROP_OFF;

        // act
        ResponseEntity<String> response = template.exchange(url, HttpMethod.GET, httpEntity, String.class);

        // assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void getRideIndex_whenSuccessfulRequest_thenMediaTypeIsJSON() {

        // arrange
        stubExternalApi_ValidResponse();

        HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);
        String url = localUrl + "?pickup=" + PICK_UP + "&dropoff=" + DROP_OFF;

        // act
        ResponseEntity<String> response = template.exchange(url, HttpMethod.GET, httpEntity, String.class);

        // assert
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
    }

    @Test
    public void getRideIndex_whenSuccessfulRequest_thenContentReturnedIsValid() throws JSONException {

        // arrange
        stubExternalApi_ValidResponse();

        HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);
        String url = localUrl + "?pickup=" + PICK_UP + "&dropoff=" + DROP_OFF;

        // act
        ResponseEntity<String> response = template.exchange(url, HttpMethod.GET, httpEntity, String.class);

        // assert
        JSONAssert.assertEquals(filteredApiResponse, response.getBody(), true);
    }

    @Test
    public void getRideIndex_whenPassengersNoIncludedAndSuccessfulRequest_then200isReturned() {

        // arrange
        stubExternalApi_ValidResponse();

        HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);
        String url = localUrl + "?pickup=" + PICK_UP + "&dropoff=" + DROP_OFF + "&passengers=7";

        // act
        ResponseEntity<String> response = template.exchange(url, HttpMethod.GET, httpEntity, String.class);

        // assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void getRideIndex_whenPassengersNoIncludedAndSuccessfulRequest_thenMediaTypeIsJSON() {

        // arrange
        stubExternalApi_ValidResponse();

        HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);
        String url = localUrl + "?pickup=" + PICK_UP + "&dropoff=" + DROP_OFF + "&passengers=7";

        // act
        ResponseEntity<String> response = template.exchange(url, HttpMethod.GET, httpEntity, String.class);

        // assert
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
    }

    @Test
    public void getRideIndex_whenPassengersNoIncludedAndSuccessfulRequest_thenContentReturnedIsValid()
            throws JSONException {

        // arrange
        stubExternalApi_ValidResponse();

        HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);
        String url = localUrl + "?pickup=" + PICK_UP + "&dropoff=" + DROP_OFF + "&passengers=7";

        // act
        ResponseEntity<String> response = template.exchange(url, HttpMethod.GET, httpEntity, String.class);

        // assert
        JSONAssert.assertEquals(filteredApiResponseByPassengers, response.getBody(), true);
    }

    // TESTING /ride/{supplier} -- using dave's API for testing

    @Test
    public void getRideSupplierIndex_whenInternalServerError_then200IsReturned() {

        // arrange
        stubDaveExternalApi_InternalServerError();

        HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);
        String url = localDaveUrl + "?pickup=" + PICK_UP + "&dropoff=" + DROP_OFF;

        // act
        ResponseEntity<String> response = template.exchange(url, HttpMethod.GET, httpEntity, String.class);

        // assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void getRideSupplierIndex_whenInternalServerError_thenMediaTypeIsJSON() {

        // arrange
        stubDaveExternalApi_InternalServerError();

        HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);
        String url = localDaveUrl + "?pickup=" + PICK_UP + "&dropoff=" + DROP_OFF;

        // act
        ResponseEntity<String> response = template.exchange(url, HttpMethod.GET, httpEntity, String.class);

        // assert
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
    }

    @Test
    public void getRideSupplierIndex_whenInternalServerError_thenContentIsEmptyList() throws JSONException {

        // arrange
        stubDaveExternalApi_InternalServerError();

        HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);
        String url = localDaveUrl + "?pickup=" + PICK_UP + "&dropoff=" + DROP_OFF;

        // act
        ResponseEntity<String> response = template.exchange(url, HttpMethod.GET, httpEntity, String.class);

        // assert
        JSONAssert.assertEquals("[]", response.getBody(), true);
    }

    @Test
    public void getRideSupplierIndex_whenRequestTimeout_then200IsReturned() {

        // arrange
        stubDaveExternalApi_RequestTimeout();

        HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);
        String url = localDaveUrl + "?pickup=" + PICK_UP + "&dropoff=" + DROP_OFF;

        // act
        ResponseEntity<String> response = template.exchange(url, HttpMethod.GET, httpEntity, String.class);

        // assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void getRideSupplierIndex_whenRequestTimeout_thenMediaTypeIsJSON() {

        // arrange
        stubDaveExternalApi_RequestTimeout();

        HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);
        String url = localDaveUrl + "?pickup=" + PICK_UP + "&dropoff=" + DROP_OFF;

        // act
        ResponseEntity<String> response = template.exchange(url, HttpMethod.GET, httpEntity, String.class);

        // assert
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
    }

    @Test
    public void getRideSupplierIndex_whenRequestTimeout_thenContentIsEmptyList() throws JSONException {

        // arrange
        stubDaveExternalApi_RequestTimeout();

        HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);
        String url = localDaveUrl + "?pickup=" + PICK_UP + "&dropoff=" + DROP_OFF;

        // act
        ResponseEntity<String> response = template.exchange(url, HttpMethod.GET, httpEntity, String.class);

        // assert
        JSONAssert.assertEquals("[]", response.getBody(), true);
    }

    @Test
    public void getRideSupplierIndex_whenMissingParameter_thenBadRequestIsReturned() {

        // arrange
        HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);
        String url = localDaveUrl + "?pickup=" + PICK_UP;

        // act
        ResponseEntity<String> response = template.exchange(url, HttpMethod.GET, httpEntity, String.class);

        // assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void getRideSupplierIndex_whenMissingParameter_thenMediaTypeIsJSON() {

        // arrange
        HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);
        String url = localDaveUrl + "?pickup=" + PICK_UP;

        // act
        ResponseEntity<String> response = template.exchange(url, HttpMethod.GET, httpEntity, String.class);

        // assert
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
    }

    @Test
    public void getRideSupplierIndex_whenInvalidParameter_thenBadRequestIsReturned() {

        // arrange
        HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);
        String url = localDaveUrl + "?pickup=" + PICK_UP + "&dropoff=50";

        // act
        ResponseEntity<String> response = template.exchange(url, HttpMethod.GET, httpEntity, String.class);

        // assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void getRideSupplierIndex_whenInvalidParameter_thenMediaTypeIsJSON() {

        // arrange
        HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);
        String url = localDaveUrl + "?pickup=" + PICK_UP + "&dropoff=50";

        // act
        ResponseEntity<String> response = template.exchange(url, HttpMethod.GET, httpEntity, String.class);

        // assert
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
    }

    @Test
    public void getRideSupplierIndex_whenSuccessfulRequest_then200isReturned() {

        // arrange
        stubDaveExternalApi_ValidResponse();

        HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);
        String url = localDaveUrl + "?pickup=" + PICK_UP + "&dropoff=" + DROP_OFF;

        // act
        ResponseEntity<String> response = template.exchange(url, HttpMethod.GET, httpEntity, String.class);

        // assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void getRideSupplierIndex_whenSuccessfulRequest_thenMediaTypeIsJSON() {

        // arrange
        stubDaveExternalApi_ValidResponse();

        HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);
        String url = localDaveUrl + "?pickup=" + PICK_UP + "&dropoff=" + DROP_OFF;

        // act
        ResponseEntity<String> response = template.exchange(url, HttpMethod.GET, httpEntity, String.class);

        // assert
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
    }

    @Test
    public void getRideSupplierIndex_whenSuccessfulRequest_thenContentReturnedIsValid() throws JSONException {

        // arrange
        stubDaveExternalApi_ValidResponse();

        HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);
        String url = localDaveUrl + "?pickup=" + PICK_UP + "&dropoff=" + DROP_OFF;

        // act
        ResponseEntity<String> response = template.exchange(url, HttpMethod.GET, httpEntity, String.class);

        // assert
        JSONAssert.assertEquals(daveOptionList, response.getBody(), true);
    }

    @Test
    public void getRideSupplierIndex_whenPassengersNoIncludedAndSuccessfulRequest_then200isReturned() {

        // arrange
        stubDaveExternalApi_ValidResponse();

        HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);
        String url = localDaveUrl + "?pickup=" + PICK_UP + "&dropoff=" + DROP_OFF + "&passengers=7";

        // act
        ResponseEntity<String> response = template.exchange(url, HttpMethod.GET, httpEntity, String.class);

        // assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void getRideSupplierIndex_whenPassengersNoIncludedAndSuccessfulRequest_thenMediaTypeIsJSON() {

        // arrange
        stubDaveExternalApi_ValidResponse();

        HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);
        String url = localDaveUrl + "?pickup=" + PICK_UP + "&dropoff=" + DROP_OFF + "&passengers=7";

        // act
        ResponseEntity<String> response = template.exchange(url, HttpMethod.GET, httpEntity, String.class);

        // assert
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
    }

    @Test
    public void getRideSupplierIndex_whenPassengersNoIncludedAndSuccessfulRequest_thenContentReturnedIsValid()
            throws JSONException {

        // arrange
        stubDaveExternalApi_ValidResponse();

        HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);
        String url = localDaveUrl + "?pickup=" + PICK_UP + "&dropoff=" + DROP_OFF + "&passengers=7";

        // act
        ResponseEntity<String> response = template.exchange(url, HttpMethod.GET, httpEntity, String.class);

        // assert
        JSONAssert.assertEquals(daveOptionListFilteredByPassengers, response.getBody(), true);
    }

    // METHODS FOR STUBBING.

    private void stubExternalApi_InternalServerError() {

        mockServer.stubFor(get(EXTERNAL_DAVE_PATH + "?pickup=" + PICK_UP + "&dropoff=" + DROP_OFF)
                .willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));
        mockServer.stubFor(get(EXTERNAL_ERIC_PATH + "?pickup=" + PICK_UP + "&dropoff=" + DROP_OFF)
                .willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));
        mockServer.stubFor(get(EXTERNAL_JEFF_PATH + "?pickup=" + PICK_UP + "&dropoff=" + DROP_OFF)
                .willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));

        mockServer.stubFor(get(EXTERNAL_DAVE_PATH + "?dropoff=" + DROP_OFF + "&pickup=" + PICK_UP)
                .willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));
        mockServer.stubFor(get(EXTERNAL_ERIC_PATH + "?dropoff=" + DROP_OFF + "&pickup=" + PICK_UP)
                .willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));
        mockServer.stubFor(get(EXTERNAL_JEFF_PATH + "?dropoff=" + DROP_OFF + "&pickup=" + PICK_UP)
                .willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));
    }

    private void stubExternalApi_RequestTimeout() {

        mockServer.stubFor(get(EXTERNAL_DAVE_PATH + "?pickup=" + PICK_UP + "&dropoff=" + DROP_OFF)
                .willReturn(aResponse().withStatus(HttpStatus.OK.value()).withFixedDelay(2000)));
        mockServer.stubFor(get(EXTERNAL_ERIC_PATH + "?pickup=" + PICK_UP + "&dropoff=" + DROP_OFF)
                .willReturn(aResponse().withStatus(HttpStatus.OK.value()).withFixedDelay(2000)));
        mockServer.stubFor(get(EXTERNAL_JEFF_PATH + "?pickup=" + PICK_UP + "&dropoff=" + DROP_OFF)
                .willReturn(aResponse().withStatus(HttpStatus.OK.value()).withFixedDelay(2000)));

        mockServer.stubFor(get(EXTERNAL_DAVE_PATH + "?dropoff=" + DROP_OFF + "&pickup=" + PICK_UP)
                .willReturn(aResponse().withStatus(HttpStatus.OK.value()).withFixedDelay(2000)));
        mockServer.stubFor(get(EXTERNAL_ERIC_PATH + "?dropoff=" + DROP_OFF + "&pickup=" + PICK_UP)
                .willReturn(aResponse().withStatus(HttpStatus.OK.value()).withFixedDelay(2000)));
        mockServer.stubFor(get(EXTERNAL_JEFF_PATH + "?dropoff=" + DROP_OFF + "&pickup=" + PICK_UP)
                .willReturn(aResponse().withStatus(HttpStatus.OK.value()).withFixedDelay(2000)));
    }

    public void stubExternalApi_ValidResponse() {
        mockServer.stubFor(get(EXTERNAL_DAVE_PATH + "?pickup=" + PICK_UP + "&dropoff=" + DROP_OFF)
                .willReturn(aResponse().withStatus(HttpStatus.OK.value()).withBody(daveResponse)));
        mockServer.stubFor(get(EXTERNAL_ERIC_PATH + "?pickup=" + PICK_UP + "&dropoff=" + DROP_OFF)
                .willReturn(aResponse().withStatus(HttpStatus.OK.value()).withBody(ericResponse)));
        mockServer.stubFor(get(EXTERNAL_JEFF_PATH + "?pickup=" + PICK_UP + "&dropoff=" + DROP_OFF)
                .willReturn(aResponse().withStatus(HttpStatus.OK.value()).withBody(jeffResponse)));

        mockServer.stubFor(get(EXTERNAL_DAVE_PATH + "?dropoff=" + DROP_OFF + "&pickup=" + PICK_UP)
                .willReturn(aResponse().withStatus(HttpStatus.OK.value()).withBody(daveResponse)));
        mockServer.stubFor(get(EXTERNAL_ERIC_PATH + "?dropoff=" + DROP_OFF + "&pickup=" + PICK_UP)
                .willReturn(aResponse().withStatus(HttpStatus.OK.value()).withBody(ericResponse)));
        mockServer.stubFor(get(EXTERNAL_JEFF_PATH + "?dropoff=" + DROP_OFF + "&pickup=" + PICK_UP)
                .willReturn(aResponse().withStatus(HttpStatus.OK.value()).withBody(jeffResponse)));
    }

    private void stubDaveExternalApi_InternalServerError() {

        mockServer.stubFor(get(EXTERNAL_DAVE_PATH + "?pickup=" + PICK_UP + "&dropoff=" + DROP_OFF)
                .willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));

        mockServer.stubFor(get(EXTERNAL_DAVE_PATH + "?dropoff=" + DROP_OFF + "&pickup=" + PICK_UP)
                .willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));
    }

    private void stubDaveExternalApi_RequestTimeout() {

        mockServer.stubFor(get(EXTERNAL_DAVE_PATH + "?pickup=" + PICK_UP + "&dropoff=" + DROP_OFF)
                .willReturn(aResponse().withStatus(HttpStatus.OK.value()).withFixedDelay(2000)));

        mockServer.stubFor(get(EXTERNAL_DAVE_PATH + "?dropoff=" + DROP_OFF + "&pickup=" + PICK_UP)
                .willReturn(aResponse().withStatus(HttpStatus.OK.value()).withFixedDelay(2000)));
    }

    public void stubDaveExternalApi_ValidResponse() {
        mockServer.stubFor(get(EXTERNAL_DAVE_PATH + "?pickup=" + PICK_UP + "&dropoff=" + DROP_OFF)
                .willReturn(aResponse().withStatus(HttpStatus.OK.value()).withBody(daveResponse)));

        mockServer.stubFor(get(EXTERNAL_DAVE_PATH + "?dropoff=" + DROP_OFF + "&pickup=" + PICK_UP)
                .willReturn(aResponse().withStatus(HttpStatus.OK.value()).withBody(daveResponse)));
    }
}

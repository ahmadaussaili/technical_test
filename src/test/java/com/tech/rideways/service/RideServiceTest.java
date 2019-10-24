package com.tech.rideways.service;

import com.tech.rideways.entities.Option;
import com.tech.rideways.entities.Ride;
import com.tech.rideways.service.utils.Client;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

public class RideServiceTest {

    @Mock
    private Client client;

    @Spy
    @InjectMocks
    private RideService rideService;

    private static final String TAXI_API_URL = "https://techtest.rideways.com";
    private static final String DAVE_PATH = "dave";
    private static final String ERIC_PATH = "eric";
    private static final String JEFF_PATH = "jeff";
    private static final String pickup = "51.470020,-0.454295";
    private static final String dropoff = "53.470020,-0.454295";

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void findRide_whenExternalApiUnavailable_thenReturnEmptyRide() {
        // arrange
        when(client.get(TAXI_API_URL, DAVE_PATH, rideService.buildParamsMap(pickup, dropoff)))
                .thenReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(""));

        // act
        Ride ride = rideService.findRide(DAVE_PATH, pickup, dropoff);

        // assert
        assertNull(ride.getSupplierId());
        assertNull(ride.getPickup());
        assertNull(ride.getDropoff());
        assertThat(ride.getOptions(), is(empty()));
    }

    @Test
    public void findRide_whenExternalApiRequestTimeouts_thenReturnEmptyRide() {
        // arrange
        when(client.get(TAXI_API_URL, DAVE_PATH, rideService.buildParamsMap(pickup, dropoff)))
                .thenReturn(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(""));

        // act
        Ride ride = rideService.findRide(DAVE_PATH, pickup, dropoff);

        // assert
        assertNull(ride.getSupplierId());
        assertNull(ride.getPickup());
        assertNull(ride.getDropoff());
        assertThat(ride.getOptions(), is(empty()));
    }

    @Test
    public void findRide_whenResponsePayloadIsInvalid_thenReturnEmptyRide() {
        // arrange
        when(client.get(TAXI_API_URL, DAVE_PATH, rideService.buildParamsMap(pickup, dropoff)))
                .thenReturn(ResponseEntity.ok("Invalid Payload"));

        // act
        Ride ride = rideService.findRide(DAVE_PATH, pickup, dropoff);

        // assert
        assertNull(ride.getSupplierId());
        assertNull(ride.getPickup());
        assertNull(ride.getDropoff());
        assertThat(ride.getOptions(), is(empty()));
    }

    @Test
    public void findRide_whenExternalApiRequestIsSuccessful_thenReturnRide() throws IOException {
        // arrange
        String fixturePath =
                new File("src/test/java/com/tech/rideways/service/fixtures/ride_response_payload.json").getAbsolutePath();
        String responsePayload = Files.readAllLines(Paths.get(fixturePath), StandardCharsets.UTF_8)
                                    .stream()
                                    .collect(Collectors.joining());

        when(client.get(TAXI_API_URL, DAVE_PATH, rideService.buildParamsMap(pickup, dropoff)))
                .thenReturn(ResponseEntity.ok(responsePayload));

        // act
        Ride ride = rideService.findRide(DAVE_PATH, pickup, dropoff);

        // assert
        assertEquals(DAVE_PATH, ride.getSupplierId());
        assertEquals(pickup, ride.getPickup());
        assertEquals(dropoff, ride.getDropoff());

        assertEquals(1, ride.getOptions().size());
        Option option = ride.getOptions().get(0);
        assertEquals("EXECUTIVE", option.getCarType());
        assertEquals(279000, option.getPrice());
    }

    @Test
    public void findOptionsWithoutPassengerConstraint_whenEmptyRide_thenReturnEmptyOptionList() {
        // arrange
        Ride emptyRide = new Ride();

        doReturn(emptyRide).when(rideService).findRide(DAVE_PATH, pickup, dropoff);

        // act
        List<Option> options =
                rideService.findOptions(DAVE_PATH, pickup, dropoff, null);

        // assert
        assertThat(options, is(empty()));

        // verify
        verify(rideService, times(1)).findRide(DAVE_PATH, pickup, dropoff);
    }

    @Test
    public void findOptionsWithPassengerConstraint_whenEmptyRide_thenReturnEmptyOptionList() {
        // arrange
        Ride emptyRide = new Ride();

        doReturn(emptyRide).when(rideService).findRide(DAVE_PATH, pickup, dropoff);

        // act
        List<Option> options =
                rideService.findOptions(DAVE_PATH, pickup, dropoff, 4);

        // assert
        assertThat(options, is(empty()));

        // verify
        verify(rideService, times(1)).findRide(DAVE_PATH, pickup, dropoff);
    }

    @Test
    public void findOptionsWithoutPassengerConstraint_whenNonEmptyRide_thenReturnOptionList() {
        // arrange
        Ride ride = new Ride(DAVE_PATH, pickup, dropoff);

        List<Option> rideOptions = new ArrayList<>();
        String carType = "EXECUTIVE";
        int price = 279000;
        Option rideOption = new Option(carType, price);
        rideOptions.add(rideOption);
        ride.setOptions(rideOptions);

        doReturn(ride).when(rideService).findRide(DAVE_PATH, pickup, dropoff);

        // act
        List<Option> options =
                rideService.findOptions(DAVE_PATH, pickup, dropoff, null);
        Option option = options.get(0);

        // assert
        assertFalse(options.isEmpty());
        assertEquals(carType, option.getCarType());
        assertEquals(price, option.getPrice());

        // verify
        verify(rideService, times(1)).findRide(DAVE_PATH, pickup, dropoff);
    }

    @Test
    public void findOptionsWithPassengerConstraint_whenNonEmptyRide_thenReturnFilteredOptionList() throws IOException {
        // arrange
        Ride ride = new Ride();
        ride.setSupplierId(DAVE_PATH);
        ride.setPickup(pickup);
        ride.setDropoff(dropoff);

        List<Option> rideOptions = new ArrayList<>();
        Option rideOption1 = new Option("EXECUTIVE", 279000);
        Option rideOption2 = new Option("PEOPLE_CARRIER", 329000);
        rideOptions.add(rideOption1);
        rideOptions.add(rideOption2);
        ride.setOptions(rideOptions);

        int passengersNo = 6;

        doReturn(ride).when(rideService).findRide(DAVE_PATH, pickup, dropoff);

        // act
        List<Option> options =
                rideService.findOptions(DAVE_PATH, pickup, dropoff, passengersNo);
        Option option = options.get(0); // the option with the EXECUTIVE car should be filtered (max passengers = 4)

        // assert
        assertFalse(options.isEmpty());
        assertTrue(options.size() == 1);
        assertEquals("PEOPLE_CARRIER", option.getCarType());
        assertEquals(329000, option.getPrice());

        // verify
        verify(rideService, times(1)).findRide(DAVE_PATH, pickup, dropoff);
    }

    @Test
    public void findOptionsByPriceDescending_whenEmptyOptionList_thenReturnEmptyOptionList() {
        // arrange
        List<Option> emptyOptions = new ArrayList<>();

        doReturn(emptyOptions).when(rideService).findOptions(DAVE_PATH, pickup, dropoff, null);

        // act
        List<Option> options =
                rideService.findOptionsByPriceDescending(DAVE_PATH, pickup, dropoff, null);

        // assert
        assertThat(options, is(empty()));

        // verify
        verify(rideService, times(1)).findOptions(DAVE_PATH, pickup, dropoff, null);
    }

    @Test
    public void findOptionsByPriceDescending_whenNonEmptyOptionList_thenReturnSortedOptionList() {
        // arrange
        List<Option> rideOptions = new ArrayList<>();
        Option rideOption1 = new Option("EXECUTIVE", 279000);
        Option rideOption2 = new Option("PEOPLE_CARRIER", 329000);
        rideOptions.add(rideOption1);
        rideOptions.add(rideOption2);

        doReturn(rideOptions).when(rideService).findOptions(DAVE_PATH, pickup, dropoff, null);

        // act
        List<Option> options =
                rideService.findOptionsByPriceDescending(DAVE_PATH, pickup, dropoff, null);

        // assert
        assertFalse(options.isEmpty());
        assertEquals("PEOPLE_CARRIER", options.get(0).getCarType());
        assertEquals(329000, options.get(0).getPrice());
        assertEquals("EXECUTIVE", options.get(1).getCarType());
        assertEquals(279000, options.get(1).getPrice());

        // verify
        verify(rideService, times(1)).findOptions(DAVE_PATH, pickup, dropoff, null);
    }

    @Test
    public void findOptionsByCarTypeWithCheapestSupplier_whenEmptyOptionList_thenReturnEmptyOptionList()
            throws IOException{
        // arrange
        List<Option> emptyOptions = new ArrayList<>();

        doReturn(emptyOptions).when(rideService).findOptions(DAVE_PATH, pickup, dropoff, null);
        doReturn(emptyOptions).when(rideService).findOptions(ERIC_PATH, pickup, dropoff, null);
        doReturn(emptyOptions).when(rideService).findOptions(JEFF_PATH, pickup, dropoff, null);

        // act
        List<Option> options =
                rideService.findOptionsByCarTypeWithCheapestSupplier(pickup, dropoff, null);

        // assert
        assertThat(options, is(empty()));

        // verify
        verify(rideService, times(1)).findOptions(DAVE_PATH, pickup, dropoff, null);
        verify(rideService, times(1)).findOptions(ERIC_PATH, pickup, dropoff, null);
        verify(rideService, times(1)).findOptions(JEFF_PATH, pickup, dropoff, null);
    }

    @Test
    public void findOptionsByCarTypeWithCheapestSupplier_whenNonEmptyOptionList_thenReturnFilteredOptionList()
            throws IOException{
        // arrange
        List<Option> daveOptions = new ArrayList<>();
        Option daveOption1 = new Option("STANDARD", 392299);
        Option daveOption2 = new Option("LUXURY", 810777);
        Option daveOption3 = new Option("PEOPLE_CARRIER", 616051);
        Option daveOption4 = new Option("MINIBUS", 665975);
        daveOptions.add(daveOption1);
        daveOptions.add(daveOption2);
        daveOptions.add(daveOption3);
        daveOptions.add(daveOption4);
        for (Option option : daveOptions) {
            option.setSupplier(DAVE_PATH);
        }

        List<Option> ericOptions = new ArrayList<>();
        Option ericOption1 = new Option("EXECUTIVE", 272905);
        Option ericOption2 = new Option("LUXURY", 235868);
        Option ericOption3 = new Option("PEOPLE_CARRIER", 387596);
        Option ericOption4 = new Option("LUXURY_PEOPLE_CARRIER", 695553);
        Option ericOption5 = new Option("MINIBUS", 103801);
        ericOptions.add(ericOption1);
        ericOptions.add(ericOption2);
        ericOptions.add(ericOption3);
        ericOptions.add(ericOption4);
        ericOptions.add(ericOption5);
        for (Option option : ericOptions) {
            option.setSupplier(ERIC_PATH);
        }

        List<Option> jeffOptions = new ArrayList<>();
        Option jeffOption1 = new Option("MINIBUS", 113801);
        jeffOptions.add(jeffOption1);
        for (Option option : jeffOptions) {
            option.setSupplier(JEFF_PATH);
        }


        doReturn(daveOptions).when(rideService).findOptions(DAVE_PATH, pickup, dropoff, null);
        doReturn(ericOptions).when(rideService).findOptions(ERIC_PATH, pickup, dropoff, null);
        doReturn(jeffOptions).when(rideService).findOptions(JEFF_PATH, pickup, dropoff, null);

        // act
        List<Option> options =
                rideService.findOptionsByCarTypeWithCheapestSupplier(pickup, dropoff, null);

        // assert
        assertFalse(options.isEmpty());
        assertEquals(ERIC_PATH, options.get(0).getSupplier());
        assertEquals("LUXURY_PEOPLE_CARRIER", options.get(0).getCarType());
        assertEquals(695553, options.get(0).getPrice());
        assertEquals(DAVE_PATH, options.get(1).getSupplier());
        assertEquals("STANDARD", options.get(1).getCarType());
        assertEquals(392299, options.get(1).getPrice());
        assertEquals(ERIC_PATH, options.get(2).getSupplier());
        assertEquals("PEOPLE_CARRIER", options.get(2).getCarType());
        assertEquals(387596, options.get(2).getPrice());
        assertEquals(ERIC_PATH, options.get(3).getSupplier());
        assertEquals("EXECUTIVE", options.get(3).getCarType());
        assertEquals(272905, options.get(3).getPrice());
        assertEquals(ERIC_PATH, options.get(4).getSupplier());
        assertEquals("LUXURY", options.get(4).getCarType());
        assertEquals(235868, options.get(4).getPrice());
        assertEquals(ERIC_PATH, options.get(5).getSupplier());
        assertEquals("MINIBUS", options.get(5).getCarType());
        assertEquals(103801, options.get(5).getPrice());

        // verify
        verify(rideService, times(1)).findOptions(DAVE_PATH, pickup, dropoff, null);
        verify(rideService, times(1)).findOptions(ERIC_PATH, pickup, dropoff, null);
        verify(rideService, times(1)).findOptions(JEFF_PATH, pickup, dropoff, null);
    }
}

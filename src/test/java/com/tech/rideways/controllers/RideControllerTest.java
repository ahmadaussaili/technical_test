package com.tech.rideways.controllers;

import com.tech.rideways.entities.Option;
import com.tech.rideways.service.RideService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@DirtiesContext
@ActiveProfiles("test")
public class RideControllerTest {

    private MockMvc mvc;

    @Mock
    private RideService rideService;

    @InjectMocks
    private RideController rideController;

    private static final String pickup = "51.470020,-0.454295";
    private static final String dropoff = "53.470020,-0.454295";
    private static final String DAVE_PATH = "dave";
    private static final String ERIC_PATH = "eric";

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mvc = MockMvcBuilders.standaloneSetup(rideController).build();
    }

    @Test
    public void getRideIndexWhenEmptyOptionList() throws Exception {

        // arrange
        when(rideService.findOptionsByCarTypeWithCheapestSupplier(pickup, dropoff, null))
                .thenReturn(Collections.<Option> emptyList());

        // act
        mvc.perform(get("/ride" + "?pickup=" + pickup + "&dropoff=" + dropoff)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(handler().methodName("getRideOptions")).andExpect(jsonPath("$.length()", equalTo(0)))
                .andExpect(content().json("[]"));

        // verify
        verify(rideService).findOptionsByCarTypeWithCheapestSupplier(pickup, dropoff, null);
    }

    @Test
    public void getRideIndexWhenNonEmptyOptionList() throws Exception {

        // arrange
        Option option1 = new Option("EXECUTIVE", 270000);
        option1.setSupplier(DAVE_PATH);
        Option option2 = new Option("MINIBUS", 110000);
        option2.setSupplier(ERIC_PATH);
        when(rideService.findOptionsByCarTypeWithCheapestSupplier(pickup, dropoff, null))
                .thenReturn(Arrays.asList(option1, option2));

        // act
        mvc.perform(get("/ride" + "?pickup=" + pickup + "&dropoff=" + dropoff)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(handler().methodName("getRideOptions")).andExpect(jsonPath("$.length()", equalTo(2)))
                .andExpect(content().json("[{'supplier': 'dave', 'price': 270000, 'car_type': 'EXECUTIVE'}, "
                                          + " {'supplier': 'eric', 'price': 110000, 'car_type': 'MINIBUS'}]"));
        ;

        // verify
        verify(rideService).findOptionsByCarTypeWithCheapestSupplier(pickup, dropoff, null);
    }

    @Test
    public void getRideSupplierIndexWhenEmptyOptionList() throws Exception {

        // arrange
        when(rideService.findOptionsByPriceDescending(DAVE_PATH, pickup, dropoff, null))
                .thenReturn(Collections.<Option> emptyList());

        // act
        mvc.perform(get("/ride/" + DAVE_PATH + "?pickup=" + pickup + "&dropoff=" + dropoff)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(handler().methodName("getRideOptions")).andExpect(jsonPath("$.length()", equalTo(0)))
                .andExpect(content().json("[]"));

        // verify
        verify(rideService).findOptionsByPriceDescending(DAVE_PATH, pickup, dropoff, null);
    }

    @Test
    public void getRideSupplierIndexWhenNonEmptyOptionList() throws Exception {

        // arrange
        Option option1 = new Option("LUXURY", 974678);
        option1.setSupplier(DAVE_PATH);
        Option option2 = new Option("PEOPLE_CARRIER", 833970);
        option2.setSupplier(DAVE_PATH);

        when(rideService.findOptionsByPriceDescending(DAVE_PATH, pickup, dropoff, null))
                .thenReturn(Arrays.asList(option1, option2));

        // act
        mvc.perform(get("/ride/" + DAVE_PATH + "?pickup=" + pickup + "&dropoff=" + dropoff)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(handler().methodName("getRideOptions")).andExpect(jsonPath("$.length()", equalTo(2)))
                .andExpect(content().json("[{'price': 974678, 'car_type': 'LUXURY'}, "
                        + " {'price': 833970, 'car_type': 'PEOPLE_CARRIER'}]"));

        // verify
        verify(rideService).findOptionsByPriceDescending(DAVE_PATH, pickup, dropoff, null);
    }

}

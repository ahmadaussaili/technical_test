package com.tech.rideways.controllers;

import com.tech.rideways.entities.Option;
import com.tech.rideways.service.RideService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@Validated
public class RideController {

    @Autowired
    private RideService rideService;

    private static final String VALIDATION_REGEX = "(\\s*-?\\d+(\\.\\d+)?)(\\s*,\\s*-?\\d+(\\.\\d+)?)";

    @GetMapping("/ride")
    public ResponseEntity<List<Option>> getRideOptions(@Valid @Pattern(regexp = VALIDATION_REGEX) @RequestParam String pickup,
                                                       @Valid @Pattern(regexp = VALIDATION_REGEX) @RequestParam String dropoff,
                                                       @RequestParam(required = false) String passengers)
            throws IOException {

        Integer passengersNo = passengers != null ? Integer.parseInt(passengers) : null;

        List<Option> options = rideService.findOptionsByCarTypeWithCheapestSupplier(pickup, dropoff, passengersNo);

        return ResponseEntity.ok(options);
    }

    @GetMapping("/ride/{supplier}")
    public ResponseEntity<List<Option>> getRideOptions(@PathVariable String supplier,
                                                       @Valid @Pattern(regexp = VALIDATION_REGEX) @RequestParam String pickup,
                                                       @Valid @Pattern(regexp = VALIDATION_REGEX) @RequestParam String dropoff,
                                                       @RequestParam(required = false) String passengers) {

        Integer passengersNo = passengers != null ? Integer.parseInt(passengers) : null;
        List<Option> options = rideService.findOptionsByPriceDescending(supplier, pickup, dropoff, passengersNo);

        return ResponseEntity.ok(options);
    }

}

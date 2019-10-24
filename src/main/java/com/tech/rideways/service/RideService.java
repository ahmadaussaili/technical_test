package com.tech.rideways.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tech.rideways.entities.Option;
import com.tech.rideways.entities.Ride;
import com.tech.rideways.service.utils.Client;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RideService {

    @Value("${taxi_api}")
    private String taxi_api_url = "https://techtest.rideways.com";

    private static final String[] SUPPLIERS = {"dave", "eric", "jeff"}; // paths to suppliers
    private static final String PARAMETER_VALIDATION_REGEX = "(\\s*-?\\d+(\\.\\d+)?)(\\s*,\\s*-?\\d+(\\.\\d+)?)";

    @Autowired
    private Client client;

    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Finds the available ride options and then filters them by the cheapest supplier.
     *
     * @param pickup
     * @param dropoff
     * @param passengersNo (optional => may be null)
     * @return rideOptionsByCheapestSupplier
     */
    public List<Option> findOptionsByCarTypeWithCheapestSupplier(String pickup, String dropoff, Integer passengersNo)
            throws IOException {

        List<Option> rideOptions = new ArrayList<>();

        // Get the ride options from each supplier and add them all to a list (rideOptions).
        for (String supplier : SUPPLIERS) {
            List<Option> options = findOptions(supplier, pickup, dropoff, passengersNo);
            options.forEach(option -> {
                option.setSupplier(supplier);
            });
            rideOptions.addAll(options);
        }

        // Group the retrieved options by car type.
        Map<String, List<Option>> rideOptionsByCarType = rideOptions.stream()
                .collect(Collectors.groupingBy(Option::getCarType, Collectors.toList()));

        List<Option> rideOptionsByCheapestSupplier = new ArrayList<>();

        // Create a filtered list that contains the ride options with the cheapest supplier (lowest price)
        for (Map.Entry<String, List<Option>> entry: rideOptionsByCarType.entrySet()) {
            rideOptionsByCheapestSupplier.add(entry.getValue().stream().min(Comparator.comparing(Option::getPrice))
                    .orElseThrow(IOException::new));
        }

        // Sort the final filtered options in descending order.
        Collections.sort(rideOptionsByCheapestSupplier);

        return rideOptionsByCheapestSupplier;
    }

    /**
     * Calls the findOptions() function and returns the options in descending order
     *
     * @param supplier
     * @param pickup
     * @param dropoff
     * @param passengersNo (optional => may be null)
     * @return rideOptions
     */
    public List<Option> findOptionsByPriceDescending(String supplier, String pickup, String dropoff, Integer passengersNo) {

        List<Option> rideOptions = findOptions(supplier, pickup, dropoff, passengersNo);

        // Sort by descending order
        Collections.sort(rideOptions);

        return rideOptions;
    }

    /**
     * Finds the available ride options from the given supplier.
     *
     * @param supplier
     * @param pickup
     * @param dropoff
     * @param passengersNo (optional => may be null)
     * @return rideOptions
     */
    public List<Option> findOptions(String supplier, String pickup, String dropoff, Integer passengersNo) throws HttpClientErrorException {

        Ride ride = findRide(supplier, pickup, dropoff);
        List<Option> rideOptions = ride.getOptions();

        if (rideOptions.isEmpty()) {
            System.out.println("A problem may have occurred when trying to reach " + supplier + "'s API.");
        } else {
            // Filter options by number of passengers
            if (passengersNo != null) {
                rideOptions = rideOptions.stream()
                        .filter(option -> option.getMaxPassengers() >= passengersNo).collect(Collectors.toList());

                if (rideOptions.isEmpty()) {
                    System.out.println("There are no available rides with " + supplier + " for " + passengersNo + " passengers.");
                }
            }
        }

        return rideOptions;
    }

    /**
     * Finds an available ride from the given supplier.
     *
     * @param supplier
     * @param pickup
     * @param dropoff
     * @return a Ride object containing data if the request was successful, or an empty Ride object if the request
     * failed because of a timeout, server problem, or API break.
     */
    protected Ride findRide(String supplier, String pickup, String dropoff) {

        // Execute the request to the given supplier API.
        ResponseEntity<String> response = client.get(taxi_api_url, supplier, buildParamsMap(pickup, dropoff));

        if (response.getStatusCode().value() != HttpStatus.SC_OK) {
            // Timeout, server is down or API is broken.
            switch (response.getStatusCode().value()) {
                case HttpStatus.SC_INTERNAL_SERVER_ERROR:
                    System.out.print("Server is unavailable: ");
                    break;
                case HttpStatus.SC_REQUEST_TIMEOUT:
                    System.out.print("Request timeout: ");
                    break;
                case HttpStatus.SC_BAD_REQUEST:
                    System.out.println("Bad request, please make sure you provide both pickup and dropoff parameters.");
                    break;
                default:
                    System.out.println("Internal error.");
            }
            return new Ride();
        }

        try {
            return objectMapper.readValue(response.getBody(), Ride.class);
        }
        catch (JsonProcessingException jsonProcessingException) {
            System.out.println("Payload from " + supplier + "'s API is invalid.");
        }
        return new Ride();
    }

    /**
     * Builds a Map containing the given parameters.
     *
     * @param pickup
     * @param dropoff
     * @return paramsMap
     */
    public Map<String, String> buildParamsMap(String pickup, String dropoff) {
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("pickup", pickup);
        paramsMap.put("dropoff", dropoff);
        return paramsMap;
    }
}

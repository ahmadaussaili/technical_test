package com.tech.rideways.cli;

import com.tech.rideways.entities.Option;
import com.tech.rideways.service.RideService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

@Component
public class CLI implements CommandLineRunner {

    @Autowired
    private RideService rideService;

    private static final List<String> SUPPLIERS = Arrays.asList("dave", "eric", "jeff");
    private static final String VALIDATION_REGEX = "(\\s*-?\\d+(\\.\\d+)?)(\\s*,\\s*-?\\d+(\\.\\d+)?)";

    @Override
    public void run(String... args) {

        if (args.length == 0) {
            return;
        }

        if(args.length < 2) {
            System.out.println("You need to specify at least the pick-up and drop-off coordinates");
            return;
        }

        try {
            String supplier = null;
            String pickup;
            String dropoff;
            Integer passengersNo = null;

            // First argument: pickup coordinates (ex: 51.470020,-0.454295)
            // Second argument: dropoff coordinates (ex: 53.470020,-0.454295)
            // Third argument: number of passengers (ex: 4)

            // NOTE: if -s is present, then the first argument is the supplier.
            if (args[0].equals("-s")) {
                // First argument: supplier (dave, eric, jeff)
                // Second argument: pickup coordinates (ex: 51.470020,-0.454295)
                // Third argument: dropoff coordinates (ex: 53.470020,-0.454295)
                // Fourth argument (optional): number of passengers (ex: 4)
                supplier = args[1].toLowerCase();

                if (!SUPPLIERS.contains(supplier)) {
                    System.out.println("No such supplier");
                    return;
                }

                pickup = args[2];
                dropoff = args[3];

                // Validate parameters
                if (!parametersAreValid(pickup, dropoff)) {
                    System.out.println("Please make sure you provide valid values for both pickup and dropoff parameters.");
                    return;
                }

                passengersNo = args.length == 5 ? Integer.parseInt(args[4]) : null;

                List<Option> options
                        = rideService.findOptionsByPriceDescending(supplier, pickup, dropoff, passengersNo);

                options.forEach(option -> {
                    System.out.println(option.getCarType() + " - " + option.getPrice());
                });

            } else {

                pickup = args[0];
                dropoff = args[1];

                // Validate parameters
                if (!parametersAreValid(pickup, dropoff)) {
                    System.out.println("Please make sure you provide valid values for both pickup and dropoff parameters.");
                    return;
                }

                passengersNo = args.length == 3 ? Integer.parseInt(args[2]) : null;

                List<Option> options
                        = rideService.findOptionsByCarTypeWithCheapestSupplier(pickup, dropoff, passengersNo);

                options.forEach(option -> {
                    System.out.println(option.getCarType() + " - " + option.getSupplier() + " - " +option.getPrice());
                });
            }

        } catch (IOException e) {
            System.out.println("Error trying to filter options.");
            e.printStackTrace();
        }
    }

    private boolean parametersAreValid(String pickup, String dropoff) {
        // Validate parameters
        return pickup.matches(VALIDATION_REGEX) && dropoff.matches(VALIDATION_REGEX);
    }
}

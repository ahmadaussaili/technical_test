package com.tech.rideways.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class Ride {

    @JsonProperty("supplier_id")
    private String supplierId;

    private String pickup;

    private String dropoff;

    private List<Option> options;

    public Ride() {
        this.options = new ArrayList<>();
    }

    public Ride(String supplierId, String pickup, String dropoff) {
        this.supplierId = supplierId;
        this.pickup = pickup;
        this.dropoff = dropoff;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }

    public String getPickup() {
        return pickup;
    }

    public void setPickup(String pickup) {
        this.pickup = pickup;
    }

    public String getDropoff() {
        return dropoff;
    }

    public void setDropoff(String dropoff) {
        this.dropoff = dropoff;
    }

    public List<Option> getOptions() {
        return options;
    }

    public void setOptions(List<Option> options) {
        this.options = options;
    }

    @Override
    public String toString() {
        return "Ride{" +
                "supplierId='" + supplierId + '\'' +
                ", pickup='" + pickup + '\'' +
                ", dropoff='" + dropoff + '\'' +
                ", options=" + options +
                '}';
    }
}

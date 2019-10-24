package com.tech.rideways.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Option implements Comparable<Option> {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String supplier;

    @JsonProperty("car_type")
    private String carType;

    @JsonProperty
    private int price;

    @JsonIgnore
    private int maxPassengers;

    public Option() {
        this.supplier = null;
    }

    public Option(String carType, int price) {
        this();
        this.carType = carType;
        this.price = price;
        setMaxPassengers();
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public String getCarType() {
        return carType;
    }

    public void setCarType(String carType) {
        this.carType = carType;
        setMaxPassengers();
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    private void setMaxPassengers() {
        switch (this.carType) {
            case "STANDARD": this.maxPassengers = 4; break;
            case "EXECUTIVE": this.maxPassengers = 4; break;
            case "LUXURY": this.maxPassengers = 4; break;
            case "PEOPLE_CARRIER": this.maxPassengers = 6; break;
            case "LUXURY_PEOPLE_CARRIER": this.maxPassengers = 6; break;
            case "MINIBUS": this.maxPassengers = 16; break;
        }
    }

    public int getMaxPassengers() {
        return maxPassengers;
    }

    @Override
    public int compareTo(Option o) {
        return o.price - this.price;
    }

    @Override
    public String toString() {
        if (supplier != null) {
            return "Option{" +
                    "supplier='" + supplier + '\'' +
                    "carType='" + carType + '\'' +
                    ", price=" + price +
                    '}';
        } else {
            return "Option{" +
                    "carType='" + carType + '\'' +
                    ", price=" + price +
                    '}';
        }
    }
}

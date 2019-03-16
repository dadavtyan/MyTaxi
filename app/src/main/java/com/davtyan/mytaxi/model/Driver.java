package com.davtyan.mytaxi.model;


public class Driver {
    private String id;
    private String model;
    private String phone;
    private String color;
    private double rating;
    private double lat;
    private double lng;

    public String getId() {
        return id;
    }

    public String getModel() {
        return model;
    }

    public String getPhone() {
        return phone;
    }

    public String getColor() {
        return color;
    }

    public double getRating() {
        return rating;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public static class Rating {
        String driver_id;
        String rating;

        public Rating(String driver_id, String rating) {
            this.driver_id = driver_id;
            this.rating = rating;
        }
    }
}

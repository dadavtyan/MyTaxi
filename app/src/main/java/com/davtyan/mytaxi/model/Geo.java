package com.davtyan.mytaxi.model;


import com.yandex.mapkit.geometry.Point;

public class Geo {
    private double lat;
    private double lng;

    public Geo(Point point) {
        lat = point.getLatitude();
        lng = point.getLongitude();
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }
}


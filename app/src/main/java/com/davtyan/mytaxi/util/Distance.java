package com.davtyan.mytaxi.util;

import com.davtyan.mytaxi.model.Driver;
import com.davtyan.mytaxi.model.Geo;

import java.util.List;

public class Distance {

    private final static double AVERAGE_RADIUS_OF_EARTH = 6371000;

    public static int calculateDistance(double userLat, double userLng, double driverLat, double driverLng) {

        double latDistance = Math.toRadians(userLat - driverLat);
        double lngDistance = Math.toRadians(userLng - driverLng);

        double a = (Math.sin(latDistance / 2) * Math.sin(latDistance / 2)) +
                (Math.cos(Math.toRadians(userLat))) *
                        (Math.cos(Math.toRadians(driverLat))) *
                        (Math.sin(lngDistance / 2)) *
                        (Math.sin(lngDistance / 2));

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return (int) (Math.round(AVERAGE_RADIUS_OF_EARTH * c));

    }

    public static Driver getNearestDriver(Geo geoUser, List<Driver> drivers) {
        if (drivers.size() == 0) return null;
        long distance;
        long temp = 1000000000;
        int nearestDriverIndex = 0;
        for (int i = 0; i < drivers.size(); i++) {
            distance = calculateDistance(
                    geoUser.getLat(),
                    geoUser.getLng(),
                    drivers.get(i).getLat(),
                    drivers.get(i).getLng()
            );

            if (distance < temp){
                temp = distance;
                nearestDriverIndex = i;
                drivers.get(i).setRating(Counter.calculateRating(drivers,drivers.get(i).getPhone()));
            }
        }
        return drivers.get(nearestDriverIndex);
    }
}


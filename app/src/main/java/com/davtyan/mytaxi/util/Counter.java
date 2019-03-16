package com.davtyan.mytaxi.util;



import com.davtyan.mytaxi.model.Driver;
import com.yandex.mapkit.directions.driving.DrivingRoute;
import com.yandex.mapkit.geometry.Point;

import java.util.List;
import java.util.Locale;

public class Counter {

     static double calculateRating(List<Driver> drivers,String phone){
        int count = 0;
        double rating = 0;
        for (Driver driver: drivers) {
            if (driver.getPhone().equals(phone)){
                count++;
                rating += driver.getRating();
            }
        }

        rating /= count;
        return rating;
    }

    public static String getTime(double time) {
        if (time < 60)
            return time + " seconds";
        else if(time < 3600)
            return String.format(Locale.ENGLISH, "%.2f", time / 60) + " minutes";
        else
            return String.format(Locale.ENGLISH, "%.2f", time / 3600) + " hours";
    }

    public static String calculatePrice(double distance) {
        if (distance < 3100) return "300 AMD";
        double Price = distance / 10;
        return String.valueOf(Price) + " AMD";
    }

    public static DrivingRoute getMinRoute(List<DrivingRoute> routes){
        List<Point> points = routes.get(0).getGeometry().getPoints();
        DrivingRoute mRoute = routes.get(0);

        for (DrivingRoute route : routes) {
            if (points.size() > route.getGeometry().getPoints().size()) {
                points = route.getGeometry().getPoints();
                mRoute = route;
            }
        }

        return mRoute;
    }
}

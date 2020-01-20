package com.example.client.Classes;

import java.nio.DoubleBuffer;
import java.util.ArrayList;

public class DistanceCounter {

    private Double latitudeA;
    private Double longitudeA;
    private Double latitudeB;
    private Double longitudeB;

    private final Double R = 6371008.0;

    public DistanceCounter(Double latitudeA, Double longitudeA, Double latitudeB, Double longitudeB) {
        this.latitudeA = latitudeA;
        this.longitudeA = longitudeA;

        this.latitudeB = latitudeB;
        this.longitudeB = longitudeB;
    }

//    52.289482 104.338980
//    https://en.wikipedia.org/wiki/Great-circle_distance - формула отсюда
    public Double getDistance() {
        latitudeA = latitudeA * Math.PI / 180;
        latitudeB = latitudeB * Math.PI / 180;
        longitudeA = longitudeA * Math.PI / 180;
        longitudeB = longitudeB * Math.PI / 180;

        Double cosLatA = Math.cos(latitudeA);
        Double cosLatB = Math.cos(latitudeB);
        Double sinLatA = Math.sin(latitudeA);
        Double sinLatB = Math.sin(latitudeB);

        Double delta = Math.abs(longitudeB - longitudeA);
        Double cosDelta = Math.cos(delta);
//        Double sinDelta = Math.sin(delta);

//        Double y = Math.sqrt(Math.pow(cosLatB * sinDelta, 2) + Math.pow(cosLatA * sinLatB - sinLatA * cosLatB * cosDelta, 2));
//        Double x = sinLatA * sinLatB + cosLatA * cosLatB * cosDelta;

//        Double result = R * Math.atan2(y, x);

        Double sigma = Math.acos(sinLatA * sinLatB + cosLatA * cosLatB * cosDelta);
        Double result = sigma * R;

        return result;
    }
}

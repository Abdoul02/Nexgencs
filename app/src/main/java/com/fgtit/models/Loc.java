package com.fgtit.models;

/**
 * Created by Abdoul on 19-09-2016.
 */
public class Loc {

    double lat,lon;

    public Loc() {
    }

    public Loc(double lat, double lon){

        this.lat = lat;
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }
}
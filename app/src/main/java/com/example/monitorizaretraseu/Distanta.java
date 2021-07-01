package com.example.monitorizaretraseu;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class Distanta {
    private float distanta = 0;
    public String distanta(ArrayList<LatLng> puncte) {
        if(puncte.size() > 2) {
            Location loc1 = new Location("");
            loc1.setLatitude(puncte.get(puncte.size()-2).latitude);
            loc1.setLongitude(puncte.get(puncte.size()-2).longitude);

            Location loc2 = new Location("");
            loc2.setLatitude(puncte.get(puncte.size()-1).latitude);
            loc2.setLongitude(puncte.get(puncte.size()-1).longitude);

            distanta += loc1.distanceTo(loc2);

            return distanta + "m";
        } else {
            for(int i=0; i < puncte.size()-1; i++) {
                Location loc1 = new Location("");
                loc1.setLatitude(puncte.get(i).latitude);
                loc1.setLongitude(puncte.get(i).longitude);

                Location loc2 = new Location("");
                loc2.setLatitude(puncte.get(i+1).latitude);
                loc2.setLongitude(puncte.get(i+1).longitude);

                distanta += loc1.distanceTo(loc2);
            }
            return String.valueOf(distanta) + "m";
        }
    }
    public void reset() {
        distanta = 0;
    }
}

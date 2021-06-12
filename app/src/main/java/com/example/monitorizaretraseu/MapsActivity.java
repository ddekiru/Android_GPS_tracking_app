package com.example.monitorizaretraseu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "MapActivity";
    private static final int SOLICITA_PERMISIUNE_DE_LOCALIZARE = 1;
    public GoogleMap harta;
    private ArrayList<LatLng> puncte;
    double latitudine;
    double longitudine;
    String provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        puncte = new ArrayList<>();
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.optiuni_harta, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Change the map type based on the user's selection.
        switch (item.getItemId()) {
            case R.id.normal_map:
                harta.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                return true;
            case R.id.hybrid_map:
                harta.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                return true;
            case R.id.satellite_map:
                harta.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                return true;
            case R.id.terrain_map:
                harta.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        harta = googleMap;
        activeazaLocatia();
        if (!harta.isMyLocationEnabled())
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        harta.setMyLocationEnabled(true);

        try {
            // schimba aspectul hartii folosind fisierul JSON din res/draw/map_style.JSON
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.map_style));

            if (!success) {
                Log.e(TAG, "Nu s-a putut schimba aspectul hartii");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Nu s-a gasit fisierul. Eroare: ", e);
        }

        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        provider = lm.getBestProvider(criteria, true);

        Location myLocation = lm.getLastKnownLocation(provider);

        if (myLocation == null) {

            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
            provider = lm.getBestProvider(criteria, false);
            myLocation = lm.getLastKnownLocation(provider);
        }

        if (myLocation != null) {
            LatLng userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());

            latitudine = myLocation.getLatitude();
            longitudine = myLocation.getLongitude();

            harta.addMarker(new MarkerOptions()
                    .position(userLocation)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    .title("Salut")
                    .snippet("Latitudine:" + latitudine + ", Longitudine:" + longitudine)
            );

            Log.v(TAG, "Latitudine=" + latitudine);
            Log.v(TAG, "Longitudine=" + longitudine);

            harta.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 18), 1500, null);


            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, new LocationListener() {
                @Override
                public void onLocationChanged(Location myLocation) {

                    // latitudinea locatiei curente
                    double latitude = myLocation.getLatitude();

                    // longitudinea locatiei curente
                    double longitude = myLocation.getLongitude();

                    // creaza un obiect LatLng pentru locatia curenta
                    LatLng latLng = new LatLng(latitude, longitude);

                    // muta camera catre locatia curenta
                    harta.moveCamera(CameraUpdateFactory.newLatLng(latLng));

                    // Apropiem camera cat sa se vada strazile din jur
                    harta.animateCamera(CameraUpdateFactory.zoomTo(15));

                    // Se traseaza linia
                    traseazaLinie(latitude, longitude);

                }

                @Override
                public void onProviderDisabled(String provider) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void onProviderEnabled(String provider) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void onStatusChanged(String provider, int status,
                                            Bundle extras) {
                    // TODO Auto-generated method stub
                }
            });

        }

        harta.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        // Se adauga butoanele de control pentru zoom
        harta.getUiSettings().setZoomControlsEnabled(true);
        setMapLongClick(harta);
        setPoiClick(googleMap);
    }

    private void traseazaLinie(double latNou, double longNou) {

        // vechile valori pentru latitudine si longitudine
        puncte.add(new LatLng(latitudine, longitudine));

        // noile valori pentru latitudine si longitudine
        puncte.add(new LatLng(latNou, longNou));

        Polyline linie = harta.addPolyline(new PolylineOptions()
            .addAll(puncte)
            .width(10)
            .color(Color.RED));

        latitudine = latNou;
        longitudine = longNou;
    }

    private void setMapLongClick(final GoogleMap map) {
        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                String snippet = String.format(Locale.getDefault(),
                        "Lat: %1$.5f, Long: %2$.5f",
                        latLng.latitude,
                        latLng.longitude);

                map.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(getString(R.string.dropped_pin))
                        .snippet(snippet)
                        .icon(BitmapDescriptorFactory.defaultMarker
                                (BitmapDescriptorFactory.HUE_ORANGE)));
            }
        });
    }

    private void setPoiClick(final GoogleMap map) {
        map.setOnPoiClickListener(new GoogleMap.OnPoiClickListener() {
            @Override
            public void onPoiClick(PointOfInterest poi) {
                Marker poiMarker = harta.addMarker(new MarkerOptions()
                        .position(poi.latLng)
                        .title(poi.name)
                        .icon(BitmapDescriptorFactory.defaultMarker
                                (BitmapDescriptorFactory.HUE_ORANGE)));
                poiMarker.showInfoWindow();
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Verifica daca sunt acordate permisiunile de locatie
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case SOLICITA_PERMISIUNE_DE_LOCALIZARE:
                if (grantResults.length > 0
                        && grantResults[0]
                        == PackageManager.PERMISSION_GRANTED) {
                    activeazaLocatia();
                    break;
                }
        }
    }

    private void activeazaLocatia() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            harta.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this, new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION},
                    SOLICITA_PERMISIUNE_DE_LOCALIZARE);
        }
    }
}
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
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Locale;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "MapActivity";
    private static final int SOLICITA_PERMISIUNE_DE_LOCALIZARE = 1;
    public GoogleMap harta;
    private ArrayList<LatLng> puncte;
    private double latitudine;
    private double longitudine;
    private String furnizor;
    private Distanta traseu = new Distanta();
    private TextView distanta;
    private TextView altitudine;
    private Marker marker = null;
    private Marker poiMarker = null;
    private Chronometer cronometru;
    private boolean start = false;
    private Polyline linie;
    private LocationProvider lp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        puncte = new ArrayList<>();
        setContentView(R.layout.activity_maps2);

        cronometru = (Chronometer) findViewById(R.id.cronometru);
        distanta = findViewById(R.id.distanta);
        altitudine = findViewById(R.id.altitudine);

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
            case R.id.normal:
                harta.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                Toast toast1 = Toast.makeText(this, "Harta normala", Toast.LENGTH_SHORT);
                toast1.show();
                return true;

            case R.id.hibrid:
                harta.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                Toast toast2 = Toast.makeText(this, "Harta hibrid", Toast.LENGTH_SHORT);
                toast2.show();
                return true;

            case R.id.satelit:
                harta.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                Toast toast3 = Toast.makeText(this, "Harta satelit", Toast.LENGTH_SHORT);
                toast3.show();
                return true;

            case R.id.teren:
                harta.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                Toast toast4 = Toast.makeText(this, "Harta teren", Toast.LENGTH_SHORT);
                toast4.show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onMapReady(GoogleMap gMap) {
        harta = gMap;
        activeazaLocatia();
        if (!harta.isMyLocationEnabled())
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED) {
                return;
            }
        harta.setMyLocationEnabled(true);

        try {
            boolean success = gMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.map_style));
            if (!success) {
                Log.e(TAG, "Nu s-a putut schimba aspectul hartii");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Nu s-a gasit fisierul. Eroare: ", e);
        }

        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        lp = lm.getProvider(LocationManager.GPS_PROVIDER);
        Log.i("suporta altitudine", String.valueOf(lp.supportsAltitude()));

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        furnizor = lm.getBestProvider(criteria, true);


        Location myLocation = lm.getLastKnownLocation(furnizor);

        if (myLocation == null) {

            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
            furnizor = lm.getBestProvider(criteria, false);
            myLocation = lm.getLastKnownLocation(furnizor);
        }

        if (myLocation != null) {
            LatLng userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());

            latitudine = myLocation.getLatitude();
            longitudine = myLocation.getLongitude();

            altitudine.setText(myLocation.getAltitude() + "m");

            String snippet = String.format(Locale.getDefault(),
                    "Lat: %1$.5f, Long: %2$.5f",
                    userLocation.latitude,
                    userLocation.longitude);

            harta.addMarker(new MarkerOptions()
                    .position(userLocation)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                    .title("Start")
                    .snippet(snippet)
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
                    double latitude = myLocation.getLatitude();
                    double longitude = myLocation.getLongitude();
                    LatLng latLng = new LatLng(latitude, longitude);

                    harta.moveCamera(CameraUpdateFactory.newLatLng(latLng));

                    if(start) {
                        traseazaLinie(latitude, longitude);
                    }
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
                public void onStatusChanged(String provider, int status, Bundle extras) {
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
        harta.getUiSettings().setZoomControlsEnabled(true);
        marcaj(harta);
        marcajTipPOI(gMap);
    }

    private void traseazaLinie(double latNou, double longNou) {

        Location loc1 = new Location("furnizor");
        loc1.setLatitude(latitudine);
        loc1.setLongitude(longitudine);
        puncte.add(new LatLng(latitudine, longitudine));

        Location loc2 = new Location("furnizor");
        loc2.setLatitude(latNou);
        loc2.setLongitude(longNou);
        puncte.add(new LatLng(latNou, longNou));

        linie = harta.addPolyline(new PolylineOptions()
                .addAll(puncte)
                .width(10)
                .color(Color.RED));

        distanta.setText(traseu.distanta(puncte));
        altitudine.setText(loc2.getAltitude() + "m");


        latitudine = latNou;
        longitudine = longNou;
    }

    private void marcaj(final GoogleMap map) {
        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                String snippet = String.format(Locale.getDefault(),
                        "Lat: %1$.5f, Long: %2$.5f",
                        latLng.latitude,
                        latLng.longitude);
                if (marker!=null) {
                    marker.remove();
                    marker=null;
                }
                if (marker == null) {
                    marker = harta.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(getString(R.string.dropped_pin))
                            .snippet(snippet)
                            .icon(BitmapDescriptorFactory.defaultMarker
                                    (BitmapDescriptorFactory.HUE_ORANGE)));
                }
            }
        });
    }

    private void marcajTipPOI(final GoogleMap map) {
        map.setOnPoiClickListener(new GoogleMap.OnPoiClickListener() {
            @Override
            public void onPoiClick(PointOfInterest poi) {
                if (poiMarker!=null) {
                    poiMarker.remove();
                    poiMarker=null;
                }
                if (poiMarker == null) {
                    poiMarker = harta.addMarker(new MarkerOptions()
                            .position(poi.latLng)
                            .title(poi.name)
                            .icon(BitmapDescriptorFactory.defaultMarker
                                    (BitmapDescriptorFactory.HUE_ORANGE)));
                    poiMarker.showInfoWindow();
                }
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
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

    public void start(View view) {
        start = true;

        puncte.clear();
        harta.clear();
        traseu.reset();
        distanta.setText(traseu.distanta(puncte));

        cronometru.setBase(SystemClock.elapsedRealtime());
        cronometru.start();
    }
}
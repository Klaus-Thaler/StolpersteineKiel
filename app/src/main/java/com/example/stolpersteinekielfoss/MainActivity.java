package com.example.stolpersteinekielfoss;


import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.CopyrightOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Copyright Mathias Uebel, Kiel Germany
 *
 * @see <a href="https://github.com/Klaus-Thaler/Stolpersteine_Kiel_Foss">Klaus Thaler on GitHub</a>
 *
 * @author Mathias Uebel
 */

public class MainActivity extends AppCompatActivity {
    public Context context;
    public static MapView mapView = null; // osm karte
    public static HashMap<Integer, ArrayList<String>> entryMap;
    // Start Position
    // https://www.openstreetmap.org/relation/62763#map=11/54.3413/10.1260
    double defaultLATITUDE = 54.3413;
    double defaultLONGITUDE = 10.1260;
    double defaultZOOM = 13.;
    private final String[] locationPerms = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public static class mUtils { // einfachere anzeige von toast
        public static void showToast(Context mContext, String message) {
            Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
        }
    }
    public static String web_link = "https://kiel-wiki.de/Stolpersteine";
    public static String preAddress = "Deutschland Kiel Schleswig-Holstein ";

    //public static String web_link = "https://de.wikipedia.org/wiki/Liste_der_Stolpersteine_in_Winsen_(Luhe)";
    //public static String preAddress = "Deutschland Winsen(Luhe) ";
    //public static String web_link = "https://de.wikipedia.org/wiki/Liste_der_Stolpersteine_in_Berlin-Pankow";
    //public static String preAddress = "Deutschland Berlin Pankow ";
    //public static String web_link = "https://de.wikipedia.org/wiki/Liste_der_Stolpersteine_in_Berlin-Mitte";
    //public static String preAddress = "Deutschleand Berlin Mitte ";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // osm map laden
        mapView = findViewById(R.id.mapView);
        //mapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        // controller fur osm
        IMapController mapController = mapView.getController();
        // start in kiel
        GeoPoint startPoint = new GeoPoint(defaultLATITUDE, defaultLONGITUDE);
        mapController.setCenter(startPoint);
        mapController.setZoom(defaultZOOM);
        addDecoration(mapView);
        // standort ermitteln
        mUtils.showToast(this, "Suche Standort.");
        ActivityCompat.requestPermissions(this, locationPerms, 2);
        MyLocationNewOverlay mMyLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), mapView);
        IMapController controller = mapView.getController();
        mMyLocationOverlay.setEnabled(false);
        mMyLocationOverlay.enableMyLocation();
        mMyLocationOverlay.enableFollowLocation();
        mMyLocationOverlay.setEnabled(true);
        mapView.getOverlays().add(mMyLocationOverlay);
        mapView.invalidate();

        // stolpersteine finden
        String mJsonString = FileManager.getJsonFromAssets(getApplicationContext(), "StolpersteineKiel.geojson");
        try {
            // get JSONObject from JSON file
            JSONObject mainObject = new JSONObject(mJsonString);
            // fetch JSONObject named features
            JSONArray elements = mainObject.getJSONArray("features");
            for (int i = 0; i < elements.length(); i++) {
                //Log.i("mInfo", " " + elements.get(i).toString());
                JSONObject elementObject = new JSONObject(elements.get(i).toString());
                JSONObject geoObject = new JSONObject(elementObject.get("geometry").toString());
                JSONArray coord = new JSONArray(geoObject.get("coordinates").toString());
                //Log.i("mInfo", " " + coord.get(1) + " - " + coord.get(0));
                JSONObject propObject = new JSONObject(elementObject.get("properties").toString());
                String name = propObject.get("name").toString();
                String description = propObject.get("description").toString();
                //Log.i("mInfo", " " + name + description);
                GeoPoint mPoint = new GeoPoint((Double) coord.get(1),
                        (Double) coord.get(0));
                Marker mMarker = new Marker(mapView);
                mMarker.setTitle(name + "\n\n" + description);
                mMarker.setPosition(mPoint);
                mapView.getOverlays().add(mMarker);
                mapView.invalidate();
            }



            // get employee name and salary
            //String name = employee.getString("");
            //String salary = employee.getString("salary");
            //Log.i("mInfo", " " + employee.get(0).toString());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }


    }
    public void addDecoration(MapView map) {
        map.setMultiTouchControls(true);
        map.setTilesScaledToDpi(true);
        // copyright
        CopyrightOverlay copyrightOverlay = new CopyrightOverlay(this);
        copyrightOverlay.setEnabled(true);
        map.getOverlays().add(copyrightOverlay);
        // scalebar
        ScaleBarOverlay myScaleBarOverlay = new ScaleBarOverlay(map);
        map.getOverlays().add(myScaleBarOverlay);
        //  compass
        CompassOverlay compassOverlay = new CompassOverlay(this,
                new InternalCompassOrientationProvider(this), map);
        compassOverlay.enableCompass();
        map.getOverlays().add(compassOverlay);
    }
    @Override
    public void onResume() {
        super.onResume();
        Configuration.getInstance().load(getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        if (mapView != null) { mapView.onResume(); }
    }
    @Override
    public void onPause() {
        super.onPause();
        Configuration.getInstance().save(getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        if (mapView != null) { mapView.onPause(); }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
/*
 * Copyright 2015 Cody Goldberg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.uri.egr.bme363lab.ui;

import android.Manifest;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.tbruyelle.rxpermissions.RxPermissions;

import butterknife.Bind;
import butterknife.ButterKnife;
import edu.uri.egr.bme363lab.R;

/**
 * Created by cody on 1/20/16.
 */
public class LocationActivity extends AppCompatActivity implements SensorEventListener, LocationListener {
    @Bind(R.id.lat_data) TextView latView;
    @Bind(R.id.lng_data) TextView lngView;
    @Bind(R.id.pressure_data) TextView pressureView;
    @Bind(R.id.elevation_data) TextView elevationView;

    // Hold the managers as an object so we can unregister our listeners when the Activity is destroyed.
    private SensorManager sensorManager;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location); // Same stuff as the previous labs.
        ButterKnife.bind(this);

        // Access the managers by calling upon getSystemService and casting to the correct object.
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        /*
            We need to do some magic here - in Marshmallow (API 23), we need to request permissions in the code.
            Previously, this was done in the AndroidManifest.xml
            We're going to use RxPermissions, a library that makes it really easy to do this for us.
         */
        RxPermissions.getInstance(this)
                .request(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                .subscribe(granted -> {
                    if (granted) { // If we have the permission, follow through with the requestLocationUpdates
                        // Ignore the red error line if you see this - Android Studio doesn't understand RxPermissions.
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                    } else {
                        Snackbar.make(elevationView, "No permissions granted!", Snackbar.LENGTH_INDEFINITE).show();
                    }
                });

        // Look up our pressure sensor through the sensorManager, and then register a listener.
        Sensor pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        sensorManager.registerListener(this, pressureSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onDestroy() {
        // We need to unregister our listeners here, otherwise we'll leak the sensors.
        // Leaking that causes battery drain, memory leaks, and other bad things.
        sensorManager.unregisterListener(this);

        // Check to see if the permissions were registered in the first place.
        // If they weren't we would crash without running isGranted();
        if (RxPermissions.getInstance(this)
                .isGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            locationManager.removeUpdates(this);
        }

        super.onDestroy();
    }

    @Override
    public void onLocationChanged(Location location) {
        // Set the text views we bound earlier here with the data we receive from onLocationChanged.
        latView.setText(String.format("%.4f", location.getLatitude()));
        lngView.setText(String.format("%.4f", location.getLongitude()));
        elevationView.setText(String.format("%.0f meters", location.getAltitude()));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Do nothing here - we don't care about it!
    }

    @Override
    public void onProviderEnabled(String provider) {
        // Do nothing here - we don't care about it!
    }

    @Override
    public void onProviderDisabled(String provider) {
        // Do nothing here - we don't care about it!
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do nothing here - we don't care about it!
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Set the pressureView bind with the sensor data from the pressure sensor.
        // The return SensorEvent contains a float array of values, but since we only have one value that is pressure,
        // We only choose the index 0.
        pressureView.setText(String.format("%.3f", event.values[0]));
    }
}

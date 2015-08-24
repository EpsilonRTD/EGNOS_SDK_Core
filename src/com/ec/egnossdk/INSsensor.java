/**
 * @file INSsensor.java
 *
 * Gets INS data from Accelerometer, Gyroscope and Magnetic Field sensors.
 * 
 * Rev: 3.0.0
 * 
 * Author: DKE Aerospace Germany GmbH
 *
 * Copyright 2012 European Commission
 *
 * Licensed under the EUPL, Version 1.1 only (the "Licence");
 * You may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 * http://ec.europa.eu/idabc/eupl
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 *
 **/
package com.ec.egnossdk;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class INSsensor implements SensorEventListener {
  private SensorManager sensorManager;
  float[] geomag = new float[3];
  float last_geomag[] = null;
  double acc_total = 0.0;
  double ACC_THRESHOLD = 0.5; // used to filter the sensor readings
  double MAG_THRESHOLD = 0.3;

  double ACC_DYN_THRESHOLD = 0.06; // used to determine static or dynamic
                                   // situations
  double MAG_DYN_THRESHOLD = 0.45;

  double ACC_STAT_THRESHOLD = 0.15; // used to determine static or dynamic
                                    // situations
  double MAG_STAT_THRESHOLD = 0.3;

  boolean static_case = true;
  boolean dynamic_case = true;

  public INSsensor(Context context) {
    sensorManager = (SensorManager) context
        .getSystemService(Context.SENSOR_SERVICE);
    Sensor linear_acc = sensorManager
        .getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
    Sensor gyrosc = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    Sensor mag_sensor = sensorManager
        .getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

    int rate = 200000; // 0.2 second

    sensorManager.registerListener(this, linear_acc, rate);
    sensorManager.registerListener(this, gyrosc, rate);
    sensorManager.registerListener(this, mag_sensor, rate);
  }

  @Override
  public void onAccuracyChanged(Sensor arg0, int arg1) {

  }

  double[] ins = new double[9];

  @Override
  public void onSensorChanged(SensorEvent event) {
    double geomagnetic_diff = 0.0;

    if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
      if (last_geomag != null) {
        geomag = event.values.clone();
        double geomag_magnitude = Math.sqrt(Math.pow(geomag[0], 2)
            + Math.pow(geomag[1], 2) + Math.pow(geomag[2], 2));

        double last_geomag_magnitude = Math.sqrt(Math.pow(last_geomag[0], 2)
            + Math.pow(last_geomag[1], 2) + Math.pow(last_geomag[2], 2));
        geomagnetic_diff = geomag_magnitude - last_geomag_magnitude;
        ins[7] = geomagnetic_diff;
      }
      last_geomag = event.values.clone();
      System.arraycopy(geomag, 0, last_geomag, 0, 3);
    }

    if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
      if (event.values[0] != 0 || event.values[1] != 0 || event.values[2] != 0) {
        ins[1] = event.values[0]; // Acceleration force along the x axis
                                  // (including gravity). unit [m/s2]
        ins[2] = event.values[1];
        ins[3] = event.values[2];
      }

    }
    /* Acceleration magnitude */
    acc_total = Math.sqrt(Math.pow(ins[1], 2) + Math.pow(ins[2], 2));
    ins[8] = acc_total;

    if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
      if (event.values[0] != 0 || event.values[1] != 0 || event.values[2] != 0) {
        ins[4] = event.values[0];
        ins[5] = event.values[1];
        ins[6] = event.values[2];
      }
    }

    if (dynamic_case) {
      // Check whether static or not
      if (acc_total < ACC_DYN_THRESHOLD && geomagnetic_diff < MAG_DYN_THRESHOLD) {
        static_case = true;// static state, no movement
        dynamic_case = false;
        ins[0] = 1;
        // Log.d("INSsensor","---static");
      } else {
        static_case = false;// dynamic case, device is moving
        dynamic_case = true;
        ins[0] = 0;
      }
    } else {
      if (acc_total > ACC_STAT_THRESHOLD
          && geomagnetic_diff > MAG_STAT_THRESHOLD) {// Add gyroscope and delta
                                                     // distance between two
                                                     // consecutive gps position
                                                     // <treshold
        static_case = false;// static state, no movement
        dynamic_case = true;
        ins[0] = 0;
        // Log.d("INSsensor","\nWas static gets dynamic.\n");
      }
    }
    // Write INS to GlobalState
    GlobalState.setInsReadings(ins);
  }
}

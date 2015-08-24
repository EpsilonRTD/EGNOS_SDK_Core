/**
 * @file BluetoothConnect.java
 *
 * Connects to an external Bluetooth receiver.Also identifies if the 
 * external Bluetooth receiver connected is an uBlox receiver.
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

/**
 * Class to connect to an external Bluetooth receiver and identify
 * the connected external Bluetooth receiver as a  uBlox receiver or not.
 **/
public class BluetoothConnect {
  private BluetoothSocket clientSocket_;
  private BluetoothSocket connectedSocket_;
  private String address_;
  private BluetoothAdapter bluetoothAdapter_;
  private BluetoothDevice device_;
  UUID generalUuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
  private static final String TAG = "EGNOS-SDK";
  /** Identifier for the uBlox receiver . */
  private static final int RECEIVER_UBLOX = 1;
  /** Number of trials to identify the receiver. */
  private static final int IDENTIFICATION_TRIALS = 4;
  public static int RECEIVER_TYPE = 0;
  public static final String KEY_SOCKET = "socketKey";
  public static final String KEY_BLUETOOTH_PREF = "bluetoothPrefKey";
 // public static GlobalState GS;
  public static boolean RECEIVER_CONNECTED = false;
  public static boolean IDENTIFY_RECEIVER = false;
  LogFiles log;

  /**
   * BluetoothConnect Constructor 
   * 
   * Constructs an interface to the global information of the application 
   * that called uBlox class and provides the address of the Bluetooth Receiver
   * selected.
   * @param context    The interface to the global information of the 
   *                   application environment.
   * @param address    The address of the Bluetooth receiver
   **/
  BluetoothConnect(final Context context, final String address) {
    Log.d(TAG, "BC | BluetoothConnect.");
    this.address_ = address;
    bluetoothAdapter_ = BluetoothAdapter.getDefaultAdapter();
   // GS = (GlobalState) context.getApplicationContext();
    log = new LogFiles();
  }

  /**
   * BluetoothConnect Constructor 
   * 
   * Constructs an interface to the global information of the application 
   * that called uBlox class..
   * @param context    The interface to the global information of the
   *                   application environment.
   **/
  public BluetoothConnect(final Context context) {
  //  GS = (GlobalState) context.getApplicationContext();
    log = new LogFiles();
  }

  /**
   * connect function 
   * 
   * Connects to Bluetooth receiver via RFCOMM channels.
   **/
  public final void connect() {
    Log.d(TAG, "BC | connect().");
    device_ = bluetoothAdapter_.getRemoteDevice(address_);
    Log.i(TAG, "Host name: " + bluetoothAdapter_.getName());
    Log.i(TAG, "Rx name: " + device_.getName());

    if (GlobalState.getSocket() != null) {
      closeConnection();
    }

    try {
      if (clientSocket_ != null) {
        clientSocket_ = null;
      }
      clientSocket_ = device_
          .createInsecureRfcommSocketToServiceRecord(generalUuid);
      clientSocket_.connect();
      RECEIVER_CONNECTED = true;
      GlobalState.setSocket(clientSocket_);
      Log.d(TAG, "BC | Client socket connected.");
      new ConnectedThread(clientSocket_);
    } catch (IOException e) {
      Method m = null;
      try {
        m = device_.getClass().getMethod("createRfcommSocket",
            new Class[] { int.class });
      } catch (SecurityException e1) {
        Log.e(TAG, "BC | Unable to connect to receiver: (" + e.getMessage()
            + ")");
        log.logError("Choose Bluetooth Receiver - Unable to connect to receiver: "+e1);
        RECEIVER_CONNECTED = false;
      } catch (NoSuchMethodException e1) {
        Log.e(TAG, "BC | Unable to connect to receiver: (" + e.getMessage()
            + ")");
        log.logError("Choose Bluetooth Receiver - Unable to connect to receiver: "+e1);
        RECEIVER_CONNECTED = false;
      }
      try {
        if (clientSocket_ != null) {
          clientSocket_ = null;
        }
        clientSocket_ = (BluetoothSocket) m.invoke(device_, 1);
        try {
          clientSocket_.connect();
          RECEIVER_CONNECTED = true;
          GlobalState.setSocket(clientSocket_);
          Log.d(TAG, "BC | Client socket connected.");
          new ConnectedThread(clientSocket_);
        } catch (IOException e1) {
          Log.e(TAG, "BC | Unable to connect to receiver: (" + e1.getMessage()
              + ")");
          log.logError("Choose Bluetooth Receiver - Unable to connect to receiver: "+e1);
          RECEIVER_CONNECTED = false;
        }
      } catch (IllegalArgumentException e1) {
        Log.e(TAG, "BC | Unable to connect to receiver: (" + e1.getMessage()
            + ")");
        log.logError("Choose Bluetooth Receiver - Unable to connect to receiver: "+e1);
        RECEIVER_CONNECTED = false;
      } catch (IllegalAccessException e1) {
        Log.e(TAG, "BC | Unable to connect to receiver: (" + e1.getMessage()
            + ")");
        log.logError("Choose Bluetooth Receiver - Unable to connect to receiver: "+e1);
        RECEIVER_CONNECTED = false;
      } catch (InvocationTargetException e1) {
        Log.e(TAG, "BC | Unable to connect to receiver: (" + e1.getMessage()
            + ")");
        log.logError("Choose Bluetooth Receiver - Unable to connect to receiver: "+e1);
        RECEIVER_CONNECTED = false;
      }
    }
  }

  /**
   * identifyuBloxReceiver function 
   * 
   * Identifies the type of the external Bluetooth receiver connected.
   * Identifies if the receiver is an uBlox receiver.
   **/
  public void identifyReceiver() {
    if (GlobalState.getSocket() != null) {
      for (int i = 0; i < IDENTIFICATION_TRIALS; i++) {
        //identifies external Bluetooth Receiver.
        IDENTIFY_RECEIVER = uBlox.identifyuBloxReceiver();
        if (IDENTIFY_RECEIVER) {
          Log.i(TAG, "BC | Identified uBlox receiver.");
          i = IDENTIFICATION_TRIALS;
          RECEIVER_TYPE = RECEIVER_UBLOX;
          GlobalState.setReceiverType(RECEIVER_TYPE);
        } else {
          if (i == IDENTIFICATION_TRIALS - 1) {
            clientSocket_ = null;
            closeConnection();
          }
          Log.e(TAG, "(BC) No receiver could be identified(" + i + ").");
        }
      }
    } else {
      Log.d(TAG, "BC | No client socket connected.");
      Log.e(TAG, "BC | Receiver could not be connected; ");
      RECEIVER_CONNECTED = false;
    }
  }

  /**
   * getReceiverType function 
   * 
   * Provides the receiver type connected.
   * @return receiverType     The connected receiver type.
   **/
  public static final int getReceiverType() {
    return RECEIVER_TYPE;
  }


  /**
   * ConnectedThread 
   * 
   * Class that opens output and input streams to write and read data from
   * the external Bluetooth receiver.
   **/
  public class ConnectedThread extends Thread {
    private InputStream inputStream_;
    private OutputStream outputStream_;

    /**
     * ConnectedThread constructor 
     * 
     * Opens an output stream and input stream.
     * @param clientSocket     The RFCOMM Bluetooth socket from the 
     *                          external Bluetooth receiver.
     **/
    public ConnectedThread(final BluetoothSocket clientSocket) {
      InputStream tmpIn = null;
      OutputStream tmpOut = null;
      connectedSocket_ = clientSocket;
      if (connectedSocket_ != null) {
        try {
          tmpIn = connectedSocket_.getInputStream();
          tmpOut = connectedSocket_.getOutputStream();
        } catch (IOException e) {
          Log.e(TAG, "BC | Unable to create temporary I/O streams;"
              + "Socket not created. (" + e.getMessage() + ")");
          log.logError("Choose Bluetooth Receiver - Unable to create temporary I/O streams: "+e);
        }
        inputStream_ = tmpIn;
        outputStream_ = tmpOut;
        GlobalState.setInputStream(inputStream_);
        GlobalState.setOutputStream(outputStream_);
      }
    }
  }

  /**
   * closeConnection function 
   * 
   * This function closes the bluetooth connection to the external Bluetooth 
   * receiver.
   */
  public void closeConnection() {
    if (GlobalState.getInputStream() != null) {
      try {
    	  GlobalState.getInputStream().close();
    	  GlobalState.setInputStream(null);
      } catch (Exception e) {
        Log.e(TAG, "EDAM | Could not close InputStream.");
        log.logError("Exit Application - Could not close Bluetooth InputStream: "+e);
      }
    }
    if (GlobalState.getOutputStream() != null) {
      try {
    	  GlobalState.getOutputStream().close();
    	  GlobalState.setOutputStream(null);
      } catch (Exception e) {
        Log.e(TAG, "EDAM | Could not close OutputStream.");
        log.logError("Exit Application - Could not close Bluetooth OutputStream: "+e);
      }
    }
    if (GlobalState.getSocket() != null) {
      try {
    	  GlobalState.getSocket().close();
    	  GlobalState.setSocket(null);
      } catch (Exception e) {
        Log.e(TAG, "EDAM | Could not close socket.");
        log.logError("Exit Application - Could not close Bluetooth Socket: "+e);
      }
    }
  }

}
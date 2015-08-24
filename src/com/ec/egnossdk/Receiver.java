/**
 * @file Receiver.java
 *
 * Reads messages from the Bluetooth receiver.
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

import android.content.Context;
import android.util.Log;

/**
 * Class that reads messages from the Bluetooth receiver.
 */
public class Receiver {

  Context context;
  static LogFiles log;
  private static final String TAG = "EGNOS-SDK";

  /**
   * Receiver Constructor.
   * 
   * Constructs an interface to global information of the application
   * that called Receiver class and file writer that writes to a file.
   * @param context      The interface to the global information of the 
   *                     application environment.
   **/
  public Receiver(Context context) {
    this.context = context;
    log = new LogFiles();
  }

  /**
   * readMessage function
   * 
   * Read data from the Bluetooth receiver.
   * @param receiverByte      The message from the receiver.
   * @return numBytesRead     The number of bytes read.
   **/
  public static int readMessage(final byte[] receiverByte) {
    int numBytesRead = 0;
  //  GlobalState gsg;
  //  gsg = BluetoothConnect.getGs();
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      Log.e(TAG, "Receiver | ThrdSleep (" + e.getMessage() + ")");
      e.printStackTrace();
    }
    StringBuffer receiverMessage = new StringBuffer("");

    try {
      numBytesRead = GlobalState.getInputStream().read(receiverByte);
      UtilsDemoApp.toHexCharArray(receiverByte);
      receiverMessage.append(UtilsDemoApp.toHex(receiverByte));
    } catch (Exception e) {
      Log.e(TAG, "Receiver | Receive error: " + e.getMessage());
      log.logError("Receiver is disconnected");
    }

    return numBytesRead;
  }

  /**
   * getNumBytes function
   * 
   * Read data from the Bluetooth receiver.
   * @param receiverByte      The message from the receiver.
   * @return numBytesRead     The number of bytes read.
   **/
  public static int getNumBytes(final byte[] receiverByte) {
    int numBytesRead = 0;
    int dataAvailable = 0;
   // GlobalState gsg;

 //   gsg = BluetoothConnect.getGs();
    if (GlobalState.getSocket() != null) {
      try {
        dataAvailable = GlobalState.getInputStream().available();
      } catch (IOException e1) {
        Log.e(TAG, "Receiver | getNumBytes() | No data available: " 
            + dataAvailable);
        GlobalState.setSocket(null);
        numBytesRead = -1;
        e1.printStackTrace();
        return 0;
      } catch (Exception e) {
        Log.e(TAG, "Receiver | getNumBytes() | Receive error: "
            + e.getMessage());
        GlobalState.setSocket(null);
        numBytesRead = -1;
        e.printStackTrace();
      }

      try {
       numBytesRead = GlobalState.getInputStream().read(receiverByte);       
      } catch (Exception e) {
        Log.e(TAG, "Receiver | getNumBytes() | Receive error: " 
            + e.getMessage());
        GlobalState.setSocket(null);
        numBytesRead = -1;
        e.printStackTrace();
      }
    } else {
    	GlobalState.setSocket(null);
    }
    return numBytesRead;
  }
  
  /**
   * getNumBytesLeftOver function
   * 
   * Read data from the Bluetooth receiver.
   * @param receiverByte      The message from the receiver.
   * @return numBytesRead     The number of bytes read.
   **/
  public static int getNumBytesLeftOver(final byte[] receiverByte) {
    int numBytesRead = 0;
    int dataAvailable = 0;
 //   GlobalState gsg;

 //   gsg = BluetoothConnect.getGs();
    if (GlobalState.getSocket() != null) {
      try {
        dataAvailable = GlobalState.getInputStream().available();
      } catch (IOException e1) {
        Log.e(TAG, "Receiver | getNumBytesLeftOver() | No data available: "
            + dataAvailable);
        GlobalState.setSocket(null);
        numBytesRead = -1;
        e1.printStackTrace();
      }
      
      if (dataAvailable != 0) {
        try {
          numBytesRead = GlobalState.getInputStream().read(receiverByte);
        } catch (Exception e) {
          Log.e(TAG, "Receiver | getNumBytesLeftOver() | Receive error: "
              + e.getMessage());
          GlobalState.setSocket(null);
          numBytesRead = -1;
          e.printStackTrace();
        }
      } else {
        Log.e(TAG, "Receiver | getNumBytesLeftOver() | No data available: "
            + dataAvailable);
        numBytesRead = -1;
      }
    } else {
      numBytesRead = -1;
      GlobalState.setSocket(null);
    }
    return numBytesRead;
  }
  
  /**
   * sendMessageToReceiver function 
   * 
   * Sends a message to receiver, depending on the identifier.
   * @param message     Message to be sent.
   **/
  protected static int sendMessageToReceiver(final String message) {
  //  GlobalState gS;
  //  gS = BluetoothConnect.getGs();
    byte[] command = UtilsDemoApp.generateByteMessage(message);
    int ret = UtilsDemoApp.write(command);
    return ret;
  }

}

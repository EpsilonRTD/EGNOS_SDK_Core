/**
 * @file EDAS.java
 *
 * An overlay to draw route from start point to end point on the Google Map.
 *
 * Rev: 3.0.0
 * 
 * Author: DKE Aerospace Germany GmbH
 * 
 * Copyright 2012 DKE Aerospace Germany GmbH
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
 **/
package com.ec.egnossdk;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import android.util.Base64;
import android.util.Log;

public class EDAS {
  
  private static final String TAG_EDAS = "Edas";
  
  public static int EGIP_PORT80 = 80;     // <NTRIP server port>
  public static int EGIP_PORT2101 = 2101; // <alternative NTRIP server port>
  
  public static String SERVER_DNS = "egnos-ip.net"; //"<server DNS entry>"
  public static String MOUNT_POINT = "EGNOS00";//"<NTRIP mountpoint>"
  public static String NTRIP_USERNAME = "dkeaerospace";//"<username>"
  public static String NTRIP_PASSWORD = "DiZkd2KT8Fr2p";//"<password>"
  
  static PrintWriter outputWriter = null;
  static DataInputStream dataInputStream = null; 
  
  int MAX_BUFFER_SIZE = 2048;
  
 
  /**
   * OpenConnection function
   * The function opens a connection to the EDAS server and returns the socket
   * @return edasSocket     The socket to the EDAS server connection
   */
  public static Socket OpenConnection() {
    Socket edasSocket = null;
    try {
      edasSocket = new Socket(SERVER_DNS, EGIP_PORT2101);

      if (authenticate(edasSocket) == -1) {
        edasSocket = null;
        Log.i(TAG_EDAS, "Edas | OpenConnection |"
            + " EDAS authentication failed");
      } else {
        Log.i(TAG_EDAS, "Edas | OpenConnection |"
            + " EDAS authenticated");
       // recveiveEGNOSIP();    
      }      
    } catch (UnknownHostException e) {
      Log.e(TAG_EDAS,
          "Edas | OpenConnection | Unknown Hostname.");
      edasSocket = null;
    } catch (IOException e) {
      Log.e(TAG_EDAS, "Edas | OpenConnection | Error: " + e);
      edasSocket = null;
    }
    return edasSocket;
  }
  
  
  
  
  /**
   * authenticate function.
   * The function requires the EDAS server authentication.
   * @param  edasSocket   The socket to the EDAS server connection
   * @return 1 if EDAS login is authenticated, otherwise -1
   */
  private static int authenticate(Socket edasSocket) {
    int r = 0;
    StringBuffer sendBuffer = null;
    String readFromEDAS = "";

    try {
      outputWriter = new PrintWriter(new BufferedWriter(
          new OutputStreamWriter(edasSocket.getOutputStream())),
          true);
    } catch (IOException e) {
      Log.e(TAG_EDAS, "Edas  | authenticate | Error: " + e);
      r = -1;
    }
    String credentials = NTRIP_USERNAME+":"+NTRIP_PASSWORD;
    String auth = Base64.encodeToString(credentials.getBytes(), 0, credentials.length(), 0);
    
    sendBuffer = new StringBuffer("GET /" + MOUNT_POINT + " HTTP/1.1\r\n");
    sendBuffer.append("Host: www."+SERVER_DNS+"\r\n");
    sendBuffer.append("Ntrip-Version: Ntrip/2.0\r\n");
    sendBuffer.append("User-Agent: NTRIP "+SERVER_DNS+"\r\n");
    sendBuffer.append("Authorization: Basic " + auth + "\r\n");
    sendBuffer.append("Accept: */*\r\n");
    sendBuffer.append("Connection: close\r\n");
    sendBuffer.append("\r\n");
   
    outputWriter.println(sendBuffer.toString());

    try {
      dataInputStream = new DataInputStream(edasSocket.getInputStream());
      readFromEDAS = dataInputStream.readLine();
      if (readFromEDAS.contains("OK"))
        r = 1;
      else
        r = -1;
     
    } catch (IOException e) {
      Log.e(TAG_EDAS, "EDAS  | authenticate | Error: " + e);
      r = -1;
    }
    return r;
  }
  
  /**
   * sendEdas function
   * The function sends messages to EDAS
   * @param  msg  The message to be sent
   */
   public void sendEdas(String msg) {
    outputWriter.println(msg);
  }
  
  /**
   * receiveEdas function
   * The function reads messages from EDAS
   */
  public static void receiveEdas() {
    int bytes_read = 0;
    byte[] data = new byte[1024];
    StringBuffer message_buffer = new StringBuffer();
    boolean message_started = false; // *
    boolean message_ended = false; // *
    int checksum_counter = 0; // *XX
    try {
      while (-1 != (bytes_read = dataInputStream.read(data, 0, data.length))) {
        // dataS = new String(data);
        // message_buffer.append(dataS);
        // Log.d(TAG_EGNOSIP,"EDAS | dataS - " + new String(dataS));
        for (int i = 0; i < bytes_read; i++) {

          if (message_started && message_ended && checksum_counter == 3) {
            // form extracted message
            String message_from_edas = message_buffer.toString();
            
            Log.d(TAG_EDAS, "EDAS |  Message: " + message_from_edas);
            processEdasMessage(message_from_edas);
            // clear buffer for the next message
            message_buffer.delete(0, message_buffer.length());
            // reset flags
            message_started = false;
            message_ended = false;
            checksum_counter = 0;
          }
          if (data[i] == '*') {
            if (message_started) {
              // message end //
              message_ended = true;
            } else {
              // message start //
              message_started = true;
            }
          }
          if (data[i] == '\n' || data[i] == '\r') {
            // drop these
          } else if (message_started) {
            message_buffer.append((char) data[i]);
            if (message_started && message_ended)
              checksum_counter += 1;
          }
        }
      }
    } catch (IOException e) {
      Log.e(
          TAG_EDAS,
          "EDAS | Problem reading bytes from EDAS server occured - "
              + e.toString());
    }
  }
  
  /**
   * closeEDASSocket function
   * The function closes the connection to the server
   * @param edasSocket    The socket to the EDAS server connection
   */
   public static void closeEDASSocket(Socket edasSocket) {
    outputWriter.close();
    try {
      dataInputStream.close();
      edasSocket.close();
    } catch (IOException e) {
      Log.e(TAG_EDAS, "EDAS | closeConnection | Error: " + e);
    }
  } 
  

  private static void processEdasMessage(String edasMsg)
  {
      String egnos_msg = SISNeT.readSisnetMessage(edasMsg, true);
      String egnosSubframe = egnos_msg.substring(12, 262);
      int mtype = UtilsDemoApp.get_type(egnosSubframe);
      Log.d("Coordinates","Edas | mtype: "+ mtype);
      //The buffer between the EDAS server and the SDK is created in the 
      //"storesEdasMessage" function.
      uBlox.storesEgnosMessage(egnos_msg, 2);
      //uBlox.storesEdasMessage(egnos_msg);
  }
  
}

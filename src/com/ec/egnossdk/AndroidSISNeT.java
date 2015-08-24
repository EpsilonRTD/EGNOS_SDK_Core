/**
 * @file AndroidSISNeT.java
 *
 * Establishes a connection to the SISNeT server and authenticates the 
 * connection with the specified SISNeT username and password.
 * Sends messages to SISNeT and receives messages
 * 
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
 */
package com.ec.egnossdk;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import android.util.Log;

/**
 * Class that opens a connection to the SISNeT server and authenticates the
 * connection with the specified SISNeT username and password.Sends messages to 
 * SISNeT and receives messages
 **/
public class AndroidSISNeT {
  public static final String SISNET_IP= "131.176.49.142";
  public static final int  SISNET_PORT_PRN120 =7777;
  public static final int  SISNET_PORT_PRN124 =7778;//DO NOT USE THIS PRN it is in test mode
  public static final int  SISNET_PORT_PRN126= 7779; 
//  public static final String  SISNET_LOGIN ="egnossdk,";  //contains additional character ,
//  public static final String  SISNET_PASSWD= "egnossdk\n";//contains additional characters \n
  private static final String TAG_SISNET = "EGNOS-SDK-AndroidSISNeT";
  PrintWriter outputWriter = null;
  DataInputStream dataInputStream = null; 

  /**
   * OpenConnection function
   * The function opens a connection to the SISNeT server and returns the socket
   * @return sisnetSocket     The socket to the SISNeT server connection
   */
	public Socket OpenConnection() {
		Socket sisnetSocket = null;
		try {
			sisnetSocket = new Socket(SISNET_IP, SISNET_PORT_PRN120);

			if (authenticate(sisnetSocket) == -1) {
				sisnetSocket = null;
				Log.i(TAG_SISNET, "AndroidSISNeT | OpenConnection |"
						+ " SISNeT authentication failed");
			} else
				Log.i(TAG_SISNET, "AndroidSISNeT | OpenConnection |"
						+ " SISNeT authenticated");

		} catch (UnknownHostException e) {
			Log.e(TAG_SISNET,
					"AndroidSISNeT | OpenConnection | Unknown Hostname.");
			sisnetSocket = null;
		} catch (IOException e) {
			Log.e(TAG_SISNET, "AndroidSISNeT | OpenConnection | Error: " + e);
			sisnetSocket = null;
		}
		return sisnetSocket;
	}
  
  /**
   * authenticate function.
   * The function requires the SISNeT server authentication.
   * @param sisnetSocket   The socket to the SISNeT server connection
   * @return 1 if SISNeT login is authenticated, otherwise -1
   */
	private int authenticate(Socket sisnetSocket) {
		int r = 0;
		StringBuffer sendBuffer = null;
		String readFromSISNeT = "";

		try {
			outputWriter = new PrintWriter(new BufferedWriter(
					new OutputStreamWriter(sisnetSocket.getOutputStream())),
					true);
		} catch (IOException e) {
			Log.e(TAG_SISNET, "AndroidSISNeT | authenticate | Error: " + e);
			r = -1;
		}

		sendBuffer = new StringBuffer("AUTH,");
		/* modified by aanagnostopoulos */
		Log.d(TAG_SISNET, "AndroidSISNeT | authenticate | Username: " + GlobalState.getSISNET_LOGIN()+", Password: "+GlobalState.getSISNET_PASSWD());
		sendBuffer.append(GlobalState.getSISNET_LOGIN()+","+GlobalState.getSISNET_PASSWD()+"\n");
//		sendBuffer.append(SISNET_LOGIN + SISNET_PASSWD);
		/* modified by aanagnostopoulos */

		outputWriter.println(sendBuffer.toString());

		try {
			dataInputStream = new DataInputStream(sisnetSocket.getInputStream());
			readFromSISNeT = dataInputStream.readLine();
			if (readFromSISNeT.startsWith("*AUTH"))
				r = 1;
			else
				r = -1;
		} catch (IOException e) {
			Log.e(TAG_SISNET, "AndroidSISNeT | authenticate | Error: " + e);
			r = -1;
		}
		return r;
	}
  
  /**
   * sendSisnet function
   * The function sends messages to SISNeT
   * @param  msg  The message to be sent
   */
   public void sendSisnet(String msg) {
		outputWriter.println(msg);
	}
  
  /**
   * recvSisnet function
   * The function reads messages from SISNeT
   * @return readFromSISNeT    The message read from SISNeT
   */
   public String recvSisnet() {	  
		String readFromSISNeT = "";
		if (uBlox.sisnetSocket != null) {
			try {
				readFromSISNeT = dataInputStream.readLine();
			} catch (IOException e) {
				Log.e(TAG_SISNET, "AndroidSISNeT | recvSisnet | Error: " + e);
				readFromSISNeT = "";
			}
		}
		return readFromSISNeT;
	}
  
  /**
   * closeConnection function
   * The function closes the connection to the server
   * @param sisnetSocket    The socket to the SISNeT server connection
   */
   public void closeConnection(Socket sisnetSocket) {
		outputWriter.close();
		try {
			dataInputStream.close();
			sisnetSocket.close();
		} catch (IOException e) {
			Log.e(TAG_SISNET, "AndroidSISNeT | closeConnection | Error: " + e);
		}
	}
}

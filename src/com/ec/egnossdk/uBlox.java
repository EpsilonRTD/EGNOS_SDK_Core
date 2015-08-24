/**
 * @file uBlox.java
 *
 * Identifies external Receiver as uBlox Receiver or not.
 * Sends, receives,parses and stores data for Ephemeris,
 * Raw and Sfrb messages.
 * Obtains the GPS and EGNOS positions from the SW Receiver
 * module.
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
 * http://ec.europa.eu/idabc/eupl.html
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.Callable;

import android.content.Context;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.ec.R;

/**
 * Class that identifies external Receiver as uBlox Receiver or not. Sends,
 * receives,parses and stores data for Ephemeris, Raw and Sfrb messages. Obtains
 * the GPS and EGNOS positions from the SW Receiver module.
 **/
public class uBlox {
	private static final int BUFFER = 1000;
	private static final int CHKSUM_START_BYTE = 4;
	private static final int PAYLOAD_START_BYTE = 12;
	/** The following defines the uBlox specific synchronization identifiers. */
	private static final String HEADER_1 = "B5";
	private static final String HEADER_2 = "62";
	/** The following defines the uBlox specific class identifiers. */
	static final String CLASS_ACK = "05";
	static final String CLASS_AID = "0B";
	static final String CLASS_CFG = "06";
	static final String CLASS_ESF = "10";
	static final String CLASS_INF = "04";
	static final String CLASS_MON = "0A";
	static final String CLASS_NAV = "01";
	static final String CLASS_RXM = "02";
	static final String CLASS_TIM = "0D";

	/** The following defines the uBlox specific message identifiers. */
	static final String ID_ACK = "01";
	static final String ID_ALM = "30";
	static final String ID_EPH = "31";
	static final String ID_NAK = "00";
	static final String ID_POSLLH = "02";
	static final String ID_PRT = "00";
	static final String ID_RAW = "10";
	static final String ID_SBAS = "16";
	static final String ID_SFRB = "11";
	static final String ID_MSG = "01";
	static final String ID_HUI = "02";

	/** Definition of SISNeT Login details. */
	private static final String SISNET_USERNAME = "egnossdk,";
	private static final String SISNET_PASSWORD = "egnossdk\n";

	/** The following defines the uBlox specific payload lengths. */
	static final int LENGTH_HEADER = 6;
	static final int LENGTH_CHKSUM = 2;
	static final int LENGTH_PRT = 20;
	private static final int LENGTH_POLL_RAW = 0;
	private static final int LENGTH_POLL_SFRB = 0;
	private static final int LENGTH_POLL_EPH_ALL = 0;
	private static final int LENGTH_POLL_POSLLH = 0;
	private static final int LENGTH_POLL_EPH_SV = 1;
	private static final int LENGTH_POLL_HUI = 0;
	private static final int LENGTH_MSG = 8;
	static int LENGTH_EPH_ALL = 104;
	static int LENGTH_EPH_SV = 8;
	static int LENGTH_POSLLH = 28;
	static int LENGTH_RAW = 0;
	static final int LENGTH_SBAS = 8;
	static int LENGTH_SFRB = 42;
	static final int HEX_BASE = 16;
	private static final int HEX_ONE_DIGIT = (int) Math.pow(HEX_BASE, 1.0);
	private static final int HEX_TWO_DIGIT = (int) Math.pow(HEX_BASE, 2.0);
	private static final int HEX_THREE_DIGIT = (int) Math.pow(HEX_BASE, 3.0);
	static final int BIN_BASE = 2;
	static Context context;
	static LogFiles logFiles;
	private GetMessagesThread getMessagesThread;
	private static final String TAG = "EGNOS-SDK-Ankur";
	private static final String TAG_MSG = "EGNOS-Messages";
	private static final String TAG_GPS = "GPS";
	private static final String TAG_DEBUG = "EGNOS-SDK-Debug";
	private static final String TAG_EPH = "EPH";
	private static final String TAG_RAW = "RAW";
	private static final String TAG_SFRB = "SFRB";
	private static final String TAG_POSLLH = "POSLLH";
	private static final String TAG_ANDROID = "Androidsisnet";
	private static final String TAG_SISNET = "EGNOS-SDK-AndroidSISNeT";
	static String[] sSf = new String[4];
	static String ephemSubFrame;
	static double[][] sat_data = new double[19][4];
	static double[][] sat_data_notused = new double[19][4];
	static double[][] sbas_data = new double[4][2];
	public static int svId = 0;
	static String msg0 = null;
	static String msg1 = null;
	static String msg1_120 = null;
	static String msg1_126 = null;
	static String msg10 = null;
	static String msg12 = null;
	static String msg7 = null;
	static String msg6 = null;
	static String msg9 = null;
	static String msg17 = null;
	static String[] m18_t = new String[5];
	static String[] m26_t = new String[25];
	static String[] msg2_5 = new String[8];
	static String[] msg24_t = new String[25];
	static String[] msg25_t = new String[15];
	static String[] ephemData = new String[32];
	static String[][] ephemTable = new String[32][5];
	static double[][] ephemerisTable = new double[32][3];
	static String egnosSubframe = "";
	static String egnosMts = "";
	static int countEphem = 0;
	static int countMsg0_t = 0;
	static int countMsg2_t = 0;
	static int countMsg3_t = 0;
	static int countMsg9_t = 0;
	static int countMsg17_t = 0;
	static int countMsg24_t = 0;
	static int countMsg25_t = 0;
	static int countMsg18_t = 0;
	static int countMsg26_t = 0;
	static int countMsg18 = 0;
	static int countMsg24 = 0;
	static int countMsg25 = 0;
	static int countMsg26 = 0;
	static int countMsg1_t = 0;
	static int countMsg10_t = 0;
	static int countMsg7_t = 0;
	static int countMsg12_t = 0;
	static int countMsg6_t = 0;
	static int countMsg4_t = 0;
	static int countMsg5_t = 0;
	public static int srrorInSisnet = 0;
	static int positionCount = 0;
	private String s;
	String[] sviD = new String[19];
	private static int egnos;
	public static int sisnet = 0;
	private static int sisnetSettings;
	public static int gps;
	public static int insSdkPositionAvailable = 0;
	private static int egnosSettings;
	private static int edasSettings;
	private static int sisnetUnavailable;
	private static int egnosUnavailable;
	private static int gpsUnavailable;
	private static int sisUnavailable;
	private static int sisAvailable;
	private static int edasUnavailable;
	static Handler messageHandler;

	static double[] currentPosition = new double[10];

	static double[] currentPosition_ = new double[850];
	public static int startThread = 0;
	int countSVID = 0;
	StringBuilder receivedmessages = new StringBuilder("");
	String extraMessages = "";
	int countMessages = 0;
	static int sfrbCountMessage = 0;
	static String recvMessage = "";
	static int cnt1 = 0;
	static int cnt2 = 0;
	static int cnt3 = 0;
	static int cnt4 = 0;
	static int cnt5 = 0;
	static int cnt6 = 0;
	static int cnt7 = 0;
	static int cnt9 = 0;
	static int cnt10 = 0;
	static int cnt12 = 0;
	static int cnt17 = 0;
	static int cnt18 = 0;
	static int cnt24 = 0;
	static int cnt25 = 0;
	static int cnt26 = 0;
	static int cnt27 = 0;
	static int iodp_120 = -1;
	static int iodp_126 = -1;

	static int count_1 = 0;
	static int count_2 = 0;
	static int count_3 = 0;
	static int count_4 = 0;
	static int count_5 = 0;
	static int count_6 = 0;
	static int count_7 = 0;
	static int count_9 = 0;
	static int count_10 = 0;
	static int count_12 = 0;
	static int count_17 = 0;
	static int count_18 = 0;
	static int count_24 = 0;
	static int count_25 = 0;
	static int count_26 = 0;

	// when set to 1 it indicates the time out of the message
	static int[] msg_TO = new int[27];

	// counter for requesting ephemeris sequentially
	static int count_eph = 0;
	static int[] eph_updated = new int[32];

	static int[] iode_old = new int[] { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1 };
	static int[] eph_set = new int[32];

	static int[] sbas_test = new int[] { 0, 0 }; // if set as 1, SBAS satellite
													// is in test mode

	static int size_mt18 = 0;
	static int iono_flag_2 = 0;
	double[] initialGPSPosition = new double[] { 0, 0, 0, 0 };
	double[] initialEGNOSPosition = new double[] { 0, 0, 0, 0 };
	static double tow = 0;
	static int egnos_position = 2;
	static int sis = -1;

	static double[] klob = new double[9];
	static double[] utc = new double[9];
	static double sys_time;
	static double time_sfrb;

	static int svIDMT9_ranging = -1;
	static int svIDMT17_ranging = -1;
	static int[] RnDoptions = new int[8];

	public static Socket sisnetSocket = null;
	public static Socket edasSocket = null;
	
	private static double[][] satelliteTypes = new double[32][4];
	public double last_height;

	public static int index_pos = 0;
	double receiverLatitude, receiverLongitude, receiverAltitude;
	static double[] pos3D = new double[3];

	static int numBytesRead = 0;
	static byte[] buffer;
	static int offset, length;

	public boolean isRead = false;
	static int TIMEOUT = 10000;
	static int countRnDSat = 0;
	
	static int TotalSatsInView= 0;
	
	GetEDASMessages getEdasMsg;
	//GPSINSTHREAD gpsINSThread;
	INSsensor ins;
	
	static INSThread insThread;
	
	 public static final String RECEIVER_DATA = "RECEIVER_DATA.txt";
	 
	 
	/**
	 * Load library for GPS-SW-Receiver Module.
	 **/
	static {
		System.loadLibrary("EGNOSSWReceiver");
	}

	/**
	 * getLongitudeLatitudeEGNOS function
	 * 
	 * Get EGNOS coordinates from SW receiver.
	 * 
	 * @param ephemData
	 *            32 X 1 table of Ephemeris Data.
	 * @param sat_data
	 *            19 X 4 table of Satellite Data.
	 * @param msg1
	 *            Message 1.
	 * @param msg10
	 *            Message 10.
	 * @param msg12
	 *            Message 12.
	 * @param msg7
	 *            Message 7.
	 * @param msg6
	 *            Message 6.
	 * @param m18_t
	 *            5 X 1 table of Message 18.
	 * @param m26_t
	 *            25 X 1 table of Message 26.
	 * @param msg2_5
	 *            8 X 1 table of Messages 2 to 5.
	 * @param msg24_t
	 *            25 X 1 table of Messages 24.
	 * @param msg25_t
	 *            15 X 1 table of Messages 24
	 * @param initial_position
	 *            4 X 1 table of Initial Position.
	 * @return coordinates 180 x 1 table of coordinates.
	 **/
	private native double[] getLongitudeLatitudeEGNOS(String[] ephemData,
			double[][] sat_data, String msg1, String msg10, String msg12,
			String msg7, String msg6, String[] m18_t, String[] m26_t,
			String[] msg2_5, String[] msg24_t, String[] msg25_t, String msg9,
			String msg17, double[] initial_position, double[] utc,
			double[] klob, int[] RnDoptions, double[][] sat_data_notused);

	/**
	 * getLongitudeLatitudeGPS function
	 * 
	 * Get GPS coordinates from SW receiver.
	 * 
	 * @param ephemData
	 *            32 X 1 table of Ephemeris Data.
	 * @param sat_data
	 *            19 X 4 table of Satellite Data.
	 * @param initial_position
	 *            4 X 1 table of Initial Position.
	 * @return coordinates 1 x 17 table of coordinates.
	 **/
	private native double[] getLongitudeLatitudeGPS(String[] ephemData,
			double[][] sat_data, double[] initial_position, double[] utc, double[][] sat_data_notused);

	/**
	 * checkSisnet function
	 * 
	 * Checks if SISNeT is available or not.
	 * 
	 * @param username
	 *            username of SISNeT login.
	 * @param password
	 *            password of SISNeT login.
	 * @return connect 0 or -1 if SISNeT is available or not.
	 **/
	private native int checkSisnet(final String username, final String password);

	public uBlox(Context context) {
		this.context = context;
		logFiles = new LogFiles();
	}

	/**
	 * uBlox Constructor.
	 * 
	 * Constructs a message handler and an interface to the global information
	 * of the application that called uBlox class.
	 * 
	 * @param handler
	 *            handler to display messages on the UI.
	 * @param context
	 *            interface to the global information of the application
	 *            environment.
	 **/
	public uBlox(Handler handler, Context context) {
		this.context = context;
		uBlox.messageHandler = handler;
		logFiles = new LogFiles();
	}

	/**
	 * init function.
	 * 
	 * This function initializes the CalculatePositionThread to buffer messages
	 * from the Bluetooth receiver.
	 **/
	public final void init() {
		// GlobalState gS;
		int receiverType = 0;
		// gS = BluetoothConnect.getGs();
		receiverType = BluetoothConnect.getReceiverType();
		switch (receiverType) {
		case 1:
			GlobalState.setErrorWhileReadingBT(1);
			if (GlobalState.getSocket() != null) {
				getMessagesThread = new GetMessagesThread();
				getMessagesThread.start();
			}
			break;
		default:
			Log.d(TAG, "uBlox | No uBlox receiver.");
		}
	}


	/**
	 * GetMessagesThread class.
	 * 
	 * A thread to start writing and reading from the Bluetooth receiver, Gets
	 * Ephemeris data, Satellite data, Raw data. Gets the GPS coordinates, EGNOS
	 * coordinates and Bluetooth Receiver coordinates.
	 **/
	public class GetMessagesThread extends Thread {
		ComputePositionThread computePosition;
		int ret = 1;

		public final void run() {
			for (int i = 0; i < 32; i++) {
				ephemerisTable[i][0] = i + 1;// prn
				ephemerisTable[i][1] = 0;// 1 if ephemeris is available for the
				// above prn, otherwise 0.
				ephemerisTable[i][2] = 0;// time of week.
			}
			//init Ephemeris data
			for (int i = 0; i < 32; i++) {
				ephemData[i] = "";
			}

			try {
				if (requestHUI() == -1) {
					Log.e(TAG,
							"uBlox | GetMessagesThread | Error in Request for HUI.");
					logFiles.logError("uBlox - GetMessagesThread - Error in request for HUI.");
				}
				if (requestRaw() == -1) {
					Log.e(TAG,
							"uBlox | GetMessagesThread | Error in Request for RAW.");
					logFiles.logError("uBlox - GetMessagesThread - Error in request for RAW.");
				}
				while (true && GlobalState.getSocket() != null
						&& GlobalState.getErrorWhileReadingBT() == 1) {
					egnosSettings = GlobalState.getEgnos();// EGNOS Signal in Space is ON/OFF in Settings.
					sisnetSettings = GlobalState.getSISNeT();// SISNeT is ON/OFF in Settings.
					edasSettings = GlobalState.getEDAS(); //EDAS is ON/OFF in Settings.
					
					sis = -1;// if -1 no EGNOS position, if 0 it is Signal in
								// Space, if 1 it is SISNeT.
					msg_TO = new int[27];

					Log.i(TAG, "uBlox | Raw, Sfrb & Eph |");

					try {
						getEgnosMessages();
					} catch (Exception e) {
						Log.e(TAG, "uBlox | getEgnosMessages: " + e);
					}

					// ret = requestPosllh();
					// if ( ret == -1) {
					// Log.e(TAG,
					// "GetMessagesThread | Error in Request for POSLLH.");
					// }else if(ret == 5) {
					// Log.e(TAG,"uBlox | GetMessagesThread | Error while reading from Bluetooth");
					// log.logError("uBlox - GetMessagesThread -  Error while reading from Bluetooth.");
					// GlobalState.setErrorWhileReadingBT(-1);
					// }

					try {
						getEgnosMessages();
					} catch (Exception e) {
						Log.e(TAG, "uBlox | getEgnosMessages: " + e);
					}

					// Request, read,parse and store Raw messages.
					ret = requestRaw();
					if (ret == -1) {
						Log.e(TAG,
								"uBlox | GetMessagesThread | Error in Request for RAW.");
						logFiles.logError("uBlox - GetMessagesThread - Error in request for RAW.");
					} else if (ret == 5) {
						Log.e(TAG,
								"uBlox | GetMessagesThread | Error while reading from Bluetooth");
						logFiles.logError("uBlox - GetMessagesThread -  Error while reading from Bluetooth.");
						GlobalState.setErrorWhileReadingBT(-1);
						GlobalState.setSocket(null);
					}

					// Compute GPS and EGNOS positions.
					try {
					  logFiles.logReceiverData("ComputeP");
						computePosition = new ComputePositionThread();
						computePosition.run();
					} catch (Exception e) {
						Log.e(TAG, "uBlox | ComputePositionThread: " + e);
					}

					startThread = 1;
				}
				if (GlobalState.getSocket() == null) {
					if (GlobalState.getisSkyplot() == false)
						displayMessage(R.string.connectToReceiver);
					gps = 0;
					logFiles.logError("uBlox - GetMessagesThread - Receiver is disconnected");
					GlobalState.setisTracking(false);
					GlobalState.setisCurrent(false);
					Log.e(TAG,
							"uBlox | GetMessagesThread | Receiver disconnected.");
				}

				if (GlobalState.getErrorWhileReadingBT() == -1) {
					BluetoothConnect bConnect = new BluetoothConnect(context);
					bConnect.closeConnection();
				}
			} catch (Exception e) {
				Log.e(TAG, "uBlox | GetMessagesThread: " + e);
				logFiles.logError("uBlox - GetMessagesThread - Error occurred: "
						+ e);
			}
		}
	}
	
	 /**
   * getEgnosMessages function
   * 
   * Gets EGNOs messages from Signal in Space or SISNeT or EDAS
   **/
  public void getEgnosMessages() {
    //Check if messages were received through SIS or SISNeT or EDAS
    boolean msgsReceived = false;
    int sisnetAvailable = 0;
          
    if(egnosSettings == 1) {//if Signal is Space is On in Settings
      if(sisnet == 0) { //if Signal in Space is available
        closeEDASConnection();
        closeSISNeTConnection();
        msgsReceived = true; 
        getSignalinSpaceMessages();
      }
    } // end of if Signal is Space in Settings 
    
    
    if(sisnetSettings == 1) {//if SISNeT is On in Settings
      if(!msgsReceived && edasSocket == null) {// if messages were not received from SIS
        closeEDASConnection();
        Log.i(TAG_SISNET,
            "uBlox | ComputePositionThread | Reading SISNeT msgs");
        if(sisnetSocket == null){
          sisnetAvailable = connectToSISNeT();
        }else{
        	sisnetAvailable = 1;
        }
        
        if(sisnetAvailable == 1) {
        	//get messages from SISNeT
	    	if(getSISNeTMsg()== -1){
	    		sisnetAvailable = -1;
	    	}
//          sisnetAvailable = getSISNeTMsg();
          if(sisnetAvailable == 1)
           msgsReceived = true;
        }        
        
        //if SISNeT is not available
        if (sisnetAvailable == -1) {
          closeSISNeTConnection();
          // displays message on UI, if SISNeT is
          // unavailable.
          if (GlobalState.getisCurrent()
              || GlobalState.getisTracking()) {
              displayMessage(R.string.sisnetUnAvailable);
              sisnetSocket = null;
              logFiles.logError("uBlox - ComputePositionThread - SISNeT is not available");
              Log.i(TAG_SISNET,
                  "uBlox | ComputePositionThread | SISNeT currently unavailable.");
          }
        }        
      }        
    }else // end of if SISNeT is On in Settings
      closeSISNeTConnection();
    
    if(edasSettings == 1) {//if EDAS is On in Settings
      if(!msgsReceived) {// if messages were not received from SIS or SISNeT
        closeSISNeTConnection();
        if(checkNetwork() ==1) {
          if(edasSocket == null)
            getEDASMessages();
        }else {
          // displays message on UI, if mobile device has
          // no network available.
          if (GlobalState.getisCurrent() || GlobalState.getisTracking()) {
            displayMessage(R.string.edasNoNetwork);
            Log.i(TAG,
                "uBlox | ComputePositionThread | Unable to connect to Egnos IP, "
                    + "network not available.");
            logFiles.logError("uBlox - ComputePositionThread - No network available");
          }
        }
       
      }
    }else//end of if EDAS is On in Settings
      closeEDASConnection();
    
    // display a message on UI, if EGNOS Signal in Space
    // in Settings is turned OFF
    // and Signal in Space is available.
    if (GlobalState.getisCurrent() || GlobalState.getisTracking()
        && egnosSettings == 0 && sisnet == 0) {
      if (sisAvailable == 0) {
        displayMessage(R.string.sisAvailable);
        Log.i(TAG,
            "uBlox | ComputePositionThread |Signal in Space "
                + "is available, please turn on EGNOS Signal in Space");
      }
      sisAvailable = 1;
    }
    
  }
  
  
  private void getSignalinSpaceMessages() {
    int ret = 1;
    String egnos_message;
    // Request, read and parse Sfrb messages.
    ret = requestSfrb();
    if (ret == -1) {
      Log.e(TAG,
          "uBlox | GetMessagesThread | Error in  Request forSfrb ");
      logFiles.logError("uBlox - GetMessagesThread -  Error in request for Sfrb.");
    } else if (ret == 5) {
      Log.e(TAG,
          "uBlox | GetMessagesThread | Error while reading from Bluetooth");
      logFiles.logError("uBlox - GetMessagesThread -  Error while reading from Bluetooth.");
      GlobalState.setErrorWhileReadingBT(-1);
      GlobalState.setSocket(null);
    } else {
      if (sat_data[0][1] != 0.0) {
        double time_tr = (double) (sat_data[0][1] - 0.1);
        String tow_msg = String.valueOf(time_tr);
        while ((int) time_tr < 6)
          tow_msg = "0" + tow_msg;
        while (tow_msg.length() < 12)
          tow_msg = tow_msg + "0";

        tow_msg = tow_msg.substring(0, 12);

        egnos_message = tow_msg + egnosSubframe;

        storesEgnosMessage(egnos_message, 0);// Stores Sfrb
                            // messages.
      }

      /*
       * if (sbas_data[0][1] != 0.0 || sbas_data[1][1] != 0.0
       * || sbas_data[2][1] != 0.0) { for(int h = 0; h < 4;
       * h++) if(sbas_data[h][0] == (double)svId){ // double
       * time_dif = time_sfrb - sys_time; // String tow_msg =
       * String .valueOf(((double) sbas_data[h][1] +
       * time_dif/1000)); String tow_msg = String
       * .valueOf(((double) sbas_data[h][1] )); if
       * (sbas_data[h][1] < 10) tow_msg = "00000" + tow_msg;
       * else if (sbas_data[h][1] < 100) tow_msg = "0000" +
       * tow_msg; else if (sbas_data[h][1] < 1000) tow_msg =
       * "000" + tow_msg; else if (sbas_data[h][1] < 10000)
       * tow_msg = "00" + tow_msg; else if (sbas_data[h][1] <
       * 100000) tow_msg = "0" + tow_msg;
       * 
       * if(tow_msg.length() < 12){ tow_msg =
       * String.valueOf(((int) sbas_data[h][1] )); tow_msg =
       * tow_msg+".00000"; }
       * 
       * tow_msg = tow_msg.substring(0, 12); egnos_message =
       * tow_msg + egnosSubframe;
       * storesEgnosMessage(egnos_message, 0);// Stores Sfrb
       * messages. break; } }else if (sat_data[0][1] != 0.0){
       * String tow_msg = String .valueOf((double)
       * sat_data[0][1]); while(tow_msg.length() < 6) tow_msg
       * = "0" + tow_msg; tow_msg = tow_msg + ".00000";
       * egnos_message = tow_msg + egnosSubframe;
       * storesEgnosMessage(egnos_message, 0);// Stores Sfrb
       * messages. }
       */
    }
  }
  
  private int connectToSISNeT() {
    if (checkNetwork() == 1) {
      sisnetSocket = SISNeT.connectSisnet();// connect to SISNeT
      if (sisnetSocket != null)
        return 1; // successfully connected to SISNeT
      else
        return -1; // unable to connect to SISNeT
    } else {
      // displays message on UI, if mobile device has
      // no network available.
      if (GlobalState.getisCurrent() || GlobalState.getisTracking()) {
        displayMessage(R.string.sisnetNoNetwork);
        Log.i(TAG,
            "uBlox | ComputePositionThread | Unable to connect to SISNeT, "
                + "network not available.");
        logFiles.logError("uBlox - ComputePositionThread - No network available");
      }
      return -2;
    }
  }
  
  private void closeSISNeTConnection() {
    if (sisnetSocket != null) {
      SISNeT.closeSisnet(sisnetSocket);
      sisnetSocket = null;
    }
  }
  
  private int connectToEDAS() {
    // Check if network is available on the device
    if (checkNetwork() == 1) {
      edasSocket = EDAS.OpenConnection();
      if(edasSocket != null)
        return 1;
      else 
        return -1;
    }else {
      // displays message on UI, if mobile device has
      // no network available.
      if (GlobalState.getisCurrent() || GlobalState.getisTracking()) {
        displayMessage(R.string.edasNoNetwork);
        Log.i(TAG,
            "uBlox | ComputePositionThread | Unable to connect to EDAS, "
                + "network not available.");
        logFiles.logError("uBlox - ComputePositionThread - No network available");
      }
      return -2;
    }
    
  }
  
  private void getEDASMessages() {

    edasSocket = EDAS.OpenConnection();
    if (edasSocket != null) {
      getEdasMsg = new GetEDASMessages();
      getEdasMsg.start();
    } else {
      closeEDASConnection();
      // displays message on UI, if SISNeT is
      // unavailable.
      if (GlobalState.getisCurrent() || GlobalState.getisTracking()) {
        displayMessage(R.string.edasUnAvailable);
        edasSocket = null;
        logFiles
            .logError("uBlox - ComputePositionThread - EDAS is not available");
        Log.i(TAG_SISNET,
            "uBlox | ComputePositionThread | EDAS currently unavailable.");
      }
    }
  }
  
  private void closeEDASConnection() {
    if(edasSocket != null) {
      EDAS.closeEDASSocket(edasSocket);
      getEdasMsg.stop();
      edasSocket = null;
    }
  }
	
	public class GetEDASMessages extends Thread{
	  public void run() {
	    EDAS.receiveEdas();	
	    edasSocket = null;
	  }
	}
	
	InsComputation insComp = new InsComputation();

	/**
	 * ComputePositionThread Thread.
	 * 
	 * A thread to get the GPS coordinates and EGNOS coordinates from the SW
	 * Receiver.
	 **/
	class ComputePositionThread extends Thread {
		double[] coordinatesGPS = new double[377];
		double[] coordinatesEgnos = new double[789];

		double gpsHDOP = 0;
		double egnosHDOP = 0;
		int iono_flag = 0;

		String gpsLatitude = "";
		String egnosLatitude = "";
		String message1 = "";
		double[] satDetails = new double[2];
		double[][] gpsSatelliteTypes = new double[32][4];
		double[][] egnosSatelliteTypes = new double[32][4];
		double[][] rndSatelliteTypes = new double[32][2];
		int satType = 0;
		
		double[] DOP=new double[4];

		public void run() {
			
			RnDoptions = GlobalState.getRndPositionType();
			// RnDoptions[0] = 3; // use Klobuchar ionospheric
			// model 1 - just klobuchar 2 - no udre
			// // 3 no degr factors
			// RnDoptions[1] = 1; // select best satellite
			// constellation
			// RnDoptions[2] = 0; // use 2D algorithm when sat
			// count < 4
			// RnDoptions[3] = 0; // use RAIM
			// RnDoptions[4] = 0; // set RRC = 0
			// RnDoptions[5] = 0; // apply best weight matrix
			// RnDoptions[6] = 0; // apply Kalman Filtering
			// RnDoptions[7] = 0; // use SBAS ranging
			//
			if (gps != 0) {
				coordinatesEgnos[9] = 0; // init iono _flag
				try {
					// Get GPS position from EGNOS SW Receiver module.
					coordinatesGPS = getLongitudeLatitudeGPS(ephemData,
							sat_data, initialGPSPosition, utc,sat_data_notused);
					tow = sat_data[0][1];

					try {
						GlobalState.setGPSTOW(sat_data[0][1]);
						Log.e("Coordinates ","tow: "+sat_data[0][1]);
					} catch (Exception e) {
						e.printStackTrace();
					}

					try {
						GlobalState.setGPSWN(coordinatesGPS[16]);
					} catch (Exception e) {
						e.printStackTrace();
					}
					
		       // Li's edit for RTCM/NMEA use

          // HDOP  VDOP  PDOP  TDOP          
          DOP[0]=coordinatesGPS[7];
          DOP[1]=coordinatesGPS[55];
          DOP[2]=coordinatesGPS[56];
          DOP[3]=coordinatesGPS[57];
          GlobalState.setDOP(DOP);
          
          // TOE
          GlobalState.setGPSTOE(coordinatesGPS[59]);
          
          // TotalSatInView
          GlobalState.setTotalSatInView(coordinatesGPS[8]+ coordinatesGPS[241]);

          // NumSatUse
          GlobalState.setNumSatUse(coordinatesGPS[10]);
          
          int size = (int)(coordinatesGPS[8] + coordinatesGPS[241]);
          
          Log.d("Satellites", "GPS Total Sats in View: "+coordinatesGPS[8]);
          // SatId, Elevation, Azimuth, SNR
          double[] SatId=new double[size];
          double[] Elevation=new double[size];
          double[] Azimuth=new double[size];
          double[] SNR=new double[size];
          //for used satellites
          for( int i=0; i<(int)coordinatesGPS[8]; i++)
          {
            if(SatId[i] == 126 && msg9 == null) {
              // Don't save the details of satellite id 126 
            }else {             
               SatId[i]=coordinatesGPS[17+2*i];
               Elevation[i]=coordinatesGPS[62+6*i];
               Azimuth[i]=coordinatesGPS[60+6*i];
               SNR[i]=coordinatesGPS[61+6*i];   
            }
          }
          
          for(int i = 0; i< (int)coordinatesGPS[241]; i++) {            
            SatId[i+(int)(coordinatesGPS[8])]=coordinatesGPS[242+i*4];
            Elevation[i+(int)(coordinatesGPS[8])]=coordinatesGPS[244+i*4];
            Azimuth[i+(int)(coordinatesGPS[8])]=coordinatesGPS[243+i*4];
            SNR[i+(int)(coordinatesGPS[8])]=coordinatesGPS[245+i*4];            
          }
          
          GlobalState.setSatId(SatId);
          GlobalState.setElevation(Elevation);
          GlobalState.setAzimuth(Azimuth);
          GlobalState.setSNR(SNR);
          
          // RTCM Message 1
          // double Modif_Zcount = coordinatesGPS[186];

          // information of used satellite 
          double[] PrnUse = new double[(int) coordinatesGPS[10]];
          double[] Prc = new double[(int) coordinatesGPS[10]];
          double[] Rrc = new double[(int) coordinatesGPS[10]];
          double[] Iodc = new double[(int) coordinatesGPS[10]];
          
          double[] Pr = new double[(int) coordinatesGPS[10]];
          double[] SatPosX = new double[(int) coordinatesGPS[10]];
          double[] SatPosY = new double[(int) coordinatesGPS[10]];
          double[] SatPosZ = new double[(int) coordinatesGPS[10]];
          
          int ModZcount = (int)Math.round(coordinatesGPS[186]%3600/0.6);
          
          for (int i = 0; i < (int) coordinatesGPS[10]; i++) {
            PrnUse[i] = coordinatesGPS[187 + 4 * i];
            Prc[i] = coordinatesGPS[188 + 4 * i];
            Log.d("Satellites", "GPS Prc: "+Prc);
            Rrc[i] = coordinatesGPS[189 + 4 * i];
            Iodc[i] = coordinatesGPS[190 + 4 * i];
            
            Pr[i] = coordinatesGPS[317 + 4 * i];
            SatPosX[i] = coordinatesGPS[318 + 4 * i];
            SatPosY[i] = coordinatesGPS[319 + 4 * i];
            SatPosZ[i] = coordinatesGPS[320 + 4 * i];
          }
          
          GlobalState.setModZcount(ModZcount);
          
          GlobalState.setPrnUse(PrnUse);
          GlobalState.setPrc(Prc);
          GlobalState.setRrc(Rrc);
          GlobalState.setIodc(Iodc);
          
          GlobalState.setPr(Pr);
          GlobalState.setSatPosX(SatPosX);
          GlobalState.setSatPosY(SatPosY);
          GlobalState.setSatPosZ(SatPosZ);

          // RTCM Message 3  (only GPS position,  better use getposition() instead)
          double[] ECEFGPS = new double[3];
          ECEFGPS[0] = coordinatesGPS[3];
          ECEFGPS[1] = coordinatesGPS[4];
          ECEFGPS[2] = coordinatesGPS[5];
          GlobalState.setECEFGPS(ECEFGPS);

				} catch (Exception e) {
					Log.e(TAG,
							"uBlox | ComputePositionThread | Error Coordinates GPS: "
									+ e);
					logFiles.logError("uBlox - ComputePositionThread - Error Coordinates GPS: "
							+ e);
				}
				Log.i(TAG, "ComputePositionThread | Coordinaates GPS:"
						+ "\nComputePositionThread | GPS Latitude:  "
						+ coordinatesGPS[0]
						+ "\nComputePositionThread | GPS Longitude: "
						+ coordinatesGPS[1]
						+ "\nComputePositionThread | GPS Altitude: "
						+ coordinatesGPS[2]);

				if (coordinatesGPS != null) {
					gpsHDOP = coordinatesGPS[7];
					Log.i(TAG, "ComputePositionThread | GPS HDOP: " + gpsHDOP);
					if (coordinatesGPS[3] != 0.0 && coordinatesGPS[4] != 0.0
							&& coordinatesGPS[5] != 0.0
							&& coordinatesGPS[6] != 0.0
							&& (gpsHDOP < 3 || gpsHDOP != 0)) {
						initialGPSPosition[0] = coordinatesGPS[3];
						initialGPSPosition[1] = coordinatesGPS[4];
						initialGPSPosition[2] = coordinatesGPS[5];
						initialGPSPosition[3] = coordinatesGPS[6];
						GlobalState.setinitialGPSPosition(initialGPSPosition);
					} else {
						initialGPSPosition = GlobalState
								.getinitialGPSPosition();
					}

					if (coordinatesGPS[0] >= -80.0 || coordinatesGPS[0] <= 80.0
							|| coordinatesGPS[1] <= 180.0
							|| coordinatesGPS[1] >= -180.0) {
						currentPosition[0] = coordinatesGPS[0]; // GPS Latitude
						currentPosition[1] = coordinatesGPS[1]; // GPS Longitude
						currentPosition[2] = coordinatesGPS[2]; // GPS Altitude

						currentPosition_[0] = currentPosition[0]; // GPS
																	// Latitude
						currentPosition_[1] = currentPosition[1]; // GPS
																	// Longitude
						currentPosition_[2] = currentPosition[2]; // GPS
																	// Altitude

						currentPosition_[8] = coordinatesGPS[8]; // Total no. of
																	// sats.
						currentPosition_[9] = coordinatesGPS[9]; // Low Elev
																	// Sats.
						currentPosition_[10] = coordinatesGPS[10]; // Sats Used
						currentPosition_[11] = coordinatesGPS[11]; // Iterations
						currentPosition_[12] = coordinatesGPS[12]; // 1 if Jump
						currentPosition_[13] = coordinatesGPS[13]; // Jump on x
						currentPosition_[14] = coordinatesGPS[14]; // Jump on y

						gpsLatitude = String.valueOf(coordinatesGPS[0]);

						for (int i = 0; i < sat_data.length; i++) {
							gpsSatelliteTypes[i][0] = sat_data[i][0];// PRN
							gpsSatelliteTypes[i][2] = sat_data[i][2] / 1000;// pesudorange
							gpsSatelliteTypes[i][3] = sat_data[i][3];// SNR
						}
						for (int i = 0; i < (int) coordinatesGPS[8]; i++) {
							satType = 1;
							Log.d("Satellite","GPS prn: "+coordinatesGPS[17 + i * 2] +"satTzpe: " +coordinatesGPS[17 + i * 2 + 1]);

							setsat_data(coordinatesGPS[17 + i * 2],
									coordinatesGPS[17 + i * 2 + 1],
									gpsSatelliteTypes, satType);
						}
						GlobalState.setGPSSatelliteType(gpsSatelliteTypes);
					}
				}

				GlobalState.setPosition(currentPosition);

				Arrays.fill(coordinatesEgnos, 0.0);

				for (int c = 0; c < 32; c++) {
					Arrays.fill(egnosSatelliteTypes[c], 0.0);
					Arrays.fill(rndSatelliteTypes[c], 0.0);
				}

				GlobalState.setEGNOSSatelliteType(egnosSatelliteTypes);
				GlobalState.setRnDSatelliteType(rndSatelliteTypes);

				if (egnos == 1) { // Number of Satellites available is more than5.
//
//					if (egnosSettings == 1 && sisnet == 1
//							&& sisnetSettings == 1) {
//						if (checkNetwork() == 0 || sisnetSocket == null)
//							checkMessageValidity();
//					} else if (egnosSettings == 1 && sisnetSettings == 0
//							&& sisnet == 1) {
//						if (checkNetwork() == 0 || sisnetSocket == null)
//							checkMessageValidity();
//					} else if (egnosSettings == 0 && sisnetSettings == 1) {
//						if (checkNetwork() == 0 || sisnetSocket == null)
//							checkMessageValidity();
//					} else if (egnosSettings == 0 && sisnetSettings == 0)
						checkMessageValidity();

					if (msg1 != null && countMsg2_t >= 2 && countMsg3_t >= 2
							&& (egnosSettings == 1 || sisnetSettings == 1 || edasSettings == 1)) {
						Log.i(TAG,
								"uBlox | ComputePositionThread | Acquiring EGNOS "
										+ "position");
						// Get EGNOS position from EGNOS SW Receiver module.
						try {
//Not in Lis version - check if needed
							RnDoptions = GlobalState.getRndPositionType();
							// RnDoptions[0] = 3; // use Klobuchar ionospheric
							// model 1 - just klobuchar 2 - no udre
							// // 3 no degr factors
							// RnDoptions[1] = 1; // select best satellite
							// constellation
							// RnDoptions[2] = 0; // use 2D algorithm when sat
							// count < 4
							// RnDoptions[3] = 0; // use RAIM
							// RnDoptions[4] = 0; // set RRC = 0
							// RnDoptions[5] = 0; // apply best weight matrix
							// RnDoptions[6] = 0; // apply Kalman Filtering
							// RnDoptions[7] = 0; // use SBAS ranging
							//
							// test 2 d pos

							// index_pos++;

							// if(index_pos >6)
							// for(int i=3; i<19; i++)
							// for(int j=0; j<4; j++)
							// sat_data[i][j]=0;

							if (msg6 != null)
								checkMessageValidity(6);

							// TEST 2D Algorithm
							/*
							 * sat_data = new double[19][4];
							 * 
							 * sat_data[0][0] = 17.0; sat_data[0][1] =
							 * 322688.401; sat_data[0][2] =
							 * 2.5261675422979128E7; sat_data[0][3] = 42.0;
							 * 
							 * sat_data[1][0] = 9.0; sat_data[1][1] =
							 * 322688.401; sat_data[1][2] =
							 * 2.0763698555840403E7; sat_data[1][3] = 50.0;
							 * 
							 * sat_data[2][0] = 14.0; sat_data[2][1] =
							 * 322688.401; sat_data[2][2] = 2.264637230654702E7;
							 * sat_data[2][3] = 44.0;
							 * 
							 * ephemData = new String[32]; ephemData[16]=
							 * "117000000000000000000000000000000000000000000000000000000000000101001010100000100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000011101001000000011011110100111100011010000000000000001111111111101000000000000100010111100000100100000000000000000000000000000000000000000000000000000000000000000000011011110000000100010000000000001100010000101110110110000000011110011000100000111010000000000000001110000100000011000000101111110110011101000001000000000100000011000110100001000000000011010101010111011100000000010011110001101000000000000000000000000000000000000000000000000000000000000000000000000000000000000011100010111110000000001001111011111101001011000000111111111010011100100111000000001101100000100001001000000000000111010010000010100010000000001101000011111110100011000000111111111010101001010111000000011011110001001000111100000000"
							 * ; ephemData[8]=
							 * "109000000000000000000000000000000000000000000000000000000000000101001010100000100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000011110100000000001110010100111100011010000000000000000000000000010111000000000101000010110100111100000000000000000000000000000000000000000000000000000000000000000000001110011111101000101101000000001011110011101000101001000000010110001101100111000010000000111110100100000000001000000000111010111111111100001011000000000001110001001010100001000000000011001111111100011100000000010011110001101000000000000000000000000000000000000000000000000000000000000000000000000000111111110101111101100110000000111100101001100100011011000000111111110111100100101000000000000111010010010011001111000000001010010110111101000001000000100101111010011111110111000000111111111010010010001111000000001110011111011101000000000000"
							 * ; ephemData[13]=
							 * "114000000000000000000000000000000000000000000000000000000000000101001010100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000011101101000000010011100100111100011010000000000000000000000000001001000000000110100101110000110000000000000000000000000000000000000000000000000000000000000000000000010011100000110001001100000000001011100100101010101000000000011101000000010111101001000000000010101100110000000011000000001011111100010101101100000000000010101101111110100001000000000011001010101011111101000000010011110001101000000000000000000000000000000000000000000000000000000000000000000000000000000000000011010000111111000000010111001010100100110100000000000000000000001000100111000000111000000100110010011111000000001001000001010110101011000000110111110110111001110100000000111111111010100100101011000000010011100000101010110000000000"
							 * ;
							 * 
							 * last_height = 4692740.6938460264;
							 */

							if (last_height != 0) {
								initialGPSPosition[2] = last_height;
							}
							// ensure no null strings are passed to JNI, causing an error in GetStringUTFChars
							String msg1_copy = msg1==null ? new String("") : msg1;
							String msg10_copy = msg10==null ? new String("") : msg10;
							String msg12_copy = msg12==null ? new String("") : msg12;
							String msg7_copy = msg7==null ? new String("") : msg7;
							String msg6_copy = msg6==null ? new String("") : msg6;
							String msg9_copy = msg9==null ? new String("") : msg9;
							String msg17_copy = msg17==null ? new String("") : msg17;
							String[] msg2_5_copy = new String[msg2_5.length];
							for (int i=0; i<msg2_5.length;i++) {
								msg2_5_copy[i] = msg2_5[i]==null ? new String("") : msg2_5[i];
							}
							String[] m18_t_copy = new String[m18_t.length];
							for (int i=0; i<m18_t.length;i++) {
								m18_t_copy[i] = m18_t[i]==null ? new String("") : m18_t[i];
							}
							String[] m26_t_copy = new String[m26_t.length];
							for (int i=0; i<m26_t.length;i++) {
								m26_t_copy[i] = m26_t[i]==null ? new String("") : m26_t[i];
							}
							String[] msg24_t_copy = new String[msg24_t.length];
							for (int i=0; i<msg24_t.length;i++) {
								msg24_t_copy[i] = msg24_t[i]==null ? new String("") : msg24_t[i];
							}
							String[] msg25_t_copy = new String[msg25_t.length];
							for (int i=0; i<msg25_t.length;i++) {
								msg25_t_copy[i] = msg25_t[i]==null ? new String("") : msg25_t[i];
							}
							coordinatesEgnos = getLongitudeLatitudeEGNOS(
									ephemData, sat_data, msg1_copy, msg10_copy, msg12_copy,
									msg7_copy, msg6_copy, m18_t_copy, m26_t_copy, msg2_5_copy, msg24_t_copy,
									msg25_t_copy, msg9_copy, msg17_copy, initialGPSPosition,
									utc, klob, RnDoptions,sat_data_notused);
							
						// Li's edit for RTCM/NMEA use

              // HDOP  VDOP  PDOP  TDOP
              double[] DOP=new double[4];
              DOP[0]=coordinatesEgnos[8];
              DOP[1]=coordinatesEgnos[500];
              DOP[2]=coordinatesEgnos[501];
              DOP[3]=coordinatesEgnos[502];
              GlobalState.setDOP(DOP);
              
              // TOE
              double gpsTOE=coordinatesEgnos[504];
              GlobalState.setGPSTOE(gpsTOE);
              
              // TotalSatInView
              double TotalSatInView = coordinatesEgnos[11] + coordinatesEgnos[591];
              GlobalState.setTotalSatInView(TotalSatInView);
              Log.d("Satellites", "EGNOS Total Sats in View: "+coordinatesEgnos[11]);

              // NumSatUse
              double NumSatUse = coordinatesEgnos[14];
              GlobalState.setNumSatUse(NumSatUse);
              
              // SatId, Elevation, Azimuth, SNR
              double[] SatId=new double[(int)TotalSatInView];
              double[] Elevation=new double[(int)TotalSatInView];
              double[] Azimuth=new double[(int)TotalSatInView];
              double[] SNR=new double[(int)TotalSatInView];
              for( int i=0; i<(int)coordinatesEgnos[11]; i++)
              {
                if(SatId[i] == 126 && msg9 == null) {
                  // Don't save the details of satellite id 126 
                }else { 
                SatId[i]=coordinatesEgnos[20+26*i];
                Elevation[i]=coordinatesEgnos[26+26*i];
                Azimuth[i]=coordinatesEgnos[505+4*i];
                SNR[i]=coordinatesEgnos[506+4*i];
                }
                Log.d("Satellites", "EGNOS Sateiites in View: "+SatId[i]);
              }
              
              for(int i = 0; i< (int)coordinatesEgnos[591]; i++) {            
                SatId[i+(int)(coordinatesEgnos[11])]=coordinatesEgnos[592+i*4];
                Elevation[i+(int)(coordinatesEgnos[11])]=coordinatesEgnos[594+i*4];
                Azimuth[i+(int)(coordinatesEgnos[11])]=coordinatesEgnos[593+i*4];
                SNR[i+(int)(coordinatesEgnos[11])]=coordinatesEgnos[595+i*4];            
              }
              GlobalState.setSatId(SatId);
              GlobalState.setElevation(Elevation);
              GlobalState.setAzimuth(Azimuth);
              GlobalState.setSNR(SNR);

              // RTCM Message 1
              // double Modif_Zcount = coordinatesEgnos[668];   // Z count
              
              // information of used satellite 
              double[] PrnUse = new double[(int) NumSatUse];
              double[] Prc = new double[(int) NumSatUse];
              double[] Rrc = new double[(int) NumSatUse];
              double[] Iodc = new double[(int) NumSatUse];
              
              double[] Pr = new double[(int) NumSatUse];
              double[] SatPosX = new double[(int) NumSatUse];
              double[] SatPosY = new double[(int) NumSatUse];
              double[] SatPosZ = new double[(int) NumSatUse];
              
              int ModZcount = (int)Math.round(coordinatesEgnos[668]%3600/0.6);

              
              for (int i = 0; i < (int) NumSatUse; i++) {
                PrnUse[i] = coordinatesEgnos[669 + 8 * i];
                Prc[i] = coordinatesEgnos[670 + 8 * i];
                Rrc[i] = coordinatesEgnos[671 + 8 * i];
                Iodc[i] = coordinatesEgnos[672 + 8 * i];
                
                Pr[i] = coordinatesEgnos[673 + 8 * i];
                SatPosX[i] = coordinatesEgnos[674 + 8 * i];
                SatPosY[i] = coordinatesEgnos[675 + 8 * i];
                SatPosZ[i] = coordinatesEgnos[676 + 8 * i];
                
              }
              
              GlobalState.setModZcount(ModZcount);
              
              GlobalState.setPrnUse(PrnUse);
              GlobalState.setPrc(Prc);
              GlobalState.setRrc(Rrc);
              GlobalState.setIodc(Iodc);
              
              GlobalState.setPr(Pr);
              GlobalState.setSatPosX(SatPosX);
              GlobalState.setSatPosY(SatPosY);
              GlobalState.setSatPosZ(SatPosZ);
              //  Li's edit end here !
              

							if (coordinatesEgnos[14] > 3) {
								last_height = coordinatesEgnos[6];
								pos3D[0] = coordinatesEgnos[4];
								pos3D[1] = coordinatesEgnos[5];
								pos3D[2] = coordinatesEgnos[6];
							}
							double distance = Math.sqrt(Math.pow(pos3D[0]
									- coordinatesEgnos[4], 2)
									+ Math.pow(pos3D[1] - coordinatesEgnos[5],
											2));
							if (distance > 1000)
								last_height = 0;

							if (msg6 != null)
								Log.i(TAG,
										"uBlox | ComputePositionThread | Acquiring EGNOS "
												+ "Stop");
						} catch (Exception e) {
							Log.e(TAG,
									"uBlox | ComputePositionThread | Error Coordinates Egnos: "
											+ e);
							logFiles.logError("uBlox - ComputePositionThread - Error Coordinates Egnos: "
									+ e);
						}

						iono_flag = (int) coordinatesEgnos[9];
						Log.d(TAG, "uBlox | iono_flag:" + iono_flag);
						Log.d(TAG, "uBlox | iono_flag 2:" + iono_flag_2);

						egnos_position = (int) coordinatesEgnos[10];

						// indicates preliminary EGNOS position or EGNOS
						// position.
						if (egnos_position == 1
								&& (iono_flag == 1 || iono_flag_2 == 1))
							egnos_position = 1; // display EGNOS position i.e.
												// green position.
						else
							egnos_position = 0;// display preliminary EGNOS
												// position i.e. orange
												// position.

						if (GlobalState.getisEgnosPosition() == 0
								|| GlobalState.getisEgnosPosition() == 2)
							GlobalState.setisEgnosPosition(egnos_position);
						Log.d(TAG,
								"uBlox | ComputePositionThread | egnos_position: "
										+ egnos_position);

						if (egnos_position == 0)
							// HPL is set to 0, to not display integrity circle
							// for preliminary EGNOS position.
							coordinatesEgnos[3] = 0;
						egnosHDOP = coordinatesEgnos[8];
						Log.d(TAG,
								"uBlox | ComputePositionThread | EGNOS HDOP: "
										+ egnosHDOP);
					}
				}

				Log.d(TAG,
						"uBlox | ComputePositionThread | Coordinaates EGNOS:"
								+ "\nuBlox | ComputePositionThread | EGNOS Latitude:  "
								+ coordinatesEgnos[0]
								+ "\nuBlox | ComputePositionThread | EGNOS Longitude: "
								+ coordinatesEgnos[1]
								+ "\nuBlox | ComputePositionThread | EGNOS Altitude:  "
								+ coordinatesEgnos[2]
								+ "\nuBlox | ComputePositionThread | HPL:  "
								+ coordinatesEgnos[3]);

				if (coordinatesEgnos != null) {
					if (coordinatesEgnos[0] >= -80.0
							|| coordinatesEgnos[0] <= 80.0
							|| coordinatesEgnos[1] <= 180.0
							|| coordinatesEgnos[1] >= -180.0) {
						currentPosition[3] = coordinatesEgnos[0]; // EGNOS
																	// Latitude
						currentPosition[4] = coordinatesEgnos[1]; // EGNOS
																	// Longitude
						currentPosition[5] = coordinatesEgnos[2]; // EGNOS
																	// Altitude
						currentPosition[6] = coordinatesEgnos[3]; // HPL

						currentPosition[7] = coordinatesEgnos[475];// R&D
																	// Latitude
						currentPosition[8] = coordinatesEgnos[476];// R&D
																	// Longitude
						currentPosition[9] = coordinatesEgnos[477];// R&D
																	// Altitude

						currentPosition_[3] = currentPosition[3]; // EGNOS
																	// Latitude
						currentPosition_[4] = currentPosition[4]; // EGNOS
																	// Longitude
						currentPosition_[5] = currentPosition[5]; // EGNOS
																	// Altitude
						currentPosition_[6] = currentPosition[6]; // HPL

						currentPosition_[15] = receiverLatitude; // Lat uBlox
						currentPosition_[16] = receiverLongitude; // Lon uBlox
						currentPosition_[17] = receiverAltitude; // Alt uBlox
						egnosLatitude = String.valueOf(currentPosition_[3]);

						for (int j = 0; j < coordinatesEgnos.length; j++)
							currentPosition_[18 + j] = coordinatesEgnos[j];

						for (int i = 0; i < sat_data.length; i++) {
							egnosSatelliteTypes[i][0] = sat_data[i][0];// PRN
							egnosSatelliteTypes[i][2] = sat_data[i][2] / 1000;// pesudorange
							egnosSatelliteTypes[i][3] = sat_data[i][3];// SNR

							rndSatelliteTypes[i][0] = sat_data[i][0];// PRN
						}
						for (int i = 0; i < currentPosition_[11 + 18]; i++) {
							satType = 2;
							Log.d("Satellite","EGNOS prn: "+currentPosition_[20 + i * 26 + 18] +"satTzpe: " +currentPosition_[21 + i * 26 + 18]);
							setsat_data(currentPosition_[20 + i * 26 + 18],
									currentPosition_[21 + i * 26 + 18],
									egnosSatelliteTypes, satType);
						}
						countRnDSat = 0;
						for (int i = 0; i < currentPosition_[11 + 18]; i++) {
							if (currentPosition[7] == 0)
								satType = 0;
							else
								satType = 3;
							setsat_data(currentPosition_[20 + i * 26 + 18],
									currentPosition_[22 + i * 26 + 18],
									rndSatelliteTypes, satType);
						}
						currentPosition_[520] = countRnDSat;// number of
															// satellites used
															// for R&D position

						GlobalState.setEGNOSSatelliteType(egnosSatelliteTypes);
						GlobalState.setRnDSatelliteType(rndSatelliteTypes);
					}
				}

				GlobalState.setPosition(currentPosition);
				try {
					if (GlobalState.getisLogFile() == 1) {
						logFiles.logPositionToSdCard(currentPosition_, sis,
								sisnet, GlobalState.getNetwork(), gpsHDOP,
								egnosHDOP, egnos_position, msg_TO);
					}
				} catch (Exception e) {
					Log.e(TAG,
							"uBlox | ComputePositionThread | Unable to create log file:"
									+ e);
				}
			}
//      RnDoptions[6] = 1;
//      if (RnDoptions[6] == 1)
//        if (insThread == null) {
//          insThread = new INSThread();
//          insThread.start();
//        }
//      
//      insComp.InsHandler();
    }
	}
	
	
	
	
	private class INSThread extends Thread{
		
		public void run(){
			
			 ins = new INSsensor(context);
		}
	}

	/**
	 * checkMessageValidity function
	 * 
	 * Checks the validity of all EGNOS messages. In case its not valid the
	 * EGNOS messages are deleted.
	 **/
	public void checkMessageValidity() {

//		checkMessageValidity(1);
//		checkMessageValidity(2);
//		checkMessageValidity(3);
//		checkMessageValidity(4);
//		checkMessageValidity(5);
//		checkMessageValidity(6);
//		checkMessageValidity(7);
//		checkMessageValidity(9);
//		checkMessageValidity(10);
//		checkMessageValidity(12);
//		checkMessageValidity(18);
//		checkMessageValidity(24);
//		checkMessageValidity(25);
//		checkMessageValidity(26);
	}

	/**
	 * setsat_data function
	 * 
	 * @param prn
	 * @param sat_type
	 * @param satelliteTypes
	 * @param satType
	 **/
	private void setsat_data(double prn, double sat_type,
			double[][] satelliteTypes, int satType) {
		for (int i = 0; i < satelliteTypes.length; i++) {
			if (satelliteTypes[i][0] == 0)
				return;
			if (prn == satelliteTypes[i][0]) {
				// type of satellite, 0- satellite not used,
				// 1 - satellite used for GPS position,
				// 2- satellite used for EGNOS position,
				// 3- satellite used for R&D position and GPS
				if (satType == 3) {
					if (sat_type == 0)
						satelliteTypes[i][1] = sat_type;
					else {
						satelliteTypes[i][1] = satType;
						countRnDSat++;
					}
				} else if (satType == 1) {
					if (sat_type == 1)
						satelliteTypes[i][1] = sat_type;
					else
						satelliteTypes[i][1] = 0;
				} else {
					if (sat_type == 2)
						satelliteTypes[i][1] = sat_type;
					else
						satelliteTypes[i][1] = 0;
				}
			}
		}
	}

	/**
	 * checkMessageValidity function
	 * 
	 * Checks the validity of EGNOS message of type MT. In case its not valid
	 * the EGNOS messages are deleted.
	 **/
	public void checkMessageValidity(int MT) {
		double msg_tow = 0;
		int corrected_sats = 0;
		int ai_msg2 = 0;
		int ai_msg3 = 0;
		int ai_msg24 = 0;
		int[] ai_msg;
		int timeOut_msg2 = 0;
		int timeOut_msg3 = 0;
		int timeOut_msg4 = 0;
		int timeOut_msg24 = 0;

		// For non precision approach
		// message 1- 600 seconds
		// Messages will be reseted at time-out, just at signal loss, due to
		// receiver restrictions
		if (msg0 == null) {

			if (MT == 1)
				if (msg1 != null) {
					msg_tow = Double.parseDouble(msg1.substring(0, 12));
					if (tow - msg_tow >= 600) {
						msg1 = null;
						msg_TO[1] = 1;
						Log.i(TAG_SISNET,
								"uBlox | checkMessageValidity | Message 1 deleted: ");
					}
				}

			if (MT == 6)
				// messages 6 Fast Corrections - 18 seconds
				if (msg6 != null) {
					msg_tow = Double.parseDouble(msg6.substring(0, 12));
					if (tow - msg_tow >= 18) {
						msg_TO[6] = 1;
						msg6 = null;
						Log.i(TAG_SISNET,
								"uBlox | checkMessageValidity | Message 6 deleted: ");
					}
				}

			if (MT == 7) {
				int k = 0;
				// message 7 - 360 seconds
				if (msg7 != null) {
					msg_tow = Double.parseDouble(msg7.substring(0, 12));

					if (msg1 != null) {
						corrected_sats = UtilsDemoApp.size_msg1(msg1.substring(
								12, 263));
						ai_msg = new int[13];
						int j = 0;
						while (k < 13) {
							if (msg1.charAt(12 + 14 + j) == '1')
								ai_msg[k++] = Integer.parseInt(msg7.substring(
										12 + 22 + (j * 4),
										12 + 22 + 4 + (j * 4)), 2);
							j++;
						}
						ai_msg2 = UtilsDemoApp.getMaxValue(ai_msg);
						timeOut_msg2 = getTimeOutInterval(ai_msg2);

						k = 0;
						ai_msg = new int[13];
						while (k < 13) {
							if (msg1.charAt(12 + 14 + j) == '1')
								ai_msg[k++] = Integer.parseInt(msg7.substring(
										12 + 22 + (j * 4),
										12 + 22 + 4 + (j * 4)), 2);
							j++;
						}
						ai_msg3 = UtilsDemoApp.getMaxValue(ai_msg);
						timeOut_msg3 = getTimeOutInterval(ai_msg3);

						timeOut_msg4 = timeOut_msg3;

						k = 0;
						ai_msg = new int[corrected_sats - 26];
						while (k < corrected_sats - 26) {
							if (msg1.charAt(12 + 14 + j) == '1')
								ai_msg[k++] = Integer.parseInt(msg7.substring(
										12 + 22 + (j * 4),
										12 + 22 + 4 + (j * 4)), 2);
							j++;
						}
						ai_msg24 = UtilsDemoApp.getMaxValue(ai_msg);
						timeOut_msg24 = getTimeOutInterval(ai_msg24);

					}
					if (tow - msg_tow >= 360) {
						msg_TO[7] = 1;
						msg7 = null;
						Log.i(TAG_SISNET,
								"uBlox | checkMessageValidity | Message 7 deleted: ");
					}
				}
			}

			if (MT == 9)
				// message 9 - 360 seconds
				if (msg9 != null) {
					msg_tow = Double.parseDouble(msg9.substring(0, 12));
					if (tow - msg_tow >= 360) {
						msg_TO[9] = 1;
						msg9 = null;
						Log.i(TAG_SISNET,
								"uBlox | checkMessageValidity | Message 9 deleted: ");
					}
				}

			if (MT == 10)
				// message 10 - 360 seconds
				if (msg10 != null) {
					msg_tow = Double.parseDouble(msg10.substring(0, 12));
					if (tow - msg_tow >= 360) {
						msg_TO[10] = 1;
						msg10 = null;
						Log.i(TAG_SISNET,
								"uBlox | checkMessageValidity | Message 10 deleted: ");
					}
				}

			if (MT == 12)
				// message 12 - 86400 seconds
				if (msg12 != null) {
					msg_tow = Double.parseDouble(msg12.substring(0, 12));
					if (tow - msg_tow >= 86400) {
						msg_TO[12] = 1;
						msg12 = null;
						Log.i(TAG_SISNET,
								"uBlox | checkMessageValidity | Message 12 deleted: ");
					}
				}

			if (MT == 18)
				// message 18 - 1200 seconds
				for (int i = 0; i < 5; i++) {
					if (m18_t[i] != null) {
						msg_tow = Double.parseDouble(m18_t[i].substring(0, 12));
						if (tow - msg_tow >= 1200) {
							msg_TO[18] = 1;
							m18_t[i] = null;
							Log.i(TAG_SISNET,
									"uBlox | checkMessageValidity | Message 18 deleted: ");
						}
					}
				}

			if (MT == 2 || MT == 3 || MT == 4 || MT == 5)
				// messages 2 to 5 Fast Corrections - 18 seconds
				for (int i = 0; i < 8; i++) {
					if (msg2_5[i] != null) {
						msg_tow = Double
								.parseDouble(msg2_5[i].substring(0, 12));
						if (msg7 == null) {
							if (tow - msg_tow >= 18) {
								msg2_5[i] = null;
								if (i == 0 || i == 4) {// message 2
									msg_TO[2] = 1;
									countMsg2_t--;
									Log.i(TAG_SISNET,
											"uBlox | checkMessageValidity | Message 2 deleted: ");
								}
								if (i == 1 || i == 5) {// message 3
									msg_TO[3] = 1;
									countMsg3_t--;
									Log.i(TAG_SISNET,
											"uBlox | checkMessageValidity | Message 3 deleted: ");
								}
								if (i == 2 || i == 6) {// message 4
									msg_TO[4] = 1;
									countMsg4_t--;
									Log.i(TAG_SISNET,
											"uBlox | checkMessageValidity | Message 4 deleted: ");
								}
							}
						} else {
							if (i == 0 || i == 4) {// message 2
								if (tow - msg_tow >= timeOut_msg2) {
									msg_TO[2] = 1;
									msg2_5[i] = null;
									countMsg2_t--;
									Log.i(TAG_SISNET,
											"uBlox | checkMessageValidity | Message 2 deleted: ");
								}
							}
							if (i == 1 || i == 5) {// message 3
								if (tow - msg_tow >= timeOut_msg3) {
									msg_TO[3] = 1;
									msg2_5[i] = null;
									countMsg3_t--;
									Log.i(TAG_SISNET,
											"uBlox | checkMessageValidity | Message 3 deleted: ");
								}
							}

							if (i == 2 || i == 6) {// message 4
								if (tow - msg_tow >= timeOut_msg4) {
									msg_TO[4] = 1;
									msg2_5[i] = null;
									countMsg4_t--;
									Log.i(TAG_SISNET,
											"uBlox | checkMessageValidity | Message 4 deleted: ");
								}
							}
						}
					}
				}

			if (MT == 24) {
				int[] tow_array = new int[10];
				// messages 24 - Long Corrections - 360 seconds
				for (int i = 0; i < 10; i++) {
					if (msg24_t[i] != null) {
						msg_tow = Double.parseDouble(msg24_t[i]
								.substring(0, 12));
						tow_array[i] = (int) (tow - msg_tow);
						if (tow - msg_tow >= 360) {
							msg_TO[24] = 1;
							msg24_t[i] = null;
							Log.i(TAG_SISNET,
									"uBlox | checkMessageValidity | Message 24 deleted: ");
						}
					}
				}
			}

			if (MT == 25)
				// messages 25 - Long Corrections - 360 seconds
				for (int i = 0; i < 5; i++) {
					if (msg25_t[i] != null) {
						msg_tow = Double.parseDouble(msg25_t[i]
								.substring(0, 12));
						if (tow - msg_tow >= 360) {
							msg_TO[25] = 1;
							msg25_t[i] = null;
							Log.i(TAG_SISNET,
									"uBlox | checkMessageValidity | Message 25 deleted: ");
						}
					}
				}

			if (MT == 26)
				// message 26 - 600 seconds
				for (int i = 0; i < 25; i++) {
					if (m26_t[i] != null) {
						msg_tow = Double.parseDouble(m26_t[i].substring(0, 12));
						if (tow - msg_tow >= 600) {
							msg_TO[26] = 1;
							m26_t[i] = null;
							Log.i(TAG_SISNET,
									"uBlox | checkMessageValidity | Message 26 deleted: ");
						}
					}
				}
		} else {
			msg1 = null;
			msg2_5 = null;
			msg6 = null;
			msg7 = null;
			msg9 = null;
			msg10 = null;
			msg12 = null;
			m18_t = null;
			msg24_t = null;
			msg25_t = null;
			m26_t = null;

			msg_tow = Double.parseDouble(msg0.substring(0, 12));
			if (tow - msg_tow >= 60) {
				msg0 = null;
				Log.i(TAG_SISNET,
						"uBlox | checkMessageValidity | Message 0 deleted: ");
			}
		}
	}

	/**
	 * getTimeOutInterval function
	 * 
	 * Gets the time out value for the corresponding degradation factor for
	 * Messages 2,3 and 24.
	 * 
	 * @param dFactor
	 *            The degradation factor.
	 * @return timeOut The corresponding time out value for the degradation
	 *         factor.
	 **/
	private int getTimeOutInterval(int dFactor) {
		int timeOut = 0;
		switch (dFactor) {
		case 0:
			timeOut = 180;
			break;
		case 1:
			timeOut = 180;
			break;
		case 2:
			timeOut = 153;
			break;
		case 3:
			timeOut = 135;
			break;
		case 4:
			timeOut = 135;
			break;
		case 5:
			timeOut = 117;
			break;
		case 6:
			timeOut = 99;
			break;
		case 7:
			timeOut = 81;
			break;
		case 8:
			timeOut = 63;
			break;
		case 9:
			timeOut = 45;
			break;
		case 10:
			timeOut = 45;
			break;
		case 11:
			timeOut = 27;
			break;
		case 12:
			timeOut = 27;
			break;
		case 13:
			timeOut = 27;
			break;
		case 14:
			timeOut = 18;
			break;
		case 15:
			timeOut = 18;
			break;
		}
		return timeOut;
	}

	/**
	 * getSISNeTMsg function
	 * 
	 * Gets the latest message from the SISNet server. Formats the acquired
	 * EGNOS message as (0-6:TOW, 6-256:Payload). Stores the EGNOs message in
	 * the corresponding message table.
	 **/
	public static int getSISNeTMsg() {
		String egnos_msg = "";
		egnos_msg = SISNeT.get_msg();
		if (egnos_msg != "") {
			egnosSubframe = egnos_msg.substring(12, 262);
			int mtype = UtilsDemoApp.get_type(egnosSubframe);
			egnosMts = Integer.toBinaryString(mtype);

			String towS = egnos_msg.substring(0, 12);
			double tow_sisnet = Double.parseDouble(towS);
			Log.d(TAG_SISNET, " towS:"+towS +", tow:"+tow);
			if (Math.abs(tow_sisnet - tow) < 100) {
				srrorInSisnet = 0;
				Log.i(TAG_SISNET, "uBlox | getSISNeTMessages  Type: " + mtype
						+ " " + egnos_msg);

				try {
					if (GlobalState.getisLogFile() == 1) {
						//logFiles.logEgnosToSdCard(
						//		"\nEGNOS Message from SISNeT \nEGNOS Mts: "
						//				+ mtype, "EGNOS Subframe: "
						//				+ egnosSubframe);
					}
				} catch (Exception e) {
					Log.e(TAG,
							"uBlox | parseSfrbS | Unable to create log file:"
									+ e);
				}
				storesEgnosMessage(egnos_msg, 1);
			} else {
				if (GlobalState.getisCurrent() || GlobalState.getisTracking()) {
					if (srrorInSisnet == 0) {
						displayMessage(R.string.errorInSISNeT);

					}
					srrorInSisnet = 1;
				}
			}

			return 1;
		} else
			return -1;
	}

	/**
	 * storesEgnosMessage function
	 * 
	 * This functions stores messages from Bluetooth receiver. The following
	 * messages are stored: Message 1, Message 10,Message 12, Message 7,
	 * Message6, 5 X 1 table of Message 18, 25 X 1 table of Message 26, 8 X 1
	 * table of Messages 2 to 5, 10 X 1 table of Messages 24.
	 * 
	 * @param egnos_message
	 *            The EGNOS message in the format (0-6:TOW, 6-256:Payload).
	 * @param sisnet_message
	 *            0 if EGNOS message is from Signal in Space and 1 if message is
	 *            from SISNeT.
	 **/
	public static void storesEgnosMessage(String egnos_message,
			int sisnet_message) {
		int band_id, block_id;
		int band_id_current, block_id_current;
		int iodp_message = 0;
		int in_ = 0;
		if (sisnet_message == 0)
			sis = 1; // Signal in Space
		else if(sisnet_message == 1){
			sis = 0;// SISNeT
			svId = 120;
		}else if(sisnet_message == 2){
      egnosSubframe = egnos_message.substring(12, 262);
      int mtype = UtilsDemoApp.get_type(egnosSubframe);
      egnosMts = Integer.toBinaryString(mtype);
      Log.d(TAG,"uBlox | storesEdasMessage | mtype: "+mtype + "\n egnosSubframe: " + egnosSubframe);
		}

		if (egnosSubframe != "" && egnosMts != "") {
			if ((svId == 120 && sbas_test[0] == 0)
					|| (svId == 126 && sbas_test[1] == 0) || sisnet_message == 2) {
				switch (Integer.parseInt(egnosMts, BIN_BASE)) {
				case 0:
					msg0 = egnos_message;
					if (svId == 120 && sisnet_message == 0)
						sbas_test[0] = 1;
					else if (svId == 126 && sisnet_message == 0)
						sbas_test[1] = 1;
					break;
				case 1:
					if (svId == 120 && sisnet_message == 0) {
						// message from Signal in Space
						msg1 = egnos_message;
						iodp_120 = Integer.parseInt(egnosSubframe.substring(
								224, 226));
					} else if (svId == 126 && sisnet_message == 0) {
						// message from Signal in Space
						msg1 = egnos_message;
						iodp_126 = Integer.parseInt(egnosSubframe.substring(
								224, 226));
					} else if (sisnet_message == 1 || sisnet_message == 2)// message from SISNeT
					{
						msg1 = egnos_message;
						iodp_120 = Integer.parseInt(egnosSubframe.substring(
								224, 226));
					}
					countMsg1_t++;
					Log.e("EgnosIP", "countMsg1_t: "+countMsg1_t);
					break;
				case 2:// iodp position: 16 to 17
					iodp_message = Integer.parseInt(egnosSubframe.substring(16,
							18));
					// if (iodp_message == iodp_120 || iodp_message == iodp_126)
					// {
					in_ = 1;
					if (msg2_5[0] != null) {
						msg2_5[4] = msg2_5[0];
					}
					msg2_5[0] = egnos_message;
					countMsg2_t++;
					// }
					break;
				case 3:// iodp position: 16 to 17
					iodp_message = Integer.parseInt(egnosSubframe.substring(16,
							18));
					// if (iodp_message == iodp_120 || iodp_message == iodp_126)
					// {
					in_ = 1;
					if (msg2_5[1] != null) {
						msg2_5[5] = msg2_5[1];
					}
					msg2_5[1] = egnos_message;
					countMsg3_t++;
					// }
					break;
				case 4:// iodp position: 16 to 17
					iodp_message = Integer.parseInt(egnosSubframe.substring(16,
							18));
					// if (iodp_message == iodp_120 || iodp_message == iodp_126)
					// {
					in_ = 1;
					if (msg2_5[2] != null) {
						msg2_5[6] = msg2_5[2];
					}
					msg2_5[2] = egnos_message;
					countMsg4_t++;
					// }
					break;
				case 5:// iodp position: 16 to 17
					iodp_message = Integer.parseInt(egnosSubframe.substring(16,
							18));
					if (iodp_message == iodp_120 || iodp_message == iodp_126) {
						in_ = 1;
						if (msg2_5[3] != null) {
							msg2_5[7] = msg2_5[3];
						}
						msg2_5[3] = egnos_message;
						countMsg5_t++;
					}
					break;
				case 6:
					in_ = 1;
					msg6 = egnos_message;
					countMsg6_t++;
					break;
				case 7:// iodp position: 18 to 19
					iodp_message = Integer.parseInt(egnosSubframe.substring(18,
							20));
					// if (iodp_message == iodp_120 || iodp_message == iodp_126)
					// {
					in_ = 1;
					msg7 = egnos_message;
					countMsg7_t++;
					Log.e("Coordinates", "Message 7: "+msg7);
					// }
					break;

				case 9:
					in_ = 1;
					svIDMT9_ranging = svId;
					msg9 = egnos_message;
					countMsg9_t++;
					break;

				case 10:
					in_ = 1;
					msg10 = egnos_message;
					countMsg10_t++;
					break;
				case 12:
					in_ = 1;
					msg12 = egnos_message;
					countMsg12_t++;
					break;
				case 17:
					in_ = 1;
					msg17 = egnos_message;
					countMsg17_t++;
					break;
				case 18:
					in_ = 1;
					if (countMsg18_t == 5)
						countMsg18_t = 0;
					band_id = UtilsDemoApp.get_bandId18(egnos_message);
					boolean m18Check = false;
					if (m18_t[0] == null) {
						m18_t[0] = egnos_message;
						size_mt18 += Math.ceil(UtilsDemoApp
								.size_msg18(egnosSubframe) / 15.0);
						countMsg18_t++;
						countMsg18++;
					} else {
						for (int i = 0; i < m18_t.length; i++) {
							if (m18_t[i] != null) {
								band_id_current = UtilsDemoApp
										.get_bandId18(m18_t[i]);
								if (band_id == band_id_current) {
									m18_t[i] = egnos_message;
									m18Check = true;
								}
							}
						}
						if (m18Check == false) {
							m18_t[countMsg18_t] = egnos_message;
							size_mt18 += Math.ceil(UtilsDemoApp
									.size_msg18(egnosSubframe) / 15.0);
							countMsg18_t++;
							countMsg18++;
						}
					}
					if (countMsg26 == size_mt18 && countMsg18 == 5)
						iono_flag_2 = 1;
					break;
				case 24:// iodp position: 110 to 111
					if (countMsg24_t == 25)
						countMsg24_t = 0;

					iodp_message = Integer.parseInt(egnosSubframe.substring(
							110, 112));
					if (iodp_message == iodp_120 || iodp_message == iodp_126) {
						in_ = 1;
						for (int i = 24; i > 0; i--) {
							msg24_t[i] = msg24_t[i - 1];
						}
						msg24_t[0] = egnos_message;
						countMsg24_t++;
						countMsg24++;
					}

					break;
				case 25:
					// velocity code position: 15
					// velocity code 0: IODP position: 117 to 118
					// velocity code 1: IODP position: 118 to 119

					if (countMsg25_t == 15)
						countMsg25_t = 0;

					int velocity_code = Integer.parseInt(egnosSubframe
							.substring(14, 15));

					if (velocity_code == 0)
						iodp_message = Integer.parseInt(egnosSubframe
								.substring(117, 119));
					else
						iodp_message = Integer.parseInt(egnosSubframe
								.substring(118, 120));

					// if (iodp_message == iodp_120 || iodp_message == iodp_126)
					// {
					in_ = 1;
					for (int i = 14; i > 0; i--) {
						msg25_t[i] = msg25_t[i - 1];
					}
					msg25_t[0] = egnos_message;
					countMsg25_t++;
					countMsg25++;
					// }
					// else
					// // in_ = 0;
					break;
				case 26:
					in_ = 1;
					if (countMsg26_t == 25)
						countMsg26_t = 0;
					band_id = UtilsDemoApp.get_bandId26(egnos_message);
					block_id = UtilsDemoApp.get_blockId26(egnos_message);
					boolean m26Check = false;
					if (m26_t[0] == null) {
						m26_t[0] = egnos_message;
						countMsg26_t++;
						countMsg26++;
					} else {
						for (int i = 0; i < m26_t.length; i++) {
							if (m26_t[i] != null) {
								band_id_current = UtilsDemoApp
										.get_bandId26(m26_t[i]);
								block_id_current = UtilsDemoApp
										.get_blockId26(m26_t[i]);
								if (band_id == band_id_current
										&& block_id == block_id_current) {
									m26_t[i] = egnos_message;
									m26Check = true;
								}
							}
						}
						if (m26Check == false) {
							m26_t[countMsg26_t] = egnos_message;
							countMsg26_t++;
							countMsg26++;
						}
						if (countMsg26 == size_mt18 && countMsg18 == 5)
							iono_flag_2 = 1;
					}
					break;
				}

				try {
					if (GlobalState.getisLogFile() == 1 && in_ == 1) {
//						logFiles.logEgnosToSdCard("\nEGNOS Mts Saved: "
//								+ Integer.parseInt(egnosMts, BIN_BASE)
//								+ "Sv ID: " + svId, "EGNOS Message: "
//								+ egnos_message);
					}
				} catch (Exception e) {
					Log.e(TAG,
							"uBlox | parseSfrbS | Unable to create log file:"
									+ e);
				}

			}
		} else
			Log.d(TAG, "uBlox | storesEgnosMessage | EGNOS subframe "
					+ "no data available");
		egnosMts = "";
		egnosSubframe = "";

	}
	
	
	 /**
   * This functions buffers messages from Bluetooth receiver.
   * The following messages are buffered:
   * Message 1, Message 10, Message 12, Message 7, Message 6,
   * 5 X 1 table of Message 18, 25 X 1 table of Message 26,
   * 8 X 1 table of Messages 2 to 5, 10 X 1 table of Messages 24.
   * @param egnos_message - the EGNOS message to be stored.
   */
  public synchronized static void storesEdasMessage(String egnos_message) {
    
        final int tow_size = 12;
    
        int band_id, block_id;
        int band_id_current, block_id_current;

        //The following assignments of svId and iodp120 are done to cope 
        //with the existing implementation. The new code nevertheless 
        //should be independent from the EGNOS satellite.
        int iodp_message = 0;
        svId = 120;
        int iodp = iodp_120;

        egnosSubframe = egnos_message.substring(tow_size, 250 + tow_size);
        int mtype = UtilsDemoApp.get_type(egnosSubframe);
        egnosMts = Integer.toBinaryString(mtype);
        Log.d(TAG,"uBlox | storesEdasMessage | mtype: "+mtype + "\n egnosSubframe: " + egnosSubframe);

        if (egnosSubframe != "" && egnosMts != "")
        {
            switch (mtype)
            {
                case 0:
                    countMsg0_t++;
                    break;
                case 1:
                    msg1 = egnos_message;
                    iodp = Integer.parseInt(egnosSubframe.substring(224,226));
                    iodp_120 = iodp;
                    countMsg1_t++;
                    break;
                case 2://iodp position: 16 to 18
                    iodp_message = Integer.parseInt(egnosSubframe.substring(16,18));
                    if (iodp_message == iodp)
                    {
                        if (msg2_5[0] != "")
                        {
                            msg2_5[4] = msg2_5[0];
                        }
                        msg2_5[0] = egnos_message;
                        countMsg2_t++;
                    }
                    break;
                case 3://iodp position: 16 to 18
                    iodp_message = Integer.parseInt(egnosSubframe.substring(16, 18));
                    if (iodp_message == iodp)
                    {
                        if (msg2_5[1] != "")
                        {
                            msg2_5[5] = msg2_5[1];
                        }
                        msg2_5[1] = egnos_message;
                        countMsg3_t++;
                    }
                    break;
                case 4://iodp position: 16 to 18
                    iodp_message = Integer.parseInt(egnosSubframe.substring(16,18));
                    if (iodp_message == iodp)
                    {
                        if (msg2_5[2] != "")
                        {
                            msg2_5[6] = msg2_5[2];
                        }
                        msg2_5[2] = egnos_message;
                        countMsg4_t++;
                    }
                    break;
                case 5://iodp position: 16 to 18
                    iodp_message = Integer.parseInt(egnosSubframe.substring(16,18));
                    if (iodp_message == iodp)
                    {
                        if (msg2_5[3] != "")
                        {
                            msg2_5[7] = msg2_5[3];
                        }
                        msg2_5[3] = egnos_message;
                        countMsg5_t++;
                    }
                    break;
                case 6:
                    msg6 = egnos_message;
                    countMsg6_t++;
                    break;
                case 7:
                    //iodp position: 18 to 20
                    iodp_message = Integer.parseInt(egnosSubframe.substring(18, 20));
                   // if (iodp_message == iodp)
                  //  {
                        msg7 = egnos_message;
                        Log.e("Coordinates", "Message 7: "+msg7);
                        countMsg7_t++;
                   // }
                    break;
                case 10:
                    msg10 = egnos_message;
                    countMsg10_t++;
                    break;
                case 12:
                    msg12 = egnos_message;
                    countMsg12_t++;
                    break;
                case 18:
                    if (countMsg18_t == 5)
                        countMsg18_t = 0;
                    band_id = UtilsDemoApp.get_bandId18(egnos_message);
                    boolean m18Check = false;
                    if (m18_t[0] == null)
                    {
                        m18_t[0] = egnos_message;
                        size_mt18 += (int)Math.ceil(UtilsDemoApp.size_msg18(egnosSubframe) / 15.0);
                        countMsg18_t++;
                        countMsg18++;
                    }
                    else
                    {
                        for (int i = 0; i < m18_t.length; i++)
                        {
                            if (m18_t[i] != null)
                            {
                                band_id_current = UtilsDemoApp.get_bandId18(m18_t[i]);
                                if (band_id == band_id_current)
                                {
                                    m18_t[i] = egnos_message;
                                    m18Check = true;
                                }
                            }
                        }
                        if (m18Check == false)
                        {
                            m18_t[countMsg18_t] = egnos_message;
                            size_mt18 += (int)Math.ceil(UtilsDemoApp.size_msg18(egnosSubframe) / 15.0);
                            countMsg18_t++;
                            countMsg18++;
                        }
                    }
                    if (countMsg26 == size_mt18 && countMsg18 == 5)
                        iono_flag_2 = 1;
                    break;
                case 24:
                    //iodp position: 110 to 112
                    if (countMsg24_t == 15)
                        countMsg24_t = 0;
                    iodp_message = Integer.parseInt(egnosSubframe.substring(110, 11));
                    if (iodp_message == iodp)
                    {
                        for (int i = 14; i > 0; i--)
                        {
                            msg24_t[i] = msg24_t[i - 1];
                        }
                        msg24_t[0] = egnos_message;
                        countMsg24_t++;
                        countMsg24++;
                    }
                    break;
                case 25:
                    // velocity code position: 15
                    // velocity code 0: IODP position: 117 to 118 
                    // velocity code 1: IODP position: 118 to 119
                    if (countMsg25_t == 10)
                        countMsg25_t = 0;
                    int velocity_code = Integer.parseInt(egnosSubframe
                        .substring(14, 15));

                    if (velocity_code == 0)
                      iodp_message = Integer.parseInt(egnosSubframe
                          .substring(117, 119));
                    else
                      iodp_message = Integer.parseInt(egnosSubframe
                          .substring(118, 120));
                    if (iodp_message == iodp)
                    {
                        for (int i = 9; i > 0; i--)
                        {
                            msg25_t[countMsg25_t] = egnos_message;
                        }
                        msg25_t[0] = egnos_message;
                        countMsg25_t++;
                        countMsg25++;
                    }
                    break;
                case 26:
                    if (countMsg26_t == 25)
                        countMsg26_t = 0;
                    band_id = UtilsDemoApp.get_bandId26(egnos_message);
                    block_id = UtilsDemoApp.get_blockId26(egnos_message);
                    boolean m26Check = false;
                    if (m26_t[0] == null)
                    {
                        m26_t[0] = egnos_message;
                        countMsg26_t++;
                        countMsg26++;
                    }
                    else
                    {
                        for (int i = 0; i < m26_t.length; i++)
                        {
                            if (m26_t[i] != null)
                            {
                                band_id_current = UtilsDemoApp.get_bandId26(m26_t[i]);
                                block_id_current = UtilsDemoApp.get_blockId26(m26_t[i]);
                                if (band_id == band_id_current && block_id ==
                                    block_id_current)
                                {
                                    m26_t[i] = egnos_message;
                                    m26Check = true;
                                }
                            }
                        }
                        if (m26Check == false)
                        {
                            m26_t[countMsg26_t] = egnos_message;
                            countMsg26_t++;
                            countMsg26++;
                        }
                        if (countMsg26 == size_mt18 && countMsg18 == 5)
                            iono_flag_2 = 1;
                    }
                    break;
                default:
                    Log.d(TAG,"uBlox | storesEdasMessage | Found MT: " + egnosMts);
                    break;
            }
              Log.d(TAG,"MT1:  {" + countMsg1_t + "} | MT2:  {" + countMsg2_t + "} | MT3:  {" + countMsg3_t + "} | MT4:  {" + countMsg4_t + "} | MT5:  {" + countMsg5_t + "} | MT6:  {" + countMsg6_t + "} | MT7: {" + countMsg7_t + "}");
              Log.d(TAG,"MT10: {" + countMsg10_t + "} | MT12: {" + countMsg12_t + "} | MT18: {" + countMsg18_t + "} | MT24: {" + countMsg24_t + "} | MT25: {" + countMsg25_t + "} | MT26: {" + countMsg26_t + "} | MT0: {" + countMsg0_t + "}");
              Log.d(TAG,"uBlox | storesEdasMessage | iodp: {" + iodp + "} ?= iodp_msg: {" + iodp_message +"}");
        }
        else
          Log.d(TAG,"uBlox | storesEdasMessage | EGNOS subframe no data available.");

        egnosMts = "";
        egnosSubframe = "";
  }

	/**
	 * identifyuBloxReceiver function
	 * 
	 * This functions identifies the connected Bluetooth receiver.
	 * 
	 * @return True if receiver is identified as uBlox receiver, otherwise
	 *         false.
	 **/
	public final static boolean identifyuBloxReceiver() {
		/** The Buffer used to read from the BT receiver */
		byte[] receiverByte = new byte[BUFFER];
		int numBytesRead = 0;
		int n = 0;
		int l = 0;
		String completeMessage = "";
		try {
			completeMessage = generateMessage(CLASS_CFG + ID_PRT);
			sendMessageToReceiver(completeMessage);
		} catch (Exception e) {
			Log.e(TAG,
					"uBlox | identifyuBloxReceiver | Write not successful. ("
							+ e.getMessage() + ")");
			logFiles.logError("uBlox - identifyuBloxReceiver - Unable to write to receiver: "
					+ e);
			return false;
		}
		/** Wait 1s to get the BT input stream. */
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			Log.e(TAG,
					"uBlox | identifyuBloxReceiver | ThrdSleep ("
							+ e.getMessage() + ")");
			e.printStackTrace();
		}

		try {
			if (GlobalState.getInputStream().available() != 0) {
				try {
					numBytesRead = Receiver.readMessage(receiverByte);
					n = 2 * numBytesRead;
					char[] output = new char[n];

					for (int k = 0; k < numBytesRead; k++) {
						byte v = receiverByte[k];
						int i = (v >> 4) & 0xf;
						output[l++] = (char) (i >= 10 ? ('a' + i - 10)
								: ('0' + i));
						i = v & 0xf;
						output[l++] = (char) (i >= 10 ? ('a' + i - 10)
								: ('0' + i));
					}
					Log.i(TAG, "n: " + n);
					Log.i(TAG,
							"Message from Receiver: " + String.valueOf(output));
					boolean blnResult = false;
					for (int i = 0; i < output.length - 8; i += 8) {
						char[] charArray1 = new char[] { output[i],
								output[i + 1], output[i + 2], output[i + 3] };
						char[] charArray2 = new char[] { 'b', '5', '6', '2' };
						if (Arrays.equals(charArray1, charArray2)) {
							char[] charArray3 = new char[] { output[i + 4],
									output[i + 5], output[i + 6], output[i + 7] };
							char[] charArray4 = new char[] { '0', '5', '0', '1' };
							if (Arrays.equals(charArray3, charArray4)) {
								blnResult = true;
								break;
							} else {
								blnResult = false;
							}
						} else {
							blnResult = false;
						}
					}

					if (blnResult) {
						return true;
					}
				} catch (Exception e) {
					Log.e(TAG,
							"uBlox | identifyuBloxReceiver | Receiver could not "
									+ "be identified. (" + e.getMessage() + ")");
					e.printStackTrace();
					return false;
				}
			} else {
				Log.e(TAG,
						"uBlox | identifyuBloxReceiver | No data available; "
								+ "inputStream error.");
				return false;
			}
		} catch (IOException e) {
			Log.e(TAG,
					"uBlox - identifyuBloxReceiver | No inputStream available. ("
							+ e.getMessage() + ")");
			e.printStackTrace();
			return false;
		}
		return false;
	}

	class ReadFromBT1 implements Callable<String> {
		byte b;
		int i = 0;

		@Override
		public String call() throws Exception {
			try {
				isRead = false;
				// Log.e(TAG,
				// "uBlox | ReadFromBT | Available: "+GlobalState.getInputStream().available());
				numBytesRead = GlobalState.getInputStream().read(buffer,
						offset, length);
				GlobalState.setInputStream(GlobalState.getSocket()
						.getInputStream());
				isRead = true;
			} catch (Exception e) {
				// Log.e(TAG, "uBlox | ReadFromBT | Error while reading: "+e);
			}
			return "Ready!";
		}
	}

	public class ReadFromBT implements Runnable {
		public void run() {
			try {
				isRead = false;
				// Log.e(TAG,
				// "uBlox | ReadFromBT | Available: "+GlobalState.getInputStream().available());
				numBytesRead = GlobalState.getInputStream().read(buffer,
						offset, length);
				GlobalState.setInputStream(GlobalState.getSocket()
						.getInputStream());
				isRead = true;
			} catch (Exception e) {
				// Log.e(TAG, "uBlox | ReadFromBT | Error while reading: "+e);
			}
		}
	}

	/**
	 * requestRaw function
	 * 
	 * First generates the request message send to the receiver and then this
	 * function handles the EGNOS Raw messages through a call of handleRaw.
	 * 
	 * @return ret Error identifier (1, -1).
	 */
	int requestRaw() {
		int ret;
		String completeMessage = "";

		completeMessage = generateMessage(CLASS_RXM + ID_RAW);
		sendMessageToReceiver(completeMessage);

		try {
			ret = handleRaw();
			if (ret == -2)
				GlobalState.setSocket(null);
		} catch (Exception e) {
			Log.e(TAG, "uBlox | RequestRaw error. (" + e + ")");
			logFiles.logError("uBlox - RequestRaw error: " + e);
			ret = -1;
		}
		return ret;
	}

	/**
	 * handleRaw function
	 * 
	 * The function reads messages from the Bluetooth receiver for classid 0201
	 * which is for Raw messages.
	 **/
	int handleRaw() {
		byte[] receivedHeader = new byte[10];
		byte[] receivedBytes = new byte[400];
		byte[] receivedMesage = new byte[10];
		// int numBytesRead = 0;
		int bytesRead = 0;
		int bytesToRead = 0;
		int iPayloadLen = 0;
		int totalLength = 0;
		String tmp1 = "";
		StringBuilder message = new StringBuilder();
		String sPayloadLen = "";
		long currentTime = 0;
		long oldTime = 0;
		long timeDiff = 0;
		ReadFromBT readBT;
		isRead = false;
		Thread r;

		if (GlobalState.getSocket() != null) {
			try {
				Log.i(TAG,
						"uBlox | handleRaw |  @@@ Receive read raw start @@@");
				// numBytesRead =
				// GlobalState.getInputStream().read(receivedHeader, 0,
				// (LENGTH_HEADER));
				numBytesRead = 0;

				buffer = receivedHeader;
				offset = 0;
				length = LENGTH_HEADER;

				oldTime = System.currentTimeMillis();
				readBT = new ReadFromBT();
				r = new Thread(readBT);
				r.start();

				while (isRead == false) {
					// Log.e(TAG,
					// "uBlox | handleRaw|  @@@ isRead 1: @@@"+isRead);
					currentTime = System.currentTimeMillis();
					timeDiff = currentTime - oldTime;
					// Log.e(TAG,
					// "uBlox | handleRaw|  @@@ Time Difference 1: @@@"+timeDiff);
					if (timeDiff > TIMEOUT) {
						Log.e(TAG,
								"uBlox | handleRaw|  @@@ Receive read raw terminated 1 @@@");
						return 5;
					}
				}
				// Log.e(TAG, "uBlox | handleRaw|  @@@ isRead 1: @@@"+isRead);
				isRead = false;

				// ExecutorService executor =
				// Executors.newSingleThreadExecutor();
				// Future<String> future = executor.submit(new ReadFromBT());
				// try {
				// // Log.e(TAG,
				// "uBlox | handleRaw|  @@@ Receive read raw started 1 @@@");
				// future.get(TIMEOUT, TimeUnit.MILLISECONDS);
				// // Log.e(TAG,
				// "uBlox | handleRaw|  @@@ Receive read raw finished 1 @@@");
				// } catch (TimeoutException e) {
				// Log.e(TAG,
				// "uBlox | handleRaw|  @@@ Receive read raw terminated 1 @@@");
				// return 5;
				// }
				// Log.i(TAG,
				// "uBlox | handleRaw |  @@@ Receive read raw end @@@");
				bytesRead = numBytesRead;
				sys_time = System.currentTimeMillis();
			} catch (Exception e) {
				Log.e(TAG, "uBlox | handleRaw | Receive error 1: " + e);
				// log.logError("uBlox - handleRaw - Receiver disconnected.");
				return -2;
			}

			if (bytesRead < LENGTH_HEADER) {
				do {
					tmp1 = "";
					tmp1 = UtilsDemoApp.byteToHex(receivedHeader);
					message.append(tmp1.substring(0, (numBytesRead * 2)));
					numBytesRead = 0;
					Arrays.fill(receivedHeader, (byte) 0);
					bytesToRead = LENGTH_HEADER - bytesRead;
					if (bytesToRead > 0) {
						try {
							// numBytesRead =
							// GlobalState.getInputStream().read(receivedHeader,
							// 0,
							// bytesToRead);

							buffer = receivedHeader;
							offset = 0;
							length = bytesToRead;
							oldTime = System.currentTimeMillis();
							readBT = new ReadFromBT();
							r = new Thread(readBT);
							r.start();

							while (isRead == false) {
								// Log.e(TAG,
								// "uBlox | handleRaw|  @@@ isRead 2: @@@"+isRead);
								currentTime = System.currentTimeMillis();
								timeDiff = currentTime - oldTime;
								// Log.e(TAG,
								// "uBlox | handleRaw|  @@@ Time Difference 2: @@@"+timeDiff);
								if (timeDiff > TIMEOUT) {
									Log.e(TAG,
											"uBlox | handleRaw|  @@@ Receive read raw terminated 2 @@@");
									return 5;
								}
							}
							// Log.e(TAG,
							// "uBlox | handleRaw|  @@@ isRead 2: @@@"+isRead);
							isRead = false;
							// ExecutorService executor =
							// Executors.newSingleThreadExecutor();
							// Future<String> future = executor.submit(new
							// ReadFromBT());
							// try {
							// // Log.e(TAG,
							// "uBlox | handleRaw|  @@@ Receive read raw started 2 @@@");
							// future.get(TIMEOUT, TimeUnit.MILLISECONDS);
							// // Log.e(TAG,
							// "uBlox | handleRaw|  @@@ Receive read raw finished 2 @@@");
							// } catch (TimeoutException e) {
							// Log.e(TAG,
							// "uBlox | handleRaw|  @@@ Receive read raw terminated 2 @@@");
							// return 5;
							// }
							bytesRead = bytesRead + numBytesRead;
							tmp1 = "";
							tmp1 = UtilsDemoApp.byteToHex(receivedHeader);
							message.append(tmp1
									.substring(0, (numBytesRead * 2)));
							sPayloadLen = UtilsDemoApp.charToStringUbl2(
									message.toString(), 8, 11);
							numBytesRead = 0;
							Arrays.fill(receivedHeader, (byte) 0);
						} catch (Exception e) {
							Log.e(TAG, "uBlox | handleRaw | Receive error 2: "
									+ e.getMessage());
							// log.logError("uBlox - handleRaw - - Receiver disconnected.");
							return -2;
						}
					}
				} while (bytesRead < LENGTH_HEADER);
				receivedHeader = null;
			} else {
				tmp1 = "";
				tmp1 = UtilsDemoApp.byteToHex(receivedHeader);
				message.append(tmp1.substring(0, (numBytesRead * 2)));
				sPayloadLen = UtilsDemoApp.charToStringUbl2(message.toString(),
						8, 11);
				numBytesRead = 0;
				Arrays.fill(receivedBytes, (byte) 0);
				receivedHeader = null;
			}

			try {
				iPayloadLen = Integer.parseInt(sPayloadLen, HEX_BASE);
			} catch (Exception e) {
				iPayloadLen = 0;
				Log.e(TAG, "uBlox | handleRaw | ERROR:  " + e.getMessage());
				logFiles.logError("uBlox - handleRaw - Number format exception for sPayloadLen");
			}

			totalLength = LENGTH_HEADER + iPayloadLen + LENGTH_CHKSUM;
			bytesToRead = totalLength - bytesRead;

			if (bytesToRead > 0) {
				do {
					try {
						// numBytesRead =
						// GlobalState.getInputStream().read(receivedBytes, 0,
						// bytesToRead);
						buffer = receivedBytes;
						offset = 0;
						length = bytesToRead;

						oldTime = System.currentTimeMillis();
						readBT = new ReadFromBT();
						r = new Thread(readBT);
						r.start();

						while (isRead == false) {
							// Log.e(TAG,
							// "uBlox | handleRaw|  @@@ isRead 3: @@@"+isRead);
							currentTime = System.currentTimeMillis();
							timeDiff = currentTime - oldTime;
							// Log.e(TAG,
							// "uBlox | handleRaw|  @@@ Time Difference 3: @@@"+timeDiff);
							if (timeDiff > TIMEOUT) {
								Log.e(TAG,
										"uBlox | handleRaw|  @@@ Receive read raw terminated 3 @@@");
								return 5;
							}
						}
						// Log.e(TAG,
						// "uBlox | handleRaw|  @@@ isRead 3: @@@"+isRead);
						isRead = false;
						// ExecutorService executor =
						// Executors.newSingleThreadExecutor();
						// Future<String> future = executor.submit(new
						// ReadFromBT());
						// try {
						// future.get(TIMEOUT, TimeUnit.MILLISECONDS);
						// } catch (TimeoutException e) {
						// Log.e(TAG,
						// "uBlox | handleRaw|  @@@ Receive read raw terminated 2 @@@");
						// return 5;
						// }
						receivedMesage = new byte[numBytesRead];
						receivedMesage = receivedBytes;
						bytesRead = bytesRead + numBytesRead;
						bytesToRead = totalLength - bytesRead;
					} catch (Exception e) {
						Log.e(TAG,
								"uBlox | handleRaw | Receive error 3: "
										+ e.getMessage());
						// log.logError("uBlox - handleRaw - - Receiver disconnected.");
						return -2;
					}
					tmp1 = "";
					tmp1 = UtilsDemoApp.byteToHex(receivedMesage);
					message.append(tmp1.substring(0, numBytesRead * 2));
					numBytesRead = 0;
					Arrays.fill(receivedBytes, (byte) 0);
				} while (bytesRead < totalLength);
			}

			if (bytesRead >= totalLength) {
				Log.i(TAG_RAW, "Complete RAW message: " + message.toString());
				char[] receiverMessage = new char[totalLength];
				recvMessage = message.toString();
				receiverMessage = recvMessage.toCharArray();
				parseRaw(receiverMessage);
				receiverMessage = null;
				receivedBytes = null;
				tmp1 = null;
				message = null;
				sPayloadLen = null;
				receivedMesage = null;
			}
		} else {
			Log.e(TAG, "uBlox | handleRaw | Error : gs.getSocket() == null ");
			logFiles.logError("uBlox - handleRaw - Receiver is disconnected");
		}
		return 1;
	}

	/**
	 * parseRaw function
	 * 
	 * This function parses and stores the Raw messages. Also logs the satellite
	 * data messages to a log file.
	 * 
	 * @param output
	 *            The message to be parsed from the receiver.
	 * @return errorNum The variable provides information about the correct
	 *         execution (1) or not (-1).
	 */
	final int parseRaw(char[] output) {
		int h = 12;
		int numSvInt = 0;
		int insNumSvInt = 0;
		double iTowLong = 0;
		double prDoubleValue = 0;
		int svInt = 0;
		int cnInt = 0;
		int countSat_data = 0;
		int countSat_data_notUsed = 0;
		boolean svidCheck = false;
		double[][] rxmRawD = new double[32][4];
	
		logFiles.logReceiverData(String.valueOf(output));

		for (int i = 0; i < 19; i++) {
			for (int j = 0; j < 4; j++) {
				sat_data[i][j] = 0.0;
			  sat_data_notused[i][j] = 0.0;
			}
		}
		for (int i = 0; i < 4; i++)
			for (int j = 0; j < 2; j++)
				sbas_data[i][j] = 0.0;

		String siTow = UtilsDemoApp.charToStringUbl(output, h + 0, h + 7);
		// Log.d(TAG, " | iTow: " + siTow);
		try {
			iTowLong = Long.parseLong(siTow, HEX_BASE);
			// Log.d(TAG, " | iTowLong: " + iTowLong);
			iTowLong = iTowLong / 1000;
		} catch (Exception e) {
			iTowLong = 0;// Log.e(TAG, "uBlox | " + e.getMessage());
			logFiles.logError("uBlox - parseRaw - Number format exception for siTow");
		}

		String numSv = Character.toString(output[h + 12])
				+ Character.toString(output[h + 13]);
		try {
			numSvInt = Integer.parseInt(numSv, HEX_BASE);
		} catch (Exception e) {
			numSvInt = 0; // Log.e(TAG, "uBlox | " + e.getMessage());
			logFiles.logError("uBlox - parseRaw - Number format exception for numSv");
		}
		TotalSatsInView = numSvInt;

		for (int i = 0; i < numSvInt; i++) {

			String pr = UtilsDemoApp.charToStringUbl(output, (h + 32 + 48 * i),
					(h + 47 + 48 * i));
			try {
				long prLongBits = Long.valueOf(pr, HEX_BASE).longValue();
				prDoubleValue = Double.longBitsToDouble(prLongBits);
			} catch (Exception e) {
				prDoubleValue = 0; // Log.e(TAG, "uBlox | " + e.getMessage());
				logFiles.logError("uBlox - parseRaw - Number format exception for pr");
			}

			String sv = Character.toString(output[h + 56 + 48 * i])
					+ Character.toString(output[h + 57 + 48 * i]);
			try {
				svInt = Integer.parseInt(sv, HEX_BASE);
			} catch (Exception e) {
				svInt = 0; // Log.e(TAG, "uBlox | " + e.getMessage());
				logFiles.logError("uBlox - parseRaw - Number format exception for sv");
			}
			if (svInt == 120 || svInt == 126)
				svidCheck = true;

			String cn = Character.toString(output[h + 60 + 48 * i])
					+ Character.toString(output[h + 61 + 48 * i]);
			try {
				cnInt = Integer.parseInt(cn, HEX_BASE);
			} catch (Exception e) {
				cnInt = 0; // Log.e(TAG, "uBlox | " + e.getMessage());
				logFiles.logError("uBlox - parseRaw - Number format exception for cn");
			}
			
			// Increase the insNumSvInt counter only if the satellite
			// has a SNR bigger than
			int test;
			if(cnInt > 30)
				insNumSvInt++;
			else
				test = 0;

			rxmRawD[i][0] = (double) svInt;
			rxmRawD[i][1] = (double) iTowLong;
			rxmRawD[i][2] = prDoubleValue;
			rxmRawD[i][3] = (double) cnInt;
			
			/*
			 * if (svInt == 120){ sbas_data[0][0] = (double) svInt;
			 * sbas_data[0][1] = (double) iTowLong - (double)(prDoubleValue /
			 * 2.99792458E8); } else if (svInt == 126){ sbas_data[1][0] =
			 * (double) svInt; sbas_data[1][1] = (double) iTowLong -
			 * (double)(prDoubleValue / 2.99792458E8); } else if(svInt > 119 &&
			 * svInt < 139){ sbas_data[2][0] = (double) svInt; sbas_data[2][1] =
			 * (double) iTowLong - (double)(prDoubleValue / 2.99792458E8); }
			 */
			if ((svInt <= 37 || (svInt > 119 && svInt < 139 && svIDMT9_ranging == svInt))
          && cnInt > 20 && countSat_data < 19) {
			  
//		  if(svInt == 3)
//			    prDoubleValue = prDoubleValue + 10000;

        sat_data[countSat_data][0] = (double) svInt;
        sat_data[countSat_data][1] = (double) iTowLong;
        sat_data[countSat_data][2] = prDoubleValue;
        sat_data[countSat_data][3] = (double) cnInt;

        if (GlobalState.getSocket() != null){
        if (svInt <= 32) {// GPS satellites only 
          if (ephemerisTable[svInt - 1][1] == 0) {
            /*
             * if (egnos == 1 && egnosSettings == 1 && sisnet == 0)
             * { //Request, read and parse Sfrb messages. if
             * (requestSfrb() == -1) { Log.e(TAG,
             * "uBlox | Error in " + "Request Sfrb ."); } else {
             * if(sat_data[0][1] != 0.0){ egnos_message =
             * String.valueOf((int)sat_data[0][1])+egnosSubframe;
             * storesEgnosMessage(egnos_message,0);//Stores Sfrb
             * messages. } } }
             */
            // Request, read ,parse and store Ephemeris messages.
            requestEph(String.valueOf(svInt));

            // No ephemeris available: disable PRN
            if (ephemerisTable[svInt - 1][1] != 1) 
              sat_data[countSat_data][0] = 0;
          
          } else {
            if ((ephemerisTable[svInt - 1][2] > (iTowLong + 7200) || ephemerisTable[svInt - 1][2] < (iTowLong - 7200))) {

              // Request, read ,parse and store Ephemeris
              // messages.
              requestEph(String.valueOf(svInt));

              // No ephemeris available: disable PRN
              if (ephemerisTable[svInt - 1][1] != 1) 
                sat_data[countSat_data][0] = 0;                     
          }
        }
        }else {//EGNOS satellites
          sat_data_notused[countSat_data_notUsed][0] = (double) svInt;
          sat_data_notused[countSat_data_notUsed][1] = (double) iTowLong;
          sat_data_notused[countSat_data_notUsed][2] = prDoubleValue;
          sat_data_notused[countSat_data_notUsed][3] = (double) cnInt;
          Log.e("Coordinates", "ublox | Satellite Not used: "+sat_data_notused[countSat_data_notUsed][0]);

          countSat_data_notUsed++;
        }       
        }
        countSat_data++;
      }else {   
          if (svInt <= 32) { 
            if (GlobalState.getSocket() != null){
             requestEph(String.valueOf(svInt));
            }
           if (ephemerisTable[svInt - 1][1] == 1) { 
            sat_data_notused[countSat_data_notUsed][0] = (double) svInt;
            sat_data_notused[countSat_data_notUsed][1] = (double) iTowLong;
            sat_data_notused[countSat_data_notUsed][2] = prDoubleValue;
            sat_data_notused[countSat_data_notUsed][3] = (double) cnInt;
            
            Log.e("Coordinates", "ublox | Satellite Not used: "+sat_data_notused[countSat_data_notUsed][0]);            
            countSat_data_notUsed++;
           }
          }else if(svIDMT9_ranging == svInt) {
            sat_data_notused[countSat_data_notUsed][0] = (double) svInt;
            sat_data_notused[countSat_data_notUsed][1] = (double) iTowLong;
            sat_data_notused[countSat_data_notUsed][2] = prDoubleValue;
            sat_data_notused[countSat_data_notUsed][3] = (double) cnInt;
            
            Log.e("Coordinates", "ublox | Satellite Not used: "+sat_data_notused[countSat_data_notUsed][0]);            
            countSat_data_notUsed++;
         }                  
      }
    }

		if (egnos_position == 1) {
			int total = 0;
			for (int i = 0; i < countSat_data; i++) {
				if (sat_data[i][0] > 0) {
					if (sat_data[i][0] < 33.0) {
						if (eph_updated[(int) (sat_data[i][0] - 1)] == 0) {
							String sv_num = String.valueOf(sat_data[i][0]);
							eph_updated[(int) (sat_data[i][0] - 1)] = 1;
							requestEph(sv_num);
							break;
						}
						total = total + eph_updated[(int) (sat_data[i][0] - 1)];
					} else
						total++; // counting for EGNOS satelites
				}
			}
			if (total == countSat_data)
				eph_updated = new int[32];
		}

		if (numSvInt > 4) { // numSVInt >= 4
			//Log.d(TAG_RAW, "numSvInt > 4 " + numSvInt);
			gps = 1;
			if(insNumSvInt > 4)
				insSdkPositionAvailable = 1;
			else
				insSdkPositionAvailable = 0;
			gpsUnavailable = 0;
			if (svidCheck) { // SVID = 120 or 126 and numSVInt > 5
				Log.d(TAG_RAW, "numSvInt " + numSvInt + " svidCheck"
						+ svidCheck);
				egnos = 1;
				sisnet = 0;
				egnosUnavailable = 0;
				sisUnavailable = 0;
			} else { // SVID ! = 120 or 126 and numSVInt >4
				Log.d(TAG_RAW, "numSvInt " + numSvInt + " svidCheck"
						+ svidCheck);
				sisnet = 1;
				egnos = 1;
				egnosUnavailable = 0;
				Log.d(TAG_RAW,
						"SV ID 120 or 126 not available, switch to SISNeT: "
								+ sisnet);
				if (sisnetSettings == 0) {
					if (GlobalState.getisCurrent()
							|| GlobalState.getisTracking()
							&& (egnosSettings == 1)) {
						if (sisUnavailable == 0)
							// display message on UI,
							// "Signal in Space is unavailable.Please turn on SISNeT"
							displayMessage(R.string.signalinSpaceUnavailable);
						sisUnavailable = 1;
					}
				}
			}
		} else if (numSvInt == 4 && !svidCheck) {
			gps = 1;
			if(insNumSvInt == 4)
				insSdkPositionAvailable = 1;
			else
				insSdkPositionAvailable = 0;
			gpsUnavailable = 0;
			egnos = 0;
			Log.d(TAG_RAW, "numSvInt == 4 && !svidCheck, EGNOS not available: "
					+ egnos);
			if (GlobalState.getisCurrent() || GlobalState.getisTracking()
					&& egnosSettings == 1) {
				if (egnosUnavailable == 0)
					displayMessage(R.string.egnosUnAvailable);
				egnosUnavailable = 1;
			}
			logFiles.logError("uBlox - parseRaw - EGNOS position currently not available"
					+ " because number of satellites in view is less than 5 ");
		} else { // numSVInt <= 4 (incl. on
			Log.d(TAG_RAW, "numSvInt " + numSvInt + " svidCheck" + svidCheck);
			gps = 0;
			insSdkPositionAvailable = 0;
			if (GlobalState.getisCurrent() || GlobalState.getisTracking()) {
				if (gpsUnavailable == 0)
					displayMessage(R.string.gpsUnAvailable);
				gpsUnavailable = 1;
			}
			logFiles.logError("uBlox - parseRaw - Position currently not available"
					+ " because number of satellites in view is less than 4 ");
			egnos = 0;
		}
		Log.d(TAG_RAW, "gps|" + gps);
		Log.d(TAG_RAW, "egnos|" + egnos);
		Log.d(TAG_RAW, "sisnet|" + sisnet);

		if (sisnetSettings == 1)
			// indicating SISNeT in Settings is turned ON, after displaying the
			// UI message
			// "Signal in Space currently  unavailable.Please turn on SISNeT."
			sisUnavailable = 0;

		String[] rxmRawS = new String[numSvInt];
		for (int j = 0; j < numSvInt; j++) {
			if (rxmRawD[j] != null) {
				rxmRawS[j] = (Double.valueOf(rxmRawD[j][0])).toString() + " | "
						+ (Double.valueOf(rxmRawD[j][1])).toString() + " | "
						+ (Double.valueOf(rxmRawD[j][2])).toString() + " | "
						+ (Double.valueOf(rxmRawD[j][3])).toString();
			}
		}
		try {
		//	if (GlobalState.getisLogFile() == 1)
			//	logFiles.logRawToSdCard(rxmRawS);
		} catch (Exception e) {
			Log.e(TAG, "uBlox | parseRaw | Unable to create log file:" + e);
		}
		return 1;
	}

	/**
	 * requestEph function
	 * 
	 * The function requests for Ephemeris messages. This function requests
	 * Ephemeris messages for any new satellite vehicle id in view, and also
	 * updates the Ephemeris data for any satellites already in view.
	 * 
	 * @param newsvId
	 *            The array of new Satellite Vehicle ids.
	 * @return The error indicator provides information about the correct
	 *         execution (1) or not (-1).
	 **/
	int requestEph(String newsvId) {
		String completeMessage = "";
		String svId = "";
		if (newsvId != null) {
			if ((int) Double.parseDouble(newsvId) < 16)
				svId = "0"
						+ Integer
								.toHexString((int) Double.parseDouble(newsvId));
			else
				svId = Integer.toHexString((int) Double.parseDouble(newsvId));

			completeMessage = generateMessageEphSv(CLASS_AID + ID_EPH, svId);
			Log.i(TAG, "uBlox - completeMessage: " + completeMessage);
			sendMessageToReceiver(completeMessage);

			try {
				if (handleEph() == -2)
					GlobalState.setSocket(null);
			} catch (Exception e) {
				Log.e(TAG, "uBlox - requestEph| exception: " + e.getMessage());
				logFiles.logError("uBlox - requestEph error: " + e);
				return -1;
			}
		}
		return 1;
	}

	/**
	 * handleEph function
	 * 
	 * The function reads messages from the Bluetooth receiver for classid 0B31
	 * which is for Ephemeris messages.
	 **/
	int handleEph() {
		byte[] receivedHeader = new byte[10];
		byte[] receivedBytes = new byte[400];
		byte[] receivedMesage = new byte[10];
		int numBytesRead = 0;
		int bytesRead = 0;
		int bytesToRead = 0;
		int iPayloadLen = 0;
		int totalLength = 0;
		String tmp1 = "";
		StringBuilder message = new StringBuilder();
		String sPayloadLen = "";
		if (GlobalState.getSocket() != null) {
			try {
				// Log.i(TAG,
				// "uBlox | handleEph |  @@@ Receive read eph start @@@");
				numBytesRead = GlobalState.getInputStream().read(
						receivedHeader, 0, (LENGTH_HEADER));
				// Log.i(TAG,
				// "uBlox | handleEph |  @@@ Receive read eph end @@@");
				bytesRead = numBytesRead;
			} catch (Exception e) {
				Log.e(TAG,
						"uBlox | handleEph | Receive error: " + e.getMessage());
				logFiles.logError("uBlox - handleEph - Receiver disconnected.");
				return -2;
			}

			if (bytesRead < LENGTH_HEADER) {
				do {
					tmp1 = "";
					tmp1 = UtilsDemoApp.byteToHex(receivedHeader);
					message.append(tmp1.substring(0, (numBytesRead * 2)));
					numBytesRead = 0;
					Arrays.fill(receivedHeader, (byte) 0);
					bytesToRead = LENGTH_HEADER - bytesRead;
					if (bytesToRead > 0) {
						try {
							numBytesRead = GlobalState.getInputStream().read(
									receivedHeader, 0, bytesToRead);
							bytesRead = bytesRead + numBytesRead;
							tmp1 = "";
							tmp1 = UtilsDemoApp.byteToHex(receivedHeader);
							message.append(tmp1
									.substring(0, (numBytesRead * 2)));
							sPayloadLen = UtilsDemoApp.charToStringUbl2(
									message.toString(), 8, 11);
							numBytesRead = 0;
							Arrays.fill(receivedHeader, (byte) 0);
						} catch (Exception e) {
							Log.e(TAG, "uBlox | handleEph | Receive error: "
									+ e.getMessage());
							logFiles.logError("uBlox - handleEph - Receiver disconnected.");
							return -2;
						}
					}
				} while (bytesRead < LENGTH_HEADER);
				receivedHeader = null;
			} else {
				tmp1 = "";
				;
				tmp1 = UtilsDemoApp.byteToHex(receivedHeader);
				message.append(tmp1.substring(0, (numBytesRead * 2)));
				sPayloadLen = UtilsDemoApp.charToStringUbl2(message.toString(),
						8, 11);
				numBytesRead = 0;
				Arrays.fill(receivedBytes, (byte) 0);
				receivedHeader = null;
			}

			try {
				iPayloadLen = Integer.parseInt(sPayloadLen, HEX_BASE);
			} catch (Exception e) {
				iPayloadLen = 0;
				Log.e(TAG, "uBlox | handleEph | ERROR:  " + e.getMessage());
				logFiles.logError("uBlox - handleEph - Number format exception for sPayloadLen");
			}

			totalLength = LENGTH_HEADER + iPayloadLen + LENGTH_CHKSUM;
			bytesToRead = totalLength - bytesRead;

			if (bytesToRead > 0) {
				do {
					try {
						numBytesRead = GlobalState.getInputStream().read(
								receivedBytes, 0, bytesToRead);
						receivedMesage = new byte[numBytesRead];
						receivedMesage = receivedBytes;
						bytesRead = bytesRead + numBytesRead;
						bytesToRead = totalLength - bytesRead;
					} catch (Exception e) {
						Log.e(TAG,
								"uBlox | handleEph | Receive error: "
										+ e.getMessage());
						logFiles.logError("uBlox - handleEph - Receiver disconnected.");
						return -2;
					}
					tmp1 = "";
					tmp1 = UtilsDemoApp.byteToHex(receivedMesage);
					message.append(tmp1.substring(0, numBytesRead * 2));
					numBytesRead = 0;
					Arrays.fill(receivedBytes, (byte) 0);
				} while (bytesRead < totalLength);
			}

			if (bytesRead >= totalLength) {
				Log.i(TAG_EPH, "Complete EPH message: " + message.toString());
				char[] receiverMessage = new char[totalLength];
				recvMessage = message.toString();
				receiverMessage = recvMessage.toCharArray();
				parseEphSv(receiverMessage);
				receiverMessage = null;
				receivedBytes = null;
				tmp1 = null;
				message = null;
				sPayloadLen = null;
				receivedMesage = null;
			}
		} else {
			Log.e(TAG, "uBlox | handleEph | Error : gs.getSocket() == null ");
			logFiles.logError("uBlox - handleEph - Receiver is disconnected.");
		}
		return 1;
	}

	/**
	 * parseEphSv function
	 * 
	 * The function parses the Ephemeris data.
	 * 
	 * @param message
	 *            The message to be parsed from the receiver.
	 * @return errorNum The variable provides information about the correct
	 *         execution (1) or not (-1).
	 **/
	static final int parseEphSv(char[] message) {
		int h = 12;
		int iPayloadLen = 0;
		long svId = 0;
		long how = 0;
		long lSftmp = 0;
		String sPayloadLen = "";
		String prnId = "";
		String sSfTmp = "";
		String sSfTmpTmp = "";
		String sSfBin = "";
		double towEph = 0;
		String sHowbin = "";
		
    logFiles.logReceiverData(String.valueOf(message));
		

	//	if (GlobalState.getSocket() != null) {
			sPayloadLen = UtilsDemoApp.charToStringUbl(message, 8, 11);
			try {
				iPayloadLen = Integer.parseInt(sPayloadLen, HEX_BASE);
			} catch (Exception e) {
				Log.e(TAG, "uBlox | parseEphSv | " + e.getMessage());
				logFiles.logError("uBlox - parseEphSv - Number format exception for sPayloadLen");
				return -1;
			}

			String sSvId = UtilsDemoApp.charToStringUbl(message, h + 0, h + 7);
			try {
				svId = Long.parseLong(sSvId, HEX_BASE);
			} catch (Exception e) {
				svId = 0; // Log.e(TAG, "uBlox | parseEphSv | " +
							// e.getMessage());
				logFiles.logError("uBlox - parseEphSv - Number format exception for svId");
			}

			String sHow = UtilsDemoApp.charToStringUbl(message, h + 8, h + 15);
			try {
				how = Long.parseLong(sHow, HEX_BASE);
				sHowbin = Long.toBinaryString(how);
				while (sHowbin.length() < 32) {
					sHowbin = "0" + sHowbin;
				}
				towEph = Long.parseLong(sHowbin.substring(8, 25), BIN_BASE) * 6;
			} catch (Exception e) {
				how = 0; // Log.e(TAG, "uBlox | parseEphSv | " +
							// e.getMessage());
				logFiles.logError("uBlox - parseEphSv - Number format exception for towEph");
			}

			if (iPayloadLen > 10) {
				for (int sf = 1; sf < 4; sf++) {
					sSf[sf] = "";
					for (int i = 0; i < 8; i++) {
						sSfTmpTmp = UtilsDemoApp.charToStringUbl(message, (h
								+ 16 + (sf - 1) * 64 + i * 8), (h + 23
								+ (sf - 1) * 64 + i * 8));
						sSfTmp = sSfTmp + sSfTmpTmp; // complete message as
														// string
						try {
							lSftmp = Long.parseLong(sSfTmp, HEX_BASE);
						} catch (Exception e) {
							svId = 0;
							Log.e(TAG, "uBlox | parseEphSv | " + e.getMessage());
							logFiles.logError("uBlox - parseEphSv - Number format exception for sSfTmp");
							return -1;
						}
						sSfBin = Long.toBinaryString(lSftmp);
						lSftmp = 0;
						sSfTmp = "";
						sSfTmpTmp = "";
						while (sSfBin.length() < 32) {
							sSfBin = "0" + sSfBin;
						}
						sSf[sf] = sSf[sf] + sSfBin;
						sSfBin = "";
					}
				}

				StringBuffer zeroBits = new StringBuffer();
				for (int zero = 0; zero < 60; zero++) {
					zeroBits.append('0');
				}

				for (int sf = 1; sf < 4; sf++) {
					String sSf_ = "";
					int count = 0;
					for (int in = 0; in < 256; in++) {
						if (in == (count * 32)) {
							sSf_ = sSf_
									+ sSf[sf].substring((in + 8), (in + 32))
									+ "000000";
							count++;
						}
					}

					sSf[sf] = sSf_;
					sSf[sf] = zeroBits + sSf[sf];
				}

				if (svId <= 9)
					prnId = '0' + String.valueOf(svId);
				else
					prnId = String.valueOf(svId);

				ephemSubFrame = prnId + sSf[1] + sSf[2] + sSf[3];

				int svID = (int) svId;

				try {
					int iode_cur = Integer.valueOf(
							ephemSubFrame.substring(360 + 2, 368 + 2), 2);

					if (iode_old[svID - 1] != iode_cur) {
						iode_old[svID - 1] = iode_cur;

						if (eph_set[svID - 1] < 4)
							eph_set[svID - 1] = eph_set[svID - 1] + 1;

						for (int i = eph_set[svID - 1]; i > 0; i--)
							if (ephemTable[svID - 1][i - 1] != null)
								ephemTable[svID - 1][i] = ephemTable[svID - 1][i - 1];

						ephemTable[svID - 1][0] = ephemSubFrame;

					}

					ephemData[svID - 1] = "" + eph_set[svID - 1] + prnId;
					for (int i = 0; i < eph_set[svID - 1]; i++)
						if (ephemTable[svID - 1][i] != null)
							ephemData[svID - 1] += ephemTable[svID - 1][i]
									.substring(2, 902);
				} catch (Exception e) {
					Log.e(TAG,
							"uBlox | parseEphSv | ERROR Creating the set of Ephemeris Data:"
									+ e);
				}

				ephemerisTable[svID - 1][1] = 1;
				ephemerisTable[svID - 1][2] = towEph;// Ephem. valid in seconds
				Log.d(TAG, "uBlox | parseEphSv | ephemSubFrame :"
						+ ephemData[svID - 1]);

				try {
//					if (GlobalState.getisLogFile() == 1)
//						logFiles.logEgnosToSdCard("SV ID : " + svId + ", "
//								+ "HOW:" + how, "\n" + "Ephemeris Data: "
//								+ eph_set[svID - 1] + "" + ephemSubFrame);
				} catch (Exception e) {
					Log.e(TAG,
							"uBlox | parseEphSv | Unable to create log file:"
									+ e);
				}
			} else {
				try {
//					if (GlobalState.getisLogFile() == 1)
//						logFiles.logEgnosToSdCard("SV ID : " + svId + ", "
//								+ "HOW:" + how, "\n"
//								+ "Ephemeris Data: No Data Available");
				} catch (Exception e) {
					Log.e(TAG,
							"uBlox | parseEphSv | Unable to create log file:"
									+ e);
				}
			}
//		} else
//			return -1;
		return 1;
	}

	/**
	 * requestSfrb function
	 * 
	 * First generates the request message send to the receiver and then this
	 * function handles the EGNOS raw messages through a call of handleSfrb.
	 * 
	 * @return Error identifier (1, -1).
	 */
	int requestSfrb() {
		int ret;
		String completeMessage = "";
		completeMessage = generateMessage(CLASS_RXM + ID_SFRB);
		sendMessageToReceiver(completeMessage);

		try {
			ret = handleSfrb();
			if (ret == -2)
				GlobalState.setSocket(null);
		} catch (Exception e) {
			Log.e(TAG, "uBlox | RequestSfrb error. (" + e.getMessage() + ")");
			logFiles.logError("uBlox - RequestSfrb error: " + e);
			ret = -1;
		}
		return ret;
	}

	/**
	 * handleSfrb function
	 * 
	 * The function reads messages from the Bluetooth receiver for classid 0211
	 * which is for Sfrb messages. .
	 **/
	int handleSfrb() {
		byte[] receivedHeader = new byte[10];
		byte[] receivedBytes = new byte[400];
		byte[] receivedMesage = new byte[10];
		// int numBytesRead = 0;
		int bytesRead = 0;
		int bytesToRead = 0;
		int iPayloadLen = 0;
		int totalLength = 0;
		String tmp1 = "";
		StringBuilder message = new StringBuilder();
		String sPayloadLen = "";
		long currentTime = 0;
		long oldTime = 0;
		long timeDiff = 0;
		ReadFromBT readBT;
		isRead = false;
		Thread r;

		if (GlobalState.getSocket() != null) {
			try {

				// numBytesRead =
				// GlobalState.getInputStream().read(receivedHeader, 0,
				// (LENGTH_HEADER));
				buffer = receivedHeader;
				offset = 0;
				length = LENGTH_HEADER;

				oldTime = System.currentTimeMillis();
				readBT = new ReadFromBT();
				r = new Thread(readBT);
				r.start();

				while (isRead == false) {
					// Log.e(TAG,
					// "uBlox | handleSfrb|  @@@ isRead 1: @@@"+isRead);
					currentTime = System.currentTimeMillis();
					timeDiff = currentTime - oldTime;
					// Log.e(TAG,
					// "uBlox | handleSfrb|  @@@ Time Difference 1: @@@"+timeDiff);
					if (timeDiff > TIMEOUT) {
						Log.e(TAG,
								"uBlox | handleSfrb|  @@@ Receive read sfrb terminated 1 @@@");
						return 5;
					}
				}
				// Log.e(TAG, "uBlox | handleSfrb|  @@@ isRead 1: @@@"+isRead);
				isRead = false;
				// ExecutorService executor =
				// Executors.newSingleThreadExecutor();
				// Future<String> future = executor.submit(new ReadFromBT());
				// try {
				// // Log.e(TAG,
				// "uBlox | handleSfrb|  @@@ Receive read sfrb started 1 @@@");
				// future.get(TIMEOUT, TimeUnit.MILLISECONDS);
				// // Log.e(TAG,
				// "uBlox | handleSfrb|  @@@ Receive read sfrb finished 1 @@@");
				// } catch (TimeoutException e) {
				// Log.e(TAG,
				// "uBlox | handleSfrb|  @@@ Receive read sfrb terminated 1 @@@");
				// return 5;
				// }

				bytesRead = numBytesRead;
				time_sfrb = System.currentTimeMillis();
			} catch (Exception e) {
				Log.e(TAG,
						"uBlox | handleSfrb | Receive error1 : "
								+ e.getMessage());
				// log.logError("uBlox - handleSfrb - Receiver disconnected.");
				return -2;
			}

			if (bytesRead < LENGTH_HEADER) {
				do {
					tmp1 = "";
					tmp1 = UtilsDemoApp.byteToHex(receivedHeader);
					// Log.e(TAG_SFRB, "1 tmp1: " + tmp1 );
					message.append(tmp1.substring(0, (numBytesRead * 2)));
					// Log.e(TAG_SFRB, "1 message: " + message );
					numBytesRead = 0;
					Arrays.fill(receivedHeader, (byte) 0);
					bytesToRead = LENGTH_HEADER - bytesRead;
					if (bytesToRead > 0) {
						try {
							// numBytesRead =
							// GlobalState.getInputStream().read(receivedHeader,
							// 0,
							// bytesToRead);
							buffer = receivedHeader;
							offset = 0;
							length = bytesToRead;
							oldTime = System.currentTimeMillis();
							readBT = new ReadFromBT();
							r = new Thread(readBT);
							r.start();

							while (isRead == false) {
								// Log.e(TAG,
								// "uBlox | handleSfrb|  @@@ isRead 2: @@@"+isRead);
								currentTime = System.currentTimeMillis();
								timeDiff = currentTime - oldTime;
								// Log.e(TAG,
								// "uBlox | handleSfrb|  @@@ Time Difference 2: @@@"+timeDiff);
								if (timeDiff > TIMEOUT) {
									Log.e(TAG,
											"uBlox | handleSfrb|  @@@ Receive read sfrb terminated 3 @@@");
									return 5;
								}
							}
							// Log.e(TAG,
							// "uBlox | handleSfrb|  @@@ isRead 2: @@@"+isRead);
							isRead = false;
							// ExecutorService executor =
							// Executors.newSingleThreadExecutor();
							// Future<String> future = executor.submit(new
							// ReadFromBT());
							// try {
							// // Log.e(TAG,
							// "uBlox | handleSfrb|  @@@ Receive read sfrb started 2 @@@");
							// future.get(TIMEOUT, TimeUnit.MILLISECONDS);
							// // Log.e(TAG,
							// "uBlox | handleSfrb|  @@@ Receive read sfrb finished 2 @@@");
							// } catch (TimeoutException e) {
							// Log.e(TAG,
							// "uBlox | handleSfrb|  @@@ Receive read sfrb terminated 2 @@@");
							// return 5;
							// }
							bytesRead = bytesRead + numBytesRead;
							tmp1 = "";
							tmp1 = UtilsDemoApp.byteToHex(receivedHeader);
							// Log.e(TAG_SFRB, "2 tmp1: " + tmp1 );
							message.append(tmp1
									.substring(0, (numBytesRead * 2)));
							// Log.e(TAG_SFRB, "2 message: " + message );
							sPayloadLen = UtilsDemoApp.charToStringUbl2(
									message.toString(), 8, 11);
							numBytesRead = 0;
							Arrays.fill(receivedHeader, (byte) 0);
						} catch (Exception e) {
							Log.e(TAG, "uBlox | handleSfrb | Receive error: 2 "
									+ e.getMessage());
							// log.logError("uBlox - handleSfrb - Receiver disconnected.");
							return -2;
						}
					}
				} while (bytesRead < LENGTH_HEADER);
				receivedHeader = null;
			} else {
				tmp1 = "";
				tmp1 = UtilsDemoApp.byteToHex(receivedHeader);
				// Log.e(TAG_SFRB, "3 tmp1: " + tmp1 );
				message.append(tmp1.substring(0, (numBytesRead * 2)));
				// Log.e(TAG_SFRB, "3 message: " + message );
				sPayloadLen = UtilsDemoApp.charToStringUbl2(message.toString(),
						8, 11);
				numBytesRead = 0;
				Arrays.fill(receivedBytes, (byte) 0);
				receivedHeader = null;
			}

			try {
				iPayloadLen = Integer.parseInt(sPayloadLen, HEX_BASE);
			} catch (Exception e) {
				iPayloadLen = 0;
				Log.e(TAG, "uBlox | handleSfrb | ERROR:  " + e.getMessage());
				// log.logError("uBlox - handleEph - Number format exception for sPayloadLen");
			}

			totalLength = LENGTH_HEADER + iPayloadLen + LENGTH_CHKSUM;
			bytesToRead = totalLength - bytesRead;

			if (bytesToRead > 0) {
				do {
					try {
						// numBytesRead =
						// GlobalState.getInputStream().read(receivedBytes, 0,
						// bytesToRead);

						buffer = receivedBytes;
						offset = 0;
						length = bytesToRead;

						// ExecutorService executor =
						// Executors.newSingleThreadExecutor();
						// Future<String> future = executor.submit(new
						// ReadFromBT());
						// try {
						// // Log.e(TAG,
						// "uBlox | handleSfrb|  @@@ Receive read sfrb started 3 @@@");
						// future.get(TIMEOUT, TimeUnit.MILLISECONDS);
						// // Log.e(TAG,
						// "uBlox | handleSfrb|  @@@ Receive read sfrb finished 3 @@@");
						// } catch (TimeoutException e) {
						// Log.e(TAG,
						// "uBlox | handleSfrb|  @@@ Receive read sfrb terminated 3 @@@");
						// return 5;
						// }

						oldTime = System.currentTimeMillis();
						readBT = new ReadFromBT();
						r = new Thread(readBT);
						r.start();

						while (isRead == false) {
							// Log.e(TAG,
							// "uBlox | handleSfrb|  @@@ isRead 3: @@@"+isRead);
							currentTime = System.currentTimeMillis();
							timeDiff = currentTime - oldTime;
							// Log.e(TAG,
							// "uBlox | handleSfrb|  @@@ Time Difference 3: @@@"+timeDiff);
							if (timeDiff > TIMEOUT) {
								Log.e(TAG,
										"uBlox | handleSfrb|  @@@ Receive read sfrb terminated 3 @@@");
								return 5;
							}
						}
						// Log.e(TAG,
						// "uBlox | handleSfrb|  @@@ isRead 3: @@@"+isRead);
						isRead = false;
						receivedMesage = new byte[numBytesRead];
						receivedMesage = receivedBytes;
						bytesRead = bytesRead + numBytesRead;
						bytesToRead = totalLength - bytesRead;
					} catch (Exception e) {
						Log.e(TAG,
								"uBlox | handleSfrb | Receive error 3: "
										+ e.getMessage());
						// log.logError("uBlox - handleSfrb - Receiver disconnected.");
						return -2;
					}
					tmp1 = "";
					tmp1 = UtilsDemoApp.byteToHex(receivedMesage);
					// Log.e(TAG_SFRB, "4 tmp1: " + tmp1 );
					message.append(tmp1.substring(0, numBytesRead * 2));
					// Log.e(TAG_SFRB, "4 message: " + message );
					numBytesRead = 0;
					Arrays.fill(receivedBytes, (byte) 0);
				} while (bytesRead < totalLength);
			}

			if (bytesRead >= totalLength) {
				Log.i(TAG_SFRB, "Complete SFRB message: " + message.toString());
				recvMessage = "";
				recvMessage = message.toString();
				parseSfrbS(recvMessage);
				receivedBytes = null;
				tmp1 = null;
				message = null;
				sPayloadLen = null;
				receivedMesage = null;
			}
		} else {
			Log.e(TAG, "uBlox | handleSfrb | Error : gs.getSocket() == null ");
			logFiles.logError("uBlox - handleSfrb - Receiver is disconnected. ");
		}
		return 1;
	}

	/**
	 * parseSfrbS function
	 * 
	 * This function parses and stores the Sfrb messages. Also logs the Egnos
	 * Mts and Egnos Subframe messages to a log file.
	 * 
	 * @param message
	 *            The message to be parsed from the receiver.
	 * @return errorNum The variable provides information about the correct
	 *         execution (1) or not (-1).
	 */
	final int parseSfrbS(String message) {
		String egnosSf = "";
		String preamble = "";
		String egnosMt = "";
		String payload = "";
		String parity = "";
		int iEgnosMt = 0;
		String Sword = "";
		String EgnosSfrb = "";
		egnosSubframe = "";

		logFiles.logReceiverData(message);
		
		// Log.d(TAG_SFRB, "Complete SFRB message: " + message);
		// Log.d(TAG_SFRB, "HEX: 0123 - BIN: " + UtilsDemoApp.hexToBin("0123"));

		svId = Integer.parseInt(message.substring(14, 16), HEX_BASE);
		if (svId == 120 || svId == 126) {
			for (int i = 0; i < 8; i++) {
				Sword = UtilsDemoApp.charToStringUbl2(message,
						(12 + 4 + 8 * i), (12 + 11 + 8 * i));
				EgnosSfrb = EgnosSfrb + Sword;
				if (i < 7) {
					egnosSf = egnosSf + UtilsDemoApp.hexToBin(Sword);
				} else {
					String tmp = UtilsDemoApp.hexToBin(Sword);
					int length = tmp.length();
					tmp = tmp + "0";
					egnosSf = egnosSf + tmp.substring(6, length) + "000000";
				}
			}

			preamble = egnosSf.substring(0, 8);
			if (!preamble.equalsIgnoreCase("01010011")
					&& !preamble.equalsIgnoreCase("10011010")
					&& !preamble.equalsIgnoreCase("11000110"))
				Log.e(TAG_SFRB, "ERROR Preamble :" + preamble);
			egnosMt = egnosSf.substring(8, 14);
			egnosMts = egnosMt;
			payload = egnosSf.substring(14, 226);
			egnosSf = egnosSf + "0";
			parity = egnosSf.substring(226, 256);
			iEgnosMt = Integer.parseInt(egnosMt, BIN_BASE);
			egnosSubframe = preamble + egnosMt + payload + parity;
			String egnosSubframeHex = UtilsDemoApp.binToHex(egnosSubframe);

			switch (iEgnosMt) {
			case 1:
				cnt1++;
				Log.d(TAG_SFRB, "uBlox |  parseSfrb | MT: " + iEgnosMt
						+ " cnt: " + cnt1 + "\n     EGNOS 256 bits: "
						+ egnosSubframe + "\n          EGNOS msg: "
						+ egnosSubframeHex);
				break;
			case 2:
				cnt2++;
				Log.d(TAG_SFRB, "uBlox |  parseSfrb | MT: " + iEgnosMt
						+ " cnt: " + cnt2 + "\n     EGNOS 256 bits: "
						+ egnosSubframe + "\n          EGNOS msg: "
						+ egnosSubframeHex);
				break;
			case 3:
				cnt3++;
				Log.d(TAG_SFRB, "uBlox |  parseSfrb | MT: " + iEgnosMt
						+ " cnt: " + cnt3 + "\n     EGNOS 256 bits: "
						+ egnosSubframe + "\n          EGNOS msg: "
						+ egnosSubframeHex);
				break;
			case 4:
				cnt4++;
				Log.d(TAG_SFRB, "uBlox |  parseSfrb | MT: " + iEgnosMt
						+ " cnt: " + cnt4 + "\n     EGNOS 256 bits: "
						+ egnosSubframe + "\n          EGNOS msg: "
						+ egnosSubframeHex);
				break;
			case 5:
				cnt5++;
				Log.d(TAG_SFRB, "uBlox |  parseSfrb | MT: " + iEgnosMt
						+ " cnt: " + cnt5 + "\n     EGNOS 256 bits:"
						+ egnosSubframe + "\n          EGNOS msg: "
						+ egnosSubframeHex);
				break;
			case 6:
				cnt6++;
				Log.d(TAG_SFRB, "uBlox |  parseSfrb | MT: " + iEgnosMt
						+ " cnt: " + cnt6 + "\n     EGNOS 256 bits:"
						+ egnosSubframe + "\n          EGNOS msg: "
						+ egnosSubframeHex);
				break;
			case 7:
				cnt7++;
				Log.d(TAG_SFRB, "uBlox |  parseSfrb | MT: " + iEgnosMt
						+ " cnt: " + cnt7 + "\n     EGNOS 256 bits:"
						+ egnosSubframe + "\n          EGNOS msg: "
						+ egnosSubframeHex);
				break;
			case 9:
				cnt9++;
				Log.d(TAG_SFRB, "uBlox |  parseSfrb | MT: " + iEgnosMt
						+ " cnt: " + cnt9 + "\n     EGNOS 256 bits:"
						+ egnosSubframe + "\n          EGNOS msg: "
						+ egnosSubframeHex);
				break;
			case 10:
				cnt10++;
				Log.d(TAG_SFRB, "uBlox |  parseSfrb | MT: " + iEgnosMt
						+ " cnt: " + cnt10 + "\n     EGNOS 256 bits:"
						+ egnosSubframe + "\n          EGNOS msg: "
						+ egnosSubframeHex);
				break;
			case 12:
				cnt12++;
				Log.d(TAG_SFRB, "uBlox |  parseSfrb | MT: " + iEgnosMt
						+ " cnt: " + cnt12 + "\n     EGNOS 256 bits:"
						+ egnosSubframe + "\n          EGNOS msg: "
						+ egnosSubframeHex);
				break;
			case 17:
				cnt17++;
				Log.d(TAG_SFRB, "uBlox |  parseSfrb | MT: " + iEgnosMt
						+ " cnt: " + cnt17 + "\n     EGNOS 256 bits:"
						+ egnosSubframe + "\n          EGNOS msg: "
						+ egnosSubframeHex);
				break;
			case 18:
				cnt18++;
				Log.d(TAG_SFRB, "uBlox |  parseSfrb | MT: " + iEgnosMt
						+ " cnt: " + cnt18 + "\n     EGNOS 256 bits:"
						+ egnosSubframe + "\n          EGNOS msg: "
						+ egnosSubframeHex);
				break;
			case 24:
				cnt24++;
				Log.d(TAG_SFRB, "uBlox |  parseSfrb | MT: " + iEgnosMt
						+ " cnt: " + cnt24 + "\n     EGNOS 256 bits:"
						+ egnosSubframe + "\n          EGNOS msg: "
						+ egnosSubframeHex);
				break;
			case 25:
				cnt25++;
				Log.d(TAG_SFRB, "uBlox |  parseSfrb | MT: " + iEgnosMt
						+ " cnt: " + cnt25 + "\n     EGNOS 256 bits:"
						+ egnosSubframe + "\n          EGNOS msg: "
						+ egnosSubframeHex);
				break;
			case 26:
				cnt26++;
				Log.d(TAG_SFRB, "uBlox |  parseSfrb | MT: " + iEgnosMt
						+ " cnt: " + cnt26 + "\n     EGNOS 256 bits:"
						+ egnosSubframe + "\n          EGNOS msg: "
						+ egnosSubframeHex);
				break;
			case 27:
				cnt27++;
				Log.d(TAG_SFRB, "uBlox |  parseSfrb | MT: " + iEgnosMt
						+ " cnt: " + cnt27 + "\n     EGNOS 256 bits:"
						+ egnosSubframe + "\n          EGNOS msg: "
						+ egnosSubframeHex);
				break;
			}
			try {
//				if (GlobalState.getisLogFile() == 1) {
//					logFiles.logEgnosToSdCard("\nEGNOS Mts: " + iEgnosMt
//							+ "Sv ID: " + svId, "EGNOS Subframe: "
//							+ egnosSubframe);
//					logFiles.logEgnosToSdCard("\nEGNOS Mts: " + iEgnosMt,
//							"\nEGNOS Hex: " + egnosSubframeHex);
//				}
			} catch (Exception e) {
				Log.e(TAG, "uBlox | parseSfrbS | Unable to create log file:"
						+ e);
			}
		} else if (svId == 124) {
			Log.d(TAG_DEBUG, "uBlox | parseSfrbS | EGNOS Satellite only for "
					+ "testing; not considered.");
		} else if (svId == 133 || svId == 134 || svId == 138) {
			Log.d(TAG_DEBUG,
					"uBlox | parseSfrbS | WAAS Satellite; not considered.");
		} else if (svId == 129 || svId == 137) {
			Log.d(TAG_DEBUG,
					"uBlox | parseSfrbS | MSAS Satellite; not considered.");
		} else if (svId >= 1 && svId <= 32) {
			Log.d(TAG_DEBUG, "uBlox | parseSfrbS | GPS Satellite.");
		} else {
			Log.e(TAG_DEBUG, "uBlox | parseSfrbS | Unknown PRN.");
		}
		return 1;
	}

	/**
	 * requestHUI function
	 * 
	 * The function requests for a Health UTC Ionosphere message.
	 **/
	int requestHUI() {
		String completeMessage = "";

		completeMessage = generateMessage(CLASS_AID + ID_HUI);
		sendMessageToReceiver(completeMessage);

		try {
			if (handleHUI() == -2)
				GlobalState.setSocket(null);
		} catch (Exception e) {
			logFiles.logError("uBlox - requestEph error: " + e);
			return -1;
		}
		return 1;
	}

	/**
	 * handleHUI function
	 * 
	 * The function reads messages from the Bluetooth receiver for class id 0B02
	 * which is for HUI messages.
	 **/
	int handleHUI() {
		byte[] receivedHeader = new byte[10];
		byte[] receivedBytes = new byte[400];
		byte[] receivedMesage = new byte[10];
		int numBytesRead = 0;
		int bytesRead = 0;
		int bytesToRead = 0;
		int iPayloadLen = 0;
		int totalLength = 0;
		String tmp1 = "";
		StringBuilder message = new StringBuilder();
		String sPayloadLen = "";

		if (GlobalState.getSocket() != null) {
			try {
				// Log.i(TAG,
				// "uBlox | handleHUI|  @@@ Receive read hui start @@@");
				numBytesRead = GlobalState.getInputStream().read(
						receivedHeader, 0, (LENGTH_HEADER));
				// Log.i(TAG,
				// "uBlox | handleHUI |  @@@ Receive read hui end @@@");
				bytesRead = numBytesRead;
			} catch (Exception e) {
				Log.e(TAG,
						"uBlox | handleHUI | Receive error: " + e.getMessage());
				logFiles.logError("uBlox - handleHUI - Receiver disconnected.");
				return -2;
			}

			if (bytesRead < LENGTH_HEADER) {
				do {
					tmp1 = "";
					tmp1 = UtilsDemoApp.byteToHex(receivedHeader);
					// Log.e(TAG_SFRB, "1 tmp1: " + tmp1 );
					message.append(tmp1.substring(0, (numBytesRead * 2)));
					// Log.e(TAG_SFRB, "1 message: " + message );
					numBytesRead = 0;
					Arrays.fill(receivedHeader, (byte) 0);
					bytesToRead = LENGTH_HEADER - bytesRead;
					if (bytesToRead > 0) {
						try {
							numBytesRead = GlobalState.getInputStream().read(
									receivedHeader, 0, bytesToRead);
							bytesRead = bytesRead + numBytesRead;
							tmp1 = "";
							tmp1 = UtilsDemoApp.byteToHex(receivedHeader);
							// Log.e(TAG_SFRB, "2 tmp1: " + tmp1 );
							message.append(tmp1
									.substring(0, (numBytesRead * 2)));
							// Log.e(TAG_SFRB, "2 message: " + message );
							sPayloadLen = UtilsDemoApp.charToStringUbl2(
									message.toString(), 8, 11);
							numBytesRead = 0;
							Arrays.fill(receivedHeader, (byte) 0);
						} catch (Exception e) {
							Log.e(TAG, "uBlox | handleSfrb | Receive error: "
									+ e.getMessage());
							logFiles.logError("uBlox - handleSfrb - Receiver disconnected.");
							return -2;
						}
					}
				} while (bytesRead < LENGTH_HEADER);
				receivedHeader = null;
			} else {
				tmp1 = "";
				tmp1 = UtilsDemoApp.byteToHex(receivedHeader);
				// Log.e(TAG_SFRB, "3 tmp1: " + tmp1 );
				message.append(tmp1.substring(0, (numBytesRead * 2)));
				// Log.e(TAG_SFRB, "3 message: " + message );
				sPayloadLen = UtilsDemoApp.charToStringUbl2(message.toString(),
						8, 11);
				numBytesRead = 0;
				Arrays.fill(receivedBytes, (byte) 0);
				receivedHeader = null;
			}

			try {
				iPayloadLen = Integer.parseInt(sPayloadLen, HEX_BASE);
			} catch (Exception e) {
				iPayloadLen = 0;
				Log.e(TAG, "uBlox | handleSfrb | ERROR:  " + e.getMessage());
				logFiles.logError("uBlox - handleEph - Number format exception for sPayloadLen");
			}

			totalLength = LENGTH_HEADER + iPayloadLen + LENGTH_CHKSUM;
			bytesToRead = totalLength - bytesRead;

			if (bytesToRead > 0) {
				do {
					try {
						numBytesRead = GlobalState.getInputStream().read(
								receivedBytes, 0, bytesToRead);
						receivedMesage = new byte[numBytesRead];
						receivedMesage = receivedBytes;
						bytesRead = bytesRead + numBytesRead;
						bytesToRead = totalLength - bytesRead;
					} catch (Exception e) {
						Log.e(TAG,
								"uBlox | handleSfrb | Receive error: "
										+ e.getMessage());
						logFiles.logError("uBlox - handleSfrb - Receiver disconnected.");
						return -2;
					}
					tmp1 = "";
					tmp1 = UtilsDemoApp.byteToHex(receivedMesage);
					// Log.e(TAG_SFRB, "4 tmp1: " + tmp1 );
					message.append(tmp1.substring(0, numBytesRead * 2));
					// Log.e(TAG_SFRB, "4 message: " + message );
					numBytesRead = 0;
					Arrays.fill(receivedBytes, (byte) 0);
				} while (bytesRead < totalLength);
			}

			if (bytesRead >= totalLength) {
				Log.i(TAG_SFRB, "Complete SFRB message: " + message.toString());
				recvMessage = "";
				recvMessage = message.toString();
				parseHUI(recvMessage);
				receivedBytes = null;
				tmp1 = null;
				message = null;
				sPayloadLen = null;
				receivedMesage = null;
			}
		} else {
			Log.e(TAG, "uBlox | handleSfrb | Error : gs.getSocket() == null ");
			logFiles.logError("uBlox - handleSfrb - Receiver is disconnected. ");
		}
		return 1;
	}

	/**
	 * parseHUI function
	 * 
	 * This function parses and stores the HUI messages.
	 * 
	 * @param message
	 *            The message to be parsed from the receiver.
	 * @return errorNum The variable provides information about the correct
	 *         execution (1) or not (-1).
	 */
	static final int parseHUI(String message) {
		int h = 11;
		int iPayloadLen;
		String sPayloadLen = "";
		int utc_flag;
		int klob_flag;
		
		logFiles.logReceiverData(message);

		//if (GlobalState.getSocket() != null) {
			sPayloadLen = UtilsDemoApp.charToStringUbl2(message, 8, 11);
			try {
				iPayloadLen = Integer.parseInt(sPayloadLen, HEX_BASE);
			} catch (Exception e) {
				Log.e(TAG, "uBlox | parseEphSv | " + e.getMessage());
				logFiles.logError("uBlox - parseEphSv - Number format exception for sPayloadLen");
				return -1;
			}

			String Fword = UtilsDemoApp.hexToBin(UtilsDemoApp.charToStringUbl2(
					message, (147), (147 + 8)));

			utc_flag = Fword.charAt(30) - 48;
			klob_flag = Fword.charAt(29) - 48;

			String SutcA0 = UtilsDemoApp.hexToBin(UtilsDemoApp
					.charToStringUbl2(message, h + 8, h + 24));
			String SutcA1 = UtilsDemoApp.hexToBin(UtilsDemoApp
					.charToStringUbl2(message, h + 24, h + 40));
			String SutcTOW = UtilsDemoApp.hexToBin(UtilsDemoApp
					.charToStringUbl2(message, h + 40, h + 48));
			String SutcWNT = UtilsDemoApp.hexToBin(UtilsDemoApp
					.charToStringUbl2(message, h + 48, h + 52));
			String SutcLS = UtilsDemoApp.hexToBin(UtilsDemoApp
					.charToStringUbl2(message, h + 52, h + 56));
			String SutcWNF = UtilsDemoApp.hexToBin(UtilsDemoApp
					.charToStringUbl2(message, h + 56, h + 60));
			String SutcDN = UtilsDemoApp.hexToBin(UtilsDemoApp
					.charToStringUbl2(message, h + 60, h + 64));
			String SutcLSF = UtilsDemoApp.hexToBin(UtilsDemoApp
					.charToStringUbl2(message, h + 64, h + 68));

			try {
				if (utc_flag == 1) {

					utc[0] = UtilsDemoApp.decodeIEEE_doublepr(SutcA0);
					utc[1] = UtilsDemoApp.decodeIEEE_doublepr(SutcA1);
					utc[2] = UtilsDemoApp.bin2dec(SutcTOW);
					utc[3] = UtilsDemoApp.bin2dec(SutcWNT);
					utc[4] = UtilsDemoApp.bin2dec(SutcLS);
					utc[5] = UtilsDemoApp.bin2dec(SutcWNF);
					utc[6] = UtilsDemoApp.bin2dec(SutcDN);
					utc[7] = UtilsDemoApp.bin2dec(SutcLSF);
					utc[8] = 1;
				} else {
					utc[0] = 0;
					utc[1] = 0;
					utc[2] = 0;
					utc[3] = 0;
					utc[4] = 0;
					utc[5] = 0;
					utc[6] = 0;
					utc[7] = 0;
					utc[8] = 0;
				}

			} catch (Exception e) {
				utc[0] = 0;
				utc[1] = 0;
				utc[2] = 0;
				utc[3] = 0;
				utc[4] = 0;
				utc[5] = 0;
				utc[6] = 0;
				utc[7] = 0;
				utc[8] = 0;

				logFiles.logError("uBlox - parseHUI - Number format exception for UTC offset");
			}

			String SklobA0 = UtilsDemoApp.hexToBin(UtilsDemoApp
					.charToStringUbl2(message, h + 72, h + 80));
			String SklobA1 = UtilsDemoApp.hexToBin(UtilsDemoApp
					.charToStringUbl2(message, h + 80, h + 88));
			String SklobA2 = UtilsDemoApp.hexToBin(UtilsDemoApp
					.charToStringUbl2(message, h + 88, h + 96));
			String SklobA3 = UtilsDemoApp.hexToBin(UtilsDemoApp
					.charToStringUbl2(message, h + 96, h + 104));
			String SklobB0 = UtilsDemoApp.hexToBin(UtilsDemoApp
					.charToStringUbl2(message, h + 104, h + 112));
			String SklobB1 = UtilsDemoApp.hexToBin(UtilsDemoApp
					.charToStringUbl2(message, h + 112, h + 120));
			String SklobB2 = UtilsDemoApp.hexToBin(UtilsDemoApp
					.charToStringUbl2(message, h + 120, h + 128));
			String SklobB3 = UtilsDemoApp.hexToBin(UtilsDemoApp
					.charToStringUbl2(message, h + 128, h + 136));

			try {
				if (klob_flag == 1) {

					klob[0] = UtilsDemoApp.decodeIEEE_singlepr(SklobA0);
					klob[1] = UtilsDemoApp.decodeIEEE_singlepr(SklobA1);
					klob[2] = UtilsDemoApp.decodeIEEE_singlepr(SklobA2);
					klob[3] = UtilsDemoApp.decodeIEEE_singlepr(SklobA3);
					klob[4] = UtilsDemoApp.decodeIEEE_singlepr(SklobB0);
					klob[5] = UtilsDemoApp.decodeIEEE_singlepr(SklobB1);
					klob[6] = UtilsDemoApp.decodeIEEE_singlepr(SklobB2);
					klob[7] = UtilsDemoApp.decodeIEEE_singlepr(SklobB3);
					klob[8] = 1;

				} else {
					klob[0] = 0;
					klob[1] = 0;
					klob[2] = 0;
					klob[3] = 0;
					klob[4] = 0;
					klob[5] = 0;
					klob[6] = 0;
					klob[7] = 0;
					klob[8] = 0;
				}
			} catch (Exception e) {
				klob[0] = 0;
				klob[1] = 0;
				klob[2] = 0;
				klob[3] = 0;
				klob[4] = 0;
				klob[5] = 0;
				klob[6] = 0;
				klob[7] = 0;
				klob[8] = 0;

				logFiles.logError("uBlox - parseHUI - Number format exception for Klobuchar coefficients");
			}

			//logFiles.logHUIToSdCard(utc, klob);
//		} else
//			return -1;
		return 1;
	}

	/**
	 * requestPosllh function
	 * 
	 * The function requests for a position message.
	 **/
	int requestPosllh() {
		int ret = 1;
		String completeMessage = "";
		completeMessage = generateMessage(CLASS_NAV + ID_POSLLH);
		sendMessageToReceiver(completeMessage);

		try {
			ret = handlePosllh();
			if (ret == -2)
				GlobalState.setSocket(null);

		} catch (Exception e) {
			Log.e(TAG, "uBlox | requestPosllh error. (" + e.getMessage() + ")");
			logFiles.logError("uBlox - requestPosllh error: " + e);
			ret = -1;
		}
		return ret;
	}

	/**
	 * handlePosllh function
	 * 
	 * The function reads messages from the Bluetooth receiver for classid 0102
	 * which is for Posllh messages.
	 **/
	int handlePosllh() {
		byte[] receivedHeader = new byte[10];
		byte[] receivedBytes = new byte[400];
		byte[] receivedMesage = new byte[10];
		int numBytesRead = 0;
		int bytesRead = 0;
		int bytesToRead = 0;
		int iPayloadLen = 0;
		int totalLength = 0;
		String tmp1 = "";
		StringBuilder message = new StringBuilder();
		String sPayloadLen = "";
		long currentTime = 0;
		long oldTime = 0;
		long timeDiff = 0;
		ReadFromBT readBT;

		if (GlobalState.getSocket() != null) {
			try {
				Log.i(TAG,
						"uBlox | handlePosllh|  @@@ Receive read pollsh start @@@");
				// numBytesRead =
				// GlobalState.getInputStream().read(receivedHeader, 0,
				// (LENGTH_HEADER));
				buffer = receivedHeader;
				offset = 0;
				length = LENGTH_HEADER;
				oldTime = System.currentTimeMillis();
				readBT = new ReadFromBT();
				readBT.run();
				while (!isRead) {
					currentTime = System.currentTimeMillis();
					timeDiff = currentTime - oldTime;
					if (timeDiff > TIMEOUT) {
						Log.e(TAG,
								"uBlox | handlePosllh|  @@@ Receive read posllh terminated 2 @@@");
						return 5;
					}
				}

				// ExecutorService executor =
				// Executors.newSingleThreadExecutor();
				// Future<String> future = executor.submit(new ReadFromBT());
				// try {
				// // Log.e(TAG,
				// "uBlox | handleSfrb|  @@@ Receive read sfrb started 2 @@@");
				// future.get(TIMEOUT, TimeUnit.MILLISECONDS);
				// // Log.e(TAG,
				// "uBlox | handleSfrb|  @@@ Receive read sfrb finished 2 @@@");
				// } catch (TimeoutException e) {
				// Log.e(TAG,
				// "uBlox | handlePosllh|  @@@ Receive read posllh terminated 2 @@@");
				// return 5;
				// }
				Log.i(TAG,
						"uBlox | handlePosllh|  @@@ Receive read pollsh end @@@");
				bytesRead = numBytesRead;
			} catch (Exception e) {
				Log.e(TAG,
						"uBlox | handlePosllh | Receive error: "
								+ e.getMessage());
				logFiles.logError("uBlox - handlePosllh - Receiver disconnected.");
				return -2;
			}

			if (bytesRead < LENGTH_HEADER) {
				do {
					tmp1 = "";
					tmp1 = UtilsDemoApp.byteToHex(receivedHeader);
					// Log.e(TAG_SFRB, "1 tmp1: " + tmp1 );
					message.append(tmp1.substring(0, (numBytesRead * 2)));
					// Log.e(TAG_SFRB, "1 message: " + message );
					numBytesRead = 0;
					Arrays.fill(receivedHeader, (byte) 0);
					bytesToRead = LENGTH_HEADER - bytesRead;
					if (bytesToRead > 0) {
						try {
							// numBytesRead =
							// GlobalState.getInputStream().read(receivedHeader,
							// 0,
							// bytesToRead);
							buffer = receivedHeader;
							offset = 0;
							length = bytesToRead;

							// ExecutorService executor =
							// Executors.newSingleThreadExecutor();
							// Future<String> future = executor.submit(new
							// ReadFromBT());
							// try {
							// // Log.e(TAG,
							// "uBlox | handleSfrb|  @@@ Receive read sfrb started 2 @@@");
							// future.get(TIMEOUT, TimeUnit.MILLISECONDS);
							// // Log.e(TAG,
							// "uBlox | handleSfrb|  @@@ Receive read sfrb finished 2 @@@");
							// } catch (TimeoutException e) {
							// Log.e(TAG,
							// "uBlox | handlePosllh|  @@@ Receive read posllh terminated 2 @@@");
							// return 5;
							// }
							oldTime = System.currentTimeMillis();
							readBT = new ReadFromBT();
							readBT.run();
							while (!isRead) {
								currentTime = System.currentTimeMillis();
								timeDiff = currentTime - oldTime;
								if (timeDiff > TIMEOUT) {
									Log.e(TAG,
											"uBlox | handlePosllh|  @@@ Receive read posllh terminated 2 @@@");
									return 5;
								}
							}
							bytesRead = bytesRead + numBytesRead;
							tmp1 = "";
							tmp1 = UtilsDemoApp.byteToHex(receivedHeader);
							// Log.e(TAG_SFRB, "2 tmp1: " + tmp1 );
							message.append(tmp1
									.substring(0, (numBytesRead * 2)));
							// Log.e(TAG_SFRB, "2 message: " + message );
							sPayloadLen = UtilsDemoApp.charToStringUbl2(
									message.toString(), 8, 11);
							numBytesRead = 0;
							Arrays.fill(receivedHeader, (byte) 0);
						} catch (Exception e) {
							Log.e(TAG, "uBlox | handlePosllh | Receive error: "
									+ e.getMessage());
							logFiles.logError("uBlox - handlePosllh -Receiver disconnected.");
							return -2;
						}
					}
				} while (bytesRead < LENGTH_HEADER);
				receivedHeader = null;
			} else {
				tmp1 = "";
				tmp1 = UtilsDemoApp.byteToHex(receivedHeader);
				// Log.e(TAG_SFRB, "3 tmp1: " + tmp1 );
				message.append(tmp1.substring(0, (numBytesRead * 2)));
				// Log.e(TAG_SFRB, "3 message: " + message );
				sPayloadLen = UtilsDemoApp.charToStringUbl2(message.toString(),
						8, 11);
				numBytesRead = 0;
				Arrays.fill(receivedBytes, (byte) 0);
				receivedHeader = null;
			}

			try {
				iPayloadLen = Integer.parseInt(sPayloadLen, HEX_BASE);
			} catch (Exception e) {
				iPayloadLen = 0;
				Log.e(TAG, "uBlox | handlePosllh | ERROR:  " + e.getMessage());
				logFiles.logError("uBlox - handlePosllh - Number format exception for sPayloadLen");
			}

			totalLength = LENGTH_HEADER + iPayloadLen + LENGTH_CHKSUM;
			bytesToRead = totalLength - bytesRead;

			if (bytesToRead > 0) {
				do {
					try {
						// numBytesRead =
						// GlobalState.getInputStream().read(receivedBytes, 0,
						// bytesToRead);
						buffer = receivedBytes;
						offset = 0;
						length = bytesToRead;

						// ExecutorService executor =
						// Executors.newSingleThreadExecutor();
						// Future<String> future = executor.submit(new
						// ReadFromBT());
						// try {
						// // Log.e(TAG,
						// "uBlox | handleSfrb|  @@@ Receive read sfrb started 3 @@@");
						// future.get(TIMEOUT, TimeUnit.MILLISECONDS);
						// // Log.e(TAG,
						// "uBlox | handleSfrb|  @@@ Receive read sfrb finished 3 @@@");
						// } catch (TimeoutException e) {
						// Log.e(TAG,
						// "uBlox | handlePosllh|  @@@ Receive read posllh terminated 3 @@@");
						// return 5;
						// }
						oldTime = System.currentTimeMillis();
						readBT = new ReadFromBT();
						readBT.run();
						while (!isRead) {
							currentTime = System.currentTimeMillis();
							timeDiff = currentTime - oldTime;
							if (timeDiff > TIMEOUT) {
								Log.e(TAG,
										"uBlox | handlePosllh|  @@@ Receive read posllh terminated 2 @@@");
								return 5;
							}
						}
						receivedMesage = new byte[numBytesRead];
						receivedMesage = receivedBytes;
						bytesRead = bytesRead + numBytesRead;
						bytesToRead = totalLength - bytesRead;
					} catch (Exception e) {
						Log.e(TAG,
								"uBlox | handlePosllh | Receive error: "
										+ e.getMessage());
						logFiles.logError("uBlox - handlePosllh - Receiver disconnected.");
						return -2;
					}
					tmp1 = "";
					tmp1 = UtilsDemoApp.byteToHex(receivedMesage);
					// Log.e(TAG_SFRB, "4 tmp1: " + tmp1 );
					message.append(tmp1.substring(0, numBytesRead * 2));
					// Log.e(TAG_SFRB, "4 message: " + message );
					numBytesRead = 0;
					Arrays.fill(receivedBytes, (byte) 0);
				} while (bytesRead < totalLength);
			}

			if (bytesRead >= totalLength) {
				Log.i(TAG_POSLLH,
						"Complete POSLLH message: " + message.toString());
				char[] receiverMessage = new char[totalLength];
				recvMessage = message.toString();
				receiverMessage = recvMessage.toCharArray();
				readPosllh(receiverMessage);
				receiverMessage = null;
				receivedBytes = null;
				tmp1 = null;
				message = null;
				sPayloadLen = null;
				receivedMesage = null;
			}
		} else {
			Log.e(TAG, "uBlox | handlePosllh | Error : gs.getSocket() == null ");
			logFiles.logError("uBlox - handlePosllh - Receiver is disconnected. ");
		}
		return 1;
	}

	/**
	 * readPosllh function
	 * 
	 * This function parses and stores the Posllh messages. Also logs the
	 * receiver coordinates to a log file.
	 * 
	 * @param message
	 *            The message to be parsed from the receiver.
	 * @return errorNum The variable provides information about the correct
	 *         execution (1) or not (-1).
	 */
	final int readPosllh(char[] message) {
		int iTow = 0;
		int h = 12;

		String siTow = UtilsDemoApp.charToStringUbl(message, h + 0, h + 7);
		try {
			iTow = Integer.parseInt(siTow, HEX_BASE);
			siTow = (Integer.valueOf(iTow)).toString();
		} catch (Exception e) {
			iTow = 0; // Log.e(TAG, "uBlox | " + e.getMessage());
			siTow = null;
			logFiles.logError("uBlox - readPosllh - Number format exception for siTow");
		}

		String sLon = UtilsDemoApp.charToStringUbl(message, h + 8, h + 15);
		try {
			receiverLongitude = Integer.parseInt(sLon, HEX_BASE);
			// currentPosition[8] = (double) (Integer.valueOf(Lon)) * 1E-7;
		} catch (Exception e) {
			// Log.e(TAG, "uBlox | " + e.getMessage());
			sLon = null;
			logFiles.logError("uBlox - readPosllh - Number format exception for sLon");
		}

		String sLat = UtilsDemoApp.charToStringUbl(message, h + 16, h + 23);
		try {
			receiverLatitude = Integer.parseInt(sLat, HEX_BASE);
			// currentPosition[7] = (double) (Integer.valueOf(Lat)) * 1E-7;
		} catch (Exception e) {
			// Log.e(TAG, "uBlox | " + e.getMessage());
			sLat = null;
			logFiles.logError("uBlox - readPosllh - Number format exception for sLat");
		}

		String sHeight = UtilsDemoApp.charToStringUbl(message, h + 24, h + 31);
		try {
			receiverAltitude = Integer.parseInt(sHeight, HEX_BASE);
			// currentPosition[9] = (double) (Integer.valueOf(height)) * 1e-3;
		} catch (Exception e) {
			// Log.e(TAG, "uBlox | " + e.getMessage());
			sHeight = null;
			logFiles.logError("uBlox - readPosllh - Number format exception for sHeight");
		}
		return 1;
	}

	/**
	 * displayMessage function
	 * 
	 * Display any message to a UI thread.
	 * 
	 * @param message
	 *            Messages to display.
	 **/
	public static void displayMessage(final int message) {
		Message msg = messageHandler
				.obtainMessage(BluetoothReceiverList.uBLOX_MESSAGE);
		Bundle bundle = new Bundle();
		bundle.putInt(BluetoothReceiverList.TOAST, message);
		msg.setData(bundle);
		messageHandler.sendMessage(msg);
	}

	/**
	 * calcChkSum function
	 * 
	 * Calculate the checksum for the uBlox message.
	 * 
	 * @param messageHex
	 *            The formated hex String.
	 * @return chkSum The messages' checksum.
	 **/
	static String calcChkSum(final String messageHex) {
		String length = "";
		String checkSum = "";
		short CK_A = 0, CK_B = 0;
		int payloadEnd = 0;

		// messageHex =
		// "B5620600140001000000D008000000E100000300010000000000D89D";
		char[] output = new char[messageHex.length()];
		for (int i = 0; i < messageHex.length(); i++) {
			output[i] = messageHex.charAt(i);
		}
		length = Character.toString(output[11])
				+ Character.toString(output[10])
				+ Character.toString(output[8]) + Character.toString(output[9]);
		int lengthPayload = Integer.parseInt(length, HEX_BASE);
		if (lengthPayload != 0) {
			/**
			 * The payload end must account for the uBlox sync bytes (4), the
			 * class and id bytes (4) as well as the length bytes -> 12.
			 */
			payloadEnd = PAYLOAD_START_BYTE + (lengthPayload * 2);
		} else {
			payloadEnd = PAYLOAD_START_BYTE;
		}
		/** Do not consider the uBlox sync bytes -> i=4. */
		for (int i = CHKSUM_START_BYTE; i < payloadEnd; i++) {
			String out = (Character.toString(output[i]) + Character
					.toString(output[i + 1]));
			int outint = Integer.parseInt(out, HEX_BASE);
			CK_A = (short) ((CK_A + outint) & 0xFF);
			CK_B = (short) ((CK_B + CK_A) & 0xFF);
			i++;
		}
		if (CK_A < HEX_ONE_DIGIT) {
			checkSum = "0";
			checkSum = checkSum + Integer.toHexString(CK_A);
		} else {
			checkSum = Integer.toHexString(CK_A);
		}

		if (CK_B < HEX_ONE_DIGIT) {
			checkSum = checkSum + "0";
			checkSum = checkSum + Integer.toHexString(CK_B);
		} else {
			checkSum = checkSum + Integer.toHexString(CK_B);
		}
		return checkSum;
	}

	/**
	 * sendMessageToReceiver function
	 * 
	 * Sends a message to receiver, depending on the identifier.
	 * 
	 * @param message
	 *            Message to be sent.
	 **/
	protected static void sendMessageToReceiver(final String message) {

		byte[] command = UtilsDemoApp.generateByteMessage(message);
		UtilsDemoApp.write(command);
	}

	/**
	 * generateMessage function
	 * 
	 * This messages retrieves a message number (also called message type MT)
	 * and generates the according message (incl. the checksum).
	 * 
	 * @param mT
	 *            The message to be generated.
	 * @return message The generated message.
	 */
	final static String generateMessage(final String mT) {
		String message = "";
		String payload = "";
		String checkSum = "";
		int length;
		if (mT == (CLASS_RXM + ID_SFRB)) {
			length = LENGTH_POLL_SFRB;
			payload = "";
			message = HEADER_1 + HEADER_2 + CLASS_RXM + ID_SFRB
					+ calcMessageLength(length) + payload;
			checkSum = calcChkSum(message);
			message = message + checkSum;
		} else if (mT == (CLASS_RXM + ID_RAW)) {
			length = LENGTH_POLL_RAW;
			payload = "";
			message = HEADER_1 + HEADER_2 + CLASS_RXM + ID_RAW
					+ calcMessageLength(length) + payload;
			checkSum = calcChkSum(message);
			message = message + checkSum;
		} else if (mT == (CLASS_AID + ID_EPH)) {
			length = LENGTH_POLL_EPH_ALL;
			payload = "";
			message = HEADER_1 + HEADER_2 + CLASS_AID + ID_EPH
					+ calcMessageLength(length) + payload;
			checkSum = calcChkSum(message);
			message = message + checkSum;
		} else if (mT == (CLASS_CFG + ID_SBAS)) {
			length = LENGTH_SBAS;
			payload = "0100010051080000";
			message = HEADER_1 + HEADER_2 + CLASS_CFG + ID_SBAS
					+ calcMessageLength(length) + payload;
			checkSum = calcChkSum(message);
			message = message + checkSum;
		} else if (mT == (CLASS_CFG + ID_PRT)) {
			length = LENGTH_PRT;
			// Payload for 115kbit/s
			// payload = "01000000D008000000C201000700010000000000";
			// Payload for 57600bit/s
			payload = "01000000D008000000E100000300010000000000";
			message = HEADER_1 + HEADER_2 + CLASS_CFG + ID_PRT
					+ calcMessageLength(length) + payload;
			checkSum = calcChkSum(message);
			message = message + checkSum;
		} else if (mT == (CLASS_NAV + ID_POSLLH)) {
			length = LENGTH_POLL_POSLLH;
			payload = "";
			message = HEADER_1 + HEADER_2 + CLASS_NAV + ID_POSLLH
					+ calcMessageLength(length) + payload;
			checkSum = calcChkSum(message);
			message = message + checkSum;
		} else if (mT == (CLASS_CFG + ID_MSG + ID_RAW)) {
			length = LENGTH_MSG;
			payload = "0210000100000000";
			message = HEADER_1 + HEADER_2 + CLASS_CFG + ID_MSG
					+ calcMessageLength(length) + payload;
			checkSum = calcChkSum(message);
			message = message + checkSum;
		} else if (mT == (CLASS_CFG + ID_MSG + ID_SFRB)) {
			length = LENGTH_MSG;
			payload = "0211000100000000";
			message = HEADER_1 + HEADER_2 + CLASS_CFG + ID_MSG
					+ calcMessageLength(length) + payload;
			checkSum = calcChkSum(message);
			message = message + checkSum;
		} else if (mT == (CLASS_CFG + ID_MSG + ID_EPH)) {
			length = LENGTH_MSG;
			payload = "0231000100000000";
			message = HEADER_1 + HEADER_2 + CLASS_CFG + ID_MSG
					+ calcMessageLength(length) + payload;
			checkSum = calcChkSum(message);
			message = message + checkSum;
		} else if (mT == (CLASS_AID + ID_HUI)) {
			length = LENGTH_POLL_HUI;
			payload = "";
			message = HEADER_1 + HEADER_2 + CLASS_AID + ID_HUI
					+ calcMessageLength(length) + payload;
			checkSum = calcChkSum(message);
			message = message + checkSum;
		}
		return message;
	}

	/**
	 * generateMessageEphSv function
	 * 
	 * Generate message for Ephemeris.
	 * 
	 * @param mT
	 *            The message to be generated.
	 * @param svId
	 *            The message to be generated.
	 * @return message The generated message.
	 */
	final String generateMessageEphSv(final String mT, final String svId) {
		String message = "";
		String payload = "";
		String checkSum = "";
		int length = 0;

		if (mT == (CLASS_AID + ID_EPH)) {
			length = LENGTH_POLL_EPH_SV;
			payload = svId;
			message = HEADER_1 + HEADER_2 + CLASS_AID + ID_EPH
					+ calcMessageLength(length) + payload;
			checkSum = calcChkSum(message);
			message = message + checkSum;
		}
		return message;
	}

	/**
	 * calcMessageLength function
	 * 
	 * Calculate the messages' length.
	 * 
	 * @param length
	 *            The message to be generated.
	 * @return lenghtBytes The generated message.
	 */
	final static String calcMessageLength(final int length) {
		String lengthBytes = "0000";
		if (length < HEX_ONE_DIGIT) {
			lengthBytes = "0";
			lengthBytes = lengthBytes + Integer.toHexString(length);
			lengthBytes = lengthBytes + "00";
		} else if (length > HEX_ONE_DIGIT && length < HEX_TWO_DIGIT) {
			lengthBytes = Integer.toHexString(length);
			lengthBytes = lengthBytes + "00";
		} else if (length > HEX_TWO_DIGIT && length < HEX_THREE_DIGIT) {
			lengthBytes = lengthBytes + "0";
			lengthBytes = Integer.toHexString(length);
		} else {
			lengthBytes = Integer.toHexString(length);
		}
		return lengthBytes;
	}

	/**
	 * checkNetwork function
	 * 
	 * This function checks if the mobile device is connected to a network via
	 * 3G or Wifi.
	 * 
	 * @return 1 if mobile device is connected to a network, otherwise 0.
	 **/
	public static final int checkNetwork() {
		int network;
		ConnectivityManager connect = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifi = connect
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo mobile = connect
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if (!wifi.isConnectedOrConnecting()
				&& !mobile.isConnectedOrConnecting()) {
			network = 0;
		} else {
			network = 1;
		}

		GlobalState.setNetwork(network);
		return network;
	}

}
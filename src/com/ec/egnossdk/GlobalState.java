/**
 * @file GlobalState.java
 *
 * Maintains a global state of a variable throughout the 
 * lifetime of the application.
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

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Application;
import android.bluetooth.BluetoothSocket;
import android.content.Context;

/**
 * Class to maintain a global state of a variable throughout the lifetime of the
 * application.
 **/
public class GlobalState extends Application {
	
	private static BluetoothSocket BTsocket = null;
	private static InputStream inputStream = null;
	private static OutputStream outputStream = null;
	private static double[] position = new double[10];
	private static double[] initGPSPosition = new double[4];
	private static double[] initEGNOSPosition = new double[4];
	private static double[][] satelliteDetails;
	private static double gpsTOW = 0;
	private static double gpsWN = 0;
	private static int egnos = 0;
	private static int sisnet = 0;
	private static int edas = 0;
	private static int receiverType = 0;
	private static int network = 0;
	private static boolean isExit = false;
	private static boolean isCurrent = false;
	private static boolean isTracking = false;
	private static boolean isSkyplot = false;
	private static int isLoggedFile = 0;
	private static int isEgnosPosition = 2;
	private static BufferedWriter internalBufferedWriter = null;
	private static BufferedWriter positionBufferedWriter = null;
	private static BufferedWriter errorBufferedWriter = null;

	private static double[][] rndsatelliteType = new double[32][2];
	private static double[][] egnossatelliteType = new double[32][4];
	private static double[][] gpssatelliteType = new double[32][4];
	private static String[][] noradData;
	private static int[] rndPositionType = new int[] { 0, 0, 0, 0, 0, 0, 0, 2 };

	private static SatellitePositions[] satellitePositions;

	private static int errorWhileReadingBT = 1;// 1 if no erroe, else -1

	// Add by Li for NMEA & RTCM use
	private static double[] DOP = new double[4]; // HDOP VDOP PDOP TDOP
	private static double gpsTOE = 0;
	private static double NumSatUse = 0;
	private static double TotalSatInView = 0;
	private static double[] SatId = new double[20];
	private static double[] Elevation = new double[20];
	private static double[] Azimuth = new double[20];
	private static double[] SNR = new double[20];
	
	private static String GPGGASentence;
	private static String GPGLLSentence;
	private static String GPGSASentence;
	private static String GPRMCSentence;
	private static String GPVTGSentence;
	private static String[] GPGSVSentence;
	
	  private static double[] PrnUse=new double[20];
	  private static double[] Prc=new double[20];
	  private static double[] Rrc=new double[20];
	  private static double[] Iodc=new double[20];
	  private static double[] ECEFGPS=new double[3];
	  
	  private static double[] Pr=new double[20];
	  private static double[] SatPosX=new double[20];
	  private static double[] SatPosY=new double[20];
	  private static double[] SatPosZ=new double[20];
	  
	  private static char[][] RtcmMsgByte1;
	  private static char[][] RtcmMsgByte2;
	  private static char[][] RtcmMsgByte3;
	  
	  private static String nmeaRTCMMessages;
	  
	  private static int ModZcount = 0;
	  
	  private static String rtcmMsg1;
	  private static String rtcmMsg2;
	  private static String rtcmMsg3;
	  

	  
	  //INS - Start
	  private static double[] tCorr = new double[20];
	  private static double[] relCorr = new double[20];
	  private static double[] SatPosCorrX=new double[20];
	  private static double[] SatPosCorrY=new double[20];
	  private static double[] SatPosCorrZ=new double[20];
	  
	  private static double state_vect_corr[] = new double[5];
	  private static double P_corr[][] = new double[5][5];
	  
	  /* modified by aanagnostopoulos */
	  private static Context context;
	  private static String SISNET_LOGIN = null;
	  private static String SISNET_PASSWD = null;

	  /* END aanagnostopoulos */
	  
	  public final static void setSatPosCorrX(double[] newSatPosCorrX) {
		  SatPosCorrX = newSatPosCorrX;
	  }

	  public final static double[] getSatPosCorrX() {
	    return SatPosCorrX;
	  }

	  public final static void setSatPosCorrY(double[] newSatPosCorrY) {
		  SatPosCorrY = newSatPosCorrY;
	  }

	  public final static double[] getSatPosCorrY() {
	    return SatPosCorrY;
	  }
	  
	  public final static void setSatPosCorrZ(double[] newSatPosCorrZ) {
		  SatPosCorrZ = newSatPosCorrZ;
	  }

	  public final static double[] getSatPosCorrZ() {
	    return SatPosCorrZ;
	  }
	  
	  public final static void setRelCorr(double[] newRelCorr) {
		  relCorr = newRelCorr;
	  }

	  public final static double[] getRelCorr() {
	    return relCorr;
	  }
	  
	  public final static void setTCorr(double[] newTCorr) {
		  tCorr = newTCorr;
	  }

	  public final static double[] getTCorr() {
	    return tCorr;
	  }
	  
	  public final static void setState_vect_corr(double[] newState_vect_corr) {
		  state_vect_corr = newState_vect_corr;
	  }

	  public final static double[] getState_vect_corr() {
	    return state_vect_corr;
	  }
	  
	  public final static void setP_Corr(double[][] newP_Corr) {
		  P_corr = newP_Corr;
	  }

	  public final static double[][] getP_Corr() {
	    return P_corr;
	  }
	  //INS - End

    
  /**
   * setModZcount function. 
   * 
   * Sets the ModZcount required.
   * @param newModZcount   
   **/
  public final static void setModZcount(int newModZcount) {
		  ModZcount = newModZcount;
  }

  /**
   * getPr getModZcount.
   * @return ModZcount,  return ModZcount values.
   **/
  public final static int getModZcount() {
    return ModZcount;
  }
	  
	  

  /**
   * setPr function. 
   * 
   * Sets the PrnUse required.
   * @param newDOP   
   **/
  public final static void setPr(double[] newPr) {
	  Pr = newPr;
  }

  /**
   * getPr function.
   * @return Pr  return Pr[] values.
   **/
  public final static double[] getPr() {
    return Pr;
  }
  
  
  
  

  public final static void setSatPosX(double[] newSatPosX) {
	  SatPosX = newSatPosX;
  }

  public final static double[] getSatPosX() {
    return SatPosX;
  }
  
  
  
  
  

  public final static void setSatPosY(double[] newSatPosY) {
	  SatPosY = newSatPosY;
  }

  public final static double[] getSatPosY() {
    return SatPosY;
  }
  
  
  
  
  

  public final static void setSatPosZ(double[] newSatPosZ) {
	  SatPosZ = newSatPosZ;
  }

  public final static double[] getSatPosZ() {
    return SatPosZ;
  }
  
  
  
  public final static void setNMEARTCMMessages(String newNmeaRtcmMessages) {
    nmeaRTCMMessages = newNmeaRtcmMessages;
  }

  /**
   * getNMEARTCMMessages function.
   * @return PrnUse  return PrnUse[] values.
   **/
  public final static String getNMEARTCMMessages() {
    return nmeaRTCMMessages;
  }

  public final static void setRtcmMessage3(String newRtcmMsg3) {
	    rtcmMsg3 = newRtcmMsg3;
	  }

	  /**
	   * getRTCMMessages3 function.
	   * @return PrnUse  return PrnUse[] values.
	   **/
	  public final static String getRtcmMessage3() {
	    return rtcmMsg3;
	  }
	  
	  /**
	   * setRTCMMessages2 function. 
	   * 
	   * Sets the PrnUse required.
	   * @param newDOP   
	   **/
	  public final static void setRtcmMessage2(String newRtcmMsg2) {
	    rtcmMsg2 = newRtcmMsg2;
	  }

	  /**
	   * getRTCMMessages2 function.
	   * @return PrnUse  return PrnUse[] values.
	   **/
	  public final static String getRtcmMessage2() {
	    return rtcmMsg2;
	  }
	  
	  /**
	   * setRTCMMessages1 function. 
	   * 
	   * Sets the PrnUse required.
	   * @param newDOP   
	   **/
	  public final static void setRtcmMessage1(String newRtcmMsg1) {
	    rtcmMsg1 = newRtcmMsg1;
	  }

	  /**
	   * getRTCMMessages1 function.
	   * @return PrnUse  return PrnUse[] values.
	   **/
	  public final static String getRtcmMessage1() {
	    return rtcmMsg1;
	  }
  
  public final static void setRtcmMessagesByte3(char[][] newRtcmMsgByte3) {
    RtcmMsgByte3 = newRtcmMsgByte3;
  }

  /**
   * getRTCMMessages3 function.
   * @return PrnUse  return PrnUse[] values.
   **/
  public final static char[][] getRtcmMessagesByte3() {
    return RtcmMsgByte3;
  }
  
  /**
   * setRTCMMessages2 function. 
   * 
   * Sets the PrnUse required.
   * @param newDOP   
   **/
  public final static void setRtcmMessagesByte2(char[][] newRtcmMsgByte2) {
    RtcmMsgByte2 = newRtcmMsgByte2;
  }

  /**
   * getRTCMMessages2 function.
   * @return PrnUse  return PrnUse[] values.
   **/
  public final static char[][] getRtcmMessagesByte2() {
    return RtcmMsgByte2;
  }
  
  /**
   * setRTCMMessages1 function. 
   * 
   * Sets the PrnUse required.
   * @param newDOP   
   **/
  public final static void setRtcmMessagesByte1(char[][] newRtcmMsgByte1) {
    RtcmMsgByte1 = newRtcmMsgByte1;
  }

  /**
   * getRTCMMessages1 function.
   * @return PrnUse  return PrnUse[] values.
   **/
  public final static char[][] getRtcmMessagesByte1() {
    return RtcmMsgByte1;
  }
  
	
  /**
   * setPrnUse function. 
   * 
   * Sets the PrnUse required.
   * @param newDOP   
   **/
  public final static void setPrnUse(double[] newPrnUse) {
    PrnUse = newPrnUse;
  }

  /**
   * getPrnUse function.
   * @return PrnUse  return PrnUse[] values.
   **/
  public final static double[] getPrnUse() {
    return PrnUse;
  }
    
  
  
    
  /**
   * setPrc function. 
   * 
   * Sets the Prc required.
   * @param newPrc   
   **/
  public final static void setPrc(double[] newPrc) {
    Prc = newPrc;
  }

  /**
   * getPrc function.
   * @return Prc  return Prc[] values.
   **/
  public final static double[] getPrc() {
    return Prc;
  }
    

  
  
  /**
   * setIodc function. 
   * 
   * Sets the newIdoc required.
   * @param newIdoc   
   **/
  public final static void setIodc(double[] newIdoc) {
    Iodc = newIdoc;
  }

  /**
   * getIodc function.
   * @return Iodc  return Iodc[] values.
   **/
  public final static double[] getIodc() {
    return Iodc;
  }  

  
  
  /**
   * setRrc function. 
   * 
   * Sets the Rrc required.
   * @param newRrc   
   **/
  public final static void setRrc(double[] newRrc) {
    Rrc = newRrc;
  }

  /**
   * getRrc function.
   * @return Rrc  return Rrc[] values.
   **/
  public final static double[] getRrc() {
    return Rrc;
  }  
  
  /**
   * setECEFGPS function. 
   * 
   * Sets the ECEFGPS of reference station.
   * @param newECEFGPS   
   **/
  public final static void setECEFGPS(double[] newECEFGPS) {
    ECEFGPS = newECEFGPS;
  }

  /**
   * getECEFGPS function.
   *  
   * Gets ECEFGPS of the reference station.
   * @return ECEFGPS  return ECEFGPS[] values.
   **/
  public final static double[] getECEFGPS() {
    return ECEFGPS;
  }
  
	 public final static void setGPGSVSentence(String[] newGPGSVSentence) {

	   GPGSVSentence = newGPGSVSentence;
	  }

	  public final static String[] getGPGSVSentence() {
	    return GPGSVSentence;
	  }
	  
	  public final static void setGPVTGSentence(String newGPVTGSentence){
	      
	    GPVTGSentence = newGPVTGSentence;
	  }
	    
	  public final static String getGPVTGSentence() {
	      return GPVTGSentence;
	  }  
	    
	  public final static void setGPRMCSentence(String newGPRMCSentence){
	    
	    GPRMCSentence = newGPRMCSentence;
	  }
	  
	  public final static String getGPRMCSentence() {
	    return GPRMCSentence;
	  }

  public final static void setGPGSASentence(String newGPGSASentence) {

    GPGSASentence = newGPGSASentence;
  }

  public final static String getGPGSASentence() {
    return GPGSASentence;
  }
	
	public final static void setGPGLLSentence(String newGPGLLSentence){
	    
	  GPGLLSentence = newGPGLLSentence;
	}
	  
	public final static String getGPGLLSentence() {
	    return GPGLLSentence;
	}  
	  
	public final static void setGPGGASentence(String newGPGGASentence){
	  
	  GPGGASentence = newGPGGASentence;
	}
	
	public final static String getGPGGASentence() {
	  return GPGGASentence;
	}
	
	/**
	 * setDOP function.
	 * 
	 * Sets the DOP required.
	 * 
	 * @param newDOP
	 **/
	public final static void setDOP(double[] newDOP) {
		DOP = newDOP;
	}

	/**
	 * getDOP function.
	 * 
	 * Gets getDOP.
	 * 
	 * @return getDOP return DOP[] values.
	 **/
	public final static double[] getDOP() {
		return DOP;
	}

	/**
	 * setGPSTOE function
	 * 
	 * Sets the GPS time of ephermeris acquired from the satellites..
	 * 
	 * @param newGPSTOE
	 **/
	public final static void setGPSTOE(final double newGPSTOE) {
		gpsTOE = newGPSTOE;
	}

	/**
	 * getGPSTOE function
	 * 
	 * Gets the GPS time of ephermeris acquired from the satellites.
	 * 
	 * @return gpsTime The GPS time of ephermeris.
	 **/
	public final static double getGPSTOE() {
		return gpsTOE;
	}

	/**
	 * setNumSatUse function
	 * 
	 * Sets the NumSatUse from the satellites..
	 * 
	 * @param newNumSatUse
	 **/
	public final static void setNumSatUse(final double newNumSatUse) {
		NumSatUse = newNumSatUse;
	}

	/**
	 * getNumSatUse function
	 * 
	 * Gets the NumSatUse from the satellites.
	 * 
	 * @return NumSatUse
	 **/
	public final static double getNumSatUse() {
		return NumSatUse;
	}

	/**
	 * setTotalSatInView function
	 * 
	 * Sets the TotalSatInView from the satellites..
	 * 
	 * @param newTotalSatInView
	 **/
	public final static void setTotalSatInView(final double newTotalSatInView) {
		TotalSatInView = newTotalSatInView;
	}

	/**
	 * getTotalSatInView function
	 * 
	 * Gets the TotalSatInView from the satellites.
	 * 
	 * @return TotalSatInView
	 **/
	public final static double getTotalSatInView() {
		return TotalSatInView;
	}

	/**
	 * setSatId function
	 * 
	 * Sets the SatId from the satellites..
	 * 
	 * @param newSatId
	 **/
	public final static void setSatId(double[] newSatId) {
		SatId = newSatId;
	}

	/**
	 * getSatId function
	 * 
	 * Gets the SatId from the satellites.
	 * 
	 * @return SatId
	 **/
	public final static double[] getSatId() {
		return SatId;
	}

	/**
	 * setElevation function
	 * 
	 * Sets the Elevation from the satellites..
	 * 
	 * @param newElevation
	 **/
	public final static void setElevation(double[] newElevation) {
		Elevation = newElevation;
	}

	/**
	 * getElevation function
	 * 
	 * Gets the Elevation from the satellites.
	 * 
	 * @return Elevation
	 **/
	public final static double[] getElevation() {
		return Elevation;
	}

	/**
	 * setAzimuth function
	 * 
	 * Sets the Azimuth from the satellites..
	 * 
	 * @param newAzimuth
	 **/
	public final static void setAzimuth(double[] newAzimuth) {
		Azimuth = newAzimuth;
	}

	/**
	 * getAzimuth function
	 * 
	 * Gets the Azimuth from the satellites.
	 * 
	 * @return Azimuth
	 **/
	public final static double[] getAzimuth() {
		return Azimuth;
	}

	/**
	 * setSNR function
	 * 
	 * Sets the SNR from the satellites..
	 * 
	 * @param newSNR
	 **/
	public final static void setSNR(double[] newSNR) {
		SNR = newSNR;
	}

	/**
	 * getSNR function
	 * 
	 * Gets the SNR from the satellites.
	 * 
	 * @return SNR
	 **/
	public final static double[] getSNR() {
		return SNR;
	}

	// Li's Edit finish here !

	/**
	 * setInternalBufferedWriter function
	 * 
	 * Sets a buffered writer for the log file named
	 * internallogfile+"current date"+.log
	 * 
	 * @param newBufferedWriter
	 *            The buffered writer for the
	 *            internallogfile+"current date"+.log.
	 **/
	public final static void setInternalBufferedWriter(
			final BufferedWriter newBufferedWriter) {
		internalBufferedWriter = newBufferedWriter;
	}

	/**
	 * getInternalBufferedWriter function
	 * 
	 * Gets a buffered writer for the log file named
	 * internallogfile+"current date"+.log
	 * 
	 * @return outputStream The buffered writer for the
	 *         internallogfile+"current date"+.log.
	 **/
	public final static BufferedWriter getInternalBufferedWriter() {
		return internalBufferedWriter;
	}

	/**
	 * setErrorBufferedWriter function
	 * 
	 * Sets a buffered writer for the log file named error+"current date"+.log
	 * 
	 * @param newBufferedWriter
	 *            The buffered writer for the error+"current date"+.log.
	 **/
	public final static void setErrorBufferedWriter(
			final BufferedWriter newBufferedWriter) {
		errorBufferedWriter = newBufferedWriter;
	}

	/**
	 * getErrorBufferedWriter function
	 * 
	 * Gets a buffered writer for the log file named error+"current date"+.log
	 * 
	 * @return outputStream The buffered writer for the
	 *         error+"current date"+.log.
	 **/
	public final static BufferedWriter getErrorBufferedWriter() {
		return errorBufferedWriter;
	}

	/**
	 * setPositionBufferedWriter function
	 * 
	 * Sets a buffered writer for the log file named
	 * position+"current date"+.log
	 * 
	 * @param newBufferedWriter
	 *            The buffered writer for the position+"current date"+.log.
	 **/
	public final static void setPositionBufferedWriter(
			final BufferedWriter newBufferedWriter) {
		positionBufferedWriter = newBufferedWriter;
	}

	/**
	 * getPositionBufferedWriter function
	 * 
	 * Gets a buffered writer for the log file named
	 * position+"current date"+.log
	 * 
	 * @return outputStream The buffered writer for the
	 *         position+"current date"+.log.
	 **/
	public final static BufferedWriter getPositionBufferedWriter() {
		return positionBufferedWriter;
	}

	/**
	 * setisEgnosPosition function.
	 * 
	 * Sets 1 if is EGNOS position, otherwise 0.
	 * 
	 * @param newisEgnosPosition
	 *            1 if EGNOS position, otherwise 0.
	 **/
	public final static void setisEgnosPosition(int newisEgnosPosition) {
		isEgnosPosition = newisEgnosPosition;
	}

	/**
	 * getisEgnosPosition function.
	 * 
	 * Gets 1 if EGNOS position, otherwise 0.
	 * 
	 * @return isEgnosPosition 1 if EGNOS position, otherwise 0.
	 **/
	public final static int getisEgnosPosition() {
		return isEgnosPosition;
	}

	/**
	 * getisExit function.
	 * 
	 * Gets TRUE or FALSE indicating application is closing
	 * 
	 * @return isCurrent TRUE or FALSE based on Exit functionality.
	 **/
	public final static boolean getisExit() {
		return isExit;
	}

	/**
	 * setisExit function
	 * 
	 * Sets TRUE or FALSE indicating application is closing
	 * 
	 * @param newisExit
	 *            TRUE or FALSE based on Exit functionality.
	 **/
	public final static void setisExit(boolean newisExit) {
		isExit = newisExit;
	}

	/**
	 * setisCurrent function
	 * 
	 * Sets TRUE or FALSE indicating Current Location is currently running or
	 * not.
	 * 
	 * @param newisCurrent
	 *            TRUE or FALSE based on Current Location functionality.
	 **/
	public final static void setisCurrent(boolean newisCurrent) {
		isCurrent = newisCurrent;
	}

	/**
	 * getisCurrent function.
	 * 
	 * Gets TRUE or FALSE indicating Current Location is currently running or
	 * not.
	 * 
	 * @return isCurrent TRUE or FALSE based on Current Location functionality.
	 **/
	public final static boolean getisCurrent() {
		return isCurrent;
	}

	/**
	 * setisCurrent function
	 * 
	 * Sets TRUE or FALSE indicating Current Location is currently running or
	 * not.
	 * 
	 * @param newisCurrent
	 *            TRUE or FALSE based on Current Location functionality.
	 **/
	public final static void setisSkyplot(boolean newisSkyplot) {
		isSkyplot = newisSkyplot;
	}

	/**
	 * getisCurrent function.
	 * 
	 * Gets TRUE or FALSE indicating Current Location is currently running or
	 * not.
	 * 
	 * @return isCurrent TRUE or FALSE based on Current Location functionality.
	 **/
	public final static boolean getisSkyplot() {
		return isSkyplot;
	}

	/**
	 * setisTracking function.
	 * 
	 * Sets TRUE or FALSE indicating Start Tracking is currently running or not.
	 * 
	 * @param newisTracking
	 *            TRUE or FALSE based on Start Tracking functionality.
	 **/
	public final static void setisTracking(boolean newisTracking) {
		isTracking = newisTracking;
	}

	/**
	 * getisTracking function.
	 * 
	 * Gets TRUE or FALSE, indicating Start Tracking is currently running or
	 * not. network = 1 if network for the device is not available, otherwise
	 * network = 0.
	 * 
	 * @return isTracking TRUE or FALSE based on Start Tracking functionality.
	 **/
	public final static boolean getisTracking() {
		return isTracking;
	}

	/**
	 * setisLogFile function.
	 * 
	 * Sets TRUE or FALSE if log file was created.
	 * 
	 * @param setLog
	 *            TRUE or FALSE if log file was created.
	 **/
	public final static void setisLogFile(int setLog) {
		isLoggedFile = setLog;
	}

	/**
	 * getisLogFile function.
	 * 
	 * Gets Sets TRUE or FALSE if log file was created.
	 * 
	 * @return isLogFile TRUE or FALSE if log file was created.
	 **/
	public final static int getisLogFile() {
		return isLoggedFile;
	}

	/**
	 * setNetwork function.
	 * 
	 * Sets network to 1 if network is available, otherwise 0.
	 * 
	 * @param newNetwork
	 *            0 or 1 based on availability of network.
	 **/
	public final static void setNetwork(int newNetwork) {
		network = newNetwork;
	}

	/**
	 * getNetwork function.
	 * 
	 * Gets the value 1 or 0, indicating network is available or not. network =
	 * 1 if network for the device is not available, otherwise network = 0.
	 * 
	 * @return network 0 or 1 based on availability of network.
	 **/
	public final static int getNetwork() {
		return network;
	}

	/**
	 * setReceiverType function.
	 * 
	 * Sets the type of the external Bluetooth Receiver.
	 * 
	 * @param newReceiverType
	 *            type of the external Bluetooth Receiver.
	 **/
	public final static void setReceiverType(int newReceiverType) {
		receiverType = newReceiverType;
	}

	/**
	 * getReceiverType function.
	 * 
	 * Gets the type of the external Bluetooth Receiver.
	 * 
	 * @return receiverType type of the external Bluetooth Receiver.
	 **/
	public final static int getReceiverType() {
		return receiverType;
	}

	/**
	 * setReceiverType function.
	 * 
	 * Sets the type of the external Bluetooth Receiver.
	 * 
	 * @param newSatelliteType
	 *            type of the external Bluetooth Receiver.
	 **/
	public final static void setSatellitePositions(
			SatellitePositions[] newSatPos) {
		satellitePositions = newSatPos;
	}

	/**
	 * satelliteType function.
	 * 
	 * Gets the type of the external Bluetooth Receiver.
	 * 
	 * @return receiverType type of the external Bluetooth Receiver.
	 **/
	public final static SatellitePositions[] getSatellitePositions() {
		return satellitePositions;
	}

	/**
	 * setReceiverType function.
	 * 
	 * Sets the type of the external Bluetooth Receiver.
	 * 
	 * @param newSatelliteType
	 *            type of the external Bluetooth Receiver.
	 **/
	public final static void setEGNOSSatelliteType(
			double[][] newegnosSatelliteType) {
		egnossatelliteType = newegnosSatelliteType;
	}

	/**
	 * satelliteType function.
	 * 
	 * Gets the type of the external Bluetooth Receiver.
	 * 
	 * @return receiverType type of the external Bluetooth Receiver.
	 **/
	public final static double[][] getEGNOSSatelliteType() {
		return egnossatelliteType;
	}

	/**
	 * setReceiverType function.
	 * 
	 * Sets the type of the external Bluetooth Receiver.
	 * 
	 * @param newSatelliteType
	 *            type of the external Bluetooth Receiver.
	 **/
	public final static void setRnDSatelliteType(double[][] newrndSatelliteType) {
		rndsatelliteType = newrndSatelliteType;
	}

	/**
	 * satelliteType function.
	 * 
	 * Gets the type of the external Bluetooth Receiver.
	 * 
	 * @return receiverType type of the external Bluetooth Receiver.
	 **/
	public final static double[][] getRnDSatelliteType() {
		return rndsatelliteType;
	}

	/**
	 * setReceiverType function.
	 * 
	 * Sets the type of the external Bluetooth Receiver.
	 * 
	 * @param newSatelliteType
	 *            type of the external Bluetooth Receiver.
	 **/
	public final static void setGPSSatelliteType(double[][] newgpsatelliteType) {
		gpssatelliteType = newgpsatelliteType;
	}

	/**
	 * satelliteType function.
	 * 
	 * Gets the type of the external Bluetooth Receiver.
	 * 
	 * @return receiverType type of the external Bluetooth Receiver.
	 **/
	public final static double[][] getGPSSatelliteType() {
		return gpssatelliteType;
	}

	/**
	 * setEgnos function.
	 * 
	 * Sets the value 1 or 0 indicating EGNOS is ON or OFF from Settings.
	 * 
	 * @param newEgnos
	 *            0 or 1 based on EGNOS ON or OFF from Settings.
	 **/
	public final static void setEgnos(int newEgnos) {
		egnos = newEgnos;
	}

	/**
	 * getEgnos function.
	 * 
	 * Gets value 1 or 0 indicating EGNOS is ON or OFF from Settings.
	 * 
	 * @return egnos 0 or 1 based on EGNOS ON or OFF from Settings.
	 **/
	public final static int getEgnos() {
		return egnos;
	}

	/**
	 * setSISNeT function.
	 * 
	 * Sets the value 1 or 0 indicating SISNeT is ON or OFF from Settings.
	 * 
	 * @param newSisnet
	 *            0 or 1 based on SISNeT ON or OFF from Settings.
	 **/
	public final static void setSISNeT(int newSisnet) {
		sisnet = newSisnet;
	}

	/**
	 * getSISNeT function.
	 * 
	 * Gets the value 1 or 0 indicating SISNeT is ON or OFF from Settings.
	 * 
	 * @return sisnet 0 or 1 based on SISNeT ON or OFF from Settings.
	 **/
	public final static int getSISNeT() {
		return sisnet;
	}
	
	/**
   * setEDAS function.
   * 
   * Sets the value 1 or 0 indicating EDAS is ON or OFF from Settings.
   * 
   * @param newEdas
   *            0 or 1 based on SISNeT ON or OFF from Settings.
   **/
  public final static void setEDAS(int newEdas) {
    edas = newEdas;
  }

  /**
   * getEDAS function.
   * 
   * Gets the value 1 or 0 indicating EDAS is ON or OFF from Settings.
   * 
   * @return edas   0 or 1 based on EDAS ON or OFF from Settings.
   **/
  public final static int getEDAS() {
    return edas;
  }

	/**
	 * setPosition function
	 * 
	 * Sets the current position acquired. Rows 0 to 9 as GPS Latitude, GPS
	 * Longitude, GPS Altitude, EGNOS Latitude, EGNOS Longitude,EGNOS Altitude,
	 * HPL, R&D Latitude, R&D Longitude and R&D altitude respectively.
	 * 
	 * @param newPosition
	 *            The array of current position.
	 **/
	public final static void setPosition(final double[] newPosition) {
		position = newPosition;
	}

	/**
	 * getPosition function
	 * 
	 * Gets the computed position as a table of 7 x 1 values. Rows 0 to 9 as GPS
	 * Latitude, GPS Longitude, GPS Altitude, EGNOS Latitude, EGNOS Longitude,
	 * EGNOS Altitude Altitude, HPL, R&D Latitude, R&D Longitude and R&D
	 * altitude respectively.
	 * 
	 * @return position The array of current position.
	 **/
	public final static double[] getPosition() {
		return position;
	}

	/**
	 * setinitialGPSPosition function
	 * 
	 * Sets the initial position acquired. The position in the ECEF (xyz and
	 * clock bias * speed of light). Used as a first estimation to obtain a
	 * position.
	 * 
	 * @param newInitialPosition
	 *            The array of initial position.
	 **/
	public final static void setinitialGPSPosition(
			final double[] newInitialPosition) {
		initGPSPosition = newInitialPosition;
	}

	/**
	 * getinitialGPSPosition function
	 * 
	 * Gets the initial position acquired. The position in the ECEF (xyz and
	 * clock bias * speed of light). Used as a first estimation to obtain a
	 * position.
	 * 
	 * @return initPosition The array of initial position.
	 **/
	public final static double[] getinitialGPSPosition() {
		return initGPSPosition;
	}

	/**
	 * setinitialEGNOSPosition function
	 * 
	 * Sets the initial position acquired. The position in the ECEF (xyz and
	 * clock bias * speed of light). Used as a first estimation to obtain a
	 * position.
	 * 
	 * @param newInitialPosition
	 *            The array of initial position.
	 **/
	public final static void setinitialEGNOSPosition(
			final double[] newInitialPosition) {
		initEGNOSPosition = newInitialPosition;
	}

	/**
	 * getinitialEGNOSPosition function
	 * 
	 * Gets the initial position acquired. The position in the ECEF (xyz and
	 * clock bias * speed of light). Used as a first estimation to obtain a
	 * position.
	 * 
	 * @return initPosition The array of initial position.
	 **/
	public final static double[] getinitialEGNOSPosition() {
		return initEGNOSPosition;
	}

	/**
	 * setSatelliteDetails function
	 * 
	 * Sets the satellite details of all the Satellites from NORAD
	 * 
	 * @param satelliteDetails
	 *            The array of satellite details.
	 **/
	public final static void setSatelliteDetails(
			final double[][] newSatelliteDetails) {
		satelliteDetails = newSatelliteDetails;
	}

	/**
	 * getSatelliteDetails function
	 * 
	 * Gets the satellite details of all the satellites from NORAD
	 * 
	 * @return satelliteDetails The array of satellite details
	 **/
	public final static double[][] getSatelliteDetails() {
		return satelliteDetails;
	}

	/**
	 * setSatelliteDetails function
	 * 
	 * Sets the satellite details of all the Satellites from NORAD
	 * 
	 * @param newNORADData
	 *            The array of satellite details.
	 **/
	public final static void setNORADData(final String[][] newNORADData) {
		noradData = newNORADData;
	}

	/**
	 * getNORADData function
	 * 
	 * Gets the satellite details of all the satellites from NORAD
	 * 
	 * @return satelliteDetails The array of satellite details
	 **/
	public final static String[][] getNORADData() {
		return noradData;
	}

	/**
	 * setGPSTOW function
	 * 
	 * Sets the GPS time of week acquired from the satellites..
	 * 
	 * @param newGPSTOW
	 *            The GPS time of week.
	 **/
	public final static void setGPSTOW(final double newGPSTOW) {
		gpsTOW = newGPSTOW;
	}

	/**
	 * getGPSTOW function
	 * 
	 * Gets the GPS time of week acquired from the satellites.
	 * 
	 * @return gpsTime The GPS time of week.
	 **/
	public final static double getGPSTOW() {
		return gpsTOW;
	}

	/**
	 * setGPSWN function
	 * 
	 * Sets the GPS week number acquired from the satellites..
	 * 
	 * @param newGPSWN
	 *            The GPS time of week.
	 **/
	public final static void setGPSWN(final double newGPSWN) {
		gpsWN = newGPSWN;
	}

	/**
	 * getGPSWN function
	 * 
	 * Gets the GPS week number acquired from the satellites.
	 * 
	 * @return gpsTime The GPS time of week.
	 **/
	public final static double getGPSWN() {
		return gpsWN;
	}

	/**
	 * setPosition function
	 * 
	 * Sets the current position acquired. Rows 0 to 9 as GPS Latitude, GPS
	 * Longitude, GPS Altitude, EGNOS Latitude, EGNOS Longitude,EGNOS Altitude,
	 * HPL, R&D Latitude, R&D Longitude and R&D altitude respectively.
	 * 
	 * @param newType
	 *            The array of current position.
	 **/
	public final static void setRndPositionType(final int[] newType) {
		rndPositionType = newType;
	}

	/**
	 * getPosition function
	 * 
	 * Gets the computed position as a table of 7 x 1 values. Rows 0 to 9 as GPS
	 * Latitude, GPS Longitude, GPS Altitude, EGNOS Latitude, EGNOS Longitude,
	 * EGNOS Altitude Altitude, HPL, R&D Latitude, R&D Longitude and R&D
	 * altitude respectively.
	 * 
	 * @return position The array of current position.
	 **/
	public final static int[] getRndPositionType() {
		return rndPositionType;
	}

	/**
	 * getSocket function
	 * 
	 * Gets an Rfcomm bluetooth socket for external Bluetooth receiver
	 * connection.
	 * 
	 * @return BTsocket The Rfcomm bluetooth socket for external Bluetooth
	 *         receiver connection.
	 **/
	public final static BluetoothSocket getSocket() {
		return BTsocket;
	}

	/**
	 * setSocket function
	 * 
	 * Sets an Rfcomm bluetooth socket for external Bluetooth receiver
	 * connection.
	 * 
	 * @param newBtSocket
	 *            The Rfcomm bluetooth socket for external Bluetooth receiver
	 *            connection.
	 **/
	public final static void setSocket(final BluetoothSocket newBtSocket) {
		BTsocket = newBtSocket;
	}

	/**
	 * getSocket function
	 * 
	 * Gets an Rfcomm bluetooth socket for external Bluetooth receiver
	 * connection.
	 * 
	 * @return BTsocket The Rfcomm bluetooth socket for external Bluetooth
	 *         receiver connection.
	 **/
	public final static int getErrorWhileReadingBT() {
		return errorWhileReadingBT;
	}

	/**
	 * setSocket function
	 * 
	 * Sets an Rfcomm bluetooth socket for external Bluetooth receiver
	 * connection.
	 * 
	 * @param newBtSocket
	 *            The Rfcomm bluetooth socket for external Bluetooth receiver
	 *            connection.
	 **/
	public final static void setErrorWhileReadingBT(int newErrorWhileReadingBT) {
		errorWhileReadingBT = newErrorWhileReadingBT;
	}

	/**
	 * getInputStream function
	 * 
	 * Gets an input stream for external Bluetooth receiver connection.
	 * 
	 * @return inputStream The input stream for external Bluetooth receiver
	 *         connection.
	 **/
	public final static InputStream getInputStream() {
		return inputStream;
	}

	/**
	 * setInputStream function
	 * 
	 * Sets an input stream for external Bluetooth receiver connection.
	 * 
	 * @param newInputStream
	 *            The input stream for external Bluetooth receiver connection.
	 **/
	public final static void setInputStream(final InputStream newInputStream) {
		inputStream = newInputStream;
	}

	/**
	 * getOutputStream function
	 * 
	 * Gets an output stream for external Bluetooth receiver connection.
	 * 
	 * @return outputStream The output stream for external Bluetooth receiver
	 *         connection.
	 **/
	public final static OutputStream getOutputStream() {
		return outputStream;
	}

	/**
	 * setOutputStream function
	 * 
	 * Sets an output stream for external Bluetooth receiver connection.
	 * 
	 * @param newOutputStream
	 *            The output stream of external Bluetooth receiver connection.
	 **/
	public final static void setOutputStream(final OutputStream newOutputStream) {
		outputStream = newOutputStream;
	}

	private static boolean isNmeaLoggingToFile = false;
	private static boolean isNmeaSendingViaBluetooth = false;
	private static BluetoothMessageTransferService bTMessageTransferServiceInstance;

	public static boolean isNmeaLoggingToFile() {
		return isNmeaLoggingToFile;
	}

	public static void setNmeaLoggingToFile(boolean isNmeaLoggingToFile) {
		GlobalState.isNmeaLoggingToFile = isNmeaLoggingToFile;
	}

	public static boolean isNmeaSendingViaBluetooth() {
		return isNmeaSendingViaBluetooth;
	}

	public static void setNmeaSendingViaBluetooth(
			boolean isNmeaSendingViaBluetooth) {
		GlobalState.isNmeaSendingViaBluetooth = isNmeaSendingViaBluetooth;
	}

	public static void setbTMessageTransferServiceInstance(
			BluetoothMessageTransferService bTMessageTransferServiceInstance) {
		GlobalState.bTMessageTransferServiceInstance = bTMessageTransferServiceInstance;
	}

	public static BluetoothMessageTransferService getbTMessageTransferServiceInstance() {
		return bTMessageTransferServiceInstance;
	}
	
	public static double[] insReadings = new double[9];
	
	public static void setInsReadings(double[] insReadings) {
		GlobalState.insReadings = insReadings;
	}

	public static double[] getInsReadings() {
		return insReadings;
	}

	/* modified by aanagnostopoulos*/
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		GlobalState.context = getApplicationContext();
	}
	
	public static Context getContext(){
		return GlobalState.context;
	}
	/* END aanagnostopoulos */

	public static String getSISNET_LOGIN() {
		return SISNET_LOGIN;
	}

	public static void setSISNET_LOGIN(String sISNET_LOGIN) {
		SISNET_LOGIN = sISNET_LOGIN;
	}

	public static String getSISNET_PASSWD() {
		return SISNET_PASSWD;
	}

	public static void setSISNET_PASSWD(String sISNET_PASSWD) {
		SISNET_PASSWD = sISNET_PASSWD;
	}
}

/**

 * @file LogFiles.java
 *
 * Writes GPS,EGNOS and Receiver positions
 * to position+"current date"+.log file and any errors 
 * in the error+"current date"+.log file on the SD Card. 
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
import java.io.IOException;
import java.util.Date;

import android.util.Log;

/**
 * Class to write logs to SD Card.
 **/
public class LogFiles {

  private BufferedWriter internalLogFileWriter;
  private BufferedWriter positionLogFileWriter;
  private BufferedWriter errorLogFileWriter;
  private static final String TAG_LOG = "LogFiles";

  /**
   * LogFiles Constructor 
   * 
   * Constructor that gets the buffered writer of the 3 log files 
   * from the GlobalState of the application.
   **/
  public LogFiles() {
    this.internalLogFileWriter = GlobalState.getInternalBufferedWriter();
    this.positionLogFileWriter = GlobalState.getPositionBufferedWriter();
    this.errorLogFileWriter = GlobalState.getErrorBufferedWriter();
  }


  /**
   * logEgnosToSdCard function 
   * 
   * Logs the Ephemeris and Sfrb data to the log file. 
   * @param mT            The message type.
   * @param message       The EGNOS message, 250bits, binary representation.
   **/
  public final void logEgnosToSdCard(final String mT, final String message) {
    BackgroundThreadWriteEgnos writeThread = new BackgroundThreadWriteEgnos(mT,
        message);
    writeThread.start();
  }


  /**
   * BackgroundThreadWriteEgnos thread 
   * 
   * Background thread that writes the Ephemeris and Sfrb data to the log file.
   **/
  class BackgroundThreadWriteEgnos extends Thread {
    private String sfrbMT_;
    private String sfrbMessage_;

    /**
     * BackgroundThreadWriteEgnos Constructor 
     * 
     * A constructor to write the Ephemeris and Sfrb data to the log file.
     * @param mT            The message type.
     * @param message       The EGNOS message, 250bits, binary representation.
     **/
    BackgroundThreadWriteEgnos(final String mT, final String message) {
      this.sfrbMT_ = mT;
      this.sfrbMessage_ = message;
    }

    @Override
    public void run() {
    	if(internalLogFileWriter != null &&  GlobalState.getisExit() == false)
          writeEgnosToSdCard(sfrbMT_, sfrbMessage_);
    };
  }

  /**
   * writeEgnosToSdCard function 
   * 
   * Writes Ephemeris and Sfrb data to the log file 
   * named internallogfile+"current date".log.
   * @param mT            The message type.
   * @param message       The EGNOS message, 250bits, binary representation.
   **/
  public final void writeEgnosToSdCard(String mT, String message) {
    try {

      Date dt = new Date();
      String curTime = dt.toString();
      internalLogFileWriter.write("\n" + curTime + "\n");
      internalLogFileWriter.write(mT + "\n");
      internalLogFileWriter.write(message + "\n");
      internalLogFileWriter.flush();
    } catch (IOException e) {
      Log.e(TAG_LOG, "Error: occured in writing to SD Card: " ,e);
    }
  }
  
  
  /**
   * logEgnosToSdCard function 
   * 
   * Logs the Ephemeris and Sfrb data to the log file. 
   * @param mT            The message type.
   * @param message       The EGNOS message, 250bits, binary representation.
   **/
  public final void logReceiverData(String message) {
    BackgroundThreadWriteRx writeThread = new BackgroundThreadWriteRx(
        message);
    writeThread.start();
  }


  /**
   * BackgroundThreadWriteEgnos thread 
   * 
   * Background thread that writes the Ephemeris and Sfrb data to the log file.
   **/
  class BackgroundThreadWriteRx extends Thread {

    private String sfrbMessage_;

    /**
     * BackgroundThreadWriteEgnos Constructor 
     * 
     * A constructor to write the Ephemeris and Sfrb data to the log file.
     * @param mT            The message type.
     * @param message       The EGNOS message, 250bits, binary representation.
     **/
    BackgroundThreadWriteRx(final String message) {
      sfrbMessage_ = message;
    }

    @Override
    public void run() {
      if(internalLogFileWriter != null &&  GlobalState.getisExit() == false)
          writeRxData(sfrbMessage_);
    };
  }

  /**
   * writeEgnosToSdCard function 
   * 
   * Writes Ephemeris and Sfrb data to the log file 
   * named internallogfile+"current date".log.
   * @param mT            The message type.
   * @param message       The EGNOS message, 250bits, binary representation.
   **/
  public final void writeRxData(String message) {
    try {

      internalLogFileWriter.write(message + ",");
      internalLogFileWriter.flush();
    } catch (IOException e) {
      Log.e(TAG_LOG, "Error: occured in writing to SD Card: " ,e);
    }
  }

  /**
   * logPositionToSdCard function 
   * 
   * Logs the GPS and EGNOS positions to the log file.
   * Also logs the status of the EGNOS ON/OFF and SISNeT ON/OFF from Settings
   * the network status, the GPS HDOP and EGNOS HDOP values.
   * @param position           The raw GPS data from the receiver.
   * @param egnos              0 or 1 if egnos data is available.
   * @param sisnet             0 or 1 if sisnet is used to get EGNOS position.
   * @param network            0 or 1 if device is connected to the Internet.
   * @param gpsHDOP            The GPS HDOP value.
   * @param egnosHDOP          The EGNOS HDOP value.
   * @param egnos_position     1 EGNOS position, 0 preliminary EGNOS position.
   * @param msg_TO             Table of EGNOS messages available or not.
   **/
  public final void logPositionToSdCard(final double[] position,
      int egnos, int sisnet, int network, double gpsHDOP, double egnosHDOP,int egnos_position, int msg_TO[]) {
    BackgroundThreadWritePosition writeThread = new BackgroundThreadWritePosition(
        position, egnos, sisnet, network, gpsHDOP, egnosHDOP, egnos_position, msg_TO);
    writeThread.start();
  }

  /**
   * BackgroundThreadWritePosition thread 
   * 
   * Background thread that writes to the log file.
   **/
  class BackgroundThreadWritePosition extends Thread {
    private double[] currentPosition_;
    private int egnos_;
    private int sisnet_;
    private int network_;
    private double gpsHDOP_;
    private double egnosHDOP_;
    private int egnos_position_;
    private int[] msg_TO_;

    /**
     * BackgroundThreadWritePosition constructor 
     * 
     * A constructor to write current position, EGNOS,SISNeT and Network status
     * and GPS and EGNOS HDOP values to a log file
     * @param position           The raw GPS data from the receiver.
     * @param egnos              0 or 1 if egnos data is available.
     * @param sisnet             0 or 1 if sisnet is used to get EGNOS position.
     * @param network            0 or 1 if device is connected to the Internet.
     * @param gpsHDOP            The GPS HDOP value.
     * @param egnosHDOP          The EGNOS HDOP value.
     * @param egnos_position     1 EGNOS position, 0 preliminary EGNOS position.
     * @param msg_TO             Table of EGNOS messages available or not.
     **/
    BackgroundThreadWritePosition(final double[] position, int egnos,
        int sisnet, int network, double gpsHDOP, double egnosHDOP, int egnos_position, int msg_TO[]) {
      this.currentPosition_ = position;
      this.egnos_ = egnos;
      this.sisnet_ = sisnet;
      this.network_ = network;
      this.gpsHDOP_ = gpsHDOP;
      this.egnosHDOP_ = egnosHDOP;
      this.egnos_position_ = egnos_position;
      this.msg_TO_ = msg_TO; 
     
    }
    @Override
    public void run() {
      if(GlobalState.getisExit() == false)
        writeUBloxPositionToSdCard(currentPosition_, egnos_, sisnet_, network_,
          gpsHDOP_,egnosHDOP_,egnos_position_,msg_TO_);
    };
  }

  /**
   * writeRawToSdCard function
   * 
   * Writes current position, EGNOS,SISNeT and Network status and
   * GPS and EGNOS HDOP values to the log file named internallogfile+"current date".log.
   * Writes GPS, EGNOS and Receiver position to the file named position+"current date".log.
   * @param currentPosition_    The raw GPS data from the receiver.
   * @param sisnet_             0 or 1 if sisnet is used to get EGNOS position.
   * @param egnos_              0 or 1 if egnos data is available.
   * @param network_            0 or 1 if device is connected to the Internet.
   * @param gpsHDOP_            The GPS HDOP value.
   * @param egnosHDOP_          The EGNOS HDOP value.
   * @param egnos_position_     1 EGNOS position, 0 preliminary EGNOS position.
   * @param msg_TO_             Table of EGNOS messages available or not.
   **/
  public final void writeUBloxPositionToSdCard(double[] currentPosition_,
      int egnos_, int sisnet_, int network_, double gpsHDOP_, 
      double egnosHDOP_, int egnos_position_, int msg_TO_[]) {
    String networkString = "";
    if (null != internalLogFileWriter) {
      if (currentPosition_[0] != -10.0 || currentPosition_[3] != -10.0) {
        try {
          Date dt = new Date();
          String curTime = dt.toString();
          internalLogFileWriter.write("\n" + curTime + "\n");

         if (network_ != 0)
           networkString = "ON";
         else
          networkString = "OFF";
          
          if(egnos_ ==  1)
        	  internalLogFileWriter.write("\nPosition from: Signal in Space.\n");
          if(egnos_ ==  0)
        	  internalLogFileWriter.write("\nPosition from: SiSNet.\n");
          if(egnos_ == -1)
        	  internalLogFileWriter.write("\nPosition from: GPS.\n");
          internalLogFileWriter.write("Internet : " + networkString + "\n");
          if (egnos_position_ == 1)
        	  internalLogFileWriter.write("Green Position.\n");
          else
        	  internalLogFileWriter.write("Orange Position.\n");
          if (currentPosition_ != null) {
            internalLogFileWriter.write("GPS HDOP: " 		+ gpsHDOP_ + "\n");
            internalLogFileWriter.write("GPS Latitude: " 	+ currentPosition_[0] + "\n");
            internalLogFileWriter.write("GPS Longitude: " 	+ currentPosition_[1] + "\n");
            internalLogFileWriter.write("GPS Altitude:" 	+ currentPosition_[2] + "\n");
            internalLogFileWriter.write("EGNOS HDOP: " 		+ egnosHDOP_ + "\n");
            internalLogFileWriter.write("EGNOS Latitude: "  + currentPosition_[3] + "\n");
            internalLogFileWriter.write("EGNOS Longitude: " + currentPosition_[4] + "\n");
            internalLogFileWriter.write("EGNOS Altitude: "  + currentPosition_[5] + "\n");
            internalLogFileWriter.write("PDOP   Latitude: "  + currentPosition_[475+18] + "\n");
            internalLogFileWriter.write("PDOP   Longitude: " + currentPosition_[476+18] + "\n");
            internalLogFileWriter.write("PDOP   Altitude: "  + currentPosition_[477+18] + "\n");
            internalLogFileWriter.write("GPS   Total Sats: "+ currentPosition_[8] 
                                       +" Used: " 			+ currentPosition_[10]
                                       +" Low elev.: " 		+ currentPosition_[9] + "\n");
            internalLogFileWriter.write("EGNOS Total Sats: "+ currentPosition_[11+18] 
                                       +" Used: " 			+ currentPosition_[14+18]
                                       +" Low elev.: " 		+ currentPosition_[12+18]
                                       +" Not Corrected: "	+ currentPosition_[13+18] + "\n");
            internalLogFileWriter.write("GPS   Iterations: "+ currentPosition_[11] + "\n");
            internalLogFileWriter.write("EGNOS Iterations: "+ currentPosition_[15+18] + "\n");
            internalLogFileWriter.write("GPS   Jump: "		+ currentPosition_[12] 
                                       +" On X: " 			+ currentPosition_[13]
                                       +" On Y: " 			+ currentPosition_[14] + "\n");
            internalLogFileWriter.write("EGNOS Jump: "		+ currentPosition_[16+18] 
                                       +" On X: " 			+ currentPosition_[17+18]
                                       +" On Y: " 			+ currentPosition_[18+18] + "\n");
            if(currentPosition_[10]==-1)
            	internalLogFileWriter.write("GPS   position generated NaN.\n");
            if(currentPosition_[14+18]==-1)
            	internalLogFileWriter.write("EGNOS position generated NaN.\n");
            for(int i=1; i<27; i++)
            	if(msg_TO_[i]==1)
            		internalLogFileWriter.write("Message " + i + " timed out.\n");
            for(int i=0; i<currentPosition_[11+18];i++)
				internalLogFileWriter.write( "PRN: "			+ currentPosition_[20 + i*26 + 18]
				                           + ", used: "			+ currentPosition_[21 + i*26 + 18]
				                           + ", rnd: "     + currentPosition_[22 + i*26 + 18]
				                           + ", prn_mask: "		+ currentPosition_[23 + i*26 + 18]
				                           + ", low elev: "		+ currentPosition_[24 + i*26 + 18]
				                           + ", tow2: "			+ currentPosition_[25 + i*26 + 18]
				                           + ", elevation: "	+ (int)currentPosition_[26 + i*26 + 18]
				                   		   + ", iono_delay: " 	+ currentPosition_[27 + i*26 + 18]
				                   		   + ", iono_model: " 	+ currentPosition_[28 + i*26 + 18]
				                   		   + ", tropo_delay: " 	+ currentPosition_[29 + i*26 + 18]
				                   		   + ", fast_delay: " 	+ currentPosition_[30 + i*26 + 18]
				                   		   + ", RRC: " 			+ currentPosition_[31 + i*26 + 18]
				                   		   + ", fast udrei: " 	+ currentPosition_[32 + i*26 + 18]
				                   		   + ", long match: "	+ (int)currentPosition_[33 + i*26 + 18]	   
				                   		   + ", daf0:  " 		+ currentPosition_[34 + i*26 + 18]
				                   		   + ", dx:  " 			+ currentPosition_[35 + i*26 + 18]                                 
				                   		   + ", dy:  " 			+ currentPosition_[36 + i*26 + 18]
				                   		   + ", dz:  " 			+ currentPosition_[37 + i*26 + 18]
				                   		   + ", Sigma2:  " 		+ currentPosition_[38 + i*26 + 18]
				                   		   + ", Sigma_flt2:  " 	+ currentPosition_[39 + i*26 + 18]
				                   		   + ", Sigma_tropo2:  "+ currentPosition_[40 + i*26 + 18]
				                           + ", Sigma_iono2:  " + currentPosition_[41 + i*26 + 18]
				                           + ", eps_fc:  " 		+ currentPosition_[42 + i*26 + 18]
								           + ", eps_rrc:  " 	+ currentPosition_[43 + i*26 + 18]
								           + ", eps_ltc:  "		+ currentPosition_[44 + i*26 + 18]
								           + ", eps_er:  " 		+ currentPosition_[45 + i*26 + 18]	   
				                   				   
				                   				   
				                   				   + "\n");
          }
          internalLogFileWriter.flush();
        } catch (IOException e) {
          Log.e(TAG_LOG, "Error: occured in writing to SD Card: ", e);
        }
      }
    }

    if (null != positionLogFileWriter) {
     if (egnos_position_ == 1) {
        try {
        	// TOW GPS ( Lat Long Altitude HDOP Satellites Used) 
        	// EGNOS ( Lat Long Altitude HDOP Satellites Used) 
        	// uBlox Lat Long Altitude HPL VPL Maritime HPL VPL Aviation
           
//            if (currentPosition_ != null) {
//          	  positionLogFileWriter.write(currentPosition_[43] + "," 
//              		+ currentPosition_[0] + "," + currentPosition_[1] + "," + currentPosition_[2] + ","
//              		+ gpsHDOP_ + "," + currentPosition_[10] + ","
//              		+ currentPosition_[3] + "," + currentPosition_[4] + "," + currentPosition_[5] + "," 
//              		+ egnosHDOP_  + "," + currentPosition_[14+18] + "," 
//              		+ currentPosition_[15] + "," + currentPosition_[16] + "," + currentPosition_[17] + ","
//              		+ (currentPosition_[6]*6.18) + "," + (currentPosition_[19+18]*5.33)+ ","
//              		+ (currentPosition_[6]*5.6) + "," 
//              		+ (currentPosition_[6]) + "," + (currentPosition_[19+18])+ ","
//              		+ (currentPosition_[475+18]) + "," + (currentPosition_[476+18])+ ","
//              		+ (currentPosition_[477+18]) + "\n");
//            }
          
          if (currentPosition_ != null) {
            positionLogFileWriter.write(currentPosition_[43] + "," //TOW
                + currentPosition_[0] + "," //GPS Latitude
                + currentPosition_[1] + "," //GPS Longitude
                + currentPosition_[2] + "," //GPS Altitude
                + currentPosition_[10] + "," // Number of satellites used to get GPS position
                + currentPosition_[3] + "," //EGNOS Latitude
                + currentPosition_[4] + "," //EGNOS Longitude
                + currentPosition_[5] + "," //EGNOS Altitude
                + currentPosition_[14+18]+ "," //Number of satellites used to get EGNOS position
                + currentPosition_[6]+  "," //HPL
                + currentPosition_[475 + 18] + "," //R&D Latitude
                + currentPosition_[476 + 18] + "," //R&D Longitude
                + currentPosition_[477 + 18] + ","  //R&D Altitude
                + currentPosition_[520] +"\n");//Number of satellites used to get R&D position
                
          }
          positionLogFileWriter.flush();
        } catch (IOException e) {
          Log.e(TAG_LOG, "Error: occured in writing to SD Card: ", e);
        }
      }
    }
  }

    /**
   * lograwToSdCard function 
   * 
   * Log the GPS raw data to the log file.
   * @param rxmRaw       The raw GPS data from the receiver.
   **/
  public final void logRawToSdCard(final String[] rxmRaw) {
    BackgroundThreadWriteRaw writeThread = new BackgroundThreadWriteRaw(rxmRaw);
    writeThread.run();
  }

  /**
   * BackgroundThreadWriteEgnos thread 
   * Background thread that writes raw GPS data to the log file.
   **/
  class BackgroundThreadWriteRaw extends Thread {
    private String[] rxmRaw_;

    /**
     * BackgroundThreadWriteRaw constructor
     * 
     * A constructor to write raw GPS data to the log file named 
     * internallogfile+"current date".log.
     * @param rxmRaw        The raw GPS data from the receiver.
     **/
    BackgroundThreadWriteRaw(final String[] rxmRaw) {
      this.rxmRaw_ = rxmRaw;
    }

    @Override
    public void run() {
    	if(null != internalLogFileWriter)
           writeRawToSdCard(rxmRaw_);
    };
  }

  /**
   * writeRawToSdCard function 
   * 
   * Writes raw GPS data to the log file named internallogfile+"current date".log.
   * @param rxmRaw          The raw GPS data from the receiver.
   **/
  public final void writeRawToSdCard(String[] rxmRaw) {
    try {
    	internalLogFileWriter.write(" SV | iTow    | Pseudorange          | SNR\n");
    	for (int j = 0; j < rxmRaw.length; j++) {
    	internalLogFileWriter.write(rxmRaw[j] + "\n");}
    	internalLogFileWriter.flush();
    } catch (IOException e) {
    	Log.e(TAG_LOG, "Error: occured in writing to SD Card: " ,e);
    }
  }
  
	/**
	 * logError function 
	 * 
	 * Log the Error messages to the log file error+"current date".log.
	 * @param errorMessage       The error message to write.
	 **/
	public final void logError(final String errorMessage) {
		BackgroundThreadWriteError writeErrorThread = new BackgroundThreadWriteError(
				errorMessage);
		writeErrorThread.start();
	}

	/**
	 * BackgroundThreadWriteError thread 
	 * 
	 * Background thread that writes error messages to the log file error+"current date".log.
	 **/
	class BackgroundThreadWriteError extends Thread {
		private String errorMessage_;

		/**
		 * BackgroundThreadWriteError constructor
		 * 
		 * A constructor to write error messages to the log file error+"current date".log.
		 * @param errorMessage       The error message to write.
		 **/
		BackgroundThreadWriteError(final String errorMessage) {
			this.errorMessage_ = errorMessage;
		}

		public void run() {
			if(null != errorLogFileWriter  && GlobalState.getisExit() == false)
				writeErrorToSdCard(errorMessage_);
		};
	}

	/**
	 * writeErrorToSdCard function 
	 * 
	 * Writes error messages to the log file error+"current date".log.
	 * @param errorMessage       The error message to write.
	 **/
	public final void writeErrorToSdCard(String errorMessage) {
		try {
		      Date dt = new Date();
		      String curTime = dt.toString();
		      errorLogFileWriter.write(curTime + ": "+errorMessage + "\n");
		  	  errorLogFileWriter.flush();
		} catch (IOException e) {
			Log.e(TAG_LOG, "Error: occured in writing to SD Card: " ,e);
		}
	}
	
	  /**
	   * logHUIToSdCard function 
	   * 
	   * Log the HUI (Health, UTCm Ionospheric) data to the log file.
	   * @param utc, klob       The UTC and Klobuchar data from the receiver.
	   **/
	  public final void logHUIToSdCard(final double utc[], final double klob[]) {
	    BackgroundThreadWriteHUI writeThread = new BackgroundThreadWriteHUI(utc,klob);
	    writeThread.run();
	  }

	  /**
	   * BackgroundThreadWriteHUI thread 
	   * Background thread that writes HUI data to the log file.
	   **/
	  class BackgroundThreadWriteHUI extends Thread {
	    private double[] HUIutc;
	    private double[] HUIklb;

	    /**
	     * BackgroundThreadWriteHUI constructor
	     * 
	     * A constructor to write HUI data to the log file named 
	     * internallogfile+"current date".log.
	     * @param utc, klob      The HUI data from the receiver.
	     **/
	    BackgroundThreadWriteHUI(final double utc[], final double klob[]) {
	      this.HUIutc = utc;
	      this.HUIklb = klob;
	    }

	    @Override
	    public void run() {
	    	if(null != internalLogFileWriter)
	           writeHUIToSdCard(HUIutc, HUIklb);
	    };
	  }

	  /**
	   * writeHUIToSdCard function 
	   * 
	   * Writes HUI data to the log file named internallogfile+"current date".log.
	   * @param utc, klob           The raw GPS data from the receiver.
	   **/
	  public final void writeHUIToSdCard(final double utc[], final double klob[]) {
	    try {
	    	internalLogFileWriter.write("UTC Paramenters: ");
	    	for (int j = 0; j < 8; j++) {
	    		internalLogFileWriter.write(utc[j]+" ");
	    	}
	    	internalLogFileWriter.write("\n");
	    	
	    	internalLogFileWriter.write("Klobuchar Paramenters: ");
	    	for (int j = 0; j < 8; j++) {
	    		internalLogFileWriter.write(klob[j]+" ");
	    	}
	    	internalLogFileWriter.write("\n");
	    	
	    	internalLogFileWriter.flush();
	    } catch (IOException e) {
	    	Log.e(TAG_LOG, "Error: occured in writing to SD Card: " ,e);
	    }
	  }
	  
	  
	  /**
     * logHUIToSdCard function 
     * 
     * Log the HUI (Health, UTCm Ionospheric) data to the log file.
     * @param utc, klob       The UTC and Klobuchar data from the receiver.
     **/
    public final void logSatelliteData(double satPRN, final float[] xY, final double[] sat_pos_array) {
      BackgroundThreadWriteSatelliteData writeThread = new BackgroundThreadWriteSatelliteData(satPRN,xY,sat_pos_array);
      writeThread.run();
    }

    /**
     * BackgroundThreadWriteHUI thread 
     * Background thread that writes HUI data to the log file.
     **/
    class BackgroundThreadWriteSatelliteData extends Thread {
      int satPRN;
      private float[] jd_array;
      private double[] sat_pos_array;

      /**
       * BackgroundThreadWriteHUI constructor
       * 
       * A constructor to write HUI data to the log file named 
       * internallogfile+"current date".log.
       * @param utc, klob      The HUI data from the receiver.
       **/
      BackgroundThreadWriteSatelliteData(double satPRN2,final float[] xY, final double[] sat_pos_array) {
        this.satPRN = (int) satPRN2;
        this.jd_array = xY;
        this.sat_pos_array = sat_pos_array;
      }

      @Override
      public void run() {
        if(null != positionLogFileWriter)
          writeSatelliteDataToSdCard(satPRN,jd_array,sat_pos_array);
      };
    }

    /**
     * writeHUIToSdCard function 
     * 
     * Writes HUI data to the log file named internallogfile+"current date".log.
     * @param utc, klob           The raw GPS data from the receiver.
     **/
    public final void writeSatelliteDataToSdCard(int satPRN,final float[] jd_array, final double[] sat_pos_array) {
      try {
        positionLogFileWriter.write("\n JD Array: ");
        positionLogFileWriter.write("Satellite PRN: "+ satPRN); 
                 
       
        for(int i = 0; i <  jd_array.length; i = i+2)
          positionLogFileWriter.write("\n  "+jd_array[i] + 
              ", "+jd_array[i+1]);                                           
      
        positionLogFileWriter.write("\n Satellite Azimuth-Elevstion Interpolated ");   
        
         for(int kk = 0 ; kk < sat_pos_array.length; kk = kk +2)
           positionLogFileWriter.write("\n  "+sat_pos_array[kk] +
                 ", "+sat_pos_array[kk+1]);
         
         
         
         
//       for(int j = 0; j < jd_array.length; j++) {                 // change 145
//       positionLogFileWriter.write("\n "+ jd_array[j]);
//     }  
         
//       for(int kk = 0 ; kk < 145; kk++)
//          positionLogFileWriter.write("\n Satellite pos values 0: "
//              + sat_pos_array[(kk) / 1][0] + " 1: "
//              + sat_pos_array[(kk) / 1][1] + " 2: "
//              + sat_pos_array[(kk) / 1][2]);
        
        positionLogFileWriter.write("\n ******************************************");
        positionLogFileWriter.flush();
      } catch (IOException e) {
        Log.e(TAG_LOG, "Error: occured in writing to SD Card: " ,e);
      }
    }
    
    
    /**
     * logHUIToSdCard function 
     * 
     * Log the HUI (Health, UTCm Ionospheric) data to the log file.
     * @param utc, klob       The UTC and Klobuchar data from the receiver.
     **/
    public final void logSatelliteXY(double satPRN, double satAzimuth,
        double satElevation, double x, double y) {
    BackgroundThreadWriteSatelliteXY writeThread = new BackgroundThreadWriteSatelliteXY(
        satPRN, satAzimuth, satElevation, x, y);
    writeThread.run();
    }

    /**
     * BackgroundThreadWriteHUI thread 
     * Background thread that writes HUI data to the log file.
     **/
    class BackgroundThreadWriteSatelliteXY extends Thread {
      int satPRN; double satAzimuth;
      double satElevation, x, y;

      /**
       * BackgroundThreadWriteHUI constructor
       * 
       * A constructor to write HUI data to the log file named 
       * internallogfile+"current date".log.
       * @param utc, klob      The HUI data from the receiver.
       **/
      BackgroundThreadWriteSatelliteXY(double satPRN, double satAzimuth,
          double satElevation, double x, double y) {
        this.satPRN = (int) satPRN;
        this.satAzimuth = satAzimuth;
        this.satElevation = satElevation;
        this.x = x;
        this.y = y;
      }

      @Override
      public void run() {
        if(null != positionLogFileWriter)
          writeSatelliteXYToSdCard(satPRN,satAzimuth,satElevation,x,y);
      };
    }

    /**
     * writeHUIToSdCard function 
     * 
     * Writes HUI data to the log file named internallogfile+"current date".log.
     * @param utc, klob           The raw GPS data from the receiver.
     **/
    public final void writeSatelliteXYToSdCard(double satPRN, double satAzimuth,
        double satElevation, double x, double y) {
      try {

        positionLogFileWriter.write("Satellite PRN: "+ satPRN); 
        positionLogFileWriter.write("Satellite Azimuth: "+ satAzimuth); 
        positionLogFileWriter.write("Satellite Elevation: "+ satElevation); 
        positionLogFileWriter.write("Satellite X: "+ x); 
        positionLogFileWriter.write("Satellite Y: "+ y); 
        
        positionLogFileWriter.write("\n ******************************************");
        positionLogFileWriter.flush();
      } catch (IOException e) {
        Log.e(TAG_LOG, "Error: occured in writing to SD Card: " ,e);
      }
    }

}

/**
 * @file NMEAMesaages.java
 *
 * Provides functions for matrix computations.
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

import android.content.Context;
import android.util.Log;

import com.ec.egnossdk.NMEACreator.GPGGA;
import com.ec.egnossdk.NMEACreator.GPGLL;
import com.ec.egnossdk.NMEACreator.GPGSA;
import com.ec.egnossdk.NMEACreator.GPGSV;
import com.ec.egnossdk.NMEACreator.GPRMC;
import com.ec.egnossdk.NMEACreator.GPVTG;

public class NMEAMesaages {
  private String TAG = "NMEA-SETTING";
  String Preamble = "01100110";
  
  public String GPGGASentence;
  public String GPGLLSentence;
  public String GPGSASentence;
  public String GPRMCSentence;
  public String GPVTGSentence;
  public String[] GPGSVSentence;

  double WeekNo, Toe, Tow;
  double TowOld;

  Integer NumSatUse;
  Integer TotalSatInView;
  Double HDOP, VDOP, PDOP, TDOP;
  Double[] BLH = new Double[3];
  Double[] BLHOld = new Double[3];

  Integer[] SatId = new Integer[20];
  Integer[] Elevation = new Integer[20];
  Integer[] Azimuth = new Integer[20];
  Integer[] SNR = new Integer[20];

  Integer Hour, Minute; // Computed From Tow
  Double Second;
  Integer YY, MM, DD; // Computed From ( WeekNo & Toe )

  // GGA
  Integer GpsQualIndic;
  Double GeoSep;
  Double AgeDiffGps;
  Integer DiffRefStId;

  
  Context context;
 
  String status = "";
  String modeIndicator = "";
  
  public NMEAMesaages(Context context) {
    this.context = context;
  }
  
  public void createNMEAData() {
    int gps_pos = 0;
    NMEACreator Create1 = new NMEACreator();
   
    GPGGA ObjectGGA = Create1.new GPGGA();
    GPGLL ObjectGLL = Create1.new GPGLL();
    GPGSA ObjectGSA = Create1.new GPGSA();
    GPGSV ObjectGSV = Create1.new GPGSV();
    GPRMC ObjectRMC = Create1.new GPRMC();
    GPVTG ObjectVTG = Create1.new GPVTG();

    /*
     * // *************************************************** //
     * 
     *                   Fetch Data From SDK
     * 
     * // *************************************************** //
     */double[] position = GlobalState.getPosition();
    Log.d(TAG, "position[0]:" + position[0]);

    if (GlobalState.getisEgnosPosition() == 1) {// EGNOS position (All)
      Log.d(TAG, "EGNOS Green position is used");
      status = "A";
      modeIndicator = "D";
      gps_pos = 2;
      BLH[0] = position[3];
      BLH[1] = position[4];
      BLH[2] = position[5];
    } else if (GlobalState.getisEgnosPosition() == 0 && position[3] != 0) {// EGNOS position (Few)
      Log.d(TAG, "EGNOS Orange position is used");
      status = "A";
      modeIndicator = "D";
      gps_pos = 2;
      BLH[0] = position[3];
      BLH[1] = position[4];
      BLH[2] = position[5];
    } else if (GlobalState.getisEgnosPosition() == 2 && position[3] == 0) {// No EGNOS position, only/ GPS position
      Log.d(TAG, "GPS position is used");
      status = "A";
      modeIndicator = "A";
      gps_pos = 1;
      BLH[0] = position[0];
      BLH[1] = position[1];
      BLH[2] = position[2];
    }else {// no position available, so invalid data
      modeIndicator = "N";
      status = "V";
      gps_pos = 0;
    }

    if (BLH[0] != 0) {

      Tow = GlobalState.getGPSTOW();
      Log.d(TAG, "tow:" + Tow);
      
      if(TowOld == Tow)
        Tow = Tow+1;
        

      WeekNo = GlobalState.getGPSWN();
      Log.d(TAG, "weeeknb:" + WeekNo);

      Toe = GlobalState.getGPSTOE();
      Log.d(TAG, "toe:" + Toe);

      TotalSatInView = (int) GlobalState.getTotalSatInView();
      Log.d(TAG, "TotalSatInView:" + TotalSatInView);

      NumSatUse = (int) GlobalState.getNumSatUse();
      Log.d(TAG, "NumSatUse:" + NumSatUse);

      for (int i = 0; i < TotalSatInView; i++) {
        SatId[i] = (int) GlobalState.getSatId()[i];
        if(SatId[i]>=120 & SatId[i]<=138)
          SatId[i] = SatId[i] - 87;
          
        Elevation[i] = (int) GlobalState.getElevation()[i];
        Azimuth[i] = (int) GlobalState.getAzimuth()[i];
        SNR[i] = (int) GlobalState.getSNR()[i];
      }
      /*
       * Log.d(TAG,"SatId:" + SatId[0]); Log.d(TAG,"Elevation:" +
       * Elevation[0]); Log.d(TAG,"Azimuth:" + Azimuth[0]); Log.d(TAG,"SNR:"
       * + SNR[0]);
       */

      double[] dop = GlobalState.getDOP();
      HDOP = dop[0];
      VDOP = dop[1];
      PDOP = dop[2];
      TDOP = dop[3];

      double[] UtcHMS = Create1.GpsTowToUtc(Tow);
      Hour = (int) UtcHMS[0];
      Minute = (int) UtcHMS[1];
      Second = UtcHMS[2];

      double[] YYMMDDHHMMSS = Create1.GPSWeekToeToDate((int) WeekNo, (int) Toe);
      YY = (int) YYMMDDHHMMSS[0];
      MM = (int) YYMMDDHHMMSS[1];
      DD = (int) YYMMDDHHMMSS[2];

      // double [][] SatDetail = GlobalState.getSatelliteDetails();
      // Log.d(TAG,"SatDetail:" + SatDetail[0][0]);

      /*
       * // *************************************************** //
       * 
       *                   Create NMEA Sentences
       * 
       * // *************************************************** //
       */// GPGGA Sentence Creator
      ObjectGGA.NMEAType = "GPGGA";
      ObjectGGA.Hour = Hour;
      ObjectGGA.Minute = Minute;
      ObjectGGA.Second = Second;

      ObjectGGA.Latitude = BLH[0];
      ObjectGGA.Longitude = BLH[1];
      ObjectGGA.GpsQualIndic = gps_pos;
      ObjectGGA.NumSatUse = NumSatUse;
      ObjectGGA.HDOP = HDOP;
      ObjectGGA.Altitude = BLH[2];
      ObjectGGA.GeoSep = null;
      ObjectGGA.AgeDiffGps = null;
      ObjectGGA.DiffRefStId = null;
      GPGGASentence = Create1.CreatorGPGGA(ObjectGGA);
      GlobalState.setGPGGASentence(GPGGASentence);      
      //writeToFile(GPGGASentence);
      Log.d(TAG, "GPGGA:" + GPGGASentence);

      // GPGLL Sentence Creator
      ObjectGLL.NMEAType = "GPGLL";
      ObjectGLL.Latitude = BLH[0];
      ObjectGLL.Longitude = BLH[1];
      ObjectGLL.Hour = Hour;
      ObjectGLL.Minute = Minute;
      ObjectGLL.Second = Second;
      ObjectGLL.Status = status;
      ObjectGLL.ModeIndicator = modeIndicator;
      GPGLLSentence = Create1.CreatorGPGLL(ObjectGLL);
      GlobalState.setGPGLLSentence(GPGLLSentence);
      //writeToFile(GPGLLSentence);
      Log.d(TAG, "GPGLL:" + GPGLLSentence);

      // GPGSA Sentence Creator
      ObjectGSA.NMEAType = "GPGSA";
      ObjectGSA.DimMode = "A";  
      ObjectGSA.FixMode = Create1.GetFixMode(HDOP, VDOP, PDOP);
      if (NumSatUse > 12)
        ObjectGSA.SatIds = SatId;
      else {
        for (int i = 0; i < NumSatUse; i++)
          ObjectGSA.SatIds[i] = SatId[i];
      }
      ObjectGSA.PDOP = PDOP;
      ObjectGSA.HDOP = HDOP;
      ObjectGSA.VDOP = VDOP;
      GPGSASentence = Create1.CreatorGPGSA(ObjectGSA);
      GlobalState.setGPGSASentence(GPGSASentence);
      //writeToFile(GPGSASentence);
      Log.d(TAG, "GPGSA:" + GPGSASentence);

      // GPGSV Sentence Creator
      ObjectGSV.NMEAType = "GPGSV";
      ObjectGSV.TotalSenNum = (int)Math.ceil(TotalSatInView / 4.0);
      ObjectGSV.TotalSatInView = TotalSatInView;
      ObjectGSV.SatId = SatId;
      ObjectGSV.Elevation = Elevation;
      ObjectGSV.Azimuth = Azimuth;
      ObjectGSV.SNR = SNR;
      GPGSVSentence = Create1.CreatorGPGSV(ObjectGSV);
      GlobalState.setGPGSVSentence(GPGSVSentence);   

      
      for (int i = 0; i < ObjectGSV.TotalSenNum; i++) {
           Log.d(TAG, "GPGSV:" + GPGSVSentence[i]);
      }

      // GPRMC Sentence Creator
      ObjectRMC.NMEAType = "GPRMC";
      ObjectRMC.Hour = Hour;
      ObjectRMC.Minute = Minute;
      ObjectRMC.Second = Second;
      ObjectRMC.Status = status;
      ObjectRMC.Latitude = BLH[0];
      ObjectRMC.Longitude = BLH[1];
      ObjectRMC.DD = DD;
      ObjectRMC.MM = MM;
      ObjectRMC.YY = YY;

      if (BLHOld[0] == null || BLHOld == BLH) {
        ObjectRMC.SpeedN = null;
        ObjectRMC.CourseT = null;
      } else {
        double[] blh = { BLH[0], BLH[1], BLH[2] };
        double[] blhold = { BLHOld[0], BLHOld[1], BLHOld[2] };
        ObjectRMC.SpeedN = Create1
            .GetSpeedOverGroundN(blhold, TowOld, blh, Tow);
        ObjectRMC.CourseT = Create1.GetCourseOverGroundT(blhold, blh);
      }

      if (BLH[0] == null)
        ;
      else {
        double[] blh = { BLH[0], BLH[1], BLH[2] };
        ObjectRMC.MagVar = Create1.GetMagneticVariation(blh, YY, context);
        if (ObjectRMC.MagVar >= 0)
          ObjectRMC.MagVarDir = "E";
        else {
          ObjectRMC.MagVar = Math.abs(ObjectRMC.MagVar);
          ObjectRMC.MagVarDir = "W";
        }
      }

      ObjectRMC.ModeIndicator = status;
      GPRMCSentence = Create1.CreatorGPRMC(ObjectRMC);
      GlobalState.setGPRMCSentence(GPRMCSentence);
     // writeToFile(GPRMCSentence);
      Log.d(TAG, "GPRMC:" + GPRMCSentence);

      // GPVTG Sentence Creator
      ObjectVTG.NMEAType = "GPVTG";

      if (BLHOld[0] == null || BLHOld == BLH) {
        ObjectVTG.CourseT = null;
        ObjectVTG.CourseM = null;
        ObjectVTG.SpeedN = null;
        ObjectVTG.SpeedK = null;
      } else {
        double[] blh = { BLH[0], BLH[1], BLH[2] };
        double[] blhold = { BLHOld[0], BLHOld[1], BLHOld[2] };
        ObjectVTG.CourseT = Create1.GetCourseOverGroundT(blhold, blh);
        ObjectVTG.CourseM = Create1.GetCourseOverGroundM(blhold, blh, YY, context);
        ObjectVTG.SpeedN = Create1
            .GetSpeedOverGroundN(blhold, TowOld, blh, Tow);
        ObjectVTG.SpeedK = Create1
            .GetSpeedOverGroundK(blhold, TowOld, blh, Tow);
      }
      ObjectVTG.ModeIndicator = status;
      GPVTGSentence = Create1.CreatorGPVTG(ObjectVTG);
      GlobalState.setGPVTGSentence(GPVTGSentence);
      //writeToFile(GPVTGSentence);
      Log.d(TAG, "GPVTG:" + GPVTGSentence);
      
      String hexL = "4c";
      String hexU = "4C";
      
      Log.d(TAG, "4c: hex to bin: "+UtilsDemoApp.hexToBin(hexL));
      Log.d(TAG, "4C: hex to bin: "+UtilsDemoApp.hexToBin(hexU));

      // Update data
     BLHOld = BLH ;
     TowOld = Tow;
      
    } else {
      Log.d(TAG,
          "Waiting for EGNOS green pos.: " + GlobalState.getisEgnosPosition());
    }
  }
}

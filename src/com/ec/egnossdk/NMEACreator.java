/**
 * @file NMEACreator.java
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

import java.util.Arrays;

import com.ec.egnossdk.TSAGeoMag;

import android.content.Context;

public class NMEACreator{
  
  double a_WGS84=6378137.0;
  double f=1.0/298.257223563;
  double e_WGS84_SQUARED=2*f-f*f;
  
  public Integer GetFixMode(Double HDOP, Double VDOP, Double PDOP)
  {
    Integer fixmode = null ;
    
    if(VDOP!=null & HDOP!=null)
      fixmode= 3;
    
    if(VDOP==null & HDOP!=null)
      fixmode= 2;
    
    if(VDOP!=null & HDOP==null)
      fixmode= 1;
    
    return fixmode;
  }
  
  

  
  /**
   * GetSpeedOverGroundK function
   * Compute   Speed Over Ground
   * @param    L,B,H of previous position and the current position
   * @return   Speed Over Ground  Km/Hour
   */
  public double GetSpeedOverGroundK(double[] BLHold, double TowOld, double[] BLHnew, double TowNew)
  {
    double[] ENU = XYZtoENU(BLHold, BLHnew);
    double dis=Math.sqrt(ENU[0]*ENU[0] + ENU[1]*ENU[1] + ENU[2]*ENU[2]) ;
    
    double timDif=TowNew-TowOld;
    if(timDif<0)
      timDif=timDif+86400*7;
    
    double SpeedK=dis/timDif*3.6; //            ( m/s * 3.6 ) = ( km/hour )
        
    return SpeedK;
  }

  
  /**
   * GetSpeedOverGroundN function
   * Compute   Speed Over Ground
   * @param    L,B,H of previous position and the current position
   * @return   Speed Over Ground  knots
   */
  public double GetSpeedOverGroundN(double[] BLHold, double TowOld, double[] BLHnew, double TowNew)
  {
    double[] ENU = XYZtoENU(BLHold, BLHnew);
    double dis=Math.sqrt(ENU[0]*ENU[0] + ENU[1]*ENU[1] + ENU[2]*ENU[2]) ;
    
    double timDif=TowNew-TowOld;
    if(timDif<0)
      timDif=timDif+86400*7;
    
    double SpeedN=dis/timDif*3.6 / 1.852; //            ( m/s * 3.6 / 1.852) = ( knots )
        
    return SpeedN;
  }
  
  
  /**
   * GetCourseOverGroundT function
   * Compute   Course Over Ground
   * @param    L,B,H of previous position and the current position
   * @return   CourseT
   */
  public double GetCourseOverGroundT(double[] BLHold, double[] BLHnew )
  {
    double[] ENU = XYZtoENU(BLHold, BLHnew);
    double GroundT = Math.atan(ENU[0]/ENU[1])*180/Math.PI;
    
    return GroundT;
  }
  
  
  
  /**
   * GetCourseOverGroundM function
   * Compute   Course Over Ground
   * @param    L,B,H of previous position and the current position
   * @return   CourseM
   */
  public double GetCourseOverGroundM(double[] BLHold, double[] BLHnew, double Year, Context context)
  {
    double[] ENU = XYZtoENU(BLHold, BLHnew);
    double GroundT = Math.atan(ENU[0]/ENU[1])*180/Math.PI;
    
    TSAGeoMag testCase=new TSAGeoMag(context);
    double declination=testCase.getDeclination(BLHnew[0], BLHnew[1], Year, BLHnew[2]/1000.0);
    double GroundM=GroundT - declination;
    
    return GroundM;
  }

  
  /**
   * GetMagneticVariation function
   * Compute Magnetic Variation
   * 
   * The true bearing = magnetic bearing + declination.
   * 
   * @param    L,B,H & Year.
   * @return   MagVar
   */
  public double GetMagneticVariation(double[] BLHnew, double Year, Context context )
  {
    double MagVar = 0;
    TSAGeoMag testCase=new TSAGeoMag(context);
    MagVar=testCase.getDeclination(BLHnew[0], BLHnew[1], Year, BLHnew[2]/1000.0);
    
    return MagVar;
    
  }
  
  
  /**
   * GpsTowToUtc function
   * Convert GPS TOW into UTC: HH, MM, SS
   * @param    TOW: time of GPS week in second.
   * @return   Double UtcHMS{HH,MM,SS}
   */
  public double[] GpsTowToUtc(double TOW)
  {
    double[] UtcHMS={0,0,0};
    
    if(TOW!=0)
    {
      double SOD = (TOW-16)%86400;  // Seconds of Day
      if(SOD<0)
        SOD+=86400;
      
      double Hour=Math.floor( SOD/3600 );
      double Minute=Math.floor( (SOD-Hour*3600)/60 );
      double Second= SOD-Hour*3600 - Minute*60;
      
      UtcHMS[0]=Hour;
      UtcHMS[1]=Minute;
      UtcHMS[2]=Second;
      
    }
    return UtcHMS;
  }
  
  
  /**
   * GPSWeekToeToDate function
   * Convert GPS week & TOE into Gregorian date.
   * @param    weeeknb: GPS week number.
   * @param    toe:     GPS TOE 
   * @return   Double YYMMDDHHMMSS[6]
   */
  public double[] GPSWeekToeToDate(int weeknb, int toe)
  {
    double[] YYMMDDHHMMSS={0,0,0,0,0,0};
    
    double JD0=JD(1980,1,6,0,0,0);
    double JD1=JD0+(weeknb)*7 + toe/86400.0;
    YYMMDDHHMMSS = Gregorian(JD1);

    return YYMMDDHHMMSS;
  }
  
  
  /**
   * XYZtoENU function
   * Conversion from Cartesian to ENU (East North Up) coordinates
   * 
   * @param   BLHold     The BLH of previous position
   * @param   BLHnew     The BLH of current  position
   * @return  ENU        The ENU of new point w.r.t old point.
   */
  public double[] XYZtoENU( double[] BLHold, double[] BLHnew )
  {
    double[] ENU={0,0,0};
    
    double[] XYZnew = BLHtoXYZ(BLHnew);
    double[] XYZold = BLHtoXYZ(BLHold);

    // Calculation of East, North and Up values
    double E = -Math.sin(BLHold[1]*Math.PI/180)*(XYZnew[0]-XYZold[0]) + Math.cos(BLHold[1]*Math.PI/180)*(XYZnew[1]-XYZold[1]);
    double N = -Math.sin(BLHold[0]*Math.PI/180)* Math.cos(BLHold[1]*Math.PI/180)*(XYZnew[0]-XYZold[0]) - Math.sin(BLHold[0]*Math.PI/180)*Math.sin(BLHold[1]*Math.PI/180)*(XYZnew[1]-XYZold[1]) +  Math.cos(BLHold[0]*Math.PI/180)*(XYZnew[2]-XYZold[2]);
    double U = Math.cos(BLHold[0]*Math.PI/180)*Math.cos(BLHold[1]*Math.PI/180)*(XYZnew[0]-XYZold[0]) + Math.cos(BLHold[0]*Math.PI/180)*Math.sin(BLHold[1]*Math.PI/180)*(XYZnew[1]-XYZold[1]) + Math.sin(BLHold[0]*Math.PI/180)*(XYZnew[2]-XYZold[2]);

    ENU[0] = E;
    ENU[1] = N;
    ENU[2] = U;
    
    return ENU;
  }
  
  
  
  /**
   * BLHtoXYZ function
   * @param    BLH[]    Latitude, Longitude, Height
   * @return   XYZ[]
   */
  public double[] BLHtoXYZ(double[] BLH)
  {
    double[] XYZ = {0,0,0};
    
    BLH[0] *= Math.PI / 180; // Conversions to rad
    BLH[1] *= Math.PI / 180;

    double N = a_WGS84/Math.sqrt(1-e_WGS84_SQUARED*Math.sin(BLH[0])*Math.sin(BLH[0]));
    XYZ[0] = (N + BLH[2])*Math.cos(BLH[0])*Math.cos(BLH[1]);
    XYZ[1] = (N + BLH[2])*Math.cos(BLH[0])*Math.sin(BLH[1]);
    XYZ[2] = ((1-e_WGS84_SQUARED)*N + BLH[2])*Math.sin(BLH[0]);
    
    return XYZ;
  }
  
  
  
  /**
   * XYZtoBLH function
   * @param L 
   * @param    XYZ[]
   * @return   BLH[]    Latitude, Longitude, Height
   */
  public double[] XYZtoBLH(double[] XYZ)
  {
    double[] BLH = {0,0,0};
    
    double x=XYZ[0];
    double y=XYZ[1];
    double z=XYZ[2];
    double e2=e_WGS84_SQUARED;
    double a=a_WGS84;
    
    if(x>0 & y>0)
      BLH[1]=Math.atan(y/x);   

    if(x<0 & y>0)
      BLH[1]=Math.atan(y/x)+Math.PI;   

    if(x<0 & y<0)
      BLH[1]=Math.atan(y/x)-Math.PI;   

    if(x>0 & y<0)
      BLH[1]=Math.atan(y/x);   

    BLH[0]=Math.atan(z/Math.sqrt(x*x+y*y));
    double N=a/Math.sqrt(1-e2*(Math.sin(BLH[0]))*(Math.sin(BLH[0])));
    BLH[2]=z/Math.sin(BLH[0])-N*(1-e2);
    
    double dB=1;
    while(dB>Math.PI/180/60)   
    {
        double B0=BLH[0];
        
        BLH[0]=Math.atan(z*(N+BLH[2])/Math.sqrt(x*x+y*y)/(BLH[2]+N*(1-e2)));
        N=a/Math.sqrt(1-e2*(Math.sin(BLH[0]))*(Math.sin(BLH[0])));
        BLH[2]=z/Math.sin(BLH[0])-N*(1-e2);
        
        dB=Math.abs(BLH[0]-B0);
    }

    
    BLH[0]=BLH[0]*180/Math.PI;
    BLH[1]=BLH[1]*180/Math.PI;
    return BLH;
  }
  
  
  
  
  /**
   * CheckSum function
   * @param    input as a String
   * @return   XOR results of the Input String
   */
  int CheckSum(String input)
  {
    char[] InputChar=input.toCharArray();
    
    int checksum=0;
    for (int i=0; i<input.length();i++)
      checksum^=InputChar[i];
    
    return checksum;
  }
  
  
  /**
   * JD function
   * @param    Year, Month, Day, Hour, Minute, Second
   * @return   Julian Day
   */
  public static double JD(int yy, int mm, int dd, int hh, int mn, double ss) {

    double y;
    double m;
    
    if (mm < 3) {
      y = yy - 1;
      m = mm + 12;
    }

    else{

      y = yy;
        m = mm;
    }
    double A = Math.floor(y / 100);
    double B = 2 - A + Math.floor(A / 4);
    
    double c,d,u, v;
    
    c = hh/24.0;
    d= mn/1440.0;
    u= ss/86400.0;
    v  = Math.floor(365.25 * y);

    double tag = c + d + u, z;
    z = Math.floor(30.6001 * (m + 1));
    
    y = v + z + dd + B + 1720994.5 + tag;                                       

    return y;
  }
  
  
  public static double[] Gregorian( double Julianday )  
  {
    double[] gregorian={0,0,0,0,0,0};
    
    double j=Julianday+32044;
    double g=Math.floor(j/146097);
    double dg= j%146097;
    double c=Math.floor(  (Math.floor(dg/36524)+1)*3/4  );
    double dc=dg-c*36524;
    double b=Math.floor(dc/1461);
    double db=(dc%1461);
    double a=Math.floor((Math.floor(db/365)+1)*3/4);
    double da=db-a*365;
    double y=400*g+100*c+4*b+a;
    double m=Math.floor((5*da+308)/153)-2;
    double d=da-Math.floor((m+4)*153/5)+122;
    double Year=y-4800+Math.floor((m+2)/12);  
    double Month=((m+2)%12)+1;    

    double day=d+1.5;
    double Day=Math.floor(day);

    double hour=(day-Day)*24;
    double Hour=Math.floor(hour);

    double minute=(hour-Hour)*60;
    double Minute=Math.floor(minute);

    double Second=(minute-Minute)*60;
    
    
    gregorian[0]=Year;
    gregorian[1]=Month;
    gregorian[2]=Day;
    gregorian[3]=Hour;
    gregorian[4]=Minute;
    gregorian[5]=Second;
    
    return gregorian;
  }






  /**
   * CreatorGPGGA function
   * @param    Object of Class GPGGA
   * @return   String of Sentence GPGGA
   */
  public String CreatorGPGGA(GPGGA ObjectGGA)
  {
    
//    SentenceGGA="$GPGGA,121252.000,3937.3032,N,11611.6046,E,1,05,2.0,45.9,M,-5.7,M,,1234*77";
    String SentenceGGA = "";
    
    // Type
    if(ObjectGGA.NMEAType==null)
      SentenceGGA+=",";
    else
      SentenceGGA+=ObjectGGA.NMEAType+",";
    
    // UTC
    if(ObjectGGA.Hour==null)
      SentenceGGA+=",";
    else
    {
      if(ObjectGGA.Hour<10)
        SentenceGGA+="0"+(int)ObjectGGA.Hour;
      else
        SentenceGGA+=(int)ObjectGGA.Hour;
      
      if(ObjectGGA.Minute<10)
        SentenceGGA+="0"+(int)ObjectGGA.Minute;
      else
        SentenceGGA+=(int)ObjectGGA.Minute;
      
      if(ObjectGGA.Second<10)
        SentenceGGA+="0"+Math.round( ObjectGGA.Second*10000 )/10000.0 +",";
      else
        SentenceGGA+=Math.round( ObjectGGA.Second*10000 )/10000.0+",";
    }

    
    // latitude
    if(ObjectGGA.Latitude==null)
      SentenceGGA+=",,";
    else
    {
      int deg = (int)(Math.floor(Math.abs(ObjectGGA.Latitude)));
      double min = ( Math.abs(ObjectGGA.Latitude) - deg )*60;
      
      if(deg<10)
        SentenceGGA+="0"+deg;
      else
        SentenceGGA+=deg;
        
      if(min<10)
        SentenceGGA+="0"+Math.round( min*10000 )/10000.0+",";
      else
        SentenceGGA+=Math.round( min*10000 )/10000.0+",";
      
      // N,S
      if(ObjectGGA.Latitude>0)
        SentenceGGA+="N,";
      else
        SentenceGGA+="S,";
    }
    
    
    // longitude
    if(ObjectGGA.Longitude==null)
      SentenceGGA+=",,";
    else
    {
      int deg = (int)(Math.floor(Math.abs(ObjectGGA.Longitude)));
      double min = ( Math.abs(ObjectGGA.Longitude) - deg )*60;
      
      if(deg<10)
        SentenceGGA+="00"+deg;
      else if(deg<100)
        SentenceGGA+="0"+deg;
      else
        SentenceGGA+=deg;
        
      if(min<10)
        SentenceGGA+="0"+Math.round( min*10000 )/10000.0+",";
      else
        SentenceGGA+=Math.round( min*10000 )/10000.0+",";
      
      // E,W
      if(ObjectGGA.Longitude>0)
        SentenceGGA+="E,";
      else
        SentenceGGA+="W,";
    }
    
    
    //GPS Quality Indicator
    if(ObjectGGA.GpsQualIndic==null)
      SentenceGGA+=",";
    else
      SentenceGGA+=(int)ObjectGGA.GpsQualIndic+",";
    
    
    //Number of Satellite in Use
    if(ObjectGGA.NumSatUse==null)
      SentenceGGA+=",";
    else
    {
      if(ObjectGGA.NumSatUse<10)
        SentenceGGA+="0"+(int)ObjectGGA.NumSatUse+",";
      else
        SentenceGGA+=(int)ObjectGGA.NumSatUse+",";
    }

    
    // HDOP
    if(ObjectGGA.HDOP==null)
      SentenceGGA+=",";
    else
      SentenceGGA+=Math.round( ObjectGGA.HDOP*10000 )/10000.0+",";

    
    //Altitude
    if(ObjectGGA.Altitude==null)
      SentenceGGA+=",,";
    else
      SentenceGGA+=Math.round( ObjectGGA.Altitude*10000 )/10000.0+",M,";

        
    //Geoid separation
    if(ObjectGGA.GeoSep==null)
      SentenceGGA+=",,";
    else
      SentenceGGA+=Math.round( ObjectGGA.GeoSep*10000 )/10000.0+",M,";

    
    //Age of Differential GPS data
    if(ObjectGGA.AgeDiffGps==null)
      SentenceGGA+=",";
    else
      SentenceGGA+=Math.round( ObjectGGA.AgeDiffGps*10000 )/10000.0 +",";
      
    
    // Differential reference station ID
    if(ObjectGGA.DiffRefStId==null)
      SentenceGGA+="";
    else
    {
      if(ObjectGGA.DiffRefStId<10)
        SentenceGGA+="000"+(int)ObjectGGA.DiffRefStId;
      else if(ObjectGGA.DiffRefStId<100)
        SentenceGGA+="00"+(int)ObjectGGA.DiffRefStId;
      else if(ObjectGGA.DiffRefStId<1000)
        SentenceGGA+="0"+(int)ObjectGGA.DiffRefStId;
      else
        SentenceGGA+=(int)ObjectGGA.DiffRefStId;
    }

    String chkSumString = "";
    int chkSum = CheckSum(SentenceGGA);
    if (chkSum < 16)
      chkSumString = "0"+Integer.toHexString(chkSum);
    else 
      chkSumString = Integer.toHexString(chkSum);
    
    // Check Sum           +
    SentenceGGA="$"+SentenceGGA+"*"+chkSumString.toUpperCase();

    return SentenceGGA;
  }
  
  
  /**
   * CreatorGPGLL function
   * @param    Object of Class GPGLL
   * @return   String of Sentence GPGLL
   */
  public String CreatorGPGLL(GPGLL ObjectGLL)
  {
    
//    "$GPGLL,3723.2475,N,12158.3416,W,161229.487,A,*2C ";    
    
    String SentenceGLL = "";
    
    // Type
    if(ObjectGLL.NMEAType==null)
      SentenceGLL+=",";
    else
      SentenceGLL+=ObjectGLL.NMEAType+",";
    
    // latitude
    if(ObjectGLL.Latitude==null)
      SentenceGLL+=",,";
    else
    {
      int deg = (int)(Math.floor(Math.abs(ObjectGLL.Latitude)));
      double min = ( Math.abs(ObjectGLL.Latitude) - deg )*60;
      
      if(deg<10)
        SentenceGLL+="0"+deg;
      else
        SentenceGLL+=deg;
        
      if(min<10)
        SentenceGLL+="0";
      SentenceGLL+=Math.round( min*10000 )/10000.0+",";
      
      // N,S
      if(ObjectGLL.Latitude>0)
        SentenceGLL+="N,";
      else
        SentenceGLL+="S,";
    }


    // longitude
    if(ObjectGLL.Longitude==null)
      SentenceGLL+=",,";
    else
    {
      int deg = (int)(Math.floor(Math.abs(ObjectGLL.Longitude)));
      double min = ( Math.abs(ObjectGLL.Longitude) - deg )*60;
      
      if(deg<10)
        SentenceGLL+="00"+deg;
      else if(deg<100)
        SentenceGLL+="0"+deg;
      else
        SentenceGLL+=deg;
        
      if(min<10)
        SentenceGLL+="0";
      SentenceGLL+=Math.round( min*10000 )/10000.0+",";
      
      // E,W
      if(ObjectGLL.Longitude>0)
        SentenceGLL+="E,";
      else
        SentenceGLL+="W,";
    }


    // UTC
    if(ObjectGLL.Hour==null)
      SentenceGLL+=",";
    else
    {
      if(ObjectGLL.Hour<10)
        SentenceGLL+="0"+(int)ObjectGLL.Hour;
      else
        SentenceGLL+=(int)ObjectGLL.Hour;
      
      if(ObjectGLL.Minute<10)
        SentenceGLL+="0"+(int)ObjectGLL.Minute;
      else
        SentenceGLL+=(int)ObjectGLL.Minute;
      
      if(ObjectGLL.Second<10)
        SentenceGLL+="0"+Math.round( ObjectGLL.Second*10000 )/10000.0+",";
      else
        SentenceGLL+=Math.round( ObjectGLL.Second*10000 )/10000.0+",";
    }

    
    // Status
    if(ObjectGLL.Status==null)
      SentenceGLL+=",";
    else
      SentenceGLL+=ObjectGLL.Status+",";
    
    
    // Mode Indicator
    if(ObjectGLL.ModeIndicator==null)
      SentenceGLL+="";
    else
      SentenceGLL+=ObjectGLL.ModeIndicator;
    
    String chkSumString = "";
    int chkSum = CheckSum(SentenceGLL);
    if (chkSum < 16)
      chkSumString = "0"+Integer.toHexString(chkSum);
    else 
      chkSumString = Integer.toHexString(chkSum);
    
    // Check Sum           +
    SentenceGLL="$"+SentenceGLL+"*"+chkSumString.toUpperCase();

    
    return SentenceGLL;
  }
  
  
  /**
   * CreatorGPGSA function
   * @param    Object of Class GPGSA
   * @return   String of Sentence GPGSA
   */
  public String CreatorGPGSA(GPGSA ObjectGSA)
  {
    
//    "$GPGSA,A,3,04,05,,09,12,,,24,,,,,2.5,1.3,2.1*39"
    
    String SentenceGSA = "";
    
    // Type
    if(ObjectGSA.NMEAType==null)
      SentenceGSA+=",";
    else
      SentenceGSA+=ObjectGSA.NMEAType+",";
    
    //Dim Mode
    if(ObjectGSA.DimMode==null)
      SentenceGSA+=",";
    else
      SentenceGSA+=ObjectGSA.DimMode+",";
    
    //Fix Mode
    if(ObjectGSA.FixMode==null)
      SentenceGSA+=",";
    else
      SentenceGSA+=ObjectGSA.FixMode+",";

    // Satellite IDs
    for (int i=0;i<12;i++)
    {
      if(ObjectGSA.SatIds[i]==null)
        SentenceGSA+=",";
      else if(ObjectGSA.SatIds[i]<10)
        SentenceGSA+="0" + ObjectGSA.SatIds[i] + ",";
      else
        SentenceGSA+= ObjectGSA.SatIds[i] + ",";
    }


    // PDOP
    if(ObjectGSA.PDOP==null)
      SentenceGSA+=",";
    else
      SentenceGSA+=Math.round( ObjectGSA.PDOP*10000 )/10000.0+",";

    
    // HDOP
    if(ObjectGSA.HDOP==null)
      SentenceGSA+=",";
    else
      SentenceGSA+=Math.round( ObjectGSA.HDOP*10000 )/10000.0+",";
    
    
    // VDOP
    if(ObjectGSA.VDOP==null)
      SentenceGSA+="";
    else
      SentenceGSA+=Math.round( ObjectGSA.VDOP*10000 )/10000.0+",";
  
    String chkSumString = "";
    int chkSum = CheckSum(SentenceGSA);
    if (chkSum < 16)
      chkSumString = "0"+Integer.toHexString(chkSum);
    else 
      chkSumString = Integer.toHexString(chkSum);
    
    // Check Sum           +
    SentenceGSA="$"+SentenceGSA+"*"+chkSumString.toUpperCase();

    
    return SentenceGSA;
  }
  
  
  /**
   * CreatorGPGSV function
   * @param    Object of Class GPGSV
   * @return   String of Sentence GPGSV
   */
  public String[] CreatorGPGSV(GPGSV ObjectGSV)
  {
    
/*    "$GPGSV,3,1,08,07,57,045,43,09,48,303,48,04,44,144,,02,39,092,*7F"
    "$GPGSV,3,2,08,24,18,178,44,26,17,230,41,05,13,292,43,08,01,147,*75"
    "$GPGSV,3,3,08,,,,,,,,,,,,,,,,*71"*/
    
    String[] SentenceGSV = new String[ ObjectGSV.TotalSenNum ];
    for(int i=0;i<ObjectGSV.TotalSenNum; i++)
      SentenceGSV[i]="";
    
    // Create sentence one by one
    for (int i=0; i<ObjectGSV.TotalSenNum; i++)
    {
      // Type
      if(ObjectGSV.NMEAType==null)
        SentenceGSV[i]+=",";
      else
        SentenceGSV[i]+=ObjectGSV.NMEAType+",";
      
      // Total number of sentences
      if(ObjectGSV.TotalSenNum==null)
        SentenceGSV[i]+=",";
      else
      {
        SentenceGSV[i]+=ObjectGSV.TotalSenNum+",";
      }
      
      // This sentences No.
      SentenceGSV[i]+=(i+1)+",";
      
      // Total Number of Satellite in View
      if(ObjectGSV.TotalSatInView==null)
        SentenceGSV[i]+=",";
      else {
        
        if(ObjectGSV.TotalSatInView<10)
          SentenceGSV[i]+="0";
        
        SentenceGSV[i]+=ObjectGSV.TotalSatInView+",";
      }

      // 4 Satellites information
      for (int j=0;j<4;j++)
      {
        if(j+4*i >= ObjectGSV.TotalSatInView)
        {
//          for(int k=0;k<4*(4-j)-1;k++)
//            SentenceGSV[i]+=",";
          break;
        }
        
        
        if(ObjectGSV.SatId[j+4*i]==null)
          SentenceGSV[i]+=",";
        else
        {
          if(ObjectGSV.SatId[j+4*i]<10)
            SentenceGSV[i]+="0" + ObjectGSV.SatId[j+4*i] + ",";
          else
            SentenceGSV[i]+= ObjectGSV.SatId[j+4*i] + ",";
        }

        
        if(ObjectGSV.Elevation[j+4*i]==null)
          SentenceGSV[i]+=",";
        else
        {
          if(ObjectGSV.Elevation[j+4*i]<10)
            SentenceGSV[i]+="0" + ObjectGSV.Elevation[j+4*i] + ",";
          else
            SentenceGSV[i]+= ObjectGSV.Elevation[j+4*i] + ",";
        }

        
        if(ObjectGSV.Azimuth[j+4*i]==null)
          SentenceGSV[i]+=",";
        else
        {
          if(ObjectGSV.Azimuth[j+4*i]<10)
            SentenceGSV[i]+="00" + ObjectGSV.Azimuth[j+4*i] + ",";
          else if(ObjectGSV.Azimuth[j+4*i]<100)
            SentenceGSV[i]+= "0"+ObjectGSV.Azimuth[j+4*i] + ",";
          else
            SentenceGSV[i]+= ObjectGSV.Azimuth[j+4*i] + ",";
        }

        
        if(j!=3) // not the last satellite in this sentence
        {
          if(ObjectGSV.SNR[j+4*i]==null)
            SentenceGSV[i]+=",";
          else
          {
            if(ObjectGSV.SNR[j+4*i]<10)
              SentenceGSV[i]+="0" + ObjectGSV.SNR[j+4*i] + ",";
            else
              SentenceGSV[i]+= ObjectGSV.SNR[j+4*i] + ",";
          }
        }
        if(j==3) // the last satellite in this sentence
        {
          if(ObjectGSV.SNR[j+4*i]==null)
            SentenceGSV[i]+="";
          else
          {
            if(ObjectGSV.SNR[j+4*i]<10)
              SentenceGSV[i]+="0" + ObjectGSV.SNR[j+4*i];
            else
              SentenceGSV[i]+= ObjectGSV.SNR[j+4*i];
          }
        }
      }

      String chkSumString = "";
      int chkSum = CheckSum(SentenceGSV[i]);
      if (chkSum < 16)
        chkSumString = "0"+Integer.toHexString(chkSum);
      else 
        chkSumString = Integer.toHexString(chkSum);
      
      // Check Sum           +
      SentenceGSV[i]="$"+SentenceGSV[i]+"*"+chkSumString.toUpperCase();
    }

    
    return SentenceGSV;
  }
  
  
  /**
   * CreatorGPRMC function
   * @param    Object of Class GPRMC
   * @return   String of Sentence GPRMC
   */
  public String CreatorGPRMC(GPRMC ObjectRMC)
  {
//    "$GPRMC,002456,A,3553.5295,N,13938.6570,E,0.0,43.1,180700,7.1,W,A*3D";

    String SentenceRMC = "";
    
    // Type
    if(ObjectRMC.NMEAType==null)
      SentenceRMC+=",";
    else
      SentenceRMC+=ObjectRMC.NMEAType+",";
    
    // UTC
    if(ObjectRMC.Hour==null)
      SentenceRMC+=",";
    else
    {
      if(ObjectRMC.Hour<10)
        SentenceRMC+="0"+(int)ObjectRMC.Hour;
      else
        SentenceRMC+=(int)ObjectRMC.Hour;
      
      if(ObjectRMC.Minute<10)
        SentenceRMC+="0"+(int)ObjectRMC.Minute;
      else
        SentenceRMC+=(int)ObjectRMC.Minute;
      
      if(ObjectRMC.Second<10)
        SentenceRMC+="0"+Math.round( ObjectRMC.Second*10000 )/10000.0+",";
      else
        SentenceRMC+=Math.round( ObjectRMC.Second*10000 )/10000.0+",";
    }

    
    // Status
    if(ObjectRMC.Status==null)
      SentenceRMC+=",";
    else
      SentenceRMC+=ObjectRMC.Status+",";
    
    
    // latitude
    if(ObjectRMC.Latitude==null)
      SentenceRMC+=",,";
    else
    {
      int deg = (int)(Math.floor(Math.abs(ObjectRMC.Latitude)));
      double min = ( Math.abs(ObjectRMC.Latitude) - deg )*60;
      
      if(deg<10)
        SentenceRMC+="0"+deg;
      else
        SentenceRMC+=deg;
        
      if(min<10)
        SentenceRMC+="0";
      SentenceRMC+=Math.round( min*10000 )/10000.0+",";
      
      // N,S
      if(ObjectRMC.Latitude>0)
        SentenceRMC+="N,";
      else
        SentenceRMC+="S,";
    }

    
    // longitude
    if(ObjectRMC.Longitude==null)
      SentenceRMC+=",,";
    else
    {
      int deg = (int)(Math.floor(Math.abs(ObjectRMC.Longitude)));
      double min = ( Math.abs(ObjectRMC.Longitude) - deg )*60;
      
      if(deg<10)
        SentenceRMC+="00"+deg;
      else if(deg<100)
        SentenceRMC+="0"+deg;
      else
        SentenceRMC+=deg;
        
      if(min<10)
        SentenceRMC+="0";
      SentenceRMC+=Math.round( min*10000 )/10000.0+",";
      
      // E,W
      if(ObjectRMC.Longitude>0)
        SentenceRMC+="E,";
      else
        SentenceRMC+="W,";
    }

    
    // Speed over ground: knots
    if(ObjectRMC.SpeedN==null)
      SentenceRMC+=",";
    else
      SentenceRMC+=ObjectRMC.SpeedN+",";
    
    // Course over ground, degrees true
    if(ObjectRMC.CourseT==null)
      SentenceRMC+=",";
    else
      SentenceRMC+=ObjectRMC.CourseT+",";
    
    // DDMMYY
    if(ObjectRMC.DD==null)
      SentenceRMC+=",";
    else
    {
      if(ObjectRMC.DD<10)
        SentenceRMC+="0"+ObjectRMC.DD;
      else
        SentenceRMC+=ObjectRMC.DD;
      
      if(ObjectRMC.MM<10)
        SentenceRMC+="0"+ObjectRMC.MM;
      else
        SentenceRMC+=ObjectRMC.MM;
      
      int YY=ObjectRMC.YY%100;
      if(YY<10)
        SentenceRMC+="0"+YY+",";
      else
        SentenceRMC+=YY+",";
    }

    
    // Magnetic variation
    if(ObjectRMC.MagVar==null)
      SentenceRMC+=",,";
    else
      SentenceRMC+=Math.round( Math.abs(ObjectRMC.MagVar)*10000 )/10000.0+","+ObjectRMC.MagVarDir+",";
    
    
    // Mode Indicator
    if(ObjectRMC.ModeIndicator==null)
      SentenceRMC+="";
    else
      SentenceRMC+=ObjectRMC.ModeIndicator;

    String chkSumString = "";
    int chkSum = CheckSum(SentenceRMC);
    if (chkSum < 16)
      chkSumString = "0"+Integer.toHexString(chkSum);
    else 
      chkSumString = Integer.toHexString(chkSum);
    
    // Check Sum           +
    SentenceRMC="$"+SentenceRMC+"*"+chkSumString.toUpperCase();

    
    return SentenceRMC;
  }
  
  
  /**
   * CreatorGPVTG function
   * @param    Object of Class GPVTG
   * @return   String of Sentence GPVTG
   */
  public String CreatorGPVTG(GPVTG ObjectVTG)
  {
    
//    "$GPVTG,096.5,T,083.5,M,0.0,N,0.0,K,D*22"
    
    String SentenceVTG = "";
    
    // NMEA TYPE
    if(ObjectVTG.NMEAType==null)
      SentenceVTG+=",";
    else
      SentenceVTG+=ObjectVTG.NMEAType+",";
    
    // Course over ground, degrees true
    if(ObjectVTG.CourseT==null)
      SentenceVTG+=",,";
    else
      SentenceVTG+=ObjectVTG.CourseT+",T,";
    
    // Course over ground, degrees magnetic
    if(ObjectVTG.CourseM==null)
      SentenceVTG+=",,";
    else
      SentenceVTG+=ObjectVTG.CourseM+",M,";
    
    // Speed over ground: knots
    if(ObjectVTG.SpeedN==null)
      SentenceVTG+=",,";
    else
      SentenceVTG+=ObjectVTG.SpeedN+",N,";
    
    
    // Speed over ground: km/hr
    if(ObjectVTG.SpeedK==null)
      SentenceVTG+=",,";
    else
      SentenceVTG+=ObjectVTG.SpeedK+",K,";
    
    // Mode Indicator
    if(ObjectVTG.ModeIndicator==null)
      SentenceVTG+="";
    else
      SentenceVTG+=ObjectVTG.ModeIndicator;

    String chkSumString = "";
    int chkSum = CheckSum(SentenceVTG);
    if (chkSum < 16)
      chkSumString = "0"+Integer.toHexString(chkSum);
    else 
      chkSumString = Integer.toHexString(chkSum);
    
    // Check Sum           +
    SentenceVTG="$"+SentenceVTG+"*"+chkSumString.toUpperCase();

    
    return SentenceVTG;
  }
  
  
  /**
   * Class that includes parameters of Sentence GPGGA
   */
  public class GPGGA
  {
    
    public String NMEAType; // NMEA TYPE
    
    public Integer Hour;      // UTC of Position
    public Integer Minute;
    public Double Second;
    
    public Double Latitude; // Latitute
    public Double Longitude; // Longitude
    public Integer GpsQualIndic; // GPS Quality Indicator
    public Integer NumSatUse ; // Number of Satellite in Use
    public Double HDOP; // Horizontal dilution of precision
    public Double Altitude; // Altitude
    public Double GeoSep;// Geoid separation
    public Double AgeDiffGps;//  Age of Differential GPS data
    public Integer DiffRefStId;//  Differential reference station ID
    

    void display()
    {
      System.out.println("NMEA TYPE:  " + NMEAType);
      System.out.println("UTC of Position: " + Hour +":"+ Minute +":"+ Second);
      System.out.println("Latitute: " + Latitude+ " Deg");
      System.out.println("Longitude: " + Longitude+ " Deg");
      System.out.println("GPS Quality Indicator: " + GpsQualIndic);
      System.out.println("Number of Satellite in Use: " + NumSatUse);
      System.out.println("HDOP: " + HDOP);
      System.out.println("Altitude: " + Altitude + " m");
      System.out.println("Geoid separation: " + GeoSep + " m");
      System.out.println("Age of Differential GPS data: " + AgeDiffGps);
      System.out.println("Differential reference station ID: " + DiffRefStId);
    }

  }


  /**
   * Class that includes parameters of Sentence GPGLL
   */
  public class GPGLL
  {
    public String NMEAType; // NMEA TYPE
    public Double Latitude; // Latitute
    public Double Longitude; // Longitude

    // UTC of Position
    public Integer Hour; 
    public Integer Minute;
    public Double Second;

    public String Status; // Status
    public String ModeIndicator; // Mode Indicator


    void display()
    {
      System.out.println("NMEA TYPE:  " + NMEAType);
      System.out.println("Latitute: " + Latitude+ " Deg");
      System.out.println("Longitude: " + Longitude+ " Deg");
      System.out.println("UTC of Position: " + Hour +":"+ Minute +":"+ Second);
      System.out.println("Status: " + Status);
      System.out.println("Mode Indicator: " + ModeIndicator );
    }
  }

  /**
   * Class that includes parameters of Sentence GPGSA
   */
  public class GPGSA
  {
    
    public String NMEAType;// NMEA TYPE
    public String DimMode;// Dim Mode
    public Integer FixMode; // Fix Mode:  1 = Fix not available, 2 = 2D, 3 = 3D    
    public Integer[] SatIds = new Integer[12];// ID numbers of satellites used in solution
    public Double PDOP;// P dilution of precision
    public Double HDOP; // H dilution of precision
    public Double VDOP; // V dilution of precision

    
    void display()
    {
      System.out.println("NMEA TYPE:  " + NMEAType);
      System.out.println("Dim Mode: " + DimMode);
      System.out.println("Fix Mode: " + FixMode);
      System.out.println("ID numbers of satellites used in solution: "+ Arrays.toString(SatIds));
      System.out.println("PDOP: " + PDOP);
      System.out.println("HDOP: " + HDOP);
      System.out.println("VDOP: " + VDOP);
    }
  }

  /**
   * Class that includes parameters of Sentence GPGSV
   */
  public class GPGSV
  {
    
    public String NMEAType; // NMEA TYPE
    public Integer TotalSenNum; // Total number of sentences
    public Integer TotalSatInView;// Total Number of Satellite in View

    // Satellite ID number,  Elevation [degrees],  Azimuth [degrees],  SNR [dBHz]
    public Integer[] SatId;
    public Integer[] Elevation;
    public Integer[] Azimuth;
    public Integer[] SNR;

    void display()
    {
      System.out.println("NMEA TYPE:  " + NMEAType);
      System.out.println("Total number of sentences: " + TotalSenNum);
      System.out.println("Total Number of Satellite in View: " + TotalSatInView);

      for(int i=0;i<TotalSatInView;i++)
        System.out.println("Satellite ID number: " + SatId[i] +", Elevation: " + Elevation[i] +", Azimuth: " + Azimuth[i] +", SNR: " + SNR[i] );
    }
  }

  /**
   * Class that includes parameters of Sentence GPRMC
   */
  public class GPRMC
  {
    public String NMEAType; // NMEA TYPE

    // UTC of Position
    public Integer Hour; 
    public Integer Minute;
    public Double Second;

    public String Status; // Status
    public Double Latitude;// Latitute
    public Double Longitude;// Longitude
    public Double SpeedN ;// Speed over ground: knots
    public Double CourseT;// Course over ground, degrees true

    // Date
    public Integer YY; 
    public Integer MM;
    public Integer DD;

    public Double MagVar; //Magnetic variation
    public String MagVarDir; // E,W
    public String ModeIndicator; // Mode Indicator

    
    void display()
    {
      System.out.println("NMEA TYPE:  " + NMEAType);
      System.out.println("UTC of Position: " + Hour +":"+ Minute +":"+ Second);
      System.out.println("Status: " + Status);
      System.out.println("Latitute: " + Latitude+ " Deg");
      System.out.println("Longitude: " + Longitude+ " Deg");
      System.out.println("Speed Over Ground [knots]: " + SpeedN );
      System.out.println("Course over ground: " + CourseT );
      System.out.println("Date: Year-" + YY +"-Month-"+ MM +"-Day-"+ DD);
      System.out.println("Magnetic variation: " + MagVar + MagVarDir );
      System.out.println("Mode Indicator: " + ModeIndicator );
    }
  }


  /**
   * Class that includes parameters of Sentence GPVTG
   */
  public class GPVTG
  {
    
    public String NMEAType; // NMEA TYPE
    public Double CourseT ; // Course over ground, degrees true
    public Double CourseM ; // Course over ground, degrees magnetic
    public Double SpeedN ;  // Speed over ground: knots
    public Double SpeedK ;  // Speed over ground: km/hr
    public String ModeIndicator;// Mode Indicator

    void display()
    {
      System.out.println("NMEA TYPE:  " + NMEAType);
      System.out.println("Course over ground [degree true]: " + CourseT );
      System.out.println("Course over ground [degree magnetic]: " + CourseM );
      System.out.println("Speed Over Ground [knots]: " + SpeedN );
      System.out.println("Speed Over Ground [km/hr]: " + SpeedK );
      System.out.println("Mode Indicator: " + ModeIndicator );
    }
  }
  
  
//  
//  public static void main(String[] args) {
//    
//    NMEACreator Creator = new NMEACreator();
//    
////    SentenceGGA="$GPGGA,121252.000,3937.3032,N,11611.6046,E,1,05,2.0,45.9,M,-5.7,M,,0000*77";
//    GPGGA ObjectGGA=new GPGGA();
//    ObjectGGA.NMEAType="GPGGA";
//    ObjectGGA.Hour=12;      // UTC of Position
//    ObjectGGA.Minute=12;
//    ObjectGGA.Second=52.0;
//    ObjectGGA.Latitude=39.62172; // Latitute
//    ObjectGGA.Longitude=116.19341; // Longitude
//    ObjectGGA.GpsQualIndic=1; // GPS Quality Indicator
//    ObjectGGA.NumSatUse=5 ; // Number of Satellite in Use
//    ObjectGGA.HDOP=2.0; // Horizontal dilution of precision
//    ObjectGGA.Altitude=45.9; // Altitude
//    ObjectGGA.GeoSep=-5.7;// Geoid separation
//    ObjectGGA.AgeDiffGps=null;//  Age of Differential GPS data
//    ObjectGGA.DiffRefStId=0000;//  Differential reference station ID
//    System.out.println(Creator.CreatorGPGGA(ObjectGGA));
////    $GPGGA,121252.0,3937.3032,N,11611.6046,E,1,05,2.0,45.9,M,-5.7,M,,0000*75
//    
//    
////    "$GPGLL,3723.2475,N,12158.3416,W,161229.487,A,*2C "
//    GPGLL ObjectGLL=new GPGLL();
//    ObjectGLL.NMEAType="GPGLL";
//    ObjectGLL.Latitude=37.3874584;
//    ObjectGLL.Longitude=-121.97236;
//    ObjectGLL.Hour=16;
//    ObjectGLL.Minute=12;
//    ObjectGLL.Second=29.487;
//    ObjectGLL.Status="A";
//    ObjectGLL.ModeIndicator="";
//    System.out.println(Creator.CreatorGPGLL(ObjectGLL));
////    $GPGLL,3723.2474,N,12158.3415,W,161229.487,A,*2
//    
//    
////    "$GPGSA,A,3,04,05,,09,12,,,24,,,,,2.5,1.3,2.1*39"
//    GPGSA ObjectGSA=new GPGSA();
//    ObjectGSA.NMEAType="GPGSA";
//    ObjectGSA.DimMode="A";
//    ObjectGSA.FixMode=3;
//    ObjectGSA.SatIds[0]=4;
//    ObjectGSA.SatIds[1]=5;
//    ObjectGSA.SatIds[3]=9;
//    ObjectGSA.SatIds[4]=12;
//    ObjectGSA.SatIds[7]=24;
//    ObjectGSA.PDOP=2.5;
//    ObjectGSA.HDOP=1.3;
//    ObjectGSA.VDOP=2.1;
//    System.out.println(Creator.CreatorGPGSA(ObjectGSA));
////    $GPGSA,A,3,04,05,,09,12,,,24,,,,,2.5,1.3,2.1*39
//    
//    
////    $GPRMC,002456,A,3553.5295,N,13938.6570,E,0.0,43.1,180700,7.1,W,A*3D
//    GPRMC ObjectRMC=new GPRMC();
//    ObjectRMC.NMEAType="GPRMC";
//    ObjectRMC.Hour=0;      // UTC of Position
//    ObjectRMC.Minute=24;
//    ObjectRMC.Second=56.0;
//    ObjectRMC.Status="A";
//    ObjectRMC.Latitude=35.89215833333; // Latitute
//    ObjectRMC.Longitude=139.644283333; // Longitude
//    ObjectRMC.SpeedN=0.0;
//    ObjectRMC.CourseT=43.1;
//    ObjectRMC.DD=18;
//    ObjectRMC.MM=7;
//    ObjectRMC.YY=0;
//    ObjectRMC.MagVar=7.1;
//    ObjectRMC.MagVarDir="W";
//    ObjectRMC.ModeIndicator="A";
//    System.out.println(Creator.CreatorGPRMC(ObjectRMC));
////    $GPRMC,002456.0,A,3553.5294,N,13938.6569,E,0.0,43.1,180700,7.1,W,A*2a
//    
//    
////    $GPVTG,096.5,T,083.5,M,0.0,N,0.0,K,D*22
//    GPVTG ObjectVTG=new GPVTG();
//    ObjectVTG.NMEAType="GPVTG";
//    ObjectVTG.CourseT=96.5;
//    ObjectVTG.CourseM=83.5;
//    ObjectVTG.SpeedN=0.0;
//    ObjectVTG.SpeedK=0.0;
//    ObjectVTG.ModeIndicator="D";
//    System.out.println(Creator.CreatorGPVTG(ObjectVTG));
////    $GPVTG,96.5,T,83.5,M,0.0,N,0.0,K,D*22
//    
//    
//    /*"$GPGSV,3,1,08,07,57,045,43,09,48,303,48,04,44,144,,02,39,092,*7F"
//      "$GPGSV,3,2,08,24,18,178,44,26,17,230,41,05,13,292,43,08,01,147,*75"
//      "$GPGSV,3,3,08,,,,,,,,,,,,,,,,*71"*/
//    
//    GPGSV ObjectGSV=new GPGSV();
//    
//    ObjectGSV.NMEAType="GPGSV";
//    ObjectGSV.TotalSenNum=3;
//    ObjectGSV.TotalSatInView=8;
//    
//    ObjectGSV.SatId=new Integer[ObjectGSV.TotalSatInView];
//    ObjectGSV.SatId[0]=7; 
//    ObjectGSV.SatId[1]=9; 
//    ObjectGSV.SatId[2]=4; 
//    ObjectGSV.SatId[3]=2;
//    ObjectGSV.SatId[4]=24; 
//    ObjectGSV.SatId[5]=26; 
//    ObjectGSV.SatId[6]=5; 
//    ObjectGSV.SatId[7]=8;
//    
//    ObjectGSV.Elevation=new Integer[ObjectGSV.TotalSatInView];
//    ObjectGSV.Elevation[0]=57; 
//    ObjectGSV.Elevation[1]=48; 
//    ObjectGSV.Elevation[2]=44; 
//    ObjectGSV.Elevation[3]=39;
//    ObjectGSV.Elevation[4]=18; 
//    ObjectGSV.Elevation[5]=17; 
//    ObjectGSV.Elevation[6]=13; 
//    ObjectGSV.Elevation[7]=1;
//    
//    ObjectGSV.Azimuth=new Integer[ObjectGSV.TotalSatInView];
//    ObjectGSV.Azimuth[0]=45; 
//    ObjectGSV.Azimuth[1]=303; 
//    ObjectGSV.Azimuth[2]=144; 
//    ObjectGSV.Azimuth[3]=92;
//    ObjectGSV.Azimuth[4]=178; 
//    ObjectGSV.Azimuth[5]=230; 
//    ObjectGSV.Azimuth[6]=292; 
//    ObjectGSV.Azimuth[7]=147;
//    
//    ObjectGSV.SNR=new Integer[ObjectGSV.TotalSatInView];
//    ObjectGSV.SNR[0]=43; 
//    ObjectGSV.SNR[1]=48; 
//
//    ObjectGSV.SNR[4]=44; 
//    ObjectGSV.SNR[5]=41; 
//    ObjectGSV.SNR[6]=43; 
//
//    
//    System.out.println(Creator.CreatorGPGSV(ObjectGSV)[0]);
//    System.out.println(Creator.CreatorGPGSV(ObjectGSV)[1]);
//    System.out.println(Creator.CreatorGPGSV(ObjectGSV)[2]);
//    
//    
//    
///*    //  Time System:         Gregorian() JD()
//    double Julian=JD(1987,05,05,12,12,12);
//    System.out.println(Julian);
//    
//    Double[] Gregor=Gregorian(Julian);
//    System.out.println(Arrays.toString(Gregor));*/
//    
//    
///*    //
//    Double[] YYMMDD=Creator.GPSWeekToeToDate(1268, 554715);
//    System.out.println(Arrays.toString(YYMMDD));*/
//    
//    
///*    double[] XYZ={6378137*0.5*Math.sqrt(2), 6378137*0.5*Math.sqrt(2), 6378137*Math.sin(Math.PI/3.0)};
//    double[] BLH= Creator.XYZtoBLH(XYZ);
//    XYZ=Creator.BLHtoXYZ(BLH);
//    
//    System.out.println(Arrays.toString(XYZ));
//    System.out.println(Arrays.toString(BLH));
//    System.out.println(Arrays.toString(XYZ));*/
//    
//    
//    
///*    double[] BLHold={0,0,100};
//    double[] BLHnew={1,1,100};
//    double[] ENU = Creator.XYZtoENU( BLHold, BLHnew );
//    System.out.println(Arrays.toString(ENU));*/
//
//
//    
//  } // end of main()

  
} // end of java


















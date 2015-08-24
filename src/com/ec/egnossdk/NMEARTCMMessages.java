/**
 * @file NMEARTCMMessages.java
 *
 * @brief Creates the RTCm messages.
 * @details For RTCM MT1, MT2 and MT3 are created. MT2 requires a 
 * EGNOS position to be available.
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
 */
package com.ec.egnossdk;

import android.util.Log;

import com.ec.egnossdk.NMEACreator;
import com.ec.egnossdk.RtcmHandler;


public class NMEARTCMMessages {
	// RTCM Parameters
	// Header
	int stationId = 1000;   // 0-1023
	int sequenceNo = 0;     // 0-7
	int stationHealth = 0;  // 0-7
	int modifiedZCount = 0; // Raw / 0.6
	
	// Message
	int[] scaleFactor = new int[20];
	int[] udre = new int[20];
	
	// MT1
	int[] satlliteId = new int[20];
	int[] iod = new int[20];
	double[] prCor = new double[20];
	double[] prCorRate = new double[20];
	double[] pr = new double[20];
	double[] satPosX = new double[20];
	double[] satPosY = new double[20];
	double[] satPosZ = new double[20];
	double[] prCorOld = new double[20];
	double[] prCorRateOld = new double[20];
	
	// MT2
	double[] deltaPrCor = new double[20];
	double[] deltaPrCorRate = new double[20];
	
	// MT3
	double[] ecefBLH = new double[3];
	double[] ecefXYZ = new double[3];
	double[] ecefgps = new double[3];
	Integer numSatUse;
	
	public void createRTCMData() {
    RtcmHandler MTCreator = new RtcmHandler();

    //Fetch RTCM Data From SDK
    numSatUse = (int) GlobalState.getNumSatUse();
    //Log.d("RTCM", "NumSatUse:  " + numSatUse);

    modifiedZCount = GlobalState.getModZcount();
    
    double[] position = GlobalState.getPosition();
    //Log.d("RTCM", "position[0]:  " + position[0]);
    
    //MT 1
    NMEACreator Create1 = new NMEACreator();
    
    // MT 3
    if (GlobalState.getisEgnosPosition() == 1) {// EGNOS position (All)
        Log.d("RTCM", "EGNOS Green position is used");
        ecefBLH[0] = position[3];
        ecefBLH[1] = position[4];
        ecefBLH[2] = position[5];
    }
    else if (GlobalState.getisEgnosPosition() == 0 && position[3] != 0) {// EGNOS position (Few)
        Log.d("RTCM", "EGNOS Orange position is used");
        ecefBLH[0] = position[3];
        ecefBLH[1] = position[4];
        ecefBLH[2] = position[5];
    }
    else if (GlobalState.getisEgnosPosition() == 2 && position[3] == 0) {// No EGNOS position, only/ GPS position
        Log.d("RTCM", "GPS position is used");
        ecefBLH[0] = position[0];
        ecefBLH[1] = position[1];
        ecefBLH[2] = position[2];
    }
    
    for (int i = 0; i < numSatUse; i++) {
      scaleFactor[i] = 0;
      udre[i] = 1;
      satlliteId[i] = (int) GlobalState.getPrnUse()[i];
      prCor[i] = GlobalState.getPrc()[i];
      prCorRate[i] = GlobalState.getRrc()[i];   
      iod[i] = (int) GlobalState.getIodc()[i];
    }

    ecefXYZ = Create1.BLHtoXYZ(ecefBLH);
    
    //Create RTCM Messages
    // MT 1
    int N = MTCreator.GetRtcmNumOfWord(1, numSatUse);
    char[][] RtcmMsgByte1 = new char[2 + N][5];
    String rtcmMsg1Header = null;
    String[] rtcmMsg1Body = new String[numSatUse];
    String rtcmMsg1 = null;

    MTCreator.CreateRtcm1(1, stationId, modifiedZCount, sequenceNo,
        numSatUse, stationHealth, scaleFactor, udre, satlliteId, prCor,
        prCorRate, iod, RtcmMsgByte1);
    
    rtcmMsg1Header = "1" + "," + stationId + "," + modifiedZCount * 0.6 + "," 
    		+ sequenceNo + "," + numSatUse + "," + stationHealth;
    
    rtcmMsg1 = rtcmMsg1Header;
    for(int i = 0; i < numSatUse; i++) {
    	rtcmMsg1Body[i] = scaleFactor[i] + "," + udre[i] + "," + satlliteId[i] 
    			+ "," + prCor[i] + "," + prCorRate[i] + "," + iod[i];
    	rtcmMsg1 = rtcmMsg1 + "\n" + rtcmMsg1Body[i];
    }
    
    rtcmMsg1 += "\n";
    Log.d("RTCM", "RTCM Message 1:" +rtcmMsg1);
    
    GlobalState.setRtcmMessage1(rtcmMsg1);
    GlobalState.setRtcmMessagesByte1(RtcmMsgByte1);
    sequenceNo = (sequenceNo + 1) % 8;
     //   For debugging only
//        for (int i=0; i<N+2; i++) {
//          Log.d("RTCM", "RTCM Message 1:" + String.valueOf(RtcmMsgByte1[i]));
//     }
        
     // MT 2
     if(prCorOld[0]!=0.0)
     {
	     for (int i=0; i<numSatUse; i++)
	     {
		     deltaPrCor[i] = prCorOld[i] - prCor[i];
		     deltaPrCorRate[i] = prCorRateOld[i] - prCorRate[i];
	     }
    	 
	     N = MTCreator.GetRtcmNumOfWord(2, numSatUse);
	     char[][] RtcmMsgByte2=new char[2+N][5];
	    
	     MTCreator.CreateRtcm2(2,stationId, modifiedZCount,sequenceNo,numSatUse,
	     stationHealth,
	     scaleFactor,udre,satlliteId,deltaPrCor,deltaPrCorRate,iod, RtcmMsgByte2);
	    
	     String rtcmMsg2Header = null;
	     String[] rtcmMsg2Body = new String[numSatUse];
	     String rtcmMsg2 = null;
	     
	     rtcmMsg2Header = "2" + "," + stationId + "," + modifiedZCount * 0.6 + "," 
	     		+ sequenceNo + "," + numSatUse + "," + stationHealth;
	     rtcmMsg2 = rtcmMsg2Header;
	     for(int i = 0; i < numSatUse; i++) {
	     	rtcmMsg2Body[i] = scaleFactor[i] + "," + udre[i] + "," + satlliteId[i] 
	     			+ "," + deltaPrCor[i] + "," + deltaPrCorRate[i] + "," + iod[i];
	        rtcmMsg2 = rtcmMsg2 + "\n" + rtcmMsg2Body[i];
	     }

	     if (deltaPrCor[0] != 0) {
	    	 rtcmMsg2 += "\n";
	       GlobalState.setRtcmMessage2(rtcmMsg2);
	       GlobalState.setRtcmMessagesByte2(RtcmMsgByte2);
	     }
	     else
	    	 rtcmMsg2 = "No EGNOS position -> no RTCM MT2";
	     
	     Log.d("RTCM", "RTCM Message 2:" +rtcmMsg2);

	     sequenceNo=(sequenceNo+1)%8;
     }
     
     // MT 3
     N = MTCreator.GetRtcmNumOfWord(3, numSatUse);
     char[][] RtcmMsgByte3 = new char[2 + N][5];
     
     MTCreator.CreateRtcm3(3, stationId, modifiedZCount, sequenceNo,
    		 numSatUse, stationHealth, ecefXYZ[0], ecefXYZ[1], ecefXYZ[2],
    		 RtcmMsgByte3);
     
     String rtcmMsg3Header = null;
     String rtcmMsg3Body = null;
     
     rtcmMsg3Header = "3" + "," + stationId + "," + modifiedZCount * 0.6 + ","
                    + sequenceNo + "," + numSatUse + "," + stationHealth + "\n";
     
     rtcmMsg3Body = ecefXYZ[0] + "," + ecefXYZ[1] + "," + ecefXYZ[2] + "\n";
     
     GlobalState.setRtcmMessage3(rtcmMsg3Header + rtcmMsg3Body);
     Log.d("RTCM", "RTCM Message 3:" +rtcmMsg3Header + rtcmMsg3Body);
     GlobalState.setRtcmMessagesByte3(RtcmMsgByte3);
     sequenceNo = (sequenceNo + 1) % 8;
     
     // Update data
     for(int p = 0; p< numSatUse;p++)
       prCorOld[p] = prCor[p];

     for(int prC = 0; prC< numSatUse;prC++)
        prCorRateOld[prC] = prCorRate[prC];
   } 
}

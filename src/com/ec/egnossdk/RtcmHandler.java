/**
 * @file RtcmHandler.java
 *
 *Recieve data from SDK, then, Create RTCM Sentences
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

import android.util.Log;


public class RtcmHandler {
	
	private static final long serialVersionUID = 1L;
	int D29Star = 1;
	int D30Star = 0;
	
	//  Length of each parameters in Header
	static int BitPreamble=8;
	static int BitMsgType=6;
	static int BitStationID=10;
	static int BitParity=6;
	
	static int BitModifiedZcount=13;
	static int BitSequenceNo=3;
	static int BitNumberOfDataWords=5;
	static int BitStationHealth=3;
	
    //  Length of each parameters in Words
	static int BitScaleFactor=1;
	static int BitUDRE=2;
	static int BitSatlliteID=5;
	static int BitPRCor=16;
	static int BitPRCorRate=8;
	static int BitIOD=8;
	
	
	// Input Parameters in Header
	int[] Preamble = {0,1,1,0,0,1,1,0};
	
/*	int MsgType; //  1-64
	int StationID; // 0-1023
	
	double ModifiedZcount;  // Raw / 0.6
	int SequenceNo; // 0-7
	int NumberOfSatellite;
	int NumberOfDataWords;  // Total words behind header
	int StationHealth; // 0-7
	
	// Input Parameters in MT1
	int ScaleFactor; //  1 or 0
	int UDRE; // 0,1,2,3
	int SatlliteID;
	double PRCor;
	double PRCorRate;
	int IOD;
	
	// MT2
	double DeltaPRCor;
	double DeltaPRCorRate;
	
	//MT3
	double ECEFX, ECEFY, ECEFZ;*/

	
	/**
	 * dec2bin function 
	 * 
	 * The function transforms decimal to binary.
	 * @param  decimal     The decimal values to be treated.
	 * @param  binary      The destination binary array.
	 * @param  size        The size of the binary array.
	 **/
	static void dec2bin(double decimal, int[] binary, int size)
	{
	  try {
		decimal=(int)(Math.rint(decimal));
		
		if(decimal>=0)
		{
			int  k=0,n=0;
			int  mod2;
			int[] temp=new int[100];
			
			do
			{
				mod2 = (int)decimal - (2 * (int)(decimal/2));	// modulus 2
				decimal   = (int)decimal / 2;
				temp[k++] = mod2 ;
			}while (decimal > 0);

			//fill with 0 to obtain a char of size characters
			while(k<size)
			{
				temp[k++] = 0 ;
			}
			while (k > 0)
				binary[n++] = temp[--k];
		}
		else
		{
			dec2bin( -1*decimal,  binary,  size);
			
			// BitWise
			for (int i=0; i<binary.length; i++)
				binary[i]=1-binary[i];
			// Plus 1
			int BirWisebinart=bin2dec(binary)+1;
			// to binary again
			dec2bin( BirWisebinart,  binary,  size);
		}
	  }catch(Exception e) {
	    Log.e("NMEA-SETTING", "Error occurred: "+e); 
	  }

	}
	
	
	/**
	 * bin2dec function 
	 * 
	 * The function transforms binary to decimal.
	 * @param  binary       The binary array to be converted.
	 * @return decimal      The destination decimal value.
	 **/
	static int bin2dec(int[] binary)
	{
		int decimal=0;
		for (int i=0; i<binary.length; i++)
			decimal=decimal + (int)( (binary[binary.length-1-i])*(Math.pow(2,i)) );
		return decimal;
	}
	
	
	/**
	 * ParityRTCM function 
	 * 
	 * The function determines the parity part of RTCM Message.
	 * @param  D29Star, D30Star     The last two parity bits of previous word.
	 * @param  data24               The RTCM message in this word.
	 * 
	 * @param  Parity6              The parity to be determined for this word.
	 **/
	static void ParityRTCM(int D29Star, int D30Star, int[] data24, int[] Parity6)
	{
		int D25=( D29Star+data24[0]+data24[1]+data24[2]+data24[4]+data24[5]
				+data24[9]+data24[10]+data24[11]+data24[12]+data24[13]
						+data24[16]+data24[17]+data24[19]+data24[22] ) %2;
		
		int D26=( D30Star+data24[1]+data24[2]+data24[3]+data24[5]+data24[6]
				+data24[10]+data24[11]+data24[12]+data24[13]+data24[14]
						+data24[17]+data24[18]+data24[20]+data24[23] ) %2;
		
		int D27=( D29Star+data24[0]+data24[2]+data24[3]+data24[4]+data24[6]
				+data24[7]+data24[11]+data24[12]+data24[13]+data24[14]
						+data24[15]+data24[18]+data24[19]+data24[21] ) %2;
		
		int D28=( D30Star+data24[1]+data24[3]+data24[4]+data24[5]+data24[7]
				+data24[8]+data24[12]+data24[13]+data24[14]+data24[15]
						+data24[16]+data24[19]+data24[20]+data24[22] ) %2;
		
		int D29=( D30Star+data24[0]+data24[2]+data24[4]+data24[5]+data24[6]
				+data24[8]+data24[9]+data24[13]+data24[14]+data24[15]
						+data24[16]+data24[17]+data24[20]+data24[21]+data24[23] ) %2;
		
		int D30=( D29Star+data24[2]+data24[4]+data24[5]+data24[7]+data24[8]
				+data24[9]+data24[10]+data24[12]+data24[14]+data24[18]
						+data24[21]+data24[22]+data24[23] ) %2;
		
		Parity6[0]=D25;  Parity6[1]=D26;  Parity6[2]=D27;
		Parity6[3]=D28;  Parity6[4]=D29;  Parity6[5]=D30;
		
	}
	
	
	/**
	 * XOR function 
	 * 
	 * The function returns the modual-2 result between 0 & 1.
	 * @param  i, j     Either 0, or 1.
	 * @return 0, or, 1
	 **/
	static int XOR(int i, int j) {

		if(i==j)
			return 0;
		else 
			return 1;
	}

	
	/**
	 * GetRtcmNumOfWord function 
	 * 
	 * The function generates the number of data words w.r.t given MsgType 
	 * @param  MsgType                  Inputs
	 * @param  NumberOfSatellite        Inputs
	 * 
	 * @return Number of Data Word
	 **/
	public final int GetRtcmNumOfWord(int MsgType, int NumberOfSatellite)
	{
		int N = 0;
		// How many words all together
		if(MsgType==1 | MsgType==2)
		{
			if(NumberOfSatellite%3 == 0)
				N=NumberOfSatellite*40/24;
			else
				N=NumberOfSatellite*40/24 +1;
		}
		else if (MsgType==3)
		{
			N=4;
		}
		else
		{
			System.out.println("Please Update the CreateRtcmHeader Function !!!" + "\n");
			System.out.println("-----Determine the Number of Data Words-----" + "\n");
		}
		return N;
	}
	
	
	/**
	 * CreateRtcmHeader function 
	 * 
	 * The function generates the header ( 1st and 2nd words ) of RTCM Message.
	 * @param  MsgType, StationID, ModifiedZcount.            Inputs
	 * @param  SequenceNo,NumberOfSatellite, StationHealth.   Inputs
	 * @param  HeaderLine1,HeaderLine2.                       Output, 24 binary bits of each word in header
	 **/
	public final void CreateRtcmHeader(	
			int MsgType,int StationID, int ModifiedZcount,
			int SequenceNo,int NumberOfSatellite, int StationHealth,
			
			int[] HeaderLine1, int[] HeaderLine2 )
	{
		
		int NumberOfDataWords = GetRtcmNumOfWord(MsgType, NumberOfSatellite);
					
		// ****************  First line  ****************
		// Preamble: 1-8
		for( int i=0; i<8; i++ ) 
			HeaderLine1[i]=Preamble[i];

		// Message Type: 9-14
		int[] BinaryMsgType = new int[6];
		dec2bin( MsgType, BinaryMsgType, 6);
		for(int i=0; i<6;i++)
			HeaderLine1[i+8]=BinaryMsgType[i];

		// StationID: 15-24
		int[] BinaryStationID = new int[10];
		dec2bin( StationID, BinaryStationID, 10);
		for(int i=0; i<10;i++)
			HeaderLine1[i+14]=BinaryStationID[i];

		//		bw.write(Arrays.toString(header1));
				
		
		// Write the Header   ****************  Second line  ****************
		// Modified Z count: 1-13
		int[] BinaryModifiedZcount = new int[13];
		dec2bin( ModifiedZcount/0.6, BinaryModifiedZcount, 13);
		for(int i=0; i<13;i++)
			HeaderLine2[i]=BinaryModifiedZcount[i];
					
		// Sequence No: 14-16
		int[] BinarySequenceNo = new int[3];
		dec2bin( SequenceNo, BinarySequenceNo, 3);
		for(int i=0; i<3;i++)
			HeaderLine2[i+13]=BinarySequenceNo[i];
		
		// Number Of Data Words: 17-21
		int[] BinaryNumberOfDataWords = new int[5];
		dec2bin( NumberOfDataWords, BinaryNumberOfDataWords, 5);
		for(int i=0; i<5;i++)
			HeaderLine2[i+16]=BinaryNumberOfDataWords[i];
		
		// StationHealth: 22-24
		int[] BinaryStationHealth = new int[3];
		dec2bin( StationHealth, BinaryStationHealth, 3);
		for(int i=0; i<3;i++)
			HeaderLine2[i+21]=BinaryStationHealth[i];

//		bw.write(Arrays.toString(header2));
	}
	

	/**
	 * CreateRtcm1 function 
	 * 
	 * The function performs the RTCM MT1 Creation.
	 * @param  MsgType, StationID, ModifiedZcount.            Inputs
	 * @param  SequenceNo,NumberOfSatellite, StationHealth.   Inputs
	 * 
	 * @param  ScaleFactor, UDRE, SatlliteID.                 Inputs
	 * @param  PRCor, PRCorRate, IOD.                         Inputs
	 * 
	 * @param  RtcmMsgByte // Whole Msg in Byte               Output
	 **/
	public final void CreateRtcm1(	
			int MsgType,int StationID, int ModifiedZcount,
			int SequenceNo,int NumberOfSatellite, int StationHealth,
	
			int[] ScaleFactor,int[] UDRE,int[] SatlliteID,
			double[] PRCor,double[] PRCorRate,int[] IOD,
			
			char[][] RtcmMsgByte) 
	{
		
//		char[][] RtcmMsgByte=new char[NumberOfDataWords+2][5]; // ByteWord for whole Msg
		int[] ByteWord= new int[5];  // ByteWord[5] for each line
		int[] Parity6=new int[6];
		
		// Generate the Header Message
		int[] HeaderLine1=new int[24];
		int[] HeaderLine2=new int[24];
		
		CreateRtcmHeader( MsgType, StationID,  ModifiedZcount,
				SequenceNo, NumberOfSatellite,  StationHealth,
				
				HeaderLine1, HeaderLine2 );

// 			**************** Header First line  ****************
					
		//  *******  Parity  ********
		ParityRTCM( D29Star,  D30Star, HeaderLine1, Parity6);
		
		// XOR
		for (int i=0; i<24; i++)
		{
			if(D30Star==0)
				HeaderLine1[i]=HeaderLine1[i];
			else
				HeaderLine1[i]=1-HeaderLine1[i];
		}
		
		// Update D29Star & D30Star
		D29Star=Parity6[4];
		D30Star=Parity6[5];
		
		// ByteWord 1-4
		for (int i=0; i<4; i++)
		{
			ByteWord[i]=(int) Math.pow(2,6);
			for (int j=0; j<6; j++)
			{
				ByteWord[i]=ByteWord[i]+(int)( HeaderLine1[i*6+j]*Math.pow(2,j) );
			}
			RtcmMsgByte[0][i]=(char)(ByteWord[i]);
		}

		// ByteWord 5
		ByteWord[4]=(int) Math.pow(2,6);
		for (int i=0; i<6; i++)
			ByteWord[4]=ByteWord[4]+(int)( Parity6[i]*Math.pow(2,i) );
		RtcmMsgByte[0][4]=(char)(ByteWord[4]);
		

// 			**************** Header Second line  ****************
		
		//  *******  Parity  ********
		ParityRTCM( D29Star,  D30Star, HeaderLine2, Parity6);
		
		// XOR
		for (int i=0; i<24; i++)
		{
			if(D30Star==0)
				HeaderLine2[i]=HeaderLine2[i];
			else
				HeaderLine2[i]=1-HeaderLine2[i];
		}
		
		// Update D29Star & D30Star
		D29Star=Parity6[4];
		D30Star=Parity6[5];
		
		// ByteWord 1-4
		for (int i=0; i<4; i++)
		{
			ByteWord[i]=(int) Math.pow(2,6);
			for (int j=0; j<6; j++)
			{
				ByteWord[i]=ByteWord[i]+(int)( HeaderLine2[i*6+j]*Math.pow(2,j) );
			}
			RtcmMsgByte[1][i]=(char)(ByteWord[i]);
		}

		// ByteWord 5
		ByteWord[4]=(int) Math.pow(2,6);
		for (int i=0; i<6; i++)
			ByteWord[4]=ByteWord[4]+(int)( Parity6[i]*Math.pow(2,i) );
		RtcmMsgByte[1][4]=(char)(ByteWord[4]);
		
		
		//************************************************************************
		// Write the Content of Message Type 1  
		int NumberOfDataWords = GetRtcmNumOfWord(MsgType, NumberOfSatellite);
		
		int[][] ContentLine=new int[NumberOfDataWords][24];
		int[][] SatelliteData=new int[NumberOfSatellite][40];
		
		// Save each satellite data (40 bits) into SatelliteData
		for( int Sat=0; Sat<NumberOfSatellite; Sat++)
		{
			// Scale factor  1
			int[] BinaryScaleFactor = new int[1];
			dec2bin( ScaleFactor[Sat], BinaryScaleFactor, 1);
			for(int i=0; i<1;i++)
				SatelliteData[Sat][i]=BinaryScaleFactor[i];
			
			double SfPRC, SfRRC;
			if(ScaleFactor[Sat]==0)
			{
				SfPRC=0.02;
				SfRRC=0.002;
			}
			else
			{
				SfPRC=0.32;
				SfRRC=0.032;
			}
			
			// UDRE  2-3
			int[] BinaryUDRE = new int[2];
			dec2bin( UDRE[Sat], BinaryUDRE, 2);
			for(int i=0; i<2;i++)
				SatelliteData[Sat][i+1]=BinaryUDRE[i];
			
			// SatlliteID  4-8
			int[] BinarySatlliteID = new int[5];
			if(SatlliteID[Sat]!=32)
				dec2bin( SatlliteID[Sat], BinarySatlliteID, 5);
			for(int i=0; i<5;i++)
				SatelliteData[Sat][i+3]=BinarySatlliteID[i];
			
			// PR Correction 9-24
			int[] BinaryPRCor = new int[16];
			dec2bin( (PRCor[Sat])/SfPRC, BinaryPRCor, 16);
			for(int i=0; i<16;i++)
				SatelliteData[Sat][i+8]=BinaryPRCor[i];
			
			// PR Correction Rate  25-32
			int[] BinaryPRCorRate = new int[8];
			dec2bin( (PRCorRate[Sat])/SfRRC, BinaryPRCorRate, 8);
			for(int i=0; i<8;i++)
				SatelliteData[Sat][i+24]=BinaryPRCorRate[i];
			
			// IOD  33-40
			int[] BinaryIOD = new int[8];
			dec2bin( IOD[Sat], BinaryIOD, 8);
			for(int i=0; i<8;i++)
				SatelliteData[Sat][i+32]=BinaryIOD[i];

		}
		
		// Distribute SatelliteData into ContentLine
		int Count=0;
		for (int i=0; i<NumberOfSatellite; i++)
		{
			for (int j=0; j<40; j++)
			{
				ContentLine[Count/24][Count%24]=SatelliteData[i][j];
				Count=Count+1;
			}
		}
		// Fill( 1 & 0 )
		if(NumberOfSatellite%3 !=0)
			for(int i=Count%24; i<24; i++)
				// even i, return 1. Odd i, renturn 0.
				ContentLine[Count/24][i]=1-i%2;  
		
		
//			bw.write(Arrays.toString(ContentLine[i]));
		for (int i=0; i<NumberOfDataWords; i++)
		{
			//  *******  Parity  ********
			ParityRTCM( D29Star,  D30Star, ContentLine[i], Parity6);
			
			// XOR
			for (int j=0; j<24; j++)
			{
				if(D30Star==0)
					ContentLine[i][j]=ContentLine[i][j];
				else
					ContentLine[i][j]=1-ContentLine[i][j];
			}
			
			// Update D29Star & D30Star
			D29Star=Parity6[4];
			D30Star=Parity6[5];
			
			// ByteWord 1-4
			for (int k=0; k<4; k++)
			{
				ByteWord[k]=(int) Math.pow(2,6);
				for (int m=0; m<6; m++)
				{
					ByteWord[k]=ByteWord[k]+(int)( ContentLine[i][k*6+m]*Math.pow(2,m) );
				}
				RtcmMsgByte[i+2][k]=(char)(ByteWord[k]);
			}

			// ByteWord 5
			ByteWord[4]=(int) Math.pow(2,6);
			for (int m=0; m<6; m++)
				ByteWord[4]=ByteWord[4]+(int)( Parity6[m]*Math.pow(2,m) );
			RtcmMsgByte[i+2][4]=(char)(ByteWord[4]);

		}

	}
	
	
	/**
	 * CreateRtcm2 function 
	 * 
	 * The function performs the RTCM MT2 Creation.
	 * @param  MsgType, StationID, ModifiedZcount.            Inputs
	 * @param  SequenceNo,NumberOfSatellite, StationHealth.   Inputs
	 * 
	 * @param  ScaleFactor, UDRE, SatlliteID.                 Inputs
	 * @param  DeltaPRCor, DeltaPRCorRate, IOD.               Inputs
	 * 
	 * @param  RtcmMsgByte // Whole Msg in Byte               Output
	 **/
	public final void CreateRtcm2(	
			int MsgType,int StationID, int ModifiedZcount,
			int SequenceNo,int NumberOfSatellite, int StationHealth,
	
			int[] ScaleFactor,int[] UDRE,int[] SatlliteID,
			double[] DeltaPRCor,double[] DeltaPRCorRate,int[] IOD,
			
			char[][] RtcmMsgByte) 
	{
		
		int[] ByteWord= new int[5];  // ByteWord[5] for each line
		int[] Parity6=new int[6];
		
		// Generate the Header Message
		int[] HeaderLine1=new int[24];
		int[] HeaderLine2=new int[24];
		
		CreateRtcmHeader( MsgType, StationID,  ModifiedZcount,
				SequenceNo, NumberOfSatellite,  StationHealth,
				
				HeaderLine1, HeaderLine2 );

//			**************** Header First line  ****************

		//  *******  Parity  ********
		ParityRTCM( D29Star,  D30Star, HeaderLine1, Parity6);
		
		// XOR
		for (int i=0; i<24; i++)
		{
			if(D30Star==0)
				HeaderLine1[i]=HeaderLine1[i];
			else
				HeaderLine1[i]=1-HeaderLine1[i];
		}
		
		// Update D29Star & D30Star
		D29Star=Parity6[4];
		D30Star=Parity6[5];
		
		// ByteWord 1-4
		for (int i=0; i<4; i++)
		{
			ByteWord[i]=(int) Math.pow(2,6);
			for (int j=0; j<6; j++)
			{
				ByteWord[i]=ByteWord[i]+(int)( HeaderLine1[i*6+j]*Math.pow(2,j) );
			}
			RtcmMsgByte[0][i]=(char)(ByteWord[i]);
		}

		// ByteWord 5
		ByteWord[4]=(int) Math.pow(2,6);
		for (int i=0; i<6; i++)
			ByteWord[4]=ByteWord[4]+(int)( Parity6[i]*Math.pow(2,i) );
		RtcmMsgByte[0][4]=(char)(ByteWord[4]);

					
// 			**************** Header Second line  ****************
		
//			bw.write(Arrays.toString(HeaderLine2));
//			bw.write(String.valueOf(HeaderLine1[i]));
		
		//  *******  Parity  ********
		ParityRTCM( D29Star,  D30Star, HeaderLine2, Parity6);
		
		// XOR
		for (int i=0; i<24; i++)
		{
			if(D30Star==0)
				HeaderLine2[i]=HeaderLine2[i];
			else
				HeaderLine2[i]=1-HeaderLine2[i];
		}
		
		// Update D29Star & D30Star
		D29Star=Parity6[4];
		D30Star=Parity6[5];
		
		// ByteWord 1-4
		for (int i=0; i<4; i++)
		{
			ByteWord[i]=(int) Math.pow(2,6);
			for (int j=0; j<6; j++)
			{
				ByteWord[i]=ByteWord[i]+(int)( HeaderLine2[i*6+j]*Math.pow(2,j) );
			}
			RtcmMsgByte[1][i]=(char)(ByteWord[i]);
		}

		// ByteWord 5
		ByteWord[4]=(int) Math.pow(2,6);
		for (int i=0; i<6; i++)
			ByteWord[4]=ByteWord[4]+(int)( Parity6[i]*Math.pow(2,i) );
		RtcmMsgByte[1][4]=(char)(ByteWord[4]);

		
		//************************************************************************
		// Write the Content of Message Type 2  
		int NumberOfDataWords = GetRtcmNumOfWord(MsgType, NumberOfSatellite);
		
		int[][] ContentLine=new int[NumberOfDataWords][24];
		int[][] SatelliteData=new int[NumberOfSatellite][40];
		
		// Save each satellite data (40 bits) into SatelliteData
		for( int Sat=0; Sat<NumberOfSatellite; Sat++)
		{
			// Scale factor  1
			int[] BinaryScaleFactor = new int[1];
			dec2bin( ScaleFactor[Sat], BinaryScaleFactor, 1);
			for(int i=0; i<1;i++)
				SatelliteData[Sat][i]=BinaryScaleFactor[i];
			
			double SfPRC, SfRRC;
			if(ScaleFactor[Sat]==0)
			{
				SfPRC=0.02;
				SfRRC=0.002;
			}
			else
			{
				SfPRC=0.32;
				SfRRC=0.032;
			}
			
			// UDRE  2-3
			int[] BinaryUDRE = new int[2];
			dec2bin( UDRE[Sat], BinaryUDRE, 2);
			for(int i=0; i<2;i++)
				SatelliteData[Sat][i+1]=BinaryUDRE[i];
			
			// SatlliteID  4-8
			int[] BinarySatlliteID = new int[5];
			if(SatlliteID[Sat]!=32)
				dec2bin( SatlliteID[Sat], BinarySatlliteID, 5);
			for(int i=0; i<5;i++)
				SatelliteData[Sat][i+3]=BinarySatlliteID[i];
			
			// Delta PR Correction 9-24
			int[] BinaryDeltaPRCor = new int[16];
			dec2bin( (DeltaPRCor[Sat])/SfPRC, BinaryDeltaPRCor, 16);
			for(int i=0; i<16;i++)
				SatelliteData[Sat][i+8]=BinaryDeltaPRCor[i];
			
			// Delta PR Correction Rate  25-32
			int[] BinaryDeltaPRCorRate = new int[8];
			dec2bin( (DeltaPRCorRate[Sat])/SfRRC, BinaryDeltaPRCorRate, 8);
			for(int i=0; i<8;i++)
				SatelliteData[Sat][i+24]=BinaryDeltaPRCorRate[i];
			
			// IOD  33-40
			int[] BinaryIOD = new int[8];
			dec2bin( IOD[Sat], BinaryIOD, 8);
			for(int i=0; i<8;i++)
				SatelliteData[Sat][i+32]=BinaryIOD[i];

		}
		
		// Distribute SatelliteData into ContentLine
		int Count=0;
		for (int i=0; i<NumberOfSatellite; i++)
		{
			for (int j=0; j<40; j++)
			{
				ContentLine[Count/24][Count%24]=SatelliteData[i][j];
				Count=Count+1;
			}
		}
		// Fill( 1 & 0 )
		if(NumberOfSatellite%3 !=0)
			for(int i=Count%24; i<24; i++)
				// even i, return 1. Odd i, renturn 0.
				ContentLine[Count/24][i]=1-i%2;  
		
		
//			bw.write(Arrays.toString(ContentLine[i]));
		for (int i=0; i<NumberOfDataWords; i++)
		{
			//  *******  Parity  ********
			ParityRTCM( D29Star,  D30Star, ContentLine[i], Parity6);
			
			// XOR
			for (int j=0; j<24; j++)
			{
				if(D30Star==0)
					ContentLine[i][j]=ContentLine[i][j];
				else
					ContentLine[i][j]=1-ContentLine[i][j];
			}
			
			// Update D29Star & D30Star
			D29Star=Parity6[4];
			D30Star=Parity6[5];
			
			// ByteWord 1-4
			for (int k=0; k<4; k++)
			{
				ByteWord[k]=(int) Math.pow(2,6);
				for (int m=0; m<6; m++)
				{
					ByteWord[k]=ByteWord[k]+(int)( ContentLine[i][k*6+m]*Math.pow(2,m) );
				}
				RtcmMsgByte[i+2][k]=(char)(ByteWord[k]);
			}

			// ByteWord 5
			ByteWord[4]=(int) Math.pow(2,6);
			for (int m=0; m<6; m++)
				ByteWord[4]=ByteWord[4]+(int)( Parity6[m]*Math.pow(2,m) );
			RtcmMsgByte[i+2][4]=(char)(ByteWord[4]);

		}

	}
	
	
	/**
	 * CreateRtcm3 function 
	 * 
	 * The function performs the RTCM MT3 Creation.
	 * @param  MsgType, StationID, ModifiedZcount.            Inputs
	 * @param  SequenceNo,NumberOfSatellite, StationHealth.   Inputs
	 * 
	 * @param  ECEFX, ECEFY, ECEFZ.                           Inputs
	 * 
	 * @param  RtcmMsgByte // Whole Msg in Byte               Output
	 **/
	public final void CreateRtcm3(	
			int MsgType,int StationID, int ModifiedZcount,
			int SequenceNo,int NumberOfSatellite, int StationHealth,

			double ECEFX,double ECEFY,double ECEFZ,
			
			char[][] RtcmMsgByte) 
	{
		
		int[] ByteWord= new int[5];  // ByteWord[5] for each line
		int[] Parity6=new int[6];
		
		// Generate the Header Message
		int[] HeaderLine1=new int[24];
		int[] HeaderLine2=new int[24];
		
		CreateRtcmHeader( MsgType, StationID,  ModifiedZcount,
				SequenceNo, NumberOfSatellite,  StationHealth,
				
				HeaderLine1, HeaderLine2 );
		
//			**************** Header First line  ****************
					
		//  *******  Parity  ********
		ParityRTCM( D29Star,  D30Star, HeaderLine1, Parity6);
		
		// XOR
		for (int i=0; i<24; i++)
		{
			if(D30Star==0)
				HeaderLine1[i]=HeaderLine1[i];
			else
				HeaderLine1[i]=1-HeaderLine1[i];
		}
		
		// Update D29Star & D30Star
		D29Star=Parity6[4];
		D30Star=Parity6[5];
		
		// ByteWord 1-4
		for (int i=0; i<4; i++)
		{
			ByteWord[i]=(int) Math.pow(2,6);
			for (int j=0; j<6; j++)
			{
				ByteWord[i]=ByteWord[i]+(int)( HeaderLine1[i*6+j]*Math.pow(2,j) );
			}
			RtcmMsgByte[0][i]=(char)(ByteWord[i]);
		}

		// ByteWord 5
		ByteWord[4]=(int) Math.pow(2,6);
		for (int i=0; i<6; i++)
			ByteWord[4]=ByteWord[4]+(int)( Parity6[i]*Math.pow(2,i) );
		RtcmMsgByte[0][4]=(char)(ByteWord[4]);

					
// 			**************** Header Second line  ****************
		
//			bw.write(Arrays.toString(HeaderLine2));
//			bw.write(String.valueOf(HeaderLine1[i]));
		
		//  *******  Parity  ********
		ParityRTCM( D29Star,  D30Star, HeaderLine2, Parity6);
		
		// XOR
		for (int i=0; i<24; i++)
		{
			if(D30Star==0)
				HeaderLine2[i]=HeaderLine2[i];
			else
				HeaderLine2[i]=1-HeaderLine2[i];
		}
		
		// Update D29Star & D30Star
		D29Star=Parity6[4];
		D30Star=Parity6[5];
		
		// ByteWord 1-4
		for (int i=0; i<4; i++)
		{
			ByteWord[i]=(int) Math.pow(2,6);
			for (int j=0; j<6; j++)
			{
				ByteWord[i]=ByteWord[i]+(int)( HeaderLine2[i*6+j]*Math.pow(2,j) );
			}
			RtcmMsgByte[1][i]=(char)(ByteWord[i]);
		}

		// ByteWord 5
		ByteWord[4]=(int) Math.pow(2,6);
		for (int i=0; i<6; i++)
			ByteWord[4]=ByteWord[4]+(int)( Parity6[i]*Math.pow(2,i) );
		RtcmMsgByte[1][4]=(char)(ByteWord[4]);
		
		
		//************************************************************************
		// Write the Content of Message Type 3
		
		final double ScaleFactor=0.01;
		int NumberOfDataWords = 4;
		
		int[][] ContentLine=new int[NumberOfDataWords][24];
		int[] SatelliteData=new int[96];
		

		// ECEF X_Coordinates  1-32
		int[] BinaryECEFX = new int[32];
		dec2bin( ECEFX/ScaleFactor, BinaryECEFX, 32);
		for(int i=0; i<32;i++)
			SatelliteData[i]=BinaryECEFX[i];
		
		// ECEF Y_Coordinates  33-64
		int[] BinaryECEFY = new int[32];
		dec2bin( ECEFY/ScaleFactor, BinaryECEFY, 32);
		for(int i=0; i<32;i++)
			SatelliteData[i+32]=BinaryECEFY[i];
		
		// ECEF Z_Coordinates  65-96
		int[] BinaryECEFZ = new int[32];
		dec2bin( ECEFZ/ScaleFactor, BinaryECEFZ, 32);
		for(int i=0; i<32;i++)
			SatelliteData[i+64]=BinaryECEFZ[i];


		// Distribute SatelliteData into ContentLine
		int Count=0;
		for (int j=0; j<96; j++)
		{
			ContentLine[Count/24][Count%24]=SatelliteData[j];
			Count=Count+1;
		}


//			bw.write(Arrays.toString(ContentLine[i]));
		for (int i=0; i<NumberOfDataWords; i++)
		{
			//  *******  Parity  ********
			ParityRTCM( D29Star,  D30Star, ContentLine[i], Parity6);
			
			// XOR
			for (int j=0; j<24; j++)
			{
				if(D30Star==0)
					ContentLine[i][j]=ContentLine[i][j];
				else
					ContentLine[i][j]=1-ContentLine[i][j];
			}
			
			// Update D29Star & D30Star
			D29Star=Parity6[4];
			D30Star=Parity6[5];
			
			// ByteWord 1-4
			for (int k=0; k<4; k++)
			{
				ByteWord[k]=(int) Math.pow(2,6);
				for (int m=0; m<6; m++)
				{
					ByteWord[k]=ByteWord[k]+(int)( ContentLine[i][k*6+m]*Math.pow(2,m) );
				}
				RtcmMsgByte[i+2][k]=(char)(ByteWord[k]);
			}

			// ByteWord 5
			ByteWord[4]=(int) Math.pow(2,6);
			for (int m=0; m<6; m++)
				ByteWord[4]=ByteWord[4]+(int)( Parity6[m]*Math.pow(2,m) );
			RtcmMsgByte[i+2][4]=(char)(ByteWord[4]);
		}

	}
	
	
	
//	@Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.nmeartcm);
//	
//        RtcmHandler MTCreator=new RtcmHandler();
//	    // call the parameters needed
//
//		//  MT1
//		int MsgType=1; //  1-64
//		int StationID=1000; // 0-1023
//		double ModifiedZcount=2772;  // Raw / 0.6
//		int SequenceNo=1; // 0-7
//		int NumberOfSatellite=5;
//		int StationHealth=0; // 0-7
//		
//		int[] ScaleFactor={0,0,0,0,0};
//		int[] UDRE={1,1,1,1,1};
//		int[] SatlliteID={15,32,29,14,25};
//		double[] PRCor={-16.44,12,12.34,8.28,-13.46};
//		double[] PRCorRate={0.042,0.052,-0.012,-0.006,-0.08};
//		int[] IOD={59,212,156,138,49};
//		
//		int N = MTCreator.GetRtcmNumOfWord(MsgType, NumberOfSatellite);
//		char[][] RtcmMsgByte1=new char[2+N][5];
//
//		MTCreator.CreateRtcm1(MsgType,StationID,(int)ModifiedZcount,SequenceNo,NumberOfSatellite, StationHealth,
//				ScaleFactor,UDRE,SatlliteID,PRCor,PRCorRate,IOD, RtcmMsgByte1);
//		SequenceNo=SequenceNo+1;
//
//		
//		
//		
//		//  MT2
//		MsgType=2;
//		StationID=1010;
//		ModifiedZcount=2778;
//		NumberOfSatellite=5;
//		StationHealth=0; // 0-7
//		
//		double[] DeltaPRCor={-16.44,12,12.34,8.28,-13.46};
//		double[] DeltaPRCorRate={0.042,0.052,-0.012,-0.006,-0.08};
//		
//		N = MTCreator.GetRtcmNumOfWord(MsgType, NumberOfSatellite);
//		char[][] RtcmMsgByte2=new char[2+N][5];
//
//		MTCreator.CreateRtcm2(MsgType,StationID,(int)ModifiedZcount,SequenceNo,NumberOfSatellite, StationHealth,
//				ScaleFactor,UDRE,SatlliteID,DeltaPRCor,DeltaPRCorRate,IOD, RtcmMsgByte2);
//		SequenceNo=SequenceNo+1;
//
//		
//		//  MT3
//		MsgType=3;
//		StationID=1005;
//		ModifiedZcount=2781;
//		NumberOfSatellite=1;
//		StationHealth=0; // 0-7
//		
//		double ECEFX=200000;
//		double ECEFY=100000;
//		double ECEFZ=78137;
//		
//		N = MTCreator.GetRtcmNumOfWord(MsgType, NumberOfSatellite);
//		char[][] RtcmMsgByte3=new char[2+N][5];
//		
//		MTCreator.CreateRtcm3(MsgType,StationID,(int)ModifiedZcount,SequenceNo,NumberOfSatellite, StationHealth,
//				ECEFX,ECEFY,ECEFZ, RtcmMsgByte3);
//		SequenceNo=SequenceNo+1;
//        
//
//	}
//	
//	

}

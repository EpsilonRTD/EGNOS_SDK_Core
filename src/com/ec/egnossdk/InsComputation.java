/**
 * @file InsComputation.java
 *
 * Computes INS position.
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


public class InsComputation {
	
	public String TAG_INS = "taginertial";
	double LatiOld = 0.0;
	double LongiOld = 0.0;
	double AltiOld = 0.0;
	double TimeOld = 0.0;
	
	double LatiOlder = 0.0;
	double LongiOlder = 0.0;
	double AltiOlder = 0.0;
	double TimeOlder = 0.0;
	
	double VNOld = 0;
	double VEOld = 0;
	double VUOld = 0;
	double NOld = 0;
	double EOld = 0;
	double UOld = 0;
	double DirectionOld = 0;
	int INSbegin = 0;
	
	float[] geomag = new float[3];
	float last_geomag[] = null;
	double acc_total =0.0;
	double ACC_THRESHOLD =0.09;
	boolean static_case = true;
	
    //  Kalman Filter is performed is a local system[N,E,U]
    //  with the following original point
	double pi =Math.PI;
	double deg2rad =Math.PI/180.0;
	double rad2deg =180.0/Math.PI;
	double Lati0=0.0;
	double Longi0=0.0;
	double Alti0=0.0;
	double aWGS84=6378137;
	double f=1/298.257222101;
	double e2WGS84=2*f-f*f;

	private boolean checkInitialPos = true;
	private boolean computeInsPos = false;
	private boolean wasStatic = false;
	private boolean becomesDynamic = false;
	private int posCnt = 0;

	public void InsHandler() {
		
	  double[] PositionBLH = new double[3];
	  double[] insReadings = GlobalState.getInsReadings();
	  double[] tmpPosition = GlobalState.getPosition();
	  PositionBLH[0] = tmpPosition[7];
	  PositionBLH[1] = tmpPosition[8];
	  PositionBLH[2] = tmpPosition[9];
	  
	  if(checkInitialPos) {
		  //Check if any SDK position is available
		  if(tmpPosition[7] != 0.0 || tmpPosition[4] != 0.0 || tmpPosition[1] != 0.0) {
			  checkInitialPos = false;
			  computeInsPos = true;
			  if(GlobalState.getisEgnosPosition() == 1) {
			    Lati0=tmpPosition[4];
			    Longi0=tmpPosition[5];
			    Alti0=tmpPosition[6];
			  }else {
				  Lati0=tmpPosition[0];
		          Longi0=tmpPosition[1];
		          Alti0=tmpPosition[2];
			  }
		  } else
			  checkInitialPos = true;
	  }

	  if(computeInsPos) {
		
		if(uBlox.insSdkPositionAvailable != 0) // SImply use position from Satellite
		{
			PositionBLH = ComputeSatelliteBasedPosition();
			if(insReadings[0] == 1) { //static
				double[] currPosition = GlobalState.getPosition();
				double[] newPosition = GlobalState.getPosition();
				posCnt++;
				newPosition[7] = 1/posCnt * PositionBLH[0] + (posCnt-1)/posCnt*currPosition[7];
				newPosition[8] = 1/posCnt * PositionBLH[1] + (posCnt-1)/posCnt*currPosition[8];
				newPosition[9] = 1/posCnt * PositionBLH[2] + (posCnt-1)/posCnt*currPosition[9];
				GlobalState.setPosition(newPosition);  
			  }
			  else { //dynamic
				 posCnt = 0; 
				 //no Kalman filtering in case an EGNOS-SDK position is available
				 //because the INs would worsen the position
			  }
		}
		else // Use INS Position
		{
			//Check if static or dynamic
				
			if( INSbegin == 0) // Initial the first epoch since GPS missing
			{
				double[] NEUOlder    = BLH2NEU ( Lati0*deg2rad, Longi0*deg2rad, Alti0, 
						LatiOlder*deg2rad, LongiOlder*deg2rad, AltiOlder, aWGS84,e2WGS84);
				
				double[] NEUOld = BLH2NEU ( Lati0*deg2rad, Longi0*deg2rad, Alti0, 
						LatiOld*deg2rad, LongiOld*deg2rad, AltiOld, aWGS84,e2WGS84);
				
				double VN =( NEUOld[0] - NEUOlder[0] ) / (TimeOld - TimeOlder);
				double VE =( NEUOld[1] - NEUOlder[1] ) / (TimeOld - TimeOlder);
				double VU =( NEUOld[2] - NEUOlder[2] ) / (TimeOld - TimeOlder);
				
				// Direction is count by Clock direction From North [N, E, U system]
				double Direction =Math.atan2( NEUOld[1] - NEUOlder[1] , NEUOld[0] - NEUOlder[0] );
				if(Direction<0)
					Direction = Direction + pi*2;
				
				double N = NEUOld[0] + VN*(TimeOld - TimeOlder);
				double E = NEUOld[1] + VE*(TimeOld - TimeOlder);
				double U = NEUOld[2] + VU*(TimeOld - TimeOlder);
				
				// Compute L, B, H
				PositionBLH = NEU2BLH(Lati0*deg2rad,  Longi0*deg2rad,  Alti0*deg2rad, 
						               N,  E,  U, aWGS84, e2WGS84);
				PositionBLH[0] = PositionBLH[0]*rad2deg;
				PositionBLH[1] = PositionBLH[1]*rad2deg;

				//  Update
				INSbegin = 1;
				TimeOld =  System.currentTimeMillis();
				NOld = N;
				EOld = E;
				UOld = U;
				VNOld = VN;
				VEOld = VE;
				VUOld = VU;
				DirectionOld = Direction;
				
			} // end of INS 1st epoch 
			else {
				//Experimental - Sensor data not consistent.
				if(insReadings[0] == 1) { //static,        
					
					// once this is true, the INS does not update position anymore, 
					// until get satellite position again
					wasStatic = true;
					//no position update
					  
				  }
				  else { //dynamic with constant known velocity
					 becomesDynamic = true;
					 if(!wasStatic) {
					 PositionBLH = ComputeInsBasedPosition(PositionBLH, 
							 insReadings[1], insReadings[2], insReadings[3], 
							 insReadings[4], insReadings[5], insReadings[6]);
					 
						double[] tmpInsPosition = GlobalState.getPosition();
						tmpInsPosition[7] = PositionBLH[0];
						tmpInsPosition[8] = PositionBLH[1];
						//INS works in 2 dimensions, no change to the altitude
						//tmpInsPosition[9] = PositionBLH[2];
						GlobalState.setPosition(tmpInsPosition);
					 //no Kalman filtering in case an EGNOS-SDK position is available
					 //because the INs would worsen the position
					 }
				  }
				//no static to dynamic situation supported
				if(wasStatic && becomesDynamic) {
					// Provide message to user saying this case is not supported until
					// a satellite position is available again.
				}
			}

		}// end of INS
	  }//INS requires an initial SDK position
	 
	}
	
	Matrix MatrixDo = new Matrix();
	/**
	 * kalmanPhone function
	 * 
	 * When GPS receiver is ON,  Use the Position
	 * When GPS receiver is OFF, Compute Position of next epoch based on INS
	 * 
	 * input parameter in Radian Unit, not degree
	 * Return the Position in either case.
	 * 
	 **/
	private double[] ComputeSatelliteBasedPosition()
	{
		double[] currentBLH = new double[3];

		double[] position = GlobalState.getPosition();		
		double Time = GlobalState.getGPSTOW(); 
		
		// Update the previous 2 epoch
		TimeOlder = TimeOld;
		LatiOlder  = LatiOld;
		LongiOlder = LongiOld;
		AltiOlder  = AltiOld;
		double Lati = 0.0;
		double Longi = 0.0;
		double Alti = 0.0;
		
		if (position[7]!= 0.0) {
			Lati = position[7];
			Longi = position[8];
			Alti = position[9];
		} else if(GlobalState.getisEgnosPosition() == 1 && position[4]!= 0.0) {
			// Use EGNOS Position
			Lati = position[4];
			Longi = position[5];
			Alti = position[6];
		  } else if(position[4]!= 0.0) { //orange pos
			  Lati = position[4];
				Longi = position[5];
				Alti = position[6];
		  } else if(position[0]!= 0.0) { //gps
			  Lati = position[0];
				Longi = position[1];
				Alti = position[2];
		  } else { //no position - should never be reached
			  Lati = 0.0;
			Longi = 0.0;
			Alti = 0.0;
		  }
		
		currentBLH[0] = Lati;
		currentBLH[1] = Longi;
		currentBLH[2] = Alti;
		
		// Update the previous 1 epoch
		TimeOld = Time;
		LatiOld  = Lati;
		LongiOld = Longi;
		AltiOld  = Alti;		
		
		return currentBLH;
	}
	

	private double[] ComputeInsBasedPosition(double[] PositionBLH
			, double accelex, double acceley, double accelez
			, double axisX, double axisY, double axisZ) {
		
		// Get Gyroscope
		double AngleVelocity = axisZ;
		
		double Time =  System.currentTimeMillis();
		
		float DeltaTime = (float)((Time - TimeOld)/1000.0);  // Time difference between 2 epochs in INS
		
		if(DeltaTime!= 0.0) {
			double DeltaDis = Math.sqrt(VNOld*VNOld + VEOld*VEOld)*DeltaTime;
			// velocity change  			
			double Direction = DirectionOld + (-1)*AngleVelocity*DeltaTime;
		
			double DeltaN = DeltaDis*Math.cos(Direction);
			double DeltaE = DeltaDis*Math.sin(Direction);
			
			double N = NOld + DeltaN;
			double E = EOld + DeltaE;
			double U = UOld;
			
			double VN = DeltaN/DeltaTime;
			double VE = DeltaE/DeltaTime;
			double VU = VUOld;
			
			// Compute L, B, H
			PositionBLH = NEU2BLH(Lati0*deg2rad,  Longi0*deg2rad,  Alti0*deg2rad, 
					               N,  E,  U, aWGS84, e2WGS84);
			PositionBLH[0] = PositionBLH[0]*rad2deg;
			PositionBLH[1] = PositionBLH[1]*rad2deg;
		
			//  Update
			TimeOld =  Time;
			NOld = N;
			EOld = E;
			UOld = U;
			VNOld = VN;
			VEOld = VE;
			VUOld = VU;
			DirectionOld = Direction;
		}
		
		return PositionBLH;
	}
	
	
	/**
	 * BLH2XYZ function
	 * 
	 * Coordinates transformation from [latitude, longitude, altitude] into WGS-84 coordinates
	 * 
	 * input parameter in Radian Unit, not degree
	 * Return the WGS-84 coordinates in XYZ[3]
	 * 
	 **/
	private double[] BLH2XYZ (double Lati, double Longi, double Alti, double aWGS84,double e2WGS84)
	{
		double[] XYZ = new double[3];
		
		double N=aWGS84/Math.sqrt(1-e2WGS84*Math.sin(Lati)*Math.sin(Lati));

		XYZ[0]=(N+Alti)*Math.cos(Lati)*Math.cos(Longi);
		XYZ[1]=(N+Alti)*Math.cos(Lati)*Math.sin(Longi);
		XYZ[2]=(N*(1-e2WGS84)+Alti)*Math.sin(Lati);
		
		return XYZ;
	}	
	
	
	/**
	 * BLH2NEU function
	 * 
	 * Coordinates transformation from WGS-84 into local [North, East, Up] coordinates
	 * 
	 * input parameter in Radian Unit, not degree
	 * Return the WGS-84 coordinates in NEU[3]
	 * 
	 **/
	private double[] BLH2NEU ( double Lati0, double Longi0, double Alti0, 
							   double Lati, double Longi, double Alti, 
							   double aWGS84,double e2WGS84)
	{		
		double[] XYZ0 = BLH2XYZ( Lati0,  Longi0,  Alti0, aWGS84, e2WGS84);
		double[] XYZ  = BLH2XYZ( Lati ,  Longi ,  Alti , aWGS84, e2WGS84);
		
		double[] XYZ_dif = MatrixDo.subtract_vec(XYZ, XYZ0);
		double[] R3XYZ_dif = MatrixDo.multiply_matxvec( MatrixDo.R3(Longi0), XYZ_dif);
		double[] R2R3XYZ_dif = MatrixDo.multiply_matxvec( MatrixDo.R2(Math.PI*0.5-Lati0), R3XYZ_dif);
		
		double[] NEU = {-R2R3XYZ_dif[0], R2R3XYZ_dif[1], R2R3XYZ_dif[2]  };
		
		return NEU;
	}
	
	
	/**
	 * NEU2BLH function
	 * 
	 * Coordinates transformation from local [North, East, Up] coordinates into WGS-84. 
	 * 
	 * input parameter in Radian Unit, not degree
	 * Return the [North, East, Up] coordinates
	 * 
	 **/
	private double[] NEU2BLH ( double Lati0, double Longi0, double Alti0, 
							   double N, double E, double U, 
							   double aWGS84,double e2WGS84)
	{		
		double[] NEU ={-N, E, U};
		
		double[][] R2R3 = MatrixDo.multiply(MatrixDo.R2(Math.PI/2-Lati0), MatrixDo.R3(Longi0));
		double[][] invR2R3 = MatrixDo.inv_33(R2R3);
		double[] Delta_XYZ = MatrixDo.multiply_matxvec(invR2R3, NEU);
		
		double[] XYZo = BLH2XYZ( Lati0,Longi0,Alti0,aWGS84,e2WGS84);

		double X = XYZo[0] + Delta_XYZ[0];
		double Y = XYZo[1] + Delta_XYZ[1];
		double Z = XYZo[2] + Delta_XYZ[2];

		double[] BLH = XYZ2BLH( X,Y,Z,aWGS84,e2WGS84);
		
		return BLH;
	}
	
	
	
	/**
	 * XYZ2BLH function
	 * 
	 * Coordinates transformation from WGS-84 coordinates into [latitude, longitude, altitude]
	 * 
	 * Return the WGS-84 coordinates in [Lati, Longi, Alti],  in Radian Unit, not degree
	 * 
	 **/
	private double[] XYZ2BLH (double X, double Y, double Z, double aWGS84,double e2WGS84)
	{
		double[] BLH = new double[3];
		
		//  Longitude is fixed
		double L = Math.atan2(Y,X);

		// Latitude and Altitude should be fixed by iteration
		// initial values
		double B=Math.atan(Z/Math.sqrt(X*X+Y*Y));
		double N=aWGS84/Math.sqrt(1-e2WGS84*(Math.sin(B))*(Math.sin(B)));
		double H=Z/Math.sin(B)-N*(1-e2WGS84);
		
		double dB=1;
		while(dB>Math.PI/180/60)   // accuruay is 1 minute, das ist iteration process
		{
			double B0 = B;

		    B=Math.atan(Z*(N+H)/Math.sqrt(X*X+Y*Y)/(H+N*(1-e2WGS84)));
		    N=aWGS84/Math.sqrt(1-e2WGS84*(Math.sin(B))*(Math.sin(B)));
		    H=Z/Math.sin(B)-N*(1-e2WGS84);
		    
		    dB=Math.abs(B-B0);
		}
		
		BLH[0] = B;
		BLH[1] = L;
		BLH[2] = H;
		
		return BLH;
	}		
	


}

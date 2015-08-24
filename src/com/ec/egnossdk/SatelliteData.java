/**
 * @file SatelliteData.java
 *
 * Gets the satellite position from the satellite data obtained from NORAD.
 * Calculates the positions for 24 hoursrs for every 10 minutes.
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

import java.util.Enumeration;
import java.util.Vector;

import android.util.Log;

import com.ec.sdp4model.SDP4Model;


/**
 * Class that gets the satellite position from the satellite data obtained from NORAD.
 * Calculates the positions for 24 hoursrs for every 10 minutes.
 */
public class SatelliteData {

	public static boolean hasDataFromNORAD = false;
	public static boolean hasAllSatelliteDetails = false;

	protected static double[] itsR = new double[3];
	protected static double[] itsV = new double[3];

	private static final String TAG_SATELLITEDATA = "satellite_data";

	public static double JD;
	public static double SEC_PER_DAY = 86400;
	public static double NUM_GPS_WEEKS = 1024; // Number of GPS weeks before a
	// week rollover
	public static double JD_GPS = 2444244.5; // Julian day at January 6, 1980, HMS
	// = 00:00:00
	public static double JD_Android = 2440587.5; // Julian day at January 1, 1970,
	// HMS = 00:00:00

	public static int satelliteCount = 0;

	// static double[][] sat_pos_array = new double[1451][3]; //change 145
	static double original_time = 0;
	// static double[] jd_array = new double[1441];  //change 145

	static int times = 1;
	static LogFiles logfiles ;
	static Vector<SatellitePositions> satPositionVector = new Vector<SatellitePositions>();

	/**
	 * getNORADData function
	 * 
	 * Get data from NORAD.
	 * 
	 * @param	satelliteDataFromNORAD	 the NORAD data
	 * @return   error     0 if no error occurred
	 */
	public static int getNORADData(String[][] satelliteDataFromNORAD) {
		satelliteDataFromNORAD = new String[50][24];
		int error = 0; 
		hasDataFromNORAD = false;

		error = NORAD.requestNORADData();
		satelliteDataFromNORAD = GlobalState.getNORADData();
		hasDataFromNORAD = true;
		return error;
	}

	/**
	 * getSatelliteDetails function
	 * 
	 * Obtain satellite positions
	 * 
	 * @param	satelliteDataFromNORAD	the NORAD data
	 * @param	gpsCoordinates          the user position
	 * @param	gpsTOW                  the gps time of week
	 * @param	gpsWeekNum              the gps week number
	 * @param	fromReceiver            true if from BT receiver, otherwise false
	 * @param	onStart                 true if onStart of feature, otherwise false
	 * @return satelliteDetails       the satellite position as an array.
	 */
	public static double[][] getSatelliteDetails(
			String[][] satelliteDataFromNORAD, double[] gpsCoordinates,
			double gpsTOW, double gpsWeekNum, boolean fromReceiver, boolean onStart) {
		satelliteCount = 0;
		hasAllSatelliteDetails = false;
		logfiles = new LogFiles();

		double[][] satelliteDetails;
		double[][] direction_sat = new double[NORAD.countSatellites][292];
		double[] direction = new double[291]; 
		double[][] data = convertNORAD(satelliteDataFromNORAD);
		double[] data_NORAD = new double[11];

		long currentTime = System.currentTimeMillis();
		Log.d(TAG_SATELLITEDATA, "Current Time: " + currentTime);

		Enumeration<SatellitePositions> e = satPositionVector.elements();

		for (int k = 0; k < NORAD.countSatellites; k++) {
			if (satelliteDataFromNORAD[k][0] != null
					&& satelliteDataFromNORAD[k][2] != null) {

				if(e.hasMoreElements() == false) {          
					SatellitePositions sat = new SatellitePositions();
					sat.prn = data[k][0];
					for(int i = 0 ; i < 145; i++)
						sat.jd_array[i] = 0;

					for(int i = 0 ; i < 145; i++) {
						sat.sat_pos_array[i][0] = 0;
						sat.sat_pos_array[i][1] = 0;
					}    
					addSatPos(sat);
				}

				SatellitePositions satPos = e.nextElement();

				if( k == NORAD.countSatellites)
					times = 0;

				// data_NORAD holds the TLE information obtained from NORAD for
				// one satellite
				data_NORAD = data[k];
				// Orientation returns an array which holds:
				// PRN ( azm elv for TOW-12h) ... ( azm elv for TOW) ... ( azm
				// elv for TOW+12h) computed every 10 minutes
				direction = Orientation(data_NORAD, gpsTOW, gpsWeekNum, gpsCoordinates,
						fromReceiver, onStart,satPos);
				direction_sat[k][0] = direction[0];// PRN of satellite
				// type of satellite 0-gps, 2- EGNOS etc.
				direction_sat[k][1] = Double.valueOf(satelliteDataFromNORAD[k][1]);

				System.arraycopy(direction, 1, direction_sat[k], 2, 290); // 290

				satelliteCount++;
			}
		}
		hasAllSatelliteDetails = true;

		long diffTime = System.currentTimeMillis() - currentTime;
		Log.d(TAG_SATELLITEDATA, "diff Time: " + diffTime);

		satelliteDetails = new double[satelliteCount][292]; // change 292
		System.arraycopy(direction_sat, 0, satelliteDetails, 0, satelliteCount);
		GlobalState.setSatelliteDetails(satelliteDetails);

		return satelliteDetails;
	}

	/**
	 * Orientation function
	 * 
	 * Computes azimuth and elevation for all satellites obtainded from NORAD 
	 *   
	 * @param	data_NORAD	             the NORAD data
	 * @param	gpsTOW                   the time of week
	 * @param	gpsWN                    the gps week number
	 * @param	usr_pos_geo              the user position
	 * @param	bt_receiver              true if from BT receiver, otherwise false
	 * @param	onStart                  true if onStart of feature, otherwise false
	 * @param satPos                   object of the Satellite Position
   * @return direction               the satellite position as an array.
	 */
	public static double[] Orientation(double[] data_NORAD, double gpsTOW,
			double gpsWN, double[] usr_pos_geo, boolean bt_receiver, boolean onStart ,SatellitePositions satPos) {
		double JD_cur = 0;

		if (bt_receiver == true) {

			// Compute the number of days since the start of GPS
			double gpsDays = gpsWN * 7 + gpsTOW / SEC_PER_DAY + 0.5;

			// Compute the current Julian Day
			JD_cur = JD_GPS + gpsDays - 0.5;
		} else {
			double time = gpsTOW / 86400000;
			JD_cur = JD_Android + time;
		}

		JD_cur = JD_cur - 2450000; // Julian Day at current GPS Time

		SDP4Model sdp4 = new SDP4Model();
		double direction[] = new double[291];    //change 291
		double direction_vec[] = new double[3];

		direction[0] = data_NORAD[0];

		sdp4.Init();
		int k = 0;

		JD = JD_cur;


		for (int i = -720; i < 721; i += 10) { 
			// TO DO: if the last 3 positions are removed, the first and the
			// last position are the same
			double t_diff;
			t_diff = (double) i / 1440.0;
			JD = JD_cur + t_diff;

			itsR = sdp4.RunSDP4(data_NORAD, JD);

			SDP4Model.SetGeodetic(usr_pos_geo);
			SDP4Model.GetHori(direction_vec);

			direction[1 + k * 2] = direction_vec[0] * 180 / Math.PI;
			direction[2 + k * 2] = direction_vec[1] * 180 / Math.PI;
			k++;
		}
		return direction;
	}

	/**
	 * addSatPos function
	 * 
	 * @param	satPos	
	 */
	public static void addSatPos(SatellitePositions satPos) {
		satPositionVector.add(satPos);
	}

  /**
   * convertNORAD function
   * 
   * Converts data obtained from NORAD to double.
   * @param satelliteDataFromNORAD        data from NORAD
   * @return satNORADData                 converted data from NORAD
   */
	public static double[][] convertNORAD(String[][] satelliteDataFromNORAD) {
		double[][] satNORADData = new double[NORAD.countSatellites][11];
		double last, first;

		for (int i = 0; i < NORAD.countSatellites; i++) {
			if (satelliteDataFromNORAD[i] != null
					&& satelliteDataFromNORAD[i][2] != null) {

				satNORADData[i][0] = Double.parseDouble(satelliteDataFromNORAD[i][2]);// PRN
				satNORADData[i][1] = Double.parseDouble(satelliteDataFromNORAD[i][10]);// First Time Derivative of the Mean Motion

				last = Double.parseDouble(satelliteDataFromNORAD[i][11].substring(
						satelliteDataFromNORAD[i][11].length() - 2,
						satelliteDataFromNORAD[i][11].length()));
				first = Double.parseDouble(satelliteDataFromNORAD[i][11].substring(0,
						satelliteDataFromNORAD[i][11].length() - 2));
				satNORADData[i][2] = Math.pow(10, last) * first;// Second Time Derivative of Mean Motion (decimal point assumed)

				last = Double.parseDouble(satelliteDataFromNORAD[i][12].substring(
						satelliteDataFromNORAD[i][11].length() - 2,
						satelliteDataFromNORAD[i][11].length()));
				first = Double.parseDouble(satelliteDataFromNORAD[i][12].substring(0,
						satelliteDataFromNORAD[i][11].length() - 2));
				satNORADData[i][3] = Math.pow(10, last) * first;// BSTAR drag term (decimal point assumed)

				satNORADData[i][4] = Double.parseDouble(satelliteDataFromNORAD[i][16]);// Inclination (Degrees)
				satNORADData[i][5] = Double.parseDouble(satelliteDataFromNORAD[i][17]);// Right Ascension of the Ascending Node [Degrees]
				satNORADData[i][6] = Double.parseDouble(satelliteDataFromNORAD[i][18]);// Eccentricity (decimal pointassumed)
				satNORADData[i][7] = Double.parseDouble(satelliteDataFromNORAD[i][19]);// Argument ofPerigee[Degrees]
				satNORADData[i][8] = Double.parseDouble(satelliteDataFromNORAD[i][20]);// Mean Anomaly[Degrees]
				satNORADData[i][9] = Double.parseDouble(satelliteDataFromNORAD[i][21]);// Mean Motion [Revs per day]
				satNORADData[i][10] = Double.parseDouble(satelliteDataFromNORAD[i][9]);// Epoch (Year, Day of the year and fractional portion of the day)
			}
		}

		return satNORADData;
	}

}

/**
 * @file NORAD.java
 *
 * Gets the GPS and SBAS satellite information from NORAD
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.util.Log;

/**
 * Class that gets the GPS and SBAS satellite information from NORAD
 */
public class NORAD {

	private static final String TAG_NORAD = "Norad_data";	
	private static String GPS_SATELLITE = "GPS";
	private static String GALILEO_SATELLITE = "GALILEO";
	private static String SBAS_SATELLITE = "SBAS";

	private static String NORAD_GPS_OPERATIONAL_URL = "http://www.celestrak.com/NORAD/elements/gps-ops.txt";
	//private static String NORAD_GALILEO_URL = "http://www.celestrak.com/NORAD/elements/galileo.txt";
	private static String NORAD_SBAS_URL = "http://www.celestrak.com/NORAD/elements/sbas.txt";

	public static int countSatellites = 0;

	/**
	 * requestNORADData function
	 * 
	 * Requests GPS and SBAS satellite details from NORAD
	 * @return   error     0 if no error occurred
	 */
	public static int requestNORADData( ){
		String[][] dataFromNORAD = new String[50][24];
		int error = 0;
		countSatellites = 0;

		error =	requestDatafromNORAD(NORAD_GPS_OPERATIONAL_URL, GPS_SATELLITE,dataFromNORAD);

		//	requestDatafromNORAD(NORAD_GALILEO_URL, GALILEO_SATELLITE,dataFromNORAD);
		if(error == 0)
			error = requestDatafromNORAD(NORAD_SBAS_URL, SBAS_SATELLITE,dataFromNORAD);

		GlobalState.setNORADData(dataFromNORAD);
		return error;
	}

	/**
	 * requestDatafromNORAD function
	 * 
	 * @param	 urlString	      the url to get NORAD data from.
	 * @param  satType          the type of the satellite GPS or SBAS
	 * @param  dataFromNORAD    The requested data from NORAD
	 * @return   0 if no error occurred, otherwise -1, -2 or -3
	 */
	private static int requestDatafromNORAD(String urlString, String satType,
			String[][] dataFromNORAD) {
		HttpURLConnection urlConnection = null;
		URL url = null;
		BufferedReader bufferedInputReader = null;
		StringBuilder requestedData = new StringBuilder("");
		try {
			url = new URL(urlString);		

			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setDoOutput(false);
			urlConnection.setDoInput(true);
			urlConnection.connect();

			bufferedInputReader = new BufferedReader(new InputStreamReader(
					urlConnection.getInputStream()));
			String urlData = "";
			while ((urlData = bufferedInputReader.readLine()) != null) {
				requestedData.append(urlData);
			}
			urlConnection.disconnect();
			if(parseNORADData(requestedData.toString(), satType,dataFromNORAD) == -3)
				return -3;
		} catch (MalformedURLException e) {
			Log.e(TAG_NORAD,
					"SKYPLOT | Unable to create URL from the given string: "
							+ urlString.toString());
			return -1;
		} catch (IOException e) {
			Log.e(TAG_NORAD, "SKYPLOT |IO error occurred: " + e);
			return -2;
		}catch(Exception e) {
			Log.e(TAG_NORAD, "SKYPLOT |Error occurred: " + e);
			return -3;
		}		
		return 0;		
	}

	/**
	 * parseNORADData function
	 * 
	 * Parses the NORAD data.
	 * 
   * @param  requestedData    the requested NORAD data 
   * @param  satType          the type of the satellite GPS or SBAS
   * @param  dataFromNORAD    The converted data from NORAD
	 * @return  0 if no error occurred, otherwise -1, -2 or -3
	 */
	private static int parseNORADData(String requestedData, String satType,
			String[][] dataFromNORAD) {

		String line0 = null;
		String line1 = null;
		String line2 = null;
		int next = 0;
		String satelliteType = "0";

		String oldSatType = satType;

		String gpsSplitString = "PRN";
		String galileoSplitString ;
		String sbasSplitString = "\\/PRN";

		if (requestedData != null) {
			Log.d(TAG_NORAD, "Requested Data from NORAD: " + requestedData);

			while (next < requestedData.length()) {
				try{
					//Line 0
					line0 = requestedData.substring(next, 23 + next);

					String[] prnValues = new String[2];
					if (satType.contains(GPS_SATELLITE) == true)
						prnValues = line0.split(gpsSplitString);
					else if (satType.contains(GALILEO_SATELLITE) == true){
						if (line0.contains("("))
							galileoSplitString = "\\(";
						else 
							galileoSplitString = " ";
						prnValues = line0.split(galileoSplitString);
					}
					else if (satType.contains(SBAS_SATELLITE) == true) {
						prnValues = line0.split(sbasSplitString);
						satType = prnValues[0].split("\\(")[1];
						prnValues[0] = prnValues[0].replace(satType, "");
					}

					if (prnValues[0].contains("(")) {
						prnValues[0] = prnValues[0].replace("(", "");
						prnValues[1] = prnValues[1].replace(")", "");
					}
					dataFromNORAD[countSatellites][0] = prnValues[0]; // Satellite Name
					if(satType.contains("")|| satType.contains(null))
						satType = oldSatType;				
					satelliteType = getsatType(satType);	

					dataFromNORAD[countSatellites][1] = satelliteType; // Type of Satellite
					if (prnValues.length > 1){
						if (satType.contains(GALILEO_SATELLITE) == true){
							prnValues[1]= prnValues[1].replace(
									")", "");
							dataFromNORAD[countSatellites][2] = prnValues[1].substring(prnValues[1].length()-3, prnValues[1].length());
						}
						else
							dataFromNORAD[countSatellites][2] = prnValues[1].replace(
									" ", ""); // PRN of Satellite							
					}

					//Line 1
					line1 = requestedData.substring(24 + next, 93 + next);
					dataFromNORAD[countSatellites][3] = line1.substring(3-1, 7);//Satellite Number
					dataFromNORAD[countSatellites][4] = String.valueOf(line1.charAt(8-1));//Classification
					dataFromNORAD[countSatellites][5] = line1.substring(10-1, 11);//International Designator (Last two digits of launch year)
					dataFromNORAD[countSatellites][6] = line1.substring(12-1, 14);//International Designator (Launch number of the year)
					dataFromNORAD[countSatellites][7] = line1.substring(15-1, 17);//International Designator (Piece of the launch)
					dataFromNORAD[countSatellites][8] = line1.substring(19-1, 20);//Epoch Year (Last two digits of year)
					dataFromNORAD[countSatellites][9] = line1.substring(19-1, 32);//Epoch Year (Last two digits of year) and
					//(Day of the year and fractional portion of the day)
					dataFromNORAD[countSatellites][10] = line1.substring(34-1, 43);//First Time Derivative of the Mean Motion
					dataFromNORAD[countSatellites][11] = line1.substring(45-1, 52);//Second Time Derivative of Mean Motion (decimal point assumed)
					dataFromNORAD[countSatellites][12] = line1.substring(54-1, 61);//BSTAR drag term (decimal point assumed)
					dataFromNORAD[countSatellites][13] = String.valueOf(line1.charAt(63-1));//Ephemeris type
					dataFromNORAD[countSatellites][14] = line1.substring(65-1, 68);//Element number
					dataFromNORAD[countSatellites][15] = String.valueOf(line1.charAt(69-1));//Checksum (Modulo 10)		

					//Line 2
					line2 = requestedData.substring(93 + next, 162 + next);		
					dataFromNORAD[countSatellites][16] = line2.substring(9-1, 16);//Inclination (Degrees)
					dataFromNORAD[countSatellites][17] = line2.substring(18-1, 25);//Right Ascension of the Ascending Node [Degrees]				
					dataFromNORAD[countSatellites][18] = line2.substring(27-1, 33);//Eccentricity (decimal point assumed)				
					dataFromNORAD[countSatellites][19] = line2.substring(35-1, 42);//Argument of Perigee [Degrees]
					dataFromNORAD[countSatellites][20] = line2.substring(44-1, 51);//Mean Anomaly [Degrees]
					dataFromNORAD[countSatellites][21] = line2.substring(53-1, 63);//Mean Motion [Revs per day]
					dataFromNORAD[countSatellites][22] = line2.substring(64-1, 68);//Revolution number at epoch [Revs]
					dataFromNORAD[countSatellites][23] = String.valueOf(line2.charAt(69-1));//Checksum (Modulo 10)	

					next = next + 162;
					countSatellites++;
					satType = oldSatType;
				}catch (Exception e) {
					Log.e(TAG_NORAD, "Error while parsing "+satType+" NORAD data: "+e);
					return -3;
				}
			}
		}
		return 0;
	}
	
	/**
	 * getsatType function
	 * 
	 * Gets the satellite type
	 * 
	 * @param	satType   type of the satellite	
	 * @return sat_type   satellite type 
	 */
	private static String getsatType(String satType) {
		String sat_type = "";
		if (satType.contains(GPS_SATELLITE))
			sat_type = "1";
		else if (satType.contains(GALILEO_SATELLITE))
			sat_type = "2";
		else if (satType.contains("EGNOS") || satType.contains("SBAS")
				|| satType.contains("SDCM"))
			sat_type = "5";
		else if (satType.contains("WAAS"))
			sat_type = "6";
		else if (satType.contains("MSAS"))
			sat_type = "7";
		else if (satType.contains("GAGAN"))
			sat_type = "8";
		else if (satType.contains("QZSS"))
			sat_type = "9";
		else 
			sat_type = "0";
		return sat_type;
	}
}

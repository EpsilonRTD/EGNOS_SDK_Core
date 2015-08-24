/**
 * @file CoordinatesMain.c
 *
 * @brief CoordinatesMain module header file defining the GPS/EGNOS position
 * computation functions.
 * @details The module integrates EGNOS Demo App and EGNOS-SW Receiver modules
 * i.e. integrate java and c using android ndk tool. This module is the
 * entrance to core of the software, it contains the two functions that
 * request the GPS and EGNOS positioning processes.
 *
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

#include "CoordinatesMain.h"
#include "stdarg.h"
#include <stdlib.h>

/**
 * android_syslog function
 * Log
 * @param level  log information
 * @param format format of string to display
 */
void android_syslog(int level, const char *format, ...)
{
	va_list arglist;

	va_start(arglist, format);
	__android_log_vprint(level, APPNAME, format, arglist);
	va_end(arglist);

	return;
}

/**
 * getLongitudeLatitudeEGNOS function
 * The function calls the positioning function from the positioning module to get the EGNOS position and HPL.
 * It returns the table that contains the results of the process.
 * @param env             Structure that contains the interface to the JVM.
 * @param obj             Java object
 * @param ephemDataArray  The table of up to 32 ephemeris data (the 3 subframes) plus the prn
 *                        number in a string format and the number of ephemeris sets available
 *                        (0: no. of ephemeris sets; 1-2:PRN, 2-902:3 subframes set 1,
 *                        903-4503: ephemeris sets 2-5)
 * @param sat_dataArray   The table of 19 satellites channels: PRN, TOW, Pseudorange, C/N0
 * @param jmsg1           The message type 1 plus TOW in string format (0-12:TOW, 12-262:Payload)
 * @param jmsg10          The message type 10 plus TOW in string format (0-12:TOW, 12-262:Payload)
 * @param jmsg12          The message type 12 plus TOW in string format (0-12:TOW, 12-262:Payload)
 * @param jmsg7           The message type 7 plus TOW in string format (0-12:TOW, 12-262:Payload)
 * @param jmsg6           The message type 6 plus TOW in string format (0-12:TOW, 12-262:Payload)
 * @param jm18_t          The table of max. 5 messages type 18 plus TOW in string format (0-12:TOW, 12-262:Payload)
 * @param jm26_t          The table of max. 25 messages type 26 plus TOW in string format (0-12:TOW, 12-262:Payload)
 * @param jmsg2_5         The table of 8 messages type 2-5 plus TOW in string format (0-12:TOW, 12-262:Payload);
 *                        msg2_5_string[] positions 0-3 :message 2-5 current time, positions 0-3 :message 2-5 previous time
 * @param jmsg24_t        The table of max. 25 messages type 24 plus TOW in string format (0-12:TOW, 12-262:Payload)
 * @param jmsg25_t        The table of max. 15 messages type 25 plus TOW in string format (0-12:TOW, 12-262:Payload)
 * @param jmsg9           The message type 9 plus TOW in string format (0-12:TOW, 12-262:Payload)
 * @param jmsg17          The message type 17 plus TOW in string format (0-12:TOW, 12-262:Payload)
 * @param jinit_pos       The initial estimation of the position solution (init_pos[0]:X ECEF in meters, init_pos[1]:Y
 *                        ECEF in meters, init_pos[2]:Z ECEF in meters, init_pos[3]:speed of light multiply by
 *                        receiver clock bias (c.dt) in meters)
 * @param jutc_array	  Array that holds the UTC parameters
 * @param jklob_array	  Array that holds the Klobuchar coefficients for ionopsheric corrections
 * @param RnDoptions	  Array that holds the R&D options
 * @return coordinates    The table containing [0]:latitude(deg.) [1]:longitude(deg.) [2]:altitude(m) [3]:HPL
 * 						  [4]:X ECEF (m)[5]:Y ECEF (m) [6]:Z ECEF (m) [7]:c.dt (m) [8]:HDOP [9]:iono_flag
 * 						  (0:ionospheric correction not computed for all PRN  (messages 26 or 18 missing),
 * 						  1:ionospheric correction computed for all PRN) [10]:egnos_position(1:indicating EGNOS
 * 						  position(i.e. green marker) to be displayed,0:indicating preliminary EGNOS Position
 * 						  (i.e. orange marker) to be displayed), Egnos corrections decoded for all satellites
 */
JNIEXPORT jdoubleArray Java_com_ec_egnossdk_uBlox_getLongitudeLatitudeEGNOS
(JNIEnv * env,jobject obj,jobjectArray ephemDataArray,jobjectArray sat_dataArray,jstring jmsg1,
		jstring jmsg10,jstring jmsg12,jstring jmsg7,jstring jmsg6,jobjectArray jm18_t,
		jobjectArray jm26_t,jobjectArray jmsg2_5,jobjectArray jmsg24_t,jobjectArray jmsg25_t,
		jstring jmsg9,jstring jmsg17, jdoubleArray jinit_pos, jdoubleArray jutc_array, 
		jdoubleArray jklob_array, jintArray RnDoptions,jdoubleArray sat_data_notUsedArray)
{

	jdoubleArray coordinates = (*env)->NewDoubleArray(env, 789);

	double utc_data[8];
	double klob_data[8];

	double  vect[3],DOP[4],PL[2];
	double init_pos[4];
	double latitude,longitude,altitude,d,egnoslatitude,egnoslongitude,egnosaltitude;
	double HPL = 0;
	double VPL = 0;
	double HDOP = 0;

	// Li   for  NMEA
	double VDOP = 0;
	double PDOP = 0;
	double TDOP = 0;
	// Li   for  NMEA

	double HDOP_lim;
	int iono_flag = 0;
	int sat_count;
	int i,j;
	Egnos_msg msg1,msg10,msg12,msg7,msg6,msg9,msg17;;
	Egnos_msg m18_t[11];
	Egnos_msg m26_t[25];
	Egnos_msg msg2_5[4][2];
	Egnos_msg msgltc24_t[25];
	Egnos_msg msgltc25_t[15];

	// set flag to compute egnos posiiton
	int egnos = 1;
	int count_fast =1;
	int count_long =1;

	double sat_array[15];
	sat_array[0] = -1;
	sat_array[1] = -1;
	sat_array[2] = -1;
	sat_array[3] = -1;
	sat_array[4] = -1;
	sat_array[5] = -1;
	sat_array[6] = -1;
	sat_array[7] = 0;
	sat_array[8] = 0;
	sat_array[9] = 0;
	sat_array[10] = 0;
	sat_array[11] = 0;
	sat_array[12] = 0;
	sat_array[13] = -1;
	sat_array[14] = -1;
	
	vect[0] = 0;
	vect[1] = 0;
	vect[2] = 0;
	
	DOP[0] = 0;
	DOP[1] = 0;
	DOP[2] = 0;
	DOP[3] = 0;
	
	PL[0] = 0;
	PL[1] = 0;
	
	Satellite S_t[19];
	Satellite S_t_notUsed[19];

	const char *ephemDataStringLine="";
	char ephemDataString[32][4504]; // 3 + 900*5 + 1
	jstring ephemjString;

	const char *msg2_5StringLine="";
	char msg2_5String[8][263];
	jstring msg2_5jString;

	const char *msg24_tStringLine="";
	char msg24_tString[25][263];
	jstring msg24_tjString;

	const char *msg25_tStringLine="";
	char msg25_tString[15][263];
	jstring msg25_tjString;

	const char *m18_tStringLine="";
	char m18_tString[5][263];
	jstring m18_tjString;

	const char *m26_tStringLine="";
	char m26_tString[25][263];
	jstring m26_tjString;

	jdouble *elementSat_data;
	jdoubleArray oneDim;

	int rnd_options[8];
	double sat_data[19][4];
	double sat_data_notUsed[19][4];
	double sbas_data[4][4];
	char eph_data[19][901];
	char eph_data_temp[19][4504];
	char eph_data_nu[19][901];

	const char* msg1String;
	const char* msg10String;
	const char* msg12String;
	const char* msg7String;
	const char* msg6String;
	const char* msg9String;
  	const char* msg17String;
	char prn_char[2]="";
	char tow_char[13]="";
	char egnos_char[251]="";
	char* username;
	char* password;
	int countMsg1 = 0;
	int countMsg6 = 0;
	int countMsg7 = 0;
	int countMsg9 = 0;
	int countMsg10 = 0;
	int countMsg12 = 0;
	int countMsg17 = 0;
	int countMsg2_5 = 0;
	int countMsg18 = 0;
	int countMsg24 = 0;
	int countMsg25 = 0;
	int countMsg26 = 0;
	int egnos_position = 2;
	
	// The R&D options  
	jint *element_rnd = (*env)->GetIntArrayElements(env, RnDoptions, 0);
	for(i=0;i<8;i++){
		rnd_options[i] = element_rnd[i];
	}
	(*env)->ReleaseIntArrayElements(env, RnDoptions, element_rnd, 0);
	
	// The table of UTC parameters  
	jdouble *element_utc = (*env)->GetDoubleArrayElements(env, jutc_array, 0);
	for(i=0;i<8;i++){
		utc_data[i] = element_utc[i];
	}
	(*env)->ReleaseDoubleArrayElements(env, jutc_array, element_utc, 0);
	
	// The table of ionopsheric coefficients used in the Klobuchar model  
	jdouble *element_klob = (*env)->GetDoubleArrayElements(env, jklob_array, 0);
	for(i=0;i<8;i++){
		klob_data[i] = element_klob[i];
	}
	(*env)->ReleaseDoubleArrayElements(env, jklob_array, element_klob, 0);
	
	// The initial user position
	jdouble *elementinit_pos = (*env)->GetDoubleArrayElements(env, jinit_pos, 0);
	for(i=0;i<4;i++){
		init_pos[i] = elementinit_pos[i];
	}
	(*env)->ReleaseDoubleArrayElements(env, jinit_pos, elementinit_pos,0);

	if(is_nan(init_pos[0]) || is_nan(init_pos[1]) || is_nan(init_pos[2]) || is_nan(init_pos[3]))
	{
		init_pos[0] = 0;
		init_pos[1] = 0;
		init_pos[2] = 0;
		init_pos[3] = 0;
	}

	// Table of ephemeris
	for (i=0; i<32; i++){
		ephemjString = (jstring) (*env)->GetObjectArrayElement(env, ephemDataArray, i);
		ephemDataStringLine = (*env)->GetStringUTFChars(env, ephemjString,JNI_FALSE);
		if(ephemDataStringLine != '\0')
			strcpy(ephemDataString[i],ephemDataStringLine);
		else
			ephemDataString[i][0] = '\0';
		(*env)->ReleaseStringUTFChars(env,ephemjString , ephemDataStringLine);
	}

	// Table of PRN TOW PR and SNR
	double prn;
	int count_satdata_nu = 0;
	int count_satdata = 0;
	int count_sbasdata = 0;
	for(i = 0; i < 19; i++){
		oneDim=(*env)->GetObjectArrayElement(env,sat_dataArray,i);
		elementSat_data=(*env)->GetDoubleArrayElements(env, oneDim,JNI_FALSE );
		prn = elementSat_data[0];
		// GPS satellites are saved first in the sat_data table, and after the SBAS satellites are added
		if (prn < 38.0 && prn > 0.0){
			for(j = 0; j < 4; j++)
			{
				sat_data[count_satdata][j] = elementSat_data[j];
			}
			count_satdata++;
		}
		else if(prn > 119 && prn < 139){
			for(j = 0; j < 4; j++)
				sbas_data[count_sbasdata][j] = elementSat_data[j];
			count_sbasdata++;
		}
			
		(*env)->ReleaseDoubleArrayElements(env,oneDim,elementSat_data,0);
	}
	
	for(j = 0; j < count_sbasdata; j++){
		sat_data[count_satdata+j][0] = sbas_data[j][0];
		sat_data[count_satdata+j][1] = sbas_data[j][1];
		sat_data[count_satdata+j][2] = sbas_data[j][2];
		sat_data[count_satdata+j][3] = sbas_data[j][3];
	}
	
	for(j = count_satdata+count_sbasdata; j < 19; j++){
		sat_data[j][0] = 0;
		sat_data[j][1] = 0;
		sat_data[j][2] = 0;
		sat_data[j][3] = 0;
	}
	
	for(j = 0; j < 19; j++){
		eph_data[j][0] = '\0';
		eph_data_temp[j][0] = '\0';
	}
	
	for( i = 0; i < 19; i++){
		oneDim = (*env)->GetObjectArrayElement(env,sat_data_notUsedArray,i);
		elementSat_data = (*env)->GetDoubleArrayElements(env, oneDim,JNI_FALSE );
		prn = elementSat_data[0];
		if (prn > 0)
		{
			for( j = 0; j < 4; j++)
			{
			  sat_data_notUsed[count_satdata_nu][j] = elementSat_data[j];
			}
			count_satdata_nu++;
		}
		(*env)->ReleaseDoubleArrayElements(env,oneDim,elementSat_data,0);
	}

	// Creates eph_data table of max 15 ephemeris, with PRN in the same order as sat_prn table
	double prn_eph = 0;
	int no_eph;
	for(j = 0; j < 19; j++){

		if (sat_data[j][0] != 0.0 && sat_data[j][0] <= 32){
			for(i = 0; i < 32; i++){
				if(ephemDataString[i][0] != '\0'){
					char char_prn[3]="";
					strncpy(char_prn,ephemDataString[i]+1,2);
					char_prn[2] = '\0';
					prn_eph =  atof(char_prn);
					if(sat_data[j][0] == prn_eph && prn_eph != 0.0){
						no_eph = ((((int)ephemDataString[i][0]))-48);
						strncpy(eph_data_temp[j],ephemDataString[i]+3,no_eph*900);
						strncpy(    eph_data[j],ephemDataString[i]+3, 900);
						eph_data_temp[j][no_eph*900]='\0';
						eph_data[j][900]='\0';
					}
				}
			}
		}
	}

	for(j = 0; j < 19; j++){
		eph_data_nu[j][0] = '\0';
	}

	// Creates eph_data_nu table of max 19 ephemeris, with PRN in the same order as sat_prn table
	// for unused satellites
	double prn_eph_nu = 0;
	for(j = 0; j < 19; j++){
		if (sat_data_notUsed[j][0] != 0.0 && sat_data_notUsed[j][0] <= 32){
			for(i = 0; i < 32; i++){
				if(ephemDataString[i][0] != '\0'){
					char char_prn[3] = "";
					strncpy(char_prn,ephemDataString[i]+1,2);
					char_prn[2] = '\0';
					prn_eph_nu =  atof(char_prn);
					if(sat_data_notUsed[j][0] == prn_eph_nu && prn_eph_nu != 0.0 ){
						strncpy(eph_data_nu[j],ephemDataString[i]+3, 900);
						eph_data_nu[j][900]='\0';
					}
				}

			}
		}
		else{
			eph_data_nu[j][0]='\0';
		}
	}

	android_syslog(ANDROID_LOG_INFO, "Acquiring EGNOS position from Signal in Space.");


	// Message type 1 PRN mask
	msg1String = (*env)->GetStringUTFChars(env, jmsg1, JNI_FALSE);

	init_msg(&msg1,1);
	if(msg1String != '\0')
	{
		strncpy(tow_char, msg1String, 12);
		tow_char[12] = '\0';

		if(atof(tow_char) != 0)
			msg1.tow = atof(tow_char);

		strncpy(egnos_char, msg1String + 12, 250);
		egnos_char[250] = '\0';

		msg1.bin_msg = egnos_char;
		decode_msg1(&msg1);
		countMsg1++;
	}

	// Get the EGNOS time
	msg12String = (*env)->GetStringUTFChars(env, jmsg12, JNI_FALSE);

	init_msg(&msg12,12);
	if(msg12String != '\0')
	{
		strncpy(tow_char, msg12String, 12);
		tow_char[12] = '\0';

		if(atof(tow_char) != 0)
			msg12.tow = atof(tow_char);

		strncpy(egnos_char, msg12String + 12, 250);
		egnos_char[250] = '\0';

		msg12.bin_msg =egnos_char;
		decode_msg12(&msg12);
		countMsg12++;
	}

	// Get the degradation factors
	msg10String = (*env)->GetStringUTFChars(env, jmsg10, JNI_FALSE);

	init_msg(&msg10,10);
	if(msg10String != '\0')
	{
		strncpy(tow_char, msg10String, 12);
		tow_char[12] = '\0';

		if(atof(tow_char) != 0)
			msg10.tow = atof(tow_char);
		msg10.tow = -1;

		strncpy(egnos_char, msg10String + 12, 250);
		egnos_char[250] = '\0';

		msg10.bin_msg =egnos_char;
		decode_msg10(&msg10);
		countMsg10++;
	}

	android_syslog(ANDROID_LOG_INFO, "before  msg7");

	// Get the fast correction degradation factors
	msg7String = (*env)->GetStringUTFChars(env, jmsg7, JNI_FALSE);
	init_msg(&msg7,7);
	if(msg7String != '\0' || msg7String != "")
	{

		strncpy(tow_char, msg7String, 12);
		tow_char[12] = '\0';

		if(atof(tow_char) != 0)
			msg7.tow = atof(tow_char);

		msg7.tow = -1;

		strncpy(egnos_char, msg7String + 12, 250);
		egnos_char[250] = '\0';

		msg7.bin_msg =egnos_char;

		decode_msg7(&msg7);
		countMsg7++;
	}

	android_syslog(ANDROID_LOG_INFO, "after msg7");

	// Get the integrity information

	msg6String = (*env)->GetStringUTFChars(env, jmsg6, JNI_FALSE);

	init_msg(&msg6,6);
	if(msg6String != '\0')
	{
		strncpy(tow_char, msg6String, 12);
		tow_char[12] = '\0';

		if(atof(tow_char) != 0)
			msg6.tow = atof(tow_char);

		strncpy(egnos_char, msg6String + 12, 250);
		egnos_char[250] = '\0';

		msg6.bin_msg =egnos_char;
		decode_msg6(&msg6);
		countMsg6++;
	}

	// GEO navigation messages
  	msg9String = (*env)->GetStringUTFChars(env, jmsg9, JNI_FALSE);

  	init_msg(&msg9,9);
  	if(msg9String != '\0')
  	{
    	strncpy(tow_char, msg9String, 12);
		tow_char[12] = '\0';
		
    	if(atof(tow_char) != 0)
      		msg9.tow = atof(tow_char);
      		
    	strncpy(egnos_char, msg9String + 12, 250);
		egnos_char[250] = '\0';
		
    	msg9.bin_msg =egnos_char;
    	decode_msg9(&msg9);
    	countMsg9++;
  	}

  	// GEO satellite almanacs
  	msg17String = (*env)->GetStringUTFChars(env, jmsg17, JNI_FALSE);

  	init_msg(&msg17,17);
  	if(msg17String != '\0')
  	{
    	strncpy(tow_char, msg17String, 12);
		tow_char[12] = '\0';	
    
    	if(atof(tow_char) != 0)
      		msg17.tow = atof(tow_char);
      		
        strncpy(egnos_char, msg17String + 12, 250);
		egnos_char[250] = '\0';
		
    	msg17.bin_msg = egnos_char;
    	decode_msg17(&msg17);
    	countMsg17++;
  }
  
	// Table of message 2-5
	for (i = 0; i < 8; i++){
		msg2_5jString = (jstring) (*env)->GetObjectArrayElement(env, jmsg2_5, i);
		msg2_5StringLine = (*env)->GetStringUTFChars(env, msg2_5jString,JNI_FALSE);

		if(msg2_5StringLine != '\0'){
			strncpy(msg2_5String[i],msg2_5StringLine,262);
			msg2_5String[i][262] = '\0';

			countMsg2_5++;
		}
		else
			msg2_5String[i][0] = '\0';

		(*env)->ReleaseStringUTFChars(env,msg2_5jString , msg2_5StringLine);
	}

	// Set the fast corrections: decodes messages 2 to 5
	set_fastCorrectionsSis(msg2_5,msg2_5String);

	// Table of message 24
	for (i=0; i<25; i++){
		msg24_tjString = (jstring) (*env)->GetObjectArrayElement(env, jmsg24_t, i);
		msg24_tStringLine = (*env)->GetStringUTFChars(env, msg24_tjString,JNI_FALSE);

		if(msg24_tStringLine != '\0'){
			strncpy(msg24_tString[countMsg24],msg24_tStringLine,262);
			msg24_tString[countMsg24][262] = '\0';
			countMsg24++;
		}

		(*env)->ReleaseStringUTFChars(env,msg24_tjString , msg24_tStringLine);
	}
	for (i = countMsg24; i < 25; i++){
		msg24_tString[i][0] = '\0';
	}

	// Table of message 25
	for (i=0; i<15; i++){
		msg25_tjString = (jstring) (*env)->GetObjectArrayElement(env, jmsg25_t, i);
		msg25_tStringLine = (*env)->GetStringUTFChars(env, msg25_tjString,JNI_FALSE);

		if(msg25_tStringLine != '\0'){
			msg25_tString[countMsg25][0] = '\0';
			strncpy(msg25_tString[countMsg25],msg25_tStringLine,262);
			msg25_tString[countMsg25][262] = '\0';

			countMsg25++;
		}
		(*env)->ReleaseStringUTFChars(env,msg25_tjString , msg25_tStringLine);
	}
	for (i = countMsg25; i < 15; i++){
		msg25_tString[i][0] = '\0';
	}

	// Set the long term corrections
	set_LongCorrections_MT24(msgltc24_t,msg24_tString);
	set_LongCorrections_MT25(msgltc25_t,msg25_tString);

	// Set the ephemeris data set corresponding to the Long Correction IODE from MT24
	char iode_eph[9] = "";
	int prn_m24, kk, no_sets,iodee, prn_m25, p;

	for (i = 0; i< countMsg24; i++){
		for (p = 0; p < 2; p++)
		{
			prn_m24 = msg1.prn[(int)msgltc24_t[i].prn_long[p][0]-1]; 	// check PRN
			for(j =0 ; j< 19; j++)
			{
				if(sat_data[j][0] == prn_m24)
				{
					no_sets = ((((int)(ephemDataString[(int)(sat_data[j][0])-1][0]))-48));

					for (kk = 0; kk < no_sets; kk++ )
					{
						strncpy(iode_eph, eph_data_temp[j]+360+kk*900, 8);
						iode_eph[8] = '\0';
						iodee = bin2dec(iode_eph);

						if(msgltc24_t[i].prn_long[p][1] == iodee){				// check IODE
							char tmp[901] =  "";
							strncpy(eph_data[j], eph_data_temp[j]+kk*900, 900);
							eph_data[j][900] = '\0';
							break;
						}
					}
				}
			}
		}
	}
	// Set the ephemeris data set corresponding to the Long Correction IODE from MT25
	for (i = 0; i < countMsg25; i++){
		for (p = 0; p < 4; p++)
		{
			prn_m25 = msg1.prn[(int)msgltc25_t[i].prn_long[p][0]-1]; 	// check PRN

			for(j = 0 ; j < 19; j++)
			{
				if(sat_data[j][0] == prn_m25)
				{
					no_sets = ((((int)(ephemDataString[(int)(sat_data[j][0])-1][0]))-48));

					for (kk = 0; kk < no_sets; kk++ )
					{
						strncpy(iode_eph, eph_data_temp[j]+360+kk*900, 8);
						iode_eph[8] = '\0';
						iodee = bin2dec(iode_eph);

						if(msgltc25_t[i].prn_long[p][1] == iodee){				// check IODE
							strncpy(eph_data[j], eph_data_temp[j]+kk*900, 900);
							eph_data[j][900] = '\0';
							break;
						}
					}
				}
			}
		}
	}

	// Table of message 18
	for (i = 0; i < 5; i++){
		m18_tjString = (jstring) (*env)->GetObjectArrayElement(env, jm18_t, i);
		m18_tStringLine = (*env)->GetStringUTFChars(env, m18_tjString,JNI_FALSE);
		if(m18_tStringLine != '\0'){
			strncpy(m18_tString[countMsg18],m18_tStringLine,262);
			m18_tString[countMsg18][262] = '\0';
			countMsg18++;
		}
		(*env)->ReleaseStringUTFChars(env,m18_tjString , m18_tStringLine);
	}
	for (i = countMsg18; i < 5; i++){
		m18_tString[i][0] = '\0';
	}

	// Table of message 26
	for (i = 0; i < 25; i++){
		m26_tjString = (jstring) (*env)->GetObjectArrayElement(env, jm26_t, i);
		m26_tStringLine = (*env)->GetStringUTFChars(env, m26_tjString,JNI_FALSE);
		if(m26_tStringLine != '\0'){
			strncpy(m26_tString[countMsg26],m26_tStringLine,262);
			m26_tString[countMsg26][262] = '\0';
			countMsg26++;
		}
		(*env)->ReleaseStringUTFChars(env,m26_tjString , m26_tStringLine);
	}
	for (i = countMsg26; i < 25; i++){
		m26_tString[i][0] = '\0';
	}

	sat_count = positioning(vect, init_pos, DOP, PL, eph_data, sat_data, &msg1, msg2_5,
			&msg6, &msg7, &msg10, &msg12,&msg9, &msg17, m18_t, msgltc24_t, msgltc25_t ,
			m26_t, m18_tString, m26_tString, egnos, &iono_flag, sat_array, S_t, 
			utc_data, klob_data, rnd_options,sat_data_notUsed,eph_data_nu,S_t_notUsed);

	android_syslog(ANDROID_LOG_INFO, "Pos CM[0]: %f\n", vect[0]);
	android_syslog(ANDROID_LOG_INFO, "Pos CM[1]: %f\n", vect[1]);
	android_syslog(ANDROID_LOG_INFO, "Pos CM[2]: %f\n", vect[2]);
	
	egnoslatitude   = get_latitude(vect);
	egnoslongitude  = get_longitude(vect);
	egnosaltitude   = get_height(vect);

	HDOP = DOP[0];

	// Li   for  NMEA
	VDOP = DOP[1];
	PDOP = DOP[2];
	TDOP = DOP[3];
	// Li   for  NMEA

	HPL = PL[0];
	VPL = PL[1];

	android_syslog(ANDROID_LOG_INFO, "CoordinatesMain | Count Message 1: %i",
			countMsg1);
	android_syslog(ANDROID_LOG_INFO, "CoordinatesMain | Count Message 2_5: %i",
			countMsg2_5);
	android_syslog(ANDROID_LOG_INFO, "CoordinatesMain | Count Message 6: %i",
			countMsg6);
	android_syslog(ANDROID_LOG_INFO, "CoordinatesMain | Count Message 7: %i",
			countMsg7);
	android_syslog(ANDROID_LOG_INFO, "CoordinatesMain | Count Message 10: %i",
			countMsg10);
	android_syslog(ANDROID_LOG_INFO, "CoordinatesMain | Count Message 12: %i",
			countMsg12);
	android_syslog(ANDROID_LOG_INFO, "CoordinatesMain | Count Message 18: %i",
			countMsg18);
	android_syslog(ANDROID_LOG_INFO, "CoordinatesMain | Count Message 24: %i",
			countMsg24);
	android_syslog(ANDROID_LOG_INFO, "CoordinatesMain | Count Message 25: %i",
			countMsg25);
	android_syslog(ANDROID_LOG_INFO, "CoordinatesMain | Count Message 26: %i",
			countMsg26);
	android_syslog(ANDROID_LOG_INFO, "CoordinatesMain | Iono Flag: %i",
			iono_flag);
	android_syslog(ANDROID_LOG_INFO, "CoordinatesMain | HPL Value: %f",
			HPL*6.18);

	for(i = 0; i < sat_count; i++)
		if (S_t[i].use == 2 && S_t[i].fast_set == 0)
			count_fast = 0;

	for(i = 0; i < sat_count; i++)
		if (S_t[i].use == 2 && S_t[i].long_set == -1)
			count_long = 0;

	android_syslog(ANDROID_LOG_INFO, "CoordinatesMain | Count_long %d",count_long);
	if(countMsg1 == 1 && countMsg7 == 1 && countMsg10 == 1 && countMsg12 == 1  && count_fast == 1 && count_long == 1 )
		egnos_position = 1;
	else
		egnos_position = 0;

	android_syslog(ANDROID_LOG_INFO, "CoordinatesMain | Egnos_Position: %i",egnos_position);

	android_syslog(ANDROID_LOG_INFO, "No of iterations: %f\n",sat_array[3]);
	android_syslog(ANDROID_LOG_INFO, "EGNOS position: %.20f %.20f %.20f %f\n",egnoslatitude,
			egnoslongitude,egnosaltitude,HDOP);

	jdouble temp[789];
	temp[0] = egnoslatitude;
	temp[1] = egnoslongitude;
	temp[2] = egnosaltitude;
	temp[3] = HPL;
	temp[4] = init_pos[0];
	temp[5] = init_pos[1];
	temp[6] = init_pos[2];
	temp[7] = init_pos[3];
	temp[8] = HDOP;
	temp[9] = iono_flag;
	temp[10] = egnos_position;

	temp[11] = sat_array[0];   // total no of sats
	temp[12] = sat_array[1];   // low elev
	temp[13] = sat_array[2];   // not in mask
	temp[14] = sat_count;      // sats used
	temp[15] = sat_array[3];   // iterations
	temp[16] = sat_array[4]; // 1 if jump
	temp[17] = sat_array[5]; // jump on x
	temp[18] = sat_array[6]; // jump on y
	temp[19] = VPL;
	
	temp[475] = sat_array[7];
	temp[476] = sat_array[8];
	temp[477] = sat_array[9];
	temp[478] = sat_array[10];
	temp[479] = sat_array[11];
	temp[480] = sat_array[12];
	temp[481] = sat_array[13];
	temp[482] = sat_array[14];
	
	android_syslog(ANDROID_LOG_INFO, "Sat array: %f\n",sat_array[7]);
	android_syslog(ANDROID_LOG_INFO, "Sat array: %f\n",sat_array[8]);
	android_syslog(ANDROID_LOG_INFO, "Sat array: %f\n",sat_array[9]);
	android_syslog(ANDROID_LOG_INFO, "Sat array DOP: %f\n",sat_array[10]);
	android_syslog(ANDROID_LOG_INFO, "Sat array DOP: %f\n",sat_array[11]);
	android_syslog(ANDROID_LOG_INFO, "Sat array DOP: %f\n",sat_array[12]);
	
	int k;
	for(k = 0; k < sat_array[0]; k++)
	{
		temp[20 + k*26]      = S_t[k].prn;
		temp[20 + k*26 + 1]  = S_t[k].use;
		temp[20 + k*26 + 2]  = S_t[k].rnd;
		temp[20 + k*26 + 3]  = S_t[k].prn_mask;
		temp[20 + k*26 + 4]  = S_t[k].low_elv;
		temp[20 + k*26 + 5]  = S_t[k].tow2;
		temp[20 + k*26 + 6]  = S_t[k].el;
		temp[20 + k*26 + 7]  = S_t[k].iono_delay;
		temp[20 + k*26 + 8]  = S_t[k].iono_model;
		temp[20 + k*26 + 9]  = S_t[k].tropo_delay;
		temp[20 + k*26 + 10]  = S_t[k].fast_delay;
		temp[20 + k*26 + 11]  = S_t[k].rrc;
		temp[20 + k*26 + 12]  = S_t[k].udrei;
		temp[20 + k*26 + 13] = S_t[k].long_set;
		temp[20 + k*26 + 14] = S_t[k].daf0;
		temp[20 + k*26 + 15] = S_t[k].dx;
		temp[20 + k*26 + 16] = S_t[k].dy;
		temp[20 + k*26 + 17] = S_t[k].dz;
		temp[20 + k*26 + 18] = S_t[k].sigma2;
		temp[20 + k*26 + 19] = S_t[k].sigma_flt2;
		temp[20 + k*26 + 20] = S_t[k].sigma_tropo2;
		temp[20 + k*26 + 21] = S_t[k].sigma_uire2;
		temp[20 + k*26 + 22] = S_t[k].eps_fc;
		temp[20 + k*26 + 23] = S_t[k].eps_rrc;
		temp[20 + k*26 + 24] = S_t[k].eps_ltc;
		temp[20 + k*26 + 25] = S_t[k].eps_er;
	}

	// Li   for  NMEA
	temp[500] = VDOP;
	temp[501] = PDOP;
	temp[502] = TDOP;

	for(k = 0; k < sat_array[0]; k++)
	{
		temp[503 + k*4] = S_t[k].weeknb;
		temp[504 + k*4] = S_t[k].toe;
		temp[505 + k*4] = S_t[k].az;
		temp[506 + k*4] = S_t[k].cn0;
	}
	// Li   for  NMEA

	temp[591] = count_satdata_nu;
	int count_nu =0;
    for(k = 0; k < count_satdata_nu;k++){
    	temp[592 + count_nu*4] = S_t_notUsed[k].prn;
    	temp[593 + count_nu*4] = S_t_notUsed[k].az;
    	temp[594 + count_nu*4] = S_t_notUsed[k].el;
    	temp[595 + count_nu*4] = S_t_notUsed[k].cn0;
        android_syslog(ANDROID_LOG_INFO, "Coordinates | prn: %f, az: %f, el: %f\n",
	    			S_t_notUsed[k].prn,S_t_notUsed[k].az,S_t_notUsed[k].el );
	}


	// Li   for  RTCM
	temp[667] = sat_count;   // Number of Satellite Used
	temp[668] = S_t[0].tow2;

	// RTCM Message 1
	int count_use=0;
	for(k = 0; k < sat_array[0]; k++){
        android_syslog(ANDROID_LOG_INFO, "Coordinates | use: %d, prn: %f, prc: %f\n",
        		S_t[k].use,S_t[k].prn,(S_t[k].pr_c-S_t[k].pr));
		if(S_t[k].use !=0)
		{
			temp[669 + count_use*8] = S_t[k].prn;
			temp[670 + count_use*8] = S_t[k].pr_c-S_t[k].pr;
			temp[671 + count_use*8] = S_t[k].rrc;
			temp[672 + count_use*8] = S_t[k].iodc;

			temp[673 + count_use*8] = S_t[k].pr;
			temp[674 + count_use*8] = S_t[k].pos_x;
			temp[675 + count_use*8] = S_t[k].pos_y;
			temp[676 + count_use*8] = S_t[k].pos_z;

			count_use=count_use+1;
		}
	}
	// Li   for  RTCM


	(*env)->SetDoubleArrayRegion( env, coordinates, 0, 789,temp);

	(*env)->ReleaseStringUTFChars(env,jmsg1,msg1String);
	(*env)->ReleaseStringUTFChars(env,jmsg10 , msg10String);
	(*env)->ReleaseStringUTFChars(env,jmsg12 , msg12String);
	(*env)->ReleaseStringUTFChars(env,jmsg6 , msg6String);

	//removed this since it is not required for API level 14 or above,
	//but required for API level below 14.
	//(*env)->DeleteLocalRef(env,coordinates);
	return coordinates;
}


/**
 * getLongitudeLatitudeGPS function
 * The function calls the positioning function from the positioning module to get the GPS position.
 * it returns the table that contains the results of the process.
 * @param env             pointer is a structure that contains the interface to the JVM.
 * @param obj             java object
 * @param ephemDataArray  The table of up to 32 ephemeris data (the 3 subframes) plus the prn
 *                        number in a string format and the number of ephemeris sets available
 *                        (0: no. of ephemeris sets; 1-2:PRN, 2-902:3 subframes set 1,
 *                        903-4503: ephemeris sets 2-5) - the most recent ephemeris set is used
 * @param sat_dataArray    The table of 19 satellites channels: PRN, TOW, Pseudorange, C/N0
 * @param jinit_pos        The initial estimation of the position solution (init_pos[0]:X ECEF in meters,
 *                         init_pos[1]:Y ECEF in meters, init_pos[2]:Z ECEF in meters, init_pos[3]:speed of light
 *                         multiply by receiver clock bias (c.dt) in meters)
 * @param jutc_array	  Array that holds the UTC parameters
 * @return coordinates     the table containing [0]:latitude(deg.) [1]:longitude(deg.) [2]:altitude(m) [3]:X ECEF
 *                         (m) [4]:Y ECEF (m) [5]:Z ECEF (m) [6]:c.dt (m) [7]:HDOP
 */
JNIEXPORT jdoubleArray Java_com_ec_egnossdk_uBlox_getLongitudeLatitudeGPS
(JNIEnv * env,jobject obj,jobjectArray ephemDataArray,jobjectArray sat_dataArray,
		jdoubleArray jinit_pos, jdoubleArray jutc_array,jdoubleArray sat_data_notUsedArray)
{
	jdoubleArray coordinates = (*env)->NewDoubleArray(env, 377);
	double utc_data[8];
	double klob_data[8];
	double  vect[3],DOP[4],PL[2];
	double init_pos[4];
	double gpslatitude,gpslongitude,gpsaltitude,d,egnoslatitude,egnoslongitude,egnosaltitude;
	double HPL = 0;
	double HDOP = 0;

	// Li   for  NMEA
	double VDOP = 0;
	double PDOP = 0;
	double TDOP = 0;
	// Li   for  NMEA

	double HDOP_lim;
	int iono_flag = 0;
	int sat_count;
	int i,j, k;
	int sock;
	Egnos_msg msg1,msg10,msg12,msg7,msg6,msg9,msg17;
	Egnos_msg m18_t[11];
	Egnos_msg m26_t[25];
	Egnos_msg msg2_5[4][2];
	Egnos_msg msg24_t[25];
	Egnos_msg msg25_t[15];

	int rnd_options[8] = {0,0,0,0,0,0,0,0};

	double sat_array[15];
	sat_array[0] = -1;
	sat_array[1] = -1;
	sat_array[2] = -1;
	sat_array[3] = -1;
	sat_array[4] = -1;
	sat_array[5] = -1;
	sat_array[6] = -1;
	sat_array[7] = -1;
	sat_array[8] = -1;
	sat_array[9] = -1;
	sat_array[10] = -1;
	sat_array[11] = -1;
	sat_array[12] = -1;
	sat_array[13] = -1;
	sat_array[14] = -1;

	Satellite S_t[19];
	Satellite S_t_notUsed[19];

	const char *ephemDataStringLine="";
	char ephemDataString[32][4504];
	jstring ephemjString;

	jdouble *elementSat_data;
	jdoubleArray oneDim;
	
	jdouble *elementUTC_data;

	char m18_tString[5][263];
	char m26_tString[25][263];
	double sat_data[19][4];
	double sat_data_notUsed[19][4];
	char eph_data[19][901];
	char eph_data_nu[19][901];

	jdouble *element = (*env)->GetDoubleArrayElements(env, jutc_array, 0);
	for(i=0;i<8;i++){
		utc_data[i] = element[i];
	}
	(*env)->ReleaseDoubleArrayElements(env, jutc_array, element, 0);
	

	jdouble *elementinit_pos = (*env)->GetDoubleArrayElements(env, jinit_pos, 0);
	for(i=0;i<4;i++){
		init_pos[i] = elementinit_pos[i];
	}
	(*env)->ReleaseDoubleArrayElements(env, jinit_pos, elementinit_pos, 0);

	if(is_nan(init_pos[0]) || is_nan(init_pos[1]) || is_nan(init_pos[2]) || is_nan(init_pos[3]))
	{
		init_pos[0] = 0.0;
		init_pos[1] = 0.0;
		init_pos[2] = 0.0;
		init_pos[3] = 0.0;
	}

	// Table of ephemeris
	for (i = 0; i < 32; i++){
		ephemjString = (jstring) (*env)->GetObjectArrayElement(env, ephemDataArray, i);
		ephemDataStringLine = (*env)->GetStringUTFChars(env, ephemjString,JNI_FALSE);
		if(ephemDataStringLine != '\0')
			strcpy(ephemDataString[i], ephemDataStringLine);
		else
			ephemDataString[i][0] = '\0';
		(*env)->ReleaseStringUTFChars(env,ephemjString , ephemDataStringLine);
	}

	double prn;
	int count_satdata = 0;
	// Table of PRN TOW PR and SNR
	for( i = 0; i < 19; i++){
		oneDim = (*env)->GetObjectArrayElement(env,sat_dataArray,i);
		elementSat_data = (*env)->GetDoubleArrayElements(env, oneDim,JNI_FALSE );
		prn = elementSat_data[0];
		// Ranging is availalble only for the EGNOS mode, so only GPS satellites are stored in the table
		if (prn < 38.0 && prn > 0.0)
		{
			for( j = 0; j < 4; j++)
			{
					sat_data[count_satdata][j] = elementSat_data[j];
			}
			count_satdata++;
		}
		(*env)->ReleaseDoubleArrayElements(env,oneDim,elementSat_data,0);
	}

	for(j = count_satdata; j < 19; j++){
		sat_data[j][0] = 0;
		sat_data[j][1] = 0;
		sat_data[j][2] = 0;
		sat_data[j][3] = 0;
	}

	int count_satdata_nu = 0;
	for( i = 0; i < 19; i++){
		oneDim = (*env)->GetObjectArrayElement(env,sat_data_notUsedArray,i);
		elementSat_data = (*env)->GetDoubleArrayElements(env, oneDim,JNI_FALSE );
		prn = elementSat_data[0];
		if (prn > 0)
		{
			for( j = 0; j < 4; j++)
			{
			  sat_data_notUsed[count_satdata_nu][j] = elementSat_data[j];
			}
			count_satdata_nu++;
		}
		(*env)->ReleaseDoubleArrayElements(env,oneDim,elementSat_data,0);
	}


	for(j = 0; j < 19; j++){
		eph_data[j][0] = '\0';
	}

	// Creates eph_data table of max 19 ephemeris, with PRN in the same order as sat_prn table
	double prn_eph = 0;
	for(j = 0; j < 19; j++){
		if (sat_data[j][0] != 0.0 && sat_data[j][0] <= 32){
			for(i = 0; i < 32; i++){
				if(ephemDataString[i][0] != '\0'){
					char char_prn[3] = "";
					strncpy(char_prn,ephemDataString[i]+1,2);
					char_prn[2] = '\0';
					prn_eph =  atof(char_prn);
					if(sat_data[j][0] == prn_eph && prn_eph != 0.0 ){
						strncpy(eph_data[j],ephemDataString[i]+3, 900);
						eph_data[j][900]='\0';
					}
				}

			}
		}
		else{
			eph_data[j][0]='\0';
		}
	}

	for(j = 0; j < 19; j++){
		eph_data_nu[j][0] = '\0';
	}

	// Creates eph_data_nu table of max 19 ephemeris, with PRN in the same order as sat_prn table
	// for unused satellites
	double prn_eph_nu = 0;
	for(j = 0; j < 19; j++){
		if (sat_data_notUsed[j][0] != 0.0 && sat_data_notUsed[j][0] <= 32){
			for(i = 0; i < 32; i++){
				if(ephemDataString[i][0] != '\0'){
					char char_prn[3] = "";
					strncpy(char_prn,ephemDataString[i]+1,2);
					char_prn[2] = '\0';
					prn_eph_nu =  atof(char_prn);
					if(sat_data_notUsed[j][0] == prn_eph_nu && prn_eph_nu != 0.0 ){
						strncpy(eph_data_nu[j],ephemDataString[i]+3, 900);
						eph_data_nu[j][900]='\0';
					}
				}

			}
		}
		else{
			eph_data_nu[j][0]='\0';
		}
	}

	// GPS position
	sat_count = positioning(vect, init_pos, DOP, PL, eph_data, sat_data, &msg1, msg2_5, &msg6,
			&msg7, &msg10, &msg12,&msg9, &msg17, m18_t, msg24_t, msg25_t, m26_t, m18_tString, 
			m26_tString, 0,&iono_flag, sat_array, S_t, utc_data, klob_data, rnd_options,
			sat_data_notUsed,eph_data_nu, S_t_notUsed);
	gpslatitude = get_latitude(vect);
	gpslongitude = get_longitude(vect);
	gpsaltitude = get_height(vect);

	HDOP = DOP[0];

	// Li   for  NMEA
	VDOP = DOP[1];
	PDOP = DOP[2];
	TDOP = DOP[3];
	// Li   for  NMEA

	HPL = PL[0];

	//android_syslog(ANDROID_LOG_INFO, "No of iterations: %f\n",sat_array[3]);
	android_syslog(ANDROID_LOG_INFO, "GPS position: %.20f %.20f %.20f %f\n",gpslatitude,
			gpslongitude,gpsaltitude,HDOP);

	jdouble temp[377];
	temp[0]= gpslatitude;
	temp[1]= gpslongitude;
	temp[2]= gpsaltitude;
	temp[3]=init_pos[0];
	temp[4]=init_pos[1];
	temp[5]=init_pos[2];
	temp[6]=init_pos[3];
	temp[7]= HDOP;

	temp[8]   = sat_array[0]; // total no. of sats.
	temp[9]   = sat_array[1]; // low elev. sats.
	temp[10]  = sat_count;    // sats used
	temp[11]  = sat_array[3]; // no. of iterations
	temp[12]  = sat_array[4]; // set to 1 if jump in position occured
	temp[13]  = sat_array[5]; // jump distance on x
	temp[14]  = sat_array[6]; // jump distance on y
	temp[15]  = S_t[0].tow;
	temp[16]  = (double)S_t[0].weeknb;
	
	for(k = 0; k < sat_array[0]; k++)
	{
		temp[17 + k*2]      = S_t[k].prn;
		temp[17 + k*2 + 1]  = S_t[k].use;
	}

	// Li   for  NMEA
	temp[55] = VDOP;
	temp[56] = PDOP;
	temp[57] = TDOP;

	for(k = 0; k < sat_array[0]; k++)
	{
		temp[58 + k*6] = S_t[k].weeknb;
		temp[59 + k*6] = S_t[k].toe;
		temp[60 + k*6] = S_t[k].az;
		temp[61 + k*6] = S_t[k].cn0;
		temp[62 + k*6] = S_t[k].el;
		temp[63 + k*6] = S_t[k].tow;
	}
	// Li   for  NMEA



	// Li   for  RTCM
	temp[185] = sat_count;
	temp[186] = S_t[0].tow2;

	// RTCM Message 1
	int count_use=0;
	for(k = 0; k < sat_array[0]; k++)
		if(S_t[k].use !=0)
		{
			temp[187 + count_use*4] = S_t[k].prn;
			temp[188 + count_use*4] = S_t[k].pr_c-S_t[k].pr;
			temp[189 + count_use*4] = S_t[k].rrc;
			temp[190 + count_use*4] = S_t[k].iodc;

			temp[317 + count_use*4] = S_t[k].pr;
			temp[318 + count_use*4] = S_t[k].pos_x;
			temp[319 + count_use*4] = S_t[k].pos_y;
			temp[320 + count_use*4] = S_t[k].pos_z;

			count_use=count_use+1;
		}
	// Li   for  RTCM

	temp[241] = count_satdata_nu;
	int count_nu =0;
    for(k = 0; k < count_satdata_nu;k++){
    	temp[242 + count_nu*4] = S_t_notUsed[k].prn;
    	temp[243 + count_nu*4] = S_t_notUsed[k].az;
    	temp[244 + count_nu*4] = S_t_notUsed[k].el;
    	temp[245 + count_nu*4] = S_t_notUsed[k].cn0;
    	android_syslog(ANDROID_LOG_INFO, "Coordinates | prn: %f, az: %f, el: %f\n",
    			S_t_notUsed[k].prn,S_t_notUsed[k].az,S_t_notUsed[k].el );
    }




	(*env)->SetDoubleArrayRegion( env, coordinates, 0, 377,temp);
	//removed this since it is not required for API level 14 or above,
	//but required for API level below 14.
	//(*env)->DeleteLocalRef(env,coordinates);
	return coordinates;
}

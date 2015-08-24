/**
 * @file Ephemeris.c
 *
 * @brief Ephemeris module source file containing the parsing functions of the
 * GPS navigation data subframes number 1,2 and 3.
 * @details The module decodes the ephemerides and the clock corrections
 * parameters from the subframes 1,2 and 3 of the broadcasted GPS navigation
 * data for a given satellite according to the IS-GPS-200E section 20.3.2
 * Message Structure.
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
 *
 */
#include "Ephemeris.h"
#include <android/log.h>

/**
 * decode_msg function.
 * The function decodes the ephemerides and the clock corrections parameters
 * from the 3 subframes of the GPS navigation data and updates the
 * Satellite structure, identified by its pointer.
 * @param Sat The Satellite object
 */
void decode_msg(Satellite * Sat)
{
	char *subfr1 = "";
	char *subfr2 = "";
	char *subfr3 = "";

	// The separation of the subframe is common way to proceed
	// UBlox and Sirf receivers split the subframes

	ReadSubfr1(Sat,subfr1);//android_syslog(ANDROID_LOG_INFO,"C: subfr1: %s\n", (*Sat).subfr1);
	ReadSubfr2(Sat,subfr2);//android_syslog(ANDROID_LOG_INFO,"C: subfr2: %s\n", (*Sat).subfr2);
	ReadSubfr3(Sat,subfr3);//android_syslog(ANDROID_LOG_INFO,"C: subfr3: %s\n", (*Sat).subfr3);
}

/**
 * ReadSubfr1 function.
 * The function decodes the 1st subframe of the GPS navigation data
 * according to the IS-GPS-200E section 20.3.3.3.1 Subframe 1 content and
 * updates the Satellite structure, identified by its pointer.
 * @param *Sat  The pointer of the Satellite structure
 * @param *data The pointer of the 300 bits
 */
void ReadSubfr1(Satellite * Sat, char * data)
{
/*	form the subframe
 * 	data is a table of 10 words, to form the subframe:
		- conversion to binary of each words
		- add the 6 bits parity at the beginning of the words,  for each words (if there arent there already)
		- concatenation of the 10 words
		- if TLM and/or HOW are deleted, add the 32/64 bits
*/
	(*Sat).weeknb = get_weeknb((*Sat).subfr1);
	(*Sat).cl2 = get_cl2((*Sat).subfr1);
	(*Sat).ura = get_ura((*Sat).subfr1);
	(*Sat).health = get_health((*Sat).subfr1);
	(*Sat).iodc = get_iodc((*Sat).subfr1);
	(*Sat).tgd = get_tgd((*Sat).subfr1);
	(*Sat).toc = get_toc((*Sat).subfr1);
	(*Sat).af0 = get_af0((*Sat).subfr1);
	(*Sat).af1 = get_af1((*Sat).subfr1);
	(*Sat).af2 = get_af2((*Sat).subfr1);
}

/**
 * ReadSubfr2 function
 * The function decodes the 2nd subframe of the GPS navigation data
 * according to the IS-GPS-200E section IS-GPS-200E : 20.3.3.4.1 Content
 * of Subframes 2 and 3 and updates the Satellite structure,
 * identified by its pointer.
 * @param *Sat  The pointer of the Satellite structure
 * @param *data The pointer of the 300 bits
 */
void ReadSubfr2(Satellite * Sat, char * data)
{
	(*Sat).iode_s2 = get_iode_s2((*Sat).subfr2);
	(*Sat).crs = get_crs((*Sat).subfr2);
	(*Sat).delta_n = get_delta_n((*Sat).subfr2);
	(*Sat).m0 = get_m0((*Sat).subfr2);
	(*Sat).cuc = get_cuc((*Sat).subfr2);
	(*Sat).e = get_e((*Sat).subfr2);
	(*Sat).cus = get_cus((*Sat).subfr2);
	(*Sat).sqrta = get_sqrta((*Sat).subfr2);
	(*Sat).toe = get_toe((*Sat).subfr2);
	(*Sat).ado = get_ado((*Sat).subfr2);
}

/**
 * ReadSubfr3 function
 * The function decodes the 3rd subframe of the GPS navigation data
 * according to the IS-GPS-200E section IS-GPS-200E : 20.3.3.4.1 Content
 * of Subframes 2 and 3 and updates the Satellite structure,
 * identified by its pointer.
 * @param *Sat  The pointer of the Satellite structure
 * @param *data The pointer of the 300 bits
 */
void ReadSubfr3(Satellite * Sat, char * data)
{
	(*Sat).cic = get_cic((*Sat).subfr3);
	(*Sat).cis = get_cis((*Sat).subfr3);
	(*Sat).crc = get_crc((*Sat).subfr3);
	(*Sat).w = get_w((*Sat).subfr3);
	(*Sat).omega0 = get_omega0((*Sat).subfr3);
	(*Sat).omegadot = get_omegadot((*Sat).subfr3);
	(*Sat).i0 = get_i0((*Sat).subfr3);
	(*Sat).idot = get_idot((*Sat).subfr3);
	(*Sat).iode_s3 = get_iode_s3((*Sat).subfr3);
}

// SUBFRAME 1

/**
 * get_tow function
 * @param  	*data		Pointer of the 1st subframe
 * @return  Value of the Time of week, in seconds
 */
int get_tow(char * data)
{
	char tmp[18];
	extract(data,30,46,tmp);
	return bin2dec(tmp)*6;				// *4*1.5 IS-GPS-200E : Figure 3-16. Time Line Relationship of HOW Message
}

/**
 * get_subfrID function

 * @param  	*data		Pointer of the navigation data
 * @return 	ID of the subframe
 */
unsigned short get_subfrID(char * data)
{
	char tmp[4];
	extract(data,49,51,tmp);
	return bin2dec(tmp);
}

/**
 * get_weeknb function

 * @param  	*data		Pointer of the 1st subframe
 * @return  Value of week number, in week
 */
unsigned short get_weeknb(char * data)
{
	char tmp[11];
	extract(data,60,69,tmp);

	return bin2dec(tmp)+ 1024;			// 10 bits %1024 IS-GPS-200E 20.3.3.3.1.1 : Transmission Week Number
}

/**
 * get_cl2 function

 * @param  	*data		Pointer of the 1st subframe
 * @return  Value of the code on L2, no units
 */
unsigned short get_cl2(char * data)
{
	char tmp[3];
	extract(data,70,71,tmp);
	return bin2dec(tmp);
}

/**
 * get_ura function

 * @param  	*data		Pointer of the 1st subframe
 * @return  Value of the SV range accuracy, in week
 */
unsigned short get_ura(char * data)
{
	char tmp[5];
	extract(data,72,75,tmp);
	return bin2dec(tmp);
}

/**
 * get_health function

 * @param  	*data		Pointer of the 1st subframe
 * @return  Value of the SV Health, no units
 */
unsigned short get_health(char * data)
{
	char tmp[7];
	extract(data,76,81,tmp);
	return bin2dec(tmp);
}

/**
 * get_iodc function

 * @param  	*data		Pointer of the 1st subframe
 * @return  Value of the Issue of Data, Clock, no units
 */
unsigned short get_iodc(char * data)
{
	char msb[3], lsb[9];
	extract(data,82,83,msb);
	extract(data,210,217,lsb);
	return ((int)bin2dec(msb)<<8) | (int)bin2dec(lsb);	//MSBs and LSBs concatenation
}

/**
 * get_tgd function

 * @param  	*data		Pointer of the 1st subframe
 * @return  Value of the Estimated Group Delay Differential, seconds
 */
double get_tgd(char * data)
{
	char tmp[9];
	extract(data,196,203,tmp);
	int tgd = (int)bin2dec(tmp);
	if(tgd > (128 - 1))									// signed conversion (8 bits)
		tgd -= 256;
	return tgd * TWO_POWER_m31;							// scale factor
}

/**
 * get_toc function

 * @param  	*data		Pointer of the 1st subframe
 * @return  Value of the Reference time clock, seconds
 */
double get_toc(char * data)
{
	char tmp[17];
	extract(data,218,233,tmp);
	return bin2dec(tmp) * 16;								// scale factor
}

/**
 * get_af2 function

 * @param  	*data		Pointer of the 1st subframe
 * @return  Value of the Polynomial clock correction coefficient af2, units (s/s^2)
 */
double get_af2(char * data)
{
	char tmp[9];
	extract(data,240,247,tmp);
	int af2 = (int)bin2dec(tmp);
	if(af2 > (128 - 1))										// signed conversion
		af2 -= 256;
	return af2 * TWO_POWER_m55;								// scale factor
}

/**
 * get_af1 function

 * @param  	*data		Pointer of the 1st subframe
 * @return  Value of the Polynomial clock correction coefficient af1, units (s/s)
 */
double get_af1(char * data)
{
	char tmp[17];
	extract(data,248,263,tmp);
	int af1 = (int)bin2dec(tmp);
	if(af1 > (TWO_POWER_15 - 1))							// signed conversion (16 bits)
		af1 -= TWO_POWER_16;
	return af1 * TWO_POWER_m43;								// scale factor
}

/**
 * get_af0 function

 * @param  	*data		Pointer of the 1st subframe
 * @return  Value of the Polynomial clock correction coefficient af0, seconds
 */
double get_af0(char * data)
{
	char tmp[23];
	extract(data,270,291,tmp);
	int af0 = (int)bin2dec(tmp);
	if(af0 > (TWO_POWER_21 - 1))							// signed conversion (22 bits)
		af0 -= TWO_POWER_22;
	return af0 * TWO_POWER_m31;								// scale factor
}

// SUBFRAME 2

/**
 * get_iode_s2 function

 * @param  	*data		Pointer of the 2nd subframe
 * @return  Value of the Issue of Data, Ephemeris -on subframe 2, no units
 */
unsigned short get_iode_s2(char * data)
{
	char tmp[9];
	extract(data,60,67,tmp);
	return bin2dec(tmp);
}

/**
 * get_crs function

 * @param  	*data		Pointer of the 2nd subframe
 * @return  Value of the Amplitude of the Sine Harmonic Correction Term to the Orbit Radius, meters
 */
double get_crs(char * data)
{
	char tmp[17];
	extract(data,68,83,tmp);
	int crs = (int)bin2dec(tmp);
	if(crs > (TWO_POWER_15 - 1))							// signed conversion (16 bits)
		crs -= TWO_POWER_16;
	return crs * TWO_POWER_m5;								// scale factor
}

/**
 * get_delta_n function

 * @param  	*data		Pointer of the 2nd subframe
 * @return  Value of the Mean Motion Difference From Computed Value, radians/sec
 */
double get_delta_n(char * data)
{
	char tmp[17];
	extract(data,90,105,tmp);
	int delta_n = (int)bin2dec(tmp);
	if(delta_n > (TWO_POWER_15 - 1))							// signed conversion (16 bits)
		delta_n -= TWO_POWER_16;
	return delta_n*TWO_POWER_m43*PI;								// scale factor and conversion semi-circle/s to rad/s
}

/**
 * get_m0 function

 * @param  	*data		Pointer of the 2nd subframe
 * @return  Value of the Mean Anomaly at Reference Time, radians
 */
double get_m0(char * data)
{
	char msb[9], lsb[25], tmp[33]="";
	extract(data,106,113,msb);									//MSBs and LSBs concatenation
	strcat(tmp,msb);
	extract(data,120,143,lsb);
	strcat(tmp,lsb);

       #ifdef Linux_H_
	 long long m0 = bin2dec(tmp);
       #else
	 __int64 m0 = bin2dec(tmp);
       #endif

	if(m0 > (TWO_POWER_31 - 1))									// signed conversion (32 bits)
			m0 -= TWO_POWER_32;
	return m0 * TWO_POWER_m31 * PI;								// scale factor and conversion semi-circle to rad
}

/**
 * get_cuc function

 * @param  	*data		Pointer of the 2nd subframe
 * @return  Value of the Amplitude of the Cosine Harmonic Correction Term to the Argument of Latitude, radians
 */
double get_cuc(char * data)
{
	char tmp[17];
	extract(data,150,165,tmp);
	int cuc = (int)bin2dec(tmp);
	if(cuc > (TWO_POWER_15 - 1))							// signed conversion (16 bits)
		cuc -= TWO_POWER_16;
	return cuc * TWO_POWER_m29;								// scale factor
}

/**
 * get_e function

 * @param  	*data		Pointer of the 2nd subframe
 * @return  Value of the Eccentricity, no units
 */
double get_e(char * data)
{
	char msb[9], lsb[25], tmp[33]="";
	extract(data,166,173,msb);								//MSBs and LSBs concatenation
	strcat(tmp,msb);
	extract(data,180,203,lsb);
	strcat(tmp,lsb);
     #ifdef Linux_H_
	 long long e = bin2dec(tmp);
     #else
	 __int64 e = bin2dec(tmp);
     #endif

	return e * TWO_POWER_m33;								// scale factor
}

/**
 * get_cus function

 * @param   *data		Pointer of the 2nd subframe
 * @return  Value of the Amplitude of the Sine Harmonic Correction Term to the Argument of Latitude, radians
 */
double get_cus(char * data)
{
	char tmp[17];
	extract(data,210,225,tmp);
	int cus = (int)bin2dec(tmp);
	if(cus > (TWO_POWER_15 - 1))							// signed conversion (16 bits)
		cus -= TWO_POWER_16;
	return cus * TWO_POWER_m29;								// scale factor
}

/**
 * get_sqrta function

 * @param  	*data		Pointer of the 2nd subframe
 * @return  Value of the Square Root of the Semi-Major Axis, meters^(1/2)
 */
double get_sqrta(char * data)
{
	char msb[9], lsb[25], tmp[33]="";
	extract(data,226,233,msb);								//MSBs and LSBs concatenation
	strcat(tmp,msb);
	extract(data,240,263,lsb);
	strcat(tmp,lsb);
       #ifdef Linux_H_
	 long long sqrta = bin2dec(tmp);
        #else
	 __int64 sqrta = bin2dec(tmp);
        #endif

	return sqrta * TWO_POWER_m19;							// scale factor
}

/**
 * get_toe function

 * @param  	*data		Pointer of the 2nd subframe
 * @return  Value of the Reference time ephemeris, seconds
 */
int get_toe(char * data)
{
	char tmp[17];
	extract(data,270,285,tmp);
	return bin2dec(tmp) * 16;								// scale factor
}

/**
 * get_ado function

 * @param  	*data		Pointer of the 2nd subframe
 * @return  Value of the Age of data offset, seconds
 */
unsigned short get_ado(char * data)
{
	char tmp[6];
	extract(data,287,291,tmp);
	return bin2dec(tmp) * 900;		// scale factor
}

// SUBFRAME 3

/**
 * get_cic function

 * @param  	*data		Pointer of the 3rd subframe
 * @return  Value of the Amplitude of the Cosine Harmonic Correction Term to the Angle of Inclination, radians
 */
double get_cic(char * data)
{
	char tmp[17];
	extract(data,60,75,tmp);
	int cic = (int)bin2dec(tmp);
	if(cic > (TWO_POWER_15 - 1))							// signed conversion (16 bits)
		cic -= TWO_POWER_16;
	return cic * TWO_POWER_m29;								// scale factor
}

/**
 * get_omega0 function

 * @param  	*data		Pointer of the 3rd subframe
 * @return  Value of the Longitude of Ascending Node of Orbit Plane at Weekly Epoch, radians
 */
double get_omega0(char * data)
{
	char msb[9], lsb[25], tmp[33]="";
	extract(data,76,83,msb);
	strcat(tmp,msb);
	extract(data,90,113,lsb);
	strcat(tmp,lsb);

     #ifdef Linux_H_
	 long long omega0 = bin2dec(tmp);
    #else
	 __int64 omega0 = bin2dec(tmp);       //MSBs and LSBs concatenation
    #endif


	if(omega0 > (TWO_POWER_31 - 1))								// signed conversion (32 bits)
		omega0 -= TWO_POWER_32;
	return  omega0 * TWO_POWER_m31 * PI;						// scale factor and conversion semi-circle to rad
}

/**
 * get_cis function

 * @param  	*data		Pointer of the 3rd subframe
 * @return  Value of the Amplitude of the Sine Harmonic Correction Term to the Angle of Inclination, radians
 */
double get_cis(char * data)
{
	char tmp[17];
	extract(data,120,135,tmp);
	int cis = (int)bin2dec(tmp);
	if(cis > (TWO_POWER_15 - 1))							// signed conversion (16 bits)
		cis -= TWO_POWER_16;
	return cis * TWO_POWER_m29;								// scale factor
}

/**
 * get_i0 function

 * @param  	*data		Pointer of the 3rd subframe
 * @return  Value of the Inclination Angle at Reference Time, radians
 */
double get_i0(char * data)
{
	char msb[9], lsb[25], tmp[33]="";
	extract(data,136,143,msb);								//MSBs and LSBs concatenation
	strcat(tmp,msb);
	extract(data,150,173,lsb);
	strcat(tmp,lsb);

        #ifdef Linux_H_
	  long long i0 = bin2dec(tmp);
        #else
	 __int64 i0 = bin2dec(tmp);       // signed conversion (32 bits)
        #endif

	if(i0 > (TWO_POWER_31 - 1))
		i0 -= TWO_POWER_32;
	return  i0 * TWO_POWER_m31 * PI;						// scale factor and conversion semi-circle to rad
}

/**
 * get_crc function

 * @param  	*data		Pointer of the 3rd subframe
 * @return  Value of the Amplitude of the Cosine Harmonic Correction Term to the Orbit Radius, meters
 */
double get_crc(char * data)
{
	char tmp[17];
	extract(data,180,195,tmp);
	int crc = (int)bin2dec(tmp);
	if(crc > (TWO_POWER_15 - 1))							// signed conversion (16 bits)
		crc -= TWO_POWER_16;
	return crc * TWO_POWER_m5;								// scale factor
}

/**
 * get_w function

 * @param  	*data		Pointer of the 3rd subframe
 * @return  Value of the Argument of Perigee, radians
 */
double get_w(char * data)
{
	char msb[9], lsb[25], tmp[33]="";
	extract(data,196,203,msb);								 //MSBs and LSBs concatenation
	strcat(tmp,msb);
	extract(data,210,233,lsb);
	strcat(tmp,lsb);

        #ifdef Linux_H_
	  long long w = bin2dec(tmp);
       #else
	 __int64 w = bin2dec(tmp);       // signed conversion (32 bits)
       #endif


	if(w > (TWO_POWER_31 - 1))
		w -= TWO_POWER_32;
	return  w * TWO_POWER_m31 * PI;						// scale factor and conversion semi-circle to rad
}

/**
 * get_omegadot function

 * @param  	*data		Pointer of the 3rd subframe
 * @return  Value of the Rate of Right Ascension, radians/s
 */
double get_omegadot(char * data)
{
	char tmp[25];
	extract(data,240,263,tmp);
	int omegadot = (int)bin2dec(tmp);
	if(omegadot > (TWO_POWER_23 - 1))						// signed conversion (24 bits)
		omegadot -= TWO_POWER_24;
	return  omegadot * TWO_POWER_m43 * PI;					// scale factor and conversion semi-circle/s to rad/s
}

/**
 * get_iode_s3 function

 * @param  	*data		Pointer of the 3rd subframe
 * @return  Value of the Issue of Data, Ephemeris -on subframe 3, no units
 */
unsigned short get_iode_s3(char * data)
{
	char tmp[9];
	extract(data,270,277,tmp);
	return bin2dec(tmp);
}

/**
 * get_idot function

 * @param  	*data		Pointer of the 3rd subframe
 * @return  Value of the Rate of Inclination Angle, radians/s
 */
double get_idot(char * data)
{
	char tmp[15];
	extract(data,278,291,tmp);
	int idot = (int)bin2dec(tmp);
	if(idot > (TWO_POWER_13 - 1))							// signed conversion (14 bits)
		idot -= TWO_POWER_14;
	return  idot * TWO_POWER_m43 * PI;						// scale factor and conversion semi-circle/s to rad/s
}

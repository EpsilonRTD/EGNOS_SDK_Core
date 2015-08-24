/**
 * @file Positioning.c
 *
 * @brief Positioning module source file containing the positioning process
 * functions.
 * @details This module is the GPS/EGNOS positioning process of the software.
 * This module builds a 19-channel GPS positioning system enhanced by EGNOS
 * corrections if enabled. The EGNOS corrections are computed from Signal In
 * Space or from the ESA SISNeT server. See the EGNOS module to obtain more
 * information on EGNOS corrections. The module computes the EGNOS Protection
 * Levels. The horizontal protection levels (HPL) provided by this application
 * are calculated according to SBAS RTCA MOPS DO229 standards, which are based
 * on hypothesis applicable for aeronautical environments. They are provided as
 * general integrity indicators but their values cannot be directly
 * extrapolated to other environments as terrestrial or maritime.
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

#include "Positioning.h"
#include <math.h>

/**
 * get_corrected_time function
 * Check the quantity t
 * (IS-GPS-200E : 20.3.3.3.3.1 User Algorithm for SV Clock Correction)
 * @param   t Time to be corrected
 * @return The corrected time
 */
double get_corrected_time(double t)
{
	if(t>(GPSWEEK_IN_SEC/2))
		t = t-GPSWEEK_IN_SEC;
	else{
		if(t<(-GPSWEEK_IN_SEC/2))
			t = t+GPSWEEK_IN_SEC;
	}
	return t;
}

/**
 * get_dtsv function
 * Calculation of SV PRN code phase time offset delta_sv without relativistic term dtr
 * (IS-GPS-200E : 20.3.3.3.3.1 User Algorithm for SV Clock Correction)
 * @param t   The corrected gps time
 * @param af0 Polynomial clock correction coefficient af0
 * @param af1 Polynomial clock correction coefficient af1
 * @param af2 Polynomial clock correction coefficient af2
 * @return The SV PRN code phase time offset
 */
double get_dtsv (double t, double af0, double af1, double af2)
{
	return (af0 + af1*t + af2*t*t);
}

/**
 * get_dtr function
 * Calculation of the relativistic correction term delta_tr
 * (IS-GPS-200E : 20.3.3.3.3.1 User Algorithm for SV Clock Correction)
 * @param sqrta Square Root of the Semi-Major Axis
 * @param Ek    Eccentric Anomaly
 * @param e     Eccentricity
 * @return The relativistic correction term
 */
double get_dtr (double sqrta, double Ek, double e)
{
	return F_CONST * e * sqrta * sin(Ek);
}

//INS
/*
 * CHECK_T repairs over- and underflow of GPS time
 * Written by Kai Borre
 * April 1, 1996
 */
double check_t(t) {
	double half_week = 302400;
	double tt = t;
	if (t >  half_week)
		tt = t-2*half_week;
	if (t < -half_week)
		tt = t+2*half_week;
	return tt;
}
//INS - End

/**
 * SV_position_computation function
 * Calculation of the SV position and time corrections (IS-GPS-200E : 20.3.3.4.3 User Algorithm for Ephemeris Determination)
 * @param *Sat   Pointer of the Satellite
 * @param egnos  The EGNOS flag (1:EGNOS enabled, 0:EGNOS disabled)
 */
void SV_position_computation(Satellite * Sat, int egnos)
{
	double t,tk,a,n,Mk,Ek,Ei,nuk,phik,duk,dik,drk,uk,rk,ik,xkp,ykp,omegak,ddtsv;
	double e = (*Sat).e;
	double t_correction = 0;
	int i;

	// ENT
	t = (*Sat).tow2;

	// init EGNOS clock correction
	ddtsv = 0;

	// SV PRN code phase time offset
	t_correction = get_dtsv(get_corrected_time(t - (*Sat).toc), (*Sat).af0, (*Sat).af1, (*Sat).af2) - (*Sat).tgd;

	if(egnos == 1)
	{
		// Egnos clock correction
		ddtsv = (*Sat).daf0 + (*Sat).daf1 * (t - (*Sat).t0);
		t_correction = t_correction + ddtsv;
	}

	t = t - (t_correction);

	tk = get_corrected_time(t - (*Sat).toe); // Correction of tk (time from
	// ephemeris epoch)
	a = ((*Sat).sqrta) * ((*Sat).sqrta); // Semi-major axis

	n = sqrt(MU_EARTH / (a * a * a)) + (*Sat).delta_n; // Corrected mean motion
	Mk = (*Sat).m0 + n * tk; // Mean anomaly

	Ek = Mk;
	Ei = 0;

	for (i = 0; i < 10; i++) // Kepler's Equation for
	{ // Eccentric Anomaly    // Iterations
		Ei = Ek;
		Ek = Mk + e * sin(Ei);
	}

	nuk = atan2((sqrt(1 - e * e) * sin(Ek)), (cos(Ek) - e)); // True Anomaly
	phik = nuk + (*Sat).w; // Argument of Latitude

	duk = (*Sat).cuc * cos(2 * phik) + (*Sat).cus * sin(2 * phik); // Argument of Latitude Correction
	drk = (*Sat).crc * cos(2 * phik) + (*Sat).crs * sin(2 * phik); // Radius Correction
	dik = (*Sat).cic * cos(2 * phik) + (*Sat).cis * sin(2 * phik); // Inclination Correction

	uk = phik + duk; // Corrected Argument of Latitude
	rk = a * (1 - e * cos(Ek)) + drk; // Corrected Radius
	ik = (*Sat).i0 + dik + ((*Sat).idot) * tk; // Corrected Inclination

	xkp = rk * cos(uk); // Positions in orbital plane
	ykp = rk * sin(uk);

	// Corrected longitude of ascending node
	omegak = (*Sat).omega0 + ((*Sat).omegadot - OMEGA_DOT_EARTH)*tk - OMEGA_DOT_EARTH*(*Sat).toe;

	(*Sat).pos_x = xkp * cos(omegak) - ykp * cos(ik) * sin(omegak); // Earth-fixed coordinates
	(*Sat).pos_y = xkp * sin(omegak) + ykp * cos(ik) * cos(omegak);
	(*Sat).pos_z = ykp * sin(ik);

	t_correction = t_correction + get_dtr((*Sat).sqrta, Ek, e);
	(*Sat).t_correction = t_correction;

	//INS - Start
	double A = (*Sat).sqrta * (*Sat).sqrta;
	double n0 = sqrt(MU_EARTH/A*A*A);
	double nUpdated = n0+ (*Sat).delta_n;
	double txRaw =(*Sat).tow - (*Sat).pr / SPEED_OF_LIGHT;

	double dt = check_t((txRaw-(*Sat).toc));
	//double tIns = txRaw - ((*Sat).af2*dt+(*Sat).af1*dt+(*Sat).af0*(*Sat).tgd); //tx_GPS
	//(*Sat).tCorr = tIns;
	//double tk = check_t(tIns-(*Sat).toe);
	double M = (*Sat).m0+nUpdated*tk;

	//M = rem(M + 2*PI, 2*PI);
	M = (M + 2*PI) - floor((M+2*PI)/2*PI);
	double E = M;

	for (i = 0; i < 9; i++) { //i = 1:10
	   double  E_old = E;
	   E = M + (*Sat).e * sin(E);
	  // double dE = rem(E-E_old,2*PI);
	   double dE= (E- E_old) - floor((E-E_old)/2*PI);
	   if (abs(dE) < 1.e-12)
	        break;
	}

	//E = rem(E + 2*PI, 2*PI);
	M = (M + 2*PI) - floor((M+2*PI)/2*PI);

	double v = atan2(sqrt(1-pow((*Sat).e, 2)) * sin(E), cos(E)-(*Sat).e);
	double phi = v+(*Sat).w;
	//phi = rem(phi, 2*PI);
	phi = phi - floor(phi/2*PI);
	double u = phi + (*Sat).cuc * cos(2*phi) + (*Sat).cus*sin(2*phi);
	double r = A*(1-(*Sat).e * cos(E)) + (*Sat).crc * cos(2*phi) + (*Sat).crs*sin(2*phi);
	double j = (*Sat).i0 + (*Sat).idot * tk + (*Sat).cic * cos(2*phi) + (*Sat).cis * sin(2*phi); //i

	double Omega = (*Sat).omega0 + ((*Sat).omegadot - OMEGA_DOT_EARTH) * tk - (*Sat).omegadot * (*Sat).toe;
	//Omega = rem(Omega+2*PI, 2*PI);
	Omega=(Omega + 2*PI) - floor((Omega+2*PI)/2*PI);

	double x1 = cos(u)*r;
	double y1 = sin(u)*r;

	(*Sat).sat_pos_x = x1 * cos(Omega) - y1 * cos(j) * sin(Omega);
	(*Sat).sat_pos_y = x1 * sin(Omega) + y1 *cos(j) * cos(Omega);
	(*Sat).sat_pos_z = y1*sin(j);
	(*Sat).rel_corr = F_CONST * (*Sat).e * sqrt(A) *sin(E);

	//INS - End

	if (egnos == 1) {
		// EGNOS Long term correction correction
		(*Sat).pos_x += (*Sat).dx + (*Sat).ddx * (t - (*Sat).t0);
		(*Sat).pos_y += (*Sat).dy + (*Sat).ddy * (t - (*Sat).t0);
		(*Sat).pos_z += (*Sat).dz + (*Sat).ddz * (t - (*Sat).t0);
	}
}

/**compute_EGNOSsat_position
 * Calculation of the EGNOS satellites position
 * @param *Sat   Pointer of the Satellite
 * @param egnos  The EGNOS flag (1:EGNOS enabled, 0:EGNOS disabled)
 */
void compute_EGNOSsat_position(Satellite * Sat, Egnos_msg * msg9) {
	double tg, dtg, tk;
	//Time of the GEO satellite.
	tg = (*Sat).tow - (*Sat).pr / SPEED_OF_LIGHT;

	//SBAS pseudorange correction.
	dtg = (*msg9).geo_nav[11] + (*msg9).geo_nav[12]*((*Sat).tow - (*msg9).geo_nav[0]);

	//tk corrected.
	tk = tg - dtg;

	//Pseudorange corrected of time.
	(*Sat).pos_x = (*msg9).geo_nav[2] + (*msg9).geo_nav[5]* (tk - (*msg9).geo_nav[0]) + (1/2)*((*msg9).geo_nav[8]*(tk - (*msg9).geo_nav[0])* (tk - (*msg9).geo_nav[0]));
	(*Sat).pos_y = (*msg9).geo_nav[3] + (*msg9).geo_nav[6]* (tk - (*msg9).geo_nav[0]) + (1/2)*((*msg9).geo_nav[9]*(tk - (*msg9).geo_nav[0])* (tk - (*msg9).geo_nav[0]));
	(*Sat).pos_z = (*msg9).geo_nav[4] + (*msg9).geo_nav[7]* (tk - (*msg9).geo_nav[0]) + (1/2)*((*msg9).geo_nav[10]*(tk - (*msg9).geo_nav[0])* (tk - (*msg9).geo_nav[0]));
}

/**
 * SV_position_correction function
 * ECEF to ECI convertion
 * (20.3.3.4.3.3.2 Earth-Centered, Inertial (ECI) Coordinate System)
 * @param    *Sat      Pointer of the Satellite
 * @param  travel_time   Time travel
 */
void SV_position_correction(Satellite * Sat, double travel_time) {
	double pos[3], tmp[3];
	// travel_time = (*Sat).pr_c/SPEED_OF_LIGHT;

	pos[0] = (*Sat).pos_x;
	pos[1] = (*Sat).pos_y;
	pos[2] = (*Sat).pos_z;

	tmp[0] = cos(OMEGA_DOT_EARTH*travel_time)*pos[0]+sin(OMEGA_DOT_EARTH*travel_time)*pos[1];
	tmp[1] = -sin(OMEGA_DOT_EARTH*travel_time)*pos[0]+cos(OMEGA_DOT_EARTH*travel_time)*pos[1];
	tmp[2] = pos[2];

	(*Sat).pos_x = tmp[0];
	(*Sat).pos_y = tmp[1];
	(*Sat).pos_z = tmp[2];
}

/**
 * positioning function
 * The function starts the 19-satellites-channel processing and computes the navigation solution.
 * @param pos        	The positions destination table (lat.,long.,alt.)
 * @param X_est      	The estimated positions table, also used for the initial positions and equal to [0,0,0,0 ]
 *                   	if there are no estimations yet
 * @param DOP        	The Dilution Of Precision destination table (DOP[0]:HDOP DOP[1]:VDOP DOP[2]:PDOP DOP[3]:TDOP)
 * @param PL         	The Protection Level destination table (PL[0]:HPL PL[1]:VPL)
 * @param eph_data   	The ephemeris data table (15 lines max of 900 bits:300 bits for each subframe)
 * @param sat_data   	The PRN,TOW,PR,SNR table (15 lines max)
 * @param *msg1      	The Egnos message type 1 pointer
 * @param msg2_5     	The Egnos messages type 2-5 table
 * @param *msg6      	The Egnos message type 6 pointer
 * @param *msg7      	The Egnos message type 7 pointer
 * @param *msg9      	The Egnos message type 9 pointer
 * @param *msg10     	The Egnos message type 10 pointer
 * @param *msg12     	The Egnos message type 12 pointer
 * @param *msg17     	The Egnos message type 17 pointer
 * @param m18_t      	The Egnos messages type 18 table
 * @param msg24_t    	The Egnos messages type 24 table
 * @param msg25_t    	The Egnos messages type 25 table
 * @param m26_t      	The Egnos messages type 26 table
 * @param m18_char   	The table of 5(max) messages 18 in 256 characters (for SIS)
 * @param m26_char   	The table of 25(max) messages 26 in 256 characters (for SIS)
 * @param egnos      	The EGNOS flag (1:EGNOS enabled, 0:EGNOS disabled)
 * @param *iono_flag 	The pointer of the flag to check if all needed messages for ionospheric correction are available
 * @param sat_array  	[0] = total no. of satellites in view
 *                   	[1] = No. of Satellites eliminated for Low Elevation (under 10 deg.)
 *                   	[2] = No. of Sats not set in PRN mask (currently 1 and 25)
 *                  	[3] = No. of iterations [15] = 1 if position jump is more than 1.5m
 *                  	[4] = position jump on x-axis [5] = position jump on y-axis
 *                   	[6] = The number of satellites used to compute the position
 * @param  S_t       	Array of struct Satellite.
 * @param utc_data	  	The array of UTC parameters
 * @param klob_data	  	Array that holds the Klobuchar coefficients for ionopsheric corrections
 * @param rnd_options	Array that holds the R&D options
 * @return          	The number of satellites used to compute the position
 */
int positioning(double pos[3], double X_est[4], double DOP[4], double PL[2],
		char eph_data[19][901], double sat_data[19][4], Egnos_msg * msg1,
		Egnos_msg msg2_5[4][2], Egnos_msg * msg6, Egnos_msg * msg7,
		Egnos_msg * msg10, Egnos_msg * msg12, Egnos_msg * msg9,
		Egnos_msg * msg17, Egnos_msg m18_t[11], Egnos_msg msg24_t[25],
		Egnos_msg msg25_t[15], Egnos_msg m26_t[25], char m18_char[5][263],
		char m26_char[25][263], int egnos, int * iono_flag, double sat_array[15],
		Satellite S_t[19], double utc_data[9], double klob_data[9], int rnd_options[8],
		double sat_data_NotUsed[19][4],char eph_data_nu[19][901],Satellite S_t_NotUsed[19]) {
	char sfr1[301] = "";
	char sfr2[301] = "";
	char sfr3[301] = "";
	int i;

	int sat_count_wls = 0;
	int sat_count_rnd = 0;
	int r = 0;
	int ranging;
	
	double gps_pos[3];
	
	gps_pos[0] = X_est[0];
	gps_pos[1] = X_est[1];
	gps_pos[2] = X_est[2];
	cconv_to_geo(gps_pos);
	
	if(EUcoverage(gps_pos[0], gps_pos[1]) == 1)
		ranging = 0;
	else
		ranging = 1;
		
	double X_est_rnd[4], X_est3[4], pos3[3];
	X_est_rnd[0] = X_est[0];
	X_est_rnd[1] = X_est[1];
	X_est_rnd[2] = X_est[2];
	X_est_rnd[3] = X_est[3];

	X_est3[0] = X_est[0];
	X_est3[1] = X_est[1];
	X_est3[2] = X_est[2];
	X_est3[3] = X_est[3];

	sat_array[7] = 0;
	sat_array[8] = 0;
	sat_array[9] = 0;

	// satellite structure for R&D position computation
	Satellite S_rnd[19];

	int sat_count_notUsed = 0;

	for (i = 0; i < 19; i++) {
		// test if PRN is different from 0 and if GPS satellite
		if (sat_data[i][0] != 0 && get_satellite_type(sat_data[i][0]) == 0) {
			// Declaration of a satellite
			Satellite S;
			// Initialization of the satellite
			init_satellite(&S);
			//S is a GPS satellite
			S.type_sat = 1;
			// Getting ephemeris data from SIS
			S.prn = sat_data[i][0];
			if (egnos == 1)
				S.use = 2;
			else
				S.use = 1;
			S.tow = sat_data[i][1];
			S.tow2 = S.tow;
			S.pr = sat_data[i][2];
			S.pr_c = S.pr;
			S.cn0 = sat_data[i][3];
			extract(eph_data[i], 0, 299, sfr1);
			S.subfr1 = sfr1;
			extract(eph_data[i], 300, 599, sfr2);
			S.subfr2 = sfr2;
			extract(eph_data[i], 600, 899, sfr3);
			S.subfr3 = sfr3;
			decode_msg(&S);

			S_t[sat_count_wls] = S;
			S_rnd[sat_count_rnd] = S;
			//update satellites counter
			sat_count_wls++;
			sat_count_rnd++;
		}

		// test if PRN is different from 0 and if EGNOS satellite(3)
		if (sat_data[i][0] != 0 && get_satellite_type(sat_data[i][0]) == 3
				&& (rnd_options[7] == 0 || rnd_options[7] == 1 )) {

			int ii;

			if(rnd_options[7] == 0){	// automatic ranging
				for(ii = 0; ii < 3; ii++)
					if((*msg17).geo_alm[ii][0] == sat_data[i][0])
						if((*msg17).geo_alm[ii][2] == 1)
							ranging = 1;
			}else						// forced ranging
				ranging = 1;

			if(ranging == 1){
				// Declaration of a satellite
				Satellite S;
				// Initialization of the satellite
				init_satellite(&S);
				//S is a SBAS satellite
				S.type_sat = 2;
				// Getting ephemeris data from SIS
				S.prn = sat_data[i][0];
				if (egnos == 1)
					S.use = 2;
				else
					S.use = 0;
				S.tow = sat_data[i][1];
				if(sat_count_rnd > 0)
					S.weeknb = S_rnd[sat_count_rnd-1].weeknb;
				S.tow2 = S.tow;
				S.pr = sat_data[i][2];
				S.pr_c = S.pr;
				S.cn0 = sat_data[i][3];

				S_rnd[sat_count_rnd] = S;
				//update satellites counter
				sat_count_rnd++;
			}
		}


		strcpy(sfr1, "");
		strcpy(sfr2, "");
		strcpy(sfr3, "");
	}


		for (i = 0; i < 19; i++) {

			int sat_type = get_satellite_type(sat_data_NotUsed[i][0]);
			if (sat_data_NotUsed[i][0] != 0 && (sat_type == 0 || sat_type == 3)) {
				// Declaration of a satellite that is not used
				Satellite S_NotUsed;
				// Initialization of the satellite
				init_satellite(&S_NotUsed);
				// Getting ephemeris data from SIS
				if (egnos == 1)
					S_NotUsed.use = 2;
				else
					S_NotUsed.use = 1;
				S_NotUsed.prn = sat_data_NotUsed[i][0];
				S_NotUsed.tow = sat_data_NotUsed[i][1];
				S_NotUsed.tow2 = S_NotUsed.tow;
				S_NotUsed.pr = sat_data_NotUsed[i][2];
				S_NotUsed.pr_c = S_NotUsed.pr;
				S_NotUsed.cn0 = sat_data_NotUsed[i][3];
				S_NotUsed.type_sat = sat_type;
				extract(eph_data_nu[i], 0, 299, sfr1);
				S_NotUsed.subfr1 = sfr1;
				extract(eph_data_nu[i], 300, 599, sfr2);
				S_NotUsed.subfr2 = sfr2;
				extract(eph_data_nu[i], 600, 899, sfr3);
				S_NotUsed.subfr3 = sfr3;
				decode_msg(&S_NotUsed);

				S_t_NotUsed[sat_count_notUsed] = S_NotUsed;
				sat_count_notUsed++;
			}
			strcpy(sfr1, "");
		    strcpy(sfr2, "");
		    strcpy(sfr3, "");
		}
//		if (sat_count_notUsed < 19)
//			for (i = sat_count_notUsed; i < 19; i++) {
//				Satellite S_NotUsed;
//				init_satellite(&S_NotUsed);
//				S_t_NotUsed[i] = S_NotUsed;
//				S_t_NotUsed[i].use = 0;
//			}


	if (sat_count_wls < 19)
		for (i = sat_count_wls; i < 19; i++) {
			Satellite S;
			init_satellite(&S);
			S_t[i] = S;
			S_t[i].use = 0;
		}

	if (sat_count_rnd < 19)
		for (i = sat_count_rnd; i < 19; i++) {
			Satellite S;
			init_satellite(&S);
			S_rnd[i] = S;
			S_rnd[i].use = 0;
		}

		
	// Computation of the position if at least 4 satellites are available
	if (sat_count_wls > 3) {
		int rnd_options[8];
		r = user_position_computation_WLS(S_t, X_est, DOP, PL, msg1, msg2_5,
				msg6, msg7, msg10, msg12, msg9, msg17, m18_t, msg24_t, msg25_t,
				m26_t, egnos, iono_flag, m18_char, m26_char, sat_count_wls,
				sat_array, utc_data, rnd_options,S_t_NotUsed,sat_count_notUsed);
		if (r > 3) {
			// Geodetic conversion
			pos[0] = X_est[0];
			pos[1] = X_est[1];
			pos[2] = X_est[2];
			cconv_to_geo(pos);
		} else {
			pos[0] = 0;
			pos[1] = 0;
			pos[2] = 0;
			DOP[0] = 0;
			DOP[1] = 0;
			DOP[2] = 0;
			DOP[3] = 0;
			PL[0] = 0;
			PL[1] = 0;
		}

	}
	
		if (sat_count_rnd > 3 && egnos == 1 &&  ( rnd_options[0] == 1 || rnd_options[1] == 1 || rnd_options[2] == 1
				|| rnd_options[3] == 1 || rnd_options[4] == 1 || rnd_options[5] == 1
				|| rnd_options[6] == 1 || rnd_options[7] == 1 || (rnd_options[7] == 0 && ranging == 1 )) ) {

		// user_position_computation_RnD - new algorithm
		// user_position_computation_RND -  the same as WLS
		r = user_position_computation_RnD(S_rnd, X_est_rnd, msg1, msg2_5,
				msg6, msg7, msg10, msg12, msg9, msg17, m18_t, msg24_t, msg25_t,
				m26_t, m18_char, m26_char, sat_count_rnd, sat_array,
				utc_data, klob_data, rnd_options);

		if(r < 4){
			sat_array[7] = 0;
			sat_array[8] = 0;
			sat_array[9] = 0;
			sat_array[10] = 0;
			sat_array[11] = 0;
			sat_array[12] = 0;
			sat_array[13] = 0;
			sat_array[14] = 0;
		}
	}
	if (X_est[2] != 0.0 && ((sat_count_wls == 3 && rnd_options[2] == 1) || 
			(sat_count_wls > 3 && r == 3 && rnd_options[2] == 1))) {
		//X_est[2] = 4692740.6938460264; 			// EGNOS Altitude
		//X_est[2] = 4692760.1307149306;		// GPS   Altitude
		r = TwoDpos_computation(S_t, X_est3, msg1, msg2_5, msg6, msg7,
				msg9, msg10, msg12, msg17, m18_t, msg24_t, msg25_t, m26_t,
				egnos, iono_flag, m18_char, m26_char, sat_count_wls, sat_array,
				utc_data, klob_data, rnd_options);

		if (r == 3) {
			// Geodetic conversion
			pos3[0] = X_est3[0];
			pos3[1] = X_est3[1];
			pos3[2] = X_est3[2];
			X_est[0] = X_est3[0];
			X_est[1] = X_est3[1];
			X_est[2] = X_est3[2];
			cconv_to_geo(pos3);
			sat_array[7] = pos3[0];
			sat_array[8] = pos3[1];
			sat_array[9] = pos3[2];

		} else {
			pos[0] = 0;
			pos[1] = 0;
			pos[2] = 0;
		}

	}
	if (r < 3){
		pos[0] = 0;
		pos[1] = 0;
		pos[2] = 0;
		DOP[0] = 0;
		DOP[1] = 0;
		DOP[2] = 0;
		DOP[3] = 0;
		PL[0] = 0;
		PL[1] = 0;
		r = sat_count_wls;
	}
		
		if(rnd_options[0] == 1 || rnd_options[1] == 1 || rnd_options[2] == 1 || rnd_options[3] == 1
				|| rnd_options[4] == 1 || rnd_options[7] == 1 || (rnd_options[7] == 0 && ranging == 1 ) || 
						rnd_options[5] == 1 || rnd_options[6] == 1)
		for(i = 0; i < 19; i++)
			if(S_rnd[i].use == 2)
				S_t[i].rnd = 1;

	return r;
}
	
	/**
	 * EUcoverage function
	 * The function determines if the position of the receiver is 
	 * located within the coverage of the European Region 
	 * @param  	lat 		The latitude of the receiver
	 * @param  	lon 		The latitude of the receiver
	 * returns  	 		1 if the receiver is inside Europe

	 */
	int EUcoverage(double lat, double lon) {
		int inside = 0;
		
		if(lat >= 20 && lat <= 70 && lon >= -40 && lon <= 40)
			inside = 1;

		return inside;
	}
	
/**
 * user_position_computation_WLS function
 * The function performs a Weighted Least Square method to compute the navigation solution.
 * @param S_t         	The table of Satellites
 * @param X_est       	The table of estimated position (The initial values are the estimations given by Bancroft method or
 * 				      	the previous estimations)
 * @param DOP         	The Dilution Of Precision destination table (DOP[0]:HDOP DOP[1]:VDOP DOP[2]:PDOP DOP[3]:TDOP)
 * @param PL          	The Protection Level destination table (PL[0]:HPL PL[1]:VPL)
 * @param *msg1       	The pointer of the message type 1
 * @param msg2_5      	The table of the the messages type 2-5
 * @param *msg6       	The pointer of the message type 6
 * @param *msg7			The pointer of the message type 7
 * @param *msg9      	The pointer of the message type 9
 * @param *msg10     	The pointer of the message type 10
 * @param *msg12     	The pointer of the message type 12
 * @param *msg17     	The pointer of the message type 17
 * @param m18_t       	The table of the messages type 18
 * @param msg24_t     	The table of the messagea type 24
 * @param msg25_t     	The Egnos messages type 25 table
 * @param m26_t       	The table of the messages type 26
 * @param egnos       	The EGNOS flag (1:EGNOS enabled, 0:EGNOS disabled)
 * @param *iono_flag  	The pointer of the flag to check if all needed messages for ionospheric correction are available
 * @param m18_char    	The table of 5(max) messages 18 in 256 characters (for SIS)
 * @param m26_char    	The table of 25(max) messages 26 in 256 characters (for SIS)
 * @param sat_count   	The number of satellites used to compute the position
 * @param sat_array  	[0] = total no. of satellites in view
 *                   	[1] = No. of Satellites eliminated for Low Elevation (under 10 deg.)
 *                   	[2] = No. of Sats not set in PRN mask (currently 1 and 25)
 *                   	[3] = No. of iterations [15] = 1 if position jump is more than 1.5m
 *                   	[4] = position jump on x-axis [5] = position jump on y-axis
 *                   	[6] = The number of satellites used to compute the position
 * @param utc_data	  	The array of UTC parameters
 * @param rnd_options	Array that holds the R&D options
 * @return sat_count  The number of satellites used to compute the position
 */
int user_position_computation_WLS(Satellite S_t[19], double X_est[4],
		double DOP[4], double PL[2], Egnos_msg * msg1, Egnos_msg msg2_5[4][2],
		Egnos_msg * msg6, Egnos_msg * msg7, Egnos_msg * msg10,
		Egnos_msg * msg12, Egnos_msg * msg9, Egnos_msg * msg17,
		Egnos_msg m18_t[11], Egnos_msg msg24_t[25], Egnos_msg msg25_t[15],
		Egnos_msg m26_t[25], int egnos, int * iono_flag, char m18_char[5][263],
		char m26_char[25][263], int sat_count, double sat_array[15],
		double utc_data[9], int rnd_options[8],Satellite S_t_NotUsed[19],
		int sat_count_notUsed) {

	double W[sat_count][sat_count];
	double pos[3], r_pos[3], r_pos_geo[3], R, R_corrected, ENU[3], sigma2[sat_count];
	double d_major, d_east2, d_north2, d_en2, dtutc, eps_ltc, H[sat_count][4],
	H_t[sat_count][4], dPR[sat_count], dX[4];
	int it = 0; // Number of iterations
	int nb_igps = 0;
	int iono_count = 0;
	int i,j;

	double bancroft_est[4] = {0,0,0,0};

	double init_pos[2];
	init_pos[0] = X_est[0];
	init_pos[1] = X_est[1];

	int total_sats = 0;
	int eliminated = 0;
	int no_egnos = 0;

	sat_array[0] = -1.0;
	sat_array[1] = -1.0;
	sat_array[2] = -1.0;
	sat_array[3] =  0;
	sat_array[4] = -1.0;
	sat_array[5] = -1.0;
	sat_array[6] = -1.0;

	total_sats = sat_count;
	int sat_used = 0;

	// Init flag to 0
	*iono_flag = 0;

	if (egnos == 1) {
		// Set the ionospheric grid
		set_ionoGridSis(m18_t, m26_t, m18_char, m26_char);
	}



	// counter for ionospheric correction (increase if a PRN has an ionospheric correction)
	iono_count = 0;
	while (it < 20) {

		if (it == 0) {
			for (i = 0; i < sat_count; i++) {
				//android_syslog(ANDROID_LOG_INFO, "prn: %f,use:: %d\n",S_t[i].prn,S_t[i].use);

				// GPST (Receiver time of reception - time of transmission)
				S_t[i].tow2 = S_t[i].tow - S_t[i].pr_c / SPEED_OF_LIGHT;
				if (egnos == 1) {
					// long term corr
					eps_ltc = set_LongCorrection(&S_t[i], msg24_t, msg25_t, msg10, msg1);
					// fast corrections
					get_fastCorrection(&S_t[i], msg1, msg2_5, msg24_t, msg6,
							msg10, msg7, eps_ltc, rnd_options);

					//android_syslog(ANDROID_LOG_INFO, "prn: %f,use:: %d\n",S_t[i].prn,S_t[i].use);
				}

				// Computation of the GPS satellite position
				if (S_t[i].type_sat == 1)
					SV_position_computation(&S_t[i], egnos);

				// Computation of the EGNOS satellite position
				if (S_t[i].type_sat == 2)
					compute_EGNOSsat_position(&S_t[i], msg9);

				sigma2[i] = 1;

				if (egnos == 1) {
					int check_prn = 0;
					for (j = 0; j < (*msg1).prn_nb; j++) {
						if (S_t[i].prn == (*msg1).prn[j]) {
							check_prn = 1;
						}
					}
					if(check_prn == 0){
						S_t[i].prn_mask	= 0;
						S_t[i].use 	= 1;
					}
				
				// Convertion to ENU preparation
				r_pos_geo[0] = X_est[0];
				r_pos_geo[1] = X_est[1];
				r_pos_geo[2] = X_est[2];
				r_pos[0] = X_est[0];
				r_pos[1] = X_est[1];
				r_pos[2] = X_est[2];
				pos[0] = S_t[i].pos_x;
				pos[1] = S_t[i].pos_y;
				pos[2] = S_t[i].pos_z;
				cconv_to_geo(r_pos_geo);
				
				// Calculate azimuth and elevation angles of the satellite
				cconv_to_ENU(ENU, pos, r_pos, r_pos_geo);
				S_t[i].az = get_azimuth(ENU);
				S_t[i].el = get_elevation(ENU);

				    if (S_t[i].el < 10) {
						eliminated++;
						S_t[i].use = 0;
						S_t[i].low_elv = 1;
					}
					
				}
			}

			if(X_est[0]==0 && X_est[1]==0 && X_est[2]==0 && X_est[3]==0)
			{
				// Initialization of receiver position estimation, initial guess computed with Bancroft method
				if(user_position_computation_bancroft(S_t,bancroft_est,sat_count)==1)
				{
					X_est[0] = bancroft_est[0];			// X
					X_est[1] = bancroft_est[1];			// Y
					X_est[2] = bancroft_est[2];			// Z
					X_est[3] = bancroft_est[3];
				}
			}
		}
		

		//Compute azimuth and elevation for satellites not used
		for (i = 0; i < sat_count_notUsed; i++) {
			if (S_t_NotUsed[i].prn != 0.0) {


				// Computation of the GPS satellite position
				if (S_t_NotUsed[i].type_sat == 0 || S_t_NotUsed[i].prn <= 32)
					SV_position_computation(&S_t_NotUsed[i], egnos);

				// Computation of the EGNOS satellite position
				if (S_t_NotUsed[i].type_sat == 3)
					compute_EGNOSsat_position(&S_t_NotUsed[i], msg9);


				// Convertion to ENU preparation
				r_pos_geo[0] = X_est[0];
				r_pos_geo[1] = X_est[1];
				r_pos_geo[2] = X_est[2];
				//android_syslog(ANDROID_LOG_INFO, " r_pos_geo[0]: %f , r_pos_geo[1]: %f, r_pos_geo[2]: %f\n",r_pos_geo[0],r_pos_geo[1],r_pos_geo[2]);
				r_pos[0] = X_est[0];
				r_pos[1] = X_est[1];
				r_pos[2] = X_est[2];
				//android_syslog(ANDROID_LOG_INFO, " r_pos[0]: %f , r_pos[1]: %f, r_pos[2]: %f\n",r_pos[0],r_pos[1],r_pos[2]);
				pos[0] = S_t_NotUsed[i].pos_x;
				pos[1] = S_t_NotUsed[i].pos_y;
				pos[2] = S_t_NotUsed[i].pos_z;
				//android_syslog(ANDROID_LOG_INFO, " pos[0]: %f , pos[1]: %f, pos[2]: %f\n",pos[0],pos[1],pos[2]);
				cconv_to_geo(r_pos_geo);

				cconv_to_ENU(ENU, pos, r_pos, r_pos_geo);
				S_t_NotUsed[i].az = get_azimuth(ENU);
				S_t_NotUsed[i].el = get_elevation(ENU);
			}
		}


		// Satellites loop
		sat_used = 0;
		for (i = 0; i < sat_count; i++) {
			//android_syslog(ANDROID_LOG_INFO, "// Satellites loop prn: %f,use:: %d\n",S_t[i].prn,S_t[i].use);

			if((S_t[i].use != 0 && egnos == 0) || (S_t[i].use == 2 && egnos == 1)) {
				sat_used++;
				// GPST (Receiver time of reception - time of transmission)
				S_t[i].tow2 = S_t[i].tow - S_t[i].pr_c / SPEED_OF_LIGHT;

				if (egnos == 1) {

					double UTCGPST = get_UTCoffset(&S_t[i], utc_data);

					// If message 12 available, time alignment t(ENT) = t(GPS) - dt(utc/GPST) + dt(utc/ENT)
					if((*msg12).tow != -1.0 && utc_data[8] == 1) {
						dtutc = (*msg12).dtls + (*msg12).a0snt + (*msg12).a1snt*(S_t[i].tow2
								- (*msg12).t0t + GPSWEEK_IN_SEC*((*msg12).gps_wknb - (*msg12).wnt));

						if ((UTCGPST - dtutc) < 1)
							S_t[i].tow2 = S_t[i].tow2 - UTCGPST + dtutc;
					}
				}
				
				// Update the GPS position
				if (S_t[i].type_sat == 1)
					SV_position_computation(&S_t[i], egnos);

				// Update the EGNOS position
				if (S_t[i].type_sat == 2 && egnos == 1)
					compute_EGNOSsat_position(&S_t[i], msg9);

				// Range
				R = sqrt(
						(S_t[i].pos_x - X_est[0]) * (S_t[i].pos_x - X_est[0])
						+ (S_t[i].pos_y - X_est[1])
						* (S_t[i].pos_y - X_est[1])
						+ (S_t[i].pos_z - X_est[2])
						* (S_t[i].pos_z - X_est[2]));

				//if(S_t[i].type_sat==1)
				SV_position_correction(&S_t[i], (R / SPEED_OF_LIGHT));

				// Range corrected after earth rotation compensation
				R_corrected = sqrt(
						(S_t[i].pos_x - X_est[0]) * (S_t[i].pos_x - X_est[0])
						+ (S_t[i].pos_y - X_est[1])
						* (S_t[i].pos_y - X_est[1])
						+ (S_t[i].pos_z - X_est[2])
						* (S_t[i].pos_z - X_est[2]));

				// Convertion to ENU preparation
				r_pos_geo[0] = X_est[0];
				r_pos_geo[1] = X_est[1];
				r_pos_geo[2] = X_est[2];
				r_pos[0] = X_est[0];
				r_pos[1] = X_est[1];
				r_pos[2] = X_est[2];
				pos[0] = S_t[i].pos_x;
				pos[1] = S_t[i].pos_y;
				pos[2] = S_t[i].pos_z;
				cconv_to_geo(r_pos_geo);

				// Calculate azimuth and elevation angles of the satellite
				cconv_to_ENU(ENU, pos, r_pos, r_pos_geo);
				S_t[i].az = get_azimuth(ENU);
				S_t[i].el = get_elevation(ENU);

				if (it > 2) {
					if (S_t[i].el < 10) {
						eliminated++;
						S_t[i].use = 0;
						sat_used--;
						S_t[i].low_elv = 1;
					}
				}

				if (egnos == 1) {
					// Compute ionospheric and tropospheric delay
					if(S_t[i].use == 2 && egnos ==1) {


						get_ionoCorrection(&S_t[i],r_pos_geo[0],r_pos_geo[1],
								m18_t,m26_t,msg10, 0);

						if(S_t[i].sigma_uire2 > 1000)
							S_t[i].use = 0;
						get_tropoCorrection(&S_t[i],r_pos_geo[0],r_pos_geo[2]);

						if(S_t[i].sigma_tropo2 > 1000)
							S_t[i].use = 0;

						if(S_t[i].sigma_flt2 > 1000)
							S_t[i].use = 0;

						// Airborne equipment multipath and noise models are not used for this implementation
						sigma2[i] = S_t[i].sigma_flt2 + S_t[i].sigma_tropo2 + S_t[i].sigma_uire2
								+ 0.36*0.36 + get_sigma_mult2(S_t[i].el);
						//+ get_sigma_mult2(S_t[i].el) + get_sigma_noisedivg2(S_t[i].el);

						if(sigma2[i] == 0)      // avoid division by 0
							S_t[i].use = 0;

						S_t[i].sigma2 = sigma2[i];
						// Correction of pseudoranges
						S_t[i].pr_c = S_t[i].pr  + S_t[i].iono_delay+ S_t[i].tropo_delay + S_t[i].fast_delay;

						android_syslog(ANDROID_LOG_INFO,
						            "EGNOS corrections: PRN: %2.0f Use: %d Elev: %d Iono: %2.3f Tropo: %2.3f Fast: %2.3f RRC: %1.3f UDRE: %d dx: %1.3f dy: %1.3f dz: %1.3f daf0: %1.1f"
						            "sigma: %.2f sigma_iono: %.2f sigma_tropo: %.2f sigma_fast: %.2f eps:_fc: %.2f eps_rrc: %.2f eps_ltc: %.2f \n",
						            S_t[i].prn,
						            S_t[i].use,
						            (int)S_t[i].el,
						            S_t[i].iono_delay,
						            S_t[i].tropo_delay,
						            S_t[i].fast_delay,
						            S_t[i].rrc,
						            S_t[i].udrei,
						            S_t[i].dx,
						            S_t[i].dy,
						            S_t[i].dz,
						            S_t[i].daf0,
						            S_t[i].sigma2,
						            S_t[i].sigma_uire2,
						            S_t[i].sigma_tropo2,
						            S_t[i].sigma_flt2,
						            S_t[i].eps_fc,
									S_t[i].eps_rrc,
									S_t[i].eps_ltc);
					}

				}

				// Pseudorange residual
				dPR[i] = S_t[i].pr_c - R_corrected+ SPEED_OF_LIGHT*S_t[i].t_correction-X_est[3];
				
				// matrix H definition
				H[i][0] = (X_est[0]-S_t[i].pos_x) / R_corrected;
				H[i][1] = (X_est[1]-S_t[i].pos_y) / R_corrected;
				H[i][2] = (X_est[2]-S_t[i].pos_z) / R_corrected;
				H[i][3] = 1;

				H_t[i][0] = - cos(S_t[i].el * PI / 180) * sin(S_t[i].az * PI / 180);
				H_t[i][1] = - cos(S_t[i].el * PI / 180) * cos(S_t[i].az * PI / 180);
				H_t[i][2] = - sin(S_t[i].el * PI / 180);
				H_t[i][3] = 1;

			} else {//if no pseudoranges data for the satellite available => fill the matrixes with
				//android_syslog(ANDROID_LOG_INFO, "no pseudoranges\n");
				R = 0;
				H[i][0] = 0;
				H[i][1] = 0;
				H[i][2] = 0;
				H[i][3] = 0;

				H_t[i][0] = 0;
				H_t[i][1] = 0;
				H_t[i][2] = 0;
				H_t[i][3] = 0;

				dPR[i] = 0;

				ENU[0] = 0;
				ENU[1] = 0;
				ENU[2] = 0;

				sigma2[i] = 1;
			}
			// Weight matrix (Diagonal matrix 15x15 with the elevation)
			for (j = 0; j < sat_count; j++) {
				if(i == j && (( S_t[i].use != 0 && egnos == 0) || (S_t[i].use == 2 && egnos == 1)) ) {
					if(egnos == 1)
					{
						W[i][j] = 1/sigma2[i];
					}
					else
					{
						W[i][j] = S_t[i].cn0;
					}
				} else {
					W[i][j] =  0;
				}
			}
		}

		// Test if number of satellites > 3
		if (sat_used < 4)
			break;

		// Iterations increment
		it++;

		double HtW[4][sat_count], HtWH[4][4], HtWH_[4][4], HtWH_HtW[4][sat_count];
		double HtH[4][4], HtH_[4][4], Ht[4][sat_count];

		// Computation of inv(trans(H).H).trans(H)
		size_b_row1=sat_count;
		size_b_col1=4;
		transpose(H,Ht);						//change
		size_b_row1=4;
		size_b_col1=sat_count;
		size_b_row2=sat_count;
		size_b_col2=4;
		multiply(Ht,H,HtH);					//change
		inv_44(HtH, HtH_);
		size_b_col2=sat_count;
		// Calculation with the weight matrix
		multiply(Ht,W,HtW);
		size_b_col2=4;
		multiply(HtW,H,HtWH);					//change
		inv_44(HtWH, HtWH_);
		size_b_col1=4;
		size_b_row2=4;
		size_b_col2=sat_count;
		multiply(HtWH_,HtW,HtWH_HtW);
		size_b_col1=sat_count;
		size_b_row2=sat_count;
		size_b_col2=4;
		multiply_matxvec(HtWH_HtW,dPR,dX);
		//android_syslog(ANDROID_LOG_INFO, "1) dX[0] : %f \n", dX[0]);
		// update estimation
		X_est[0] += dX[0];
		X_est[1] += dX[1];
		X_est[2] += dX[2];
		X_est[3] += dX[3];
		//android_syslog(ANDROID_LOG_INFO, "2) X_est[0] : %f \n", X_est[0]);
		// DOP
		DOP[0] = get_HDOP(HtH_);
		DOP[1] = get_VDOP(HtH_);
		DOP[2] = get_PDOP(HtH_);
		DOP[3] = get_TDOP(HtH_);

		// Check if HDOP is too high, position cannot be resolved
		if(DOP[0] > 20) {
			sat_used = 0;  // the function will return 0
			break;
		}

		if(egnos == 1) {

			double HtW[4][sat_count], HtWH[4][4], HtWH_[4][4];
			double HtH[4][4], HtH_[4][4], Ht[4][sat_count];

			size_b_row1=sat_count;
			size_b_col1=4;
			transpose(H_t,Ht);
			size_b_row1=4;
			size_b_col1=sat_count;
			size_b_row2=sat_count;
			size_b_col2=4;
			multiply(Ht,H_t,HtH);
			inv_44(HtH, HtH_);
			size_b_col2=sat_count;
			// Computation with the weight matrix
			multiply(Ht,W,HtW);
			size_b_col2=4;
			multiply(HtW,H_t,HtWH);
			inv_44(HtWH, HtWH_);

			// computation of HPL from matrix inv(Ht.W.H)
			d_east2 = HtWH_[0][0];
			d_north2 = HtWH_[1][1];
			d_en2 = HtWH_[0][1]*HtWH_[0][1];

			d_major = sqrt(((d_east2 + d_north2) / 2) + sqrt(((d_east2 - d_north2) / 2)	*((d_east2 - d_north2)/2) + d_en2));
			PL[0] = d_major;           //HPL without K factor

			PL[1] = sqrt(HtWH_[2][2]); //VPL without K factor; VPL=du.K
		}

		// Geodetic conversion
		pos[0] = X_est[0];
		pos[1] = X_est[1];
		pos[2] = X_est[2];
		cconv_to_geo(pos);

		double norm=sqrt(dX[0]*dX[0]+dX[1]*dX[1]+dX[2]*dX[2]);
		if(norm < 1E-8 && it > 6)	  break;

	}

	no_egnos=total_sats-sat_used-eliminated;
	
	// check for satellites without iono corrections
	*iono_flag = egnos;
	for (i = 0; i < sat_count; i++)
		if (S_t[i].use == 2 && egnos == 1 ){
			if( S_t[i].iono_delay == 0)
				*iono_flag = 0;
			else
				iono_count++;
		}

	//android_syslog(ANDROID_LOG_INFO, "X_est[0]: %f ,init_pos[0]: %f\n", X_est[0],init_pos[0]);
    android_syslog(
			ANDROID_LOG_INFO,
			"Total Sats: %d Low Elv.:%d Missing from mask: %d Required iono: %d "
			"Iono_count: %d Init pos: %f Jump: %f\n",
			total_sats, eliminated, no_egnos, sat_used, iono_count,
			init_pos[0], X_est[0] - init_pos[0]);
	android_syslog(ANDROID_LOG_INFO, "Iono flag: %d\n", *iono_flag);

	sat_array[0] = (double) total_sats; // total number of satellites in view
	sat_array[1] = (double) eliminated; // low elev
	sat_array[2] = (double) no_egnos; // not in PRN mask
	sat_array[3] = (double) it; // no of iterations

	// set sat_array[4] if a jump of more then 1.5m occurs in the position
	if( (init_pos[0] != 0 && sqrt((X_est[0] - init_pos[0])*(X_est[0] - init_pos[0]) + (X_est[1] - init_pos[1])*(X_est[1] - init_pos[1])) >= 1.5)  )
	{
		sat_array[4] = 1.0;
		sat_array[5] = X_est[0] - init_pos[0];  // jump on x axis
		sat_array[6] = X_est[1] - init_pos[1];  // jump on y axis
	}
	else
	{
		sat_array[4] = 0.0;
		sat_array[5] = 0.0;
		sat_array[6] = 0.0;
	}

	return sat_used;
}

/**
 * user_position_computation_RnD function
 * The function performs a Weighted Least Square method to compute the navigation solution applying
 * R&D algorithms.
 * @param S_t         The table of Satellites
 * @param X_est       The table of estimated position (The initial values are the estimations given by Bancroft method or
 * 				      the previous estimations)
 * @param *msg1       The pointer of the message type 1
 * @param msg2_5      The table of the the messages type 2-5
 * @param *msg6       The pointer of the message type 6
 * @param *msg7       The pointer of the message type 7
 * @param *msg10      The pointer of the message type 10
 * @param *msg12      The pointer of the message type 12
 * @param m18_t       The table of the messages type 18
 * @param msg24_t     The table of the messagea type 24
 * @param msg25_t     The Egnos messages type 25 table
 * @param m26_t       The table of the messages type 26
 * @param m18_char    The table of 5(max) messages 18 in 256 characters (for SIS)
 * @param m26_char    The table of 25(max) messages 26 in 256 characters (for SIS)
 * @param sat_count   The number of satellites used to compute the position
 * @param sat_array  [7] = Latitude of RnD position
 *                   [8] = Longitude of RnD position
 *                   [9] = Altitude of RnD position
 * @return sat_count  The number of satellites used to compute the position
 */
int user_position_computation_RnD(Satellite S_t[19], double X_est[4],
		Egnos_msg * msg1, Egnos_msg msg2_5[4][2],
		Egnos_msg * msg6, Egnos_msg * msg7, Egnos_msg * msg10,
		Egnos_msg * msg12, Egnos_msg * msg9, Egnos_msg * msg17,
		Egnos_msg m18_t[11], Egnos_msg msg24_t[25], Egnos_msg msg25_t[15],
		Egnos_msg m26_t[25], char m18_char[5][263],	char m26_char[25][263],
		int sat_count, double sat_array[15],  double utc_data[9],
		double klob_data[9], int rnd_options[8]) {

	double W[sat_count][sat_count], DOP[4];
	double pos[3], r_pos[3], r_pos_geo[3], R, R_corrected, ENU[3], sigma2[sat_count];
	double eps_ltc, H[sat_count][4], dPR[sat_count], dX[4];
	double dtutc = 0;
	int it = 0; // Number of iterations
	int i,j;
	double iono[2];


	sat_array[7] =  0;
	sat_array[8] =  0;
	sat_array[9] =  0;
	sat_array[10] = -1.0;
	sat_array[11] = -1.0;
	sat_array[12] = -1.0;
	sat_array[13] = -1.0;
	sat_array[14] = -1.0;

	int sat_used = 0;

	int egnos = 1;

	double PL[2];

	double pos_dop[3];

	if (egnos == 1) {
	// Set the ionospheric grid
	set_ionoGridSis(m18_t, m26_t, m18_char, m26_char);

	// Computing the EGNOS corrections and integrity
	for (i = 0; i < sat_count; i++) {
		// GPST (Receiver time of reception - time of transmission)
		S_t[i].tow2 = S_t[i].tow - S_t[i].pr_c / SPEED_OF_LIGHT;

		// long term corr
		eps_ltc = set_LongCorrection(&S_t[i], msg24_t, msg25_t, msg10, msg1);
		// fast corrections
		get_fastCorrection(&S_t[i], msg1, msg2_5, msg24_t, msg6,
				msg10, msg7, eps_ltc, rnd_options);

		double UTCGPST = get_UTCoffset(&S_t[i], utc_data);

		// If message 12 available, time alignment t(ENT) = t(GPS) - dt(utc/GPST) + dt(utc/ENT)
		if((*msg12).tow != -1.0 && utc_data[8] == 1) {
			dtutc = (*msg12).dtls + (*msg12).a0snt + (*msg12).a1snt*(S_t[i].tow2
					- (*msg12).t0t + GPSWEEK_IN_SEC*((*msg12).gps_wknb - (*msg12).wnt));

			if ((UTCGPST - dtutc) < 1)
				S_t[i].tow2 = S_t[i].tow2 - UTCGPST + dtutc;
		}

		// Computation of the GPS satellite position
		if (S_t[i].type_sat == 1)
			SV_position_computation(&S_t[i], egnos);

		// Computation of the EGNOS satellite position
		if (S_t[i].type_sat == 2)
			compute_EGNOSsat_position(&S_t[i], msg9);

		// Range
		R = sqrt(
				(S_t[i].pos_x - X_est[0]) * (S_t[i].pos_x - X_est[0])
				+ (S_t[i].pos_y - X_est[1])
				* (S_t[i].pos_y - X_est[1])
				+ (S_t[i].pos_z - X_est[2])
				* (S_t[i].pos_z - X_est[2]));

		SV_position_correction(&S_t[i], (R / SPEED_OF_LIGHT));

		// Convertion to ENU preparation
		r_pos_geo[0] = X_est[0];
		r_pos_geo[1] = X_est[1];
		r_pos_geo[2] = X_est[2];
		r_pos[0] = X_est[0];
		r_pos[1] = X_est[1];
		r_pos[2] = X_est[2];
		pos[0] = S_t[i].pos_x;
		pos[1] = S_t[i].pos_y;
		pos[2] = S_t[i].pos_z;
		cconv_to_geo(r_pos_geo);

		// Calculate azimuth and elevation angles of the satellite
		cconv_to_ENU(ENU, pos, r_pos, r_pos_geo);
		S_t[i].az = get_azimuth(ENU);
		S_t[i].el = get_elevation(ENU);

		if (S_t[i].el < 10) {
			S_t[i].use = 0;
			S_t[i].low_elv = 1;
		}

		int check_prn = 0;
		for (j = 0; j < (*msg1).prn_nb; j++) {
			if (S_t[i].prn == (*msg1).prn[j]) {
				check_prn = 1;
			}
		}
		if(check_prn == 0){
			S_t[i].prn_mask	= 0;
			S_t[i].use 	= 1;
		}

		// Compute ionospheric and tropospheric delay
		if(S_t[i].use == 2) {
			sat_used++;

			get_ionoCorrection(&S_t[i],r_pos_geo[0],r_pos_geo[1],
					m18_t,m26_t,msg10,3);

			if(S_t[i].sigma_uire2 > 1000)
				S_t[i].use = 0;

			get_tropoCorrection(&S_t[i],r_pos_geo[0],r_pos_geo[2]);

			if(S_t[i].sigma_tropo2 > 1000)
				S_t[i].use = 0;

			if(S_t[i].sigma_flt2 > 1000)
				S_t[i].use = 0;

			// Airborne equipment multipath and noise models are not used for this implementation
			sigma2[i] = S_t[i].sigma_flt2 + S_t[i].sigma_tropo2 + S_t[i].sigma_uire2;

			if(sigma2[i] == 0)      // avoid division by 0
				S_t[i].use = 0;

			S_t[i].sigma2 = sigma2[i];
			// Correction of pseudoranges
			S_t[i].pr_c = S_t[i].pr  + S_t[i].iono_delay+ S_t[i].tropo_delay + S_t[i].fast_delay;

//			ionospheric_model(klob_data, S_t[i], r_pos_geo[0], r_pos_geo[1], iono);
//			S_t[i].iono_model = iono[0];
//			if(S_t[i].iono_delay == 0){
//				S_t[i].pr_c = S_t[i].pr_c - ionDelay_model;
//			}
		}
		if((*msg12).tow != -1.0)
			S_t[i].tow2 = S_t[i].tow - S_t[i].pr_c / SPEED_OF_LIGHT - UTCGPST + dtutc;
		else
			S_t[i].tow2 = S_t[i].tow - S_t[i].pr_c / SPEED_OF_LIGHT;

		// Weight matrix (Diagonal matrix 15x15 with the elevation)
		for (j = 0; j < sat_count; j++) {
			if(i == j && S_t[i].use == 2)	W[i][j] = 1/sigma2[i];
			else							W[i][j] =  0;
		}
	 }
	}

	if(sat_used > 3)
  	rnd_user_position_computation(S_t, sat_count, H, W, dPR, X_est, pos,
				DOP, dX, rnd_options, msg1, msg2_5, msg6, msg7, msg10, msg12,
				msg9, msg17, m18_t, msg24_t, msg25_t, m26_t, m18_char,
				m26_char, sat_array, utc_data, klob_data);
	if (rnd_options[3] == 1 && sat_used > 4){
		raim(S_t, sat_count, H, 2, W, dPR, X_est, dX, pos_dop,msg9);	//pos_raim*/
	    if(sat_used > 3)
	      	rnd_user_position_computation(S_t, sat_count, H, W, dPR, X_est, pos,
	    				DOP, dX, rnd_options, msg1, msg2_5, msg6, msg7, msg10, msg12,
	    				msg9, msg17, m18_t, msg24_t, msg25_t, m26_t, m18_char,
	    				m26_char, sat_array, utc_data, klob_data);
	}

	if(sat_used > 4 && rnd_options[1] == 1)
		DOPpos_computation(S_t, X_est, sat_count, pos_dop, H, W, msg9, dPR);

   if (pos_dop[0] == 0 && pos_dop[1] == 0 && pos_dop[2] == 0){
		pos_dop[0] = pos[0];
		pos_dop[1] = pos[1];
		pos_dop[2] = pos[2];
	}

	// Positions obtained with the Best DOP algorithm
	sat_array[7] = pos[0];
	sat_array[8] = pos[1];
	sat_array[9] = pos[2];

	sat_array[10] = pos_dop[0];
	sat_array[11] = pos_dop[1];
	sat_array[12] = pos_dop[2];

	sat_array[13] = PL[0];
	sat_array[14] = PL[1];

	return sat_used;
}

/**
 * rnd_user_position_computation function
 * The function performs a Weighted Least Square method to compute the navigation solution applying
 * R&D algorithms.
 * @param S_t         The table of Satellites
 * @param sat_count   The number of satellites used to compute the position
 * @param H			  The geometry matrix
 * @param W			  The weight matrix
 * @param dPR		  The pseudorange residual
 * @param X_est		  Position result in ECEF coordinates
 * @param pos		  Position result expressed in latitude, longitude and altitude
 * @param DOP		  Vector of 4 different DOP values (HDOP, VDOP, PDOP, GDOP)
 * @param dX		  The last update to the position
 * @param rnd_options Array that holds the R&D option
 * @param *msg1       The pointer of the message type 1
 * @param msg2_5      The table of the the messages type 2-5
 * @param *msg6       The pointer of the message type 6
 * @param *msg7       The pointer of the message type 7
 * @param *msg10      The pointer of the message type 10
 * @param *msg12      The pointer of the message type 12
 * @param m18_t       The table of the messages type 18
 * @param msg24_t     The table of the messagea type 24
 * @param msg25_t     The Egnos messages type 25 table
 * @param m26_t       The table of the messages type 26
 * @param m18_char    The table of 5(max) messages 18 in 256 characters (for SIS)
 * @param m26_char    The table of 25(max) messages 26 in 256 characters (for SIS)
 * @param sat_count   The number of satellites used to compute the position
 * @param sat_array   [7] = Latitude of RnD position
 *                    [8] = Longitude of RnD position
 *                    [9] = Altitude of RnD position
 * @param utc_data	  The array of UTC parameters
 * @param   klob_data The ionopsheric coeficients alpha nad beta
 * @return sat_used   The number of satellites used to compute the position
 */
int rnd_user_position_computation(Satellite S_t[19], int sat_count, double H[sat_count][4],
		double W[sat_count][sat_count],double dPR[sat_count], double X_est[4], double pos[3],
		double DOP[4], double dX[4],int rnd_options[8],Egnos_msg * msg1, Egnos_msg msg2_5[4][2],
		Egnos_msg * msg6, Egnos_msg * msg7, Egnos_msg * msg10,
		Egnos_msg * msg12, Egnos_msg * msg9, Egnos_msg * msg17,
		Egnos_msg m18_t[11], Egnos_msg msg24_t[25], Egnos_msg msg25_t[15],
		Egnos_msg m26_t[25], char m18_char[5][263],	char m26_char[25][263],
		double sat_array[15],  double utc_data[9],
		double klob_data[9]){

	double PL[2];
	double r_pos[3], r_pos_geo[3], R, R_corrected, ENU[3], sigma2[sat_count];
	double d_major, d_east2, d_north2, d_en2, dtutc, eps_ltc,
	H_t[sat_count][4];
	int it = 0; // Number of iterations
	int i,j;
	double iono[2];

	double bancroft_est[4] = {0,0,0,0};

	int eliminated = 0;

	sat_array[7] =  0;
	sat_array[8] =  0;
	sat_array[9] =  0;

	int sat_used = 0;

	int egnos = 1;

	// counter for ionospheric correction (increase if a PRN has an ionospheric correction)
	while (it < 20) {

		if (it == 0) {

			for (i = 0; i < sat_count; i++) {
				// GPST (Receiver time of reception - time of transmission)
				S_t[i].tow2 = S_t[i].tow - S_t[i].pr_c / SPEED_OF_LIGHT;
				if (egnos == 1) {
					// long term corr
					eps_ltc = set_LongCorrection(&S_t[i], msg24_t, msg25_t, msg10, msg1);
					// fast corrections
					get_fastCorrection(&S_t[i], msg1, msg2_5, msg24_t, msg6,
							msg10, msg7, eps_ltc, rnd_options);
				}

				// Computation of the GPS satellite position
				if (S_t[i].type_sat == 1)
					SV_position_computation(&S_t[i], egnos);

				// Computation of the EGNOS satellite position
				if (S_t[i].type_sat == 2)
					compute_EGNOSsat_position(&S_t[i], msg9);

				sigma2[i] = 1;

				if (egnos == 1) {
					int check_prn = 0;
					for (j = 0; j < (*msg1).prn_nb; j++) {
						if (S_t[i].prn == (*msg1).prn[j]) {
							check_prn = 1;
						}
					}
					if(check_prn == 0){
						S_t[i].prn_mask	= 0;
						S_t[i].use 	= 1;
					}

				// Convertion to ENU preparation
				r_pos_geo[0] = X_est[0];
				r_pos_geo[1] = X_est[1];
				r_pos_geo[2] = X_est[2];
				r_pos[0] = X_est[0];
				r_pos[1] = X_est[1];
				r_pos[2] = X_est[2];
				pos[0] = S_t[i].pos_x;
				pos[1] = S_t[i].pos_y;
				pos[2] = S_t[i].pos_z;
				cconv_to_geo(r_pos_geo);

				// Calculate azimuth and elevation angles of the satellite
				cconv_to_ENU(ENU, pos, r_pos, r_pos_geo);
				S_t[i].az = get_azimuth(ENU);
				S_t[i].el = get_elevation(ENU);

				if (S_t[i].el < 10) {
						eliminated++;
						S_t[i].use = 0;
						S_t[i].low_elv = 1;
					}
				}
			}

			if(X_est[0]==0 && X_est[1]==0 && X_est[2]==0 && X_est[3]==0)
			{
				// Initialization of receiver position estimation, initial guess computed with Bancroft method
				if(user_position_computation_bancroft(S_t,bancroft_est,sat_count)==1)
				{
					X_est[0] = bancroft_est[0];			// X
					X_est[1] = bancroft_est[1];			// Y
					X_est[2] = bancroft_est[2];			// Z
					X_est[3] = bancroft_est[3];
				}
			}
		}

		// Satellites loop
		sat_used = 0;
		for (i = 0; i < sat_count; i++) {

			if((S_t[i].use != 0 && egnos == 0) || (S_t[i].use == 2 && egnos == 1)) {
				sat_used++;
				// GPST (Receiver time of reception - time of transmission)
				S_t[i].tow2 = S_t[i].tow - S_t[i].pr_c / SPEED_OF_LIGHT;

				if (egnos == 1) {

					double UTCGPST = get_UTCoffset(&S_t[i], utc_data);

					// If message 12 available, time alignment t(ENT) = t(GPS) - dt(utc/GPST) + dt(utc/ENT)
					if((*msg12).tow != -1.0 && utc_data[8] == 1) {
						dtutc = (*msg12).dtls + (*msg12).a0snt + (*msg12).a1snt*(S_t[i].tow2
								- (*msg12).t0t + GPSWEEK_IN_SEC*((*msg12).gps_wknb - (*msg12).wnt));

						if ((UTCGPST - dtutc) < 1)
							S_t[i].tow2 = S_t[i].tow2 - UTCGPST + dtutc;
					}
				}

				// Update the GPS position
				if (S_t[i].type_sat == 1)
					SV_position_computation(&S_t[i], egnos);

				// Update the EGNOS position
				if (S_t[i].type_sat == 2)
					compute_EGNOSsat_position(&S_t[i], msg9);

				// Range
				R = sqrt(
						(S_t[i].pos_x - X_est[0]) * (S_t[i].pos_x - X_est[0])
						+ (S_t[i].pos_y - X_est[1])
						* (S_t[i].pos_y - X_est[1])
						+ (S_t[i].pos_z - X_est[2])
						* (S_t[i].pos_z - X_est[2]));

				//if(S_t[i].type_sat==1)
				SV_position_correction(&S_t[i], (R / SPEED_OF_LIGHT));

				// Range corrected after earth rotation compensation
				R_corrected = sqrt(
						(S_t[i].pos_x - X_est[0]) * (S_t[i].pos_x - X_est[0])
						+ (S_t[i].pos_y - X_est[1])
						* (S_t[i].pos_y - X_est[1])
						+ (S_t[i].pos_z - X_est[2])
						* (S_t[i].pos_z - X_est[2]));

				// Convertion to ENU preparation
				r_pos_geo[0] = X_est[0];
				r_pos_geo[1] = X_est[1];
				r_pos_geo[2] = X_est[2];
				r_pos[0] = X_est[0];
				r_pos[1] = X_est[1];
				r_pos[2] = X_est[2];
				pos[0] = S_t[i].pos_x;
				pos[1] = S_t[i].pos_y;
				pos[2] = S_t[i].pos_z;
				cconv_to_geo(r_pos_geo);

				// Calculate azimuth and elevation angles of the satellite
				cconv_to_ENU(ENU, pos, r_pos, r_pos_geo);
				S_t[i].az = get_azimuth(ENU);
				S_t[i].el = get_elevation(ENU);

				if (it > 2) {
					if (S_t[i].el < 10) {
						eliminated++;
						S_t[i].use = 0;
						sat_used--;
						S_t[i].low_elv = 1;
					}
				}

				double altitude = r_pos_geo[2];
				if(rnd_options[7] == 1)
					altitude = fabs(r_pos_geo[2]);

				// Compute ionospheric and tropospheric delay
				if(S_t[i].use == 2 && egnos ==1){

						get_ionoCorrection(&S_t[i],r_pos_geo[0],r_pos_geo[1],
								m18_t,m26_t,msg10, rnd_options[0]);

						if(S_t[i].sigma_uire2 > 1000)
							S_t[i].use = 0;

					get_tropoCorrection(&S_t[i],r_pos_geo[0],altitude);

						if(S_t[i].sigma_tropo2 > 1000)
							S_t[i].use = 0;

						if(S_t[i].sigma_flt2 > 1000)
							S_t[i].use = 0;

						// Airborne equipment multipath and noise models are not used for this implementation
						sigma2[i] = S_t[i].sigma_flt2 + S_t[i].sigma_tropo2 + S_t[i].sigma_uire2;
						//		+ 0.36*0.36 + get_sigma_mult2(S_t[i].el);
						//+ get_sigma_mult2(S_t[i].el) + get_sigma_noisedivg2(S_t[i].el);

						if(sigma2[i] == 0)      // avoid division by 0
							S_t[i].use = 0;

					S_t[i].sigma2 = sigma2[i];
					// Correction of pseudoranges
					S_t[i].pr_c = S_t[i].pr  + S_t[i].iono_delay+ S_t[i].tropo_delay + S_t[i].fast_delay;

					if(klob_data[8] == 1){
						ionospheric_model(klob_data, S_t[i],r_pos_geo[0],r_pos_geo[1], iono);
						S_t[i].iono_model = iono[0];
						if(S_t[i].iono_delay == 0 && (rnd_options[0] == 1)){// || rnd_options[0] == 2)){
							S_t[i].pr_c = S_t[i].pr_c - iono[0];
							sigma2[i] = sigma2[i] + iono[1];
						}
						S_t[i].sigma2 = sigma2[i];
					}

				}

				// Pseudorange residual
				dPR[i] = S_t[i].pr_c - R_corrected+ SPEED_OF_LIGHT*S_t[i].t_correction-X_est[3];

				// matrix H definition
				H[i][0] = (X_est[0]-S_t[i].pos_x) / R_corrected;
				H[i][1] = (X_est[1]-S_t[i].pos_y) / R_corrected;
				H[i][2] = (X_est[2]-S_t[i].pos_z) / R_corrected;
				H[i][3] = 1;

				H_t[i][0] = cos(S_t[i].el * PI / 180) * cos(S_t[i].az * PI / 180);
				H_t[i][1] = cos(S_t[i].el * PI / 180) * sin(S_t[i].az * PI / 180);
				H_t[i][2] = sin(S_t[i].el * PI / 180);
				H_t[i][3] = 1;

			} else {//if no pseudoranges data for the satellite available => fill the matrixes with
				R = 0;
				H[i][0] = 0;
				H[i][1] = 0;
				H[i][2] = 0;
				H[i][3] = 0;

				H_t[i][0] = 0;
				H_t[i][1] = 0;
				H_t[i][2] = 0;
				H_t[i][3] = 0;

				dPR[i] = 0;

				ENU[0] = 0;
				ENU[1] = 0;
				ENU[2] = 0;

				sigma2[i] = 1;
			}
			// Weight matrix (Diagonal matrix 15x15 with the elevation)
			for (j = 0; j < sat_count; j++) {
				if(i == j && (( S_t[i].use != 0 && egnos == 0) || (S_t[i].use == 2 && egnos == 1)) ) {
					if(egnos == 1 && rnd_options[7] != 1 && rnd_options[5] != 1)
					{
						W[i][j] = 1/sigma2[i];
					}
					else
					{
						W[i][j] = S_t[i].cn0;
					}
				} else {
					W[i][j] =  0;
				}
			}
		}

		// Test if number of satellites > 3
		if (sat_used < 4)
			break;

		// Iterations increment
		it++;

		double HtW[4][sat_count], HtWH[4][4], HtWH_[4][4], HtWH_HtW[4][sat_count];
		double HtH[4][4], HtH_[4][4], Ht[4][sat_count];

		// Computation of inv(trans(H).H).trans(H)
		size_b_row1=sat_count;
		size_b_col1=4;
		transpose(H,Ht);						//change
		size_b_row1=4;
		size_b_col1=sat_count;
		size_b_row2=sat_count;
		size_b_col2=4;
		multiply(Ht,H,HtH);					//change
		inv_44(HtH, HtH_);
		size_b_col2=sat_count;
		// Calculation with the weight matrix
		multiply(Ht,W,HtW);
		size_b_col2=4;
		multiply(HtW,H,HtWH);					//change
		inv_44(HtWH, HtWH_);
		size_b_col1=4;
		size_b_row2=4;
		size_b_col2=sat_count;
		multiply(HtWH_,HtW,HtWH_HtW);
		size_b_col1=sat_count;
		size_b_row2=sat_count;
		size_b_col2=4;
		multiply_matxvec(HtWH_HtW,dPR,dX);

		// update estimation
		X_est[0] += dX[0];
		X_est[1] += dX[1];
		X_est[2] += dX[2];
		X_est[3] += dX[3];

		// DOP
		DOP[0] = get_HDOP(HtH_);
		DOP[1] = get_VDOP(HtH_);
		DOP[2] = get_PDOP(HtH_);
		DOP[3] = get_TDOP(HtH_);

		// Check if HDOP is too high, position cannot be resolved
		if(DOP[0] > 20) {
			sat_used = 0;  // the function will return 0
			break;
		}

		if(egnos == 1) {

			double HtW[4][sat_count], HtWH[4][4], HtWH_[4][4];
			double HtH[4][4], HtH_[4][4], Ht[4][sat_count];

			size_b_row1=sat_count;
			size_b_col1=4;
			transpose(H_t,Ht);
			size_b_row1=4;
			size_b_col1=sat_count;
			size_b_row2=sat_count;
			size_b_col2=4;
			multiply(Ht,H_t,HtH);
			inv_44(HtH, HtH_);
			size_b_col2=sat_count;
			// Computation with the weight matrix
			multiply(Ht,W,HtW);
			size_b_col2=4;
			multiply(HtW,H_t,HtWH);
			inv_44(HtWH, HtWH_);

			// computation of HPL from matrix inv(Ht.W.H)
			d_east2 = HtWH_[0][0];
			d_north2 = HtWH_[1][1];
			d_en2 = HtWH_[0][1]*HtWH_[0][1];

			d_major = sqrt(((d_east2 + d_north2) / 2) + sqrt(((d_east2 - d_north2) / 2)	*((d_east2 - d_north2)/2) + d_en2));
			PL[0] = d_major;           //HPL without K factor

			PL[1] = sqrt(HtWH_[2][2]); //VPL without K factor; VPL=du.K
		}

		// Geodetic conversion
		pos[0] = X_est[0];
		pos[1] = X_est[1];
		pos[2] = X_est[2];
		cconv_to_geo(pos);

		double norm=sqrt(dX[0]*dX[0]+dX[1]*dX[1]+dX[2]*dX[2]);
		if(norm < 1E-8 && it > 6)	  break;
	}

	return sat_used;
}

/**
 * user_position_computation_bancroft function
 * Calculation of the receiver positions, Bancroft method
 * @param   S_t         The table of Satellites
 * @param   pos         The destination table of position and receiver clock bias
 * @param   sat_count   The number of satellites used to compute the position
 * @return  1 if successfull, 0 if not
 */
int user_position_computation_bancroft(Satellite S_t[19], double pos[4],int sat_count)
{
	double M[4] = {1,1,1,-1};
	double Bt[4][sat_count], BtB[4][4],  BtB_[4][4], BtB_Bt[4][sat_count],BtB_BtE[4];
	double BtB_BtAlpha[4],B[sat_count][4],rb[2][4],E[sat_count],Alpha[sat_count];
	double a,b,c,delta,r1,r2;//2nd order equation parameters
	int i,j;
	int r = 1;
	double x, y, z, traveltime, range, angle, cosa, sina;
	int iter;

	// Satellites loop
	for (j = 0; j < sat_count; j++) {
		// Matrix B definitions
		B[j][0] = S_t[j].pos_x;
		B[j][1] = S_t[j].pos_y;
		B[j][2] = S_t[j].pos_z;
		B[j][3] = S_t[j].pr + SPEED_OF_LIGHT * S_t[j].t_correction;

		E[j] = 1;
	}
	//compensate satellite position for earth rotation

	for (iter = 0; iter < 2; iter++) {
		for (i = 0; i < sat_count; i++) {
			x = B[i][0];
			y = B[i][1];
			if (iter == 0) {
				traveltime = 0.072;
			} else {
				z = B[i][2];
				range = pow((x-pos[0]),2)+pow((y-pos[1]),2)+pow((z-pos[2]),2);
				traveltime = sqrt(range)/SPEED_OF_LIGHT;
			}
			angle = traveltime * 7.292115147e-5;
			cosa = cos(angle);
			sina = sin(angle);
			B[i][0] = cosa * x + sina * y;
			B[i][1] = -sina * x + cosa * y;
		}

		for (j = 0; j < sat_count; j++) {
			// Matrix Alpha
			Alpha[j] = 0.5 * lorentz_4_4(B[j], B[j]);
		}

		// calculation of B+ = inv(trans(B).B).trans(B)
		size_b_row1 = sat_count;
		size_b_col1 = 4;
		transpose(B, Bt);
		size_b_row1 = 4;
		size_b_col1 = sat_count;
		size_b_row2 = sat_count;
		size_b_col2 = 4;
		multiply(Bt, B, BtB);
		inv_44(BtB, BtB_);
		size_b_col1 = 4;
		size_b_row2 = 4;
		size_b_col2 = sat_count;
		multiply(BtB_, Bt, BtB_Bt);
		size_b_col1 = sat_count;
		// calculation of the matrixes B+.E and B+.Alpha
		multiply_matxvec(BtB_Bt, E, BtB_BtE);
		multiply_matxvec(BtB_Bt, Alpha, BtB_BtAlpha);

		// Solving the 2nd order equation
		a = lorentz_4_4(BtB_BtE, BtB_BtE);
		b = 2 * (lorentz_4_4(BtB_BtE, BtB_BtAlpha) - 1);
		c = lorentz_4_4(BtB_BtAlpha, BtB_BtAlpha);
		delta = b * b - 4 * a * c;

		if (delta < 0)
			r = 0;
		else {
			// The two solutions
			r1 = (-b - sqrt(delta)) / (2 * a);
			r2 = (-b + sqrt(delta)) / (2 * a);

			// Solving position equations; pos = r*M*BtB_BtE + M*BtB_BtAlpha
			for (i = 0; i < 4; i++) {
				rb[0][i] = M[i] * (r1 * BtB_BtE[i] + BtB_BtAlpha[i]);
				rb[1][i] = M[i] * (r2 * BtB_BtE[i] + BtB_BtAlpha[i]);
			}
			// Finding the right solution; the right values are the ones which the radius is the closest
			// to the earth radius (defined here with the WGS84 value)
			if(fabs(sqrt(rb[0][0]*rb[0][0] + rb[0][1]*rb[0][1] + rb[0][2]*rb[0][2]) - a_WGS84)
					< fabs(sqrt(rb[1][0]*rb[1][0] + rb[1][1]*rb[1][1] + rb[1][2]*rb[1][2]) - a_WGS84))
			{
				pos[0] = rb[0][0];
				pos[1] = rb[0][1];
				pos[2] = rb[0][2];
				pos[3] = rb[0][3];
			}
			else
			{
				pos[0] = rb[1][0];
				pos[1] = rb[1][1];
				pos[2] = rb[1][2];
				pos[3] = rb[1][3];
			}
		}
	}

	return r;
}

/**
 * get_latitude function
 * return the receiver latitude value
 * @param *vect Pointer of the table containing the position in geodetic coordinates
 */
double get_latitude(double vect[3]) {
	return vect[0];
}

/**
 * get_longitude function
 * return the receiver longitude value
 * @param *vect Pointer of the table containing the position in geodetic coordinates
 */
double get_longitude(double vect[3]) {
	return vect[1];
}
/**
 * get_height function
 * return the receiver longitude value
 * @param *vect Pointer of the table containing the position in geodetic coordinates
 */
double get_height(double vect[3]) {
	return vect[2];
}

/**
 * cconv_to_cart function
 * Conversion from Geodetic to Cartesian coordinates (WGS84)
 * @param   *vect   Pointer of the vector with initial Geodetic values(phi,lambda,h)
 */
void cconv_to_cart(double * vect) {
	double N, X, Y, Z; //vect[0] -> phi, vect[1] -> lambda, vect[2] -> height

	vect[0] *= PI / 180; // Conversions to rad
	vect[1] *= PI / 180;

	N = a_WGS84/sqrt(1-e_WGS84_SQUARED*sin(vect[0])*sin(vect[0]));
	X = (N + vect[2])*cos(vect[0])*cos(vect[1]);
	Y = (N + vect[2])*cos(vect[0])*sin(vect[1]);
	Z = (((b_WGS84*b_WGS84)/(a_WGS84*a_WGS84))*N + vect[2])*sin(vect[0]);

	vect[0] = X;
	vect[1] = Y;
	vect[2] = Z;
}

/**
 * cconv_to_geo function
 * Conversion from Cartesian to Geodetic coordinates (WGS84)
 * @param   *vect   Pointer of the vector with initial Cartesian values (X,Y,Z)
 */
void cconv_to_geo(double * vect) {
	double h_1, phi_1, phi, lambda, h, k, N, j;

	// To simplify, defining the constant k
	k = sqrt(vect[0] * vect[0] + vect[1] * vect[1]);

	// 1st value of phi
	phi = atan2(vect[2], k * (1 - e_WGS84_SQUARED));

	// Start with h=0
	h = 0;

	for (j = 0; j < 5; j++) {
		N = a_WGS84 / sqrt(1 - e_WGS84_SQUARED * sin(phi) * sin(phi));
		h_1 = h;
		h = k / cos(phi) - N;
		phi_1 = phi;
		phi = atan2(vect[2], k * (1.0 - e_WGS84_SQUARED * (N / (N + h))));

		if (fabs(phi - phi_1) < 1E-9 && fabs(h - h_1) < 1E-9 * a_WGS84)
			break;
	}
	lambda = atan2(vect[1], vect[0]);
	//if(lambda < 0.0)
	//  lambda += 2*PI;

	// Conversions to deg
	vect[0] = phi * 180 / PI;
	vect[1] = lambda * 180 / PI;
	vect[2] = h;
}

/**
 * get_GDOP function
 * Calculation of the Geometric Dilution of Precision, form the values of D matrix
 * @param   D   The D matrix inv(trans(H).H))
 * @return    The computed GDOP
 */
double get_GDOP(double D[4][4]) {
	return sqrt(D[0][0] + D[1][1] + D[2][2] + D[3][3]);
}

/**
 * get_PDOP function
 * Calculation of the Position Dilution of Precision, form the values of D matrix
 * @param   D   The D matrix inv(trans(H).H))
 * @return    The computed PDOP
 */
double get_PDOP(double D[4][4]) {
	return sqrt(D[0][0] + D[1][1] + D[2][2]);
}

/**
 * get_HDOP function
 * Calculation of the Horizontal Dilution of Precision, form the values of D matrix
 * @param   D   The D matrix inv(trans(H).H))
 * @return    The computed HDOP
 */
double get_HDOP(double D[4][4]) {
	return sqrt(D[0][0] + D[1][1]);
}

/**
 * get_TDOP function
 * Calculation of the Time Dilution of Precision, form the values of D matrix
 * @param   D   The D matrix inv(trans(H).H))
 * @return    The computed TDOP
 */
double get_TDOP(double D[4][4]) {
	return sqrt(D[3][3]);
}

/**
 * get_VDOP function
 * Calculation of the Vertical Dilution of Precision, form the values of D matrix
 * @param   D   The D matrix inv(trans(H).H))
 * @return    The computed VDOP
 */
double get_VDOP(double D[4][4]) {
	return sqrt(D[2][2]);
}

/**
 * get_sigma_mult2 function
 * Calculation of the multipath error squared - DO-229D J.2.4
 * @param   elevation   The Satellite elevation
 * @return    The computed error in meters^2
 */
double get_sigma_mult2(double elevation) {
	double sigma_mult;

	if (elevation >= 2)
		sigma_mult = 0.13 + 0.53 * exp(-elevation / 10);
	else
		sigma_mult = 0;

	return sigma_mult * sigma_mult;
}

/**
 * get_sigma_noisedivg2 function
 * Calculation of the noise error squared plus the divg error squared - DO-229D J.2.4; User Guide for EGNOS Annexe 7
 * @param   elevation   The Satellite elevation
 * @return    The computed error sum in meters^2
 */
double get_sigma_noisedivg2(double elevation) {
	double sigma;

	if (elevation >= 5)
		sigma = (0.36 + (0.15 - 0.36) * (elevation - 5) / (90 - 5)); //Interpolation
	else
		sigma = 0;

	return sigma * sigma;
}

/**
 * ionospheric_model function
 * Computation of the ionopsheric correction based on the Klobuchar model
 * @param   klob_data		The ionopsheric coeficients alpha nad beta
 * @param   S				The Satellite structure
 * @param   lat    			The latitude of the user
 * @param   lon				The latitude of the user
 * @return    				The ionopsheric correction
 */
void ionospheric_model(double klob_data[9], Satellite S,double lat,double lon, double iono[2]){

	double alpha[4];
	double beta[4];
	double psi, iono_lat, iono_lon, lat_m, t, sF, per,amp, x, dIon, lat_smcirc, lon_smcirc;
	int i;

	for (i = 0; i < 4; i++) {
		alpha[i] = klob_data[i];
		beta[i] = klob_data[i + 4];
	}

	double azm = S.az * PI / 180; // conversion from degrees to radians
	double elv = S.el / 180; // conversion from degrees to semicircles

	lat_smcirc = lat / 180; // conversion from degrees to semicircles
	lon_smcirc = lon / 180; // conversion from degrees to semicircles

	//  Compute the earth-centered angle
	psi = 0.0137 / (elv + 0.11) - 0.022;

	//  Compute the subionospheric latitude
	iono_lat = lat_smcirc + psi * cos(azm);

	if (iono_lat > 0.416)
		iono_lat = 0.416;
	else if (iono_lat < -0.416)
		iono_lat = -0.416;

	//  Compute the subionospheric longitude
	iono_lon = lon_smcirc + psi * sin(azm) / cos(iono_lat * PI);

	//  Calculate the geomagnetic latitude of the earth projection of the ionospheric intersection point
	lat_m = iono_lat + 0.064 * cos((iono_lon - 1.617) * PI);

	//  Calculate the slant factor
	sF = 1 + 16 * (0.53 - elv) * (0.53 - elv) * (0.53 - elv);

	//  Calculate the period by using the beta terms from the GPS message
	per = beta[0] + beta[1] * lat_m + beta[2] * lat_m * lat_m
			+ beta[3] * lat_m * lat_m * lat_m;
	if (per < 72000)
		per = 72000;

	//  Calculate the amplitude by using the alpha terms from the GPS message
	amp = alpha[0] + alpha[1] * lat_m + alpha[2] * lat_m * lat_m
			+ alpha[3] * lat_m * lat_m * lat_m;
	if (amp < 0)
		amp = 0;

	//  Find the local time at the subionospheric point
	t = 4.32e4 * iono_lon + S.tow;
	t = t - ((int) t / 86400) * 86400;
	if (t > 86400)
		t = t - 86400;
	if (t < 0)
		t = t + 86400;

	// Calculate the argument of the cosine term
	x = 2 * PI * (t - 50400) / per;

	//  Determine the ionospheric correction (in meters)
	if (fabs(x) < PI / 2)
		dIon = sF * (5.E-9 + amp * (1 - x * x / 2 + x * x * x * x / 24));
	else
		dIon = sF * (5.E-9);

	dIon = SPEED_OF_LIGHT * dIon;

	double sigma[2], Fpp, Sig_vert, sigma_iono;

	Fpp = get_fpp((S).el);

	lat_m = lat_m * 180;
	if (lat_m <= 20)
		Sig_vert = 9;

	if (lat_m <= 55 && lat_m > 20)
		Sig_vert = 4.5;

	if (lat_m > 55)
		Sig_vert = 6;

	sigma[0] = (dIon/5) * (dIon/5);
	sigma[1] = (Fpp * Sig_vert) * (Fpp * Sig_vert);

	if(sigma[0] > sigma[1])
		sigma_iono = sigma[0];
	else
		sigma_iono = sigma[1];

	iono[0] = dIon;
	iono[1] = sigma_iono;
}

/**
 * DOPpos_computation function
 * R&D method for computing the user position after eliminating
 * from the satellite constellation the satellite that maximizes the PDOP
 * @param S_t			The Satellite structure for all satellites in view
 * @param X_est			Position result in ECEF coordinates
 * @param sat_count		The number of satellites in view
 * @param pos			Position result expressed in latitude, longitude and altitude
 * @param H_all			The geometry matrix
 * @param W_all			The weight matrix
 * @param msg9			The Egnos message type 9 pointer
 * @param dPR			The pseudorange residual
 */
void DOPpos_computation(Satellite S_t[19], double X_est[4], int sat_count, double pos[3],
		double H_all[sat_count][4],double W_all[sat_count][sat_count],
		Egnos_msg * msg9, double dPR[sat_count]){

	int i,j;
	double hdop_vec[sat_count];
	double gdop_vec[sat_count];
	double pdop_vec[sat_count];

	for(i=0; i<sat_count; i++){

		if(S_t[i].use == 2){
			int k = 0;
			double h_small[sat_count-1][4];
			double h_smallt[4][sat_count-1];
			double h_smallth_small[4][4];
			double h_smallth_small_[4][4];
			int m;

			if (i > 0)
				while (k < i) {
					for (j = 0; j < 4; j++)
						h_small[k][j] = H_all[k][j];
					k++;
				}
			for (m = i + 1; m < sat_count; m++) {
				for (j = 0; j < 4; j++)
					h_small[k][j] = H_all[m][j];
				k++;
			}

			size_b_row1 = sat_count - 1;
			size_b_col1 = 4;
			transpose(h_small, h_smallt);
			size_b_row1 = 4;
			size_b_col1 = sat_count - 1;
			size_b_row2 = sat_count - 1;
			size_b_col2 = 4;
			multiply(h_smallt, h_small, h_smallth_small);
			inv_44(h_smallth_small, h_smallth_small_);

			hdop_vec[i] = get_HDOP(h_smallth_small_);
			gdop_vec[i] = get_GDOP(h_smallth_small_);
			pdop_vec[i] = get_PDOP(h_smallth_small_);

		} else {
			hdop_vec[i] = 100;
			gdop_vec[i] = 100;
			pdop_vec[i] = 100;
		}
	}
	double Min = pdop_vec[0];
	int pos_min = 0;
	for (i = 0; i < sat_count; i++)
		if (pdop_vec[i] < Min) {
			Min = pdop_vec[i];
			pos_min = i;
		}

	S_t[pos_min].use = 0;

	double W[sat_count][sat_count];
	for(i = 0; i< sat_count; i++)
		for(j = 0; j< sat_count; j++){
			if(i == j && S_t[i].use == 2)
				W[i][j] = W_all[i][j];
			else
				W[i][j] = 0;
		}

	double H[sat_count][4], y[sat_count], DOP[4], dX[4];
	//test if the two(here and beelow are equivalent)
	GPSposition_computation(S_t, sat_count, H, W, y, X_est, pos, DOP, dX, msg9);

	double result_wls[2], result_dop[2], dx[4];

	check_residual(sat_count, H_all, W_all, dPR, result_wls, dx);
	check_residual(sat_count, H, W, y, result_dop, dX);

	if(result_wls[0] <= result_dop[0] * 1.4){
		pos[0] = 0;
		pos[1] = 0;
		pos[2] = 0;
	}
}

/**
 * TwoDpos_computation function
 * Algorithm for computing the user position when only 3 satellites are available
 * @param S_t			The Satellite structure for all satellites in view
 * @param X_est			Position result in ECEF coordinates
 * @param *msg1       	The pointer of the message type 1
 * @param msg2_5      	The table of the the messages type 2-5
 * @param *msg6       	The pointer of the message type 6
 * @param *msg7       	The pointer of the message type 7
 * @param *msg9       	The pointer of the message type 9
 * @param *msg10      	The pointer of the message type 10
 * @param *msg12      	The pointer of the message type 12
 * @param *msg17      	The pointer of the message type 17
 * @param m18_t       	The table of the messages type 18
 * @param msg24_t     	The table of the messagea type 24
 * @param msg25_t     	The Egnos messages type 25 table
 * @param m26_t       	The table of the messages type 26
 * @param egnos       	The EGNOS flag (1:EGNOS enabled, 0:EGNOS disabled)
 * @param *iono_flag  	The pointer of the flag to check if all needed messages for ionospheric correction are available
 * @param m18_char    	The table of 5(max) messages 18 in 256 characters (for SIS)
 * @param m26_char    	The table of 25(max) messages 26 in 256 characters (for SIS)
 * @param sat_count   	The number of satellites used to compute the position
 * @param sat_array   	[0] = total no. of satellites in view
 *                    	[1] = No. of Satellites eliminated for Low Elevation (under 10 deg.)
 *                    	[2] = No. of Sats not set in PRN mask (currently 1 and 25)
 *                    	[3] = No. of iterations [15] = 1 if position jump is more than 1.5m
 *                    	[4] = position jump on x-axis [5] = position jump on y-axis
 *                    	[6] = The number of satellites used to compute the position
 * @param utc_data	  	The array of UTC parameters
 * @param klob_data	  	Array that holds the Klobuchar coefficients for ionopsheric corrections
 * @param rnd_options	Array that holds the R&D options
 * @return			  	Number of satellites used in the computation
 */
int TwoDpos_computation(Satellite S_t[19], double X_est[4], Egnos_msg * msg1,
		Egnos_msg msg2_5[4][2],	Egnos_msg * msg6, Egnos_msg * msg7,
		Egnos_msg * msg9, Egnos_msg * msg10, Egnos_msg * msg12, Egnos_msg * msg17,
		Egnos_msg m18_t[11], Egnos_msg msg24_t[25], Egnos_msg msg25_t[15],
		Egnos_msg m26_t[25], int egnos, int * iono_flag, char m18_char[5][263],
		char m26_char[25][263], int sat_count, double sat_array[15],
		double utc_data[9], double klob_data[9], int rnd_options[8]) {

	double H3[sat_count][3], dX3[3], dPR[sat_count], sigma2[sat_count];
	double H3tW[3][sat_count], H3tWH3[3][3], H3tWH3_[3][3], H3tWH3_H3tW[3][sat_count]; // with weight matrix
	double H3tH3[3][3], H3tH3_[3][3], H3t[3][sat_count];
	double R, ENU[3], W[sat_count][sat_count], R_corrected, eps_ltc;
	double r_pos[3], r_pos_geo[3], pos[3], dtutc;
	int it = 0; // Number of iterations
	int nb_igps = 0;
	int iono_count = 0;
	int i, j;
	
	int no_egnos;

	int total_sats = sat_count; 
	int eliminated = 0;

	double altitude = X_est[2];

	int sat_used = 0;

	// Init flag to 0
	*iono_flag = 0;

	// counter for ionospheric correction (increase if a PRN has an ionospheric correction)
	iono_count = 0;
	while (it < 20) {
		if (it == 0) {
			for (i = 0; i < sat_count; i++) {
				// GPST (Receiver time of reception - time of transmission)
				S_t[i].tow2 = S_t[i].tow - S_t[i].pr_c / SPEED_OF_LIGHT;
				if (egnos == 1) {
					// long term corr
					eps_ltc = set_LongCorrection(&S_t[i], msg24_t, msg25_t, msg10, msg1);
					// fast corrections

					get_fastCorrection(&S_t[i], msg1, msg2_5, msg24_t, msg6, msg10, msg7, 
							eps_ltc, rnd_options);

				}

				// Computation of the GPS satellite position
				if (S_t[i].type_sat == 1)
					SV_position_computation(&S_t[i], egnos);

				// Computation of the EGNOS satellite position
				if (S_t[i].type_sat == 2)
					compute_EGNOSsat_position(&S_t[i], msg9);
			}

			if (egnos == 1) {
				// Set the ionospheric grid
				set_ionoGridSis(m18_t, m26_t, m18_char, m26_char);
			}
		}

		// Satellites loop
		sat_used = 0;
		for (i = 0; i < sat_count; i++) {
			int check_prn = 1;

			if (egnos == 1) {
				check_prn = 0;
				for (j = 0; j < (*msg1).prn_nb; j++) {
					if (S_t[i].prn == (*msg1).prn[j]) {
						check_prn = 1;
					}
				}
				if(check_prn == 0)
					S_t[i].use = 1;
			}

			if(S_t[i].use != 0) {
				sat_used++;
				// GPST (Receiver time of reception - time of transmission)
				S_t[i].tow2 = S_t[i].tow - S_t[i].pr_c / SPEED_OF_LIGHT;

				if (egnos == 1) {

					double UTCGPST = get_UTCoffset(&S_t[i], utc_data);

					// If message 12 available, time alignment t(ENT) = t(GPS) - dt(utc/GPST) + dt(utc/ENT)
					if((*msg12).tow != -1) {
						dtutc = (*msg12).dtls + (*msg12).a0snt + (*msg12).a1snt*(S_t[i].tow2
								- (*msg12).t0t + GPSWEEK_IN_SEC*((*msg12).gps_wknb - (*msg12).wnt));

						if ((UTCGPST - dtutc) < 1)
							S_t[i].tow2 = S_t[i].tow2 - UTCGPST + dtutc;
					}
				}

				// Update the GPS position
				if (S_t[i].type_sat == 1)
					SV_position_computation(&S_t[i], egnos);

				// Update the EGNOS position
				if (S_t[i].type_sat == 2 && egnos == 1)
					compute_EGNOSsat_position(&S_t[i], msg9);

				// Range
				R = sqrt(
						(S_t[i].pos_x - X_est[0]) * (S_t[i].pos_x - X_est[0])
						+ (S_t[i].pos_y - X_est[1])
						* (S_t[i].pos_y - X_est[1])
						+ (S_t[i].pos_z - X_est[2])
						* (S_t[i].pos_z - X_est[2]));

				SV_position_correction(&S_t[i], (R / SPEED_OF_LIGHT));

				// Range corrected after earth rotation compensation
				R_corrected = sqrt(
						(S_t[i].pos_x - X_est[0]) * (S_t[i].pos_x - X_est[0])
						+ (S_t[i].pos_y - X_est[1])
						* (S_t[i].pos_y - X_est[1])
						+ (S_t[i].pos_z - X_est[2])
						* (S_t[i].pos_z - X_est[2]));

				// Convertion to ENU preparation
				r_pos_geo[0] = X_est[0];
				r_pos_geo[1] = X_est[1];
				r_pos_geo[2] = X_est[2];
				r_pos[0] = X_est[0];
				r_pos[1] = X_est[1];
				r_pos[2] = X_est[2];
				pos[0] = S_t[i].pos_x;
				pos[1] = S_t[i].pos_y;
				pos[2] = S_t[i].pos_z;
				cconv_to_geo(r_pos_geo);

				// Calculate azimuth and elevation angles of the satellite
				cconv_to_ENU(ENU, pos, r_pos, r_pos_geo);
				S_t[i].az = get_azimuth(ENU);
				S_t[i].el = get_elevation(ENU);

				if (it > 2) {
					if (S_t[i].el < 10) {
						eliminated++;
						S_t[i].use = 0;
						S_t[i].low_elv = 1;
					}
				}

				if (egnos == 1) {
					// Compute ionospheric and tropospheric delay
					if(it == 4 && S_t[i].use == 2) {
						get_ionoCorrection(&S_t[i],r_pos_geo[0],r_pos_geo[1],
								m18_t,m26_t,msg10,3);
						if(nb_igps > 2)// || nb_igps == -1)
							iono_count++;

						if (S_t[i].sigma_uire2 > 1000)
							S_t[i].sigma_uire2 = 0;
						get_tropoCorrection(&S_t[i],r_pos_geo[0],r_pos_geo[2]);

						if (S_t[i].sigma_tropo2 > 1000)
							S_t[i].sigma_tropo2 = 0;

						if (S_t[i].sigma_flt2 > 1000)
							S_t[i].sigma_flt2 = 0;

						// Airborne equipment multipath and noise models are not used for this implementation
						sigma2[i] = S_t[i].sigma_flt2 + S_t[i].sigma_tropo2 + S_t[i].sigma_uire2;
						//+ get_sigma_mult2(S_t[i].el) + get_sigma_noisedivg2(S_t[i].el);

						if(sigma2[i] == 0)      // avoid division by 0
							sigma2[i] = 1;

						S_t[i].pr_c = S_t[i].pr  + S_t[i].iono_delay+ S_t[i].tropo_delay + S_t[i].fast_delay;

					}
				}

				// Pseudorange residual
				dPR[i] = S_t[i].pr_c - R_corrected+ SPEED_OF_LIGHT*S_t[i].t_correction-X_est[3];

				// matrix H definition
				H3[i][0] = (X_est[0] - S_t[i].pos_x) / S_t[i].pr_c;
				H3[i][1] = (X_est[1] - S_t[i].pos_y) / S_t[i].pr_c;
				H3[i][2] = 1;

			} else { //if no pseudoranges data for the satellite available => fill the matrixes with
				R = 0;

				H3[i][0] = 0;
				H3[i][1] = 0;
				H3[i][2] = 0;

				dPR[i] = 0;

				ENU[0] = 0;
				ENU[1] = 0;
				ENU[2] = 0;

				//	sigma2[i] = 1;
			}
			// Weight matrix (Diagonal matrix 12x12 with the elevation)
			for (j = 0; j < sat_count; j++) {
				if (i == j) {
					if (egnos == 1) {
						W[i][j] = S_t[i].cn0; //1/sigma2[i];
					} else {
						W[i][j] = S_t[i].cn0;
					}
				} else {
					W[i][j] = 0;
				}
			}
		}

		if (sat_used < 3)
			break;

		it++;

		size_b_row1 = sat_count;
		size_b_col1 = 3;
		transpose(H3, H3t);
		size_b_row1 = 3;
		size_b_col1 = sat_count;
		size_b_row2 = sat_count;
		size_b_col2 = 3;
		multiply(H3t, H3, H3tH3);
		inv_33(H3tH3, H3tH3_);
		size_b_col2 = sat_count;
		// Calculation with the weight matrix
		multiply(H3t, W, H3tW);
		size_b_col2 = 3;
		multiply(H3tW, H3, H3tWH3);
		inv_33(H3tWH3, H3tWH3_);
		size_b_col1 = 3;
		size_b_row2 = 3;
		size_b_col2 = sat_count;
		multiply(H3tWH3_, H3tW, H3tWH3_H3tW);
		size_b_col1 = sat_count;
		size_b_row2 = sat_count;
		size_b_col2 = 3;
		multiply_matxvec(H3tWH3_H3tW, dPR, dX3);

		// update estimation
		X_est[0] += dX3[0];
		X_est[1] += dX3[1];
		X_est[2] = altitude;
		X_est[3] += dX3[2];

		// Geodetic conversion
		pos[0] = X_est[0];
		pos[1] = X_est[1];
		pos[2] = X_est[2];
		cconv_to_geo(pos);

		double norm = sqrt(dX3[0] * dX3[0] + dX3[1] * dX3[1] + dX3[2] * dX3[2]);

		if(norm<1E-8 && it>5)	  break;
	}
	
		no_egnos=total_sats-sat_used-eliminated;
	
	// check for satellites without ionospheric corrections
	*iono_flag = egnos;
	for (i = 0; i < sat_count; i++)
		if (S_t[i].use == 2 && egnos == 1 ){
			if( S_t[i].iono_delay == 0)
				*iono_flag = 0;
			else
				iono_count++;
		}
	
//	 android_syslog(ANDROID_LOG_INFO,
//      "Total Sats: %d Low Elv.:%d Missing from mask: %d Required iono: %d Iono_count: %d\n", total_sats,
//      eliminated, no_egnos, sat_used, iono_count);
//	android_syslog(ANDROID_LOG_INFO, "Iono flag: %d\n", *iono_flag);

	return sat_used;
}

/**
 * raim function
 * Method that implements the Receiver Autonomous Integrity Monitoring
 * @param S_t			The Satellite structure for all satellites in view
 * @param sat_count		The number of satellites in view
 * @param H				The geometry matrix
 * @param W				The weight matrix
 * @param dPR			The pseudorange residual
 * @param X_est			Position result in ECEF coordinates
 * @param dX			The last update to the position
 * @param pos			Position result expressed in latitude, longitude and altitude
 * @param msg9			The Egnos message type 9 pointer
 * @return				1 if successfull, or 0 is error
 */
int raim(Satellite S_t[19], int sat_count, double H[sat_count][4], int mode,
		double W[sat_count][sat_count], double y[sat_count], double X_est[4], double dx[4],
		double pos[3], Egnos_msg * msg9) {

	double pos_gps[3];
	double ChiSq;
	int ok = 0;
	int i, j;
	double residual[sat_count];
	double nalfa0 = 1.61182512114663; //the inverse cumulative distribution of a normal distribution with probability 1-0.107

	int sat_used = 0;
	int sat_detected = 0;

	if(sat_count > 4){
		double residual_check[2];
		check_residual(sat_count, H, W, y, residual_check, dx);

		sat_used = residual_check[1];
		int degfree = sat_used - 4;
		ChiSq = calcchisquare(degfree);

		double Hdx[sat_count];

		size_b_row1 = sat_count;
		size_b_col1 = 4;
		size_b_row2 = 4;
		size_b_col2 = 1;
		multiply_matxvec(H, dx, Hdx);
		size_b_row1 = sat_count;
		size_b_col1 = 1;
		subtract_vec(y, Hdx, residual);

		double val = 0;
		for (i = 0; i < sat_count; i++)
			val += (residual[i] * residual[i]);

		val = sqrt(val);

		if (residual_check[0] > ChiSq)
			ok = 0; // local test is required
		else
			ok = 1;
	}

	if(ok == 1)
	{
		android_syslog(ANDROID_LOG_INFO, "Global test Passed ! \n");
	}
	else if(ok == 0)
	{
		android_syslog(ANDROID_LOG_INFO, "Global test Failed ! \n");
	}


	// var 1
	/*	while( ok == 0){
		int status;
		double pos_vec[sat_count][3];
		double test_val[sat_count];
		double residual[2];
		double min;
		int pos_min;

		if(sat_used > 4){
			for(i = 0; i< sat_count; i++)
				if (S_t[i].use == 2)
				{
					S_t[i].use = 0;
					double H_gps[sat_count][4], y_gps[sat_count], DOP_gps[4], dx_gps[4];
					GPSposition_computation(S_t, sat_count, H_gps, W, y_gps, X_est, pos_gps, DOP_gps, dx_gps);
					pos_vec[i][0] = pos_gps[0];
					pos_vec[i][1] = pos_gps[1];
					pos_vec[i][2] = pos_gps[2];
					status = check_residual(sat_count, H_gps, W, y_gps, residual, dx_gps);
					test_val[i] = residual[0];
					S_t[i].use = 2;
				}else{
					test_val[i] = 1E+7;
				}

			min = test_val[0];
			pos_min = 0;
			for (i = 0; i < sat_count; i++)
				if (test_val[i] < min) {
					min = test_val[i];
					pos_min = i;
				}

			S_t[pos_min].use = 0;

			pos[0] = pos_vec[pos_min][0];
			pos[1] = pos_vec[pos_min][1];
			pos[2] = pos_vec[pos_min][2];

			if (test_val[pos_min] >  calcchisquare(sat_used-4-1)){
				ok = 0;
				sat_used = sat_used - 1;
				if(sat_used < 5){
					break;
				}
			}
			else ok = 1;

		}else{
			pos[0] = 0;
			pos[1] = 0;
			pos[2] = 0;
		}
	}*/

	//var 2
	// compute (res-mean)/std_dev

	while( ok == 0){
		int status;
		double pos_vec[sat_count][3];
		double res_vec[sat_count];

		if(sat_used > 4){
			double H_gps[sat_count][4], y_gps[sat_count], DOP_gps[4], dx_gps[4];
			GPSposition_computation(S_t, sat_count, H_gps, W, y_gps, X_est, pos_gps, DOP_gps, dx_gps, msg9);
			pos_vec[i][0] = pos_gps[0];
			pos_vec[i][1] = pos_gps[1];
			pos_vec[i][2] = pos_gps[2];
			check_residual1(sat_count, H_gps, W, y_gps, res_vec, dx_gps);

			double max = res_vec[0];

			int pos_max = 0;
			for (i = 0; i < sat_count; i++)
				if (res_vec[i] > max) {
					max = res_vec[i];
					pos_max = i;
				}

			S_t[pos_max].use = 0;

			pos[0] = pos_vec[pos_max][0];
			pos[1] = pos_vec[pos_max][1];
			pos[2] = pos_vec[pos_max][2];

			if (res_vec[pos_max] >  calcchisquare(sat_used-4-1)){
				ok = 0;
				sat_used = sat_used - 1;
				sat_detected++;
				if(sat_used < 5){
					break;
				}
			}
			else ok = 1;

		}else{
			pos[0] = 0;
			pos[1] = 0;
			pos[2] = 0;
		}
	}
	return ok;
}

/**
 * calcchisquare function
 * method for selection of chi square based on the degree of freedom
 * @param degFre		The degree of freedom = no of satellites - 4
 * @return				ChiSq
 */
double calcchisquare(int degFree) {

	double ChiSq = 0;
	double chi[8] = { 2.55422131249637, 4.41454982637944, 6.03332708539159,
			7.53904147809598, 8.9766286869308, 10.3676252014224,
			11.7242374630789, 13.0541503831666 };

	if (degFree > 0 && degFree < 9)
		ChiSq = chi[degFree - 1];
	else
		ChiSq = chi[7];

	return ChiSq;
}

/**
 * check_residual function
 * method for computing the least squares residual value
 * @param sat_count		The number of satellites in view
 * @param H				The geometry matrix
 * @param W				The weight matrix
 * @param y				The pseudorange residual
 * @param result		Array holding the residual value and the number of satellities used
 * @param dX			The last update to the position
 * @return				Number of satellites used in the computation
 */
int check_residual(int sat_count, double H[sat_count][4], double W[sat_count][sat_count],
		double y[sat_count], double result[2], double dx[4]){

	double cov_v_hat[sat_count][sat_count], Ht[4][sat_count], HtW[4][sat_count],
	HtWH[4][4], HtWH_[4][4];
	double HHtWH_[sat_count][4], HHtWH_Ht[sat_count][sat_count];
	//double v_hat_transp[1][sat_count], v_hat_transpW[1][sat_count];
	double v_hat[sat_count];
	double Sigma_mat[sat_count][sat_count];
	int i, j;
	int status = 0;
	double res_value = 0;
	double val = 0;

	double test = 0;

	int sat_used = 0;
	for (i = 0; i < sat_count; i++)
		for (j = 0; j < sat_count; j++)
			if (i == j && W[i][j] != 0){
				Sigma_mat[i][j] = 1 / W[i][j];
				sat_used++;
			} else
				Sigma_mat[i][j] = 0;

	if (sat_used > 4) {

		size_b_row1 = sat_count;
		size_b_col1 = 4;
		transpose(H, Ht);
		size_b_row1 = 4;
		size_b_col1 = sat_count;
		size_b_row2 = sat_count;
		size_b_col2 = sat_count;
		multiply(Ht, Sigma_mat, HtW);
		size_b_col2 = 4;
		multiply(HtW, H, HtWH);
		inv_44(HtWH, HtWH_);

		size_b_row1 = sat_count;
		size_b_col1 = 4;
		size_b_row2 = 4;
		size_b_col2 = 4;
		multiply(H, HtWH_, HHtWH_);

		size_b_row1 = sat_count;
		size_b_col1 = 4;
		size_b_row2 = 4;
		size_b_col2 = sat_count;
		multiply(HHtWH_, Ht, HHtWH_Ht);

		size_b_row1 = sat_count;
		size_b_col1 = sat_count;
		subtract_mat(Sigma_mat, HHtWH_Ht, cov_v_hat); //covariance of estimates residuals

		size_b_row1 = sat_count;
		size_b_col1 = sat_count;
		size_b_row2 = sat_count;
		size_b_col2 = sat_count;
		double cov_v_hatW[sat_count][sat_count];

		multiply(cov_v_hat, W, cov_v_hatW);

		size_b_row1 = sat_count;
		size_b_col1 = sat_count;
		size_b_row2 = sat_count;
		size_b_col2 = 1;
		multiply_matxvec(cov_v_hatW, y, v_hat);

		for (i = 0; i < sat_count; i++)
			test += v_hat[i] * v_hat[i];

		double Hdx[sat_count];
		double residual[sat_count];
		size_b_row1 = sat_count;
		size_b_col1 = 4;
		size_b_row2 = 4;
		size_b_col2 = 1;
		multiply_matxvec(H, dx, Hdx);
		size_b_row1 = sat_count;
		size_b_col1 = 1;
		subtract_vec(y, Hdx, residual);


		for (i = 0; i < sat_count; i++)
			val += (residual[i] * residual[i]);

		val = sqrt(val);

		res_value = sqrt(test);
		status = 1;
	}else
		status = 0;

	result[0] = res_value;		// replace res_value
	result[1] = sat_used;

	return status;
}

/**
 * GPSposition_computation function
 * Basic algorithm for computing the user position
 * @param S_t			The Satellite structure for all satellites in view. Max 19
 * @param sat_count		The number of satellites in view
 * @param H				The geometry matrix
 * @param W				The weight matrix
 * @param dPR			The pseudorange residual
 * @param X_est			Position result in ECEF coordinates
 * @param pos			Position result expressed in latitude, longitude and altitude
 * @param DOP			Vector of 4 different DOP values (HDOP, VDOP, PDOP, GDOP)
 * @param dX			The last update to the position
 * @param msg9			The Egnos message type 9 pointer
 * @param utc_data		The array of UTC parameters
 * @return	sat_used	Number of satellites used in the computation
 */
int GPSposition_computation(Satellite S_t[19], int sat_count, double H[sat_count][4],
		double W[sat_count][sat_count],double dPR[sat_count], double X_est[4], double pos[3],
		double DOP[4], double dX[4], Egnos_msg * msg9){

	int it = 0;
	int i;
	int sat_used = sat_count;
	double R_corrected, r_pos_geo[3], r_pos[3], ENU[3], R;

	int egnos =1;

	while (it < 20) {

		// Satellites loop
		sat_used = 0;
		for (i = 0; i < sat_count; i++) {

			if (S_t[i].use == 2) {
				sat_used++;

				//S_t[i].tow2 = S_t[i].tow - S_t[i].pr_c / SPEED_OF_LIGHT;

				// Update the GPS position
				if (S_t[i].type_sat == 1)
					SV_position_computation(&S_t[i], egnos);

				// Computation of the EGNOS satellite position
				if (S_t[i].type_sat == 2)
					compute_EGNOSsat_position(&S_t[i], msg9);

				// Range
				R = sqrt(
						(S_t[i].pos_x - X_est[0]) * (S_t[i].pos_x - X_est[0])
						+ (S_t[i].pos_y - X_est[1])
						* (S_t[i].pos_y - X_est[1])
						+ (S_t[i].pos_z - X_est[2])
						* (S_t[i].pos_z - X_est[2]));

				//if(S_t[i].type_sat==1)
				SV_position_correction(&S_t[i], (R / SPEED_OF_LIGHT));

				// Range corrected after earth rotation compensation
				R_corrected = sqrt(
						(S_t[i].pos_x - X_est[0]) * (S_t[i].pos_x - X_est[0])
						+ (S_t[i].pos_y - X_est[1])	* (S_t[i].pos_y - X_est[1])
						+ (S_t[i].pos_z - X_est[2])	* (S_t[i].pos_z - X_est[2]));

				// Pseudorange residual
				dPR[i] = S_t[i].pr_c - R_corrected+ SPEED_OF_LIGHT*S_t[i].t_correction-X_est[3];

				// matrix H definition
				H[i][0] = (X_est[0] - S_t[i].pos_x) / R_corrected;
				H[i][1] = (X_est[1] - S_t[i].pos_y) / R_corrected;
				H[i][2] = (X_est[2] - S_t[i].pos_z) / R_corrected;
				H[i][3] = 1;

			} else { //if no pseudoranges data for the satellite available => fill the matrixes with
				R_corrected = 0;
				H[i][0] = 0;
				H[i][1] = 0;
				H[i][2] = 0;
				H[i][3] = 0;
				dPR[i] = 0;

			}
		}

		// Test if number of satellites > 3
		if (sat_used < 4)
			break;

		// Iterations increment
		it++;

		// Calculation of inv(trans(H).H).trans(H)
		int sat_size = sat_count;
		double HtW[4][sat_size], HtWH[4][4], HtWH_[4][4], HtWH_HtW[4][sat_size]; // with weight matrix
		double HtH[4][4], HtH_[4][4], Ht[4][sat_size];

		size_b_row1 = sat_size;
		size_b_col1 = 4;
		transpose(H, Ht); // H_temp
		size_b_row1 = 4;
		size_b_col1 = sat_size;
		size_b_row2 = sat_size;
		size_b_col2 = 4;
		multiply(Ht, H, HtH);
		inv_44(HtH, HtH_);
		size_b_col2 = sat_size;
		// Calculation with the weight matrix
		multiply(Ht, W, HtW); // W_temp
		size_b_col2 = 4;
		multiply(HtW, H, HtWH);
		inv_44(HtWH, HtWH_);
		size_b_col1 = 4;
		size_b_row2 = 4;
		size_b_col2 = sat_size;
		multiply(HtWH_, HtW, HtWH_HtW);
		size_b_col1 = sat_size;
		size_b_row2 = sat_size;
		size_b_col2 = 4;
		multiply_matxvec(HtWH_HtW, dPR, dX); //dPR_temp

		// update estimation
		X_est[0] += dX[0];
		X_est[1] += dX[1];
		X_est[2] += dX[2];
		X_est[3] += dX[3];

		// DOP
		DOP[0] = get_HDOP(HtH_);
		DOP[1] = get_VDOP(HtH_);
		DOP[2] = get_PDOP(HtH_);
		DOP[3] = get_TDOP(HtH_);

		// Check if HDOP is too high, position cannot be resolved
		if (DOP[0] > 20) {
			sat_used = 0; // the function will return 0
			break;
		}

		// Geodetic conversion
		pos[0] = X_est[0];
		pos[1] = X_est[1];
		pos[2] = X_est[2];
		cconv_to_geo(pos);

		double norm = sqrt(dX[0] * dX[0] + dX[1] * dX[1] + dX[2] * dX[2]);
		if (norm < 1E-8 && it > 6)
			break;
	}

	return sat_used;
}

/**
 * get_UTCoffset function
 * Computes the offset between GPS and EGNOS Network Time (ENT)
 * @param Sat			the Satellite structure
 * @param utc_data		The array of UTC parameters
 * @return				Offset GPS ENT
 */
double get_UTCoffset(Satellite * Sat, double utc_data[9]){

	double UTCGPST;
	double delta_utc, utc_time, W_utc;
	if(utc_data[8] == 1){
		delta_utc = utc_data[4] + utc_data[0] + utc_data[1]*((* Sat).tow2-utc_data[2]+604800*((* Sat).weeknb-utc_data[3]));
		if((int)utc_data[5]  > (* Sat).weeknb || ((int)utc_data[5] == (* Sat).weeknb && (utc_data[6]*86400 - 6*3600) > (* Sat).tow2)){
			//IS-GPS-200D case a
			utc_time = (* Sat).tow2 - delta_utc;
		}
		if((int)utc_data[5] == (* Sat).weeknb && (utc_data[6]*86400 - 6*3600 <  (* Sat).tow2) && (utc_data[6]*86400 + 6*3600 >  (* Sat).tow2)){
			//IS-GPS-200D case yb
			W_utc = mod(((* Sat).tow2 - delta_utc - 43200),86400) + 43200;
			utc_time = mod(W_utc, 86400 + utc_data[7]-utc_data[4]);
		}
		if((int)utc_data[5]  < (* Sat).weeknb || ((int)utc_data[5] == (* Sat).weeknb && (utc_data[6]*86400 + 6*3600) < (* Sat).tow2)){
			//IS-GPS-200D case c
			utc_time = (* Sat).tow2 - delta_utc;
		}
	UTCGPST = (* Sat).tow2 - utc_time;
		}else
			UTCGPST = 0;

	return UTCGPST;
}

void check_residual1(int sat_count,double H[sat_count][4],double W[sat_count][sat_count],
		double y[sat_count],double residual[sat_count],double dx[4]){
	int i;
	double std_res[sat_count];
	double pos_vec[sat_count][3];

	double Hdx[sat_count];
	size_b_row1 = sat_count;
	size_b_col1 = 4;
	size_b_row2 = 4;
	size_b_col2 = 1;
	multiply_matxvec(H, dx, Hdx);
	size_b_row1 = sat_count;
	size_b_col1 = 1;
	subtract_vec(y, Hdx, residual);

	double std = 0;
	std = std_dev(sat_count,residual);
	double medie = 0;
	medie = mean(sat_count, residual);

	for(i = 0; i< sat_count; i++)
	{
		std_res[i] = (residual[i] - medie) / std;
	}
}

double mean(int sat_count,double residual[sat_count]){
	int i;
	double sum = 0;
	double meanResult = 0;

	for(i = 0; i <sat_count; i++){
		sum = sum + residual[i];
	}
	meanResult = sum / sat_count;
	return meanResult;
}

double std_dev(int sat_count, double residual[sat_count]) {
	double std = 0;
	double sumOfSqrs = 0;
	double avg = mean(sat_count, residual);
	int i = 0;
	for (i = 0; i < sat_count; i++) {
	   sumOfSqrs += pow(((double) residual[i] - avg), 2);
	}

	std = sqrt(sumOfSqrs / (sat_count- 1));
	return std;
}



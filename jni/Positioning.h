/**
 * @file Positioning.h
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

#ifndef POSITIONING_H_
#define POSITIONING_H_

#include "Matrix.h"
#include "Ephemeris.h"
#include "Ionosphere.h"
#include "Troposphere.h"
#include "Fast_correction.h"
#include "Long_correction.h"

void init_positioning(void);
double get_corrected_time(double t);
double get_dtsv (double t, double af0, double af1, double af2);
double get_dtr (double sqrta, double Ek, double e);
void SV_position_computation(Satellite * Sat, int egnos);
void SV_position_correction(Satellite * Sat, double travel_time);
int positioning(double pos[3], double X_est[4],   double DOP[4], double PL[2],
		char eph_data[19][901], double sat_data[19][4], Egnos_msg * msg1, Egnos_msg msg2_5[4][2],
		Egnos_msg * msg6, Egnos_msg * msg7, Egnos_msg * msg10, Egnos_msg * msg12,
		Egnos_msg * msg9, Egnos_msg * msg17, Egnos_msg m18_t[11], Egnos_msg msg24_t[25],
		Egnos_msg msg25_t[15], Egnos_msg m26_t[25], char m18_char[5][263],
		char m26_char[25][263],	int egnos, int * iono_flag, double sat_array[15], Satellite S_t[19],
		double utc_data[9], double klob_data[9], int rnd_options[8],
		double sat_data_NotUsed[19][4],char eph_data_nu[19][901],Satellite S_t_NotUsed[19]);
int user_position_computation_WLS(Satellite S_t[19], double X_est[4],  double DOP[4],
		double PL[2], Egnos_msg * msg1, Egnos_msg msg2_5[4][2], Egnos_msg * msg6,
		Egnos_msg * msg7, Egnos_msg * msg10, Egnos_msg * msg12, Egnos_msg * msg9, 
		Egnos_msg * msg17, Egnos_msg m18_t[11], Egnos_msg msg24_t[25], Egnos_msg msg25_t[15],
		Egnos_msg m26_t[25], int egnos,	int * iono_flag,char m18_char[5][263], char m26_char[25][263],
		int sat_count, double sat_array[15],double utc_data[9], int rnd_options[8],Satellite S_t_NotUsed[19],
		int sat_count_notUsed);
int user_position_computation_RND(Satellite S_t[19], double X_est[4],
		Egnos_msg * msg1, Egnos_msg msg2_5[4][2],
		Egnos_msg * msg6, Egnos_msg * msg7, Egnos_msg * msg10,
		Egnos_msg * msg12, Egnos_msg * msg9, Egnos_msg * msg17,
		Egnos_msg m18_t[11], Egnos_msg msg24_t[25], Egnos_msg msg25_t[15],
		Egnos_msg m26_t[25], char m18_char[5][263],	char m26_char[25][263],
		int sat_count, double sat_array[15],  double utc_data[9],
		double klob_data[9], int rnd_options[8]);
int rnd_user_position_computation(Satellite S_t[19], int sat_count, double H[sat_count][4],
		double W[sat_count][sat_count],double dPR[sat_count], double X_est[4], double pos[3],
		double DOP[4], double dX[4],int rnd_options[8],Egnos_msg *msg1, Egnos_msg msg2_5[4][2],
		Egnos_msg *msg6, Egnos_msg *msg7, Egnos_msg *msg10,
		Egnos_msg *msg12, Egnos_msg *msg9, Egnos_msg *msg17,
		Egnos_msg m18_t[11], Egnos_msg msg24_t[25], Egnos_msg msg25_t[15],
		Egnos_msg m26_t[25], char m18_char[5][263],	char m26_char[25][263],
		double sat_array[15],  double utc_data[9],
		double klob_data[9]);
int user_position_computation_bancroft(Satellite S_t[19], double pos[4], int sat_count);
void cconv_to_cart(double * vect);
void cconv_to_geo(double * vect);
double get_latitude(double vect[3]);
double get_longitude(double vect[3]);
double get_height(double vect[3]);
double get_GDOP(double D[4][4]);
double get_PDOP(double D[4][4]);
double get_HDOP(double D[4][4]);
double get_TDOP(double D[4][4]);
double get_VDOP(double D[4][4]);
double get_sigma_mult2(double elevation);
double get_sigma_noisedivg2(double elevation);
void ionospheric_model(double klob_data[9], Satellite S, double lat, double lon,  double iono[2]);
void DOPpos_computation (Satellite S_t[19], double X_est[4], int sat_count, double pos[3],
		double H_all[sat_count][4],double W_all[sat_count][sat_count],
		Egnos_msg * msg9, double dPR[sat_count]);
int TwoDpos_computation(Satellite S_t[19], double X_est[4],
		Egnos_msg * msg1, Egnos_msg msg2_5[4][2], Egnos_msg * msg6, Egnos_msg * msg7,
		Egnos_msg * msg9, Egnos_msg * msg10, Egnos_msg * msg12, Egnos_msg * msg17,
		Egnos_msg m18_t[11], Egnos_msg msg24_t[25], Egnos_msg msg25_t[15],
		Egnos_msg m26_t[25], int egnos, int * iono_flag, char m18_char[5][263],
		char m26_char[25][263], int sat_count, double sat_array[15], double utc_data[9],
		double klob_data[9], int rnd_options[8]);
double calcchisquare(int degFree);
int check_residual(int sat_count, double H[sat_count][4], double W[sat_count][sat_count],
		double y[sat_count], double result[2], double dx[4]);
int GPSposition_computation(Satellite S_t[19], int sat_count, double H[sat_count][4],
		double W[sat_count][sat_count],double y[sat_count], double X_est[4], double pos[3],
		double DOP[4], double dX[4], Egnos_msg * msg9);
double get_UTCoffset(Satellite * S, double utc_data[9]);
int EUcoverage(double lat, double lon);
void check_residual1(int sat_count,double H[sat_count][4],double W[sat_count][sat_count],
		double y[sat_count],double residual[sat_count],double dx[4]);
double mean(int sat_count,double residual[sat_count]);
double std_dev(int sat_count, double residual[sat_count]);
#endif /* POSITIONING_H_ */

/**
 * @file Satellite.c
 *
 * @brief Satellite module source file containing functions related to the
 * Satellite structure (initialization...).
 * @details The module contains functions to initialize the Satellite
 * structure, to identify the SV type (GPS...), to convert to local coordinates
 * system and to compute the elevation and azimuth angle of the SV.
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

#include "Satellite.h"

/**
 * init_satellite function
 * The function initialize all the values of a given satellite to 0
 * @param *Sat  The pointer of the satellite
 */
void init_satellite(Satellite * Sat)
{
	(*Sat).prn          = 0.0;
	(*Sat).tow          = 0.0;
	(*Sat).pr           = 0.0;
	(*Sat).pr_c         = 0.0;
	(*Sat).cn0          = 0.0;
	(*Sat).pos_x        = 0.0;
	(*Sat).pos_y        = 0.0;
	(*Sat).pos_z        = 0.0;
	(*Sat).t_correction = 0.0;
	(*Sat).tow2         = 0.0;
	(*Sat).tow3         = 0.0;
	(*Sat).weeknb       = 0;
	(*Sat).ura          = 0;
	(*Sat).cl2          = 0;
	(*Sat).health       = 0;
	(*Sat).iodc         = 0;
	(*Sat).iode_s1      = 0;
	(*Sat).tgd          = 0.0;
	(*Sat).toc          = 0;
	(*Sat).af0          = 0.0;
	(*Sat).af1          = 0.0;
	(*Sat).af2          = 0.0;
	(*Sat).iode_s2      = 0;
	(*Sat).crs          = 0.0;
	(*Sat).delta_n      = 0.0;
	(*Sat).m0           = 0.0;
	(*Sat).cuc          = 0.0;
	(*Sat).cus          = 0.0;
	(*Sat).e            = 0.0;
	(*Sat).sqrta        = 0.0;
	(*Sat).toe          = 0;
	(*Sat).ado          = 0.0;
	(*Sat).cic          = 0.0;
	(*Sat).cis          = 0.0;
	(*Sat).crc          = 0.0;
	(*Sat).w            = 0.0;
	(*Sat).omega0       = 0;
	(*Sat).omegadot     = 0;
	(*Sat).i0           = 0;
	(*Sat).idot         = 0;
	(*Sat).iode_s3      = 0;
	(*Sat).iono_delay   = 0.0;
	(*Sat).iono_model   = 0.0;
	(*Sat).tropo_delay  = 0.0;
	(*Sat).fast_delay   = 0.0;
	(*Sat).sigma_flt2   = 0.0;
	(*Sat).sigma_tropo2 = 0.0;
	(*Sat).sigma_uire2  = 0.0;
	(*Sat).sigma2		= 0.0;
	(*Sat).daf1         = 0.0;
	(*Sat).daf0         = 0.0;
	(*Sat).dx           = 0.0;
	(*Sat).dy           = 0.0;
	(*Sat).dz           = 0.0;
	(*Sat).ddx          = 0.0;
	(*Sat).ddy          = 0.0;
	(*Sat).ddz          = 0.0;
	(*Sat).t0           = 0.0;
	(*Sat).use          = 0;
	(*Sat).udrei        = -1;
	(*Sat).rrc        	= 0.0;
	(*Sat).fast_set		= 0;
	(*Sat).long_set		= -1;
	(*Sat).type_sat		= 0;
	(*Sat).eps_fc 		= 0;
	(*Sat).eps_rrc 		= 0;
	(*Sat).eps_ltc 		= 0;
	(*Sat).eps_er 		= 0;
	(*Sat).prn_mask		= 1;
	(*Sat).low_elv		= 0;
	(*Sat).rnd			= 0;
	(*Sat).az           = 0;
	(*Sat).el           = 0;
	//INS - start
	(*Sat).roota        = 0;
	(*Sat).sat_pos_x        = 0;
	(*Sat).sat_pos_y        = 0;
	(*Sat).sat_pos_z        = 0;
	(*Sat).rel_corr        = 0;
	(*Sat).tCorr = 0;
	//INs - end
}

/**
 * cconv_to_ENU function
 * Conversion from Cartesian to ENU (East North Up) coordinates
 * @param   ENU     The destination table of ENU coordinates
 * @param   sat     The satellite positions
 * @param   X_est   The estimate receiver positions in ECEF
 * @param   geod    The estimate receiver positions in geodetic
 */
void cconv_to_ENU(double ENU[3], double sat[3], double X_est[3], double geod[3])
{
	double E,N,U;

	// Calculation of East, North and Up values
	E = -sin(geod[1]*PI/180)*(sat[0]-X_est[0]) + cos(geod[1]*PI/180)*(sat[1]-X_est[1]);
	N = -sin(geod[0]*PI/180)* cos(geod[1]*PI/180)*(sat[0]-X_est[0]) - sin(geod[0]*PI/180)*sin(geod[1]*PI/180)*(sat[1]-X_est[1]) +  cos(geod[0]*PI/180)*(sat[2]-X_est[2]);
	U = cos(geod[0]*PI/180)*cos(geod[1]*PI/180)*(sat[0]-X_est[0]) + cos(geod[0]*PI/180)*sin(geod[1]*PI/180)*(sat[1]-X_est[1]) + sin(geod[0]*PI/180)*(sat[2]-X_est[2]);

	ENU[0] = E;
	ENU[1] = N;
	ENU[2] = U;
}

/**
 * get_elevation function
 * Calculation of the elevation angle form ENU coordinates : el = arctan(U/(sqrt(E*E + N*N)))
 * @param   ENU   Table of E,N,U coordinates
 * @return      The computed elevation angle in deg.
 */
double get_elevation(double ENU[3])
{
	double hr = sqrt(ENU[0]*ENU[0] + ENU[1]*ENU[1]);
	return atan2(ENU[2], hr)*180/PI;
}

/**
 * get_azimuth function
 * Calculation of the azimuth angle form ENU coordinates : az = arctan(E/N)
 * @param  ENU Table of E,N,U coordinates
 * @return     The computed azimuth angle in deg.
 */
double get_azimuth(double ENU[3])
{
	double az = atan2(ENU[0], ENU[1])*180/PI;
	if(az<0)// obtain the angle from 0 to 360 deg
		az = az + 360;
	return az;
}

/**
 * get_satellite_type function
 * The function determine in which constellation the satellite are from (GPS,EGNOS...)
 * @param prn The prn number of the satellite
 * @return    0(GPS), 1(Glonass), 2(Future constellations), 3(EGNOS/SBAS), 4(Future constellations)
 */
int get_satellite_type(double prn)
{
	int r;

	if(prn <38)					// GPS constellation
		r = 0;
	if(prn > 37 && prn <62)
		r = 1;
	if(prn > 61 && prn <120)
		r = 2;
	if(prn > 119 && prn <139)	// SBAS Constellation
		r = 3;
	if(prn > 138 && prn <211)
		r = 4;

	return r;
}

/**
 * is_GPS function
 * The function determine if the prn is part of the GPS constellation
 * @param prn The prn number of the satellite
 * @return    0(false), 1(true)
 */
int is_GPS(double prn)
{
	int r;
	if(get_satellite_type(prn)==0)
		r = 1;
	else
		r=0;
	return r;
}

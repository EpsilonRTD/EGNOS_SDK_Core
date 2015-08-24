/**
 * @file Troposphere.c
 *
 * @brief Troposphere module source file containing the tropospheric corrections
 * and model variances computation functions.
 * @details This module defines the SBAS troposphere model. It performs the
 * computation of the tropospheric corrections and model variances according
 * to the DO-229D section A.4.2.4.
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

#include "Troposphere.h"

/**
 * get_tropoCorrection function
 * The function computes the tropospheric correction according to the troposheric model from RTCA - DO-229D A.4.2.4
 * @param	*Sat		The pointer of the Satellite
 * @param	latitude	User latitude
 * @param	height		User height
 * @return				1
 */
double get_tropoCorrection(Satellite * Sat, double latitude, double height)
{
	double P,dP,T,dT,E,dE,B,dB,L,dL,zhyd,zwet,dhyd,dwet,m,tmp,max,base,power;
	double lat[5] = {15,30,45,60,75};							// Deg.
	double elevation = (*Sat).el;
	int j,D,Dmin,r;

	// Meteorological parameters : average
	double P0[5] = {1013.25,1017.25,1015.75,1011.75,1013.00}; 	// mbar
	double T0[5] = {299.65,294.15,283.15,272.15,263.65};  		// K
	double E0[5] = {26.31,21.79,11.66,6.78,4.11};  				// mbar
	double B0[5] = {6.30E-3, 6.05E-3, 5.58E-3,5.39E-3,4.53E-3};	// K/m
	double L0[5] = {2.77,3.15,2.57,1.81,1.55};					// dimensionless

	// Meteorological parameters : Seasonal variation
	double dP0[5] = {0.0,-3.75,-2.25,-1.75,-0.50}; 				// mbar
	double dT0[5] = {0.0,7.00,11.00,15.00,14.50};  				// K
	double dE0[5] = {0.0,8.85,7.24,5.36,3.39};  				// mbar
	double dB0[5] = {0.0,0.25e-3,0.32e-3,0.81e-3,0.62e-3};		// K/m
	double dL0[5] = {0.0,0.33,0.46,0.74,0.30};					// dimensionless

	// Get the day of the year number
	D = floor((*Sat).weeknb*7-((floor((*Sat).weeknb*7/365.25))*365.25))+floor((*Sat).tow2/86400)+6;

	if(latitude > 0)	// Northern latitudes
		Dmin = 28;
	else				// Southern latitudes
		Dmin = 211;

	// Interpolation
	if((latitude>15.0) && (latitude<75.0))
	{
		// Position in the parameters table (function of the lat. 15deg. increment)
		j = (int)(latitude/15) - 1;

		P = interpolate(latitude,lat[j],lat[j+1],P0[j],P0[j+1]);
		T = interpolate(latitude,lat[j],lat[j+1],T0[j],T0[j+1]);
		E = interpolate(latitude,lat[j],lat[j+1],E0[j],E0[j+1]);
		B = interpolate(latitude,lat[j],lat[j+1],B0[j],B0[j+1]);
		L = interpolate(latitude,lat[j],lat[j+1],L0[j],L0[j+1]);

		dP = interpolate(latitude,lat[j],lat[j+1],dP0[j],dP0[j+1]);
		dT = interpolate(latitude,lat[j],lat[j+1],dT0[j],dT0[j+1]);
		dE = interpolate(latitude,lat[j],lat[j+1],dE0[j],dE0[j+1]);
		dB = interpolate(latitude,lat[j],lat[j+1],dB0[j],dB0[j+1]);
		dL = interpolate(latitude,lat[j],lat[j+1],dL0[j],dL0[j+1]);
	}
	else
	{
		// Take directly the values for lat<= 15 or lat >=75
		if(latitude <= 15)
			j = 0;
		if(latitude >= 75)
			j = 4;

		P = P0[j];
		T = T0[j];
		E = E0[j];
		B = B0[j];
		L = L0[j];

		dP = dP0[j];
		dT = dT0[j];
		dE = dE0[j];
		dB = dB0[j];
		dL = dL0[j];
	}
	tmp = cos((2*PI*(D-Dmin)) / 365.25);

	// Parameters computations
	P = P - dP*tmp;
	T = T - dT*tmp;
	E = E - dE*tmp;
	B = B - dB*tmp;
	L = L - dL*tmp;

	// Zero-altitude zenith delay computations
	base = (1 - ((B*height)/T));

	if(base > 0)	// Check if the base if positive
	{
		zhyd = (1E-6*K1*Rd*P) / Gm;
		power = (G / (Rd*B));
		dhyd = pow(base,power) * zhyd;

		zwet = ((1E-6*K2*Rd) / (Gm*(L+1) - B*Rd)) * (E/T);
		power = (((L+1)*G) / (Rd*B) )-1;
		dwet = pow(base,power) * zwet;
	}
	else
	{
		dhyd = 0;
		dwet = 0;
	}

	// Computation of m(Elevation)
	if(elevation >= 2)
	{
		if(elevation >= 4)
			m = 1.001/sqrt(0.002001 + sin(elevation*PI/180)*sin(elevation*PI/180));
		else
		{
			if(0 > (4-elevation))
				max = 0;
			else
				max = 4-elevation;

			m = (1.001/sqrt(0.002001 + sin(elevation*PI/180)*sin(elevation*PI/180))) * (1 + 0.015*max*max);
		}
	}
	else
		m = 0;

  // Compute the delay (m) and error accuracy (m^2) (err=err_TVE*m(elevation) where err_TVE=0.12m)
    (*Sat).tropo_delay = -(dhyd + dwet)*m;
    (*Sat).sigma_tropo2 = (0.12*m)*(0.12*m);

	r = 1;

	return r;
}

/**
 * interpolate function
 * The function interpolates a value for a given latitude : value_a + (value_b-value_a)*(latitude - latitude_a)/(latitude_b - latitude_a)
 * @param	latitude	User latitude
 * @param	latitude_a	Latitude
 * @param	latitude_b	Latitude
 * @param	value_a		User latitude
 * @param	value_b		User height
 * @return				The interpolated value
 */
double interpolate(double latitude, double latitude_a, double latitude_b, double value_a, double value_b)
{
	return (value_a + (value_b-value_a)*(latitude-latitude_a)/(latitude_b-latitude_a));
}

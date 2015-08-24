/**
 * @file Ionosphere.c
 *
 * @brief Ionosphere module source file containing the ionospheric corrections
 * and model variances computation functions.
 * @details This module defines the EGNOS ionosphere model.
 * It decodes the ionospheric bands and corrections messages from EGNOS,
 * defines the Ionospheric Grid Points and computes the EGNOS
 * ionospheric corrections and degradations according to the DO-229D
 * sections A.4.4.9 and A.4.4.10.
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

#include "Ionosphere.h"
#include "stdlib.h"
/**
 * set_ionoGridSis function
 * The function set the ionospheric grid from from Signal In Space EGNOS messages.
 * For each bands defined in the band_ids parameter, the IGPS are decoded.
 * @param m18_t    Message 18 destination table
 * @param m26_t    Message 26 destination table
 * @param m18_char The input binaries table for message type 18
 * @param m26_char The input binaries table for message type 26
 * @return         1 if succesful, 0 if not
 */
int set_ionoGridSis(Egnos_msg m18_t[11], Egnos_msg m26_t[25],
		char m18_char[5][263], char m26_char[25][263]) {
	char tow[13] = "";
	char egnos[251] = "";
	int i, j, r, iodi;
	int total_block = 0; // The total number of IGP blocks

	for(i = 0; i< 11; i++){
		Egnos_msg msg18;
		init_msg(&msg18, 18);
		m18_t[i] = msg18;
	}

	// Messages type 18 part
	for (i = 0; i < 5; i++) {

		Egnos_msg msg18;
		init_msg(&msg18, 18);
		
		if (m18_char[i][0] != '\0'){

			strncpy(tow, m18_char[i], 12);
			tow[12] = '\0';
			msg18.tow = atof(tow); //printf("tow %f\n",msg18.tow);

			if (msg18.tow != 0) {
				strncpy(egnos, m18_char[i] + 12, 250);
				egnos[250] = '\0';
				msg18.bin_msg = egnos; //printf("egnos %s\n",msg18.bin_msg);

				int band_ID;
				band_ID = decode_msg18(&msg18);
				if ( band_ID != -1) {
					iodi = msg18.iodi;
					m18_t[band_ID] = msg18;

					// Update the total nb. of IGP blocks
					total_block = total_block + m18_t[band_ID].block_nb;
					r = 1;
				}else{
					r = 0;
				}
			}else{
				r = 0;
			}
		}
	}

// Messages type 26 part
j = 0;
while (j < 25) {
	Egnos_msg msg26;
	init_msg(&msg26, 26);
	
	if (m26_char[i][0] != '\0'){

		strncpy(tow, m26_char[j], 12);
		tow[12] = '\0';
		msg26.tow = atof(tow); //printf("tow %f\n",msg26.tow);

		if (msg26.tow != 0) {
			strncpy(egnos, m26_char[j] + 12, 250);
			egnos[250] = '\0';
			msg26.bin_msg = egnos; //printf("egnos %s\n",msg26.bin_msg);

			if (decode_msg26(&msg26) == 1) {
				// IODI check
				if (msg26.iodi == iodi)
					r = 1;
				else
					r = 0;

				m26_t[j] = msg26;
			} else {
				m26_t[j] = msg26;
				msg26.bin_msg = "";
				r = 0;
			}
		} else {
			m26_t[j] = msg26;
			msg26.bin_msg = "";
			r = 0;
		}
	}
	j++;
}

return r;
}

/**
 * get_ionoCorrection function
 * The function computes the ionospheric correction in (m), computes the model variance in (m^2) for the given satellite and updates its iono_delay and sigma_uire2 parameters.
 * The function returns the number of IGPs used to compute these values. The computations are performed if the IGPs number value is equal to 3 or 4.
 * @param *Sat      The pointer of the Satellite
 * @param user_lat  The estimated user latitude   (deg)
 * @param user_long The estimated user longitude  (deg)
 * @param m18_t     The messages 18 table
 * @param m26_t     The messages 26 table
 * @param *msg10    The pointer of the message 10
 * @param flag		If 1 sigma_iono doesn't include the degradation factor
 * @return          The number of IGPs (-1: PRN not monitored or one IGP is don't use status)
 */
void get_ionoCorrection(Satellite * Sat, double user_lat, double user_long,Egnos_msg m18_t[11],
		Egnos_msg m26_t[25], Egnos_msg * msg10, int flag) {
	double ipp[2], interp[2];
	int i;
	double igps_sel[4][6];
	igps_sel[0][2] = 0;
	igps_sel[1][2] = 0;
	igps_sel[2][2] = 0;
	igps_sel[3][2] = 0;
	double fpp, iono_delay, uire_acc, egnos_time;
	int nb_igps = 0;

	egnos_time = (*Sat).tow2;

	// Compute the IPP lat. and long. (ipp table)
	IPPlocation(ipp, user_lat, user_long, (*Sat).el, (*Sat).az);
	// Selection of the IGPs
	nb_igps = IGPsSelect(igps_sel, ipp, m18_t, m26_t);
	// if GIVD >= 63.875 correction is not available
	for(i = 0; i < 4; i++)
	{
		if(igps_sel[i][2] >= 63.875)
		{
			nb_igps = 0;
		}

	}
	if (nb_igps >= 3) {
		// Interpolation of the IPP
		if (nb_igps == 4 && ipp[0] <= 85)
			IPPInterpolation4(interp, igps_sel, ipp, egnos_time, msg10, flag);
		if (nb_igps == 4 && ipp[0] > 85)
			IPPInterpolation4_above85(interp, igps_sel, ipp, egnos_time, msg10, flag);
		if (nb_igps == 3)
			IPPInterpolation3(interp, igps_sel, ipp, egnos_time, msg10, flag);

		// Computation of the obliquity factor
		fpp = get_fpp((*Sat).el);

		// Computation of the Ionospheric delay
		iono_delay = -fpp * interp[0];
		(*Sat).iono_delay = iono_delay;

		// Computation of the UIRE accuracy
		uire_acc = fpp * fpp * interp[1];
		(*Sat).sigma_uire2 = uire_acc;
	} else
		nb_igps = 0;

}

/**
 * IPPlocation function
 * The function computes the Ionospheric Pierce Point Location - DO-229D A.4.4.10.1
 * @param ipp     The table containing the latitude and longitude (deg)
 * @param user_lat    The user latitude (deg)
 * @param user_long   The user longitude  (deg)
 * @param E       Satellite Elevation (deg)
 * @param A       Satellite Azimuth (deg)
 */
void IPPlocation(double ipp[2], double user_lat, double user_long, double E,
		double A) {
	double angle_ipp, user_lat_deg;
	user_lat_deg = user_lat;
	user_lat *= PI / 180;
	user_long *= PI / 180;
	E *= PI / 180;
	A *= PI / 180;

	angle_ipp = PI / 2 - E
			- asin((EARTH_RADIUS * cos(E)) / (EARTH_RADIUS + HI));

	// Latitude of the IPP
	ipp[0] = asin(
			sin(user_lat) * cos(angle_ipp)
			+ cos(user_lat) * sin(angle_ipp) * cos(A));

	// Longitude of the IPP
	if ((user_lat_deg > 70
			&& ((tan(angle_ipp) * cos(A)) > tan(PI / 2 - user_lat)))
			|| (user_lat_deg < -70
					&& ((tan(angle_ipp) * cos(A + PI)) > tan(PI / 2 + user_lat))))
		ipp[1] = user_long + PI - asin((sin(angle_ipp) * sin(A)) / cos(ipp[0]));
	else
		ipp[1] = user_long + asin((sin(angle_ipp) * sin(A)) / cos(ipp[0]));

	// COnversion to deg.
	ipp[0] *= 180 / PI;
	ipp[1] *= 180 / PI;
}

/**
 * IGPsSelect function
 * The function selects the 4 or 3 IGPs - DO-229D A.4.4.10.2
 * @param igps     The destination table containing the IGPs lat.,long.,IGPVD and GIVEI
 * @param ipp      The table containing the ipp lat. and long.
 * @param m18_t    The messages 18 table
 * @param m26_t    The messages 26 table
 * @return         The number of selected IGPs
 */
int IGPsSelect(double igps[4][6], double ipp[2], Egnos_msg m18_t[11],Egnos_msg m26_t[25]) {
	int block_info[4][3]; // block info contains the block ID, position (0-14) and band_id for 4 points
	int igps_18 = 0; // check IGPs nb for msg 18
	int igps_26 = 0; // check IGPs nb for msg 26
	int par_lat,par_lon;

	int lat_spacing, lon_spacing;

	igps[0][4] = -1;
	igps[1][4] = -1;
	igps[2][4] = -1;
	igps[3][4] = -1;

	igps[0][5] = -1;
	igps[1][5] = -1;
	igps[2][5] = -1;
	igps[3][5] = -1;

	block_info[0][1] = -1;
	block_info[1][1] = -1;
	block_info[2][1] = -1;
	block_info[3][1] = -1;

	int check = 0;
	int check_10x10 = 0;

	// latitude less then 60 and band 9 available
	if (ipp[0] <= 55 || (ipp[0] <= 60 && m18_t[9].band_id==9))
	{
		par_lat = 2; // the parity is not set in this case
		par_lon = 2;
		lat_spacing = 5;
		lon_spacing = 5;

		igps_18 = get_defined_IGPs(igps, m18_t, block_info, lat_spacing, lon_spacing, par_lat, par_lon, ipp);

		if (igps_18 == 4)
		{
			igps_26 = get_monitored_IGPs(m26_t, block_info,igps);
			if(igps_26 == 4)
				return igps_26;
			if(igps_26 == 3)
			{
				check = 0;
				check = check_Triangle(igps, ipp, 5, 5);
				if(check == 1)
				{
					igps_26 = get_monitored_IGPs(m26_t, block_info,igps);
					return igps_26;
				}

				else
					return 0;
			}
			else
				return 0;
		}
		if (igps_18 == 3)
		{
			check = 0;
			check = check_Triangle(igps, ipp, 5, 5);
			if(check == 1)
			{
				igps_26 = get_monitored_IGPs(m26_t, block_info,igps);
				if(igps_26 == 3)
					return igps_26;
				else
					return 0;
			}
			else
				check_10x10 = 1;
		}

		if (igps_18 < 3 || check_10x10 == 1) {
			lat_spacing = 10;
			lon_spacing = 10;
			igps_26 = calculateIGPs(ipp, igps, block_info, m18_t, m26_t, lat_spacing, lon_spacing);
			return igps_26;
		}
	}
	if (ipp[0] > 55 && ipp[0] <= 60 && m18_t[9].band_id != 9)
	{
		lat_spacing = 10;
		lon_spacing = 10;
		igps_26 = calculateIGPs(ipp, igps, block_info, m18_t, m26_t, lat_spacing, lon_spacing);
	}
	if (ipp[0] > 60 && ipp[0] <= 75 && m18_t[9].band_id == 9)
	{
		lat_spacing =  5;
		lon_spacing = 10;
		igps_26 = calculateIGPs(ipp, igps, block_info, m18_t, m26_t, lat_spacing, lon_spacing);
		if (igps_26 < 3)
		{
			lat_spacing = 10;
			lon_spacing = 10;
			igps_26 = calculateIGPs(ipp, igps, block_info, m18_t, m26_t, lat_spacing, lon_spacing);
		}
	}
	if (ipp[0] > 60 && ipp[0] <= 75 && m18_t[9].band_id != 9)
	{
		lat_spacing = 10;
		lon_spacing = 10;
		igps_26 = calculateIGPs(ipp, igps, block_info, m18_t, m26_t, lat_spacing, lon_spacing);
	}
	if (ipp[0] > 75 && ipp[0] <= 85 && m18_t[9].band_id == 9)
	{
		igps_26 = calculateIGPs_above75(ipp, igps, block_info, m18_t, m26_t);
	}
	if (ipp[0] > 75 && ipp[0] <= 85 && m18_t[9].band_id != 9)
	{
		igps_26 = calculateIGPs_above75(ipp, igps, block_info, m18_t, m26_t);
	}
	if (ipp[0] > 85)
	{
		igps[0][0] = 85;
		igps[1][0] = 85;
		igps[2][0] = 85;
		igps[3][0] = 85;

		if(ipp[1] < 0)
			igps[2][1] = floor(ipp[1] / 90) * 90 - 90;
		else
			igps[2][1] = ceil(ipp[1] / 90) * 90;

		int long_igp3 = igps[2][1];

		switch(long_igp3)
		{
		case -180:
			igps[0][1] =   0;
			igps[1][1] =  90;
			igps[3][1] = -90;
			break;
		case  -90:
			igps[0][1] =   90;
			igps[1][1] = -180;
			igps[3][1] =    0;
			break;
		case    0:
			igps[0][1] =-180;
			igps[1][1] = -90;
			igps[3][1] =  90;
			break;
		case   90:
			igps[0][1] = -90;
			igps[1][1] =   0;
			igps[3][1] =-180;
			break;
		}

		if (m18_t[9].band_id == 9)
		{
			int band_id = m18_t[9].band_id;
			igps_18 = get_IGPs(band_id, block_info, igps, m18_t);
		}
		if (igps_18 == 4)
		{
			igps_26 = get_monitored_IGPs(m26_t, block_info,igps);
			if(igps_26 == 4)
				return igps_26;
			else
				return 0;
		}
		else
			return 0;
	}
	return igps_26;
}

/**
 * calculateIGPs_above75 function
 * The function determines the IGPs that are used for computing the ionopsheric correction for
 * latitudes above 75 degrees latitude
 * @param igps        The destination table containing the IGPs lat.,long.,IGPVD and GIVEI
 * @param block_info  The position of the GIVD values in MT26 corresponding to the IGPs found
 * @param ipp         The table containing the ipp lat. and long.
 * @param m18_t       The messages 18 table
 * @param m26_t       The messages 26 table
 * @return            The number of selected IGPs
 */
int calculateIGPs_above75(double ipp[2], double igps[4][6], int block_info[4][3], Egnos_msg m18_t[11], Egnos_msg m26_t[25])
{
	int i, k, j, block_id, band_pos, max;
	int long_min = 0;
	int long_max = 0;
	int long3 = 0;
	double x = 0;
	int no_igps = 0;
	int igps_26 = 0;
	int igps_18 = 0;
	double givd_min = 0;
	double give_min = 0;
	double givd_max = 0;
	double give_max = 0;
	int block_info_min[3]={-1,-1,-1};
	int block_info_max[3]={-1,-1,-1};

	int block[3]={-1,-1,-1};
	igps[0][0] = 75;
	igps[1][0] = 75;
	igps[2][0] = 85;
	igps[3][0] = 85;

	if(ipp[1] < 0)
		long3 = floor(ipp[1] / 10) * 10 - 10;
	else
		long3 = floor(ipp[1] / 10) * 10;

	igps[0][1] = long3;
	igps[1][1] = long3 + 10;
	igps[2][1] = long3;
	igps[3][1] = long3 + 10;

	int band9_factor = 1;
	if(m18_t[9].band_id != 9)
		band9_factor = 3;

	for (k = 0; k < 2; k++){
		if(m18_t[9].band_id != 9)
		{
			band_pos = get_BandSelect(igps[k][1]);
		}
		else
			band_pos = 9;

		j = 0; // First value of the block line number
		block_id = m18_t[band_pos].igp_blocks[0][0]; // First value of the block ID
		max = m18_t[band_pos].block_nb * 15;

		for (i = 0; i < max; i++) // Stop condition: the block number stored in the egnos msg * 15 (15 lines per blocks) is reached
		{
			// Reset the value of the block line
			if (block_id != m18_t[band_pos].igp_blocks[i][0])
				j = 0;

			// Update block_id
			block_id = m18_t[band_pos].igp_blocks[i][0];

			if ((m18_t[band_pos].igp_blocks[i][0] != -1)
					&& (igps[k][0] == m18_t[band_pos].igp_blocks[i][1])
					&& (igps[k][1] == m18_t[band_pos].igp_blocks[i][2])) {
				// save the block_id and the line number of the IGPs in the block
				block_info[k][0] = block_id;
				block_info[k][1] = j;
				block_info[k][2] = m18_t[band_pos].band_id;

				// Update the status of the IGP to 1 (defined)
				igps[k][4] = 1;

				// Increment the number of IGPs
				igps_18++;
			}
			j++;
		}
	}

	no_igps = get_monitored_IGPs(m26_t,block_info,igps);
	igps_26 += no_igps;

	for(k = 2; k < 4; k++)
	{
		if ((igps[k][1]-round(igps[k][1]/30 * band9_factor)) != 0)
		{
			if (ipp[1] >= 0)
			{
				long_min = floor(igps[k][1] / 30 * band9_factor) * 30 * band9_factor;
				long_max = long_min + 30;
			}
			else
			{
				long_min = ceil(igps[k][1] / 30 * band9_factor) * 30 * band9_factor - 30 * band9_factor;
				long_max = long_min + 30;
			}

			x = (igps[k][1] - long_min)/30 * band9_factor;

			if(m18_t[9].band_id != 9)
			{
				band_pos = get_BandSelect(long_min);
			}
			else
				band_pos = 9;

			// Check message type 18 : are the 4 IGPs defined in the igp_blocks table for the given band_id (=! 9)
			j = 0; // First value of the block line number
			int block_id = m18_t[band_pos].igp_blocks[0][0]; // First value of the block ID
			int max = m18_t[band_pos].block_nb * 15;
			// Check for the 4 IGPs
			for (i = 0; i < max; i++) // Stop condition: the block number stored in the egnos msg * 15 (15 lines per blocks) is reached
			{
				// Reset the value of the block line
				if (block_id != m18_t[band_pos].igp_blocks[i][0])
					j = 0;

				// Update block_id
				block_id = m18_t[band_pos].igp_blocks[i][0];

				if ((m18_t[band_pos].igp_blocks[i][0] != -1)
						&& (m18_t[band_pos].igp_blocks[i][1] == 85)
						&& (m18_t[band_pos].igp_blocks[i][2] == long_min)) {
					// save the block_id and the line number of the IGPs in the block
					block_info_min[0] = block_id;
					block_info_min[1] = j;
					block_info_min[2] = m18_t[band_pos].band_id;
					igps_18++;
				}
				if ((m18_t[band_pos].igp_blocks[i][0] != -1)
						&& (m18_t[band_pos].igp_blocks[i][1] == 85)
						&& (m18_t[band_pos].igp_blocks[i][2] == long_max)) {
					// save the block_id and the line number of the IGPs in the block
					block_info_max[0] = block_id;
					block_info_max[1] = j;
					block_info_max[2] = m18_t[band_pos].band_id;
					igps_18++;
				}
				j++;
			}

			// Search for the position in the m26_t table where the band_id and block_id are the same in msg 18 and 26
			for (j = 0; j < 25; j++) {
				if ((m26_t[j].band_id == block_info_min[2])&& (m26_t[j].block_id == block_info_min[0])
						&& m26_t[j].grid_point[block_info_min[1]][0] == 1)
				{
					// Save the IGPVD and GIVEI
					givd_min = m26_t[j].grid_point[block_info_min[1]][1]; // IGPVD
					give_min = m26_t[j].grid_point[block_info_min[1]][2]; // GIVEI
				}

				if ((m26_t[j].band_id == block_info_max[2])&& (m26_t[j].block_id == block_info_max[0])
						&& m26_t[j].grid_point[block_info_max[1]][0] == 1)
				{
					// Save the IGPVD and GIVEI
					givd_max = m26_t[j].grid_point[block_info_max[1]][1]; // IGPVD
					give_max = m26_t[j].grid_point[block_info_max[1]][2]; // GIVEI
				}
			}

			if((givd_min == 0 && give_min == 0) || (givd_max == 0 && give_max == 0))
			{
				igps[k][2]  =  0;
				igps[k][3]  =  0;
				igps[k][4]  = -1;
				igps[k][5]  = -1;
			}

			igps[k][2] = (1 - x)*givd_min + x*givd_max;
			igps[k][3] = (1 - x)*give_min + x*give_max;
			igps[k][4] = 1;                     // Status
			igps[k][5] =  m26_t[j].tow ;              // TOW for the message
			// Increment the number of IGPs
			igps_26++;

		}
		else{

			if(m18_t[9].band_id != 9)
			{
				band_pos = get_BandSelect(igps[k][1]);
			}
			else
				band_pos = 9;

			int j = 0; // First value of the block line number
			int block_id = m18_t[band_pos].igp_blocks[0][0]; // First value of the block ID
			int max = m18_t[band_pos].block_nb * 15;
			// Check for the 4 IGPs
			for (i = 0; i < max; i++) // Stop condition: the block number stored in the egnos msg * 15 (15 lines per blocks) is reached
			{
				// Reset the value of the block line
				if (block_id != m18_t[band_pos].igp_blocks[i][0])
					j = 0;

				// Update block_id
				block_id = m18_t[band_pos].igp_blocks[i][0];

				if ((m18_t[band_pos].igp_blocks[i][0] != -1)
						&& (m18_t[band_pos].igp_blocks[i][1] == 85)
						&& (m18_t[band_pos].igp_blocks[i][2] == igps[k][1])) {
					// save the block_id and the line number of the IGPs in the block
					block[0] = block_id;
					block[1] = j;
					block[2] = m18_t[band_pos].band_id;
				}
				j++;
			}
			for (j = 0; j < 25; j++) {
				if ((m26_t[j].band_id == block[2])
						&& (m26_t[j].block_id == block[0]))
					break;
			}

			if(j<25)
			{
				// Check the IGPs with status == 1
				if (m26_t[j].grid_point[block[1]][0] == 1) {
					// Save the IGPVD and GIVEI
					igps[k][2] = m26_t[j].grid_point[block[1]][1]; // IGPVD
					igps[k][3] = m26_t[j].grid_point[block[1]][2]; // GIVEI
					igps[k][4] = 1;                     // Status
					igps[k][5] =  m26_t[j].tow ;              // TOW for the message
					// Increment the number of IGPs
					igps_26++;
				}
			}
		}
	}
	if(igps_18 < 4)
		return 0;
	if(igps_26 < 4)
		return 0;

	return igps_26;
}

/**
 * calculateIGPs function
 * The function determines the IGPs (spaced at 5x10 or 10x10 degress) that are used for computing
 * the ionopsheric correction for latitudes under 75 degrees latitude
 * @param igps          The destination table containing the IGPs lat.,long.,IGPVD and GIVEI
 * @param block_info    The position of the GIVD values in MT26 corresponding to the IGPs found
 * @param ipp           The table containing the ipp lat. and long.
 * @param m18_t         The messages 18 table
 * @param m26_t         The messages 26 table
 * @param  lat_spacing  The latitude spacing of the cell surrounding the IPP
 * @param  lon_spacing  The longitude spacing of the cell surrounding the IPP
 * @return            The number of selected IGPs
 */
int calculateIGPs(double ipp[2], double igps[4][6], int block_info[4][3], Egnos_msg m18_t[5], Egnos_msg m26_t[25], int lat_spacing, int lon_spacing)
{
	int par_lat, par_lon;
	int igps_18 = 0;
	int igps_26 = 0;
	int check;

	par_lat = 1;
	par_lon = 0;

	igps_18 = get_defined_IGPs(igps, m18_t, block_info, lat_spacing, lon_spacing, par_lat, par_lon, ipp);
	if (igps_18 == 4)
	{
		igps_26 = get_monitored_IGPs(m26_t, block_info,igps);
		if(igps_26 == 4)
			return igps_26;
		if(igps_26 == 3)
		{
			check = 0;
			check = check_Triangle(igps, ipp, lat_spacing, lon_spacing);
			if(check == 1)
				igps_26 = get_monitored_IGPs(m26_t, block_info,igps);
			else
				return 0;
		}
		else
			return 0;
	}
	else
	{
		//2. search even lat and even lon
		par_lat = 0;
		par_lon = 0;

		igps_18 = get_defined_IGPs(igps, m18_t, block_info, lat_spacing, lon_spacing, par_lat, par_lon, ipp);

		if(igps_18 == 4)
		{
			igps_26 = get_monitored_IGPs(m26_t, block_info,igps);
			if(igps_26 == 4)
				return igps_26;
			if(igps_26 == 3)
			{
				check = 0;
				check = check_Triangle(igps, ipp, lat_spacing, lon_spacing);
				if(check == 1)
					return igps_26;
				else
					return 0;
			}
			else
				return 0;
		}

		else
		{
			//3. search odd lat and odd lon
			par_lat = 1;
			par_lon = 1;

			igps_18 = get_defined_IGPs(igps, m18_t, block_info, lat_spacing, lon_spacing, par_lat, par_lon, ipp);

			if(igps_18 == 4)
			{
				igps_26 = get_monitored_IGPs(m26_t, block_info,igps);
				if(igps_26 == 4)
					return igps_26;
				if(igps_26 == 3)
				{
					check = 0;
					check = check_Triangle(igps, ipp, lat_spacing, lon_spacing);
					if(check == 1)
						return igps_26;
					else
						return 0;
				}
				else
					return 0;
			}

			else
			{
				//4. search even lat and odd lon
				par_lat = 0;
				par_lon = 1;

				igps_18 = get_defined_IGPs(igps, m18_t, block_info, lat_spacing, lon_spacing, par_lat, par_lon, ipp);

				if(igps_18 == 4)
				{
					igps_26 = get_monitored_IGPs(m26_t, block_info,igps);
					if(igps_26 == 4)
						return igps_26;
					if(igps_26 == 3)
					{
						check = 0;
						check = check_Triangle(igps, ipp, lat_spacing, lon_spacing);
						if(check == 1)
							return igps_26;
						else
							return 0;
					}
					else
						return 0;
				}

				else
				{
					//5. search odd lat and even lon
					par_lat = 1;
					par_lon = 0;

					igps_18 = get_defined_IGPs(igps, m18_t, block_info, lat_spacing, lon_spacing, par_lat, par_lon, ipp);


					if(igps_18 == 3)
					{
						check = 0;
						check = check_Triangle(igps, ipp, lat_spacing, lon_spacing);
						if(check == 1)
						{
							igps_26 = get_monitored_IGPs(m26_t, block_info,igps);
						}
					}
					else
					{
						//6. search even lat and even lon
						par_lat = 0;
						par_lon = 0;

						igps_18 = get_defined_IGPs(igps, m18_t, block_info, lat_spacing, lon_spacing, par_lat, par_lon, ipp);

						if(igps_18 == 3)
						{
							check = 0;
							check = check_Triangle(igps, ipp, lat_spacing, lon_spacing);
							if(check == 1)
								igps_26 = get_monitored_IGPs(m26_t, block_info,igps);
						}
						else
						{
							//7. search odd lat and odd lon
							par_lat = 1;
							par_lon = 1;

							igps_18 = get_defined_IGPs(igps, m18_t, block_info, lat_spacing, lon_spacing, par_lat, par_lon, ipp);

							if(igps_18 == 3)
							{
								check = 0;
								check = check_Triangle(igps, ipp, lat_spacing, lon_spacing);
								if(check == 1)
									igps_26 = get_monitored_IGPs(m26_t, block_info,igps);
							}
							else
							{
								//8. search even lat and odd lon
								par_lat = 0;
								par_lon = 1;

								igps_18 = get_defined_IGPs(igps, m18_t, block_info, lat_spacing, lon_spacing, par_lat, par_lon, ipp);

								if(igps_18 == 3)
								{
									check = 0;
									check = check_Triangle(igps, ipp, lat_spacing, lon_spacing);
									if(check == 1)
										igps_26 = get_monitored_IGPs(m26_t, block_info,igps);
								}
								else
									return 0;
							}
						}
					}

				}
			}
		}
	}
	return igps_26;
}

/**
 * get_defined_IGPs function
 * The function determines the IGPs that are used for computing the ionopsheric correction and if
 * they are defined in the ionospheric mask, MT18
 * @param  igps          The destination table containing the IGPs lat.,long.,IGPVD and GIVEI
 * @param  m18_t         The messages 18 table
 * @param  block_info    The position of the GIVD values in MT26 corresponding to the IGPs found
 * @param  lat_spacing	 The latitude spacing of the cell surrounding the IPP
 * @param  lon_spacing	 The longitude spacing of the cell surrounding the IPP
 * @param  par_lat		 Sets the latitude to even or odd
 * @param  par_lon		 Sets the longitude to even or odd
 * @param  ipp           The table containing the ipp lat. and long.
 * @return            The number of selected IGPs
 */
int get_defined_IGPs(double igps[4][6], Egnos_msg m18_t[11], int block_info[4][3], int lat_spacing, int lon_spacing, int par_lat, int par_lon, double ipp[2])
{
	int igps_18 = 0;
	int no_igps = 0;
	int band_id1, band_id2;


	igps[0][4] = -1;
	igps[1][4] = -1;
	igps[2][4] = -1;
	igps[3][4] = -1;

	block_info[0][1] = -1;
	block_info[1][1] = -1;
	block_info[2][1] = -1;
	block_info[3][1] = -1;

	SelectCells(igps, ipp, lat_spacing, lon_spacing, par_lat, par_lon);

	band_id1 = get_BandSelect(igps[0][1]);
	band_id2 = get_BandSelect(igps[1][1]);

	no_igps = get_IGPs(band_id1, block_info, igps, m18_t);
	igps_18 += no_igps;
	if(band_id1 != band_id2)
	{
		igps_18 = get_IGPs(band_id2, block_info, igps, m18_t);
		igps_18 += no_igps;
	}
	if(igps[3][0] > 55 && m18_t[9].band_id==9)
	{
		no_igps = get_IGPs(9, block_info, igps, m18_t);
		igps_18 += no_igps;
	}

	return igps_18;
}

/**
 * get_IGPs function
 * The function determines the position of the IGPs in the band.
 * @param   band_pos        The band where the IGPs is located
 * @param   block_info      The position of the GIVD values in MT26 corresponding to the IGPs found
 * @param   igps            The destination table containing the IGPs lat.,long.,IGPVD and GIVEI
 * @param   m18_t           The messages 18 table
 * @return  igps_18         The number of selected IGPs
 */
int get_IGPs(int band_pos, int block_info[4][3], double igps[4][6], Egnos_msg m18_t[11]){

	int i,k;
	int igps_18 = 0;
	// Check message type 18 : are the 4 IGPs defined in the igp_blocks table for the given band_id (=! 9)
	int j = 0; // First value of the block line number
	int block_id = m18_t[band_pos].igp_blocks[0][0]; // First value of the block ID
	int max = m18_t[band_pos].block_nb * 15;
	// Check for the 4 IGPs
	for (k = 0; k < 4; k++){
		for (i = 0; i < max; i++) // Stop condition: the block number stored in the egnos msg * 15 (15 lines per blocks) is reached
		{
			// Reset the value of the block line
			if (block_id != m18_t[band_pos].igp_blocks[i][0])
				j = 0;

			// Update block_id
			block_id = m18_t[band_pos].igp_blocks[i][0];


			if ((m18_t[band_pos].igp_blocks[i][0] != -1)
					&& (igps[k][0] == m18_t[band_pos].igp_blocks[i][1])
					&& (igps[k][1] == m18_t[band_pos].igp_blocks[i][2])) {
				// save the block_id and the line number of the IGPs in the block
				block_info[k][0] = block_id;
				block_info[k][1] = j;
				block_info[k][2] = m18_t[band_pos].band_id;

				// Update the status of the IGP to 1 (defined)
				igps[k][4] = 1;

				// Increment the number of IGPs
				igps_18++;

			}

			j++;
		}
	}

	return igps_18;
}

/**
 * get_monitored_IGPs function
 * The function determines if selected IGPs are monitored or not
 * @param block_info      The position of the GIVD values in MT26 corresponding to the IGPs found
 * @param m26_t           The messages 18 table
 * @param igps            The destination table containing the IGPs lat.,long.,IGPVD and GIVEI
 * @return                The number of selected IGPs
 */
int get_monitored_IGPs(Egnos_msg m26_t[25], int block_info[4][3], double igps[4][6])
{
	int igps_26 = 0;
	int i,j;
	// Check for the 4 IGPs
	for (i = 0; i < 4; i++) {
		// Search fo the position in the m26_t table where the band_id and block_id are the same in msg 18 and 26
		for (j = 0; j < 25; j++) {
			if ((m26_t[j].band_id == block_info[i][2])
					&& (m26_t[j].block_id == block_info[i][0]))
				break;
		}

		if(j<25)
		{
			// Check the IGPs with status == 1
			if (m26_t[j].grid_point[block_info[i][1]][0] == 1) {
				// Save the IGPVD and GIVEI
				igps[i][2] = m26_t[j].grid_point[block_info[i][1]][1]; // IGPVD
				igps[i][3] = m26_t[j].grid_point[block_info[i][1]][2]; // GIVEI
				igps[i][4] = 1;                     // Status
				igps[i][5] =  m26_t[j].tow ;              // TOW for the message

				// Increment the number of IGPs
				igps_26++;
			}
			// Check the IGPs with status == 0
			if (m26_t[j].grid_point[block_info[i][1]][0] == 0) {
				// IGPVD, GIVEI and status equal to -1 for this IGP
				igps[i][2] =  0;
				igps[i][3] =  0;
				igps[i][4] = -1;

				// No Incrementation in that case
				//igps_26++;
			}
			// Check the IGPs with status == -1 : no inospheric correction available
			if (m26_t[j].grid_point[block_info[i][1]][0] == -1) {
				// IGPVD, GIVEI and status equal to -1 for all IGPs
				igps[0][2] =  0;
				igps[0][3] = -1;
				igps[0][4] = -1;

				igps[1][2] =  0;
				igps[1][3] =  0;
				igps[1][4] = -1;

				igps[2][2] =  0;
				igps[2][3] =  0;
				igps[2][4] = -1;

				igps[3][2] =  0;
				igps[3][3] =  0;
				igps[3][4] = -1;

				// Fix the IGPs number to -1
				igps_26 = -1;
			}
		}
	}

	return igps_26;

}
/**
 * IPPInterpolation4 function
 * The function computes the interpolated IPP Vertical Delay and Model Variance with 4 IGPs - DO-229D A.4.4.10.3
 * for latitudes lower than 85 degrees
 * @param results   The destination table containing the computed IPP Vertical Delay(m) and Model Variance(m^2)
 * @param igps      The table containing the lat.,long.,IGPVD, GIVEI and status for the 4 IGPs
 * @param ipp     The IPP latitude and longitude table            (deg)
 * @param t       The current egnos time                    (s)
 * @param *msg10    The pointer of the message 10
 * @param flag		If 1 sigma_iono doesn't include the degradation factor
 * @return        1 if succeful, 0 if not
 */
void IPPInterpolation4(double results[2], double igps[4][6], double ipp[2],
		double t, Egnos_msg * msg10, int flag) {
	double w[4];
	double xpp, ypp; // sigma_iono2 = sigma_iono^2
	double lat1 = 0;
	double lat2 = 0;
	double long1 = 0;
	double long2 = 0;
	double sigma_iono2[4];
	int i;

	// computation of xpp and ypp (assume that the IPP is below 85N)
	// lat1=S of IPP (min),lat2=N of IPP (max),long1=W of IPP (min),long2=E of IPP (max)
	lat1 = igps[0][0];
	long1 = igps[0][1];
	for (i = 0; i < 4; i++) {
		if (igps[i][0] < lat1)
			lat1 = igps[i][0];
		if (igps[i][0] > lat1)
			lat2 = igps[i][0];
		if (igps[i][1] < long1)
			long1 = igps[i][1];
		if (igps[i][1] > long1)
			long2 = igps[i][1];
	}
	//printf("long1:%f, long2:%f, lat1:%f, lat2:%f\n",long1,long2,lat1,lat2);
	xpp = (ipp[1] - long1) / (long2 - long1);
	ypp = (ipp[0] - lat1) / (lat2 - lat1);

	// Computation of the weights
	w[0] = xpp * ypp;
	w[1] = (1 - xpp) * ypp;
	w[2] = (1 - xpp) * (1 - ypp);
	w[3] = xpp * (1 - ypp);

	// Computation of the IPPVD and UIVE accuracy
	results[0] = 0;
	results[1] = 0;

	results[0] = w[0] * igps[3][2] + w[1] * igps[2][2]+ w[2] * igps[0][2]+w[3] * igps[1][2]; // IPPVD
	sigma_iono2[0] = get_sigmaIono2(get_GIVEaccuracy((int) igps[3][3]), t,
			igps[3][5], msg10, flag); // sigma_iono^2 from degradation model using msg 10
	sigma_iono2[1] = get_sigmaIono2(get_GIVEaccuracy((int) igps[2][3]), t,
			igps[2][5], msg10, flag); // sigma_iono^2 from degradation model using msg 10
	sigma_iono2[2] = get_sigmaIono2(get_GIVEaccuracy((int) igps[0][3]), t,
			igps[0][5], msg10, flag); // sigma_iono^2 from degradation model using msg 10
	sigma_iono2[3] = get_sigmaIono2(get_GIVEaccuracy((int) igps[1][3]), t,
			igps[1][5], msg10, flag); // sigma_iono^2 from degradation model using msg 10

	for (i = 0; i < 4; i++) {
		results[1] += w[i] * sigma_iono2[i]; // UIVE accuracy
	}
}

/**
 * IPPInterpolation4_above85 function
 * The function computes the interpolated IPP Vertical Delay and Model Variance with 4 IGPs - DO-229D A.4.4.10.3
 * for latitudes higher than 85 degrees
 * @param results   The destination table containing the computed IPP Vertical Delay(m) and Model Variance(m^2)
 * @param igps      The table containing the lat.,long.,IGPVD, GIVEI and status for the 4 IGPs
 * @param ipp     	The IPP latitude and longitude table            (deg)
 * @param t       	The current Egnos time                    (s)
 * @param *msg10    The pointer of the message 10
 * @param flag		If 1 sigma_iono doesn't include the degradation factor
 * @return        	1 if successful, 0 if not
 */
void IPPInterpolation4_above85(double results[2], double igps[4][6], double ipp[2],
		double t, Egnos_msg * msg10, int flag) {
	double w[4];
	double xpp, ypp; // sigma_iono2 = sigma_iono^2
	double sigma_iono2[4];
	int i, long3;

	if(ipp[1] < 0)
		long3 = round(ipp[1] / 90) - 90;
	else
		long3 = round(ipp[1] / 90);

	ypp = (fabs(ipp[0])-85) / 10;
	xpp = (ipp[1] - long3) / 90 * (1 - 2*ypp) + ypp;

	// Computation of the weights
	w[0] = xpp * ypp;
	w[1] = (1 - xpp) * ypp;
	w[2] = (1 - xpp) * (1 - ypp);
	w[3] = xpp * (1 - ypp);

	// Computation of the IPPVD and UIVE accuracy
	results[0] = 0;
	results[1] = 0;

	results[0] = w[0] * igps[0][2] + w[1] * igps[1][2]+ w[2] * igps[2][2]+w[3] * igps[3][2]; // IPPVD
	sigma_iono2[0] = get_sigmaIono2(get_GIVEaccuracy((int) igps[0][3]), t,
			igps[0][5], msg10, flag); // sigma_iono^2 from degradation model using msg 10
	sigma_iono2[1] = get_sigmaIono2(get_GIVEaccuracy((int) igps[1][3]), t,
			igps[1][5], msg10, flag); // sigma_iono^2 from degradation model using msg 10
	sigma_iono2[2] = get_sigmaIono2(get_GIVEaccuracy((int) igps[2][3]), t,
			igps[2][5], msg10, flag); // sigma_iono^2 from degradation model using msg 10
	sigma_iono2[3] = get_sigmaIono2(get_GIVEaccuracy((int) igps[3][3]), t,
			igps[3][5], msg10, flag); // sigma_iono^2 from degradation model using msg 10

	for (i = 0; i < 4; i++) {
		results[1] += w[i] * sigma_iono2[i]; // UIVE accuracy
	}
}

/**
 * get_triangle_orientation
 * The function determines the orientation of the triangle formed by 3 monitored IGP points
 * @param  igps         the ionospheric grid points that define the square which holds the IPP
 * @return status       1, 2, 3, or 4 depending on the orientation of the triangle.
 */
int get_triangle_orientation(double igps[4][6]) {
	int i;
	int status = -1;

	for (i = 0; i < 4; i++) {
		if (igps[i][4] == -1) {
			switch (i) {
			case 0:
				status = 3;
				break;
			case 1:
				status = 2;
				break;
			case 2:
				status = 4;
				break;
			case 3:
				status = 1;
				break;
			}
		}
	}
	return status;
}


/**
 * IPPInterpolation3 function
 * The function computes the interpolated IPP Vertical Delay and Model Variance with 3 IGPs - DO-229D A.4.4.10.3
 * @param results The destination table containing the computed IPP Vertical Delay (m) and Model Variance (m^2)
 * @param igps    The table containing the lat.,long.,IGPVD, GIVEI and status for the 4 IGPs
 * @param ipp     The IPP latitude and longitude table                (deg)
 * @param t       The current Egnos time                              (s)
 * @param *msg10  The pointer of the message 10
 * @param flag		If 1 sigma_iono doesn't include the degradation factor
 * @return        1 if succesful, 0 if not
 */
void IPPInterpolation3(double results[2], double igps[4][6], double ipp[2],
		double t, Egnos_msg * msg10, int flag) {
	double w[3];
	double xpp, ypp;
	double lat1 = 0;
	double lat2 = 0;
	double long1 = 0;
	double long2 = 0;
	double sigma_iono2[3];
	int i;
	int status;

	double lat_igp2 = 0;
	double long_igp2 = 0;
	double delta_lat = 0;
	double delta_long = 0;

	// computation of xpp and ypp (assume that the IPP is below 85N)
	// lat1=S of IPP (min),lat2=N of IPP (max),long1=W of IPP (min),long2=E of IPP (max)
	lat1 = igps[0][0];
	long1 = igps[0][1];
	for (i = 0; i < 4; i++) {
		if (igps[i][0] < lat1)
			lat1 = igps[i][0];
		if (igps[i][0] > lat1)
			lat2 = igps[i][0];
		if (igps[i][1] < long1)
			long1 = igps[i][1];
		if (igps[i][1] > long1)
			long2 = igps[i][1];
	}

	// Computation of the IPPVD and UIVE accuracy
	results[0] = 0;
	results[1] = 0;

	// status indicates the orientation of the triangle, 4 cases possible
	status = get_triangle_orientation(igps);
	switch (status) {
	case 1:
		lat_igp2 = igps[0][0];
		long_igp2 = igps[0][1];

		delta_lat = fabs(ipp[0]-lat_igp2);
		delta_long = fabs(ipp[1]-long_igp2);

		xpp = delta_long / (long2 - long1);
		ypp = delta_lat / (lat2 - lat1);

		// Computation of the weights
		w[0] = ypp;
		w[1] = 1 - xpp - ypp;
		w[2] = xpp;

		sigma_iono2[0] = get_sigmaIono2(get_GIVEaccuracy((int) igps[2][3]), t, igps[2][5], msg10, flag); // sigma_iono^2 from degradation model using msg 10
		sigma_iono2[1] = get_sigmaIono2(get_GIVEaccuracy((int) igps[0][3]), t, igps[0][5], msg10, flag);
		sigma_iono2[2] = get_sigmaIono2(get_GIVEaccuracy((int) igps[1][3]), t, igps[1][5], msg10, flag);
		results[0] = w[0] * igps[2][2]     + w[1] * igps[0][2]     + w[2] * igps[1][2]; // IPPVD
		results[1] = w[0] * sigma_iono2[0] + w[1] * sigma_iono2[1] + w[2] * sigma_iono2[2];
		break;
	case 2:
		lat_igp2 = igps[2][0];
		long_igp2 = igps[2][1];

		delta_lat = fabs(ipp[0]-lat_igp2);
		delta_long = fabs(ipp[1]-long_igp2);

		xpp = delta_long / (long2 - long1);
		ypp = delta_lat / (lat2 - lat1);

		// Computation of the weights
		w[0] = ypp;
		w[1] = 1 - xpp - ypp;
		w[2] = xpp;
		sigma_iono2[0] = get_sigmaIono2(get_GIVEaccuracy((int) igps[0][3]), t, igps[0][5], msg10, flag); // sigma_iono^2 from degradation model using msg 10
		sigma_iono2[1] = get_sigmaIono2(get_GIVEaccuracy((int) igps[2][3]), t, igps[2][5], msg10, flag);
		sigma_iono2[2] = get_sigmaIono2(get_GIVEaccuracy((int) igps[3][3]), t, igps[3][5], msg10, flag);
		results[0] = w[0] * igps[0][2]     + w[1] * igps[2][2]     + w[2] * igps[3][2]; // IPPVD
		results[1] = w[0] * sigma_iono2[0] + w[1] * sigma_iono2[1] + w[2] * sigma_iono2[2];
		break;
	case 3:
		lat_igp2 = igps[3][0];
		long_igp2 = igps[3][1];

		delta_lat = fabs(ipp[0]-lat_igp2);
		delta_long = fabs(ipp[1]-long_igp2);

		xpp = delta_long / (long2 - long1);
		ypp = delta_lat / (lat2 - lat1);

		// Computation of the weights
		w[0] = ypp;
		w[1] = 1 - xpp - ypp;
		w[2] = xpp;
		sigma_iono2[0] = get_sigmaIono2(get_GIVEaccuracy((int) igps[1][3]), t, igps[1][5], msg10, flag); // sigma_iono^2 from degradation model using msg 10
		sigma_iono2[1] = get_sigmaIono2(get_GIVEaccuracy((int) igps[3][3]), t, igps[3][5], msg10, flag);
		sigma_iono2[2] = get_sigmaIono2(get_GIVEaccuracy((int) igps[2][3]), t, igps[2][5], msg10, flag);
		results[0] = w[0] * igps[1][2]     + w[1] * igps[3][2]     + w[2] * igps[2][2]; // IPPVD
		results[1] = w[0] * sigma_iono2[0] + w[1] * sigma_iono2[1] + w[2] * sigma_iono2[2];
		break;
	case 4:
		lat_igp2 = igps[1][0];
		long_igp2 = igps[1][1];

		delta_lat = fabs(ipp[0]-lat_igp2);
		delta_long = fabs(ipp[1]-long_igp2);

		xpp = delta_long / (long2 - long1);
		ypp = delta_lat / (lat2 - lat1);

		// Computation of the weights
		w[0] = ypp;
		w[1] = 1 - xpp - ypp;
		w[2] = xpp;
		sigma_iono2[0] = get_sigmaIono2(get_GIVEaccuracy((int) igps[3][3]), t, igps[3][5], msg10, flag); // sigma_iono^2 from degradation model using msg 10
		sigma_iono2[1] = get_sigmaIono2(get_GIVEaccuracy((int) igps[1][3]), t, igps[1][5], msg10, flag);
		sigma_iono2[2] = get_sigmaIono2(get_GIVEaccuracy((int) igps[0][3]), t, igps[0][5], msg10, flag);
		results[0] = w[0] * igps[3][2]     + w[1] * igps[1][2]     + w[2] * igps[0][2]; // IPPVD
		results[1] = w[0] * sigma_iono2[0] + w[1] * sigma_iono2[1] + w[2] * sigma_iono2[2];
		break;
	}
}

/**
 * SelectCells function
 * The function computes the 4 closest points to IPP (lat. and long. difference defined by inc)
 * @param igps      The destination table containing the lat. and long. of the 4 computed points;
 *                  Order of points in igps: igps[0]:S-E igps[1]:S-W igps[2]:N-E igps[2]:N-W
 * @param ipp       The table containing the ipp lat. and long.
 * @param inc_lat   The increment value for latitude
 * @param inc_long  The increment value for longitude
 * @param  par_lat	Sets the latitude to even or odd
 * @param  par_lon	Sets the longitude to even or odd
 */
void SelectCells(double igps[4][6], double ipp[2], int inc_lat, int inc_long, int par_lat, int par_lon) {
	double r, r1, r2;
	double intp, fractp;
	int i, sign, inc;

	for (i = 0; i < 2; i++) {
		if (i == 0)
			inc = inc_lat;
		else
			inc = inc_long;
		if (ipp[i] < 0)
			sign = -1;
		else
			sign = 1;
		r = ipp[i] / 10;
		fractp = modf(r, &intp);
		if (fabs(fractp) > 0.5 )
			r1 = intp * 10 + ceil(fabs(fractp)) * 5 * sign;
		if (fabs(fractp) < 0.5 )
			r1 = intp * 10;
		if( i == 0)
		{
			if(par_lat == 0)
			{
				if(((int)r1)%2 != 0)
					r1=r1-5;
			}
			else if ((par_lat == 1))
				if(((int)r1)%2 == 0)
					r1=r1-5;
		}
		else
		{
			if(par_lon==0)
			{
				if(((int)r1)%2 != 0)
					r1=r1-5;
			}
			else if ((par_lon == 1))
				if(((int)r1)%2 == 0)
					r1=r1-5;
		}
		r2 = r1 + inc * sign;

		if (sign == -1)
		{
			int tmp=r2;
			r2=r1;
			r1=tmp;
		}

		if (i == 0) {
			igps[0][i] = r1;
			igps[1][i] = r1;
			igps[2][i] = r2;
			igps[3][i] = r2;
		} else {
			igps[0][i] = r1;
			igps[1][i] = r2;
			igps[2][i] = r1;
			igps[3][i] = r2;
		}
	}
}

/**
 * get_GIVEaccuracy function
 * The function returns the GIVE equivalent to the GIVEI - DO-229D table A-17
 * @param givei   The GIVE Indicator (GIVEI)
 * @return      The GIVE (m^2)
 */
double get_GIVEaccuracy(int givei) {
	double acc;
	switch (givei) {
	case 0:
		acc = 0.0084;
		break;
	case 1:
		acc = 0.0333;
		break;
	case 2:
		acc = 0.0749;
		break;
	case 3:
		acc = 0.1331;
		break;
	case 4:
		acc = 0.2079;
		break;
	case 5:
		acc = 0.2994;
		break;
	case 6:
		acc = 0.4075;
		break;
	case 7:
		acc = 0.5322;
		break;
	case 8:
		acc = 0.6735;
		break;
	case 9:
		acc = 0.8315;
		break;
	case 10:
		acc = 1.1974;
		break;
	case 11:
		acc = 1.8709;
		break;
	case 12:
		acc = 3.3260;
		break;
	case 13:
		acc = 20.7870;
		break;
	case 14:
		acc = 187.0826;
		break;
	case 15:
		acc = -1;
		break;
	}
	return acc;
}

/**
 * get_BandSelect function
 * The function selects the bands numbers in which the given longitude is located (bands 3,4,5 or 6)
 * @param longitude   The longitude (deg)
 * @return        The band number
 */
int get_BandSelect(double longitude) {
	int nb = -1;

	if (longitude >= -60.0 && longitude <= -25.0)
		nb = 3;
	if (longitude > -25.0 && longitude <= 15.0)
		nb = 4;
	if (longitude > 15.0 && longitude <= 55.0)
		nb = 5;
	if (longitude > 55.0 && longitude <= 95.0)
		nb = 6;

	return nb;
}

/**
 * check_Triangle function
 * The function checks if a given point is in the triangle defined by 3 points
 * @param igps    The table containing the lat. and long. of the 3 points
 * @param ipp   The table containing the lat. and long. of the point
 * @param inc_lat The increment value for latitude
 * @param inc_long  The increment value for longitude
 * @return      1 if the point is in the triangle, 0 if not
 */
int check_Triangle(double igps[4][6], double ipp[2], int inc_lat, int inc_long) {
	//int opposite1[2], opposite2[2];
	int excluded_pt[2], pt3[2];
	int i, r;

	// Compute the 3 points
	for (i = 0; i < 4; i++) {
		if (igps[i][4] == -1) {
			excluded_pt[0] = igps[i][0];
			excluded_pt[1] = igps[i][1];
			switch (i) {
			case 0:
				pt3[0] = igps[i][0] + inc_lat;
				pt3[1] = igps[i][1] + inc_long;
				/* opposite1[0] = igps[i][0] + inc_lat;
        opposite1[1] = igps[i][1];
        opposite2[0] = igps[i][0];
        opposite2[1] = igps[i][1] + inc_long;*/
				break;
			case 1:
				pt3[0] = igps[i][0] + inc_lat;
				pt3[1] = igps[i][1] - inc_long;
				/* opposite1[0] = igps[i][0];
        opposite1[1] = igps[i][1] - inc_long;
        opposite2[0] = igps[i][0] + inc_lat;
        opposite2[1] = igps[i][1];*/
				break;
			case 2:
				pt3[0] = igps[i][0] - inc_lat;
				pt3[1] = igps[i][1] + inc_long;
				/*   opposite1[0] = igps[i][0];
        opposite1[1] = igps[i][1] + inc_long;
        opposite2[0] = igps[i][0] - inc_lat;
        opposite2[1] = igps[i][1];*/
				break;
			case 3:
				pt3[0] = igps[i][0] - inc_lat;
				pt3[1] = igps[i][1] - inc_long;
				/*  opposite1[0] = igps[i][0] - inc_lat;
        opposite1[1] = igps[i][1];
        opposite2[0] = igps[i][0];
        opposite2[1] = igps[i][1] - inc_long;*/
				break;
			}
		}
	}

	// Compute the segment between the 3rd point and the IPP
	double sum1 = 0;
	double sum2 = 0;

	sum1 += sqrt(
			(pt3[0] - ipp[0]) * (pt3[0] - ipp[0])
			+ (pt3[1] - ipp[1]) * (pt3[1] - ipp[1]));

	sum2 += sqrt(
			(excluded_pt[0] - ipp[0]) * (excluded_pt[0] - ipp[0])
			+ (excluded_pt[1] - ipp[1]) * (excluded_pt[1] - ipp[1]));

	// IPP is in the triangle if the segment is <= half of the hypotenuse
	if (sum1 <= sum2)
		r = 1;
	else
		r = 0;

	return r;
}

/**
 * get_fpp function
 * The function computes the obliquity factor Fpp
 * @param el      The elevation (deg)
 * @return        The obliquity factor
 */
double get_fpp(double el) {
	double tmp1, tmp2;

	tmp1 = (EARTH_RADIUS * cos(el * PI / 180)) / (EARTH_RADIUS + HI);
	tmp2 = 1 - tmp1 * tmp1;

	return 1 / (sqrt(tmp2));
}

/**
 * get_sigmaiono function
 * The function computes degradation of the ionospheric correction - DO-229D table A.4.5.2
 * @param sigma_give2 	The sigma_give^2
 * @param t           	The current egnos time                              (s)
 * @param t_iono      	The time of transmission of the ionospheric message (s)
 * @param *msg10      	The pointer of the message type 10
 * @param flag			If 1 sigma_iono doesn't include the degradation factor
 * @return            	The degradation of the ionospheric model variance   (m^2)
 */
double get_sigmaIono2(double sigma_give2, double t, double t_iono,
		Egnos_msg * msg10, int flag) {
	double sigma_iono2, eps_iono;

	if (sigma_give2 != -1) {
		if((*msg10).tow != -1){
			eps_iono = (*msg10).ciono_step * floor((t - t_iono) / (*msg10).iiono)
			+ (*msg10).ciono_ramp * (t - t_iono);
			
			if(flag == 3)
				eps_iono = 0;

			if ((*msg10).rss_iono == 0)
				sigma_iono2 = (sqrt(sigma_give2) + eps_iono) * (sqrt(sigma_give2) + eps_iono);
			else
				sigma_iono2 = (sigma_give2) + (eps_iono * eps_iono);
		}else
			sigma_iono2 = sigma_give2;
	} else
		sigma_iono2 = 0;

	return sigma_iono2;
}

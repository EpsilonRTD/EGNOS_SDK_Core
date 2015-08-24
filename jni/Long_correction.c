/**
 * @file Long_correction.c
 *
 * @brief Long_correction module source file containing the long term
 * corrections parameters and degradations computation functions.
 * @details The class decodes Long term corrections messages from
 * EGNOS. It computes the EGNOS Long term corrections and
 * degradations as specified in the MOPS (DO-229D) document Appendix A.
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

#include "Long_correction.h"
#include "stdlib.h"
/**
 * set_LongCorrections_MT24 function.
 * The function creates a table of maximum 15 Egnos_msg structures from
 * long term corrections messages.
 * @param msg_t       The long corrections destination table
 * @param msgltc_char The input characters table of 25 messages. The first
 *                    12 characters of each line is the TOW in decimal.
 *                    The next 250 characters are the EGNOS payload.
 * @return            1 if successful, 0 if not, -1 parity issue
 */
int set_LongCorrections_MT24(Egnos_msg msg_t[25],char msgltc_char[25][263])
{
	char tow[13]="";
	char egnos[251]="";
	int i;
	int r = 1;
	int fast = 1; // flag to decode also fast corrections

	for(i = 0; i < 25; i++)
	{
		Egnos_msg msg;
		init_msg(&msg,24);

		strncpy(tow,msgltc_char[i],12);
		tow[12] = '\0';
		msg.tow = atof(tow);

		if(msg.tow != 0)
		{
			strncpy(egnos, msgltc_char[i]+12, 250);
			egnos[250] = '\0';
			msg.bin_msg = egnos;

			if(decode_msg24(&msg,fast)==0)
				r = 0;
		}
		else
		{
			msg.bin_msg = "";
			r = 0;
		}

		msg_t[i] = msg;
	}
	return r;
}

/**
 * set_LongCorrections_MT25 function.
 * The function creates a table of maximum 15 Egnos_msg structures from
 * long term corrections messages.
 * @param msg_t       The long corrections destination table
 * @param msgltc_char The input characters table of 10 messages. The first
 *                    12 characters of each line is the TOW in decimal.
 *                    The next 250 characters are the EGNOS payload.
 * @return            1 if successful, 0 if not, -1 parity issue
 */
int set_LongCorrections_MT25(Egnos_msg msg_t[15],char msgltc_char[15][263])
{
	char tow[13]="";
	char egnos[251]="";
	int i;
	int r = 1;

	for(i = 0; i < 15; i++)
	{
		Egnos_msg msg;
		init_msg(&msg,25);

		strncpy(tow,msgltc_char[i],12);
		tow[12] = '\0';
		msg.tow = atof(tow);

		if(msg.tow != 0)
		{
			strncpy(egnos, msgltc_char[i]+12, 250);
			egnos[250] = '\0';
			msg.bin_msg = egnos;

			if(decode_msg25(&msg)==0)
				r = 0;
		}
		else
		{
			msg.bin_msg = "";
			r = 0;
		}

		msg_t[i] = msg;
	}
	return r;
}


/**
 * set_LongCorrection function.
 * The function updates the Satellite structure, identified by its pointer,
 * with the long term corrections parameters. The function returns the long
 * term corrections degradation epsilon_ltc computed with the degradation
 * factors parameters from the message type 10 - DO-229D A.4.5.1.3.
 * @param *Sat      The Satellite pointer
 * @param  msg24_t  The table of the messages
 * @param msg25_t	The table of the message 25
 * @param *msg10    The pointer of the message type 10
 * @param *msg1     The pointer of the message type 1
 * @return          The degradation parameter in meters
 */
double set_LongCorrection(Satellite *Sat, Egnos_msg msg24_t[25],  Egnos_msg msg25_t[15], Egnos_msg *msg10, Egnos_msg *msg1)
{
	int i,j;
	double t,max[3];
	double eps_ltc = 0;

	t = (*Sat).tow2;

	for(i = 0; i < 25; i++)
	{
		if(((int)(*Sat).prn == (*msg1).prn[(int)msg24_t[i].prn_long[0][0]-1]) || ((int)(*Sat).prn == (*msg1).prn[(int)msg24_t[i].prn_long[1][0]-1]))
		{
			if(((int)(*Sat).prn == (*msg1).prn[(int)msg24_t[i].prn_long[0][0]-1]))
				j = 0;
			else
				j = 1;

			if((*Sat).iode_s2 == msg24_t[i].prn_long[j][1])
			{
				(*Sat).long_set = 1;
				(*Sat).dx = msg24_t[i].prn_long[j][2];
				(*Sat).dy = msg24_t[i].prn_long[j][3];
				(*Sat).dz = msg24_t[i].prn_long[j][4];
				(*Sat).daf0 = msg24_t[i].prn_long[j][5];
				(*Sat).ddx = msg24_t[i].prn_long[j][6];
				(*Sat).ddy = msg24_t[i].prn_long[j][7];
				(*Sat).ddz = msg24_t[i].prn_long[j][8];
				(*Sat).daf1 = msg24_t[i].prn_long[j][9];
				(*Sat).t0 = msg24_t[i].prn_long[j][10];

				// Computes the Long term correction degradation parameter
				if(msg24_t[i].velocity == 0)
				{
					eps_ltc = (*msg10).cltc_v0*floor((t - msg24_t[i].tow)/(*msg10).iltc_v0);
				}
				else
				{
					if(msg24_t[i].velocity == 1)
					{
						if(((*Sat).t0 < t) && (t < ((*Sat).t0 + (*msg10).iltc_v1)))
							eps_ltc = 0;
						else
						{
							max[0] = 0;
							max[1] = (*Sat).t0 - t;
							max[2] =  t - (*Sat).t0 - (*msg10).iltc_v1;
							eps_ltc = (*msg10).cltc_lsb + (*msg10).cltc_v1*max_(max);
						}
					}
					else
						eps_ltc = 0;

				}
			}
			else
				(*Sat).long_set = 0;
			break;	// PRN found in the mask, exit of the loop
		}
		if ((*Sat).long_set == 1)
			break;
	}

	for(i = 0; i < 15; i++){
		for (j = 0; j < 4; j++)
		{
			if(((int)(*Sat).prn == (*msg1).prn[(int)msg25_t[i].prn_long[j][0]-1]))
			{
				if((*Sat).iode_s2 == msg25_t[i].prn_long[j][1])
				{
					(*Sat).long_set = 1;
					(*Sat).dx 	= msg25_t[i].prn_long[j][2];
					(*Sat).dy 	= msg25_t[i].prn_long[j][3];
					(*Sat).dz 	= msg25_t[i].prn_long[j][4];
					(*Sat).daf0 = msg25_t[i].prn_long[j][5];
					(*Sat).ddx 	= msg25_t[i].prn_long[j][6];
					(*Sat).ddy 	= msg25_t[i].prn_long[j][7];
					(*Sat).ddz 	= msg25_t[i].prn_long[j][8];
					(*Sat).daf1 = msg25_t[i].prn_long[j][9];
					(*Sat).t0 	= msg25_t[i].prn_long[j][10];

					// Computes the Long term correction degradation parameter
					if(msg25_t[i].velocity == 0)
					{
						eps_ltc = (*msg10).cltc_v0*floor((t - msg25_t[i].tow)/(*msg10).iltc_v0);
					}
					else
					{
						if(msg25_t[i].velocity == 1)
						{
							if(((*Sat).t0 < t) && (t < ((*Sat).t0 + (*msg10).iltc_v1)))
								eps_ltc = 0;
							else
							{
								max[0] = 0;
								max[1] = (*Sat).t0 - t;
								max[2] =  t - (*Sat).t0 - (*msg10).iltc_v1;
								eps_ltc = (*msg10).cltc_lsb + (*msg10).cltc_v1*max_(max);
							}
						}
						else
							eps_ltc = 0;

					}
				}
				else
					(*Sat).long_set = 0;
				break;	// PRN found in the mask, exit of the loop

			}
		}
		if ((*Sat).long_set == 1)
			break;
	}
	return eps_ltc;
}

/**
 * max function.
 * The function finds the maximum of the table values
 * @param values	The 3 values to compare
 * @return 			The maximum of the 3 values
 */
double max_(double values[3])
{
	int i;
	double tmp1=0;
	double tmp2=0;
	tmp1 = values[0];
	for(i=0;i<3;i++)
	{
		tmp2 = values[i];
		if(tmp2>tmp1)
			tmp1 = values[i];
	}
	return tmp1;
}

/**
 * @file Fast_correction.c
 *
 * @brief Fast correction module source file containing the fast corrections
 * and model variances computation functions.
 * @details The module decodes fast corrections messages from SIS.
 * It computes the EGNOS fast corrections and the EGNOS fast and long term
 * corrections  model variance for a given Satellite as specified in the MOPS
 * (DO-229D) document Appendix A.
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

#include "Fast_correction.h"
#include "Positioning.h"
#include "stdlib.h"
/**
 * set_fastCorrectionsSis function.
 * The function creates a table of maximum 4 by 2 Egnos_msg structures from
 * fast correction messages.
 * @param msg2_5      The fast corrections destination table
 * @param msg2_5_char The input characters table of 8 messages
 *                    (positions 0-3:current TOW, positions 4-7:previous TOW).
 *                    The first 6 characters of each line is the TOW in
 *                    decimal. The next 250 characters are the EGNOS payload).
 * @return            1 if successful, 0 if not, -1 parity issue
 */
int set_fastCorrectionsSis(Egnos_msg msg2_5[4][2], char msg2_5_char[8][263])
{
	char tow[13]="";
	char egnos[251]="";
	int i;
	int r = 1;
	Egnos_msg msg_fc;
	
	for(i = 0; i < 8; i++){
		if(msg2_5_char[i][0] != '\0'){
			init_msg(&msg_fc,(i%4)+2);
			
			strncpy(tow,msg2_5_char[i],12);		// current messages (0-3)
			tow[12] = '\0';
			
			msg_fc.tow = atof(tow);
			android_syslog(ANDROID_LOG_INFO,"C: msg_fc.tow: %f",msg_fc.tow );
			
			strncpy(egnos,msg2_5_char[i]+12, 250);
			egnos[250] = '\0';
			msg_fc.bin_msg = egnos;//printf("egnos %s\n",msg_occ1.bin_msg);
	
			if(decode_msg2_5(&msg_fc)==0)
				r = 0;
	
		}else{
			msg_fc.tow 		= -1;
			msg_fc.bin_msg 	= "";
			msg_fc.use		= 0;
		}
		
		if(i < 4)
			msg2_5[i%4][0] = msg_fc;
		else
			msg2_5[i%4][1] = msg_fc;
		
	}

	return r;
}

/**
 * get_fastCorrection function
 * The function updates the Satellite structure, identified by its pointer,
 * with the computed pseudorange fast correction and the fast and long term
 * corrections model variance. The pseudorange fast correction is computed
 * from the messages types 2-5 and 24. The fast and long term corrections
 * model variance is computed with the UDRE degradations from the message
 * type 6 if it is broadcasted and the degradation factors from the messages
 * types 7 and 10. Otherwise the model variance is computed with the UDRE
 * degradations from the messages types 2-5 and the degradation factors from
 * the messages types 7 and 10.
 * @param *Sat      	The pointer of the Satellite
 * @param *msg1     	The pointer of the message type 1 (PRN mask)
 * @param msg2_5    	The table of messages type 2 to 5
 * @param msg24_t   	The table of messages type 24
 * @param *msg6     	The pointer of the message type 6
 * @param *msg10    	The pointer of the message type 10
 * @param *msg7     	The pointer of the message type 7
 * @param eps_ltc_m 	The previously computed degradation parameter for long term correction  (m)
 * @param rnd_options 	Determines whether or not UDRE 14 satellites are being used (for R&D)
 * @return          	1 if the computations is successfully performed, 0 if not
 */
int get_fastCorrection(Satellite * Sat, Egnos_msg * msg1, Egnos_msg msg2_5[4][2], Egnos_msg msg24_t[15], Egnos_msg * msg6,
		Egnos_msg * msg10, Egnos_msg * msg7, double eps_ltc_m,
		int rnd_options[8])
{
	int i,prn_pos,r,iodf0,iodf1,pos24;		// iodf0 = IODF current; iodf1 = IODF previous
	int check_prn = 0;
	double t,a,prc0,prc1,t0,t1,rrc,ifc;	// prc0 = PRC(current) = PRC(tof); prc1 = PRC(previous); t0 = tof ; t1 = tof of the previous msg
	double fast_delay = 0;
	double sigma_udre2 = 0;
	double sigma_flt2 = 0;
	double eps_fc = 0;			// Degradation parameter for fast correction data
	double eps_rrc = 0;			// Degradation parameter for range rate correction data
	double eps_ltc = 0;			// Degradation parameter for long term correction or GEO navigation message data
	double eps_er = 0;			// Degradation parameter for en routh through NPA app
	int flag_set = 0;

	int udrei0, udrei1;
	double rrc_correction=0;
	int use_mt24 = 0;
	int limit =0;
	limit = ((int)(*msg1).prn_nb/13)*13;
	if (((*msg1).prn_nb < 7 && (*msg1).prn_nb > 0) || ((*msg1).prn_nb < 20 && (*msg1).prn_nb > 13)
			|| ((*msg1).prn_nb < 33 && (*msg1).prn_nb > 26) || ((*msg1).prn_nb < 46 && (*msg1).prn_nb > 39)
			|| ((*msg1).prn_nb < 59 && (*msg1).prn_nb > 52))
		use_mt24 = 1;

	t = (*Sat).tow2;

	// Search in the prn table of message type 1
	for(i=0;i<(*msg1).prn_nb;i++)
	{
		if((*Sat).prn == (*msg1).prn[i])
		{
			check_prn = 1;
			prn_pos = i+1;
		}
	}
	
	// Check if correction is available for the PRN defining the Satellite
	if(check_prn == 1)
	{
		//prn_pos is the position-1 in the prn mask table
		if(prn_pos <= 13)
		{
			// Current and previous Fast correction
			prc0 = msg2_5[0][0].prc[prn_pos-1];
			prc1 = msg2_5[0][1].prc[prn_pos-1];
			udrei0 = msg2_5[0][0].udre[prn_pos-1];
			udrei1 = msg2_5[0][1].udre[prn_pos-1];
		if(udrei0 == 14 && rnd_options[0] != 1)	 
			(*Sat).use=1;
		if(udrei0 == 15)	 
			(*Sat).use=1;

			if (msg2_5[0][0].use == 1 )
				flag_set = 1;

			// Current and previous time of applicability
			t0 = msg2_5[0][0].tow;
			t1 = msg2_5[0][1].tow;

			// Model variance
			if((*msg6).tow  != -1 && (((*Sat).tow-(*msg6).tow) < 18))
			{
				if(((*msg6).iodf_msg6[0] == msg2_5[0][0].iodf) || ((*msg6).iodf_msg6[0] == 3)){
					//sigma_udre2 = get_UDREaccuracy((*msg6).udre[prn_pos-13-1]);
					sigma_udre2 = get_UDREaccuracy((*msg6).udre_msg6[prn_pos-1]);
					if((*msg6).udre_msg6[prn_pos-1] == 14 && rnd_options[0] != 1 )
						(*Sat).use=1;
					if((*msg6).udre_msg6[prn_pos-1] == 15 )
						(*Sat).use=1;
				}
				else{
					sigma_udre2 = get_UDREaccuracy((int)msg2_5[0][0].udre[prn_pos-1]);
				}
			}
			else
				sigma_udre2 = get_UDREaccuracy((int)msg2_5[0][0].udre[prn_pos-1]);

			// Current and previous IODF
			iodf0 = msg2_5[0][0].iodf;
			iodf1 = msg2_5[0][1].iodf;
		}
		if((prn_pos > 13) && (prn_pos <= 26))
		{
			prc0 = msg2_5[1][0].prc[prn_pos-13-1];
			prc1 = msg2_5[1][1].prc[prn_pos-13-1];
			udrei0 = msg2_5[1][0].udre[prn_pos-13-1];
			udrei1 = msg2_5[1][1].udre[prn_pos-13-1];
			
			if(udrei0 == 14 && rnd_options[0] != 1)	 
				(*Sat).use=1;
			if(udrei0 == 15)	 
				(*Sat).use=1;
			
			t0 = msg2_5[1][0].tow;
			t1 = msg2_5[1][1].tow;

			if (msg2_5[1][0].use == 1 )
				flag_set = 1;

			if((*msg6).tow  != -1 && (((*Sat).tow-(*msg6).tow) < 18))
			{
				if(((*msg6).iodf_msg6[1] == msg2_5[1][0].iodf) || ((*msg6).iodf_msg6[1] == 3)){
					//sigma_udre2 = get_UDREaccuracy((*msg6).udre[prn_pos-13-1]);
					sigma_udre2 = get_UDREaccuracy((*msg6).udre_msg6[prn_pos-1]);
					if((*msg6).udre_msg6[prn_pos-1] == 14 && rnd_options[0] != 1 )
						(*Sat).use=1;
					if((*msg6).udre_msg6[prn_pos-1] == 15 )
						(*Sat).use=1;
				}
				else{
					sigma_udre2 = get_UDREaccuracy((int)msg2_5[1][0].udre[prn_pos-13-1]);
				}
			}
			else
				sigma_udre2 = get_UDREaccuracy((int)msg2_5[1][0].udre[prn_pos-13-1]);

			iodf0 = msg2_5[1][0].iodf;
			iodf1 = msg2_5[1][1].iodf;
		}
		if((prn_pos > 26) && (prn_pos <= 39))
		{
			prc0 = msg2_5[2][0].prc[prn_pos-26-1];
			prc1 = msg2_5[2][1].prc[prn_pos-26-1];
			udrei0 = msg2_5[2][0].udre[prn_pos-26-1];
			udrei1 = msg2_5[2][1].udre[prn_pos-26-1];
					
			if(udrei0 == 14 && rnd_options[0] != 1)	 
			(	*Sat).use=1;
			if(udrei0 == 15)	 
				(*Sat).use=1;
			
			t0 = msg2_5[2][0].tow;
			t1 = msg2_5[2][1].tow;

			if (msg2_5[2][0].use == 1 )
				flag_set = 1;

			if((*msg6).tow  != -1 && (((*Sat).tow-(*msg6).tow) < 18))
			{
				if(((*msg6).iodf_msg6[2] == msg2_5[2][0].iodf) || ((*msg6).iodf_msg6[2] == 3)){
					//sigma_udre2 = get_UDREaccuracy((*msg6).udre[prn_pos-26-1]);
					sigma_udre2 = get_UDREaccuracy((*msg6).udre_msg6[prn_pos-1]);
					if((*msg6).udre_msg6[prn_pos-1] == 14 && rnd_options[0] != 1 )
						(*Sat).use=1;
					if((*msg6).udre_msg6[prn_pos-1] == 15 )
						(*Sat).use=1;
				}
				else{
					sigma_udre2 = get_UDREaccuracy((int)msg2_5[2][0].udre[prn_pos-26-1]);
				}
			}
			else
				sigma_udre2 = get_UDREaccuracy((int)msg2_5[2][0].udre[prn_pos-26-1]);

			iodf0 = msg2_5[2][0].iodf;
			iodf1 = msg2_5[2][1].iodf;
		}
		if((prn_pos > 39) && (prn_pos <= 51))
		{
			prc0 = msg2_5[3][0].prc[prn_pos-39-1];
			prc1 = msg2_5[3][1].prc[prn_pos-39-1];
			udrei0 = msg2_5[3][0].udre[prn_pos-39-1];
			udrei1 = msg2_5[3][1].udre[prn_pos-39-1];
					
			if(udrei0 == 14 && rnd_options[0] != 1)	 
				(*Sat).use=1;
			if(udrei0 == 15)	 
				(*Sat).use=1;
				
			t0 = msg2_5[3][0].tow;
			t1 = msg2_5[3][1].tow;

			if (msg2_5[3][0].use == 1 )
				flag_set = 1;

			if((*msg6).tow  != -1 && (((*Sat).tow-(*msg6).tow) < 18))
			{
				if(((*msg6).iodf_msg6[3] == msg2_5[3][0].iodf) || ((*msg6).iodf_msg6[3] == 3)){
					//sigma_udre2 = get_UDREaccuracy((*msg6).udre[prn_pos-39-1]);
					sigma_udre2 = get_UDREaccuracy((*msg6).udre_msg6[prn_pos-1]);
					if((*msg6).udre_msg6[prn_pos-1] == 14 && rnd_options[0] != 1 )
						(*Sat).use=1;
					if((*msg6).udre_msg6[prn_pos-1] == 15 )
						(*Sat).use=1;
				}
				else{
					sigma_udre2 = get_UDREaccuracy((int)msg2_5[3][0].udre[prn_pos-39-1]);
				}
			}
			else
				sigma_udre2 = get_UDREaccuracy((int)msg2_5[3][0].udre[prn_pos-39-1]);

			iodf0 = msg2_5[3][0].iodf;
			iodf1 = msg2_5[3][1].iodf;
		}

		// if not corrections available for the Satellite, check in message 24
		if(use_mt24 == 1 && msg24_t[0].use == 1 && prn_pos > limit)
		{
			// pos24 is the position in the udre/prc table for message 24, (PRN position in PRN mask)-block_id*13 -1; corrections in message 24
			pos24 = prn_pos-(msg24_t[0].block_id)*13-1;
			prc0 = msg24_t[0].prc[pos24];
			prc1 = msg24_t[1].prc[pos24];
			udrei0 = msg24_t[0].udre[pos24];
			udrei1 = msg24_t[1].udre[pos24];
			
			if(udrei0 == 14 && rnd_options[0] != 1)	 
				(*Sat).use=1;
			if(udrei0 == 15)	 
				(*Sat).use=1;
			
			t0 = msg24_t[0].tow;
			t1 = msg24_t[1].tow;

			if (msg24_t[0].use == 1 )
				flag_set =1 ;

			if((*msg6).tow  != -1 && (((*Sat).tow-(*msg6).tow) < 18))
			{
				if(((*msg6).iodf_msg6[3] == msg24_t[0].iodf) || ((*msg6).iodf_msg6[3] == 3))
					//sigma_udre2 = get_UDREaccuracy((*msg6).udre[prn_pos-13-1]);
					sigma_udre2 = get_UDREaccuracy((*msg6).udre_msg6[prn_pos-1]);
			}
			else
				sigma_udre2 = get_UDREaccuracy((int)msg24_t[0].udre[pos24]);

			iodf0 = msg24_t[0].iodf;
			iodf1 = msg24_t[1].iodf;
		}

		// According to (DO-229D J.2.2) no active MT 7 or 10 applied to obtain sigma_flt2
		if( ((*msg7).tow == -1 || (*msg10).tow == -1) )//|| (eps_ltc_m > 0) )
			sigma_flt2 = (sqrt(sigma_udre2) + 8)*(sqrt(sigma_udre2) + 8);
		else
		{
			if(t0 != -1 && flag_set == 1)	// t0 == -1 : No correction available
			{
				// Fast corrections degradation factor
				a = (*msg7).ai[prn_pos-1][1];

				// User time out
				ifc = (*msg7).ai[prn_pos-1][2];

				// Check fast corrections factor
				if(a != 0)
				{
					// Compute the range rate correction
					if((t0 - 1) > t1 && udrei1 != -1 && prc1!=0)
						rrc = (prc0 - prc1)/(t0 - t1);
					else
						rrc = 0;
						
					if(rnd_options[4] == 1)
						rrc = 0;

					if (rrc != 0)
						// Compute the range rate correction degradation
						eps_rrc = get_epsilonRrc(a,t,ifc,(*msg10).brrc,iodf0,iodf1,t0,t1);
				}
				else
				{
					// Range rate correction equals to 0
					rrc = 0;

					// Range rate correction degradation equals to 0
					eps_rrc = 0;
				}

				
				// Compute fast delay in meters
				fast_delay = prc0 + rrc*(t - t0);
				rrc_correction = rrc*(t - t0);


				// Computes the degradation parameter for fast correction
				eps_fc = get_epsilonFc(a,t,t0,(*msg7).tlat);

				// Computes the degradation parameter for long term correction,get from long_correction computation
				eps_ltc = eps_ltc_m;
				
				//eps_ltc = 0;
				//eps_fc = 0;
				//eps_rrc = 0;

				eps_er = (*msg10).cer;
				
				(*Sat).eps_fc 	= eps_fc;
				(*Sat).eps_rrc 	= eps_rrc;
				(*Sat).eps_ltc 	= eps_ltc;
				(*Sat).eps_er 	= eps_er;
				
				if(rnd_options[0] == 3){
					eps_fc = 0;
					eps_rrc = 0;
					eps_ltc = 0;
					eps_er = 0;
				}
					
		
				if((*msg10).rss_udre == 0)
					sigma_flt2 = (sqrt(sigma_udre2) + eps_fc + eps_rrc + eps_ltc + eps_er)*(sqrt(sigma_udre2) + eps_fc + eps_rrc + eps_ltc + eps_er);
				else
					sigma_flt2 = sigma_udre2 + eps_fc*eps_fc + eps_rrc*eps_rrc + eps_ltc*eps_ltc + eps_er*eps_er;
			}
			else
			{
				fast_delay = 0;
				sigma_flt2 = 0;
			}
		}
		r = 1;
	}
	else
	{
		fast_delay = 0;
		sigma_flt2 = 0;
		r = 0;
	}
	
	if ((*Sat).type_sat == 2)
	{
		(*Sat).fast_set = 1;
		(*Sat).use 		= 2;
	}
	
	(*Sat).fast_set = flag_set;
	(*Sat).udrei = udrei0;
	if((*Sat).udrei < 14 || ((*Sat).udrei < 15 && rnd_options[0] == 1 )){
		(*Sat).rrc = rrc_correction;
		(*Sat).fast_delay = fast_delay;
		(*Sat).sigma_flt2 = sigma_flt2;
	}else{
		(*Sat).rrc = 0;
		(*Sat).fast_delay = 0;
		(*Sat).sigma_flt2 = 0;
	}
	

	return r;
}

/**
 * get_epsilonFc function.
 * The function computes and returns the degradation parameter
 * for fast correction - DO-229D A.4.5.1.1
 * @param a    The fast degradation factor	from message type 10 (m/s^2)
 * @param t    The current egnos time                            (s)
 * @param tu   The time of applicability of the fast correction	 (s)
 * @param tlat System latency from Message type 7                (s)
 * @return     The degradation parameter in meters
 */
double get_epsilonFc(double a, double t, double tu, double tlat)
{
	double tmp = t - tu + tlat;
	return a*tmp*tmp/2;
}

/**
 * get_epsilonRrc function.
 * The function computes and returns the degradation parameter
 * for range rate correction - DO-229D A.4.5.1.2
 * @param a     The fast degradation factor	from message type 10 (m/s^2)
 * @param t     The current egnos time                           (s)
 * @param ifc   The time-out interval                            (s)
 * @param brrc  Brrc from message type 10                        (m)
 * @param iodf0 Current Issue of Data - Fast correction
 * @param iodf1 Previous Issue of Data - Fast correction
 * @param t0    Current time of applicability                    (s)
 * @param t1    Previous time of applicability                   (s)
 * @return      The degradation parameter in meters
 */
double get_epsilonRrc(double a, double t, double ifc, double brrc, unsigned short iodf0, unsigned short iodf1, double t0, double t1)
{
	double eps_rrc,tmp;

	if(iodf0 != 3 && iodf1 != 3)
	{
		if((iodf0-iodf1)%3 == 1)
			eps_rrc = 0;
		else
		{
			if(t0 != t1)	// Check division by 0
				eps_rrc =(a*ifc/4 + brrc/(t0 - t1))*(t - t0);
			else
				eps_rrc = a*ifc/4;
		}
	}
	else
	{
		tmp = fabs((t0 - t1)-ifc/2);

		if(tmp == 0)
			eps_rrc = 0;
		else
		{
			if(t0 != t1)	// Check division by 0
				eps_rrc = (a*tmp/2 + brrc/(t0 - t1))*(t - t0);
			else
				eps_rrc = a*tmp/2;
		}
	}

	return eps_rrc;
}

/**
 * get_UDREaccuracy function.
 * The function returns the UDRE accuracy equivalent to the UDRE indicator
 * - DO-229D table A-6
 * @param udrei The UDRE indicator
 * @return      The UDRE accuracy (m^2) (0:Not monitored, -1:Do not use)
 */
double get_UDREaccuracy(int udrei)
{
	double acc = 0;
	switch(udrei)
	{
		case 0: acc = 0.0520;
			break;
		case 1: acc = 0.0924;
			break;
		case 2: acc = 0.1444;
			break;
		case 3: acc = 0.2830;
			break;
		case 4: acc = 0.4678;
			break;
		case 5: acc = 0.8315;
			break;
		case 6: acc = 1.2992;
			break;
		case 7: acc = 1.8709;
			break;
		case 8: acc = 2.5465;
			break;
		case 9: acc = 3.3260;
			break;
		case 10: acc = 5.1968;
			break;
		case 11: acc = 20.7870;
			break;
		case 12: acc = 230.9661;
			break;
		case 13: acc = 2078.695;
			break;
		case 14: acc = 0;
			break;
		case 15: acc = -1;
			break;
	}
	return acc;
}


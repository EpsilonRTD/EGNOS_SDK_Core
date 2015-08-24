/**
 * @file Egnos.c
 *
 * @brief Egnos module source file containing the EGNOS messages parsing
 * functions.
 * @details The module creates an Egnos_msg structure. The structure defines
 * an EGNOS message  and contains all parameters related to an EGNOS message.
 * It includes  parameters common to all message types, the type, the payload,
 *  and the time of week.
 * The other parameters are type specific. The class contains all functions
 * to parse an EGNOS message as specified in the MOPS (DO-229D)
 * document Appendix A.
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
 * @mainpage The EGNOS SDK
 * <b>EGNOS</b> is a satellite-based augmentation system (SBAS), an infrastructure that consists
 * of three geostationary satellites over Europe and a network of ground stations. The
 * system improves the open public service offered by the Global Positioning System (GPS)
 * by providing corrections of the error caused by the ionosphere.
 *
 * <b>SISNeT</b> is a server that provides the same EGNOS signal as it would be received from the
 * satellites through terrestrial communication, allowing to receive the EGNOS corrections
 * independently of the EGNOS signal-in-space. The user must register at ESA to be able to
 * access the free-of-charge service.
 *
 * The <b>EGNOS SDK</b> has been designed to allow application developers to take advantage of the
 * EGNOS benefits, and to use them for the software they develop for mobile devices. The
 * open-source library in the EGNOS SDK offers the possibility to include EGNOS corrections
 * for a more accurate position, as well as Integrity.
 *
 * The <b>Demonstration Application</b> shows the main features of the EGNOS SDK at work, providing
 * application developers with examples on how the EGNOS library can be used and showing the
 * benefits of the EGNOS corrections on positioning.
 *
 * The <b>EGNOS SDK Interface</b> provides the necessary functionalities for interfacing
 * the GUI with the software receiver.
 *
 * For additional information on the EGNOS SDK please visit <a>http://www.egnos-portal.eu/</a>
 *
 */

#include "Egnos.h"
#include "stdarg.h"
#include <android/log.h>
#define APPNAME "EGNOS"


/**
 * init_msg function.
 * The function initializes the Egnos_msg structure, identified by its pointer.
 * The type of the message is defined.
 * The values of the PRN mask are initialized to 0 and all the others are
 * initialized to -1.
 * @param *msg  The pointer of the message
 * @param type  The type of the message
 */
void init_msg(Egnos_msg * msg, int type)
{
	int i,j;

	// Definition of the message type
	(*msg).m_type = type;

	// Initialization of the PRN table and Fast corrections degradation factor table
	for(i=0;i<51;i++)
	{
		(*msg).prn[i] = 0;
		(*msg).ai[i][0] = 0;
		(*msg).ai[i][1] = 0;
		(*msg).ai[i][2] = 0;
		(*msg).ai[i][3] = 0;
		(*msg).ai[i][4] = 0;
	}

	// Initialization of the igp_blocks table
	for(i=0;i<210;i++)
	{
		(*msg).igp_blocks[i][0] = -1;
		(*msg).igp_blocks[i][1] = -1;
		(*msg).igp_blocks[i][2] = -1;
	}

	// Init of Long term corrections
	for(i=0;i<4;i++)
	{
		for(j=0;j<11;j++)
			(*msg).prn_long[i][j] = 0;

	}

	// Init of fast corrections
	for(i=0;i<13;i++)
	{
		(*msg).prc[i] = 0;
		(*msg).udre[i] = -1;
	}

	(*msg).a0snt = -1;
	(*msg).a1snt = -1;
	(*msg).band_id = -1;
	(*msg).band_nb = -1;
	(*msg).block_id = -1;
	(*msg).block_nb = -1;
	(*msg).brrc = -1;
	(*msg).cer = -1;
	(*msg).cgeo_lsb = -1;
	(*msg).cgeo_v = -1;
	(*msg).ciono_ramp = -1;
	(*msg).ciono_step = -1;
	(*msg).cltc_lsb = -1;
	(*msg).cltc_v0 = -1;
	(*msg).cltc_v1 = -1;
	(*msg).dn = -1;
	(*msg).dtls = -1;
	(*msg).dtlsf = -1;
	(*msg).gps_tow = -1;
	(*msg).gps_wknb = -1;
	(*msg).igeo = -1;
	(*msg).iiono =-1;
	(*msg).iltc_v0 = -1;
	(*msg).iltc_v1 = -1;
	(*msg).iodf = -1;
	(*msg).iodi = -1;
	(*msg).prn_nb = -1;
	(*msg).rss_iono = -1;
	(*msg).rss_udre = -1;
	(*msg).t0t = -1;
	(*msg).tlat = -1;
	(*msg).tow = -1;
	(*msg).utc_id = -1;
	(*msg).velocity = -1;
	(*msg).wknb = -1;
	(*msg).wnlsf = -1;
	(*msg).wnt = -1;
	(*msg).use = 0;
	(*msg).ranging = 0;
}

/**
 * decode_msg1 function.
 * The function decodes the message type 1 (PRN mask) according to the DO-229D
 * document (A.4.4.2). The function updates the Egnos_msg structure,
 * identified by its pointer, with the PRN mask table.
 * @param *msg  The pointer of the message
 * @return    1 if successful, 0 if not, -1 parity issue
 */
int decode_msg1(Egnos_msg * msg)
{
	int i,j;
	int r = 0;
	char type[7];
	char tmp[139];
	char bin[1];

	// Message type check
	extract((*msg).bin_msg,8,13,type);
	if(bin2dec(type) == (*msg).m_type)
	{
		// GPS PRN : slots 1-37 (i : 0-36); j is the prn mask, i+1 is the prn
		strncpy(tmp, (*msg).bin_msg + 14, 138);
		tmp[138] = '\0';

		j = 0;
		for(i=0;i<138;i++)		// 37:max size of gps prns
		{
			extract(tmp,i,i,bin);
			if(strcmp(bin,"1") == 0)
			{
				(*msg).prn[j] = i+1;
				j++;
			}
		}
		// Save the number of PRNs
		(*msg).prn_nb = j;

		// IODP
		/*extract((*msg).bin_msg,224,225,tmp);
		(*msg).iodp = bin2dec(tmp);*/

		r = 1;
	}
	else
		r = 0;

	return r;
}

/**
 * decode_msg2_5 function.
 * The function decodes the message type 2 to 5 (fast corrections) according
 * to the DO-229D document (A.4.4.2).
 * The function updates the Egnos_msg structure, identified by its pointer,
 * with the fast corrections parameters (PRC and UDRE)
 * indicator parameters.
 * @param   *msg  The pointer of the message
 * @return      1 if successful, 0 if not, -1 parity issue
 */
int decode_msg2_5(Egnos_msg * msg)
{
	int i,int_tmp;
	int r = 0;
	char type[6];
	char tmp[100];

	// Message type check
	extract((*msg).bin_msg,8,13,type);
	if(bin2dec(type) == (*msg).m_type)
	{
		(*msg).use = 1;
		extract((*msg).bin_msg,14,15,tmp);
		(*msg).iodf = bin2dec(tmp);

		for(i=0;i<13;i++)
		{
			extract((*msg).bin_msg,18+12*i,29+12*i,tmp);
			int_tmp = bin2dec(tmp);
			if(int_tmp > (TWO_POWER_11 - 1))	// signed conversion (12 bits)
				int_tmp -= TWO_POWER_12;
			(*msg).prc[i] = int_tmp*0.125;


			extract((*msg).bin_msg,174+4*i,177+4*i,tmp);
			(*msg).udre[i] = bin2dec(tmp);

		}
		r = 1;

	}

	return r;
}

/**
 * decode_msg24 function.
 * The function decodes the message type 24 (Mixed Fast and Long term
 * corrections) according to the DO-229D document (A.4.4.8). The function
 * updates the Egnos_msg structure, identified by its pointer, with the long
 * term corrections parameters and fast corrections parameters (PRC and UDRE)
 * if the fast corrections flag is equal to 1.
 * @param   *msg  The pointer of the message
 * @param   fast  The fast corrections flag (0:Fast corrections not parsed,
 *                                           1:Fast corrections parsed)
 * @return      1 if successful, 0 if not, -1 parity issue
 */
int decode_msg24(Egnos_msg * msg, int fast)
{
	int int_tmp,i;
	int r = 0;
	char type[6];
	char tmp[100];
	char bin[1];

	// Message type check
	extract((*msg).bin_msg,8,13,type);

	if(bin2dec(type) == (*msg).m_type)
	{
		(*msg).use = 1;

		// Check IODP equals to IODP of message type 1
		// Fast corrections
		if(fast == 1)
		{
			for(i=0;i<6;i++)
			{
				extract((*msg).bin_msg,14+12*i,25+12*i,tmp);
				int_tmp = bin2dec(tmp);
				if(int_tmp > (TWO_POWER_11 - 1))	// signed conversion (12 bits)
					int_tmp -= TWO_POWER_12;
				(*msg).prc[i] = int_tmp*0.125;

				extract((*msg).bin_msg,86+4*i,89+4*i,tmp);
				(*msg).udre[i] = bin2dec(tmp);
			}
			extract((*msg).bin_msg,112,113,tmp);
			(*msg).block_id = bin2dec(tmp);
		}

		extract((*msg).bin_msg,106+14,106+14,bin);
		(*msg).velocity = bin2dec(bin);

		if((*msg).velocity == 0)//velocity code = 0
		{
			extract((*msg).bin_msg,106+15,106+20,tmp);	//PRN
			(*msg).prn_long[0][0] = (double)bin2dec(tmp);

			extract((*msg).bin_msg,106+21,106+28,tmp);	//IODE
			(*msg).prn_long[0][1] = (double)bin2dec(tmp);

			extract((*msg).bin_msg,106+29,106+37,tmp);	//dx
			int_tmp = bin2dec(tmp);
			if(int_tmp > (TWO_POWER_8 - 1))	// signed conversion (9 bits)
				int_tmp -= TWO_POWER_9;
			(*msg).prn_long[0][2] = int_tmp*0.125;

			extract((*msg).bin_msg,106+38,106+46,tmp);	//dy
			int_tmp = bin2dec(tmp);
			if(int_tmp > (TWO_POWER_8 - 1))	// signed conversion (9 bits)
				int_tmp -= TWO_POWER_9;
			(*msg).prn_long[0][3] = int_tmp*0.125;

			extract((*msg).bin_msg,106+47,106+55,tmp);	//dz
			int_tmp = bin2dec(tmp);
			if(int_tmp > (TWO_POWER_8 - 1))	// signed conversion (9 bits)
				int_tmp -= TWO_POWER_9;
			(*msg).prn_long[0][4] = int_tmp*0.125;

			extract((*msg).bin_msg,106+56,106+65,tmp);	//daf0
			int_tmp = bin2dec(tmp);
			if(int_tmp > (TWO_POWER_9 - 1))	// signed conversion (10 bits)
				int_tmp -= TWO_POWER_10;
			(*msg).prn_long[0][5] = int_tmp*TWO_POWER_m31;

			extract((*msg).bin_msg,106+66,106+71,tmp);	// PRN
			(*msg).prn_long[1][0] = (double)bin2dec(tmp);

			extract((*msg).bin_msg,106+72,106+79,tmp);	//IODE
			(*msg).prn_long[1][1] = (double)bin2dec(tmp);

			extract((*msg).bin_msg,106+80,106+88,tmp);	//dx
			int_tmp = bin2dec(tmp);
			if(int_tmp > (TWO_POWER_8 - 1))	// signed conversion (9 bits)
				int_tmp -= TWO_POWER_9;
			(*msg).prn_long[1][2] = int_tmp*0.125;

			extract((*msg).bin_msg,106+89,106+97,tmp);	//dy
			int_tmp = bin2dec(tmp);
			if(int_tmp > (TWO_POWER_8 - 1))	// signed conversion (9 bits)
				int_tmp -= TWO_POWER_9;
			(*msg).prn_long[1][3] = int_tmp*0.125;

			extract((*msg).bin_msg,106+98,106+106,tmp);	//dz
			int_tmp = bin2dec(tmp);
			if(int_tmp > (TWO_POWER_8 - 1))	// signed conversion (9 bits)
				int_tmp -= TWO_POWER_9;
			(*msg).prn_long[1][4] = int_tmp*0.125;

			extract((*msg).bin_msg,106+107,106+116,tmp);//daf0
			int_tmp = bin2dec(tmp);
			if(int_tmp > (TWO_POWER_9 - 1))	// signed conversion (10 bits)
				int_tmp -= TWO_POWER_10;
			(*msg).prn_long[1][5] = int_tmp*TWO_POWER_m31;

			r = 1;
		}
		else
		{
			if((*msg).velocity == 1)//velocity code = 1
			{
				extract((*msg).bin_msg,106+15,106+20,tmp);	// PRN
				(*msg).prn_long[0][0] = (double)bin2dec(tmp);

				extract((*msg).bin_msg,106+21,106+28,tmp);	//IODE
				(*msg).prn_long[0][1] = (double)bin2dec(tmp);

				extract((*msg).bin_msg,106+29,106+39,tmp);	//dx
				int_tmp = bin2dec(tmp);
				if(int_tmp > (TWO_POWER_10 - 1))	// signed conversion (11 bits)
					int_tmp -= TWO_POWER_11;
				(*msg).prn_long[0][2] = int_tmp*0.125;

				extract((*msg).bin_msg,106+40,106+50,tmp);	//dy
				int_tmp = bin2dec(tmp);
				if(int_tmp > (TWO_POWER_10 - 1))	// signed conversion (11 bits)
					int_tmp -= TWO_POWER_11;
				(*msg).prn_long[0][3] = int_tmp*0.125;

				extract((*msg).bin_msg,106+51,106+61,tmp);	//dz
				int_tmp = bin2dec(tmp);
				if(int_tmp > (TWO_POWER_10 - 1))	// signed conversion (11 bits)
					int_tmp -= TWO_POWER_11;
				(*msg).prn_long[0][4] = int_tmp*0.125;

				extract((*msg).bin_msg,106+62,106+72,tmp);	//daf0
				int_tmp = bin2dec(tmp);
				if(int_tmp > (TWO_POWER_10 - 1))	// signed conversion (11 bits)
					int_tmp -= TWO_POWER_11;
				(*msg).prn_long[0][5] = int_tmp*TWO_POWER_m31;

				extract((*msg).bin_msg,106+73,106+80,tmp);	//ddx
				int_tmp = bin2dec(tmp);
				if(int_tmp > (TWO_POWER_7 - 1))	// signed conversion (8 bits)
					int_tmp -= TWO_POWER_8;
				(*msg).prn_long[0][6] = int_tmp*TWO_POWER_m11;

				extract((*msg).bin_msg,106+81,106+88,tmp);	//ddy
				int_tmp = bin2dec(tmp);
				if(int_tmp > (TWO_POWER_7 - 1))	// signed conversion (8 bits)
					int_tmp -= TWO_POWER_8;
				(*msg).prn_long[0][7] = int_tmp*TWO_POWER_m11;

				extract((*msg).bin_msg,106+89,106+96,tmp);	//ddz
				int_tmp = bin2dec(tmp);
				if(int_tmp > (TWO_POWER_7 - 1))	// signed conversion (8 bits)
					int_tmp -= TWO_POWER_8;
				(*msg).prn_long[0][8] = int_tmp*TWO_POWER_m11;

				extract((*msg).bin_msg,106+97,106+104,tmp);	//daf1
				int_tmp = bin2dec(tmp);
				if(int_tmp > (TWO_POWER_7 - 1))	// signed conversion (8 bits)
					int_tmp -= TWO_POWER_8;
				(*msg).prn_long[0][9] = int_tmp*TWO_POWER_m39;

				extract((*msg).bin_msg,106+105,106+117,tmp);	//t0
				(*msg).prn_long[0][10] = bin2dec(tmp)*16;

				r = 1;
			}
		}
	}
	else
		r = 0;

	return r;
}

/**
 * decode_msg25 function.
 * The function decodes the message type 25 (Long term corrections) according
 * to the DO-229D document (A.4.4.7). The function updates the Egnos_msg
 * structure, identified by its pointer,  with the long term corrections
 * parameters.
 * @param   *msg  The pointer of the message
 * @return      1 if successful, 0 if not, -1 parity issue
 */
int decode_msg25(Egnos_msg * msg)
{
	int i,j,int_tmp;
	int r = 0;
	char type[6];
	char tmp[100];
	char bin[1];

	// Message type check
	extract((*msg).bin_msg,8,13,type);
	(*msg).m_type=25;
	if(bin2dec(type) == (*msg).m_type)
	{
		extract((*msg).bin_msg,14,14,bin);
		(*msg).velocity = bin2dec(bin);

		for(j=0;j<4;j=j+2)
		{
			if(j==2)
				i=106;
			else
				i=0;

			if((*msg).velocity == 0)//velocity code = 0
			{
				extract((*msg).bin_msg,i+15,i+20,tmp);	//PRN
				(*msg).prn_long[0+j][0] = (double)bin2dec(tmp);

				extract((*msg).bin_msg,i+21,i+28,tmp);	//IODE
				(*msg).prn_long[0+j][1] = (double)bin2dec(tmp);

				extract((*msg).bin_msg,i+29,i+37,tmp);	//dx
				int_tmp = bin2dec(tmp);
				if(int_tmp > (TWO_POWER_8 - 1))	// signed conversion (9 bits)
					int_tmp -= TWO_POWER_9;
				(*msg).prn_long[0+j][2] = int_tmp*0.125;

				extract((*msg).bin_msg,i+38,i+46,tmp);	//dy
				int_tmp = bin2dec(tmp);
				if(int_tmp > (TWO_POWER_8 - 1))	// signed conversion (9 bits)
					int_tmp -= TWO_POWER_9;
				(*msg).prn_long[0+j][3] = int_tmp*0.125;

				extract((*msg).bin_msg,i+47,i+55,tmp);	//dz
				int_tmp = bin2dec(tmp);
				if(int_tmp > (TWO_POWER_8 - 1))	// signed conversion (9 bits)
					int_tmp -= TWO_POWER_9;
				(*msg).prn_long[0+j][4] = int_tmp*0.125;

				extract((*msg).bin_msg,i+56,i+65,tmp);	//daf0
				int_tmp = bin2dec(tmp);
				if(int_tmp > (TWO_POWER_9 - 1))	// signed conversion (10 bits)
					int_tmp -= TWO_POWER_10;
				(*msg).prn_long[0+j][5] = int_tmp*TWO_POWER_m31;

				extract((*msg).bin_msg,i+66,i+71,tmp);	// PRN
				(*msg).prn_long[1+j][0] = (double)bin2dec(tmp);

				extract((*msg).bin_msg,i+72,i+79,tmp);	//IODE
				(*msg).prn_long[1+j][1] = (double)bin2dec(tmp);

				extract((*msg).bin_msg,i+80,i+88,tmp);	//dx
				int_tmp = bin2dec(tmp);
				if(int_tmp > (TWO_POWER_8 - 1))	// signed conversion (9 bits)
					int_tmp -= TWO_POWER_9;
				(*msg).prn_long[1+j][2] = int_tmp*0.125;

				extract((*msg).bin_msg,i+89,i+97,tmp);	//dy
				int_tmp = bin2dec(tmp);
				if(int_tmp > (TWO_POWER_8 - 1))	// signed conversion (9 bits)
					int_tmp -= TWO_POWER_9;
				(*msg).prn_long[1+j][3] = int_tmp*0.125;

				extract((*msg).bin_msg,i+98,i+106,tmp);	//dz
				int_tmp = bin2dec(tmp);
				if(int_tmp > (TWO_POWER_8 - 1))	// signed conversion (9 bits)
					int_tmp -= TWO_POWER_9;
				(*msg).prn_long[1+j][4] = int_tmp*0.125;

				extract((*msg).bin_msg,i+107,i+116,tmp);//daf0
				int_tmp = bin2dec(tmp);
				if(int_tmp > (TWO_POWER_9 - 1))	// signed conversion (10 bits)
					int_tmp -= TWO_POWER_10;
				(*msg).prn_long[1+j][5] = int_tmp*TWO_POWER_m31;

				r = 1;

			}
			if((*msg).velocity == 1)//velocity code = 1
			{
				extract((*msg).bin_msg,i+15,i+20,tmp);	// PRN
				(*msg).prn_long[0+j][0] = (double)bin2dec(tmp);

				extract((*msg).bin_msg,i+21,i+28,tmp);	//IODE
				(*msg).prn_long[0+j][1] = (double)bin2dec(tmp);

				extract((*msg).bin_msg,i+29,i+39,tmp);	//dx
				int_tmp = bin2dec(tmp);
				if(int_tmp > (TWO_POWER_10 - 1))	// signed conversion (11 bits)
					int_tmp -= TWO_POWER_11;
				(*msg).prn_long[0+j][2] = int_tmp*0.125;

				extract((*msg).bin_msg,i+40,i+50,tmp);	//dy
				int_tmp = bin2dec(tmp);
				if(int_tmp > (TWO_POWER_10 - 1))	// signed conversion (11 bits)
					int_tmp -= TWO_POWER_11;
				(*msg).prn_long[0+j][3] = int_tmp*0.125;

				extract((*msg).bin_msg,i+51,i+61,tmp);	//dz
				int_tmp = bin2dec(tmp);
				if(int_tmp > (TWO_POWER_10 - 1))	// signed conversion (11 bits)
					int_tmp -= TWO_POWER_11;
				(*msg).prn_long[0+j][4] = int_tmp*0.125;

				extract((*msg).bin_msg,i+62,i+72,tmp);	//daf0
				int_tmp = bin2dec(tmp);
				if(int_tmp > (TWO_POWER_10 - 1))	// signed conversion (11 bits)
					int_tmp -= TWO_POWER_11;
				(*msg).prn_long[0+j][5] = int_tmp*TWO_POWER_m31;

				extract((*msg).bin_msg,i+73,i+80,tmp);	//ddx
				int_tmp = bin2dec(tmp);
				if(int_tmp > (TWO_POWER_7 - 1))	// signed conversion (8 bits)
					int_tmp -= TWO_POWER_8;
				(*msg).prn_long[0+j][6] = int_tmp*TWO_POWER_m11;

				extract((*msg).bin_msg,i+81,i+88,tmp);	//ddy
				int_tmp = bin2dec(tmp);
				if(int_tmp > (TWO_POWER_7 - 1))	// signed conversion (8 bits)
					int_tmp -= TWO_POWER_8;
				(*msg).prn_long[0+j][7] = int_tmp*TWO_POWER_m11;

				extract((*msg).bin_msg,i+89,i+96,tmp);	//ddz
				int_tmp = bin2dec(tmp);
				if(int_tmp > (TWO_POWER_7 - 1))	// signed conversion (8 bits)
					int_tmp -= TWO_POWER_8;
				(*msg).prn_long[0+j][8] = int_tmp*TWO_POWER_m11;

				extract((*msg).bin_msg,i+97,i+104,tmp);	//daf1
				int_tmp = bin2dec(tmp);
				if(int_tmp > (TWO_POWER_7 - 1))	// signed conversion (8 bits)
					int_tmp -= TWO_POWER_8;
				(*msg).prn_long[0+j][9] = int_tmp*TWO_POWER_m39;

				extract((*msg).bin_msg,i+105,i+117,tmp);	//t0
				(*msg).prn_long[0+j][10] = bin2dec(tmp)*16;

				r = 1;
			}
		}

	}
	else
		r = 0;

	return r;
}

/**
 * decode_msg18 function.
 * The function decodes the message type 18 (Ionospheric Grid Point mask)
 * according to the DO-229D document (A.4.4.9). The function updates the
 * Egnos_msg structure, identified by its pointer, with the IGP mask table
 * (igp_blocks).
 * @param *msg    The pointer of the message
 * @return        Band id if successful or -1 if error is encountered
 */
int decode_msg18(Egnos_msg * msg)
{
	int i,j;
	int r = 0;
	char type[6];
	char tmp[800];
	char tmp2[300];

	// Message type check
	extract((*msg).bin_msg,8,13,type);
	if(bin2dec(type) == (*msg).m_type)
	{
		extract((*msg).bin_msg,14,17,tmp);
		(*msg).band_nb = bin2dec(tmp);
		if((*msg).band_nb == 0)
			r = 0;
		else
		{
			// Band ID
			extract((*msg).bin_msg,18,21,tmp);
			(*msg).band_id = bin2dec(tmp);

			// IODI
			extract((*msg).bin_msg,22,23,tmp);
			(*msg).iodi = bin2dec(tmp);

			// IGP mask
			extract((*msg).bin_msg,24,224,tmp2);
			int block;							// block number
			int block_inc;						// block number increment
			int start_lat,start_long;			// start values of lat. and long.
			switch((*msg).band_id)
			{
			// Band number 3 //////////////////////////////////////////////////////////////////////////////////////
			case 3:
				// Initial values
				start_long = -60;//60W -> -60E
				block = 0;
				block_inc = 0;
				j = 0;			// j is the line position in the igp_blocks table

				// -75N to 75N ////////////////////////////////////////////
				start_lat = -75;
				for(i=0;i<27;i++)
				{
					// increment block id
					if(block_inc == 15)
					{
						block++;
						block_inc = 0;
					}
					// bit == 1
					if(tmp2[i] == '1')
					{
						// save block id
						(*msg).igp_blocks[j][0] = block;
						// save the longitude value
						(*msg).igp_blocks[j][2] = start_long;			// 60W
						// save the latitude value
						(*msg).igp_blocks[j][1] = start_lat;

						// incrementation
						j++;
						block_inc++;
					}
					// Incrementation of latitude value
					if(start_lat<-55 || start_lat>=55)
						start_lat = start_lat + 10;
					else
						start_lat = start_lat + 5;
				}

				// -55N to 55N ////////////////////////////////////////////
				start_lat = -55;
				for(i=27;i<50;i++)
				{
					// Incrementation of longitude value
					if(i==27)
						start_long = start_long + 5;

					// increment block id
					if(block_inc == 15)
					{
						block++;
						block_inc = 0;
					}
					if(tmp2[i] == '1')
					{
						(*msg).igp_blocks[j][0] = block;
						(*msg).igp_blocks[j][2] = start_long;			// 55W
						(*msg).igp_blocks[j][1] = start_lat;
						j++;
						block_inc++;
					}
					// Incrementation of latitude value
					if(start_lat<-55 || start_lat>=55)
						start_lat = start_lat + 10;
					else
						start_lat = start_lat + 5;
				}

				// -85N to 75N ////////////////////////////////////////////
				start_lat = -85;
				for(i=50;i<78;i++)
				{
					if(i==50)
						start_long = start_long + 5;

					// increment block id
					if(block_inc == 15)
					{
						block++;
						block_inc = 0;
					}
					if(tmp2[i] == '1')
					{
						(*msg).igp_blocks[j][0] = block;
						(*msg).igp_blocks[j][2] = start_long;			// 50W
						(*msg).igp_blocks[j][1] = start_lat;
						j++;
						block_inc++;
					}
					// Incrementation of latitude value
					if(start_lat<-55 || start_lat>=55)
						start_lat = start_lat + 10;
					else
						start_lat = start_lat + 5;
				}

				// -55N to 55N ////////////////////////////////////////////
				start_lat = -55;
				for(i=78;i<101;i++)
				{
					if(i==78)
						start_long = start_long + 5;

					// increment block id
					if(block_inc == 15)
					{
						block++;
						block_inc = 0;
					}
					if(tmp2[i] == '1')
					{
						(*msg).igp_blocks[j][0] = block;
						(*msg).igp_blocks[j][2] = start_long;			// 45W
						(*msg).igp_blocks[j][1] = start_lat;
						j++;
						block_inc++;
					}
					// Incrementation of latitude value
					if(start_lat<-55 || start_lat>=55)
						start_lat = start_lat + 10;
					else
						start_lat = start_lat + 5;

				}

				// -75N to 75N ////////////////////////////////////////////
				start_lat = -75;
				for(i=101;i<128;i++)
				{
					if(i==101)
						start_long = start_long + 5;

					// increment block id
					if(block_inc == 15)
					{
						block++;
						block_inc = 0;
					}
					if(tmp2[i] == '1')
					{
						(*msg).igp_blocks[j][0] = block;
						(*msg).igp_blocks[j][2] = start_long;			// 40W
						(*msg).igp_blocks[j][1] = start_lat;
						j++;
						block_inc++;
					}
					// Incrementation of latitude value
					if(start_lat<-55 || start_lat>=55)
						start_lat = start_lat + 10;
					else
						start_lat = start_lat + 5;

				}
				// -55N to 55N ////////////////////////////////////////////
				start_lat = -55;
				for(i=128;i<151;i++)
				{
					if(i==128)
						start_long = start_long + 5;

					// increment block id
					if(block_inc == 15)
					{
						block++;
						block_inc = 0;
					}
					if(tmp2[i] == '1')
					{
						(*msg).igp_blocks[j][0] = block;
						(*msg).igp_blocks[j][2] = start_long;			// 35W
						(*msg).igp_blocks[j][1] = start_lat;
						j++;
						block_inc++;
					}
					// Incrementation of latitude value
					if(start_lat<-55 || start_lat>=55)
						start_lat = start_lat + 10;
					else
						start_lat = start_lat + 5;

				}
				// -75N to 75N ////////////////////////////////////////////
				start_lat = -75;
				for(i=151;i<178;i++)
				{
					if(i==151)
						start_long = start_long + 5;

					// increment block id
					if(block_inc == 15)
					{
						block++;
						block_inc = 0;
					}
					if(tmp2[i] == '1')
					{
						(*msg).igp_blocks[j][0] = block;
						(*msg).igp_blocks[j][2] = start_long;			// 30W
						(*msg).igp_blocks[j][1] = start_lat;
						j++;
						block_inc++;
					}
					// Incrementation of latitude value
					if(start_lat<-55 || start_lat>=55)
						start_lat = start_lat + 10;
					else
						start_lat = start_lat + 5;

				}
				// -55N to 55N ////////////////////////////////////////////
				start_lat = -55;
				for(i=178;i<201;i++)
				{
					if(i==178)
						start_long = start_long + 5;

					// increment block id
					if(block_inc == 15)
					{
						block++;
						block_inc = 0;
					}
					if(tmp2[i] == '1')
					{
						(*msg).igp_blocks[j][0] = block;
						(*msg).igp_blocks[j][2] = start_long;			// 25W
						(*msg).igp_blocks[j][1] = start_lat;
						j++;
						block_inc++;
					}
					// Incrementation of latitude value
					if(start_lat<-55 || start_lat>=55)
						start_lat = start_lat + 10;
					else
						start_lat = start_lat + 5;

				}
				(*msg).block_nb = block+1;

				break;
				// Band number 4 //////////////////////////////////////////////////////////////////////////////////////
			case 4:
				start_long = -20;//20W -> -20E
				block = 0;
				block_inc = 0;
				j = 0;

				// -75N to 75N ////////////////////////////////////////////
				start_lat = -75;
				for(i=0;i<27;i++)
				{
					// increment block id
					if(block_inc == 15)
					{
						block++;
						block_inc = 0;
					}
					if(tmp2[i] == '1')
					{
						(*msg).igp_blocks[j][0] = block;
						(*msg).igp_blocks[j][2] = start_long;			// 20W
						(*msg).igp_blocks[j][1] = start_lat;
						j++;
						block_inc++;
					}
					// Incrementation of latitude value
					if(start_lat<-55 || start_lat>=55)
						start_lat = start_lat + 10;
					else
						start_lat = start_lat + 5;

				}

				// -55N to 55N ////////////////////////////////////////////
				start_lat = -55;
				for(i=27;i<50;i++)
				{
					if(i==27)
						start_long = start_long + 5;

					// increment block id
					if(block_inc == 15)
					{
						block++;
						block_inc = 0;
					}
					if(tmp2[i] == '1')
					{
						(*msg).igp_blocks[j][0] = block;
						(*msg).igp_blocks[j][2] = start_long;			// 15W
						(*msg).igp_blocks[j][1] = start_lat;
						j++;
						block_inc++;
					}
					// Incrementation of latitude value
					if(start_lat<-55 || start_lat>=55)
						start_lat = start_lat + 10;
					else
						start_lat = start_lat + 5;

				}

				// -75N to 75N ////////////////////////////////////////////
				start_lat = -75;
				for(i=50;i<77;i++)
				{
					if(i==50)
						start_long = start_long + 5;

					// increment block id
					if(block_inc == 15)
					{
						block++;
						block_inc = 0;
					}
					if(tmp2[i] == '1')
					{
						(*msg).igp_blocks[j][0] = block;
						(*msg).igp_blocks[j][2] = start_long;			// 10W
						(*msg).igp_blocks[j][1] = start_lat;
						j++;
						block_inc++;
					}
					// Incrementation of latitude value
					if(start_lat<-55 || start_lat>=55)
						start_lat = start_lat + 10;
					else
						start_lat = start_lat + 5;

				}

				// -55N to 55N ////////////////////////////////////////////
				start_lat = -55;
				for(i=77;i<100;i++)
				{
					if(i==77)
						start_long = start_long + 5;

					// increment block id
					if(block_inc == 15)
					{
						block++;
						block_inc = 0;
					}
					if(tmp2[i] == '1')
					{
						(*msg).igp_blocks[j][0] = block;
						(*msg).igp_blocks[j][2] = start_long;			// 5W
						(*msg).igp_blocks[j][1] = start_lat;
						j++;
						block_inc++;
					}
					// Incrementation of latitude value
					if(start_lat<-55 || start_lat>=55)
						start_lat = start_lat + 10;
					else
						start_lat = start_lat + 5;

				}

				// -75N to 85N ////////////////////////////////////////////
				start_lat = -75;
				for(i=100;i<128;i++)
				{
					if(i==100)
						start_long = start_long + 5;

					// increment block id
					if(block_inc == 15)
					{
						block++;
						block_inc = 0;
					}
					if(tmp2[i] == '1')
					{
						(*msg).igp_blocks[j][0] = block;
						(*msg).igp_blocks[j][2] = start_long;			// 0W
						(*msg).igp_blocks[j][1] = start_lat;
						j++;
						block_inc++;
					}
					// Incrementation of latitude value
					if(start_lat<-55 || start_lat>=55)
						start_lat = start_lat + 10;
					else
						start_lat = start_lat + 5;

				}
				// -55N to 55N ////////////////////////////////////////////
				start_lat = -55;
				for(i=128;i<151;i++)
				{
					if(i==128)
						start_long = start_long + 5;

					// increment block id
					if(block_inc == 15)
					{
						block++;
						block_inc = 0;
					}
					if(tmp2[i] == '1')
					{
						(*msg).igp_blocks[j][0] = block;
						(*msg).igp_blocks[j][2] = start_long;			// 5E
						(*msg).igp_blocks[j][1] = start_lat;
						j++;
						block_inc++;
					}
					// Incrementation of latitude value
					if(start_lat<-55 || start_lat>=55)
						start_lat = start_lat + 10;
					else
						start_lat = start_lat + 5;

				}
				// -75N to 75N ////////////////////////////////////////////
				start_lat = -75;
				for(i=151;i<178;i++)
				{
					if(i==151)
						start_long = start_long + 5;

					// increment block id
					if(block_inc == 15)
					{
						block++;
						block_inc = 0;
					}
					if(tmp2[i] == '1')
					{
						(*msg).igp_blocks[j][0] = block;
						(*msg).igp_blocks[j][2] = start_long;			// 10E
						(*msg).igp_blocks[j][1] = start_lat;
						j++;
						block_inc++;
					}
					// Incrementation of latitude value
					if(start_lat<-55 || start_lat>=55)
						start_lat = start_lat + 10;
					else
						start_lat = start_lat + 5;

				}
				// -55N to 55N ////////////////////////////////////////////
				start_lat = -55;
				for(i=178;i<201;i++)
				{
					if(i==178)
						start_long = start_long + 5;

					// increment block id
					if(block_inc == 15)
					{
						block++;
						block_inc = 0;
					}
					if(tmp2[i] == '1')
					{
						(*msg).igp_blocks[j][0] = block;
						(*msg).igp_blocks[j][2] = start_long;			// 15E
						(*msg).igp_blocks[j][1] = start_lat;
						j++;
						block_inc++;
					}
					// Incrementation of latitude value
					if(start_lat<-55 || start_lat>=55)
						start_lat = start_lat + 10;
					else
						start_lat = start_lat + 5;

				}
				(*msg).block_nb = block+1;
				break;
				// Band number 5 //////////////////////////////////////////////////////////////////////////////////////
			case 5:
				start_long = 20;//20E
				block = 0;
				block_inc = 0;
				j = 0;

				// -75N to 75N ////////////////////////////////////////////
				start_lat = -75;
				for(i=0;i<27;i++)
				{
					// increment block id
					if(block_inc == 15)
					{
						block++;
						block_inc = 0;
					}
					if(tmp2[i] == '1')
					{
						(*msg).igp_blocks[j][0] = block;
						(*msg).igp_blocks[j][2] = start_long;			// 20E
						(*msg).igp_blocks[j][1] = start_lat;
						j++;
						block_inc++;
					}
					// Incrementation of latitude value
					if(start_lat<-55 || start_lat>=55)
						start_lat = start_lat + 10;
					else
						start_lat = start_lat + 5;

				}

				// -55N to 55N ////////////////////////////////////////////
				start_lat = -55;
				for(i=27;i<50;i++)
				{
					if(i==27)
						start_long = start_long + 5;

					// increment block id
					if(block_inc == 15)
					{
						block++;
						block_inc = 0;
					}
					if(tmp2[i] == '1')
					{
						(*msg).igp_blocks[j][0] = block;
						(*msg).igp_blocks[j][2] = start_long;			// 25E
						(*msg).igp_blocks[j][1] = start_lat;
						j++;
						block_inc++;
					}
					// Incrementation of latitude value
					if(start_lat<-55 || start_lat>=55)
						start_lat = start_lat + 10;
					else
						start_lat = start_lat + 5;

				}

				// -75N to 75N ////////////////////////////////////////////
				start_lat = -75;
				for(i=50;i<77;i++)
				{
					if(i==50)
						start_long = start_long + 5;

					// increment block id
					if(block_inc == 15)
					{
						block++;
						block_inc = 0;
					}
					if(tmp2[i] == '1')
					{
						(*msg).igp_blocks[j][0] = block;
						(*msg).igp_blocks[j][2] = start_long;			// 30E
						(*msg).igp_blocks[j][1] = start_lat;
						j++;
						block_inc++;
					}
					// Incrementation of latitude value
					if(start_lat<-55 || start_lat>=55)
						start_lat = start_lat + 10;
					else
						start_lat = start_lat + 5;

				}

				// -55N to 55N ////////////////////////////////////////////
				start_lat = -55;
				for(i=77;i<100;i++)
				{
					if(i==77)
						start_long = start_long + 5;

					// increment block id
					if(block_inc == 15)
					{
						block++;
						block_inc = 0;
					}
					if(tmp2[i] == '1')
					{
						(*msg).igp_blocks[j][0] = block;
						(*msg).igp_blocks[j][2] = start_long;			// 35E
						(*msg).igp_blocks[j][1] = start_lat;
						j++;
						block_inc++;
					}
					// Incrementation of latitude value
					if(start_lat<-55 || start_lat>=55)
						start_lat = start_lat + 10;
					else
						start_lat = start_lat + 5;

				}

				// -85N to 75N ////////////////////////////////////////////
				start_lat = -85;
				for(i=100;i<128;i++)
				{
					if(i==100)
						start_long = start_long + 5;

					// increment block id
					if(block_inc == 15)
					{
						block++;
						block_inc = 0;
					}
					if(tmp2[i] == '1')
					{
						(*msg).igp_blocks[j][0] = block;
						(*msg).igp_blocks[j][2] = start_long;			// 40E
						(*msg).igp_blocks[j][1] = start_lat;
						j++;
						block_inc++;
					}
					// Incrementation of latitude value
					if(start_lat<-55 || start_lat>=55)
						start_lat = start_lat + 10;
					else
						start_lat = start_lat + 5;

				}
				// -55N to 55N ////////////////////////////////////////////
				start_lat = -55;
				for(i=128;i<151;i++)
				{
					if(i==128)
						start_long = start_long + 5;

					// increment block id
					if(block_inc == 15)
					{
						block++;
						block_inc = 0;
					}
					if(tmp2[i] == '1')
					{
						(*msg).igp_blocks[j][0] = block;
						(*msg).igp_blocks[j][2] = start_long;			// 45E
						(*msg).igp_blocks[j][1] = start_lat;
						j++;
						block_inc++;
					}
					// Incrementation of latitude value
					if(start_lat<-55 || start_lat>=55)
						start_lat = start_lat + 10;
					else
						start_lat = start_lat + 5;

				}
				// -75N to 75N ////////////////////////////////////////////
				start_lat = -75;
				for(i=151;i<178;i++)
				{
					if(i==151)
						start_long = start_long + 5;

					// increment block id
					if(block_inc == 15)
					{
						block++;
						block_inc = 0;
					}
					if(tmp2[i] == '1')
					{
						(*msg).igp_blocks[j][0] = block;
						(*msg).igp_blocks[j][2] = start_long;			// 50E
						(*msg).igp_blocks[j][1] = start_lat;
						j++;
						block_inc++;
					}
					// Incrementation of latitude value
					if(start_lat<-55 || start_lat>=55)
						start_lat = start_lat + 10;
					else
						start_lat = start_lat + 5;

				}
				// -55N to 55N ////////////////////////////////////////////
				start_lat = -55;
				for(i=178;i<201;i++)
				{
					if(i==178)
						start_long = start_long + 5;

					// increment block id
					if(block_inc == 15)
					{
						block++;
						block_inc = 0;
					}
					if(tmp2[i] == '1')
					{
						(*msg).igp_blocks[j][0] = block;
						(*msg).igp_blocks[j][2] = start_long;			// 55E
						(*msg).igp_blocks[j][1] = start_lat;
						j++;
						block_inc++;
					}
					// Incrementation of latitude value
					if(start_lat<-55 || start_lat>=55)
						start_lat = start_lat + 10;
					else
						start_lat = start_lat + 5;

				}
				(*msg).block_nb = block+1;
				break;
				// Band number 6 //////////////////////////////////////////////////////////////////////////////////////
			case 6:
				start_long = 60;//60E
				block = 0;
				block_inc = 0;
				j = 0;

				// -75N to 75N ////////////////////////////////////////////
				start_lat = -75;
				for(i=0;i<27;i++)
				{
					// increment block id
					if(block_inc == 15)
					{
						block++;
						block_inc = 0;
					}
					if(tmp2[i] == '1')
					{
						(*msg).igp_blocks[j][0] = block;
						(*msg).igp_blocks[j][2] = start_long;			// 60E
						(*msg).igp_blocks[j][1] = start_lat;
						j++;
						block_inc++;
					}
					// Incrementation of latitude value
					if(start_lat<-55 || start_lat>=55)
						start_lat = start_lat + 10;
					else
						start_lat = start_lat + 5;

				}

				// -55N to 55N ////////////////////////////////////////////
				start_lat = -55;
				for(i=27;i<50;i++)
				{
					if(i==27)
						start_long = start_long + 5;

					// increment block id
					if(block_inc == 15)
					{
						block++;
						block_inc = 0;
					}
					if(tmp2[i] == '1')
					{
						(*msg).igp_blocks[j][0] = block;
						(*msg).igp_blocks[j][2] = start_long;			// 65E
						(*msg).igp_blocks[j][1] = start_lat;
						j++;
						block_inc++;
					}
					// Incrementation of latitude value
					if(start_lat<-55 || start_lat>=55)
						start_lat = start_lat + 10;
					else
						start_lat = start_lat + 5;

				}

				// -75N to 75N ////////////////////////////////////////////
				start_lat = -75;
				for(i=50;i<77;i++)
				{
					if(i==50)
						start_long = start_long + 5;

					// increment block id
					if(block_inc == 15)
					{
						block++;
						block_inc = 0;
					}
					if(tmp2[i] == '1')
					{
						(*msg).igp_blocks[j][0] = block;
						(*msg).igp_blocks[j][2] = start_long;			// 70E
						(*msg).igp_blocks[j][1] = start_lat;
						j++;
						block_inc++;
					}
					// Incrementation of latitude value
					if(start_lat<-55 || start_lat>=55)
						start_lat = start_lat + 10;
					else
						start_lat = start_lat + 5;

				}

				// -55N to 55N ////////////////////////////////////////////
				start_lat = -55;
				for(i=77;i<100;i++)
				{
					if(i==77)
						start_long = start_long + 5;

					// increment block id
					if(block_inc == 15)
					{
						block++;
						block_inc = 0;
					}
					if(tmp2[i] == '1')
					{
						(*msg).igp_blocks[j][0] = block;
						(*msg).igp_blocks[j][2] = start_long;			// 75E
						(*msg).igp_blocks[j][1] = start_lat;
						j++;
						block_inc++;
					}
					// Incrementation of latitude value
					if(start_lat<-55 || start_lat>=55)
						start_lat = start_lat + 10;
					else
						start_lat = start_lat + 5;

				}

				// -75N to 75N ////////////////////////////////////////////
				start_lat = -75;
				for(i=100;i<127;i++)
				{
					if(i==100)
						start_long = start_long + 5;

					// increment block id
					if(block_inc == 15)
					{
						block++;
						block_inc = 0;
					}
					if(tmp2[i] == '1')
					{
						(*msg).igp_blocks[j][0] = block;
						(*msg).igp_blocks[j][2] = start_long;			// 80E
						(*msg).igp_blocks[j][1] = start_lat;
						j++;
						block_inc++;
					}
					// Incrementation of latitude value
					if(start_lat<-55 || start_lat>=55)
						start_lat = start_lat + 10;
					else
						start_lat = start_lat + 5;

				}
				// -55N to 55N ////////////////////////////////////////////
				start_lat = -55;
				for(i=127;i<150;i++)
				{
					if(i==127)
						start_long = start_long + 5;

					// increment block id
					if(block_inc == 15)
					{
						block++;
						block_inc = 0;
					}
					if(tmp2[i] == '1')
					{
						(*msg).igp_blocks[j][0] = block;
						(*msg).igp_blocks[j][2] = start_long;			// 85E
						(*msg).igp_blocks[j][1] = start_lat;
						j++;
						block_inc++;
					}
					// Incrementation of latitude value
					if(start_lat<-55 || start_lat>=55)
						start_lat = start_lat + 10;
					else
						start_lat = start_lat + 5;

				}
				// -75N to 85N ////////////////////////////////////////////
				start_lat = -75;
				for(i=150;i<178;i++)
				{
					if(i==150)
						start_long = start_long + 5;

					// increment block id
					if(block_inc == 15)
					{
						block++;
						block_inc = 0;
					}
					if(tmp2[i] == '1')
					{
						(*msg).igp_blocks[j][0] = block;
						(*msg).igp_blocks[j][2] = start_long;			// 90E
						(*msg).igp_blocks[j][1] = start_lat;
						j++;
						block_inc++;
					}
					// Incrementation of latitude value
					if(start_lat<-55 || start_lat>=55)
						start_lat = start_lat + 10;
					else
						start_lat = start_lat + 5;

				}
				// -55N to 55N ////////////////////////////////////////////
				start_lat = -55;
				for(i=178;i<201;i++)
				{
					if(i==178)
						start_long = start_long + 5;

					// increment block id
					if(block_inc == 15)
					{
						block++;
						block_inc = 0;
					}
					if(tmp2[i] == '1')
					{
						(*msg).igp_blocks[j][0] = block;
						(*msg).igp_blocks[j][2] = start_long;			// 95E
						(*msg).igp_blocks[j][1] = start_lat;
						j++;
						block_inc++;
					}
					// Incrementation of latitude value
					if(start_lat<-55 || start_lat>=55)
						start_lat = start_lat + 10;
					else
						start_lat = start_lat + 5;

				}
				(*msg).block_nb = block+1;
				break;
				// Band number 9 //////////////////////////////////////////////////////////////////////////////////////
			case 9:
				start_lat = 60;//60N
				block = 0;
				block_inc = 0;
				j = 0;
				// -180E to 175E ////////////////////////////////////////////
				start_long = -180;
				for(i=0;i<72;i++)
				{
					// increment block id
					if(block_inc == 15)
					{
						block++;
						block_inc = 0;
					}

					if(tmp2[i] == '1')
					{
						(*msg).igp_blocks[j][0] = block;
						(*msg).igp_blocks[j][1] = start_lat;			// 60N
						(*msg).igp_blocks[j][2] = start_long;
						j++;
						block_inc++;
					}
					start_long = start_long + 5;						// Increment is 5 for lat = 60N
				}
				// -180E to 170E ////////////////////////////////////////////
				start_long = -180;
				for(i=72;i<108;i++)
				{
					if(i==72)
						start_lat = start_lat + 5;

					// increment block id
					if(block_inc == 15)
					{
						block++;
						block_inc = 0;
					}
					if(tmp2[i] == '1')
					{
						(*msg).igp_blocks[j][0] = block;
						(*msg).igp_blocks[j][1] = start_lat;			// 65N
						(*msg).igp_blocks[j][2] = start_long;
						j++;
						block_inc++;
					}
					start_long = start_long + 10;						// Increment is 10 for lat>=65N
				}
				// -180E to 170E ////////////////////////////////////////////
				start_long = -180;
				for(i=108;i<144;i++)
				{
					if(i==108)
						start_lat = start_lat + 5;

					// increment block id
					if(block_inc == 15)
					{
						block++;
						block_inc = 0;
					}
					if(tmp2[i] == '1')
					{
						(*msg).igp_blocks[j][0] = block;
						(*msg).igp_blocks[j][1] = start_lat;			// 70N
						(*msg).igp_blocks[j][2] = start_long;
						j++;
						block_inc++;
					}
					start_long = start_long + 10;						// Increment is 10 for lat>=65N
				}
				// -180E to 170E ////////////////////////////////////////////
				start_long = -180;
				for(i=144;i<180;i++)
				{
					if(i==144)
						start_lat = start_lat + 5;

					// increment block id
					if(block_inc == 15)
					{
						block++;
						block_inc = 0;
					}
					if(tmp2[i] == '1')
					{
						(*msg).igp_blocks[j][0] = block;
						(*msg).igp_blocks[j][1] = start_lat;			// 75N
						(*msg).igp_blocks[j][2] = start_long;
						j++;
						block_inc++;
					}
					start_long = start_long + 10;						// Increment is 10 for lat>=65N
				}
				// -180E to 150E ////////////////////////////////////////////
				start_long = -180;
				for(i=180;i<192;i++)
				{
					if(i==180)
						start_lat = start_lat + 10;

					// increment block id
					if(block_inc == 15)
					{
						block++;
						block_inc = 0;
					}
					if(tmp2[i] == '1')
					{
						(*msg).igp_blocks[j][0] = block;
						(*msg).igp_blocks[j][1] = start_lat;			// 85N
						(*msg).igp_blocks[j][2] = start_long;
						j++;
						block_inc++;
					}
					start_long = start_long + 30;						// Increment is 10 for lat>=65N
				}
				(*msg).block_nb = block+1;
				break;
			}
			r = 1;

		}
	}
	if( r != 0 )
		return (*msg).band_id;
	else
		return -1;
}

/**
 * decode_msg26 function.
 * The function decodes the message type 26 (Ionospheric delay corrections)
 * according to the DO-229D document (A.4.4.10). The function updates the
 * Egnos_msg structure, identified by its pointer, with the ionospheric
 * corrections table (grid_point).
 * @param *msg    The pointer of the message
 * @return        1 if successfully parsed, 0
 */
int decode_msg26(Egnos_msg * msg)
{
	int i;
	int r = 0;
	char type[6];
	char tmp[10];
	//printf("BIN: %s\n",(*msg).bin_msg);

	// Message type check
	extract((*msg).bin_msg,8,13,type);
	if(bin2dec(type) == (*msg).m_type)
	{
		// Band ID
		extract((*msg).bin_msg,14,17,tmp);
		(*msg).band_id = bin2dec(tmp);

		// Block ID
		extract((*msg).bin_msg,18,21,tmp);
		(*msg).block_id = bin2dec(tmp);

		int pos = 22;
		for(i=0;i<15;i++)
		{
			extract((*msg).bin_msg,pos,pos+8,tmp);		// IGPVD
			(*msg).grid_point[i][1] = bin2dec(tmp)*0.125;
			pos = pos + 9;

			extract((*msg).bin_msg,pos,pos+3,tmp);		// GIVEI
			(*msg).grid_point[i][2] = bin2dec(tmp);
			pos = pos + 4;

			if((*msg).grid_point[i][1] == 63.875)		// check dont use
				(*msg).grid_point[i][0] = -1;
			else
			{
				if((*msg).grid_point[i][2] == 15)		// check not monitored
					(*msg).grid_point[i][0] = 0;
				else
					(*msg).grid_point[i][0] = 1;
			}
		}
		// IODI
		extract((*msg).bin_msg,217,218,tmp);
		(*msg).iodi = bin2dec(tmp);

		r = 1;

	}

	return r;
}

/**
 * decode_msg7 function.
 * The function decodes the message type 7 (Fast correction degradation factors)
 *  according to the DO-229D document (A.4.4.5). The function updates the
 * Egnos_msg structure, identified by its pointer, with the table of fast
 * corrections degradation factors (ai).
 * @param   *msg  The pointer of the message
 * @return      1 if successfully parsed, 0
 */
int decode_msg7(Egnos_msg * msg)
{
	int i,j;
	int r = 0;
	char type[6];
	char char_tmp[4];
	//printf("BIN: %s\n",(*msg).bin_msg);

	// Message type check
	extract((*msg).bin_msg,8,13,type);
	if(bin2dec(type) == (*msg).m_type)
	{
		extract((*msg).bin_msg,14,17,char_tmp);
		(*msg).tlat = bin2dec(char_tmp);

		/*extract((*msg).bin_msg,18,19,char_tmp);
		(*msg).iodp = bin2dec(char_tmp);*/
		j = 0;
		for(i=0; i<51; i++)
		{
			extract((*msg).bin_msg,22+j,25+j,char_tmp);
			get_degradationFactor((int)bin2dec(char_tmp),(*msg).ai[i],i);
			j += 4;
		}
		r = 1;
	}
	else
		r = 0;

	return r;
}

/**
 * decode_msg10 function.
 * The function decodes the message type 10 (Degradation factors) according to
 * the DO-229D document (A.4.4.6). The function updates the Egnos_msg
 * structure, identified by its pointer, with the degradation factors
 * parameters.
 * @param *msg  The pointer of the message
 * @return    1 if successfully parsed, 0
 */
int decode_msg10(Egnos_msg * msg)
{
	int r = 0;
	char type[6];
	char char_tmp[10];
	//printf("BIN: %s\n",(*msg).bin_msg);

	// Message type check
	extract((*msg).bin_msg,8,13,type);
	if(bin2dec(type) == (*msg).m_type)
	{
		extract((*msg).bin_msg,14,23,char_tmp);
		(*msg).brrc = bin2dec(char_tmp)*0.002;

		extract((*msg).bin_msg,24,33,char_tmp);
		(*msg).cltc_lsb = bin2dec(char_tmp)*0.002;

		extract((*msg).bin_msg,34,43,char_tmp);
		(*msg).cltc_v1 = bin2dec(char_tmp)*0.00005;

		extract((*msg).bin_msg,44,52,char_tmp);
		(*msg).iltc_v1 = bin2dec(char_tmp);

		extract((*msg).bin_msg,53,62,char_tmp);
		(*msg).cltc_v0 = bin2dec(char_tmp)*0.002;

		extract((*msg).bin_msg,63,71,char_tmp);
		(*msg).iltc_v0 = bin2dec(char_tmp);
		if((*msg).iltc_v0 == 0)
			(*msg).iltc_v0 = 1;

		extract((*msg).bin_msg,72,81,char_tmp);
		(*msg).cgeo_lsb= bin2dec(char_tmp)*0.0005;

		extract((*msg).bin_msg,82,91,char_tmp);
		(*msg).cgeo_v= bin2dec(char_tmp)*0.00005;

		extract((*msg).bin_msg,92,100,char_tmp);
		(*msg).igeo= bin2dec(char_tmp);

		extract((*msg).bin_msg,101,106,char_tmp);
		(*msg).cer = bin2dec(char_tmp)*0.5;

		extract((*msg).bin_msg,107,116,char_tmp);
		(*msg).ciono_step = bin2dec(char_tmp)*0.001;

		extract((*msg).bin_msg,117,125,char_tmp);
		(*msg).iiono = bin2dec(char_tmp);
		if((*msg).iiono == 0)
			(*msg).iiono = 1;

		extract((*msg).bin_msg,126,135,char_tmp);
		(*msg).ciono_ramp = bin2dec(char_tmp)*0.000005;

		extract((*msg).bin_msg,136,136,char_tmp);
		(*msg).rss_udre = bin2dec(char_tmp);

		extract((*msg).bin_msg,137,137,char_tmp);
		(*msg).rss_iono = bin2dec(char_tmp);

		r = 1;
	}
	else
		r = 0;

	return r;
}

/**
 * decode_msg12 function.
 * The function decodes the message type 12 (SBAS network time) according to
 * the DO-229D document (A.4.4.15). The function updates the Egnos_msg
 * structure, identified by its pointer, with the SBAS network time parameters.
 * @param *msg  The pointer of the message
 * @return    1 if successfully parsed, 0
 */
int decode_msg12(Egnos_msg * msg)
{
	int r = 0;
	char type[6];
	char char_tmp[33];
#ifdef Linux_H_
	long long int_tmp;
#else
	__int64 int_tmp;
#endif

	// Message type check
	extract((*msg).bin_msg,8,13,type);
	if(bin2dec(type) == (*msg).m_type)
	{
		// A1snt
		extract((*msg).bin_msg,14,37,char_tmp);
		int_tmp = bin2dec(char_tmp);
		if(int_tmp > (TWO_POWER_23 - 1))	// signed conversion (24 bits)
			int_tmp -= TWO_POWER_24;
		(*msg).a1snt = int_tmp*TWO_POWER_m50;

		// A0snt
		extract((*msg).bin_msg,38,69,char_tmp);
		int_tmp = bin2dec(char_tmp);
		if(int_tmp > (TWO_POWER_31 - 1))	// signed conversion (32 bits)
			int_tmp -= TWO_POWER_32;
		(*msg).a0snt = int_tmp*TWO_POWER_m30;

		// t0t
		extract((*msg).bin_msg,70,77,char_tmp);
		int_tmp = bin2dec(char_tmp);
		(*msg).t0t = int_tmp*TWO_POWER_12;

		// WNt
		extract((*msg).bin_msg,78,85,char_tmp);
		(*msg).wnt = bin2dec(char_tmp);

		// Dtls
		extract((*msg).bin_msg,86,93,char_tmp);
		int_tmp = bin2dec(char_tmp);
		if(int_tmp > (TWO_POWER_7 - 1))	// signed conversion (8 bits)
			int_tmp -= TWO_POWER_8;
		(*msg).dtls = int_tmp;

		// WNlsf
		extract((*msg).bin_msg,94,101,char_tmp);
		(*msg).wnlsf = bin2dec(char_tmp);

		// DN
		extract((*msg).bin_msg,102,109,char_tmp);
		(*msg).dn = bin2dec(char_tmp);

		// Dtlsf
		extract((*msg).bin_msg,110,117,char_tmp);
		int_tmp = bin2dec(char_tmp);
		if(int_tmp > (TWO_POWER_7 - 1))	// signed conversion (8 bits)
			int_tmp -= TWO_POWER_8;
		(*msg).dtlsf = int_tmp;

		// UTC ID
		extract((*msg).bin_msg,118,120,char_tmp);
		(*msg).utc_id = bin2dec(char_tmp);

		// GPS TOW
		extract((*msg).bin_msg,121,140,char_tmp);
		(*msg).gps_tow = bin2dec(char_tmp);

		// GPS Week nb.
		extract((*msg).bin_msg,141,150,char_tmp);
		(*msg).gps_wknb = bin2dec(char_tmp);

		r = 1;
	}
	else
		r = 0;

	return r;
}

/**
 * decode_msg9 function.
 * The function decodes the message type 9 (GEO navigation message) according
 * to the DO-229D document (A.4.4.11). The function updates the Egnos_msg
 * structure, identified by its pointer, with the GEO navigation table (geo_nav).
 * @param *msg  The pointer of the message
 * @return    1 if successfully parsed, 0
 */
int decode_msg9(Egnos_msg * msg)
{
	int r = 0;
	char type[6];
	char char_tmp[32];
#ifdef Linux_H_
	long long int_tmp;
#else
	__int64 int_tmp;
#endif

	// Message type check
	strncpy(type, (*msg).bin_msg + 8, 6);
	type[6] = '\0';
	if(bin2dec(type) == (*msg).m_type)
	{
		// t0
		strncpy(char_tmp, (*msg).bin_msg + 22, 13);
		char_tmp[13] = '\0';
		(*msg).geo_nav[0] = bin2dec(char_tmp)*16;

		// URA
		strncpy(char_tmp, (*msg).bin_msg + 35, 4);
		char_tmp[4] = '\0';
		(*msg).geo_nav[1] = bin2dec(char_tmp);

		// Xg
		strncpy(char_tmp, (*msg).bin_msg + 39, 30);
		char_tmp[30] = '\0';
		int_tmp = bin2dec(char_tmp);
		if(int_tmp > (TWO_POWER_29 - 1))	// signed conversion (30 bits)
			int_tmp -= TWO_POWER_30;
		(*msg).geo_nav[2] = int_tmp*0.08;

		// Yg
		strncpy(char_tmp, (*msg).bin_msg + 69, 30);
		char_tmp[30] = '\0';
		int_tmp = bin2dec(char_tmp);
		if(int_tmp > (TWO_POWER_29 - 1))	// signed conversion (30 bits)
			int_tmp -= TWO_POWER_30;
		(*msg).geo_nav[3] = int_tmp*0.08;

		// Zg
		strncpy(char_tmp, (*msg).bin_msg + 99, 25);
		char_tmp[25] = '\0';
		int_tmp = bin2dec(char_tmp);
		if(int_tmp > (TWO_POWER_24 - 1))	// signed conversion (25 bits)
			int_tmp -= TWO_POWER_25;
		(*msg).geo_nav[4] = int_tmp*0.4;

		// dXg
		strncpy(char_tmp, (*msg).bin_msg + 124, 17);
		char_tmp[17] = '\0';
		int_tmp = bin2dec(char_tmp);
		if(int_tmp > (TWO_POWER_16 - 1))	// signed conversion (17 bits)
			int_tmp -= TWO_POWER_17;
		(*msg).geo_nav[5] = int_tmp*0.000625;

		// dYg
		strncpy(char_tmp, (*msg).bin_msg + 141, 17);
		char_tmp[17] = '\0';
		int_tmp = bin2dec(char_tmp);
		if(int_tmp > (TWO_POWER_16 - 1))	// signed conversion (17 bits)
			int_tmp -= TWO_POWER_17;
		(*msg).geo_nav[6] = int_tmp*0.000625;

		// dZg
		strncpy(char_tmp, (*msg).bin_msg + 158, 18);
		char_tmp[18] = '\0';
		int_tmp = bin2dec(char_tmp);
		if(int_tmp > (TWO_POWER_17 - 1))	// signed conversion (18 bits)
			int_tmp -= TWO_POWER_18;
		(*msg).geo_nav[7] = int_tmp*0.004;

		// ddXg
		strncpy(char_tmp, (*msg).bin_msg + 176, 10);
		char_tmp[10] = '\0';
		int_tmp = bin2dec(char_tmp);
		if(int_tmp > (TWO_POWER_9 - 1))	// signed conversion (10 bits)
			int_tmp -= TWO_POWER_10;
		(*msg).geo_nav[8] = int_tmp*0.0000125;

		// ddYg
		strncpy(char_tmp, (*msg).bin_msg + 186, 10);
		char_tmp[10] = '\0';
		int_tmp = bin2dec(char_tmp);
		if(int_tmp > (TWO_POWER_9 - 1))	// signed conversion (10 bits)
			int_tmp -= TWO_POWER_10;
		(*msg).geo_nav[9] = int_tmp*0.0000125;

		// ddZg
		strncpy(char_tmp, (*msg).bin_msg + 196, 10);
		char_tmp[10] = '\0';
		int_tmp = bin2dec(char_tmp);
		if(int_tmp > (TWO_POWER_9 - 1))	// signed conversion (10 bits)
			int_tmp -= TWO_POWER_10;
		(*msg).geo_nav[10] = int_tmp*0.0000625;

		// afg0
		strncpy(char_tmp, (*msg).bin_msg + 206, 12);
		char_tmp[12] = '\0';
		int_tmp = bin2dec(char_tmp);
		if(int_tmp > (TWO_POWER_11 - 1))	// signed conversion (12 bits)
			int_tmp -= TWO_POWER_12;
		(*msg).geo_nav[11] = int_tmp*TWO_POWER_m31;

		// afg1
		strncpy(char_tmp, (*msg).bin_msg + 218, 8);
		char_tmp[8] = '\0';
		int_tmp = bin2dec(char_tmp);
		if(int_tmp > (TWO_POWER_7 - 1))	// signed conversion (8 bits)
			int_tmp -= TWO_POWER_8;
		(*msg).geo_nav[12] = int_tmp*TWO_POWER_m40;

		r = 1;
	}
	else
		r = 0;

	return r;
}

/**
 * decode_msg17 function.
 * The function decodes the message type 17 (GEO almanacs) according to the
 * DO-229D document (A.4.4.12). The function updates the Egnos_msg structure,
 * identified by its pointer,  with the GEO almanacs table (geo_alm).
 * @param *msg  The pointer of the message
 * @return    1 if successfully parsed, 0
 */
int decode_msg17(Egnos_msg * msg)
{
	int r = 0;
	int i,shift;
	char type[7];
	char char_tmp[16];
#ifdef Linux_H_
	long long int_tmp;
#else
	__int64 int_tmp;
#endif

	// Message type check
	strncpy(type, (*msg).bin_msg + 8, 6);
	type[6] = '\0';

	if(bin2dec(type) == (*msg).m_type)
	{
		shift = 0;

		for(i = 0; i < 3; i++)
		{
			// Data ID
			strncpy(char_tmp, (*msg).bin_msg + 14 + shift, 2);
			char_tmp[2] = '\0';
			(*msg).geo_alm[i][0] = bin2dec(char_tmp);

			// PRN
			strncpy(char_tmp, (*msg).bin_msg + 16 + shift, 8);
			char_tmp[8] = '\0';
			(*msg).geo_alm[i][1] = bin2dec(char_tmp);

			// Ranging flag
			strncpy(char_tmp, (*msg).bin_msg + 24 + shift, 1);
			char_tmp[1] = '\0';
			(*msg).geo_alm[i][2] = bin2dec(char_tmp);

			// Corrections flag
			strncpy(char_tmp, (*msg).bin_msg + 25 + shift, 1);
			char_tmp[1] = '\0';
			(*msg).geo_alm[i][3] = bin2dec(char_tmp);

			// Integrity flag
			strncpy(char_tmp, (*msg).bin_msg + 26 + shift, 1);
			char_tmp[1] = '\0';
			(*msg).geo_alm[i][4] = bin2dec(char_tmp);

			// Service Provider ID
			strncpy(char_tmp, (*msg).bin_msg + 28 + shift, 4);
			char_tmp[4] = '\0';
			(*msg).geo_alm[i][5] = bin2dec(char_tmp);

			// Xg
			strncpy(char_tmp, (*msg).bin_msg + 32 + shift, 15);
			char_tmp[15] = '\0';
			int_tmp = bin2dec(char_tmp);
			if(int_tmp > (TWO_POWER_14 - 1))	// signed conversion (15 bits)
				int_tmp -= TWO_POWER_15;
			(*msg).geo_alm[i][6] = int_tmp*2600;

			// Yg
			strncpy(char_tmp, (*msg).bin_msg + 47 + shift, 15);
			char_tmp[15] = '\0';
			int_tmp = bin2dec(char_tmp);
			if(int_tmp > (TWO_POWER_14 - 1))	// signed conversion (15 bits)
				int_tmp -= TWO_POWER_15;
			(*msg).geo_alm[i][7] = int_tmp*2600;

			// Zg
			strncpy(char_tmp, (*msg).bin_msg + 62 + shift, 9);
			char_tmp[9] = '\0';
			int_tmp = bin2dec(char_tmp);
			if(int_tmp > (TWO_POWER_8 - 1))	// signed conversion (9 bits)
				int_tmp -= TWO_POWER_9;
			(*msg).geo_alm[i][8] = int_tmp*26000;

			// dXg
			strncpy(char_tmp, (*msg).bin_msg + 71 + shift, 3);
			char_tmp[3] = '\0';
			int_tmp = bin2dec(char_tmp);
			if(int_tmp > (4 - 1))			// signed conversion (3 bits)
				int_tmp -= 8;
			(*msg).geo_alm[i][9] = int_tmp*10;

			// dYg
			strncpy(char_tmp, (*msg).bin_msg + 74 + shift, 3);
			char_tmp[3] = '\0';
			int_tmp = bin2dec(char_tmp);
			if(int_tmp > (4 - 1))			// signed conversion (3 bits)
				int_tmp -= 8;
			(*msg).geo_alm[i][10] = int_tmp*10;

			// dZg
			strncpy(char_tmp, (*msg).bin_msg + 77 + shift, 4);
			char_tmp[4] = '\0';
			int_tmp = bin2dec(char_tmp);
			if(int_tmp > (8 - 1))			// signed conversion (4 bits)
				int_tmp -= 16;
			(*msg).geo_alm[i][11] = int_tmp*60;

			// t0
			strncpy(char_tmp, (*msg).bin_msg + 215, 11);
			char_tmp[11] = '\0';
			(*msg).geo_alm[i][12] = bin2dec(char_tmp)*64;

			shift += 67;
		}
		r = 1;
	}
	else
		r = 0;

	return r;
}

/**
 * decode_msg6 function.
 * The function decodes the message type 6 (Integrity information) according
 * to the DO-229D document (A.4.4.4). The function updates the Egnos_msg
 * structure, identified by its pointer,  with the UDRE indicator
 * and IODF tables for message 6 (udre_msg6 and iodf_msg6).
 * @param *msg  The pointer of the message
 * @return    1 if successfully parsed, 0
 */
int decode_msg6(Egnos_msg * msg)
{
	int r = 0;
	int i,shift;
	char type[7];
	char char_tmp[5];
	//printf("BIN: %s\n",(*msg).bin_msg);

	// Message type check
	extract((*msg).bin_msg,8,13,type);
	if(bin2dec(type) == (*msg).m_type)
	{
		// IODF2
		strncpy(char_tmp, (*msg).bin_msg + 14, 2 );
		char_tmp[2] = '\0';
		(*msg).iodf_msg6[0] = bin2dec(char_tmp);

		// IODF3
		strncpy(char_tmp, (*msg).bin_msg + 16, 2 );
		char_tmp[2] = '\0';
		(*msg).iodf_msg6[1] = bin2dec(char_tmp);

		// IODF4
		strncpy(char_tmp, (*msg).bin_msg + 18, 2 );
		char_tmp[2] = '\0';
		(*msg).iodf_msg6[2] = bin2dec(char_tmp);

		// IODF5
		strncpy(char_tmp, (*msg).bin_msg + 20, 2 );
		char_tmp[2] = '\0';
		(*msg).iodf_msg6[3] = bin2dec(char_tmp);

		shift = 0;

		for(i=0;i<37;i++)		// 37:max size of gps prns
		{
			// UDREi
			strncpy(char_tmp, (*msg).bin_msg + 22 + shift, 4 );
			char_tmp[4] = '\0';
			(*msg).udre_msg6[i] = bin2dec(char_tmp);

			shift += 4;
		}
		r = 1;
	}
	else
		r = 0;

	return r;
}

/**
 * get_degradationFactor function.
 * The function returns the degradation factor parameters table  from the
 * degradation factor indicator of the message type 7 - DO-229D table A.4.5.1
 * @param aii       The degradation factor indicator
 * @param factors   The degradation factors destination table
 * @param i         The position in the table (PRN = i+1)
 */
void get_degradationFactor(int aii, double factors[5], int i)
{
	switch(aii)
	{
	case 0:
		factors[0] = i+1;
		factors[1] = 0;
		factors[2] = 180;
		factors[3] = 120;
		factors[4] = 60;
		break;
	case 1:
		factors[0] = i+1;
		factors[1] = 0.00005;
		factors[2] = 180;
		factors[3] = 120;
		factors[4] = 60;
		break;
	case 2:
		factors[0] = i+1;
		factors[1] = 0.00009;
		factors[2] = 153;
		factors[3] = 102;
		factors[4] = 51;
		break;
	case 3:
		factors[0] = i+1;
		factors[1] = 0.00012;
		factors[2] = 135;
		factors[3] = 90;
		factors[4] = 45;
		break;
	case 4:
		factors[0] = i+1;
		factors[1] = 0.00015;
		factors[2] = 135;
		factors[3] = 90;
		factors[4] = 45;
		break;
	case 5:
		factors[0] = i+1;
		factors[1] = 0.00020;
		factors[2] = 117;
		factors[3] = 78;
		factors[4] = 39;
		break;
	case 6:
		factors[0] = i+1;
		factors[1] = 0.00030;
		factors[2] = 99;
		factors[3] = 66;
		factors[4] = 33;
		break;
	case 7:
		factors[0] = i+1;
		factors[1] = 0.00045;
		factors[2] = 81;
		factors[3] = 54;
		factors[4] = 27;
		break;
	case 8:
		factors[0] = i+1;
		factors[1] = 0.00060;
		factors[2] = 63;
		factors[3] = 42;
		factors[4] = 21;
		break;
	case 9:
		factors[0] = i+1;
		factors[1] = 0.00090;
		factors[2] = 45;
		factors[3] = 30;
		factors[4] = 15;
		break;
	case 10:
		factors[0] = i+1;
		factors[1] = 0.00150;
		factors[2] = 45;
		factors[3] = 30;
		factors[4] = 15;
		break;
	case 11:
		factors[0] = i+1;
		factors[1] = 0.00210;
		factors[2] = 27;
		factors[3] = 18;
		factors[4] = 9;
		break;
	case 12:
		factors[0] = i+1;
		factors[1] = 0.00270;
		factors[2] = 27;
		factors[3] = 18;
		factors[4] = 9;
		break;
	case 13:
		factors[0] = i+1;
		factors[1] = 0.00330;
		factors[2] = 27;
		factors[3] = 18;
		factors[4] = 9;
		break;
	case 14:
		factors[0] = i+1;
		factors[1] = 0.00460;
		factors[2] = 18;
		factors[3] = 12;
		factors[4] = 6;
		break;
	case 15:
		factors[0] = i+1;
		factors[1] = 0.00580;
		factors[2] = 18;
		factors[3] = 12;
		factors[4] = 6;
		break;
	}
}

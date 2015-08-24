/**
 * @file Egnos.h
 *
 * @brief Egnos module header file defining the Egnos_msg structure and
 * the EGNOS messages parsing functions.
 * @details The module creates an Egnos_msg structure. The structure defines
 * an EGNOS message  and contains all parameters related to an EGNOS message.
 * It includes  parameters common to all message types, the type, the payload,
 * and the time of week.
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
 */

#ifndef EGNOS_H_
#define EGNOS_H_

#define Linux_H_

#include "Utils.h"
#include "Constants.h"

/**
 * @typedef Egnos_msg Egnos_msg
 */
typedef struct Egnos_msg Egnos_msg;

/**
 * @struct Egnos_msg Egnos.h "Egnos.h"
 * @brief The structure defines an EGNOS message and contains all related parameters.
 * @details The Egnos structure includes parameters common to all message types,
 * the type, the payload, the time of week. The other parameters are type specific.
 */
struct Egnos_msg
{
  unsigned short m_type;         /*!<  Message type */
  short iodp;                    /*!< Issue Of Data - PRN */
  short iodi;                    /*!< Issue Of Data - Ionosphere */
  short iodf;                    /*!< Issue Of Data - Fast correction */
  int wknb;                      /*!< Week number */
  double tow;                    /*!< Time Of Week                                      (s) */
  double prn[51];                /*!< PRN mask : Table of 51 PRNs */
  short prn_nb;                  /*!< The number of PRNs */
  char * sisnet_parity;          /*!< 8 parity bits from SISNeT */
  char * egnos_parity;           /*!< 24 parity bits from SISNeT */
  char * hex_msg;                /*!< */
  char * bin_msg;                /*!< 250 bits EGNOS message */

  short band_nb;                 /*!< Number of IGP bands */
  short band_id;                 /*!< IGP band number */
  short block_id;                /*!< Block ID */
  double grid_point[15][3];      /*!< The 15 grid points with status(1:ok, 0:not monitored, -1:dont use), IGPVD and GIVEI values (usable for a given block id) */
  short igp_blocks[210][3];      /*!< The 210 lines (14 blocks max * 15 grid points) of block ID,Lat,Long */
  short block_nb;                /*!< The number of blocks */

  double a1snt;                  /*!< Polynomial term                                   (s/s) */
  double a0snt;                  /*!< Polynomial term                                   (s) */
  double t0t;                    /*!< Reference time for UTC data                       (s) */
  double dtls;                   /*!< Delta time due to leap seconds                    (s) */
  double gps_tow;                /*!< GPS Time of Week                                  (s) */
  double dtlsf;                  /*!< */
  int wnt;                       /*!< UTC reference week number                         (week) */
  int wnlsf;                     /*!< */
  int dn;                        /*!< Day number                                        (day) */
  int utc_id;                    /*!< UTC standard identifier */
  int gps_wknb;                  /*!< GPS week number */

  short iltc_v1;                 /*!< Update interval for velocity code = 1             (s) */
  short iltc_v0;                 /*!< Update interval for velocity code = 0             (s) */
  short iiono;                   /*!< */
  short rss_udre;                /*!< Root Sum Square flag UDRE */
  short rss_iono;                /*!< Root Sum Square flag IONO */
  short igeo;                    /*!<                                                   (s) */
  double brrc;                   /*!< Relative estimation noise/round off err parameter (m) */
  double cltc_lsb;               /*!< Max round-off error                               (m) */
  double cltc_v1;                /*!< Cltc for velocity code = 1                        (m/s) */
  double cltc_v0;                /*!< Cltc for velocity code = 0                        (m) */
  double cgeo_lsb;               /*!<                                                   (m) */
  double cgeo_v;                 /*!<                                                   (m/s) */
  double cer;                    /*!< Degradation  parameter                            (m) */
  double ciono_step;             /*!<                                                   (m) */
  double ciono_ramp;             /*!< */
  short velocity;                /*!< Velocity code */
  double prc[13];                /*!< Table of 13 PRC */
  double udre[13];               /*!< Table of 13 UDRE */
  double prn_long[4][11];        /*!< PRN,IODE,dx,dy,dz,daf0,ddx,ddy,ddz,daf1,t0 for max. 4 PRNs */

  double ai[51][5];              /*!< Fast corrections degradation factor table PRN,ai(m/s^2),Ifc1,Ifc2,Max update */
  double tlat;                   /*!< System latency                                    (s) */

  double geo_nav[13];            /*!< GEO navigation parameters table t0,ura,xg,yg,zg,dxg,dyg,dzg,ddxg,ddyg,ddzg,afg0,afg1 */
  double geo_alm[3][13];         /*!< GEO almanacs table for 3 satellites Data_ID,PRN,Health_status,xg,yg,zg,dxg,dyg,dzg,t0 */

  unsigned short udre_msg6[51];  /*!< UDRE for 51 satellites */
  unsigned short iodf_msg6[4];   /*!< Table of IODFi (IODF_msg6[0]=IODF2,IODF_msg6[1]=IODF3,IODF_msg6[2]=IODF4,IODF_msg6[3]=IODF5)  */
  int use;						 /*!< When set to 1 indicates the message has been set not only initialized						    */
  int ranging;					 /*!< Set to 1 when SBAS ranging is on																*/
};

void init_msg(Egnos_msg * msg, int type);
int decode_msg1(Egnos_msg * msg);
int decode_msg2_5(Egnos_msg * msg);
int decode_msg7(Egnos_msg * msg);
int decode_msg10(Egnos_msg * msg);
int decode_msg12(Egnos_msg * msg);
int decode_msg18(Egnos_msg * msg);
int decode_msg24(Egnos_msg * msg, int fast);
int decode_msg25(Egnos_msg * msg);
int decode_msg26(Egnos_msg * msg);
int decode_msg9(Egnos_msg * msg);
int decode_msg17(Egnos_msg * msg);
int decode_msg6(Egnos_msg * msg);
void get_degradationFactor(int aii, double factors[5], int i);

#endif /* EGNOS_H_ */

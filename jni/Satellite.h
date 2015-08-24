/**
 * @file Satellite.h
 *
 * @brief Satellite module header file defining the Satellite structure.
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

#ifndef SATELLITE_C_
#define SATELLITE_C_

#include <stdio.h>
#include <math.h>
#include "Constants.h"

/**
 * @typedef Satellite Satellite
 */
typedef struct Satellite Satellite;

/**
 * @struct Satellite Satellite.h "Satellite.h"
 * @brief The structure defines a satellite vehicle and contains all related parameters.
 * @details The Satellite structure includes parameters common to all satellite vehicles, time of week, measurments,
 * ephemerides, clock corrections, position, elevation, azimuth, EGNOS fast, long term, ionospheric and troposheric corrections terms.
 */
struct Satellite
{
  double prn;             /*!< PRN number of the satellite */
  char *subfr1;           /*!< 300 bits of ephemeris subframe 1 */
  char *subfr2;           /*!< 300 bits of ephemeris subframe 2 */
  char *subfr3;           /*!< 300 bits of ephemeris subframe 3 */
  double pr;              /*!< Pseudorange                                                                  (m) */
  double pr_c;            /*!< Corrected pseudorange                                                        (m) */
  double cn0;             /*!< C/N0                                                                         (dBHz)*/
  double tow;             /*!< Time of week                                                                 (s) */
  double tow2;            /*!< Corrected Time of week                                                       (s) */
  double tow3;            /*!< Time of week                                                                 (s) */
  int toe;                /*!< Reference time ephemeris                                                     (s) */
  double toc;             /*!< Reference time clock                                                         (s) */
  unsigned short ado;     /*!< Age of data offset                                                           (s) */
  unsigned short weeknb;  /*!< Transmission week number                                                     (week) */
  unsigned short cl2;     /*!< Code on L2 */
  unsigned short ura;     /*!< SV range accuracy                                                            (m) */
  unsigned short health;  /*!< SV Health */
  unsigned short iodc;    /*!< Issue of Data, Clock */
  unsigned short iode_s1; /*!< Issue of Data, Ephemeris */
  unsigned short iode_s2; /*!< Issue of Data, Ephemeris -on subframe 2 */
  unsigned short iode_s3; /*!< Issue of Data, Ephemeris -on subframe 3 */
  unsigned short dfl2p;   /*!< Data Flag for L2 P-Code */
  double tgd;             /*!< Estimated Group Delay Differential                                           (s) */
  double af0;             /*!< Polynomial clock correction coefficient af0                                  (s) */
  double af1;             /*!< Polynomial clock correction coefficient af1                                  (s/s) */
  double af2;             /*!< Polynomial clock correction coefficient af2                                  (s/s^2) */
  double crs;             /*!< Amplitude of the Sine Harmonic Correction Term to the Orbit Radius           (m) */
  double cuc;             /*!< Amplitude of the Cosine Harmonic Correction Term to the Argument of Latitude (rad) */
  double cus;             /*!< Amplitude of the Sine Harmonic Correction Term to the Argument of Latitude   (rad) */
  double crc;             /*!< Amplitude of the Cosine Harmonic Correction Term to the Orbit Radius         (m) */
  double cic;             /*!< Amplitude of the Cosine Harmonic Correction Term to the Angle of Inclination (rad) */
  double cis;             /*!< Amplitude of the Sine Harmonic Correction Term to the Angle of Inclination   (rad) */
  double idot;            /*!< Rate of Inclination Angle                                                    (rad/s) */
  double omegadot;        /*!< Rate of Right Ascension                                                      (rad/s) */
  double w;               /*!< Argument of Perigee                                                          (rad) */
  double i0;              /*!< Inclination Angle at Reference Time                                          (rad) */
  double omega0;          /*!< Longitude of Ascending Node of Orbit Plane at Weekly Epoch                   (rad) */
  double sqrta;           /*!< Square Root of the Semi-Major Axis                                           (m^(1/2)) */
  double e;               /*!< Eccentricity */
  double delta_n;         /*!< Mean Motion Difference From Computed Value                                   (rad/s) */
  double m0;              /*!< Mean Anomaly at Reference Time                                               (rad) */
  double pos_x;           /*!< X position of the SV in ECEF coordinates                                     (m) */
  double pos_y;           /*!< Y position of the SV in ECEF coordinates                                     (m) */
  double pos_z;           /*!< Z position of the SV in ECEF coordinates                                     (m) */
  double v_x;             /*!< X velocity of the SV                                                         (m/s) */
  double v_y;             /*!< Y velocity of the SV                                                         (m/s) */
  double v_z;             /*!< Z velocity of the SV                                                         (m/s) */
  double t_correction;    /*!< satellite clock bias                                                         (s) */
  double iono_delay;      /*!< Ionospheric delay computed based on EGNOS correction messages                (m) */
  double iono_model;      /*!< Ionospheric delay computed based on the Klobuchar Model                      (m) */
  double tropo_delay;     /*!< Tropospheric delay                                                           (m) */
  double fast_delay;      /*!< Fast correction delay                                                        (m) */
  double az;              /*!< Azimuth                                                                      (deg) */
  double el;              /*!< Elevation                                                                    (deg) */
  double dx;              /*!< Long term correction                                                         (m) */
  double dy;              /*!< Long term correction                                                         (m) */
  double dz;              /*!< Long term correction                                                         (m) */
  double ddx;             /*!< Long term correction                                                         (m/s) */
  double ddy;             /*!< Long term correction                                                         (m/s) */
  double ddz;             /*!< Long term correction                                                         (m/s) */
  double daf0;            /*!< Long term correction                                                         (s)   */
  double daf1;            /*!< Long term correction                                                         (s/s) */
  double t0;              /*!< Long term correction                                                         (s)   */
  double sigma_flt2;      /*!< Degradation of Fast and Long-term correction                                 (m^2) */
  double sigma_uire2;     /*!< Degradation of Ionospheric delay                                             (m^2) */
  double sigma_tropo2;    /*!< Degradation of Tropospheric correction                                       (m^2) */
  double sigma2;    	  /*!< Degradation of EGNOS correction                                       (m^2) */
  int use;                /*!< Set to 1 when satellite is to be used in computations                              */
  int udrei;              /*!< Fast correction accuracy indicator                                                 */
  double rrc;             /*!< Range rate correction value added to the fast correction                           */
  int fast_set;           /*!< set to 1 when valid fast corrections are available                           */
  int long_set;           /*!< set to 1 when valid slow corrections are available                           */
  int type_sat;
  double eps_fc;
  double eps_rrc;
  double eps_ltc;
  double eps_er;
  int prn_mask;
  int low_elv;
  int rnd;

  //INS - start
  double roota;
  double sat_pos_x;
  double sat_pos_y;
  double sat_pos_z;
  double rel_corr;
  double tCorr;
  //INS - end


};
void init_satellite(Satellite * Sat);
void cconv_to_ENU(double ENU[3], double sat[3], double X_est[3], double geod[3]);
double get_elevation(double ENU[3]);
double get_azimuth(double ENU[3]);
int get_satellite_type(double prn);
int is_GPS(double prn);

#endif /* SATELLITE_C_ */

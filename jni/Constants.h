/**
 * @file Constants.h
 *
 * @brief Constants module header file defining the constant parameters
 * used in the software.
 * @details The module defines the GPS constants from the GPS user Interface
 * specification IS-GPS-200E, the WGS84 constants for coordinate system
 * conversion from the WGS84 system definition document
 * (http://earth-info.nga.mil/GandG/publications/tr8350.2/tr8350_2.html), the
 * SBAS constants from the MOPS document DO-229D.
 * It also contains the values of the powers of two used in the software
 * and other specific constants.
 *
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

#ifndef CONSTANTS_H_
#define CONSTANTS_H_

// IS-GPS-200E Constants

#define SPEED_OF_LIGHT  2.99792458E8     /*!< Value of speed of light                             (m/s) */
#define MU_EARTH        3.986005E14      /*!< Value of Earth's universal gravitational parameters (m^3/s^2) */
#define PI              3.1415926535897931  /*!< Value of PI number */
#define OMEGA_DOT_EARTH 7.2921151467E-5  /*!< Value of the earth's rotation rate                  (rad/s) */
#define GPSWEEK_IN_SEC  604800           /*!< Value of the GPS time is one week                   (s) */
#define F_CONST         -4.442807633E-10 /*!< Value of -2.sqrt(u)/(c^2)                           (s/m^0.5) */

// WGS84 : http://earth-info.nga.mil/GandG/publications/tr8350.2/tr8350_2.html

#define a_WGS84         6378137.0000       /*!< Value of the semi-major axis (a) for WGS 84    (m) */
#define b_WGS84         6356752.3142       /*!< Value of the semi-minor axis (b) for WGS 84    (m) */
#define e_WGS84         8.1819190842622E-2 /*!< Value of the first eccentricity (e) for WGS 84 (m) */
#define e_WGS84_SQUARED 6.69437999014132E-3  //!< Value of e^2                                   (m^2) */

// DO-229D Constants

#define EARTH_RADIUS 6378136.3 /*!< Value of the Earth radius                       (m) */
#define HI           350000    /*!< Value of the height of the max electron density (m) */
#define K1           77.604    /*!<                                                 (K/mbar) */
#define K2           382000    /*!<                                                 (K^2/mbar) */
#define Rd           287.054   /*!<                                                 (J/(kg.K)) */
#define Gm           9.784     /*!<                                                 (m/s^2) */
#define G            9.80665   /*!<                                                 (m/s^2) */

// Power of two values

#define TWO_POWER_m5 0.03125              /*!< Value of 2^-5 */
#define TWO_POWER_m11 0.00048828125       /*!< Value of 2^-11 */
#define TWO_POWER_m19 1.9073486328125E-6  /*!< Value of 2^-19 */
#define TWO_POWER_m29 1.862645149230957E-9  /*!< Value of 2^-29 */
#define TWO_POWER_m30 9.3132257461547852E-10 /*!< Value of 2^-30 */
#define TWO_POWER_m31 4.6566128730773926E-10 /*!< Value of 2^-31 */
#define TWO_POWER_m33 1.1641532182693481E-10 /*!< Value of 2^-33 */
#define TWO_POWER_m39 1.8189894035458565E-12 /*!< Value of 2^-39 */
#define TWO_POWER_m40 9.0949470177292824E-13 /*!< Value of 2^-40 */
#define TWO_POWER_m43 1.1368683772161603E-13 /*!< Value of 2^-43 */
#define TWO_POWER_m50 8.8817841970012523E-16 /*!< Value of 2^-50 */
#define TWO_POWER_m55 2.7755575615628914E-17 /*!< Value of 2^-55 */

#define TWO_POWER_7 128         /*!< Value of 2^7 */
#define TWO_POWER_8 256         /*!< Value of 2^8 */
#define TWO_POWER_9 512         /*!< Value of 2^9 */
#define TWO_POWER_10 1024       /*!< Value of 2^10 */
#define TWO_POWER_11 2048       /*!< Value of 2^11 */
#define TWO_POWER_12 4096       /*!< Value of 2^12 */
#define TWO_POWER_13 8192       /*!< Value of 2^13 */
#define TWO_POWER_14 16384      /*!< Value of 2^14 */
#define TWO_POWER_15 32768      /*!< Value of 2^15 */
#define TWO_POWER_16 65536      /*!< Value of 2^16 */
#define TWO_POWER_17 131072     /*!< Value of 2^17 */
#define TWO_POWER_18 262144     /*!< Value of 2^18 */
#define TWO_POWER_21 2097152    /*!< Value of 2^21 */
#define TWO_POWER_22 4194304    /*!< Value of 2^22 */
#define TWO_POWER_23 8388608    /*!< Value of 2^23 */
#define TWO_POWER_24 16777216   /*!< Value of 2^24 */
#define TWO_POWER_25 33554432   /*!< Value of 2^25 */
#define TWO_POWER_29 536870912  /*!< Value of 2^29 */
#define TWO_POWER_30 1073741824 /*!< Value of 2^30 */
#define TWO_POWER_31 2147483648LL				/*!< Value of 2^31 */
#define TWO_POWER_32 4294967296LL				/*!< Value of 2^32 */

// Selection of satellites

#define SAT_SELECTION 8								// Number of satellites to be selected

#endif /* CONSTANTS_H_ */

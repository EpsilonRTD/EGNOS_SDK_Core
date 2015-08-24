/**
 * @file Ephemeris.h
 *
 * @brief Ephemeris module header file defining the parsing functions of the
 * GPS navigation data subframes number 1,2 and 3.
 * @details The module decodes the ephemerides and the clock corrections
 * parameters from the subframes 1,2 and 3 of the broadcasted GPS navigation
 * data for a given satellite according to the IS-GPS-200E section 20.3.2
 * Message Structure.
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

#ifndef EPHEMERIS_H_
#define EPHEMERIS_H_
#define Linux_H_

#include "Utils.h"
#include "Satellite.h"

void decode_msg(Satellite * Sat);
void ReadSubfr1(Satellite * Sat,char * data);
void ReadSubfr2(Satellite * Sat,char * data);
void ReadSubfr3(Satellite * Sat,char * data);
int get_toe(char * data);
int get_tow(char * data);
double get_toc(char * data);
unsigned short get_prn(char * data);
unsigned short get_weeknb(char * data);
unsigned short get_ado(char * data);
unsigned short get_cl2(char * data);
unsigned short get_ura(char * data);
unsigned short get_health(char * data);
unsigned short get_iodc(char * data);
unsigned short get_iode_s2(char * data);
unsigned short get_iode_s3(char * data);
double get_tgd(char * data);
double get_af0(char * data);
double get_af1(char * data);
double get_af2(char * data);
double get_crs(char * data);
double get_cuc(char * data);
double get_cus(char * data);
double get_crc(char * data);
double get_cic(char * data);
double get_cis(char * data);
double get_idot(char * data);
double get_omegadot(char * data);
double get_w(char * data);
double get_i0(char * data);
double get_omega0(char * data);
double get_sqrta(char * data);
double get_e(char * data);
double get_delta_n(char * data);
double get_m0(char * data);

#endif /* EPHEMERIS_H_ */

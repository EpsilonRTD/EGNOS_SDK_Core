/**
 * @file Ionosphere.h
 *
 * @brief Ionosphere module header file defining the ionospheric corrections
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

#ifndef IONOSPHERE_H_
#define IONOSPHERE_H_

#include "Satellite.h"
#include "Egnos.h"

void IPPlocation(double ipp[2], double user_lat, double user_long,double E, double A);
void SelectCells(double igps[4][6], double ipp[2], int inc_lat, int inc_long, int par_lat, int par_lon);
void IPPInterpolation4(double results[2], double igps[4][6],double ipp[2], double t, Egnos_msg * msg10, int flag);
void IPPInterpolation3(double results[2], double igps[4][6],double ipp[2], double t, Egnos_msg * msg10, int flag);
int set_ionoGridSis(Egnos_msg m18_t[11], Egnos_msg m26_t[25], char m18_char[5][263], char m26_char[25][263]);
int get_BandSelect(double user_long);
int IGPsSelect(double igps[4][6], double ipp[2], Egnos_msg m18_t[11], Egnos_msg m26_t[25]);
int check_Triangle(double igps[4][6], double ipp[2], int inc_lat, int inc_long);
void get_ionoCorrection(Satellite * Sat, double user_lat, double user_long, Egnos_msg m18_t[11], 
Egnos_msg m26_t[25],Egnos_msg * msg10, int flag);
double get_GIVEaccuracy(int givei);
double get_fpp(double el);
double get_sigmaIono2(double sigma_give2, double t, double t_iono, Egnos_msg * msg10, int flag);
int get_defined_IGPs(double igps[4][6], Egnos_msg m18_t[11], int block_info[4][3], int lat_spacing, int lon_spacing, int par_lat, int par_lon, double ipp[2]);
int get_monitored_IGPs(Egnos_msg m26_t[25], int block_info[4][3], double igps[4][6]);
int calculateIGPs(double ipp[2], double igps[4][6], int block_info[4][3], Egnos_msg m18_t[11], Egnos_msg m26_t[25], int lat_spacing, int lon_spacing);
void IPPInterpolation4_above85(double results[2], double igps[4][6],double ipp[2], double t, 
Egnos_msg * msg10, int flag);
int calculateIGPs_above75(double ipp[2], double igps[4][6], int block_info[4][3], Egnos_msg m18_t[11], 
Egnos_msg m26_t[25]);
int get_IGPs(int band_id, int block_info[4][3], double igps[4][6], Egnos_msg m18_t[11]);

#endif /* IONOSPHERE_H_ */

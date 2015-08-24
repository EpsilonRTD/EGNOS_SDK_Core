/**
 * @file Fast_correction.h
 *
 * @brief Fast correction module header file defining the fast corrections
 * and model variances computation functions.
 * @details The module decodes Fast corrections messages from SIS.
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

#ifndef FAST_CORRECTION_H_
#define FAST_CORRECTION_H_

#include "Satellite.h"
#include "Egnos.h"

int set_fastCorrectionsSis(Egnos_msg msg2_5[4][2], char msg2_5_char[8][263]);
int set_fastCorrections(Egnos_msg msg2_5[4][2]);
int get_fastCorrection(Satellite * Sat, Egnos_msg * msg1, Egnos_msg msg2_5[4][2],
		Egnos_msg msg24_t[15], Egnos_msg * msg6, Egnos_msg * msg10, Egnos_msg * msg7,
		double eps_ltc_m, int rnd_options[8]);
double get_UDREaccuracy(int udrei);
double get_epsilonFc(double a, double t, double tu, double tlat);
double get_epsilonRrc(double a, double t, double ifc, double brrc, unsigned short iodf0, unsigned short iodf1, double t0, double t1);

#endif /* FAST_CORRECTION_H_ */

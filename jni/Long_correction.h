/**
 * @file Long_correction.h
 *
 * @brief Long_correction module header file defining the long term
 * corrections parameters and degradation computation functions.
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

#include "Satellite.h"
#include "Egnos.h"

int set_LongCorrections_MT24(Egnos_msg msg_t[25],char msg24_char[25][263]);
int set_LongCorrections_MT25(Egnos_msg msg_t[15],char msg24_char[15][263]);
double set_LongCorrection(Satellite *Sat, Egnos_msg msg24_t[25],  Egnos_msg msg25_t[15], Egnos_msg *msg10, Egnos_msg *msg1);
double max_(double values[3]);


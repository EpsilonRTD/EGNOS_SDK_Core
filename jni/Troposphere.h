/**
 * @file Troposphere.h
 *
 * @brief Troposphere module header file defining the tropospheric corrections
 * and model variances computation functions.
 * @details This module defines the SBAS troposphere model. It performs the
 * computation of the tropospheric corrections and model variances according
 * to the DO-229D section A.4.2.4.
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

#ifndef TROPOSPHERE_H_
#define TROPOSPHERE_H_

#include <math.h>
#include <time.h>
#include "Constants.h"
#include "Satellite.h"
#include "Egnos.h"

double get_tropoCorrection(Satellite * Sat, double latitude, double height);
double interpolate(double latitude, double latitude_a, double latitude_b, double value_a, double value_b);

#endif /* TROPOSPHERE_H_ */

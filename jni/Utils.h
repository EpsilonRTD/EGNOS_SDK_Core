/**
 * @file Utils.h
 *
 * @brief Utils module header file defining useful math/string functions.
 * @details The module is a library of utilities functions such as numeral
 * systems conversions, characters extraction...
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

#ifndef UTILS_H_
#define UTILS_H_
#define Linux_H_

#include <math.h>
#include <string.h>
#include <stdlib.h>

void dec2bin(double decimal, char *binary, int size);
char* hex2bin4(char hexade);
#ifdef Linux_H_
   long long bin2dec(char *binary);
#else
  __int64 bin2dec(char *binary);
#endif
void extract (const char *c, int begin, int end, char *result);
int is_nan(double value);
double mod(double dividend, double divisor);
#endif /* UTILS_H_ */

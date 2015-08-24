/**
 * @file CoordinatesMain.c
 *
 * @brief CoordinatesMain module header file defining the GPS/EGNOS position
 * computation functions.
 * @details The module integrates EGNOS Demo App and EGNOS-SW Receiver modules
 * i.e. integrate java and c using android ndk tool. This module is the
 * entrance to core of the software, it contains the two functions that
 * request the GPS and EGNOS positioning processes.
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
#ifndef COORDINATESMAIN_H_
#define COORDINATESMAIN_H_

#include <android/log.h>
#define APPNAME "Coordinates"
#include "Positioning.h"
#include "Fast_correction.h"
#include "Long_correction.h"
#include "Utils.h"
#include "Egnos.h"
#include <string.h>
#include <jni.h>

JNIEXPORT jdoubleArray Java_com_ec_egnossdk_uBlox_getLongitudeLatitudeEGNOS
(JNIEnv * env,jobject obj,jobjectArray ephemDataArray,jobjectArray sat_dataArray,jstring jmsg1,
		jstring jmsg10,jstring jmsg12,jstring jmsg7,jstring jmsg6,jobjectArray jm18_t,
		jobjectArray jm26_t,jobjectArray jmsg2_5,jobjectArray jmsg24_t,jobjectArray jmsg25_t,
		jstring jmsg9,jstring jmsg17, jdoubleArray jinit_pos, jdoubleArray jutc_array,
		jdoubleArray jklob_array, jintArray RnDoptions,jdoubleArray sat_data_notUsedArray);
JNIEXPORT jdoubleArray Java_com_ec_egnossdk_uBlox_getLongitudeLatitudeGPS
(JNIEnv * env,jobject obj,jobjectArray ephemDataArray,jobjectArray sat_dataArray,
		jdoubleArray jinit_pos, jdoubleArray jutc_array,jdoubleArray sat_data_notUsedArray);

#endif /* COORDINATESMAIN_H_ */

LOCAL_PATH := $(call my-dir)
 
include $(CLEAR_VARS)
 
# Here we give our module name and source file(s)
LOCAL_MODULE    := EGNOSSWReceiver
LOCAL_SRC_FILES := CoordinatesMain.c Egnos.c Ephemeris.c Fast_correction.c Ionosphere.c Long_correction.c Matrix.c Positioning.c Satellite.c   Troposphere.c Utils.c   
LOCAL_C_INCLUDES := CoordinatesMain.h Constants.h Egnos.h Ephemeris.h Fast_correction.h Ionosphere.h Long-correction.h Matrix.h Positioning.h Satellite.h Troposphere.h Utils.h 
 
LOCAL_LDLIBS := -llog 

include $(BUILD_SHARED_LIBRARY)
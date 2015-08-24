EGNOS SDK Core v3.0.0 Android 5 Compatible
==========================================

This version of EGNOS SDK is based upon the official GSA one, as found in: http://egnos-portal.gsa.europa.eu/developer-platform/egnos-toolkits/egnos-sdk/download-egnos-sdk .It has been re-factored into an Eclipse ADT-NDK project, providing the following:

Android 5 (platform 21) support
-------------------------------
- Various JNI C code segmentation faults, thrown when running the original version under Android 5, were detected and rooted out.
- NULL String checks were added to the uBlox Java module, before calling any JNI methods, to prevent errors thrown by GetStringUTFChars under Android 5.
- Android & NDK Library settings have been updated, to target android-21.

SISNet Login support
---------------------
- Added configurable SISNet username & password support (thus removing the hardwired values set in the original version). Accessible via GlobalState's setSISNET_LOGIB and setSISNET_PASSWD methods.

Various fixes
-------------
- Included stdarg.h in various C files, to avoid Eclipse compiler complaints.
- Removed redundant (since it belongs to the EGNOS SDK Demo App project) com.ec.egnosdemoapp.EGNOSCorrectionInputOutput import statement from various Java classes.
- Removed redundant java.awt.Toolkit import statement from TSAGeoMag.java.
- Added passing the application Context to GlobalState, upon creation.

  
package com.ec.sdp4model;

/*
Copyright: (c) 2002-2010 Horst Meyerdierks.

This programme is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public Licence as
published by the Free Software Foundation; either version 2 of
the Licence, or (at your option) any later version.

This programme is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public Licence for more details.

You should have received a copy of the GNU General Public Licence
along with this programme; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.

Author: Horst Meyerdierks
        http://www.chiandh.me.uk

- The method Satellite.SAT_REFLECTION was ported from Pascal code
  from Randy John's SKYSAT programme (Randy John, 2002, SKYSAT v0.64,
  http://home.attbi.com/~skysat).

- The class SDP4 was ported from Fortran code published by Hoots et al.
  (Felix R. Hoots, Roland L. Roehrich, T.S. Kelso, 1980, 1988,
  Spacetrack report no. 3, Models for propagation of NORAD element sets,
  http://www.celestrak.com/NORAD/documentation/).
 java.text.DecimalFormat;
  */

import com.ec.egnossdk.SatelliteData;

public class SDP4Model
{
  
  protected static double itsLong; // Geographic longitude in rad.
  protected static double itsLat; // Geographic latitude in rad.
  protected static double itsHeight; // Height above sea level in Gm.
  protected static double itsX; // Distance from Earth's axis in Gm.
  protected static double itsZ; // Distance from Earth's equatorial plane in Gm.
  protected static double[] itsR = new double[3];
  protected static double[] itsV = new double[3];
  
  protected static final double A = 0.006378140; // Earth's equatorial radius in
  // Gm.
  protected static final double B = 0.006356775; // Earth's polar radius in Gm.
  protected static final double F = 298.257; // Earth's flattening.
  public static double JD;

	/** The TLE epoch expressed in JD minus 2450000 days. */
	public double itsEpochJD;

	protected double E1_XMO,E1_XNODEO,E1_OMEGAO,E1_EO,E1_XINCL,
	E1_XNO,E1_XNDT2O,E1_XNDD6O,E1_BSTAR,E1_X,E1_Y,E1_Z,
	E1_XDOT,E1_YDOT,E1_ZDOT,E1_EPOCH,E1_DS50;

	protected double C1_CK2,C1_CK4,C1_E6A,C1_QOMS2T,C1_S,C1_TOTHRD,
	C1_XJ3,C1_XKE,C1_XKMPER,C1_XMNPDA,C1_AE;

	protected double C2_DE2RA,C2_PI,C2_PIO2,C2_TWOPI,C2_X3PIO2;

	protected double SDP4_A1,SDP4_A3OVK2,SDP4_AO,SDP4_AODP,
	SDP4_AYCOF,SDP4_BETAO,SDP4_BETAO2,SDP4_C1,SDP4_C2,
	SDP4_C4,SDP4_COEF,SDP4_COEF1,SDP4_COSG,SDP4_COSIO,
	SDP4_DEL1,SDP4_DELO,SDP4_EETA,SDP4_EOSQ,
	SDP4_ETA,SDP4_ETASQ,SDP4_OMGDOT,SDP4_PERIGE,SDP4_PINVSQ,
	SDP4_PSISQ,SDP4_QOMS24,SDP4_S4,SDP4_SING,
	SDP4_SINIO,SDP4_T2COF,SDP4_TEMP1,SDP4_TEMP2,SDP4_TEMP3,
	SDP4_THETA2,SDP4_THETA4,SDP4_TSI,SDP4_X1M5TH,
	SDP4_X1MTH2,SDP4_X3THM1,SDP4_X7THM1,SDP4_XHDOT1,SDP4_XLCOF,
	SDP4_XMDOT,SDP4_XNODCF,SDP4_XNODOT,SDP4_XNODP;

	protected double DEEP_A1,DEEP_A2,DEEP_A3,DEEP_A4,DEEP_A5,DEEP_A6,
	DEEP_A7,DEEP_A8,DEEP_A9,DEEP_A10,DEEP_AINV2,DEEP_ALFDP,
	DEEP_AQNV,DEEP_ATIME,DEEP_BETDP,DEEP_BFACT,DEEP_C,DEEP_CC,
	DEEP_COSIS,DEEP_COSOK,DEEP_COSQ,DEEP_CTEM,DEEP_D2201,
	DEEP_D2211,DEEP_D3210,DEEP_D3222,DEEP_D4410,DEEP_D4422,
	DEEP_D5220,DEEP_D5232,DEEP_D5421,DEEP_D5433,DEEP_DALF,
	DEEP_DAY,DEEP_DBET,DEEP_DEL1,DEEP_DEL2,DEEP_DEL3,DEEP_DELT,
	DEEP_DLS,DEEP_E3,DEEP_EE2,DEEP_EOC,DEEP_EQ,DEEP_F2,
	DEEP_F220,DEEP_F221,DEEP_F3,DEEP_F311,DEEP_F321,DEEP_F322,
	DEEP_F330,DEEP_F441,DEEP_F442,DEEP_F522,DEEP_F523,
	DEEP_F542,DEEP_F543,DEEP_FASX2,DEEP_FASX4,DEEP_FASX6,
	DEEP_FT,DEEP_G200,DEEP_G201,DEEP_G211,DEEP_G300,DEEP_G310,
	DEEP_G322,DEEP_G410,DEEP_G422,DEEP_G520,DEEP_G521,DEEP_G532,
	DEEP_G533,DEEP_GAM,DEEP_OMEGAQ,DEEP_PE,DEEP_PGH,DEEP_PH,
	DEEP_PINC,DEEP_PL,DEEP_PREEP,DEEP_S1,DEEP_S2,
	DEEP_S3,DEEP_S4,DEEP_S5,DEEP_S6,DEEP_S7,DEEP_SAVTSN,DEEP_SE,
	DEEP_SE2,DEEP_SE3,DEEP_SEL,DEEP_SES,DEEP_SGH,DEEP_SGH2,
	DEEP_SGH3,DEEP_SGH4,DEEP_SGHL,DEEP_SGHS,DEEP_SH,DEEP_SH2,
	DEEP_SH3,DEEP_SH1,DEEP_SHS,DEEP_SI,DEEP_SI2,DEEP_SI3,
	DEEP_SIL,DEEP_SINI2,DEEP_SINIS,DEEP_SINOK,DEEP_SINQ,
	DEEP_SINZF,DEEP_SIS,DEEP_SL,DEEP_SL2,DEEP_SL3,DEEP_SL4,
	DEEP_SLL,DEEP_SLS,DEEP_SSE,DEEP_SSG,DEEP_SSH,DEEP_SSI,
	DEEP_SSL,DEEP_STEM,DEEP_STEP2,DEEP_STEPN,DEEP_STEPP,
	DEEP_TEMP,DEEP_TEMP1,DEEP_THGR,DEEP_X1,DEEP_X2,DEEP_X2LI,
	DEEP_X2OMI,DEEP_X3,DEEP_X4,DEEP_X5,DEEP_X6,DEEP_X7,DEEP_X8,
	DEEP_XFACT,DEEP_XGH2,DEEP_XGH3,DEEP_XGH4,DEEP_XH2,DEEP_XH3,
	DEEP_XI2,DEEP_XI3,DEEP_XL,DEEP_XL2,DEEP_XL3,DEEP_XL4,
	DEEP_XLAMO,DEEP_XLDOT,DEEP_XLI,DEEP_XLS,
	DEEP_XMAO,DEEP_XNDDT,DEEP_XNDOT,DEEP_XNI,DEEP_XNO2,
	DEEP_XNODCE,DEEP_XNOI,DEEP_XNQ,DEEP_XOMI,DEEP_XPIDOT,
	DEEP_XQNCL,DEEP_Z1,DEEP_Z11,DEEP_Z12,DEEP_Z13,DEEP_Z2,
	DEEP_Z21,DEEP_Z22,DEEP_Z23,DEEP_Z3,DEEP_Z31,DEEP_Z32,
	DEEP_Z33,DEEP_ZCOSG,DEEP_ZCOSGL,DEEP_ZCOSH,DEEP_ZCOSHL,
	DEEP_ZCOSI,DEEP_ZCOSIL,DEEP_ZE,DEEP_ZF,DEEP_ZM,DEEP_ZMO,
	DEEP_ZMOL,DEEP_ZMOS,DEEP_ZN,DEEP_ZSING,DEEP_ZSINGL,
	DEEP_ZSINH,DEEP_ZSINHL,DEEP_ZSINI,DEEP_ZSINIL,DEEP_ZX,DEEP_ZY;
	protected int DEEP_IRESFL,DEEP_ISYNFL,DEEP_IRET,DEEP_IRETN,DEEP_LS;

	protected double DEEP_ZNS,DEEP_C1SS,DEEP_ZES,DEEP_ZNL,DEEP_C1L,
	DEEP_ZEL,DEEP_ZCOSIS,DEEP_ZSINIS,DEEP_ZSINGS,
	DEEP_ZCOSGS,DEEP_Q22,DEEP_Q31,DEEP_Q33,DEEP_G22,DEEP_G32,
	DEEP_G44,DEEP_G52,DEEP_G54,
	DEEP_ROOT22,DEEP_ROOT32,DEEP_ROOT44,DEEP_ROOT52,DEEP_ROOT54,
	DEEP_THDT;

	protected double DPINI_EQSQ,DPINI_SINIQ,DPINI_COSIQ,
	DPINI_RTEQSQ,DPINI_AO,DPINI_COSQ2,DPINI_SINOMO,DPINI_COSOMO,
	DPINI_BSQ,DPINI_XLLDOT,DPINI_OMGDT,DPINI_XNODOT,DPINI_XNODP;

	protected double DPSEC_XLL,DPSEC_OMGASM,DPSEC_XNODES,DPSEC_EM,
	DPSEC_XINC,DPSEC_XN,DPSEC_T;
	
	protected double QO, SO, XJ2, XJ4;


	/**
	 * Initialise the SDP4.
	 */
	public void Init()
	{
		itsR = new double[3];
		itsV = new double[3];
		itsR[0] = 0.01; itsR[1] = 0.; itsR[2] = 0.;
		itsV[0] = 0.;   itsV[1] = 0.; itsV[2] = 0.;
		itsEpochJD    = 0.;

		// Initialize E1
		E1_XMO = 0.;
		E1_XNODEO = 0.;
		E1_OMEGAO = 0.;
		E1_EO = 0.;
		E1_XINCL = 0.;
		E1_XNO = 0.;
		E1_XNDT2O = 0.;
		E1_XNDD6O = 0.;
		E1_BSTAR = 0.;
		E1_X = 0.;
		E1_Y = 0.;
		E1_Z = 0.;
		E1_XDOT = 0.;
		E1_YDOT = 0.;
		E1_ZDOT = 0.;
		E1_EPOCH = 0.;
		E1_DS50 = 0.;

		C1_E6A = 1.E-6;
		C1_TOTHRD = .66666667;
		C1_XJ3 = -.253881E-5;
		C1_XKE = .743669161E-1;
		C1_XKMPER = 6378.135;
		C1_XMNPDA = 1440.;
		C1_AE = 1.;

		QO = 120.0;
		SO = 78.0;
		XJ2 = 1.082616E-3;
		XJ4 = -1.65597E-6;
		C1_CK2 = .5 * XJ2 * C1_AE * C1_AE;
		C1_CK4 = -.375 * XJ4 * C1_AE * C1_AE * C1_AE * C1_AE;
		C1_QOMS2T  = ((QO - SO) * C1_AE / C1_XKMPER);
		C1_QOMS2T *= C1_QOMS2T;
		C1_QOMS2T *= C1_QOMS2T;
		C1_S = C1_AE * (1. + SO / C1_XKMPER);

		C2_DE2RA = .174532925E-1;
		C2_PI = 3.14159265;
		C2_PIO2 = 1.57079633;
		C2_TWOPI = 6.2831853;
		C2_X3PIO2 = 4.71238898;

		SDP4_A1 = 0.;
		SDP4_A3OVK2 = 0.;
		SDP4_AO = 0.;
		SDP4_AODP = 0.;
		SDP4_AYCOF = 0.;
		SDP4_BETAO = 0.;
		SDP4_BETAO2 = 0.;
		SDP4_C1 = 0.;
		SDP4_C2 = 0.;
		SDP4_C4 = 0.;
		SDP4_COEF = 0.;
		SDP4_COEF1 = 0.;
		SDP4_COSG = 0.;
		SDP4_COSIO = 0.;
		SDP4_DEL1 = 0.;
		SDP4_DELO = 0.;
		SDP4_EETA = 0.;
		SDP4_EOSQ = 0.;
		SDP4_ETA = 0.;
		SDP4_ETASQ = 0.;
		SDP4_OMGDOT = 0.;
		SDP4_PERIGE = 0.;
		SDP4_PINVSQ = 0.;
		SDP4_PSISQ = 0.;
		SDP4_QOMS24 = 0.;
		SDP4_S4 = 0.;
		SDP4_SING = 0.;
		SDP4_SINIO = 0.;
		SDP4_T2COF = 0.;
		SDP4_TEMP1 = 0.;
		SDP4_TEMP2 = 0.;
		SDP4_TEMP3 = 0.;
		SDP4_THETA2 = 0.;
		SDP4_THETA4 = 0.;
		SDP4_TSI = 0.;
		SDP4_X1M5TH = 0.;
		SDP4_X1MTH2 = 0.;
		SDP4_X3THM1 = 0.;
		SDP4_X7THM1 = 0.;
		SDP4_XHDOT1 = 0.;
		SDP4_XLCOF = 0.;
		SDP4_XMDOT = 0.;
		SDP4_XNODCF = 0.;
		SDP4_XNODOT = 0.;
		SDP4_XNODP = 0.;

		DEEP_A1 = 0.;
		DEEP_A2 = 0.;
		DEEP_A3 = 0.;
		DEEP_A4 = 0.;
		DEEP_A5 = 0.;
		DEEP_A6 = 0.;
		DEEP_A7 = 0.;
		DEEP_A8 = 0.;
		DEEP_A9 = 0.;
		DEEP_A10 = 0.;
		DEEP_AINV2 = 0.;
		DEEP_ALFDP = 0.;
		DEEP_AQNV = 0.;
		DEEP_ATIME = 0.;
		DEEP_BETDP = 0.;
		DEEP_BFACT = 0.;
		DEEP_C = 0.;
		DEEP_CC = 0.;
		DEEP_COSIS = 0.;
		DEEP_COSOK = 0.;
		DEEP_COSQ = 0.;
		DEEP_CTEM = 0.;
		DEEP_D2201 = 0.;
		DEEP_D2211 = 0.;
		DEEP_D3210 = 0.;
		DEEP_D3222 = 0.;
		DEEP_D4410 = 0.;
		DEEP_D4422 = 0.;
		DEEP_D5220 = 0.;
		DEEP_D5232 = 0.;
		DEEP_D5421 = 0.;
		DEEP_D5433 = 0.;
		DEEP_DALF = 0.;
		DEEP_DAY = 0.;
		DEEP_DBET = 0.;
		DEEP_DEL1 = 0.;
		DEEP_DEL2 = 0.;
		DEEP_DEL3 = 0.;
		DEEP_DELT = 0.;
		DEEP_DLS = 0.;
		DEEP_E3 = 0.;
		DEEP_EE2 = 0.;
		DEEP_EOC = 0.;
		DEEP_EQ = 0.;
		DEEP_F2 = 0.;
		DEEP_F220 = 0.;
		DEEP_F221 = 0.;
		DEEP_F3 = 0.;
		DEEP_F311 = 0.;
		DEEP_F321 = 0.;
		DEEP_F322 = 0.;
		DEEP_F330 = 0.;
		DEEP_F441 = 0.;
		DEEP_F442 = 0.;
		DEEP_F522 = 0.;
		DEEP_F523 = 0.;
		DEEP_F542 = 0.;
		DEEP_F543 = 0.;
		DEEP_FASX2 = 0.;
		DEEP_FASX4 = 0.;
		DEEP_FASX6 = 0.;
		DEEP_FT = 0.;
		DEEP_G200 = 0.;
		DEEP_G201 = 0.;
		DEEP_G211 = 0.;
		DEEP_G300 = 0.;
		DEEP_G310 = 0.;
		DEEP_G322 = 0.;
		DEEP_G410 = 0.;
		DEEP_G422 = 0.;
		DEEP_G520 = 0.;
		DEEP_G521 = 0.;
		DEEP_G532 = 0.;
		DEEP_G533 = 0.;
		DEEP_GAM = 0.;
		DEEP_OMEGAQ = 0.;
		DEEP_PE = 0.;
		DEEP_PGH = 0.;
		DEEP_PH = 0.;

		DEEP_PINC = 0.;
		DEEP_PL = 0.;
		DEEP_PREEP = 0.;
		DEEP_S1 = 0.;
		DEEP_S2 = 0.;
		DEEP_S3 = 0.;
		DEEP_S4 = 0.;
		DEEP_S5 = 0.;
		DEEP_S6 = 0.;
		DEEP_S7 = 0.;
		DEEP_SAVTSN = 0.;
		DEEP_SE = 0.;
		DEEP_SE2 = 0.;
		DEEP_SE3 = 0.;
		DEEP_SEL = 0.;
		DEEP_SES = 0.;
		DEEP_SGH = 0.;
		DEEP_SGH2 = 0.;
		DEEP_SGH3 = 0.;
		DEEP_SGH4 = 0.;
		DEEP_SGHL = 0.;
		DEEP_SGHS = 0.;
		DEEP_SH = 0.;
		DEEP_SH2 = 0.;
		DEEP_SH3 = 0.;
		DEEP_SH1 = 0.;
		DEEP_SHS = 0.;
		DEEP_SI = 0.;
		DEEP_SI2 = 0.;
		DEEP_SI3 = 0.;
		DEEP_SIL = 0.;
		DEEP_SINI2 = 0.;
		DEEP_SINIS = 0.;
		DEEP_SINOK = 0.;
		DEEP_SINQ = 0.;
		DEEP_SINZF = 0.;
		DEEP_SIS = 0.;
		DEEP_SL = 0.;
		DEEP_SL2 = 0.;
		DEEP_SL3 = 0.;
		DEEP_SL4 = 0.;
		DEEP_SLL = 0.;
		DEEP_SLS = 0.;
		DEEP_SSE = 0.;
		DEEP_SSG = 0.;
		DEEP_SSH = 0.;
		DEEP_SSI = 0.;
		DEEP_SSL = 0.;
		DEEP_STEM = 0.;
		DEEP_STEP2 = 0.;
		DEEP_STEPN = 0.;
		DEEP_STEPP = 0.;
		DEEP_TEMP = 0.;
		DEEP_TEMP1 = 0.;
		DEEP_THGR = 0.;
		DEEP_X1 = 0.;
		DEEP_X2 = 0.;
		DEEP_X2LI = 0.;
		DEEP_X2OMI = 0.;
		DEEP_X3 = 0.;
		DEEP_X4 = 0.;
		DEEP_X5 = 0.;
		DEEP_X6 = 0.;
		DEEP_X7 = 0.;
		DEEP_X8 = 0.;
		DEEP_XFACT = 0.;
		DEEP_XGH2 = 0.;
		DEEP_XGH3 = 0.;
		DEEP_XGH4 = 0.;
		DEEP_XH2 = 0.;
		DEEP_XH3 = 0.;
		DEEP_XI2 = 0.;
		DEEP_XI3 = 0.;
		DEEP_XL = 0.;
		DEEP_XL2 = 0.;
		DEEP_XL3 = 0.;
		DEEP_XL4 = 0.;

		DEEP_XLAMO = 0.;
		DEEP_XLDOT = 0.;
		DEEP_XLI = 0.;
		DEEP_XLS = 0.;
		DEEP_XMAO = 0.;
		DEEP_XNDDT = 0.;
		DEEP_XNDOT = 0.;
		DEEP_XNI = 0.;
		DEEP_XNO2 = 0.;
		DEEP_XNODCE = 0.;
		DEEP_XNOI = 0.;
		DEEP_XNQ = 0.;
		DEEP_XOMI = 0.;
		DEEP_XPIDOT = 0.;
		DEEP_XQNCL = 0.;
		DEEP_Z1 = 0.;
		DEEP_Z11 = 0.;
		DEEP_Z12 = 0.;
		DEEP_Z13 = 0.;
		DEEP_Z2 = 0.;
		DEEP_Z21 = 0.;
		DEEP_Z22 = 0.;
		DEEP_Z23 = 0.;
		DEEP_Z3 = 0.;
		DEEP_Z31 = 0.;
		DEEP_Z32 = 0.;
		DEEP_Z33 = 0.;
		DEEP_ZCOSG = 0.;
		DEEP_ZCOSGL = 0.;
		DEEP_ZCOSH = 0.;
		DEEP_ZCOSHL = 0.;
		DEEP_ZCOSI = 0.;
		DEEP_ZCOSIL = 0.;
		DEEP_ZE = 0.;
		DEEP_ZF = 0.;
		DEEP_ZM = 0.;
		DEEP_ZMO = 0.;
		DEEP_ZMOL = 0.;
		DEEP_ZMOS = 0.;
		DEEP_ZN = 0.;
		DEEP_ZSING = 0.;
		DEEP_ZSINGL = 0.;
		DEEP_ZSINH = 0.;
		DEEP_ZSINHL = 0.;
		DEEP_ZSINI = 0.;
		DEEP_ZSINIL = 0.;
		DEEP_ZX = 0.;
		DEEP_ZY = 0.;
		DEEP_IRESFL = 0;
		DEEP_ISYNFL = 0;
		DEEP_IRET = 0;
		DEEP_IRETN = 0;
		DEEP_LS = 0;

		DEEP_ZNS = 1.19459E-5;
		DEEP_C1SS = 2.9864797E-6;
		DEEP_ZES = 0.01675;
		DEEP_ZNL = 1.5835218E-4;
		DEEP_C1L = 4.7968065E-7;
		DEEP_ZEL = 0.05490;
		DEEP_ZCOSIS = 0.91744867;
		DEEP_ZSINIS = 0.39785416;
		DEEP_ZSINGS = -0.98088458;
		DEEP_ZCOSGS = 0.1945905;
		DEEP_Q22 = 1.7891679E-6;
		DEEP_Q31 = 2.1460748E-6;
		DEEP_Q33 = 2.2123015E-7;
		DEEP_G22 = 5.7686396;
		DEEP_G32 = 0.95240898;
		DEEP_G44 = 1.8014998;
		DEEP_G52 = 1.0508330;
		DEEP_G54 = 4.4108898;
		DEEP_ROOT22 = 1.7891679E-6;
		DEEP_ROOT32 = 3.7393792E-7;
		DEEP_ROOT44 = 7.3636953E-9;
		DEEP_ROOT52 = 1.1428639E-7;
		DEEP_ROOT54 = 2.1765803E-9;
		DEEP_THDT = 4.3752691E-3;

		return;
	}


	/**
	 * Calculates the two-dimensional inverse tangents. 
	 */
	protected final double ACTAN(double SINX, double COSX)
	{
		double value, TEMP;

		if (COSX == 0.) {
			if (SINX == 0.) {
				value = 0.;
			}
			else if (SINX > 0.) {
				value = C2_PIO2;
			}
			else {
				value = C2_X3PIO2;
			}
		}
		else if (COSX > 0.) {
			if (SINX == 0.) {
				value = 0.;
			}
			else if (SINX > 0.) {
				TEMP = SINX / COSX;
				value = Math.atan(TEMP);
			}
			else {
				value = C2_TWOPI;
				TEMP = SINX / COSX;
				value = value + Math.atan(TEMP);
			}
		}
		else {
			value = C2_PI;
			TEMP = SINX / COSX;
			value = value + Math.atan(TEMP);
		}

		return value;
	}


	/**
	 * Deep space initialization. 
	 * */
	protected final void DEEP1()
	{
		DEEP_THGR = THETAG(E1_EPOCH);
		DEEP_EQ = E1_EO;
		DEEP_XNQ = DPINI_XNODP;
		DEEP_AQNV = 1./DPINI_AO;
		DEEP_XQNCL = E1_XINCL;
		DEEP_XMAO = E1_XMO;
		DEEP_XPIDOT = DPINI_OMGDT + DPINI_XNODOT;
		DEEP_SINQ = Math.sin(E1_XNODEO);
		DEEP_COSQ = Math.cos(E1_XNODEO);
		DEEP_OMEGAQ = E1_OMEGAO;

		DEEP_DAY = E1_DS50 + 18261.5;
		if (DEEP_DAY != DEEP_PREEP) {
			DEEP_PREEP = DEEP_DAY;
			DEEP_XNODCE = 4.5236020 - 9.2422029E-4 * DEEP_DAY;
			DEEP_STEM = Math.sin(DEEP_XNODCE);
			DEEP_CTEM = Math.cos(DEEP_XNODCE);
			DEEP_ZCOSIL = .91375164 - .03568096 * DEEP_CTEM;
			DEEP_ZSINIL = Math.sqrt(1. - DEEP_ZCOSIL * DEEP_ZCOSIL);
			DEEP_ZSINHL = .089683511 * DEEP_STEM / DEEP_ZSINIL;
			DEEP_ZCOSHL = Math.sqrt(1. - DEEP_ZSINHL * DEEP_ZSINHL);
			DEEP_C = 4.7199672 + .22997150 * DEEP_DAY;
			DEEP_GAM = 5.8351514 + .0019443680 * DEEP_DAY;
			DEEP_ZMOL = FMOD2P(DEEP_C - DEEP_GAM);
			DEEP_ZX = .39785416 * DEEP_STEM / DEEP_ZSINIL;
			DEEP_ZY = DEEP_ZCOSHL * DEEP_CTEM + 0.91744867 * DEEP_ZSINHL * DEEP_STEM;
			DEEP_ZX = ACTAN(DEEP_ZX, DEEP_ZY);
			DEEP_ZX = DEEP_GAM + DEEP_ZX - DEEP_XNODCE;
			DEEP_ZCOSGL = Math.cos(DEEP_ZX);
			DEEP_ZSINGL = Math.sin(DEEP_ZX);
			DEEP_ZMOS = 6.2565837 + .017201977 * DEEP_DAY;
			DEEP_ZMOS = FMOD2P(DEEP_ZMOS);
		}

		DEEP_SAVTSN = 1.E20;
		DEEP_ZCOSG = DEEP_ZCOSGS;
		DEEP_ZSING = DEEP_ZSINGS;
		DEEP_ZCOSI = DEEP_ZCOSIS;
		DEEP_ZSINI = DEEP_ZSINIS;
		DEEP_ZCOSH = DEEP_COSQ;
		DEEP_ZSINH = DEEP_SINQ;
		DEEP_CC = DEEP_C1SS;
		DEEP_ZN = DEEP_ZNS;
		DEEP_ZE = DEEP_ZES;
		DEEP_ZMO = DEEP_ZMOS;
		DEEP_XNOI = 1./DEEP_XNQ;

		DEEP_A1  =  DEEP_ZCOSG * DEEP_ZCOSH + DEEP_ZSING * DEEP_ZCOSI * DEEP_ZSINH;
		DEEP_A3  = -DEEP_ZSING * DEEP_ZCOSH + DEEP_ZCOSG * DEEP_ZCOSI * DEEP_ZSINH;
		DEEP_A7  = -DEEP_ZCOSG * DEEP_ZSINH + DEEP_ZSING * DEEP_ZCOSI * DEEP_ZCOSH;
		DEEP_A8  =  DEEP_ZSING * DEEP_ZSINI;
		DEEP_A9  =  DEEP_ZSING * DEEP_ZSINH + DEEP_ZCOSG * DEEP_ZCOSI * DEEP_ZCOSH;
		DEEP_A10 =  DEEP_ZCOSG * DEEP_ZSINI;
		DEEP_A2  =  DPINI_COSIQ * DEEP_A7 + DPINI_SINIQ * DEEP_A8;
		DEEP_A4  =  DPINI_COSIQ * DEEP_A9 + DPINI_SINIQ * DEEP_A10;
		DEEP_A5  = -DPINI_SINIQ * DEEP_A7 + DPINI_COSIQ * DEEP_A8;
		DEEP_A6  = -DPINI_SINIQ * DEEP_A9 + DPINI_COSIQ * DEEP_A10;

		DEEP_X1 =  DEEP_A1 * DPINI_COSOMO + DEEP_A2 * DPINI_SINOMO;
		DEEP_X2 =  DEEP_A3 * DPINI_COSOMO + DEEP_A4 * DPINI_SINOMO;
		DEEP_X3 = -DEEP_A1 * DPINI_SINOMO + DEEP_A2 * DPINI_COSOMO;
		DEEP_X4 = -DEEP_A3 * DPINI_SINOMO + DEEP_A4 * DPINI_COSOMO;
		DEEP_X5 =  DEEP_A5 * DPINI_SINOMO;
		DEEP_X6 =  DEEP_A6 * DPINI_SINOMO;
		DEEP_X7 =  DEEP_A5 * DPINI_COSOMO;
		DEEP_X8 =  DEEP_A6 * DPINI_COSOMO;

		DEEP_Z31 = 12. * DEEP_X1 * DEEP_X1 - 3. * DEEP_X3 * DEEP_X3;
		DEEP_Z32 = 24. * DEEP_X1 * DEEP_X2 - 6. * DEEP_X3 * DEEP_X4;
		DEEP_Z33 = 12. * DEEP_X2 * DEEP_X2 - 3. * DEEP_X4 * DEEP_X4;
		DEEP_Z1  =  3. * (DEEP_A1 * DEEP_A1 + DEEP_A2 * DEEP_A2)
				+ DEEP_Z31 * DPINI_EQSQ;
		DEEP_Z2  =  6. * (DEEP_A1 * DEEP_A3 + DEEP_A2 * DEEP_A4)
				+ DEEP_Z32 * DPINI_EQSQ;
		DEEP_Z3  =  3. * (DEEP_A3 * DEEP_A3 + DEEP_A4 * DEEP_A4)
				+ DEEP_Z33 * DPINI_EQSQ;
		DEEP_Z11 = -6. * DEEP_A1 * DEEP_A5
				+ DPINI_EQSQ * (-24. * DEEP_X1 * DEEP_X7 - 6. * DEEP_X3 * DEEP_X5);
		DEEP_Z12 = -6. * (DEEP_A1 *DEEP_A6 + DEEP_A3 * DEEP_A5)
				+ DPINI_EQSQ * (-24. * (DEEP_X2 * DEEP_X7 + DEEP_X1 * DEEP_X8)
						- 6. * (DEEP_X3 * DEEP_X6 + DEEP_X4 * DEEP_X5));
		DEEP_Z13 = -6. * DEEP_A3 * DEEP_A6
				+ DPINI_EQSQ * (-24. * DEEP_X2 * DEEP_X8 - 6. * DEEP_X4 * DEEP_X6);
		DEEP_Z21 =  6. * DEEP_A2 * DEEP_A5
				+ DPINI_EQSQ * ( 24. * DEEP_X1 * DEEP_X5 - 6. * DEEP_X3 * DEEP_X7);
		DEEP_Z22 =  6. * (DEEP_A4 * DEEP_A5 + DEEP_A2 * DEEP_A6)
				+ DPINI_EQSQ * ( 24. * (DEEP_X2 * DEEP_X5 + DEEP_X1 * DEEP_X6)
						- 6. * (DEEP_X4 * DEEP_X7 + DEEP_X3 * DEEP_X8));
		DEEP_Z23 =  6. * DEEP_A4 * DEEP_A6
				+ DPINI_EQSQ * ( 24. * DEEP_X2 * DEEP_X6 - 6. * DEEP_X4 * DEEP_X8);
		DEEP_Z1 =  DEEP_Z1 + DEEP_Z1 + DPINI_BSQ * DEEP_Z31;
		DEEP_Z2 =  DEEP_Z2 + DEEP_Z2 + DPINI_BSQ * DEEP_Z32;
		DEEP_Z3 =  DEEP_Z3 + DEEP_Z3 + DPINI_BSQ * DEEP_Z33;
		DEEP_S3 =  DEEP_CC * DEEP_XNOI;
		DEEP_S2 = -.5 * DEEP_S3 / DPINI_RTEQSQ;
		DEEP_S4 =  DEEP_S3 * DPINI_RTEQSQ;
		DEEP_S1 = -15. * DEEP_EQ * DEEP_S4;
		DEEP_S5 =  DEEP_X1 * DEEP_X3 + DEEP_X2 * DEEP_X4;
		DEEP_S6 =  DEEP_X2 * DEEP_X3 + DEEP_X1 * DEEP_X4;
		DEEP_S7 =  DEEP_X2 * DEEP_X4 - DEEP_X1 * DEEP_X3;
		DEEP_SE =  DEEP_S1 * DEEP_ZN * DEEP_S5;
		DEEP_SI =  DEEP_S2 * DEEP_ZN * (DEEP_Z11 + DEEP_Z13);
		DEEP_SL = -DEEP_ZN * DEEP_S3 * (DEEP_Z1  + DEEP_Z3
				- 14. - 6. * DPINI_EQSQ);
		DEEP_SGH =  DEEP_S4 * DEEP_ZN * (DEEP_Z31 + DEEP_Z33 - 6.);
		DEEP_SH  = -DEEP_ZN * DEEP_S2 * (DEEP_Z21 + DEEP_Z23);
		if (DEEP_XQNCL < 5.2359877E-2) DEEP_SH = 0.0;
		DEEP_EE2 =  2. * DEEP_S1 * DEEP_S6;
		DEEP_E3  =  2. * DEEP_S1 * DEEP_S7;
		DEEP_XI2 =  2. * DEEP_S2 * DEEP_Z12;
		DEEP_XI3 =  2. * DEEP_S2 * (DEEP_Z13 - DEEP_Z11);
		DEEP_XL2 = -2. * DEEP_S3 * DEEP_Z2;
		DEEP_XL3 = -2. * DEEP_S3 * (DEEP_Z3 - DEEP_Z1);
		DEEP_XL4 = -2. * DEEP_S3 * (-21. - 9. * DPINI_EQSQ) * DEEP_ZE;
		DEEP_XGH2 =   2. * DEEP_S4 * DEEP_Z32;
		DEEP_XGH3 =   2. * DEEP_S4 * (DEEP_Z33 - DEEP_Z31);
		DEEP_XGH4 = -18. * DEEP_S4 * DEEP_ZE;
		DEEP_XH2 = -2. * DEEP_S2 * DEEP_Z22;
		DEEP_XH3 = -2. * DEEP_S2 * (DEEP_Z23 - DEEP_Z21);

		DEEP_SSE = DEEP_SE;
		DEEP_SSI = DEEP_SI;
		DEEP_SSL = DEEP_SL;
		DEEP_SSH = DEEP_SH / DPINI_SINIQ;
		DEEP_SSG = DEEP_SGH - DPINI_COSIQ * DEEP_SSH;
		DEEP_SE2 = DEEP_EE2;
		DEEP_SI2 = DEEP_XI2;
		DEEP_SL2 = DEEP_XL2;
		DEEP_SGH2 = DEEP_XGH2;
		DEEP_SH2 = DEEP_XH2;
		DEEP_SE3 = DEEP_E3;
		DEEP_SI3 = DEEP_XI3;
		DEEP_SL3 = DEEP_XL3;
		DEEP_SGH3 = DEEP_XGH3;
		DEEP_SH3 = DEEP_XH3;
		DEEP_SL4 = DEEP_XL4;
		DEEP_SGH4 = DEEP_XGH4;
		DEEP_ZCOSG = DEEP_ZCOSGL;
		DEEP_ZSING = DEEP_ZSINGL;
		DEEP_ZCOSI = DEEP_ZCOSIL;
		DEEP_ZSINI = DEEP_ZSINIL;
		DEEP_ZCOSH = DEEP_ZCOSHL * DEEP_COSQ + DEEP_ZSINHL * DEEP_SINQ;
		DEEP_ZSINH = DEEP_SINQ * DEEP_ZCOSHL - DEEP_COSQ * DEEP_ZSINHL;
		DEEP_ZN = DEEP_ZNL;
		DEEP_CC = DEEP_C1L;
		DEEP_ZE = DEEP_ZEL;
		DEEP_ZMO = DEEP_ZMOL;

		DEEP_A1  =  DEEP_ZCOSG * DEEP_ZCOSH + DEEP_ZSING * DEEP_ZCOSI * DEEP_ZSINH;
		DEEP_A3  = -DEEP_ZSING * DEEP_ZCOSH + DEEP_ZCOSG * DEEP_ZCOSI * DEEP_ZSINH;
		DEEP_A7  = -DEEP_ZCOSG * DEEP_ZSINH + DEEP_ZSING * DEEP_ZCOSI * DEEP_ZCOSH;
		DEEP_A8  =  DEEP_ZSING * DEEP_ZSINI;
		DEEP_A9  =  DEEP_ZSING * DEEP_ZSINH + DEEP_ZCOSG * DEEP_ZCOSI * DEEP_ZCOSH;
		DEEP_A10 =  DEEP_ZCOSG * DEEP_ZSINI;
		DEEP_A2  =  DPINI_COSIQ * DEEP_A7 + DPINI_SINIQ * DEEP_A8;
		DEEP_A4  =  DPINI_COSIQ * DEEP_A9 + DPINI_SINIQ * DEEP_A10;
		DEEP_A5  = -DPINI_SINIQ * DEEP_A7 + DPINI_COSIQ * DEEP_A8;
		DEEP_A6  = -DPINI_SINIQ * DEEP_A9 + DPINI_COSIQ * DEEP_A10;

		DEEP_X1 =  DEEP_A1 * DPINI_COSOMO + DEEP_A2 * DPINI_SINOMO;
		DEEP_X2 =  DEEP_A3 * DPINI_COSOMO + DEEP_A4 * DPINI_SINOMO;
		DEEP_X3 = -DEEP_A1 * DPINI_SINOMO + DEEP_A2 * DPINI_COSOMO;
		DEEP_X4 = -DEEP_A3 * DPINI_SINOMO + DEEP_A4 * DPINI_COSOMO;
		DEEP_X5 =  DEEP_A5 * DPINI_SINOMO;
		DEEP_X6 =  DEEP_A6 * DPINI_SINOMO;
		DEEP_X7 =  DEEP_A5 * DPINI_COSOMO;
		DEEP_X8 =  DEEP_A6 * DPINI_COSOMO;

		DEEP_Z31 = 12. * DEEP_X1 * DEEP_X1 - 3. * DEEP_X3 * DEEP_X3;
		DEEP_Z32 = 24. * DEEP_X1 * DEEP_X2 - 6. * DEEP_X3 * DEEP_X4;
		DEEP_Z33 = 12. * DEEP_X2 * DEEP_X2 - 3. * DEEP_X4 * DEEP_X4;
		DEEP_Z1  =  3. * (DEEP_A1 * DEEP_A1 + DEEP_A2 * DEEP_A2)
				+ DEEP_Z31 * DPINI_EQSQ;
		DEEP_Z2  =  6. * (DEEP_A1 * DEEP_A3 + DEEP_A2 * DEEP_A4)
				+ DEEP_Z32 * DPINI_EQSQ;
		DEEP_Z3  =  3. * (DEEP_A3 * DEEP_A3 + DEEP_A4 * DEEP_A4)
				+ DEEP_Z33 * DPINI_EQSQ;
		DEEP_Z11 = -6. * DEEP_A1 * DEEP_A5
				+ DPINI_EQSQ * (-24. * DEEP_X1 * DEEP_X7 - 6. * DEEP_X3 * DEEP_X5);
		DEEP_Z12 = -6. * (DEEP_A1 *DEEP_A6 + DEEP_A3 * DEEP_A5)
				+ DPINI_EQSQ * (-24. * (DEEP_X2 * DEEP_X7 + DEEP_X1 * DEEP_X8)
						- 6. * (DEEP_X3 * DEEP_X6 + DEEP_X4 * DEEP_X5));
		DEEP_Z13 = -6. * DEEP_A3 * DEEP_A6
				+ DPINI_EQSQ * (-24. * DEEP_X2 * DEEP_X8 - 6. * DEEP_X4 * DEEP_X6);
		DEEP_Z21 =  6. * DEEP_A2 * DEEP_A5
				+ DPINI_EQSQ * ( 24. * DEEP_X1 * DEEP_X5 - 6. * DEEP_X3 * DEEP_X7);
		DEEP_Z22 =  6. * (DEEP_A4 * DEEP_A5 + DEEP_A2 * DEEP_A6)
				+ DPINI_EQSQ * ( 24. * (DEEP_X2 * DEEP_X5 + DEEP_X1 * DEEP_X6)
						- 6. * (DEEP_X4 * DEEP_X7 + DEEP_X3 * DEEP_X8));
		DEEP_Z23 =  6. * DEEP_A4 * DEEP_A6
				+ DPINI_EQSQ * ( 24. * DEEP_X2 * DEEP_X6 - 6. * DEEP_X4 * DEEP_X8);
		DEEP_Z1 =  DEEP_Z1 + DEEP_Z1 + DPINI_BSQ * DEEP_Z31;
		DEEP_Z2 =  DEEP_Z2 + DEEP_Z2 + DPINI_BSQ * DEEP_Z32;
		DEEP_Z3 =  DEEP_Z3 + DEEP_Z3 + DPINI_BSQ * DEEP_Z33;
		DEEP_S3 =  DEEP_CC * DEEP_XNOI;
		DEEP_S2 = -.5 * DEEP_S3 / DPINI_RTEQSQ;
		DEEP_S4 =  DEEP_S3 * DPINI_RTEQSQ;
		DEEP_S1 = -15. * DEEP_EQ * DEEP_S4;
		DEEP_S5 =  DEEP_X1 * DEEP_X3 + DEEP_X2 * DEEP_X4;
		DEEP_S6 =  DEEP_X2 * DEEP_X3 + DEEP_X1 * DEEP_X4;
		DEEP_S7 =  DEEP_X2 * DEEP_X4 - DEEP_X1 * DEEP_X3;
		DEEP_SE =  DEEP_S1 * DEEP_ZN * DEEP_S5;
		DEEP_SI =  DEEP_S2 * DEEP_ZN * (DEEP_Z11 + DEEP_Z13);
		DEEP_SL = -DEEP_ZN * DEEP_S3 * (DEEP_Z1  + DEEP_Z3
				- 14. - 6. * DPINI_EQSQ);
		DEEP_SGH =  DEEP_S4 * DEEP_ZN * (DEEP_Z31 + DEEP_Z33 - 6.);
		DEEP_SH  = -DEEP_ZN * DEEP_S2 * (DEEP_Z21 + DEEP_Z23);
		if (DEEP_XQNCL < 5.2359877E-2) DEEP_SH = 0.0;
		DEEP_EE2 =  2. * DEEP_S1 * DEEP_S6;
		DEEP_E3  =  2. * DEEP_S1 * DEEP_S7;
		DEEP_XI2 =  2. * DEEP_S2 * DEEP_Z12;
		DEEP_XI3 =  2. * DEEP_S2 * (DEEP_Z13 - DEEP_Z11);
		DEEP_XL2 = -2. * DEEP_S3 * DEEP_Z2;
		DEEP_XL3 = -2. * DEEP_S3 * (DEEP_Z3 - DEEP_Z1);
		DEEP_XL4 = -2. * DEEP_S3 * (-21. - 9. * DPINI_EQSQ) * DEEP_ZE;
		DEEP_XGH2 =   2. * DEEP_S4 * DEEP_Z32;
		DEEP_XGH3 =   2. * DEEP_S4 * (DEEP_Z33 - DEEP_Z31);
		DEEP_XGH4 = -18. * DEEP_S4 * DEEP_ZE;
		DEEP_XH2 = -2. * DEEP_S2 * DEEP_Z22;
		DEEP_XH3 = -2. * DEEP_S2 * (DEEP_Z23 - DEEP_Z21);

		DEEP_SSE = DEEP_SSE + DEEP_SE;
		DEEP_SSI = DEEP_SSI + DEEP_SI;
		DEEP_SSL = DEEP_SSL + DEEP_SL;
		DEEP_SSG = DEEP_SSG + DEEP_SGH - DPINI_COSIQ / DPINI_SINIQ * DEEP_SH;
		DEEP_SSH = DEEP_SSH + DEEP_SH / DPINI_SINIQ;

		DEEP_IRESFL = 0;
		DEEP_ISYNFL = 0;
		if (DEEP_XNQ >= .0052359877 || DEEP_XNQ <= .0034906585) {
			if (DEEP_XNQ < 8.26E-3  || DEEP_XNQ > 9.24E-3) return;
			if (DEEP_EQ  < 0.5) return;
			DEEP_IRESFL = 1;
			DEEP_EOC = DEEP_EQ * DPINI_EQSQ;
			DEEP_G201 = -.306 - (DEEP_EQ - .64) * .440;

			if (DEEP_EQ <= .65) {
				DEEP_G211 =     3.616  -    13.247  * DEEP_EQ
						+    16.290  * DPINI_EQSQ;
				DEEP_G310 =   -19.302  +   117.390  * DEEP_EQ
						-   228.419  * DPINI_EQSQ +   156.591  * DEEP_EOC;
				DEEP_G322 =   -18.9068 +   109.7927 * DEEP_EQ
						-   214.6334 * DPINI_EQSQ +   146.5816 * DEEP_EOC;
				DEEP_G410 =   -41.122  +   242.694  * DEEP_EQ
						-   471.094  * DPINI_EQSQ +   313.953  * DEEP_EOC;
				DEEP_G422 =  -146.407  +   841.880  * DEEP_EQ
						-  1629.014  * DPINI_EQSQ +  1083.435  * DEEP_EOC;
				DEEP_G520 =  -532.114  +  3017.977  * DEEP_EQ
						-  5740.     * DPINI_EQSQ +  3708.276  * DEEP_EOC;
			}
			else {
				DEEP_G211 =   -72.099  +   331.819  * DEEP_EQ
						-   508.738  * DPINI_EQSQ +   266.724  * DEEP_EOC;
				DEEP_G310 =  -346.844  +  1582.851  * DEEP_EQ
						-  2415.925  * DPINI_EQSQ +  1246.113  * DEEP_EOC;
				DEEP_G322 =  -342.585  +  1554.908  * DEEP_EQ
						-  2366.899  * DPINI_EQSQ +  1215.972  * DEEP_EOC;
				DEEP_G410 = -1052.797  +  4758.686  * DEEP_EQ
						-  7193.992  * DPINI_EQSQ +  3651.957  * DEEP_EOC;
				DEEP_G422 = -3581.69   + 16178.11   * DEEP_EQ
						- 24462.77   * DPINI_EQSQ + 12422.52   * DEEP_EOC;
				if (DEEP_EQ <= .715) {
					DEEP_G520 =  1464.74 -  4664.75 * DEEP_EQ +  3763.64 * DPINI_EQSQ;
				}
				else {
					DEEP_G520 = -5149.66 + 29936.92 * DEEP_EQ - 54087.36 * DPINI_EQSQ
							+ 31324.56 * DEEP_EOC;
				}
			}

			if (DEEP_EQ < .7) {
				DEEP_G533 = -919.2277  + 4988.61   * DEEP_EQ
						- 9064.77   * DPINI_EQSQ + 5542.21  * DEEP_EOC;
				DEEP_G521 = -822.71072 + 4568.6173 * DEEP_EQ
						- 8491.4146 * DPINI_EQSQ + 5337.524 * DEEP_EOC;
				DEEP_G532 = -853.666   + 4690.25   * DEEP_EQ
						- 8624.77   * DPINI_EQSQ + 5341.4   * DEEP_EOC;
			}
			else {
				DEEP_G533 = -37995.78  + 161616.52 * DEEP_EQ
						- 229838.2  * DPINI_EQSQ + 109377.94 * DEEP_EOC;
				DEEP_G521 = -51752.104 + 218913.95 * DEEP_EQ
						- 309468.16 * DPINI_EQSQ + 146349.42 * DEEP_EOC;
				DEEP_G532 = -40023.88  + 170470.89 * DEEP_EQ
						- 242699.48 * DPINI_EQSQ + 115605.82 * DEEP_EOC;
			}

			DEEP_SINI2 = DPINI_SINIQ * DPINI_SINIQ;
			DEEP_F220 =   .75 * (1. + 2. * DPINI_COSIQ + DPINI_COSQ2);
			DEEP_F221 =  1.5     * DEEP_SINI2;
			DEEP_F321 =  1.875   * DPINI_SINIQ * (1. - 2. * DPINI_COSIQ
					- 3. * DPINI_COSQ2);
			DEEP_F322 = -1.875   * DPINI_SINIQ * (1. + 2. * DPINI_COSIQ
					- 3. * DPINI_COSQ2);
			DEEP_F441 = 35.      * DEEP_SINI2 * DEEP_F220;
			DEEP_F442 = 39.3750  * DEEP_SINI2 * DEEP_SINI2;
			DEEP_F522 =  9.84375   * DPINI_SINIQ * (DEEP_SINI2 * ( 1.
					- 2. * DPINI_COSIQ -  5. * DPINI_COSQ2)
					+  .33333333 * (-2. + 4. * DPINI_COSIQ + 6. * DPINI_COSQ2));
			DEEP_F523 = DPINI_SINIQ * (4.92187512 * DEEP_SINI2
					* (-2. - 4. * DPINI_COSIQ
							+ 10. * DPINI_COSQ2) + 6.56250012 * ( 1. + 2. * DPINI_COSIQ
									- 3. * DPINI_COSQ2));
			DEEP_F542 = 29.53125 * DPINI_SINIQ * ( 2. - 8. * DPINI_COSIQ
					+ DPINI_COSQ2 * (-12. + 8. * DPINI_COSIQ + 10. * DPINI_COSQ2));
			DEEP_F543 = 29.53125 * DPINI_SINIQ * (-2. - 8. * DPINI_COSIQ
					+ DPINI_COSQ2 * ( 12. + 8. * DPINI_COSIQ - 10. * DPINI_COSQ2));
			DEEP_XNO2 = DEEP_XNQ * DEEP_XNQ;
			DEEP_AINV2 = DEEP_AQNV * DEEP_AQNV;
			DEEP_TEMP1 = 3. * DEEP_XNO2 * DEEP_AINV2;
			DEEP_TEMP = DEEP_TEMP1 * DEEP_ROOT22;
			DEEP_D2201 = DEEP_TEMP * DEEP_F220*DEEP_G201;
			DEEP_D2211 = DEEP_TEMP * DEEP_F221*DEEP_G211;
			DEEP_TEMP1 = DEEP_TEMP1 * DEEP_AQNV;
			DEEP_TEMP = DEEP_TEMP1 * DEEP_ROOT32;
			DEEP_D3210 = DEEP_TEMP * DEEP_F321 * DEEP_G310;
			DEEP_D3222 = DEEP_TEMP * DEEP_F322 * DEEP_G322;
			DEEP_TEMP1 = DEEP_TEMP1 * DEEP_AQNV;
			DEEP_TEMP = 2. * DEEP_TEMP1 * DEEP_ROOT44;
			DEEP_D4410 = DEEP_TEMP * DEEP_F441 * DEEP_G410;
			DEEP_D4422 = DEEP_TEMP * DEEP_F442 * DEEP_G422;
			DEEP_TEMP1 = DEEP_TEMP1 * DEEP_AQNV;
			DEEP_TEMP = DEEP_TEMP1 * DEEP_ROOT52;
			DEEP_D5220 = DEEP_TEMP * DEEP_F522 * DEEP_G520;
			DEEP_D5232 = DEEP_TEMP * DEEP_F523 * DEEP_G532;
			DEEP_TEMP = 2. * DEEP_TEMP1 * DEEP_ROOT54;
			DEEP_D5421 = DEEP_TEMP * DEEP_F542 * DEEP_G521;
			DEEP_D5433 = DEEP_TEMP * DEEP_F543 * DEEP_G533;
			DEEP_XLAMO = DEEP_XMAO + E1_XNODEO + E1_XNODEO - DEEP_THGR - DEEP_THGR;
			DEEP_BFACT = DPINI_XLLDOT + DPINI_XNODOT + DPINI_XNODOT
					- DEEP_THDT - DEEP_THDT;
			DEEP_BFACT = DEEP_BFACT + DEEP_SSL + DEEP_SSH + DEEP_SSH;
		}

		else {
			DEEP_IRESFL = 1;
			DEEP_ISYNFL = 1;
			DEEP_G200 = 1.0 + DPINI_EQSQ * (-2.5 + .8125 * DPINI_EQSQ);
			DEEP_G310 = 1.0 + 2.0 * DPINI_EQSQ;
			DEEP_G300 = 1.0 + DPINI_EQSQ * (-6.0 + 6.60937 * DPINI_EQSQ);
			DEEP_F220 = .75 * (1. + DPINI_COSIQ) * (1. + DPINI_COSIQ);
			DEEP_F311 = .9375 * DPINI_SINIQ * DPINI_SINIQ * (1. + 3. * DPINI_COSIQ)
					- .75 * (1. + DPINI_COSIQ);
			DEEP_F330 = 1. + DPINI_COSIQ;
			DEEP_F330 = 1.875 * DEEP_F330 * DEEP_F330 * DEEP_F330;
			DEEP_DEL1 = 3. * DEEP_XNQ  * DEEP_XNQ  * DEEP_AQNV * DEEP_AQNV;
			DEEP_DEL2 = 2. * DEEP_DEL1 * DEEP_F220 * DEEP_G200 * DEEP_Q22;
			DEEP_DEL3 = 3. * DEEP_DEL1 * DEEP_F330 * DEEP_G300 * DEEP_Q33 * DEEP_AQNV;
			DEEP_DEL1 = DEEP_DEL1 * DEEP_F311 * DEEP_G310 * DEEP_Q31 * DEEP_AQNV;
			DEEP_FASX2 = .13130908;
			DEEP_FASX4 = 2.8843198;
			DEEP_FASX6 = .37448087;
			DEEP_XLAMO = DEEP_XMAO + E1_XNODEO + E1_OMEGAO - DEEP_THGR;
			DEEP_BFACT = DPINI_XLLDOT + DEEP_XPIDOT - DEEP_THDT;
			DEEP_BFACT = DEEP_BFACT + DEEP_SSL + DEEP_SSG + DEEP_SSH;
		}

		DEEP_XFACT = DEEP_BFACT - DEEP_XNQ;

		DEEP_XLI = DEEP_XLAMO;
		DEEP_XNI = DEEP_XNQ;
		DEEP_ATIME =    0.;
		DEEP_STEPP =  720.;
		DEEP_STEPN = -720.;
		DEEP_STEP2 = 259200.;
		return;
	}


	/**
	 * Deep space secular effects. 
	 * */
	protected final void DEEP2()
	{
		DPSEC_XLL    = DPSEC_XLL    + DEEP_SSL * DPSEC_T;
		DPSEC_OMGASM = DPSEC_OMGASM + DEEP_SSG * DPSEC_T;
		DPSEC_XNODES = DPSEC_XNODES + DEEP_SSH * DPSEC_T;
		DPSEC_EM   = E1_EO    + DEEP_SSE * DPSEC_T;
		DPSEC_XINC = E1_XINCL + DEEP_SSI * DPSEC_T;
		if (DPSEC_XINC < 0.) {
			DPSEC_XINC   = -DPSEC_XINC;
			DPSEC_XNODES =  DPSEC_XNODES + C2_PI;
			DPSEC_OMGASM =  DPSEC_OMGASM - C2_PI;
		}
		if (DEEP_IRESFL == 0) return;

		for (;;) {

			if (DEEP_ATIME == 0. ||
					(DPSEC_T >= 0. && DEEP_ATIME <  0.) ||
					(DPSEC_T <  0. && DEEP_ATIME >= 0.)) {
				if (DPSEC_T < 0.) {
					DEEP_DELT = DEEP_STEPN;
				}
				else {
					DEEP_DELT = DEEP_STEPP;
				}
				DEEP_ATIME = 0.;
				DEEP_XNI = DEEP_XNQ;
				DEEP_XLI = DEEP_XLAMO;
				if (Math.abs(DPSEC_T - DEEP_ATIME) >= DEEP_STEPP) {
					DEEP_IRET  = 125;
					DEEP_IRETN = 165;
				}
				else {
					DEEP_FT = DPSEC_T - DEEP_ATIME;
					DEEP_IRETN = 140;
				}
			}
			else if (Math.abs(DPSEC_T) >= Math.abs(DEEP_ATIME)) {
				DEEP_DELT = DEEP_STEPN;
				if (DPSEC_T > 0.) DEEP_DELT = DEEP_STEPP;
				if (Math.abs(DPSEC_T - DEEP_ATIME) >= DEEP_STEPP) {
					DEEP_IRET  = 125;
					DEEP_IRETN = 165;
				}
				else {
					DEEP_FT = DPSEC_T-DEEP_ATIME;
					DEEP_IRETN = 140;
				}
			}
			else {
				DEEP_DELT = DEEP_STEPP;
				if (DPSEC_T >= 0.) DEEP_DELT = DEEP_STEPN;
				DEEP_IRET  = 100;
				DEEP_IRETN = 165;
			}

			for (;;) {

				if (DEEP_ISYNFL != 0) {
					DEEP_XNDOT = DEEP_DEL1 * Math.sin(DEEP_XLI - DEEP_FASX2)
							+ DEEP_DEL2 * Math.sin(2. * (DEEP_XLI - DEEP_FASX4))
							+ DEEP_DEL3 * Math.sin(3. * (DEEP_XLI - DEEP_FASX6));
					DEEP_XNDDT = DEEP_DEL1 * Math.cos(DEEP_XLI - DEEP_FASX2)
							+ 2. * DEEP_DEL2 * Math.cos(2. * (DEEP_XLI - DEEP_FASX4))
							+ 3. * DEEP_DEL3 * Math.cos(3. * (DEEP_XLI - DEEP_FASX6));
				}
				else {
					DEEP_XOMI  = DEEP_OMEGAQ + DPINI_OMGDT * DEEP_ATIME;
					DEEP_X2OMI = DEEP_XOMI + DEEP_XOMI;
					DEEP_X2LI  = DEEP_XLI + DEEP_XLI;
					DEEP_XNDOT = DEEP_D2201 * Math.sin(DEEP_X2OMI + DEEP_XLI - DEEP_G22)
							+ DEEP_D2211 * Math.sin( DEEP_XLI   - DEEP_G22)
							+ DEEP_D3210 * Math.sin( DEEP_XOMI  + DEEP_XLI  - DEEP_G32)
							+ DEEP_D3222 * Math.sin(-DEEP_XOMI  + DEEP_XLI  - DEEP_G32)
							+ DEEP_D4410 * Math.sin( DEEP_X2OMI + DEEP_X2LI - DEEP_G44)
							+ DEEP_D4422 * Math.sin( DEEP_X2LI  - DEEP_G44)
							+ DEEP_D5220 * Math.sin( DEEP_XOMI  + DEEP_XLI  - DEEP_G52)
							+ DEEP_D5232 * Math.sin(-DEEP_XOMI  + DEEP_XLI  - DEEP_G52)
							+ DEEP_D5421 * Math.sin( DEEP_XOMI  + DEEP_X2LI - DEEP_G54)
							+ DEEP_D5433 * Math.sin(-DEEP_XOMI  + DEEP_X2LI - DEEP_G54);
					DEEP_XNDDT = DEEP_D2201 * Math.cos(DEEP_X2OMI + DEEP_XLI - DEEP_G22)
							+ DEEP_D2211 * Math.cos( DEEP_XLI   - DEEP_G22)
							+ DEEP_D3210 * Math.cos( DEEP_XOMI  + DEEP_XLI  - DEEP_G32)
							+ DEEP_D3222 * Math.cos(-DEEP_XOMI  + DEEP_XLI  - DEEP_G32)
							+ DEEP_D5220 * Math.cos( DEEP_XOMI  + DEEP_XLI  - DEEP_G52)
							+ DEEP_D5232 * Math.cos(-DEEP_XOMI  + DEEP_XLI  - DEEP_G52)
							+ 2. * (DEEP_D4410 * Math.cos(DEEP_X2OMI + DEEP_X2LI - DEEP_G44)
									+ DEEP_D4422 * Math.cos( DEEP_X2LI  - DEEP_G44)
									+ DEEP_D5421 * Math.cos( DEEP_XOMI  + DEEP_X2LI - DEEP_G54)
									+ DEEP_D5433 * Math.cos(-DEEP_XOMI  + DEEP_X2LI - DEEP_G54));
				}
				DEEP_XLDOT = DEEP_XNI + DEEP_XFACT;
				DEEP_XNDDT = DEEP_XNDDT * DEEP_XLDOT;
				if (DEEP_IRETN == 140) {
					DPSEC_XN = DEEP_XNI + DEEP_XNDOT * DEEP_FT
							+ DEEP_XNDDT * DEEP_FT * DEEP_FT * 0.5;
					DEEP_XL = DEEP_XLI + DEEP_XLDOT * DEEP_FT
							+ DEEP_XNDOT * DEEP_FT * DEEP_FT * 0.5;
					DEEP_TEMP = -DPSEC_XNODES + DEEP_THGR + DPSEC_T * DEEP_THDT;
					DPSEC_XLL = DEEP_XL - DPSEC_OMGASM + DEEP_TEMP;
					if (DEEP_ISYNFL == 0) DPSEC_XLL = DEEP_XL + DEEP_TEMP + DEEP_TEMP;
					return;
				}
				if (DEEP_IRETN == 165) {
					DEEP_XLI = DEEP_XLI + DEEP_XLDOT * DEEP_DELT
							+ DEEP_XNDOT * DEEP_STEP2;
					DEEP_XNI = DEEP_XNI + DEEP_XNDOT * DEEP_DELT
							+ DEEP_XNDDT * DEEP_STEP2;
					DEEP_ATIME = DEEP_ATIME + DEEP_DELT;
				}
				if (DEEP_IRET == 125) {
					if (Math.abs(DPSEC_T - DEEP_ATIME) >= DEEP_STEPP) {
						DEEP_IRET  = 125;
						DEEP_IRETN = 165;
					}
					else {
						DEEP_FT = DPSEC_T - DEEP_ATIME;
						DEEP_IRETN = 140;
					}
				}
				if (DEEP_IRET != 125) break;
			}
		}
	}


	/**
	 * Deep space lunar-solar periodics. 
	 * */
	protected final void DEEP3()
	{
		DEEP_SINIS = Math.sin(DPSEC_XINC);
		DEEP_COSIS = Math.cos(DPSEC_XINC);
		if (Math.abs(DEEP_SAVTSN - DPSEC_T) >= 30.) {
			DEEP_SAVTSN = DPSEC_T;
			DEEP_ZM = DEEP_ZMOS +    DEEP_ZNS * DPSEC_T;
			DEEP_ZF = DEEP_ZM + 2. * DEEP_ZES * Math.sin(DEEP_ZM);
			DEEP_SINZF = Math.sin(DEEP_ZF);
			DEEP_F2 =  .5 * DEEP_SINZF * DEEP_SINZF - .25;
			DEEP_F3 = -.5 * DEEP_SINZF * Math.cos(DEEP_ZF);
			DEEP_SES  = DEEP_SE2  * DEEP_F2 + DEEP_SE3  * DEEP_F3;
			DEEP_SIS  = DEEP_SI2  * DEEP_F2 + DEEP_SI3  * DEEP_F3;
			DEEP_SLS  = DEEP_SL2  * DEEP_F2 + DEEP_SL3  * DEEP_F3
					+ DEEP_SL4  * DEEP_SINZF;
			DEEP_SGHS = DEEP_SGH2 * DEEP_F2 + DEEP_SGH3 * DEEP_F3
					+ DEEP_SGH4 * DEEP_SINZF;
			DEEP_SHS  = DEEP_SH2  * DEEP_F2 + DEEP_SH3  * DEEP_F3;
			DEEP_ZM = DEEP_ZMOL + DEEP_ZNL * DPSEC_T;
			DEEP_ZF = DEEP_ZM + 2. * DEEP_ZEL * Math.sin(DEEP_ZM);
			DEEP_SINZF = Math.sin(DEEP_ZF);
			DEEP_F2 =  .5 * DEEP_SINZF * DEEP_SINZF - .25;
			DEEP_F3 = -.5 * DEEP_SINZF * Math.cos(DEEP_ZF);
			DEEP_SEL  = DEEP_EE2  * DEEP_F2 + DEEP_E3   * DEEP_F3;
			DEEP_SIL  = DEEP_XI2  * DEEP_F2 + DEEP_XI3  * DEEP_F3;
			DEEP_SLL  = DEEP_XL2  * DEEP_F2 + DEEP_XL3  * DEEP_F3
					+ DEEP_XL4  * DEEP_SINZF;
			DEEP_SGHL = DEEP_XGH2 * DEEP_F2 + DEEP_XGH3 * DEEP_F3
					+ DEEP_XGH4 * DEEP_SINZF;
			DEEP_SH1 = DEEP_XH2 * DEEP_F2 + DEEP_XH3 * DEEP_F3;
			DEEP_PE   = DEEP_SES + DEEP_SEL;
			DEEP_PINC = DEEP_SIS + DEEP_SIL;
			DEEP_PL   = DEEP_SLS + DEEP_SLL;
		}
		DEEP_PGH = DEEP_SGHS + DEEP_SGHL;
		DEEP_PH  = DEEP_SHS  + DEEP_SH1;
		DPSEC_XINC = DPSEC_XINC + DEEP_PINC;
		DPSEC_EM = DPSEC_EM + DEEP_PE;

		if (DEEP_XQNCL >= .2) {
			DEEP_PH = DEEP_PH / DPINI_SINIQ;
			DEEP_PGH = DEEP_PGH - DPINI_COSIQ * DEEP_PH;
			DPSEC_OMGASM = DPSEC_OMGASM + DEEP_PGH;
			DPSEC_XNODES = DPSEC_XNODES + DEEP_PH;
			DPSEC_XLL = DPSEC_XLL + DEEP_PL;
		}
		else {
			DEEP_SINOK = Math.sin(DPSEC_XNODES);
			DEEP_COSOK = Math.cos(DPSEC_XNODES);
			DEEP_ALFDP = DEEP_SINIS*DEEP_SINOK;
			DEEP_BETDP = DEEP_SINIS*DEEP_COSOK;
			DEEP_DALF  =  DEEP_PH * DEEP_COSOK + DEEP_PINC * DEEP_COSIS * DEEP_SINOK;
			DEEP_DBET  = -DEEP_PH * DEEP_SINOK + DEEP_PINC * DEEP_COSIS * DEEP_COSOK;
			DEEP_ALFDP = DEEP_ALFDP + DEEP_DALF;
			DEEP_BETDP = DEEP_BETDP + DEEP_DBET;
			DEEP_XLS   = DPSEC_XLL + DPSEC_OMGASM + DEEP_COSIS * DPSEC_XNODES;
			DEEP_DLS   = DEEP_PL + DEEP_PGH - DEEP_PINC * DPSEC_XNODES * DEEP_SINIS;
			DEEP_XLS   = DEEP_XLS + DEEP_DLS;
			DPSEC_XNODES =ACTAN(DEEP_ALFDP, DEEP_BETDP);
			DPSEC_XLL    = DPSEC_XLL + DEEP_PL;
			DPSEC_OMGASM = DEEP_XLS - DPSEC_XLL
					- Math.cos(DPSEC_XINC) * DPSEC_XNODES;
		}
		return;
	}

	/**
	 * Deep space initialization. 
	 * */
	protected final void DPINIT(double EOSQ, double SINIO, double COSIO,
			double BETAO, double AODP, double THETA2, double SING, double COSG,
			double BETAO2, double XMDOT, double OMGDOT, double XNODOTT, double XNODPP)
	{
		DPINI_EQSQ = EOSQ;
		DPINI_SINIQ = SINIO;
		DPINI_COSIQ = COSIO;
		DPINI_RTEQSQ = BETAO;
		DPINI_AO = AODP;
		DPINI_COSQ2 = THETA2;
		DPINI_SINOMO = SING;
		DPINI_COSOMO = COSG;
		DPINI_BSQ = BETAO2;
		DPINI_XLLDOT = XMDOT;
		DPINI_OMGDT = OMGDOT;
		DPINI_XNODOT = XNODOTT;
		DPINI_XNODP = XNODPP;
		DEEP1();
		EOSQ = DPINI_EQSQ;
		SINIO = DPINI_SINIQ;
		COSIO = DPINI_COSIQ;
		BETAO = DPINI_RTEQSQ;
		AODP = DPINI_AO;
		THETA2 = DPINI_COSQ2;
		SING = DPINI_SINOMO;
		COSG = DPINI_COSOMO;
		BETAO2 = DPINI_BSQ;
		XMDOT = DPINI_XLLDOT;
		OMGDOT = DPINI_OMGDT;
		XNODOTT = DPINI_XNODOT;
		XNODPP = DPINI_XNODP;
		return;
	}


	/**
	 * Deep space lunar-solar periodics. 
	 * */
	protected final void DPPER(double[] dpper_args)
	{
		DPSEC_EM     = dpper_args[0];
		DPSEC_XINC   = dpper_args[1];
		DPSEC_OMGASM = dpper_args[2];
		DPSEC_XNODES = dpper_args[3];
		DPSEC_XLL    = dpper_args[4];
		DEEP3();
		dpper_args[0] = DPSEC_EM;
		dpper_args[1] = DPSEC_XINC;
		dpper_args[2] = DPSEC_OMGASM;
		dpper_args[3] = DPSEC_XNODES;
		dpper_args[4] = DPSEC_XLL;
		return;
	}


	/**
	 * Deep space secular effects. 
	 * */
	protected final void DPSEC(double[] dpsec_args, double[] TSINCE)
	{
		DPSEC_XLL    = dpsec_args[0];
		DPSEC_OMGASM = dpsec_args[1];
		DPSEC_XNODES = dpsec_args[2];
		/* DPSEC_EM = EMM
		 * DPSEC_XINC = XINCC */
		DPSEC_XN = dpsec_args[5];
		DPSEC_T = TSINCE[0];
		DEEP2();
		dpsec_args[0] = DPSEC_XLL;
		dpsec_args[1] = DPSEC_OMGASM;
		dpsec_args[2] = DPSEC_XNODES;
		dpsec_args[3] = DPSEC_EM;
		dpsec_args[4] = DPSEC_XINC;
		dpsec_args[5] = DPSEC_XN;
		TSINCE[0] = DPSEC_T;
		return;
	}


	/**
	 * Calculates the modulo 2 pi. 
	 * */
	protected final double FMOD2P(double X)
	{
		double value;
		int    I;
		value = X;
		I = (int)(value/C2_TWOPI);
		value = value - I * C2_TWOPI;
		if (value < 0) value += C2_TWOPI;
		return value;
	}

	/**
	 * Run the SDP4 model.
	 * @param data_norad		Array holding the NORAD data
	 * @param julDay			The current time expressed in Julian days
	 */
	public final double[] RunSDP4(double[] data_norad, double julDay)
	{
		double A, AXN, AYN, AYNL, BETA, BETAL, CAPU, COS2U, COSEPW,
		COSIK, COSNOK, COSU, COSUK, E, ECOSE, ELSQ, EM, EPW, ESINE, OMGADF,
		PL, R, RDOT, RDOTK, RFDOT, RFDOTK, RK, SIN2U, SINEPW, SINIK,
		SINNOK, SINU, SINUK, TEMP, TEMP4, TEMP5, TEMP6, TEMPA,
		TEMPE, TEMPL, TSQ, U, UK, UX, UY, UZ, VX, VY, VZ, XINC, XINCK,
		XL, XLL, XLT, XMAM, XMDF, XMX, XMY, XN, XNODDF, XNODE, XNODEK;
		double[] dpsec_args = new double[6];
		double[] dpper_args = new double[5];
		int I;

		TEMP4 = 0.;
		TEMP5 = 0.;
		TEMP6 = 0.;
		COSEPW = 0.;
		SINEPW = 0.;
		EM = 0.;
		XINC = 0.;

		int[] IFLAG = new int[1];
		double[] TSINCE = new double[1];

		IFLAG[0] = 1;
		TSINCE[0] = 0;

		E1_XNDT2O = data_norad[1];
		E1_XNDD6O = data_norad[2];
		E1_BSTAR = data_norad[3]/ C1_AE / 1E5;
		E1_XINCL = data_norad[4];
		E1_XNODEO = data_norad[5];
		E1_EO = data_norad[6]/1E7;
		E1_OMEGAO = data_norad[7];
		E1_XMO = data_norad[8];
		E1_XNO = data_norad[9];
		E1_EPOCH = data_norad[10];

		E1_XNODEO = E1_XNODEO * C2_DE2RA;
		E1_OMEGAO = E1_OMEGAO * C2_DE2RA;
		E1_XMO    = E1_XMO    * C2_DE2RA;
		E1_XINCL  = E1_XINCL  * C2_DE2RA;
		E1_XNO    = E1_XNO    * C2_TWOPI / C1_XMNPDA;
		E1_XNDT2O = E1_XNDT2O * C2_TWOPI / C1_XMNPDA / C1_XMNPDA;
		E1_XNDD6O = E1_XNDD6O * C2_TWOPI / C1_XMNPDA / C1_XMNPDA / C1_XMNPDA;

		double year  = Math.floor(E1_EPOCH / 1000.);

		double day   = E1_EPOCH - 1000. * year;
		if (year < 57) {year += 2000.;} else {year += 1900.;}
		year--;
		itsEpochJD  = 5643.5 - 10000. + day;
		itsEpochJD += 365. * (year - 1985.) + Math.floor(year/4.)
				- Math.floor(year/100.) + Math.floor(year/400.)	+ 306.;

		TSINCE[0] = C1_XMNPDA * (julDay - itsEpochJD);

		if (IFLAG[0] != 0) {

			SDP4_A1 = Math.pow(C1_XKE / E1_XNO, C1_TOTHRD);
			SDP4_COSIO = Math.cos(E1_XINCL);
			SDP4_THETA2 = SDP4_COSIO * SDP4_COSIO;
			SDP4_X3THM1 = 3. * SDP4_THETA2 - 1.;
			SDP4_EOSQ = E1_EO * E1_EO;
			SDP4_BETAO2 = 1. - SDP4_EOSQ;
			SDP4_BETAO = Math.sqrt(SDP4_BETAO2);
			SDP4_DEL1 = 1.5 * C1_CK2 * SDP4_X3THM1
					/ (SDP4_A1 * SDP4_A1 * SDP4_BETAO * SDP4_BETAO2);
			SDP4_AO = SDP4_A1 * (1. - SDP4_DEL1 * (.5 * C1_TOTHRD + SDP4_DEL1
					* (1. + 134./81. * SDP4_DEL1)));
			SDP4_DELO = 1.5 * C1_CK2 * SDP4_X3THM1
					/ (SDP4_AO * SDP4_AO * SDP4_BETAO * SDP4_BETAO2);
			SDP4_XNODP = E1_XNO / (1. + SDP4_DELO);
			SDP4_AODP = SDP4_AO / (1. - SDP4_DELO);

			SDP4_S4 = C1_S;
			SDP4_QOMS24 = C1_QOMS2T;
			SDP4_PERIGE = (SDP4_AODP * (1. - E1_EO) - C1_AE) * C1_XKMPER;
			if (SDP4_PERIGE < 156.) {
				SDP4_S4 = SDP4_PERIGE - 78.;
				if (SDP4_PERIGE <= 98.) {
					SDP4_S4 = 20.;
				}
				SDP4_QOMS24 = ((120. - SDP4_S4) * C1_AE / C1_XKMPER);
				SDP4_QOMS24 *= SDP4_QOMS24;
				SDP4_QOMS24 *= SDP4_QOMS24;
				SDP4_S4 = SDP4_S4 / C1_XKMPER + C1_AE;
			}
			SDP4_PINVSQ = 1. / (SDP4_AODP * SDP4_AODP * SDP4_BETAO2 * SDP4_BETAO2);
			SDP4_SING = Math.sin(E1_OMEGAO);
			SDP4_COSG = Math.cos(E1_OMEGAO);
			SDP4_TSI = 1. / (SDP4_AODP - SDP4_S4);
			SDP4_ETA = SDP4_AODP * E1_EO * SDP4_TSI;
			SDP4_ETASQ = SDP4_ETA * SDP4_ETA;
			SDP4_EETA = E1_EO * SDP4_ETA;
			SDP4_PSISQ = Math.abs(1. - SDP4_ETASQ);
			SDP4_COEF = SDP4_QOMS24 * SDP4_TSI * SDP4_TSI * SDP4_TSI * SDP4_TSI;
			SDP4_COEF1 = SDP4_COEF / Math.pow(SDP4_PSISQ, 3.5);
			SDP4_C2 = SDP4_COEF1 * SDP4_XNODP * (SDP4_AODP * (1. + 1.5 * SDP4_ETASQ
					+ SDP4_EETA * (4. + SDP4_ETASQ))
					+ .75 * C1_CK2 * SDP4_TSI / SDP4_PSISQ * SDP4_X3THM1
					* (8. + 3. * SDP4_ETASQ * (8. + SDP4_ETASQ)));
			SDP4_C1 = E1_BSTAR * SDP4_C2;
			SDP4_SINIO = Math.sin(E1_XINCL);
			SDP4_A3OVK2 = -C1_XJ3 / C1_CK2 * C1_AE * C1_AE * C1_AE;
			SDP4_X1MTH2 = 1. - SDP4_THETA2;
			SDP4_C4 = 2. * SDP4_XNODP * SDP4_COEF1 * SDP4_AODP * SDP4_BETAO2
					* (SDP4_ETA * (2. + .5 * SDP4_ETASQ) + E1_EO * (.5 + 2. * SDP4_ETASQ)
							- 2. * C1_CK2 * SDP4_TSI / (SDP4_AODP * SDP4_PSISQ)
							* (-3. * SDP4_X3THM1 * (1. - 2. * SDP4_EETA + SDP4_ETASQ
									* (1.5 - .5 * SDP4_EETA)) + .75 * SDP4_X1MTH2
									* (2. * SDP4_ETASQ - SDP4_EETA * (1. + SDP4_ETASQ))
									* Math.cos(2. * E1_OMEGAO)));
			SDP4_THETA4 = SDP4_THETA2 * SDP4_THETA2;
			SDP4_TEMP1 = 3. * C1_CK2 * SDP4_PINVSQ * SDP4_XNODP;
			SDP4_TEMP2 = SDP4_TEMP1 * C1_CK2 * SDP4_PINVSQ;
			SDP4_TEMP3 = 1.25 * C1_CK4 * SDP4_PINVSQ * SDP4_PINVSQ * SDP4_XNODP;
			SDP4_XMDOT = SDP4_XNODP + .5 * SDP4_TEMP1 * SDP4_BETAO * SDP4_X3THM1
					+ .0625 * SDP4_TEMP2 * SDP4_BETAO
					* (13. - 78. * SDP4_THETA2 + 137. * SDP4_THETA4);
			SDP4_X1M5TH = 1. - 5. * SDP4_THETA2;
			SDP4_OMGDOT = -.5 * SDP4_TEMP1 * SDP4_X1M5TH
					+ .0625 * SDP4_TEMP2 * (7. - 114. * SDP4_THETA2 + 395. * SDP4_THETA4)
					+ SDP4_TEMP3 * (3. - 36. * SDP4_THETA2 + 49. * SDP4_THETA4);
			SDP4_XHDOT1 = -SDP4_TEMP1 * SDP4_COSIO;
			SDP4_XNODOT = SDP4_XHDOT1 + (.5 * SDP4_TEMP2 * (4. - 19. * SDP4_THETA2)
					+ 2. * SDP4_TEMP3 * (3. - 7. * SDP4_THETA2)) * SDP4_COSIO;
			SDP4_XNODCF = 3.5 * SDP4_BETAO2 * SDP4_XHDOT1 * SDP4_C1;
			SDP4_T2COF = 1.5 * SDP4_C1;
			SDP4_XLCOF = .125 * SDP4_A3OVK2 * SDP4_SINIO
					* (3. + 5. * SDP4_COSIO) / (1. + SDP4_COSIO);
			SDP4_AYCOF = .25 * SDP4_A3OVK2 * SDP4_SINIO;
			SDP4_X7THM1 = 7. * SDP4_THETA2 - 1.;
			IFLAG[0] = 0;
			DPINIT(SDP4_EOSQ, SDP4_SINIO, SDP4_COSIO, SDP4_BETAO, SDP4_AODP,
					SDP4_THETA2, SDP4_SING, SDP4_COSG, SDP4_BETAO2, SDP4_XMDOT,
					SDP4_OMGDOT, SDP4_XNODOT, SDP4_XNODP);
		}

		XMDF   = E1_XMO    + SDP4_XMDOT  * TSINCE[0];
		OMGADF = E1_OMEGAO + SDP4_OMGDOT * TSINCE[0];
		XNODDF = E1_XNODEO + SDP4_XNODOT * TSINCE[0];
		TSQ = TSINCE[0] * TSINCE[0];
		XNODE = XNODDF + SDP4_XNODCF * TSQ;
		TEMPA = 1. - SDP4_C1 * TSINCE[0];
		TEMPE = E1_BSTAR * SDP4_C4 * TSINCE[0];
		TEMPL = SDP4_T2COF * TSQ;
		XN = SDP4_XNODP;

		dpsec_args[0] = XMDF;
		dpsec_args[1] = OMGADF;
		dpsec_args[2] = XNODE;
		dpsec_args[3] = EM;
		dpsec_args[4] = XINC;
		dpsec_args[5] = XN;
		DPSEC(dpsec_args, TSINCE);
		XMDF   = dpsec_args[0];
		OMGADF = dpsec_args[1];
		XNODE  = dpsec_args[2];
		EM     = dpsec_args[3];
		XINC   = dpsec_args[4];
		XN     = dpsec_args[5];

		A = Math.pow(C1_XKE / XN, C1_TOTHRD) * TEMPA * TEMPA;
		E = EM - TEMPE;
		XMAM = XMDF + SDP4_XNODP * TEMPL;

		dpper_args[0] = E;
		dpper_args[1] = XINC;
		dpper_args[2] = OMGADF;
		dpper_args[3] = XNODE;
		dpper_args[4] = XMAM;
		DPPER(dpper_args);
		E      = dpper_args[0];
		XINC   = dpper_args[1];
		OMGADF = dpper_args[2];
		XNODE  = dpper_args[3];
		XMAM   = dpper_args[4];

		XL = XMAM + OMGADF + XNODE;
		BETA = Math.sqrt(1. - E * E);
		XN = C1_XKE / Math.pow(A, 1.5);

		AXN = E * Math.cos(OMGADF);
		TEMP = 1. / (A * BETA * BETA);
		XLL = TEMP * SDP4_XLCOF * AXN;
		AYNL = TEMP * SDP4_AYCOF;
		XLT = XL + XLL;
		AYN = E * Math.sin(OMGADF) + AYNL;

		CAPU = FMOD2P(XLT - XNODE);
		SDP4_TEMP2 = CAPU;
		for (I = 1; I < 11; I++) {
			SINEPW = Math.sin(SDP4_TEMP2);
			COSEPW = Math.cos(SDP4_TEMP2);
			SDP4_TEMP3 = AXN * SINEPW;
			TEMP4 = AYN * COSEPW;
			TEMP5 = AXN * COSEPW;
			TEMP6 = AYN * SINEPW;
			EPW = (CAPU - TEMP4 + SDP4_TEMP3 - SDP4_TEMP2)
					/ (1. - TEMP5 - TEMP6) + SDP4_TEMP2;
			if (Math.abs(EPW-SDP4_TEMP2) <= C1_E6A) break;
			SDP4_TEMP2 = EPW;
		}

		ECOSE = TEMP5 + TEMP6;
		ESINE = SDP4_TEMP3 - TEMP4;
		ELSQ = AXN * AXN + AYN * AYN;
		TEMP = 1. - ELSQ;
		PL = A * TEMP;
		R = A * (1. - ECOSE);
		SDP4_TEMP1 = 1. / R;
		RDOT = C1_XKE * Math.sqrt(A) * ESINE * SDP4_TEMP1;
		RFDOT = C1_XKE * Math.sqrt(PL) * SDP4_TEMP1;
		SDP4_TEMP2 = A * SDP4_TEMP1;
		BETAL = Math.sqrt(TEMP);
		SDP4_TEMP3 = 1. / (1. + BETAL);
		COSU = SDP4_TEMP2 * (COSEPW - AXN + AYN * ESINE * SDP4_TEMP3);
		SINU = SDP4_TEMP2 * (SINEPW - AYN - AXN * ESINE * SDP4_TEMP3);
		U = ACTAN(SINU, COSU);
		SIN2U =2. * SINU * COSU;
		COS2U =2. * COSU * COSU - 1.;
		TEMP = 1. / PL;
		SDP4_TEMP1 = C1_CK2 * TEMP;
		SDP4_TEMP2 = SDP4_TEMP1 * TEMP;

		RK = R * (1. - 1.5 * SDP4_TEMP2 * BETAL * SDP4_X3THM1)
				+ .5 * SDP4_TEMP1 * SDP4_X1MTH2 * COS2U;
		UK = U - .25 * SDP4_TEMP2 * SDP4_X7THM1 * SIN2U;
		XNODEK = XNODE + 1.5 * SDP4_TEMP2 * SDP4_COSIO * SIN2U;
		XINCK = XINC + 1.5 * SDP4_TEMP2 * SDP4_COSIO * SDP4_SINIO * COS2U;
		RDOTK = RDOT - XN * SDP4_TEMP1 * SDP4_X1MTH2 * SIN2U;
		RFDOTK = RFDOT + XN * SDP4_TEMP1
				* (SDP4_X1MTH2 * COS2U + 1.5 * SDP4_X3THM1);

		SINUK = Math.sin(UK);
		COSUK = Math.cos(UK);
		SINIK = Math.sin(XINCK);
		COSIK = Math.cos(XINCK);
		SINNOK = Math.sin(XNODEK);
		COSNOK = Math.cos(XNODEK);
		XMX = -SINNOK * COSIK;
		XMY =  COSNOK * COSIK;
		UX = XMX * SINUK + COSNOK * COSUK;
		UY = XMY * SINUK + SINNOK * COSUK;
		UZ = SINIK * SINUK;
		VX = XMX * COSUK - COSNOK * SINUK;
		VY = XMY * COSUK - SINNOK * SINUK;
		VZ = SINIK * COSUK;

		E1_X = RK * UX;
		E1_Y = RK * UY;
		E1_Z = RK * UZ;
		E1_XDOT = RDOTK * UX + RFDOTK * VX;
		E1_YDOT = RDOTK * UY + RFDOTK * VY;
		E1_ZDOT = RDOTK * UZ + RFDOTK * VZ;

		itsR[0] = E1_X    * C1_XKMPER / C1_AE/1E6;
		itsR[1] = E1_Y    * C1_XKMPER / C1_AE/1E6;
		itsR[2] = E1_Z    * C1_XKMPER / C1_AE/1E6;
		itsV[0] = E1_XDOT * C1_XKMPER / C1_AE * C1_XMNPDA / 86400.;
		itsV[1] = E1_YDOT * C1_XKMPER / C1_AE * C1_XMNPDA / 86400.;
		itsV[2] = E1_ZDOT * C1_XKMPER / C1_AE * C1_XMNPDA / 86400.;

		return itsR;
	}

	/**
	 * Calculates the Greenwich sidereal time. 
	 * */
	protected final double THETAG(double EP)
	{
		double D, THETA, TWOPI, YR, TEMP, value;
		int JY, N, I;
		TWOPI = 6.28318530717959;
		YR = (EP + 2.E-7) * 1.E-3;
		JY = (int)YR;
		YR = JY;
		D = EP - YR * 1.E3;
		if (JY < 57) JY = JY + 100;
		N = (JY - 69) / 4;
		if (JY < 70) N = (JY - 72) / 4;
		E1_DS50 = 7305. + 365. * (JY - 70) + N + D;
		THETA = 1.72944494 + 6.3003880987 * E1_DS50;
		TEMP = THETA / TWOPI;
		I = (int)TEMP;
		TEMP = I;
		value = THETA - TEMP * TWOPI;
		if (value < 0.) value = value + TWOPI;
		return value;
	}
	
	/**
   * Computes the horizontal spherical coordinates (azimuth and elevation).
   * @param direction Array containing azimuth (rad), elevation (rad)
   *   and the topocentric distance in Gm. 
   */
  public static void GetHori(double[] direction) {
    double t1[] = new double[3];
    double t2[] = new double[3];

    Mean2Topo(itsR, t1);
    Topo2Hori(t1, t2);
    Spher(t2, direction);
    direction[0] = NormAngle180(direction[0]);
  }

  /**
   * Set the station parameters.
   * @param usr_pos_geo User latitude, longitude and height
   */
  public static void SetGeodetic(double[] usr_pos_geo) {

    double theFactor;
    
    JD = SatelliteData.JD;

    /* Normalize longitude, set height in Gm */

    itsLong = NormAngle0(usr_pos_geo[1] / 180. * Math.PI);
    itsLat = usr_pos_geo[0] / 180. * Math.PI;
    itsHeight = usr_pos_geo[2] / 1E9;

    if (itsLat <= -Math.PI / 2.) {
      itsX = 0.;
      itsZ = +B;
    } else if (itsLat >= +Math.PI / 2.) {
      itsX = 0.;
      itsZ = -B;
    } else if (1E-6 > Math.abs(itsLat)) {
      itsX = A * Math.cos(itsLat);
      itsZ = A * Math.sin(itsLat);
    } else if (0. < itsLat) {
      theFactor = F / (F - 1.);
      theFactor /= Math.tan(itsLat);
      theFactor *= theFactor;
      itsX = itsHeight * Math.cos(itsLat) + A
          * Math.sqrt(1. - 1. / (1. + theFactor));
      itsZ = itsHeight * Math.sin(itsLat) + B
          * Math.sqrt(1. / (1. + theFactor));
    } else {
      theFactor = F / (F - 1.);
      theFactor /= Math.tan(itsLat);
      theFactor *= theFactor;
      itsX = itsHeight * Math.cos(itsLat) + A
          * Math.sqrt(1. - 1. / (1. + theFactor));
      itsZ = itsHeight * Math.sin(itsLat) - B
          * Math.sqrt(1. / (1. + theFactor));
    }
  }

  /**
   * Normalize an angle around zero.
   * @param angle The angle before normalization. 
   * */
  static public double NormAngle0(double angle) {
    double theAngle;
    theAngle = angle;
    while (theAngle <= -Math.PI)
      theAngle += 2. * Math.PI;
    while (theAngle > Math.PI)
      theAngle -= 2. * Math.PI;
    return theAngle;
  }

  /**
   * Convert J2000 to equinox of date coordinates.
   * @param jD      The equinox to which the returned coordinates should refer.
   * @param inPosVect x y z position in Gm.
   * @param outPosVect  x y z position in Gm.
   */
  protected final void J20002Mean(double jD, double inPosVect[], double outPosVect[]) 
  {
    double t, zeta, z, theta;

    t = (GetJulEpoch(jD) - 2000.) / 100;
    zeta = .6406161 * t + 8.39e-5 * t * t + 5e-6 * t * t * t;
    z = .6406161 * t + 3.041e-4 * t * t + 5.1e-6 * t * t * t;
    theta = .556753 * t - 1.185e-4 * t * t - 1.16e-5 * t * t * t;
    zeta /= 180. / Math.PI;
    z /= 180. / Math.PI;
    theta /= 180. / Math.PI;

    double mat[] = {
        +Math.cos(zeta) * Math.cos(theta) * Math.cos(z) - Math.sin(zeta)
        * Math.sin(z),
        -Math.sin(zeta) * Math.cos(theta) * Math.cos(z) - Math.cos(zeta)
        * Math.sin(z),
        -Math.sin(theta) * Math.cos(z),
        +Math.cos(zeta) * Math.cos(theta) * Math.sin(z) + Math.sin(zeta)
        * Math.cos(z),
        -Math.sin(zeta) * Math.cos(theta) * Math.sin(z) + Math.cos(zeta)
        * Math.cos(z), -Math.sin(theta) * Math.sin(z),
        +Math.cos(zeta) * Math.sin(theta), -Math.sin(zeta) * Math.sin(theta),
        +Math.cos(theta) };

    outPosVect[0] = mat[0] * inPosVect[0] + mat[1] * inPosVect[1] + mat[2]
        * inPosVect[2];
    outPosVect[1] = mat[3] * inPosVect[0] + mat[4] * inPosVect[1] + mat[5]
        * inPosVect[2];
    outPosVect[2] = mat[6] * inPosVect[0] + mat[7] * inPosVect[1] + mat[8]
        * inPosVect[2];

  }

  /**
   * Convert geocentric RA(right ascension)/Dec (declination) to topocentric 
   * HA (Hour Angle)/Dec.
   * @param inPosVector   x y z position in Gm.
   * @param outPosVector  x y z position in Gm
   */
  protected final static void Mean2Topo(double inPosVector[],
      double outPosVector[]) {
    double theLST;
    double mat[] = new double[4];
    double vec[] = new double[3];

    theLST = GetLST() * Math.PI / 12.;
    mat[0] = Math.cos(theLST);
    mat[1] = Math.sin(theLST);
    mat[2] = Math.sin(theLST);
    mat[3] = -Math.cos(theLST);

    GetX0Z(vec);
    outPosVector[0] = inPosVector[0] * mat[0] + inPosVector[1] * mat[1]
        - vec[0];
    outPosVector[1] = inPosVector[0] * mat[2] + inPosVector[1] * mat[3];
    outPosVector[2] = inPosVector[2] - vec[2];

  }

  /**
   * Convert HA/Dec to azimuth and elevation.
   * @param inPosVector   xyz position in Gm.
   * @param outPosVector  xyz position in Gm.
   */
  protected final static void Topo2Hori(double inPosVector[], double outPosVector[]) {
    double mat[] = new double[4];
    double theLat;

    theLat = itsLat;

    mat[0] = -Math.sin(theLat);
    mat[1] = Math.cos(theLat);
    mat[2] = Math.cos(theLat);
    mat[3] = Math.sin(theLat);

    outPosVector[0] = inPosVector[0] * mat[0] + inPosVector[2] * mat[1];
    outPosVector[1] = -inPosVector[1];
    outPosVector[2] = inPosVector[0] * mat[2] + inPosVector[2] * mat[3];
  }

  /**
   * Normalize an angle around 180 degrees.
   * @param angle The angle before normalization.
   * @return    Normalized angle.
   */
  static public double NormAngle180(double angle) {
    double theAngle;
    theAngle = NormAngle0(angle);
    if (0. > theAngle)
      theAngle += 2. * Math.PI;
    return theAngle;
  }

  /**
   * Convert orthogonal to spherical coordinates.
   * @param rect_coor Rectangular coordinates x, y, z.
   * @param spher_coord Spherical coordinates azimuth (rad), 
   * elevation (rad), and distance from origin (same unit as x, y, z).
   */
  static public void Spher(double rect_coor[], double spher_coord[]) {
    double xx, yy, zz;

    if (rect_coor[0] == 0. && rect_coor[1] == 0. && rect_coor[2] == 0.) {
      spher_coord[0] = 0;
      spher_coord[1] = 0;
      spher_coord[2] = 0;
      return;
    }

    spher_coord[2] = Math.sqrt(rect_coor[0] * rect_coor[0] + rect_coor[1] * rect_coor[1] + rect_coor[2]
        * rect_coor[2]);

    xx = rect_coor[0] / spher_coord[2];
    yy = rect_coor[1] / spher_coord[2];
    zz = rect_coor[2] / spher_coord[2];

    spher_coord[1] = Math.asin(zz);
    spher_coord[0] = Math.atan2(yy, xx);

    if (spher_coord[0] < 0.)
      spher_coord[0] += 2. * Math.PI;

    return;
  }

  /**
   * Return Greenwich Sidereal Time in hours.
   * The value is calculated based on the expression given by
   * USNO/RGO, 1990, <em>The Astronomical Almanach for the Year 1992</em>, 
   * U.S. Government Printing Office, Washington DC, Her Majesty's Stationery Office, London, p.B6,
   * but where only the fraction of a full rotation is carried forward in each step.
   */
  public final static double GetGST() {
    double hr, ts;
    double t1, t2, t3, t4;
    hr = GetUT();
    t1 = (Math.floor(JD + 0.5) - 0.5 - 1545.) / 36525.;
    t3 = -7.1759e-11 * t1 * t1 * t1;
    t2 = 24. * (t3 - Math.floor(t3));
    t3 = 1.07759e-6 * t1 * t1;
    t2 += 24. * (t3 - Math.floor(t3));
    t3 = 2.1390378e-3 * t1;
    t2 += 24. * (t3 - Math.floor(t3));
    t3 = 100. * t1;
    t2 += 24. * (t3 - Math.floor(t3));
    ts = 6.69737456 + t2 + 1.00273791 * hr;

    while (ts < 0.)
      ts += 24.;
    while (ts >= 24.)
      ts -= 24.;

    return ts;
  }

  /**
   * Returns Universal Time in hours.
   */
  public final static double GetUT() {
    double theTime;

    theTime = JD + 0.5;
    theTime = theTime - Math.floor(theTime);
    while (theTime < 0.)
      theTime++;
    while (theTime >= 1.)
      theTime--;
    theTime *= 24.;

    return theTime;
  }

  /**
   * Return local sidereal time in hours. 
   */
  public final static double GetLST() {

    double LST_time;

    LST_time = GetGST();
    LST_time += itsLong * 12. / Math.PI;

    while (0. > LST_time)
      LST_time += 24.;

    while (24. <= LST_time)
      LST_time -= 24.;

    return LST_time;
  }

  /**
   * Return the station rectangular position.
   * @param aTriplet
   *          The geocentric rectangular coordinates in the coordinate system
   *          where the x-z plane is the plane of the observatory's meridian.
   *          The second number is therefore always returned as zero.
   */
  public final static void GetX0Z(double aTriplet[]) {
    aTriplet[0] = itsX;
    aTriplet[1] = 0.;
    aTriplet[2] = itsZ;
    return;
  }

  /**
   * Returns the Julian Epoch in years.
   */
  public final static double GetJulEpoch(double itsJD) {
    return ((itsJD - 1545. + DeltaT(itsJD)) / 365.25 + 2000.);
  }

  /**
   * Returns TT (Terrestrial Time) - UT (Universal Time) in days.
   * TT has a fixed offset from TAI (Temps Atomique International).
   * TT = TAI + 32.184 s
   *
   **/
  protected final static double DeltaT(double itsJD) {

    double JulEpoch;
    double t;
    double D = 0; 

    JulEpoch = 2000. + (itsJD - 1545.) / 365.25;

    if (1987 <= JulEpoch && 2030 >= JulEpoch) {
      t = (JulEpoch - 2000.);
      D = 0.5 * t + 62.5;
      D /= 86400.;
    }

    return D;
  }

  /**
   * Set the mean equinox-of-date rectangular coordinates.
   * @param itsJD   The equinox to which the given coordinates refer.
   * @param aTriplet  The x, y and z coordinates.(normally in Gm)
   */
  protected static void SetMean(double itsJD, double aTriplet[]) {
    double t1[] = new double[3];
    int i;

    Mean2J2000(itsJD, aTriplet, t1);
    for (i = 0; i < 3; i++)
      itsR[i] = t1[i];
  }

  /**
   * Convert equinox of date to J2000 coordinates.
   */
  protected final static void Mean2J2000(double itsJD, double inTriplets[],
      double outTriplets[]) {
    double t, zeta, z, theta;

    t = (GetJulEpoch(itsJD) - 2000.) / 100.;
    zeta = .6406161 * t + 8.39e-5 * t * t + 5e-6 * t * t * t;
    z = .6406161 * t + 3.041e-4 * t * t + 5.1e-6 * t * t * t;
    theta = .556753 * t - 1.185e-4 * t * t - 1.16e-5 * t * t * t;
    zeta /= 180 / Math.PI;
    z /= 180 / Math.PI;
    theta /= 180 / Math.PI;

    double mat[] = {
        +Math.cos(zeta) * Math.cos(theta) * Math.cos(z) - Math.sin(zeta)
        * Math.sin(z),
        -Math.sin(zeta) * Math.cos(theta) * Math.cos(z) - Math.cos(zeta)
        * Math.sin(z),
        -Math.sin(theta) * Math.cos(z),
        +Math.cos(zeta) * Math.cos(theta) * Math.sin(z) + Math.sin(zeta)
        * Math.cos(z),
        -Math.sin(zeta) * Math.cos(theta) * Math.sin(z) + Math.cos(zeta)
        * Math.cos(z), -Math.sin(theta) * Math.sin(z),
        +Math.cos(zeta) * Math.sin(theta), -Math.sin(zeta) * Math.sin(theta),
        +Math.cos(theta) };

    outTriplets[0] = mat[0] * inTriplets[0] + mat[3] * inTriplets[1] + mat[6]
        * inTriplets[2];
    outTriplets[1] = mat[1] * inTriplets[0] + mat[4] * inTriplets[1] + mat[7]
        * inTriplets[2];
    outTriplets[2] = mat[2] * inTriplets[0] + mat[5] * inTriplets[1] + mat[8]
        * inTriplets[2];

  }
}

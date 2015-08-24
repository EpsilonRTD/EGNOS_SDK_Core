
////////////////////////////////////////////////////////////////////////////
//
//	GeoMag.java - originally geomag.c
//	Ported to Java 1.0.2 by Tim Walker	
//	tim.walker@worldnet.att.net
//	tim@acusat.com
//
//	Updated: 1/28/98
//
//	Original source geomag.c available at 
//	http://www.ngdc.noaa.gov/seg/potfld/DoDWMM.html
//
//	NOTE: original comments from the geomag.c source file are in ALL CAPS
//	Tim's added comments for the port to Java are not
//
////////////////////////////////////////////////////////////////////////////

package com.ec.egnossdk;
//import java.awt.Toolkit; aanagnostopoulos
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.StringTokenizer;

import android.content.Context;
import android.content.res.AssetManager;

/**
 *	This is a class to generate the magnetic declination,
 *	magnetic field strength and inclination for any point
 *	on the earth.  The true bearing = magnetic bearing + declination.
 * 	This class is adapted from an Applet from the NOAA National Data Center
 *	at http://www.ngdc.noaa.gov/seg/segd.shtml.  None of the calculations
 *	were changed.  This class requires an input file named WMM-95.DAT, which
 *	must be in the same directory that the application is run from.  
 *	NOTE: The original version of
 *	the applet, calculated the declination as a function of the date.
 *	This class assumes a date of 1 January 1999, using the 1995 geomagnetic
 *	data.  The WMM-95.DAT file will probably be updated by NOAA in Dec 1999.
 *	This version assumes the calculation is wanted for an altitude of sea level.
 *
 *	Using the correct date, the declination is accurate to about 0.5 degrees.
 *
 *  This is the LANL D-3 version of the GeoMagnetic calculator from
 *	the NOAA National Data Center at http://www.ngdc.noaa.gov/seg/segd.shtml.
 *	
 *	Adapted by John St. Ledger, Los Alamos National Laboratory
 *	June 25, 1999
 *
 *	Verified by comparison to the source code downloaded from the web.
 *	The declination calculated by that source is exactly the same as this class.
 *	The applet on the web calculates a declination several minutes 
 *	different than this class.  Suspect this is the difference between
 *	the DOD model used in this class and the IGRF model of the magnetic field.
 *	Does not return the grid variation near the poles.  These lines commented out, 
 *	and can easily be restored.
 *
 *	Version 2 Comments:  The world magnetic model is updated every 5 years.
 *	The data for 2000 uses the same algorithm to calculate the magnetic field
 *	field variables.  The only change is in the spherical harmonic coefficients
 *	in the input file.  The input file has been renamed to WMM.COF.  Once again,
 *	the date was fixed.  This time to January 1, 2001.  Also, a deprecated
 *	constructor for StreamTokenizer was replaced, and the error messages in the catch
 *	clause were changed.  Methods to get the field strength and inclination
 *	were added.
 *
 *
 *	@version 3.0
 *	January 19, 2000
 *	Updated for 2000 data.
 *
 *	@version 4.0
 *	March 1, 2002
 *	Changed so that if data file doesn't exist,
 *	it uses an internal version of the coefficients from
 *	the 2000 update.
 *
 *
 *	@version 5.0
 *	June 1, 2005
 *	Changed so that if data file doesn't exist,
 *	it uses an internal version of the coefficients from
 *	the 2005 update.  Previously, only calculated the declination at sea level
 *	for one date.  Now can return all of the variables as a function of date and
 *	altitude.  Original version used float variables.  All changed to type double.
 *	Does not check if the input date is within the valid block.
 *
 *	@version 5.1
 *	June 20, 2005
 *	Fixed a bug discoverd by Alvin Liem.  In my zeal to clean up compiler comments
 *	I deleted some double casts for integers that resulted in an integer division
 *	being made when double division was needed.
 *
 *     References:
 *
 *       JOHN M. QUINN, DAVID J. KERRIDGE AND DAVID R. BARRACLOUGH,
 *            WORLD MAGNETIC CHARTS FOR 1985 - SPHERICAL HARMONIC
 *            MODELS OF THE GEOMAGNETIC FIELD AND ITS SECULAR
 *            VARIATION, GEOPHYS. J. R. ASTR. SOC. (1986) 87,
 *            PP 1143-1157
 *
 *       DEFENSE MAPPING AGENCY TECHNICAL REPORT, TR 8350.2:
 *            DEPARTMENT OF DEFENSE WORLD GEODETIC SYSTEM 1984,
 *            SEPT. 30 (1987)
 *
 *       JOSEPH C. CAIN, ET AL.; A PROPOSED MODEL FOR THE
 *            INTERNATIONAL GEOMAGNETIC REFERENCE FIELD - 1965,
 *            J. GEOMAG. AND GEOELECT. VOL. 19, NO. 4, PP 335-355
 *            (1967) (SEE APPENDIX)
 *
 *       ALFRED J. ZMUDA, WORLD MAGNETIC SURVEY 1957-1969,
 *            INTERNATIONAL ASSOCIATION OF GEOMAGNETISM AND
 *            AERONOMY (IAGA) BULLETIN #28, PP 186-188 (1971)
 *
 *       JOHN M. QUINN, RACHEL J. COLEMAN, MICHAEL R. PECK, AND
 *            STEPHEN E. LAUBER; THE JOINT US/UK 1990 EPOCH
 *            WORLD MAGNETIC MODEL, TECHNICAL REPORT NO. 304,
 *            NAVAL OCEANOGRAPHIC OFFICE (1991)
 *
 *       JOHN M. QUINN, RACHEL J. COLEMAN, DONALD L. SHIEL, AND
 *            JOHN M. NIGRO; THE JOINT US/UK 1995 EPOCH WORLD
 *            MAGNETIC MODEL, TECHNICAL REPORT NO. 314, NAVAL
 *            OCEANOGRAPHIC OFFICE (1995)
 *
 *
 *
 *
 *     WMM-2000 is a National Imagery and Mapping Agency (NIMA) standard 
 *     product. It is covered under NIMA Military Specification: 
 *     MIL-W-89500 (1993).
 *
 *     For information on the use and applicability of this product contact
 *
 *                     DIRECTOR
 *                     NATIONAL IMAGERY AND MAPPING AGENCY/HEADQUARTERS
 *                     ATTN: CODE P33
 *                     12310 SUNRISE VALLEY DRIVE
 *                     RESTON, VA 20191-3449
 *                     (703) 264-3002
 *
 *
 *     The FORTRAN version of GEOMAG PROGRAMMED BY:
 *
 *                     JOHN M. QUINN  7/19/90
 *                     FLEET PRODUCTS DIVISION, CODE N342
 *                     NAVAL OCEANOGRAPHIC OFFICE (NAVOCEANO)
 *                     STENNIS SPACE CENTER (SSC), MS 39522-5001
 *                     USA
 *                     PHONE:   COM:  (601) 688-5828
 *                               AV:        485-5828
 *                              FAX:  (601) 688-5521
 *
 *     NOW AT:
 *
 *                     GEOMAGNETICS GROUP
 *                     U. S. GEOLOGICAL SURVEY   MS 966
 *                     FEDERAL CENTER
 *                     DENVER, CO   80225-0046
 *                     USA
 *                     PHONE:   COM: (303) 273-8475
 *                              FAX: (303) 273-8600
 *                     EMAIL:   quinn@ghtmail.cr.usgs.gov
 *
 *
 */
public class TSAGeoMag
{
    
    //variables for magnetic calculations ////////////////////////////////////
    //
    // Variables were identified in geomag.for, the FORTRAN
    // version of the geomag calculator.
    
    /**	The input string which contains each line of input for the
     *	wmm.cof input file.  Added so that all data was internal, so that 
     *	applications do not have to mess with carrying around a data file.
     *   In the TSAGeoMag Class, the columns in this file are as follows:
     *   n, m,      gnm,      hnm,       dgnm,      dhnm
     */
    private String [] input = 
    {    "    2005.0            WMM-2005        10/18/2004",
            "  1  0  -29556.8       0.0        8.0        0.0",
            "  1  1   -1671.7    5079.8       10.6      -20.9",
            "  2  0   -2340.6       0.0      -15.1        0.0",
            "  2  1    3046.9   -2594.7       -7.8      -23.2",
            "  2  2    1657.0    -516.7       -0.8      -14.6",
            "  3  0    1335.4       0.0        0.4        0.0",
            "  3  1   -2305.1    -199.9       -2.6        5.0",
            "  3  2    1246.7     269.3       -1.2       -7.0",
            "  3  3     674.0    -524.2       -6.5       -0.6",
            "  4  0     919.8       0.0       -2.5        0.0",
            "  4  1     798.1     281.5        2.8        2.2",
            "  4  2     211.3    -226.0       -7.0        1.6",
            "  4  3    -379.4     145.8        6.2        5.8",
            "  4  4     100.0    -304.7       -3.8        0.1",
            "  5  0    -227.4       0.0       -2.8        0.0",
            "  5  1     354.6      42.4        0.7        0.0",
            "  5  2     208.7     179.8       -3.2        1.7",
            "  5  3    -136.5    -123.0       -1.1        2.1",
            "  5  4    -168.3     -19.5        0.1        4.8",
            "  5  5     -14.1     103.6       -0.8       -1.1",
            "  6  0      73.2       0.0       -0.7        0.0",
            "  6  1      69.7     -20.3        0.4       -0.6",
            "  6  2      76.7      54.7       -0.3       -1.9",
            "  6  3    -151.2      63.6        2.3       -0.4",
            "  6  4     -14.9     -63.4       -2.1       -0.5",
            "  6  5      14.6      -0.1       -0.6       -0.3",
            "  6  6     -86.3      50.4        1.4        0.7",
            "  7  0      80.1       0.0        0.2        0.0",
            "  7  1     -74.5     -61.5       -0.1        0.6",
            "  7  2      -1.4     -22.4       -0.3        0.4",
            "  7  3      38.5       7.2        1.1        0.2",
            "  7  4      12.4      25.4        0.6        0.3",
            "  7  5       9.5      11.0        0.5       -0.8",
            "  7  6       5.7     -26.4       -0.4       -0.2",
            "  7  7       1.8      -5.1        0.6        0.1",
            "  8  0      24.9       0.0        0.1        0.0",
            "  8  1       7.7      11.2        0.3       -0.2",
            "  8  2     -11.6     -21.0       -0.4        0.1",
            "  8  3      -6.9       9.6        0.3        0.3",
            "  8  4     -18.2     -19.8       -0.3        0.4",
            "  8  5      10.0      16.1        0.2        0.1",
            "  8  6       9.2       7.7        0.4       -0.2",
            "  8  7     -11.6     -12.9       -0.7        0.4",
            "  8  8      -5.2      -0.2        0.4        0.4",
            "  9  0       5.6       0.0        0.0        0.0",
            "  9  1       9.9     -20.1        0.0        0.0",
            "  9  2       3.5      12.9        0.0        0.0",
            "  9  3      -7.0      12.6        0.0        0.0",
            "  9  4       5.1      -6.7        0.0        0.0",
            "  9  5     -10.8      -8.1        0.0        0.0",
            "  9  6      -1.3       8.0        0.0        0.0",
            "  9  7       8.8       2.9        0.0        0.0",
            "  9  8      -6.7      -7.9        0.0        0.0",
            "  9  9      -9.1       6.0        0.0        0.0",
            " 10  0      -2.3       0.0        0.0        0.0",
            " 10  1      -6.3       2.4        0.0        0.0",
            " 10  2       1.6       0.2        0.0        0.0",
            " 10  3      -2.6       4.4        0.0        0.0",
            " 10  4       0.0       4.8        0.0        0.0",
            " 10  5       3.1      -6.5        0.0        0.0",
            " 10  6       0.4      -1.1        0.0        0.0",
            " 10  7       2.1      -3.4        0.0        0.0",
            " 10  8       3.9      -0.8        0.0        0.0",
            " 10  9      -0.1      -2.3        0.0        0.0",
            " 10 10      -2.3      -7.9        0.0        0.0",
            " 11  0       2.8       0.0        0.0        0.0",
            " 11  1      -1.6       0.3        0.0        0.0",
            " 11  2      -1.7       1.2        0.0        0.0",
            " 11  3       1.7      -0.8        0.0        0.0",
            " 11  4      -0.1      -2.5        0.0        0.0",
            " 11  5       0.1       0.9        0.0        0.0",
            " 11  6      -0.7      -0.6        0.0        0.0",
            " 11  7       0.7      -2.7        0.0        0.0",
            " 11  8       1.8      -0.9        0.0        0.0",
            " 11  9       0.0      -1.3        0.0        0.0",
            " 11 10       1.1      -2.0        0.0        0.0",
            " 11 11       4.1      -1.2        0.0        0.0",
            " 12  0      -2.4       0.0        0.0        0.0",
            " 12  1      -0.4      -0.4        0.0        0.0",
            " 12  2       0.2       0.3        0.0        0.0",
            " 12  3       0.8       2.4        0.0        0.0",
            " 12  4      -0.3      -2.6        0.0        0.0",
            " 12  5       1.1       0.6        0.0        0.0",
            " 12  6      -0.5       0.3        0.0        0.0",
            " 12  7       0.4       0.0        0.0        0.0",
            " 12  8      -0.3       0.0        0.0        0.0",
            " 12  9      -0.3       0.3        0.0        0.0",
            " 12 10      -0.1      -0.9        0.0        0.0",
            " 12 11      -0.3      -0.4        0.0        0.0",
            " 12 12      -0.1       0.8        0.0        0.0",
            
    };
    
    /**
     *	Geodetic altitude in km. An input,
     *	but set to zero in this class.  Changed 
     *	back to an input in version 5.  If not specified,
     *	then is 0.
     */
    private double alt = 0;
    /**
     *	Geodetic latitude in deg.  An input.
     */
    private double glat = 0;
    /**
     *	Geodetic longitude in deg.  An input.
     */
    private double glon = 0;
    /**
     *	Time is decimal years.  An input
     */
    private double time = 0;
    /**
     *	Geomagnetic declination in deg.
     *	East is positive, West is negative.
     *	(The negative of variation.)
     */
    private double dec = 0;
    /**
     *	Geomagnetic inclination in deg.
     *	Down is positive, up is negative.
     */
    private double dip = 0;
    /**
     *	Geomagnetic total intensity, in nano Teslas.
     */
    private double ti = 0;
    /**
     *	Geomagnetic grid variation, referenced to
     *	grid North.  Not calculated or output in version 5.0.
     */
    //private double gv = 0;
    /**
     *	The maximum number of degrees of the spherical harmonic model.
     */
    private int maxdeg = 12;
    /**
     *	The maximum order of spherical harmonic model.
     */
    private int maxord;
    private int n,m,j,D1,D2,D3,D4; //icomp,i,
    //private boolean bOutDated = false;
    /**	
     *	An error flag to set whether the input file with the fit
     *	coefficients was found, and read OK.  If the input file is not found,
     *	or the data wasn't read,  
     *	it is true, and the declination, etc. is always returned as 0.0.
     *	In version 5, this is always false, so that a calculation is always
     *	performed.
     */
    private boolean inputError = false;
    
    /**	Added in version 5.  In earlier versions the date for the calculation was held as a
     *  constant.  In version 5, if no date is specified in the calulation, this date is used
     * 	by default.
     */
    private final double currentFixedDate = 2007;
    
    /**	Added in version 5.  In earlier versions the altitude for the calculation was held as a
     *  constant at 0.  In version 5, if no altitude is specified in the calculation, this
     * 	altitude is used by default.
     */
    private final double currentFixedAltitude = 0;
    
    /**
     *	The gauss coefficients of main geomagnetic model (nt).
     */
    private double c[][] = new double[13][13];
    /**
     *	The gauss coefficients of secular geomagnetic model (nt/yr).
     */
    private double cd[][] = new double[13][13];
    /**
     *	The time adjusted geomagnetic gauss coefficients (nt).
     */
    private double tc[][] = new double[13][13];
    /**
     *	The theta derivative of p(n,m) (unnormalized).
     */
    private double dp[][] = new double[13][13];
    /**
     *	The Schmidt normalization factors.
     */
    private double snorm[] = new double[169];
    /**
     *	The sine of (m*spherical coord. longitude).
     */
    private double sp[] = new double[13];
    /**
     *	The cosine of (m*spherical coord. longitude).
     */
    private double cp[] = new double[13];
    private double fn[] = new double[13];
    private double fm[] = new double[13];
    /**
     *	The associated legendre polynomials for m=1 (unnormalized).
     */
    private double pp[] = new double[13];
    private double k[][] = new double[13][13];
    private double pi,dtr,epoch,gnm,hnm,dgnm,dhnm,flnmj,otime,oalt,
    olat,olon,dt,rlon,rlat,srlon,srlat,crlon,crlat,srlat2,
    crlat2,q,q1,q2,ct,st,r2,r,d,ca,sa,aor,ar,br,bt,bp,bpp,
    par,temp1,temp2,parp,bx,by,bz,bh, a,b,re ,a2,b2,c2,a4,b4, c4;
    /**
     *	The context of NMEARTCMActivity
     */
    Context context;
    //
    ////////////////////////////////////////////////////////////////////////////
    
    /**
     *	Instantiates object by calling InitModel().
     
     */
    public TSAGeoMag(Context context)
    {
        this.context = context;
        //read model data from file and initialize the GeoMag routine
        InitModel();
    }
    
    
    
    /**
     *
     *	InitModel reads data from file and initializes magnetic model.  If
     *	the file is not present, or an IO exception occurs, then the internal
     *	values valid for 2005 will be used.
     *
     */
    private void InitModel()
    {
        glat = 0;
        glon = 0;
        //bOutDated = false;
        //String strModel = new String();
        //String strFile = new String("WMM.COF");
        //		String strFile = new String("wmm-95.dat");
        
        // INITIALIZE CONSTANTS
        maxord = maxdeg;
        sp[0] = 0.0;
        cp[0] = snorm[0] = pp[0] = 1.0;
        dp[0][0] = 0.0;
        /**
         *	Semi-major axis of WGS-84 ellipsoid, in km.
         */
        a = 6378.137;
        /**
         *	Semi-minor axis of WGS-84 ellipsoid, in km.
         */
        b = 6356.7523142;
        /**
         *	Mean radius of IAU-66 ellipsoid, in km.
         */
        re = 6371.2;
        a2 = a * a;
        b2 = b * b;
        c2 = a2 - b2;
        a4 = a2 * a2;
        b4 = b2 * b2;
        c4 = a4 - b4;
        
        
        try{
            //open data file and parse values
            //InputStream is;
            Reader is;
            
            //			if(!m_fStandAlone)
            //			{
            //				URL tmpurl = new URL(getDocumentBase(), strFile);
            //				//outputArea.setText(tmpurl.toString());
            //				is = tmpurl.openStream();
            //			}
            //			else
            //			{
            //is = new FileInputStream(strFile);
           // is = new InputStreamReader(new FileInputStream("WMM.COF") );
            
            AssetManager assetManager = context.getAssets();
            InputStream isFromWWM;
            isFromWWM = assetManager.open("WMM.COF");

  
            //			}
            // Warning: This was a deprecated constructor when 'is' was a
            // FileInputStream.
            StreamTokenizer str = new StreamTokenizer(isFromWWM);
            
            
            // READ WORLD MAGNETIC MODEL SPHERICAL HARMONIC COEFFICIENTS 
            c[0][0] = 0.0;
            cd[0][0] = 0.0;
            str.nextToken();
            epoch = str.nval;
            // System.out.println("Epoch is: " + epoch);
            str.nextToken();
            //strModel = str.sval;
            str.nextToken();
            
            //loop to get data from file
            while(true){
                str.nextToken();
                if(str.nval == 9999)
                    break;
                
                n = (int)str.nval;
                str.nextToken();
                m = (int)str.nval;
                str.nextToken();
                gnm = str.nval;
                str.nextToken();
                hnm = str.nval;
                str.nextToken();
                dgnm = str.nval;
                str.nextToken();
                dhnm = str.nval;
                
                if (m <= n)
                {
                    c[m][n] = gnm;
                    cd[m][n] = dgnm;
                    
                    if (m != 0)
                    {
                        c[n][m-1] = hnm;
                        cd[n][m-1] = dhnm;
                    }
                }
                
            }	//while(true)
            
            // moved to after exception catch
            // CONVERT SCHMIDT NORMALIZED GAUSS COEFFICIENTS TO UNNORMALIZED
            /*snorm[0] = 1.0;
             for (n = 1; n <= maxord; n++){
             
             snorm[n] = snorm[n - 1] * (2 * n - 1) / n;
             j = 2;
             
             for(m = 0,D1 = 1,D2 = (n - m + D1) / D1; D2 > 0; D2--, m += D1){
             k[m][n] = (((n - 1) * (n - 1))-(m * m))/((2 * n-1)*(2*n-3));
             if(m > 0){
             flnmj = ((n - m + 1) * j) / (n + m);
             snorm[n + m * 13] = snorm[n + (m -1 ) * 13] * Math.sqrt(flnmj);
             j = 1;
             c[n][m-1] = snorm[n + m * 13] * c[n][m-1];
             cd[n][m-1] = snorm[n + m * 13] * cd[n][m-1];
             }
             c[m][n] = snorm[n + m * 13] * c[m][n];
             cd[m][n] = snorm[n + m * 13] * cd[m][n];
             }	//for(m...)
             
             fn[n] = (n+1);
             fm[n] = n;
             
             }	//for(n...)
             
             k[1][1] = 0.0;
             
             otime = oalt = olat = olon = -1000.0;*/
            
            isFromWWM.close();
        }	//try
        // version 2, catch FileNotFound and IO exceptions separately, 
        // rather than catching all exceptions.	  
        catch(FileNotFoundException e){
            String message = new String(e.toString());
            //			outputArea.setText(message);
            //			System.err.println("Error:  " + message);
            
            System.out.println("\nNOTICE      NOTICE      NOTICE      ");
            System.out.println("Error:  " + message);
            System.out.println("Error in TSAGeoMag.InitModel()");
            System.out.println("The input file WMM.COF was not found in the same");
            System.out.println("directory as the application.");
            System.out.println("The magnetic field components are set to internal values.");
//            Toolkit.getDefaultToolkit().beep(); aanagnostopoulos
            setCoeff();
            inputError = false;
        }
        catch(IOException e){
            String message = new String(e.toString());
            System.out.println("\nNOTICE      NOTICE      NOTICE      ");
            System.out.println("Error:  " + message);
            System.out.println("Error in TSAGeoMag.InitModel()");
            System.out.println("The input file WMM.COF was found, but there was a problem ");
            System.out.println("reading the data.");
            System.out.println("The magnetic field components are set to internal values.");
//            Toolkit.getDefaultToolkit().beep(); aanagnostopoulos
            setCoeff();
            inputError = false;
        }
        // CONVERT SCHMIDT NORMALIZED GAUSS COEFFICIENTS TO UNNORMALIZED
        snorm[0] = 1.0;
        for (n = 1; n <= maxord; n++){
            
            snorm[n] = snorm[n - 1] * (2 * n - 1) / n;
            j = 2;
            
            for(m = 0,D1 = 1,D2 = (n - m + D1) / D1; D2 > 0; D2--, m += D1){
                 k[m][n] = (double) (((n - 1) * (n - 1))-(m * m))/(double) ((2 * n-1)*(2*n-3));
                if(m > 0){
                    flnmj = ((n - m + 1) * j) / (double) (n + m);
                    snorm[n + m * 13] = snorm[n + (m -1 ) * 13] * Math.sqrt(flnmj);
                    j = 1;
                    c[n][m-1] = snorm[n + m * 13] * c[n][m-1];
                    cd[n][m-1] = snorm[n + m * 13] * cd[n][m-1];
                }
                c[m][n] = snorm[n + m * 13] * c[m][n];
                cd[m][n] = snorm[n + m * 13] * cd[m][n];
            }	//for(m...)
            
            fn[n] = (n+1);
            fm[n] = n;
            
        }	//for(n...)
        
        k[1][1] = 0.0;
        
        otime = oalt = olat = olon = -1000.0;
        
        
    }
    
    /**     PURPOSE:  THIS ROUTINE COMPUTES THE DECLINATION (DEC),
     *               INCLINATION (DIP), TOTAL INTENSITY (TI) AND
     *               GRID VARIATION (GV - POLAR REGIONS ONLY, REFERENCED
     *               TO GRID NORTH OF POLAR STEREOGRAPHIC PROJECTION) OF
     *               THE EARTH'S MAGNETIC FIELD IN GEODETIC COORDINATES
     *               FROM THE COEFFICIENTS OF THE CURRENT OFFICIAL
     *               DEPARTMENT OF DEFENSE (DOD) SPHERICAL HARMONIC WORLD
     *               MAGNETIC MODEL (WMM-2000).  THE WMM SERIES OF MODELS IS
     *               UPDATED EVERY 5 YEARS ON JANUARY 1'ST OF THOSE YEARS
     *               WHICH ARE DIVISIBLE BY 5 (I.E. 1980, 1985, 1990 ETC.)
     *               BY THE NAVAL OCEANOGRAPHIC OFFICE IN COOPERATION
     *               WITH THE BRITISH GEOLOGICAL SURVEY (BGS).  THE MODEL
     *               IS BASED ON GEOMAGNETIC SURVEY MEASUREMENTS FROM
     *               AIRCRAFT, SATELLITE AND GEOMAGNETIC OBSERVATORIES.
     *
     *    
     *
     *     ACCURACY:  IN OCEAN AREAS AT THE EARTH'S SURFACE OVER THE
     *                ENTIRE 5 YEAR LIFE OF A DEGREE AND ORDER 12
     *                SPHERICAL HARMONIC MODEL SUCH AS WMM-95, THE ESTIMATED
     *                RMS ERRORS FOR THE VARIOUS MAGENTIC COMPONENTS ARE:
     *
     *                DEC  -   0.5 Degrees
     *                DIP  -   0.5 Degrees
     *                TI   - 280.0 nanoTeslas (nT)
     *                GV   -   0.5 Degrees
     *
     *                OTHER MAGNETIC COMPONENTS THAT CAN BE DERIVED FROM
     *                THESE FOUR BY SIMPLE TRIGONOMETRIC RELATIONS WILL
     *                HAVE THE FOLLOWING APPROXIMATE ERRORS OVER OCEAN AREAS:
     *
     *                X    - 140 nT (North)
     *                Y    - 140 nT (East)
     *                Z    - 200 nT (Vertical)  Positive is down
     *                H    - 200 nT (Horizontal)
     *
     *                OVER LAND THE RMS ERRORS ARE EXPECTED TO BE SOMEWHAT
     *                HIGHER, ALTHOUGH THE RMS ERRORS FOR DEC, DIP AND GV
     *                ARE STILL ESTIMATED TO BE LESS THAN 0.5 DEGREE, FOR
     *                THE ENTIRE 5-YEAR LIFE OF THE MODEL AT THE EARTH's
     *                SURFACE.  THE OTHER COMPONENT ERRORS OVER LAND ARE
     *                MORE DIFFICULT TO ESTIMATE AND SO ARE NOT GIVEN.
     *
     *                THE ACCURACY AT ANY GIVEN TIME OF ALL FOUR
     *                GEOMAGNETIC PARAMETERS DEPENDS ON THE GEOMAGNETIC
     *                LATITUDE.  THE ERRORS ARE LEAST AT THE EQUATOR AND
     *                GREATEST AT THE MAGNETIC POLES.
     *
     *                IT IS VERY IMPORTANT TO NOTE THAT A DEGREE AND
     *                ORDER 12 MODEL, SUCH AS WMM-2000 DESCRIBES ONLY
     *                THE LONG WAVELENGTH SPATIAL MAGNETIC FLUCTUATIONS
     *                DUE TO EARTH'S CORE.  NOT INCLUDED IN THE WMM SERIES
     *                MODELS ARE INTERMEDIATE AND SHORT WAVELENGTH
     *                SPATIAL FLUCTUATIONS OF THE GEOMAGNETIC FIELD
     *                WHICH ORIGINATE IN THE EARTH'S MANTLE AND CRUST.
     *                CONSEQUENTLY, ISOLATED ANGULAR ERRORS AT VARIOUS
     *                POSITIONS ON THE SURFACE (PRIMARILY OVER LAND, IN
     *                CONTINENTAL MARGINS AND OVER OCEANIC SEAMOUNTS,
     *                RIDGES AND TRENCHES) OF SEVERAL DEGREES MAY BE
     *                EXPECTED. ALSO NOT INCLUDED IN THE MODEL ARE
     *                NONSECULAR TEMPORAL FLUCTUATIONS OF THE GEOMAGNETIC
     *                FIELD OF MAGNETOSPHERIC AND IONOSPHERIC ORIGIN.
     *                DURING MAGNETIC STORMS, TEMPORAL FLUCTUATIONS CAN
     *                CAUSE SUBSTANTIAL DEVIATIONS OF THE GEOMAGNETIC
     *                FIELD FROM MODEL VALUES.  IN ARCTIC AND ANTARCTIC
     *                REGIONS, AS WELL AS IN EQUATORIAL REGIONS, DEVIATIONS
     *                FROM MODEL VALUES ARE BOTH FREQUENT AND PERSISTENT.
     *
     *                IF THE REQUIRED DECLINATION ACCURACY IS MORE
     *                STRINGENT THAN THE WMM SERIES OF MODELS PROVIDE, THEN
     *                THE USER IS ADVISED TO REQUEST SPECIAL (REGIONAL OR
     *                LOCAL) SURVEYS BE PERFORMED AND MODELS PREPARED BY
     *                THE USGS, WHICH OPERATES THE US GEOMAGNETIC
     *                OBSERVATORIES.  REQUESTS OF THIS NATURE SHOULD
     *                BE MADE THROUGH NIMA AT THE ADDRESS ABOVE.
     *
     *
     *
     *     NOTE:  THIS VERSION OF GEOMAG USES THE WMM-2005 GEOMAGNETIC
     *            MODEL REFERENCED TO THE WGS-84 GRAVITY MODEL ELLIPSOID
     *
     *	@param	fLat			The latitude in decimal degrees.
     *	@param	fLon			The longitude in decimal degrees.
     *	@param	year			The date as a decimal year.
     *	@param	altitude		The altitude in kilometers.
     */
    //	void CalcGeoMag(double fLat, double fLon, double fAlt, double fTime, boolean bCurrent)
    private void calcGeoMag(double fLat, double fLon, double year, double altitude)
    {		
        
        glat =  fLat;
        glon =  fLon;
        //alt = fAlt;
        alt = altitude;
        //time = fTime;
        /**
         *	The date in decimal years for calculating the magnetic field components.
         */
        time = year;
        
        
//        dt=0;
        dt = time - epoch;
        //if (otime < 0.0 && (dt < 0.0 || dt > 5.0))
        //		if(bCurrent){
        //			if (dt < 0.0 || dt > 5.0) 
        //				bOutDated = true;
        //			else
        //				bOutDated = false;
        //		}
        
        pi =  3.14159265359;		// could be replaced by Math.PI
        dtr = (pi/180.0);
        rlon = glon * dtr;
        rlat = glat * dtr;
        srlon = Math.sin(rlon);
        srlat = Math.sin(rlat);
        crlon = Math.cos(rlon);
        crlat = Math.cos(rlat);
        srlat2 = srlat * srlat;
        crlat2 = crlat * crlat;
        sp[1] = srlon;
        cp[1] = crlon;
        
        // CONVERT FROM GEODETIC COORDS. TO SPHERICAL COORDS.
        if (alt != oalt || glat != olat){
            q = Math.sqrt(a2 - c2 * srlat2);
            q1 = alt * q;
            q2 = ((q1 + a2) / (q1 + b2)) * ((q1 + a2) / (q1 + b2));
            ct = srlat / Math.sqrt(q2 * crlat2 + srlat2);
            st = Math.sqrt(1.0 - (ct * ct));
            r2 = ((alt*alt) + 2.0 * q1 + (a4 - c4 * srlat2) / (q * q));
            r = Math.sqrt(r2);
            d = Math.sqrt(a2 * crlat2 + b2 * srlat2);
            ca = (alt + d) / r;
            sa = c2 * crlat * srlat / (r * d);
        }
        if (glon != olon){
            for (m = 2; m <= maxord; m++){
                sp[m] = sp[1] * cp[m-1] + cp[1] * sp[m-1];
                cp[m] = cp[1] * cp[m-1] - sp[1] * sp[m-1];
            }
        }
        aor = re / r;
        ar = aor * aor;
        br = bt = bp = bpp = 0.0;
        
        for(n = 1; n <= maxord; n++){
            ar = ar * aor;
            for (m = 0,D3 = 1,D4 = (n + m + D3) / D3; D4 > 0; D4--,m += D3){
                
                //COMPUTE UNNORMALIZED ASSOCIATED LEGENDRE POLYNOMIALS
                //AND DERIVATIVES VIA RECURSION RELATIONS
                if(alt != oalt || glat != olat){
                    if(n == m){
                        snorm[n + m * 13] = st * snorm[n - 1 + (m - 1) * 13];				  
                        dp[m][n] = st * dp[m-1][n-1]+ ct* snorm[n - 1 + (m - 1) * 13];
                    }
                    if(n == 1 && m == 0){
                        snorm[n + m * 13] = ct * snorm[n - 1 + m * 13];
                        dp[m][n] = ct * dp[m][n - 1] - st * snorm[n - 1 + m * 13];
                    }
                    if(n > 1 && n != m){
                        if(m > n - 2) 
                            snorm[n - 2 + m * 13] = 0.0;
                        if(m > n - 2)
                            dp[m][n - 2] = 0.0;
                        snorm[n + m * 13] = ct * snorm[n - 1 + m * 13] - k[m][n] * snorm[n - 2 + m * 13];
                        dp[m][n] = ct * dp[m][n - 1] - st * snorm[n - 1 + m * 13] - k[m][n] * dp[m][n - 2];
                    }
                }
                
                //TIME ADJUST THE GAUSS COEFFICIENTS
                
                if(time != otime){
                    tc[m][n] = c[m][n] + dt * cd[m][n];
                    
                    if(m != 0)
                        tc[n][m - 1] = c[n][m - 1]+ dt * cd[n][m - 1];
                }
                
                //ACCUMULATE TERMS OF THE SPHERICAL HARMONIC EXPANSIONS
                
                par = ar * snorm[ n + m * 13];
                if(m == 0){
                    temp1 = tc[m][n] * cp[m];
                    temp2 = tc[m][n] * sp[m];
                }
                else{
                    temp1 = tc[m][n] * cp[m] + tc[n][m - 1] * sp[m];
                    temp2 = tc[m][n] * sp[m] - tc[n][m - 1] * cp[m];
                }
                
                bt = bt - ar * temp1 * dp[m][n];
                bp += (fm[m] * temp2 * par);
                br += (fn[n] * temp1 * par);
                
                //SPECIAL CASE:  NORTH/SOUTH GEOGRAPHIC POLES
                
                if(st == 0.0 && m == 1){
                    if(n == 1)
                        pp[n] = pp[n - 1];
                    else 
                        pp[n] = ct * pp[n - 1] - k[m][n] * pp[n - 2];
                    parp = ar * pp[n];
                    bpp += (fm[m] * temp2 * parp);
                }
                
            }	//for(m...)
            
        }	//for(n...)
        
        
        if(st == 0.0)
            bp = bpp;
        else 
            bp /= st;
        
        //ROTATE MAGNETIC VECTOR COMPONENTS FROM SPHERICAL TO
        //GEODETIC COORDINATES
        // bx must be the east-west field component
        // by must be the north-south field component
        // bx must be the vertical field component.
        bx = -bt * ca - br * sa;
        by = bp;
        bz = bt * sa - br * ca;
        
        //COMPUTE DECLINATION (DEC), INCLINATION (DIP) AND
        //TOTAL INTENSITY (TI)
        
        bh = Math.sqrt((bx * bx)+(by * by));
        ti = Math.sqrt((bh * bh)+(bz * bz));
        //	Calculate the declination.
        dec = (Math.atan2(by, bx) / dtr);
        //System.out.println( "Dec is: " + dec );
        dip = (Math.atan2(bz, bh) / dtr);
        
        //	This is the variation for grid navigation.
        //	Not used at this time.  See St. Ledger for explanation.
        //COMPUTE MAGNETIC GRID VARIATION IF THE CURRENT
        //GEODETIC POSITION IS IN THE ARCTIC OR ANTARCTIC
        //(I.E. GLAT > +55 DEGREES OR GLAT < -55 DEGREES)
        // Grid North is referenced to the 0 Meridian of a polar
        // stereographic projection.
        
        //OTHERWISE, SET MAGNETIC GRID VARIATION TO -999.0
        /*
         gv = -999.0;
         if (Math.abs(glat) >= 55.){
         if (glat > 0.0 && glon >= 0.0) 
         gv = dec-glon;
         if (glat > 0.0 && glon < 0.0) 
         gv = dec + Math.abs(glon);
         if (glat < 0.0 && glon >= 0.0) 
         gv = dec+glon;
         if (glat < 0.0 && glon < 0.0) 
         gv = dec - Math.abs(glon);
         if (gv > +180.0) 
         gv -= 360.0;
         if (gv < -180.0) 
         gv += 360.0;
         }
         */
        otime = time;
        oalt = alt;
        olat = glat;
        olon = glon;
        
    }
    
    
    /**
     *	Returns the declination from the 2005 Department of
     *	Defense geomagnetic model and data, in degrees.  The
     *	magnetic heading + declination = true heading.
     * 	(True heading + variation = magnetic heading.)
     *
     *	@param	dlat		Latitude in decimal degrees.
     *	@param 	dlong	Longitude in decimal degrees.
     */
    public double getDeclination( double dlat, double dlong )
    {
        if( inputError)
        {	
            return 0.0;
        }
        else
        {
            calcGeoMag( dlat, dlong, currentFixedDate, currentFixedAltitude );
            return  dec;
        }
    }
    
    
    
    /**
     *	Returns the declination from the 2005 Department of
     *	Defense geomagnetic model and data, in degrees.  The
     *	magnetic heading + declination = true heading.
     * 	(True heading + variation = magnetic heading.)
     *
     *	@param	dlat			Latitude in decimal degrees.
     *	@param 	dlong		Longitude in decimal degrees.
     *	@param	year			The date as a decimial year.
     *	@param	altitude		The altitude in kilometers.
     */
    public double getDeclination( double dlat, double dlong, double year, double altitude )
    {
        if( inputError)
        {	
            return 0.0;
        }
        else
        {
            calcGeoMag( dlat, dlong, year, altitude );
            return  dec;
        }
    }
    
    
    /**
     *	Returns the magnetic field intensity from the 2005
     *	Department of Defense geomagnetic model and data
     *	in nano Tesla.
     *
     *	@param	dlat		Latitude in decimal degrees.
     *	@param 	dlong	Longitude in decimal degrees.
     */
    public double getIntensity( double dlat, double dlong )
    {
        if( inputError)
        {	
            return 0.0;
        }
        else
        {
            calcGeoMag( dlat, dlong, currentFixedDate, currentFixedAltitude );
            return  ti;
        }
    }
    
    /**
     *	Returns the magnetic field intensity from the 2005 
     *	Department of Defense geomagnetic model and data
     *	in nano Tesla.
     *
     *	@param	dlat	 		Latitude in decimal degrees.
     *	@param 	dlong		Longitude in decimal degrees.
     *	@param	year			Date of the calculation in decimal years.
     *	@param	altitude		Altitude of the calculation in kilometers.
     */
    public double getIntensity( double dlat, double dlong, double year, double altitude )
    {
        if( inputError)
        {	
            return 0.0;
        }
        else
        {
            calcGeoMag( dlat, dlong, year, altitude );
            return  ti;
        }
    }
    /**
     *	Returns the horizontal magnetic field intensity from the 2005
     *	Department of Defense geomagnetic model and data
     *	in nano Tesla.
     *
     *	@param	dlat		Latitude in decimal degrees.
     *	@param 	dlong	Longitude in decimal degrees.
     */
    public double getHorizontalIntensity( double dlat, double dlong )
    {
        if( inputError)
        {	
            return 0.0;
        }
        else
        {
            calcGeoMag( dlat, dlong, currentFixedDate, currentFixedAltitude );
            return  bh;
        }
    }
    
    /**
     *	Returns the horizontal magnetic field intensity from the 2005 
     *	Department of Defense geomagnetic model and data
     *	in nano Tesla.
     *
     *	@param	dlat	 		Latitude in decimal degrees.
     *	@param 	dlong		Longitude in decimal degrees.
     *	@param	year			Date of the calculation in decimal years.
     *	@param	altitude		Altitude of the calculation in kilometers.
     */
    public double getHorizontalIntensity( double dlat, double dlong, double year, double altitude )
    {
        if( inputError)
        {	
            return 0.0;
        }
        else
        {
            calcGeoMag( dlat, dlong, year, altitude );
            return  bh;
        }
    }
    /**
     *	Returns the vertical magnetic field intensity from the 2005
     *	Department of Defense geomagnetic model and data
     *	in nano Tesla.
     *
     *	@param	dlat		Latitude in decimal degrees.
     *	@param 	dlong	Longitude in decimal degrees.
     */
    public double getVerticalIntensity( double dlat, double dlong )
    {
        if( inputError)
        {	
            return 0.0;
        }
        else
        {
            calcGeoMag( dlat, dlong, currentFixedDate, currentFixedAltitude );
            return  bz;
        }
    }
    
    /**
     *	Returns the vertical magnetic field intensity from the 2005 
     *	Department of Defense geomagnetic model and data
     *	in nano Tesla.
     *
     *	@param	dlat	 		Latitude in decimal degrees.
     *	@param 	dlong		Longitude in decimal degrees.
     *	@param	year			Date of the calculation in decimal years.
     *	@param	altitude		Altitude of the calculation in kilometers.
     */
    public double getVerticalIntensity( double dlat, double dlong, double year, double altitude )
    {
        if( inputError)
        {	
            return 0.0;
        }
        else
        {
            calcGeoMag( dlat, dlong, year, altitude );
            return  bz;
        }
    }
    /**
     *	Returns the northerly magnetic field intensity from the 2005
     *	Department of Defense geomagnetic model and data
     *	in nano Tesla.
     *
     *	@param	dlat		Latitude in decimal degrees.
     *	@param 	dlong	Longitude in decimal degrees.
     */
    public double getNorthIntensity( double dlat, double dlong )
    {
        if( inputError)
        {	
            return 0.0;
        }
        else
        {
            calcGeoMag( dlat, dlong, currentFixedDate, currentFixedAltitude );
            return  by;
        }
    }
    
    /**
     *	Returns the northerly magnetic field intensity from the 2005 
     *	Department of Defense geomagnetic model and data
     *	in nano Tesla.
     *
     *	@param	dlat	 		Latitude in decimal degrees.
     *	@param 	dlong		Longitude in decimal degrees.
     *	@param	year			Date of the calculation in decimal years.
     *	@param	altitude		Altitude of the calculation in kilometers.
     */
    public double getNorthIntensity( double dlat, double dlong, double year, double altitude )
    {
        if( inputError)
        {	
            return 0.0;
        }
        else
        {
            calcGeoMag( dlat, dlong, year, altitude );
            return  by;
        }
    }
    /**
     *	Returns the easterly magnetic field intensity from the 2005
     *	Department of Defense geomagnetic model and data
     *	in nano Tesla.
     *
     *	@param	dlat		Latitude in decimal degrees.
     *	@param 	dlong	Longitude in decimal degrees.
     */
    public double getEastIntensity( double dlat, double dlong )
    {
        if( inputError)
        {	
            return 0.0;
        }
        else
        {
            calcGeoMag( dlat, dlong, currentFixedDate, currentFixedAltitude );
            return  bx;
        }
    }
    
    /**
     *	Returns the easterly magnetic field intensity from the 2005 
     *	Department of Defense geomagnetic model and data
     *	in nano Tesla.
     *
     *	@param	dlat	 		Latitude in decimal degrees.
     *	@param 	dlong		Longitude in decimal degrees.
     *	@param	year			Date of the calculation in decimal years.
     *	@param	altitude		Altitude of the calculation in kilometers.
     */
    public double getEastIntensity( double dlat, double dlong, double year, double altitude )
    {
        if( inputError)
        {	
            return 0.0;
        }
        else
        {
            calcGeoMag( dlat, dlong, year, altitude );
            return  bx;
        }
    }
    /**
     *	Returns the magnetic field dip angle from the 2000 
     *	Department of Defense geomagnetic model and data,
     *	in degrees.  Uses default date (2007) and altitude (0).
     *
     *	@param	dlat		Latitude in decimal degrees.
     *	@param 	dlong	Longitude in decimal degrees.
     */
    public double getDip( double dlat, double dlong )
    {
        if( inputError)
        {	
            return 0.0;
        }
        else
        {
            calcGeoMag( dlat, dlong, currentFixedDate, currentFixedAltitude );
            return  dip;
        }
    }
    
    /**
     *	Returns the magnetic field dip angle from the 2000 
     *	Department of Defense geomagnetic model and data,
     *	in degrees.  Uses default date (2007) and altitude (0).
     *
     *	@param	dlat			Latitude in decimal degrees.
     *	@param 	dlong		Longitude in decimal degrees.
     *	@param	year			Date of the calculation in decimal years.
     *	@param	altitude		Altitude of the calculation in kilometers.
     */
    public double getDip( double dlat, double dlong, double year, double altitude )
    {
        if( inputError)
        {	
            return 0.0;
        }
        else
        {
            calcGeoMag( dlat, dlong, year, altitude );
            return  dip;
        }
    }
    
    /**	This method sets the input data to the internal 2005 coefficents.
     *	If there is an exception reading the input file WMM.COF, these values 
     *	are used.
     */
    private void setCoeff()
    {
        
        c[0][0] = 0.0;
        cd[0][0] = 0.0;
        StringTokenizer parser = new StringTokenizer(input[0]);
        
        epoch =  Double.valueOf(parser.nextToken()).doubleValue();
        System.out.println("Epoch is: " + epoch);
        String strModel = parser.nextToken();
        System.out.println("Model is: " + strModel);
        //loop to get data from internal values
        for(int i=1; i<input.length; i++)
        {
            parser = new StringTokenizer(input[i]);
            
            n = Integer.parseInt(parser.nextToken());
            m = Integer.parseInt(parser.nextToken());
            gnm = Double.valueOf(parser.nextToken()).doubleValue();
            hnm = Double.valueOf(parser.nextToken()).doubleValue();
            dgnm = Double.valueOf(parser.nextToken()).doubleValue();
            dhnm = Double.valueOf(parser.nextToken()).doubleValue();
            
            if (m <= n)
            {
                c[m][n] = gnm;
                cd[m][n] = dgnm;
                
                if (m != 0)
                {
                    c[n][m-1] = hnm;
                    cd[n][m-1] = dhnm;
                }
            }
            
        }
        
    }
    
//    private void GetDec()
//    {
//    	dec=7.7;
//    }
    
    
    
//    /** Prints out a table of test values for verification */
//    public static void main(String [] args)
//    {
//        double date = 2007;
//        System.out.println("Check of field values for a date of " + date + ": ");
//        System.out.println("Returns magnetic variation above 60 degrees latitude.");
//        TSAGeoMag testCase = new TSAGeoMag();
//         // i is the latitude, j is the longitude, k is the altitude
//        
////        testCase.GetDec();
////        System.out.println(testCase.dec);
//        
//        for(int i=-45; i<55; i+=15)
//        {
//            for(int j=-180; j<180; j+=60)
//            {
//                for(int k=0; k<200; k+=100)
//                {
//                    System.out.println("For a latitude of " +i+" degrees, a longitude of " + j + 
//                            " degrees, and an altitude of " + k + " kilometers: ");
//                    System.out.println("    The declination is: " + testCase.getDeclination(i, j, date, k) + " degrees.");
//                    System.out.println("    The total intensity is: " + testCase.getIntensity(i, j, date, k) + " nT.");
//                    System.out.println("    The dip angle is: " + testCase.getDip(i, j, date, k) + " degrees.");
//                    System.out.println("    The horizontal intensity is: " + testCase.getHorizontalIntensity(i, j, date, k) + " nT.");
//                    System.out.println("    The vertical intensity is: " + testCase.getVerticalIntensity(i, j, date, k) + " nT.");
//                    System.out.println("    The north intensity is: " + testCase.getNorthIntensity(i, j, date, k) + " nT.");
//                    System.out.println("    The east intensity is: " + testCase.getEastIntensity(i, j, date, k) + " nT.");
//                }
//            }
//        }
//        
//            }
}

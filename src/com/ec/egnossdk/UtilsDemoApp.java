/**
 * @file UtilsDemoApp.java
 *
 * Utility class for the EGNOS Demo app.
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
 * http://ec.europa.eu/idabc/eupl
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 *
 **/
package com.ec.egnossdk;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

/**
 * Utility class for the EGNOS Demo app
 **/
public class UtilsDemoApp {
  static GlobalState gs;
  Context context;
  static LogFiles log;
  static Handler errorhandler_;
  private static final String TAG = "EGNOS-SDK";

  /**
   * UtilsDemoApp Constructor. 
   * 
   * Constructs an interface to global information of the
   * application that called uBlox class and writer that writes to a log file.
   * @param context    interface to the global information of the application
   *                   environment.
   **/
  public UtilsDemoApp(Context context) {
    this.context = context;
    gs = (GlobalState) context.getApplicationContext();
    log = new LogFiles();
  }

  /**
   * get_type function 
   * 
   * The function that gets the message type from the egnos subframe.
   * @param  sfr         the egnos subframe.
   * @return The message type from the egnos subframe.
   */
  public static int get_type(String sfr) {
    String tmp;
    tmp = extract(sfr, 8, 13);
    return (int) bin2dec(tmp);
  }
  
  public static int getMaxValue(int[] numbers){  
	  int maxValue = numbers[0];  
	  for(int i=1;i < numbers.length;i++){  
	    if(numbers[i] > maxValue){  
	      maxValue = numbers[i];  
	    }  
	  }  
	  return maxValue;  
	}  

	/**
	 * size_msg18 function 
	 * 
	 * Counts the number of 1's in the payload of EGNOS Subframe in Message 18.
	 * @param  egnosframe     The EGNOS Subframe of Message 18.
	 * @return size_ones      The count of 1's in the payload of Message 18.
	 **/
	static int size_msg18(String egnosframe)
	{
		int size_ones=0;
		String aa = UtilsDemoApp.extract(egnosframe,24,224);
		size_ones = 0;
		for(int i=0; i<aa.length();i++)
		{
			if(aa.charAt(i)=='1')
				size_ones++;
		}
		return size_ones;
	}
	
	
	/**
	 * size_msg1 function 
	 * 
	 * Counts the number of 1's in the payload of EGNOS Subframe in Message 1.
	 * @param  egnosframe     The EGNOS Subframe of Message 1.
	 * @return size_ones      The count of 1's in the payload of Message 1.
	 **/
	static int size_msg1(String egnosframe)
	{
		int size_ones=0;
		String aa = UtilsDemoApp.extract(egnosframe,14,65);
		size_ones = 0;
		for(int i=0; i<aa.length();i++)
		{
			if(aa.charAt(i)=='1')
				size_ones++;
		}
		return size_ones;
	}
	
	
	
  /**
   * get_bandId18 function 
   * 
   * The function that gets the band id for Message 18 from the egnos subframe.
   * @param  sfr         the egnos subframe.
   * @return The band id for Message 18 from the egnos subframe.
   */
  public static int get_bandId18(String sfr) {
    String tmp;
    // Band ID
    tmp = extract(sfr, 30, 33);
    return (int) bin2dec(tmp);
  }

  /**
   * get_bandId26 function 
   * The function that gets the band id for Message 26 from the egnos subframe.
   * 
   * @param  sfr         the egnos subframe.
   * @return The band id for Message 26 from the egnos subframe.
   */
  public static int get_bandId26(String sfr) {
    String tmp;
    // Band ID
    tmp = extract(sfr, 26, 29);
    return (int) bin2dec(tmp);
  }

  /**
   * get_blockId26 function  
   * 
   * The function that gets the block id for Message 26 from the egnos subframe.
   * @param  sfr         the egnos subframe.
   * @return The block id for Message 26 from the egnos subframe.
   */
  public static int get_blockId26(String sfr) {
    String tmp;
    // Band ID
    tmp = extract(sfr, 30, 33);
    return (int) bin2dec(tmp);
  }

  /**
   * bin2dec function 
   * 
   * Binary to decimal conversion
   * @param binary    binary string to convert
   * @return sum      The converted decimal value
   */
  public static long bin2dec(String binary) {
    long sum = 0;
    sum = Long.parseLong(binary, 2);
    return sum;
  }

  /**
   * extract function 
   * 
   * Extract characters between the positions begin and end
   * @param c          string to be extracted
   * @param begin      Begin position of the chain to be extract
   * @param end        End position of the chain to be extract
   * @return result    The extracted string
   */
  public static String extract(String c, int begin, int end) {
    String result = null;
    result = c.substring(begin, end + 1);
    return result;
  }

  /**
   * toHex function 
   * 
   * Converts an array of bytes to its corresponding hexadecimal string.
   * @param data     Byte array from the Bluetooth receiver.
   * @return buf     A string which contains the converted hexadecimal values.
   **/
  public static String toHex(byte[] data) {
    String HEX_DIGITS = "0123456789abcdef";
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i != data.length; i++) {
      int v = data[i] & 0xff;
      buf.append(HEX_DIGITS.charAt(v >> 4));
      buf.append(HEX_DIGITS.charAt(v & 0xf));
    }
    return buf.toString();
  }
  
  /**
   * byteToHex function 
   * 
   * Converts an array of bytes to its corresponding hexadecimal string.
   * @param data     The byte array from the Bluetooth receiver.
   * @return buf     A string which contains the converted hexadecimal values.
   **/
  public static String byteToHex(byte[] data) {
    StringBuilder hexString = new StringBuilder();
    for (int i = 0; i < data.length; i++) {
      String hex = Integer.toHexString(0xFF & data[i]);
      if (hex.length() == 1) {
        hexString.append("0");
      }
      hexString.append(hex);
    }
    return hexString.toString();
  }
  
  /**
   * toHexCharArray function 
   * 
   * Converts an array of bytes to its corresponding
   * hexadecimal string. This is an alternative function to toHex.
   * @param input      Byte array from the Bluetooth receiver.
   * @return output    A string which contains the converted hexadecimal values.
   **/
  public static char[] toHexCharArray(byte[] input) {
    int m = input.length;
    int n = 2 * m;
    int l = 0;
    char[] output = new char[n];
    for (int k = 0; k < m; k++) {
      byte v = input[k];
      int i = (v >> 4) & 0xf;
      output[l++] = (char) (i >= 10 ? ('a' + i - 10) : ('0' + i));
      i = v & 0xf;
      output[l++] = (char) (i >= 10 ? ('a' + i - 10) : ('0' + i));
    }
  
    return output;
  }


  /**
   * hexToBin function 
   * 
   * Converts hexadecimal value to a Binary.
   * @param  hex        The hexadecimal value to convert.
   * @return tmp         The converted binary value.
   **/
  public static String hexToBin(String hex) {
	  String bin = "";
	  String tmp = "";
	  
	  for(int j=0; j< hex.length(); j++ ) {
		  switch(hex.charAt(j)) {
			  case '0':
			  		bin="0000";
			  		break;
			  case '1':
				  	bin="0001";
				  	break;
			  case '2':
				  	bin="0010";
				  	break;
			  case '3':
				  	bin="0011";
				  	break;
			  case '4':
				  	bin="0100";
				  	break;
			  case '5':
				  	bin="0101";
				  	break;
			  case '6':
				  	bin="0110";
				  	break;
			  case '7':
				  	bin="0111";
				  	break;
			  case '8':
				  	bin="1000";
				  	break;
			  case '9':
				  	bin="1001";
				  	break;
			  case 'A':
				  	bin="1010";
				  	break;
			  case 'B':
				  	bin="1011";
				  	break;
			  case 'C':
				  	bin="1100";
				  	break;
			  case 'D':
				  	bin="1101";
				  	break;
			  case 'E':
				  	bin="1110";
				  	break;
			  case 'F':
				  	bin="1111";
				  	break;
			  case 'a':
				  	bin="1010";
				  	break;
			  case 'b':
				  	bin="1011";
				  	break;
			  case 'c':
				  	bin="1100";
				  	break;
			  case 'd':
				  	bin="1101";
				  	break;
			  case 'e':
				  	bin="1110";
				  	break;
			  case 'f':
				  	bin="1111";
				  	break;
		  }
		  tmp=tmp+bin;
	  }
	  
	  return tmp;
  }
  /**
   * hex2bin4 function.
   * The function performs the conversion from hexadecimal to binary.
   * It converts 1 hexadecimal char to 4 binary digits.
   * @param  hexade Hexadecimal number character
   * @return        The pointer of the string with the converted decimal number
   */
  public  static String hex2bin4(char hexade)
  {
    String bin = "";
    switch(hexade)
    {
      case '0':bin = "0000";
        break;
      case '1':bin = "0001";
        break;
      case '2':bin = "0010";
        break;
      case '3':bin = "0011";
        break;
      case '4':bin = "0100";
        break;
      case '5':bin = "0101";
        break;
      case '6':bin = "0110";
        break;
      case '7':bin = "0111";
        break;
      case '8':bin = "1000";
        break;
      case '9':bin = "1001";
        break;
      case 'A':bin = "1010";
        break;
      case 'B':bin = "1011";
        break;
      case 'C':bin = "1100";
        break;
      case 'D':bin = "1101";
        break;
      case 'E':bin = "1110";
        break;
      case 'F':bin = "1111";
        break;
      default:bin = "0000";
        break;
    }
    return bin;
  }

  /**
   * binToHex function 
   * 
   * Converts Binary value to a hexadecimal.
   * @param  bin            The binary value to be converted.
   * @return hextmp         The converted hexadecimal value.
   **/
  public static String binToHex(String bin) {
	  String hex = "";
	  String tmp = "";
	  String hextmp = "";
	  bin=bin+"0";
	  
	  for(int i=0; i< bin.length()-4; i+=4 )
	  {
		  tmp = bin.substring(i, i+4);
		  if (tmp.compareToIgnoreCase("0000") == 0) {
			  hex = "0";
		  }
		  if (tmp.compareToIgnoreCase("0001") == 0) {
			  hex = "1";
		  }
		  if (tmp.compareToIgnoreCase("0010") == 0) {
			  hex = "2";
		  }
		  if (tmp.compareToIgnoreCase("0011") == 0) {
			  hex = "3";
		  }
		  if (tmp.compareToIgnoreCase("0100") == 0) {
			  hex = "4";
		  }
		  if (tmp.compareToIgnoreCase("0101") == 0) {
			  hex = "5";
		  }
		  if (tmp.compareToIgnoreCase("0110") == 0) {
			  hex = "6";
		  }
		  if (tmp.compareToIgnoreCase("0111") == 0) {
			  hex = "7";
		  }
		  if (tmp.compareToIgnoreCase("1000") == 0) {
			  hex = "8";
		  }
		  if (tmp.compareToIgnoreCase("1001") == 0) {
			  hex = "9";
		  }
		  if (tmp.compareToIgnoreCase("1010") == 0) {
			  hex = "A";
		  }
		  if (tmp.compareToIgnoreCase("1011") == 0) {
			  hex = "B";
		  }
		  if (tmp.compareToIgnoreCase("1100") == 0) {
			  hex = "C";
		  }
		  if (tmp.compareToIgnoreCase("1101") == 0) {
			  hex = "D";
		  }
		  if (tmp.compareToIgnoreCase("1110") == 0) {
			  hex = "E";
		  }
		  if (tmp.compareToIgnoreCase("1111") == 0) {
			  hex = "F";
		  }
		  
		  hextmp = hextmp + hex;
	  }
	  
	  return hextmp;
	  
  }
  
  /**
   * charToStringUbl function 
   * 
   * Converts a char array to string in the reverse order.
   * @param charArray    The character array to be converted.
   * @param start        The start index.
   * @param end          The end index.
   * @return convString  The converted string.
   **/
  public static String charToStringUbl(final char[] charArray, final int start,
      final int end) {
    String convString = "";

    for (int i = end; i > start; i -= 2) {
      convString = convString + Character.toString(charArray[i - 1])
          + Character.toString(charArray[i]);
    }
    return convString;
  }

  /**
   * charToStringUbl2 function 
   * 
   * Converts a string in the reverse order.
   * @param  rawMessage    The string to be reversed.
   * @param  start         The start index.
   * @param  end           The end index.
   * @return convString    The converted string.
   **/
  public static String charToStringUbl2(final String rawMessage, final int start,
      final int end) {
    String convString = "";

    for (int i = end; i > start; i -= 2) {
      convString = convString + rawMessage.charAt(i-1) + rawMessage.charAt(i);
    }
    return convString;
  }
  
  /**
   * decodeIEEE_singlepr function 
   * 
   * Decodes a binary String according to IEEE 754 Single Precision Format 
   * @param  rawMessage    The string to be decoded.
   * @return convString    The converted string.
   **/
  public static double decodeIEEE_singlepr(String message) {
	  
	  double fraction = 1;
	  double result;
	  
	  String Sexponent = message.substring(1, 9);
	  String Sfraction = message.substring(9, 32);
	  
	  int exponent = (int)bin2dec(Sexponent);
	 
	  for(int i = 0; i < 23; i++)
		  if(Sfraction.charAt(i) == '1')
			  fraction = fraction + Math.pow(2,-i-1);
	  
	  result = fraction * Math.pow(2, (exponent-127));
	  
	  if(message.charAt(0) == '1')
		  result = -1 * result;
	  
    return result;
  }
  
  /**
   * decodeIEEE_doublepr function 
   * 
   * Decodes a binary String according to IEEE 754 Double Precision Format 
   * @param  rawMessage    The string to be decoded.
   * @return convString    The converted string.
   **/
  public static double decodeIEEE_doublepr(String message) {
	  
	  double fraction = 1;
	  double result;
	  
	  String Sexponent = message.substring(1, 12);
	  String Sfraction = message.substring(12, 64);
	  
	  int exponent = (int)bin2dec(Sexponent);
	 
	  for(int i = 0; i < 52; i++)
		  if(Sfraction.charAt(i) == '1')
			  fraction = fraction + Math.pow(2,-i-1);
	  
	  result = fraction * Math.pow(2, (exponent-1023));
	  
	  if(message.charAt(0) == '1')
		  result = -1 * result;
	  
    return result;
  }
  
  /**
   * generateMessage function 
   * 
   * Converts Hexadecimal   String to bytes
   * @param messageHex      Hexadecimal string as e.g A0A20008A6001C01000000000C3B0B3
   * @return message      ArrayList of Hexadecimal strings
   **/
  public static byte[] generateByteMessage(final String messageHex) {
    int length = messageHex.length() / 2;
    ByteBuffer message = ByteBuffer.allocate(length);
    message.put(new BigInteger(messageHex, 16).toByteArray(), 1, length);
    return message.array();
  }

  /**
   * writeMessageToReceiver function 
   * Write the messages to Bluetooth receiver as bytes.
   * 
   * @param buffer        The message xyz as a an array of bytes.
   **/
  public static int write(final byte[] buffer) {
    boolean execute = false;

    try {
    	GlobalState.getSocket();
      execute = true;

    } catch (Exception e) {
      Log.e(TAG, "Utils | write() Error: " + e.getMessage());
      log.logError("Receiver is disconnected");
      GlobalState.setSocket(null);
      return -1;
    }

    if (execute) {
      try {
        if (GlobalState.getSocket() != null) {
        	GlobalState.getOutputStream().write(buffer);
        	GlobalState.getOutputStream().flush();
        }
      } catch (IOException e) {
        Log.e(TAG, "Utils | write() Error: " + e.getMessage());
        log.logError("Receiver is disconnected");
        GlobalState.setSocket(null);
        return -1;
      }
      return 1;
    } else
      return -1;
  }
  
  /**
   * getDistance function 
   * 
   * Gets the distance between two gps location points based on Haversine formula.
   * @param   oldLatitude
   * @param   oldLongitude
   * @param   coordinates
   * @return  distance     The distance between 2 gps location points
   **/
  public static double getDistance(double oldLatitude, double oldLongitude, double[] coordinates){
    double distance = 0;
    double PI = 3.1415926535898;

    // Haversine formula
    double dlon = (coordinates[1] * PI/180)- (oldLongitude * PI/180);
    double dlat = (coordinates[0] * PI/180) - (oldLatitude * PI/180);
    double a = Math.pow(Math.sin(dlat / 2.0), 2) +
               Math.cos(oldLatitude * PI/180) *
               Math.cos((coordinates[0] * PI/180)) *
               Math.pow(Math.sin(dlon / 2.0), 2.0);
    double c = 2 * Math.asin(Math.min(1.0, Math.sqrt(a)));
    distance =  (6357 * 1000) * c;// Haversine formula result
    return distance;
  }
  
  /**
   * cconv_to_cart function
   * Conversion from Geodetic to Cartesian coordinates (WGS84)
   * @param   *vect   Pointer of the vector with initial Geodetic values(phi,lambda,h)
   */
  public static void cconv_to_cart(double[] vect) {
  	double N, X, Y, Z; //vect[0] -> phi, vect[1] -> lambda, vect[2] -> height

  	double a_WGS84 = 6378137.0000;
  	double b_WGS84 = 6356752.3142;
  	double e_WGS84_SQUARED = 6.69437999014132E-3;
  	
  	vect[0] *= Math.PI / 180; // Conversions to rad
  	vect[1] *= Math.PI / 180;

  	N = a_WGS84/Math.sqrt(1-e_WGS84_SQUARED*Math.sin(vect[0])*Math.sin(vect[0]));
  	X = (N + vect[2])*Math.cos(vect[0])*Math.cos(vect[1]);
  	Y = (N + vect[2])*Math.cos(vect[0])*Math.sin(vect[1]);
  	Z = (((b_WGS84*b_WGS84)/(a_WGS84*a_WGS84))*N + vect[2])*Math.sin(vect[0]);

  	vect[0] = X;
  	vect[1] = Y;
  	vect[2] = Z;
  }
}
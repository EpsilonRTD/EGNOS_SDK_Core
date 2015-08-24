/**
 * @file SISNeT.java
 *
 * SISNeT module source file containing the connection functions to the
 * server and the ephemerides/EGNOS messages request functions.
 * The modules handles the connection to the SISNeT server via the
 * imported environment specific library functions (Winsisnet,
 * Androidsisnet...). It contains the sending/receiving functions for the GPS
 * ephemerides and the EGNOS messages.
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
 * http://ec.europa.eu/idabc/eupl
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 *
 */

package com.ec.egnossdk;

import java.net.Socket;

import android.util.Log;

public class SISNeT {
  private static final String TAG_SISNET = "EGNOS-SDK-AndroidSISNeT";
  
  static AndroidSISNeT androidSisnet = new AndroidSISNeT();
  
   /**
    * connectSisnet function
    * The function calls the connection to the SISNeT server function from the
    * environment specific library.
    * @return The socket for the SISNeT server connection
    */
    static Socket connectSisnet(){
       return androidSisnet.OpenConnection();
    }
   
     /**
      * closeSisnet function
      * The function calls the connection closing function from the environment
      * specific library.
      * @param sock The socket used to connect to the SISNeT server.
      */
     public static void closeSisnet(Socket sock){
       androidSisnet.closeConnection(sock);
     }
 
         
     /**
      * get_sisnetmsg function
      * The function requests and receives the latest EGNOS messages from 
      * the SISNeT server.It decompresses and parses the received messages.
      */
     static String get_msg(){
       int i,len;
       double tow;
       StringBuffer sendMsg = new StringBuffer("");
       String buffer = "";
       String err_msg,tmp,bin_msg;
       String[] parts = new String[5];
       int shift = 0;
       int wknb = 0;
       String sisnet_parity = "";
       StringBuffer egnos = new StringBuffer("");
       String egnosMessage ="";
       
       sendMsg.append("MSG\n");
       
       androidSisnet.sendSisnet(sendMsg.toString());
       buffer = androidSisnet.recvSisnet();       
		
		if (buffer != null) {
			int length = buffer.length();
			if (length > 20) {
				if (errDetect(buffer) == 0) {
					// Decompress the sisnet message
					tmp = decompress(buffer);

					// TOW, Week, EGNOS message in hex, Parity
					i = 0;
					if (tmp.charAt(0) == '*')
						shift = 0;						// MODIFIED: it was =1

					tmp = tmp.replace('*', ',');
					parts = tmp.split(",");//UtilsDemoApp.split(tmp, ",");

					// Week part
					wknb = Integer.parseInt(parts[2 - shift]);

					// GPS time partsa
					tow = Double.parseDouble(parts[3 - shift]);

					// EGNOS part
					len = parts[4 - shift].length();

					for (i = 0; i < len; i++)
						egnos.append(UtilsDemoApp.hex2bin4(parts[4 - shift]
								.charAt(i)));
					bin_msg = egnos.toString().substring(0, 250);
					// Parity part
					sisnet_parity = parts[5 - shift].substring(0, 2);

					String tow_str = String.valueOf((int) tow);
					while (tow_str.length() < 6)
						tow_str = "0" + tow_str;
					if(tow_str.length() == 6)
						tow_str = tow_str + ".00000";
					egnosMessage = tow_str + bin_msg;// time of week and EGNOS message in binary	      
				}else 
					egnosMessage = buffer;
			} else {
				err_msg = errParse(errDetect(buffer), buffer);
				Log.e(TAG_SISNET, "Sisnet | DEBUG: Server response:" + err_msg);
			}
		} else {
			Log.e(TAG_SISNET, "Sisnet | No message available from SISNeT");
			egnosMessage = "";
		}
       return egnosMessage;       
     }
     
     /**
      * decompress function
      * The function performs the decompression of the message received from the
      * SISNeT get_msg request.
      * @param  buffer        The message to be decompressed
      * @return  tmp          The decompressed message
      */
	static String decompress(String buffer) {
		StringBuffer tmp = new StringBuffer("");
		String bin1, bin2, bin3;
		int i, j, len;


		len = buffer.length();
		for (i = 0; i < len; i++) {
			if (buffer.charAt(i) == '|') {
				bin1 = UtilsDemoApp.hex2bin4(buffer.charAt(i + 1));
				for (j = 0; j < UtilsDemoApp.bin2dec(bin1) - 1; j++) {
					tmp.append(buffer.charAt(i - 1));
				}
				i = i + 2;
			}
			if (buffer.charAt(i) == '/') {
				bin1 = UtilsDemoApp.hex2bin4(buffer.charAt(i + 1));
				bin2 = UtilsDemoApp.hex2bin4(buffer.charAt(i + 2));
				bin3 = bin1.concat(bin2);

				for (j = 0; j < UtilsDemoApp.bin2dec(bin3) - 1; j++) {
					tmp.append(buffer.charAt(i - 1));
				}
				i = i + 2;
			} else {
				tmp.append(buffer.charAt(i));
			}
		}

		return tmp.toString();
	}
     
     /**
      * errDetect function
      * The function detects a SISNeT error message.
      * @param   buffer  The message from SISNeT
      * @return          The detected error code, 0 if no error detected
      */
	static int errDetect(String buffer) {
		String err;
		int err_code, r;
		err = UtilsDemoApp.extract(buffer, 0, 5);
		if (err.startsWith("*ERR,")) {
			Log.i(TAG_SISNET, "Sisnet | errDetect : " + buffer);
			err_code = buffer.charAt(5);// char at 5 in string err is the error
										// code.
			r = err_code;
		} else
			r = 0;
		return r;
	}
     
     /**
      * errParse function
      * The function returns the error message from a message and an error code.
      * @param err_code the error code number
      * @param buffer   The message from SISNeT
      * @return         The error message equivalent to the error code
      */
	 static String errParse(int err_code, String buffer) {
		String err_msg;
		int pos;
		switch (err_code) {
		case 10:
			pos = buffer.indexOf("\n");
			err_msg = UtilsDemoApp.extract(buffer, 8, pos);
			break;
		default:
			pos = buffer.indexOf("\n");
			err_msg = UtilsDemoApp.extract(buffer, 7, pos);
			break;
		}
		return err_msg;
	}
	 
   /**
    * readSisnetMessage function
    * Splits the received SISNeT message.
    * @param sisnetMessage The message received in SISNeT *MSG format.
    * @return  The EGNOS message contained in the SISNeT message without the checksum.
    */
   public static String readSisnetMessage(String sisnetMessage)
   {
     String[] item = sisnetMessage.split(",");
     int length = item.length - 1;
     item[length] = decompress(item[length]);
     
     String newMsg = "";
     for (int i = 0; i < item.length; i++)
     {
         if (i < item.length - 1)
             newMsg += item[i] + ",";
         else
             newMsg += item[i];
     }
     
     //remove the checksum at the end of the string containing the EGNOS message
     //ToDo check the chekcsum for correctness.
     //ToDo (optional) check the EGNOS parity for correctness
     String egnosMsg = item[length].substring(0, item[length].length() - 3);
     
    String binaryEgnosMsg = ""
        + "0000"
        + String.valueOf(Integer.parseInt(
            String.valueOf(Integer.parseInt(egnosMsg, 16)), 2));
     
//     String binaryEgnosMsg = String.Join(String.Empty,
//         egnosMsg.Select(c => Convert.ToString(Convert.ToInt32(c.ToString(), 16), 2).PadLeft(4, '0')));

     String tow = item[1];
     while (tow.length() < 6)
         tow = "0" + tow;

     egnosMsg = tow + binaryEgnosMsg;
     
     return egnosMsg;
   }
   
  /**
   * readSisnetMessage function Splits the received SISNeT message.
   * 
   * @param egnosipMessage
   *          The message received in *MSG format.
   * @param sdkFormat
   *          If set, the function returns the GPS TOW and the EGNOS message as
   *          one string (required by SDK).
   * @return The EGNOS message contained in the SISNeT message without the
   *         checksum.
   */
   public static String readSisnetMessage(String egnosipMessage, boolean sdkFormat)
   {
        String[] message_parts = egnosipMessage.split(",");
     
        // decompress short number notation
         String egnosMsg = decompress(message_parts[message_parts.length - 1]);
         
         // remove check sum
         egnosMsg = egnosMsg.substring(0, egnosMsg.length() - 3);
         
         // convert to binary form
         egnosMsg = UtilsDemoApp.hexToBin(egnosMsg.toLowerCase());
         // put time of week in the right form
         String tow = message_parts[2];
         while (tow.length() < 12)
             tow = tow + "0";
         
         return tow + egnosMsg;
   }
	 
	 
}

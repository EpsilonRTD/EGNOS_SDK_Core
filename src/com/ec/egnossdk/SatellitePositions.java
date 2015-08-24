/**
 * @file SatellitePositions.java
 *
 * Gets the satellite position from the satellite data obtained from NORAD.
 * Calculates the positions for 24 hoursrs for every 10 minutes.
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

public class SatellitePositions {
  
   public double[][] sat_pos_array = new double[145][3]; //change 145
   double[] jd_array = new double[145];  //change 145
   public double prn;
   public double[][] direction_vec = new double[145][3];
   
   public double[][] satPosXYZ = new double[145][3];
  
   SatellitePositions(){
     
   }
    

}

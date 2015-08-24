/**
 * @file Utils.c
 *
 * @brief Utils module source file containing useful math/string functions.
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

#include "Utils.h"

/**
 * hex2bin4 function.
 * The function performs the conversion from hexadecimal to binary.
 * It converts 1 hexadecimal char to 4 binary digits.
 * @param  *hexade Hexadecimal number character
 * @return         The pointer of the string with the converted decimal number
 */
char* hex2bin4(char hexade)
{
	char *bin="";
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
 * dec2bin function.
 * The function performs the conversion from decimal to binary.
 * @param decimal Decimal number to convert
 * @param *binary Pointer of the char with the converted decimal number
 * @param size    The size of the binary number
 */
void dec2bin(double decimal, char *binary, int size)
{
	int  k=0,n=0;
	int  mod2;
	char temp[100]= "";
	do
	{
		mod2 = decimal - (2 * (int)(decimal/2));	// modulus 2
		decimal   = (int)decimal / 2;
		temp[k++] = mod2 + '0';
	}while (decimal > 0);

	//fill with 0 to obtain a char of size characters
	while(k<size)
	{
		strcat(temp,"0");
		k++;
	}
	while (k >= 0)
		binary[n++] = temp[--k];
	binary[n-1] = 0;
}

/**
 * bin2dec function.
 * The function performs the conversion from binary to decimal.
 * @param  *binary Pointer of the binary char to convert
 * @return         The converted decimal value
 */
#ifdef Linux_H_
   long long bin2dec(char *binary)
{
	int i;
	long long p2,num,sum=0;
	int len=strlen(binary) - 1;
	for(i = 0; i <= len; i++)
	{
		p2 = 1;
		num = (binary[i])- '0'; // char to numeric value
		p2 = p2<<(len-i);		// p2 = pow(2,len-i);
	    sum = sum + num*p2; 	// sum it up
	 }
	 return(sum);
}
#else
__int64 bin2dec(char *binary)
{
	int i;
	__int64 p2,num,sum=0;
	int len=strlen(binary) - 1;
	for(i = 0; i <= len; i++)
	{
		p2 = 1;
		num = (binary[i])- '0'; // char to numeric value
		p2 = p2<<(len-i);		// p2 = pow(2,len-i);
	    sum = sum + num*p2; 	// sum it up
	 }
	 return(sum);
}
#endif

/**
 * extract function.
 * The function extracts characters between two positions.
 * @param *c      Pointer of the initial char
 * @param begin   Begin position of the chain to be extract
 * @param end     End position of the chain to be extract
 * @param *result The extracted char
 */
void extract(const char *c, int begin, int end, char *result)
{
  //result[end+1-begin] = '\0';
  //memcpy (result,(char*)c+begin, end+1-begin);

	int i;
	for (i = begin; i <= end; i++)
	{
		result[i-begin] = c[i];
	}
	result[i-begin] = '\0';
}

/**
 * isnan function.
 * The function returns 1 if the value in parameter is NaN
 * @param value The value to test
 * @return      1 if value is NaN, 0 if not
 */
int is_nan(double value)
{
  int r;
  if(value!=value)
    r = 1;
  else
    r = 0;

  return r;
}
/**
 * mod function.
 * The function returns the modulo result between
 * dividend and divisor
 * @param dividend 	The value set as dividend
 * @param divisor 	The value set as divider
 * @return      	The result of the operation
 */
double mod(double dividend, double divisor){
	double result;
	int part = (int)(dividend/divisor);
	result = dividend - (double)(part*divisor);
	return result;
}

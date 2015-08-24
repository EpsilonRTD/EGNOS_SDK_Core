/**
 * @file Matrix.c
 *
 * @brief Matrix module source file containing the matrix operations
 * functions.
 * @details The module is a mathematical library performing operations on
 * fixed size matrices.
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
#include "Matrix.h"

/**
 * det_33 function
 * Calculation of a given 3x3 matrix determinant, line development
 * @param  	matrix 		The input 3x3 matrix
 * @return The determinant
 */
double det_33(double matrix[3][3])
{
	return  matrix[0][0]*(matrix[1][1]*matrix[2][2] - matrix[2][1]*matrix[1][2]) - matrix[0][1]*(matrix[1][0]*matrix[2][2] - matrix[1][2]*matrix[2][0]) + matrix[0][2]*(matrix[1][0]*matrix[2][1] - matrix[1][1]*matrix[2][0]);
}

/**
 * inv_33 function
 * Inversion of a given 3x3 matrix	Method : INV(M) = 1/det(M) * Trans(Com(M))
 * @param  	matrix 		The input 3x3 matrix
 * @param	matrix_inv	The inverted matrix result
 */
void inv_33(double matrix[3][3], double matrix_inv [3][3])
{
	double A = matrix[1][1] * matrix[2][2] - matrix[1][2] * matrix[2][1];	// matrix of cofactors elements
	double B = matrix[1][2] * matrix[2][0] - matrix[1][0] * matrix[2][2];
	double C = matrix[1][0] * matrix[2][1] - matrix[1][1] * matrix[2][0];
	double D = matrix[0][2] * matrix[2][1] - matrix[0][1] * matrix[2][2];
	double E = matrix[0][0] * matrix[2][2] - matrix[0][2] * matrix[2][0];
	double F = matrix[0][1] * matrix[2][0] - matrix[0][0] * matrix[2][1];
	double G = matrix[0][1] * matrix[1][2] - matrix[0][2] * matrix[1][1];
	double H = matrix[0][2] * matrix[1][0] - matrix[0][0] * matrix[1][2];
	double I = matrix[0][0] * matrix[1][1] - matrix[0][1] * matrix[1][0];

	double t_det = 1 / det_33(matrix);									// 1 over the determinant of matrix

	matrix_inv[0][0] = t_det*A;											// Inverted matrix elements
	matrix_inv[0][1] = t_det*D;
	matrix_inv[0][2] = t_det*G;
	matrix_inv[1][0] = t_det*B;
	matrix_inv[1][1] = t_det*E;
	matrix_inv[1][2] = t_det*H;
	matrix_inv[2][0] = t_det*C;
	matrix_inv[2][1] = t_det*F;
	matrix_inv[2][2] = t_det*I;
}

/**
 * submat_44 function
 * Extract a submatrix 3x3, at row r and column c, from a 4x4 matrix
 * @param  	matrix 		The input 4x4 matrix
 * @param	submatrix 	The 3x3 submatrix
 * @param	r			Row
 * @param	c			Column
 */
void submat_44(double matrix[4][4],double submatrix[3][3], int r, int c)
{
  	int i,j,is,js;

  	is = 0;
  	for(i=0;i<4;i++)												// row browse
	{
  			if (i!=r)												// test if the row should be skipped
  			{
  				is++;
  				js=0;
  				for(j=0;j<4;j++)									// column browse
  				{
  					if(j!=c)										// test if the column should be skipped
  					{
  						js++;
  						submatrix[is-1][js-1] = matrix[i][j];
  					}
  				}
  			}
  		}
}

/**
 * det_44 function
 * Calculation of a given 4x4 matrix determinant, Cramer's rule, column development
 * @param  	matrix	 	The input 4x4 matrix
 * @return The determinant
 */
double det_44(double matrix[4][4])
{
    double  det, result = 0;
    double sub[3][3];
    int n;
    int sign = 1;										// variable for the sign of matrix elements

    for(n=0;n<4;n++)
    {
    	submat_44(matrix, sub,0,n);						// 3x3 submatrix, 0,n => development by column
        det = det_33(sub);								// determinant of the submatrix
        result += sign * matrix[0][n] * det;			// determinant of the 4x4 matrix with the sum of the submatrix determinants
        sign *= -1;										// sign update
    }

    return result;
}

/**
 * inv_44 function
 * Inversion of a given 4x4 matrix, Method : 1/M(i,j) = 1/det(M) * det(submat(M(j,i))) * (-1)^(i+j)
 * @param  	matrix 		The input 4x4 matrix
 * @param	matrix_inv	The inverted matrix result
 */
void inv_44(double matrix[4][4], double matrix_inv [4][4])
{

    double det44 = det_44(matrix);								// determinant of the 4x4 matrix

    double tmp_33[3][3];
    int i,j,sign;

	for(j=0;j<4;j++)
	{
		for(i=0;i<4;i++)
		{
			sign = 1 - ((i+j)%2) * 2;							// sign update
			submat_44(matrix,tmp_33,i,j);						// 3x3 submatrix
			matrix_inv[j][i] = sign * det_33(tmp_33)/ det44;
		}
	}
}

/**
 * transpose function
 * Transpose calculation of a given matrix of dynamically allocated size.
 * @param  	matrix 		The input matrix
 * @param	matrix_t	The transpose matrix result
 */
void transpose(double matrix[size_b_row1][size_b_col1], double matrix_t[size_b_col1][size_b_row1])
{
	int i,j;
	for(i=0;i<size_b_row1;i++)
	{
		for(j=0; j<size_b_col1;j++)
			matrix_t[j][i] = matrix[i][j];
	}
}

/**
 * transpose_vec function
 * Transpose calculation of a given 1 column matrix of dynamically allocated size.
 * @param  	matrix 		The input 1 column matrix
 * @param	matrix_t	The transpose matrix result
 */
void transpose_vec(double matrix[size_b_row1], double matrix_t[1][size_b_row1])
{
	int i;
	for(i=0;i<size_b_row1;i++)
	{
			matrix_t[1][i] = matrix[i];
	}
}

/**
 * multiply function
 * Multiplication of two matrices which have a size dynamically allocated.
 * @param  	matrix1 		The first matrix
 * @param	matrix2	        The second matrix
 * @param   result          The output matrix
 */
void multiply(double matrix1[size_b_row1][size_b_col1], double matrix2[size_b_row2][size_b_col2], double result[size_b_row1][size_b_col2])
{
	int i,j,k;
	for(i=0;i<size_b_row1;i++)
	{
		for(j=0;j<size_b_col2;j++)
		{
			result[i][j]=0;
			for(k=0;k<size_b_col1;k++)
				result[i][j] += matrix1[i][k]*matrix2[k][j];
		}
	}
}

/**
 * multiply_vecxmat function
 * Multiplication of column vector with a matrix
 * @param  	matrix1 		The column vector
 * @param	matrix2	        The matrix
 * @param   result          The output matrix
 */
void multiply_vecxmat(double matrix1[size_b_col1], double matrix2[size_b_row2][size_b_col2], double result[size_b_col2])
{
	int j,k;

		for(j=0;j<size_b_col2;j++)
		{
			result[j]=0;
			for(k=0;k<size_b_col1;k++)
				result[j] += matrix1[k]*matrix2[k][j];
		}
}

/**
 * multiply_matxvec function
 * Multiplication of matrix with a row vector.
 * @param  	matrix1 		The matrix
 * @param	matrix2	        The row vector
 * @param   result          The output matrix
 */
void multiply_matxvec(double matrix1[size_b_row1][size_b_col1], double matrix2[size_b_row2], double result[size_b_row1])
{
	int i,k;
	for(i=0;i<size_b_row1;i++)
	{
			result[i]=0;
			for(k=0;k<size_b_col1;k++)
				result[i]+= matrix1[i][k]*matrix2[k];
	}
}


/**
 * lorentz_4_4 function
 * Lorentz product computation : <a,b> = Trans(a)*M*b; In our case, a and b will be always 1x4 matrix
 * @param  	matrix1 	The first matrix
 * @param	matrix2		The second matrix
 * @return				The computed Lorentz product
 */
double lorentz_4_4(double matrix1[4], double matrix2[4])
{
	/* M = 	[1 0 0 0]
			[0 1 0 0]
			[0 0 1 0]
			[0 0 0-1]
	*/
	return matrix1[0]*matrix2[0] + matrix1[1]*matrix2[1] + matrix1[2]*matrix2[2] - matrix1[3]*matrix2[3];
}

/**
 * subtract_mat function
 * Subtraction of matrix2 from matrix1
 * @param  	matrix1 		The matrix from where the subtraction is made
 * @param	matrix2	        The matrix that is subtracted
 * @param   result          The output matrix
 */
void subtract_mat(double matrix1[size_b_row1][size_b_col1], double matrix2[size_b_row1][size_b_col1], double result[size_b_row1][size_b_col1])
{
	int i,j;

	for(i = 0; i < size_b_row1; i++)
		for(j = 0; j < size_b_col1; j++)
			result[i][j] = matrix1[i][j] - matrix2[i][j];

}

/**
 * subtract_vec function
 * Subtraction of two single dimension matrices
 * @param  	matrix1 		The matrix from where the subtraction is made
 * @param	matrix2	        The matrix that is subtracted
 * @param   result          The output matrix
 */
void subtract_vec(double matrix1[size_b_row1], double matrix2[size_b_row1], double result[size_b_row1])
{
	int i;

	for(i = 0; i < size_b_row1; i++)
			result[i] = matrix1[i] - matrix2[i];

}

/**
 * @file Matrix.h
 *
 * @brief Matrix module header file defining the matrix operations
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

#ifndef MATRIX_H_
#define MATRIX_H_
//#include "Androidsisnet.h"
#include <android/log.h>

int size_b_row1;
int size_b_col1;
int size_b_row2;
int size_b_col2;

double det_33(double matrix[3][3]);
void inv_33(double matrix[3][3], double matrix_inv [3][3]);
void submat_44(double matrix[4][4],double submatrix[3][3], int r, int c);
double det_44(double matrix[4][4]);
void inv_44(double matrix[4][4], double matrix_inv [4][4]);
double lorentz_4_4(double matrix1[4], double matrix2[4]);
void transpose(double [size_b_row1][size_b_col1], double [size_b_col1][size_b_row1]);
void multiply(double [size_b_row1][size_b_col1], double [size_b_row2][size_b_col2], double [size_b_row1][size_b_col2]);
void multiply_matxvec(double [size_b_row1][size_b_col1], double [size_b_row2], double [size_b_row1]);
void multiply_vecxmat(double [size_b_col1], double [size_b_row2][size_b_col2], double [size_b_col2]);
void subtract_mat(double matrix1[size_b_row1][size_b_col1], double matrix2[size_b_row1][size_b_col1], double result[size_b_row1][size_b_col1]);
void subtract_vec(double matrix1[size_b_row1], double matrix2[size_b_row1], double result[size_b_row1]);
void transpose_vec(double matrix[size_b_row1], double matrix_t[1][size_b_row1]);
#endif /* MATRIX_H_ */

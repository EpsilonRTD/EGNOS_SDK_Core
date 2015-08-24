/**
 * @file Matrix.java
 *
 * Provides functions for matrix computations.
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
 **/
package com.ec.egnossdk;

public class Matrix {
		private int nrows;
		private int ncols;
		private double[][] data;

		public Matrix(double[][] dat) {
			this.setData(dat);
			this.setNrows(dat.length);
			this.setNcols(dat[0].length);
		}

		public Matrix(int nrow, int ncol) {
			this.setNrows(nrow);
			this.setNcols(ncol);
			setData(new double[nrow][ncol]);
		}
		
		public Matrix() {
		  
		}

		public int getNrows() {
			return nrows;
		}

		public void setNrows(int nrows) {
			this.nrows = nrows;
		}

		public int getNcols() {
			return ncols;
		}

		public void setNcols(int ncols) {
			this.ncols = ncols;
		}

		public double[][] getData() {
			return data;
		}

		public void setData(double[][] data) {
			this.data = data;
		}
		
		public double[][] getValues() {
			return data;
		}

		public void setValues(double[][] values) {
			this.data = values;
		}

		public void setValueAt(int row, int col, double value) {
			data[row][col] = value;
		}

		public double getValueAt(int row, int col) {
			return data[row][col];
		}

		public boolean isSquare() {
			return nrows == ncols;
		}

		public int size() {
			if (isSquare())
				return nrows;
			return -1;
		}

		public Matrix multiplyByConstant(double constant) {
			Matrix mat = new Matrix(nrows, ncols);
			for (int i = 0; i < nrows; i++) {
				for (int j = 0; j < ncols; j++) {
					mat.setValueAt(i, j, data[i][j] * constant);
				}
			}
			return mat;
		}
		
		public Matrix insertColumnWithValue1() {
			Matrix X_ = new Matrix(this.getNrows(), this.getNcols()+1);
			for (int i=0;i<X_.getNrows();i++) {
				for (int j=0;j<X_.getNcols();j++) {
					if (j==0)
						X_.setValueAt(i, j, 1.0);
					else 
						X_.setValueAt(i, j, this.getValueAt(i, j-1));
					
				}
			}
			return X_;
		}

		/**
		 * multiply_vecxmat function
		 * Multiplication of column vector with a matrix
		 * @param  	matrix1 		The column vector
		 * @param	matrix2	        The matrix
		 * @param   result          The output matrix
		 */
		public static double [][] multiply_matxvec(double vector[][], double matrix[][])
		{
			double result[][]= new double [vector.length][1];
			int j,k;
			
			for(j=0;j<matrix.length;j++)
			{
				result[j][0]=0;
				for(k=0;k<vector.length;k++)
					result[j][0] += matrix[j][k]*vector[k][0];
			}
			return result;
		}
		
		public static double[] multiply_vecXMat(double vector[], double matrix[][])	
		{   
			int j,k;
			double result[]= new double [matrix.length];
			
			for(j=0;j<matrix.length;j++)
			{
				result[j] = 0;
				for(k=0;k<vector.length;k++)
					result[j] += matrix[j][k]*vector[k];
			}
			return result;
		}
		
//		public static double[][] multiply_MatXMat(double matrixA[][], double matrixB[][])	
//		{   
//			double result[][]= new double [matrixA.length][matrixA.length];
//			int j,k;
//			
//			for(j=0;j<matrixA.length;j++)
//				for(k=0;k<matrixA.length;k++)
//					result[j][k] += matrixA[j][k]*matrixB[j][k];
//			}
//			return result;
//		}

	public static double[][] multiply_Matrix(double matrix1[][], double matrix2[][])
	{
		int i,j,k;
		double result[][] = new double[matrix1.length][matrix2[0].length];
		
		for(i=0;i<matrix1.length;i++)
		{
			for(j=0;j<matrix2[0].length;j++)
			{
				result[i][j]=0;
				for(k=0;k<matrix1.length;k++)
					result[i][j] += matrix1[i][k]*matrix2[k][j];
			}
		}
		return result;
	}

	public static double[][] div_Matrix(double matrix1[][], double matrix2[][])
	{
		int i,j,k;
		double result[][] = new double[matrix1.length][matrix1.length];
		
		for(i=0;i<matrix1.length;i++)
		{
			for(j=0;j<matrix2.length;j++)
			{
				result[i][j]=0;
				for(k=0;k<matrix1.length;k++)
					result[i][j] -= matrix1[i][k]*matrix2[k][j];
			}
		}
		return result;
	}
		
		public static double multiply_constXVec(double value, double vector[])
		{
			double result=0;
			
			for(int i=0; i<vector.length;i++)
				result += value*vector[i];
			return result;
		}
		
		public static double[][] add_mat(double matrix1[][], double matrix2[][])
		{
			int i,j;
			double result[][] = new double[matrix1.length][matrix1.length];

			for(i = 0; i < matrix1.length; i++)
				for(j = 0; j < matrix1.length; j++)
					result[i][j] = matrix1[i][j] + matrix2[i][j];
			return result;

		}
		
		public static double[][] sub_mat(double matrix1[][], double matrix2[][])
		{
			int i,j;
			double result[][] = new double[matrix1.length][matrix1.length];

			for(i = 0; i < matrix1.length; i++)
				for(j = 0; j < matrix1.length; j++)
					result[i][j] = matrix1[i][j] - matrix2[i][j];
			return result;

		}
		
		public static double[] sub_vect(double vect1[], double vect2[])
		{
			int i,j;
			double result[] = new double[vect1.length];

			for(i = 0; i < vect1.length; i++)
					result[i] = vect1[i] - vect2[i];
			return result;

		}
		
		public static double[] add_vect(double vect1[], double vect2[])
		{
			int i,j;
			double result[] = new double[vect1.length];

			for(i = 0; i < vect1.length; i++)
					result[i] = vect1[i] + vect2[i];
			return result;

		}
		
		public static double mult_vect(double vect1[], double vect2[])
		{
			int i,j;
			double result = 0;

			for(i = 0; i < vect1.length; i++)
					result += vect1[i] * vect2[i];
			return result;

		}
		
		public static double[] div_vectByConst(double vect1[], double value)
		{
			int i,j;
			double result[] = new double[vect1.length];

			for(i = 0; i < vect1.length; i++)
					result[i] = vect1[i] / value;
			return result;

		}
		
		private static double determinante2Per2(double[][] matrice){
		    return matrice[0][0]*matrice[1][1]-matrice[1][0]*matrice[0][1];
		}

		public double determinante(){
		    int i;
		    int j;
		    int k;
		    double det=0;
		    double[][] mx=new double[nrows-1][nrows-1];
		    if (nrows==2){
		        det=Matrix.determinante2Per2(data);
		    }
		    
		    else if(nrows==1){
		        det=data[0][0];
		    }
		    
		    else{
		        double[][]matrixSupport=new double[nrows][nrows];
		        for (i=0;i<nrows;i++){
		            for(j=0;j<nrows;j++){
		                matrixSupport[i][j]=data[i][j];
		            }
		        }
		        
		     // cerco di ricondurre la matrice ad avere la prima riga con tutti zeri 
		        //tranne eventualmente un solo elemento
		        boolean c=false;
		        i=0;
		            while (c==false){
		                if(i==nrows){
		                    c=true;
		                }
		                else{
		                    if(data[i][0]==0){
		                        i++;
		                    }
		                    
		                    else{
		                        for(j=0;j<i;j++){
		                            double x=-matrixSupport[j][0]/data[i][0];
		                            for(k=0;k<nrows;k++){
		                                matrixSupport[j][k]=matrixSupport[j][k]+data[i][k]*x;
		                            }
		                            
		                        }
		                        for(j=i+1;j<nrows;j++){
		                            double x=-data[j][0]/data[i][0];
		                            for(k=0;k<nrows;k++){
		                                matrixSupport[j][k]=matrixSupport[j][k]+data[i][k]*x;
		                            }
		                        }
		                        c=true;
		                }
		            }
		            }
		            for(i=0;i<=nrows-1;i++){
		                if(matrixSupport[i][0]==0){
		                }
		                else{
		                    // crea la matrice ottenuta eliminando la i-ma colonna della matrice di partenza
		                        mx=Matrix.eliminaRigaColonna(matrixSupport,nrows,i,0).data;
		                        det+=(matrixSupport[i][0]*Math.pow(-1,i+2)*(new 
		    					       Matrix(mx).determinante()));
		                    }
		                }
		            }
		            return det;
		        }

		public Matrix matriceAggiunta(){
		    int i;
		    int j;
		    double[][] aggiunta=new double[nrows][nrows];
		    Matrix mTrasposta=trasposta();
		    for (i=0;i<nrows;i++){
		        for(j=0;j<nrows;j++){
		            aggiunta[j][i]=Math.pow(-1,i+j+2)*Matrix.
				eliminaRigaColonna(mTrasposta.data,nrows,j,i).determinante();
		        }
		    }
		    return new Matrix(aggiunta);
		}


		public Matrix matriceInversa(){
		    int i;
		    int j;
		    double d=determinante();
		    if (d==0){
		        return new Matrix(data);
		    }
		    else{
		        double[][] mInversa=new double[nrows][nrows];
		        for (i=0;i<nrows;i++){
		            for(j=0;j<nrows;j++){
		                mInversa[i][j]=matriceAggiunta().data[i][j]/d;
		            }
		        }
		        return new Matrix(mInversa);
		                
		    }
		}


		public Matrix trasposta(){
		    double [][]mx=new double[nrows][nrows];
		    int i;
		    int j;
		    for (i=0;i<nrows;i++){
		        for (j=0;j<nrows;j++){
		            mx[i][j]=data[j][i];
		        }
		    }
		    return new Matrix(mx);
		}




		public static Matrix eliminaRigaColonna(double[][] matr,int nrows, int colonna,
										int riga){
		    double[][] matrice=new double[nrows-1][nrows-1];
		    int i;
		    int j;
		    for(i=0;i<nrows;i++){
		        for(j=0;j<nrows;j++){
		            if (i==riga){
		            }
		            else if (j==colonna){
		            }
		            else{
		                if (i<riga){
		                    if(j<colonna){
		                        matrice[j][i]=matr[j][i];
		                    }
		                    else{
		                        matrice[j-1][i]=matr[j][i];
		                    }
		                }
		                else{
		                    if(j<colonna){
		                        matrice[j][i-1]=matr[j][i];
		                    }
		                    else{
		                        matrice[j-1][i-1]=matr[j][i];
		                    }
		                }
		            }
		        }
		    }
		    return new Matrix(matrice);
		}
		
		public double[][] R1(double angle)
	  {
	    double[][] R = { {1, 0, 0}, 
	                 {0, Math.cos(angle), Math.sin(angle)}, 
	                 {0, -Math.sin(angle), Math.cos(angle)} };
	    return R;
	  }
	  
	  
	  public double[][] R2(double angle)
	  {
	    double[][] R = { {Math.cos(angle), 0, -Math.sin(angle)}, 
	                 {0, 1, 0}, 
	                 {Math.sin(angle), 0,  Math.cos(angle)} };
	    return R;
	  }
	  
	  
	  public double[][] R3(double angle)
	  {
	    double[][] R = { { Math.cos(angle), Math.sin(angle), 0}, 
	                 {-Math.sin(angle), Math.cos(angle), 0}, 
	                 {0, 0, 1} };
	    return R;
	  }
	  
	  
	  /**
	   * det_33 function
	   * Calculation of a given 3x3 matrix determinant, line development
	   * @param   matrix    The input 3x3 matrix
	   * @return The determinant
	   */
	  public double det_33(double matrix[][])
	  {
	    double det =  matrix[0][0]*(matrix[1][1]*matrix[2][2] - matrix[2][1]*matrix[1][2]) - matrix[0][1]*(matrix[1][0]*matrix[2][2] - matrix[1][2]*matrix[2][0]) + matrix[0][2]*(matrix[1][0]*matrix[2][1] - matrix[1][1]*matrix[2][0]);
	    return det;
	  }

	  /**
	   * inv_33 function
	   * Inversion of a given 3x3 matrix  Method : INV(M) = 1/det(M) * Trans(Com(M))
	   * @param   matrix    The input 3x3 matrix
	   * @param matrix_inv  The inverted matrix result
	   */
	  public double[][] inv_33(double matrix[][])
	  {
	    double matrix_inv [][] = new double[3][3];
	    
	    double A = matrix[1][1] * matrix[2][2] - matrix[1][2] * matrix[2][1]; // matrix of cofactors elements
	    double B = matrix[1][2] * matrix[2][0] - matrix[1][0] * matrix[2][2];
	    double C = matrix[1][0] * matrix[2][1] - matrix[1][1] * matrix[2][0];
	    double D = matrix[0][2] * matrix[2][1] - matrix[0][1] * matrix[2][2];
	    double E = matrix[0][0] * matrix[2][2] - matrix[0][2] * matrix[2][0];
	    double F = matrix[0][1] * matrix[2][0] - matrix[0][0] * matrix[2][1];
	    double G = matrix[0][1] * matrix[1][2] - matrix[0][2] * matrix[1][1];
	    double H = matrix[0][2] * matrix[1][0] - matrix[0][0] * matrix[1][2];
	    double I = matrix[0][0] * matrix[1][1] - matrix[0][1] * matrix[1][0];

	    double t_det = 1 / det_33(matrix);                  // 1 over the determinant of matrix

	    matrix_inv[0][0] = t_det*A;                     // Inverted matrix elements
	    matrix_inv[0][1] = t_det*D;
	    matrix_inv[0][2] = t_det*G;
	    matrix_inv[1][0] = t_det*B;
	    matrix_inv[1][1] = t_det*E;
	    matrix_inv[1][2] = t_det*H;
	    matrix_inv[2][0] = t_det*C;
	    matrix_inv[2][1] = t_det*F;
	    matrix_inv[2][2] = t_det*I;
	    
	    return matrix_inv;
	  }

	  /**
	   * submat_44 function
	   * Extract a submatrix 3x3, at row r and column c, from a 4x4 matrix
	   * @param   matrix    The input 4x4 matrix
	   * @param submatrix   The 3x3 submatrix
	   * @param r     Row
	   * @param c     Column
	   */
	  void submat_44(double matrix[][],double submatrix[][], int r, int c)
	  {
	      int i,j,is,js;

	      is = 0;
	      for(i=0;i<4;i++)                        // row browse
	    {
	          if (i!=r)                       // test if the row should be skipped
	          {
	            is++;
	            js=0;
	            for(j=0;j<4;j++)                  // column browse
	            {
	              if(j!=c)                    // test if the column should be skipped
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
	   * @param   matrix    The input 4x4 matrix
	   * @return The determinant
	   */
	  double det_44(double matrix[][])
	  {
	      double  det, result = 0;
	      double[][] sub = new double[3][3];
	      int n;
	      int sign = 1;                   // variable for the sign of matrix elements

	      for(n=0;n<4;n++)
	      {
	        submat_44(matrix, sub,0,n);           // 3x3 submatrix, 0,n => development by column
	          det = det_33(sub);                // determinant of the submatrix
	          result += sign * matrix[0][n] * det;      // determinant of the 4x4 matrix with the sum of the submatrix determinants
	          sign *= -1;                   // sign update
	      }

	      return result;
	  }

	  /**
	   * inv_44 function
	   * Inversion of a given 4x4 matrix, Method : 1/M(i,j) = 1/det(M) * det(submat(M(j,i))) * (-1)^(i+j)
	   * @param   matrix    The input 4x4 matrix
	   * @param matrix_inv  The inverted matrix result
	   */
	  public double[][] inv_44(double matrix[][])
	  {

	    double[][] matrix_inv = new double[4][4];
	        
	      double det44 = det_44(matrix);                // determinant of the 4x4 matrix

	      double[][] tmp_33 = new double[3][3];
	      int i,j,sign;

	    for(j=0;j<4;j++)
	    {
	      for(i=0;i<4;i++)
	      {
	        sign = 1 - ((i+j)%2) * 2;             // sign update
	        submat_44(matrix,tmp_33,i,j);           // 3x3 submatrix
	        matrix_inv[j][i] = sign * det_33(tmp_33)/ det44;
	      }
	    }
	    
	    return matrix_inv;
	  }

	  /**
	   * transpose function
	   * Transpose calculation of a given matrix of dynamically allocated size.
	   * @param   matrix    The input matrix
	   * @param matrix_t  The transpose matrix result
	   */
	  public double[][] transpose(double matrix[][])
	  {
	    int row = matrix.length;
	    int col = matrix[0].length;
	    
	    double[][] matrix_t = new double[col][row];
	    
	    for(int i=0;i<row;i++)
	    {
	      for(int j=0; j<col;j++)
	        matrix_t[j][i] = matrix[i][j];
	    }
	    
	    return matrix_t;
	  }

	  /**
	   * transpose_vec function
	   * Transpose calculation of a given 1 column matrix of dynamically allocated size.
	   * @param   matrix    The input 1 column matrix
	   * @param matrix_t  The transpose matrix result
	   */
	  public double[][] transpose_vec(double matrix[])
	  {
	    int row = matrix.length;
	    
	    double matrix_t[][] = new double[1][row];
	    
	    for(int i=0;i<row;i++)
	    {
	        matrix_t[1][i] = matrix[i];
	    }
	    return matrix_t;
	  }

	  /**
	   * multiply function
	   * Multiplication of two matrices which have a size dynamically allocated.
	   * @param   matrix1     The first matrix
	   * @param matrix2         The second matrix
	   * @param   result          The output matrix
	   */
	  public double[][] multiply(double matrix1[][], double matrix2[][])
	  {
	    int row1 = matrix1.length;
	    int col1 = matrix1[0].length;
	    
	    int row2 = matrix2.length;
	    int col2 = matrix2[0].length;
	    
	    double[][] result = new double[row1][col2];
	    
	    if(col1 == row2)
	    {
	      for(int i=0;i<row1;i++)
	        for(int j=0;j<col2;j++)
	        {
	          result[i][j]=0;
	          for(int k=0;k<col1;k++)
	            result[i][j] += matrix1[i][k]*matrix2[k][j];
	        }
	    }
	    else
	      ;
	    
	    return result;

	  }

	  /**
	   * multiply_vecxmat function
	   * Multiplication of column vector with a matrix
	   * @param   matrix1     The column vector
	   * @param matrix2         The matrix
	   * @param   result          The output matrix
	   */
	  public double[] multiply_vecxmat(double matrix1[], double matrix2[][])
	  {
	    int col1 = matrix1.length;
	    
	    int row2 = matrix2.length;
	    int col2 = matrix2[0].length;
	    
	    double[] result = new double[col2];
	    
	    if(col1 == row2)
	      for(int j=0;j<col2;j++)
	      {
	        result[j]=0;
	        for(int k=0;k<col1;k++)
	          result[j] += matrix1[k]*matrix2[k][j];
	      }
	    else
	      ;
	    
	    return result;
	  }

	  /**
	   * multiply_matxvec function
	   * Multiplication of matrix with a row vector.
	   * @param   matrix1     The matrix
	   * @param matrix2         The row vector
	   * @param   result          The output matrix
	   */
	  public double[] multiply_matxvec(double matrix1[][], double matrix2[])
	  {
	    int row1 = matrix1.length;
	    int col1 = matrix1[0].length;
	    
	    int row2 = matrix2.length;
	    
	    double[] result = new double[row1];
	    
	    if(col1 == row2)
	      for(int i=0;i<row1;i++)
	      {
	          result[i]=0;
	          for(int k=0;k<col1;k++)
	            result[i]+= matrix1[i][k]*matrix2[k];
	      }
	    else
	      ;
	    return result;
	  }


	  /**
	   * lorentz_4_4 function
	   * Lorentz product computation : <a,b> = Trans(a)*M*b; In our case, a and b will be always 1x4 matrix
	   * @param   matrix1   The first matrix
	   * @param matrix2   The second matrix
	   * @return        The computed Lorentz product
	   */
	  double lorentz_4_4(double matrix1[], double matrix2[])
	  {
	    /* M =  [1 0 0 0]
	        [0 1 0 0]
	        [0 0 1 0]
	        [0 0 0-1]
	    */
	    return matrix1[0]*matrix2[0] + matrix1[1]*matrix2[1] + matrix1[2]*matrix2[2] - matrix1[3]*matrix2[3];
	  }

	  /**
	   * subtract_mat function
	   * Subtraction of matrix2 from matrix1
	   * @param   matrix1     The matrix from where the subtraction is made
	   * @param matrix2         The matrix that is subtracted
	   * @param   result          The output matrix
	   */
	  public double[][] subtract_mat(double matrix1[][], double matrix2[][])
	  {
	    int row1 = matrix1.length;
	    int col1 = matrix1[0].length;
	    
	    int row2 = matrix2.length;
	    int col2 = matrix2[0].length;
	    
	    double[][] result = new double[row1][col1];
	    
	    if(row1 == row2 & col1 == col2)
	      for(int i = 0; i < row1; i++)
	        for(int j = 0; j < col1; j++)
	          result[i][j] = matrix1[i][j] - matrix2[i][j];

	    else
	      ;
	    
	    return result;
	  }

	  /**
	   * subtract_vec function
	   * Subtraction of two single dimension matrices
	   * @param   matrix1     The matrix from where the subtraction is made
	   * @param matrix2         The matrix that is subtracted
	   * @param   result          The output matrix
	   */
	  public double[] subtract_vec(double matrix1[], double matrix2[])
	  {
	    int row1 = matrix1.length;
	    int row2 = matrix2.length;
	    
	    double[] result = new double[row1];

	    if(row1 == row2)
	      for(int i = 0; i < row1; i++)
	          result[i] = matrix1[i] - matrix2[i];
	    else
	      ;
	    return result;

	  }

		
}

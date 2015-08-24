package com.ec.egnossdk;




public  class Matrix_square{
    public double[][] matrix;
    int dim;
    Matrix_square(int n,double[][] matrice){
        dim=n;
        matrix=new double[n][n];
        matrix=matrice;
        
    }
    
    private static double determinante2Per2(double[][] matrice){
    return matrice[0][0]*matrice[1][1]-matrice[1][0]*matrice[0][1];
}

public double determinante(){
    int i;
    int j;
    int k;
    double det=0;
    double[][] mx=new double[dim-1][dim-1];
    if (dim==2){
        det=Matrix_square.determinante2Per2(matrix);
    }
    
    else if(dim==1){
        det=matrix[0][0];
    }
    
    else{
        double[][]matrixSupport=new double[dim][dim];
        for (i=0;i<dim;i++){
            for(j=0;j<dim;j++){
                matrixSupport[i][j]=matrix[i][j];
            }
        }
        
     // cerco di ricondurre la matrice ad avere la prima riga con tutti zeri 
        //tranne eventualmente un solo elemento
        boolean c=false;
        i=0;
            while (c==false){
                if(i==dim){
                    c=true;
                }
                else{
                    if(matrix[i][0]==0){
                        i++;
                    }
                    
                    else{
                        for(j=0;j<i;j++){
                            double x=-matrixSupport[j][0]/matrix[i][0];
                            for(k=0;k<dim;k++){
                                matrixSupport[j][k]=matrixSupport[j][k]+matrix[i][k]*x;
                            }
                            
                        }
                        for(j=i+1;j<dim;j++){
                            double x=-matrix[j][0]/matrix[i][0];
                            for(k=0;k<dim;k++){
                                matrixSupport[j][k]=matrixSupport[j][k]+matrix[i][k]*x;
                            }
                        }
                        c=true;
                }
            }
            }
            for(i=0;i<=dim-1;i++){
                if(matrixSupport[i][0]==0){
                }
                else{
                    // crea la matrice ottenuta eliminando la i-ma colonna della matrice di partenza
                        mx=Matrix_square.eliminaRigaColonna(matrixSupport,dim,i,0).matrix;
                        det+=(matrixSupport[i][0]*Math.pow(-1,i+2)*(new 
    					       Matrix_square(dim-1,mx).determinante()));
                    }
                }
            }
            return det;
        }

public Matrix_square matriceAggiunta(){
    int i;
    int j;
    double[][] aggiunta=new double[dim][dim];
    Matrix_square mTrasposta=trasposta();
    for (i=0;i<dim;i++){
        for(j=0;j<dim;j++){
            aggiunta[j][i]=Math.pow(-1,i+j+2)*Matrix_square.
		eliminaRigaColonna(mTrasposta.matrix,dim,j,i).determinante();
        }
    }
    return new Matrix_square(dim,aggiunta);
}


public Matrix_square matriceInversa(){
    int i;
    int j;
    double d=determinante();
    if (d==0){
        return new Matrix_square(dim,matrix);
    }
    else{
        double[][] mInversa=new double[dim][dim];
        for (i=0;i<dim;i++){
            for(j=0;j<dim;j++){
                mInversa[i][j]=matriceAggiunta().matrix[i][j]/d;
            }
        }
        return new Matrix_square(dim,mInversa);
                
    }
}


public Matrix_square trasposta(){
    double [][]mx=new double[dim][dim];
    int i;
    int j;
    for (i=0;i<dim;i++){
        for (j=0;j<dim;j++){
            mx[i][j]=matrix[j][i];
        }
    }
    return new Matrix_square(dim,mx);
}




public static Matrix_square eliminaRigaColonna(double[][] matr,int dim, int colonna,
								int riga){
    double[][] matrice=new double[dim-1][dim-1];
    int i;
    int j;
    for(i=0;i<dim;i++){
        for(j=0;j<dim;j++){
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
    return new Matrix_square(dim-1,matrice);
}


}
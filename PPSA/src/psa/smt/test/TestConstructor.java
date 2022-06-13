package psa.smt.test;

import psa.smt.entity.MatrixSkyLine;


public class TestConstructor {
	public static void main(String[] args) {
		MatrixSkyLine matA = new MatrixSkyLine(new double[][]{
				   { 6.0, 15.0, 55.0 },
				   { 15.0, 55.0, 225.0 },
				   { 55.0, 225.0, 979.0 }
				});
		System.out.println(matA);
		
		MatrixSkyLine matB = new MatrixSkyLine(new double[][]{
				   { 15.0, -5.0  , 0.0 , -5  },
				   { -5.0 , 12.0 , -2.0  , 0 },
				   { 0.0, -2.0  , 6.0 , -2   },
				   { -5   , 0   , -2.0 , 9.0 },
				});
		System.out.println(matB);
		System.out.println("Cholesky Decomposed : ");
		MatrixSkyLine decomposeByCholesky = matB.decomposeByCholesky();
		System.out.println(decomposeByCholesky.getLowerTraingularMatrix());
	}
}

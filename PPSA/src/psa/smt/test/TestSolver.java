package psa.smt.test;

import psa.smt.decomposer.CholeskyDecompositor;
import psa.smt.decomposer.ForwardBackwardSubstitutor;
import psa.smt.entity.MatrixSkyLine;

public class TestSolver {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/**
		 * Problem from pg.279 of 'numeric methods for eng...'
		 */
		MatrixSkyLine matA = new MatrixSkyLine(new double[][]{
				   { 6.0, 15.0, 55.0 },
				   { 15.0, 55.0, 225.0 },
				   { 55.0, 225.0, 979.0 }
				});

		MatrixSkyLine decomposeByCholesky = matA.decomposeByCholesky();
		
		double[] bVect = new double[]{5,6,15};
		ForwardBackwardSubstitutor substitutor = new ForwardBackwardSubstitutor(decomposeByCholesky, bVect);
		double[] solvedXVals = substitutor.solve();
		
		for (int i = 0; i < solvedXVals.length; i++) {
			System.out.print(solvedXVals[i]+" , ");
		}
		System.out.println("Sec : \n");
		
		double[] bVectSex = new double[]{4,9,24};
		ForwardBackwardSubstitutor substitutorSec = new ForwardBackwardSubstitutor(decomposeByCholesky, bVectSex);
		double[] solvedXValsSec = substitutorSec.solve();
		
		for (int i = 0; i < solvedXValsSec.length; i++) {
			System.out.print(solvedXValsSec[i]+" , ");
		}
		
		
	}

}

package com.ppsa.util;

import java.util.Map;
import java.util.Set;

import com.psa.entity.VectorMatrix;

public class EntityTranslationUtil {
	public static double[][] getDoubleArrayFromMatrix(VectorMatrix vectorMatrix) {
		int sizeOfVector = vectorMatrix.getSizeOfVector();
		Map<Integer, Double> vectorValues = vectorMatrix.getVectorValues();
		double[][] matrixValues = new double[sizeOfVector][1];
		Set<Integer> keySet = vectorValues.keySet();

		int i=0;
		for (Integer vectorKey : keySet) {
			Double value = vectorValues.get(vectorKey);
			matrixValues[i++][0]=value;
		}
		
		return matrixValues;
	}

	public static double[][] getDoubleArrayFromMatrixAndInverseIt(
			VectorMatrix vectorMatrix) {
		int sizeOfVector = vectorMatrix.getSizeOfVector();
		Map<Integer, Double> vectorValues = vectorMatrix.getVectorValues();
		double[][] matrixValues = new double[1][sizeOfVector];
		Set<Integer> keySet = vectorValues.keySet();

		int i=0;
		for (Integer vectorKey : keySet) {
			Double value = vectorValues.get(vectorKey);
			matrixValues[0][i++]=value;
		}
		
		return matrixValues;
	}
	
	public static Double[][] getDoubleValuedMAtrix(double[][] matrix) {
		int rowSize = matrix.length;
		int colSize = matrix[0].length;
		Double[][] doubleMatrix = new Double[rowSize][colSize];
		
		for (int i = 0; i < rowSize; i++) {
			for (int j = 0; j < colSize; j++) {
				doubleMatrix[i][j] = matrix[i][j];
			}
		}
		
		return doubleMatrix;
	}
	
	public static double[][] setValuesToSimpleMatrix(
			Double[][] reducedGlobalStiffnessMatrix) {
		int rowSize = reducedGlobalStiffnessMatrix.length;
		int colSize = rowSize; //reducedGlobalStiffnessMatrix[0].length;
		double[][] newMatrix = new double[rowSize][colSize];
		for (int i = 0; i < rowSize; i++) {
			for (int j = 0; j < colSize; j++) {
				newMatrix[i][j] = reducedGlobalStiffnessMatrix[i][j];
			}
		}
		return newMatrix;
	}

	public static double[] getVectorAsArray(VectorMatrix forceVector) {
		double[] values = new double[forceVector.getSizeOfVector()]; 
		Map<Integer, Double> vectorValues = forceVector.getVectorValues();
		Set<Integer> keySet = vectorValues.keySet();
		int i=0;
		for (Integer key : keySet) {
			values[i]=vectorValues.get(key);
			i++;
		}
		return values;
	}

	public static Double[][] get2DMatrixFromSingleArray(double[] solvedVals) {
		Double[][] valsReturn = new Double[solvedVals.length][1];
		for (int i = 0; i < solvedVals.length; i++) {
			valsReturn[i][0] = solvedVals[i];
		}
		return valsReturn;
	}

	public static double[][] get2DMatrixFromSingleArray_simple(
			double[] localForceVals) {
		double[][] valsReturn = new double[localForceVals.length][1];
		for (int i = 0; i < localForceVals.length; i++) {
			valsReturn[i][0] = localForceVals[i];
		}
		return valsReturn;
	}

	public static double[] getVectorFromThisMat(double[][] arrayCopy) {
		int rowSize = arrayCopy.length;
		int columnSize = arrayCopy[0].length;
		if(columnSize!=1)
			throw new RuntimeException("Not a vector to transform from matrix..");
	

		double[] valsReturn = new double[rowSize];
		for (int i = 0; i < rowSize; i++) {
			valsReturn[i] = arrayCopy[i][0];
		}
		return valsReturn;
	
	}
}

package com.ppsa.math;

import java.util.List;

import com.ppsa.exception.PPSAMathException;
import com.psa.entity.VectorMatrix;

public interface MathOperation {
	
	/**
	 * Remove rows and columns as mentioned in Param.
	 * @param matrix
	 * @param indexesToBeRemoved
	 */
	public Double[][] reduceMatrix(Double[][] matrixToBeAltered,List<Integer> indexesToBeRemoved);
	public double[][] getSubMatix(Double[][] matrixToBeAltered,int rowBeginIndex,int rowEndIndex, int colBeginIndex, int colEndIndex);
	public Double[][] getDisplacementVector(Double[][] reducedGlobalStiffnessMatrix,VectorMatrix forceVector) throws PPSAMathException;
	public double getReactionAtSupport(double[][] aRowInStiffnessMatrix,double[][] fullNodalDispVector) throws PPSAMathException;
	double getAxialMemberForce(double[][] csMatric,
			double[][] doubleArrayFromMatrix) throws PPSAMathException;
	public double[][] getValuesOfSpecifiedColumn(Double[][] stiffnessMatrix,
			int indexInMatrix) throws PPSAMathException;
	public double[] multiplyMatixVector(double[][] matrixA,double[] matrixB) throws PPSAMathException;
	
}

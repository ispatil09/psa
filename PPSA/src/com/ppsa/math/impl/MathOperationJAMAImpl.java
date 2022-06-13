package com.ppsa.math.impl;

import java.util.ArrayList;
import java.util.List;

import psa.smt.decomposer.ForwardBackwardSubstitutor;
import psa.smt.entity.MatrixSkyLine;
import Jama.Matrix;

import com.ppsa.exception.PPSAMathException;
import com.ppsa.math.MathOperation;
import com.ppsa.math.matrix.MatrixMathematics;
import com.ppsa.math.matrix.NoSquareException;
import com.ppsa.util.EntityTranslationUtil;
import com.psa.entity.VectorMatrix;

public class MathOperationJAMAImpl implements MathOperation {

	@Override
	public Double[][] reduceMatrix(Double[][] matrixToBeAltered,
			List<Integer> indexesToBeRetained) {

		int sizeOfIndexesToBeRetained = indexesToBeRetained.size();
		int alteredMatrixLength = sizeOfIndexesToBeRetained;
		int originalMatrixSize = matrixToBeAltered.length;

		Double[][] newMatrix = new Double[alteredMatrixLength][alteredMatrixLength];

		int newMatrixRow = 0;
		int newMatrixCol = 0;
		ArrayList<Integer> tempListForRow = new ArrayList<Integer>(
				indexesToBeRetained);
		ArrayList<Integer> tempListForCol = new ArrayList<Integer>(
				indexesToBeRetained);
		for (int rowNum = 0; rowNum < originalMatrixSize; rowNum++) {
			boolean rowAllowAddFlag = false;
			for (int k = 0; k < tempListForRow.size(); k++) {
				Integer coOrdNumForRow = tempListForRow.get(k);
				if (rowNum == coOrdNumForRow) {
					rowAllowAddFlag = true;
					tempListForRow.remove(coOrdNumForRow);
					// rowSkipCount++;
					break;
				}

			}
			for (int colNum = 0; colNum < originalMatrixSize; colNum++) {

				boolean colAddAllowFlag = false;
				for (int k = 0; k < tempListForCol.size(); k++) {
					Integer coOrdNumForCol = tempListForCol.get(k);
					if (colNum == coOrdNumForCol) {
						colAddAllowFlag = true;
						tempListForCol.remove(coOrdNumForCol);
						// colSkipCount++;
						break;
					}
				}

				if (rowAllowAddFlag && colAddAllowFlag) {

					newMatrix[newMatrixRow][newMatrixCol] = matrixToBeAltered[rowNum][colNum];
					newMatrixCol++;
				}
			}
			newMatrixCol = 0;
			tempListForCol.addAll(indexesToBeRetained);
			if (rowAllowAddFlag)
				newMatrixRow++;
		}

		return newMatrix;
	}

	@Override
	public Double[][] getDisplacementVector(
			Double[][] reducedGlobalStiffnessMatrix, VectorMatrix forceVector)
			throws PPSAMathException {
		
		//return codeSportImpl(reducedGlobalStiffnessMatrix, forceVector);

		return jamaImpl(reducedGlobalStiffnessMatrix, forceVector);

		//return la4jImpl(reducedGlobalStiffnessMatrix, forceVector);

		//return useScilabImpl(reducedGlobalStiffnessMatrix, forceVector);
		
//		return skylineMatImpl(reducedGlobalStiffnessMatrix, forceVector);
	}

	private Double[][] skylineMatImpl(Double[][] reducedGlobalStiffnessMatrix,
			VectorMatrix forceVector) throws PPSAMathException {
		// int sizeOfVector = forceVector.getSizeOfVector();

		// Inputs to Scilab.
		double[][] simpleDoubleMatrix = EntityTranslationUtil
				.setValuesToSimpleMatrix(reducedGlobalStiffnessMatrix);
		double[] vectorMatrix = EntityTranslationUtil
				.getVectorAsArray(forceVector);
		double[] solvedVals = null;
		// setValuesSimpleVector(forceVector);

		// ------------CODE SPORT-----------------------------------

		/**
		 * "If" check is to be performed, in case of all nodes fixed,
		 * there will be no data in reduced stiffness matrix since all coOrdinates are restrained.
		 * Which in turn causes nullPointerException in creating jama.Matrix object.
		 * At line "n = A[0].length;". 
		 */
		if (simpleDoubleMatrix.length != 0) {
			long nanoTimeFirst = System.nanoTime();
			MatrixSkyLine matrixSkyLine = new MatrixSkyLine(simpleDoubleMatrix);
			long nanoTimeSec = System.nanoTime();
			/*System.out.println("Contructor Time : "+(nanoTimeSec-nanoTimeFirst));*/
			
			//String lowerTraingularMatrix = matrixSkyLine.getLowerTraingularMatrix();
			
			//Matrix redForceVector = new Matrix(vectorMatrix);

			//Matrix inverseRedGlobalMatrix = null;

			try {
				long nanoTime1 = System.nanoTime();
				
				//double det = redGlobalMatrix.det();
				/*long nanoTime2 = System.nanoTime();
				System.out.println("TD (mS) det : "+(nanoTime2-nanoTime1)/1000000);
				//System.out.println("det of redM = " + det);
				inverseRedGlobalMatrix = redGlobalMatrix.inverse();*/
				
				MatrixSkyLine decomposeByCholesky = matrixSkyLine.decomposeByCholesky();
				//decomposeByCholesky.getLowerTraingularMatrix();
				ForwardBackwardSubstitutor substitutor = new ForwardBackwardSubstitutor(decomposeByCholesky, vectorMatrix);
				
				long nanoTime2 = System.nanoTime();
				/*System.out.println("TD (mS) decompose time : "+(nanoTime2-nanoTime1)/1000000);*/
				
				solvedVals = substitutor.solve();
				
				long nanoTime3 = System.nanoTime();
				/*System.out.println("TD (mS) eqn solve : "+(nanoTime3-nanoTime2)/1000000);*/
				/*System.out.println("Inversed Matrix : \n"
						+ inverseRedGlobalMatrix.getArray());*/
			} catch (Exception e) {
				String message = e.getMessage();
				e.printStackTrace();
				throw new PPSAMathException(message);
			}

			/*Matrix nodeDispVector = inverseRedGlobalMatrix
					.times(redForceVector);

			double[][] values = nodeDispVector.getArrayCopy();*/
			long nanoTime1 = System.nanoTime();
			Double[][] doubleValuedMAtrix = EntityTranslationUtil
					.get2DMatrixFromSingleArray(solvedVals);

			long nanoTime2 = System.nanoTime();
			/*System.out.println("TD (mS) inv Converstion : "+(nanoTime2-nanoTime1)/1000000);*/
			return doubleValuedMAtrix;
		}
		else
			return new Double[0][0];
	}

	private Double[][] la4jImpl(Double[][] reducedGlobalStiffnessMatrix,
			VectorMatrix forceVector) throws PPSAMathException {
		// int sizeOfVector = forceVector.getSizeOfVector();

		// Inputs to Scilab.
		double[][] simpleDoubleMatrix = EntityTranslationUtil
				.setValuesToSimpleMatrix(reducedGlobalStiffnessMatrix);
		double[][] vectorMatrix = EntityTranslationUtil
				.getDoubleArrayFromMatrix(forceVector);
		// setValuesSimpleVector(forceVector);

		// ------------CODE SPORT-----------------------------------

		/**
		 * "If" check is to be performed, in case of all nodes fixed,
		 * there will be no data in reduced stiffness matrix since all coOrdinates are restrained.
		 * Which in turn causes nullPointerException in creating jama.Matrix object.
		 * At line "n = A[0].length;". 
		 */
		if (simpleDoubleMatrix.length != 0) {/*
			//Matrix redGlobalMatrix = new Matrix(simpleDoubleMatrix);
			CRSMatrix redGlobalMatrix = new CRSMatrix(simpleDoubleMatrix);
			org.la4j.matrix.Matrix redForceVector = new Basic2DMatrix(vectorMatrix);
			//Matrix redForceVector = new Matrix(vectorMatrix);

			org.la4j.matrix.Matrix inverseRedGlobalMatrix = null;

			try {
				long nanoTime2 = System.nanoTime();
				
				MatrixInverter inverterMat = redGlobalMatrix.withInverter(LinearAlgebra.INVERTER);
				inverseRedGlobalMatrix = inverterMat.inverse(LinearAlgebra.CRS_FACTORY);
				
				//inverseRedGlobalMatrix = redGlobalMatrix.inverse();
				long nanoTime3 = System.nanoTime();
				System.out.println("TD (mS) inv : "+(nanoTime3-nanoTime2)/1000000);
				System.out.println("Inversed Matrix : \n"
						+ inverseRedGlobalMatrix.getArray());
			} catch (Exception e) {
				String message = e.getMessage();
				e.printStackTrace();
				throw new PPSAMathException(message);
			}

			org.la4j.matrix.Matrix nodeDispVector = inverseRedGlobalMatrix
					.multiply(redForceVector);

			//double[][] values = null ; //nodeDispVector.toA
			double values[][] = ((DenseMatrix) nodeDispVector.copy(Matrices.DEFAULT_DENSE_FACTORY)).toArray();
			Double[][] doubleValuedMAtrix = EntityTranslationUtil
					.getDoubleValuedMAtrix(values);

			return doubleValuedMAtrix;
		*/
			return null;
		}
		else
			return new Double[0][0];
	
		//return null;
	}

	private Double[][] codeSportImpl(Double[][] reducedGlobalStiffnessMatrix,
			VectorMatrix forceVector) throws PPSAMathException {
		//int sizeOfVector = forceVector.getSizeOfVector();
		
		// Inputs to Scilab.
		double[][] simpleDoubleMatrix = EntityTranslationUtil.setValuesToSimpleMatrix(
				reducedGlobalStiffnessMatrix);
		double[][] vectorMatrix = EntityTranslationUtil.getDoubleArrayFromMatrix(forceVector);
		//setValuesSimpleVector(forceVector);
		
		
		//------------CODE SPORT-----------------------------------
		
		com.ppsa.math.matrix.Matrix redGlobalMatrix = new com.ppsa.math.matrix.Matrix(simpleDoubleMatrix);
		com.ppsa.math.matrix.Matrix redForceVector = new com.ppsa.math.matrix.Matrix(vectorMatrix);
		
		com.ppsa.math.matrix.Matrix inverseRedGlobalMatrix;
		try {
			inverseRedGlobalMatrix = MatrixMathematics.inverse(redGlobalMatrix);
		} catch (NoSquareException e) {
			throw new PPSAMathException(e.getMessage());
		}
		
		com.ppsa.math.matrix.Matrix nodeDispVector = MatrixMathematics.multiply(inverseRedGlobalMatrix, redForceVector);
		
		double[][] values = nodeDispVector.getValues();
		
		Double[][] doubleValuedMAtrix = EntityTranslationUtil.getDoubleValuedMAtrix(values);
		
		return doubleValuedMAtrix;}
	
	private Double[][] jamaImpl(Double[][] reducedGlobalStiffnessMatrix,
			VectorMatrix forceVector) throws PPSAMathException {
		// int sizeOfVector = forceVector.getSizeOfVector();

		// Inputs to Scilab.
		double[][] simpleDoubleMatrix = EntityTranslationUtil
				.setValuesToSimpleMatrix(reducedGlobalStiffnessMatrix);
		double[][] vectorMatrix = EntityTranslationUtil
				.getDoubleArrayFromMatrix(forceVector);
		// setValuesSimpleVector(forceVector);

		// ------------CODE SPORT-----------------------------------

		/**
		 * "If" check is to be performed, in case of all nodes fixed,
		 * there will be no data in reduced stiffness matrix since all coOrdinates are restrained.
		 * Which in turn causes nullPointerException in creating jama.Matrix object.
		 * At line "n = A[0].length;". 
		 */
		if (simpleDoubleMatrix.length != 0) {
			Matrix redGlobalMatrix = new Matrix(simpleDoubleMatrix);
			Matrix redForceVector = new Matrix(vectorMatrix);

			Matrix inverseRedGlobalMatrix = null;

			try {
				long nanoTime1 = System.nanoTime();

				//double det = redGlobalMatrix.det();
				long nanoTime2 = System.nanoTime();
				/*System.out.println("TD (mS) det : "+(nanoTime2-nanoTime1)/1000000);*/
				//System.out.println("det of redM = " + det);
				inverseRedGlobalMatrix = redGlobalMatrix.inverse();
				long nanoTime3 = System.nanoTime();
				/*System.out.println("TD (mS) inv : "+(nanoTime3-nanoTime2)/1000000);*/
				/*System.out.println("Inversed Matrix : \n"
						+ inverseRedGlobalMatrix.getArray());*/
			} catch (Exception e) {
				String message = e.getMessage();
				e.printStackTrace();
				throw new PPSAMathException(message);
			}

			Matrix nodeDispVector = inverseRedGlobalMatrix
					.times(redForceVector);

			double[][] values = nodeDispVector.getArrayCopy();

			Double[][] doubleValuedMAtrix = EntityTranslationUtil
					.getDoubleValuedMAtrix(values);

			return doubleValuedMAtrix;
		}
		else
			return new Double[0][0];
	}

	private Double[][] useScilabImpl(Double[][] reducedGlobalStiffnessMatrix,
			VectorMatrix forceVector) throws PPSAMathException {/*
		int sizeOfVector = forceVector.getSizeOfVector();

		if(reducedGlobalStiffnessMatrix.length!=0) {
		// Inputs to Scilab.
		double[][] simpleDoubleMatrix = EntityTranslationUtil
				.setValuesToSimpleMatrix(reducedGlobalStiffnessMatrix);
		double[][] vectorMatrix = EntityTranslationUtil
				.getDoubleArrayFromMatrix(forceVector);

		// Variables to hold Scilab output.
		ScilabDouble dispVector = new ScilabDouble();
		try {
			Scilab sci = new Scilab();
			if (sci.open()) {

				ScilabDouble reducedGlobalMatrix = new ScilabDouble(
						simpleDoubleMatrix);
				ScilabDouble reducedForceVector = new ScilabDouble(vectorMatrix);

				sci.put("redGlobalMatrix", reducedGlobalMatrix);
				sci.put("redForceVector", reducedForceVector);

				sci.exec("disp(redGlobalMatrix)");

				sci.exec("inverseRedGlobalMatrix=redGlobalMatrix^-1");
				sci.exec("disp(inverseRedGlobalMatrix)");

				sci.exec("dispVector=inverseRedGlobalMatrix*redForceVector");
				sci.exec("disp(dispVector)");

				dispVector = (ScilabDouble) sci.get("dispVector");

				sci.close();

			} else {
				throw new PPSAMathException("Could not start JavaSCI");
			}
		} catch (org.scilab.modules.javasci.JavasciException e) {
			// throw new PPSAMathException("JavaSci thrown exception..");
			e.printStackTrace();
		}
		double[][] realPart = dispVector.getRealPart();
		Double[][] nodalDisplacementVector = EntityTranslationUtil
				.getDoubleValuedMAtrix(realPart);
		return nodalDisplacementVector;
		}
		else */
			return new Double[0][0];
	}

	@Override
	public double getReactionAtSupport(double[][] aRowInStiffnessMatrix,
			double[][] fullNodalDispVector) throws PPSAMathException {

		Matrix rowValuesOfMatrix = new Matrix(aRowInStiffnessMatrix);
		Matrix nodalDispVector = new Matrix(fullNodalDispVector);

		Matrix reactionValue = rowValuesOfMatrix.times(nodalDispVector);

		double[][] arrayCopy = reactionValue.getArrayCopy();
		return arrayCopy[0][0];
	}

	@Override
	public double getAxialMemberForce(double[][] csMatric,
			double[][] doubleArrayFromMatrix) throws PPSAMathException {

		Matrix nodalDispVector = new Matrix(doubleArrayFromMatrix);
		Matrix rowInGlobalMatrix = new Matrix(csMatric);
		
		Matrix reactionValue = nodalDispVector.times(rowInGlobalMatrix);

		double[][] arrayCopy = reactionValue.getArrayCopy();
		return arrayCopy[0][0];

	}

	@Override
	public double[][] getValuesOfSpecifiedColumn(Double[][] stiffnessMatrix,
			int indexInMatrix) throws PPSAMathException {
		double[][] matrixSimple = EntityTranslationUtil
				.setValuesToSimpleMatrix(stiffnessMatrix);

		double[][] matrixReturn = new double[1][stiffnessMatrix.length];

		for (int j = 0; j < matrixSimple[0].length; j++) {
				matrixReturn[0][j] = matrixSimple[indexInMatrix][j];
		}
		return matrixReturn;
	}

	public double[][] getTransformedElementMatrix(double[][] transMatrix,
			double[][] localStiffMatrix) {
		
		Matrix transMat = new Matrix(transMatrix);
		Matrix localStiffMat = new Matrix(localStiffMatrix);
		
		Matrix transposedTransMat = transMat.transpose();
		
		Matrix firstMult = transposedTransMat.times(localStiffMat);
		Matrix finalMat = firstMult.times(transMat);
		return finalMat.getArray();
	}

	public double[] transformLocalForceVector(double[][] transformationMatrix, double[] localForceVals) {
		double[][] localForceMat = EntityTranslationUtil.get2DMatrixFromSingleArray_simple(localForceVals);
		
		
		Matrix transMat = new Matrix(transformationMatrix); 
		Matrix forceMat = new Matrix(localForceMat); 
		
		Matrix transposedTransMat = transMat.transpose();
		
		Matrix globalForceMat = transposedTransMat.times(forceMat);
		double[][] arrayCopy = globalForceMat.getArrayCopy();
		double[] vectorVals = EntityTranslationUtil.getVectorFromThisMat(arrayCopy);
		
		return vectorVals;
	}

	public double[] transformGlobalForceVector(double[][] transformationMatrix,
			double[] globalForceVals) {
		double[][] globalForceMat = EntityTranslationUtil.get2DMatrixFromSingleArray_simple(globalForceVals);
		
		
		Matrix transMat = new Matrix(transformationMatrix); 
		Matrix forceMat = new Matrix(globalForceMat);

		//Matrix transposedTransMat = transMat.transpose();
		
		Matrix localForceMat = transMat.times(forceMat);
		double[][] arrayCopy = localForceMat.getArrayCopy();
		double[] vectorVals = EntityTranslationUtil.getVectorFromThisMat(arrayCopy);
		
		return vectorVals;
	}

	@Override
	public double[][] getSubMatix(Double[][] matrixToBeAltered,
			int rowBeginIndex, int rowEndIndex, int colBeginIndex,
			int colEndIndex) {
		double[][] subMatrix = new double[rowEndIndex-rowBeginIndex+1][colEndIndex-colBeginIndex+1];
		int rowIndAdder = 0;
		for (int i = rowBeginIndex; i < rowEndIndex+1; i++) {
			int colIndAdder = 0;
			for (int j = colBeginIndex; j < colEndIndex+1; j++) {
				subMatrix[rowIndAdder][colIndAdder]=matrixToBeAltered[i][j];
				colIndAdder++;
			}
			rowIndAdder++;
		}
		//This condition arises when all the Coordinates are restrained
		if(rowEndIndex==-1) {
			subMatrix = new double[1][colEndIndex-colBeginIndex+1];
			int colIndAdder = 0;
			for (int j = colBeginIndex; j < colEndIndex+1; j++) {
				subMatrix[0][colIndAdder]=0;
				colIndAdder++;
			}
		}
		
		return subMatrix;
	}


	@Override
	public double[] multiplyMatixVector(double[][] matrixA, double[] matrixB) throws PPSAMathException {
		//int a = matrixA.length;
		int width= matrixA.length;
		int widthSec= matrixA[0].length;
		int depthB = matrixB.length;
		
		if(widthSec!=depthB)
			throw new PPSAMathException("Matrix dimensions not agreeing...");
		
		double[] matrixOut = new double[width];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < depthB; j++) {
				matrixOut[i]+=matrixA[i][j]*matrixB[j];
			}
		}
		return matrixOut;
	}
}

package com.ppsa.math.impl;

import java.util.ArrayList;
import java.util.List;

import com.ppsa.exception.PPSAMathException;
import com.ppsa.math.MathOperation;
import com.ppsa.math.matrix.Matrix;
import com.ppsa.math.matrix.MatrixMathematics;
import com.ppsa.math.matrix.NoSquareException;
import com.ppsa.util.EntityTranslationUtil;
import com.psa.entity.VectorMatrix;

public class MathOperationCodeSportImpl { /* implements MathOperation {

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
			Double[][] reducedGlobalStiffnessMatrix, VectorMatrix forceVector) throws PPSAMathException {
		//int sizeOfVector = forceVector.getSizeOfVector();
		
		// Inputs to Scilab.
		double[][] simpleDoubleMatrix = EntityTranslationUtil.setValuesToSimpleMatrix(
				reducedGlobalStiffnessMatrix);
		double[][] vectorMatrix = EntityTranslationUtil.getDoubleArrayFromMatrix(forceVector);
		//setValuesSimpleVector(forceVector);
		
		
		//------------CODE SPORT-----------------------------------
		
		Matrix redGlobalMatrix = new Matrix(simpleDoubleMatrix);
		Matrix redForceVector = new Matrix(vectorMatrix);
		
		Matrix inverseRedGlobalMatrix;
		try {
			inverseRedGlobalMatrix = MatrixMathematics.inverse(redGlobalMatrix);
		} catch (NoSquareException e) {
			throw new PPSAMathException(e.getMessage());
		}
		
		Matrix nodeDispVector = MatrixMathematics.multiply(inverseRedGlobalMatrix, redForceVector);
		
		double[][] values = nodeDispVector.getValues();
		
		Double[][] doubleValuedMAtrix = EntityTranslationUtil.getDoubleValuedMAtrix(values);
		
		return doubleValuedMAtrix;
	}

	@Override
	public double getReactionAtSupport(double[][] aRowInStiffnessMatrix,
			double[][] fullNodalDispVector) throws PPSAMathException {

		Matrix rowValuesOfMatrix = new Matrix(aRowInStiffnessMatrix);
		Matrix nodalDispVector = new Matrix(fullNodalDispVector);

		Matrix reactionValue = MatrixMathematics.multiply(rowValuesOfMatrix,
				nodalDispVector);

		return reactionValue.getValueAt(0, 0);
	}

	@Override
	public double getAxialMemberForce(double[][] csMatric,
			double[][] doubleArrayFromMatrix) throws PPSAMathException {

		Matrix nodalDispVector = new Matrix(doubleArrayFromMatrix);
		Matrix rowInGlobalMatrix = new Matrix(csMatric);

		Matrix reactionValue = MatrixMathematics.multiply(nodalDispVector,
				rowInGlobalMatrix);

		return reactionValue.getValueAt(0, 0);

	}
*/}

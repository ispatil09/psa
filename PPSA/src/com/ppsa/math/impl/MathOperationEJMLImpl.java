package com.ppsa.math.impl;


public class MathOperationEJMLImpl { /*implements MathOperation {

	@Override
	public Double[][] reduceMatrix(Double[][] matrixToBeAltered,
			List<Integer> indexesToBeRetained) {

		int sizeOfIndexesToBeRetained = indexesToBeRetained.size();
		int alteredMatrixLength = sizeOfIndexesToBeRetained;
		int originalMatrixSize = matrixToBeAltered.length;

		Double[][] newMatrix = new Double[alteredMatrixLength][alteredMatrixLength];

		int newMatrixRow=0;
		int newMatrixCol=0;
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
					//rowSkipCount++;
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
						//colSkipCount++;
						break;
					}
				}

				if (rowAllowAddFlag && colAddAllowFlag) {
					
					newMatrix[newMatrixRow][newMatrixCol] = matrixToBeAltered[rowNum][colNum];
					newMatrixCol++;
				}
			}
			newMatrixCol=0;
			tempListForCol.addAll(indexesToBeRetained);
			if(rowAllowAddFlag)
				newMatrixRow++;
		}

		return newMatrix;
	}

	@Override
	public VectorMatrix getDisplacementVector(
			Double[][] reducedGlobalStiffnessMatrix,
			VectorMatrix forceVector) {
		int sizeOfVector = forceVector.getSizeOfVector();
		SimpleMatrix simpleMatrix = new SimpleMatrix(sizeOfVector, sizeOfVector);
		
		setValuesToSimpleMatrix(simpleMatrix,reducedGlobalStiffnessMatrix,sizeOfVector);
		
		SimpleMatrix invertedMatrix = simpleMatrix.invert();
		
		System.out.println(simpleMatrix);
		System.out.println(invertedMatrix);
		
		return null;
	}

	private void setValuesToSimpleMatrix(SimpleMatrix simpleMatrix,
			Double[][] reducedGlobalStiffnessMatrix,int size) {
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				simpleMatrix.set(i, j, reducedGlobalStiffnessMatrix[i][j]);
			}
		}
	}
*/}

















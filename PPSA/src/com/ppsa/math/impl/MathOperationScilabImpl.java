package com.ppsa.math.impl;


public class MathOperationScilabImpl { /*implements MathOperation {

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
		int sizeOfVector = forceVector.getSizeOfVector();
		
		// Inputs to Scilab.
		double[][] simpleDoubleMatrix = EntityTranslationUtil.setValuesToSimpleMatrix(
				reducedGlobalStiffnessMatrix);
		double[][] vectorMatrix = setValuesSimpleVector(forceVector);
		
		// Variables to hold Scilab output.
		ScilabDouble dispVector = new ScilabDouble();
		try {
			Scilab sci = new Scilab();
			if (sci.open()) {
				*//**
				 * SCILAB OPERATION STARTS HERE
				 *//*
				
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
				
				dispVector=(ScilabDouble) sci.get("dispVector");
				
				sci.close();
				
			} else {
				throw new PPSAMathException("Could not start JavaSCI");
			}
		} catch (org.scilab.modules.javasci.JavasciException e) {
			throw new PPSAMathException("JavaSci thrown exception..");
		}
		double[][] realPart = dispVector.getRealPart();
		Double[][] nodalDisplacementVector = getValuesOfSimpleMatrix(realPart, sizeOfVector);
		return nodalDisplacementVector;
	}

	private double[][] setValuesSimpleVector(VectorMatrix forceVector) {
		int sizeOfVector = forceVector.getSizeOfVector();
		Map<Integer, Double> vectorValues = forceVector.getVectorValues();
		double[][] newVectorMatrix = new double[sizeOfVector][1];
		int i=0;
		for (Integer keyForForceValue: vectorValues.keySet()) {
			Double forceValue = vectorValues.get(keyForForceValue);
			newVectorMatrix[i][0] = forceValue;
			i++;
		}
		return newVectorMatrix;
	}

	private Double[][] getValuesOfSimpleMatrix(
			double[][] reducedGlobalStiffnessMatrix, int size) {
		Double[][] newMatrix = new Double[size][1];
		for (int i = 0; i < size; i++) {
				newMatrix[i][0] = reducedGlobalStiffnessMatrix[i][0];
		}
		return newMatrix;
	}

	@Override
	public double getReactionAtSupport(double[][] aRowInStiffnessMatrix,
			double[][] fullNodalDispVector) throws PPSAMathException {
		
		// Inputs to Scilab.
		
		// Variables to hold Scilab output.
		ScilabDouble reactionValueMat = new ScilabDouble();
		try {
			Scilab sci = new Scilab();
			if (sci.open()) {
				*//**
				 * SCILAB OPERATION STARTS HERE
				 *//*
				
				ScilabDouble rowValuesOfMatrix = new ScilabDouble(
						aRowInStiffnessMatrix);
				ScilabDouble nodalDispVector = new ScilabDouble(fullNodalDispVector);
				
				sci.put("rowInGlobalMatrix", rowValuesOfMatrix);
				sci.put("nodalDispVector", nodalDispVector);
				
				//sci.exec("disp(redGlobalMatrix)");
				
				sci.exec("reactionValue=rowInGlobalMatrix*nodalDispVector");
				
				sci.exec("disp(reactionValue)");
				
				reactionValueMat=(ScilabDouble) sci.get("reactionValue");
				
				sci.close();
				
			} else {
				throw new PPSAMathException("Could not start JavaSCI");
			}
		} catch (org.scilab.modules.javasci.JavasciException e) {
			throw new PPSAMathException("JavaSci thrown exception..");
		}
		double[][] realPart = reactionValueMat.getRealPart();
		return realPart[0][0];
	}

	public double getAxialMemberForce(double[][] csMatric,
			double[][] doubleArrayFromMatrix) throws PPSAMathException {		
		// Inputs to Scilab.
		
		// Variables to hold Scilab output.
		ScilabDouble reactionValueMat = new ScilabDouble();
		try {
			Scilab sci = new Scilab();
			if (sci.open()) {
				*//**
				 * SCILAB OPERATION STARTS HERE
				 *//*
				
				ScilabDouble rowValuesOfMatrix = new ScilabDouble(
						csMatric);
				ScilabDouble nodalDispVector = new ScilabDouble(doubleArrayFromMatrix);
				
				sci.put("rowInGlobalMatrix", rowValuesOfMatrix);
				sci.put("nodalDispVector", nodalDispVector);
				
				//sci.exec("disp(redGlobalMatrix)");
				
				sci.exec("reactionValue=nodalDispVector*rowInGlobalMatrix");
				
				sci.exec("disp(reactionValue)");
				
				reactionValueMat=(ScilabDouble) sci.get("reactionValue");
				
				sci.close();
				
			} else {
				throw new PPSAMathException("Could not start JavaSCI");
			}
		} catch (org.scilab.modules.javasci.JavasciException e) {
			throw new PPSAMathException("JavaSci thrown exception..");
		}
		double[][] realPart = reactionValueMat.getRealPart();
		return realPart[0][0];
		}
*/}

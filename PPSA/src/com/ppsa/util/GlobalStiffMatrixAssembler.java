package com.ppsa.util;

import java.util.List;
import java.util.Set;

import com.psa.entity.FiniteElement;
import com.psa.entity.GlobalStiffnessMatrixEntity;
import com.psa.entity.StiffnessMatrixEntity;
import com.psa.entity.impl.Structure;
import com.psa.exception.PSAException;

public class GlobalStiffMatrixAssembler {
	public static void assembleGlobalStiffnesMatrix(Structure structure)
			throws PSAException {
		GlobalStiffnessMatrixEntity globalStiffnessMatrixEntity = structure
				.getGlobalStiffnessMatrix();
		Set<FiniteElement> finiteElements = structure.getFiniteElements();
			for (FiniteElement finiteElement : finiteElements) {
				//FiniteElement finiteElement = (OneDimFiniteElement) finiteElement;

				StiffnessMatrixEntity stiffnessMatrixEntity = finiteElement
						.getStiffnessMatrixEntity();
				Integer elementDegreeOfFreedom = stiffnessMatrixEntity
						.getDegreeOfFreedom();

				for (int i = 0; i < elementDegreeOfFreedom; i++) {
					addAllElementsOfColumnInGlobalMatrix(i, elementDegreeOfFreedom,
							stiffnessMatrixEntity, globalStiffnessMatrixEntity);
				}

			}
			//System.out.println("Finished Assembling..");
	}
	
	/**
	 * @param colIndex
	 * @param elementDOF
	 * @param stiffnessMatrixEntity
	 * @param globalStiffnessMatrixEntity
	 */
	private static void addAllElementsOfColumnInGlobalMatrix(int colIndex,
			int elementDOF, StiffnessMatrixEntity stiffnessMatrixEntity,
			GlobalStiffnessMatrixEntity globalStiffnessMatrixEntity) {

		List<Integer> coOrinatesOfFreedom = stiffnessMatrixEntity
				.getCoOrinatesOfFreedom();
		Double[][] stiffnessMatrix = stiffnessMatrixEntity.getStiffnessMatrix();

		// Lot of Difference between colIndex and colCoOrdinateNum
		Integer colCoOrdinateNum = coOrinatesOfFreedom.get(colIndex);
		for (int rowIndex = 0; rowIndex < elementDOF; rowIndex++) {
			int rowCoOrdinateNum = coOrinatesOfFreedom.get(rowIndex);
			Double valueAtTheseIndexes = stiffnessMatrix[colIndex][rowIndex];
			givenColCordNumRowCordNumAndItsValueAddItToGSM(colCoOrdinateNum,
					rowCoOrdinateNum, valueAtTheseIndexes,
					globalStiffnessMatrixEntity);
		}
	}
	
	private static void givenColCordNumRowCordNumAndItsValueAddItToGSM(
			Integer colCoOrdinateNum, int rowCoOrdinateNum,
			Double valueAtTheseIndexes,
			GlobalStiffnessMatrixEntity globalStiffnessMatrixEntity) {

		List<Integer> globalCoOrinatesOfFreedom = globalStiffnessMatrixEntity
				.getGlobalCoOrinatesOfFreedom();
		Double[][] globalStiffnessMatrix = globalStiffnessMatrixEntity
				.getGlobalStiffnessMatrix();

		int indexOfCol = globalCoOrinatesOfFreedom.indexOf(colCoOrdinateNum);
		int indexOfRow = globalCoOrinatesOfFreedom.indexOf(rowCoOrdinateNum);

		globalStiffnessMatrix[indexOfRow][indexOfCol] = globalStiffnessMatrix[indexOfRow][indexOfCol]
				+ valueAtTheseIndexes;
	}
}

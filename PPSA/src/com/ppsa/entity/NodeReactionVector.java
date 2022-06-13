package com.ppsa.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ppsa.exception.PPSAException;
import com.ppsa.exception.PPSAMathException;
import com.ppsa.math.impl.MathOperationJAMAImpl;
import com.ppsa.util.EntityTranslationUtil;
import com.psa.entity.GlobalStiffnessMatrixEntity;
import com.psa.entity.LoadCase;
import com.psa.entity.VectorMatrix;
import com.psa.entity.impl.Structure;

public class NodeReactionVector implements VectorMatrix,Serializable {

	private int DOF = 0;
	private Map<Integer, Double> reactionValues = new HashMap<Integer, Double>();
	private List<Integer> coOrdinatesOfFreedom = new ArrayList<Integer>();

	public void setNodeReactions(LoadCase loadCase)
			throws PPSAException {
		GlobalStiffnessMatrixEntity globalStiffnessMatrix = loadCase.getParentStructure()
				.getGlobalStiffnessMatrix();
		ReducedDisplacementVector reducedDisplacementVectorMatrix = (ReducedDisplacementVector) loadCase.getParentStructure()
				.getReducedDisplacementVectorMatrix();
		List<Integer> removedCoOrdinates = reducedDisplacementVectorMatrix
				.getRemovedCoOrdinates();
		// Map<Integer, Double> vectorValues =
		// reducedDisplacementVectorMatrix.getVectorValues();

		VectorMatrix fullNodalDisplacementVector = loadCase
				.getFullNodalDisplacementVector();
		
		//Map<Integer, Double> vectorValues = fullNodalDisplacementVector.getVectorValues();
		
		//sortByKey(fullNodalDisplacementVector.getVectorValues());
		
		double[][] fullDisplMatrixValues = EntityTranslationUtil
				.getDoubleArrayFromMatrix(fullNodalDisplacementVector);

		coOrdinatesOfFreedom.addAll(removedCoOrdinates);
		this.DOF = coOrdinatesOfFreedom.size();

		for (Integer coOrdNum : removedCoOrdinates) {
			// Get row values in that coOrdNum from globalStiffnessMatrix.
			if(coOrdNum==127)
				System.out.println("Delete Me...");
			
			//Structure parentStructure = loadCase.getParentStructure();
			//VectorMatrix displacementVectorMatrix = parentStructure.getDisplacementVectorMatrix();
			//List<Integer> coOrdinatedOfFreedom = displacementVectorMatrix.getCoOrdinatedOfFreedom();
			
			int indexOfRow = getIndexOf(fullNodalDisplacementVector,coOrdNum);
			/*int indexOfRow = globalStiffnessMatrix
					.getGlobalCoOrinatesOfFreedom().indexOf(coOrdNum); get index from fullNodDisp....
			Make a report of this non sorted problem of globalStiff.... in log */
			double[][] rowData = getRowValuesInThisCoOrdinateNum(
					globalStiffnessMatrix, indexOfRow);

			// multiply that row with fullNodalDisplacementVector from
			// mathOperation
			try {
				double reactionAtSupport = new MathOperationJAMAImpl()
						.getReactionAtSupport(rowData, fullDisplMatrixValues);
				this.reactionValues.put(coOrdNum, reactionAtSupport);
			} catch (PPSAMathException e) {
				throw new PPSAException(e.getMessage());
			}
		}
		reactionAddForLoadsDirectlyAppliedOnSupport(loadCase
				.getGlobalForceVector());
		//Collections.sort(coOrdinatesOfFreedom);
		loadCase.setSupportReactionsVector(this);
	}

	private int getIndexOf(VectorMatrix fullNodalDisplacementVector,
			Integer coOrdNum) {
		Map<Integer, Double> vectorValues = fullNodalDisplacementVector.getVectorValues();
		Set<Integer> keySet = vectorValues.keySet();
		int count = 0;
		for (Integer key : keySet) {
			if(key==coOrdNum)
				return count;
			
			count++;
		}
		return 0;
	}

	private void reactionAddForLoadsDirectlyAppliedOnSupport(
			VectorMatrix globalForceVector) {
		Map<Integer, Double> vectorValues = globalForceVector.getVectorValues();
		for (Integer coOrdNum : this.coOrdinatesOfFreedom) {
			Double inducedForceVal = vectorValues.get(coOrdNum);
			if (inducedForceVal != null) {
				Double originalForceVal = this.reactionValues.get(coOrdNum);
				this.reactionValues.put(coOrdNum, originalForceVal
						- inducedForceVal);
			}
		}

	}

	private double[][] getRowValuesInThisCoOrdinateNum(
			GlobalStiffnessMatrixEntity globalStiffnessMatrix, Integer rowIndex) {
		Double[][] globalStiffnessMatrixValues = globalStiffnessMatrix
				.getGlobalStiffnessMatrix();
		Integer dofSize = globalStiffnessMatrix.getGlobalDegreeOfFreedom();
		// globalStiffnessMatrix.get

		double[][] rowArray = new double[1][dofSize];
		for (int i = 0; i < dofSize; i++) {
			rowArray[0][i] = globalStiffnessMatrixValues[rowIndex][i];
		}
		return rowArray;
	}

	@Override
	public List<Integer> getCoOrdinatedOfFreedom() {
		return this.coOrdinatesOfFreedom;
	}

	@Override
	public int getSizeOfVector() {
		return this.DOF;
	}

	@Override
	public Map<Integer, Double> getVectorValues() {
		return this.reactionValues;
	}

}

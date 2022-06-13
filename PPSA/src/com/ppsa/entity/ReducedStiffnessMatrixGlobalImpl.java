package com.ppsa.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.ppsa.math.MathOperation;
import com.ppsa.math.impl.MathOperationCodeSportImpl;
import com.ppsa.math.impl.MathOperationJAMAImpl;
import com.ppsa.math.impl.MathOperationScilabImpl;
import com.psa.entity.GlobalStiffnessMatrixEntity;
import com.psa.entity.LoadCase;
import com.psa.entity.VectorMatrix;
import com.psa.entity.impl.Structure;
import com.psa.exception.PSAException;

public class ReducedStiffnessMatrixGlobalImpl implements GlobalStiffnessMatrixEntity,Serializable {

	private int DOF;
	private Double[][] reducedGlobalStiffnessMatrix;
	public Double[][] dummyStiffnessMatrix;
	private List<Integer> reducedCoOrdinatesOfFreedom = new ArrayList<Integer>();

	@Override
	public void setGlobalStiffnessMatrix(Structure  structure)
			throws PSAException {
		
		VectorMatrix reducedDisplacementVector = structure.getReducedDisplacementVectorMatrix();
		
		if(reducedDisplacementVector==null)
			throw new PSAException("Global Displacement Vector is not reduced.");
		
		List<Integer> reducedCoOrdinatedOfFreedom = reducedDisplacementVector.getCoOrdinatedOfFreedom();
		
		reducedCoOrdinatesOfFreedom.addAll(reducedCoOrdinatedOfFreedom);
		
		DOF = reducedCoOrdinatesOfFreedom.size();
		reducedGlobalStiffnessMatrix= new Double[DOF][DOF];

		reduceGlobalStiffnessMatrix(structure);
		
		structure.setReducedGlobalStiffnessMatrix(this);
		
	}

	private void reduceGlobalStiffnessMatrix(Structure structure) {
		GlobalStiffnessMatrixEntity globalStiffnessMatrix = structure.getGlobalStiffnessMatrix();
		List<Integer> originalCoOrinatesOfFreedom = globalStiffnessMatrix.getGlobalCoOrinatesOfFreedom();
		Double[][] originalGlobalStiffnessMatrix = globalStiffnessMatrix.getGlobalStiffnessMatrix();
		Integer originalSizeDOF = globalStiffnessMatrix.getGlobalDegreeOfFreedom();
		
		copyContentOfOriginalMatrix(originalGlobalStiffnessMatrix,originalSizeDOF);
		List<Integer> indexesToBeRemoved = prepareIndexesToBeRemoved(originalCoOrinatesOfFreedom);
		MathOperation mathOperation = new MathOperationJAMAImpl();

		reducedGlobalStiffnessMatrix=mathOperation.reduceMatrix(dummyStiffnessMatrix, indexesToBeRemoved);
	}

	private List<Integer> prepareIndexesToBeRemoved(
			List<Integer> originalCoOrinatesOfFreedom) {
		List<Integer> indexesToBeRemoved = new ArrayList<Integer>();
		
		//indexesToBeRemoved.addAll(originalCoOrinatesOfFreedom);
		getIndexsOfMatrix(indexesToBeRemoved,originalCoOrinatesOfFreedom);
		// now reduce the index 
		return indexesToBeRemoved;
	}

	private void getIndexsOfMatrix(List<Integer> indexesToBeRemoved,List<Integer> originalCoOrdinatesOfFreedom) {
		for (Integer originalCoOrdNum : originalCoOrdinatesOfFreedom) {
			boolean addToIndexesToBeRemovedFlag=false;
			List<Integer> duplicateReducedCoOrdinatesOfFreedom = new ArrayList<Integer>(reducedCoOrdinatesOfFreedom);
			for (Integer reducedCoOrdOfFreedom : duplicateReducedCoOrdinatesOfFreedom) {
				if(originalCoOrdNum==reducedCoOrdOfFreedom) {
					addToIndexesToBeRemovedFlag=true;
					duplicateReducedCoOrdinatesOfFreedom.remove(reducedCoOrdOfFreedom);
					break;
				}
			}
			if(addToIndexesToBeRemovedFlag) {
				indexesToBeRemoved.add(originalCoOrdinatesOfFreedom.indexOf(originalCoOrdNum));
			}
		}
	}

	private void copyContentOfOriginalMatrix(
			Double[][] originalGlobalStiffnessMatrix,int DOF) {
		dummyStiffnessMatrix=new Double[DOF][DOF];
		for (int i = 0; i < DOF; i++) {
			for (int j = 0; j < DOF; j++) {
				dummyStiffnessMatrix[i][j]=originalGlobalStiffnessMatrix[i][j];
			}
		}
	}

	@Override
	public List<Integer> getGlobalCoOrinatesOfFreedom() {
		return this.reducedCoOrdinatesOfFreedom;
	}

	@Override
	public Integer getGlobalDegreeOfFreedom() {
		return DOF;
	}

	@Override
	public Double[][] getGlobalStiffnessMatrix() {
		return reducedGlobalStiffnessMatrix;
	}
}

package com.ppsa.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.ppsa.exception.PPSAMathException;
import com.ppsa.math.impl.MathOperationJAMAImpl;
import com.psa.entity.GlobalStiffnessMatrixEntity;
import com.psa.entity.LoadCase;
import com.psa.entity.LoadCase.SupportSettlementEntity;
import com.psa.entity.VectorMatrix;
import com.psa.entity.enums.DOF_TYPE;
import com.psa.entity.impl.Node;
import com.psa.entity.impl.Structure;

public class NodalDisplacementResultVector implements VectorMatrix,Serializable {
	private int DOF = 0;
	private List<Integer> coOrdinatesOfFreedom = new ArrayList<Integer>();
	private Map<Integer, Double> displacementVectorValues = new TreeMap<Integer, Double>();

	public void setNodalDisplacementVector(LoadCase loadCase
			) throws PPSAMathException {

		Structure structure = loadCase.getParentStructure();
		GlobalStiffnessMatrixEntity reducedGlobalStiffnessMatrix = structure.getReducedGlobalStiffnessMatrix();

		VectorMatrix reducedGlobalForceVector = loadCase
				.getReducedGlobalForceVector();

		coOrdinatesOfFreedom.addAll(reducedGlobalStiffnessMatrix
				.getGlobalCoOrinatesOfFreedom());
		DOF = coOrdinatesOfFreedom.size();

		// To get known displacements
		Map<Integer, Double> knownDisplacements = getKnownDisplacements(loadCase, structure);
		
		
		List<Integer> indexesToBeRetained = new ArrayList<Integer>();
		for(int i=0; i<knownDisplacements.size(); i++) {
			indexesToBeRetained.add(DOF+i);
		}

		double[][] subGlobalStiffnessMatrixForForceAlteration = new MathOperationJAMAImpl()
				.getSubMatix(structure.getGlobalStiffnessMatrix()
						.getGlobalStiffnessMatrix(), 0, DOF - 1, DOF,
						knownDisplacements.size() - 1);
		Set<Integer> keySet = knownDisplacements.keySet();
		double[] displacementValues = new double[knownDisplacements.size()-DOF];
		int counter=0;
		int i=0;
		for (Integer key : keySet) {
			if(counter>=DOF) {
				Double disp = knownDisplacements.get(key);
				if(disp==1.0)
					disp=0d;

				displacementValues[i]=disp;
				i++;
			}
			counter++;
		}

		double[] settlmentModificationForcesVector = new MathOperationJAMAImpl().multiplyMatixVector(subGlobalStiffnessMatrixForForceAlteration, displacementValues);
		finalizeReducedGlobalForceVector(reducedGlobalForceVector,settlmentModificationForcesVector);
		//reducedGlobalForceVector = reducedGlobalForceVector - {7Partial Stiff Mat}x{Known displacements}

		performNodalDisplacementOperation(
				reducedGlobalStiffnessMatrix.getGlobalStiffnessMatrix(),
				reducedGlobalForceVector);

		//Collections.sort(coOrdinatesOfFreedom);
		//this.displacementVectorValues.putAll(knownDisplacements);
		new FullNodalDisplacementVector().setVectorValues(loadCase,displacementVectorValues,knownDisplacements);
		//loadCase.setFullNodalDisplacementVector();
		loadCase.setReducedNodalDisplacementVector(this);
	}

	private void finalizeReducedGlobalForceVector(
			VectorMatrix reducedGlobalForceVector,
			double[] settlmentModificationForcesVector) {
		Map<Integer, Double> vectorValues = reducedGlobalForceVector.getVectorValues();
		Set<Integer> keySet = vectorValues.keySet();
		int counter=0;
		for (Integer key : keySet) {
			Double val = vectorValues.get(key);
			double finalVal = val-settlmentModificationForcesVector[counter];
			vectorValues.put(key, finalVal);
			counter++;
		}
	}

	private Map<Integer, Double> getKnownDisplacements(LoadCase loadCase, Structure structure) {
		// Known Displacements are found after a certain DOF in global Force Vector
		//GlobalStiffnessMatrixEntity globalStiffnessMatrix = structure.getGlobalStiffnessMatrix();
		//List<Integer> globalCoOrinatesOfFreedom = globalStiffnessMatrix.getGlobalCoOrinatesOfFreedom();
		Map<Integer,Double> knownDisplacementsTemp = structure.getDisplacementVectorMatrix().getVectorValues();
		Map<Integer,Double> knownDisplacements = cloneMap(knownDisplacementsTemp);
		/*for (int i=DOF+1; i<globalCoOrinatesOfFreedom.size()+1;i++) {
			//Integer dof_int = globalCoOrinatesOfFreedom.get(i);
			knownDisplacements.put(i,0d);
		}*/
		
		List<SupportSettlementEntity> supportSettlements = loadCase.getSupportSettlements();
		for (SupportSettlementEntity supportSettlementEntity : supportSettlements) {
			Set<Node> nodes = supportSettlementEntity.getNodes();
			DOF_TYPE cordinate = supportSettlementEntity.getCordinate();
			double settlement = supportSettlementEntity.getSettlement();
			
			for (Node node : nodes) {
				if(cordinate==DOF_TYPE.FX) {
					int coordNum = node.getxCordinateNum();
					knownDisplacements.put(coordNum, settlement);
				}
				else if(cordinate==DOF_TYPE.FY) {
					int coordNum = node.getyCordinateNum();
					knownDisplacements.put(coordNum, settlement);
				}
				else if(cordinate==DOF_TYPE.FZ) {
					int coordNum = node.getzCordinateNum();
					knownDisplacements.put(coordNum, settlement);
				}
				else if(cordinate==DOF_TYPE.MX) {
					int coordNum = node.getMxCordinateNum();
					knownDisplacements.put(coordNum, settlement);
				}
				else if(cordinate==DOF_TYPE.MY) {
					int coordNum = node.getMyCordinateNum();
					knownDisplacements.put(coordNum, settlement);
				}
				else if(cordinate==DOF_TYPE.MZ) {
					int coordNum = node.getMzCordinateNum();
					knownDisplacements.put(coordNum, settlement);
				}
			}
		}
		return knownDisplacements;
	}

	private Map<Integer, Double> cloneMap(
			Map<Integer, Double> mapToBeCloned) {
		Map<Integer, Double> clonedMap = new TreeMap<Integer,Double>();
		Set<Integer> keySet = mapToBeCloned.keySet();
		for (Integer key : keySet) {
			clonedMap.put(key, mapToBeCloned.get(key));
		}
		return clonedMap;
	}

	private void performNodalDisplacementOperation(
			Double[][] globalStiffnessMatrix,
			VectorMatrix reducedGlobalForceVector) throws PPSAMathException {
		Double[][] displacementVector = new MathOperationJAMAImpl()
				.getDisplacementVector(globalStiffnessMatrix,
						reducedGlobalForceVector);
		int i = 0;
		for (Integer coOrdNum : this.coOrdinatesOfFreedom) {
			displacementVectorValues.put(coOrdNum, displacementVector[i++][0]);
		}
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
		return this.displacementVectorValues;
	}

}

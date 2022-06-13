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

import com.psa.entity.LoadCase;
import com.psa.entity.VectorMatrix;
import com.psa.entity.impl.Structure;
import com.psa.exception.PSAException;

public class ReducedDisplacementVector implements VectorMatrix,Serializable {

	private SortedMap<Integer, Double> reducedDisplacementValues = new TreeMap<Integer, Double>();
	private List<Integer> reducedCoOrdinatesOfFreedom = new ArrayList<Integer>();
	private List<Integer> removedCoOrdinates = new ArrayList<Integer>();

	/**
	 * Perform reduction of Displacement Vector, GlobalStiffnessMatrix and
	 * ForceVector for various load cases.
	 * 
	 * @param loadCase 
	 * @throws PSAException
	 */
	public void setReducedDisplacementVectorAndOtherReductions(
			Structure structure) throws PSAException {
		VectorMatrix displacementVectorMatrix = structure
				.getDisplacementVectorMatrix();
		List<Integer> originalCoOrinatesOfFreedom = displacementVectorMatrix
				.getCoOrdinatedOfFreedom();
		Map<Integer, Double> vectorValues = displacementVectorMatrix
				.getVectorValues();

		for (Integer coOrdNum : originalCoOrinatesOfFreedom) {
			Double dispAtCoOrdinate = vectorValues.get(coOrdNum);
			if (dispAtCoOrdinate != 0) {
				reducedCoOrdinatesOfFreedom.add(coOrdNum);
				reducedDisplacementValues.put(coOrdNum, dispAtCoOrdinate);
			} else
				removedCoOrdinates.add(coOrdNum);
		}

		//Collections.sort(reducedCoOrdinatesOfFreedom);
		structure.setReducedDisplacementVectorMatrix(this);

		new ReducedStiffnessMatrixGlobalImpl()
				.setGlobalStiffnessMatrix(structure);

			Set<Integer> keySet = structure.getLoadCases().keySet();
			for (Integer key : keySet) {
				LoadCase loadCase = structure.getLoadCase(key);
				new ReducedForceVector().setReducedForceVector(loadCase,
						removedCoOrdinates);
			}
			
	}

	@Override
	public List<Integer> getCoOrdinatedOfFreedom() {
		return this.reducedCoOrdinatesOfFreedom;
	}

	@Override
	public int getSizeOfVector() {
		return this.reducedCoOrdinatesOfFreedom.size();
	}

	@Override
	public Map<Integer, Double> getVectorValues() {
		return reducedDisplacementValues;
	}

	public List<Integer> getRemovedCoOrdinates() {
		return removedCoOrdinates;
	}

	public void setRemovedCoOrdinates(List<Integer> removedCoOrdinates) {
		this.removedCoOrdinates = removedCoOrdinates;
	}

}
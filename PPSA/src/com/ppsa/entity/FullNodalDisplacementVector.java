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


public class FullNodalDisplacementVector implements VectorMatrix,Serializable {
	private int DOF = 0;
	//This TreeMap os Sorted one
	private Map<Integer, Double> dispValues = new TreeMap<Integer, Double>();
	private List<Integer> coOrdinatesOfFreedom = new ArrayList<Integer>();
	//private SortedMap<Integer, Double> sortedMap = null;

	/*public void setFullNodalDisplacementVectorToLoadCase(LoadCase loadCase) {
		VectorMatrix reducedNodalDisplacementVector = loadCase.getReducedNodalDisplacementVector();
		List<Integer> redCoOrdinatedOfFreedom = reducedNodalDisplacementVector.getCoOrdinatedOfFreedom();
		Map<Integer, Double> redVectorValues = reducedNodalDisplacementVector.getVectorValues();
		this.coOrdinatesOfFreedom.addAll(redCoOrdinatedOfFreedom);
		this.dispValues.putAll(redVectorValues);
		
		ReducedDisplacementVector reducedDisplacementVectorMatrix = (ReducedDisplacementVector) loadCase.getParentStructure().getReducedDisplacementVectorMatrix();
		List<Integer> removedCoOrdinates = reducedDisplacementVectorMatrix.getRemovedCoOrdinates();
		Map<Integer, Double> globalDispVectorectorValues = loadCase.getParentStructure().getDisplacementVectorMatrix().getVectorValues();
		this.coOrdinatesOfFreedom.addAll(removedCoOrdinates);
		//Collections.sort(this.coOrdinatesOfFreedom);
		
		for (Integer coOrdNum : removedCoOrdinates) {
			Double value = globalDispVectorectorValues.get(coOrdNum);
			this.dispValues.put(coOrdNum, value);
		}
		
		this.DOF = this.coOrdinatesOfFreedom.size();

		//sortedMap = new TreeMap<Integer,Double>(dispValues);
		//Collections.sort(coOrdinatesOfFreedom);
		loadCase.setFullNodalDisplacementVector(this);
	}*/
	
	public void setVectorValues(LoadCase loadCase,
			Map<Integer, Double> displacementVectorValues,
			Map<Integer, Double> knownDisplacements) {
		//this.dispValues.putAll(displacementVectorValues);
		this.dispValues.putAll(knownDisplacements);
		Set<Integer> keySetOne = displacementVectorValues.keySet();
		for (Integer key : keySetOne) {
			this.dispValues.put(key, displacementVectorValues.get(key));
		}
		
		Set<Integer> keySet = this.dispValues.keySet();
		for (Integer key : keySet) {
			this.coOrdinatesOfFreedom.add(key);
		}
		DOF=coOrdinatesOfFreedom.size();
		loadCase.setFullNodalDisplacementVector(this);
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
		return this.dispValues;
	}
	
	/*public void setVectorValues(Map<Integer,Double> vectorValues) {
		this.dispValues = vectorValues;
		Set<Integer> keySet = this.dispValues.keySet();
		coOrdinatesOfFreedom.clear();
		for (Integer key : keySet) {
			coOrdinatesOfFreedom.add(key);
		}
		this.DOF = coOrdinatesOfFreedom.size();
	}*/
	
}

package com.ppsa.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.psa.entity.LoadCase;
import com.psa.entity.VectorMatrix;

public class ReducedForceVector implements VectorMatrix,Serializable {

	private int DOF = 0;
	private SortedMap<Integer, Double> reducedForceValues = new TreeMap<Integer, Double>();
	private List<Integer> reducedCoOrdinatesOfFreedom = new ArrayList<Integer>();

	public void setReducedForceVector(LoadCase loadCase, List<Integer> removedCoOrdinates) {
			VectorMatrix globalForceVector = loadCase.getGlobalForceVector();
			
			reducedForceValues.putAll(globalForceVector.getVectorValues());
			
			reducedCoOrdinatesOfFreedom.addAll(globalForceVector.getCoOrdinatedOfFreedom());
			reducedCoOrdinatesOfFreedom.removeAll(removedCoOrdinates);
			DOF=reducedCoOrdinatesOfFreedom.size();
			for (Integer coOrdNum : removedCoOrdinates) {
				reducedForceValues.remove(coOrdNum);
			}
			
			//Collections.sort(reducedCoOrdinatesOfFreedom);
			loadCase.setReducedGlobalForceVector(this);
		}

	@Override
	public List<Integer> getCoOrdinatedOfFreedom() {
		// TODO Auto-generated method stub
		return reducedCoOrdinatesOfFreedom;
	}

	@Override
	public int getSizeOfVector() {
		// TODO Auto-generated method stub
		return DOF;
	}

	@Override
	public Map<Integer, Double> getVectorValues() {
		// TODO Auto-generated method stub
		return reducedForceValues;
	}

}

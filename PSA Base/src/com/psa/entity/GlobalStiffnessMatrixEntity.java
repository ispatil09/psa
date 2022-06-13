package com.psa.entity;

import java.util.List;

import com.psa.entity.impl.Structure;
import com.psa.exception.PSAException;

public interface GlobalStiffnessMatrixEntity {
	
	public void setGlobalStiffnessMatrix(
			Structure structure) throws PSAException;

	public Integer getGlobalDegreeOfFreedom();

	public List<Integer> getGlobalCoOrinatesOfFreedom();

	public Double[][] getGlobalStiffnessMatrix();
}

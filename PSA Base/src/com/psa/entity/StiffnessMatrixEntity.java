package com.psa.entity;

import java.util.List;

import com.psa.exception.PSAException;

public interface StiffnessMatrixEntity {
	/**
	 * Set stiffness matrix for any kind of Finite element.
	 * 
	 * @param finiteElement
	 * @return
	 * @throws PSAException
	 */
	public StiffnessMatrixEntity setStiffnessMatrixForElement(
			FiniteElement finiteElement) throws PSAException;

	public Integer getDegreeOfFreedom();

	public List<Integer> getCoOrinatesOfFreedom();

	public Double[][] getStiffnessMatrix();
}

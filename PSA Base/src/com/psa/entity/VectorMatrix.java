package com.psa.entity;

import java.util.List;
import java.util.Map;

/**
 * Can be used for creating displacement vector and force vector.
 * 
 * @author SONY
 * 
 */
public interface VectorMatrix {
	public int getSizeOfVector();

	public List<Integer> getCoOrdinatedOfFreedom();

	public Map<Integer, Double> getVectorValues();
}

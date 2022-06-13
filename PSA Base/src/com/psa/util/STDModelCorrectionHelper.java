package com.psa.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.psa.entity.impl.OneDimFiniteElement;

public class STDModelCorrectionHelper implements Serializable {
	/**
	 * Indicates that if CON MEMBER LOAD distance 'd1' has to be same or
	 * to be subtracted by member length.
	 * 
	 * In simple words it indicates how STAAD is considering load distance
	 * references.
	 */
	private Map<OneDimFiniteElement, Boolean> nodeDistReverseIndicator = new HashMap<>();

	public void addEntryReverseIndicatorEntry(OneDimFiniteElement oneDimFiniteElement,boolean bool) {
		this.nodeDistReverseIndicator.put(oneDimFiniteElement, bool);
	}
	
	public boolean getEntryReverseIndicatorEntry(OneDimFiniteElement oneDimFiniteElement) {
		return this.nodeDistReverseIndicator.get(oneDimFiniteElement);
	}
}

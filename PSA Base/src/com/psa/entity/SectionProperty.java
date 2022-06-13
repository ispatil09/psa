package com.psa.entity;

public interface SectionProperty {
	/**
	 * Get cross sectional area of one D finite element.
	 * @return
	 */
	public double getCSArea();
	public double getInertialMomentY();
	public double getInertialMomentZ();
	public double getPolarMoment();
	
}

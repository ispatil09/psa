package com.psa.entity;

import java.io.Serializable;

import com.psa.entity.enums.ForceUnit;
import com.psa.entity.enums.LengthUnit;

public class StructureUnits implements Serializable {
	private ForceUnit forceUnit;
	private LengthUnit lengthUnit;
	public ForceUnit getForceUnit() {
		return forceUnit;
	}
	public void setForceUnit(ForceUnit forceUnit) {
		this.forceUnit = forceUnit;
	}
	public LengthUnit getLengthUnit() {
		return lengthUnit;
	}
	public void setLengthUnit(LengthUnit lengthUnit) {
		this.lengthUnit = lengthUnit;
	}
	
}

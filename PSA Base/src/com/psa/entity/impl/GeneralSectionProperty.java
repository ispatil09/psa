package com.psa.entity.impl;

import com.psa.entity.SectionProperty;

public class GeneralSectionProperty implements SectionProperty {

	private double CSArea = 0;
	private double IY = 0;
	private double IZ = 0;
	private double IX = 0;
	
	public GeneralSectionProperty(double aX, double iX, double iY, double iZ) {
		this.CSArea=aX;
		this.IX=iX;
		this.IY=iY;
		this.IZ=iZ;
	}

	public void setCSArea(double cSArea) {
		CSArea = cSArea;
	}

	public void setIY(double iY) {
		IY = iY;
	}

	public void setIZ(double iZ) {
		IZ = iZ;
	}

	public void setIX(double iX) {
		IX = iX;
	}

	@Override
	public double getCSArea() {
		return CSArea;
	}

	@Override
	public double getInertialMomentY() {
		return IY;
	}

	@Override
	public double getInertialMomentZ() {
		return IZ;
	}

	@Override
	public double getPolarMoment() {
		return IX;
	}

}

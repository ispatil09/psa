package com.psa.entity;

import java.io.Serializable;

import com.sd.entity.SectionDetailer;;

public class SectionPropertyIndian implements SectionProperty,Serializable {

	private double csArea=0;
	private double inertialMomentY=0;
	private double inertialMomentZ=0;
	private double polarMoment=0;

	public SectionPropertyIndian(SectionDetailer sectionDetailer) {
		csArea = sectionDetailer.getCSArea();
		inertialMomentY = sectionDetailer.getInertialMomentY();
		inertialMomentZ = sectionDetailer.getInertialMomentZ();
		polarMoment = sectionDetailer.getPolarMoment();
	}

	@Override
	public double getCSArea() {
		return csArea;
	}

	@Override
	public double getInertialMomentY() {
		return inertialMomentY;
	}

	@Override
	public double getInertialMomentZ() {
		return inertialMomentZ;
	}

	@Override
	public double getPolarMoment() {
		return polarMoment;
	}

}

package com.psa.entity.impl;

import java.io.Serializable;

import com.psa.entity.SectionProperty;

public class PrisSectionProperty implements SectionProperty,Serializable {
	private double YD;
	private double ZD;
	
	public PrisSectionProperty(double yd2, double zd2) {
		this.YD = yd2;
		this.ZD = zd2;
	}
	public double getYD() {
		return YD;
	}
	public void setYD(double yD) {
		YD = yD;
	}
	public double getZD() {
		return ZD;
	}
	public void setZD(double zD) {
		ZD = zD;
	}
	@Override
	public double getCSArea() {
		return YD*ZD;
	}
	@Override
	public double getInertialMomentZ() {
		double momOfInertiaY = (ZD*YD*YD*YD)/12;
		return momOfInertiaY;
	}
	@Override
	public double getInertialMomentY() {
		double momOfInertiaZ = (YD*ZD*ZD*ZD)/12;
		return momOfInertiaZ;
	}
	@Override
	public double getPolarMoment() {
		if(YD==0.1&&ZD==0.1)
			return 0.000014046;
		else if((YD==0.1&&ZD==0.2)||(YD==0.2&&ZD==0.1))
			return 0.000045657;
		else if((YD==0.1&&ZD==0.3)||(YD==0.3&&ZD==0.1))
			return 0.000078751;
		else if((YD==0.1&&ZD==0.4)||(YD==0.4&&ZD==0.1))
			return 0.000111777;
		else if(YD==0.2&&ZD==0.2)
			return 0.000224744;
		else if((YD==0.2&&ZD==0.3)||(YD==0.3&&ZD==0.2))
			return 0.000469257;
		else if((YD==0.2&&ZD==0.4)||(YD==0.4&&ZD==0.2))
			return 0.0007305041;
		else if(YD==0.3&&ZD==0.3)
			return 0.001137773;
		else if((YD==0.3&&ZD==0.4)||(YD==0.4&&ZD==0.3))
			return 0.001946876;
		else if(YD==0.4&&ZD==0.4)
			return 0.0035959;
		else
			throw new RuntimeException("Polar/Torsion Moment not configured for section : "+YD+" , "+ZD);
	}
}
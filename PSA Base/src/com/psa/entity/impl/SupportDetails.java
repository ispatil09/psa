package com.psa.entity.impl;

import java.io.Serializable;

import com.psa.entity.enums.SupportType;

public class SupportDetails implements Serializable {

	private SupportType supportName;
	
	private boolean xDisplaceabale;
	private boolean yDisplaceabale;
	private boolean zDisplaceabale;
	private boolean xRotatable;
	private boolean yRotatable;
	private boolean zRotatable;

	private double xStiffness;
	private double yStiffness;
	private double zStiffness;
	
	public SupportDetails() {
		supportName=SupportType.FREE;
		xDisplaceabale=true;
		yDisplaceabale=true;
		zDisplaceabale=true;
		xRotatable=true;
		yRotatable=true;
		zRotatable=true;
	}
	
	public SupportType getSupportName() {
		return supportName;
	}

	public void setSupportName(SupportType supportName) {
		this.supportName = supportName;

		if (supportName.equals(SupportType.FIXED)) {
			this.xDisplaceabale = false;
			this.xRotatable = false;
			this.yDisplaceabale = false;
			this.yRotatable = false;
			this.zDisplaceabale = false;
			this.zRotatable = false;
		} else if (supportName.equals(SupportType.PINNED)) {
			this.xDisplaceabale = false;
			this.xRotatable = true;
			this.yDisplaceabale = false;
			this.yRotatable = true;
			this.zDisplaceabale = false;
			this.zRotatable = true;
		}
	}

	public boolean isxDisplaceabale() {
		return xDisplaceabale;
	}

	public void setxDisplaceabale(boolean xDisplaceabale) {
		this.xDisplaceabale = xDisplaceabale;
	}

	public boolean isyDisplaceabale() {
		return yDisplaceabale;
	}

	public void setyDisplaceabale(boolean yDisplaceabale) {
		this.yDisplaceabale = yDisplaceabale;
	}

	public boolean iszDisplaceabale() {
		return zDisplaceabale;
	}

	public void setzDisplaceabale(boolean zDisplaceabale) {
		this.zDisplaceabale = zDisplaceabale;
	}

	public boolean isxRotatable() {
		return xRotatable;
	}

	public void setxRotatable(boolean xRotatable) {
		this.xRotatable = xRotatable;
	}

	public boolean isyRotatable() {
		return yRotatable;
	}

	public void setyRotatable(boolean yRotatable) {
		this.yRotatable = yRotatable;
	}

	public boolean iszRotatable() {
		return zRotatable;
	}

	public void setzRotatable(boolean zRotatable) {
		this.zRotatable = zRotatable;
	}

	public double getxStiffness() {
		return xStiffness;
	}

	public void setxStiffness(double xStiffness) {
		this.xStiffness = xStiffness;
	}

	public double getyStiffness() {
		return yStiffness;
	}

	public void setyStiffness(double yStiffness) {
		this.yStiffness = yStiffness;
	}

	public double getzStiffness() {
		return zStiffness;
	}

	public void setzStiffness(double zStiffness) {
		this.zStiffness = zStiffness;
	}
	/*@Override
	public String toString() {
		String str = yDisplaceabale+" , Settlement = "+ySettlement;
		return str;
	}*/
}

package com.psa.entity;

import java.io.Serializable;

public class MaterialProperty implements Serializable {
	private String propertyName;
	private double E;
	private double poisson=0.3;

	public MaterialProperty(String name) {
		propertyName = name;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	public double getE() {
		return E;
	}

	public void setE(double e) {
		E = e;
	}
	
	public double getPoisson() {
		return poisson;
	}

	public void setPoisson(double poisson) {
		this.poisson = poisson;
	}

	public double getG() {
		
		double G = E / (2*(1+poisson));  
		return G;
	}

	@Override
	public String toString() {
		return propertyName+","+E;
	}
}

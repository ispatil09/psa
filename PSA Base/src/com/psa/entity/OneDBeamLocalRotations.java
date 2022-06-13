package com.psa.entity;

import java.io.Serializable;

public class OneDBeamLocalRotations implements Serializable {
	private double[] localRotations_NodeOne = null;
	private double[] localRotations_NodeTwo = null;

	public double[] getLocalRotations_NodeOne() {
		return localRotations_NodeOne;
	}
	public void setLocalRotations_NodeOne(double[] localRotations_NodeOne) {
		this.localRotations_NodeOne = localRotations_NodeOne;
	}
	public double[] getLocalRotations_NodeTwo() {
		return localRotations_NodeTwo;
	}
	public void setLocalRotations_NodeTwo(double[] localRotations_NodeTwo) {
		this.localRotations_NodeTwo = localRotations_NodeTwo;
	}

}

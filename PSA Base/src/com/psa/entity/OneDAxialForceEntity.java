package com.psa.entity;

import java.io.Serializable;

public class OneDAxialForceEntity implements Serializable {
	private VectorMatrix CSValueVector = null;
	private VectorMatrix nodesAssociatedDispVector = null;
	private double axialForce = 0;
	private double AEbyLval = 0;

	public VectorMatrix getCSValueVector() {
		return CSValueVector;
	}

	public void setCSValueVector(VectorMatrix cSValueVector) {
		CSValueVector = cSValueVector;
	}

	public VectorMatrix getNodesAssociatedDispVector() {
		return nodesAssociatedDispVector;
	}

	public void setNodesAssociatedDispVector(
			VectorMatrix nodesAssociatedDispVector) {
		this.nodesAssociatedDispVector = nodesAssociatedDispVector;
	}

	public double getAxialForce() {
		return axialForce;
	}

	public void setAxialForce(double axialForce) {
		this.axialForce = axialForce;
	}

	public double getAEbyLval() {
		return AEbyLval;
	}

	public void setAEbyLval(double aEbyLval) {
		AEbyLval = aEbyLval;
	}

}

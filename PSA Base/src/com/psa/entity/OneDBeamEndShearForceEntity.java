package com.psa.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.psa.entity.impl.Node;

public class OneDBeamEndShearForceEntity implements Serializable {
	private Map<Node, Double> shearForces_FY = new HashMap<Node, Double>();
	private Map<Node, Double> shearForces_FZ = new HashMap<Node, Double>();

	public void addShearForce_FY(Node node,Double shearForce) {
		shearForces_FY.put(node, shearForce);
	}

	public double getShearForce_FY(Node node) {
		return shearForces_FY.get(node);
	}
	public void addShearForce_FZ(Node node,Double shearForce) {
		shearForces_FZ.put(node, shearForce);
	}
	
	public double getShearForce_FZ(Node node) {
		return shearForces_FZ.get(node);
	}

}

package com.psa.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.psa.entity.impl.Node;

public class OneDBeamEndMomentsEntity implements Serializable {
	private Map<Node, Double> moments_MZ = new HashMap<Node, Double>();
	private Map<Node, Double> moments_MY = new HashMap<Node, Double>();

	public void addMoment_MZ(Node node,Double moment) {
		moments_MZ.put(node, moment);
	}

	public double getMoment_MZ(Node node) {
		return moments_MZ.get(node);
	}
	public void addMoment_MY(Node node,Double moment) {
		moments_MY.put(node, moment);
	}
	
	public double getMoment_MY(Node node) {
		return moments_MY.get(node);
	}

}

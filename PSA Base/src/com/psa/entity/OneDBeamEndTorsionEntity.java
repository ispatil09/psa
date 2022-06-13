package com.psa.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.psa.entity.impl.Node;

public class OneDBeamEndTorsionEntity implements Serializable {
	private Map<Node, Double> moments_MX = new HashMap<Node, Double>();

	public void addTorsion_MX(Node node,Double moment) {
		moments_MX.put(node, moment);
	}

	public double getTorsion_MX(Node node) {
		return moments_MX.get(node);
	}
}

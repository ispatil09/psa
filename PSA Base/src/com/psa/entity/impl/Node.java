package com.psa.entity.impl;

import java.io.Serializable;

import com.psa.entity.CommonNode;
import com.psa.entity.enums.SupportType;

public class Node extends CommonNode implements Serializable,Comparable<Node> {

	private SupportDetails supportDetails;

	// Not required implement anything now only.
	// Perhaps not required to put here.
	// Bcos it varies for different loadCases.
	// Hence put it in LoadCase class as , Map<Node,NodeResult>
	/*private NodeResults nodeResults = new NodeResults();*/

	/**
	 * Constructor for PLANE structure nodes.
	 * 
	 * @param nodeNum
	 * @param xCord
	 * @param yCord
	 * @param zCord
	 */
	public Node(int nodeNum, double xCord, double yCord, int xCordNum,
			int yCordNum) {
		setNodeNumber(nodeNum);
		setxCordinate(xCord);
		setyCordinate(yCord);
		setxCordinateNum(xCordNum);
		setyCordinateNum(yCordNum);
	}

	public Node(int nodeNum, double xCord, double yCord, double zCord,
			int xCordNum, int yCordNum, int zCordNum) {
		setNodeNumber(nodeNum);
		setxCordinate(xCord);
		setyCordinate(yCord);
		setzCordinate(zCord);
		setxCordinateNum(xCordNum);
		setyCordinateNum(yCordNum);
		setzCordinateNum(zCordNum);
	}

	public Node(int nodeNum) {
		setNodeNumber(nodeNum);
	}

	public SupportDetails getSupportDetails() {
		return supportDetails;
	}

	public void setSupportDetails(SupportDetails supportDetails) {
		this.supportDetails = supportDetails;
	}

	public boolean isSupport() {
		SupportType supportName = this.supportDetails.getSupportName();
		if(supportName!=SupportType.FREE)
			return true;
		else
			return false;
	}
	/*public NodeResults getNodeResults() {
		return nodeResults;
	}

	public void setNodeResults(NodeResults nodeResults) {
		this.nodeResults = nodeResults;
	}*/

	@Override
	public int compareTo(Node nodeSec) {
		if(nodeSec.getNodeNumber()<this.getNodeNumber())
			return 1;
		else
			return 0;
	}
}

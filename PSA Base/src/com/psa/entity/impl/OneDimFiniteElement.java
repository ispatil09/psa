package com.psa.entity.impl;

import java.util.List;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;

import com.psa.entity.FiniteElement;
import com.psa.entity.MaterialProperty;
import com.psa.entity.SectionProperty;
import com.psa.entity.enums.OneDimFiniteElementType;

public class OneDimFiniteElement extends FiniteElement implements Serializable {
	private final int NUMOFNODES = 2;
	private MaterialProperty materialProperty = new MaterialProperty(null);
	private SectionProperty sectionProperty = null;
	private OneDimFiniteElementType finiteElementType;
	private Node firstNode = null;
	private Node secondNode = null;
	private double lengthOfElement=0;
	private double angleOfInclination=0;

	public OneDimFiniteElement(int elementNum) {
		setNumOfNodes(NUMOFNODES);
		setElementNumber(elementNum);
	}

	public SectionProperty getSectionProperty() {
		return sectionProperty;
	}

	public void setSectionProperty(SectionProperty sectionProperty) {
		this.sectionProperty = sectionProperty;
	}

	public MaterialProperty getMaterialProperty() {
		return materialProperty;
	}

	public void setMaterialProperty(MaterialProperty materialProperty) {
		this.materialProperty = materialProperty;
	}

	public OneDimFiniteElementType getFiniteElementType() {
		return finiteElementType;
	}

	public void setFiniteElementType(OneDimFiniteElementType finiteElementType) {
		this.finiteElementType = finiteElementType;
	}

	public Node getFirstNode() {
		return firstNode;
	}

	public void setFirstNode(Node firstNode) {
		this.firstNode = firstNode;
	}

	public Node getSecondNode() {
		return secondNode;
	}

	public void setSecondNode(Node secondNode) {
		this.secondNode = secondNode;
	}
	
	public double getLengthOfElement() {
		return lengthOfElement;
	}

	public void setLengthOfElement(double lengthOfElement) {
		this.lengthOfElement = lengthOfElement;
	}

	public double getAngleOfInclination() {
		return angleOfInclination;
	}

	public void setAngleOfInclination(double angleOfInclination) {
		this.angleOfInclination = angleOfInclination;
	}

	public List<Node> getNodesByTheirOrder() {
		List<Node> nodes = new ArrayList<Node>();
		nodes.add(firstNode);
		nodes.add(secondNode);
		return nodes;
	}

}

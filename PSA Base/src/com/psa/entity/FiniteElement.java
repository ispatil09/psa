package com.psa.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.psa.entity.impl.Node;
import com.psa.exception.PSAStructureInstantiationException;
import com.psa.util.ResourceBundleUtil;

public class FiniteElement implements Serializable,Comparable<FiniteElement> {

	private Set<CommonNode> nodes = new HashSet<CommonNode>();
	private int numOfNodes = 0;
	private int elementNumber = 0;
	private StiffnessMatrixEntity stiffnessMatrixEntity;

	public Set<CommonNode> getNodes() {
		return nodes;
	}

	public void setNodes(Set<CommonNode> nodes) {
		this.nodes = nodes;
	}

	public int getNumOfNodes() {
		return numOfNodes;
	}

	protected void setNumOfNodes(int numOfNodes) {
		this.numOfNodes = numOfNodes;
	}

	public int getElementNumber() {
		return elementNumber;
	}

	public void setElementNumber(int elementNumber) {
		this.elementNumber = elementNumber;
	}

	public void addNode(Node node) throws PSAStructureInstantiationException {
		if (getNodes().size() < numOfNodes)
			nodes.add(node);
		else {
			String generalMsg = ResourceBundleUtil.getString("stru_create_error", "MAX_NODE_REACHED");
			throw new PSAStructureInstantiationException(generalMsg+" "+node.getNodeNumber());
		}
	}

	public boolean removeNode(Node node) {
		boolean isRemoved = false;
		if (getNodes().size() != 0) {
			isRemoved = getNodes().remove(node);
		}
		return isRemoved;
	}

	public StiffnessMatrixEntity getStiffnessMatrixEntity() {
		return stiffnessMatrixEntity;
	}

	public void setStiffnessMatrixEntity(StiffnessMatrixEntity stiffnessMatrixEntity) {
		this.stiffnessMatrixEntity = stiffnessMatrixEntity;
	}

	@Override
	public String toString() {

		// return nodes.toString();
		return elementNumber + "";
	}

	@Override
	public boolean equals(Object obj) {
		FiniteElement element = (FiniteElement) obj;
		if (this.elementNumber == element.getElementNumber())
			return true;
		else
			return false;
	}

	@Override
	public int hashCode() {
		return elementNumber;
	}

	@Override
	public int compareTo(FiniteElement feArg) {
		if(this.elementNumber>feArg.getElementNumber())
			return 1;
		else
			return -1;
	}
}

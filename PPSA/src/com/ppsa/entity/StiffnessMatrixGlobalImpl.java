package com.ppsa.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.psa.entity.CommonNode;
import com.psa.entity.GlobalStiffnessMatrixEntity;
import com.psa.entity.enums.StructureType;
import com.psa.entity.impl.Node;
import com.psa.entity.impl.Structure;
import com.psa.exception.PSAException;

/**
 * Since global stiffness matrix is formed by assembling element stiffness matrix,
 * here we need not do any processing. But, simply create a GlobalStiffnessMatrixEntity
 * with proper coordinate numbers. So that element matrix can be fit in here by finding
 * actual coordinate numbers. 
 * @author SONY
 *
 */
public class StiffnessMatrixGlobalImpl implements GlobalStiffnessMatrixEntity,Serializable {

	private int DOF = 0;
	private Double[][] globalStiffnessMatrix;
	private List<Integer> globalCoOrdinatesOfFreedom = new ArrayList<Integer>();

	@Override
	public void setGlobalStiffnessMatrix(Structure structure)
			throws PSAException {
		
		setCoOrdinatesOfFreedom(structure);

		DOF = globalCoOrdinatesOfFreedom.size();
		globalStiffnessMatrix= new Double[DOF][DOF];

		initializeArrayElementsToZero();

		Collections.sort(globalCoOrdinatesOfFreedom);
		structure.setGlobalStiffnessMatrix(this);
	}

	private void initializeArrayElementsToZero() {
		for (int i = 0; i < DOF; i++) {
			for (int j = 0; j < DOF; j++) {
				globalStiffnessMatrix[i][j]=0d;
			}
		}
		
	}

	private void setCoOrdinatesOfFreedom(Structure structure) {
		// later: change check condition.
		if (structure.getStructureType() == StructureType.TRUSS) {
			for (CommonNode commonNode : structure.getNodes()) {
				Node node = (Node) commonNode;
				if (!node.isSupport()) {
					globalCoOrdinatesOfFreedom.add(commonNode.getxCordinateNum());
					globalCoOrdinatesOfFreedom.add(commonNode.getyCordinateNum());
				}
			}
			for (CommonNode commonNode : structure.getNodes()) {
				Node node = (Node) commonNode;
				if (node.isSupport()) {
					globalCoOrdinatesOfFreedom.add(commonNode.getxCordinateNum());
					globalCoOrdinatesOfFreedom.add(commonNode.getyCordinateNum());
				}
			}
		} else if (structure.getStructureType() == StructureType.PLANE) {
			//First adding only numbers of unknown disp 
			for (CommonNode commonNode : structure.getNodes()) {
				Node node = (Node) commonNode;
				if (!node.isSupport()) {
					globalCoOrdinatesOfFreedom.add(commonNode
							.getxCordinateNum());
					globalCoOrdinatesOfFreedom.add(commonNode
							.getyCordinateNum());
					globalCoOrdinatesOfFreedom.add(commonNode
							.getMzCordinateNum());
				}
			}
			// Adding for Known Zero Displacement (KZD)
			for (CommonNode commonNode : structure.getNodes()) {
				Node node = (Node) commonNode;
				if (node.isSupport()) {
					globalCoOrdinatesOfFreedom.add(commonNode
							.getxCordinateNum());
					globalCoOrdinatesOfFreedom.add(commonNode
							.getyCordinateNum());
					globalCoOrdinatesOfFreedom.add(commonNode
							.getMzCordinateNum());
				}
			}
		} else if (structure.getStructureType() == StructureType.TRUSS3D) {
			for (CommonNode commonNode : structure.getNodes()) {
				Node node = (Node) commonNode;
				if (!node.isSupport()) {
					globalCoOrdinatesOfFreedom.add(commonNode.getxCordinateNum());
					globalCoOrdinatesOfFreedom.add(commonNode.getyCordinateNum());
					globalCoOrdinatesOfFreedom.add(commonNode.getzCordinateNum());
				}
			}
			for (CommonNode commonNode : structure.getNodes()) {
				Node node = (Node) commonNode;
				if (node.isSupport()) {
					globalCoOrdinatesOfFreedom.add(commonNode.getxCordinateNum());
					globalCoOrdinatesOfFreedom.add(commonNode.getyCordinateNum());
					globalCoOrdinatesOfFreedom.add(commonNode.getzCordinateNum());
				}
			}
		} else if (structure.getStructureType() == StructureType.SPACE) {
			for (CommonNode commonNode : structure.getNodes()) {
				Node node = (Node) commonNode;
				if (!node.isSupport()) {
					globalCoOrdinatesOfFreedom.add(commonNode.getxCordinateNum());
					globalCoOrdinatesOfFreedom.add(commonNode.getyCordinateNum());
					globalCoOrdinatesOfFreedom.add(commonNode.getzCordinateNum());
					
					globalCoOrdinatesOfFreedom.add(commonNode.getMxCordinateNum());
					globalCoOrdinatesOfFreedom.add(commonNode.getMyCordinateNum());
					globalCoOrdinatesOfFreedom.add(commonNode.getMzCordinateNum());
				}
			}
			for (CommonNode commonNode : structure.getNodes()) {
				Node node = (Node) commonNode;
				if (node.isSupport()) {
					globalCoOrdinatesOfFreedom.add(commonNode.getxCordinateNum());
					globalCoOrdinatesOfFreedom.add(commonNode.getyCordinateNum());
					globalCoOrdinatesOfFreedom.add(commonNode.getzCordinateNum());
					
					globalCoOrdinatesOfFreedom.add(commonNode.getMxCordinateNum());
					globalCoOrdinatesOfFreedom.add(commonNode.getMyCordinateNum());
					globalCoOrdinatesOfFreedom.add(commonNode.getMzCordinateNum());
				}
			}
		}
		
		//globalCoOrdinatesOfFreedom.addAll(structure.getGlobalCoOrdinatesOfFreedom());
	}

	@Override
	public List<Integer> getGlobalCoOrinatesOfFreedom() {
		// TODO Auto-generated method stub
		return this.globalCoOrdinatesOfFreedom;
	}

	@Override
	public Integer getGlobalDegreeOfFreedom() {
		// TODO Auto-generated method stub
		return DOF;
	}

	@Override
	public Double[][] getGlobalStiffnessMatrix() {
		// TODO Auto-generated method stub
		return globalStiffnessMatrix;
	}

}

package com.ppsa.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.psa.entity.CommonNode;
import com.psa.entity.GlobalStiffnessMatrixEntity;
import com.psa.entity.LoadCase;
import com.psa.entity.VectorMatrix;
import com.psa.entity.impl.Node;
import com.psa.entity.impl.Structure;
import com.psa.entity.impl.SupportDetails;

public class DisplacementVector implements VectorMatrix, Serializable {

	private int DOF = 0;
	private SortedMap<Integer, Double> displacementValues = new TreeMap<Integer, Double>();
	private List<Integer> coOrdinatesOfFreedom = new ArrayList<Integer>();

	public void setDisplacementVector(Structure structure) {
		GlobalStiffnessMatrixEntity globalStiffnessMatrix = structure.getGlobalStiffnessMatrix();
		List<Integer> globalCoOrinatesOfFreedom = globalStiffnessMatrix
				.getGlobalCoOrinatesOfFreedom();

		this.coOrdinatesOfFreedom = globalCoOrinatesOfFreedom;
		this.DOF = coOrdinatesOfFreedom.size();

		Set<Node> nodes = structure.getNodes();
		for (CommonNode node : nodes) {

			SupportDetails supportDetails = node.getSupportDetails();
			// double xStiffness = supportDetails.getxStiffness();

			// Check for xCoOrd
			int xCordinateNum = node.getxCordinateNum();
			// later: use node.isSupport() to check put for xStiffness
			if (supportDetails.isxDisplaceabale()) {
				displacementValues.put(xCordinateNum, 1d);
			} else {
				displacementValues.put(xCordinateNum, 0d);
			}

			// Check for yCoOrd
			int yCordinateNum = node.getyCordinateNum();

			if (supportDetails.isyDisplaceabale()) {
				displacementValues.put(yCordinateNum, 1d);
			} else {
				displacementValues.put(yCordinateNum, 0d);
			}
			
			int zCordinateNum = node.getzCordinateNum();
			if (supportDetails.iszDisplaceabale()) {
				displacementValues.put(zCordinateNum, 1d);
			} else {
				displacementValues.put(zCordinateNum, 0d);
			}
			
			int mxCordinateNum = node.getMxCordinateNum();
			if (supportDetails.isxRotatable()) {
				displacementValues.put(mxCordinateNum, 1d);
			} else {
				displacementValues.put(mxCordinateNum, 0d);
			}
			
			int myCordinateNum = node.getMyCordinateNum();
			if (supportDetails.isyRotatable()) {
				displacementValues.put(myCordinateNum, 1d);
			} else {
				displacementValues.put(myCordinateNum, 0d);
			}
			
			// Check for mzCoOrd
			int mzCordinateNum = node.getMzCordinateNum();
			if (supportDetails.iszRotatable()) {
				displacementValues.put(mzCordinateNum, 1d);
			} else {
				displacementValues.put(mzCordinateNum, 0d);
			}
		}

		//here use another criteria loadcase support displacement
		
		/**
		 * If problem is 2d truss. Even then i am trying to add mzCordinateNum
		 * which is '0'. So remove that '0' item. In general below remove
		 * command can act as filter for all the unwanted co-ordinates. Only
		 * Required co-ordinates generated by CoOrdinateNumberingUtil are non
		 * zero and are persisted without any problem.
		 */
		displacementValues.remove(0);
		
		//Collections.sort(coOrdinatesOfFreedom);
		
		structure.setDisplacementVectorMatrix(this);

	}

	@Override
	public List<Integer> getCoOrdinatedOfFreedom() {
		return this.coOrdinatesOfFreedom;
	}

	@Override
	public int getSizeOfVector() {
		return this.DOF;
	}

	@Override
	public Map<Integer, Double> getVectorValues() {
		return displacementValues;
	}

}
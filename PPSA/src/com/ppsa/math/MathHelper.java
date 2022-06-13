package com.ppsa.math;

import java.util.ArrayList;
import java.util.Set;

import com.ppsa.entity.StiffnessMatrixBeam3DImpl;
import com.ppsa.math.impl.MathOperationJAMAImpl;
import com.psa.entity.CommonNode;
import com.psa.entity.StiffnessMatrixEntity;
import com.psa.entity.LoadCase.MemberLoad.UniformForce;
import com.psa.entity.impl.Node;
import com.psa.entity.impl.OneDimFiniteElement;

public class MathHelper {
	/*private static void setElementLengthAndAngle(OneDimFiniteElement oneDimFiniteElement) {
		Node firstNode = oneDimFiniteElement.getFirstNode();
		Node secondNode = oneDimFiniteElement.getSecondNode();
		
		double xLengthDifference = secondNode.getxCordinate()
				- firstNode.getxCordinate();
		double yLengthDifference = secondNode.getyCordinate()
				- firstNode.getyCordinate();

		double ratio = yLengthDifference / xLengthDifference;
		// System.out.println("Ratio = "+ratio);

		double angle = Math.toDegrees(Math.atan(ratio));
		double angleOfInclination = angle;
		// System.out.println("Angle  = "+angle);
		double lengthOfFiniteElement = (yLengthDifference / (Math.sin(Math
				.toRadians(angle))));

		if (yLengthDifference == 0 && angle == 0) // Because gives math error
			lengthOfFiniteElement = xLengthDifference;

		oneDimFiniteElement.setAngleOfInclination(angleOfInclination);
		oneDimFiniteElement.setLengthOfElement(lengthOfFiniteElement);
	}*/
	
	/*public static void setBasicOneDimFiniteElementData(OneDimFiniteElement oneDimFiniteElement) {
		Set<CommonNode> nodes = oneDimFiniteElement.getNodes();
		// Considering that only two nodes are present
		CommonNode firstNode = null;
		CommonNode secondNode = null;

		ArrayList<CommonNode> nodesArray = new ArrayList<CommonNode>(nodes);

		if (nodesArray.get(0).getxCordinate() == nodesArray.get(1)
				.getxCordinate()) {
			System.out
					.println("First node x same as sec node x. So chose below");
			if (nodesArray.get(0).getyCordinate() < nodesArray.get(1)
					.getyCordinate()) {
				firstNode = nodesArray.get(0);
				secondNode = nodesArray.get(1);
			} else {
				firstNode = nodesArray.get(1);
				secondNode = nodesArray.get(0);
			}
			
			
			  Original before finding some clarification from
			 *  testCase "SimplePlaneConcYSec".
			 
			 
			 if (nodesArray.get(0).getyCordinate() < nodesArray.get(1)
					.getyCordinate()) {
				firstNode = nodesArray.get(1);
				secondNode = nodesArray.get(0);
			} else {
				firstNode = nodesArray.get(0);
				secondNode = nodesArray.get(1);
			}
			
			*//**
			 * This is how STAAD considers first and 
			 * second node when both nodes x coOrdinates are equal.
			 * i.e., based on the lowest NODE NUMBER and NOT by 
			 * judgment of their Y CO-ORDINATE value.
			 * 
			 *//*
			if (nodesArray.get(0).getNodeNumber() < nodesArray.get(1)
					.getNodeNumber()) {
				firstNode = nodesArray.get(0);
				secondNode = nodesArray.get(1);
			} else {
				firstNode = nodesArray.get(1);
				secondNode = nodesArray.get(0);
			}

		} else if (nodesArray.get(0).getxCordinate() < nodesArray.get(1)
				.getxCordinate()) {
			System.out.println("First node x is less");
			firstNode = nodesArray.get(0);
			secondNode = nodesArray.get(1);
		} else {
			System.out.println("Second node x is less");
			firstNode = nodesArray.get(1);
			secondNode = nodesArray.get(0);
		}
		
		oneDimFiniteElement.setFirstNode((Node) firstNode);
		oneDimFiniteElement.setSecondNode((Node) secondNode);
		
		setElementLengthAndAngle(oneDimFiniteElement);
	}*/

	/**
	 * Multiply by 'Sin'.
	 * @param resultantNodalForceOnNode
	 * @param angleOfInclination
	 * @return
	 */
	public static double resolveForceInX(double resultantNodalForceOnNode,
			double angleOfInclination) {
		double angleInRadians = Math.toRadians(angleOfInclination);
		double forceInX = resultantNodalForceOnNode*Math.sin(angleInRadians);
		return forceInX;
	}

	/**
	 * Multiply by 'Cos'.
	 * @param resultantNodalForceOnFirstNode
	 * @param angleOfInclination
	 * @return
	 */
	public static double resolveForceInY(double resultantNodalForceOnFirstNode,
			double angleOfInclination) {
		double angleInRadians = Math.toRadians(angleOfInclination);
		double forceInY = (resultantNodalForceOnFirstNode)*(Math.cos(angleInRadians));
		return forceInY;
	}
	
	public static double getMod(double doubleVal) {
		if (doubleVal < 0)
			return -doubleVal;
		else
			return doubleVal;
	}

	public static double[] resolveForcesToLocal(double[] globalForceVals,
			OneDimFiniteElement oneDimFiniteElement) {
		StiffnessMatrixEntity stiffnessMatrixEntity = oneDimFiniteElement
				.getStiffnessMatrixEntity();
		StiffnessMatrixBeam3DImpl beam3dImpl = (StiffnessMatrixBeam3DImpl) stiffnessMatrixEntity;

		double[][] transformationMatrix = beam3dImpl.getTransformationMatrix();
		
		double[] localForceVector = new MathOperationJAMAImpl()
				.transformGlobalForceVector(transformationMatrix,
						globalForceVals);

		return localForceVector;
	}
	
	public static double getGlobalForceFromProjectedLength_PZ(
			UniformForce uniformForce, OneDimFiniteElement oneDimFiniteElement) {
		Node firstNode = oneDimFiniteElement.getFirstNode();
		Node secondNode = oneDimFiniteElement.getSecondNode();
		double lengthOfElement = oneDimFiniteElement.getLengthOfElement();
		
		double yCordNodeOne = firstNode.getyCordinate();
		double yCordNodeSec = secondNode.getyCordinate();
		double yProjectionLength = yCordNodeSec-yCordNodeOne;
		
		double xCordNodeOne = firstNode.getxCordinate();
		double xCordNodeSec = secondNode.getxCordinate();
		double xProjectionLength = xCordNodeSec-xCordNodeOne;
		// The length of beam seen from y-axis(Plan).  
		double elementProjectedLengthFromPlan = Math.sqrt((yProjectionLength*yProjectionLength)+(xProjectionLength*xProjectionLength));
		double multFactor = elementProjectedLengthFromPlan/lengthOfElement;
		double forceValProjected = uniformForce.getForceVal();
		double forceInGlobalZ = forceValProjected*multFactor;
		return forceInGlobalZ;
	}


	public static double getGlobalForceFromProjectedLength_PX(
			UniformForce uniformForce, OneDimFiniteElement oneDimFiniteElement) {
		Node firstNode = oneDimFiniteElement.getFirstNode();
		Node secondNode = oneDimFiniteElement.getSecondNode();
		double lengthOfElement = oneDimFiniteElement.getLengthOfElement();
		
		double zCordNodeOne = firstNode.getzCordinate();
		double zCordNodeSec = secondNode.getzCordinate();
		double zProjectionLength = zCordNodeSec-zCordNodeOne;
		
		double yCordNodeOne = firstNode.getyCordinate();
		double yCordNodeSec = secondNode.getyCordinate();
		double yProjectionLength = yCordNodeSec-yCordNodeOne;
		// The length of beam seen from y-axis(Plan).  
		double elementProjectedLengthFromPlan = Math.sqrt((zProjectionLength*zProjectionLength)+(yProjectionLength*yProjectionLength));
		double multFactor = elementProjectedLengthFromPlan/lengthOfElement;
		double forceValProjected = uniformForce.getForceVal();
		double forceInGlobalX = forceValProjected*multFactor;
		return forceInGlobalX;
	}


	public static double getGlobalForceFromProjectedLength_PY(UniformForce uniformForce,
			OneDimFiniteElement oneDimFiniteElement) {
		Node firstNode = oneDimFiniteElement.getFirstNode();
		Node secondNode = oneDimFiniteElement.getSecondNode();
		double lengthOfElement = oneDimFiniteElement.getLengthOfElement();
		
		double zCordNodeOne = firstNode.getzCordinate();
		double zCordNodeSec = secondNode.getzCordinate();
		double zProjectionLength = zCordNodeSec-zCordNodeOne;
		
		double xCordNodeOne = firstNode.getxCordinate();
		double xCordNodeSec = secondNode.getxCordinate();
		double xProjectionLength = xCordNodeSec-xCordNodeOne;
		// The length of beam seen from y-axis(Plan).  
		double elementProjectedLengthFromPlan = Math.sqrt((zProjectionLength*zProjectionLength)+(xProjectionLength*xProjectionLength));
		double multFactor = elementProjectedLengthFromPlan/lengthOfElement;
		double forceValProjected = uniformForce.getForceVal();
		double forceInGlobalY = forceValProjected*multFactor;
		return forceInGlobalY;
	}
	public static double[] resolveToToGlobalFomLocal(double[] localForceVals,
			OneDimFiniteElement oneDimFiniteElement) {
		StiffnessMatrixEntity stiffnessMatrixEntity = oneDimFiniteElement.getStiffnessMatrixEntity();
		StiffnessMatrixBeam3DImpl beam3dImpl = (StiffnessMatrixBeam3DImpl) stiffnessMatrixEntity;
		double[][] transformationMatrix = beam3dImpl.getTransformationMatrix();
		
		MathOperationJAMAImpl jamaImpl = new MathOperationJAMAImpl();
		return jamaImpl.transformLocalForceVector(transformationMatrix,localForceVals);
	}
}

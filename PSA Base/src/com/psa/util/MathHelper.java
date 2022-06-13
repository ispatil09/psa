package com.psa.util;

import java.util.ArrayList;
import java.util.Set;

import com.psa.entity.CommonNode;
import com.psa.entity.enums.StructureType;
import com.psa.entity.impl.Node;
import com.psa.entity.impl.OneDimFiniteElement;
import com.psa.entity.impl.Structure;

public class MathHelper {
	private static void setElementLengthAndAngle(OneDimFiniteElement oneDimFiniteElement, Structure structure) {
		Node firstNode = oneDimFiniteElement.getFirstNode();
		Node secondNode = oneDimFiniteElement.getSecondNode();
		
		StructureType structureType = structure.getStructureType();
		if(structureType==StructureType.PLANE || structureType==StructureType.TRUSS) {
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

			lengthOfFiniteElement=getMod(lengthOfFiniteElement);
			oneDimFiniteElement.setAngleOfInclination(angleOfInclination);
			oneDimFiniteElement.setLengthOfElement(lengthOfFiniteElement);
		} else {
			double xCordinateFirst = firstNode.getxCordinate();
			double yCordinateFirst = firstNode.getyCordinate();
			double zCordinateFirst = firstNode.getzCordinate();
			
			double xCordinateSecond = secondNode.getxCordinate();
			double yCordinateSecond = secondNode.getyCordinate();
			double zCordinateSecond = secondNode.getzCordinate();
			
			double xDiff = xCordinateSecond-xCordinateFirst;
			double yDiff = yCordinateSecond-yCordinateFirst;
			double zDiff = zCordinateSecond-zCordinateFirst;
			
			double xDiffSquare = xDiff*xDiff;
			double yDiffSquare = yDiff*yDiff;
			double zDiffSquare = zDiff*zDiff;
			
			double value = xDiffSquare+yDiffSquare+zDiffSquare;
			
			double elementLength = Math.sqrt(value);
			oneDimFiniteElement.setLengthOfElement(elementLength);
		}
	}

	public static void setBasicOneDimFiniteElementData(OneDimFiniteElement oneDimFiniteElement, Structure structure) {
		Set<CommonNode> nodes = oneDimFiniteElement.getNodes();
		// Considering that only two nodes are present
		Node firstNode = oneDimFiniteElement.getFirstNode();
		Node secondNode = oneDimFiniteElement.getFirstNode();

		ArrayList<CommonNode> nodesArray = new ArrayList<CommonNode>(nodes);

		
		/**
		 * Decide firstNode and secondNode.
		 */
		/*StructureType structureType = structure.getStructureType();
		if (structureType == StructureType.PLANE
				|| structureType == StructureType.TRUSS) {
			if (nodesArray.get(1).getxCordinate() < nodesArray.get(0)
					.getxCordinate()) {
				System.out.println("First node x is less");
				oneDimFiniteElement.setFirstNode((Node) nodesArray.get(1));
				oneDimFiniteElement.setSecondNode((Node) nodesArray.get(0));
			}
		}*//*else if (nodesArray.get(0).getxCordinate() > nodesArray.get(1)
				.getxCordinate()) {
			System.out.println("Second node x is less");
			firstNode = nodesArray.get(1);
			secondNode = nodesArray.get(0);
		}
		
		*//**
		 * If this condition fails then it means that
		 * firstNode and secondNode are decided while adding
		 * nodes itself. Based on first added node entry. 
		 *//*
		if (nodesArray.get(0).getxCordinate() != nodesArray.get(1)
				.getxCordinate()) {
			oneDimFiniteElement.setFirstNode((Node) firstNode);
			oneDimFiniteElement.setSecondNode((Node) secondNode);
		}*/
		
		setElementLengthAndAngle(oneDimFiniteElement,structure);
	}
	
	
	/**
	  * Below code not required for BasicPSA Project.
	  */
	/*public static double resolveForceInX(double resultantNodalForceOnFirstNode,
			double angleOfInclination) {
		double angleInRadians = Math.toRadians(angleOfInclination);
		double forceInX = resultantNodalForceOnFirstNode*Math.sin(angleInRadians);
		return forceInX;
	}

	 
	public static double resolveForceInY(double resultantNodalForceOnFirstNode,
			double angleOfInclination) {
		double angleInRadians = Math.toRadians(angleOfInclination);
		double forceInY = (resultantNodalForceOnFirstNode)*(Math.cos(angleInRadians));
		return forceInY;
	}*/
	
	public static double getMod(double doubleVal) {
		if (doubleVal < 0)
			return -doubleVal;
		else
			return doubleVal;
	}
}

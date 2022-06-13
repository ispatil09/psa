package com.ppsa.util;

import java.util.Set;

import com.psa.entity.CommonNode;
import com.psa.entity.enums.StructureType;
import com.psa.entity.impl.Node;
import com.psa.entity.impl.Structure;
import com.psa.entity.impl.SupportDetails;

public class CoOrdinateNumberingUtil {

	public static void setCoOrdinateNumbersForStructure(Structure structure) {
		Set<Node> nodes = structure.getNodes();
		//List<Integer> globalCoOrdinatesOfFreedom = structure.getGlobalCoOrdinatesOfFreedom();
		
		StructureType structureType = structure.getStructureType();
		if(structureType==StructureType.TRUSS)
			setTrussPlaneCoOrdinates(nodes);
		else if(structureType==StructureType.PLANE)
			setFramePlaneCoOrdinates(nodes);
		else if(structureType==StructureType.TRUSS3D)
			setTruss3DCoOrdinates(nodes);
		else if(structureType==StructureType.SPACE)
			setSpaceFrameCoOrdinates(nodes);
	}

	private static void setSpaceFrameCoOrdinates(Set<Node> nodes) {
		int coOrdNum = 1;
		for (CommonNode node : nodes) {
			Node nodeImpl = (Node) node;
			boolean supportFlag = nodeImpl.isSupport();
			
			if (!supportFlag) {
				//globalCoOrdinatesOfFreedom.add(coOrdNum);
				node.setxCordinateNum(coOrdNum++);
				//globalCoOrdinatesOfFreedom.add(coOrdNum);
				node.setyCordinateNum(coOrdNum++);
				//node.setyCordinateNum(coOrdNum++);
				node.setzCordinateNum(coOrdNum++);
				
				node.setMxCordinateNum(coOrdNum++);
				node.setMyCordinateNum(coOrdNum++);
				node.setMzCordinateNum(coOrdNum++);
			}
		}
		for (CommonNode node : nodes) {
			Node nodeImpl = (Node) node;
			boolean supportFlag = nodeImpl.isSupport();
			
			if (supportFlag) {
				//globalCoOrdinatesOfFreedom.add(coOrdNum);
				node.setxCordinateNum(coOrdNum++);
				//globalCoOrdinatesOfFreedom.add(coOrdNum);
				node.setyCordinateNum(coOrdNum++);
				//node.setyCordinateNum(coOrdNum++);
				node.setzCordinateNum(coOrdNum++);
				
				node.setMxCordinateNum(coOrdNum++);
				node.setMyCordinateNum(coOrdNum++);
				node.setMzCordinateNum(coOrdNum++);
			}
		}
	}

	private static void setTruss3DCoOrdinates(Set<Node> nodes) {
		int coOrdNum = 1;
		for (CommonNode node : nodes) {
			
			Node nodeImpl = (Node) node;
			boolean supportFlag = nodeImpl.isSupport();
			
			if (!supportFlag) {
				//globalCoOrdinatesOfFreedom.add(coOrdNum);
				node.setxCordinateNum(coOrdNum++);
				//globalCoOrdinatesOfFreedom.add(coOrdNum);
				node.setyCordinateNum(coOrdNum++);
				//node.setyCordinateNum(coOrdNum++);
				node.setzCordinateNum(coOrdNum++);
			}
		}
		for (CommonNode node : nodes) {
			
			Node nodeImpl = (Node) node;
			boolean supportFlag = nodeImpl.isSupport();
			
			if (supportFlag) {
				//globalCoOrdinatesOfFreedom.add(coOrdNum);
				node.setxCordinateNum(coOrdNum++);
				//globalCoOrdinatesOfFreedom.add(coOrdNum);
				node.setyCordinateNum(coOrdNum++);
				//node.setyCordinateNum(coOrdNum++);
				node.setzCordinateNum(coOrdNum++);
			}
		}
	}

	private static void setFramePlaneCoOrdinates(Set<Node> nodes) {
		int coOrdNum = 1;
		/**
		 * All the movable nodes(that are not support)
		 */
		for (CommonNode node : nodes) {
			//globalCoOrdinatesOfFreedom.add(coOrdNum);
			Node nodeImpl = (Node) node;
			boolean supportFlag = nodeImpl.isSupport();
			
			if (!supportFlag) {
				node.setxCordinateNum(coOrdNum++);
				// globalCoOrdinatesOfFreedom.add(coOrdNum);
				node.setyCordinateNum(coOrdNum++);
				// node.setzCordinateNum(++coOrdNum); Only for Space structures.

				// globalCoOrdinatesOfFreedom.add(coOrdNum);
				node.setMzCordinateNum(coOrdNum++);
				// node.setMyCordinateNum(++coOrdNum);
				// node.setMxCordinateNum(++coOrdNum);
			}
		}
		
		/**
		 * From all the nodes which have released DOF 
		 */
		for (CommonNode node : nodes) {
			//globalCoOrdinatesOfFreedom.add(coOrdNum);
			Node nodeImpl = (Node) node;
			boolean supportFlag = nodeImpl.isSupport();
			SupportDetails supportDetails = nodeImpl.getSupportDetails();
			//Node nodeImpl = (Node) node;
			if (supportFlag) {
				if(supportDetails.isxDisplaceabale())
					node.setxCordinateNum(coOrdNum++);
				// globalCoOrdinatesOfFreedom.add(coOrdNum);
				if(supportDetails.isyDisplaceabale())
					node.setyCordinateNum(coOrdNum++);
				// node.setzCordinateNum(++coOrdNum); Only for Space structures.

				// globalCoOrdinatesOfFreedom.add(coOrdNum);
				if(supportDetails.iszRotatable())
					node.setMzCordinateNum(coOrdNum++);
				// node.setMyCordinateNum(++coOrdNum);
				// node.setMxCordinateNum(++coOrdNum);
			}
		}
		/**
		 * All nodes that are restrained??????
		 */
		for (CommonNode node : nodes) {
			//globalCoOrdinatesOfFreedom.add(coOrdNum);
			Node nodeImpl = (Node) node;
			boolean supportFlag = nodeImpl.isSupport();
			SupportDetails supportDetails = nodeImpl.getSupportDetails();
			//Node nodeImpl = (Node) node;
			if (supportFlag) {
				if(!supportDetails.isxDisplaceabale())
					node.setxCordinateNum(coOrdNum++);
				// globalCoOrdinatesOfFreedom.add(coOrdNum);
				if(!supportDetails.isyDisplaceabale())
					node.setyCordinateNum(coOrdNum++);
				// node.setzCordinateNum(++coOrdNum); Only for Space structures.

				// globalCoOrdinatesOfFreedom.add(coOrdNum);
				if(!supportDetails.iszRotatable())
					node.setMzCordinateNum(coOrdNum++);
				// node.setMyCordinateNum(++coOrdNum);
				// node.setMxCordinateNum(++coOrdNum);
			}
		}
	}

	private static void setTrussPlaneCoOrdinates(Set<Node> nodes) {
		int coOrdNum = 1;
		for (CommonNode node : nodes) {
			//globalCoOrdinatesOfFreedom.add(coOrdNum);
			Node nodeImpl = (Node) node;
			boolean supportFlag = nodeImpl.isSupport();
			
			if(!supportFlag) {
			node.setxCordinateNum(coOrdNum++);
			//globalCoOrdinatesOfFreedom.add(coOrdNum);
			node.setyCordinateNum(coOrdNum++);
			//node.setyCordinateNum(coOrdNum++);
			} 
		}
		
		for (CommonNode node : nodes) {
			//globalCoOrdinatesOfFreedom.add(coOrdNum);
			Node nodeImpl = (Node) node;
			boolean supportFlag = nodeImpl.isSupport();
			
			if(supportFlag) {
			node.setxCordinateNum(coOrdNum++);
			//globalCoOrdinatesOfFreedom.add(coOrdNum);
			node.setyCordinateNum(coOrdNum++);
			//node.setyCordinateNum(coOrdNum++);
			} 
		}
	}

	/**
	 * Not used in processing, but may be User will need this.
	 * 
	 * @param structureType
	 * @param node1
	 * @param node2
	 */
	public static void swapCoOrinateNums(StructureType structureType,
			CommonNode node1, CommonNode node2) {
		if (structureType == StructureType.PLANE) {
			int node1XCord = node1.getxCordinateNum();
			int node1YCord = node1.getyCordinateNum();

			int node2XCord = node2.getxCordinateNum();
			int node2YCord = node2.getyCordinateNum();

			node1.setxCordinateNum(node2XCord);
			node1.setyCordinateNum(node2YCord);

			node2.setxCordinateNum(node1XCord);
			node2.setyCordinateNum(node1YCord);
		}
	}
}

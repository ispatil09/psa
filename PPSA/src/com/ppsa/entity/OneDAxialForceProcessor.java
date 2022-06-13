package com.ppsa.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ppsa.exception.PPSAException;
import com.ppsa.exception.PPSAMathException;
import com.ppsa.math.impl.MathOperationJAMAImpl;
import com.ppsa.util.EntityTranslationUtil;
import com.psa.entity.FiniteElement;
import com.psa.entity.LoadCase;
import com.psa.entity.OneDAxialForceEntity;
import com.psa.entity.StiffnessMatrixEntity;
import com.psa.entity.VectorMatrix;
import com.psa.entity.impl.Node;
import com.psa.entity.impl.NodeResults;
import com.psa.entity.impl.OneDimFiniteElement;
import com.psa.entity.impl.Structure;

public class OneDAxialForceProcessor implements Serializable {
	/*private int numOfElements = 0;
	private Map<Integer, Double> axialForceValues = new HashMap<Integer, Double>();*/
	private List<Integer> oneDFiniteElementNumbers = new ArrayList<Integer>();
	private List<OneDimFiniteElement> oneDFiniteElements = new ArrayList<OneDimFiniteElement>();

	public void processAxialForceInMembers(LoadCase loadCase) throws PPSAException {
		Structure structure = loadCase.getParentStructure();
		Set<FiniteElement> finiteElements = structure.getFiniteElements();
		for (FiniteElement finiteElement : finiteElements) {
			
			//FiniteElement finiteElement = (OneDimFiniteElementChild) childElement;
			findOneDFiniteElements(finiteElement);
		}
		// this.numOfElements = this.oneDFiniteElementNumbers.size();
		Map<OneDimFiniteElement, OneDAxialForceEntity> axialForceEntities = loadCase.getAxialForceEntities();
		for (OneDimFiniteElement oneDFiniteElement : this.oneDFiniteElements) {
			OneDAxialForceEntity oneDAxialForceEntity = axialForceEntities
					.get(oneDFiniteElement);
			try {
				processThisOneDFiniteElement(oneDFiniteElement,
						oneDAxialForceEntity,loadCase);
			} catch (PPSAMathException e) {
				throw new PPSAException(e.getMessage());
			}
		}
	}

	private void processThisOneDFiniteElement(
			OneDimFiniteElement oneDimFiniteElement,
			OneDAxialForceEntity oneDAxialForceEntity, LoadCase loadCase) throws PPSAMathException {

		setMemberCSValueVector(oneDimFiniteElement, oneDAxialForceEntity);
		setMemberAssociatedNodeDispValues(oneDimFiniteElement,
				oneDAxialForceEntity,loadCase);

		VectorMatrix csValueVector = oneDAxialForceEntity.getCSValueVector();
		double[][] csMatric = EntityTranslationUtil.getDoubleArrayFromMatrix(csValueVector);

		VectorMatrix nodeAssociatedDispMatrix = oneDAxialForceEntity.getNodesAssociatedDispVector();
		double[][] nodeAssociatedDispMatrixDoubleArray = EntityTranslationUtil.getDoubleArrayFromMatrixAndInverseIt(nodeAssociatedDispMatrix);

		double matMulRes = new MathOperationJAMAImpl().getAxialMemberForce(csMatric, nodeAssociatedDispMatrixDoubleArray);

		double AEbyLVal = getAEbyL(oneDimFiniteElement);

		double memberAxialForce=matMulRes*AEbyLVal;
		//System.out.println("OutOfSci : "+memberAxialForce);
		oneDAxialForceEntity.setAEbyLval(AEbyLVal);
		oneDAxialForceEntity.setAxialForce(memberAxialForce);
	}

	private double getAEbyL(OneDimFiniteElement oneDimFiniteElement) {
		double csArea = oneDimFiniteElement.getSectionProperty().getCSArea();
		double eVal = oneDimFiniteElement.getMaterialProperty().getE();
		double lengthOfFiniteElement = oneDimFiniteElement.getLengthOfElement();
		return csArea*eVal/lengthOfFiniteElement;
	}

	private void setMemberAssociatedNodeDispValues(
			OneDimFiniteElement oneDimFiniteElement,
			OneDAxialForceEntity oneDAxialForceEntity, LoadCase loadCase) {
		NodeDispVectorForOneDElement dispValueVector = new NodeDispVectorForOneDElement().getDispValueVector(oneDimFiniteElement, loadCase);
		oneDAxialForceEntity.setNodesAssociatedDispVector(dispValueVector);
	}

	private void setMemberCSValueVector(
			OneDimFiniteElement oneDimFiniteElement,
			OneDAxialForceEntity oneDAxialForceEntity) {
		CSValueVector csValueVector = new CSValueVector()
				.getCSValueVector(oneDimFiniteElement);
		oneDAxialForceEntity.setCSValueVector(csValueVector);
	}

	private void findOneDFiniteElements(FiniteElement finiteElement) {
		if (finiteElement instanceof OneDimFiniteElement) {
			OneDimFiniteElement oneDimFiniteElement = (OneDimFiniteElement) finiteElement;
			oneDFiniteElementNumbers.add(oneDimFiniteElement.getElementNumber());
			oneDFiniteElements.add(oneDimFiniteElement);
		}
	}

	private class CSValueVector implements VectorMatrix,Serializable {
		private int DOF = 0;
		private Map<Integer, Double> vectorValues = new HashMap<Integer, Double>();
		private List<Integer> coOrdinatesOfFreedom = new ArrayList<Integer>();

		public CSValueVector getCSValueVector(
				OneDimFiniteElement oneDimFiniteElement) {

			StiffnessMatrixEntity stiffnessMatrixEntity = oneDimFiniteElement
					.getStiffnessMatrixEntity();
			// Later use oneDimFiniteElement.getMemberType., to check type of
			// member.
			if (stiffnessMatrixEntity instanceof StiffnessMatrixTruss2DImpl) {
				processFor2DTrussElement(stiffnessMatrixEntity);
			} else if (stiffnessMatrixEntity instanceof StiffnessMatrixBeam2DImpl) {
				processFor2DBeamElement(stiffnessMatrixEntity);
			} else if (stiffnessMatrixEntity instanceof StiffnessMatrixTruss3DImpl) {
				processFor3DTrussElement(stiffnessMatrixEntity);
			} else if (stiffnessMatrixEntity instanceof StiffnessMatrixBeam3DImpl) {
				processFor3DBeamElement(stiffnessMatrixEntity);
			}

			return this;
		}


		/**
		 * Implementation same as 3D-Truss.
		 * @param stiffnessMatrixEntity
		 */
		private void processFor3DBeamElement(
				StiffnessMatrixEntity stiffnessMatrixEntity) {
			// Add dummy values. However those r not required.
			for (int i = 1; i < 13; i++) {
				this.coOrdinatesOfFreedom.add(i);
			}
			this.DOF = coOrdinatesOfFreedom.size();

			StiffnessMatrixBeam3DImpl stiffnessMatrixBeam3DImpl = (StiffnessMatrixBeam3DImpl) stiffnessMatrixEntity;
			double cxValue = stiffnessMatrixBeam3DImpl.getLamda();
			double cyValue = stiffnessMatrixBeam3DImpl.getMu();
			double czValue = stiffnessMatrixBeam3DImpl.getNu();

			this.vectorValues.put(1, cxValue);
			this.vectorValues.put(2, cyValue);
			this.vectorValues.put(3, czValue);
			this.vectorValues.put(4, 0d);
			this.vectorValues.put(5, 0d);
			this.vectorValues.put(6, 0d);
			this.vectorValues.put(7, -cxValue);
			this.vectorValues.put(8, -cyValue);
			this.vectorValues.put(9, -czValue);
			this.vectorValues.put(10, 0d);
			this.vectorValues.put(11, 0d);
			this.vectorValues.put(12, 0d);
		}


		private void processFor3DTrussElement(
				StiffnessMatrixEntity stiffnessMatrixEntity) {
			// Add dummy values. However those r not required.
			for (int i = 1; i < 7; i++) {
				this.coOrdinatesOfFreedom.add(i);
			}
			this.DOF = coOrdinatesOfFreedom.size();

			StiffnessMatrixTruss3DImpl stiffnessMatrixTruss3DImpl = (StiffnessMatrixTruss3DImpl) stiffnessMatrixEntity;
			double cxValue = stiffnessMatrixTruss3DImpl.getCx();
			double cyValue = stiffnessMatrixTruss3DImpl.getCy();
			double czValue = stiffnessMatrixTruss3DImpl.getCz();

			this.vectorValues.put(1, cxValue);
			this.vectorValues.put(2, cyValue);
			this.vectorValues.put(3, czValue);
			this.vectorValues.put(4, -cxValue);
			this.vectorValues.put(5, -cyValue);
			this.vectorValues.put(6, -czValue);
		}

		private void processFor2DBeamElement(
				StiffnessMatrixEntity stiffnessMatrixEntity) {
			// Add dummy values. However those r not required.
			for (int i = 1; i < 7; i++) {
				this.coOrdinatesOfFreedom.add(i);
			}
			this.DOF = coOrdinatesOfFreedom.size();

			StiffnessMatrixBeam2DImpl stiffnessMatrixTruss2DImpl = (StiffnessMatrixBeam2DImpl) stiffnessMatrixEntity;
			double cValue = stiffnessMatrixTruss2DImpl.getCValue();
			double sValue = stiffnessMatrixTruss2DImpl.getSValue();

			this.vectorValues.put(1, cValue);
			this.vectorValues.put(2, sValue);
			this.vectorValues.put(3, 0d);
			this.vectorValues.put(4, -cValue);
			this.vectorValues.put(5, -sValue);
			this.vectorValues.put(6, 0d);
		}

		private void processFor2DTrussElement(
				StiffnessMatrixEntity stiffnessMatrixEntity) {
			// Add dummy values. However those r not required.
			for (int i = 1; i < 5; i++) {
				this.coOrdinatesOfFreedom.add(i);
			}
			this.DOF = coOrdinatesOfFreedom.size();

			StiffnessMatrixTruss2DImpl stiffnessMatrixTruss2DImpl = (StiffnessMatrixTruss2DImpl) stiffnessMatrixEntity;
			double cValue = stiffnessMatrixTruss2DImpl.getCValue();
			double sValue = stiffnessMatrixTruss2DImpl.getSValue();

			this.vectorValues.put(1, cValue);
			this.vectorValues.put(2, sValue);
			this.vectorValues.put(3, -cValue);
			this.vectorValues.put(4, -sValue);
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
			return this.vectorValues;
		}

	}

	private class NodeDispVectorForOneDElement implements VectorMatrix,Serializable {
		private int DOF = 0;
		private Map<Integer, Double> vectorValues = new LinkedHashMap<Integer, Double>();
		private List<Integer> coOrdinatesOfFreedom = new ArrayList<Integer>();

		public NodeDispVectorForOneDElement getDispValueVector(
				OneDimFiniteElement oneDimFiniteElement,LoadCase loadCase) {

			StiffnessMatrixEntity stiffnessMatrixEntity = oneDimFiniteElement
					.getStiffnessMatrixEntity();
			// Later use oneDimFiniteElement.getMemberType., to check type of
			// member.
			if (stiffnessMatrixEntity instanceof StiffnessMatrixTruss2DImpl) {
				processFor2DTrussElement(oneDimFiniteElement, loadCase);
			} else if (stiffnessMatrixEntity instanceof StiffnessMatrixBeam2DImpl) {
				processFor2DBeamElement(oneDimFiniteElement, loadCase);
			} else if (stiffnessMatrixEntity instanceof StiffnessMatrixTruss3DImpl) {
				processFor3DTrussElement(oneDimFiniteElement, loadCase);
			} else if (stiffnessMatrixEntity instanceof StiffnessMatrixBeam3DImpl) {
				processFor3DTrussElement_Space(oneDimFiniteElement, loadCase);
			}

			return this;
		}

		private void processFor3DTrussElement_Space(
				OneDimFiniteElement oneDimFiniteElement, LoadCase loadCase) {
			Node firstNode = oneDimFiniteElement.getFirstNode();
			
			this.coOrdinatesOfFreedom.add(firstNode.getxCordinateNum());
			this.coOrdinatesOfFreedom.add(firstNode.getyCordinateNum());
			this.coOrdinatesOfFreedom.add(firstNode.getzCordinateNum());
			this.coOrdinatesOfFreedom.add(firstNode.getMxCordinateNum());
			this.coOrdinatesOfFreedom.add(firstNode.getMyCordinateNum());
			this.coOrdinatesOfFreedom.add(firstNode.getMzCordinateNum());
			
			NodeResults nodeResults = loadCase.getNodeResults().get(firstNode);
			
			Double xDisp = nodeResults.getxDisplacement();
			Double yDisp = nodeResults.getyDisplacement();
			Double zDisp = nodeResults.getzDisplacement();
			Double xRot = nodeResults.getxRotation();
			Double yRot = nodeResults.getyRotation();
			Double zRot = nodeResults.getzRotation();
			
			this.vectorValues.put(firstNode.getxCordinateNum(), xDisp);
			this.vectorValues.put(firstNode.getyCordinateNum(), yDisp);
			this.vectorValues.put(firstNode.getzCordinateNum(), zDisp);
			this.vectorValues.put(firstNode.getMxCordinateNum(), xRot);
			this.vectorValues.put(firstNode.getMyCordinateNum(), yRot);
			this.vectorValues.put(firstNode.getMzCordinateNum(), zRot);
			
			Node secondNode = oneDimFiniteElement.getSecondNode();
			
			this.coOrdinatesOfFreedom.add(secondNode.getxCordinateNum());
			this.coOrdinatesOfFreedom.add(secondNode.getyCordinateNum());
			this.coOrdinatesOfFreedom.add(secondNode.getzCordinateNum());
			this.coOrdinatesOfFreedom.add(secondNode.getMxCordinateNum());
			this.coOrdinatesOfFreedom.add(secondNode.getMyCordinateNum());
			this.coOrdinatesOfFreedom.add(secondNode.getMzCordinateNum());
			
			nodeResults=loadCase.getNodeResults().get(secondNode);
			
			xDisp = nodeResults.getxDisplacement();
			yDisp = nodeResults.getyDisplacement();
			zDisp = nodeResults.getzDisplacement();
			xRot = nodeResults.getxRotation();
			yRot = nodeResults.getyRotation();
			zRot = nodeResults.getzRotation();
			
			this.vectorValues.put(secondNode.getxCordinateNum(), xDisp);
			this.vectorValues.put(secondNode.getyCordinateNum(), yDisp);
			this.vectorValues.put(secondNode.getzCordinateNum(), zDisp);
			this.vectorValues.put(secondNode.getMxCordinateNum(), xRot);
			this.vectorValues.put(secondNode.getMyCordinateNum(), yRot);
			this.vectorValues.put(secondNode.getMzCordinateNum(), zRot);

			this.DOF = this.coOrdinatesOfFreedom.size();
			}

		private void processFor3DTrussElement(
				OneDimFiniteElement oneDimFiniteElement, LoadCase loadCase) {
				Node firstNode = oneDimFiniteElement.getFirstNode();
				
				this.coOrdinatesOfFreedom.add(firstNode.getxCordinateNum());
				this.coOrdinatesOfFreedom.add(firstNode.getyCordinateNum());
				this.coOrdinatesOfFreedom.add(firstNode.getzCordinateNum());
//				this.coOrdinatesOfFreedom.add(firstNode.getMxCordinateNum());
//				this.coOrdinatesOfFreedom.add(firstNode.getMyCordinateNum());
//				this.coOrdinatesOfFreedom.add(firstNode.getMzCordinateNum());
				
				NodeResults nodeResults = loadCase.getNodeResults().get(firstNode);
				
				Double xDisp = nodeResults.getxDisplacement();
				Double yDisp = nodeResults.getyDisplacement();
				Double zDisp = nodeResults.getzDisplacement();
//				Double xRot = nodeResults.getxRotation();
//				Double yRot = nodeResults.getyRotation();
//				Double zRot = nodeResults.getzRotation();
				
				this.vectorValues.put(firstNode.getxCordinateNum(), xDisp);
				this.vectorValues.put(firstNode.getyCordinateNum(), yDisp);
				this.vectorValues.put(firstNode.getzCordinateNum(), zDisp);
//				this.vectorValues.put(firstNode.getMxCordinateNum(), xRot);
//				this.vectorValues.put(firstNode.getMyCordinateNum(), yRot);
//				this.vectorValues.put(firstNode.getMzCordinateNum(), zRot);
				
				Node secondNode = oneDimFiniteElement.getSecondNode();
				
				this.coOrdinatesOfFreedom.add(secondNode.getxCordinateNum());
				this.coOrdinatesOfFreedom.add(secondNode.getyCordinateNum());
				this.coOrdinatesOfFreedom.add(secondNode.getzCordinateNum());
//				this.coOrdinatesOfFreedom.add(secondNode.getMxCordinateNum());
//				this.coOrdinatesOfFreedom.add(secondNode.getMyCordinateNum());
//				this.coOrdinatesOfFreedom.add(secondNode.getMzCordinateNum());
				
				nodeResults=loadCase.getNodeResults().get(secondNode);
				
				xDisp = nodeResults.getxDisplacement();
				yDisp = nodeResults.getyDisplacement();
				zDisp = nodeResults.getzDisplacement();
//				xRot = nodeResults.getxRotation();
//				yRot = nodeResults.getyRotation();
//				zRot = nodeResults.getzRotation();
				
				this.vectorValues.put(secondNode.getxCordinateNum(), xDisp);
				this.vectorValues.put(secondNode.getyCordinateNum(), yDisp);
				this.vectorValues.put(secondNode.getzCordinateNum(), zDisp);
//				this.vectorValues.put(secondNode.getMxCordinateNum(), xRot);
//				this.vectorValues.put(secondNode.getMyCordinateNum(), yRot);
//				this.vectorValues.put(secondNode.getMzCordinateNum(), zRot);

				this.DOF = this.coOrdinatesOfFreedom.size();
				}

		private void processFor2DBeamElement(
				OneDimFiniteElement oneDimFiniteElement, LoadCase loadCase) {

			//Set<CommonNode> nodes = oneDimFiniteElement.getNodes();

			/*for (CommonNode commonNode : nodes) {
				this.coOrdinatesOfFreedom.add(commonNode.getxCordinateNum());
				this.coOrdinatesOfFreedom.add(commonNode.getyCordinateNum());
				this.coOrdinatesOfFreedom.add(commonNode.getMzCordinateNum());
				
				NodeResults nodeResults = loadCase.getNodeResults().get(commonNode);
				
				Double xDisp = nodeResults.getxDisplacement();
				Double yDisp = nodeResults.getyDisplacement();
				Double zRot = nodeResults.getzRotation();
				
				this.vectorValues.put(commonNode.getxCordinateNum(), xDisp);
				this.vectorValues.put(commonNode.getyCordinateNum(), yDisp);
				this.vectorValues.put(commonNode.getMzCordinateNum(), zRot);
			}*/
			
			Node firstNode = oneDimFiniteElement.getFirstNode();
			
			this.coOrdinatesOfFreedom.add(firstNode.getxCordinateNum());
			this.coOrdinatesOfFreedom.add(firstNode.getyCordinateNum());
			this.coOrdinatesOfFreedom.add(firstNode.getMzCordinateNum());
			
			NodeResults nodeResults = loadCase.getNodeResults().get(firstNode);
			
			Double xDisp = nodeResults.getxDisplacement();
			Double yDisp = nodeResults.getyDisplacement();
			Double zRot = nodeResults.getzRotation();
			
			this.vectorValues.put(firstNode.getxCordinateNum(), xDisp);
			this.vectorValues.put(firstNode.getyCordinateNum(), yDisp);
			this.vectorValues.put(firstNode.getMzCordinateNum(), zRot);
			
			Node secondNode = oneDimFiniteElement.getSecondNode();
			
			this.coOrdinatesOfFreedom.add(secondNode.getxCordinateNum());
			this.coOrdinatesOfFreedom.add(secondNode.getyCordinateNum());
			this.coOrdinatesOfFreedom.add(secondNode.getMzCordinateNum());
			
			nodeResults=loadCase.getNodeResults().get(secondNode);
			
			xDisp = nodeResults.getxDisplacement();
			yDisp = nodeResults.getyDisplacement();
			zRot = nodeResults.getzRotation();
			
			this.vectorValues.put(secondNode.getxCordinateNum(), xDisp);
			this.vectorValues.put(secondNode.getyCordinateNum(), yDisp);
			this.vectorValues.put(secondNode.getMzCordinateNum(), zRot);

			this.DOF = this.coOrdinatesOfFreedom.size();
		}

		private void processFor2DTrussElement(
				OneDimFiniteElement oneDimFiniteElement, LoadCase loadCase) {

			/*Set<CommonNode> nodes = oneDimFiniteElement.getNodes();

			for (CommonNode commonNode : nodes) {
				this.coOrdinatesOfFreedom.add(commonNode.getxCordinateNum());
				this.coOrdinatesOfFreedom.add(commonNode.getyCordinateNum());
				
				NodeResults nodeResults = loadCase.getNodeResults().get(commonNode);
				
				Double xDisp = nodeResults.getxDisplacement();
				Double yDisp = nodeResults.getyDisplacement();
				
				this.vectorValues.put(commonNode.getxCordinateNum(), xDisp);
				this.vectorValues.put(commonNode.getyCordinateNum(), yDisp);
			}

			this.DOF = this.coOrdinatesOfFreedom.size();*/
			Node firstNode = oneDimFiniteElement.getFirstNode();
			
			this.coOrdinatesOfFreedom.add(firstNode.getxCordinateNum());
			this.coOrdinatesOfFreedom.add(firstNode.getyCordinateNum());
			
			NodeResults nodeResults = loadCase.getNodeResults().get(firstNode);
			
			Double xDisp = nodeResults.getxDisplacement();
			Double yDisp = nodeResults.getyDisplacement();
			
			this.vectorValues.put(firstNode.getxCordinateNum(), xDisp);
			this.vectorValues.put(firstNode.getyCordinateNum(), yDisp);
			
			Node secondNode = oneDimFiniteElement.getSecondNode();
			
			this.coOrdinatesOfFreedom.add(secondNode.getxCordinateNum());
			this.coOrdinatesOfFreedom.add(secondNode.getyCordinateNum());
			
			nodeResults=loadCase.getNodeResults().get(secondNode);
			
			xDisp = nodeResults.getxDisplacement();
			yDisp = nodeResults.getyDisplacement();
			
			this.vectorValues.put(secondNode.getxCordinateNum(), xDisp);
			this.vectorValues.put(secondNode.getyCordinateNum(), yDisp);

			this.DOF = this.coOrdinatesOfFreedom.size();
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
			return this.vectorValues;
		}

	}
}

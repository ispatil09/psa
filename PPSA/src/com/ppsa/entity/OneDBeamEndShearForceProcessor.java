package com.ppsa.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ppsa.exception.PPSAException;
import com.ppsa.exception.PPSAMathException;
import com.ppsa.math.MathHelper;
import com.ppsa.math.MathOperation;
import com.ppsa.math.impl.MathOperationJAMAImpl;
import com.ppsa.util.EntityTranslationUtil;
import com.psa.entity.FiniteElement;
import com.psa.entity.LoadCase;
import com.psa.entity.OneDAxialForceEntity;
import com.psa.entity.OneDBeamEndShearForceEntity;
import com.psa.entity.StiffnessMatrixEntity;
import com.psa.entity.VectorMatrix;
import com.psa.entity.enums.OneDimFiniteElementType;
import com.psa.entity.enums.StructureType;
import com.psa.entity.impl.Node;
import com.psa.entity.impl.OneDimFiniteElement;
import com.psa.entity.impl.Structure;

public class OneDBeamEndShearForceProcessor implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/*private int numOfElements = 0;
	private Map<Integer, Double> axialForceValues = new HashMap<Integer, Double>();*/
	private List<Integer> oneDFiniteElementNumbers = new ArrayList<Integer>();
	private List<OneDimFiniteElement> oneDFiniteElements = new ArrayList<OneDimFiniteElement>();

	public void processEndShearForceInMembers(LoadCase loadCase) throws PPSAException {
		Structure structure = loadCase.getParentStructure();
//		if(structure.getStructureType()==StructureType.SPACE)
//			return;
		Set<FiniteElement> finiteElements = structure.getFiniteElements();
		for (FiniteElement finiteElement : finiteElements) {
			findOneDFiniteElements(finiteElement);
		}
		// this.numOfElements = this.oneDFiniteElementNumbers.size();
		Map<OneDimFiniteElement, OneDAxialForceEntity> axialForceEntities = loadCase
				.getAxialForceEntities();
		Map<OneDimFiniteElement, OneDBeamEndShearForceEntity> beamEndShearForceEntities = loadCase.getBeamEndShearForceEntities();
		for (OneDimFiniteElement oneDFiniteElement : oneDFiniteElements) {
			VectorMatrix nodesAssociatedDispVector = axialForceEntities
					.get(oneDFiniteElement).getNodesAssociatedDispVector();
			OneDBeamEndShearForceEntity oneDBeamEndShearForceEntity = beamEndShearForceEntities.get(oneDFiniteElement);
			
			try {
				processThisOneDFiniteElement(oneDFiniteElement,
						nodesAssociatedDispVector,oneDBeamEndShearForceEntity ,loadCase);
			} catch (PPSAMathException e) {
				throw new PPSAException(e.getMessage());
			}
		}
	}

	private void processThisOneDFiniteElement(
			OneDimFiniteElement oneDimFiniteElement,
			VectorMatrix nodeAssociatedDispMatrix, OneDBeamEndShearForceEntity oneDBeamEndShearForceEntity,LoadCase loadCase) throws PPSAMathException {

		/*setMemberCSValueVector(oneDimFiniteElement, oneDAxialForceEntity);
		setMemberAssociatedNodeDispValues(oneDimFiniteElement,
				oneDAxialForceEntity,loadCase);*/
		
		Node firstNode = oneDimFiniteElement.getFirstNode();
		Node secondNode = oneDimFiniteElement.getSecondNode();
		
		/*VectorMatrix csValueVector = oneDAxialForceEntity.getCSValueVector();
		double[][] csMatric = EntityTranslationUtil.getDoubleArrayFromMatrix(csValueVector);*/
		MathOperation mathOperation = new MathOperationJAMAImpl();

		StiffnessMatrixEntity stiffnessMatrixEntity = oneDimFiniteElement.getStiffnessMatrixEntity();
		double angleOfInclination = oneDimFiniteElement.getAngleOfInclination();
		

		int indexInMatrixLocalX = getIndexToRetreiveFromLocalX(stiffnessMatrixEntity,firstNode);
		
		double[][] valuesOfSpecifiedColumn = mathOperation.getValuesOfSpecifiedColumn(stiffnessMatrixEntity.getStiffnessMatrix(),indexInMatrixLocalX);

		double[][] doubleArrayFromMatrix = EntityTranslationUtil.getDoubleArrayFromMatrix(nodeAssociatedDispMatrix);
		
		double firstNodeSFInLocalX = mathOperation.getAxialMemberForce(doubleArrayFromMatrix,valuesOfSpecifiedColumn);
		double resolveForceFromLocalX = MathHelper.resolveForceInX(firstNodeSFInLocalX, angleOfInclination);
		
		int indexInMatrixLocalY = getIndexToRetreiveFromLocalY(stiffnessMatrixEntity,firstNode);
		
		valuesOfSpecifiedColumn = mathOperation.getValuesOfSpecifiedColumn(stiffnessMatrixEntity.getStiffnessMatrix(),indexInMatrixLocalY);
		
		//doubleArrayFromMatrix = EntityTranslationUtil.getDoubleArrayFromMatrix(nodeAssociatedDispMatrix);
		
		double firstNodeSFInLocalY = mathOperation.getAxialMemberForce(doubleArrayFromMatrix,valuesOfSpecifiedColumn);
		double resolveForceFromLocalY = MathHelper.resolveForceInY(firstNodeSFInLocalY, angleOfInclination);

		double firstNodeFinalSF=resolveForceFromLocalY-resolveForceFromLocalX;
		
		oneDBeamEndShearForceEntity.addShearForce_FY(firstNode, firstNodeFinalSF);
		/*System.out.println("Beam : " + oneDimFiniteElement + " , Node(1) : "
				+ firstNode + ", SF : " + firstNodeFinalSF);*/
		
		/**
		 * Copy for secondNode.
		 */
		
		indexInMatrixLocalX = getIndexToRetreiveFromLocalX(stiffnessMatrixEntity,secondNode);
		
		valuesOfSpecifiedColumn = mathOperation.getValuesOfSpecifiedColumn(stiffnessMatrixEntity.getStiffnessMatrix(),indexInMatrixLocalX);
		
		//double[][] doubleArrayFromMatrix = EntityTranslationUtil.getDoubleArrayFromMatrix(nodeAssociatedDispMatrix);
		
		double secondNodeSFInLocalX = mathOperation.getAxialMemberForce(doubleArrayFromMatrix,valuesOfSpecifiedColumn);
		resolveForceFromLocalX = MathHelper.resolveForceInX(secondNodeSFInLocalX, angleOfInclination);
		
		indexInMatrixLocalY = getIndexToRetreiveFromLocalY(stiffnessMatrixEntity,secondNode);
		
		valuesOfSpecifiedColumn = mathOperation.getValuesOfSpecifiedColumn(stiffnessMatrixEntity.getStiffnessMatrix(),indexInMatrixLocalY);
		
		//doubleArrayFromMatrix = EntityTranslationUtil.getDoubleArrayFromMatrix(nodeAssociatedDispMatrix);
		
		double secondNodeSFInLocalY = mathOperation.getAxialMemberForce(doubleArrayFromMatrix,valuesOfSpecifiedColumn);
		resolveForceFromLocalY = MathHelper.resolveForceInY(secondNodeSFInLocalY, angleOfInclination);
		
		double secondNodeFinalSF=resolveForceFromLocalY-resolveForceFromLocalX;
		
		oneDBeamEndShearForceEntity.addShearForce_FY(secondNode, secondNodeFinalSF);
		/*System.out.println("Beam : " + oneDimFiniteElement + " , Node(2) : "
				+ secondNode + ", SF : " + secondNodeFinalSF);*/
	}

	private int getIndexToRetreiveFromLocalY(
			StiffnessMatrixEntity stiffnessMatrixEntity, Node node) {
		int yCordinateNum = node.getyCordinateNum();
		int index = stiffnessMatrixEntity.getCoOrinatesOfFreedom().indexOf(yCordinateNum);
		return index;
	}

	private int getIndexToRetreiveFromLocalX(
			StiffnessMatrixEntity stiffnessMatrixEntity, Node node) {
		int xCordinateNum = node.getxCordinateNum();
		int index = stiffnessMatrixEntity.getCoOrinatesOfFreedom().indexOf(xCordinateNum);
		return index;
	}

	private double getAEbyL(OneDimFiniteElement oneDimFiniteElement) {
		double csArea = oneDimFiniteElement.getSectionProperty().getCSArea();
		double eVal = oneDimFiniteElement.getMaterialProperty().getE();
		double lengthOfFiniteElement = oneDimFiniteElement.getLengthOfElement();
		return csArea*eVal/lengthOfFiniteElement;
	}

/*	private void setMemberAssociatedNodeDispValues(
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
	}*/

	private void findOneDFiniteElements(FiniteElement finiteElement) {
		if (finiteElement instanceof OneDimFiniteElement) {
			OneDimFiniteElement oneDimFiniteElement = (OneDimFiniteElement) finiteElement;
			if (oneDimFiniteElement.getFiniteElementType() == OneDimFiniteElementType.BEAM2D
					|| oneDimFiniteElement.getFiniteElementType() == OneDimFiniteElementType.BEAM3D) {
				oneDFiniteElementNumbers.add(oneDimFiniteElement
						.getElementNumber());
				oneDFiniteElements.add(oneDimFiniteElement);
			}
		}
	}
}

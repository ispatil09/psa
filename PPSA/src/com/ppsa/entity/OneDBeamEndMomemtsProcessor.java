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
import com.ppsa.util.OpenPSA;
import com.psa.entity.FiniteElement;
import com.psa.entity.LoadCase;
import com.psa.entity.OneDAxialForceEntity;
import com.psa.entity.OneDBeamEndMomentsEntity;
import com.psa.entity.OneDBeamEndShearForceEntity;
import com.psa.entity.OneDBeamEndTorsionEntity;
import com.psa.entity.OneDBeamLocalRotations;
import com.psa.entity.StiffnessMatrixEntity;
import com.psa.entity.VectorMatrix;
import com.psa.entity.enums.OneDimFiniteElementType;
import com.psa.entity.impl.Node;
import com.psa.entity.impl.OneDimFiniteElement;
import com.psa.entity.impl.Structure;
import com.psa.exception.PSAStructureInstantiationException;

public class OneDBeamEndMomemtsProcessor implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/*private int numOfElements = 0;
	private Map<Integer, Double> axialForceValues = new HashMap<Integer, Double>();*/
	private List<Integer> oneDFiniteElementNumbers = new ArrayList<Integer>();
	private List<OneDimFiniteElement> oneDFiniteElements = new ArrayList<OneDimFiniteElement>();
    
	public void processEndMomentsInMembers(LoadCase loadCase) throws PPSAException {
		Structure structure = loadCase.getParentStructure();
		Set<FiniteElement> finiteElements = structure.getFiniteElements();
		for (FiniteElement finiteElement : finiteElements) {
			findOneDFiniteElements(finiteElement);
		}
		// this.numOfElements = this.oneDFiniteElementNumbers.size();
		Map<OneDimFiniteElement, OneDAxialForceEntity> axialForceEntities = loadCase
				.getAxialForceEntities();
		Map<OneDimFiniteElement, OneDBeamEndMomentsEntity> beamEndMomentEntities = loadCase.getBeamEndMomentEntities();
		
		/*List<OneDimFiniteElementChild> tempList = new ArrayList<>();
		tempList.addAll(oneDFiniteElements);
		Collections.sort(tempList);*/
		//oneDFiniteElements.
		for (OneDimFiniteElement oneDFiniteElement : this.oneDFiniteElements) {
			VectorMatrix nodesAssociatedDispVector = axialForceEntities
					.get(oneDFiniteElement).getNodesAssociatedDispVector();
			OneDBeamEndMomentsEntity oneDBeamEndMomentsEntity = beamEndMomentEntities.get(oneDFiniteElement);
			
			try {
				//if(oneDFiniteElement.getFiniteElementType()==OneDimFiniteElementType.BEAM2D) { 
					// No need 
					processThisOneDFiniteElement(oneDFiniteElement,
							nodesAssociatedDispVector,oneDBeamEndMomentsEntity,loadCase);
				//}
			} catch (PPSAMathException e) {
				throw new PPSAException(e.getMessage());
			}
		}
	}

	private void processThisOneDFiniteElement(
			OneDimFiniteElement oneDimFiniteElement,
			VectorMatrix nodeAssociatedDispMatrix, OneDBeamEndMomentsEntity oneDBeamEndMomentsEntity, LoadCase loadCase) throws PPSAMathException {
		OneDimFiniteElementType finiteElementType = oneDimFiniteElement.getFiniteElementType();
		/*setMemberCSValueVector(oneDimFiniteElement, oneDAxialForceEntity);
		setMemberAssociatedNodeDispValues(oneDimFiniteElement,
				oneDAxialForceEntity,loadCase);*/

		Node firstNode = oneDimFiniteElement.getFirstNode();
		Node secondNode = oneDimFiniteElement.getSecondNode();
		MathOperation mathOperation = new MathOperationJAMAImpl();
		StiffnessMatrixEntity stiffnessMatrixEntity = oneDimFiniteElement.getStiffnessMatrixEntity();
		
		// firstNode _MZ
		int indexInMatrix = getIndexToRetreiveFrom(stiffnessMatrixEntity,firstNode.getMzCordinateNum());		
		Double[][] stiffnessMatrix = stiffnessMatrixEntity.getStiffnessMatrix();
		double[][] valuesOfSpecifiedColumn = mathOperation.getValuesOfSpecifiedColumn(stiffnessMatrix,indexInMatrix);
		double[][] doubleArrayFromMatrix = EntityTranslationUtil.getDoubleArrayFromMatrix(nodeAssociatedDispMatrix);
		double firstNodeMoment_MZ = mathOperation.getAxialMemberForce(doubleArrayFromMatrix,valuesOfSpecifiedColumn);
		
		// secondNode _MZ
		indexInMatrix = getIndexToRetreiveFrom(stiffnessMatrixEntity,secondNode.getMzCordinateNum());
		valuesOfSpecifiedColumn = mathOperation.getValuesOfSpecifiedColumn(stiffnessMatrix,indexInMatrix);
		doubleArrayFromMatrix = EntityTranslationUtil.getDoubleArrayFromMatrix(nodeAssociatedDispMatrix);
		double secondNodeMoment_MZ = mathOperation.getAxialMemberForce(doubleArrayFromMatrix,valuesOfSpecifiedColumn);
		

		if(finiteElementType==OneDimFiniteElementType.BEAM3D) {
			// firstNode _MX
			indexInMatrix = getIndexToRetreiveFrom(stiffnessMatrixEntity,firstNode.getMxCordinateNum());		
			valuesOfSpecifiedColumn = mathOperation.getValuesOfSpecifiedColumn(stiffnessMatrix,indexInMatrix);
			doubleArrayFromMatrix = EntityTranslationUtil.getDoubleArrayFromMatrix(nodeAssociatedDispMatrix);
			double firstNodeMoment_MX = mathOperation.getAxialMemberForce(doubleArrayFromMatrix,valuesOfSpecifiedColumn);
			// firstNode _MY
			indexInMatrix = getIndexToRetreiveFrom(stiffnessMatrixEntity,firstNode.getMyCordinateNum());		
			valuesOfSpecifiedColumn = mathOperation.getValuesOfSpecifiedColumn(stiffnessMatrix,indexInMatrix);
			doubleArrayFromMatrix = EntityTranslationUtil.getDoubleArrayFromMatrix(nodeAssociatedDispMatrix);
			double firstNodeMoment_MY = mathOperation.getAxialMemberForce(doubleArrayFromMatrix,valuesOfSpecifiedColumn);

			// firstNode _MX
			indexInMatrix = getIndexToRetreiveFrom(stiffnessMatrixEntity,firstNode.getxCordinateNum());		
			valuesOfSpecifiedColumn = mathOperation.getValuesOfSpecifiedColumn(stiffnessMatrix,indexInMatrix);
			doubleArrayFromMatrix = EntityTranslationUtil.getDoubleArrayFromMatrix(nodeAssociatedDispMatrix);
			double firstNodeForce_FX = mathOperation.getAxialMemberForce(doubleArrayFromMatrix,valuesOfSpecifiedColumn);
			// firstNode _MY
			indexInMatrix = getIndexToRetreiveFrom(stiffnessMatrixEntity,firstNode.getyCordinateNum());		
			valuesOfSpecifiedColumn = mathOperation.getValuesOfSpecifiedColumn(stiffnessMatrix,indexInMatrix);
			doubleArrayFromMatrix = EntityTranslationUtil.getDoubleArrayFromMatrix(nodeAssociatedDispMatrix);
			double firstNodeForce_FY = mathOperation.getAxialMemberForce(doubleArrayFromMatrix,valuesOfSpecifiedColumn);			
			// firstNode _MY
			indexInMatrix = getIndexToRetreiveFrom(stiffnessMatrixEntity,firstNode.getzCordinateNum());		
			valuesOfSpecifiedColumn = mathOperation.getValuesOfSpecifiedColumn(stiffnessMatrix,indexInMatrix);
			doubleArrayFromMatrix = EntityTranslationUtil.getDoubleArrayFromMatrix(nodeAssociatedDispMatrix);
			double firstNodeForce_FZ = mathOperation.getAxialMemberForce(doubleArrayFromMatrix,valuesOfSpecifiedColumn);			
			// firstNode _Torsion
			indexInMatrix = getIndexToRetreiveFrom(stiffnessMatrixEntity,firstNode.getMxCordinateNum());		
			valuesOfSpecifiedColumn = mathOperation.getValuesOfSpecifiedColumn(stiffnessMatrix,indexInMatrix);
			doubleArrayFromMatrix = EntityTranslationUtil.getDoubleArrayFromMatrix(nodeAssociatedDispMatrix);
			double firstNodeTorsion = mathOperation.getAxialMemberForce(doubleArrayFromMatrix,valuesOfSpecifiedColumn);			
			
			// secondNode _MX
			indexInMatrix = getIndexToRetreiveFrom(stiffnessMatrixEntity,secondNode.getMxCordinateNum());
			valuesOfSpecifiedColumn = mathOperation.getValuesOfSpecifiedColumn(stiffnessMatrix,indexInMatrix);
			doubleArrayFromMatrix = EntityTranslationUtil.getDoubleArrayFromMatrix(nodeAssociatedDispMatrix);
			double secondNodeMoment_MX = mathOperation.getAxialMemberForce(doubleArrayFromMatrix,valuesOfSpecifiedColumn);
			// secondNode _MY
			indexInMatrix = getIndexToRetreiveFrom(stiffnessMatrixEntity,secondNode.getMyCordinateNum());
			valuesOfSpecifiedColumn = mathOperation.getValuesOfSpecifiedColumn(stiffnessMatrix,indexInMatrix);
			doubleArrayFromMatrix = EntityTranslationUtil.getDoubleArrayFromMatrix(nodeAssociatedDispMatrix);
			double secondNodeMoment_MY = mathOperation.getAxialMemberForce(doubleArrayFromMatrix,valuesOfSpecifiedColumn);
			
			// secondNode _MX
			indexInMatrix = getIndexToRetreiveFrom(stiffnessMatrixEntity,secondNode.getxCordinateNum());
			valuesOfSpecifiedColumn = mathOperation.getValuesOfSpecifiedColumn(stiffnessMatrix,indexInMatrix);
			doubleArrayFromMatrix = EntityTranslationUtil.getDoubleArrayFromMatrix(nodeAssociatedDispMatrix);
			double secondNodeForce_FX = mathOperation.getAxialMemberForce(doubleArrayFromMatrix,valuesOfSpecifiedColumn);
			// secondNode _MY
			indexInMatrix = getIndexToRetreiveFrom(stiffnessMatrixEntity,secondNode.getyCordinateNum());
			valuesOfSpecifiedColumn = mathOperation.getValuesOfSpecifiedColumn(stiffnessMatrix,indexInMatrix);
			doubleArrayFromMatrix = EntityTranslationUtil.getDoubleArrayFromMatrix(nodeAssociatedDispMatrix);
			double secondNodeForce_FY = mathOperation.getAxialMemberForce(doubleArrayFromMatrix,valuesOfSpecifiedColumn);
			// secondNode _MY
			indexInMatrix = getIndexToRetreiveFrom(stiffnessMatrixEntity,secondNode.getzCordinateNum());
			valuesOfSpecifiedColumn = mathOperation.getValuesOfSpecifiedColumn(stiffnessMatrix,indexInMatrix);
			doubleArrayFromMatrix = EntityTranslationUtil.getDoubleArrayFromMatrix(nodeAssociatedDispMatrix);
			double secondNodeForce_FZ = mathOperation.getAxialMemberForce(doubleArrayFromMatrix,valuesOfSpecifiedColumn);
			// secondNode _MY
			indexInMatrix = getIndexToRetreiveFrom(stiffnessMatrixEntity,secondNode.getMxCordinateNum());
			valuesOfSpecifiedColumn = mathOperation.getValuesOfSpecifiedColumn(stiffnessMatrix,indexInMatrix);
			doubleArrayFromMatrix = EntityTranslationUtil.getDoubleArrayFromMatrix(nodeAssociatedDispMatrix);
			double secondNodeTorsion = mathOperation.getAxialMemberForce(doubleArrayFromMatrix,valuesOfSpecifiedColumn);

			/**
			 * Resolving the moments to local co_Ordinates
			 */
			double[] globalForceVals = new double[12];
			globalForceVals[0] = firstNodeForce_FX;
			globalForceVals[1] = firstNodeForce_FY;
			globalForceVals[2] = firstNodeForce_FZ;
			globalForceVals[3] = firstNodeMoment_MX;
			globalForceVals[4] = firstNodeMoment_MY;
			globalForceVals[5] = firstNodeMoment_MZ;
			globalForceVals[6] = secondNodeForce_FX;
			globalForceVals[7] = secondNodeForce_FY;
			globalForceVals[8] = secondNodeForce_FZ;
			globalForceVals[9] = secondNodeMoment_MX;
			globalForceVals[10] = secondNodeMoment_MY;
			globalForceVals[11] = secondNodeMoment_MZ;
			double[] resolveForcesToLocal = MathHelper.resolveForcesToLocal(
					globalForceVals, oneDimFiniteElement);
			firstNodeMoment_MZ = resolveForcesToLocal[5];
			secondNodeMoment_MZ = resolveForcesToLocal[11];
			firstNodeMoment_MY = resolveForcesToLocal[4];
			secondNodeMoment_MY = resolveForcesToLocal[10];
			oneDBeamEndMomentsEntity.addMoment_MY(firstNode, firstNodeMoment_MY);
			oneDBeamEndMomentsEntity.addMoment_MY(secondNode, secondNodeMoment_MY);
			
			Map<OneDimFiniteElement, OneDBeamEndShearForceEntity> beamEndShearEntities = loadCase.getBeamEndShearForceEntities();
			Map<OneDimFiniteElement, OneDBeamEndTorsionEntity> beamEndTosionEntities = loadCase.getBeamEndTorsionEntities();
			OneDBeamEndShearForceEntity oneDBeamEndShearEntity = beamEndShearEntities.get(oneDimFiniteElement);
			OneDBeamEndTorsionEntity oneDBeamEndTorsionEntity = beamEndTosionEntities.get(oneDimFiniteElement);
			double SF1_Y = resolveForcesToLocal[1];
			double SF1_Z = resolveForcesToLocal[2];
			double SF2_Y = resolveForcesToLocal[7];
			double SF2_Z = resolveForcesToLocal[8];
			oneDBeamEndShearEntity.addShearForce_FY(firstNode, SF1_Y);
			oneDBeamEndShearEntity.addShearForce_FZ(firstNode, SF1_Z);
			oneDBeamEndShearEntity.addShearForce_FY(secondNode, SF2_Y);
			oneDBeamEndShearEntity.addShearForce_FZ(secondNode, SF2_Z);

			firstNodeTorsion = resolveForcesToLocal[3];
			secondNodeTorsion = resolveForcesToLocal[9];
			oneDBeamEndTorsionEntity.addTorsion_MX(firstNode, firstNodeTorsion);
			oneDBeamEndTorsionEntity.addTorsion_MX(secondNode, secondNodeTorsion);
			
			// ---------------------------------
			//-- Getting Local Node Rotations --
			// ---------------------------------
			double[] nodeDisplacementsNodeOne = null;
			double[] nodeDisplacementsNodeTwo = null;
			try {
				nodeDisplacementsNodeOne = OpenPSA.getNodeDisplacements(loadCase.getParentStructure(), loadCase.getLoadCaseNum(), firstNode.getNodeNumber());
				nodeDisplacementsNodeTwo = OpenPSA.getNodeDisplacements(loadCase.getParentStructure(), loadCase.getLoadCaseNum(), secondNode.getNodeNumber());
			} catch (PSAStructureInstantiationException e) {
				e.printStackTrace();
			}
			double[] globalRotations = new double[12];
			globalRotations[3] = nodeDisplacementsNodeOne[3];
			globalRotations[4] = nodeDisplacementsNodeOne[4];
			globalRotations[5] = nodeDisplacementsNodeOne[5];
			globalRotations[9] = nodeDisplacementsNodeTwo[3];
			globalRotations[10] = nodeDisplacementsNodeTwo[4];
			globalRotations[11] = nodeDisplacementsNodeTwo[5];
			double[] localRotations = MathHelper.resolveForcesToLocal(globalRotations, oneDimFiniteElement);
			
			double[] localRotations_NodeOne = new double[3];
			double[] localRotations_NodeTwo = new double[3];
			
			localRotations_NodeOne[0] = localRotations[3];
			localRotations_NodeOne[1] = localRotations[4];
			localRotations_NodeOne[2] = localRotations[5];
			localRotations_NodeTwo[0] = localRotations[9];
			localRotations_NodeTwo[1] = localRotations[10];
			localRotations_NodeTwo[2] = localRotations[11];
			
			Map<OneDimFiniteElement, OneDBeamLocalRotations> beamEndLocalRotations = loadCase.getBeamEndLocalRotations();
			OneDBeamLocalRotations oneDBeamLocalRotations = beamEndLocalRotations.get(oneDimFiniteElement);
			oneDBeamLocalRotations.setLocalRotations_NodeOne(localRotations_NodeOne);
			oneDBeamLocalRotations.setLocalRotations_NodeTwo(localRotations_NodeTwo);
		}
		oneDBeamEndMomentsEntity.addMoment_MZ(firstNode, firstNodeMoment_MZ);
		oneDBeamEndMomentsEntity.addMoment_MZ(secondNode, secondNodeMoment_MZ);
		
	}

	private int getIndexToRetreiveFrom(
			StiffnessMatrixEntity stiffnessMatrixEntity, int cordinateNum) {
		//int mzCordinateNum = node.getMzCordinateNum();
		int index = stiffnessMatrixEntity.getCoOrinatesOfFreedom().indexOf(cordinateNum);
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

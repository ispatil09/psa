package com.ppsa.util;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ppsa.entity.DisplacementVector;
import com.ppsa.entity.ForceVector;
import com.ppsa.entity.FullNodalDisplacementVector;
import com.ppsa.entity.NodalDisplacementResultVector;
import com.ppsa.entity.NodeReactionVector;
import com.ppsa.entity.OneDAxialForceProcessor;
import com.ppsa.entity.OneDBeamEndMomemtsProcessor;
import com.ppsa.entity.OneDBeamEndShearForceProcessor;
import com.ppsa.entity.ReducedDisplacementVector;
import com.ppsa.entity.StiffnessMatrixBeam2DImpl;
import com.ppsa.entity.StiffnessMatrixBeam3DImpl;
import com.ppsa.entity.StiffnessMatrixTruss2DImpl;
import com.ppsa.entity.StiffnessMatrixTruss3DImpl;
import com.ppsa.exception.PPSAException;
import com.ppsa.exception.PPSAMathException;
import com.ppsa.exception.PPSAModellingFaultException;
import com.psa.entity.CommonNode;
import com.psa.entity.FiniteElement;
import com.psa.entity.GlobalStiffnessMatrixEntity;
import com.psa.entity.LoadCase;
import com.psa.entity.StiffnessMatrixEntity;
import com.psa.entity.VectorMatrix;
import com.psa.entity.enums.OneDimFiniteElementType;
import com.psa.entity.enums.StructureType;
import com.psa.entity.impl.Node;
import com.psa.entity.impl.NodeResults;
import com.psa.entity.impl.OneDimFiniteElement;
import com.psa.entity.impl.Structure;
import com.psa.exception.PSAException;

public class MatrixGenerationUtil {
	public static void setStiffnessMatrixToAllElements(Structure structure)
			throws PSAException {

		Set<FiniteElement> finiteElements = structure.getFiniteElements();
			for (FiniteElement finiteElement : finiteElements) {

				OneDimFiniteElement oneDimFiniteElement = (OneDimFiniteElement) finiteElement;
				if (oneDimFiniteElement.getFiniteElementType() == OneDimFiniteElementType.TRUSS2D) {
					StiffnessMatrixEntity matrixEntity = new StiffnessMatrixTruss2DImpl();
					matrixEntity.setStiffnessMatrixForElement(oneDimFiniteElement);
				} else if (oneDimFiniteElement.getFiniteElementType() == OneDimFiniteElementType.BEAM2D) {
					StiffnessMatrixEntity matrixEntity = new StiffnessMatrixBeam2DImpl();
					matrixEntity.setStiffnessMatrixForElement(oneDimFiniteElement);
				} else if (oneDimFiniteElement.getFiniteElementType() == OneDimFiniteElementType.TRUSS3D) {
					StiffnessMatrixEntity matrixEntity = new StiffnessMatrixTruss3DImpl();
					matrixEntity.setStiffnessMatrixForElement(oneDimFiniteElement);
				} else if (oneDimFiniteElement.getFiniteElementType() == OneDimFiniteElementType.BEAM3D) {
					StiffnessMatrixEntity matrixEntity = new StiffnessMatrixBeam3DImpl();
					matrixEntity.setStiffnessMatrixForElement(oneDimFiniteElement);
				}
			}
	}

	/*public void setGlobalStiffnesMatrix(LoadCase loadCase)
			throws PSAException {
		GlobalStiffnessMatrixEntity matrixEntity = new StiffnessMatrixGlobalImpl();
		matrixEntity.setGlobalStiffnessMatrix(loadCase);
	}*/


	public static void generateDisplacementVecor(Structure structure) {
		new DisplacementVector().setDisplacementVector(structure);
	}

	public static void generateGlobalForceVectorsForDifferentLoadCases(
			Structure structure) throws PPSAModellingFaultException {
			//new ForceVector().setForceVector(loadCase);
			
			Set<Integer> loadCasesKeySet = structure.getLoadCases().keySet();

			for (Integer loadCaseNum : loadCasesKeySet) {
				LoadCase loadCase = structure.getLoadCase(loadCaseNum);
				new ForceVector().setForceVector(loadCase);
			}
	}

	public static void reducedGlobalStiffnessMatrix(Structure structure)
			throws PSAException {
		new ReducedDisplacementVector()
				.setReducedDisplacementVectorAndOtherReductions(structure);
	}

	public static void performNodalDisplacementCalculation(Structure structure)
			throws PPSAMathException {

			/*new NodalDisplacementResultVector().setNodalDisplacementVector(
					loadCase);*/
			Set<Integer> loadCasesKeySet = structure.getLoadCases().keySet();

			for (Integer loadCaseNum : loadCasesKeySet) {
				LoadCase loadCase = structure.getLoadCase(loadCaseNum);
				new NodalDisplacementResultVector().setNodalDisplacementVector(
						loadCase);
			}
	}

	public static void setNodalDisplacementToEntities(Structure structure)
			throws PPSAMathException {

			/*new FullNodalDisplacementVector()
					.setFullNodalDisplacementVectorToLoadCase(loadCase);
			setNodeResultValues(loadCase);*/
		Set<Integer> loadCasesKeySet = structure.getLoadCases().keySet();

		for (Integer loadCaseNum : loadCasesKeySet) {
			LoadCase loadCase = structure.getLoadCase(loadCaseNum);
			/*new FullNodalDisplacementVector()
					.setFullNodalDisplacementVectorToLoadCase(loadCase);*/
			setNodeResultValues(loadCase);
		}
	}

	private static void setNodeResultValues(LoadCase loadCase) {
		VectorMatrix fullNodalDisplacementVector = loadCase
				.getFullNodalDisplacementVector();
		Map<Integer, Double> fullVectorValues = fullNodalDisplacementVector
				.getVectorValues();
		Set<Node> nodes = loadCase.getParentStructure().getNodes();
		Map<CommonNode, NodeResults> nodeResults = loadCase.getNodeResults();

		for (CommonNode node : nodes) {
			int xCorNum = node.getxCordinateNum();
			int yCorNum = node.getyCordinateNum();
			int mzCorNum = node.getMzCordinateNum();
			int mxCorNum = node.getMxCordinateNum();
			int myCorNum = node.getMyCordinateNum();
			int zCordNum = node.getzCordinateNum();

			Double xDisp = fullVectorValues.get(xCorNum);
			Double yDisp = fullVectorValues.get(yCorNum);
			Double mzRotation = fullVectorValues.get(mzCorNum);
			Double mxRotation = fullVectorValues.get(mxCorNum);
			Double myRotation = fullVectorValues.get(myCorNum);
			Double zDisp = fullVectorValues.get(zCordNum);
			

			NodeResults nodeResult = nodeResults.get(node);
			nodeResult.setxDisplacement(xDisp);
			nodeResult.setyDisplacement(yDisp);
			/**
			 * To check zDisp is not null.
			 * Otherwise later gives nullPointerException.
			 * It gives null when 2dTruss.
			 */
			if (zDisp != null)
				nodeResult.setzDisplacement(zDisp);
			if(mzRotation!=null)
				nodeResult.setzRotation(mzRotation);
			if(mxRotation!=null)
				nodeResult.setxRotation(mxRotation);
			if(myRotation!=null)
				nodeResult.setyRotation(myRotation);
		}
	}

	public static void computeNodeReactionsAtSupport(Structure structure)
			throws PPSAException {

		/*System.out.println("For node reactions");
			new NodeReactionVector().setNodeReactions(loadCase);*/

		Set<Integer> loadCasesKeySet = structure.getLoadCases().keySet();

		//System.out.println("For node reactions");
		for (Integer loadCaseNum : loadCasesKeySet) {
			LoadCase loadCase = structure.getLoadCase(loadCaseNum);
			new NodeReactionVector().setNodeReactions(loadCase);
		}
	}

	public static void computeSectionForcesOnMembers(Structure structure)
			throws PPSAException {

		Map<Integer, LoadCase> loadCases = structure.getLoadCases();
		Set<Integer> keySet = loadCases.keySet();
		for (Integer loadCaseId : keySet) {
			LoadCase loadCase = loadCases.get(loadCaseId);
			//System.out.println("For Axial Force..");
			new OneDAxialForceProcessor().processAxialForceInMembers(loadCase);
			
			new OneDBeamEndMomemtsProcessor().processEndMomentsInMembers(loadCase);
			//System.out.println("/n For Shear Force /n");
			if(structure.getStructureType()!=StructureType.SPACE) // Otherwise it is taken care in MomentProcessor
				new OneDBeamEndShearForceProcessor().processEndShearForceInMembers(loadCase);
		}
	}

	/**
	 * Some entities like loadCase.getNodeResults() are to be filled with
	 * appropriate data. The result data is usually found in vector matrix,
	 * which need to be populated in proper entities.
	 * 
	 * @param structureEntity
	 */
	public static void fillBasicDataInObjects(Structure structure) {
		Map<Integer, LoadCase> loadCases = structure.getLoadCases();
		Set<Integer> keySet = loadCases.keySet();
		for (Integer loadCaseId : keySet) {
			LoadCase loadCase = loadCases.get(loadCaseId);
			setNodeResults(loadCase);
		}
	}

	private static void setNodeResults(LoadCase loadCase) {
		VectorMatrix supportReactionsVector = loadCase
				.getSupportReactionsVector();
		Map<CommonNode, NodeResults> nodeResults = loadCase.getNodeResults();

		List<Integer> coOrdinatedOfFreedom = supportReactionsVector
				.getCoOrdinatedOfFreedom();
		Map<Integer, Double> vectorValues = supportReactionsVector
				.getVectorValues();

		Set<Node> nodes = loadCase.getParentStructure().getNodes();

		/**
		 * later :
		 * coOrdOfFreedom loop can be removed for efficiency.
		 */
		for (Integer coOrdOfFreedom : coOrdinatedOfFreedom) {
			for (CommonNode node : nodes) {

				//if (node.isSupport()) {
					NodeResults nodeResult = nodeResults.get(node);
					if (node.getxCordinateNum() == coOrdOfFreedom) {
						Double forceVal = vectorValues.get(node
								.getxCordinateNum());
						nodeResult.setxReaction(forceVal);
					} else if (node.getyCordinateNum() == coOrdOfFreedom) {
						Double forceVal = vectorValues.get(node
								.getyCordinateNum());
						nodeResult.setyReaction(forceVal);
					} else if (node.getMzCordinateNum() == coOrdOfFreedom) {
						Double momentVal = vectorValues.get(node
								.getMzCordinateNum());
						nodeResult.setzMoment(momentVal);
					} else if (node.getzCordinateNum() == coOrdOfFreedom) {
						Double forceVal = vectorValues.get(node
								.getzCordinateNum());
						nodeResult.setzReaction(forceVal);
					} else if (node.getMxCordinateNum() == coOrdOfFreedom) {
						Double momentVal = vectorValues.get(node
								.getMxCordinateNum());
						nodeResult.setxMoment(momentVal);
					} else if (node.getMyCordinateNum() == coOrdOfFreedom) {
						Double momentVal = vectorValues.get(node
								.getMyCordinateNum());
						nodeResult.setyMoment(momentVal);
					}
			}
		}
	}

}

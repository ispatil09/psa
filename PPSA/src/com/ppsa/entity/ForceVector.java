package com.ppsa.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.ppsa.exception.PPSAModellingFaultException;
import com.ppsa.math.MathHelper;
import com.ppsa.math.impl.MathOperationJAMAImpl;
import com.ppsa.util.FixEndMomentUtil;
import com.psa.entity.CommonNode;
import com.psa.entity.GlobalStiffnessMatrixEntity;
import com.psa.entity.LoadCase;
import com.psa.entity.LoadCase.MemberLoad;
import com.psa.entity.LoadCase.MemberLoad.ConcentratedForce;
import com.psa.entity.LoadCase.MemberLoad.UniformForce;
import com.psa.entity.LoadCase.NodalLoad;
import com.psa.entity.StiffnessMatrixEntity;
import com.psa.entity.VectorMatrix;
import com.psa.entity.enums.ConMemberLoadDirection;
import com.psa.entity.enums.MemberLoadType;
import com.psa.entity.enums.StructureType;
import com.psa.entity.enums.UniMemberLoadDirection;
import com.psa.entity.impl.Node;
import com.psa.entity.impl.OneDimFiniteElement;
import com.psa.entity.impl.Structure;

public class ForceVector implements VectorMatrix, Serializable {

	private int DOF = 0;
	private SortedMap<Integer, Double> forceValues = new TreeMap<Integer, Double>();
	private List<Integer> coOrdinatesOfFreedom = new ArrayList<Integer>();

	public void setForceVector(LoadCase loadCase)
			throws PPSAModellingFaultException {
		GlobalStiffnessMatrixEntity globalStiffnessMatrix = loadCase.getParentStructure()
				.getGlobalStiffnessMatrix();
		List<Integer> globalCoOrinatesOfFreedom = globalStiffnessMatrix
				.getGlobalCoOrinatesOfFreedom();

		this.coOrdinatesOfFreedom = globalCoOrinatesOfFreedom;
		this.DOF = coOrdinatesOfFreedom.size();

		initiateForceValues();
		/**
		 * Nodal loads processing.
		 */
		processNodalLoads(loadCase);

		/**
		 * Perform for member loads.
		 */
		processMemberLoads(loadCase);

		// Collections.sort(coOrdinatesOfFreedom);
		loadCase.setGlobalForceVector(this);
		// setOtherCoOrdinateForcesToZero(loadCase);
	}

	
	private void processNodalLoads(LoadCase loadCase) {
		
		Set<NodalLoad> nodalLoads = loadCase.getNodalLoads().keySet();
		for (NodalLoad nodalLoad : nodalLoads) {
			Set<CommonNode> nodes = loadCase.getNodalLoads().get(nodalLoad);

			for (CommonNode commonNode : nodes) {
				// Check for xCordNum
				int xCordinateNumOfNode = commonNode.getxCordinateNum();
				double forceInX = nodalLoad.getfX();
				forceInX = forceInX + forceValues.get(xCordinateNumOfNode);
				forceValues.put(xCordinateNumOfNode, forceInX);

				// Check for yCordNum
				int yCordinateNumOfNode = commonNode.getyCordinateNum();
				double forceInY = nodalLoad.getfY();
				forceInY = forceInY + forceValues.get(yCordinateNumOfNode);
				forceValues.put(yCordinateNumOfNode, forceInY);

				// Force in Z nodal Load
				int zCordinateNumOfNode = commonNode.getzCordinateNum();
				if (zCordinateNumOfNode != 0) {
					double forceInZ = nodalLoad.getfZ();
					forceInZ = forceInZ + forceValues.get(zCordinateNumOfNode);
					forceValues.put(zCordinateNumOfNode, forceInZ);
				}
				
				// Check for mxCordNum
				int mxCordinateNumOfNode = commonNode.getMxCordinateNum();
				if (mxCordinateNumOfNode != 0) {
					double momInX = nodalLoad.getmX();
					momInX = momInX + forceValues.get(mxCordinateNumOfNode);
					forceValues.put(mxCordinateNumOfNode, momInX);
				}
				
				// Check for myCordNum
				int myCordinateNumOfNode = commonNode.getMyCordinateNum();
				if (myCordinateNumOfNode != 0) {
					double momInY = nodalLoad.getmY();
					momInY = momInY + forceValues.get(myCordinateNumOfNode);
					forceValues.put(myCordinateNumOfNode, momInY);
				}
				
				// Check for mzCordNum
				int mzCordinateNumOfNode = commonNode.getMzCordinateNum();
				if (mzCordinateNumOfNode != 0) {
					double momInZ = nodalLoad.getmZ();
					momInZ = momInZ + forceValues.get(mzCordinateNumOfNode);
					forceValues.put(mzCordinateNumOfNode, momInZ);
				}
			}
		}
	}
	
	private void processMemberLoads(LoadCase loadCase)
			throws PPSAModellingFaultException {
		Map<MemberLoad, Set<OneDimFiniteElement>> memberLoads = loadCase
				.getMemberLoads();
		Structure structure = loadCase.getParentStructure();
		StructureType structureType = structure.getStructureType();
		Set<MemberLoad> memLoads = memberLoads.keySet();
		for (MemberLoad memberLoad : memLoads) {
			Set<OneDimFiniteElement> oneDimFiniteElements = loadCase
					.getMemberLoads().get(memberLoad);

			for (OneDimFiniteElement oneDimFiniteElement : oneDimFiniteElements) {
				if (oneDimFiniteElement instanceof OneDimFiniteElement) {
					/**
					 * See to it that no extra loops coming.. Efficiency is very
					 * much important in this case.
					 */
					MemberLoadType memberLoadType = memberLoad
							.getMemberLoadType();
					if (memberLoadType == MemberLoadType.UNI) {
						if (structureType == StructureType.SPACE) {
							processUniMemberLoads_SpaceFrame(memberLoad, oneDimFiniteElement);
						} else {
							processUniMemberLoads(memberLoad, oneDimFiniteElement);
						}
					} else if (memberLoadType == MemberLoadType.CON) {
						if (structureType == StructureType.SPACE) {
							
							processConMemberLoads_SpaceFrame(memberLoad,
									oneDimFiniteElement,
									structure);
						} else {
							processConMemberLoads(memberLoad,
									oneDimFiniteElement,
									structure);
						}
					}
				}
			}
		}
		//System.out.println("Processed ForceVector...");
	}
	
	private void processUniMemberLoads_SpaceFrame(MemberLoad memberLoad,
			OneDimFiniteElement oneDimFiniteElement) throws PPSAModellingFaultException {
		UniformForce uniformForce = memberLoad.getUniformForce();
		UniMemberLoadDirection loadDirection = uniformForce.getLoadDirection();
		if(loadDirection == UniMemberLoadDirection.X) {

			processUniMemLoadLocal_X_SpaceFrame(oneDimFiniteElement, uniformForce);
		
			
		} else if (loadDirection == UniMemberLoadDirection.Y) {
			processUniMemLoadLocal_Y_SpaceFrame(oneDimFiniteElement, uniformForce);
			
		} else if (loadDirection == UniMemberLoadDirection.Z) {
			processUniMemLoadLocal_Z_SpaceFrame(oneDimFiniteElement, uniformForce);
			
		} else if (loadDirection == UniMemberLoadDirection.GX) {

			double[] globalForceVals = new double[12];
			double originalForceVal_GX = uniformForce.getForceVal();
			globalForceVals[0] = originalForceVal_GX;
			resolveGlobalForceToLocalForcesAndApply_UniForce(uniformForce,globalForceVals,oneDimFiniteElement);
			// Set Back to original
			uniformForce.setForceAndDirection(originalForceVal_GX, UniMemberLoadDirection.GX);
			
		} else if (loadDirection == UniMemberLoadDirection.GY) {

			double[] globalForceVals = new double[12];
			double originalForceVal_GY = uniformForce.getForceVal();
			globalForceVals[1] = originalForceVal_GY;
			resolveGlobalForceToLocalForcesAndApply_UniForce(uniformForce,globalForceVals,oneDimFiniteElement);
			// Set Back to original
			uniformForce.setForceAndDirection(originalForceVal_GY, UniMemberLoadDirection.GY);
			
		} else if (loadDirection == UniMemberLoadDirection.GZ) {

			double[] globalForceVals = new double[12];
			double originalForceVal_GZ = uniformForce.getForceVal();
			globalForceVals[2] = originalForceVal_GZ;
			resolveGlobalForceToLocalForcesAndApply_UniForce(uniformForce,globalForceVals,oneDimFiniteElement);
			// Set Back to original
			uniformForce.setForceAndDirection(originalForceVal_GZ, UniMemberLoadDirection.GZ);
			
		} else if (loadDirection == UniMemberLoadDirection.PX) {
			double originalForceVal_PX = uniformForce.getForceVal();
			
			double loadGX = MathHelper.getGlobalForceFromProjectedLength_PX(uniformForce,oneDimFiniteElement);
			uniformForce.setForceAndDirection(loadGX, UniMemberLoadDirection.GX);
			double[] globalForceVals = new double[12];
			globalForceVals[0] = loadGX;
			resolveGlobalForceToLocalForcesAndApply_UniForce(uniformForce,globalForceVals,oneDimFiniteElement);
			// Set Back to original
			uniformForce.setForceAndDirection(originalForceVal_PX, UniMemberLoadDirection.PX);
		} else if (loadDirection == UniMemberLoadDirection.PY) {

			double originalForceVal_PY = uniformForce.getForceVal();
			
			//double[] localForces = transformProjectedForceToGlobalForces(projectedForceVals,oneDimFiniteElement);
			
			//double loadGY = localForces[1];
			double loadGY = MathHelper.getGlobalForceFromProjectedLength_PY(uniformForce,oneDimFiniteElement);
			uniformForce.setForceAndDirection(loadGY, UniMemberLoadDirection.GY);
			double[] globalForceVals = new double[12];
			globalForceVals[1] = loadGY;
			resolveGlobalForceToLocalForcesAndApply_UniForce(uniformForce,globalForceVals,oneDimFiniteElement);
			// Set Back to original
			uniformForce.setForceAndDirection(originalForceVal_PY, UniMemberLoadDirection.PY);
		} else if (loadDirection == UniMemberLoadDirection.PZ) {

			double originalForceVal_PZ = uniformForce.getForceVal();
			
			double loadGZ = MathHelper.getGlobalForceFromProjectedLength_PZ(uniformForce,oneDimFiniteElement);
			uniformForce.setForceAndDirection(loadGZ, UniMemberLoadDirection.GZ);
			double[] globalForceVals = new double[12];
			globalForceVals[2] = loadGZ;
			resolveGlobalForceToLocalForcesAndApply_UniForce(uniformForce,globalForceVals,oneDimFiniteElement);
			// Set Back to original
			uniformForce.setForceAndDirection(originalForceVal_PZ, UniMemberLoadDirection.PZ);
		}
	}


	/*private double[] transformProjectedForceToGlobalForces(double[] globalForceVals,
			OneDimFiniteElement oneDimFiniteElement) {
		
		StiffnessMatrixEntity stiffnessMatrixEntity = oneDimFiniteElement.getStiffnessMatrixEntity();
		StiffnessMatrixBeam3DImpl beam3dImpl = (StiffnessMatrixBeam3DImpl) stiffnessMatrixEntity;
		
		double[][] transformationMatrix = beam3dImpl.getTransformationMatrix();
		//double[] localForceVector = new MathOperationJAMAImpl().transformGlobalForceVector(transformationMatrix, globalForceVals);
		double loadPX = globalForceVals[0];
		double loadPY = globalForceVals[1];
		double loadPZ = globalForceVals[2];
		
		double cX = MathHelper.getMod(transformationMatrix[0][0]);
		double cY = MathHelper.getMod(transformationMatrix[0][1]);
		double cZ = MathHelper.getMod(transformationMatrix[0][2]);

		double loadGX = loadPX*cY*cZ*cX;
		double loadGY = loadPY*cX*cZ*cX;
		double loadGZ = loadPZ*cZ;
		
		double[] globalForces = {loadGX,loadGY,loadGZ};
		
		return globalForces; 
	}*/


	private void resolveGlobalForceToLocalForcesAndApply_UniForce(
			UniformForce uniformForce, double[] globalForceVals,
			OneDimFiniteElement oneDimFiniteElement) throws PPSAModellingFaultException {
		
		StiffnessMatrixEntity stiffnessMatrixEntity = oneDimFiniteElement.getStiffnessMatrixEntity();
		StiffnessMatrixBeam3DImpl beam3dImpl = (StiffnessMatrixBeam3DImpl) stiffnessMatrixEntity;
		
		double[][] transformationMatrix = beam3dImpl.getTransformationMatrix();
		double[] localForceVector = new MathOperationJAMAImpl().transformGlobalForceVector(transformationMatrix, globalForceVals);
		
		double force_LX = localForceVector[0];
		double force_LY = localForceVector[1];
		double force_LZ = localForceVector[2];

		uniformForce.setForceAndDirection(force_LY, UniMemberLoadDirection.Y);
		processUniMemLoadLocal_Y_SpaceFrame(oneDimFiniteElement, uniformForce);
		uniformForce.setForceAndDirection(force_LZ, UniMemberLoadDirection.Z);
		processUniMemLoadLocal_Z_SpaceFrame(oneDimFiniteElement, uniformForce);
		uniformForce.setForceAndDirection(force_LX, UniMemberLoadDirection.X);
		processUniMemLoadLocal_X_SpaceFrame(oneDimFiniteElement, uniformForce);
	}


	private void processUniMemLoadLocal_Z_SpaceFrame(
			OneDimFiniteElement oneDimFiniteElement, UniformForce uniformForce) throws PPSAModellingFaultException {
		double elementLength = oneDimFiniteElement.getLengthOfElement();

		double resultantNodalForceOnFirstNode=0;
		double resultantNodalForceOnSecondNode=0;
		double nodalMomentOnFirstNode=0;
		double nodalMomentOnSecondNode=0;
		
			/**
			 * Formulas for Fixed End Moment referred from textbook
			 * 'Indeterminate structures'- 4th edition by R.L.Jindal
			 * page number 570.
			 * 
			 */
			//double rangeBegin = uniformForce.getRangeBegin();
			double rangeEnd = uniformForce.getRangeEnd();
			if(!uniformForce.isPatchLoad())
				rangeEnd = oneDimFiniteElement.getLengthOfElement();

			double c = elementLength-uniformForce.getRangeEnd();

			if(rangeEnd<0 || c<0)
				throw new PPSAModellingFaultException(
						"UDL range improper for element "
								+ oneDimFiniteElement.getElementNumber());


			double forceVal = uniformForce.getForceVal();
			double[] fixedEndReactions = FixEndMomentUtil.getFixedEndReactionAndMomentsForPatchUDL_LY(elementLength, uniformForce,forceVal);
			resultantNodalForceOnFirstNode = fixedEndReactions[0];
			resultantNodalForceOnSecondNode = fixedEndReactions[1];
			nodalMomentOnFirstNode = fixedEndReactions[2];
			nodalMomentOnSecondNode = fixedEndReactions[3];

		
		/*double[] forceVals = new double[12];
		forceVals[1] = resultantNodalForceOnFirstNode;
		forceVals[5] = nodalMomentOnFirstNode;
		forceVals[7] = resultantNodalForceOnSecondNode;
		forceVals[11] = nodalMomentOnSecondNode;*/
		
		double[] forceVals = new double[12];
		forceVals[2] = resultantNodalForceOnFirstNode;
		forceVals[4] = -nodalMomentOnFirstNode;
		forceVals[8] = resultantNodalForceOnSecondNode;
		forceVals[10] = -nodalMomentOnSecondNode;
		
		/***
		 * LATER : Just make it a function no requirement of vector.
		 */
		LocalForceVector forceVector = new LocalForceVector(forceVals,oneDimFiniteElement);
		
	}


	private void processUniMemLoadLocal_Y_SpaceFrame(
			OneDimFiniteElement oneDimFiniteElement, UniformForce uniformForce) throws PPSAModellingFaultException {
		double elementLength = oneDimFiniteElement.getLengthOfElement();

		double resultantNodalForceOnFirstNode=0;
		double resultantNodalForceOnSecondNode=0;
		double nodalMomentOnFirstNode=0;
		double nodalMomentOnSecondNode=0;
		
			/**
			 * Formulas for Fixed End Moment referred from textbook
			 * 'Indeterminate structures'- 4th edition by R.L.Jindal
			 * page number 570.
			 * 
			 */
			//double rangeBegin = uniformForce.getRangeBegin();
			double rangeEnd = uniformForce.getRangeEnd();
			if(!uniformForce.isPatchLoad())
				rangeEnd = oneDimFiniteElement.getLengthOfElement();

			double c = elementLength-uniformForce.getRangeEnd();

			if(rangeEnd<0 || c<0)
				throw new PPSAModellingFaultException(
						"UDL range improper for element "
								+ oneDimFiniteElement.getElementNumber());


			double forceVal = uniformForce.getForceVal();
			double[] fixedEndReactions = FixEndMomentUtil.getFixedEndReactionAndMomentsForPatchUDL_LY(elementLength, uniformForce,forceVal);
			resultantNodalForceOnFirstNode = fixedEndReactions[0];
			resultantNodalForceOnSecondNode = fixedEndReactions[1];
			nodalMomentOnFirstNode = fixedEndReactions[2];
			nodalMomentOnSecondNode = fixedEndReactions[3];

		
		double[] forceVals = new double[12];
		forceVals[1] = resultantNodalForceOnFirstNode;
		forceVals[5] = nodalMomentOnFirstNode;
		forceVals[7] = resultantNodalForceOnSecondNode;
		forceVals[11] = nodalMomentOnSecondNode;
		
		/***
		 * LATER : Just make it a function no requirement of vector.
		 */
		LocalForceVector forceVector = new LocalForceVector(forceVals,oneDimFiniteElement);
		
	}


	private void processUniMemLoadLocal_X_SpaceFrame(
			OneDimFiniteElement oneDimFiniteElement, UniformForce uniformForce) throws PPSAModellingFaultException {
		double elementLength = oneDimFiniteElement.getLengthOfElement();

		double resultantNodalForceOnFirstNodeLocalX=0;
		double resultantNodalForceOnSecondNodeLocalX=0;
		/*double nodalMomentOnFirstNode=0;
		double nodalMomentOnSecondNode=0;*/
		

			double rangeBegin = uniformForce.getRangeBegin();
			double rangeEnd = uniformForce.getRangeEnd();				
			if(!uniformForce.isPatchLoad())
				rangeEnd = oneDimFiniteElement.getLengthOfElement();

			double c = elementLength-uniformForce.getRangeEnd();

			if(rangeEnd<0 || c<0)
				throw new PPSAModellingFaultException(
						"UDL range improper for element "
								+ oneDimFiniteElement.getElementNumber());
			
			double totalLenOfForce = rangeEnd-rangeBegin;
			double resultantTotalForce = totalLenOfForce*uniformForce.getForceVal();
			double resultantActLocation = rangeBegin+(totalLenOfForce/2);
			resultantNodalForceOnFirstNodeLocalX = resultantTotalForce*(elementLength-resultantActLocation)/elementLength;
			resultantNodalForceOnSecondNodeLocalX = resultantTotalForce*resultantActLocation/elementLength;


		Node firstNode = oneDimFiniteElement.getFirstNode();
		Node secondNode = oneDimFiniteElement.getSecondNode();

		double[] forceVals = new double[12];
		forceVals[0] = resultantNodalForceOnFirstNodeLocalX;
		forceVals[6] = resultantNodalForceOnSecondNodeLocalX;

		LocalForceVector forceVector = new LocalForceVector(forceVals,oneDimFiniteElement);
		/*addMemberForceEffectOnNodeLoadFromLoadOfLocalX(oneDimFiniteElement, firstNode,
				resultantNodalForceOnFirstNode);

		addMemberForceEffectOnNodeLoadFromLoadOfLocalX(oneDimFiniteElement, secondNode,
				resultantNodalForceOnSecondNode);*/
	}


	private void processConMemberLoads_SpaceFrame(MemberLoad memberLoad,
			OneDimFiniteElement oneDimFiniteElement, Structure structure) throws PPSAModellingFaultException {
		ConcentratedForce conForce = memberLoad.getConcentratedForce();
		ConMemberLoadDirection loadDirection = conForce.getLoadDirection();
		if (loadDirection == ConMemberLoadDirection.Y) {
			processConMemLoadLocal_Y_SpaceFrame(oneDimFiniteElement, conForce);

		} else if (loadDirection == ConMemberLoadDirection.Z) {
			processConMemLoadLocal_Z_SpaceFrame(oneDimFiniteElement, conForce);

		} else if (loadDirection == ConMemberLoadDirection.X) {
			processConMemLoadLocal_X_SpaceFrame(oneDimFiniteElement, conForce);

		} else if (loadDirection == ConMemberLoadDirection.GY) {
			double[] globalForceVals = new double[12];
			double originalForceVal_GY = conForce.getForceVal();
			globalForceVals[1] = originalForceVal_GY;
			resolveGlobalForceToLocalForcesAndApply_ConForce(conForce,globalForceVals,oneDimFiniteElement);
			// Set Back to original
			conForce.setForceAndDirection(originalForceVal_GY, ConMemberLoadDirection.GY);
			
		} else if (loadDirection == ConMemberLoadDirection.GX) {
			double[] globalForceVals = new double[12];
			double originalForceVal_GX = conForce.getForceVal();
			globalForceVals[0] = originalForceVal_GX;
			resolveGlobalForceToLocalForcesAndApply_ConForce(conForce,globalForceVals,oneDimFiniteElement);
			// Set Back to original
			conForce.setForceAndDirection(originalForceVal_GX, ConMemberLoadDirection.GX);
			
		} else if (loadDirection == ConMemberLoadDirection.GZ) {
			double[] globalForceVals = new double[12];
			double originalForceVal_GZ = conForce.getForceVal();
			globalForceVals[2] = originalForceVal_GZ;
			resolveGlobalForceToLocalForcesAndApply_ConForce(conForce,globalForceVals,oneDimFiniteElement);
			// Set Back to original
			conForce.setForceAndDirection(originalForceVal_GZ, ConMemberLoadDirection.GZ);
			
		}
	}


	private void resolveGlobalForceToLocalForcesAndApply_ConForce(ConcentratedForce conForce,
			double[] globalForceVals, OneDimFiniteElement oneDimFiniteElement) throws PPSAModellingFaultException {
		
		StiffnessMatrixEntity stiffnessMatrixEntity = oneDimFiniteElement.getStiffnessMatrixEntity();
		StiffnessMatrixBeam3DImpl beam3dImpl = (StiffnessMatrixBeam3DImpl) stiffnessMatrixEntity;
		
		double[][] transformationMatrix = beam3dImpl.getTransformationMatrix();
		double[] localForceVector = new MathOperationJAMAImpl().transformGlobalForceVector(transformationMatrix, globalForceVals);
		
		double force_LX = localForceVector[0];
		double force_LY = localForceVector[1];
		double force_LZ = localForceVector[2];
		
		conForce.setForceAndDirection(force_LY, ConMemberLoadDirection.Y);
		processConMemLoadLocal_Y_SpaceFrame(oneDimFiniteElement, conForce);
		conForce.setForceAndDirection(force_LZ, ConMemberLoadDirection.Z);
		processConMemLoadLocal_Z_SpaceFrame(oneDimFiniteElement, conForce);
		conForce.setForceAndDirection(force_LX, ConMemberLoadDirection.X);
		processConMemLoadLocal_X_SpaceFrame(oneDimFiniteElement, conForce);
	}


	private void processConMemLoadLocal_X_SpaceFrame(
			OneDimFiniteElement oneDimFiniteElement, ConcentratedForce conForce) throws PPSAModellingFaultException {
		double forceVal = conForce.getForceVal();
		double elementLength = oneDimFiniteElement.getLengthOfElement();

		/**
		 * distance from first node to load location.
		 */
		double a = conForce.getD1();

		/**
		 * distance from load location to second node.
		 */
		double b = elementLength - a;

		if (b < 0 || a < 0)
			throw new PPSAModellingFaultException(
					"d1 value exceeds length of member."
							+ "While defining CON MEMBER LOAD");

		Node firstNode = oneDimFiniteElement.getFirstNode();
		Node secondNode = oneDimFiniteElement.getSecondNode();

		/**
		 * Formulae taken from TextBook 'Structural and Stress Analysis'-2nd Edition. 
		 * - BY 'T.H.G.Megson'. page number 32. 
		 */
		double resultantNodalForceOnFirstNodeLocalX = forceVal*b/elementLength;
		double resultantNodalForceOnSecondNodeLocalX = forceVal*a/elementLength;

		double[] forceVals = new double[12];
		forceVals[0] = resultantNodalForceOnFirstNodeLocalX;
		forceVals[6] = resultantNodalForceOnSecondNodeLocalX;

		LocalForceVector forceVector = new LocalForceVector(forceVals,oneDimFiniteElement);
	}


	private void processConMemLoadLocal_Z_SpaceFrame(
			OneDimFiniteElement oneDimFiniteElement, ConcentratedForce conForce) throws PPSAModellingFaultException {
		double forceVal = conForce.getForceVal();
		double elementLength = oneDimFiniteElement.getLengthOfElement();

		/**
		 * distance from first node to load location.
		 */
		double a = conForce.getD1();

		/**
		 * distance from load location to second node.
		 */
		double b = elementLength - a;

		
		if (b < 0 || a < 0)
			throw new PPSAModellingFaultException(
					"d1 value exceeds length of member."
							+ "While defining CON MEMBER LOAD");

		Node firstNode = oneDimFiniteElement.getFirstNode();
		Node secondNode = oneDimFiniteElement.getSecondNode();

		/**
		 * Note : ResultantForce should be resolved to fx & fy.
		 */

		double resultantNodalForceOnFirstNode = (forceVal * b * b * (elementLength + (2 * a)))
				/ (elementLength * elementLength * elementLength);
		double resultantNodalForceOnSecondNode = (forceVal * a * a * (elementLength + (2 * b)))
				/ (elementLength * elementLength * elementLength);

		/**
		 * Note : Moment need not be resolved. later : verify
		 */
		double nodalMomentOnFirstNode = (forceVal * a * b * b)
				/ (elementLength * elementLength);
		double nodalMomentOnSecondNode = -(forceVal * a * a * b)
				/ (elementLength * elementLength);
		double[] forceVals = new double[12];
		forceVals[2] = resultantNodalForceOnFirstNode;
		forceVals[4] = -nodalMomentOnFirstNode;
		forceVals[8] = resultantNodalForceOnSecondNode;
		forceVals[10] = -nodalMomentOnSecondNode;
		
		LocalForceVector forceVector = new LocalForceVector(forceVals,oneDimFiniteElement);
	}


	private void processConMemLoadLocal_Y_SpaceFrame(
			OneDimFiniteElement oneDimFiniteElement, ConcentratedForce conForce) throws PPSAModellingFaultException {
		double forceVal = conForce.getForceVal();
		double elementLength = oneDimFiniteElement.getLengthOfElement();

		/**
		 * distance from first node to load location.
		 */
		double a = conForce.getD1();

		/**
		 * distance from load location to second node.
		 */
		double b = elementLength - a;

		
		if (b < 0 || a < 0)
			throw new PPSAModellingFaultException(
					"d1 value exceeds length of member."
							+ "While defining CON MEMBER LOAD");

		Node firstNode = oneDimFiniteElement.getFirstNode();
		Node secondNode = oneDimFiniteElement.getSecondNode();

		/**
		 * Note : ResultantForce should be resolved to fx & fy.
		 */

		double resultantNodalForceOnFirstNode = (forceVal * b * b * (elementLength + (2 * a)))
				/ (elementLength * elementLength * elementLength);
		double resultantNodalForceOnSecondNode = (forceVal * a * a * (elementLength + (2 * b)))
				/ (elementLength * elementLength * elementLength);

		/**
		 * Note : Moment need not be resolved. later : verify
		 */
		double nodalMomentOnFirstNode = (forceVal * a * b * b)
				/ (elementLength * elementLength);
		double nodalMomentOnSecondNode = -(forceVal * a * a * b)
				/ (elementLength * elementLength);
		double[] forceVals = new double[12];
		forceVals[1] = resultantNodalForceOnFirstNode;
		forceVals[5] = nodalMomentOnFirstNode;
		forceVals[7] = resultantNodalForceOnSecondNode;
		forceVals[11] = nodalMomentOnSecondNode;
		
		/***
		 * LATER : Just make it a function no requirement of vector.
		 */
		LocalForceVector forceVector = new LocalForceVector(forceVals,oneDimFiniteElement);
	}


	private void processUniMemberLoads(MemberLoad memberLoad,
			OneDimFiniteElement oneDimFiniteElement) throws PPSAModellingFaultException {
		UniformForce uniformForce = memberLoad.getUniformForce();
		UniMemberLoadDirection loadDirection = uniformForce.getLoadDirection();
		if(loadDirection == UniMemberLoadDirection.X) {

			processUniMemLoadLocal_X(oneDimFiniteElement, uniformForce);
		
			
		} else if (loadDirection == UniMemberLoadDirection.Y) {
			processUniMemLoadLocal_Y(oneDimFiniteElement, uniformForce);
			
		} else if (loadDirection == UniMemberLoadDirection.GX) {
			
			double originalForceVal = uniformForce.getForceVal();

			double forceValResolvedOnLY = MathHelper.resolveForceInX(originalForceVal,
					oneDimFiniteElement.getAngleOfInclination());
			double forceValResolvedOnLX = MathHelper.resolveForceInY(originalForceVal,
					oneDimFiniteElement.getAngleOfInclination());
			
			uniformForce.setForceVal(-forceValResolvedOnLY);
			processUniMemLoadLocal_Y(oneDimFiniteElement, uniformForce);

			uniformForce.setForceVal(forceValResolvedOnLX);
			processUniMemLoadLocal_X(oneDimFiniteElement, uniformForce);
			
			uniformForce.setForceVal(originalForceVal);

		} else if (loadDirection == UniMemberLoadDirection.GY) {

			double originalForceVal = uniformForce.getForceVal();

			double forceValResolvedOnLY = MathHelper.resolveForceInY(originalForceVal,
					oneDimFiniteElement.getAngleOfInclination());
			double forceValResolvedOnLX = MathHelper.resolveForceInX(originalForceVal,
					oneDimFiniteElement.getAngleOfInclination());
			
			uniformForce.setForceVal(forceValResolvedOnLY);
			processUniMemLoadLocal_Y(oneDimFiniteElement, uniformForce);

			uniformForce.setForceVal(forceValResolvedOnLX);
			processUniMemLoadLocal_X(oneDimFiniteElement, uniformForce);
			
			uniformForce.setForceVal(originalForceVal);
			
		
			
		} else if (loadDirection == UniMemberLoadDirection.PX) {
			/**
			 * Same as GX but forceVal given is to be resolved. And the resolved
			 * value can be considered as GX. STAAD GUI itself will resolve this
			 * force and display when PX is given.
			 */

			double forceValUnResolved = uniformForce.getForceVal();

			double originalForceVal = MathHelper.resolveForceInX(forceValUnResolved,
					MathHelper.getMod(oneDimFiniteElement
							.getAngleOfInclination()));
			
			double forceValResolvedOnLY = MathHelper.resolveForceInX(originalForceVal,
					oneDimFiniteElement.getAngleOfInclination());
			double forceValResolvedOnLX = MathHelper.resolveForceInY(originalForceVal,
					oneDimFiniteElement.getAngleOfInclination());
			
			uniformForce.setForceVal(-forceValResolvedOnLY);
			processUniMemLoadLocal_Y(oneDimFiniteElement, uniformForce);

			uniformForce.setForceVal(forceValResolvedOnLX);
			processUniMemLoadLocal_X(oneDimFiniteElement, uniformForce);
			
			uniformForce.setForceVal(forceValUnResolved);

		} else if (loadDirection == UniMemberLoadDirection.PY) {

			double forceValUnResolved = uniformForce.getForceVal();

			double originalForceVal = MathHelper.resolveForceInY(forceValUnResolved,
					MathHelper.getMod(oneDimFiniteElement
							.getAngleOfInclination()));
			
			double forceValResolvedOnLY = MathHelper.resolveForceInY(originalForceVal,
					oneDimFiniteElement.getAngleOfInclination());
			double forceValResolvedOnLX = MathHelper.resolveForceInX(originalForceVal,
					oneDimFiniteElement.getAngleOfInclination());
			
			uniformForce.setForceVal(forceValResolvedOnLY);
			processUniMemLoadLocal_Y(oneDimFiniteElement, uniformForce);

			uniformForce.setForceVal(forceValResolvedOnLX);
			processUniMemLoadLocal_X(oneDimFiniteElement, uniformForce);
			
			uniformForce.setForceVal(forceValUnResolved);
		}
	}

	private void processConMemberLoads(MemberLoad memberLoad,
			OneDimFiniteElement oneDimFiniteElement, Structure structure)
			throws PPSAModellingFaultException {
		ConcentratedForce conForce = memberLoad.getConcentratedForce();
		ConMemberLoadDirection loadDirection = conForce.getLoadDirection();
		if (loadDirection == ConMemberLoadDirection.Y) {
			processConMemLoadLocal_Y(oneDimFiniteElement, conForce);

		} else if (loadDirection == ConMemberLoadDirection.X) {
			processConMemLoadLocal_X(oneDimFiniteElement, conForce);

		} else if (loadDirection == ConMemberLoadDirection.GY) {
			double originalForceVal = conForce.getForceVal();
			double forceValResolvedOnY = MathHelper.resolveForceInY(originalForceVal,
					oneDimFiniteElement.getAngleOfInclination());

			double forceValResolvedOnLocalX = MathHelper.resolveForceInX(
					originalForceVal, oneDimFiniteElement.getAngleOfInclination());
			
			conForce.setForceVal(forceValResolvedOnY);
			processConMemLoadLocal_Y(oneDimFiniteElement, conForce);
			
			conForce.setForceVal(forceValResolvedOnLocalX);
			processConMemLoadLocal_X(oneDimFiniteElement, conForce);

			conForce.setForceVal(originalForceVal);
		
		} else if (loadDirection == ConMemberLoadDirection.GX) {
			double originalForceVal = conForce.getForceVal();
			double forceValResolvedOnY = MathHelper.resolveForceInX(originalForceVal,
					oneDimFiniteElement.getAngleOfInclination());

			double forceValResolvedOnLocalX = MathHelper.resolveForceInY(
					originalForceVal, oneDimFiniteElement.getAngleOfInclination());
			
			conForce.setForceVal(-forceValResolvedOnY);
			processConMemLoadLocal_Y(oneDimFiniteElement, conForce);

			conForce.setForceVal(forceValResolvedOnLocalX);
			processConMemLoadLocal_X(oneDimFiniteElement, conForce);
			
			conForce.setForceVal(originalForceVal);

		}
	}

	private void processConMemLoadLocal_X(
			OneDimFiniteElement oneDimFiniteElement, ConcentratedForce conForce)
			throws PPSAModellingFaultException {
		double forceVal = conForce.getForceVal();
		double elementLength = oneDimFiniteElement.getLengthOfElement();

		/**
		 * distance from first node to load location.
		 */
		double a = conForce.getD1();

		/**
		 * distance from load location to second node.
		 */
		double b = elementLength - a;

		if (b < 0 || a < 0)
			throw new PPSAModellingFaultException(
					"d1 value exceeds length of member."
							+ "While defining CON MEMBER LOAD");

		Node firstNode = oneDimFiniteElement.getFirstNode();
		Node secondNode = oneDimFiniteElement.getSecondNode();

		/**
		 * Formulae taken from TextBook 'Structural and Stress Analysis'-2nd Edition. 
		 * - BY 'T.H.G.Megson'. page number 32. 
		 */
		double resultantNodalForceOnFirstNodeLocalX = forceVal*b/elementLength;
		double resultantNodalForceOnSecondNodeLocalX = forceVal*a/elementLength;

		addMemberForceEffectOnNodeLoadFromLoadOfLocalX(oneDimFiniteElement,
				firstNode, resultantNodalForceOnFirstNodeLocalX);
		addMemberForceEffectOnNodeLoadFromLoadOfLocalX(oneDimFiniteElement,
				secondNode, resultantNodalForceOnSecondNodeLocalX);
	}

	private void processConMemLoadLocal_Y(
			OneDimFiniteElement oneDimFiniteElement, ConcentratedForce conForce)
			throws PPSAModellingFaultException {
		double forceVal = conForce.getForceVal();
		double elementLength = oneDimFiniteElement.getLengthOfElement();

		/**
		 * distance from first node to load location.
		 */
		double a = conForce.getD1();

		/**
		 * distance from load location to second node.
		 */
		double b = elementLength - a;

		
		if (b < 0 || a < 0)
			throw new PPSAModellingFaultException(
					"d1 value exceeds length of member."
							+ "While defining CON MEMBER LOAD");

		Node firstNode = oneDimFiniteElement.getFirstNode();
		Node secondNode = oneDimFiniteElement.getSecondNode();

		/**
		 * Note : ResultantForce should be resolved to fx & fy.
		 */

		double resultantNodalForceOnFirstNode = (forceVal * b * b * (elementLength + (2 * a)))
				/ (elementLength * elementLength * elementLength);
		double resultantNodalForceOnSecondNode = (forceVal * a * a * (elementLength + (2 * b)))
				/ (elementLength * elementLength * elementLength);

		/**
		 * Note : Moment need not be resolved. later : verify
		 */
		double nodalMomentOnFirstNode = (forceVal * a * b * b)
				/ (elementLength * elementLength);
		double nodalMomentOnSecondNode = -(forceVal * a * a * b)
				/ (elementLength * elementLength);

		addMemberForceEffectOnNodeLoadFromLoadOfLocalY(oneDimFiniteElement,
				firstNode, resultantNodalForceOnFirstNode,
				nodalMomentOnFirstNode);

		addMemberForceEffectOnNodeLoadFromLoadOfLocalY(oneDimFiniteElement,
				secondNode, resultantNodalForceOnSecondNode,
				nodalMomentOnSecondNode);
	}

	private void processUniMemLoadLocal_Y(
			OneDimFiniteElement oneDimFiniteElement, UniformForce uniformForce)
			throws PPSAModellingFaultException {
		double elementLength = oneDimFiniteElement.getLengthOfElement();

		double resultantNodalForceOnFirstNode=0;
		double resultantNodalForceOnSecondNode=0;
		double nodalMomentOnFirstNode=0;
		double nodalMomentOnSecondNode=0;
		

		/*if (!uniformForce.isPatchLoad()) {
			double totalForceOnMember = forceVal * elementLength;
			*//**
			 * Note : ResultantForce should be resolved to fx & fy.
			 *//*
			resultantNodalForceOnFirstNode = totalForceOnMember / 2;
			resultantNodalForceOnSecondNode = totalForceOnMember / 2;

			*//**
			 * Note : Moment need not be resolved. later : verify
			 *//*
			double momentOnNode = forceVal * elementLength * elementLength
					/ 12;
			nodalMomentOnFirstNode = momentOnNode;
			nodalMomentOnSecondNode = -momentOnNode;
		} else {*/
			
			/**
			 * Formulas for Fixed End Moment referred from textbook
			 * 'Indeterminate structures'- 4th edition by R.L.Jindal
			 * page number 570.
			 * 
			 */
			//double rangeBegin = uniformForce.getRangeBegin();
			double rangeEnd = uniformForce.getRangeEnd();
			if(!uniformForce.isPatchLoad())
				rangeEnd = elementLength;

			double c = elementLength-uniformForce.getRangeEnd();

			if(rangeEnd<0 || c<0)
				throw new PPSAModellingFaultException(
						"UDL range improper for element "
								+ oneDimFiniteElement.getElementNumber());

			Node firstNode = oneDimFiniteElement.getFirstNode();
			Node secondNode = oneDimFiniteElement.getSecondNode();

			double forceVal = uniformForce.getForceVal();
			double[] fixedEndReactions = FixEndMomentUtil.getFixedEndReactionAndMomentsForPatchUDL_LY(elementLength, uniformForce,forceVal);
			resultantNodalForceOnFirstNode = fixedEndReactions[0];
			resultantNodalForceOnSecondNode = fixedEndReactions[1];
			nodalMomentOnFirstNode = fixedEndReactions[2];
			nodalMomentOnSecondNode = fixedEndReactions[3];

		addMemberForceEffectOnNodeLoadFromLoadOfLocalY(oneDimFiniteElement,
				firstNode, resultantNodalForceOnFirstNode,
				nodalMomentOnFirstNode);

		addMemberForceEffectOnNodeLoadFromLoadOfLocalY(oneDimFiniteElement,
				secondNode, resultantNodalForceOnSecondNode,
				nodalMomentOnSecondNode);
	}

	private void processUniMemLoadLocal_X(
			OneDimFiniteElement oneDimFiniteElement, UniformForce uniformForce)
			throws PPSAModellingFaultException {
		double elementLength = oneDimFiniteElement.getLengthOfElement();

		double resultantNodalForceOnFirstNode=0;
		double resultantNodalForceOnSecondNode=0;
		/*double nodalMomentOnFirstNode=0;
		double nodalMomentOnSecondNode=0;*/
		

			double rangeBegin = uniformForce.getRangeBegin();
			double rangeEnd = uniformForce.getRangeEnd();				
			if(!uniformForce.isPatchLoad())
				rangeEnd = oneDimFiniteElement.getLengthOfElement();

			double c = elementLength-uniformForce.getRangeEnd();

			if(rangeEnd<0 || c<0)
				throw new PPSAModellingFaultException(
						"UDL range improper for element "
								+ oneDimFiniteElement.getElementNumber());
			
			double totalLenOfForce = rangeEnd-rangeBegin;
			double resultantTotalForce = totalLenOfForce*uniformForce.getForceVal();
			double resultantActLocation = rangeBegin+(totalLenOfForce/2);
			resultantNodalForceOnFirstNode = resultantTotalForce*(elementLength-resultantActLocation)/elementLength;
			resultantNodalForceOnSecondNode = resultantTotalForce*resultantActLocation/elementLength;


		Node firstNode = oneDimFiniteElement.getFirstNode();
		Node secondNode = oneDimFiniteElement.getSecondNode();

		addMemberForceEffectOnNodeLoadFromLoadOfLocalX(oneDimFiniteElement, firstNode,
				resultantNodalForceOnFirstNode);

		addMemberForceEffectOnNodeLoadFromLoadOfLocalX(oneDimFiniteElement, secondNode,
				resultantNodalForceOnSecondNode);
	}


	/**
	 * This method resolves the resultant component to 'x' and 'y' directions
	 * and then adds that force to appropriate coOrdinates.
	 */
	private void addMemberForceEffectOnNodeLoadFromLoadOfLocalY(
			OneDimFiniteElement oneDimFiniteElement, Node node,
			double resultantNodalForceOnFirstNode, double nodalMomentOnFirstNode) {

		// Check for yCordNum
		int xCordinateNumOfNode = node.getxCordinateNum();
		double angleOfInclination = oneDimFiniteElement.getAngleOfInclination();
		double forceInX = -MathHelper.resolveForceInX(
				resultantNodalForceOnFirstNode, angleOfInclination);
		forceInX = forceInX + forceValues.get(xCordinateNumOfNode);
		forceValues.put(xCordinateNumOfNode, forceInX);

		// Check for yCordNum
		int yCordinateNumOfNode = node.getyCordinateNum();
		double forceInY = MathHelper.resolveForceInY(
				resultantNodalForceOnFirstNode, angleOfInclination);
		forceInY = forceInY + forceValues.get(yCordinateNumOfNode);
		forceValues.put(yCordinateNumOfNode, forceInY);

		// Check for mzCordNum
		int mzCordinateNumOfNode = node.getMzCordinateNum();
		if (mzCordinateNumOfNode != 0) {
			double momInZ = nodalMomentOnFirstNode;
			momInZ = momInZ + forceValues.get(mzCordinateNumOfNode);
			forceValues.put(mzCordinateNumOfNode, momInZ);
		}
	}

	private void addMemberForceEffectOnNodeLoadFromLoadOfLocalX(
			OneDimFiniteElement oneDimFiniteElement, Node node,
			double resultantNodalForceOnFirstNode) {

		double angleOfInclination = oneDimFiniteElement.getAngleOfInclination();
		// Check for xCordNum
		int gxCordinateNumOfNode = node.getxCordinateNum();
		double forceInGlobalX = MathHelper.resolveForceInY(
				resultantNodalForceOnFirstNode, angleOfInclination);
		forceInGlobalX = forceInGlobalX + forceValues.get(gxCordinateNumOfNode);
		forceValues.put(gxCordinateNumOfNode, forceInGlobalX);

		// Check for yCordNum
		int gyCordinateNumOfNode = node.getyCordinateNum();
		double forceInGlobalY = MathHelper.resolveForceInX(
				resultantNodalForceOnFirstNode, angleOfInclination);
		forceInGlobalY = forceInGlobalY + forceValues.get(gyCordinateNumOfNode);
		forceValues.put(gyCordinateNumOfNode, forceInGlobalY);

	}
	
	private void initiateForceValues() {
		for (Integer coOfFreedom : this.coOrdinatesOfFreedom) {
			forceValues.put(coOfFreedom, 0d);
		}

	}

	@Override
	public List<Integer> getCoOrdinatedOfFreedom() {
		return coOrdinatesOfFreedom;
	}

	@Override
	public int getSizeOfVector() {
		return DOF;
	}

	@Override
	public Map<Integer, Double> getVectorValues() {
		return forceValues;
	}
	
	private class LocalForceVector implements VectorMatrix {

		List<Integer> coOrdsOfFreedom = new ArrayList<>();
		Map<Integer, Double> vectorValues = new TreeMap<Integer,Double>();
		double[] forceVector = null; 
		
		public LocalForceVector(double[] localForceVals, OneDimFiniteElement oneDimFiniteElement) {
			forceVector = MathHelper.resolveToToGlobalFomLocal(localForceVals, oneDimFiniteElement);
			
			addForcesToGlobalForceVector(oneDimFiniteElement);
		}

		private void addForcesToGlobalForceVector(OneDimFiniteElement oneDimFiniteElement) {

			Node firstNode = oneDimFiniteElement.getFirstNode();
			// Check for yCordNum
			int xCordinateNumOfNodeFirst = firstNode.getxCordinateNum();
			Double earlierForce = forceValues.get(xCordinateNumOfNodeFirst);
			double localForce = forceVector[0];
			double totalForce = earlierForce + localForce;
			forceValues.put(xCordinateNumOfNodeFirst, totalForce);

			// Check for yCordNum
			int yCordinateNumOfNodeFirst = firstNode.getyCordinateNum();
			earlierForce = forceValues.get(yCordinateNumOfNodeFirst);
			localForce = forceVector[1];
			totalForce = earlierForce + localForce;
			forceValues.put(yCordinateNumOfNodeFirst, totalForce);

			// Check for mzCordNum
			int mzCordinateNumOfNodeFirst = firstNode.getMzCordinateNum();
			localForce = forceVector[5];
			earlierForce = forceValues.get(mzCordinateNumOfNodeFirst);
			totalForce = localForce + earlierForce;
			forceValues.put(mzCordinateNumOfNodeFirst, totalForce);

			// Check for zCordNum
			int zCordinateNumOfNodeFirst = firstNode.getzCordinateNum();
			earlierForce = forceValues.get(zCordinateNumOfNodeFirst);
			localForce = forceVector[2];
			totalForce = earlierForce + localForce;
			forceValues.put(zCordinateNumOfNodeFirst, totalForce);

			// Check for myCordNum
			int myCordinateNumOfNodeFirst = firstNode.getMyCordinateNum();
			localForce = forceVector[4];
			earlierForce = forceValues.get(myCordinateNumOfNodeFirst);
			totalForce = localForce + earlierForce;
			forceValues.put(myCordinateNumOfNodeFirst, totalForce);
			
			// Check for myCordNum
			int mxCordinateNumOfNodeFirst = firstNode.getMxCordinateNum();
			localForce = forceVector[3];
			earlierForce = forceValues.get(mxCordinateNumOfNodeFirst);
			totalForce = localForce + earlierForce;
			forceValues.put(mxCordinateNumOfNodeFirst, totalForce);

			// ------------ SecNode Start ------------------
			// ----------------------------------------------
			Node secondNode = oneDimFiniteElement.getSecondNode();
			// Check for yCordNum
			int xCordinateNumOfNodeSec = secondNode.getxCordinateNum();
			earlierForce = forceValues.get(xCordinateNumOfNodeSec);
			localForce = forceVector[6];
			totalForce = earlierForce + localForce;
			forceValues.put(xCordinateNumOfNodeSec, totalForce);

			// Check for yCordNum
			int yCordinateNumOfNodeSec = secondNode.getyCordinateNum();
			earlierForce = forceValues.get(yCordinateNumOfNodeSec);
			localForce = forceVector[7];
			totalForce = earlierForce + localForce;
			forceValues.put(yCordinateNumOfNodeSec, totalForce);

			// Check for mzCordNum
			int mzCordinateNumOfNodeSec = secondNode.getMzCordinateNum();
			localForce = forceVector[11];
			earlierForce = forceValues.get(mzCordinateNumOfNodeSec);
			totalForce = localForce + earlierForce;
			forceValues.put(mzCordinateNumOfNodeSec, totalForce);
			
			// Check for zCordNum
			int zCordinateNumOfNodeSec = secondNode.getzCordinateNum();
			earlierForce = forceValues.get(zCordinateNumOfNodeSec);
			localForce = forceVector[8];
			totalForce = earlierForce + localForce;
			forceValues.put(zCordinateNumOfNodeSec, totalForce);
			
			// Check for mzCordNum
			int myCordinateNumOfNodeSec = secondNode.getMyCordinateNum();
			localForce = forceVector[10];
			earlierForce = forceValues.get(myCordinateNumOfNodeSec);
			totalForce = localForce + earlierForce;
			forceValues.put(myCordinateNumOfNodeSec, totalForce);
			
			// Check for mzCordNum
			int mxCordinateNumOfNodeSec = secondNode.getMxCordinateNum();
			localForce = forceVector[9];
			earlierForce = forceValues.get(mxCordinateNumOfNodeSec);
			totalForce = localForce + earlierForce;
			forceValues.put(mxCordinateNumOfNodeSec, totalForce);
		}

		@Override
		public List<Integer> getCoOrdinatedOfFreedom() {
			return coOrdinatesOfFreedom;
		}

		@Override
		public int getSizeOfVector() {
			int size = coOrdinatesOfFreedom.size();
			return size;
		}

		@Override
		public Map<Integer, Double> getVectorValues() {
			return vectorValues;
		}
		
		public double[] getForces() {
			return forceVector;
		}
	}

}

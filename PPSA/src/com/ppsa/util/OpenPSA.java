package com.ppsa.util;

import java.nio.file.Path;
import java.util.Map;

import com.ppsa.entity.StiffnessMatrixGlobalImpl;
import com.ppsa.exception.PPSAException;
import com.ppsa.exception.PPSAIOException;
import com.ppsa.math.MathHelper;
import com.ppsa.report.ReporterCalculationWebPageNew;
import com.psa.entity.CommonNode;
import com.psa.entity.FiniteElement;
import com.psa.entity.LoadCase;
import com.psa.entity.OneDBeamLocalRotations;
import com.psa.entity.SectionProperty;
import com.psa.entity.enums.OneDimFiniteElementType;
import com.psa.entity.enums.StructureType;
import com.psa.entity.impl.Node;
import com.psa.entity.impl.NodeResults;
import com.psa.entity.impl.OneDimFiniteElement;
import com.psa.entity.impl.Structure;
import com.psa.exception.PSAException;
import com.psa.exception.PSAStructureInstantiationException;
import com.psa.io.parser.FileParser;
import com.psa.io.parser.std.deprecated.STDFileParserOne;

import curves.fem.util.Type2Curve;

public class OpenPSA {

	public static Structure postProcessStructure(Path stdFilePathToRead)
			throws PPSAIOException, PPSAException {
		/*Path stdFilePathToRead = EnvFileIOUtil
				.getSTDFilePathToReadFromEnv(probName);*/
		FileParser parser = new STDFileParserOne();
		Structure structureEntity = null;
		try {
			
			//long nanoTime1 = System.nanoTime();
			
			structureEntity = parser.getStructure(stdFilePathToRead);

			//long nanoTime2 = System.nanoTime();
			/*System.out.println("TD (mS) Parser : "+(nanoTime2-nanoTime1)/1000000);*/

				//MeshGenerationUtil.generateMesh(structureEntity);
				
				/*long nanoTime3 = System.nanoTime();
				System.out.println("TD (mS) Mesher : "+(nanoTime3-nanoTime2)/1000000);*/
				
				CoOrdinateNumberingUtil.setCoOrdinateNumbersForStructure(structureEntity);

				MatrixGenerationUtil.setStiffnessMatrixToAllElements(structureEntity);

				new StiffnessMatrixGlobalImpl()
						.setGlobalStiffnessMatrix(structureEntity);

				GlobalStiffMatrixAssembler
						.assembleGlobalStiffnesMatrix(structureEntity);
				
				MatrixGenerationUtil.generateDisplacementVecor(structureEntity);
				
				MatrixGenerationUtil
						.generateGlobalForceVectorsForDifferentLoadCases(structureEntity);
				MatrixGenerationUtil
						.reducedGlobalStiffnessMatrix(structureEntity);

				MatrixGenerationUtil
						.performNodalDisplacementCalculation(structureEntity);

				//included in "performNodalDisplacementCalculation(structureEntity);"
				MatrixGenerationUtil
						.setNodalDisplacementToEntities(structureEntity);
				MatrixGenerationUtil
						.computeNodeReactionsAtSupport(structureEntity);
				MatrixGenerationUtil
						.computeSectionForcesOnMembers(structureEntity);

				MatrixGenerationUtil.fillBasicDataInObjects(structureEntity);
				structureEntity.setStructurePostProcessed(true);

				//long nanoTime11 = System.nanoTime();
				/*System.out.println("TD (mS) Total Processing : "+(nanoTime11-nanoTime1)/1000000);*/

			ReporterCalculationWebPageNew.generateWebReport(structureEntity,stdFilePathToRead);
			// PPSADataCopyUtil.savePostProcessedStructure(structureEntity);

		} catch (PSAException e) {
			e.printStackTrace();
			throw new PPSAException(e.getMessage());
		}
		return structureEntity;
	}

	public static double[] getSupportReactions(Structure structure,
			Integer loadCaseNum, int nodeNum) {
		LoadCase loadCase = structure.getLoadCase(loadCaseNum);
		Map<CommonNode , NodeResults> nodeResults = loadCase.getNodeResults();
		
		Node node = null;
		try {
			node = structure.getNode(nodeNum);
		} catch (PSAStructureInstantiationException e) {
			e.printStackTrace();
		}
		
		NodeResults nodeResult = nodeResults.get(node);
		
		Double xReaction = nodeResult.getxReaction();
		Double yReaction = nodeResult.getyReaction();
		Double zReaction = nodeResult.getzReaction();
		
		Double xMoment = nodeResult.getxMoment();
		Double yMoment = nodeResult.getyMoment();
		Double zMoment = nodeResult.getzMoment();
		
		double[] nodeReactions = new double[6];
		nodeReactions[0]=xReaction;
		nodeReactions[1]=yReaction;
		nodeReactions[2]=zReaction;
		
		nodeReactions[3]=xMoment;
		nodeReactions[4]=yMoment;
		nodeReactions[5]=zMoment;
		
		return nodeReactions;
	}

	public static void insertNode(Structure structure,
			OneDimFiniteElement oneDimFiniteElement, double distanceFromFirstNode) {
		// To-DO write code
		
	}
	
	public static double[] getIntermediateMemberForcesAtDistance(Structure structure,int memberId , double ratio , int loadCaseId) throws PPSAException {
		
		if(ratio<0 || ratio>1)
			throw new PPSAException("Ratio must be within 0.0 to 1.0");
		
		double sectionForces[] = new double[6];
		LoadCase loadCase = structure.getLoadCase(loadCaseId);
		FiniteElement beamElement = structure.getFiniteElementByNum(memberId);
		
		//Map<OneDimFiniteElementChild, OneDBeamEndMomentsEntity> beamEndMomentEntities = loadCase.getBeamEndMomentEntities();
		//OneDBeamEndMomentsEntity oneDBeamEndMomentsEntity = beamEndMomentEntities.get(beamElement);
		
		if(beamElement instanceof OneDimFiniteElement) {
			
			OneDimFiniteElement oneDimFiniteElement = (OneDimFiniteElement) beamElement;
			OneDimFiniteElementType finiteElementType = oneDimFiniteElement.getFiniteElementType();
			if(finiteElementType==OneDimFiniteElementType.TRUSS2D || finiteElementType==OneDimFiniteElementType.TRUSS3D) {
				double curveFEMAxialForceVal = Helper_OpenPSA.curveFEMAxial(loadCase,oneDimFiniteElement,ratio);
				sectionForces[0]+=curveFEMAxialForceVal;
				
				/*double curveMemLoadAxialVal = Helper_OpenPSA.curveMemberLoadAxial(loadCase,oneDimFiniteElement,ratio);
				sectionForces[0]+=curveMemLoadAxialVal;*/
				return sectionForces; 
			}

			double curveFemEndMomentsVal_MZ = Helper_OpenPSA.curveFemEndMoments_MZ(loadCase,oneDimFiniteElement,ratio);
			sectionForces[5]+=curveFemEndMomentsVal_MZ;

			double curveMemLoadMomentsVal_MZ = Helper_OpenPSA.curveMemberLoadMoments_MZ(loadCase,oneDimFiniteElement,ratio);
			sectionForces[5]+=curveMemLoadMomentsVal_MZ;

			double curveFEMSFVal = Helper_OpenPSA.curveFEMSF_FY(loadCase,oneDimFiniteElement,ratio);
			sectionForces[1]+=curveFEMSFVal;

			double curveMemLoadSFVal = Helper_OpenPSA.curveMemberLoadSF_FY(loadCase,oneDimFiniteElement,ratio);
			sectionForces[1]+=curveMemLoadSFVal;

			if (structure.getStructureType() == StructureType.SPACE) {
				double curveFEMSFVal_FZ = Helper_OpenPSA.curveFEMSF_FZ(
						loadCase, oneDimFiniteElement, ratio);
				sectionForces[2] += curveFEMSFVal_FZ;
				double curveMemLoadSFVal_FZ = Helper_OpenPSA
						.curveMemberLoadSF_FZ(loadCase, oneDimFiniteElement,
								ratio);
				sectionForces[2] += curveMemLoadSFVal_FZ;

				double curveFemEndMomentsVal_MY = Helper_OpenPSA.curveFemEndMoments_MY(loadCase,oneDimFiniteElement,ratio);
				sectionForces[4]+=curveFemEndMomentsVal_MY;
				
				double curveMemLoadMomentsVal_MY = Helper_OpenPSA.curveMemberLoadMoments_MY(loadCase,oneDimFiniteElement,ratio);
				sectionForces[4]+=curveMemLoadMomentsVal_MY;
				
				double curveFemEndTorsionVal = Helper_OpenPSA.curveFemEndTorsion(loadCase,oneDimFiniteElement,ratio);
				sectionForces[3]+=curveFemEndTorsionVal;
			}
			

			/**
			 * ToDo
			 */
			double curveFEMAxialForceVal = Helper_OpenPSA.curveFEMAxial(loadCase,oneDimFiniteElement,ratio);
			sectionForces[0]+=curveFEMAxialForceVal;
			
			double curveMemLoadAxialVal = Helper_OpenPSA.curveMemberLoadAxial(loadCase,oneDimFiniteElement,ratio);
			sectionForces[0]+=curveMemLoadAxialVal;
		} else {
			throw new PPSAException("Element "+memberId+" should be OneDimFiniteElement.");
		}

		return sectionForces;
	}

	/**
	 * Based on article 'Lecture : Bending (VI) — 
	 * - Methods of Moment-Area and Superposition' by 'Yubao Zhen'.
	 * Notations used are also as used in the article.
	 */
	public static double[] getIntermediateMemberTransDisplacements(Structure structure,int memberId , double ratio , int loadCaseId) throws PPSAException {
		
		if(ratio<0 || ratio>1)
			throw new PPSAException("Ratio must be within 0.0 to 1.0");
		
		LoadCase loadCase = structure.getLoadCase(loadCaseId);
		FiniteElement beamElement = structure.getFiniteElementByNum(memberId);


		//Map<OneDimFiniteElementChild, OneDBeamEndMomentsEntity> beamEndMomentEntities = loadCase.getBeamEndMomentEntities();
		//OneDBeamEndMomentsEntity oneDBeamEndMomentsEntity = beamEndMomentEntities.get(beamElement);

		if(beamElement instanceof OneDimFiniteElement) {
			OneDimFiniteElement oneDimFiniteElement = (OneDimFiniteElement) beamElement;
			OneDimFiniteElementType finiteElementType = oneDimFiniteElement.getFiniteElementType();
			if(finiteElementType==OneDimFiniteElementType.TRUSS2D || finiteElementType==OneDimFiniteElementType.TRUSS3D) {
				return getTransDisplacementForTrussElement(oneDimFiniteElement,ratio,loadCase);
			} else if(finiteElementType==OneDimFiniteElementType.BEAM3D) {
				return getTransDisplacementFor_BeamElement3D(oneDimFiniteElement,ratio,loadCase);
			} else if(finiteElementType==OneDimFiniteElementType.BEAM2D) {
				return getTransDisplacementFor_BeamElement2D(ratio, loadCase, oneDimFiniteElement);
			}
			
		} else {
			throw new PPSAException("Element "+memberId+" should be OneDimFiniteElement.");
		}
		return null;
	}

	private static double[] getTransDisplacementFor_BeamElement2D(double ratio, LoadCase loadCase,
			OneDimFiniteElement oneDimFiniteElement) {
		double transDisplacement[] = new double[5];
		double D1D2Total = 0;
		double inertialMomentZ = oneDimFiniteElement.getSectionProperty().getInertialMomentZ();
		double e = oneDimFiniteElement.getMaterialProperty().getE();
		double D1D2ByFemVal = Helper_OpenPSA.deflectionByCurveFemEndMoments_MZ(loadCase,oneDimFiniteElement,ratio,inertialMomentZ*e);
		D1D2Total+=D1D2ByFemVal;

		double D1D2ByMemLoads = Helper_OpenPSA.deflectionByCurveMemberLoadMoments(loadCase,oneDimFiniteElement,ratio,inertialMomentZ*e);
		D1D2Total+=D1D2ByMemLoads;

		Node firstNode = oneDimFiniteElement.getFirstNode();
		NodeResults nodeResults = loadCase.getNodeResults().get(firstNode);
		Double yDisplacement = nodeResults.getyDisplacement();
		Double xDisplacement = nodeResults.getxDisplacement();

		double multCos = MathHelper.resolveForceInY(1,oneDimFiniteElement.getAngleOfInclination());
		double multSin = MathHelper.resolveForceInX(1,oneDimFiniteElement.getAngleOfInclination());
		Double zRotation = nodeResults.getzRotation();
		Double finalDeflectionExclusivelyInternalLoads = getFinalDeflectionByMomentTwoExclusiveArea(ratio,
				oneDimFiniteElement, D1D2Total, zRotation);

		Node secondNode = oneDimFiniteElement.getSecondNode();
		NodeResults nodeResults2 = loadCase.getNodeResults().get(secondNode);
		Double yDisplacementSec = nodeResults2.getyDisplacement();
		Double xDisplacementSec = nodeResults2.getxDisplacement();

		/*Type2Curve type2Curve = new Type2Curve(yDisplacement, yDisplacementSec, lengthOfElement);
		double deflectionByBothNodesRelative = type2Curve.getValueAtASection(ratio);*/
		/*double yDispExclusiveFEMOne = -0.0017574855668031552;
		double yDispExclusiveFEMSec = -0.0017971637507417275;*/

		double curveFemEndMomentsValOne = Helper_OpenPSA.deflectionByCurveFemEndMoments_MZ(loadCase,oneDimFiniteElement,0,inertialMomentZ*e);
		Double finalDeflectionOnlyInternalNodeOne = getFinalDeflectionByMomentTwoExclusiveArea(0, oneDimFiniteElement, curveFemEndMomentsValOne, zRotation);
		double curveFemEndMomentsValSec = Helper_OpenPSA.deflectionByCurveFemEndMoments_MZ(loadCase,oneDimFiniteElement,1,inertialMomentZ*e);
		Double finalDeflectionOnlyInternalNodeTwo = getFinalDeflectionByMomentTwoExclusiveArea(1, oneDimFiniteElement, curveFemEndMomentsValSec, zRotation);
		
		// --- OBSERVATION CODE ---
		
		
		double yDispOnlyFEMOne = 0 - yDisplacement;//finalDeflectionOnlyInternalNodeOne*multCos - yDisplacement;
		//double yDispOnlyFEMSec = finalDeflectionOnlyInternalNodeTwo*multCos - yDisplacementSec;
		

		Double yDispVals = yDispOnlyFEMOne;//-(ratio*(yDispOnlyFEMOne-yDispOnlyFEMSec));
		
		
		// --- OBSERVATION CODE END ---
		
		/*if(oneDimFiniteElement.getElementNumber()==1)
			System.out.println("Ratio = "+ratio+", yDispVals = "+finalDeflectionOnlyInternalNodeTwo);*/

		double xDispExclusiveFEMOne = -finalDeflectionOnlyInternalNodeOne*(multSin) - xDisplacement;
		double xDispExclusiveFEMSec = -finalDeflectionOnlyInternalNodeTwo*(multSin) - xDisplacementSec;

		double xDispVals = 0;
		xDispVals = xDispExclusiveFEMOne-(ratio*(xDispExclusiveFEMOne-xDispExclusiveFEMSec));
		/**
		 * Local deflections.
		 */
		{
			double interpolatedTriangleVal=ratio*finalDeflectionOnlyInternalNodeTwo;
			/**
			 * 'finalDeflectionExclusivelyInternalLoads' is in localY direction itself.
			 * So no need to use multCos.
			 */
			transDisplacement[3]=finalDeflectionExclusivelyInternalLoads-interpolatedTriangleVal;
		}
		
		
		/**
		 * Global deflections.
		 */
		{
			double global_Y_Disp = (finalDeflectionExclusivelyInternalLoads * multCos) - (yDispVals);
			transDisplacement[1] = global_Y_Disp;

			double global_X_Disp = -(finalDeflectionExclusivelyInternalLoads * multSin) - (xDispVals);
			transDisplacement[0] = global_X_Disp;
		}
			return transDisplacement;
	}

	private static double[] getTransDisplacementFor_BeamElement3D(OneDimFiniteElement oneDimFiniteElement,
			double ratio, LoadCase loadCase) throws PPSAException {
			if(ratio<0 || ratio>1)
				throw new PPSAException("Ratio must be within 0.0 to 1.0");


			OneDBeamLocalRotations oneDBeamLocalRotations = loadCase.getBeamEndLocalRotations().get(oneDimFiniteElement);
			double[] localRotations_NodeOne = oneDBeamLocalRotations.getLocalRotations_NodeOne();
			double[] localRotations_NodeTwo = oneDBeamLocalRotations.getLocalRotations_NodeTwo();
			

			double transDisplacement[] = new double[5];
			double D1D2Total_LocalY = 0;
			double D1D2Total_LocalZ = 0;
			SectionProperty sectionProperty = oneDimFiniteElement.getSectionProperty();
			double inertialMomentZ = sectionProperty.getInertialMomentZ();
			double inertialMomentY = sectionProperty.getInertialMomentY();
			double e = oneDimFiniteElement.getMaterialProperty().getE();

			double D1D2ByFemVal_LocalY = Helper_OpenPSA.deflectionByCurveFemEndMoments_MZ(loadCase,oneDimFiniteElement,ratio,inertialMomentZ*e);
			D1D2Total_LocalY+=D1D2ByFemVal_LocalY;
			
			double D1D2ByFemVal_LocalZ = Helper_OpenPSA.deflectionByCurveFemEndMoments_MY(loadCase,oneDimFiniteElement,ratio,inertialMomentY*e);
			D1D2Total_LocalZ+=D1D2ByFemVal_LocalZ;

			double[] D1D2ByMemLoads = Helper_OpenPSA.deflectionByCurveMemberLoadMoments_Space(loadCase,oneDimFiniteElement,ratio,inertialMomentZ*e,inertialMomentY*e);
			double D1D2ByMemLoads_LocalY = D1D2ByMemLoads[0];
			D1D2Total_LocalY+=D1D2ByMemLoads_LocalY;
			double D1D2ByMemLoads_LocalZ = - D1D2ByMemLoads[1];
			D1D2Total_LocalZ+=D1D2ByMemLoads_LocalZ;
//			double D1D2ByMemLoads_LocalZ = Helper_OpenPSA.deflectionByCurveMemberLoadMoments_Space(loadCase,oneDimFiniteElement,ratio,inertialMomentY*e);
//			D1D2Total_LocalZ+=D1D2ByMemLoads_LocalZ;

			Node firstNode = oneDimFiniteElement.getFirstNode();
			NodeResults nodeResults = loadCase.getNodeResults().get(firstNode);
			Double yDisplacement = nodeResults.getyDisplacement();
			Double xDisplacement = nodeResults.getxDisplacement();
			Double zDisplacement = nodeResults.getzDisplacement();
			

//			double multCos = MathHelper.resolveForceInY(1,oneDimFiniteElement.getAngleOfInclination());
//			double multSin = MathHelper.resolveForceInX(1,oneDimFiniteElement.getAngleOfInclination());
			Double zRotationLocal = localRotations_NodeOne[2];
			Double yRotationLocal = localRotations_NodeOne[1];
			Double finalDeflectionExclusivelyInternalLoads_LocalY = getFinalDeflectionByMomentTwoExclusiveArea(ratio,
					oneDimFiniteElement, D1D2Total_LocalY, zRotationLocal);
			Double finalDeflectionExclusivelyInternalLoads_LocalZ = getFinalDeflectionByMomentTwoExclusiveArea(ratio,
					oneDimFiniteElement, D1D2Total_LocalZ, yRotationLocal);

			Node secondNode = oneDimFiniteElement.getSecondNode();
			NodeResults nodeResults2 = loadCase.getNodeResults().get(secondNode);
			Double yDisplacementSec = nodeResults2.getyDisplacement();
			Double xDisplacementSec = nodeResults2.getxDisplacement();
			Double zDisplacementSec = nodeResults2.getzDisplacement();

			/*Type2Curve type2Curve = new Type2Curve(yDisplacement, yDisplacementSec, lengthOfElement);
			double deflectionByBothNodesRelative = type2Curve.getValueAtASection(ratio);*/
			/*double yDispExclusiveFEMOne = -0.0017574855668031552;
			double yDispExclusiveFEMSec = -0.0017971637507417275;*/

			double curveFemEndMomentsValOne_LocalY = Helper_OpenPSA.deflectionByCurveFemEndMoments_MZ(loadCase,oneDimFiniteElement,0,inertialMomentZ*e);
			Double finalDeflectionOnlyInternalNodeOne_LocalY = getFinalDeflectionByMomentTwoExclusiveArea(0, oneDimFiniteElement, curveFemEndMomentsValOne_LocalY, zRotationLocal);
			double curveFemEndMomentsValSec_LocalY = Helper_OpenPSA.deflectionByCurveFemEndMoments_MZ(loadCase,oneDimFiniteElement,1,inertialMomentZ*e);
			Double finalDeflectionOnlyInternalNodeTwo_LocalY = getFinalDeflectionByMomentTwoExclusiveArea(1, oneDimFiniteElement, curveFemEndMomentsValSec_LocalY, zRotationLocal);
			
			double curveFemEndMomentsValOne_LocalZ = Helper_OpenPSA.deflectionByCurveFemEndMoments_MY(loadCase,oneDimFiniteElement,0,inertialMomentY*e);
			Double finalDeflectionOnlyInternalNodeOne_LocalZ = getFinalDeflectionByMomentTwoExclusiveArea(0, oneDimFiniteElement, curveFemEndMomentsValOne_LocalZ, yRotationLocal);
			double curveFemEndMomentsValSec_LocalZ = Helper_OpenPSA.deflectionByCurveFemEndMoments_MY(loadCase,oneDimFiniteElement,1,inertialMomentY*e);
			Double finalDeflectionOnlyInternalNodeTwo_LocalZ = getFinalDeflectionByMomentTwoExclusiveArea(1, oneDimFiniteElement, curveFemEndMomentsValSec_LocalZ, yRotationLocal);

			double[] localDeflections = new double[12];
			localDeflections[1] = finalDeflectionOnlyInternalNodeOne_LocalY;
			localDeflections[7] = finalDeflectionOnlyInternalNodeTwo_LocalY;
			localDeflections[2] = -finalDeflectionOnlyInternalNodeOne_LocalZ;
			localDeflections[8] = -finalDeflectionOnlyInternalNodeTwo_LocalZ;

			double[] globalDeflections = MathHelper.resolveToToGlobalFomLocal(localDeflections, oneDimFiniteElement);

			double yDispOnlyFEMOne = globalDeflections[1] - yDisplacement;
			double yDispOnlyFEMSec = globalDeflections[7] - yDisplacementSec;

			Double yDispVals = yDispOnlyFEMOne-(ratio*(yDispOnlyFEMOne-yDispOnlyFEMSec));

			double xDispExclusiveFEMOne = (globalDeflections[0] - xDisplacement);
			double xDispExclusiveFEMSec = (globalDeflections[6] - xDisplacementSec);

			double xDispVals = xDispExclusiveFEMOne-(ratio*(xDispExclusiveFEMOne-xDispExclusiveFEMSec));
			
			double zDispExclusiveFEMOne = (globalDeflections[2] - zDisplacement);
			double zDispExclusiveFEMSec = (globalDeflections[8] - zDisplacementSec);
			
			double zDispVals = zDispExclusiveFEMOne-(ratio*(zDispExclusiveFEMOne-zDispExclusiveFEMSec));
			/**
			 * Local deflections.
			 */
			{
				double interpolatedTriangleVal=ratio*finalDeflectionOnlyInternalNodeTwo_LocalY;
				/**
				 * 'finalDeflectionExclusivelyInternalLoads' is in localY direction itself.
				 * So no need to use multCos.
				 */
				transDisplacement[3]=finalDeflectionExclusivelyInternalLoads_LocalY-interpolatedTriangleVal;
				// Local-Z
				interpolatedTriangleVal=ratio*finalDeflectionOnlyInternalNodeTwo_LocalZ;
				/**
				 * 'finalDeflectionExclusivelyInternalLoads' is in localY direction itself.
				 * So no need to use multCos.
				 */
				transDisplacement[4]=finalDeflectionExclusivelyInternalLoads_LocalZ-interpolatedTriangleVal;
			}

			/**
			 * Global deflections.
			 */
			{
				double[] localDeflectionsExclusivelyInternal = new double[12];
				localDeflectionsExclusivelyInternal[1] = finalDeflectionExclusivelyInternalLoads_LocalY;
				localDeflectionsExclusivelyInternal[2] = -finalDeflectionExclusivelyInternalLoads_LocalZ;
				double[] globalDefsExclusivelyInternal = MathHelper.resolveToToGlobalFomLocal(localDeflectionsExclusivelyInternal, oneDimFiniteElement);

				double global_Y_Disp = (globalDefsExclusivelyInternal[1]) - (yDispVals);
				transDisplacement[1] = global_Y_Disp;

				double global_X_Disp = (globalDefsExclusivelyInternal[0]) - (xDispVals);
				transDisplacement[0] = global_X_Disp;
				
				double global_Z_Disp = (globalDefsExclusivelyInternal[2]) - (zDispVals);
				transDisplacement[2] = global_Z_Disp;
			}
			return transDisplacement;
	}

	private static double[] getTransDisplacementForTrussElement(OneDimFiniteElement oneDimFiniteElement,
			double ratio, LoadCase loadCase) {
		double transDisplacement[] = new double[5];
		double lengthOfElement = oneDimFiniteElement.getLengthOfElement();
		Node firstNode = oneDimFiniteElement.getFirstNode();
		Map<CommonNode, NodeResults> nodeResultsList = loadCase.getNodeResults();
		NodeResults nodeResults = nodeResultsList.get(firstNode);
		Double yDisplacement = nodeResults.getyDisplacement();
		Double xDisplacement = nodeResults.getxDisplacement();
		Double zDisplacement = nodeResults.getzDisplacement();
		
		Node secondNode = oneDimFiniteElement.getSecondNode();
		NodeResults nodeResults2 = nodeResultsList.get(secondNode);
		Double yDisplacementSec = nodeResults2.getyDisplacement();
		Double xDisplacementSec = nodeResults2.getxDisplacement();
		Double zDisplacementSec = nodeResults2.getzDisplacement();
		
		Type2Curve type2Curve_GY = new Type2Curve(yDisplacement, -yDisplacementSec, lengthOfElement);
		transDisplacement[1] = type2Curve_GY.getValueAtASection(ratio*lengthOfElement);
		
		Type2Curve type2Curve_GX = new Type2Curve(xDisplacement, -xDisplacementSec, lengthOfElement);
		transDisplacement[0] = type2Curve_GX.getValueAtASection(ratio*lengthOfElement);

		Type2Curve type2Curve_GZ = new Type2Curve(zDisplacement, -zDisplacementSec, lengthOfElement);
		transDisplacement[2] = type2Curve_GZ.getValueAtASection(ratio*lengthOfElement);
		
		return transDisplacement;
	}

	private static Double getFinalDeflectionByMomentTwoExclusiveArea(double ratio,
			OneDimFiniteElement oneDimFiniteElement,
			double D2D1, double zRotation) {
		double lengthOfElement = oneDimFiniteElement.getLengthOfElement();
		double a = lengthOfElement*ratio;
		Double DD1 = zRotation * a;
		//changed here
		//if(ratio!=0 && ratio!=1.0 && oneDimFiniteElement.getElementNumber()==2)
		/*if(ratio==1 && oneDimFiniteElement.getElementNumber()==2)
			System.out.println("DD1 (zRot*a) end= "+"Ele = "+oneDimFiniteElement+" , ratio ="+ratio+" , "+DD1);*/
		//if(ratio!=0 && ratio!=1.0 && oneDimFiniteElement.getElementNumber()==2)
		/*if(ratio==0 && oneDimFiniteElement.getElementNumber()==2)
			System.out.println("D2D1 final = "+ratio+" , "+D2D1);*/
		
		Double finalDeflection = DD1-D2D1;
		//if(ratio!=0 && ratio!=1.0 && oneDimFiniteElement.getElementNumber()==2)
		/*if(ratio==0 && oneDimFiniteElement.getElementNumber()==2)
			System.out.println("finalDeflection = "+ratio+" , "+finalDeflection);*/
		return finalDeflection;
	}

	public static double[] getNodeDisplacements(Structure structure,
			Integer loadCaseNum, int nodeNum) throws PSAStructureInstantiationException{
		LoadCase loadCase = structure.getLoadCase(loadCaseNum);
		Node node = structure.getNode(nodeNum);
		NodeResults nodeResults = loadCase.getNodeResults().get(node);
		
		double[] nodeResultVals = new double[6];
		nodeResultVals[0]= nodeResults.getxDisplacement();
		nodeResultVals[1]= nodeResults.getyDisplacement();
		if(nodeResults.getzDisplacement()!=null)
			nodeResultVals[2]= nodeResults.getzDisplacement();
		
		nodeResultVals[3]= nodeResults.getxRotation();
		nodeResultVals[4]= nodeResults.getyRotation();
		nodeResultVals[5]= nodeResults.getzRotation();
		return nodeResultVals;
	}

}

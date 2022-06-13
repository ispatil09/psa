package com.ppsa.util;

import java.util.Map;
import java.util.Set;

import com.ppsa.math.MathHelper;
import com.psa.entity.LoadCase;
import com.psa.entity.LoadCase.MemberLoad;
import com.psa.entity.LoadCase.MemberLoad.ConcentratedForce;
import com.psa.entity.LoadCase.MemberLoad.UniformForce;
import com.psa.entity.OneDAxialForceEntity;
import com.psa.entity.OneDBeamEndMomentsEntity;
import com.psa.entity.OneDBeamEndShearForceEntity;
import com.psa.entity.OneDBeamEndTorsionEntity;
import com.psa.entity.enums.ConMemberLoadDirection;
import com.psa.entity.enums.MemberLoadType;
import com.psa.entity.enums.OneDimFiniteElementType;
import com.psa.entity.enums.UniMemberLoadDirection;
import com.psa.entity.impl.OneDimFiniteElement;

import curves.fem.util.Type1Curve;
import curves.fem.util.Type1pCurve;
import curves.fem.util.Type2Curve;
import curves.fem.util.Type2CurvePartial;
import curves.fem.util.Type3CurvePartial_LX;
import curves.fem.util.Type4pCurve;

public class Helper_OpenPSA {
	public static double curveMemberLoadAxial(LoadCase loadCase,
			OneDimFiniteElement oneDimFiniteElement, double ratio) {
		Set<MemberLoad> memberLoadsAssociated = loadCase.getAllMemberLoadsForThisFE(oneDimFiniteElement);
		double lengthOfElement = oneDimFiniteElement.getLengthOfElement();
		OneDimFiniteElementType finiteElementType = oneDimFiniteElement.getFiniteElementType();
		double xVal = ratio*lengthOfElement;

		double sectionAxialDueToUDL = 0;
		for (MemberLoad memberLoad : memberLoadsAssociated) {
			if(memberLoad.getMemberLoadType()==MemberLoadType.UNI) {
				UniformForce udlLoad = memberLoad.getUniformForce();
				double udlForceVal = udlLoad.getForceVal();
				double forceValResolvedOn_LX = 0;
				if(finiteElementType!=OneDimFiniteElementType.BEAM3D) {
					forceValResolvedOn_LX = getLoadInIn_LX(oneDimFiniteElement,
						memberLoad, udlLoad, udlForceVal);
				} else {
					forceValResolvedOn_LX = getLoadInIn_LX_SpaceFrame(oneDimFiniteElement,
							memberLoad, udlLoad, udlForceVal);
				}
				sectionAxialDueToUDL += processAxialforUni_LX(
						oneDimFiniteElement, lengthOfElement, xVal, udlLoad, forceValResolvedOn_LX);
			} else if(memberLoad.getMemberLoadType()==MemberLoadType.CON) {
				ConcentratedForce concentratedForce = memberLoad.getConcentratedForce();
				double forceVal = concentratedForce.getForceVal();
				double a = concentratedForce.getD1();
				double b = lengthOfElement-a;
				double angleOfInclination = oneDimFiniteElement.getAngleOfInclination();
				double resolveForceInX = 0;
				if(concentratedForce.getLoadDirection() == ConMemberLoadDirection.X) {

					// Getting peak moment value.
					resolveForceInX = forceVal;
					
				} else if(concentratedForce.getLoadDirection() == ConMemberLoadDirection.GY) {
					if(finiteElementType!=OneDimFiniteElementType.BEAM3D) {
						resolveForceInX = MathHelper.resolveForceInX(forceVal, angleOfInclination);
					} else {
						double[] globalForceVals = new double[12];
						globalForceVals[1] = forceVal;
						double[] resolveForcesToLocal = MathHelper.resolveForcesToLocal(globalForceVals, oneDimFiniteElement);
						resolveForceInX = resolveForcesToLocal[0];
					}
				} else if(concentratedForce.getLoadDirection() == ConMemberLoadDirection.GX) {
					if(finiteElementType!=OneDimFiniteElementType.BEAM3D) {
						resolveForceInX = MathHelper.resolveForceInY(forceVal, angleOfInclination);
					} else {
						double[] globalForceVals = new double[12];
						globalForceVals[0] = forceVal;
						double[] resolveForcesToLocal = MathHelper.resolveForcesToLocal(globalForceVals, oneDimFiniteElement);
						resolveForceInX = resolveForcesToLocal[0];
					}
				} else if(concentratedForce.getLoadDirection() == ConMemberLoadDirection.GZ) {
					if(finiteElementType!=OneDimFiniteElementType.BEAM3D) {
						resolveForceInX = MathHelper.resolveForceInY(forceVal, angleOfInclination);
					} else {
						double[] globalForceVals = new double[12];
						globalForceVals[2] = forceVal;
						double[] resolveForcesToLocal = MathHelper.resolveForcesToLocal(globalForceVals, oneDimFiniteElement);
						resolveForceInX = resolveForcesToLocal[0];
					}
				}
				sectionAxialDueToUDL+= processAxialforCon_LX(lengthOfElement,
						xVal, resolveForceInX, a, b);
			}
		}
		return sectionAxialDueToUDL;
	}

	private static double getLoadInIn_LX_SpaceFrame(
			OneDimFiniteElement oneDimFiniteElement, MemberLoad memberLoad,
			UniformForce udlLoad, double udlForceVal) {
		double forceValResolvedOn_LX = 0;
		double forceVal = udlLoad.getForceVal();
		if(udlLoad.getLoadDirection() == UniMemberLoadDirection.X) {
			forceValResolvedOn_LX = udlForceVal;
			return forceValResolvedOn_LX;
		} else if(memberLoad.getUniformForce().getLoadDirection() == UniMemberLoadDirection.GX) {
			double[] globalForceVals = new double[12];
			globalForceVals[0] = forceVal;
			double[] resolveForcesToLocal = MathHelper.resolveForcesToLocal(globalForceVals,
					oneDimFiniteElement);
			forceValResolvedOn_LX = resolveForcesToLocal[0];
			return forceValResolvedOn_LX;
			
		} else if(memberLoad.getUniformForce().getLoadDirection() == UniMemberLoadDirection.GY) {
			double[] globalForceVals = new double[12];
			globalForceVals[1] = forceVal;
			double[] resolveForcesToLocal = MathHelper.resolveForcesToLocal(globalForceVals,
					oneDimFiniteElement);
			forceValResolvedOn_LX = resolveForcesToLocal[0];
			return forceValResolvedOn_LX;
			
		} else if(memberLoad.getUniformForce().getLoadDirection() == UniMemberLoadDirection.PX) {

			// Below statement converts PX load to GX value
			double forceValResolvedOnXFirst = MathHelper.getGlobalForceFromProjectedLength_PX(udlLoad, oneDimFiniteElement);

			double[] globalForceVals = new double[12];
			globalForceVals[0] = forceValResolvedOnXFirst;
			double[] resolveForcesToLocal = MathHelper.resolveForcesToLocal(globalForceVals,
					oneDimFiniteElement);
			forceValResolvedOn_LX = resolveForcesToLocal[0];
			return forceValResolvedOn_LX;
			
		} else if(memberLoad.getUniformForce().getLoadDirection() == UniMemberLoadDirection.PY) {
			
			double forceValResolvedOnYFirst = MathHelper.getGlobalForceFromProjectedLength_PY(udlLoad, oneDimFiniteElement);

			double[] globalForceVals = new double[12];
			globalForceVals[1] = forceValResolvedOnYFirst;
			double[] resolveForcesToLocal = MathHelper.resolveForcesToLocal(globalForceVals,
					oneDimFiniteElement);
			forceValResolvedOn_LX = resolveForcesToLocal[0];
			return forceValResolvedOn_LX;
		} else if(memberLoad.getUniformForce().getLoadDirection() == UniMemberLoadDirection.GZ) {
			double[] globalForceVals = new double[12];
			globalForceVals[2] = forceVal;
			double[] resolveForcesToLocal = MathHelper.resolveForcesToLocal(globalForceVals,
					oneDimFiniteElement);
			forceValResolvedOn_LX = resolveForcesToLocal[0];
			return forceValResolvedOn_LX;
			
		} else if(memberLoad.getUniformForce().getLoadDirection() == UniMemberLoadDirection.PZ) {
			
			double forceValResolvedOnYFirst = MathHelper.getGlobalForceFromProjectedLength_PZ(udlLoad, oneDimFiniteElement);

			double[] globalForceVals = new double[12];
			globalForceVals[2] = forceValResolvedOnYFirst;
			double[] resolveForcesToLocal = MathHelper.resolveForcesToLocal(globalForceVals,
					oneDimFiniteElement);
			forceValResolvedOn_LX = resolveForcesToLocal[0];
			return forceValResolvedOn_LX;
		}
		return 0;
	}

	private static double getLoadInIn_LX(
			OneDimFiniteElement oneDimFiniteElement, MemberLoad memberLoad,
			UniformForce udlLoad, double udlForceVal) {
		double forceValResolvedOn_LX = 0;
		if(udlLoad.getLoadDirection() == UniMemberLoadDirection.X) {
			forceValResolvedOn_LX = udlForceVal;
			return forceValResolvedOn_LX;
		} else if(memberLoad.getUniformForce().getLoadDirection() == UniMemberLoadDirection.GX) {
			forceValResolvedOn_LX = MathHelper.resolveForceInY(udlForceVal,
					oneDimFiniteElement.getAngleOfInclination());
			return forceValResolvedOn_LX;
			
		} else if(memberLoad.getUniformForce().getLoadDirection() == UniMemberLoadDirection.GY) {

			forceValResolvedOn_LX = MathHelper.resolveForceInX(udlForceVal,
					oneDimFiniteElement.getAngleOfInclination());
			return forceValResolvedOn_LX;
			
		} else if(memberLoad.getUniformForce().getLoadDirection() == UniMemberLoadDirection.PX) {

			// Below statement converts PX load to GX value
			double forceValResolvedOnXFirst = MathHelper.resolveForceInX(udlForceVal,
					MathHelper.getMod(oneDimFiniteElement
							.getAngleOfInclination()));
			forceValResolvedOn_LX = MathHelper.resolveForceInY(forceValResolvedOnXFirst,
					oneDimFiniteElement.getAngleOfInclination());
			return forceValResolvedOn_LX;
			
		} else if(memberLoad.getUniformForce().getLoadDirection() == UniMemberLoadDirection.PY) {
			
			double forceValResolvedOnYFirst = MathHelper.resolveForceInY(udlForceVal,
					MathHelper.getMod(oneDimFiniteElement
							.getAngleOfInclination()));
			forceValResolvedOn_LX = MathHelper.resolveForceInX(forceValResolvedOnYFirst,
					oneDimFiniteElement.getAngleOfInclination());
			
			return forceValResolvedOn_LX;
		}
		return forceValResolvedOn_LX;
	}

	private static double processAxialforUni_LX(
			OneDimFiniteElement oneDimFiniteElement, double lengthOfElement,
			double xVal, UniformForce udlLoad,
			double udlForceVal) {

		double elementLength = oneDimFiniteElement.getLengthOfElement();

		double rangeBegin = udlLoad.getRangeBegin();
		double rangeEnd = udlLoad.getRangeEnd();
		if (!udlLoad.isPatchLoad())
			rangeEnd = oneDimFiniteElement.getLengthOfElement();

		double totalLenOfForce = rangeEnd - rangeBegin;
		double resultantTotalForce = totalLenOfForce * udlForceVal;
		double resultantActLocation = rangeBegin + (totalLenOfForce / 2);
		double resultantNodalForceOnFirstNode = resultantTotalForce
				* (elementLength - resultantActLocation) / elementLength;
		double resultantNodalForceOnSecondNode = resultantTotalForce
				* resultantActLocation / elementLength;

		Type2Curve type2Curve = new Type2Curve(-resultantNodalForceOnFirstNode,
				-resultantNodalForceOnSecondNode, lengthOfElement);
		double valueAtASection = type2Curve.getValueAtASection(xVal);

		return valueAtASection;
	}

	private static double processAxialforCon_LX(double lengthOfElement,
			double xVal, double forceVal,
			double a, double b) {
		
		double resultantNodalForceOnFirstNodeLocalX = forceVal*b/lengthOfElement;
		double resultantNodalForceOnSecondNodeLocalX = forceVal*a/lengthOfElement;
		
		Type2Curve type2Curve = new Type2Curve(-resultantNodalForceOnFirstNodeLocalX, -resultantNodalForceOnSecondNodeLocalX, lengthOfElement);
		double valueAtASection = type2Curve.getValueAtASection(xVal);

		return valueAtASection;
	}

	public static double curveFEMAxial(LoadCase loadCase,
			OneDimFiniteElement oneDimFiniteElement, double ratio) {

		Map<OneDimFiniteElement, OneDAxialForceEntity> axialForceEntities = loadCase.getAxialForceEntities();
		OneDAxialForceEntity oneDAxialForceEntity = axialForceEntities.get(oneDimFiniteElement);
		double axialForce = oneDAxialForceEntity.getAxialForce();
		
		return axialForce;
	
	}

	public static double curveFEMSF_FY(LoadCase loadCase,
			OneDimFiniteElement oneDimFiniteElement, double ratio) {
		Map<OneDimFiniteElement, OneDBeamEndShearForceEntity> beamEndShearForceEntities = loadCase.getBeamEndShearForceEntities();
		OneDBeamEndShearForceEntity oneDBeamEndShearForceEntity = beamEndShearForceEntities.get(oneDimFiniteElement);
		double sfFirstNode = oneDBeamEndShearForceEntity.getShearForce_FY(oneDimFiniteElement.getFirstNode());
		//double sfSecondNode = oneDBeamEndShearForceEntity.getShearForce(oneDimFiniteElement.getSecondNode());
		
		double lengthOfElement = oneDimFiniteElement.getLengthOfElement();
		
		Type1Curve type1Curve = new Type1Curve(lengthOfElement, sfFirstNode);

		//double sectionAtDistance = lengthOfElement * ratio;

		double sfAtRatioSection = type1Curve.getValueAtASection();

		//double momentAtRatioSection = performMomentInterpolation(lengthOfElement,sectionAtDistance,momentFirstNode,momentSecondNode);
		return sfAtRatioSection;
	}

	public static double curveMemberLoadSF_FY(LoadCase loadCase,
			OneDimFiniteElement oneDimFiniteElement, double ratio) {
		Set<MemberLoad> memberLoadsAssociated = loadCase.getAllMemberLoadsForThisFE(oneDimFiniteElement);
		double lengthOfElement = oneDimFiniteElement.getLengthOfElement();
		double xVal = ratio*lengthOfElement;
		
		double sectionSFDueToUDL = 0;
		for (MemberLoad memberLoad : memberLoadsAssociated) {
			OneDimFiniteElementType finiteElementType = oneDimFiniteElement.getFiniteElementType();
			if(memberLoad.getMemberLoadType()==MemberLoadType.UNI) {
				UniformForce udlLoad = memberLoad.getUniformForce();
				double forceVal = udlLoad.getForceVal();
				//UniformForce uniformForce = memberLoad.getUniformForce();
				double forceValResolvedOnY = 0;
				if(udlLoad.getLoadDirection() == UniMemberLoadDirection.Y) {
					forceValResolvedOnY = forceVal;
				} else if(memberLoad.getUniformForce().getLoadDirection() == UniMemberLoadDirection.GX) {
					if(finiteElementType==OneDimFiniteElementType.BEAM2D) {
						forceValResolvedOnY = MathHelper.resolveForceInX(forceVal,
							-oneDimFiniteElement.getAngleOfInclination());
					} else {
						double[] globalForceVals = new double[12];
						globalForceVals[0] = forceVal;
						double[] resolveForcesToLocal = MathHelper.resolveForcesToLocal(globalForceVals, oneDimFiniteElement);
						forceValResolvedOnY = resolveForcesToLocal[1];
					}
					
				} else if(memberLoad.getUniformForce().getLoadDirection() == UniMemberLoadDirection.GY) {
					if(finiteElementType==OneDimFiniteElementType.BEAM2D) {
						forceValResolvedOnY = MathHelper.resolveForceInY(forceVal,
							-oneDimFiniteElement.getAngleOfInclination());
					} else {
						double[] globalForceVals = new double[12];
						globalForceVals[1] = forceVal;
						double[] resolveForcesToLocal = MathHelper.resolveForcesToLocal(globalForceVals, oneDimFiniteElement);
						forceValResolvedOnY = resolveForcesToLocal[1];
					}
					
				} else if(memberLoad.getUniformForce().getLoadDirection() == UniMemberLoadDirection.GZ) {
					if(finiteElementType==OneDimFiniteElementType.BEAM2D) {
						throw new RuntimeException("No GZ for plane frames");
					} else {
						double[] globalForceVals = new double[12];
						globalForceVals[2] = forceVal;
						double[] resolveForcesToLocal = MathHelper.resolveForcesToLocal(globalForceVals, oneDimFiniteElement);
						forceValResolvedOnY = resolveForcesToLocal[1];
					}
					
				} else if(memberLoad.getUniformForce().getLoadDirection() == UniMemberLoadDirection.PX) {

					if(finiteElementType==OneDimFiniteElementType.BEAM2D) {
					// Below statement converts PX load to GX value
					double forceValResolvedOnXFirst = MathHelper.resolveForceInX(forceVal,
							MathHelper.getMod(oneDimFiniteElement
									.getAngleOfInclination()));
					forceValResolvedOnY = MathHelper.resolveForceInX(forceValResolvedOnXFirst,
							-oneDimFiniteElement.getAngleOfInclination());
					} else {
						double globalForceFromProjectedLength_PX = MathHelper.getGlobalForceFromProjectedLength_PX(udlLoad, oneDimFiniteElement);
						double[] globalForceVals = new double[12];
						globalForceVals[0] = globalForceFromProjectedLength_PX;
						double[] resolveForcesToLocal = MathHelper.resolveForcesToLocal(globalForceVals, oneDimFiniteElement);
						forceValResolvedOnY = resolveForcesToLocal[1];
					}

				} else if(memberLoad.getUniformForce().getLoadDirection() == UniMemberLoadDirection.PY) {
					if(finiteElementType==OneDimFiniteElementType.BEAM2D) {
					double forceValResolvedOnYFirst = MathHelper.resolveForceInY(forceVal,
							MathHelper.getMod(oneDimFiniteElement
									.getAngleOfInclination()));
					forceValResolvedOnY = MathHelper.resolveForceInY(forceValResolvedOnYFirst,
							-oneDimFiniteElement.getAngleOfInclination());
					} else {
						double globalForceFromProjectedLength_PY = MathHelper.getGlobalForceFromProjectedLength_PY(udlLoad, oneDimFiniteElement);
						double[] globalForceVals = new double[12];
						globalForceVals[1] = globalForceFromProjectedLength_PY;
						double[] resolveForcesToLocal = MathHelper.resolveForcesToLocal(globalForceVals, oneDimFiniteElement);
						forceValResolvedOnY = resolveForcesToLocal[1];
					}
				} else if(memberLoad.getUniformForce().getLoadDirection() == UniMemberLoadDirection.PZ) {
					if(finiteElementType==OneDimFiniteElementType.BEAM2D) {
					throw new RuntimeException("No PZ for plane frame..");
					} else {
						double globalForceFromProjectedLength_PZ = MathHelper.getGlobalForceFromProjectedLength_PZ(udlLoad, oneDimFiniteElement);
						double[] globalForceVals = new double[12];
						globalForceVals[2] = globalForceFromProjectedLength_PZ;
						double[] resolveForcesToLocal = MathHelper.resolveForcesToLocal(globalForceVals, oneDimFiniteElement);
						forceValResolvedOnY = resolveForcesToLocal[1];
					}
				}
				sectionSFDueToUDL = processSectionSFforUni_LY(oneDimFiniteElement, lengthOfElement, xVal, sectionSFDueToUDL, udlLoad, forceValResolvedOnY);
			} else if(memberLoad.getMemberLoadType()==MemberLoadType.CON) {
				ConcentratedForce concentratedForce = memberLoad.getConcentratedForce();
				double forceVal = concentratedForce.getForceVal();
				double a = concentratedForce.getD1();
				double b = lengthOfElement-a;
				double angleOfInclination = oneDimFiniteElement.getAngleOfInclination();
				double resolveForceInY = 0;
				ConMemberLoadDirection loadDirection = concentratedForce.getLoadDirection();
				if(loadDirection == ConMemberLoadDirection.Y) {
					resolveForceInY = forceVal;
				} else if(loadDirection == ConMemberLoadDirection.GY) {
					if(finiteElementType==OneDimFiniteElementType.BEAM2D) {
						resolveForceInY = MathHelper.resolveForceInY(forceVal, angleOfInclination);
					} else {
						double[] globalForceVals = new double[12];
						globalForceVals[1] = forceVal;
						double[] resolveForcesToLocal = MathHelper.resolveForcesToLocal(globalForceVals, oneDimFiniteElement);
						resolveForceInY = resolveForcesToLocal[1];
					}
				} else if(loadDirection == ConMemberLoadDirection.GX) {
					if(finiteElementType==OneDimFiniteElementType.BEAM2D) {
						resolveForceInY = -MathHelper.resolveForceInX(forceVal, angleOfInclination);
					} else {
						double[] globalForceVals = new double[12];
						globalForceVals[0] = forceVal;
						double[] resolveForcesToLocal = MathHelper.resolveForcesToLocal(globalForceVals, oneDimFiniteElement);
						resolveForceInY = resolveForcesToLocal[1];
					}
					// Getting peak moment value.
				} else if(loadDirection == ConMemberLoadDirection.GZ) {
					if(finiteElementType==OneDimFiniteElementType.BEAM2D) {
						throw new RuntimeException("No GZ force for plane structures..");
					} else {
						double[] globalForceVals = new double[12];
						globalForceVals[2] = forceVal;
						double[] resolveForcesToLocal = MathHelper.resolveForcesToLocal(globalForceVals, oneDimFiniteElement);
						resolveForceInY = resolveForcesToLocal[1];
					}
					// Getting peak moment value.
				}
				sectionSFDueToUDL = processSFforCon_LY(lengthOfElement,
						xVal, sectionSFDueToUDL, resolveForceInY, a, b);
			}
		}
		return sectionSFDueToUDL;
	}

	public static double processSFforCon_LY(double lengthOfElement,
			double xVal, double sectionSFDueToUDL, double forceVal, double a,
			double b) {
		double resultantForceOnFirstNode = (forceVal * b * b * (lengthOfElement + (2 * a)))
				/ (lengthOfElement * lengthOfElement * lengthOfElement);
		
		double resultantNodalForceOnSecondNode = (forceVal * a * a * (lengthOfElement + (2 * b)))
				/ (lengthOfElement * lengthOfElement * lengthOfElement);
		
		Type1pCurve type1pCurve = new Type1pCurve(lengthOfElement,-resultantForceOnFirstNode,-resultantNodalForceOnSecondNode,a);
		double typ1pCurveVal = type1pCurve.getValueAtASection(xVal);
		sectionSFDueToUDL += typ1pCurveVal;
		return sectionSFDueToUDL;
	}

	private static double processSectionSFforUni_LY(
			OneDimFiniteElement oneDimFiniteElement, double lengthOfElement,
			double xVal, double sectionSFDueToUDL, UniformForce udlLoad,
			double udlForceVal) {
		double rangeBegin = udlLoad.getRangeBegin();
		double rangeEnd = udlLoad.getRangeEnd();

		if(!udlLoad.isPatchLoad())
			rangeEnd=oneDimFiniteElement.getLengthOfElement();
		//udlLoad
		double[] fixedEndReactions = FixEndMomentUtil.getFixedEndReactionAndMomentsForPatchUDL_LY(lengthOfElement,udlLoad,udlForceVal);

		Type2CurvePartial type2CurvePartial = new Type2CurvePartial(-fixedEndReactions[0], -fixedEndReactions[1], lengthOfElement,rangeBegin,rangeEnd,udlForceVal);
		double valueAtASection = type2CurvePartial.getValueAtASection(xVal);

		sectionSFDueToUDL += valueAtASection;
		return sectionSFDueToUDL;
	}

	public static double curveMemberLoadMoments_MZ(LoadCase loadCase,
			OneDimFiniteElement oneDimFiniteElement, double ratio) {
		// Find UDL's on this member for given LoadCase
		//Map<MemberLoad, Set<OneDimFiniteElement>> memberLoads = loadCase.getMemberLoads();
		Set<MemberLoad> memberLoadsAssociated = loadCase.getAllMemberLoadsForThisFE(oneDimFiniteElement);
		double lengthOfElement = oneDimFiniteElement.getLengthOfElement();
		double xVal = ratio*lengthOfElement;
		OneDimFiniteElementType finiteElementType = oneDimFiniteElement.getFiniteElementType();

		double sectionMZDueToUDL = 0;
		for (MemberLoad memberLoad : memberLoadsAssociated) {
			if(memberLoad.getMemberLoadType()==MemberLoadType.UNI) {
				UniformForce udlLoad = memberLoad.getUniformForce();
				double originalUdlForceVal = udlLoad.getForceVal();
				UniMemberLoadDirection loadDirection = udlLoad.getLoadDirection();
				double forceValResolvedOnY = 0;

				if(loadDirection == UniMemberLoadDirection.Y) {
					forceValResolvedOnY = originalUdlForceVal;
				} else if(loadDirection == UniMemberLoadDirection.GX) {
					
					if(finiteElementType==OneDimFiniteElementType.BEAM2D) {
						forceValResolvedOnY = -MathHelper.resolveForceInX(originalUdlForceVal,
							oneDimFiniteElement.getAngleOfInclination());
					} else {
						double[] globalForceVals = new double[12];
						globalForceVals[0] = originalUdlForceVal;
						double[] resolveForcesToLocal = MathHelper.resolveForcesToLocal(globalForceVals, oneDimFiniteElement);
						forceValResolvedOnY = resolveForcesToLocal[1];
					}
					
				} else if(loadDirection == UniMemberLoadDirection.GY) {
					if(finiteElementType==OneDimFiniteElementType.BEAM2D) {
						forceValResolvedOnY = MathHelper.resolveForceInY(originalUdlForceVal,
							oneDimFiniteElement.getAngleOfInclination());
					} else {
						double[] globalForceVals = new double[12];
						globalForceVals[1] = originalUdlForceVal;
						double[] resolveForcesToLocal = MathHelper.resolveForcesToLocal(globalForceVals, oneDimFiniteElement);
						forceValResolvedOnY = resolveForcesToLocal[1];
					}
				} else if(loadDirection == UniMemberLoadDirection.GZ) {
					if(finiteElementType==OneDimFiniteElementType.BEAM2D) {
						throw new RuntimeException("No GZ for support for plane structures...");
					} else {
						double[] globalForceVals = new double[12];
						globalForceVals[2] = originalUdlForceVal;
						double[] resolveForcesToLocal = MathHelper.resolveForcesToLocal(globalForceVals, oneDimFiniteElement);
						forceValResolvedOnY = resolveForcesToLocal[1];
					}
				} else if(loadDirection == UniMemberLoadDirection.PX) {
					if(finiteElementType==OneDimFiniteElementType.BEAM2D) {
					double forceValResolvedOnXFirst = MathHelper.resolveForceInX(originalUdlForceVal,
							MathHelper.getMod(oneDimFiniteElement
									.getAngleOfInclination()));
						forceValResolvedOnY = -MathHelper.resolveForceInX(forceValResolvedOnXFirst,
							oneDimFiniteElement.getAngleOfInclination());
					} else {
						double globalForceFromProjectedLength_PX = MathHelper.getGlobalForceFromProjectedLength_PX(udlLoad, oneDimFiniteElement);
						
						double[] globalForceVals = new double[12];
						globalForceVals[0] = globalForceFromProjectedLength_PX;
						double[] resolveForcesToLocal = MathHelper.resolveForcesToLocal(globalForceVals, oneDimFiniteElement);
						forceValResolvedOnY = resolveForcesToLocal[1];
					}
				} else if(loadDirection == UniMemberLoadDirection.PY) {
					if(finiteElementType==OneDimFiniteElementType.BEAM2D) {
					double forceValResolvedOnYFirst = MathHelper.resolveForceInY(originalUdlForceVal,
							MathHelper.getMod(oneDimFiniteElement
									.getAngleOfInclination()));
						forceValResolvedOnY = MathHelper.resolveForceInY(forceValResolvedOnYFirst,
							oneDimFiniteElement.getAngleOfInclination());
					} else {
						double globalForceFromProjectedLength_PY = MathHelper.getGlobalForceFromProjectedLength_PY(udlLoad, oneDimFiniteElement);
						
						double[] globalForceVals = new double[12];
						globalForceVals[1] = globalForceFromProjectedLength_PY;
						double[] resolveForcesToLocal = MathHelper.resolveForcesToLocal(globalForceVals, oneDimFiniteElement);
						forceValResolvedOnY = resolveForcesToLocal[1];
					}
				} else if(loadDirection == UniMemberLoadDirection.PZ) {
					if(finiteElementType==OneDimFiniteElementType.BEAM2D) {
					throw new RuntimeException("No PZ load support for plane frame..");
					} else {
						double globalForceFromProjectedLength_PZ = MathHelper.getGlobalForceFromProjectedLength_PZ(udlLoad, oneDimFiniteElement);
						
						double[] globalForceVals = new double[12];
						globalForceVals[2] = globalForceFromProjectedLength_PZ;
						double[] resolveForcesToLocal = MathHelper.resolveForcesToLocal(globalForceVals, oneDimFiniteElement);
						forceValResolvedOnY = resolveForcesToLocal[1];
					}
				}
				// Final Performing
				udlLoad.setForceVal(forceValResolvedOnY);
				sectionMZDueToUDL = processSectoinMZforUni_LY(
						lengthOfElement, xVal,
						sectionMZDueToUDL, udlLoad);
				udlLoad.setForceVal(originalUdlForceVal);
			} else if(memberLoad.getMemberLoadType()==MemberLoadType.CON) {
				ConcentratedForce concentratedForce = memberLoad.getConcentratedForce();
				double forceVal = concentratedForce.getForceVal();
				double a = concentratedForce.getD1();
				double b = lengthOfElement-a;
				double angleOfInclination = oneDimFiniteElement.getAngleOfInclination();
				double resolveForceInY = 0;
				if(concentratedForce.getLoadDirection() == ConMemberLoadDirection.Y) {
					resolveForceInY = forceVal;
				} else if(concentratedForce.getLoadDirection() == ConMemberLoadDirection.GY) {
					if(finiteElementType==OneDimFiniteElementType.BEAM2D) {
						// Resolving force
						resolveForceInY = MathHelper.resolveForceInY(forceVal, angleOfInclination);
					} else {
						double[] globalForceVals = new double[12];
						globalForceVals[1] = forceVal;
						double[] resolveForcesToLocal = MathHelper.resolveForcesToLocal(globalForceVals, oneDimFiniteElement);
						resolveForceInY = resolveForcesToLocal[1];
					}
					
				} else if(concentratedForce.getLoadDirection() == ConMemberLoadDirection.GX) {
					if(finiteElementType==OneDimFiniteElementType.BEAM2D) {
						// Resolving force
						resolveForceInY = -MathHelper.resolveForceInX(forceVal, angleOfInclination);
					} else {
						double[] globalForceVals = new double[12];
						globalForceVals[0] = forceVal;
						double[] resolveForcesToLocal = MathHelper.resolveForcesToLocal(globalForceVals, oneDimFiniteElement);
						resolveForceInY = resolveForcesToLocal[1];
					}
				} else if(concentratedForce.getLoadDirection() == ConMemberLoadDirection.GZ) {
					if(finiteElementType==OneDimFiniteElementType.BEAM2D) {
						throw new RuntimeException("GZ force not allowed for plane structure..");
					} else {
						double[] globalForceVals = new double[12];
						globalForceVals[2] = forceVal;
						double[] resolveForcesToLocal = MathHelper.resolveForcesToLocal(globalForceVals, oneDimFiniteElement);
						resolveForceInY = resolveForcesToLocal[1];
					}
				}
				
				sectionMZDueToUDL = processSectionMZforCon_LY(
						lengthOfElement, xVal, sectionMZDueToUDL,
						resolveForceInY, a, b);
			}
		}
		
		return sectionMZDueToUDL;
	}

	public static double processSectionMZforCon_LY(double lengthOfElement,
			double xVal, double sectionMZDueToUDL, double forceVal, double a,
			double b) {
		double nodalMomentOnFirstNode = (forceVal * a * b * b)
				/ (lengthOfElement * lengthOfElement);
		double nodalMomentOnSecondNode = -(forceVal * a * a * b)
				/ (lengthOfElement * lengthOfElement);

		Type1pCurve type1pCurve = new Type1pCurve(lengthOfElement,-nodalMomentOnFirstNode,-nodalMomentOnSecondNode,a);
		double typ1pCurveVal = type1pCurve.getValueAtASection(xVal);
		sectionMZDueToUDL += typ1pCurveVal;
		

		// Getting peak moment value.
		double resultantForceOnFirstNode = (forceVal * b * b * (lengthOfElement + (2 * a)))
				/ (lengthOfElement * lengthOfElement * lengthOfElement);
		double peakMomentValFirstNode = resultantForceOnFirstNode * a;
		
		double resultantNodalForceOnSecondNode = (forceVal * a * a * (lengthOfElement + (2 * b)))
				/ (lengthOfElement * lengthOfElement * lengthOfElement);
		double peakMomentValSecondNode = resultantNodalForceOnSecondNode * b;

		Type4pCurve type4pCurve = new Type4pCurve(lengthOfElement, a, peakMomentValFirstNode,peakMomentValSecondNode);
		double type4pCurveVal = type4pCurve.getValueAtASection(xVal);
		sectionMZDueToUDL += type4pCurveVal;
		return sectionMZDueToUDL;
	}

	public static double processSectoinMZforUni_LY(
			double lengthOfElement,
			double xVal, double sectionMZDueToUDL, UniformForce udlLoad) {
		//double lengthOfElement = oneDimFiniteElement.getLengthOfElement();
		double rangeEnd = udlLoad.getRangeEnd();

		if(!udlLoad.isPatchLoad())
			rangeEnd=lengthOfElement;

		double forceVal = udlLoad.getForceVal();
		double[] fixedEndReactions = FixEndMomentUtil.getFixedEndReactionAndMomentsForPatchUDL_LY(lengthOfElement, udlLoad,forceVal);

		Type3CurvePartial_LX type3Curve = new Type3CurvePartial_LX(lengthOfElement,udlLoad,fixedEndReactions[0]);
		double parabolicCurveVal = type3Curve.getValueAtASection(xVal);

		//double endCurveVal = udlForceVal*lengthOfElement*lengthOfElement/12;
		Type1Curve type1Curve = new Type1Curve(lengthOfElement,fixedEndReactions[2]);
		double straightLineVal = type1Curve.getValueAtASection();

		/*Type2Curve type2CurveSec = new Type2Curve(-fixedEndReactions[2]-fixedEndReactions[3],0, lengthOfElement);
		double straightLineValSec = type2CurveSec.getValueAtASection(xVal);*/
		

		sectionMZDueToUDL += parabolicCurveVal-straightLineVal;
		return sectionMZDueToUDL;
	}

	/**
	 * Returns the moments obtained by FEM values alone.
	 * Curve is linearly varying.
	 * @param loadCase 
	 * @param oneDBeamEndMomentsEntity
	 * @param oneDimFiniteElement
	 * @param ratio 
	 * @return 
	 */
	public static double curveFemEndMoments_MZ(
			LoadCase loadCase, OneDimFiniteElement oneDimFiniteElement, double ratio) {
		Map<OneDimFiniteElement, OneDBeamEndMomentsEntity> beamEndMomentEntities = loadCase.getBeamEndMomentEntities();
		OneDBeamEndMomentsEntity oneDBeamEndMomentsEntity = beamEndMomentEntities.get(oneDimFiniteElement);
		double momentFirstNode = oneDBeamEndMomentsEntity.getMoment_MZ(oneDimFiniteElement.getFirstNode());
		double momentSecondNode = oneDBeamEndMomentsEntity.getMoment_MZ(oneDimFiniteElement.getSecondNode());

		double lengthOfElement = oneDimFiniteElement.getLengthOfElement();

		Type2Curve type2Curve = new Type2Curve(momentFirstNode, momentSecondNode, lengthOfElement);

		double sectionAtDistance = lengthOfElement * ratio;

		double momentAtRatioSection = type2Curve.getValueAtASection(sectionAtDistance);

		//double momentAtRatioSection = performMomentInterpolation(lengthOfElement,sectionAtDistance,momentFirstNode,momentSecondNode);
		return momentAtRatioSection;
	}

	public static double deflectionByCurveFemEndMoments_MZ(LoadCase loadCase,
			OneDimFiniteElement oneDimFiniteElement, double ratio, double inertialMomentY) {Map<OneDimFiniteElement, OneDBeamEndMomentsEntity> beamEndMomentEntities = loadCase.getBeamEndMomentEntities();
			OneDBeamEndMomentsEntity oneDBeamEndMomentsEntity = beamEndMomentEntities.get(oneDimFiniteElement);
			double momentFirstNode = oneDBeamEndMomentsEntity.getMoment_MZ(oneDimFiniteElement.getFirstNode());
			double momentSecondNode = oneDBeamEndMomentsEntity.getMoment_MZ(oneDimFiniteElement.getSecondNode());

			double lengthOfElement = oneDimFiniteElement.getLengthOfElement();

			Type2Curve type2Curve = new Type2Curve(momentFirstNode, momentSecondNode, lengthOfElement);

			double sectionAtDistance = lengthOfElement * ratio;

			double momentAtRatioSection = type2Curve.getMomentOfAreaAbout(sectionAtDistance,inertialMomentY);

			//double momentAtRatioSection = performMomentInterpolation(lengthOfElement,sectionAtDistance,momentFirstNode,momentSecondNode);
			return momentAtRatioSection;
			}

	public static double deflectionByCurveMemberLoadMoments(LoadCase loadCase,
			OneDimFiniteElement oneDimFiniteElement, double ratio, double inertialMomentY) {

		// Find UDL's on this member for given LoadCase
		//Map<MemberLoad, Set<OneDimFiniteElement>> memberLoads = loadCase.getMemberLoads();
		Set<MemberLoad> memberLoadsAssociated = loadCase.getAllMemberLoadsForThisFE(oneDimFiniteElement);
		double lengthOfElement = oneDimFiniteElement.getLengthOfElement();
		double xVal = ratio*lengthOfElement;

		double deflectionDueToMemLoad = 0;
		for (MemberLoad memberLoad : memberLoadsAssociated) {
			if(memberLoad.getMemberLoadType()==MemberLoadType.UNI) {
				UniformForce udlLoad = memberLoad.getUniformForce();
				double originalUdlForceVal = udlLoad.getForceVal();
				UniMemberLoadDirection loadDirection = udlLoad.getLoadDirection();
				if(loadDirection == UniMemberLoadDirection.Y) {

				deflectionDueToMemLoad += processDeflectionUni_LY(
						lengthOfElement, xVal, udlLoad,inertialMomentY);
				} else if(loadDirection == UniMemberLoadDirection.GX) {
					
					double forceValResolvedOnY = MathHelper.resolveForceInX(originalUdlForceVal,
							oneDimFiniteElement.getAngleOfInclination());
					
					udlLoad.setForceVal(-forceValResolvedOnY);
					deflectionDueToMemLoad += processDeflectionUni_LY(
							lengthOfElement, xVal,udlLoad,inertialMomentY);
					udlLoad.setForceVal(originalUdlForceVal);
					
				} else if(loadDirection == UniMemberLoadDirection.GY) {
					
					double forceValResolvedOnY = MathHelper.resolveForceInY(originalUdlForceVal,
							oneDimFiniteElement.getAngleOfInclination());
					
					udlLoad.setForceVal(forceValResolvedOnY);
					deflectionDueToMemLoad += processDeflectionUni_LY(
							lengthOfElement, xVal,udlLoad,inertialMomentY);
					udlLoad.setForceVal(originalUdlForceVal);
				} else if(loadDirection == UniMemberLoadDirection.PX) {
					
					// Below statement converts PX load to GX value
					double forceValResolvedOnXFirst = MathHelper.resolveForceInX(originalUdlForceVal,
							MathHelper.getMod(oneDimFiniteElement
									.getAngleOfInclination()));
					// Rest below code copied from GX direction above
					double forceValResolvedOnY = MathHelper.resolveForceInX(forceValResolvedOnXFirst,
							oneDimFiniteElement.getAngleOfInclination());
					
					udlLoad.setForceVal(-forceValResolvedOnY);
					deflectionDueToMemLoad += processDeflectionUni_LY(
							lengthOfElement, xVal,udlLoad,inertialMomentY);
					udlLoad.setForceVal(originalUdlForceVal);
					
					
				} else if(loadDirection == UniMemberLoadDirection.PY) {
					
					// Below statement converts PX load to GX value
					double forceValResolvedOnYFirst = MathHelper.resolveForceInY(originalUdlForceVal,
							MathHelper.getMod(oneDimFiniteElement
									.getAngleOfInclination()));
					// Rest below code copied from GX direction above
					double forceValResolvedOnY = MathHelper.resolveForceInY(forceValResolvedOnYFirst,
							oneDimFiniteElement.getAngleOfInclination());

					udlLoad.setForceVal(forceValResolvedOnY);
					deflectionDueToMemLoad += processDeflectionUni_LY(
							lengthOfElement, xVal,udlLoad,inertialMomentY);
					udlLoad.setForceVal(originalUdlForceVal);
				}
			} else if(memberLoad.getMemberLoadType()==MemberLoadType.CON) {
				ConcentratedForce concentratedForce = memberLoad.getConcentratedForce();
				double forceVal = concentratedForce.getForceVal();
				double a = concentratedForce.getD1();
				double b = lengthOfElement-a;
				double angleOfInclination = oneDimFiniteElement.getAngleOfInclination();
				ConMemberLoadDirection loadDirection = concentratedForce.getLoadDirection();
				if(loadDirection == ConMemberLoadDirection.Y) {
					
					deflectionDueToMemLoad += processDeflectionCon_LY(
							lengthOfElement, xVal, forceVal,
							a, b,inertialMomentY);

				} else if(loadDirection == ConMemberLoadDirection.GY) {
					// Resolving force
					double resolveForceInY = MathHelper.resolveForceInY(forceVal, angleOfInclination);
					deflectionDueToMemLoad += processDeflectionCon_LY(
							lengthOfElement, xVal, resolveForceInY, a, b,inertialMomentY);
					
				} else if(loadDirection == ConMemberLoadDirection.GX) {
					// Resolving force
					double resolveForceInX = -MathHelper.resolveForceInX(forceVal, angleOfInclination);
					deflectionDueToMemLoad += processDeflectionCon_LY(
							lengthOfElement, xVal,
							resolveForceInX, a, b,inertialMomentY);
					
				}
			}
		}
		
		return deflectionDueToMemLoad;
	}

	private static double processDeflectionCon_LY(double lengthOfElement,
			double xVal, double forceVal,
			double a, double b, double inertialMomentY) {
		double deflectionFinal=0;
		double nodalMomentOnFirstNode = (forceVal * a * b * b)
				/ (lengthOfElement * lengthOfElement);
		double nodalMomentOnSecondNode = -(forceVal * a * a * b)
				/ (lengthOfElement * lengthOfElement);

		Type1pCurve type1pCurve = new Type1pCurve(lengthOfElement,-nodalMomentOnFirstNode,-nodalMomentOnSecondNode,a);
		double typ1pCurveVal = type1pCurve.getMomentAboutSection(xVal,inertialMomentY);
		deflectionFinal += typ1pCurveVal;
		

		// Getting peak moment value.
		double resultantForceOnFirstNode = (forceVal * b * b * (lengthOfElement + (2 * a)))
				/ (lengthOfElement * lengthOfElement * lengthOfElement);
		double peakMomentValFirstNode = resultantForceOnFirstNode * a;
		
		double resultantNodalForceOnSecondNode = (forceVal * a * a * (lengthOfElement + (2 * b)))
				/ (lengthOfElement * lengthOfElement * lengthOfElement);
		double peakMomentValSecondNode = resultantNodalForceOnSecondNode * b;

		Type4pCurve type4pCurve = new Type4pCurve(lengthOfElement, a, peakMomentValFirstNode,peakMomentValSecondNode);
		double type4pCurveVal = type4pCurve.getMomentAboutSection(xVal,inertialMomentY);
		deflectionFinal += type4pCurveVal;
		return deflectionFinal;
	}

	private static double processDeflectionUni_LY(double lengthOfElement,
			double xVal, UniformForce udlLoad, double inertialMomentY) {
		//double lengthOfElement = oneDimFiniteElement.getLengthOfElement();
		double rangeEnd = udlLoad.getRangeEnd();

		if(!udlLoad.isPatchLoad())
			rangeEnd=lengthOfElement;

		double forceVal = udlLoad.getForceVal();
		if(forceVal==0)
			return 0;
		
		double[] fixedEndReactions = FixEndMomentUtil.getFixedEndReactionAndMomentsForPatchUDL_LY(lengthOfElement, udlLoad,forceVal);

		Type2CurvePartial type2CurvePartial = new Type2CurvePartial(-fixedEndReactions[0], -fixedEndReactions[1], lengthOfElement,udlLoad.getRangeBegin(),rangeEnd,udlLoad.getForceVal());
		double pointOfCOntraflexure = type2CurvePartial.getSFDMeetZeroLocation();
		
		Type3CurvePartial_LX type3Curve = new Type3CurvePartial_LX(lengthOfElement,udlLoad,fixedEndReactions[0]);
		double parabolicCurveVal = type3Curve.getMomentAboutSection(xVal,inertialMomentY,pointOfCOntraflexure);
		
		//double endCurveVal = udlForceVal*lengthOfElement*lengthOfElement/12;
		Type1Curve type1Curve = new Type1Curve(lengthOfElement,fixedEndReactions[2]);
		double straightLineVal = type1Curve.getMomentAboutSection(xVal,inertialMomentY);
		
		/*Type2Curve type2CurveSec = new Type2Curve(-fixedEndReactions[2]-fixedEndReactions[3],0, lengthOfElement);
		double straightLineValSec = type2CurveSec.getValueAtASection(xVal);*/
		
		double deflection = parabolicCurveVal-straightLineVal;
		return deflection;
	}

	public static double curveFemEndMoments_MY(LoadCase loadCase,
			OneDimFiniteElement oneDimFiniteElement, double ratio) {
		Map<OneDimFiniteElement, OneDBeamEndMomentsEntity> beamEndMomentEntities = loadCase.getBeamEndMomentEntities();
		OneDBeamEndMomentsEntity oneDBeamEndMomentsEntity = beamEndMomentEntities.get(oneDimFiniteElement);
		double momentFirstNode = oneDBeamEndMomentsEntity.getMoment_MY(oneDimFiniteElement.getFirstNode());
		double momentSecondNode = oneDBeamEndMomentsEntity.getMoment_MY(oneDimFiniteElement.getSecondNode());

		double lengthOfElement = oneDimFiniteElement.getLengthOfElement();

		Type2Curve type2Curve = new Type2Curve(momentFirstNode, momentSecondNode, lengthOfElement);

		double sectionAtDistance = lengthOfElement * ratio;

		double momentAtRatioSection = type2Curve.getValueAtASection(sectionAtDistance);

		//double momentAtRatioSection = performMomentInterpolation(lengthOfElement,sectionAtDistance,momentFirstNode,momentSecondNode);
		return momentAtRatioSection;
	}

	public static double curveMemberLoadMoments_MY(LoadCase loadCase,
			OneDimFiniteElement oneDimFiniteElement, double ratio) {
		
		// Find UDL's on this member for given LoadCase
		//Map<MemberLoad, Set<OneDimFiniteElement>> memberLoads = loadCase.getMemberLoads();
		Set<MemberLoad> memberLoadsAssociated = loadCase.getAllMemberLoadsForThisFE(oneDimFiniteElement);
		double lengthOfElement = oneDimFiniteElement.getLengthOfElement();
		double xVal = ratio*lengthOfElement;
		OneDimFiniteElementType finiteElementType = oneDimFiniteElement.getFiniteElementType();

		double sectionMZDueToUDL = 0;
		for (MemberLoad memberLoad : memberLoadsAssociated) {
			if(memberLoad.getMemberLoadType()==MemberLoadType.UNI) {
				UniformForce udlLoad = memberLoad.getUniformForce();
				double originalUdlForceVal = udlLoad.getForceVal();
				UniMemberLoadDirection loadDirection = udlLoad.getLoadDirection();
				double forceValResolvedOnY = 0;

				if(loadDirection == UniMemberLoadDirection.Z) {
					forceValResolvedOnY = -originalUdlForceVal;
				} else if(loadDirection == UniMemberLoadDirection.GX) {
						double[] globalForceVals = new double[12];
						globalForceVals[0] = originalUdlForceVal;
						double[] resolveForcesToLocal = MathHelper.resolveForcesToLocal(globalForceVals, oneDimFiniteElement);
						forceValResolvedOnY = -resolveForcesToLocal[2];
				} else if(loadDirection == UniMemberLoadDirection.GY) {
						double[] globalForceVals = new double[12];
						globalForceVals[1] = originalUdlForceVal;
						double[] resolveForcesToLocal = MathHelper.resolveForcesToLocal(globalForceVals, oneDimFiniteElement);
						forceValResolvedOnY = resolveForcesToLocal[2];
				} else if(loadDirection == UniMemberLoadDirection.GZ) {
						double[] globalForceVals = new double[12];
						globalForceVals[2] = originalUdlForceVal;
						double[] resolveForcesToLocal = MathHelper.resolveForcesToLocal(globalForceVals, oneDimFiniteElement);
						forceValResolvedOnY = -resolveForcesToLocal[2];
				} else if(loadDirection == UniMemberLoadDirection.PX) {
						double globalForceFromProjectedLength_PX = -MathHelper.getGlobalForceFromProjectedLength_PX(udlLoad, oneDimFiniteElement);
						
						double[] globalForceVals = new double[12];
						globalForceVals[0] = globalForceFromProjectedLength_PX;
						double[] resolveForcesToLocal = MathHelper.resolveForcesToLocal(globalForceVals, oneDimFiniteElement);
						forceValResolvedOnY = resolveForcesToLocal[2];
				} else if(loadDirection == UniMemberLoadDirection.PY) {
						double globalForceFromProjectedLength_PY = MathHelper.getGlobalForceFromProjectedLength_PY(udlLoad, oneDimFiniteElement);
						
						double[] globalForceVals = new double[12];
						globalForceVals[1] = globalForceFromProjectedLength_PY;
						double[] resolveForcesToLocal = MathHelper.resolveForcesToLocal(globalForceVals, oneDimFiniteElement);
						forceValResolvedOnY = resolveForcesToLocal[2];
				} else if(loadDirection == UniMemberLoadDirection.PZ) {
						double globalForceFromProjectedLength_PZ = MathHelper.getGlobalForceFromProjectedLength_PZ(udlLoad, oneDimFiniteElement);
						
						double[] globalForceVals = new double[12];
						globalForceVals[2] = globalForceFromProjectedLength_PZ;
						double[] resolveForcesToLocal = MathHelper.resolveForcesToLocal(globalForceVals, oneDimFiniteElement);
						forceValResolvedOnY = -resolveForcesToLocal[2];
				}
				// Final Performing
				udlLoad.setForceVal(forceValResolvedOnY);
				sectionMZDueToUDL = processSectoinMZforUni_LY(
						lengthOfElement, xVal,
						sectionMZDueToUDL, udlLoad);
				udlLoad.setForceVal(originalUdlForceVal);
			} else if(memberLoad.getMemberLoadType()==MemberLoadType.CON) {
				ConcentratedForce concentratedForce = memberLoad.getConcentratedForce();
				double forceVal = concentratedForce.getForceVal();
				double a = concentratedForce.getD1();
				double b = lengthOfElement-a;
				double angleOfInclination = oneDimFiniteElement.getAngleOfInclination();
				double resolveForceInY = 0;
				if(concentratedForce.getLoadDirection() == ConMemberLoadDirection.Z) {
					resolveForceInY = -forceVal;
				} else if(concentratedForce.getLoadDirection() == ConMemberLoadDirection.GY) {
					if(finiteElementType==OneDimFiniteElementType.BEAM2D) {
						// Resolving force
						resolveForceInY = MathHelper.resolveForceInY(forceVal, angleOfInclination);
					} else {
						double[] globalForceVals = new double[12];
						globalForceVals[1] = forceVal;
						double[] resolveForcesToLocal = MathHelper.resolveForcesToLocal(globalForceVals, oneDimFiniteElement);
						resolveForceInY = resolveForcesToLocal[2];
					}
					
				} else if(concentratedForce.getLoadDirection() == ConMemberLoadDirection.GX) {
					if(finiteElementType==OneDimFiniteElementType.BEAM2D) {
						// Resolving force
						resolveForceInY = -MathHelper.resolveForceInX(forceVal, angleOfInclination);
					} else {
						double[] globalForceVals = new double[12];
						globalForceVals[0] = forceVal;
						double[] resolveForcesToLocal = MathHelper.resolveForcesToLocal(globalForceVals, oneDimFiniteElement);
						resolveForceInY = -resolveForcesToLocal[2];
					}
				} else if(concentratedForce.getLoadDirection() == ConMemberLoadDirection.GZ) {
					if(finiteElementType==OneDimFiniteElementType.BEAM2D) {
						throw new RuntimeException("GZ force not allowed for plane structure..");
					} else {
						double[] globalForceVals = new double[12];
						globalForceVals[2] = forceVal;
						double[] resolveForcesToLocal = MathHelper.resolveForcesToLocal(globalForceVals, oneDimFiniteElement);
						resolveForceInY = -resolveForcesToLocal[2];
					}
				}
				
				sectionMZDueToUDL = processSectionMZforCon_LY(
						lengthOfElement, xVal, sectionMZDueToUDL,
						resolveForceInY, a, b);
			}
		}
		
		return sectionMZDueToUDL;
	}

	public static double curveFEMSF_FZ(LoadCase loadCase,
			OneDimFiniteElement oneDimFiniteElement, double ratio) {
		Map<OneDimFiniteElement, OneDBeamEndShearForceEntity> beamEndShearForceEntities = loadCase.getBeamEndShearForceEntities();
		OneDBeamEndShearForceEntity oneDBeamEndShearForceEntity = beamEndShearForceEntities.get(oneDimFiniteElement);
		double sfFirstNode = oneDBeamEndShearForceEntity.getShearForce_FZ(oneDimFiniteElement.getFirstNode());
		//double sfSecondNode = oneDBeamEndShearForceEntity.getShearForce(oneDimFiniteElement.getSecondNode());

		double lengthOfElement = oneDimFiniteElement.getLengthOfElement();

		Type1Curve type1Curve = new Type1Curve(lengthOfElement, sfFirstNode);

		//double sectionAtDistance = lengthOfElement * ratio;

		double sfAtRatioSection = type1Curve.getValueAtASection();

		//double momentAtRatioSection = performMomentInterpolation(lengthOfElement,sectionAtDistance,momentFirstNode,momentSecondNode);
		return sfAtRatioSection;
	}

	public static double curveMemberLoadSF_FZ(LoadCase loadCase,
			OneDimFiniteElement oneDimFiniteElement, double ratio) {
		Set<MemberLoad> memberLoadsAssociated = loadCase.getAllMemberLoadsForThisFE(oneDimFiniteElement);
		double lengthOfElement = oneDimFiniteElement.getLengthOfElement();
		double xVal = ratio*lengthOfElement;
		double sectionSFDueToUDL = 0;
		
		for (MemberLoad memberLoad : memberLoadsAssociated) {
			if(memberLoad.getMemberLoadType()==MemberLoadType.UNI) {
				UniformForce udlLoad = memberLoad.getUniformForce();
				double forceVal = udlLoad.getForceVal();
				//UniformForce uniformForce = memberLoad.getUniformForce();
				double forceValResolvedOnZ = 0;
				if(udlLoad.getLoadDirection() == UniMemberLoadDirection.Z) {
					forceValResolvedOnZ = forceVal;
				} else if(memberLoad.getUniformForce().getLoadDirection() == UniMemberLoadDirection.GX) {
						double[] globalForceVals = new double[12];
						globalForceVals[0] = forceVal;
						double[] resolveForcesToLocal = MathHelper.resolveForcesToLocal(globalForceVals, oneDimFiniteElement);
						forceValResolvedOnZ = resolveForcesToLocal[2];
					
				} else if(memberLoad.getUniformForce().getLoadDirection() == UniMemberLoadDirection.GY) {
						double[] globalForceVals = new double[12];
						globalForceVals[1] = forceVal;
						double[] resolveForcesToLocal = MathHelper.resolveForcesToLocal(globalForceVals, oneDimFiniteElement);
						forceValResolvedOnZ = resolveForcesToLocal[2];
					
				} else if(memberLoad.getUniformForce().getLoadDirection() == UniMemberLoadDirection.GZ) {
					double[] globalForceVals = new double[12];
					globalForceVals[2] = forceVal;
					double[] resolveForcesToLocal = MathHelper.resolveForcesToLocal(globalForceVals, oneDimFiniteElement);
					forceValResolvedOnZ = resolveForcesToLocal[2];
				
				} else if(memberLoad.getUniformForce().getLoadDirection() == UniMemberLoadDirection.PX) {

						double globalForceFromProjectedLength_PX = MathHelper.getGlobalForceFromProjectedLength_PX(udlLoad, oneDimFiniteElement);
						double[] globalForceVals = new double[12];
						globalForceVals[0] = globalForceFromProjectedLength_PX;
						double[] resolveForcesToLocal = MathHelper.resolveForcesToLocal(globalForceVals, oneDimFiniteElement);
						forceValResolvedOnZ = resolveForcesToLocal[2];
					
				} else if(memberLoad.getUniformForce().getLoadDirection() == UniMemberLoadDirection.PY) {
						double globalForceFromProjectedLength_PY = MathHelper.getGlobalForceFromProjectedLength_PY(udlLoad, oneDimFiniteElement);
						double[] globalForceVals = new double[12];
						globalForceVals[1] = globalForceFromProjectedLength_PY;
						double[] resolveForcesToLocal = MathHelper.resolveForcesToLocal(globalForceVals, oneDimFiniteElement);
						forceValResolvedOnZ = resolveForcesToLocal[2];
				} else if(memberLoad.getUniformForce().getLoadDirection() == UniMemberLoadDirection.PZ) {
					double globalForceFromProjectedLength_PZ = MathHelper.getGlobalForceFromProjectedLength_PZ(udlLoad, oneDimFiniteElement);
					double[] globalForceVals = new double[12];
					globalForceVals[2] = globalForceFromProjectedLength_PZ;
					double[] resolveForcesToLocal = MathHelper.resolveForcesToLocal(globalForceVals, oneDimFiniteElement);
					forceValResolvedOnZ = resolveForcesToLocal[2];
			}
				sectionSFDueToUDL = processSectionSFforUni_LY(oneDimFiniteElement, lengthOfElement, xVal, sectionSFDueToUDL, udlLoad, forceValResolvedOnZ);
			} else if(memberLoad.getMemberLoadType()==MemberLoadType.CON) {
				ConcentratedForce concentratedForce = memberLoad.getConcentratedForce();
				double forceVal = concentratedForce.getForceVal();
				double a = concentratedForce.getD1();
				double b = lengthOfElement-a;
				double angleOfInclination = oneDimFiniteElement.getAngleOfInclination();
				double resolveForceInY = 0;
				ConMemberLoadDirection loadDirection = concentratedForce.getLoadDirection();
				if(loadDirection == ConMemberLoadDirection.Z) {
					resolveForceInY = forceVal;
				} else if(loadDirection == ConMemberLoadDirection.GY) {
						double[] globalForceVals = new double[12];
						globalForceVals[1] = forceVal;
						double[] resolveForcesToLocal = MathHelper.resolveForcesToLocal(globalForceVals, oneDimFiniteElement);
						resolveForceInY = resolveForcesToLocal[2];
				} else if(loadDirection == ConMemberLoadDirection.GX) {
						double[] globalForceVals = new double[12];
						globalForceVals[0] = forceVal;
						double[] resolveForcesToLocal = MathHelper.resolveForcesToLocal(globalForceVals, oneDimFiniteElement);
						resolveForceInY = resolveForcesToLocal[2];
				} else if(loadDirection == ConMemberLoadDirection.GZ) {
						double[] globalForceVals = new double[12];
						globalForceVals[2] = forceVal;
						double[] resolveForcesToLocal = MathHelper.resolveForcesToLocal(globalForceVals, oneDimFiniteElement);
						resolveForceInY = resolveForcesToLocal[2];
				}
				sectionSFDueToUDL = processSFforCon_LY(lengthOfElement,
						xVal, sectionSFDueToUDL, resolveForceInY, a, b);
			}
		}
		return sectionSFDueToUDL;
	}

	public static double curveFemEndTorsion(LoadCase loadCase,
			OneDimFiniteElement oneDimFiniteElement, double ratio) {
		Map<OneDimFiniteElement, OneDBeamEndTorsionEntity> beamEndTosionEntities = loadCase.getBeamEndTorsionEntities();
		OneDBeamEndTorsionEntity oneDBeamEndTorsionEntity = beamEndTosionEntities.get(oneDimFiniteElement);
		double torsionFirstNode = oneDBeamEndTorsionEntity.getTorsion_MX(oneDimFiniteElement.getFirstNode());
		double torsionSecondNode = oneDBeamEndTorsionEntity.getTorsion_MX(oneDimFiniteElement.getSecondNode());

		double lengthOfElement = oneDimFiniteElement.getLengthOfElement();

		Type2Curve type2Curve = new Type2Curve(torsionFirstNode, torsionSecondNode, lengthOfElement);

		double sectionAtDistance = lengthOfElement * ratio;

		double momentAtRatioSection = type2Curve.getValueAtASection(sectionAtDistance);

		//double momentAtRatioSection = performMomentInterpolation(lengthOfElement,sectionAtDistance,momentFirstNode,momentSecondNode);
		return momentAtRatioSection;
	}

	public static double deflectionByCurveFemEndMoments_MY(LoadCase loadCase,
			OneDimFiniteElement oneDimFiniteElement, double ratio, double inertialMomentZ) {Map<OneDimFiniteElement, OneDBeamEndMomentsEntity> beamEndMomentEntities = loadCase.getBeamEndMomentEntities();
			OneDBeamEndMomentsEntity oneDBeamEndMomentsEntity = beamEndMomentEntities.get(oneDimFiniteElement);
			double momentFirstNode = oneDBeamEndMomentsEntity.getMoment_MY(oneDimFiniteElement.getFirstNode());
			double momentSecondNode = oneDBeamEndMomentsEntity.getMoment_MY(oneDimFiniteElement.getSecondNode());

			double lengthOfElement = oneDimFiniteElement.getLengthOfElement();

			Type2Curve type2Curve = new Type2Curve(momentFirstNode, momentSecondNode, lengthOfElement);

			double sectionAtDistance = lengthOfElement * ratio;

			double momentAtRatioSection = type2Curve.getMomentOfAreaAbout(sectionAtDistance,inertialMomentZ);

			//double momentAtRatioSection = performMomentInterpolation(lengthOfElement,sectionAtDistance,momentFirstNode,momentSecondNode);
			return momentAtRatioSection;
			}

	public static double[] deflectionByCurveMemberLoadMoments_Space(
			LoadCase loadCase, OneDimFiniteElement oneDimFiniteElement,
			double ratio, double inertialMomentY, double inertialMomentZ) {

		// Find UDL's on this member for given LoadCase
		//Map<MemberLoad, Set<OneDimFiniteElement>> memberLoads = loadCase.getMemberLoads();
		Set<MemberLoad> memberLoadsAssociated = loadCase.getAllMemberLoadsForThisFE(oneDimFiniteElement);
		double lengthOfElement = oneDimFiniteElement.getLengthOfElement();
		double xVal = ratio*lengthOfElement;
		double[] deflectionsDueToMemLoad = new double[2];
		double deflectionDueToMemLoad_LY = 0;
		double deflectionDueToMemLoad_LZ = 0;
		for (MemberLoad memberLoad : memberLoadsAssociated) {

			if(memberLoad.getMemberLoadType()==MemberLoadType.UNI) {
				UniformForce udlLoad = memberLoad.getUniformForce();
				double originalUdlForceVal = udlLoad.getForceVal();
				UniMemberLoadDirection loadDirection = udlLoad.getLoadDirection();
				double forceValResolvedOnY = 0;
				double forceValResolvedOnZ = 0;
				if(loadDirection == UniMemberLoadDirection.Y) {
					forceValResolvedOnY = originalUdlForceVal;
					
				} else if(loadDirection == UniMemberLoadDirection.Z) {
					forceValResolvedOnZ = originalUdlForceVal;
					
				} else if(loadDirection == UniMemberLoadDirection.GX) {
					
					double[] globalForceVals = new double[12];
					globalForceVals[0] = originalUdlForceVal;
					double[] resolveForcesToLocal = MathHelper.resolveForcesToLocal(globalForceVals, oneDimFiniteElement);
					forceValResolvedOnY = resolveForcesToLocal[1];
					forceValResolvedOnZ = resolveForcesToLocal[2];
					
				} else if(loadDirection == UniMemberLoadDirection.GY) {
					
					double[] globalForceVals = new double[12];
					globalForceVals[1] = originalUdlForceVal;
					double[] resolveForcesToLocal = MathHelper.resolveForcesToLocal(globalForceVals, oneDimFiniteElement);
					forceValResolvedOnY = resolveForcesToLocal[1];
					forceValResolvedOnZ = resolveForcesToLocal[2];
					
				} else if(loadDirection == UniMemberLoadDirection.PX) {
					double globalForceFromProjectedLength_PX = MathHelper.getGlobalForceFromProjectedLength_PX(udlLoad, oneDimFiniteElement);
					
					double[] globalForceVals = new double[12];
					globalForceVals[0] = globalForceFromProjectedLength_PX;
					double[] resolveForcesToLocal = MathHelper.resolveForcesToLocal(globalForceVals, oneDimFiniteElement);
					forceValResolvedOnY = resolveForcesToLocal[1];
					forceValResolvedOnZ = resolveForcesToLocal[2];
					
				} else if(loadDirection == UniMemberLoadDirection.PY) {
					double globalForceFromProjectedLength_PY = MathHelper.getGlobalForceFromProjectedLength_PY(udlLoad, oneDimFiniteElement);
					
					double[] globalForceVals = new double[12];
					globalForceVals[1] = globalForceFromProjectedLength_PY;
					double[] resolveForcesToLocal = MathHelper.resolveForcesToLocal(globalForceVals, oneDimFiniteElement);
					forceValResolvedOnY = resolveForcesToLocal[1];
					forceValResolvedOnZ = resolveForcesToLocal[2];
				} else if(loadDirection == UniMemberLoadDirection.GZ) {
					
					double[] globalForceVals = new double[12];
					globalForceVals[2] = originalUdlForceVal;
					double[] resolveForcesToLocal = MathHelper.resolveForcesToLocal(globalForceVals, oneDimFiniteElement);
					forceValResolvedOnY = resolveForcesToLocal[1];
					forceValResolvedOnZ = resolveForcesToLocal[2];
				} else if(loadDirection == UniMemberLoadDirection.PZ) {
					double globalForceFromProjectedLength_PZ = MathHelper.getGlobalForceFromProjectedLength_PZ(udlLoad, oneDimFiniteElement);
					double[] globalForceVals = new double[12];
					globalForceVals[2] = globalForceFromProjectedLength_PZ;
					double[] resolveForcesToLocal = MathHelper.resolveForcesToLocal(globalForceVals, oneDimFiniteElement);
					forceValResolvedOnY = resolveForcesToLocal[1];
					forceValResolvedOnZ = resolveForcesToLocal[2];
				}
				
				udlLoad.setForceVal(forceValResolvedOnY);
				deflectionDueToMemLoad_LY += processDeflectionUni_LY(
						lengthOfElement, xVal,udlLoad,inertialMomentY);
				udlLoad.setForceVal(forceValResolvedOnZ);
				deflectionDueToMemLoad_LZ += processDeflectionUni_LY(
						lengthOfElement, xVal,udlLoad,inertialMomentZ);
				
				udlLoad.setForceVal(originalUdlForceVal);
				
			} else if(memberLoad.getMemberLoadType()==MemberLoadType.CON) {
				ConcentratedForce concentratedForce = memberLoad.getConcentratedForce();
				double forceVal = concentratedForce.getForceVal();
				double a = concentratedForce.getD1();
				double b = lengthOfElement-a;
				ConMemberLoadDirection loadDirection = concentratedForce.getLoadDirection();
				double resolveForceInY = 0;
				double resolveForceInZ = 0;
				if(loadDirection == ConMemberLoadDirection.Y) {
					resolveForceInY = forceVal;
				} if(loadDirection == ConMemberLoadDirection.Z) {
					resolveForceInZ = forceVal;
				} else if(loadDirection == ConMemberLoadDirection.GY) {
					// Resolving force
					double[] globalForceVals = new double[12];
					globalForceVals[1] = forceVal;
					double[] resolveForcesToLocal = MathHelper.resolveForcesToLocal(globalForceVals, oneDimFiniteElement);
					resolveForceInY = resolveForcesToLocal[1];
					resolveForceInZ = resolveForcesToLocal[2];
					
				} else if(loadDirection == ConMemberLoadDirection.GX) {
					// Resolving force
					double[] globalForceVals = new double[12];
					globalForceVals[0] = forceVal;
					double[] resolveForcesToLocal = MathHelper.resolveForcesToLocal(globalForceVals, oneDimFiniteElement);
					resolveForceInY = resolveForcesToLocal[1];
					resolveForceInZ = resolveForcesToLocal[2];
					
				} else if(loadDirection == ConMemberLoadDirection.GZ) {
					// Resolving force
					double[] globalForceVals = new double[12];
					globalForceVals[2] = forceVal;
					double[] resolveForcesToLocal = MathHelper.resolveForcesToLocal(globalForceVals, oneDimFiniteElement);
					resolveForceInY = resolveForcesToLocal[1];
					resolveForceInZ = resolveForcesToLocal[2];
					
				}
				deflectionDueToMemLoad_LY += processDeflectionCon_LY(
						lengthOfElement, xVal, resolveForceInY, a, b,inertialMomentY);
				deflectionDueToMemLoad_LZ += processDeflectionCon_LY(
						lengthOfElement, xVal, resolveForceInZ, a, b,inertialMomentZ);
			}
		}
		deflectionsDueToMemLoad[0]=deflectionDueToMemLoad_LY;
		deflectionsDueToMemLoad[1]=deflectionDueToMemLoad_LZ;
		return deflectionsDueToMemLoad;
	}


	/*public static double curveMemberLoadAxial_Beam3D(LoadCase loadCase,
			OneDimFiniteElement oneDimFiniteElement, double ratio) {
		Set<MemberLoad> memberLoadsAssociated = loadCase.getAllMemberLoadsForThisFE(oneDimFiniteElement);
		double lengthOfElement = oneDimFiniteElement.getLengthOfElement();
		double xVal = ratio*lengthOfElement;

		double sectionAxialDueToUDL = 0;
		for (MemberLoad memberLoad : memberLoadsAssociated) {
			if(memberLoad.getMemberLoadType()==MemberLoadType.UNI) {
				UniformForce udlLoad = memberLoad.getUniformForce();
				double udlForceVal = udlLoad.getForceVal();
				UniformForce uniformForce = memberLoad.getUniformForce();
				
				if(uniformForce.getLoadDirection() == UniMemberLoadDirection.X) {
				
					sectionAxialDueToUDL += processAxialforUni_LX(
							oneDimFiniteElement, lengthOfElement, xVal, udlLoad, udlForceVal);
				} else if(memberLoad.getUniformForce().getLoadDirection() == UniMemberLoadDirection.GX) {
					double forceValResolvedOnX = MathHelper.resolveForceInY(udlForceVal,
							oneDimFiniteElement.getAngleOfInclination());
					
					sectionAxialDueToUDL += processAxialforUni_LX(
							oneDimFiniteElement, lengthOfElement, xVal, udlLoad, forceValResolvedOnX);
				} else if(memberLoad.getUniformForce().getLoadDirection() == UniMemberLoadDirection.GY) {

					double forceValResolvedOnX = MathHelper.resolveForceInX(udlForceVal,
							oneDimFiniteElement.getAngleOfInclination());
					
					sectionAxialDueToUDL += processAxialforUni_LX(
							oneDimFiniteElement, lengthOfElement, xVal, udlLoad, forceValResolvedOnX);
					
				} else if(memberLoad.getUniformForce().getLoadDirection() == UniMemberLoadDirection.PX) {

					// Below statement converts PX load to GX value
					double forceValResolvedOnXFirst = MathHelper.resolveForceInX(udlForceVal,
							MathHelper.getMod(oneDimFiniteElement
									.getAngleOfInclination()));
					double forceValResolvedOnX = MathHelper.resolveForceInY(forceValResolvedOnXFirst,
							oneDimFiniteElement.getAngleOfInclination());

					sectionAxialDueToUDL += processAxialforUni_LX(
							oneDimFiniteElement, lengthOfElement, xVal, udlLoad, forceValResolvedOnX);
					
				} else if(memberLoad.getUniformForce().getLoadDirection() == UniMemberLoadDirection.PY) {
					
					double forceValResolvedOnYFirst = MathHelper.resolveForceInY(udlForceVal,
							MathHelper.getMod(oneDimFiniteElement
									.getAngleOfInclination()));
					double forceValResolvedOnY = MathHelper.resolveForceInX(forceValResolvedOnYFirst,
							oneDimFiniteElement.getAngleOfInclination());
					
					sectionAxialDueToUDL += processAxialforUni_LX(
							oneDimFiniteElement, lengthOfElement, xVal, udlLoad, forceValResolvedOnY);
				}
			} else if(memberLoad.getMemberLoadType()==MemberLoadType.CON) {
				ConcentratedForce concentratedForce = memberLoad.getConcentratedForce();
				double forceVal = concentratedForce.getForceVal();
				double a = concentratedForce.getD1();
				double b = lengthOfElement-a;
				double angleOfInclination = oneDimFiniteElement.getAngleOfInclination();
				if(concentratedForce.getLoadDirection() == ConMemberLoadDirection.X) {

					// Getting peak moment value.
					sectionAxialDueToUDL+= processAxialforCon_LX(lengthOfElement,
							xVal, forceVal, a, b);
					
				} else if(concentratedForce.getLoadDirection() == ConMemberLoadDirection.GY) {

					double resolveForceInX = MathHelper.resolveForceInX(forceVal, angleOfInclination);
					sectionAxialDueToUDL+= processAxialforCon_LX(lengthOfElement,
							xVal, resolveForceInX, a, b);

				} else if(concentratedForce.getLoadDirection() == ConMemberLoadDirection.GX) {

					double resolveForceInX = MathHelper.resolveForceInY(forceVal, angleOfInclination);
					// Getting peak moment value.
					sectionAxialDueToUDL+= processAxialforCon_LX(lengthOfElement,
							xVal, resolveForceInX, a, b);

				}
			}
		}
		return sectionAxialDueToUDL;
	}*/
}

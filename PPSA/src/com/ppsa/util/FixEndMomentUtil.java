package com.ppsa.util;

import com.psa.entity.LoadCase.MemberLoad.UniformForce;

public class FixEndMomentUtil {
	/**
	 * FixEndMoment for patch load start from 'rangeBegin' to 'rangeEnd'.
	 * Only for 'localX' direction.
	 * @param elementLength
	 * @param udlLoad
	 * @return
	 */
	public static double[] getFixedEndReactionAndMomentsForPatchUDL_LY(double elementLength, UniformForce udlLoad,double forceVal) {
		double[] femVals = new double[4];

		double rangeBegin = udlLoad.getRangeBegin();
		double rangeEnd = udlLoad.getRangeEnd();
		//double forceVal = udlLoad.getForceVal();
		if(!udlLoad.isPatchLoad())
			rangeEnd=elementLength;
			
		double lenSquare = elementLength*elementLength;
		double lenCube = lenSquare*elementLength;
		double rangeEndSquare = rangeEnd*rangeEnd;
		double rangeEndCube = rangeEndSquare*rangeEnd;
		
		
		double nodalForceOnFirstNode = ((forceVal*rangeEnd)/(2*lenCube))*(2*lenCube-2*rangeEndSquare*elementLength+rangeEndCube);
		double nodalForceOnSecondNode = ((forceVal*rangeEndCube)/(2*lenCube))*(2*elementLength-rangeEnd);
		double momentOnFirstNode=((forceVal*rangeEndSquare)/(12*lenSquare))*(6*lenSquare-8*rangeEnd*elementLength+3*rangeEndSquare);
		double momentOnSecondNode=-((forceVal*rangeEndCube)/(12*lenSquare))*(4*elementLength-3*rangeEnd);

		// Now the subtracting the reactions to account for extra UDL.
		if(!(rangeBegin==0)) {
			double rangeBeginSquare = rangeBegin*rangeBegin;
			double rangeBeginCube = rangeBeginSquare*rangeBegin;
			forceVal = -forceVal;

			nodalForceOnFirstNode += ((forceVal*rangeBegin)/(2*lenCube))*(2*lenCube-2*rangeBeginSquare*elementLength+rangeBeginCube);
			nodalForceOnSecondNode += ((forceVal*rangeBeginCube)/(2*lenCube))*(2*elementLength-rangeBegin);
			momentOnFirstNode+=((forceVal*rangeBeginSquare)/(12*lenSquare))*(6*lenSquare-8*rangeBegin*elementLength+3*rangeBeginSquare);
			momentOnSecondNode+=-((forceVal*rangeBeginCube)/(12*lenSquare))*(4*elementLength-3*rangeBegin);
		}
		
		femVals[0] = nodalForceOnFirstNode;
		femVals[1] = nodalForceOnSecondNode;
		femVals[2] = momentOnFirstNode;
		femVals[3] = momentOnSecondNode;
		return femVals;
	}
	
	
	public static double[] getFixedEndReactionAndMomentsForPatchUDL_GY(
			double elementLength, UniformForce uniformForce, double forceValResolvedOnY) {
		double[] femVals = new double[4];
		
		double rangeBegin = uniformForce.getRangeBegin();
		double rangeEnd = uniformForce.getRangeEnd();
		double forceVal = uniformForce.getForceVal();
		
		if(!uniformForce.isPatchLoad())
			rangeEnd=elementLength;

		double lenSquare = elementLength*elementLength;
		double lenCube = lenSquare*elementLength;
		double rangeEndSquare = rangeEnd*rangeEnd;
		double rangeEndCube = rangeEndSquare*rangeEnd;
		double rangeBeginSquare = rangeBegin*rangeBegin;
		double rangeBeginCube = rangeBeginSquare*rangeBegin;

		double nodalForceOnFirstNode = ((forceVal*rangeEnd)/(2*lenCube))*(2*lenCube-2*rangeEndSquare*elementLength+rangeEndCube);
		double nodalForceOnSecondNode = ((forceVal*rangeEndCube)/(2*lenCube))*(2*elementLength-rangeEnd);
		double momentOnFirstNode=((forceValResolvedOnY*rangeEndSquare)/(12*lenSquare))*(6*lenSquare-8*rangeEnd*elementLength+3*rangeEndSquare);
		double momentOnSecondNode=-((forceValResolvedOnY*rangeEndCube)/(12*lenSquare))*(4*elementLength-3*rangeEnd);

		// Now the subtracting the reactions to account for extra UDL.
		if(!(rangeBegin==0)) {
			double reverseForceVal = -forceVal;
			forceValResolvedOnY = -forceValResolvedOnY;

			nodalForceOnFirstNode += ((reverseForceVal*rangeBegin)/(2*lenCube))*(2*lenCube-2*rangeBeginSquare*elementLength+rangeBeginCube);
			nodalForceOnSecondNode += ((reverseForceVal*rangeBeginCube)/(2*lenCube))*(2*elementLength-rangeBegin);
			momentOnFirstNode+=((forceValResolvedOnY*rangeBeginSquare)/(12*lenSquare))*(6*lenSquare-8*rangeBegin*elementLength+3*rangeBeginSquare);
			momentOnSecondNode+=-((forceValResolvedOnY*rangeBeginCube)/(12*lenSquare))*(4*elementLength-3*rangeBegin);
		}
		femVals[0] = nodalForceOnFirstNode;
		femVals[1] = nodalForceOnSecondNode;
		femVals[2] = momentOnFirstNode;
		femVals[3] = momentOnSecondNode;
		return femVals;
	}


	public static double[] getFixedEndReactionAndMomentsForPatchUDL_PY(
			double elementLength, UniformForce uniformForce, double forceValResolvedForPY,
			double forceValResolvedOnY) {
		double[] femVals = new double[4];
		
		double rangeBegin = uniformForce.getRangeBegin();
		double rangeEnd = uniformForce.getRangeEnd();
		//double forceVal = uniformForce.getForceVal();
		
		if(!uniformForce.isPatchLoad())
			rangeEnd=elementLength;
		
		double lenSquare = elementLength*elementLength;
		double lenCube = lenSquare*elementLength;
		double rangeEndSquare = rangeEnd*rangeEnd;
		double rangeEndCube = rangeEndSquare*rangeEnd;
		double rangeBeginSquare = rangeBegin*rangeBegin;
		double rangeBeginCube = rangeBeginSquare*rangeBegin;

		double nodalForceOnFirstNode = ((forceValResolvedForPY*rangeEnd)/(2*lenCube))*(2*lenCube-2*rangeEndSquare*elementLength+rangeEndCube);
		double nodalForceOnSecondNode = ((forceValResolvedForPY*rangeEndCube)/(2*lenCube))*(2*elementLength-rangeEnd);
		double momentOnFirstNode=((forceValResolvedOnY*rangeEndSquare)/(12*lenSquare))*(6*lenSquare-8*rangeEnd*elementLength+3*rangeEndSquare);
		double momentOnSecondNode=-((forceValResolvedOnY*rangeEndCube)/(12*lenSquare))*(4*elementLength-3*rangeEnd);

		// Now the subtracting the reactions to account for extra UDL.
		if(!(rangeBegin==0)) {
			double reverseForceVal = -forceValResolvedForPY;
			forceValResolvedOnY = -forceValResolvedOnY;

			nodalForceOnFirstNode += ((reverseForceVal*rangeBegin)/(2*lenCube))*(2*lenCube-2*rangeBeginSquare*elementLength+rangeBeginCube);
			nodalForceOnSecondNode += ((reverseForceVal*rangeBeginCube)/(2*lenCube))*(2*elementLength-rangeBegin);
			momentOnFirstNode+=((forceValResolvedOnY*rangeBeginSquare)/(12*lenSquare))*(6*lenSquare-8*rangeBegin*elementLength+3*rangeBeginSquare);
			momentOnSecondNode+=-((forceValResolvedOnY*rangeBeginCube)/(12*lenSquare))*(4*elementLength-3*rangeBegin);
		}
		femVals[0] = nodalForceOnFirstNode;
		femVals[1] = nodalForceOnSecondNode;
		femVals[2] = momentOnFirstNode;
		femVals[3] = momentOnSecondNode;
		return femVals;
	}


	public static double[] getFixedEndReactionAndMomentsForPatchUDL_LX(
			double elementLength, UniformForce uniformForce) {
		double[] femVals = new double[4];

		double rangeBegin = uniformForce.getRangeBegin();
		double rangeEnd = uniformForce.getRangeEnd();
		double forceVal = uniformForce.getForceVal();
		if(!uniformForce.isPatchLoad())
			rangeEnd=elementLength;
			
		double lenSquare = elementLength*elementLength;
		double lenCube = lenSquare*elementLength;
		double rangeEndSquare = rangeEnd*rangeEnd;
		double rangeEndCube = rangeEndSquare*rangeEnd;
		
		
		double nodalForceOnFirstNode = ((forceVal*rangeEnd)/(2*lenCube))*(2*lenCube-2*rangeEndSquare*elementLength+rangeEndCube);
		double nodalForceOnSecondNode = ((forceVal*rangeEndCube)/(2*lenCube))*(2*elementLength-rangeEnd);
//		double momentOnFirstNode=((forceVal*rangeEndSquare)/(12*lenSquare))*(6*lenSquare-8*rangeEnd*elementLength+3*rangeEndSquare);
//		double momentOnSecondNode=-((forceVal*rangeEndCube)/(12*lenSquare))*(4*elementLength-3*rangeEnd);

		// Now the subtracting the reactions to account for extra UDL.
		if(!(rangeBegin==0)) {
			double rangeBeginSquare = rangeBegin*rangeBegin;
			double rangeBeginCube = rangeBeginSquare*rangeBegin;
			forceVal = -forceVal;

			nodalForceOnFirstNode += ((forceVal*rangeBegin)/(2*lenCube))*(2*lenCube-2*rangeBeginSquare*elementLength+rangeBeginCube);
			nodalForceOnSecondNode += ((forceVal*rangeBeginCube)/(2*lenCube))*(2*elementLength-rangeBegin);
//			momentOnFirstNode+=((forceVal*rangeBeginSquare)/(12*lenSquare))*(6*lenSquare-8*rangeBegin*elementLength+3*rangeBeginSquare);
//			momentOnSecondNode+=-((forceVal*rangeBeginCube)/(12*lenSquare))*(4*elementLength-3*rangeBegin);
		}

		femVals[0] = nodalForceOnFirstNode;
		femVals[1] = nodalForceOnSecondNode;
		femVals[2] = 0;
		femVals[3] = 0;
		return femVals;
	}
}

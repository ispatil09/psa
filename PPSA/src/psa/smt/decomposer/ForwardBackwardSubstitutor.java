package psa.smt.decomposer;

import java.util.LinkedList;

import psa.smt.entity.MatrixSkyLine;
import psa.smt.util.DataConvertionUtil;

public class ForwardBackwardSubstitutor {
	private MatrixSkyLine lowerDecomposeSkyLine = null;
	private double[] bVector = null;
	
	public ForwardBackwardSubstitutor(MatrixSkyLine lowerMatDecompose , double[] bVect) {
		this.lowerDecomposeSkyLine = lowerMatDecompose;
		this.bVector = bVect;
	}
	
	public double[] solve() {

		long nanoTime1 = System.nanoTime();
		double[] dVect = forwardSubstitute();
		long nanoTime2 = System.nanoTime();		
		System.out.println("TD (mS) TotalForwards : "+(nanoTime2-nanoTime1)/1000000);

		double[] xVector = backSubstitute(dVect);
		long nanoTime3 = System.nanoTime();		
		System.out.println("TD (mS) TotalBackward : "+(nanoTime3-nanoTime2)/1000000);
		
		return xVector;
	}

	private double[] backSubstitute(double[] dVect) {
		int maxIndex = bVector.length-1;
		LinkedList<Double> xVector = new LinkedList<Double>();
		//this.lowerDecomposeSkyLine.toString();
		long nanoTime1 = System.nanoTime();
		for (int i = 0; i < maxIndex+1; i++) {
			xVector.add(0d);
		}
		long nanoTime2 = System.nanoTime();
		System.out.println("TD (mS) forLoopInit : "+(nanoTime2-nanoTime1)/1000000);
		
		lowerDecomposeSkyLine.initializeBackSubEnvelopeData();
		
		long nanoTime3 = System.nanoTime();
		System.out.println("TD (mS) initBackSubEnvlpData : "+(nanoTime3-nanoTime2)/1000000);
//		int limitingInd=maxIndex;
		for (int i = maxIndex; i >= 0; i--) {
			// int envInd = lowerDecomposeSkyLine.getEnvelopeIndexOfThisRow(i);
			/*
			 * int val=limitingInd; if(limitingInd>envInd) val=envInd;
			 */

			double multipliedCoeffs = 0;
			int backSubEnvlopeInd = lowerDecomposeSkyLine.getBackSubEnvelopeForThisRow(i);
			for (int j = backSubEnvlopeInd; j > i; j--) {
				double locMat = lowerDecomposeSkyLine.get(i, j);
				double locArr = xVector.get(j);
				double onceMultipliedVal = locMat * locArr;
				multipliedCoeffs += onceMultipliedVal;
			}
			double denominatorInDCoeff = this.lowerDecomposeSkyLine.get(i, i);
			double dVectorCoeffVal = (dVect[i] - multipliedCoeffs)
					/ denominatorInDCoeff;
			xVector.set(i, dVectorCoeffVal);
			// limitingInd--;
		}
		long nanoTime4 = System.nanoTime();
		System.out.println("TD (mS) backWardSub : "+(nanoTime4-nanoTime3)/1000000);
		return DataConvertionUtil.getDoubleVals(xVector);
	}

	private double[] forwardSubstitute() {
		LinkedList<Double> dVector = new LinkedList<Double>();
		for (int i = 0; i < bVector.length; i++) {

			int envIndex = lowerDecomposeSkyLine.getEnvelopeIndexOfThisRow(i);
			double multipliedCoeffs = 0;
			for (int j = envIndex; j < dVector.size(); j++) {
				double onceMultipliedVal = lowerDecomposeSkyLine.get(i,j)*dVector.get(j);
				multipliedCoeffs+=onceMultipliedVal;
			}
			double denominatorInDCoeff = this.lowerDecomposeSkyLine.get(i, i);
			double dVectorCoeffVal = (bVector[i]-multipliedCoeffs)/denominatorInDCoeff;
			dVector.add(dVectorCoeffVal);
		}

		return DataConvertionUtil.getDoubleVals(dVector);
	}
}
package curves.fem.util;

/**
 * SF for partial UDL.
 * @author SONY
 *
 */
public class Type2CurvePartial {

	private double momentFirstNode;
	private double momentSecondNode;
	private double lengthOfElement;
	private double rangeBegin;
	private double rangeEnd;
	private double udlForceVal;

	public Type2CurvePartial(double momentFirstNode, double momentSecondNode, double lengthOfElement, double rangeBegin, double rangeEnd, double udlForceVal) {
		this.momentFirstNode=momentFirstNode;
		this.momentSecondNode=momentSecondNode;
		this.lengthOfElement=lengthOfElement;
		this.rangeBegin=rangeBegin;
		this.rangeEnd=rangeEnd;
		this.udlForceVal=udlForceVal;
	}
	public double getValueAtASection(double sectionAtDistance) {
		if(sectionAtDistance<=rangeBegin)
			return momentFirstNode;
		else if(sectionAtDistance>rangeBegin && sectionAtDistance<=rangeEnd) {
			double lenOfInterpol = rangeEnd-rangeBegin;
			double sectionInInterpolRange = sectionAtDistance-rangeBegin;
			double momentAtRatioSection = performMomentInterpolation(lenOfInterpol,sectionInInterpolRange,momentFirstNode,momentSecondNode);
			return momentAtRatioSection;
		}
		else
			return -momentSecondNode;
	}
	
	private static double performMomentInterpolation(double lengthOfElementToBeInterpolated,
			double sectionAtDistance, double momentFirstNode,
			double momentSecondNode) {
		double numerator = momentFirstNode*lengthOfElementToBeInterpolated;
		double denominator = momentFirstNode+momentSecondNode;
		
		double intersectionDistance = numerator/denominator;
		
		// Actual interpolation considering total moment difference
		double interpolatedVal = (sectionAtDistance/lengthOfElementToBeInterpolated)*denominator;
		
		if(sectionAtDistance<intersectionDistance)
			return momentFirstNode-interpolatedVal;
		else if(sectionAtDistance>intersectionDistance)
			return momentFirstNode-interpolatedVal;
		else  // Exactly at point of intersection.
			return 0;
	}
	
	/**
	 * Method returns the location of contraflexure.
	 * i.e, point where SFD meet zero axis.
	 * 
	 * @return
	 */
	public double getSFDMeetZeroLocation() {
		double lengthOfElementToBeInterpolated = rangeEnd-rangeBegin;
		double numerator = momentFirstNode*lengthOfElementToBeInterpolated;
		double denominator = momentFirstNode+momentSecondNode;
		
		double intersectionDistance = numerator/denominator;
		double locationOfContraflexure = intersectionDistance+rangeBegin;
		return locationOfContraflexure;
	}
}

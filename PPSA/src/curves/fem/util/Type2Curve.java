package curves.fem.util;

import com.ppsa.math.MathHelper;

/**
 * A linear curve representing joining two end moments.
 * Used for effect of FEM end moments.  
 * @author SONY
 *
 */
public class Type2Curve {
	
	private double valFirstNode = 0;
	private double valSecondNode = 0;

	private double lengthOfElement = 0;
	
	public Type2Curve(double valFirstNode,double valSecondNode,double lengthOfElement) {
		this.valFirstNode = valFirstNode;
		this.valSecondNode = valSecondNode;
		this.lengthOfElement = lengthOfElement;
	}
	
	public double getValueAtASection(double sectionAtDistance) {
		double momentAtRatioSection = performMomentInterpolation(sectionAtDistance);
		return momentAtRatioSection;
	}
	
	private double performMomentInterpolation(
			double sectionAtDistance) {
		//double numerator = valFirstNode*lengthOfElement;
		double denominator = valFirstNode+valSecondNode;
		
		//double intersectionDistance = numerator/denominator;
		
		// Actual interpolation considering total moment difference
		double interpolatedVal = (sectionAtDistance/lengthOfElement)*denominator;
		
		return valFirstNode-interpolatedVal;
	}

	public double getMomentOfAreaAbout(double sectionAtDistance, double inertialMomentY) {
		//double numerator = valFirstNode*lengthOfElement;
		/*double denominator = valFirstNode+valSecondNode;
		
		// Actual interpolation considering total moment difference
		double interpolatedVal = (sectionAtDistance / lengthOfElement)
				* denominator;*/
		double bmAtSection = performMomentInterpolation(sectionAtDistance);
		//double bmAtSection = valFirstNode - interpolatedVal;
		double moment = getMomentAboutSection(sectionAtDistance, bmAtSection,
				inertialMomentY);
		return moment;

	}

	private double getMomentAboutSection(double sectionLocation, double valAtSection, double inertialMomentY) {
		double numerator = valFirstNode*lengthOfElement;
		double denominator = valFirstNode+valSecondNode;
		/*double numerator = lengthOfElement;
		double denominator = ((-valSecondNode/valFirstNode)+1);*/
		
		double axisMeetLocation = numerator/denominator;
		
		/*double numerator = valFirstNode*lengthOfElement;
		double denominator = valFirstNode+valSecondNode;
		
		double axisMeetLocation = numerator/denominator;*/
	
		double D2D1=0;
		
		if((valFirstNode+valSecondNode)==0) { // Indicates BMD is parallel. And not meeting at any location
							 // And the axis meet location will be infinity. 
			D2D1 = calcMomForRectangular(sectionLocation,valAtSection,inertialMomentY);
		} else if((axisMeetLocation <= lengthOfElement)&& (axisMeetLocation > 0)) { // Confirms meet location within length
			if (sectionLocation < axisMeetLocation) {
				D2D1 = calcMomForThisHalfTrap(sectionLocation, valFirstNode,
						valAtSection, inertialMomentY);

			} else if (sectionLocation >= axisMeetLocation) {
				D2D1 = calcMomForTwoTriangles(sectionLocation,
						axisMeetLocation, valFirstNode, valAtSection,
						inertialMomentY);
			}
		} else if(axisMeetLocation>lengthOfElement) { // Axis meet after length
			D2D1 = calcMomForThisHalfTrap(sectionLocation, valFirstNode,
					valAtSection, inertialMomentY);
		} else if(axisMeetLocation<=0) { // Axis meet before zero
			D2D1 = calcMomForThisHalfTrapReverseRect(sectionLocation, valFirstNode,
					valAtSection, inertialMomentY);
		}
		return D2D1;
//		return D2D1;
	}
	private double calcMomForThisHalfTrapReverseRect(double lengthAlongAxis,
			double firstNodeValue, double valAtSection, double inertialMomentY) {
		double totalMom;
		double areaOfRectange = (lengthAlongAxis)*(firstNodeValue/inertialMomentY);
		double areaOfTraingle = 0.5*((valAtSection-firstNodeValue)/inertialMomentY)*lengthAlongAxis;

		double centroidOfRectangle = lengthAlongAxis/2;
//		double centroidOfTriangleFirst = (1d/3d)*lengthAlongAxis;
		double centroidOfTriangleSecond = (1d/3d)*lengthAlongAxis;

		double momTriangleSecond=centroidOfTriangleSecond*areaOfTraingle;
		double momRectFirst = centroidOfRectangle*areaOfRectange;
//		double momTriangleFirst = centroidOfTriangleFirst*areaOfTraingle;
		totalMom = momRectFirst+momTriangleSecond;
		return totalMom;
	}

	private double calcMomForRectangular(double sectionLocation,
			double valAtSection, double inertialMomentY) {
		double areaOfRect = sectionLocation*(valAtSection/inertialMomentY);
		double centroidOfRect = sectionLocation/2;
		double momOfRect = areaOfRect*centroidOfRect;
		return momOfRect;
	}

	private double calcMomForTwoTriangles(double sectionAtDistance,double axisMeetLocation,
			double valFirstNode, double valAtSection, double inertialMomentY) {
		double areaOfFirstTriangle = 0.5*axisMeetLocation*(valFirstNode/inertialMomentY);
		double secTriangleLength = sectionAtDistance-axisMeetLocation;
		double areaOfSecTriangle = 0.5*secTriangleLength*(valAtSection/inertialMomentY);
		
		double centroidFirstLenDist = ((2d/3d)*axisMeetLocation)+secTriangleLength;
		double centroidSecLenDist = (1d/3d)*secTriangleLength;
		
		double momByFirstTriangle = areaOfFirstTriangle*centroidFirstLenDist;
		double momBySecTriangle = areaOfSecTriangle*centroidSecLenDist;
		double finalD1D2 = momByFirstTriangle+momBySecTriangle;
		
		return finalD1D2;
	}
	private double calcMomForThisHalfTrap(double lengthAlongAxis, double triangleHeight,
			double valAtSection, double inertialMomentY) {
		double totalMom;
		double areaOfRectange = (lengthAlongAxis)*(valAtSection/inertialMomentY);
		double areaOfTraingle = 0.5*((triangleHeight-valAtSection)/inertialMomentY)*lengthAlongAxis;

		double centroidOfRectangle = lengthAlongAxis/2;
//		double centroidOfTriangleFirst = (1d/3d)*lengthAlongAxis;
		double centroidOfTriangleSecond = (2d/3d)*lengthAlongAxis;

		double momTriangleSecond=centroidOfTriangleSecond*areaOfTraingle;
		double momRectFirst = centroidOfRectangle*areaOfRectange;
//		double momTriangleFirst = centroidOfTriangleFirst*areaOfTraingle;
		totalMom = momRectFirst+momTriangleSecond;
		return totalMom;
	}
}

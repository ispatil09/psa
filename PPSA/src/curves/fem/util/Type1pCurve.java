package curves.fem.util;

/**
 * Curve is having same value till point load location,
 * there after the curve changes it's value yet continues
 * to be straight.
 * @author SONY
 *
 */
public class Type1pCurve {
	private double lengthOfElement;
	private double firstNodeVal;
	private double secondNodeVal;
	private double d1Dist;

	public Type1pCurve(double lengthOfElement, double firstNodeVal,double secondNodeVal,double pointLoadDist) {
		this.lengthOfElement = lengthOfElement;
		this.firstNodeVal = firstNodeVal;
		this.secondNodeVal = secondNodeVal;
		this.d1Dist = pointLoadDist;
	}

	public double getValueAtASection(double sectionAtDistance) {
		if(sectionAtDistance<=d1Dist)
			return firstNodeVal;
		else
			return -secondNodeVal;
	}

	public double getMomentAboutSection(double xVal, double inertialMomentY) {
		if(xVal<=d1Dist) {
			double areaRectangle = (firstNodeVal/inertialMomentY)*xVal;
			double centroidRectangle = xVal/2;
			double momentRectangle = areaRectangle*centroidRectangle;
			return momentRectangle;
		}
		else {
			double lenAfterD1 = xVal-d1Dist;
			
			double areaRectangleFirst = (firstNodeVal/inertialMomentY)*d1Dist;
			double centroidRectangleFirst = (d1Dist/2)+lenAfterD1;
			double momentRectangleFirst = areaRectangleFirst*centroidRectangleFirst;
			
			double areaRectangleSec = (secondNodeVal/inertialMomentY)*lenAfterD1;
			double centroidRectangleSecond = (lenAfterD1/2);
			double momentRectangleSecond = areaRectangleSec*centroidRectangleSecond;
			return momentRectangleFirst-momentRectangleSecond;
		}
	}
}

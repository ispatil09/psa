package curves.fem.util;

/**
 * BMD representing a point load on simply supported beam.
 * i.e., '0' values at both ends and two peak values at location
 * of application of point load.Both Curves are linearly varying.
 * @author SONY
 *
 */
public class Type4pCurve {
	private double lengthOfElement;
	private double d1Dist;
	private double peakMomentValFirstNode;
	private double peakMomentValSecondNode;

	public Type4pCurve(double lengthOfElement ,double pointLoadDist , double peakMomentValFirstNode,double peakMomentValSecondNode) {
		this.lengthOfElement = lengthOfElement;
		this.d1Dist = pointLoadDist;
		this.peakMomentValFirstNode=peakMomentValFirstNode;
		this.peakMomentValSecondNode=peakMomentValSecondNode;
		
	}

	public double getValueAtASection(double sectionAtDistance) {
		double a = d1Dist;
		double b = lengthOfElement-a;
		double valAtXDist = 0;

		if(sectionAtDistance<=a)
			valAtXDist = sectionAtDistance*peakMomentValFirstNode/a;
		else {
			double distFromOtherEnd = lengthOfElement-sectionAtDistance;
			valAtXDist = distFromOtherEnd*peakMomentValSecondNode/b;
		}

		return valAtXDist;
	}

	public double getMomentAboutSection(double xVal, double inertialMomentY) {
		double a = d1Dist;
		double b = lengthOfElement-a;
		double mByIValAtXDist = 0;

		if(xVal<=a) {
			mByIValAtXDist = (xVal*peakMomentValFirstNode/a)/inertialMomentY;
			double lengthTriangleFirst = xVal;
			double heightTriangleFirst = mByIValAtXDist;
			double areaTriangleFirst = 0.5*lengthTriangleFirst*heightTriangleFirst;
			double centroidTriangleFirst = (1d/3d)*xVal;
			double momentTriangleFirst = areaTriangleFirst*centroidTriangleFirst;
			return momentTriangleFirst;
		} else {
			double distFromOtherEnd = lengthOfElement-xVal;
			double lenAfterD1 = xVal-d1Dist;
			mByIValAtXDist = (distFromOtherEnd*peakMomentValSecondNode/b)/inertialMomentY;
			double valAtXDist = (distFromOtherEnd*peakMomentValSecondNode/b);
			
			//mByIValAtXDist = (xVal*peakMomentValFirstNode/a)/inertialMomentY;
			double lengthTriangleFirst1 = d1Dist;
			double heightTriangleFirst = peakMomentValFirstNode/inertialMomentY;
			double areaTriangleFirst = 0.5*lengthTriangleFirst1*heightTriangleFirst;
			double centroidTriangleFirst = ((1d/3d)*d1Dist)+lenAfterD1;
			double momentTriangleFirst = areaTriangleFirst*centroidTriangleFirst;
			
			double lengthTriangleSecond = lenAfterD1;
			double heightTriangleSecond = (peakMomentValSecondNode-valAtXDist)/inertialMomentY;
			double areaTriangleSecond = 0.5*lengthTriangleSecond*heightTriangleSecond;
			double centroidTriangleSecond = (2d/3d)*lenAfterD1;
			double momentTriangleSecond = areaTriangleSecond*centroidTriangleSecond;
			
			//double lengthRectangle = lenAfterD1;
			double areaRectangle = lenAfterD1*(mByIValAtXDist);
			double centroidRectangle = lenAfterD1/2;
			double momentRectangle = centroidRectangle*areaRectangle;
			
			double finalMoment = momentTriangleFirst+momentRectangle+momentTriangleSecond;
			return finalMoment;
		}

		//return mByIValAtXDist;
	}
}

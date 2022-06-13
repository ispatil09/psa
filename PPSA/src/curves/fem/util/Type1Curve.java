package curves.fem.util;
/**
 * Most simplest straight line curve representing 
 * same value trough out length of member.
 * @author SONY
 *
 */
public class Type1Curve {

	private double lengthOfElement;
	private double straightLineVal;

	public Type1Curve(double lengthOfElement, double straightLineVal) {
		this.lengthOfElement = lengthOfElement;
		this.straightLineVal=straightLineVal;
	}

	public double getValueAtASection() {
		return straightLineVal;
	}

	public double getMomentAboutSection(double xVal, double inertialMomentY) {
		double areaOfRectangle = (straightLineVal/inertialMomentY)*xVal;
		double centroidOfRectangle = xVal/2;
		double momentOfRectangle = areaOfRectangle*centroidOfRectangle;
		return momentOfRectangle;
	}
	

}

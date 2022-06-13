package curves.fem.util;
/**
 * A parabolic curve that represents BM of UDL on beam.
 * @author SONY
 *
 */
public class Type3Curve {

	private double udlForceVal;
	private double lengthOfElement;

	public Type3Curve(double lengthOfElement, double udlForceVal) {
		this.udlForceVal=udlForceVal;
		this.lengthOfElement=lengthOfElement;
	}

	public double getValueAtASection(double xVal) {
		double valAtSection = (udlForceVal*lengthOfElement*xVal/2) - (udlForceVal*xVal*xVal/2);
		return valAtSection;
	}

	
	/*if (!patchLoadFlag) {
		double valAtSection = (udlForceVal * lengthOfElement * xVal / 2)
				- (udlForceVal * xVal * xVal / 2);
		return valAtSection;
	} else {*/
}

package curves.fem.util;

import com.psa.entity.LoadCase.MemberLoad.UniformForce;

/**
 * Moment diagram for Partial UDL.
 * @author SONY
 *
 */
public class Type3CurvePartial_GX {

	private double udlForceVal;
	private double lengthOfElement;
	private double rangeStart;
	private double rangeEnd;
	private boolean patchLoadFlag;
	private double nodalForceOnFirstNode;

	public Type3CurvePartial_GX(double lengthOfElement, UniformForce uniformForce,double udlForceValResolved, double nodalForceOnFirstNode) {
		this.udlForceVal=udlForceValResolved;
		this.lengthOfElement=lengthOfElement;
		this.patchLoadFlag = uniformForce.isPatchLoad();
		if (patchLoadFlag) {
			this.rangeStart = uniformForce.getRangeBegin();
			this.rangeEnd = uniformForce.getRangeEnd();
			this.nodalForceOnFirstNode = nodalForceOnFirstNode;
		}
	}

	public double getValueAtASection(double xVal) {
		//double nodalForceOnFirstNode = 0;
		if (!patchLoadFlag) {
			double valAtSection = (udlForceVal * lengthOfElement * xVal / 2)
					- (udlForceVal * xVal * xVal / 2);
			return valAtSection;
		} else {
			//nodalForceOnFirstNode = getReactionOnFirstNode();
			
			double momentFromSuppReaction = nodalForceOnFirstNode*xVal;
			if(xVal<=rangeStart)
				return momentFromSuppReaction;
			else if(xVal>=rangeStart && xVal<=rangeEnd) {
				double udlLenConsidered = xVal-rangeStart;
				return momentFromSuppReaction - udlLenConsidered*udlForceVal*udlLenConsidered/2;
			}
			else {
				double udlLen = rangeEnd-rangeStart;
				double extaLenFromUdlEnd = xVal-rangeEnd;
				return momentFromSuppReaction - (udlLen*udlForceVal)*((udlLen/2)+extaLenFromUdlEnd);
			}
			
		}
		
	}
}

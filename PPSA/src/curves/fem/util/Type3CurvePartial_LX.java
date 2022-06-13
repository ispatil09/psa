package curves.fem.util;

import org.apache.poi.extractor.ExtractorFactory;

import com.ppsa.math.MathHelper;
import com.ppsa.util.OpenPSA;
import com.psa.entity.LoadCase.MemberLoad.UniformForce;

/**
 * Moment diagram for Partial UDL.
 * @author SONY
 *
 */
public class Type3CurvePartial_LX {

	private double udlForceVal;
	private double lengthOfElement;
	private double rangeStart;
	private double rangeEnd;
	private boolean patchLoadFlag;
	private double nodalForceOnFirstNode;

	public Type3CurvePartial_LX(double lengthOfElement, UniformForce uniformForce,double nodalForceOnFirstNode) {
		this.udlForceVal=uniformForce.getForceVal();
		this.lengthOfElement=lengthOfElement;
		this.patchLoadFlag = uniformForce.isPatchLoad();
		this.nodalForceOnFirstNode = nodalForceOnFirstNode;
		if (patchLoadFlag) {
			this.rangeStart = uniformForce.getRangeBegin();
			this.rangeEnd = uniformForce.getRangeEnd();
//			this.nodalForceOnFirstNode = nodalForceOnFirstNode;
		} else {
			this.rangeStart = 0;
			this.rangeEnd = lengthOfElement;
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
				double moment = momentFromSuppReaction - udlLenConsidered*udlForceVal*udlLenConsidered/2;
				return moment;
			}
			else {
				double udlLen = rangeEnd-rangeStart;
				double extaLenFromUdlEnd = xVal-rangeEnd;
				return momentFromSuppReaction - (udlLen*udlForceVal)*((udlLen/2)+extaLenFromUdlEnd);
			}

		}

	}

	/**
	 * @param xVal
	 * @param inertialMomentY
	 * @param pointOfCOntraflexure 
	 * @param fixedEndMomentNodeOne 
	 * @return
	 */
	public double getMomentAboutSection(double xVal, double inertialMomentY, double pointOfCOntraflexure) {
		//double nodalForceOnFirstNode = 0;
		/*if (!patchLoadFlag) {
			//throw new RuntimeException("Need to do");
			rangeStart=0;
			rangeEnd=lengthOfElement;
		}*/ //else {
			//nodalForceOnFirstNode = getReactionOnFirstNode();
			
			double mByIvalDueToSuppReaction = (nodalForceOnFirstNode*xVal)/inertialMomentY;
			if(xVal<=rangeStart) {
				double areaOfFirstTriangle = 0.5*xVal*mByIvalDueToSuppReaction;
				double centroidOfFirstTriangle = (1d/3d)*xVal;
				double momentTriangle = areaOfFirstTriangle*centroidOfFirstTriangle;
				return momentTriangle;
			}
			else if(xVal>=rangeStart && xVal<=rangeEnd) {
				double udlLenConsidered = xVal-rangeStart;
				double mByIValAtSection = mByIvalDueToSuppReaction - ((udlLenConsidered*udlForceVal*udlLenConsidered/2)/inertialMomentY);

				// First Triangle solve
				double mByIvalTriangleEnd = (nodalForceOnFirstNode*rangeStart)/inertialMomentY;
				double areaOfFirstTriangle = 0.5*rangeStart*mByIvalTriangleEnd;
				double centroidFirstTriangle = ((1d/3d)*rangeStart)+udlLenConsidered;
				double momentTriangle = areaOfFirstTriangle*centroidFirstTriangle;

				// The mid section of UDL 
//				double sectionOfMidUDL = rangeStart+((rangeEnd-rangeStart)/2);
				double sectionOfMaxMoment = pointOfCOntraflexure;
				
				double udlLenConsideredForMax = sectionOfMaxMoment-rangeStart;
				double udlLenAfterMax = udlLenConsidered-udlLenConsideredForMax;
				double mByIvalDueToSuppReactionForMax = (nodalForceOnFirstNode*(sectionOfMaxMoment))/inertialMomentY;
				double maxMByIval = getMaxMByIValue(mByIvalDueToSuppReactionForMax,udlLenConsideredForMax,inertialMomentY);
				//Assuming linearity
				/*double areaOfRectangle = (mByIvalTriangleEnd)*udlLenConsidered;
				double centroidOfRectangle = udlLenConsidered/2;
				double momentRectangle = areaOfRectangle * centroidOfRectangle;

				double areaSmallTriangle = 0.5*(mByIValAtSection-mByIvalTriangleEnd)*udlLenConsidered;
				double centroidSmallTriangle = (1d/3d)*udlLenConsidered;
				double momentSmallTriangle = areaSmallTriangle*centroidSmallTriangle;
				
				double momentFinal = momentTriangle+momentRectangle+momentSmallTriangle;*/
				
				// Considering Rectangle
				if (xVal <= sectionOfMaxMoment) {
					//RectangleFirst
					double areaRectangle = maxMByIval * udlLenConsideredForMax;
					double centroidRectangle = udlLenConsidered-(udlLenConsideredForMax-(udlLenConsideredForMax / 2));
					double momentRectangle = areaRectangle * centroidRectangle;

					//RectangleSecond (Which is to be minused)
					double lenRectangleSec = udlLenConsideredForMax-udlLenConsidered;
					double areaRectangleSec = maxMByIval * lenRectangleSec;
					double centroidRectangleSec = udlLenConsidered-(udlLenConsideredForMax-(lenRectangleSec / 2));
					double momentRectangleSec = areaRectangleSec * centroidRectangleSec;
					
					// inverseParabola(Big parabola)
					double inversedParabolaLen = udlLenConsideredForMax;
					double inversedParabolaHeight = maxMByIval
							- mByIvalTriangleEnd;
					double areaInversedParabola = inversedParabolaHeight
							* inversedParabolaLen / 3;
					double centroidInversedParabola = udlLenConsidered-(udlLenConsideredForMax-((0.75d)
							* udlLenConsideredForMax));
					double momentInversedParabola = areaInversedParabola
							* centroidInversedParabola;
					
					// inverseParabolaSec(Small parabola adjacent to RectangleSec)
					double inversedParabolaLenSec = lenRectangleSec;
					double inversedParabolaHeightSec = maxMByIval
							- mByIValAtSection;
					double areaInversedParabolaSec = inversedParabolaHeightSec
							* inversedParabolaLenSec / 3;
					double centroidInversedParabolaSec = udlLenConsidered-(udlLenConsideredForMax-((0.75d)
							* lenRectangleSec));
					double momentInversedParabolaSec = areaInversedParabolaSec
							* centroidInversedParabolaSec;

					double momentFinal = momentTriangle + momentRectangle-momentRectangleSec
							- momentInversedParabola+momentInversedParabolaSec;
					return momentFinal;
				} else {         // the result should include one more parabola
					//double udlLenConsideredForMax=(rangeEnd-rangeStart)/2;
					//double sectionOfMaxMoment = getSectionOfMaxMoment();
					/*double udlLenConsideredForMax = sectionOfMaxMoment-rangeStart;
					double udlLenAfterMax = udlLenConsidered-udlLenConsideredForMax;
					double mByIvalDueToSuppReactionForMax = (nodalForceOnFirstNode*(sectionOfMaxMoment))/inertialMomentY;
					double maxMByIval = getMaxMByIValue(mByIvalDueToSuppReactionForMax,udlLenConsideredForMax,inertialMomentY);*/
					
					double areaRectangle = maxMByIval * udlLenConsidered;
					double centroidRectangle = udlLenConsidered / 2;
					double momentRectangle = areaRectangle * centroidRectangle;

					// Consider inverseParabola
					double inversedParabolaLenFirst = udlLenConsideredForMax;
					double inversedParabolaHeight = maxMByIval
							- mByIvalTriangleEnd;
					double areaInversedParabola = inversedParabolaHeight
							* inversedParabolaLenFirst / 3;
					double centroidInversedParabola = ((3d / 4d)
							* udlLenConsideredForMax)+udlLenAfterMax;
					double momentInversedParabolaFirst = areaInversedParabola
							* centroidInversedParabola;

					// Consider inverseParabolaSecond
					double inversedParabolaLenSecond = udlLenAfterMax;
					double inversedParabolaHeightSec = maxMByIval
							- mByIValAtSection;
					double areaInversedParabolaSecond = inversedParabolaHeightSec
							* inversedParabolaLenSecond / 3;
					double centroidInversedParabolaSecond = ((1d / 4d)
							* udlLenAfterMax);
					double momentInversedParabolaSec = areaInversedParabolaSecond
							* centroidInversedParabolaSecond;
					
					double momentFinal = momentTriangle + momentRectangle
							- momentInversedParabolaFirst-momentInversedParabolaSec;
					return momentFinal;
				}
			}
			else {
				//double udlLenAfterMax = udlLenConsidered-udlLenConsideredForMax;
				
				double sectionOfPeak = pointOfCOntraflexure;
				double udlLenConsideredForMax=sectionOfPeak-rangeStart;
				double lenAfterSectionPeak = xVal-sectionOfPeak;
				double udlLen=rangeEnd-rangeStart;
				double udlLenAfterPeak = rangeEnd-sectionOfPeak;
				double lenAfterRangeEnd = xVal-rangeEnd;
				
				double mByIvalDueToSuppReactionForMax = (nodalForceOnFirstNode*(sectionOfPeak ))/inertialMomentY;
				double maxMByIval = getMaxMByIValue(mByIvalDueToSuppReactionForMax,udlLenConsideredForMax,inertialMomentY);

				double mByIvalDueToSuppReactionAfterUDLEnd=(nodalForceOnFirstNode*rangeEnd)/inertialMomentY;
				//Note : 'mByIValAtUDLEnd' might be equal to 'mByIvalTriangleEnd'
				double mByIValAtUDLEnd = mByIvalDueToSuppReactionAfterUDLEnd - ((udlLen*udlForceVal*(udlLen/2))/inertialMomentY);
				
				
				double momentFromSuppReaction = (nodalForceOnFirstNode*xVal)/inertialMomentY;
				//double extaLenFromUdlEnd = xVal-rangeEnd;
				double mByIValAtSection=momentFromSuppReaction - ((udlLen*udlForceVal)*((udlLen/2)+lenAfterRangeEnd)/inertialMomentY);
				
				// First Triangle solve
				double mByIvalTriangleEnd = (nodalForceOnFirstNode*rangeStart)/inertialMomentY;
				double areaOfFirstTriangle = 0.5*rangeStart*mByIvalTriangleEnd;
				double centroidFirstTriangle = ((1d/3d)*rangeStart)+(xVal-rangeStart);
				double momentTriangle = areaOfFirstTriangle*centroidFirstTriangle;

				// Big rectangle in UDL range
				double areaRectangle = maxMByIval * udlLen;
				double centroidRectangle = (udlLen / 2)+lenAfterRangeEnd;
				double momentRectangle = areaRectangle * centroidRectangle;

				// Consider inverseParabola
				double inversedParabolaLenFirst = udlLenConsideredForMax;
				double inversedParabolaHeight = maxMByIval
						- mByIvalTriangleEnd;
				double areaInversedParabola = inversedParabolaHeight
						* inversedParabolaLenFirst / 3;
				double centroidInversedParabola = ((3d / 4d)
						* udlLenConsideredForMax)+lenAfterSectionPeak;
				double momentInversedParabolaFirst = areaInversedParabola
						* centroidInversedParabola;

				// Consider inverseParabolaSecond
				double inversedParabolaLenSecond = udlLenAfterPeak;
				double inversedParabolaHeightSec = maxMByIval
						- mByIValAtUDLEnd;
				double areaInversedParabolaSecond = inversedParabolaHeightSec
						* inversedParabolaLenSecond / 3;
				double centroidInversedParabolaSecond = ((1d / 4d)
						* udlLenAfterPeak)+lenAfterRangeEnd;
				double momentInversedParabolaSec = areaInversedParabolaSecond
						* centroidInversedParabolaSecond;
				
				// Rectangle after UDL end
				double lengthRectangleSec = xVal-rangeEnd;
				double heightRectangleSec = mByIValAtSection;
				double areaRectangleSec = lengthRectangleSec*heightRectangleSec;
				double centroidRectangleSec = lenAfterRangeEnd/2;
				double momentRectangleSec = areaRectangleSec*centroidRectangleSec;
				
				// Triangle after UDL end
				double lengthTriangleSec = lengthRectangleSec;
				double heightTriangleSec=mByIValAtUDLEnd-mByIValAtSection;
				double areaTriangleSec = 0.5*heightTriangleSec*lengthTriangleSec;
				double centroidTriangleSec = (2d/3d)*lengthTriangleSec;
				double momentTriangleSec = areaTriangleSec*centroidTriangleSec;
				
				double momentFinal = momentTriangle + momentRectangle
						- momentInversedParabolaFirst
						- momentInversedParabolaSec + momentRectangleSec
						+ momentTriangleSec;
				return momentFinal;
			}
		
	}

	/*
	 * Use iterative method and get the max B.M location 
	 * occuring within the UDL range.
	 * @return
	 * @deprecated Iteration takes lot of time. Instead get
	 * max BM location by SFD.
	 */
	/*private double getSectionOfMaxMomentByIteration() {
		double highestMomentsLocationFound = 0;
		double highestMomentFound = 0;
		
		double stepSize = 0.00001;
		//double iterationEnd = rangeEnd;
		double xVal = rangeStart;
		do {
			double momentFromSuppReaction = nodalForceOnFirstNode*xVal;
			double udlLenConsidered = xVal-rangeStart;
			double moment = momentFromSuppReaction - udlLenConsidered*udlForceVal*udlLenConsidered/2;
			
			if(MathHelper.getMod(moment)>MathHelper.getMod(highestMomentFound)) {
				highestMomentFound = moment;
				highestMomentsLocationFound = xVal;
			}
			
			xVal +=  stepSize;

		} while(xVal<rangeEnd);

		return highestMomentsLocationFound;
//		return rangeStart+((rangeEnd-rangeStart)/2);
	}*/

	private double getMaxMByIValue(double mByIvalDueToSuppReactionForMax, double udlLenConsidered, double inertialMomentY) {
		double mByIValAtSection = mByIvalDueToSuppReactionForMax - ((udlLenConsidered*udlForceVal*udlLenConsidered/2)/inertialMomentY);
		return mByIValAtSection;
	}
}

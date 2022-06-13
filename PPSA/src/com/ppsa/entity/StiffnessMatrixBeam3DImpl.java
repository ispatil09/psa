package com.ppsa.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.ppsa.exception.PPSAStiffnessMatrixExcpetion;
import com.ppsa.math.impl.MathOperationJAMAImpl;
import com.ppsa.util.EntityTranslationUtil;
import com.psa.entity.FiniteElement;
import com.psa.entity.MaterialProperty;
import com.psa.entity.SectionProperty;
import com.psa.entity.StiffnessMatrixEntity;
import com.psa.entity.enums.OneDimFiniteElementType;
import com.psa.entity.impl.Node;
import com.psa.entity.impl.OneDimFiniteElement;
import com.psa.exception.PSAException;

public class StiffnessMatrixBeam3DImpl implements StiffnessMatrixEntity,Serializable {


	private final int DOF = 12;
	//private Double[][] unProcessedStiffnessMatrix = new Double[6][6];
	private Double[][] processedStiffnessMatrix = null;
	
	/**
	 * TransMatrix(Transformation matrix) created by referring 'Computer analysis of framed structures'
	 * by Damodar Maity.
	 */
	double transMatrix[][] = new double[12][12];
	/**
	 * Creating element matrix (UnTransformed).
	 * 
	 */
	double LocalStiffMat[][] = new double[12][12];
	/**
	 * CAUSTION : Never try to sort this this.coOrdinatesOfFreedom.
	 * Because, it's element stiffness matrix is depending on
	 * elements first and second node. Whose coOrdinates may not
	 * be in cronological order.
	 */
	private List<Integer> coOrdinatesOfFreedom = new ArrayList<Integer>();
	
	// Hope deprecated
//	private double angleOfInclination = 0;
//	private double CValue = 0;
//	private double SValue = 0;
//	private double CSValue = 0;
//	private double CsquareValue=0;
//	private double SsquareValue = 0;
	// Area*E/Length value 
	
	private double lengthOfFiniteElement = 0;
	private double crossSectionalArea = 0;
	private double youngsModulus = 0;
	private double AEbyLValue = 0;
	private double Cx=0;
	private double Cy=0;
	private double Cz=0;
	private double MtOfInertiaY;
	private double MtOfInertiaZ;
	private double G;
	private double J=0;//0.000014046; //0.00001666667;
	

	public StiffnessMatrixEntity setStiffnessMatrixForElement(
			FiniteElement oneDimFE)
			throws PPSAStiffnessMatrixExcpetion {

		OneDimFiniteElement oneDimFiniteElement=(OneDimFiniteElement)oneDimFE;
		
		OneDimFiniteElementType finiteElementType = oneDimFiniteElement.getFiniteElementType();
		if(finiteElementType!=OneDimFiniteElementType.BEAM3D)
			throw new PPSAStiffnessMatrixExcpetion("This element is not BEAM_3_D type");

		// Basic PSA -> MathHelper.setBasicOneDimFiniteElementData(oneDimFiniteElement);
		generateBasicData(oneDimFiniteElement);
		createUnProcessedElementMatrices(oneDimFiniteElement);
		generateFinalStiffnessMatrix();

		if (this.coOrdinatesOfFreedom.size() != this.DOF)
			throw new PPSAStiffnessMatrixExcpetion("Lacking D.O.F size");

		//Collections.sort(coOrdinatesOfFreedom);
		oneDimFiniteElement.setStiffnessMatrixEntity(this);
		return this;
	}

	private void generateFinalStiffnessMatrix() {
		/*for (int i = 0; i < DOF; i++) {
			for (int j = 0; j < DOF; j++) {
				processedStiffnessMatrix[i][j]=unProcessedStiffnessMatrix[i][j]*AEbyLValue;
			}
		}*/
		MathOperationJAMAImpl jamaImpl = new MathOperationJAMAImpl();
		double[][] transformedElementMatrix = jamaImpl.getTransformedElementMatrix(transMatrix,LocalStiffMat);
		Double[][] doubleValuedMAtrix = EntityTranslationUtil.getDoubleValuedMAtrix(transformedElementMatrix);
		processedStiffnessMatrix=doubleValuedMAtrix;
	}

	private void createUnProcessedElementMatrices(OneDimFiniteElement oneDimFiniteElement) {
		
		double lamda = getLamda();
		double mu = getMu();
		double nu = getNu();
		
		Node firstNode = oneDimFiniteElement.getFirstNode();
		Node secondNode= oneDimFiniteElement.getSecondNode();
		
		double xDiff = secondNode.getxCordinate()-firstNode.getxCordinate();
		double yDiff = secondNode.getyCordinate()-firstNode.getyCordinate();
		double zDiff = secondNode.getzCordinate()-firstNode.getzCordinate();
		if(xDiff==0 && yDiff!=0 && zDiff==0) {
			transMatrix[0][1]=transMatrix[3][4]=transMatrix[6][7]=transMatrix[9][10]=mu;
			transMatrix[1][0]=transMatrix[4][3]=transMatrix[7][6]=transMatrix[10][9]=-mu;
			transMatrix[2][2]=transMatrix[5][5]=transMatrix[8][8]=transMatrix[11][11]=1.0;
		} else {
			double xDiffSquare = xDiff * xDiff;
			double yDiffSquare = yDiff * yDiff;
			double zDiffSquare = zDiff * zDiff;
			double sqrtOfxDiffSquarePluszDiffSquare = Math.sqrt(xDiffSquare
					+ zDiffSquare);

			double lamdaTwo = - lamda * mu * lengthOfFiniteElement
					/ sqrtOfxDiffSquarePluszDiffSquare;
			double muTwo = sqrtOfxDiffSquarePluszDiffSquare
					/ lengthOfFiniteElement;
			double nuTwo = - mu * nu * lengthOfFiniteElement
					/ sqrtOfxDiffSquarePluszDiffSquare;

			if (Double.isNaN(lamdaTwo))
				lamdaTwo = 0;
			if (Double.isNaN(muTwo))
				muTwo = 0;
			if (Double.isNaN(nuTwo))
				nuTwo = 0;

			double lamdaThree = -nu * lengthOfFiniteElement
					/ sqrtOfxDiffSquarePluszDiffSquare;
			double nuThree = lamda * lengthOfFiniteElement
					/ sqrtOfxDiffSquarePluszDiffSquare;
			double muThree = 1 - (lamdaThree * lamdaThree)
					- (nuThree * nuThree);

			if (Double.isNaN(lamdaThree))
				lamdaThree = 0;
			if (Double.isNaN(muThree))
				muThree = 0;
			if (Double.isNaN(nuThree))
				nuThree = 0;

			// Setting first row vals(first row of smal R matrix).
			transMatrix[0][0] = transMatrix[3][3] = transMatrix[6][6] = transMatrix[9][9] = lamda;
			transMatrix[0][1] = transMatrix[3][4] = transMatrix[6][7] = transMatrix[9][10] = mu;
			transMatrix[0][2] = transMatrix[3][5] = transMatrix[6][8] = transMatrix[9][11] = nu;

			// Setting second row vals(second row of smal R matrix).
			transMatrix[1][0] = transMatrix[4][3] = transMatrix[7][6] = transMatrix[10][9] = lamdaTwo;
			transMatrix[1][1] = transMatrix[4][4] = transMatrix[7][7] = transMatrix[10][10] = muTwo;
			transMatrix[1][2] = transMatrix[4][5] = transMatrix[7][8] = transMatrix[10][11] = nuTwo;

			// Setting Third row vals(Third row of smal R matrix).
			transMatrix[2][0] = transMatrix[5][3] = transMatrix[8][6] = transMatrix[11][9] = lamdaThree;
			transMatrix[2][1] = transMatrix[5][4] = transMatrix[8][7] = transMatrix[11][10] = muThree;
			transMatrix[2][2] = transMatrix[5][5] = transMatrix[8][8] = transMatrix[11][11] = nuThree;
		}
		/**
		 * Creating local element stiff mat.
		 * Without translation. 
		 */
		
		double lenSquare = lengthOfFiniteElement*lengthOfFiniteElement;
		double lenCube = lenSquare * lengthOfFiniteElement;
		
		double const1 = AEbyLValue;
		double const2 = G*J/lengthOfFiniteElement;
		
		double constantY = youngsModulus*MtOfInertiaY;
		double constantZ = youngsModulus*MtOfInertiaZ;
		//40
		LocalStiffMat[0][0]=LocalStiffMat[6][6]= const1;
		LocalStiffMat[0][6]=LocalStiffMat[6][0]= -const1;
		LocalStiffMat[3][3]=LocalStiffMat[9][9]= const2;
		LocalStiffMat[3][9]=LocalStiffMat[9][3]= -const2;
		
		LocalStiffMat[1][1]=LocalStiffMat[7][7]= 12*constantZ/lenCube;
		LocalStiffMat[7][1]=LocalStiffMat[1][7]= -12*constantZ/lenCube;
		
		LocalStiffMat[1][5] = LocalStiffMat[1][11] = LocalStiffMat[5][1] = LocalStiffMat[11][1] = 
				6 * constantZ / lenSquare;
		LocalStiffMat[7][5] = LocalStiffMat[5][7] = LocalStiffMat[7][11] = LocalStiffMat[11][7] = 
				-6 * constantZ / lenSquare;
		
		LocalStiffMat[2][2]=LocalStiffMat[8][8]= 12*constantY/lenCube;
		LocalStiffMat[8][2]=LocalStiffMat[2][8]= -12*constantY/lenCube;
		
		LocalStiffMat[10][2] = LocalStiffMat[4][2] = LocalStiffMat[2][4] = LocalStiffMat[2][10] = 
				-6 * constantY / lenSquare;
		LocalStiffMat[10][8] = LocalStiffMat[4][8] = LocalStiffMat[8][4] = LocalStiffMat[8][10] = 
				6 * constantY / lenSquare;
		
		LocalStiffMat[4][4]=LocalStiffMat[10][10]= 4*constantY/lengthOfFiniteElement;
		LocalStiffMat[5][5]=LocalStiffMat[11][11]= 4*constantZ/lengthOfFiniteElement;
		LocalStiffMat[4][10]=LocalStiffMat[10][4]= 2*constantY/lengthOfFiniteElement;
		LocalStiffMat[5][11]=LocalStiffMat[11][5]= 2*constantZ/lengthOfFiniteElement;
	}

	private void generateBasicData(OneDimFiniteElement oneDimFiniteElement) {
		
		//this.angleOfInclination=oneDimFiniteElement.getAngleOfInclination();
		this.lengthOfFiniteElement=oneDimFiniteElement.getLengthOfElement();
		
		Node firstNode = oneDimFiniteElement.getFirstNode();
		int xCordinateNumFirst = firstNode.getxCordinateNum();
		int yCordinateNumFirst = firstNode.getyCordinateNum();
		int zCordinateNumFirst = firstNode.getzCordinateNum();
		int mxCordinateNumFirst = firstNode.getMxCordinateNum();
		int myCordinateNumFirst = firstNode.getMyCordinateNum();
		int mzCordinateNumFirst = firstNode.getMzCordinateNum();
		
		coOrdinatesOfFreedom.add(xCordinateNumFirst);
		coOrdinatesOfFreedom.add(yCordinateNumFirst);
		coOrdinatesOfFreedom.add(zCordinateNumFirst);
		coOrdinatesOfFreedom.add(mxCordinateNumFirst);
		coOrdinatesOfFreedom.add(myCordinateNumFirst);
		coOrdinatesOfFreedom.add(mzCordinateNumFirst);

		Node secondNode = oneDimFiniteElement.getSecondNode();
		int xCordinateNumSec = secondNode.getxCordinateNum();
		int yCordinateNumSec = secondNode.getyCordinateNum();
		int zCordinateNumSec = secondNode.getzCordinateNum();
		int mxCordinateNumSec = secondNode.getMxCordinateNum();
		int myCordinateNumSec = secondNode.getMyCordinateNum();
		int mzCordinateNumSec = secondNode.getMzCordinateNum();
		coOrdinatesOfFreedom.add(xCordinateNumSec);
		coOrdinatesOfFreedom.add(yCordinateNumSec);
		coOrdinatesOfFreedom.add(zCordinateNumSec);
		coOrdinatesOfFreedom.add(mxCordinateNumSec);
		coOrdinatesOfFreedom.add(myCordinateNumSec);
		coOrdinatesOfFreedom.add(mzCordinateNumSec);

		MaterialProperty materialProperty = oneDimFiniteElement.getMaterialProperty();
		youngsModulus = materialProperty.getE();
		SectionProperty sectionProperty = oneDimFiniteElement.getSectionProperty();
		crossSectionalArea = sectionProperty.getCSArea();
		AEbyLValue=crossSectionalArea*youngsModulus/lengthOfFiniteElement;
		MtOfInertiaY = sectionProperty.getInertialMomentY();
		MtOfInertiaZ = sectionProperty.getInertialMomentZ();
		J=sectionProperty.getPolarMoment();
		G=materialProperty.getG();


		/*// ----------------------
		CValue = Math.cos(Math.toRadians(angleOfInclination));
		SValue = Math.sin(Math.toRadians(angleOfInclination));
		// Set basic C and S value.
		// Because even if angleOfInclination = 90. Cos(toRadians(90)) gives
		// some value close to zero.
		if(angleOfInclination==90)
			CValue=0;
		
		CSValue = CValue * SValue;
		// -----------------------*/

		double cxVal = (secondNode.getxCordinate() - firstNode.getxCordinate())/lengthOfFiniteElement;
		double cyVal = (secondNode.getyCordinate() - firstNode.getyCordinate())/lengthOfFiniteElement;
		double czVal = (secondNode.getzCordinate() - firstNode.getzCordinate())/lengthOfFiniteElement;
		if(Double.isNaN(cxVal))
			cxVal=0;
		if(Double.isNaN(cyVal))
			cyVal=0;
		if(Double.isNaN(czVal))
			czVal=0;
		
		setLamda(cxVal);
		setMu(cyVal);
		setNu(czVal);
		
	}

	public double getLengthOfFiniteElement() {
		return this.lengthOfFiniteElement;
	}

	public double getCrossSectionalArea() {
		return this.crossSectionalArea;
	}

	public double getYoungsModulus() {
		return this.youngsModulus;
	}

	public double getAEByLValue() {
		return this.AEbyLValue;
	}

	@Override
	public List<Integer> getCoOrinatesOfFreedom() {
		return coOrdinatesOfFreedom;
	}

	@Override
	public Integer getDegreeOfFreedom() {

		return DOF;
	}

	@Override
	public Double[][] getStiffnessMatrix() {
		return this.processedStiffnessMatrix;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (Integer coOrdNum : this.coOrdinatesOfFreedom) {
			builder.append(coOrdNum + " ");
		}
		return builder.toString();
	}

	public double getLamda() {
		return Cx;
	}

	public double getMu() {
		return Cy;
	}

	public double getNu() {
		return Cz;
	}
	
	public void setLamda(double cxVal) {
		 this.Cx=cxVal;
	}

	public void setMu(double cyVal) {
		this.Cy=cyVal;
	}

	public void setNu(double czVal) {
		this.Cz=czVal;
	}

	public double[][] getTransformationMatrix() {
		return transMatrix;
		
	}


}

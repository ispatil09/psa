package com.ppsa.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.ppsa.exception.PPSAStiffnessMatrixExcpetion;
import com.psa.entity.FiniteElement;
import com.psa.entity.StiffnessMatrixEntity;
import com.psa.entity.enums.OneDimFiniteElementType;
import com.psa.entity.impl.Node;
import com.psa.entity.impl.OneDimFiniteElement;

public class StiffnessMatrixTruss3DImpl implements StiffnessMatrixEntity,Serializable {

	private final int DOF = 6;
	private Double[][] unProcessedStiffnessMatrix = new Double[6][6];
	private Double[][] processedStiffnessMatrix = new Double[6][6];
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
	
	
	public StiffnessMatrixEntity setStiffnessMatrixForElement(
			FiniteElement oneDimFE)
			throws PPSAStiffnessMatrixExcpetion {

		OneDimFiniteElement oneDimFiniteElement=(OneDimFiniteElement)oneDimFE;
		
		OneDimFiniteElementType finiteElementType = oneDimFiniteElement.getFiniteElementType();
		if(finiteElementType!=OneDimFiniteElementType.TRUSS3D)
			throw new PPSAStiffnessMatrixExcpetion("This element is not TRUSS_3_D type");

		// Basic PSA -> MathHelper.setBasicOneDimFiniteElementData(oneDimFiniteElement);
		generateBasicData(oneDimFiniteElement);
		createUnProcessedElementMatrix();
		generateFinalStiffnessMatrix();
		
		if (this.coOrdinatesOfFreedom.size() != this.DOF)
			throw new PPSAStiffnessMatrixExcpetion("Lacking D.O.F size");

		//Collections.sort(coOrdinatesOfFreedom);
		oneDimFiniteElement.setStiffnessMatrixEntity(this);
		return this;
	}

	private void generateFinalStiffnessMatrix() {
		for (int i = 0; i < DOF; i++) {
			for (int j = 0; j < DOF; j++) {
				processedStiffnessMatrix[i][j]=unProcessedStiffnessMatrix[i][j]*AEbyLValue;
			}
		}
	}

	private void createUnProcessedElementMatrix() {
		double CxSquare = getCx() * getCx();
		double CySquare = getCy() * getCy();
		double CzSquare = getCz() * getCz();
		
		double CxCy = getCx() * getCy();
		double CxCz = getCx() * getCz();
		double CyCz = getCy() * getCz();
		
		
		// double CAndSValue=CValue*CValue;

		// First Column
		unProcessedStiffnessMatrix[0][0] = CxSquare;
		unProcessedStiffnessMatrix[1][0] = CxCy;
		unProcessedStiffnessMatrix[2][0] = CxCz;
		unProcessedStiffnessMatrix[3][0] = -CxSquare;
		unProcessedStiffnessMatrix[4][0] = -CxCy;
		unProcessedStiffnessMatrix[5][0] = -CxCz;

		// Second Column
		unProcessedStiffnessMatrix[0][1] = CxCy;
		unProcessedStiffnessMatrix[1][1] = CySquare;
		unProcessedStiffnessMatrix[2][1] = CyCz;
		unProcessedStiffnessMatrix[3][1] = -CxCy;
		unProcessedStiffnessMatrix[4][1] = -CySquare;
		unProcessedStiffnessMatrix[5][1] = -CyCz;

		// Third Column
		unProcessedStiffnessMatrix[0][2] = CxCz;
		unProcessedStiffnessMatrix[1][2] = CyCz;
		unProcessedStiffnessMatrix[2][2] = CzSquare;
		unProcessedStiffnessMatrix[3][2] = -CxCz;
		unProcessedStiffnessMatrix[4][2] = -CyCz;
		unProcessedStiffnessMatrix[5][2] = -CzSquare;

		// Fourth Column (Same as first col. with only sign change)
		unProcessedStiffnessMatrix[0][3] = -CxSquare;
		unProcessedStiffnessMatrix[1][3] = -CxCy;
		unProcessedStiffnessMatrix[2][3] = -CxCz;
		unProcessedStiffnessMatrix[3][3] = CxSquare;
		unProcessedStiffnessMatrix[4][3] = CxCy;
		unProcessedStiffnessMatrix[5][3] = CxCz;
		
		// Fifth Column (Same as second col. with only sign change)
		unProcessedStiffnessMatrix[0][4] = -CxCy;
		unProcessedStiffnessMatrix[1][4] = -CySquare;
		unProcessedStiffnessMatrix[2][4] = -CyCz;
		unProcessedStiffnessMatrix[3][4] = CxCy;
		unProcessedStiffnessMatrix[4][4] = CySquare;
		unProcessedStiffnessMatrix[5][4] = CyCz;
		
		// Sixth Column (Same as third col. with only sign change)
		unProcessedStiffnessMatrix[0][5] = -CxCz;
		unProcessedStiffnessMatrix[1][5] = -CyCz;
		unProcessedStiffnessMatrix[2][5] = -CzSquare;
		unProcessedStiffnessMatrix[3][5] = CxCz;
		unProcessedStiffnessMatrix[4][5] = CyCz;
		unProcessedStiffnessMatrix[5][5] = CzSquare;
	}

	private void generateBasicData(OneDimFiniteElement oneDimFiniteElement) {
		
		//this.angleOfInclination=oneDimFiniteElement.getAngleOfInclination();
		this.lengthOfFiniteElement=oneDimFiniteElement.getLengthOfElement();
		
		Node firstNode = oneDimFiniteElement.getFirstNode();
		int xCordinateNumFirst = firstNode.getxCordinateNum();
		int yCordinateNumFirst = firstNode.getyCordinateNum();
		int zCordinateNumFirst = firstNode.getzCordinateNum();
		coOrdinatesOfFreedom.add(xCordinateNumFirst);
		coOrdinatesOfFreedom.add(yCordinateNumFirst);
		coOrdinatesOfFreedom.add(zCordinateNumFirst);

		Node secondNode = oneDimFiniteElement.getSecondNode();
		int xCordinateNumSec = secondNode.getxCordinateNum();
		int yCordinateNumSec = secondNode.getyCordinateNum();
		int zCordinateNumSec = secondNode.getzCordinateNum();
		coOrdinatesOfFreedom.add(xCordinateNumSec);
		coOrdinatesOfFreedom.add(yCordinateNumSec);
		coOrdinatesOfFreedom.add(zCordinateNumSec);

		youngsModulus = oneDimFiniteElement.getMaterialProperty().getE();
		crossSectionalArea = oneDimFiniteElement.getSectionProperty().getCSArea();
		AEbyLValue=crossSectionalArea*youngsModulus/lengthOfFiniteElement;

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
		
		setCx((secondNode.getxCordinate() - firstNode.getxCordinate())/lengthOfFiniteElement);
		setCy((secondNode.getyCordinate() - firstNode.getyCordinate())/lengthOfFiniteElement);
		setCz((secondNode.getzCordinate() - firstNode.getzCordinate())/lengthOfFiniteElement);
		
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

	
	public Double[][] getUnProcessedStiffnessMatrix() {
		return this.unProcessedStiffnessMatrix;
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

	public double getCx() {
		return Cx;
	}

	public double getCy() {
		return Cy;
	}

	public double getCz() {
		return Cz;
	}
	
	public void setCx(double cxVal) {
		 this.Cx=cxVal;
	}

	public void setCy(double cyVal) {
		this.Cy=cyVal;
	}

	public void setCz(double czVal) {
		this.Cz=czVal;
	}

}

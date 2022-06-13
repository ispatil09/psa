package com.ppsa.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.ppsa.exception.PPSAStiffnessMatrixExcpetion;
import com.ppsa.math.MathHelper;
import com.psa.entity.CommonNode;
import com.psa.entity.FiniteElement;
import com.psa.entity.StiffnessMatrixEntity;
import com.psa.entity.enums.OneDimFiniteElementType;
import com.psa.entity.impl.Node;
import com.psa.entity.impl.OneDimFiniteElement;

public class StiffnessMatrixTruss2DImpl implements StiffnessMatrixEntity,Serializable {

	private final int DOF = 4;
	private Double[][] unProcessedStiffnessMatrix = new Double[4][4];
	private Double[][] processedStiffnessMatrix = new Double[4][4];
	/**
	 * CAUSTION : Never try to sort this this.coOrdinatesOfFreedom.
	 * Because, it's element stiffness matrix is depending on
	 * elements first and second node. Whose coOrdinates may not
	 * be in cronological order.
	 */
	private List<Integer> coOrdinatesOfFreedom = new ArrayList<Integer>();
	
	private double angleOfInclination = 0;
	private double lengthOfFiniteElement = 0;
	
	private double CValue = 0;
	private double SValue = 0;
	private double CSValue = 0;
	private double crossSectionalArea = 0;
	private double youngsModulus = 0;
	private double CsquareValue=0;
	private double SsquareValue = 0;
	// Area*E/Length value 
	private double AEbyLValue = 0;
	
	
	public StiffnessMatrixEntity setStiffnessMatrixForElement(
			FiniteElement oneDimFE)
			throws PPSAStiffnessMatrixExcpetion {

		OneDimFiniteElement oneDimFiniteElement=(OneDimFiniteElement)oneDimFE;
		
		OneDimFiniteElementType finiteElementType = oneDimFiniteElement.getFiniteElementType();
		if(finiteElementType!=OneDimFiniteElementType.TRUSS2D)
			throw new PPSAStiffnessMatrixExcpetion("This element is not TRUSS2D type");
		
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
		CsquareValue = CValue * CValue;
		SsquareValue = SValue * SValue;
		// double CAndSValue=CValue*CValue;

		// First Column
		unProcessedStiffnessMatrix[0][0] = CsquareValue;
		unProcessedStiffnessMatrix[1][0] = CSValue;
		unProcessedStiffnessMatrix[2][0] = -CsquareValue;
		unProcessedStiffnessMatrix[3][0] = -CSValue;

		// Second Column
		unProcessedStiffnessMatrix[0][1] = CSValue;
		unProcessedStiffnessMatrix[1][1] = SsquareValue;
		unProcessedStiffnessMatrix[2][1] = -CSValue;
		unProcessedStiffnessMatrix[3][1] = -SsquareValue;

		// Third Column
		unProcessedStiffnessMatrix[0][2] = -CsquareValue;
		unProcessedStiffnessMatrix[1][2] = -CSValue;
		unProcessedStiffnessMatrix[2][2] = CsquareValue;
		unProcessedStiffnessMatrix[3][2] = CSValue;

		// Fourth Column
		unProcessedStiffnessMatrix[0][3] = -CSValue;
		unProcessedStiffnessMatrix[1][3] = -SsquareValue;
		unProcessedStiffnessMatrix[2][3] = CSValue;
		unProcessedStiffnessMatrix[3][3] = SsquareValue;
	}

	private void generateBasicData(OneDimFiniteElement oneDimFiniteElement) {
		
		this.angleOfInclination=oneDimFiniteElement.getAngleOfInclination();
		this.lengthOfFiniteElement=oneDimFiniteElement.getLengthOfElement();
		
		int xCordinateNumFirst = oneDimFiniteElement.getFirstNode().getxCordinateNum();
		int yCordinateNumFirst = oneDimFiniteElement.getFirstNode().getyCordinateNum();
		coOrdinatesOfFreedom.add(xCordinateNumFirst);
		coOrdinatesOfFreedom.add(yCordinateNumFirst);

		int xCordinateNumSec = oneDimFiniteElement.getSecondNode().getxCordinateNum();
		int yCordinateNumSec = oneDimFiniteElement.getSecondNode().getyCordinateNum();
		coOrdinatesOfFreedom.add(xCordinateNumSec);
		coOrdinatesOfFreedom.add(yCordinateNumSec);

		youngsModulus = ((OneDimFiniteElement) oneDimFiniteElement)
				.getMaterialProperty().getE();
		crossSectionalArea = ((OneDimFiniteElement) oneDimFiniteElement)
				.getSectionProperty().getCSArea();
		CValue = Math.cos(Math.toRadians(angleOfInclination));
		SValue = Math.sin(Math.toRadians(angleOfInclination));

		AEbyLValue=crossSectionalArea*youngsModulus/lengthOfFiniteElement;
		
		// Set basic C and S value.
		// Because even if angleOfInclination = 90. Cos(toRadians(90)) gives
		// some value close to zero.
		if(angleOfInclination==90)
			CValue=0;

		CSValue = CValue * SValue;
	}

	public double getAngleOfInclination() {
		return this.angleOfInclination;
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

	public double getCValue() {
		return this.CValue;
	}

	public double getSValue() {
		return this.SValue;
	}

	public double getCSquareValue() {
		return this.CsquareValue;
	}
	
	public double getSSquareValue() {
		return this.SsquareValue;
	}
	
	public double getCSValue() {
		return this.CSValue;
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

}

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

public class StiffnessMatrixBeam2DImpl implements StiffnessMatrixEntity,Serializable {

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

	/**
	 * it is angle subtended by first node to second node.
	 * Measured from horizontal right side in anticlockwise direction.
	 * Angle in degrees. If measured in clock wise direction you get
	 * -ve direction value.
	 */
	private double angleOfInclination = 0;
	private double CValue = 0;
	private double SValue = 0;
	private double CSValue = 0;
	private double crossSectionalArea = 0;
	private double momentOfInertia = 0;
	private double lengthOfFiniteElement = 0;
	private double youngsModulus = 0;
	private double CsquareValue = 0;
	private double SsquareValue = 0;
	// E/Length value
	private double EbyLValue = 0;

	/*
	 * Refer "Finite Element Method" by Pearson publication, by 3 authors. page
	 * no. 159 to know about following constants.
	 */

	private double k11;
	private double k12;
	private double k13;
	private double k22;
	private double k23;
	private double k33;

	public StiffnessMatrixEntity setStiffnessMatrixForElement(
			FiniteElement finiteElement) throws PPSAStiffnessMatrixExcpetion {

		OneDimFiniteElement oneDimFiniteElement = (OneDimFiniteElement) finiteElement;

		OneDimFiniteElementType finiteElementType = oneDimFiniteElement
				.getFiniteElementType();
		if (finiteElementType != OneDimFiniteElementType.BEAM2D)
			throw new PPSAStiffnessMatrixExcpetion(
					"This element is not BEAM2D type");

		
		// BasicPSA -> MathHelper.setBasicOneDimFiniteElementData(oneDimFiniteElement); 
		
		generateBasicData(oneDimFiniteElement);
		generateRawKValues();
		createUnProcessedElementMatrix();
		generateFinalStiffnessMatrix();

		if (this.coOrdinatesOfFreedom.size() != this.DOF)
			throw new PPSAStiffnessMatrixExcpetion("Lacking D.O.F size");

		//Collections.sort(coOrdinatesOfFreedom);
		oneDimFiniteElement.setStiffnessMatrixEntity(this);
		return this;
	}

	private void generateRawKValues() {
		// Creating some common terms.
		double TwelveIbyLsquare = (12 * momentOfInertia)
				/ (lengthOfFiniteElement * lengthOfFiniteElement);
		double SixIbyL = 6 * momentOfInertia / lengthOfFiniteElement;

		k11 = (crossSectionalArea * CsquareValue)
				+ (TwelveIbyLsquare * SsquareValue);
		k12 = (crossSectionalArea - TwelveIbyLsquare) * CSValue;
		k13 = -SixIbyL * SValue;
		k22 = (crossSectionalArea * SsquareValue)
				+ (TwelveIbyLsquare * CsquareValue);
		k23 = SixIbyL * CValue;
		k33 = 4 * momentOfInertia;

	}

	private void generateFinalStiffnessMatrix() {
		for (int i = 0; i < DOF; i++) {
			for (int j = 0; j < DOF; j++) {
				processedStiffnessMatrix[i][j]=0d;
				processedStiffnessMatrix[i][j] = unProcessedStiffnessMatrix[i][j] * EbyLValue;
			}
		}

	}

	private void createUnProcessedElementMatrix() {
		// First Column
		unProcessedStiffnessMatrix[0][0] = k11;
		unProcessedStiffnessMatrix[1][0] = k12;
		unProcessedStiffnessMatrix[2][0] = k13;
		unProcessedStiffnessMatrix[3][0] = -k11;
		unProcessedStiffnessMatrix[4][0] = -k12;
		unProcessedStiffnessMatrix[5][0] = k13;

		// Second Column
		unProcessedStiffnessMatrix[0][1] = k12;
		unProcessedStiffnessMatrix[1][1] = k22;
		unProcessedStiffnessMatrix[2][1] = k23;
		unProcessedStiffnessMatrix[3][1] = -k12;
		unProcessedStiffnessMatrix[4][1] = -k22;
		unProcessedStiffnessMatrix[5][1] = k23;

		// Third Column
		unProcessedStiffnessMatrix[0][2] = k13;
		unProcessedStiffnessMatrix[1][2] = k23;
		unProcessedStiffnessMatrix[2][2] = k33;
		unProcessedStiffnessMatrix[3][2] = -k13;
		unProcessedStiffnessMatrix[4][2] = -k23;
		unProcessedStiffnessMatrix[5][2] = 0.5 * k33;

		// Fourth Column
		unProcessedStiffnessMatrix[0][3] = -k11;
		unProcessedStiffnessMatrix[1][3] = -k12;
		unProcessedStiffnessMatrix[2][3] = -k13;
		unProcessedStiffnessMatrix[3][3] = k11;
		unProcessedStiffnessMatrix[4][3] = k12;
		unProcessedStiffnessMatrix[5][3] = -k13;

		// Fifth Column
		unProcessedStiffnessMatrix[0][4] = -k12;
		unProcessedStiffnessMatrix[1][4] = -k22;
		unProcessedStiffnessMatrix[2][4] = -k23;
		unProcessedStiffnessMatrix[3][4] = k12;
		unProcessedStiffnessMatrix[4][4] = k22;
		unProcessedStiffnessMatrix[5][4] = -k23;
		
		// Sixth Column
		unProcessedStiffnessMatrix[0][5] = k13;
		unProcessedStiffnessMatrix[1][5] = k23;
		unProcessedStiffnessMatrix[2][5] = 0.5*k33;
		unProcessedStiffnessMatrix[3][5] = -k13;
		unProcessedStiffnessMatrix[4][5] = -k23;
		unProcessedStiffnessMatrix[5][5] = k33;
	}

	private void generateBasicData(OneDimFiniteElement oneDimFiniteElement) {
		
		this.angleOfInclination=oneDimFiniteElement.getAngleOfInclination();
		this.lengthOfFiniteElement=oneDimFiniteElement.getLengthOfElement();

		Node firstNode = oneDimFiniteElement.getFirstNode();
		int xCordinateNumFirst = firstNode.getxCordinateNum();
		int yCordinateNumFirst = firstNode.getyCordinateNum();
		int mzCordinateNumFirst = firstNode.getMzCordinateNum();
		coOrdinatesOfFreedom.add(xCordinateNumFirst);
		coOrdinatesOfFreedom.add(yCordinateNumFirst);
		coOrdinatesOfFreedom.add(mzCordinateNumFirst);

		Node secondNode = oneDimFiniteElement.getSecondNode();
		int xCordinateNumSec = secondNode.getxCordinateNum();
		int yCordinateNumSec = secondNode.getyCordinateNum();
		int mzCordinateNumSec = secondNode.getMzCordinateNum();
		coOrdinatesOfFreedom.add(xCordinateNumSec);
		coOrdinatesOfFreedom.add(yCordinateNumSec);
		coOrdinatesOfFreedom.add(mzCordinateNumSec);
		
		
		/*for (CommonNode node : oneDimFiniteElement.getNodes()) {
			int xCordinateNumFirst = node.getxCordinateNum();
			int yCordinateNumFirst = node.getyCordinateNum();
			int mzCordinateNumFirst = node.getMzCordinateNum();
			coOrdinatesOfFreedom.add(xCordinateNumFirst);
			coOrdinatesOfFreedom.add(yCordinateNumFirst);
			coOrdinatesOfFreedom.add(mzCordinateNumFirst);
		}*/
		

		youngsModulus = oneDimFiniteElement.getMaterialProperty().getE();
		crossSectionalArea = oneDimFiniteElement.getSectionProperty()
				.getCSArea();
		momentOfInertia = oneDimFiniteElement.getSectionProperty()
				.getInertialMomentZ();
		CValue = Math.cos(Math.toRadians(angleOfInclination));
		SValue = Math.sin(Math.toRadians(angleOfInclination));

		EbyLValue = youngsModulus / lengthOfFiniteElement;

		// Set basic C and S value.
		// Because even if angleOfInclination = 90. Cos(toRadians(90)) gives
		// some value close to zero.
		if ((angleOfInclination == 90)||(angleOfInclination == -90)) {
			CValue = 0;
		}

		CsquareValue = CValue * CValue;
		SsquareValue = SValue * SValue;
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

	public double getEByLValue() {
		return this.EbyLValue;
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

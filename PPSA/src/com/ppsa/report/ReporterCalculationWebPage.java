package com.ppsa.report;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ppsa.entity.StiffnessMatrixBeam2DImpl;
import com.ppsa.entity.StiffnessMatrixTruss2DImpl;
import com.psa.entity.FiniteElement;
import com.psa.entity.GlobalStiffnessMatrixEntity;
import com.psa.entity.LoadCase;
import com.psa.entity.OneDAxialForceEntity;
import com.psa.entity.StiffnessMatrixEntity;
import com.psa.entity.VectorMatrix;
import com.psa.entity.enums.OneDimFiniteElementType;
import com.psa.entity.impl.OneDimFiniteElement;
import com.psa.entity.impl.Structure;
import com.psa.exception.PSAException;
import com.psa.io.DataCopyUtil;

public class ReporterCalculationWebPage {/*

	public static void generateWebReport(Structure structure)
			throws PSAException {

		String probName = structure.getProblemName();
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(WebPageReportConstantStrings.HEAD);
		stringBuilder.append(probName);
		stringBuilder.append(WebPageReportConstantStrings.HEAD_CLOSE);

		// Write about structure details
		getContentOfWebPage(stringBuilder, structure);

		stringBuilder.append(WebPageReportConstantStrings.CLOSE_BODY);
		new DataCopyUtil().ceateFileAndSaveText(probName,
				stringBuilder.toString());
	}

	private static void getContentOfWebPage(StringBuilder stringBuilder,
			Structure structure) {
		writeStiffnessMatrixDetailsForElements(stringBuilder, structure);
		writeGlobalStiffnessMatrix(stringBuilder, structure);
		writeDisplacementVector(stringBuilder, structure);
		writeForceVectorsForVariousLoadCases(stringBuilder, structure);
		writeReducedDisplaceMentVector(stringBuilder, structure);
		writeReducedGlobalStiffnessMatrix(stringBuilder, structure);
		writeReducedLoadCases(stringBuilder, structure);
		writeDisplacementResults(stringBuilder, structure);
		writeFullDisplacementVextor(stringBuilder, structure);
		writeSupportReactions(stringBuilder, structure);
		writeMemAxialForces(stringBuilder, structure);
	}

	private static void writeMemAxialForces(StringBuilder stringBuilder,
			Structure structure) {
		Set<Integer> loadCaseKeySet = structure.getLoadCases().keySet();
		for (Integer loadCaseNum : loadCaseKeySet) {
			LoadCase loadCase = structure.getLoadCase(loadCaseNum);
			writeMemAxialForcesForLoadCase(structure, loadCase, stringBuilder);

		}
	}

	private static void writeMemAxialForcesForLoadCase(Structure structure,
			LoadCase loadCase, StringBuilder stringBuilder) {

		stringBuilder
				.append("<h2><font color=\"blue\"><i>Member Axial forces For Load Case : "
						+ loadCase.getLoadCaseNum() + "</i></font></h2>\n");

		Map<OneDimFiniteElement, OneDAxialForceEntity> axialForceEntities = loadCase
				.getAxialForceEntities();
		Set<OneDimFiniteElement> keySet = axialForceEntities.keySet();

		for (OneDimFiniteElement oneDimFiniteElement : keySet) {
			stringBuilder.append("<h3> For Element : "
					+ oneDimFiniteElement.getElementNumber() + "</h3>\n");
			OneDAxialForceEntity axialForceEntity = axialForceEntities
					.get(oneDimFiniteElement);
			stringBuilder.append("<h3> A*E/L : "
					+ axialForceEntity.getAEbyLval() + "</h3>\n");
			stringBuilder.append("<h3> C and S Value vector : </h3>");
			writeGlobalForceVector(stringBuilder,
					axialForceEntity.getCSValueVector());

			stringBuilder
					.append("<h3> Associated Node  Displacement Vector : </h3>");
			stringBuilder.append("<h3>"
					+ axialForceEntity.getNodesAssociatedDispVector()
							.getCoOrdinatedOfFreedom() + "</h3>\n");
			writeGlobalForceVector(stringBuilder,
					axialForceEntity.getNodesAssociatedDispVector());

			stringBuilder.append("<h3> Axial Force : </h3>");
			stringBuilder.append("<h3><font color=\"purple\">"
					+ axialForceEntity.getAxialForce() + "</font></h3>\n");
		}
	}

	private static void writeSupportReactions(StringBuilder stringBuilder,
			Structure structure) {
		Set<Integer> loadCaseKeySet = structure.getLoadCases().keySet();
		for (Integer loadCaseNum : loadCaseKeySet) {
			LoadCase loadCase = structure.getLoadCase(loadCaseNum);
			writeSupportReactionsForLoadCase(structure, loadCase, stringBuilder);
		}
	}

	private static void writeSupportReactionsForLoadCase(Structure structure,
			LoadCase loadCase, StringBuilder stringBuilder) {

		VectorMatrix supportReactionVector = loadCase
				.getSupportReactionsVector();
		stringBuilder
				.append("<h2><font color=\"blue\"><i>Support Reactions For Load Case : "
						+ loadCase.getLoadCaseNum() + "</i></font></h2>\n");
		stringBuilder.append("<h3>"
				+ supportReactionVector.getCoOrdinatedOfFreedom() + "</h3>\n");
		writeGlobalForceVector(stringBuilder, supportReactionVector);

	}

	private static void writeFullDisplacementVextor(
			StringBuilder stringBuilder, Structure structure) {
		Set<Integer> loadCaseKeySet = structure.getLoadCases().keySet();
		for (Integer loadCaseNum : loadCaseKeySet) {
			LoadCase loadCase = structure.getLoadCase(loadCaseNum);
			writeFullDisplacementVectorForLoadCase(structure, loadCase,
					stringBuilder);
		}
	}

	private static void writeFullDisplacementVectorForLoadCase(
			Structure structure, LoadCase loadCase, StringBuilder stringBuilder) {

		VectorMatrix fullDispVector = loadCase.getFullNodalDisplacementVector();
		stringBuilder
				.append("<h2><font color=\"blue\"><i>Full Displacement Vector For Load Case : "
						+ loadCase.getLoadCaseNum() + "</i></font></h2>\n");
		stringBuilder.append("<h3>" + fullDispVector.getCoOrdinatedOfFreedom()
				+ "</h3>\n");
		writeGlobalForceVector(stringBuilder, fullDispVector);

	}

	private static void writeDisplacementResults(StringBuilder stringBuilder,
			Structure structure) {
		Set<Integer> loadCaseKeySet = structure.getLoadCases().keySet();
		for (Integer loadCaseNum : loadCaseKeySet) {
			LoadCase loadCase = structure.getLoadCase(loadCaseNum);
			writeDisplacementForLoadCase(structure, loadCase, stringBuilder);
		}
	}

	private static void writeDisplacementForLoadCase(Structure structure,
			LoadCase loadCase, StringBuilder stringBuilder) {

		VectorMatrix nodalDispVector = loadCase
				.getReducedNodalDisplacementVector();
		stringBuilder
				.append("<h2><font color=\"blue\"><i>Displacement Vector For Load Case : "
						+ loadCase.getLoadCaseNum() + "</i></font></h2>\n");
		stringBuilder.append("<h3>" + nodalDispVector.getCoOrdinatedOfFreedom()
				+ "</h3>\n");
		writeGlobalForceVector(stringBuilder, nodalDispVector);

	}

	private static void writeReducedLoadCases(StringBuilder stringBuilder,
			Structure structure) {
		Set<Integer> loadCaseKeySet = structure.getLoadCases().keySet();
		for (Integer loadCaseNum : loadCaseKeySet) {
			LoadCase loadCase = structure.getLoadCase(loadCaseNum);
			writeReducedGlobalForceVector(structure, loadCase, stringBuilder);
		}

	}

	private static void writeReducedGlobalForceVector(Structure structure,
			LoadCase loadCase, StringBuilder stringBuilder) {

		VectorMatrix reducedGlobalForceVector = loadCase
				.getReducedGlobalForceVector();
		stringBuilder
				.append("<h2><font color=\"blue\"><i>Reduced Force Vector For Load Case : "
						+ loadCase.getLoadCaseNum() + "</i></font></h2>\n");
		stringBuilder.append("<h3>"
				+ reducedGlobalForceVector.getCoOrdinatedOfFreedom()
				+ "</h3>\n");
		writeGlobalForceVector(stringBuilder, reducedGlobalForceVector);

	}

	private static void writeReducedGlobalStiffnessMatrix(
			StringBuilder stringBuilder, Structure structure) {
		GlobalStiffnessMatrixEntity reducedGlobalStiffnessMatrix = structure
				.getReducedGlobalStiffnessMatrix();
		List<Integer> reducedCoOrinatesOfFreedom = reducedGlobalStiffnessMatrix
				.getGlobalCoOrinatesOfFreedom();
		stringBuilder
				.append(" GLOBAL STIFFNESS MATRIX : Reduced Co Free..</br>\n");
		stringBuilder.append("" + reducedCoOrinatesOfFreedom + "</br>\n");
		writeElementStiffnessMatrix(stringBuilder,
				reducedGlobalStiffnessMatrix.getGlobalStiffnessMatrix(),
				reducedGlobalStiffnessMatrix.getGlobalDegreeOfFreedom());

	}

	private static void writeReducedDisplaceMentVector(
			StringBuilder stringBuilder, Structure structure) {
		VectorMatrix reducedDisplacementVectorMatrix = structure
				.getReducedDisplacementVectorMatrix();
		stringBuilder
				.append("<h2><font color=\"blue\"><i>Reduced Global Displacement Vector</i></font></h2>\n");
		stringBuilder.append("<h3>"
				+ reducedDisplacementVectorMatrix.getCoOrdinatedOfFreedom()
				+ "</h3>\n");

		writeGlobalDisplacementVector(stringBuilder,
				reducedDisplacementVectorMatrix);

	}

	private static void writeForceVectorsForVariousLoadCases(
			StringBuilder stringBuilder, Structure structure) {
		Set<Integer> loadCaseKeySet = structure.getLoadCases().keySet();
		for (Integer loadCaseNum : loadCaseKeySet) {
			LoadCase loadCase = structure.getLoadCase(loadCaseNum);
			writeGlobalForceVector(structure, loadCase, stringBuilder);
		}

	}

	private static void writeGlobalForceVector(Structure structure,
			LoadCase loadCase, StringBuilder stringBuilder) {
		VectorMatrix globalForceVector = loadCase.getGlobalForceVector();
		stringBuilder
				.append("<h2><font color=\"blue\"><i>Global Force Vector For Load Case : "
						+ loadCase.getLoadCaseNum() + "</i></font></h2>\n");
		stringBuilder.append("<h3>"
				+ globalForceVector.getCoOrdinatedOfFreedom() + "</h3>\n");
		writeGlobalForceVector(stringBuilder, globalForceVector);

	}

	private static void writeGlobalForceVector(StringBuilder stringBuilder,
			VectorMatrix vectorMatrix) {
		List<Integer> coOrdinatedOfFreedom = vectorMatrix
				.getCoOrdinatedOfFreedom();
		Map<Integer, Double> vectorValues = vectorMatrix.getVectorValues();
		for (Integer coOrNum : coOrdinatedOfFreedom) {
			Double forceValue = vectorValues.get(coOrNum);

			stringBuilder.append(forceValue + "<br/>\n");
		}

	}

	private static void writeDisplacementVector(StringBuilder stringBuilder,
			Structure structure) {
		VectorMatrix displacementVectorMatrix = structure
				.getDisplacementVectorMatrix();
		stringBuilder
				.append("<h2><font color=\"blue\"><i>Global Displacement Vector</i></font></h2>\n");
		stringBuilder.append("<h3>"
				+ displacementVectorMatrix.getCoOrdinatedOfFreedom()
				+ "</h3>\n");

		writeGlobalDisplacementVector(stringBuilder, displacementVectorMatrix);

	}

	private static void writeGlobalDisplacementVector(StringBuilder builder,
			VectorMatrix displacementVectorMatrix) {
		List<Integer> coOrdinatedOfFreedom = displacementVectorMatrix
				.getCoOrdinatedOfFreedom();
		Map<Integer, Double> vectorValues = displacementVectorMatrix
				.getVectorValues();
		for (Integer coOrNum : coOrdinatedOfFreedom) {
			Double displacementValue = vectorValues.get(coOrNum);
			String dispString = displacementValue + "";
			if (displacementValue != 0) {
				if (displacementValue == 1)
					dispString = "d" + coOrNum;
				else
					dispString=displacementValue+" : stiffness";
			}
					builder.append(dispString + "<br/>\n");
		}

	}

	private static void writeGlobalStiffnessMatrix(StringBuilder stringBuilder,
			Structure structure) {
		stringBuilder
				.append("<h2><font color=\"blue\"><i>Global Stiffness Matrix [Assembled] </i></font></h2>\n");
		GlobalStiffnessMatrixEntity globalStiffnessMatrix = structure
				.getGlobalStiffnessMatrix();

		writeGlobalStiffnessMatrix(stringBuilder, globalStiffnessMatrix);
	}

	private static void writeGlobalStiffnessMatrix(StringBuilder stringBuilder,
			GlobalStiffnessMatrixEntity globalStiffnessMatrix) {
		List<Integer> coOrinatesOfFreedom = globalStiffnessMatrix
				.getGlobalCoOrinatesOfFreedom();
		Double[][] stiffnessMatrix = globalStiffnessMatrix
				.getGlobalStiffnessMatrix();

		stringBuilder.append("<h3>" + coOrinatesOfFreedom + "</h3>\n");
		for (int i = 0; i < coOrinatesOfFreedom.size(); i++) {
			for (int j = 0; j < coOrinatesOfFreedom.size(); j++) {
				stringBuilder.append(stiffnessMatrix[i][j] + " # ");
			}
			stringBuilder.append("<br/>\n");
		}
	}

	private static void writeStiffnessMatrixDetailsForElements(
			StringBuilder stringBuilder, Structure structure) {
		for (FiniteElement element : structure.getFiniteElements()) {
			StiffnessMatrixEntity stiffnessMatrixEntity = element
					.getStiffnessMatrixEntity();
			int elementNum = element.getElementNumber();

			stringBuilder.append("<h2>Finite Element : " + elementNum
					+ "</h2>\n");
			writeBasicDataOfStiffnessMatrix(stringBuilder,
					stiffnessMatrixEntity,element);
			stringBuilder
					.append("<h3><font color=\"blue\"><i>Element Stiffness Matrix </i></font></h3>\n");
			stringBuilder.append("<h3>"
					+ stiffnessMatrixEntity.getCoOrinatesOfFreedom()
					+ "</h3>\n");
			writeGeneralElementStiffnessMatrixUnProcessed(stringBuilder,
					stiffnessMatrixEntity);
			stringBuilder
					.append("<h3><font color=\"blue\"><i>Final Stiffness Matrix </i></font></h3>\n");
			stringBuilder.append("<h3>"
					+ stiffnessMatrixEntity.getCoOrinatesOfFreedom()
					+ "</h3>\n");
			writeGeneralElementStiffnessMatrixProcessed(stringBuilder,
					stiffnessMatrixEntity);
		}
	}

	private static void writeGeneralElementStiffnessMatrixProcessed(
			StringBuilder stringBuilder,
			StiffnessMatrixEntity stiffnessMatrixEntity) {
		if(stiffnessMatrixEntity instanceof StiffnessMatrixTruss2DImpl) {
		writeElementStiffnessMatrix(stringBuilder,
				((StiffnessMatrixTruss2DImpl) stiffnessMatrixEntity)
						.getStiffnessMatrix(),
				stiffnessMatrixEntity.getDegreeOfFreedom());
		} else if(stiffnessMatrixEntity instanceof StiffnessMatrixBeam2DImpl) {
			writeElementStiffnessMatrix(stringBuilder,
					((StiffnessMatrixBeam2DImpl) stiffnessMatrixEntity)
							.getStiffnessMatrix(),
					stiffnessMatrixEntity.getDegreeOfFreedom());
			}
		
	}

	private static void writeGeneralElementStiffnessMatrixUnProcessed(
			StringBuilder stringBuilder,
			StiffnessMatrixEntity stiffnessMatrixEntity) {
		if (stiffnessMatrixEntity instanceof StiffnessMatrixTruss2DImpl) {
			writeElementStiffnessMatrix(stringBuilder,
					((StiffnessMatrixTruss2DImpl) stiffnessMatrixEntity)
							.getUnProcessedStiffnessMatrix(),
					stiffnessMatrixEntity.getCoOrinatesOfFreedom().size());
		} else if (stiffnessMatrixEntity instanceof StiffnessMatrixBeam2DImpl) {
			writeElementStiffnessMatrix(stringBuilder,
					((StiffnessMatrixBeam2DImpl) stiffnessMatrixEntity)
							.getUnProcessedStiffnessMatrix(),
					stiffnessMatrixEntity.getCoOrinatesOfFreedom().size());
		}

	}

	private static void writeBasicDataOfStiffnessMatrix(StringBuilder builder,
			StiffnessMatrixEntity stiffnessMatrixEntity, FiniteElement element) {

		OneDimFiniteElement oneDimFiniteElement = (OneDimFiniteElement) element;
		OneDimFiniteElementType finiteElementType = oneDimFiniteElement.getFiniteElementType();
		
		if(finiteElementType==OneDimFiniteElementType.TRUSS2D) {
		StiffnessMatrixTruss2DImpl stiffnessMatrix = (StiffnessMatrixTruss2DImpl) stiffnessMatrixEntity;
		extractDataFrom2DTrussStiffnessMatrix(builder, stiffnessMatrix);
		} else if(finiteElementType==OneDimFiniteElementType.BEAM2D) {
			StiffnessMatrixBeam2DImpl stiffnessMatrix = (StiffnessMatrixBeam2DImpl) stiffnessMatrixEntity;
			extractDataFrom2DBeamStiffnessMatrix(builder, stiffnessMatrix);
			}

	}

	private static void extractDataFrom2DBeamStiffnessMatrix(
			StringBuilder builder, StiffnessMatrixBeam2DImpl stiffnessMatrixBeam2DImpl) {
		builder.append("<h3><font color=\"red\">Angle = "
				+ stiffnessMatrixBeam2DImpl.getAngleOfInclination()
				+ "</h3>\n");
		builder.append("<h3>Length = "
				+ stiffnessMatrixBeam2DImpl.getLengthOfFiniteElement()
				+ "</h3>\n");
		builder.append("<h3>E = "
				+ stiffnessMatrixBeam2DImpl.getYoungsModulus() + "</h3>\n");

		builder.append("<h3>CS Area = "
				+ stiffnessMatrixBeam2DImpl.getCrossSectionalArea()
				+ "</h3>\n");

		builder.append("<h3>C = Cos(Angle) = "
				+ stiffnessMatrixBeam2DImpl.getCValue() + "</h3>\n");
		builder.append("<h3>C^2 =  "
				+ stiffnessMatrixBeam2DImpl.getCSquareValue() + "</h3>\n");
		builder.append("<h3>S = Sin(Angle) = "
				+ stiffnessMatrixBeam2DImpl.getSValue() + "</h3>\n");
		builder.append("<h3>S^2 = "
				+ stiffnessMatrixBeam2DImpl.getSSquareValue() + "</h3>\n");
		builder.append("<h3>CS = Sin(Angle)XCos(Angle) = "
				+ stiffnessMatrixBeam2DImpl.getCSValue() + "</h3>\n");

		builder.append("<h3>E/L = "
				+ stiffnessMatrixBeam2DImpl.getEByLValue() + "</font></h3>\n");
	}

	private static void extractDataFrom2DTrussStiffnessMatrix(StringBuilder builder,
			StiffnessMatrixTruss2DImpl stiffnessMatrixTruss2DImpl) {
		builder.append("<h3><font color=\"red\">Angle = "
				+ stiffnessMatrixTruss2DImpl.getAngleOfInclination()
				+ "</h3>\n");
		builder.append("<h3>Length = "
				+ stiffnessMatrixTruss2DImpl.getLengthOfFiniteElement()
				+ "</h3>\n");
		builder.append("<h3>E = "
				+ stiffnessMatrixTruss2DImpl.getYoungsModulus() + "</h3>\n");

		builder.append("<h3>CS Area = "
				+ stiffnessMatrixTruss2DImpl.getCrossSectionalArea()
				+ "</h3>\n");

		builder.append("<h3>C = Cos(Angle) = "
				+ stiffnessMatrixTruss2DImpl.getCValue() + "</h3>\n");
		builder.append("<h3>C^2 =  "
				+ stiffnessMatrixTruss2DImpl.getCSquareValue() + "</h3>\n");
		builder.append("<h3>S = Sin(Angle) = "
				+ stiffnessMatrixTruss2DImpl.getSValue() + "</h3>\n");
		builder.append("<h3>S^2 = "
				+ stiffnessMatrixTruss2DImpl.getSSquareValue() + "</h3>\n");
		builder.append("<h3>CS = Sin(Angle)XCos(Angle) = "
				+ stiffnessMatrixTruss2DImpl.getCSValue() + "</h3>\n");

		builder.append("<h3>AxE/L = "
				+ stiffnessMatrixTruss2DImpl.getAEByLValue() + "</font></h3>\n");
	}

	private static void writeElementStiffnessMatrix(
			StringBuilder stringBuilder, Double[][] matrix, int size) {

		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				stringBuilder.append(matrix[i][j] + " # ");
			}
			stringBuilder.append("<br/>\n");
		}
	}
*/}

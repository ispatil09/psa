package com.ppsa.test.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ppsa.exception.PPSAException;
import com.ppsa.exception.PPSAIOException;
import com.ppsa.math.MathHelper;
import com.ppsa.util.OpenPSA;
import com.ppsa.util.POIUtil;
import com.psa.entity.FiniteElement;
import com.psa.entity.impl.PSASpecificCommands;
import com.psa.entity.impl.Structure;
import com.psa.exception.PSAStructureInstantiationException;
import com.psa.io.EnvFileIOUtil;

public class ExcelOutputTest {
	private static final double PRECISION = 0.001;

	private static final double PRECISION_HIGH = 5.0; // Percentage

	private static final double PRECISION_SENSITIVE = 0.1;

	private String problemName;
	private String problemExcelFilePath;
	private String testReporterExcelFilePath;

	private XSSFWorkbook workBook;
	private XSSFSheet workSheet;

	private XSSFWorkbook testWorkBook;
	private XSSFSheet testWorkSheet;

	private List<Integer> loadCaseNums;

	public void testStructure(String probName, Path problemPath, PSASpecificCommands psaSpecificCommands) throws 
			PPSAIOException, PPSAException {
		this.problemName = probName;
		String environmentPath = EnvFileIOUtil.getEnvironmentPath();

		problemExcelFilePath = environmentPath + "\\" + probName + "\\"
				+ probName + "_Test.xlsx";
		Path excelFilePath = Paths.get(problemExcelFilePath);
		File excelFile = excelFilePath.toFile();

		testReporterExcelFilePath = environmentPath + "\\TestReport.xlsx";
		Path testExcelFilePath = Paths.get(testReporterExcelFilePath);
		File testExcelFile = testExcelFilePath.toFile();

		prepareWorkBookObject(excelFile, testExcelFile);

		/*
		 * Structure structure = PPSADataCopyUtil
		 * .getPostProcessedStructureFromFile(probName);
		 */

		Path stdFilePathToReadFromEnv = EnvFileIOUtil.getSTDFilePathToReadFromEnv(probName);
		if(problemPath==null)
			problemPath=stdFilePathToReadFromEnv;
		Structure structure = OpenPSA
				.postProcessStructure(problemPath);

		performTest(structure);
		
	}

	private void prepareWorkBookObject(File excelFile, File testExcelFile) throws PPSAIOException
			{
		try {
			if(excelFile.exists())
				workBook = new XSSFWorkbook(new FileInputStream(excelFile));
			else {
				System.out.println("File \""+excelFile.toPath()+"\" not found");
				System.out.println("SUGGESTION : Run Excel macro first (STAAD.pro should be installed for Macro to work)");
			}
			if(testExcelFile.exists())
				testWorkBook = new XSSFWorkbook(new FileInputStream(testExcelFile));
		} catch (IOException e) {
			throw new PPSAIOException(e.getMessage());
		}
	}

	private void performTest(Structure structure) throws PPSAException {

		if (testWorkBook != null) {
			testWorkSheet = testWorkBook.getSheet("Reactions");
			// Initialize value of testReport for reaction fail to '0'.
			POIUtil.refreshTestReactionEntry(0, this.problemName,
					testWorkSheet, structure);
		}
		checkSupportReactions(structure);
		//checkMemberAnswers(structure);
		checkMemberAnswersCompleteSectionForces(structure);
		checkNodeDisplacements(structure);
		
		performWriteOperationToFile();

	}
	
	private void checkNodeDisplacements(Structure structure) throws PPSAException {
		workSheet = workBook.getSheet("N_Disp");

		//int numOfLoadCases = POIUtil.getIntAtCell(0, 1, workSheet);
		int numOfNodes = POIUtil.getIntAtCell(1, 1, workSheet);

		List<ReactionTestEntity> supportNumList = getSupportsList(numOfNodes);
		//loadCaseNums = getLoadCaseNums(numOfLoadCases);

		// POIUtil.checkStringInColumn(3, "mY", workSheet);

		for (ReactionTestEntity reactionTestEntity : supportNumList) {
			checkSingleNodeDisplacement(reactionTestEntity, loadCaseNums,
					structure);
		}
	}

	private void checkSingleNodeDisplacement(
			ReactionTestEntity dispTestEntity, List<Integer> loadCaseNums2,
			Structure structure) throws PPSAException {
		int nodeNum = dispTestEntity.nodeNum;
		int rowInd = dispTestEntity.rowIndex;
		// int colInd = reactionTestEntity.colIndex;

		int i = 0;
		for (Integer loadCaseNum : loadCaseNums) {
			double[] nodeDisplacements = null;
			try {
				nodeDisplacements = OpenPSA
						.getNodeDisplacements (structure, loadCaseNum, nodeNum);
			} catch (PSAStructureInstantiationException e) {
				throw new PPSAException("Cant get displacement ; node : " + nodeNum);
			}
			POIUtil.writeToCell(rowInd + 0, 5 + 4 * i, nodeDisplacements[0]*1000,
					workSheet);
			POIUtil.writeToCell(rowInd + 1, 5 + 4 * i, nodeDisplacements[1]*1000,
					workSheet);
			POIUtil.writeToCell(rowInd + 2, 5 + 4 * i, nodeDisplacements[2]*1000,
					workSheet);

			POIUtil.writeToCell(rowInd + 3, 5 + 4 * i, nodeDisplacements[3],
					workSheet);
			POIUtil.writeToCell(rowInd + 4, 5 + 4 * i, nodeDisplacements[4],
					workSheet);
			POIUtil.writeToCell(rowInd + 5, 5 + 4 * i, nodeDisplacements[5],
					workSheet);

			// Now perform subtraction. And compare.
			performReactionComparisonDisp(rowInd, i, nodeDisplacements[0]*1000, 0);
			performReactionComparisonDisp(rowInd, i, nodeDisplacements[1]*1000, 1);
			performReactionComparisonDisp(rowInd, i, nodeDisplacements[2]*1000, 2);

			performReactionComparisonDisp(rowInd, i, nodeDisplacements[3], 3);
			performReactionComparisonDisp(rowInd, i, nodeDisplacements[4], 4);
			performReactionComparisonDisp(rowInd, i, nodeDisplacements[5], 5);

			i++;
		}
	}

	private void checkMemberAnswersCompleteSectionForces(Structure structure) throws PPSAException {

		Set<FiniteElement> finiteElements = structure.getFiniteElements();
		int elementCount = 0;
		for (FiniteElement finiteElement : finiteElements) {
			elementCount++;
			int elementNumber = finiteElement.getElementNumber();
			workSheet = workBook.getSheet("FE " + elementNumber);

			/*
			 * String elementType = POIUtil.getStringAtCell(0, 0, workSheet); if
			 * (elementType.equalsIgnoreCase("TRUSS")) {
			 */
			int i = 0;
			for (Integer loadCaseNum : loadCaseNums) {
				/*
				 * double axialForce =
				 * OpenPSA.getAxialForceForElement(structure, loadCaseNum,
				 * elementNumber); POIUtil.writeToCell(2, 4 + 5 * i, axialForce,
				 * workSheet); performAxialForceComparison(2, i, elementCount,
				 * axialForce);
				 */

				/**
				 * Moment check
				 */

				for (int j = 0; j <= 12; j++) {
					double ratio = (j / 12d);

					double[] sectionForces = OpenPSA
							.getIntermediateMemberForcesAtDistance(structure,
									elementNumber, ratio, loadCaseNum);
					double[] transDisplacements = OpenPSA.getIntermediateMemberTransDisplacements(structure,
									elementNumber, ratio, loadCaseNum);
					/**
					 * BMZ results. 
					 */
					{
						POIUtil.writeToCell(7 + j, 4 + 5 * i, sectionForces[5],
								workSheet);
						performAxialForceComparisonNonMod(7 + j, i,
								elementCount, sectionForces[5]);
					}
					
					/**
					 * SFY results.
					 */
					{
						POIUtil.writeToCell(21 + j, 4 + 5 * i, sectionForces[1],
								workSheet);
						performAxialForceComparisonNonMod(21 + j, i,
								elementCount, sectionForces[1]);
					}
					
//					/**
//					 * AxialForce results.
//					 */
					{
						POIUtil.writeToCell(35 + j, 4 + 5 * i, sectionForces[0],
								workSheet);
						performAxialForceComparisonNonMod(35 + j, i,
								elementCount, sectionForces[0]);
					}
					
					/**
					 * Deflection results.
					 */
					{
						POIUtil.writeToCell(49 + j, 4 + 5 * i, transDisplacements[1]*1000,
								workSheet);
						performAxialForceComparisonNonMod_SensitiveVals(49 + j, i,
								elementCount, transDisplacements[1]*1000);
						
						POIUtil.writeToCell(63 + j, 4 + 5 * i, transDisplacements[0]*1000,
								workSheet);
						performAxialForceComparisonNonMod_SensitiveVals(63 + j, i,
								elementCount, transDisplacements[0]*1000);
						
						
						/**
						 * Tried to write local Deflection.
						 */
						
						POIUtil.writeToCell(77 + j, 4 + 5 * i, transDisplacements[3]*1000,workSheet);
						POIUtil.writeToCell(147 + j, 4 + 5 * i, transDisplacements[4]*1000,workSheet);
						
						// Global Z
						POIUtil.writeToCell(91 + j, 4 + 5 * i, transDisplacements[2]*1000,
								workSheet);
						performAxialForceComparisonNonMod_SensitiveVals(91 + j, i,
								elementCount, transDisplacements[2]*1000);
					}
					/**
					 * BMY results. 
					 */
					{
						POIUtil.writeToCell(105 + j, 4 + 5 * i, sectionForces[4],
								workSheet);
						performAxialForceComparisonNonMod(105 + j, i,
								elementCount, sectionForces[4]);
					}
					/**
					 * SFZ results. 
					 */
					{
						POIUtil.writeToCell(119 + j, 4 + 5 * i, sectionForces[2],
								workSheet);
						performAxialForceComparisonNonMod(119 + j, i,
								elementCount, sectionForces[2]);
					}
					/**
					 * Torsion results. 
					 */
					{
						POIUtil.writeToCell(133 + j, 4 + 5 * i, sectionForces[3],
								workSheet);
						performAxialForceComparisonNonMod(133 + j, i,
								elementCount, sectionForces[3]);
					}
				}
				i++;
			}

			/*
			 * } else { System.out.println("BEAM TYPE TESTING .."); }
			 */
		}
//		performWriteOperationToFile();
	}

	private void performAxialForceComparisonNonMod_SensitiveVals(int rowInd, int i,
			int elementCount, double axialForce) {
		double forceValFromStaad = POIUtil.getDoubleAtCell(rowInd, 3 + 5 * i,
				workSheet);
		double forceDifference = forceValFromStaad - axialForce;
		POIUtil.writeToCell(rowInd, 5 + 5 * i, forceDifference, workSheet);
		POIUtil.refreshCellColor(rowInd, 5 + 5 * i, workSheet);

		// later: see how to highlight single cell if it has high difference.
		// POIUtil.formatThisCell(rowInd+forceIndex,6+4*i,workSheet,workBook);
		/*if(forceValFromStaad==0)	// To avoid Infinity
			forceValFromStaad=0.00001;*/
		double percentDifference = (forceDifference/forceValFromStaad)*100;
		double percentDiffMod = MathHelper.getMod(percentDifference);
		if ( percentDiffMod > PRECISION_HIGH && MathHelper.getMod(forceDifference)>PRECISION_SENSITIVE) {
			POIUtil.changeCellColorAccordingToLevel_SENSITIVE(rowInd, 5 + 5 * i,
					workBook, workSheet);
			if(testWorkBook!=null) {
			POIUtil.increaseAxialForceFailNumber_SENSITIVE(this.problemName,
					testWorkSheet, 2 + elementCount);
			}
		}
	}

	private void performAxialForceComparisonNonMod(int rowInd, int i,
			int elementCount, double axialForce) {
		double forceValFromStaad = POIUtil.getDoubleAtCell(rowInd, 3 + 5 * i,
				workSheet);
		double forceDifference = forceValFromStaad - axialForce;
		POIUtil.writeToCell(rowInd, 5 + 5 * i, forceDifference, workSheet);
		POIUtil.refreshCellColor(rowInd, 5 + 5 * i, workSheet);

		// later: see how to highlight single cell if it has high difference.
		// POIUtil.formatThisCell(rowInd+forceIndex,6+4*i,workSheet,workBook);
		if (MathHelper.getMod(forceDifference) > PRECISION) {
			POIUtil.changeCellColorAccordingToLevel(rowInd, 5 + 5 * i,
					workBook, workSheet, forceDifference);
			if (testWorkBook != null) {
				POIUtil.increaseAxialForceFailNumber(this.problemName,
						testWorkSheet, 2 + elementCount - 1, forceDifference);
			}
		}
	}

	private void checkMemberAnswers(Structure structure) {/*
		Set<FiniteElement> finiteElements = structure.getFiniteElements();
		int elementCount = 0;
		for (FiniteElement finiteElement : finiteElements) {
			elementCount++;
			int elementNumber = finiteElement.getElementNumber();
			workSheet = workBook.getSheet("FE " + elementNumber);

			String elementType = POIUtil.getStringAtCell(0, 0, workSheet);
			if (elementType.equalsIgnoreCase("TRUSS")) {
				int i = 0;
				for (Integer loadCaseNum : loadCaseNums) {
					double axialForce = OpenPSA
							.getAxialForceForElement(structure, loadCaseNum,
									elementNumber);
					POIUtil.writeToCell(2, 4 + 5 * i, axialForce, workSheet);
					performAxialForceComparison(2, i, elementCount, axialForce);
					i++;
				}

			} else {
				System.out.println("BEAM TYPE TESTING ..");
			}
		}
	*/}
	
	private void performAxialForceComparison(int rowInd, int i,
			int elementCount, double axialForce) {
		double forceValFromStaad = POIUtil.getDoubleAtCell(rowInd, 3 + 5 * i,
				workSheet);
		double forceDifference = MathHelper.getMod(forceValFromStaad) - MathHelper.getMod(axialForce);
		POIUtil.writeToCell(rowInd, 5 + 5 * i, forceDifference, workSheet);
		POIUtil.refreshCellColor(rowInd, 5 + 5 * i, workSheet);

		// later: see how to highlight single cell if it has high difference.
		// POIUtil.formatThisCell(rowInd+forceIndex,6+4*i,workSheet,workBook);
		if (MathHelper.getMod(forceDifference) > PRECISION) {
			POIUtil.changeCellColorAccordingToLevel(rowInd, 5 + 5 * i,
					workBook, workSheet, forceDifference);
			if(testWorkBook!=null) {
			POIUtil.increaseAxialForceFailNumber(this.problemName,
					testWorkSheet, 2 + elementCount - 1, forceDifference);
			}
		}
	}

	private void checkSupportReactions(Structure structure) {
		workSheet = workBook.getSheet("Reactions");

		int numOfLoadCases = POIUtil.getIntAtCell(0, 1, workSheet);
		int numOfSupports = POIUtil.getIntAtCell(1, 1, workSheet);

		List<ReactionTestEntity> supportNumList = getSupportsList(numOfSupports);
		loadCaseNums = getLoadCaseNums(numOfLoadCases);

		// POIUtil.checkStringInColumn(3, "mY", workSheet);

		for (ReactionTestEntity reactionTestEntity : supportNumList) {
			checkSingleSupportReaction(reactionTestEntity, loadCaseNums,
					structure);
		}
	}

	private void checkSingleSupportReaction(
			ReactionTestEntity reactionTestEntity, List<Integer> loadCaseNums,
			Structure structure) {
		int nodeNum = reactionTestEntity.nodeNum;
		int rowInd = reactionTestEntity.rowIndex;
		// int colInd = reactionTestEntity.colIndex;

		int i = 0;
		for (Integer loadCaseNum : loadCaseNums) {
			double[] supportReactions = OpenPSA
					.getSupportReactions(structure, loadCaseNum, nodeNum);
			POIUtil.writeToCell(rowInd + 0, 5 + 4 * i, supportReactions[0],
					workSheet);
			POIUtil.writeToCell(rowInd + 1, 5 + 4 * i, supportReactions[1],
					workSheet);
			POIUtil.writeToCell(rowInd + 2, 5 + 4 * i, supportReactions[2],
					workSheet);

			POIUtil.writeToCell(rowInd + 3, 5 + 4 * i, supportReactions[3],
					workSheet);
			POIUtil.writeToCell(rowInd + 4, 5 + 4 * i, supportReactions[4],
					workSheet);
			POIUtil.writeToCell(rowInd + 5, 5 + 4 * i, supportReactions[5],
					workSheet);

			// Now perform subtraction. And compare.
			performReactionComparison(rowInd, i, supportReactions, 0);
			performReactionComparison(rowInd, i, supportReactions, 1);
			performReactionComparison(rowInd, i, supportReactions, 2);

			performReactionComparison(rowInd, i, supportReactions, 3);
			performReactionComparison(rowInd, i, supportReactions, 4);
			performReactionComparison(rowInd, i, supportReactions, 5);

			i++;
		}
	}

	private void performReactionComparisonDisp(int rowInd, int i,
			double checkValueRef, int j) {
		double forceValFromStaad = POIUtil.getDoubleAtCell(rowInd + j,
				4 + 4 * i, workSheet);
		double forceDifference = forceValFromStaad
				- checkValueRef;
		// Bring back color to normal, if previously was changed.
		POIUtil.refreshCellColor(rowInd + j, 6 + 4 * i, workSheet);
		POIUtil.writeToCell(rowInd + j, 6 + 4 * i, forceDifference,
				workSheet);

		// later: see how to highlight single cell if it has high difference.
		// POIUtil.formatThisCell(rowInd+forceIndex,6+4*i,workSheet,workBook);
		double forceDiffPercent = (forceDifference/forceValFromStaad)*100;
		double forceDifPercentMod = MathHelper.getMod(forceDiffPercent);
		if ( forceDifPercentMod> PRECISION_HIGH && MathHelper.getMod(forceDifference)>PRECISION_SENSITIVE) {
			POIUtil.changeCellColorAccordingToLevel_SENSITIVE(rowInd + j,
					6 + 4 * i, workBook, workSheet);
			if (testWorkBook != null) {
				POIUtil.increaseReactionFailNumberAccordingToLevel_SENSITIVE(0,
						problemName, testWorkBook);
			}
		}
	}

	private void performReactionComparison(int rowInd, int i,
			double[] supportReactions, int forceIndex) {
		double forceValFromStaad = POIUtil.getDoubleAtCell(rowInd + forceIndex,
				4 + 4 * i, workSheet);
		double forceDifference = forceValFromStaad
				- supportReactions[forceIndex];
		// Bring back color to normal, if previously was changed.
		POIUtil.refreshCellColor(rowInd + forceIndex, 6 + 4 * i, workSheet);
		POIUtil.writeToCell(rowInd + forceIndex, 6 + 4 * i, forceDifference,
				workSheet);

		// later: see how to highlight single cell if it has high difference.
		// POIUtil.formatThisCell(rowInd+forceIndex,6+4*i,workSheet,workBook);
		boolean NAN_Flag = false; 
		if(Double.isNaN(forceDifference))
			NAN_Flag = true;
		if (MathHelper.getMod(forceDifference) > PRECISION) {
			POIUtil.changeCellColorAccordingToLevel(rowInd + forceIndex,
					6 + 4 * i, workBook, workSheet, forceDifference);
			if (testWorkBook != null) {
				POIUtil.increaseReactionFailNumberAccordingToLevel(0,
						problemName, testWorkBook, forceDifference);
			}
		}
		if(NAN_Flag) {
			POIUtil.changeCellColorAccordingToLevel(rowInd + forceIndex,
					6 + 4 * i, workBook, workSheet, 1000d);
			if (testWorkBook != null) {
				POIUtil.increaseReactionFailNumberAccordingToLevel(0,
						problemName, testWorkBook, 1000d);
			}
		}
	}

	private List<ReactionTestEntity> getSupportsList(int numOfSupports) {
		List<ReactionTestEntity> supportNumList = new ArrayList<ReactionTestEntity>();
		for (int i = 0; i < numOfSupports; i++) {
			int nodeSupportNum = POIUtil.getIntAtCell(3 + 7 * i, 2, workSheet);

			ReactionTestEntity reactionTestEntity = new ReactionTestEntity(
					nodeSupportNum, 3 + 7 * i, 2);
			supportNumList.add(reactionTestEntity);
		}
		return supportNumList;
	}

	private List<Integer> getLoadCaseNums(int numOfLoadCases) {

		List<Integer> loadCaseNums = new ArrayList<Integer>();

		int rowInd = 3;

		for (int i = 0; i < numOfLoadCases; i++) {
			String lcString = POIUtil.getStringAtCell(rowInd - 1, 4 + 4 * i,
					workSheet);
			String substring = lcString.substring(4, lcString.length());
			// System.out.println(substring);
			int lcNum = Integer.parseInt(substring);
			loadCaseNums.add(lcNum);
		}
		return loadCaseNums;
	}

	private void performWriteOperationToFile() {
		try {
			// Write the workbook in file system
			FileOutputStream out = new FileOutputStream(new File(
					problemExcelFilePath));
			workBook.write(out);
			out.close();

			if (testWorkBook != null) {
				FileOutputStream out2 = new FileOutputStream(new File(
						testReporterExcelFilePath));
				testWorkBook.write(out2);
				out2.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private class ReactionTestEntity {
		int nodeNum;
		int rowIndex;
		int colIndex;

		public ReactionTestEntity(int nodeNum, int rowIndex, int colIndex) {
			this.nodeNum = nodeNum;
			this.rowIndex = rowIndex;
			this.colIndex = colIndex;
		}
	}
}
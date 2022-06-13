package com.ppsa.test.excel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ppsa.exception.PPSAException;
import com.ppsa.exception.PPSAIOException;
import com.ppsa.math.MathHelper;
import com.ppsa.util.OpenPSA;
import com.ppsa.util.POIUtil;
import com.psa.entity.FiniteElement;
import com.psa.entity.LoadCase;
import com.psa.entity.impl.Node;
import com.psa.entity.impl.OneDimFiniteElement;
import com.psa.entity.impl.PSASpecificCommands;
import com.psa.entity.impl.Structure;
import com.psa.exception.PSAStructureInstantiationException;
import com.psa.io.EnvFileIOUtil;

public class ExcelOutputExecute {
	private static final double PRECISION = 0.001;

	private static final double PRECISION_HIGH = 5.0; // Percentage

	private static final double PRECISION_SENSITIVE = 0.1;

	private String problemName;
	private String problemExcelFilePath;
	//private String testReporterExcelFilePath;

	private XSSFWorkbook workBook;
	private XSSFSheet workSheet;

	//private XSSFWorkbook testWorkBook;
	//private XSSFSheet testWorkSheet;

	private List<Integer> loadCaseNums;

	public void writeResults(String probName, Path problemPath, PSASpecificCommands psaSpecificCommands) throws 
			PPSAIOException, PPSAException {
		this.problemName = probName;
		String environmentPath = EnvFileIOUtil.getEnvironmentPath();

		problemExcelFilePath = problemPath.getParent().toString()+"\\"
				+ probName + ".xlsx";
		Path excelFilePath = Paths.get(problemExcelFilePath);
		File excelFile = excelFilePath.toFile();
		try {
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		prepareWorkBookObject(excelFile);

		/*
		 * Structure structure = PPSADataCopyUtil
		 * .getPostProcessedStructureFromFile(probName);
		 */

		//Path stdFilePathToReadFromEnv = EnvFileIOUtil.getSTDFilePathToReadFromEnv(probName);
		Structure structure = OpenPSA
				.postProcessStructure(problemPath);

		performWrite(structure);
		
	}

	private void prepareWorkBookObject(File excelFile) throws PPSAIOException
			{
		try {
			workBook = new XSSFWorkbook();
			//FileOutputStream fileOutputStream = new FileOutputStream(excelFile);
			//workBook.createSheet("Reactions");
			//workBook.write(fileOutputStream);
			//fileOutputStream.close();
			
		} catch (Exception e) {
			throw new PPSAIOException(e.getMessage());
		}
	}

	private void performWrite(Structure structure) throws PPSAException {

		// Initialize value of testReport for reaction fail to '0'.
		/*POIUtil.refreshTestReactionEntry(0, this.problemName, testWorkSheet,
				structure);*/
		writeSupportReactions(structure);
		//checkMemberAnswers(structure);
		writeMemberAnswersCompleteSectionForces(structure);
		checkNodeDisplacements(structure);
		
		performWriteOperationToFile();

	}
	
	private void checkNodeDisplacements(Structure structure) throws PPSAException {
		workSheet = workBook.createSheet("N_Disp");

		//int numOfLoadCases = POIUtil.getIntAtCell(0, 1, workSheet);
		Set<Node> nodes = structure.getNodes();

		//List<ReactionTestEntity> supportNumList = getSupportsList(numOfNodes);
		//loadCaseNums = getLoadCaseNums(numOfLoadCases);

		// POIUtil.checkStringInColumn(3, "mY", workSheet);

		int nodeCount=0;
		for (Node node : nodes) {
			checkSingleNodeDisplacement(node,
					structure,nodeCount);
			nodeCount++;
		}
	}

	private void checkSingleNodeDisplacement(
			Node node,
			Structure structure, int nodeCount) throws PPSAException {
		/*int nodeNum = dispTestEntity.nodeNum;*/
		writeBasicData_NodeDisp(node,nodeCount);
		POIUtil.writeToCell(3 , 2, "Node",
				workSheet);
		
		int rowInd = 4+7*nodeCount;
		// int colInd = reactionTestEntity.colIndex;
		Set<Integer> loadCaseNumbers = structure.getLoadCases().keySet();
		int i = 0;
		for (Integer loadCaseNum : loadCaseNumbers) {
			POIUtil.writeToCell(3 , 4 + i, "L.C "+loadCaseNum,
					workSheet);
			
			double[] nodeDisplacements = null;
			try {
				nodeDisplacements = OpenPSA
						.getNodeDisplacements (structure, loadCaseNum, node.getNodeNumber());
			} catch (PSAStructureInstantiationException e) {
				throw new PPSAException("Cant get displacement ; node : " + node.getNodeNumber());
			}
			POIUtil.writeToCell(rowInd + 0, 4 + i, nodeDisplacements[0]*1000,
					workSheet);
			POIUtil.writeToCell(rowInd + 1, 4 + i, nodeDisplacements[1]*1000,
					workSheet);
			POIUtil.writeToCell(rowInd + 2, 4 + i, nodeDisplacements[2]*1000,
					workSheet);

			POIUtil.writeToCell(rowInd + 3, 4 + i, nodeDisplacements[3],
					workSheet);
			POIUtil.writeToCell(rowInd + 4, 4 + i, nodeDisplacements[4],
					workSheet);
			POIUtil.writeToCell(rowInd + 5, 4 + i, nodeDisplacements[5],
					workSheet);

			// Now perform subtraction. And compare.
			/*performReactionComparisonDisp(rowInd, i, nodeDisplacements[0]*1000, 0);
			performReactionComparisonDisp(rowInd, i, nodeDisplacements[1]*1000, 1);
			performReactionComparisonDisp(rowInd, i, nodeDisplacements[2]*1000, 2);

			performReactionComparisonDisp(rowInd, i, nodeDisplacements[3], 3);
			performReactionComparisonDisp(rowInd, i, nodeDisplacements[4], 4);
			performReactionComparisonDisp(rowInd, i, nodeDisplacements[5], 5);*/
			i++;
		}
	}

	private void writeBasicData_NodeDisp(Node node, int nodeCount) {
		int rowInd=4+7*nodeCount;
		
		
		POIUtil.writeToCell(rowInd , 2, node.getNodeNumber(),
				workSheet);
		POIUtil.writeToCell(rowInd + 0, 3, "dX",
				workSheet);
		POIUtil.writeToCell(rowInd + 1, 3, "dY",
				workSheet);
		POIUtil.writeToCell(rowInd + 2, 3, "dZ",
				workSheet);

		POIUtil.writeToCell(rowInd + 3, 3, "rX",
				workSheet);
		POIUtil.writeToCell(rowInd + 4, 3, "rY",
				workSheet);
		POIUtil.writeToCell(rowInd + 5, 3, "rZ",
				workSheet);
		
	}

	private void writeMemberAnswersCompleteSectionForces(Structure structure) throws PPSAException {

		
		Set<FiniteElement> finiteElements = structure.getFiniteElements();
		//int elementCount = 0;
		for (FiniteElement finiteElement : finiteElements) {
			//elementCount++;
			int elementNumber = finiteElement.getElementNumber();
			workSheet = workBook.createSheet("FE " + elementNumber);
			OneDimFiniteElement oneDimFiniteElement = (OneDimFiniteElement)finiteElement;
			writeDescriptionData_MemberSheet(oneDimFiniteElement.getLengthOfElement());

			/*
			 * String elementType = POIUtil.getStringAtCell(0, 0, workSheet); if
			 * (elementType.equalsIgnoreCase("TRUSS")) {
			 */
			Set<Integer> loadCaseIds = structure.getLoadCases().keySet();
			int i = 0;
			for (Integer loadCaseNum : loadCaseIds) {
				POIUtil.writeToCell(4 , 4 + i, "L.C "+loadCaseNum,
						workSheet);
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
						POIUtil.writeToCell(5 + j, 4 + i, sectionForces[5],
								workSheet);
					}
					
					/**
					 * SFY results.
					 */
					{
						POIUtil.writeToCell(19 + j, 4 + i, sectionForces[1],
								workSheet);
					}
					
					/**
					 * AxialForce results.
					 */
					{
						POIUtil.writeToCell(33 + j, 4 + i, sectionForces[0],
								workSheet);
					}
					
					/**
					 * Deflection results.
					 */
					{
						POIUtil.writeToCell(47 + j, 4 + i, transDisplacements[1]*1000,
								workSheet);
						
						POIUtil.writeToCell(61 + j, 4 + i, transDisplacements[0]*1000,
								workSheet);
						
						
						/**
						 * Tried to write local Deflection.
						 */
						
						POIUtil.writeToCell(75 + j, 4 + i, transDisplacements[3]*1000,workSheet);
						POIUtil.writeToCell(145 + j, 4 + i, transDisplacements[4]*1000,workSheet);
						
						// Global Z
						POIUtil.writeToCell(89 + j, 4 + i, transDisplacements[2]*1000,
								workSheet);
						/*performAxialForceComparisonNonMod_SensitiveVals(91 + j, i,
								elementCount, transDisplacements[2]*1000);*/
					}
					/**
					 * BMY results. 
					 */
					{
						POIUtil.writeToCell(103 + j, 4 + i, sectionForces[4],
								workSheet);
						/*performAxialForceComparisonNonMod(105 + j, i,
								elementCount, sectionForces[4]);*/
					}
					/**
					 * SFZ results. 
					 */
					{
						POIUtil.writeToCell(117 + j, 4 + i, sectionForces[2],
								workSheet);
						/*performAxialForceComparisonNonMod(119 + j, i,
								elementCount, sectionForces[2]);*/
					}
					/**
					 * Torsion results. 
					 */
					{
						POIUtil.writeToCell(131 + j, 4 + i, sectionForces[3],
								workSheet);
						/*performAxialForceComparisonNonMod(133 + j, i,
								elementCount, sectionForces[3]);*/
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

	private void writeDescriptionData_MemberSheet(double elementLength) {
		POIUtil.writeToCell(4, 3, "Location",
				workSheet);
		
		POIUtil.writeToCell(5 , 2, "BM Z",
				workSheet);
			POIUtil.writeToCell(19 , 2, "SF Y",
					workSheet);
			POIUtil.writeToCell(33 , 2, "Axial",
					workSheet);
			POIUtil.writeToCell(47,2, "Def..GY",
					workSheet);
			
			POIUtil.writeToCell(61 , 2, "Def..GX",
					workSheet);
			POIUtil.writeToCell(75 , 2, "Def..LY",workSheet);
			POIUtil.writeToCell(145 , 2, "Def..LZ",workSheet);
			// Global Z
			POIUtil.writeToCell(89 , 2, "Def..GZ",
					workSheet);
			POIUtil.writeToCell(103 , 2, "BM Y",
					workSheet);
			POIUtil.writeToCell(117 , 2, "SF Z",
					workSheet);
			POIUtil.writeToCell(131 , 2, "Torsion",
					workSheet);
		
		
		
		//--------------------------------------------
		
		for (int j = 0; j <= 12; j++) {
			double ratio = (j / 12d)*elementLength;

			/*double[] sectionForces = OpenPSA
					.getIntermediateMemberForcesAtDistance(structure,
							elementNumber, ratio, loadCaseNum);
			double[] transDisplacements = OpenPSA.getIntermediateMemberTransDisplacements(structure,
							elementNumber, ratio, loadCaseNum);*/
			/**
			 * BMZ results. 
			 */
			{
				POIUtil.writeToCell(5 + j, 3, ratio,
						workSheet);
			}
			
			/**
			 * SFY results.
			 */
			{
				POIUtil.writeToCell(19 + j, 3, ratio,
						workSheet);
			}
			
			/**
			 * AxialForce results.
			 */
			{
				POIUtil.writeToCell(33 + j, 3, ratio,
						workSheet);
			}
			
			/**
			 * Deflection results.
			 */
			{
				POIUtil.writeToCell(47 + j, 3, ratio,
						workSheet);
				
				POIUtil.writeToCell(61 + j, 3, ratio,
						workSheet);
				
				
				/**
				 * Tried to write local Deflection.
				 */
				
				POIUtil.writeToCell(75 + j, 3, ratio,workSheet);
				POIUtil.writeToCell(145 + j, 3, ratio,workSheet);
				// Global Z
				POIUtil.writeToCell(89 + j, 3, ratio,
						workSheet);
				/*performAxialForceComparisonNonMod_SensitiveVals(91 + j, i,
						elementCount, transDisplacements[2]*1000);*/
			}
			/**
			 * BMY results. 
			 */
			{
				POIUtil.writeToCell(103 + j, 3, ratio,
						workSheet);
				/*performAxialForceComparisonNonMod(105 + j, i,
						elementCount, sectionForces[4]);*/
			}
			/**
			 * SFZ results. 
			 */
			{
				POIUtil.writeToCell(117 + j, 3, ratio,
						workSheet);
				/*performAxialForceComparisonNonMod(119 + j, i,
						elementCount, sectionForces[2]);*/
			}
			/**
			 * Torsion results. 
			 */
			{
				POIUtil.writeToCell(131 + j, 3, ratio,
						workSheet);
				/*performAxialForceComparisonNonMod(133 + j, i,
						elementCount, sectionForces[3]);*/
			}
		}
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
			/*POIUtil.increaseAxialForceFailNumber_SENSITIVE(this.problemName,
					testWorkSheet, 2 + elementCount);*/
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
			/*POIUtil.increaseAxialForceFailNumber(this.problemName,
					testWorkSheet, 2 + elementCount - 1, forceDifference);*/
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
			/*POIUtil.increaseAxialForceFailNumber(this.problemName,
					testWorkSheet, 2 + elementCount - 1, forceDifference);*/
		}
	}

	private void writeSupportReactions(Structure structure) {
		workSheet = workBook.createSheet("Reactions");

		//POIUtil.writeToCell(0, 1,structure.getLoadCases().size() ,workSheet);
		//int numOfSupports = POIUtil.getIntAtCell(1, 1, workSheet);

		/*List<ReactionTestEntity> supportNumList = getSupportsList(numOfSupports);
		loadCaseNums = getLoadCaseNums(structure);*/

		// POIUtil.checkStringInColumn(3, "mY", workSheet);
		
			writeSingleSupportReaction(structure);
	}

	private void writeSingleSupportReaction(Structure structure) {
		/*int nodeNum = reactionTestEntity.nodeNum;*/
		// int colInd = reactionTestEntity.colIndex;
		
		writeDataDescriptions(structure);
		
		Set<Node> nodes = structure.getNodes();
		Map<Integer, LoadCase> loadCases = structure.getLoadCases();
		Set<Integer> loadCaseIds = loadCases.keySet();
		int i = 0;
		for (Integer loadCaseId : loadCaseIds) {
		int rowInd = 4;
		for (Node node : nodes) {
			if(node.isSupport()) {
			double[] supportReactions = OpenPSA
					.getSupportReactions(structure, loadCaseId, node.getNodeNumber());
			
			POIUtil.writeToCell(rowInd + 0, 3 + i, supportReactions[0],
					workSheet);
			POIUtil.writeToCell(rowInd + 1, 3 + i, supportReactions[1],
					workSheet);
			POIUtil.writeToCell(rowInd + 2, 3 + i, supportReactions[2],
					workSheet);

			POIUtil.writeToCell(rowInd + 3, 3 + i, supportReactions[3],
					workSheet);
			POIUtil.writeToCell(rowInd + 4, 3 + i, supportReactions[4],
					workSheet);
			POIUtil.writeToCell(rowInd + 5, 3 + i, supportReactions[5],
					workSheet);
			
			rowInd=rowInd+7;
			}
		  }
		i++;
		}
	}

	private void writeDataDescriptions(Structure structure) {
		Set<Node> nodes = structure.getNodes();
		Map<Integer, LoadCase> loadCases = structure.getLoadCases();
		Set<Integer> loadCaseIds = loadCases.keySet();
		POIUtil.writeToCell(3, 1 , "Node",
				workSheet);
		int i = 0;
		for (Integer loadCaseId : loadCaseIds) {
			POIUtil.writeToCell(3, 3+i , "L.C "+loadCaseId,
					workSheet);
			i++;
		}
			int rowInd = 4;
			for (Node node : nodes) {
				if (node.isSupport()) {

					// Write Node Num
					POIUtil.writeToCell(rowInd + 0, 1, node.getNodeNumber(),
							workSheet);

					POIUtil.writeToCell(rowInd + 0, 2 , "Fx",
							workSheet);
					POIUtil.writeToCell(rowInd + 1, 2, "Fy",
							workSheet);
					POIUtil.writeToCell(rowInd + 2, 2, "Fz",
							workSheet);

					POIUtil.writeToCell(rowInd + 3, 2, "Mx",
							workSheet);
					POIUtil.writeToCell(rowInd + 4, 2, "My",
							workSheet);
					POIUtil.writeToCell(rowInd + 5, 2, "Mz",
							workSheet);

					rowInd = rowInd + 7;
				}
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
		if (MathHelper.getMod(forceDifference) > PRECISION) {
			POIUtil.changeCellColorAccordingToLevel(rowInd + forceIndex,
					6 + 4 * i, workBook, workSheet, forceDifference);
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

	private List<Integer> getLoadCaseNums(Structure structure) {/*

		List<Integer> loadCaseNums = new ArrayList<Integer>();
		structure.getLoadCases()
		int rowInd = 3;

		for (int i = 0; i < structure; i++) {
			String lcString = POIUtil.getStringAtCell(rowInd - 1, 4 + 4 * i,
					workSheet);
			String substring = lcString.substring(4, lcString.length());
			// System.out.println(substring);
			int lcNum = Integer.parseInt(substring);
			loadCaseNums.add(lcNum);
		}*/
		return null;//loadCaseNums;
	}

	private void performWriteOperationToFile() {
		try {
			// Write the workbook in file system
			FileOutputStream out = new FileOutputStream(new File(
					problemExcelFilePath));
			workBook.write(out);
			out.close();

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
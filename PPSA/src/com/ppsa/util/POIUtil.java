package com.ppsa.util;

import java.util.Iterator;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ppsa.math.MathHelper;
import com.ppsa.setting.PrecisionConstants;
import com.psa.entity.impl.Structure;

public class POIUtil implements PrecisionConstants {

	public static String getStringAtCell(int rowIndex, int colIndex,
			XSSFSheet workSheet) {
		Cell cell = getCellByIndexes(rowIndex, colIndex, workSheet);

		String stringCellValue = cell.getStringCellValue();
		return stringCellValue;
	}

	public static double getDoubleAtCell(int rowIndex, int colIndex,
			XSSFSheet workSheet) {
		Cell cell = getCellByIndexes(rowIndex, colIndex, workSheet);
		double doubleCellValue = cell.getNumericCellValue();
		return doubleCellValue;
	}

	public static int getIntAtCell(int rowIndex, int colIndex,
			XSSFSheet workSheet) {

		Cell cell = getCellByIndexes(rowIndex, colIndex, workSheet);

		double doubleCellValue = cell.getNumericCellValue();
		String doubleString = String.valueOf(doubleCellValue);
		String intString = doubleString.substring(0, doubleString.length() - 2);
		int intVal = Integer.parseInt(intString);
		return intVal;
	}

	public static void increaseReactionFailNumberAccordingToLevel(int colIndex, String keyWord,
			XSSFWorkbook testWorkBook, double forceDifference) {
		XSSFSheet testWorkSheet = testWorkBook.getSheet("Reactions");

		Iterator<Row> iterator = testWorkSheet.iterator();
		boolean stringNotFoundFlag = true;
		int numOfRows = 0;
		double numOfFailedReactions = 0;
		Cell reactionFailCell = null;
		while (iterator.hasNext()) {
			Row row = iterator.next();

			Cell cell = CellUtil.getCell(row, colIndex);

			String stringCellValue = cell.getStringCellValue();

			if (stringCellValue.equalsIgnoreCase(keyWord)) {
				// System.out.println("Cell Contains KEWORD : " + keyWord
				// +" at - "+cell.getRowIndex() +" , "+cell.getColumnIndex());
				int rowNum = row.getRowNum();
				reactionFailCell = CellUtil.getCell(row, 1);
				numOfFailedReactions = reactionFailCell.getNumericCellValue();
				numOfFailedReactions++;
				writeToCell(rowNum, 1, numOfFailedReactions, testWorkSheet);
				stringNotFoundFlag = false;
				break;
			}
			numOfRows++;
		}
		if (stringNotFoundFlag) {
			writeToCell(numOfRows, 0, keyWord, testWorkSheet);
			writeToCell(numOfRows, 1, 1, testWorkSheet);
		}
		if(reactionFailCell==null)
			System.out.println("nulllll...");
		if(MathHelper.getMod(forceDifference)>PRECISION_LEVEL_3)
			changeCellColorIfPrecisionLevel3Crossed(reactionFailCell,testWorkSheet);
	}

	private static void changeCellColorIfPrecisionLevel3Crossed(Cell cell,
			XSSFSheet testWorkSheet) {
		XSSFWorkbook testWorkbook = testWorkSheet.getWorkbook();
		XSSFCellStyle cellStyle = testWorkbook.createCellStyle();
		XSSFFont font = testWorkbook.createFont();
		font.setColor(HSSFColor.RED.index);
		cellStyle.setFont(font);
		cell.setCellStyle(cellStyle);
		
	}

	public static boolean refreshTestReactionEntry(int colIndex,
			String keyWord, XSSFSheet testWorkSheet, Structure structure) {

		Iterator<Row> iterator = testWorkSheet.iterator();
		boolean stringNotFoundFlag = true;
		int numOfRows = 0;
		while (iterator.hasNext()) {
			Row row = iterator.next();

			Cell cell = CellUtil.getCell(row, colIndex);

			String stringCellValue = cell.getStringCellValue();

			if (stringCellValue.equalsIgnoreCase(keyWord)) {
				// System.out.println("Cell Contains KEWORD : " + keyWord
				// +" at - "+cell.getRowIndex() +" , "+cell.getColumnIndex());
				// writeToCell(numOfRows, 0, keyWord, testWorkSheet);
				initializeData(testWorkSheet, numOfRows, structure);
				stringNotFoundFlag = false;
				break;
			}
			numOfRows++;
		}
		if (stringNotFoundFlag) {
			writeToCell(numOfRows + 1, 0, keyWord, testWorkSheet);
			initializeData(testWorkSheet, numOfRows, structure);
		}
		return stringNotFoundFlag;
	}

	private static void initializeData(XSSFSheet testWorkSheet, int numOfRows,
			Structure structure) {
		writeToCell(numOfRows + 1, 1, 0, testWorkSheet);
		int finiteElementsNum = structure.getFiniteElements().size();

		for (int i = 0; i < finiteElementsNum; i++) {
			writeToCell(numOfRows + 1, 2 + i, 0, testWorkSheet);
		}

	}

	public static void writeToCell(int rowIndex, int colIndex, Object object,
			XSSFSheet workSheet) {
		Cell cell = getCellByIndexes(rowIndex, colIndex, workSheet);
		if (object instanceof String)
			cell.setCellValue((String) object);
		else if (object instanceof Double)
			cell.setCellValue((double) object);
		else if (object instanceof Integer)
			cell.setCellValue((int) object);

	}

	private static Cell getCellByIndexes(int rowIndex, int colIndex,
			XSSFSheet workSheet) {
		Row row = CellUtil.getRow(rowIndex, workSheet);
		Cell cell = CellUtil.getCell(row, colIndex);

		return cell;
	}

	public static void increaseAxialForceFailNumber(String keyWord,
			XSSFSheet testWorkSheet, int colIndexToIncrease,
			double forceDifferenceVal) {

		Iterator<Row> iterator = testWorkSheet.iterator();
		//boolean stringNotFoundFlag = true;
		double numOfFailedReactions = 0;
		Cell axialForceFailCell=null;
		while (iterator.hasNext()) {
			Row row = iterator.next();

			Cell cell = CellUtil.getCell(row, 0);

			String stringCellValue = cell.getStringCellValue();

			if (stringCellValue.equalsIgnoreCase(keyWord)) {
				// System.out.println("Cell Contains KEWORD : " + keyWord
				// +" at - "+cell.getRowIndex() +" , "+cell.getColumnIndex());
				int rowNum = row.getRowNum();
				axialForceFailCell = CellUtil.getCell(row,
						colIndexToIncrease);
				numOfFailedReactions = axialForceFailCell.getNumericCellValue();
				if(MathHelper.getMod(forceDifferenceVal)>PRECISION_LEVEL_3)
					numOfFailedReactions++;
				
				writeToCell(rowNum, colIndexToIncrease, numOfFailedReactions,
						testWorkSheet);
				break;
			}
		}
		if(MathHelper.getMod(forceDifferenceVal)>PRECISION_LEVEL_3)
			changeCellColorIfPrecisionLevel3Crossed(axialForceFailCell,testWorkSheet);
	}

	public static void changeCellColorAccordingToLevel(int rowInd, int colInd,
			XSSFWorkbook workBook, XSSFSheet workSheet, double forceDifference) {
		
		forceDifference = MathHelper.getMod(forceDifference);
		CellStyle style = workBook.createCellStyle();
		Font font = workBook.createFont();

		font.setColor(HSSFColor.GREEN.index);

		if (MathHelper.getMod(forceDifference) > PRECISION_LEVEL_2)
			font.setColor(HSSFColor.BLUE.index);
		if (forceDifference > PRECISION_LEVEL_3) {
			font.setColor(HSSFColor.RED.index);
		}

		style.setFont(font);

		Cell cellByIndexes = getCellByIndexes(rowInd, colInd, workSheet);
		cellByIndexes.setCellStyle(style);

		//increaseReactionFailNumber(colIndexInTestWorkbook, problemName, testWorkBook);
		//if(setRedInTestReportFlag)
			
	}

	public static void refreshCellColor(int rowIndex, int colIndex,
			XSSFSheet workSheet) {
		Cell cell = getCellByIndexes(rowIndex, colIndex, workSheet);
		XSSFWorkbook workbook = workSheet.getWorkbook();
		CellStyle style = workbook.createCellStyle();
		Font font = workbook.createFont();
		font.setColor(HSSFColor.AUTOMATIC.index);
		style.setFont(font);
		cell.setCellStyle(style);
	}

	/**
	 * For sensitive data like deflection. 
	 */
	public static void changeCellColorAccordingToLevel_SENSITIVE(int rowInd, int colInd,
			XSSFWorkbook workBook, XSSFSheet workSheet) {
		
		//forceDifference = MathHelper.getMod(forceDifference);
		CellStyle style = workBook.createCellStyle();
		Font font = workBook.createFont();

		//font.setColor(HSSFColor.GREEN.index);

		/*if (MathHelper.getMod(forceDifference) > PRECISION_LEVEL_2)
			font.setColor(HSSFColor.BLUE.index);
		if (forceDifference > PRECISION_LEVEL_3) {*/
			font.setColor(HSSFColor.RED.index);
		//}

		style.setFont(font);

		Cell cellByIndexes = getCellByIndexes(rowInd, colInd, workSheet);
		cellByIndexes.setCellStyle(style);

		//increaseReactionFailNumber(colIndexInTestWorkbook, problemName, testWorkBook);
		//if(setRedInTestReportFlag)
			
	}

	/***
	 * For Sensitive Data like deflection.  
	 */
	public static void increaseReactionFailNumberAccordingToLevel_SENSITIVE(
			int colIndex, String keyWord, XSSFWorkbook testWorkBook) {
		XSSFSheet testWorkSheet = testWorkBook.getSheet("Reactions");

		Iterator<Row> iterator = testWorkSheet.iterator();
		boolean stringNotFoundFlag = true;
		int numOfRows = 0;
		double numOfFailedReactions = 0;
		Cell reactionFailCell = null;
		while (iterator.hasNext()) {
			Row row = iterator.next();

			Cell cell = CellUtil.getCell(row, colIndex);

			String stringCellValue = cell.getStringCellValue();

			if (stringCellValue.equalsIgnoreCase(keyWord)) {
				// System.out.println("Cell Contains KEWORD : " + keyWord
				// +" at - "+cell.getRowIndex() +" , "+cell.getColumnIndex());
				int rowNum = row.getRowNum();
				reactionFailCell = CellUtil.getCell(row, 1);
				numOfFailedReactions = reactionFailCell.getNumericCellValue();
				numOfFailedReactions++;
				writeToCell(rowNum, 1, numOfFailedReactions, testWorkSheet);
				stringNotFoundFlag = false;
				break;
			}
			numOfRows++;
		}
		if (stringNotFoundFlag) {
			writeToCell(numOfRows, 0, keyWord, testWorkSheet);
			writeToCell(numOfRows, 1, 1, testWorkSheet);
		}
		//if(MathHelper.getMod(forceDifference)>PRECISION_LEVEL_3)
			changeCellColorIfPrecisionLevel3Crossed(reactionFailCell,testWorkSheet);
	}

	public static void increaseAxialForceFailNumber_SENSITIVE(
			String keyWord, XSSFSheet testWorkSheet, int colIndexToIncrease) {

		Iterator<Row> iterator = testWorkSheet.iterator();
		//boolean stringNotFoundFlag = true;
		double numOfFailedReactions = 0;
		Cell axialForceFailCell=null;
		while (iterator.hasNext()) {
			Row row = iterator.next();

			Cell cell = CellUtil.getCell(row, 0);

			String stringCellValue = cell.getStringCellValue();

			if (stringCellValue.equalsIgnoreCase(keyWord)) {
				// System.out.println("Cell Contains KEWORD : " + keyWord
				// +" at - "+cell.getRowIndex() +" , "+cell.getColumnIndex());
				int rowNum = row.getRowNum();
				axialForceFailCell = CellUtil.getCell(row,
						colIndexToIncrease);
				numOfFailedReactions = axialForceFailCell.getNumericCellValue();
				//if(MathHelper.getMod(forceDifferenceVal)>PRECISION_LEVEL_3)
				numOfFailedReactions++;
				writeToCell(rowNum, colIndexToIncrease, numOfFailedReactions,
						testWorkSheet);
				break;
			}
		}
		//if(MathHelper.getMod(forceDifferenceVal)>PRECISION_LEVEL_3)
			changeCellColorIfPrecisionLevel3Crossed(axialForceFailCell,testWorkSheet);
	}
}

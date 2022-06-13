package com.sd.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.usermodel.XSSFSheet;

public class POIUtil {

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

	/*private static void initializeData(XSSFSheet testWorkSheet, int numOfRows) {
		writeToCell(numOfRows + 1, 1, 0, testWorkSheet);
		int finiteElementsNum = structure.getFiniteElements().size();

		for (int i = 0; i < finiteElementsNum; i++) {
			writeToCell(numOfRows + 1, 2 + i, 0, testWorkSheet);
		}

	}*/

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
}

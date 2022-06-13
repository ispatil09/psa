package com.sd.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.psa.io.EnvFileIOUtil;
import com.sd.enums.COUNTRY_STEEL_SECTION;

public class ExcelDataReader {
	private static final int SEARCH_ROWS_LIMIT = 230;
	private static String excelFolderPath = null;
	private static XSSFWorkbook workBook = null;
	public static double[] getSectionData(COUNTRY_STEEL_SECTION country, String sectionName) {
		//ResourceBundle resourceBundle = ResourceBundle.getBundle("EXCEL_PATH");
		//excelFolderPath = resourceBundle.getString("EXCEL_FOLDER");
		excelFolderPath=System.getProperties().getProperty("user.dir");
		if(country==COUNTRY_STEEL_SECTION.INDIAN)
			return searchFiles_IND(sectionName);
		return null;
	}

	private static double[] searchFiles_IND(String sectionName) {
		String countryExcelFileName = System.getProperties().getProperty("user.dir")+"\\INDIAN_STEEL.xlsx";
		Path testExcelFilePath = Paths.get(countryExcelFileName);
		File readExcelFile = testExcelFilePath.toFile();
		prepareWorkBookObject(readExcelFile);
		int numberOfSheets = workBook.getNumberOfSheets();
		String stringMatchRef = null;
		for (int i = 0; i < numberOfSheets; i++) {
			XSSFSheet workSheet = workBook.getSheetAt(i);
			
			int rowInd=0;
			do {
				rowInd++;
				stringMatchRef = POIUtil.getStringAtCell(rowInd, 1, workSheet);
				if(stringMatchRef.equalsIgnoreCase(sectionName)) {
					double INERTIA_CONV=10E-9;
					double AREA_CONV=10E-5;

					String sheetName = workSheet.getSheetName();
					double sheetIndex = i;
					double area = POIUtil.getDoubleAtCell(rowInd, 2, workSheet);
					double IX = POIUtil.getDoubleAtCell(rowInd, 3, workSheet);
					double IY = POIUtil.getDoubleAtCell(rowInd, 4, workSheet);
					double IZ = POIUtil.getDoubleAtCell(rowInd, 5, workSheet);
					double[] data = new double[5];
					data[0] = sheetIndex;
					data[1] = area*AREA_CONV;
					data[2] = IX*INERTIA_CONV;
					data[3] = IY*INERTIA_CONV;
					data[4] = IZ*INERTIA_CONV;
					
					//System.out.println("Found section match at : sheet - "+sheetName+" , row - "+rowInd+", area = "+area);
					return data;
				}
			} while(rowInd!=SEARCH_ROWS_LIMIT);
		}
		return null;
	}

	private static void prepareWorkBookObject(File readExcelFile) {
		try {
			workBook = new XSSFWorkbook(new FileInputStream(readExcelFile));
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

}

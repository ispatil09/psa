package com.ppsa.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.ppsa.exception.PPSAException;
import com.ppsa.exception.PPSAIOException;
import com.ppsa.test.excel.ExcelOutputTest;
import com.psa.exception.PSAException;
import com.psa.io.EnvFileIOUtil;

public class TestAllExcelFilesInEnv {

	/**
	 * @param args
	 * @throws InitializationException
	 * @throws PSAException
	 * @throws PPSAIOException
	 * @throws PPSAException
	 */
	public static void main(String[] args) {

		List<String> list = new ArrayList<String>();

		Set<String> problemNames = EnvFileIOUtil
				.getAllProblemNamesInEnvironment();
		//EntityStore.scilab=new Scilab();
		for (String problemName : problemNames) {
			try {
				testThisSingleFile(problemName);
			} catch (Exception e) {
				list.add(problemName);
				e.printStackTrace();
			}
		}
		System.out.println("\n\nSkipped Test cases : " + list.size());
		System.out.println(list);
	}

	private static void testThisSingleFile(String probName)
			throws PPSAIOException, PPSAException {
		System.out.println("Testing Single file : " + probName);
		ExcelOutputTest testExcel = new ExcelOutputTest();
		testExcel.testStructure(probName,null,null);
	}

}

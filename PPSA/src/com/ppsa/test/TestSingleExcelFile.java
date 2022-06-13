package com.ppsa.test;

import com.ppsa.exception.PPSAIOException;
import com.ppsa.test.excel.ExcelOutputTest;
import com.psa.entity.impl.PSASpecificCommands;
import com.psa.exception.PSAException;

public class TestSingleExcelFile {

	/**
	 * @param args
	 * @throws PSAException
	 * @throws PPSAIOException
	 */
	public static void main(String[] args) throws PSAException, PPSAIOException {
		System.out.println(System.getProperties().getProperty("user.dir"));
		String structureName = "";
		if(args.length!=0)
			structureName=args[0];
		else
			structureName = "SupportSettlement 04 STruss2";
			//structureName = "SupportSettlement 02 PFrame";

		PSASpecificCommands psaSpecificCommands = new PSASpecificCommands();
		psaSpecificCommands.setMeshFlag(false);

		ExcelOutputTest testExcel = new ExcelOutputTest();
		testExcel.testStructure(structureName,null, psaSpecificCommands);
		System.out.println("Tested Single file : " + structureName);

	}
}
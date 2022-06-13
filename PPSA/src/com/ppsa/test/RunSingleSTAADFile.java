package com.ppsa.test;

import java.nio.file.Paths;
import java.util.Scanner;

import com.ppsa.exception.PPSAIOException;
import com.ppsa.test.excel.ExcelOutputTest;
import com.psa.entity.impl.PSASpecificCommands;
import com.psa.exception.PSAException;
import com.psa.util.ScannerUtil;

public class RunSingleSTAADFile {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws PSAException, PPSAIOException {
		
		//new ScannerUtil().getString("Name of the STAAD file to analyse : ");
		String workingDirectory = System.getProperties().getProperty("user.dir");
		System.out.println(workingDirectory);
		String structureName = "";
		if(args.length!=0)
			structureName=args[0];
		else
			structureName = "Plane";

		System.out.println("DONE... DEL");
		PSASpecificCommands psaSpecificCommands = new PSASpecificCommands();
		psaSpecificCommands.setMeshFlag(false);

		ExcelOutputTest testExcel = new ExcelOutputTest();
		testExcel.testStructure(structureName, Paths.get(workingDirectory
				+ "\\" + structureName + ".std"), psaSpecificCommands);
		System.out.println("Tested Single file : " + structureName);

	}

}

package com.ppsa.test;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.ppsa.exception.PPSAException;
import com.ppsa.exception.PPSAIOException;
import com.ppsa.test.excel.ExcelOutputExecute;
import com.ppsa.test.excel.ExcelOutputTest;
import com.psa.exception.PSAFileIOException;
import com.psa.exception.PSAUnParsableFileException;
import com.psa.io.DataCopyUtil;
import com.psa.io.EnvFileIOUtil;
import com.psa.util.ScannerUtil;

public class TestConsoleApplication {
	public static void main(String[] args) throws PPSAIOException,
			PPSAException {
		System.out.println("----------------------------------------------------");
		System.out
				.println("Program for Structural Analysis (PSA) v1.0\nProgrammed by Ishwargouda.S.Patil\nEmail:patil.ishwargouda09@gmail.com");
		System.out.println("----------------------------------------------------");
		while (true) {
			String command = ScannerUtil.getCommand();

			try {
				processCommand(command);
			} catch (Exception e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
		}
	}

	private static void processCommand(String command)
			throws PSAFileIOException, PSAUnParsableFileException,
			PPSAIOException, PPSAException {
		String[] split = command.split(" ");
		String firstCommand = split[0];
		switch (firstCommand) {
		case "a":
			analyseCommand(command);
			break;

		case "exit":
			System.exit(0);
			break;

		default:
			System.err.println("Unknown commands entered - ");
			break;
		}
	}

	private static void analyseCommand(String command)
			throws PSAFileIOException, PSAUnParsableFileException,
			PPSAIOException, PPSAException {

		String[] args = command.split(" ");
		Path problemPath = null;
		String probName = null;
		boolean contains = args[1].contains("\\");
		if (!contains) {
			if (!args[1].contains("\""))
				problemPath = getPathOfProblem(args[1]);
			else {
				int firstIndexOf = command.indexOf("\"", 0);
				int lastIndexOf = command.lastIndexOf("\"");
				String substring = command.substring(firstIndexOf + 1,
					lastIndexOf);
				problemPath = getPathOfProblem(substring);
			}
				
			
		} else {
			//System.out.println("Absolute Path Given..\n");

			if (!args[1].contains("\""))
				problemPath = Paths.get(args[1]);
			else {
				int firstIndexOf = command.indexOf("\"", 0);
				int lastIndexOf = command.lastIndexOf("\"");

				String substring = command.substring(firstIndexOf + 1,
						lastIndexOf);
				//System.out.println(substring);
				problemPath = Paths.get(substring);
			}
			if (command.endsWith("--copy")) {
				problemPath = new DataCopyUtil().copyContentTo(
						problemPath.toString(), "std");
				String name = problemPath.toFile().getName();
				System.out
				.println("MESSAGE : Already a structure exists in working directory by given file name\nNew Structure created in Working directory by Name: "
						+ name.substring(0, name.length() - 4));
			}
		}
		String fileName = problemPath.toFile().getName();
		probName=fileName.substring(0, fileName.length()-4);
		if(command.endsWith("--test")) {
			new ExcelOutputTest().testStructure(probName,problemPath, null);
			System.out.println(probName+".std - Tested Successfully!!!");
		}
		else {
				new ExcelOutputExecute().writeResults(probName,problemPath, null);
			System.out.println(probName+".std - Analysed Successfully!!!");
		}
	
	}

	private static Path getPathOfProblem(String problemName) {
		Path problemPath = null;
		String workingDir = EnvFileIOUtil.getEnvironmentPath();
		if (problemName.endsWith(".std"))
			problemName = problemName.substring(0, problemName.length() - 4);
		/*Check if file in outside folder*/
		String fileString = workingDir + "\\" + problemName + ".std";
		problemPath = Paths.get(fileString);
		if (!problemPath.toFile().exists()) {
			problemPath = EnvFileIOUtil
					.getSTDFilePathToReadFromEnv(problemName);
		}
		return problemPath;
	}
}

package com.psa.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.psa.entity.impl.Structure;
import com.psa.exception.PSAFileIOException;

/**
 * Basically this class is for playing with content/data of files
 * 
 * @author SONY
 * 
 */
public class DataCopyUtil {
	/**
	 * U give path of any file with its intended format and it will be copied to
	 * environment directory Note: May be used RARELY
	 */
	public Path copyContentTo(String inputFilePath, String formatOfFile)
			throws PSAFileIOException {
		// String inputFilePath =
		// "C:/Users/SONY/Desktop/xp share/Sample Structure for input/Simple Truss/Simp/SimpleTrussTest.std";
		try {
			String makeDirInEnv = new EnvFileIOUtil().createFileInEnv(
					inputFilePath, formatOfFile);
			// System.out.println("FINALLY CREATED FILE : " + makeDirInEnv);

			Path fileToBeCopied = Paths.get(inputFilePath);
			byte[] fileArrayCopied;
			fileArrayCopied = Files.readAllBytes(fileToBeCopied);

			Path pathOfFileToBeWrittenTo = Paths.get(makeDirInEnv);
			Files.write(pathOfFileToBeWrittenTo, fileArrayCopied);
			return pathOfFileToBeWrittenTo;
		} catch (IOException e) {
			e.printStackTrace();
			throw new PSAFileIOException(e.getMessage());
		}
	}

	public Path copySTDFileToEnvPath(String fileLocation)
			throws PSAFileIOException {
		Path copiedContentTo = copyContentTo(fileLocation, "std");
		return copiedContentTo;
	}

	public void ceateFileAndSaveText(String nameOfProblem, String textToBeSave,Path filePath,String fileFormat)
			throws PSAFileIOException {

		Path stdFilePathToRead = null;
		if(filePath==null) {
			EnvFileIOUtil.getSTDFilePathToReadFromEnv(nameOfProblem);
		} else
			stdFilePathToRead=filePath;
		
		
		
		String pathOfSTDFile = stdFilePathToRead.toString();
		String pathOfHTMLFile = pathOfSTDFile.substring(0,
				pathOfSTDFile.length() - 3)
				+ fileFormat;

		File file = new File(pathOfHTMLFile);

		try {
			FileWriter fileWriter = new FileWriter(file);

			fileWriter.write(textToBeSave);
			fileWriter.close();

		} catch (IOException e) {
			throw new PSAFileIOException(e.getMessage());
		}
	}

	public static void savePreProcessedStructure(Structure structure) throws PSAFileIOException
			 {

		String problemName = structure.getProblemName();
		String environmentPath = EnvFileIOUtil.getEnvironmentPath();
		try {
		FileOutputStream fos = new FileOutputStream(environmentPath + "\\"
				+ problemName + "\\" + problemName + ".psa");
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(structure);
		oos.flush();
		oos.close();
		} catch (IOException e) {
			throw new PSAFileIOException(e.getMessage());
		}
	}
	
	public static Structure getPreProcessedStructure(String problemName)
			 {

		String environmentPath = EnvFileIOUtil.getEnvironmentPath();
		FileInputStream fis;
		ObjectInputStream ois;
		Object object = null;
		Structure structure = null;
		try {
			fis = new FileInputStream(environmentPath + "\\"
					+ problemName + "\\" + problemName + ".psa");
		
		ois = new ObjectInputStream(fis);
		
		object = null;
		
			object = ois.readObject();
			structure = (Structure)object;
			ois.close();
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return structure;
	}
	
}

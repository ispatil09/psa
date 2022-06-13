package com.psa.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import com.psa.exception.PSAFileIOException;
import com.psa.util.ResourceBundleUtil;

/**
 * Class dealing with creation of directories and file usually related to
 * environment path
 * 
 * @author SONY
 * 
 */
public class EnvFileIOUtil {

	/*
	 * public boolean copyInputFileToEnv(String inputFilePath , String format) {
	 * 
	 * }
	 */

	public String createFileInEnv(String inputFilePath, String format)
			throws PSAFileIOException {
		/*
		 * inputFilePath =
		 * "C:/Users/SONY/Desktop/xp share/Sample Structure for input/Simple Truss/Simp/SimpleTrussTest.std"
		 * ; format = "std";
		 */
		Path path = Paths.get(inputFilePath);
		File specifiedFileToBeCopied = getSpecifiedFile(path, format); // Tells
																		// if
																		// input
																		// file
																		// format
																		// wrong

		// get directory name to be appended in env path
		String stringFileName = specifiedFileToBeCopied.getName();
		stringFileName.contains(".");
		int index = stringFileName.indexOf(".");

		String dirName = stringFileName.substring(0, index);

		String envPathString = getEnvironmentPath().toString();
		String probDirPathString = envPathString + "\\" + dirName;
		Path probDirPath = Paths.get(probDirPathString);
		File fileDirToBeCreated = probDirPath.toFile(); // Directory path
		File fileToBeGenerated = null; // .std file path
		if (fileDirToBeCreated.exists()) {
			boolean dontMakeDirFlag = true;
			//final int MAX_CHANCE_TO_CHANGE_DIRNAME = 99;
			int chance = 1;
			while (dontMakeDirFlag) {
				String changedDirName = changeSameDirName(dirName,
						chance);
				String probDirPathStringCahnaged = envPathString + "\\"
						+ changedDirName;
				fileDirToBeCreated = new File(probDirPathStringCahnaged);
				boolean stillExists = fileDirToBeCreated.exists();
				if (!stillExists) {
					dontMakeDirFlag = false;
					fileDirToBeCreated.mkdir();
					probDirPathStringCahnaged = probDirPathStringCahnaged
							+ "\\" + changedDirName + "." + format;
					fileToBeGenerated = new File(probDirPathStringCahnaged);
					try {
						fileToBeGenerated.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
						throw new PSAFileIOException(e.getMessage());
					}
				}
				chance += 1;
			}
			if (dontMakeDirFlag)
				System.out
						.println("SORRY : max chance to change dir name over : "
								+ chance);

		} else {
			fileDirToBeCreated.mkdirs();
			probDirPathString = probDirPathString + "/" + dirName + "."
					+ format;
			fileToBeGenerated = new File(probDirPathString);
			try {
				fileToBeGenerated.createNewFile();
			} catch (IOException e) {
				//e.printStackTrace();
				throw new PSAFileIOException(e.getMessage());
			}
		}

		return fileToBeGenerated.getPath();
	}

	private String changeSameDirName(String dirName,
			int chance) {
		//Random random = new Random();
		//int nextInt = random.nextInt(mAX_CHANCE_TO_CHANGE_DIRNAME);

		return dirName + "("+chance+")";
	}

	public static String getEnvironmentPath() {
		String pathString = null;
		try {
			pathString = ResourceBundleUtil.getString("info_env", "ENV_PATH");
		} catch (Exception e) {
			//System.out.println(e.getMessage());
			//System.exit(0);
		}
		if (pathString == null) {
			pathString = System.getProperty("user.dir");
			//pathString = pathString + "\\mSAP ENV";
			
		}

		// Path path = Paths.get(pathString);
		return pathString;
	}

	public File getSpecifiedFile(Path path, String format)
			throws PSAFileIOException {
		File file = path.toFile();
		boolean fileExists = file.exists();
		if (fileExists) {
			// Check if it end with std
			String fileName = file.getName();
			boolean endsWith = fileName.endsWith(format);
			if (endsWith)
				return file;
			else {
				String message = ResourceBundleUtil.getString("file_io_error",
						"INVALID_FILE_FORMAT");
				throw new PSAFileIOException(message + ": Expecting " + format
						+ " format");
			}
		} else {
			String message = ResourceBundleUtil.getString("file_io_error",
					"FILE_NOT_FOUND");
			throw new PSAFileIOException(message);
		}
	}

	public static Path getSTDFilePathToReadFromEnv(String nameOfProbInPath) {
		String environmentPath = getEnvironmentPath();
		String fileInEnv = environmentPath + "\\" + nameOfProbInPath + "\\"
				+ nameOfProbInPath + ".std";
		// System.out.println("file in ENV path: "+fileInEnv);
		return Paths.get(fileInEnv);
	}

	public static Set<String> getAllProblemNamesInEnvironment() {
		Set<String> problemNames = new HashSet<String>();
		String environmentPath = getEnvironmentPath();
		Path envPath = Paths.get(environmentPath);
		File file = envPath.toFile();
		File[] listRoots = file.listFiles();
		for (File fileSub : listRoots) {
			if (fileSub.isDirectory()) {
				String probName = fileSub.getName();

				problemNames.add(probName);
			}

		}

		return problemNames;
	}
}

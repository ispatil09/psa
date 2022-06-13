package com.psa.io.parser.std;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

import com.psa.entity.impl.Structure;
import com.psa.exception.PSAFileIOException;
import com.psa.exception.PSAUnParsableFileException;
import com.psa.io.parser.FileParser;
import com.psa.util.ResourceBundleUtil;

public class STDFileParserOOP implements FileParser {

	@Override
	public Structure getStructure(Path inputFilePath)
			throws PSAUnParsableFileException, PSAFileIOException {/*
		File inputFile = inputFilePath.toFile();
		if (!inputFile.exists()) {
			String message = ResourceBundleUtil.getString("file_io_error",
					"NO_FILE_IN_ENV");
			throw new PSAFileIOException(message + ": "
					+ inputFilePath.toString());
		}
		
		Scanner scanner = new Scanner(inputFile);
		scanner.
		
		String[] inputLines = new String[100];
		Charset charset = Charset.forName("US-ASCII");
		try (BufferedReader reader = Files.newBufferedReader(inputFilePath,
				charset)) {
			String line = null;
			int i = 1;
			while ((line = reader.readLine()) != null) {
				inputLines[i] = line;
				i++;
			}
		} catch (IOException x) {
			throw new PSAFileIOException(
					"Unable to read input file : may be security exception");
		}

		String strucName = inputFilePath.getFileName()
				.toString();
		Structure structure = new Structure(strucName.substring(0, strucName.length()-4));
		getStructureData(structure, inputLines);

		return structure;
	*/
	return null;	
	}

}

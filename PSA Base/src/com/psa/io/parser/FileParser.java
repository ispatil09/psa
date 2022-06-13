package com.psa.io.parser;

import java.nio.file.Path;

import com.psa.entity.impl.Structure;
import com.psa.exception.PSAFileIOException;
import com.psa.exception.PSAUnParsableFileException;

public interface FileParser {
	public Structure getStructure(Path pathOfFile) throws PSAUnParsableFileException, PSAFileIOException;
}

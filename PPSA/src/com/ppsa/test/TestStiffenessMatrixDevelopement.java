package com.ppsa.test;

import java.io.IOException;
import java.nio.file.Path;

import com.ppsa.exception.PPSAException;
import com.ppsa.exception.PPSAIOException;
import com.ppsa.util.OpenPSA;
import com.psa.entity.impl.Structure;
import com.psa.exception.PSAException;
import com.psa.io.EnvFileIOUtil;

public class TestStiffenessMatrixDevelopement {

	/**
	 * @param args
	 * @throws PSAException 
	 * @throws IOException 
	 * @throws PPSAIOException 
	 * @throws PPSAException 
	 */
	public static void main(String[] args) throws  PPSAIOException, PPSAException {
		//Path stdFilePathToRead = new EnvFileIOUtil().getSTDFilePathToReadFromEnv("TestTrussData_space");
		String probName = "PlaneFrame1";
		Path stdFilePathToRead = EnvFileIOUtil
				.getSTDFilePathToReadFromEnv(probName);
		Structure postProcessStructure = OpenPSA.postProcessStructure(stdFilePathToRead);
		/*Structure postProcessedStructure = PPSADataCopyUtil.getPostProcessedStructureFromFile("TestCaseProb2");
		System.out.println(postProcessedStructure);*/
	}
}

package com.ppsa.test;

import java.nio.file.Path;

import com.ppsa.exception.PPSAException;
import com.ppsa.exception.PPSAIOException;
import com.ppsa.util.OpenPSA;
import com.psa.entity.FiniteElement;
import com.psa.entity.impl.OneDimFiniteElement;
import com.psa.entity.impl.Structure;
import com.psa.io.EnvFileIOUtil;

public class TestBMDCurve {
	public static void main(String[] args) throws PPSAIOException, PPSAException {
		String structureName = "PlaneComplex1";
		Path stdFilePathToRead = EnvFileIOUtil
				.getSTDFilePathToReadFromEnv(structureName);
		Structure structureEntity = OpenPSA.postProcessStructure(stdFilePathToRead);
		
		
		int memIds[] = {1,7,10,11};
		
		for (int i = 0; i < memIds.length; i++) {
			
			/*System.out.println("\nElement "+memIds[i]+" : LoadCase 1\n");*/
			for (int j = 0; j <= 12; j++) {
				double ratio = (j / 12d);
				double[] intermediateMemberForcesAtDistance = OpenPSA
						.getIntermediateMemberForcesAtDistance(structureEntity,
								memIds[i], ratio, 1);
				System.out.println("" + ratio + " : "
						+ intermediateMemberForcesAtDistance[5]);
			}
		}
	}
}

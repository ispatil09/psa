package com.psa.process;

import com.psa.entity.impl.Structure;

public interface StructureProcessor {
	public void processStructureForAnalysis(Structure structure,int loadCaseNum);
}

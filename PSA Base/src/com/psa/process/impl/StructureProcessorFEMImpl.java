package com.psa.process.impl;

import com.psa.entity.impl.Structure;
import com.psa.process.StructureProcessor;

public class StructureProcessorFEMImpl implements StructureProcessor {
	@Override
	public void processStructureForAnalysis(Structure structure,int loadCaseNum) {/*
		LoadCase loadCase = new LoadCase(1,"FirstLoad CAssse");
		NodalLoad nodalLoadOne = loadCase.new NodalLoad(1);
		nodalLoadOne.setfX(12.4f);
		NodalLoad nodalLoadTwo = loadCase.new NodalLoad(2);
		nodalLoadTwo.setfY(33.4f);
		
		try {
			structure.addLoadCase(loadCase);
			structure.addLoadToANode(nodalLoadOne,3);
			structure.addLoadToANode(nodalLoadTwo, 3);
		} catch (PSAStructureInstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {		
		LoadCase loadCaseSec = new LoadCase(2,"Sec LC");
		NodalLoad nodalLoadSFirst = loadCaseSec.new NodalLoad(1);
		structure.addLoadCase(loadCaseSec);
		structure.addLoadToANode(nodalLoadSFirst, 2);
			
		} catch (PSAStructureInstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}
}

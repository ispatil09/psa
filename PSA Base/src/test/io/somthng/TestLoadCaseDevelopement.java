package test.io.somthng;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.psa.entity.FiniteElement;
import com.psa.entity.LoadCase;
import com.psa.entity.LoadCase.MemberLoad;
import com.psa.entity.LoadCase.SupportSettlementEntity;
import com.psa.entity.MaterialProperty;
import com.psa.entity.impl.Node;
import com.psa.entity.impl.OneDimFiniteElement;
import com.psa.entity.impl.Structure;
import com.psa.entity.impl.SupportDetails;
import com.psa.exception.PSAFileIOException;
import com.psa.exception.PSAUnParsableFileException;
import com.psa.exception.PSAUnParsableSTDFileException;
import com.psa.io.DataCopyUtil;
import com.psa.io.EnvFileIOUtil;
import com.psa.io.parser.FileParser;
import com.psa.io.parser.std.deprecated.STDFileParserOne;

public class TestLoadCaseDevelopement {

	/**
	 * @param args
	 * @throws PSAFileIOException 
	 * @throws PSAUnParsableFileException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws PSAFileIOException, PSAUnParsableFileException, IOException {
		Path stdFilePathToRead = EnvFileIOUtil.getSTDFilePathToReadFromEnv("SupportSettlement 02 PFrame");
		//Path stdFilePathToRead = new EnvFileIOUtil().getSTDFilePathToReadFromEnv("TestTrussData_space");
		FileParser parser = new STDFileParserOne();
		Structure structureEntity = parser.getStructure(stdFilePathToRead);
		//System.out.println(structureEntity);
		LoadCase loadCase = structureEntity.getLoadCase(1);
		List<SupportSettlementEntity> supportSettlements = loadCase.getSupportSettlements();
		for (SupportSettlementEntity supportSettlementEntity : supportSettlements) {
			System.out.println(supportSettlementEntity);
		}
		/*LoadCase loadCase2 = structureEntity.getLoadCase(2);
		List<SupportSettlementEntity> supportSettlementsSec = loadCase2.getSupportSettlements();
		for (SupportSettlementEntity supportSettlementEntity : supportSettlementsSec) {
			System.out.println(supportSettlementEntity);
		}*/

		System.out.println("Done");
	}

}

/*//new DataCopyUtil().copySTDFileToEnvPath("C:\\Users\\SONY\\Desktop\\xp share\\Sample Structure for input\\TestTrussData_space\\TestTrussData_space.std");
		Path stdFilePathToRead = EnvFileIOUtil.getSTDFilePathToReadFromEnv("TestCaseProb1");
		//Path stdFilePathToRead = new EnvFileIOUtil().getSTDFilePathToReadFromEnv("TestTrussData_space");
		FileParser parser = new STDFileParserOne();
		Structure structureEntity = parser.getStructure(stdFilePathToRead);
		new DataCopyUtil().ceateFileAndSaveText("TestTrussData_space", "This is text \n to be Saved");
		System.out.println("HHH");
		DataCopyUtil.savePreProcessedStructure(structureEntity);
	
		
		Structure postProcessedStructure = DataCopyUtil.getPreProcessedStructure("TestCaseProb1");
		System.out.println("Loaded Structure \n" + postProcessedStructure);
		
		EnvFileIOUtil.getAllProblemNamesInEnvironment();*/

// ---------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------


/*   // Testing Load cases work properly or no
Path stdFilePathToRead = new EnvFileIOUtil().getSTDFilePathToReadFromEnv("Structure1");
Structure structureEntity = new FileAnalyser().getStructureEntity(stdFilePathToRead);
new StructureProcessorFEMImpl().processStructureForAnalysis(structureEntity, 1);
LoadCase loadCase = structureEntity.getLoadCase(1);
System.out.println("LOAD CASE : "+loadCase.getLoadCaseName());
for (NodalLoad load : loadCase.getNodalLoads()) {
	System.out.println("Load case they belong to : "+load.getLoadCase());
	System.out.println(load);
}

System.out.println("\n\nNESSSS\n\n");

for (Node node : structureEntity.getNodes()) {
	System.out.println("Node : "+node.getNodeNumber()+" , loads : "+node.getNodalLoads().size());
	for (NodalLoad noLoad : node.getNodalLoads()) {
		System.out.println(noLoad+" CASE : "+noLoad.getLoadCase());
	}
}*/

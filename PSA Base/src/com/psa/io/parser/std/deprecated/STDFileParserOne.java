package com.psa.io.parser.std.deprecated;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import com.psa.entity.CommonNode;
import com.psa.entity.MaterialProperty;
import com.psa.entity.enums.FileFormatParsed;
import com.psa.entity.enums.ForceUnit;
import com.psa.entity.enums.LengthUnit;
import com.psa.entity.enums.OneDimFiniteElementType;
import com.psa.entity.enums.StructureType;
import com.psa.entity.impl.Node;
import com.psa.entity.impl.OneDimFiniteElement;
import com.psa.entity.impl.Structure;
import com.psa.exception.PSAFileIOException;
import com.psa.exception.PSAStructureInstantiationException;
import com.psa.exception.PSAUnParsableFileException;
import com.psa.exception.PSAUnParsableSTDFileException;
import com.psa.io.parser.FileParser;
import com.psa.util.MathHelper;
import com.psa.util.ResourceBundleUtil;

/**
 * Class is able to parse STD data till "END DEFINE MATERIAL".
 * To parse data from "MEMBER PROPERTY", see {@link STDFileParserTwo}.
 * @author SONY
 *
 */
public class STDFileParserOne implements FileParser {
	@Override
	public Structure getStructure(Path inputFilePath)
			throws PSAFileIOException, PSAUnParsableFileException {
		File inputFile = inputFilePath.toFile();
		if (!inputFile.exists()) {
			String message = ResourceBundleUtil.getString("file_io_error",
					"NO_FILE_IN_ENV");
			throw new PSAFileIOException(message + ": "
					+ inputFilePath.toString());
		}
		// System.out.println("Im still executing");
		String[] inputLines = new String[400];
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
		structure.setFormatParsed(FileFormatParsed.std);
		getStructureData(structure, inputLines);

		return structure;
	}

	/**
	 * This is main function which iterates through all lines..
	 * @param structure
	 * @param inputLines
	 * @return
	 * @throws PSAStructureInstantiationException 
	 * @throws PSAUnParsableFileException 
	 */
	private Structure getStructureData(Structure structure, String[] inputLines) throws PSAUnParsableFileException {
		//System.out.println("FROM method");

		int i = 1;

		setStructureType(structure, inputLines[i]);
		i++;
		if (inputLines[i].equals(ResourceBundleUtil.getString("basic_std_file_commands", "START_JOB_INFO"))) {

			i=processJobInformation(structure, inputLines, i);
		}
		//System.out.println("End of Job Info line : " + i);
		i++;
		
		i=getLineNumOfUnit(inputLines,i); // This function gives one extra line count
		//System.out.println("Unit line : "+i);
		setStructureUnits(structure,inputLines[i-1]);
		if (inputLines[i].equals(ResourceBundleUtil.getString("basic_std_file_commands", "JOINT_CO"))) {
			i=processJointCoOrdinates(structure, inputLines, i);
		}
		//System.out.println("After processing Joint Co-ord : "+i);
		i++;
		if (inputLines[i].equals(ResourceBundleUtil.getString("basic_std_file_commands", "MEM_INCI"))) {
			i=processMemberIncidence(structure, inputLines, i);
		}
		i++;
		//System.out.println("After member incidence over : "+i);
		
		int materialPropertiesCount=0;
		if(inputLines[i].equals(ResourceBundleUtil.getString("basic_std_file_commands", "START_MAT_PROP"))) {
			materialPropertiesCount = getMaterialPropertiesCount(inputLines,i);
		}
		//System.out.println("Material Properties count = "+materialPropertiesCount);
		i++;
		i=processMaterialProperties(structure,inputLines,i,materialPropertiesCount);
		//System.out.println("After Material Prop : "+i);
		//her afdsksfdnkl ln
		STDFileParserTwo fileParserTwo= new STDFileParserTwo();
		fileParserTwo.parseFileAfterDefineMaterial(structure, inputLines, i);
		return null;
	}

	private int processMaterialProperties(Structure structure,
			String[] inputLines, int i, int materialPropertiesCount) {
		for (int j = 0; j < materialPropertiesCount; j++) {
			String[] split = inputLines[i].split(" ");
			String materialName = split[1];
			MaterialProperty materialProperty = new MaterialProperty(materialName);
			i++;
			String[] splitSec = inputLines[i].split(" ");
			String EValString = splitSec[1];
			double Eval=0;
			try {
			Eval = Integer.parseInt(EValString);
			}
			catch (NumberFormatException e) {
				Eval=getProperEVal(EValString);
			}
			materialProperty.setE(Eval);
			structure.addMaterail(materialProperty);
			i=i+5;
		}
		return i;
	}

	private double getProperEVal(String eValString) {
		String[] strNew = eValString.split("e");
		String baseString = strNew[0];
		Double baseVal=Double.parseDouble(baseString);
		
		String powerString = strNew[1];
		int power = Integer.parseInt(powerString);
		
		double multiplant = StrictMath.pow(10, power);
		double EValue = baseVal*multiplant;
		//System.out.println(multiplant);
		//System.out.println(EValue);
		return EValue;
	}

	private int getMaterialPropertiesCount(String[] inputLines, int i) throws PSAUnParsableSTDFileException {
		i++;
		int count=1;	// Assuming that at least one material property there
		int j=i;
		String materialLine = "ISOTROPIC";
		do {
			j=j+6;
			String[] split = inputLines[j].split(" ");
			if(split[0].equals(materialLine))
				count++;
			
		} while (!inputLines[j].equals(ResourceBundleUtil.getString("basic_std_file_commands", "END_MAT_PROP")));
		return count;
	}
	
	/*do {
		// inputLines[]
		j=j+1;
		if(inputLines[j]==null) {
			//System.out.println("EOL reached"); Means input finished but no 'END JOB INFORMATION'
			throw new InavlidSTDFileException(ResourceBundleUtil.getString("std_file_error", "NO_END_MAT_PROP"));
		}
	} while (!inputLines[j].equals(ResourceBundleUtil.getString("basic_std_file_commands", "END_MAT_PROP")));
	return j;*/

	private int processMemberIncidence(Structure structure,
			String[] inputLines, int i) throws PSAUnParsableSTDFileException {
		boolean nextLineIsMemInci=true; // Assumed that very next Line after "MEMBER INCIDENCES" is having ";" in it
		while(nextLineIsMemInci) {
			i++;
			nextLineIsMemInci=inputLines[i+1].contains(";");
			processLineForMemIncidense(structure,inputLines[i]);
		}
		return i;
	}

	private void processLineForMemIncidense(Structure structure, String lineOfMemInci) throws PSAUnParsableSTDFileException {
		String[] split = lineOfMemInci.split(";");
		for (String string : split) {
			String[] strData = string.split(" ");
			int m=0;
			if(strData[0].equals("")) // Usually after ';' next word will have gap, this causes strData[0]="" ie.,nothing
				m=+1;
			int elementNum = Integer.parseInt(strData[0+m]);
			int nodeOneInt= Integer.parseInt(strData[1+m]);
			int nodeTwoInt = Integer.parseInt(strData[2+m]);
			
			OneDimFiniteElement finiteElement = new OneDimFiniteElement(elementNum);
			try {
				//finiteElement.addNode(null);
				CommonNode nodeConfirmOne = structure.getNode(nodeOneInt);
				CommonNode nodeConfirmTwo = structure.getNode(nodeTwoInt);
				
				nodeConfirmOne.addFiniteElement(finiteElement);
				nodeConfirmTwo.addFiniteElement(finiteElement);
				
				finiteElement.addNode((Node) nodeConfirmOne);
				finiteElement.addNode((Node) nodeConfirmTwo);
				
				/**
				 * Later you can add separately the type of element.
				 * As of now this is taken care in STDFileParserThree.
				 * Where all the element's are at a time set to type
				 * by observing type of structure.
				 */
				/*if(structure.getStructureType()==StructureType.TRUSS)
					finiteElement.setFiniteElementType(OneDimFiniteElementType.TRUSS2D);
				else if(structure.getStructureType()==StructureType.PLANE)
					finiteElement.setFiniteElementType(OneDimFiniteElementType.BEAM2D);
				else if(structure.getStructureType()==StructureType.TRUSS3D)
					finiteElement.setFiniteElementType(OneDimFiniteElementType.TRUSS3D);*/
				
				structure.addFiniteElementEntry(finiteElement);
				
				finiteElement.setFirstNode((Node)nodeConfirmOne);
				finiteElement.setSecondNode((Node)nodeConfirmTwo);
				StructureType structureType = structure.getStructureType();
				if (structureType == StructureType.PLANE
						|| structureType == StructureType.TRUSS) {
					if (nodeConfirmOne.getxCordinate() > nodeConfirmTwo
							.getxCordinate()) {
						finiteElement.setFirstNode((Node) nodeConfirmTwo);
						finiteElement.setSecondNode((Node) nodeConfirmOne);
						System.out.println("WARNING: Element "+elementNum+" should connect node "+nodeConfirmTwo+" first and then node "+nodeConfirmOne);
						System.out.println("Because for Plane structures it is preferred that beam should connect the left node first");
					}
				}
				
				MathHelper.setBasicOneDimFiniteElementData(finiteElement,structure);
				
				if(finiteElement.getFirstNode().getNodeNumber()==nodeConfirmOne.getNodeNumber())
					structure.getStdModelCorrectionHelper().addEntryReverseIndicatorEntry(finiteElement, false);
				else
					structure.getStdModelCorrectionHelper().addEntryReverseIndicatorEntry(finiteElement, true);
			} catch (PSAStructureInstantiationException e) {
				e.printStackTrace();
				throw new PSAUnParsableSTDFileException(e.getMessage());
			}
		}
		
	}

	private int processJointCoOrdinates(Structure structure,
			String[] inputLines, int i) throws PSAUnParsableSTDFileException {
		boolean nextLineIsCordinates=true; // Assumed that very next Line after "JOINT COORDINATES" is having ";" in it
		while(nextLineIsCordinates) {
			i++;
			nextLineIsCordinates=inputLines[i+1].contains(";");
			processLineForCoOrdinates(structure,inputLines[i]);
		}
		return i;
	}

	private void processLineForCoOrdinates(Structure structure, String lineOfCoOrinates) throws PSAUnParsableSTDFileException {
			String[] split = lineOfCoOrinates.split(";");
			for (String string : split) {
				String[] strData = string.split(" ");
				int m=0;
				if(strData[0].equals("")) // Usually after ';' next word will have gap, this causes strData[0]="" ie.,nothing
					m=+1;
				
				int nodeNum = Integer.parseInt(strData[0+m]);
				float xCord = Float.parseFloat(strData[1+m]);
				float yCord = Float.parseFloat(strData[2+m]);
				float zCord = Float.parseFloat(strData[3+m]);

				Node node = null;
					node = new Node(nodeNum, xCord, yCord, zCord,0,0,0);
				
				try {
					if(nodeNum==4)
						System.out.println("Del here.. Node 4..");
					
					structure.addNodeEntry(node);
				} catch (PSAStructureInstantiationException e) {
					throw new PSAUnParsableSTDFileException(e.getMessage());
				}
			}
	}

	private void setStructureUnits(Structure structure,String inputLine) throws PSAUnParsableSTDFileException {
		String[] splitStrs = inputLine.split(" ");
		String firstWord = splitStrs[1];
		String secondWord = splitStrs[2];
		
		if(firstWord.equalsIgnoreCase("NEWTON"))
			structure.getStructureUnits().setForceUnit(ForceUnit.NEWTON);
		else if(firstWord.equalsIgnoreCase("METER"))
			structure.getStructureUnits().setLengthUnit(LengthUnit.METER);
		
		if(secondWord.equalsIgnoreCase("METER"))
			structure.getStructureUnits().setLengthUnit(LengthUnit.METER);
		else if(secondWord.equalsIgnoreCase("NEWTON"))
			structure.getStructureUnits().setForceUnit(ForceUnit.NEWTON);
		
//		Tempeorily no need to check
		/*ForceUnit forceUnit = structure.getStructureUnits().getForceUnit();
		LengthUnit lengthUnit = structure.getStructureUnits().getLengthUnit();
		
		if(forceUnit==null || lengthUnit==null) {
			String string = ResourceBundleUtil.getString("std_file_error", "UNSUPPORTED_UNIT");
			throw new PSAUnParsableSTDFileException(string);
		}*/
		
	}

	private int getLineNumOfUnit(String[] inputLines,int i) throws PSAUnParsableSTDFileException {
		int j=i;
		String firstWordOfLine=null;
		do {
			String[] split = inputLines[j].split(" ");
			firstWordOfLine=split[0];
			j=j+1;
			if(inputLines[j]==null) {
				throw new PSAUnParsableSTDFileException(ResourceBundleUtil.getString("std_file_error", "NO_UNIT"));
			}
		} while (!firstWordOfLine.equals("UNIT"));
		return j;
	}

	private int processJobInformation(Structure structure,
			String[] inputLines, int i) throws PSAUnParsableSTDFileException {
		int j=i;
		do {
			// inputLines[]
			j=j+1;
			if(inputLines[j]==null) {
				//System.out.println("EOL reached"); Means input finished but no 'END JOB INFORMATION'
				throw new PSAUnParsableSTDFileException(ResourceBundleUtil.getString("std_file_error", "NO_END_JOB"));
			}
		} while (!inputLines[j].equals(ResourceBundleUtil.getString("basic_std_file_commands", "END_JOB_INFO")));
		return j;
	}

	private void setStructureType(Structure structure, String string) {

		String[] subStrings = string.split(" ");
		String structureType = subStrings[1];
		if (structureType.equals("TRUSS"))
			structure.setStructureType(StructureType.TRUSS);
		else if (structureType.equals("PLANE"))
			structure.setStructureType(StructureType.PLANE);
		else if (structureType.equals("SPACE"))
			structure.setStructureType(StructureType.SPACE);
		if(subStrings.length==3) {
			structure.setStructureType(StructureType.TRUSS3D);
		}
	}
}

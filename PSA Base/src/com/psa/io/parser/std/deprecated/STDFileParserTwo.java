package com.psa.io.parser.std.deprecated;

import java.util.ArrayList;
import java.util.List;

import com.psa.entity.FiniteElement;
import com.psa.entity.SectionProperty;
import com.psa.entity.SectionPropertyIndian;
import com.psa.entity.impl.GeneralSectionProperty;
import com.psa.entity.impl.OneDimFiniteElement;
import com.psa.entity.impl.PrisSectionProperty;
import com.psa.entity.impl.Structure;
import com.psa.exception.PSAUnParsableSTDFileException;
import com.psa.util.ResourceBundleUtil;
import com.sd.entity.SectionDetailer;
import com.sd.util.StructuralSteelSectionHelper;

/**
 * Class to parse STD file from 'END DEFINE MATERIAL'.
 * 
 * @author SONY
 * 
 */
public class STDFileParserTwo {

	public void parseFileAfterDefineMaterial(Structure structure, String[] inputLines, int i)
			throws PSAUnParsableSTDFileException {

		i++;
		i = parseMemberPropertyForSection(structure, inputLines, i);
		STDFileParserThree fileParserThree = new STDFileParserThree();
		fileParserThree.parseAfterConstants(structure,inputLines,i);
	}

	/**
	 * Parsing till "CONSTANTS" command.
	 * @param structure
	 * @param inputLines
	 * @param i
	 * @throws PSAUnParsableSTDFileException
	 */
	private int parseMemberPropertyForSection(Structure structure,
			String[] inputLines, int i) throws PSAUnParsableSTDFileException{
		int lineNumOfConstants = getLineNumOfConstantsCommand(inputLines, i);
		int numOfLinesToProcess = lineNumOfConstants - i;
		//System.out.println("Number of lines to process in between : " + numOfLinesToProcess);
		boolean isIndianSectionLine = checkIfMemberPropertyIndian(inputLines, i);
		for (int j = 0; j < numOfLinesToProcess-1; j++) {
			boolean isMemSectionLine = checkIfMemberPropertyCommand(inputLines, i);
			if (!isIndianSectionLine) {
				if (isMemSectionLine) {
					i++;
					// later: Commented assuming that MEMBER PROPERTY AMERICAN
					// appears only once.
					// j++;
					processLineOfSectionData(structure, inputLines, i);
					i++;
				} else {
					processLineOfSectionData(structure, inputLines, i);
					i++;
				}
			} else { // Indian Section
				String countryName = "INDIAN";
				if (isMemSectionLine) {
					i++;
					processLineOfSectionData_ST(structure, inputLines, i,countryName);
					i++;
				} else {
					processLineOfSectionData_ST(structure, inputLines, i,countryName);
					i++;
				}
			}
		}
		return i;
	}

	private void processLineOfSectionData_ST(Structure structure,
			String[] inputLines, int i, String countryName) throws PSAUnParsableSTDFileException {
		String[] splitString = inputLines[i].split("TABLE");

		List<Integer> memArray = new ArrayList<Integer>();
		// splitString[0] -> Members
		if(splitString.length>1) {
			memArray = getMembersAssociated(splitString[0]);
		} else { // find how many lines of members are there.
			int prisCommandLine = 0;
			for(int k=i;k<i+10;k++) {
				String[] splitStringCheck = inputLines[k].split("TABLE");
				if(splitStringCheck.length>1) {
					prisCommandLine = k;
					break;
				}
			}
			
			// Get mems from lines where no PRIS command not there
			for(int k=i;k<prisCommandLine;k++) {
				String[] split = inputLines[k].split("-");
				List<Integer> membersAssociated = getMembersAssociated(split[0]);
				memArray.addAll(membersAssociated);
			}
			
			// Get mems from lines where no PRIS command there
			{
				splitString = inputLines[prisCommandLine].split("TABLE");
				List<Integer> membersAssociated = getMembersAssociated(splitString[0]);
				memArray.addAll(membersAssociated);
			}
		}

		// splitString[1] -> Section Property
		String sectionNameRaw = splitString[splitString.length-1];
		String[] splitStrsSec = sectionNameRaw.split(" ");
		String sectionName = splitStrsSec[2];
		SectionProperty sectionProperty = getSectionProperty_ST(countryName,sectionName);

		setSectionPropertyToFiniteElements(structure, memArray, sectionProperty);
	}

	private SectionProperty getSectionProperty_ST(String countryName,String sectionName) {
		SectionDetailer sectionDetailer = StructuralSteelSectionHelper.getSectionDetailer(countryName, sectionName);
		SectionPropertyIndian propertyIndian = new SectionPropertyIndian(sectionDetailer);
		if(propertyIndian==null)
			throw new RuntimeException("Section : null");
		return propertyIndian;
	}

	private boolean checkIfMemberPropertyIndian(String[] inputLines, int i) {
		/*String memSectionCommand = ResourceBundleUtil.getString(
				"basic_std_file_commands", "MEM_SECTION");*/
		boolean isIndianSectionLine = inputLines[i].endsWith("INDIAN");
		return isIndianSectionLine;
	}

	private int getLineNumOfConstantsCommand(String[] inputLines, int i) {
		int lineNumOfConst = 0;
		for (int j = i; j < inputLines.length-1; j++) {
			if(inputLines[j+1]!=null)
				if (inputLines[j].equalsIgnoreCase("CONSTANTS"))
					return lineNumOfConst = j;
		}
		return lineNumOfConst;
	}

	/**
	 * Checks if line is having command "MEMBER PROPERTY".
	 * 
	 * @param inputLines
	 * @param i
	 * @return
	 */
	private boolean checkIfMemberPropertyCommand(String[] inputLines, int i) {
		String memSectionCommand = ResourceBundleUtil.getString(
				"basic_std_file_commands", "MEM_SECTION");
		boolean isMemSectionLine = inputLines[i].regionMatches(true, 0,
				memSectionCommand, 0, memSectionCommand.length());
		return isMemSectionLine;
	}

	/**
	 * Processes line containing section data. Example :
	 * "2 TO 4 PRIS YD 0.03 ZD 0.03"
	 * 
	 * @param structure
	 * @param inputLines
	 * @param i
	 * @throws PSAUnParsableSTDFileException
	 */
	private void processLineOfSectionData(Structure structure,
			String[] inputLines, int i) throws PSAUnParsableSTDFileException {
		String[] splitString = inputLines[i].split("PRIS");

		List<Integer> memArray = new ArrayList<Integer>();
		// splitString[0] -> Members
		if(splitString.length>1) {
			memArray = getMembersAssociated(splitString[0]);
		} else { // find how many lines of members are there.
			int prisCommandLine = 0;
			for(int k=i;k<i+10;k++) {
				String[] splitStringCheck = inputLines[k].split("PRIS");
				if(splitStringCheck.length>1) {
					prisCommandLine = k;
					break;
				}
			}
			
			// Get mems from lines where no PRIS command not there
			for(int k=i;k<prisCommandLine;k++) {
				String[] split = inputLines[k].split("-");
				List<Integer> membersAssociated = getMembersAssociated(split[0]);
				memArray.addAll(membersAssociated);
			}
			
			// Get mems from lines where no PRIS command there
			{
				splitString = inputLines[prisCommandLine].split("PRIS");
				List<Integer> membersAssociated = getMembersAssociated(splitString[0]);
				memArray.addAll(membersAssociated);
			}
		}

		// splitString[1] -> Section Property
		SectionProperty sectionProperty = getSectionProperty(splitString[1]);

		setSectionPropertyToFiniteElements(structure, memArray, sectionProperty);
	}

	/**
	 * 
	 * @param structure
	 * @param memArray
	 *            Array of Finite Elements to whom cross section is set
	 * @param sectionProperty
	 *            object of section property
	 * @throws PSAUnParsableSTDFileException
	 */
	private void setSectionPropertyToFiniteElements(Structure structure,
			List<Integer> memArray, SectionProperty sectionProperty)
			throws PSAUnParsableSTDFileException {
		/*int actualMemArraySize = 0;
		for (int j = 0; j < memArray.length; j++) {
			if (memArray[j] != 0)
				actualMemArraySize++;
			else
				break;
		}*/

		for (Integer member : memArray) {
			FiniteElement finiteElementByNum = structure
					.getFiniteElementByNum(member);
			if (finiteElementByNum == null)
				throw new PSAUnParsableSTDFileException("FiniteElement "
						+ member + " not defined: can't add section propperty");

			if (finiteElementByNum instanceof OneDimFiniteElement) {
				OneDimFiniteElement oneDimFiniteElement = (OneDimFiniteElement) finiteElementByNum;
				oneDimFiniteElement.setSectionProperty(sectionProperty);

			} else
				throw new PSAUnParsableSTDFileException(
						"Finite Element "
								+ member
								+ " is not OneDimensional to have cross sectional properties");

		}
	}

	private SectionProperty getSectionProperty(String sectionPropString) {
		String[] splitStrs = sectionPropString.split(" ");
		if (sectionPropString.contains("YD")) {
			/*double yd = Double.parseDouble(splitStrs[2]);
			double zd = Double.parseDouble(splitStrs[4]);
			return new PrisSectionProperty(yd, zd);*/
			double yd=0;
			double zd=0;
			for (int i = 1; i < splitStrs.length; i++) {
				if(!(i%2==0)) {
				if(splitStrs[i].equals("YD"))
					yd=Double.parseDouble(splitStrs[i+1]);
				else if(splitStrs[i].equals("ZD"))
					zd=Double.parseDouble(splitStrs[i+1]);
				}
			}
			return new PrisSectionProperty(yd, zd);
		} else {
			double AX=0;
			double IY=0;
			double IZ=0;
			double IX=0;
			for (int i = 1; i < splitStrs.length; i++) {
				if(!(i%2==0)) {
				if(splitStrs[i].equals("AX"))
					AX=Double.parseDouble(splitStrs[i+1]);
				else if(splitStrs[i].equals("IY"))
					IY=Double.parseDouble(splitStrs[i+1]);
				else if(splitStrs[i].equals("IZ"))
					IZ=Double.parseDouble(splitStrs[i+1]);
				else if(splitStrs[i].equals("IX"))
					IX=Double.parseDouble(splitStrs[i+1]);
				}
			}
			
			//System.out.println("section General");
			return new GeneralSectionProperty(AX,IX,IY,IZ);
		}
	}

	/**
	 * 'TO' keyword solved while getting members.
	 * 
	 * @param members
	 * @return
	 */
	private List<Integer> getMembersAssociated(String members) {
		String[] splitStr = members.split(" ");
//		int membersArray[] = new int[400];
		List<Integer> membersArray = new ArrayList<Integer>();
		int temp = 0;
		int loc = 0;
		for (int j = 0; j < splitStr.length; j++) {
			try {
				temp = Integer.parseInt(splitStr[j]);
				membersArray.add(temp);
				loc++;

			} catch (NumberFormatException e) { // Exception caught only when
												// word is "TO"
				String stringWord = splitStr[j];
				if (stringWord.equalsIgnoreCase("TO")) {
					int uptoMem = Integer.parseInt(splitStr[j + 1]);
					int fromMem = Integer.parseInt(splitStr[j - 1]);
					int sizeOfTo = uptoMem - fromMem;
					int l = 0;
					for (int m = 1; m < sizeOfTo + 1; m++) {
						membersArray.add(fromMem + m);
						l++;
					}
					loc = loc + l;
				}
			}
		}
		List<Object> objectList = new ArrayList<Object>();
		
		return membersArray;
	}

}

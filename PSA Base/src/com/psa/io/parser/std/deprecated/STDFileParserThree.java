package com.psa.io.parser.std.deprecated;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.psa.entity.CommonNode;
import com.psa.entity.FiniteElement;
import com.psa.entity.LoadCase;
import com.psa.entity.LoadCase.SupportSettlementEntity;
import com.psa.entity.OneDBeamEndMomentsEntity;
import com.psa.entity.OneDBeamLocalRotations;
import com.psa.entity.LoadCase.MemberLoad;
import com.psa.entity.LoadCase.MemberLoad.ConcentratedForce;
import com.psa.entity.LoadCase.MemberLoad.UniformForce;
import com.psa.entity.LoadCase.NodalLoad;
import com.psa.entity.MaterialProperty;
import com.psa.entity.OneDAxialForceEntity;
import com.psa.entity.OneDBeamEndShearForceEntity;
import com.psa.entity.OneDBeamEndTorsionEntity;
import com.psa.entity.VectorMatrix;
import com.psa.entity.enums.ConMemberLoadDirection;
import com.psa.entity.enums.DOF_TYPE;
import com.psa.entity.enums.StructureType;
import com.psa.entity.enums.UniMemberLoadDirection;
import com.psa.entity.enums.MemberLoadType;
import com.psa.entity.enums.SupportType;
import com.psa.entity.impl.Node;
import com.psa.entity.impl.NodeResults;
import com.psa.entity.impl.OneDimFiniteElement;
import com.psa.entity.impl.Structure;
import com.psa.entity.impl.SupportDetails;
import com.psa.exception.PSAException;
import com.psa.exception.PSAFileIOException;
import com.psa.exception.PSAStructureInstantiationException;
import com.psa.exception.PSAUnParsableSTDFileException;
import com.psa.io.DataCopyUtil;

public class STDFileParserThree {

	public void parseAfterConstants(Structure structure, String[] inputLines,
			int i) throws PSAUnParsableSTDFileException {

		i = parseMaterialProperties(structure, inputLines, i);
		//System.out.println("After Material : " + i);

		i++;

		i = parseSupportDetails(structure, inputLines, i);
		//System.out.println("After Supports : " + i);

		// For load cases
		ArrayList<Integer> lineNumsOfLoadCases = getLineNumsOfLoadCases(
				structure, inputLines, i);
		//System.out.println("Lines of LC : " + lineNumsOfLoadCases);
		parseLoadCases(structure, inputLines, i, lineNumsOfLoadCases);
		processTrussORSpace(inputLines, structure);

		/*try {
			DataCopyUtil.savePreProcessedStructure(structure);
		} catch (PSAFileIOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}*/
	}

	private void processTrussORSpace(String[] inputLines, Structure structure) {
		//String[] splitStrs = inputLines[1].split(" ");
		if (structure.getStructureType()==StructureType.TRUSS)
			structure.setAllFiniteElementsTruss();
		else if (structure.getStructureType()==StructureType.PLANE)
			structure.setAllFiniteElementsBeam();
		else if (structure.getStructureType()==StructureType.TRUSS3D)
			structure.setAllFiniteElementsTruss3D();
		else if (structure.getStructureType()==StructureType.SPACE)
			structure.setAllFiniteElementsBeam3D();
	}

	private int parseLoadCases(Structure structure, String[] inputLines, int i,
			ArrayList<Integer> lineNumsOfLoadCases)
			throws PSAUnParsableSTDFileException {
		Map<Integer, LoadCase> loadCases = new HashMap<Integer, LoadCase>();

		int actualNumOfLoadCases = lineNumsOfLoadCases.size();
		// Important : add dummy value to loadCases. dummy value represents end
		// of last load case
		if (actualNumOfLoadCases != 0)
			addEndLineOfLAstLoadCase(inputLines, lineNumsOfLoadCases);
		for (int j = 0; j < actualNumOfLoadCases; j++) {
			i = lineNumsOfLoadCases.get(j);
			String inputLine = inputLines[i];
			LoadCase loadCase = processLineOfLoadCase(inputLine,structure);

			// add Map<OneDimFiniteElement, OneDAxialForceEntity>
			// axialForceEntities , Set values
			//Set<FiniteElement> finiteElements = structure.getFiniteElements();

			

			i++;
			int numOfLinesToNextLoadCase = 0;

			if (lineNumsOfLoadCases.get(j + 1) != 0)
				numOfLinesToNextLoadCase = lineNumsOfLoadCases.get(j + 1)
						- lineNumsOfLoadCases.get(j);

			int lineOfJointLoad = 0;
			for (int k = i; k < i + numOfLinesToNextLoadCase; k++) {
				if (inputLines[k].equalsIgnoreCase("JOINT LOAD")) {
					lineOfJointLoad = k;
					break;
				}
			}
			if (lineOfJointLoad != 0) {
				i = lineOfJointLoad;
				i++;
			}
			else
				i--;
			
			int lineOfJointLoadEnd = 0;
			if (lineOfJointLoad != 0) {
				for (int k = i; k < i + numOfLinesToNextLoadCase; k++) {
					String[] splitStrs = inputLines[k].split(" ");
					try {
						int checkNum = Integer.parseInt(splitStrs[0]);
					} catch (NumberFormatException e) {
						lineOfJointLoadEnd = k;
						break;
					}
				}
			}

			for (int k = i; k < lineOfJointLoadEnd; k++) {
				processJointLoadsLine(inputLines[k], loadCase, structure);
			}

			// ----- Trying for Member Load include ----
			// ------------------------------------------

			i++;

			// int numOfLinesToNextLoadCase = 0;

			/*
			 * if (lineNumsOfLoadCases.get(j + 1) != 0) numOfLinesToNextLoadCase
			 * = lineNumsOfLoadCases.get(j + 1) - lineNumsOfLoadCases.get(j);
			 */

			int lineOfMemberLoad = 0;
			for (int k = i; k < i + numOfLinesToNextLoadCase; k++) {
				if (inputLines[k].equalsIgnoreCase("MEMBER LOAD")) {
					lineOfMemberLoad = k;
					break;
				}
			}
			if (lineOfMemberLoad != 0) {
				i = lineOfMemberLoad;
				i++;
			}
			else
				i--;
			int lineOfMemberLoadEnd = 0;
			if (lineOfMemberLoad != 0) {
				for (int k = i; k < i + numOfLinesToNextLoadCase; k++) {
					String[] splitStrs = inputLines[k].split(" ");
					try {
						int checkNum = Integer.parseInt(splitStrs[0]);
					} catch (NumberFormatException e) {
						lineOfMemberLoadEnd = k;
						break;
					}
				}
			}

			for (int k = i; k < lineOfMemberLoadEnd; k++) {
				processMemberLoadsLine(inputLines[k], loadCase, structure);
			}

			// ----- END : Trying for Member Load include ----
			
			// ----- Trying for Support Settlement  ----
						// ------------------------------------------

						i++;

						// int numOfLinesToNextLoadCase = 0;

						/*
						 * if (lineNumsOfLoadCases.get(j + 1) != 0) numOfLinesToNextLoadCase
						 * = lineNumsOfLoadCases.get(j + 1) - lineNumsOfLoadCases.get(j);
						 */

						int lineOfSupportSettlement = 0;
						int numOfLinesToNextLoadCase_For_Suppsett = 0;
						if (lineNumsOfLoadCases.get(j + 1) != 0)
							numOfLinesToNextLoadCase_For_Suppsett = lineNumsOfLoadCases.get(j + 1)
									- i;
						for (int k = i; k < i + numOfLinesToNextLoadCase_For_Suppsett; k++) {
							if (inputLines[k].equalsIgnoreCase("SUPPORT DISPLACEMENT LOAD")) {
								lineOfSupportSettlement = k;
								break;
							}
						}
						if (lineOfSupportSettlement != 0) {
							i = lineOfSupportSettlement;
							i++;
						}
						else
							i--;
						int lineOfSupportSettlementEnd = 0;
						if (lineOfSupportSettlement != 0) {
							for (int k = i; k < i + numOfLinesToNextLoadCase; k++) {
								String[] splitStrs = inputLines[k].split(" ");
								try {
									int checkNum = Integer.parseInt(splitStrs[0]);
								} catch (NumberFormatException e) {
									lineOfSupportSettlementEnd = k;
									break;
								}
							}
						}

						for (int k = i; k < lineOfSupportSettlementEnd; k++) {
							processSupportSettlementLine(inputLines[k], loadCase, structure);
						}

						// ----- END : Trying for Support Settlement include ----
			
			boolean containsKey = loadCases.containsKey(loadCase
					.getLoadCaseNum());
			if (containsKey)
				throw new PSAUnParsableSTDFileException(
						" Duplicate LOAD CASE number found : "
								+ loadCase.getLoadCaseNum());
			loadCases.put(loadCase.getLoadCaseNum(), loadCase);

		}
		structure.setLoadCases(loadCases);
		return i;
	}

	private void processSupportSettlementLine(String lineOfSettlementLoad, LoadCase loadCase,
			Structure structure) throws PSAUnParsableSTDFileException {
		String[] splitStrs = null;
		String DOF = null;
		double displacement = 0;
		int index = 0;
		if(lineOfSettlementLoad.contains("F")) {
			splitStrs = lineOfSettlementLoad.split("F");
			index = lineOfSettlementLoad.indexOf("F");
		}
		else if(lineOfSettlementLoad.contains("M")) {
			splitStrs = lineOfSettlementLoad.split("M");
			index = lineOfSettlementLoad.indexOf("M");
		}
		DOF = lineOfSettlementLoad.substring(index, (index+2));
		String dispStr = lineOfSettlementLoad.substring(index+3,lineOfSettlementLoad.length());
		displacement = Double.parseDouble(dispStr);
		//Convert displacement to radian
		if(lineOfSettlementLoad.contains("M")) {
			displacement = displacement*Math.PI/180;
		}
			

		String stringOfMembers = splitStrs[0];
		ArrayList<Integer> nodesAssociatedArray = getMembersAssociated(stringOfMembers);
		SupportSettlementEntity supportSettlementEntity = loadCase.new SupportSettlementEntity();
		supportSettlementEntity.setCordinate(DOF);
		supportSettlementEntity.setSettlement(displacement);
		System.out.println("Nodes...");
		try {
			addSupportSettlemntDetailsToLoadCase(supportSettlementEntity,loadCase,nodesAssociatedArray,structure);
		} catch (PSAStructureInstantiationException e) {
			throw new PSAUnParsableSTDFileException(e.getMessage());
		}
	}

	private void addSupportSettlemntDetailsToLoadCase(
			SupportSettlementEntity supportSettlementEntity, LoadCase loadCase,
			ArrayList<Integer> nodesAssociatedArray, Structure structure) throws PSAStructureInstantiationException {
		Set<Node> nodes = new HashSet<Node>();
		for (Integer nodeNum : nodesAssociatedArray) {
			try {
				Node node = structure.getNode(nodeNum);
				nodes.add(node);
			} catch (PSAStructureInstantiationException e) {
				throw new PSAStructureInstantiationException(e.getMessage());
			}
		}
		supportSettlementEntity.setNodes(nodes);
		loadCase.addSupportSettlement(supportSettlementEntity);
	}

	private void processMemberLoadsLine(String lineOfMemberLoad,
			LoadCase loadCase, Structure structure) throws PSAUnParsableSTDFileException {
		String[] splitStrs = lineOfMemberLoad.split(" ");
		int indexOfLoadBeginning = getIndexOfLoadBeginning(splitStrs);
		String stringOfMembers = "";
		for (int j = 0; j < indexOfLoadBeginning; j++) {
			stringOfMembers = stringOfMembers + " " + splitStrs[j];
		}
		ArrayList<Integer> membersAssociatedArray = getMembersAssociated(stringOfMembers);
		Set<OneDimFiniteElement> membersArray = getMembersArray(membersAssociatedArray,
				structure);
		addMemberLoadValues(splitStrs, indexOfLoadBeginning,loadCase,membersArray);
	}

	private void addMemberLoadValues(String[] splitStrs,
			int indexOfLoadBeginning, LoadCase loadCase, Set<OneDimFiniteElement> membersArray) throws PSAUnParsableSTDFileException {
		
			if (splitStrs[indexOfLoadBeginning].equalsIgnoreCase(MemberLoadType.UNI.toString())) {
				MemberLoad memberLoad = loadCase.new MemberLoad(MemberLoadType.UNI);
				UniformForce uniformForce = memberLoad.getUniformForce();
				
				String directionString = splitStrs[indexOfLoadBeginning + 1];
				setMemberDirection(directionString,uniformForce);
				
				String forceString = splitStrs[indexOfLoadBeginning + 2];
				double forceVal = Double.parseDouble(forceString);
				uniformForce.setForceVal(forceVal);
				
				if(splitStrs.length>(indexOfLoadBeginning + 3)) {
					String rangeBeginString = splitStrs[indexOfLoadBeginning + 3];
					String rangeEndString = splitStrs[indexOfLoadBeginning + 4];
					
					double rangeBegin = Double.parseDouble(rangeBeginString);
					double rangeEnd = Double.parseDouble(rangeEndString);
					
					uniformForce.setRangeBegin(rangeBegin);
					uniformForce.setRangeEnd(rangeEnd);
				} 
				
				loadCase.addMemberLoad(memberLoad, membersArray);
			} else if (splitStrs[indexOfLoadBeginning].equalsIgnoreCase(MemberLoadType.CON.toString())) {
				MemberLoad memberLoad = loadCase.new MemberLoad(MemberLoadType.CON);
				//UniformForce uniformForce = memberLoad.getUniformForce();
				ConcentratedForce conForce = memberLoad.getConcentratedForce();
				
				String directionString = splitStrs[indexOfLoadBeginning + 1];
				setConLoadDirection(directionString,conForce);
				
				String forceString = splitStrs[indexOfLoadBeginning + 2];
				double forceVal = Double.parseDouble(forceString);
				conForce.setForceVal(forceVal);
				
				String d1String = splitStrs[indexOfLoadBeginning + 3];
				double d1Val = Double.parseDouble(d1String);
				conForce.setD1(d1Val);
				
				loadCase.addMemberLoad(memberLoad, membersArray);
			}
	}

	private void setConLoadDirection(String directionString,
			ConcentratedForce conForce) throws PSAUnParsableSTDFileException {
		if(directionString.equalsIgnoreCase(UniMemberLoadDirection.Y.toString())) {
			conForce.setLoadDirection(ConMemberLoadDirection.Y);
		} else if(directionString.equalsIgnoreCase(UniMemberLoadDirection.X.toString())) {
			conForce.setLoadDirection(ConMemberLoadDirection.X);
		} else if(directionString.equalsIgnoreCase(UniMemberLoadDirection.GY.toString())) {
			conForce.setLoadDirection(ConMemberLoadDirection.GY);
		} else if(directionString.equalsIgnoreCase(UniMemberLoadDirection.GX.toString())) {
			conForce.setLoadDirection(ConMemberLoadDirection.GX);
		} else if(directionString.equalsIgnoreCase(UniMemberLoadDirection.Z.toString())) {
			conForce.setLoadDirection(ConMemberLoadDirection.Z);
		} else if(directionString.equalsIgnoreCase(UniMemberLoadDirection.GZ.toString())) {
			conForce.setLoadDirection(ConMemberLoadDirection.GZ);
		} 
	}

	private void setMemberDirection(String directionString,
			UniformForce uniformForce) throws PSAUnParsableSTDFileException {
		if(directionString.equalsIgnoreCase(UniMemberLoadDirection.Y.toString())) {
			uniformForce.setLoadDirection(UniMemberLoadDirection.Y);
		} else if(directionString.equalsIgnoreCase(UniMemberLoadDirection.X.toString())) {
			uniformForce.setLoadDirection(UniMemberLoadDirection.X);
			//throw new PSAUnParsableSTDFileException("Member Load with Local X direction has no valid meaning..");
		} else if(directionString.equalsIgnoreCase(UniMemberLoadDirection.GY.toString())) {
			uniformForce.setLoadDirection(UniMemberLoadDirection.GY);
		} else if(directionString.equalsIgnoreCase(UniMemberLoadDirection.GX.toString())) {
			uniformForce.setLoadDirection(UniMemberLoadDirection.GX);
		} else if(directionString.equalsIgnoreCase(UniMemberLoadDirection.PX.toString())) {
			uniformForce.setLoadDirection(UniMemberLoadDirection.PX);
		} else if(directionString.equalsIgnoreCase(UniMemberLoadDirection.PY.toString())) {
			uniformForce.setLoadDirection(UniMemberLoadDirection.PY);
		} else if(directionString.equalsIgnoreCase(UniMemberLoadDirection.Z.toString())) {
			uniformForce.setLoadDirection(UniMemberLoadDirection.Z);
		} else if(directionString.equalsIgnoreCase(UniMemberLoadDirection.GZ.toString())) {
			uniformForce.setLoadDirection(UniMemberLoadDirection.GZ);
		} else if(directionString.equalsIgnoreCase(UniMemberLoadDirection.PZ.toString())) {
			uniformForce.setLoadDirection(UniMemberLoadDirection.PZ);
		}
		
	}

	private Set<OneDimFiniteElement> getMembersArray(
			ArrayList<Integer> commonNodesArray, Structure structure)
			throws PSAUnParsableSTDFileException {
		Set<OneDimFiniteElement> oneDFiniteElements = new HashSet<OneDimFiniteElement>();
		for (Integer nodeNumber : commonNodesArray) {
			FiniteElement finiteElementByNum = structure
					.getFiniteElementByNum(nodeNumber);
			oneDFiniteElements.add((OneDimFiniteElement) finiteElementByNum);
		}
		return oneDFiniteElements;
	}

	private void addEndLineOfLAstLoadCase(String[] inputLines,
			ArrayList<Integer> lineNumsOfLoadCases) {
		int startLineOfLastLoadCase = 0;

		startLineOfLastLoadCase = lineNumsOfLoadCases.get(lineNumsOfLoadCases
				.size() - 1);
		int lastLineOfLastLoadCase = getEndOfLastLoadCase(inputLines,
				startLineOfLastLoadCase);
		lineNumsOfLoadCases.add(lastLineOfLastLoadCase);
	}

	private int getEndOfLastLoadCase(String[] inputLines,
			int startOfLastLoadCase) {
		int count = startOfLastLoadCase;
		for (int j = startOfLastLoadCase + 1; j < inputLines.length; j++) {
			if (inputLines[j].equalsIgnoreCase("PERFORM ANALYSIS"))
				break;
			else if (inputLines[j].equalsIgnoreCase("FINISH"))
				break;

			else if (inputLines[j + 1] == null)
				break;

			count++;

		}
		return count;
	}

	/**
	 * method sets NodalLodal with Set<CommonNodes>, present in line.
	 * 
	 * @param lineOfJointLoad
	 * @param loadCase
	 * @throws PSAUnParsableSTDFileException
	 */
	private void processJointLoadsLine(String lineOfJointLoad,
			LoadCase loadCase, Structure structure)
			throws PSAUnParsableSTDFileException {
		String[] splitStrs = lineOfJointLoad.split(" ");
		int indexOfLoadBeginning = getIndexOfLoadBeginning(splitStrs);
		String stringOfMembers = "";
		for (int j = 0; j < indexOfLoadBeginning; j++) {
			stringOfMembers = stringOfMembers + " " + splitStrs[j];
		}
		ArrayList<Integer> commonNodesArray = getMembersAssociated(stringOfMembers);
		Set<CommonNode> commonNodes = getCommonNodes(commonNodesArray,
				structure);

		NodalLoad nodalLoad = loadCase.new NodalLoad();
		addNodalLoadValues(splitStrs, indexOfLoadBeginning, nodalLoad);
		loadCase.addNodalLoad(nodalLoad, commonNodes);
	}

	private Set<CommonNode> getCommonNodes(ArrayList<Integer> commonNodesArray,
			Structure structure) throws PSAUnParsableSTDFileException {
		Set<CommonNode> commonNodes = new HashSet<CommonNode>();
		for (Integer nodeNumber : commonNodesArray) {
			try {
				Node node = structure.getNode(nodeNumber);
				commonNodes.add(node);
			} catch (PSAStructureInstantiationException e) {
				throw new PSAUnParsableSTDFileException(e.getMessage());
			}
		}
		return commonNodes;
	}

	private void addNodalLoadValues(String[] splitStrs,
			int indexOfLoadBeginning, NodalLoad nodalLoad) {
		for (int j = indexOfLoadBeginning; j < splitStrs.length; j++) {
			if (splitStrs[j].equalsIgnoreCase("FX")) {
				String fxString = splitStrs[j + 1];
				double parseDouble = Double.parseDouble(fxString);
				nodalLoad.setfX(parseDouble);
			} else if (splitStrs[j].equalsIgnoreCase("FY")) {
				String fyString = splitStrs[j + 1];
				double parseDouble = Double.parseDouble(fyString);
				nodalLoad.setfY(parseDouble);
			} else if (splitStrs[j].equalsIgnoreCase("FZ")) {
				String fzString = splitStrs[j + 1];
				double parseDouble = Double.parseDouble(fzString);
				nodalLoad.setfZ(parseDouble);
			} else if (splitStrs[j].equalsIgnoreCase("MX")) {
				String mxString = splitStrs[j + 1];
				double parseDouble = Double.parseDouble(mxString);
				nodalLoad.setmX(parseDouble);
			} else if (splitStrs[j].equalsIgnoreCase("MY")) {
				String myString = splitStrs[j + 1];
				double parseDouble = Double.parseDouble(myString);
				nodalLoad.setmY(parseDouble);
			} else if (splitStrs[j].equalsIgnoreCase("MZ")) {
				String mzString = splitStrs[j + 1];
				double parseDouble = Double.parseDouble(mzString);
				nodalLoad.setmZ(parseDouble);
			}
		}
	}

	/**
	 * Returns index of word where loading starts. Ex: index where
	 * MX,MY,FX,...etc starts. Other than TO are considered as end.
	 * 
	 * @param splitStrs
	 * @return
	 */
	private int getIndexOfLoadBeginning(String[] splitStrs) {
		for (int j = 0; j < splitStrs.length; j++) {
			try {
				int parseIntCheck = Integer.parseInt(splitStrs[j]);
			} catch (NumberFormatException e) {
				if (splitStrs[j].equalsIgnoreCase("TO"))
					continue;
				else {
					return j;
				}
			}
		}
		return 0;
	}

	private LoadCase processLineOfLoadCase(String inputLine, Structure structure) {

		String[] splitStrs = inputLine.split(" ");
		int loadCaseNum = Integer.parseInt(splitStrs[1]);

		String loadType = new String("");
		String loadCaseTitle = new String("");
		int loadTypePresentAtLocation = checkNumContainsString(splitStrs,
				"LOADTYPE");
		int loadCaseTitlePresentAtLocation = checkNumContainsString(splitStrs,
				"TITLE");

		// Only loadType is present
		if (loadTypePresentAtLocation != 0
				&& loadCaseTitlePresentAtLocation == 0) {
			for (int j = loadTypePresentAtLocation + 1; j < splitStrs.length; j++) {
				loadType = loadType + " " + splitStrs[j];
			}
		}

		// Only loadCase Title is present
		if (loadTypePresentAtLocation == 0
				&& loadCaseTitlePresentAtLocation != 0) {
			for (int j = loadCaseTitlePresentAtLocation + 1; j < splitStrs.length; j++) {
				loadCaseTitle = loadCaseTitle + " " + splitStrs[j];
			}
		}

		// Both loadType and LoadCaseTitle are present
		if (loadTypePresentAtLocation != 0
				&& loadCaseTitlePresentAtLocation != 0) {

			// loadType is present first
			if (loadTypePresentAtLocation < loadCaseTitlePresentAtLocation) {

				// LoadType process
				for (int j = loadTypePresentAtLocation + 1; j < loadCaseTitlePresentAtLocation; j++) {
					loadType = loadType + " " + splitStrs[j];
				}

				// LoadCaseTitle process
				for (int j = loadCaseTitlePresentAtLocation + 1; j < splitStrs.length; j++) {
					loadCaseTitle = loadCaseTitle + " " + splitStrs[j];
				}
			}

			// loadCaseTitle is present first
			if (loadCaseTitlePresentAtLocation < loadTypePresentAtLocation) {

				// LoadCaseTitle process
				for (int j = loadCaseTitlePresentAtLocation + 1; j < loadTypePresentAtLocation; j++) {
					loadCaseTitle = loadCaseTitle + " " + splitStrs[j];
				}

				// LoadCaseTitle process
				for (int j = loadTypePresentAtLocation + 1; j < splitStrs.length; j++) {
					loadType = loadType + " " + splitStrs[j];
				}
			}
		}

		/*System.out.println("LType : " + loadType + " , LTitle : "
				+ loadCaseTitle);*/

		LoadCase loadCase = new LoadCase(loadCaseNum, loadCaseTitle, loadType,structure);
		addBasicEntitiesToLoadCase(loadCase,structure);
		addNodeResultsEntityToAllNodes(loadCase);
		return loadCase;
	}
	private static void addNodeResultsEntityToAllNodes(LoadCase loadCase) {
		Set<Node> nodes = loadCase.getParentStructure().getNodes();

		Map<CommonNode, NodeResults> nodeResults = loadCase.getNodeResults();
		for (CommonNode node : nodes) {
			NodeResults nodeResult = new NodeResults();
			nodeResults.put(node, nodeResult);
		}

		loadCase.setNodeResults(nodeResults);
	}

	private void addBasicEntitiesToLoadCase(LoadCase loadCase,
			Structure structure) {
		Map<OneDimFiniteElement, OneDAxialForceEntity> axialForceEntities = loadCase.getAxialForceEntities();
		Map<OneDimFiniteElement, OneDBeamEndMomentsEntity> beamEndMomentEntities = loadCase.getBeamEndMomentEntities();
		Map<OneDimFiniteElement, OneDBeamEndShearForceEntity> beamEndShearForceEntities = loadCase.getBeamEndShearForceEntities();
		Map<OneDimFiniteElement, OneDBeamEndTorsionEntity> beamEndTorsionEntities = loadCase.getBeamEndTorsionEntities();
		Map<OneDimFiniteElement, OneDBeamLocalRotations> beamEndLocalRotations = loadCase.getBeamEndLocalRotations();
		
		Set<FiniteElement> finiteElements = structure.getFiniteElements();
		for (FiniteElement finiteElement : finiteElements) {
			if(finiteElement instanceof OneDimFiniteElement) {
				OneDimFiniteElement oneDimFiniteElement = (OneDimFiniteElement) finiteElement;
			OneDAxialForceEntity axialForceEntity = new OneDAxialForceEntity();
			OneDBeamEndMomentsEntity beamEndMomentsEntity = new OneDBeamEndMomentsEntity();
			OneDBeamEndShearForceEntity beamEndShearForceEntity = new OneDBeamEndShearForceEntity();
			OneDBeamEndTorsionEntity oneDBeamEndTorsionEntity = new OneDBeamEndTorsionEntity();
			OneDBeamLocalRotations oneDBeamLocalRotations = new OneDBeamLocalRotations();

			axialForceEntities.put(oneDimFiniteElement, axialForceEntity);
			beamEndMomentEntities.put(oneDimFiniteElement, beamEndMomentsEntity);
			beamEndShearForceEntities.put(oneDimFiniteElement, beamEndShearForceEntity);
			beamEndTorsionEntities.put(oneDimFiniteElement, oneDBeamEndTorsionEntity);
			beamEndLocalRotations.put(oneDimFiniteElement, oneDBeamLocalRotations);
			}
		}
	}

	/**
	 * Checks if a String is present.
	 * 
	 * @param splitStrs
	 * @param stringToCheck
	 * @return locationOfString in array.
	 */
	private int checkNumContainsString(String[] splitStrs, String stringToCheck) {
		for (int j = 0; j < splitStrs.length; j++) {
			if (splitStrs[j].equalsIgnoreCase(stringToCheck))
				return j;
		}
		return 0;
	}

	private ArrayList<Integer> getLineNumsOfLoadCases(Structure structure,
			String[] inputLines, int i) {
		ArrayList<Integer> lineNumsOfLoadCases = new ArrayList<Integer>();
		for (int j = i; j < inputLines.length; j++) {
			if (inputLines[j] != null) {
				String[] splitStrs = inputLines[j].split(" ");

				boolean isFirstWordLoad = splitStrs[0].equalsIgnoreCase("Load");
				if (isFirstWordLoad) {
					lineNumsOfLoadCases.add(j);
				}
			}
		}
		return lineNumsOfLoadCases;
	}

	private int parseSupportDetails(Structure structure, String[] inputLines,
			int i) throws PSAUnParsableSTDFileException {
		int lineNumOfSupportDetailsEnd = getLineNumOfSupportDetailsEnd(
				inputLines, i);
		// System.out.println("End of Support details at : "+lineNumOfSupportDetailsEnd);
		int numOfLinesToProcess = lineNumOfSupportDetailsEnd - i;

		for (int j = 0; j < numOfLinesToProcess; j++) {
			processLineOfSupportData(structure, inputLines, i);
			i++;
		}
		return i;
	}

	private void processLineOfSupportData(Structure structure,
			String[] inputLines, int i) throws PSAUnParsableSTDFileException {
		SupportDetails supportDetails = getSupportEntity(inputLines, i);

		String[] splitStr = inputLines[i].split(supportDetails.getSupportName()
				.toString());
		ArrayList<Integer> memArray = getMembersAssociated(splitStr[0]);

		setSupportDetailsToNodes(structure, memArray, supportDetails);
		setSupportDetailsToOtherCommonNodes(structure);

	}

	private void setSupportDetailsToOtherCommonNodes(Structure structure) {
		Set<Node> nodes = structure.getNodes();
		for (Node node : nodes) {
			SupportDetails supportDetails = node.getSupportDetails();
			if (supportDetails == null) {
				supportDetails = new SupportDetails();
				node.setSupportDetails(supportDetails);
			}
		}
	}

	private void setSupportDetailsToNodes(Structure structure,
			ArrayList<Integer> memArray, SupportDetails supportDetails)
			throws PSAUnParsableSTDFileException {
		int actualMemArraySize = memArray.size();

		try {
			for (int j = 0; j < actualMemArraySize; j++) {
				CommonNode commonNode = structure.getNode(memArray.get(j));
				if (commonNode == null)
					throw new PSAUnParsableSTDFileException("Node "
							+ memArray.get(j)
							+ " not defined : can not add support details");

				commonNode.setSupportDetails(supportDetails);
			}
		} catch (PSAException e) {
			throw new PSAUnParsableSTDFileException(e.getMessage());
		}
	}

	private int getActualSizeOfArray(int[] memArray) {
		int actualMemArraySize = 0;
		for (int j = 0; j < memArray.length; j++) {
			if (memArray[j] != 0)
				actualMemArraySize++;
			else
				break;
		}
		return actualMemArraySize;
	}

	private SupportDetails getSupportEntity(String[] inputLines, int i) {

		SupportDetails supportDetails = new SupportDetails();
		getSupportNameString(inputLines[i], supportDetails);
		// supportDetails.setSupportName(supportName);
		return supportDetails;
	}

	private void getSupportNameString(String string,
			SupportDetails supportDetails) {
		String[] splitStr = string.split(" ");
		for (int j = 0; j < splitStr.length; j++) {
			if (splitStr[j].equals(SupportType.FIXED.toString())) {
				// Now check if any further data after FIXED command.
				supportDetails.setSupportName(SupportType.FIXED);
				setFixedButDetails(j, splitStr, supportDetails);
			} else if (splitStr[j].equals(SupportType.PINNED.toString())) {
				supportDetails.setSupportName(SupportType.PINNED);
			}
		}
	}

	private void setFixedButDetails(int j, String[] splitStr,
			SupportDetails supportDetails) {
		if (splitStr.length > j + 1) { // This implies that there is some data
										// after
										// fixed command.
			if (splitStr[j + 1].equalsIgnoreCase("BUT")) {
				for (int i = j + 2; i < splitStr.length; i++) {
					setReleasedItem(splitStr[i], supportDetails, splitStr, i);
				}
			}
		}
	}

	private void setReleasedItem(String string, SupportDetails supportDetails,
			String[] splitStr, int i) {
		if (string.equalsIgnoreCase("FX")) {
			supportDetails.setxDisplaceabale(true);
		} else if (string.equalsIgnoreCase("FY")) {
			supportDetails.setyDisplaceabale(true);
		} else if (string.equalsIgnoreCase("MZ")) {
			supportDetails.setzRotatable(true);
		} else if (string.equalsIgnoreCase("FZ")) {
			supportDetails.setzDisplaceabale(true);
		} else if (string.equalsIgnoreCase("MX")) {
			supportDetails.setxRotatable(true);
		} else if (string.equalsIgnoreCase("MY")) {
			supportDetails.setyRotatable(true);
		}
		// later: for including stiffness.
		else if (string.equalsIgnoreCase("KFX")) {
			String xStiffString = splitStr[i + 1];
			double xStiffVal = Double.parseDouble(xStiffString);
			supportDetails.setxStiffness(xStiffVal);
			supportDetails.setxDisplaceabale(true);
		} else if (string.equalsIgnoreCase("KFY")) {
			String yStiffString = splitStr[i + 1];
			double yStiffVal = Double.parseDouble(yStiffString);
			supportDetails.setyStiffness(yStiffVal);
			supportDetails.setyDisplaceabale(true);
		}
	}

	private int getLineNumOfSupportDetailsEnd(String[] inputLines, int i) {
		for (int j = i; j < inputLines.length; j++) {
			if (inputLines[j] != null) {
				String[] splitStrs = inputLines[j].split(" ");

				try {
					int checkInt = Integer.parseInt(splitStrs[0]);
				} catch (NumberFormatException e) {
					return j;
				}
			}
		}
		return 0;
	}

	private int parseMaterialProperties(Structure structure,
			String[] inputLines, int i) throws PSAUnParsableSTDFileException {
		i++;

		int lineNumOfSupports = getLineNumOfSupportsCommandOrEndOfMaterial(
				inputLines, i);
		int numOfLinesToProcess = lineNumOfSupports - i;

		if (numOfLinesToProcess == 0)
			processLineOfMaterialData(structure, inputLines, i);

		for (int j = 0; j < numOfLinesToProcess; j++) {
			processLineOfMaterialData(structure, inputLines, i);
			i++;
		}
		return i;
	}

	private void processLineOfMaterialData(Structure structure,
			String[] inputLines, int i) throws PSAUnParsableSTDFileException {
		String[] splitString = inputLines[i].split("MEMB");

		boolean lineContainsMemFlag=true;
		if(splitString.length>1)
			lineContainsMemFlag = true;
		else
			lineContainsMemFlag = false;
		
		if(!lineContainsMemFlag) // Line is not processable 
			return;
		else {
		
		// splitString[1] -> Members
		ArrayList<Integer> memArray = getMembersAssociated(splitString[1]);
		// splitString[0] -> Material Property Name
		MaterialProperty materialProperty = null;
		materialProperty = getMaterialProperty(structure,
				splitString[0]);

		int l = i;
		//boolean nextLineHavingMembFlag = false;
		while(inputLines[l].endsWith("-")) {
			ArrayList<Integer> membersAssociated = getMembersAssociated(inputLines[l+1]);
			memArray.addAll(membersAssociated);
			/*String[] splitMem = inputLines[l+1].split("MEMB");
			if(splitMem.length>1)
				nextLineHavingMembFlag = true;*/
			l++;
		}

		setMaterialPropertyToFiniteElements(structure, memArray,
				materialProperty);
		}
	}

	private void setMaterialPropertyToFiniteElements(Structure structure,
			ArrayList<Integer> memArray, MaterialProperty materialProperty)
			throws PSAUnParsableSTDFileException {
		int actualMemArraySize = memArray.size();

		for (int j = 0; j < actualMemArraySize; j++) {
			FiniteElement finiteElementByNum = structure
					.getFiniteElementByNum(memArray.get(j));
			if (finiteElementByNum == null)
				throw new PSAUnParsableSTDFileException("FiniteElement "
						+ memArray.get(j)
						+ " not defined : can not add material property");

			if (finiteElementByNum instanceof OneDimFiniteElement) {
				OneDimFiniteElement oneDimFiniteElement = (OneDimFiniteElement) finiteElementByNum;
				oneDimFiniteElement.setMaterialProperty(materialProperty);

			} else
				throw new PSAUnParsableSTDFileException(
						"Finite Element "
								+ memArray.get(j)
								+ " is not OneDimensional to have cross sectional properties(Temperory)");

		}
	}

	private MaterialProperty getMaterialProperty(Structure structure,
			String materialLine) throws PSAUnParsableSTDFileException {
		String[] splitArray = materialLine.split(" ");
		String materialName = splitArray[1];
		MaterialProperty materialPropertyByName = structure
				.getMaterialPropertyByName(materialName);
		if (materialPropertyByName != null)
			return materialPropertyByName;
		else
			throw new PSAUnParsableSTDFileException(
					"Invalid material name, Name: " + materialName);
	}

	private ArrayList<Integer> getMembersAssociated(String members) {
		String[] splitStr = members.split(" ");
		// int membersArray[] = new int[20];
		ArrayList<Integer> membersArray = new ArrayList<Integer>();
		int temp = 0;
		int loc = 0;
		for (int j = 0; j < splitStr.length; j++) {
			if (splitStr[j].equals(""))
				continue;
			try {
				temp = Integer.parseInt(splitStr[j]);
				membersArray.add(loc, temp);
				loc++;

			} catch (NumberFormatException e) { // Exception caught only when
												// word is "TO"
				if (splitStr[j].equalsIgnoreCase("TO")) {
					int uptoMem = Integer.parseInt(splitStr[j + 1]);
					int indexOfFromMem = membersArray.size() - 1;
					int fromMem = membersArray.get(indexOfFromMem);
					int sizeOfTo = uptoMem - fromMem;
					int l = 0;
					for (int m = fromMem + 1; m < sizeOfTo + fromMem; m++) {
						membersArray.add(m);
						l++;
					}
					loc = loc + l;
				}
			}
		}

		return membersArray;
	}

	private int getLineNumOfSupportsCommandOrEndOfMaterial(String[] inputLines,
			int i) {
		int lineNumOfConst = 0;
		for (int j = i; j < inputLines.length - 1; j++) {
			if (inputLines[j + 1] != null)
				if (inputLines[j].equalsIgnoreCase("SUPPORTS"))
					return lineNumOfConst = j;
		}
		return lineNumOfConst;
	}

}

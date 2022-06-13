package com.ppsa.util;


public class MeshGenerationUtil {/*
	private static final double ONE_DIM_ELEMENT_SIZE = 0.1;
	*//**
	 * Contains id's of childElements so far created.
	 * This list will help in preventing creation of duplicate element's.
	 *//*
	private static int childFiniteElementNumbers = 30;
	*//**
	 * Contains id's of childNodes so far created.
	 * This list will help in preventing creation of duplicate node's.
	 *//*
	// later : change this number to make sure it is highest of all nodes defined by user in 'Structure'.
	private static int childNodeNumbers = 30;
	*//**
	 * 
	 * @param structure
	 * @param loadCase2 
	 *//*
	public static void generateMesh(Structure structure) {
		addAllParentStructureNodes(loadCase);
		Set<FiniteElement> finiteElements = structure.getFiniteElements();
			for (FiniteElement finiteElement : finiteElements) {
				meshThisElement(finiteElement,loadCase);
			}
		addNodeResultsEntityToAllNodes(loadCase);
		assignForcesToChildElements(loadCase);
	}

	private static void assignForcesToChildElements(LoadCase loadCase) {
		Map<MemberLoad, Set<OneDimFiniteElement>> memberLoads = loadCase.getMemberLoads();
		Set<MemberLoad> keySet = memberLoads.keySet();
		for (MemberLoad memberLoad : keySet) {
			Set<OneDimFiniteElement> finiteElements = memberLoads.get(memberLoad);
			List<OneDimFiniteElement> oneDElements_NonChild = new ArrayList<>();
			oneDElements_NonChild.addAll(finiteElements);
			for (OneDimFiniteElement oneDimFiniteElement : oneDElements_NonChild) {
				List<ChildElement> childElements = loadCase.getChildElementsForThisParentElement(oneDimFiniteElement);
				// later : check whether you have to applt to all elements 
				// or only for elements in particular range
				for (ChildElement childElement : childElements) {
					finiteElements.add((OneDimFiniteElementChild) childElement);
				}
			}
		}
		
	}

	private static void addNodeResultsEntityToAllNodes(LoadCase loadCase) {
		Set<CommonNode> childrenNodes = loadCase.getChildrenNodes();

		Map<CommonNode, NodeResults> nodeResults = new HashMap<CommonNode, NodeResults>();
		for (CommonNode node : childrenNodes) {
			NodeResults nodeResult = new NodeResults();
			nodeResults.put(node, nodeResult);
		}

		loadCase.setNodeResults(nodeResults);
	}

	private static void addAllParentStructureNodes(LoadCase loadCase) {
		Set<CommonNode> childrenNodes = loadCase.getChildrenNodes();
		childrenNodes.addAll(loadCase.getParentStructure().getNodes());
	}
	
	private static void meshThisElement(FiniteElement finiteElement, LoadCase loadCase) {
		if(finiteElement instanceof OneDimFiniteElement) {
			OneDimFiniteElement oneDimFiniteElement = (OneDimFiniteElement) finiteElement;
			
			if(oneDimFiniteElement.getFiniteElementType() == OneDimFiniteElementType.TRUSS2D) {
				noMeshRequiredForThisOneDimElement(oneDimFiniteElement,loadCase);
			}
			else if (oneDimFiniteElement.getFiniteElementType() == OneDimFiniteElementType.BEAM2D) {
//				boolean meshRequired = checkIfThisElementRequireMeshing(loadCase, oneDimFiniteElement);
				boolean meshRequired=false;
				if(meshRequired) {
					// For time being whole length
					// Later find significant points and mesh
					completeLengthMeshRequiredForThisOneDimElement(oneDimFiniteElement,loadCase);
				}
				else
					noMeshRequiredForThisOneDimElement(oneDimFiniteElement,loadCase);
			}
			
			double lengthOfElement = oneDimFiniteElement.getLengthOfElement();
			
			double meshElementLength = lengthOfElement/ONE_D_MESH_FINE;
			
			for (int i = 1; i < ONE_D_MESH_FINE; i++) {
				OpenPSA.insertNode(structure,oneDimFiniteElement,meshElementLength*i);
			}
		}
	}

	private static boolean checkIfThisElementRequireMeshing(LoadCase loadCase,
			OneDimFiniteElement oneDimFiniteElement) {
		Map<MemberLoad, Set<OneDimFiniteElement>> memberLoads = loadCase.getMemberLoads();
		Set<MemberLoad> keySet = memberLoads.keySet();
		boolean meshRequiredFlag = false;
		for (MemberLoad memberLoad : keySet) {
			Set<OneDimFiniteElement> setOfElements = memberLoads.get(memberLoad);
			meshRequiredFlag = setOfElements.contains(oneDimFiniteElement);
			if(meshRequiredFlag)
				return meshRequiredFlag;
		}
		return meshRequiredFlag;
	}

	private static void completeLengthMeshRequiredForThisOneDimElement(
			OneDimFiniteElement oneDimFiniteElement, LoadCase loadCase) {
		Node firstNode = oneDimFiniteElement.getFirstNode();
		double xCordinateOfFirstNode = firstNode.getxCordinate();
		double yCordinateOfFirstNode = firstNode.getyCordinate();
		
		Node secondNode = oneDimFiniteElement.getSecondNode();
		
		double angleOfInclination = oneDimFiniteElement.getAngleOfInclination();
		
		*//**
		 * LinkedList is used so that nodes can be retreived in order
		 * to associate with finite elements in correct order. 
		 *//*
		LinkedList<Node> childNodes = new LinkedList<>();
		
		childNodes.add(firstNode);
		
		// Perform node addition until whole length is reached
		double lengthOfElement = oneDimFiniteElement.getLengthOfElement();
		double meshLengthFinished = ONE_DIM_ELEMENT_SIZE;
		do {
			
			double xProjection = MathHelper.resolveForceInY(meshLengthFinished, angleOfInclination);
			double xCoOrdOfNewNode = xCordinateOfFirstNode + xProjection;
			
			double yProjection = MathHelper.resolveForceInX(meshLengthFinished, angleOfInclination);
			double yCoOrdOfNewNode = yCordinateOfFirstNode + yProjection;
			
			ChildNode childNode = new ChildNode(++childNodeNumbers, xCoOrdOfNewNode, yCoOrdOfNewNode, 0, 0);
			childNodes.add(childNode);
			
			meshLengthFinished = meshLengthFinished
					+ ONE_DIM_ELEMENT_SIZE;
		} while (meshLengthFinished<lengthOfElement);
		childNodes.add(secondNode);
		
		List<ChildElement> childElements = new ArrayList<>();
		
		int nodesSize = childNodes.size();
		int nodesAddedFinished = 0;
		do {
		
		OneDimFiniteElementChild oneDimFiniteElementChild = new OneDimFiniteElementChild(++childFiniteElementNumbers, oneDimFiniteElement);
		oneDimFiniteElementChild.setLengthOfElement(ONE_DIM_ELEMENT_SIZE);
		setBasicPropertiesOfChildElement(oneDimFiniteElement,
				oneDimFiniteElementChild, childNodes.get(nodesAddedFinished), childNodes.get(++nodesAddedFinished));
		setSectionForceEntitiesToChild(oneDimFiniteElementChild,loadCase);
		
		childElements.add(oneDimFiniteElementChild);
		} while (nodesAddedFinished < (nodesSize-1));
		
		loadCase.getChildFiniteElements().put(oneDimFiniteElement, childElements);
		
		childNodes.removeFirst();
		childNodes.removeLast();
		
		addTheseChildNodesToNodesListOfLoadCase(loadCase,childNodes);
	}

	private static void addTheseChildNodesToNodesListOfLoadCase(
			LoadCase loadCase, LinkedList<Node> childNodes) {
		Set<CommonNode> childrenNodes = loadCase.getChildrenNodes();
		childrenNodes.addAll(childNodes);
	}

	*//**
	 * This element does not require any meshing and it's parent element
	 * properties are same to it's child element.
	 * And only one child element present to this element.
	 * @param oneDimFiniteElement
	 * @param loadCase
	 *//*
	private static void noMeshRequiredForThisOneDimElement(
			OneDimFiniteElement oneDimFiniteElement, LoadCase loadCase) {
		List<ChildElement> childElements = new ArrayList<>();
		// include this when multiple required do while
		// (allRequiredMeshElementsAdded) {
		OneDimFiniteElementChild oneDimFiniteElementChild = new OneDimFiniteElementChild(
				oneDimFiniteElement.getElementNumber(), oneDimFiniteElement);

		Node firstNode = oneDimFiniteElement.getFirstNode();
		Node secondNode = oneDimFiniteElement.getSecondNode();

		oneDimFiniteElementChild.setLengthOfElement(oneDimFiniteElement.getLengthOfElement());
		setBasicPropertiesOfChildElement(oneDimFiniteElement,
				oneDimFiniteElementChild, firstNode, secondNode);
		setSectionForceEntitiesToChild(oneDimFiniteElementChild, loadCase);

		childElements.add(oneDimFiniteElementChild);
		loadCase.getChildFiniteElements().put(oneDimFiniteElement,
				childElements);

	}

	private static void setSectionForceEntitiesToChild(
			OneDimFiniteElementChild oneDimFiniteElementChild, LoadCase loadCase) {
		
		 Map<OneDimFiniteElementChild, OneDAxialForceEntity> axialForceEntities = loadCase.getAxialForceEntities();
		
		//for (FiniteElement finiteElement : finiteElements) {
			// later : check if finiteElement.isOneDElemnt or some other
			// logic.
			//OneDimFiniteElement oneDFiniteElement = (OneDimFiniteElement) finiteElement;
			
			OneDAxialForceEntity oneDAxialForceEntity = new OneDAxialForceEntity();
			axialForceEntities.put(oneDimFiniteElementChild, oneDAxialForceEntity);
			
		loadCase.setAxialForceEntities(axialForceEntities);
		
		if(oneDimFiniteElementChild.getFiniteElementType()==OneDimFiniteElementType.BEAM2D) {
			Map<OneDimFiniteElementChild, OneDBeamEndMomentsEntity> beamEndMomentsEntities = loadCase.getBeamEndMomentEntities();
			Map<OneDimFiniteElementChild, OneDBeamEndShearForceEntity> beamEndShearForceEntities = loadCase.getBeamEndShearForceEntities();
			OneDBeamEndMomentsEntity beamEndMomentsEntity = new OneDBeamEndMomentsEntity();
			beamEndMomentsEntities.put(oneDimFiniteElementChild, beamEndMomentsEntity);
			OneDBeamEndShearForceEntity beamEndShearForceEntity = new OneDBeamEndShearForceEntity();
			beamEndShearForceEntities.put(oneDimFiniteElementChild, beamEndShearForceEntity);
			loadCase.setBeamEndMomentEntities(beamEndMomentsEntities);
			loadCase.setBeamEndShearForceEntities(beamEndShearForceEntities);

		}
	}

	private static void setBasicPropertiesOfChildElement(
			OneDimFiniteElement oneDimFiniteElement,
			OneDimFiniteElementChild oneDimFiniteElementChild, Node firstNode,
			Node secondNode) {
		//oneDimFiniteElementChild.setLengthOfElement(oneDimFiniteElement.getLengthOfElement());
		oneDimFiniteElementChild.setFirstNode(firstNode);
		oneDimFiniteElementChild.setSecondNode(secondNode);
		//oneDimFiniteElementChild.setLengthOfElement(MeshingParameters.ONE_DIM_ELEMENT_SIZE);
		
		Set<CommonNode> nodesInFiniteElement = oneDimFiniteElementChild.getNodes();
		nodesInFiniteElement.add(firstNode);
		nodesInFiniteElement.add(secondNode);
	}
*/}

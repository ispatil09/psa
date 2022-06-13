package com.psa.entity.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.psa.entity.CommonNode;
import com.psa.entity.FiniteElement;
import com.psa.entity.GlobalStiffnessMatrixEntity;
import com.psa.entity.LoadCase;
import com.psa.entity.MaterialProperty;
import com.psa.entity.StructureUnits;
import com.psa.entity.VectorMatrix;
import com.psa.entity.enums.FileFormatParsed;
import com.psa.entity.enums.OneDimFiniteElementType;
import com.psa.entity.enums.StructureType;
import com.psa.exception.PSAStructureInstantiationException;
import com.psa.util.ResourceBundleUtil;
import com.psa.util.STDModelCorrectionHelper;

public class Structure implements Serializable {
	private String problemName;
	private boolean structurePostProcessed=false;
	private Set<Node> commonNodes = new HashSet<Node>();
	private Set<FiniteElement> finiteElements = new HashSet<FiniteElement>();
	private StructureType structureType;
	private StructureUnits structureUnits = new StructureUnits();
	private Map<Integer, LoadCase> loadCases = new HashMap<Integer, LoadCase>();
	private Map<String, MaterialProperty> materials = new HashMap<String,MaterialProperty>();
	private PSASpecificCommands psaSpecificCommands = null;
	private GlobalStiffnessMatrixEntity globalStiffnessMatrix = null;
	private GlobalStiffnessMatrixEntity reducedGlobalStiffnessMatrix = null;
	private VectorMatrix displacementVectorMatrix = null;
	private VectorMatrix reducedDisplacementVectorMatrix = null;
	/* moved in 'LoadCase' private Map<LoadCase,GlobalStiffnessMatrixEntity> globalStiffnessMatrix = new HashMap<>();
	private Map<LoadCase,GlobalStiffnessMatrixEntity> reducedGlobalStiffnessMatrix = new HashMap<>();
	private Map<LoadCase,VectorMatrix> displacementVectorMatrix = new HashMap<>();
	private Map<LoadCase,VectorMatrix> reducedDisplacementVectorMatrix = new HashMap<>();
	private Map<LoadCase,List<Integer>> globalCoOrdinatesOfFreedom = new HashMap<>();*/
	private FileFormatParsed formatParsed = null;
	
	private STDModelCorrectionHelper stdModelCorrectionHelper = new STDModelCorrectionHelper();

	public Structure(String fileName) {
		this.setProblemName(fileName);
	}

	public String getProblemName() {
		return problemName;
	}

	public void setProblemName(String problemName) {
		this.problemName = problemName;
	}

	public Set<Node> getNodes() {
		return commonNodes;
	}

	public void setNodes(Set<Node> nodes) {
		this.commonNodes = nodes;
	}

	public Set<FiniteElement> getFiniteElements() {
		return finiteElements;
	}

	public void setFiniteElements(Set<FiniteElement> finiteElements) {
		this.finiteElements = finiteElements;
	}

	public StructureType getStructureType() {
		return structureType;
	}

	public void setStructureType(StructureType structureType) {
		this.structureType = structureType;
	}

	public StructureUnits getStructureUnits() {
		return structureUnits;
	}

	public void setStructureUnits(StructureUnits structureUnits) {
		this.structureUnits = structureUnits;
	}

	public Map<Integer, LoadCase> getLoadCases() {
		return loadCases;
	}

	public void setLoadCases(Map<Integer, LoadCase> loadCases) {
		this.loadCases = loadCases;
	}
	
	public void setMaterials(Map<String, MaterialProperty> materials) {
		this.materials = materials;
	}
	
	public Map<String, MaterialProperty> getMaterials() {
		return materials;
	}
	
	public GlobalStiffnessMatrixEntity getGlobalStiffnessMatrix() {
		return globalStiffnessMatrix;
	}

	public void setGlobalStiffnessMatrix(GlobalStiffnessMatrixEntity globalStiffnessMatrix) {
		this.globalStiffnessMatrix = globalStiffnessMatrix;
	}


	public GlobalStiffnessMatrixEntity getReducedGlobalStiffnessMatrix() {
		return reducedGlobalStiffnessMatrix;
	}

	public void setReducedGlobalStiffnessMatrix(
			GlobalStiffnessMatrixEntity reducedGlobalStiffnessMatrix) {
		this.reducedGlobalStiffnessMatrix = reducedGlobalStiffnessMatrix;
	}


	public boolean isStructurePostProcessed() {
		return structurePostProcessed;
	}

	public void setStructurePostProcessed(boolean structureIsPostProcessed) {
		this.structurePostProcessed = structureIsPostProcessed;
	}

	public void addMaterail(MaterialProperty materialProperty) {
		materials.put(materialProperty.getPropertyName(), materialProperty);
	}

	public boolean addNodeEntry(Node commonNode)
			throws PSAStructureInstantiationException {
		boolean isRemoved = commonNodes.add(commonNode);
		if (isRemoved)
			return isRemoved;
		else {
			String generalMsg = ResourceBundleUtil.getString(
					"stru_create_error", "DUP_NODE_ENTRY");
			throw new PSAStructureInstantiationException(generalMsg + " ["
					+ commonNode.getNodeNumber() + "]");
		}
	}

	public boolean removeNodeEntry(CommonNode node) {
		boolean isRemoved = false;
		if (commonNodes.size() != 0) {
			isRemoved = getNodes().remove(node);
		}
		return isRemoved;
	}

	public boolean addFiniteElementEntry(FiniteElement finiteElement)
			throws PSAStructureInstantiationException {
		boolean isAdded = finiteElements.add(finiteElement);
		if (isAdded)
			return isAdded;
		else {
			String generalMsg = ResourceBundleUtil.getString(
					"stru_create_error", "DUP_ELEMENT_ENTRY");
			throw new PSAStructureInstantiationException(generalMsg + " ["
					+ finiteElement.getElementNumber() + "]");
		}
	}

	public boolean removeFiniteElementEntry(FiniteElement finiteElement) {
		boolean isRemoved = false;
		if (finiteElements.size() != 0) {
			isRemoved = finiteElements.remove(finiteElement);
		}
		return isRemoved;
	}

	public Node getNode(int nodeNum) throws PSAStructureInstantiationException {
		Node nodeReference = new Node(nodeNum);
		boolean contains = commonNodes.contains(nodeReference);
		if (!contains) {
			String generalMsg = ResourceBundleUtil.getString(
					"stru_create_error", "NODE_NOT_EXIST");
			throw new PSAStructureInstantiationException(generalMsg + ": Node "
					+ nodeNum);
		}

		for (Node commonNode : commonNodes) {
			if (commonNode.equals(nodeReference))
				return commonNode;
		}
		return null; // Assuming that this code is never reached
	}

	public void addLoadCase(LoadCase loadCase) {
		loadCases.put(loadCase.getLoadCaseNum(), loadCase);
	}

	public LoadCase getLoadCase(int loadCaseNum) {
		return loadCases.get(loadCaseNum);
	}

	public FiniteElement getFiniteElementByNum(int idNum) {
		for(FiniteElement finiteElement : this.finiteElements) {
			if(finiteElement.getElementNumber()==idNum)
				return finiteElement;
		}
		return null;
	}
	
	public MaterialProperty getMaterialPropertyByName(String materialName) {
		MaterialProperty materialProperty = materials.get(materialName);
		return materialProperty;
	}
	
	public void setAllFiniteElementsTruss() {
		for (FiniteElement element : this.finiteElements) {
			OneDimFiniteElement oneDimFiniteElement = (OneDimFiniteElement) element;
			oneDimFiniteElement.setFiniteElementType(OneDimFiniteElementType.TRUSS2D);
		}
	}

	public void setAllFiniteElementsBeam() {
		for (FiniteElement element : this.finiteElements) {
			OneDimFiniteElement oneDimFiniteElement = (OneDimFiniteElement) element;
			oneDimFiniteElement.setFiniteElementType(OneDimFiniteElementType.BEAM2D);
		}
	}
	
	public FileFormatParsed getFormatParsed() {
		return formatParsed;
	}

	public void setFormatParsed(FileFormatParsed formatParsed) {
		this.formatParsed = formatParsed;
	}

	public STDModelCorrectionHelper getStdModelCorrectionHelper() {
		return stdModelCorrectionHelper;
	}

	public void setStdModelCorrectionHelper(STDModelCorrectionHelper stdModelCorrectionHelper) {
		this.stdModelCorrectionHelper = stdModelCorrectionHelper;
	}

	public PSASpecificCommands getPsaSpecificCommands() {
		return psaSpecificCommands;
	}

	public void setPsaSpecificCommands(PSASpecificCommands psaSpecificCommands) {
		this.psaSpecificCommands = psaSpecificCommands;
	}

	public VectorMatrix getDisplacementVectorMatrix() {
		return displacementVectorMatrix;
	}

	public void setDisplacementVectorMatrix(VectorMatrix displacementVectorMatrix) {
		this.displacementVectorMatrix = displacementVectorMatrix;
	}

	public VectorMatrix getReducedDisplacementVectorMatrix() {
		return reducedDisplacementVectorMatrix;
	}

	public void setReducedDisplacementVectorMatrix(
			VectorMatrix reducedDisplacementVectorMatrix) {
		this.reducedDisplacementVectorMatrix = reducedDisplacementVectorMatrix;
	}

	@Override
	public String toString() {
		return "NODES=" + commonNodes.size() + commonNodes.toString() + "\nFE="
				+ finiteElements.size() + finiteElements.toString();
	}

	public void setAllFiniteElementsTruss3D() {
		for (FiniteElement element : this.finiteElements) {
			OneDimFiniteElement oneDimFiniteElement = (OneDimFiniteElement) element;
			oneDimFiniteElement.setFiniteElementType(OneDimFiniteElementType.TRUSS3D);
		}
	}

	public void setAllFiniteElementsBeam3D() {
		for (FiniteElement element : this.finiteElements) {
			OneDimFiniteElement oneDimFiniteElement = (OneDimFiniteElement) element;
			oneDimFiniteElement.setFiniteElementType(OneDimFiniteElementType.BEAM3D);
		}
	}

}

package com.psa.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.psa.entity.enums.ConMemberLoadDirection;
import com.psa.entity.enums.DOF_TYPE;
import com.psa.entity.enums.MemberLoadType;
import com.psa.entity.enums.UniMemberLoadDirection;
import com.psa.entity.impl.Node;
import com.psa.entity.impl.NodeResults;
import com.psa.entity.impl.OneDimFiniteElement;
import com.psa.entity.impl.Structure;

	public class LoadCase implements Serializable {
		private int loadCaseNum;
		private Structure parentStructure = null;
		private String loadCaseTitle;
		private String loadType;
		private Map<NodalLoad,Set<CommonNode>> nodalLoads = new HashMap<NodalLoad, Set<CommonNode>>();
		private Map<MemberLoad,Set<OneDimFiniteElement>> memberLoads = new HashMap<MemberLoad, Set<OneDimFiniteElement>>();
		private VectorMatrix globalForceVector = null;
		private VectorMatrix reducedGlobalForceVector = null;
		private VectorMatrix reducedNodalDisplacementVector = null;
		private VectorMatrix fullNodalDisplacementVector = null;
		/**
		 * For support nodes only. Hence does not contain all CoOrdinates.
		 */
		private VectorMatrix supportReactionsVector = null;
		private Map<CommonNode, NodeResults> nodeResults = new HashMap<CommonNode,NodeResults>();
		//private VectorMatrix oneDFiniteElementAxialForceVector = null;
		private Map<OneDimFiniteElement, OneDAxialForceEntity> axialForceEntities = new HashMap<>();
		private Map<OneDimFiniteElement, OneDBeamEndMomentsEntity> beamEndMomentEntities = new HashMap<>();
		private Map<OneDimFiniteElement, OneDBeamEndShearForceEntity> beamEndShearForceEntities = new HashMap<>();
		private Map<OneDimFiniteElement, OneDBeamEndTorsionEntity> beamEndTorsionEntities = new HashMap<>();
		private Map<OneDimFiniteElement, OneDBeamLocalRotations> beamEndLocalRotations = new HashMap<>();

		/*private GlobalStiffnessMatrixEntity globalStiffnessMatrix = null;
		private GlobalStiffnessMatrixEntity reducedGlobalStiffnessMatrix = null;*/
		
		//private Map<FiniteElement, List<ChildElement>> childFiniteElements = new HashMap<>();
		/**
		 * Includes even nodes of parent structure.
		 */
		//private Set<CommonNode> childrenNodes = new HashSet<CommonNode>();
		
		/*private GlobalStiffnessMatrixEntity globalStiffnessMatrix = null;
		private GlobalStiffnessMatrixEntity reducedGlobalStiffnessMatrix = null;*/
		private VectorMatrix displacementVectorMatrix = null;
		private VectorMatrix reducedDisplacementVectorMatrix = null;
		private List<Integer> globalCoOrdinatesOfFreedom = new ArrayList<>();
		private List<SupportSettlementEntity> supportSettlements = new ArrayList<SupportSettlementEntity>();

		public LoadCase(int loadCaseNumber,Structure structure) {
			this.loadCaseNum=loadCaseNumber;
			this.setParentStructure(structure);
//			loadCases.put(loadCaseNumber, this);
		}
		
		public LoadCase(int loadCaseNum, String loadCaseTitle,String loadType,Structure structure) {
			this.loadCaseNum=loadCaseNum;
			this.loadCaseTitle=loadCaseTitle;
			this.loadType=loadType;
			this.setParentStructure(structure);
//			loadCases.put(loadCaseNum, this);
		}
		
		public void setLoadCaseNum(int loadCaseNum) {
			this.loadCaseNum = loadCaseNum;
		}
		
		public int getLoadCaseNum() {
			return loadCaseNum;
		}
		
		public String getLoadType() {
			return loadType;
		}

		public void setLoadType(String loadType) {
			this.loadType = loadType;
		}

		public void setLoadCaseTitle(String loadCaseName) {
			this.loadCaseTitle = loadCaseName;
		}
		
		public String getLoadCaseTitle() {
			return loadCaseTitle;
		}
		
		public VectorMatrix getGlobalForceVector() {
			return globalForceVector;
		}

		public void setGlobalForceVector(VectorMatrix globalForceVector) {
			this.globalForceVector = globalForceVector;
		}

		public VectorMatrix getReducedGlobalForceVector() {
			return reducedGlobalForceVector;
		}

		public void setReducedGlobalForceVector(VectorMatrix reducedGlobalForceVector) {
			this.reducedGlobalForceVector = reducedGlobalForceVector;
		}

		public VectorMatrix getReducedNodalDisplacementVector() {
			return reducedNodalDisplacementVector;
		}

		public void setReducedNodalDisplacementVector(VectorMatrix nodalDisplacementVector) {
			this.reducedNodalDisplacementVector = nodalDisplacementVector;
		}

		public Map<NodalLoad,Set<CommonNode>> getNodalLoads() {
			return nodalLoads;
		}
		
		public void addNodalLoad(NodalLoad nodalLoad,Set<CommonNode> commonNodes) {
			this.nodalLoads.put(nodalLoad,commonNodes);
		}
		
		public Map<MemberLoad,Set<OneDimFiniteElement>> getMemberLoads() {
			return memberLoads;
		}

		public void setMemberLoads(Map<MemberLoad,Set<OneDimFiniteElement>> memberLoads) {
			this.memberLoads = memberLoads;
		}

		public VectorMatrix getFullNodalDisplacementVector() {
			return fullNodalDisplacementVector;
		}

		public void setFullNodalDisplacementVector(
				VectorMatrix fullNodalDisplacementVector) {
			this.fullNodalDisplacementVector = fullNodalDisplacementVector;
		}

		public Map<CommonNode, NodeResults> getNodeResults() {
			return nodeResults;
		}

		public void setNodeResults(Map<CommonNode, NodeResults> nodeResults) {
			this.nodeResults = nodeResults;
		}

		public VectorMatrix getSupportReactionsVector() {
			return supportReactionsVector;
		}

		public void setSupportReactionsVector(VectorMatrix supportReactionsVector) {
			this.supportReactionsVector = supportReactionsVector;
		}

		public Map<OneDimFiniteElement, OneDAxialForceEntity> getAxialForceEntities() {
			return axialForceEntities;
		}

		public void setAxialForceEntities(Map<OneDimFiniteElement, OneDAxialForceEntity> axialForceEntities) {
			this.axialForceEntities = axialForceEntities;
		}

		public class NodalLoad implements Serializable {
			private double fX=0;
			private double fY=0;
			private double fZ=0;
			
			private double mX=0;
			private double mY=0;
			private double mZ=0;
			
			public NodalLoad() {
				fX=0;
				fY=0;
				fZ=0;
				
				mX=0;
				mY=0;
				mZ=0;
			}
			
			public double getfX() {
				return fX;
			}
			public void setfX(double fX) {
				this.fX = fX;
			}
			public double getfY() {
				return fY;
			}
			public void setfY(double fY) {
				this.fY = fY;
			}
			public double getfZ() {
				return fZ;
			}
			public void setfZ(double fZ) {
				this.fZ = fZ;
			}
			public double getmX() {
				return mX;
			}
			public void setmX(double mX) {
				this.mX = mX;
			}
			public double getmY() {
				return mY;
			}
			public void setmY(double mY) {
				this.mY = mY;
			}
			public double getmZ() {
				return mZ;
			}
			public void setmZ(double mZ) {
				this.mZ = mZ;
			}
			
			public Set<CommonNode> getCommonNodesForThisNodalLoad(NodalLoad nodalLoad) {
				Set<CommonNode> setOfCommonNodes = nodalLoads.get(nodalLoad);
				return setOfCommonNodes;
			}
			
			public NodeResults getNodeResultsForNode(Node node) {
				
				return nodeResults.get(node);
			}
			
			@Override
			public String toString() {
				
				return fX+","+fY+","+mZ;
			}
			
			@Override
			public int hashCode() {
				return loadCaseNum;
			}
		}
		
		public class MemberLoad implements Serializable {
			private MemberLoadType memberLoadType;
			private UniformForce uniformForce;
			private ConcentratedForce concentratedForce;
			
			public MemberLoad(MemberLoadType loadType) {
				this.memberLoadType=loadType;
				if(loadType==MemberLoadType.UNI) {
					uniformForce = new UniformForce();
				} else if(loadType==MemberLoadType.CON) {
					setConcentratedForce(new ConcentratedForce());
				} 
			}
			
			public MemberLoadType getMemberLoadType() {
				return memberLoadType;
			}
			public void setMemberLoadType(MemberLoadType memberLoadType) {
				this.memberLoadType = memberLoadType;
			}
			
			public UniformForce getUniformForce() {
				return uniformForce;
			}
			public void setUniformForce(UniformForce uniformForce) {
				this.uniformForce = uniformForce;
			}
			
			public ConcentratedForce getConcentratedForce() {
				return concentratedForce;
			}
			
			public void setConcentratedForce(ConcentratedForce concentratedForce) {
				this.concentratedForce = concentratedForce;
			}


			public class UniformForce implements Serializable {
				private double forceVal;
				private UniMemberLoadDirection loadDirection;
				private double rangeBegin=0;
				private double rangeEnd=0;
				public void setForceAndDirection(double forceValue, UniMemberLoadDirection direction) {
					this.forceVal = forceValue;
					this.loadDirection = direction;
				}
				public double getForceVal() {
					return forceVal;
				}
				public void setForceVal(double forceVal) {
					this.forceVal = forceVal;
				}
				public UniMemberLoadDirection getLoadDirection() {
					return loadDirection;
				}
				public void setLoadDirection(UniMemberLoadDirection loadDirection) {
					this.loadDirection = loadDirection;
				}
				public double getRangeBegin() {
					return rangeBegin;
				}
				public void setRangeBegin(double rangeBegin) {
					this.rangeBegin = rangeBegin;
				}
				public double getRangeEnd() {
					return rangeEnd;
				}
				public void setRangeEnd(double rangeEnd) {
					this.rangeEnd = rangeEnd;
				}
				public boolean isPatchLoad() {
					if (rangeBegin==0 && rangeEnd==0)
						return false;
					else 
						return true;
				}
			}
			
			public class ConcentratedForce implements Serializable {
				private double forceVal;
				private ConMemberLoadDirection loadDirection;
				private double d1;
				public void setForceAndDirection(double force,ConMemberLoadDirection direction) {
					this.forceVal = force;
					this.loadDirection = direction;
				}
				public double getForceVal() {
					return forceVal;
				}
				public void setForceVal(double forceVal) {
					this.forceVal = forceVal;
				}
				public ConMemberLoadDirection getLoadDirection() {
					return loadDirection;
				}
				public void setLoadDirection(ConMemberLoadDirection loadDirection) {
					this.loadDirection = loadDirection;
				}
				public double getD1() {
					return d1;
				}
				public void setD1(double d1) {
					this.d1 = d1;
				}
			}
			
			@Override
			public String toString() {
				StringBuilder stringBuilder = new StringBuilder(this.memberLoadType.toString());
				return stringBuilder.toString();
			}

		}

		public void addMemberLoad(MemberLoad memberLoad,
				Set<OneDimFiniteElement> membersArray) {
			
			/*if(memberLoad.getMemberLoadType()==MemberLoadType.CON) {
				ConcentratedForce conForce = memberLoad.getConcentratedForce();
				for (OneDimFiniteElement oneDimFiniteElement : membersArray) {
					boolean entryReverseIndicatorEntry = structure.getEntryReverseIndicatorEntry(oneDimFiniteElement);
					if(entryReverseIndicatorEntry) {
						double enterdD1 = conForce.getD1();
						double alteredD1 = oneDimFiniteElement.getLengthOfElement()-enterdD1;
						conForce.setD1(alteredD1);
					}
				}
			}*/
			
			this.memberLoads.put(memberLoad,membersArray);
		}

		public Map<OneDimFiniteElement, OneDBeamEndMomentsEntity> getBeamEndMomentEntities() {
			return beamEndMomentEntities;
		}

		public void setBeamEndMomentEntities(Map<OneDimFiniteElement, OneDBeamEndMomentsEntity> beamEndMomentEntities) {
			this.beamEndMomentEntities = beamEndMomentEntities;
		}

		public Map<OneDimFiniteElement, OneDBeamEndShearForceEntity> getBeamEndShearForceEntities() {
			return beamEndShearForceEntities;
		}

		public void setBeamEndShearForceEntities(
				Map<OneDimFiniteElement, OneDBeamEndShearForceEntity> beamEndShearForceEntities) {
			this.beamEndShearForceEntities = beamEndShearForceEntities;
		}

		public VectorMatrix getDisplacementVectorMatrix() {
			return displacementVectorMatrix;
		}

		public void setDisplacementVectorMatrix(
				VectorMatrix displacementVectorMatrix) {
			this.displacementVectorMatrix = displacementVectorMatrix;
		}

		public VectorMatrix getReducedDisplacementVectorMatrix() {
			return reducedDisplacementVectorMatrix;
		}

		public void setReducedDisplacementVectorMatrix(
				VectorMatrix reducedDisplacementVectorMatrix) {
			this.reducedDisplacementVectorMatrix = reducedDisplacementVectorMatrix;
		}

		public List<Integer> getGlobalCoOrdinatesOfFreedom() {
			return globalCoOrdinatesOfFreedom;
		}

		public void setGlobalCoOrdinatesOfFreedom(
				List<Integer> globalCoOrdinatesOfFreedom) {
			this.globalCoOrdinatesOfFreedom = globalCoOrdinatesOfFreedom;
		}

		public Structure getParentStructure() {
			return parentStructure;
		}

		public void setParentStructure(Structure parentStructure) {
			this.parentStructure = parentStructure;
		}
		
		/*public Set<ChildElement> getAllChildrenElementsIrrespectiveOfThereParent() {
			Set<ChildElement> childElementsToBeReturned = new HashSet<>();
			Set<FiniteElement> keySet = this.childFiniteElements.keySet();
			for (FiniteElement finiteElement : keySet) {
				List<ChildElement> setOfChildElements = childFiniteElements.get(finiteElement);
				childElementsToBeReturned.addAll(setOfChildElements);
			}
			return childElementsToBeReturned;
		}*/
		
		/*public Map<FiniteElement, List<ChildElement>> getChildFiniteElements() {
		return childFiniteElements;
	}

	public void setChildFiniteElements(Map<FiniteElement, List<ChildElement>> childFiniteElements) {
		this.childFiniteElements = childFiniteElements;
	}*/
		public Set<MemberLoad> getAllMemberLoadsForThisFE(OneDimFiniteElement oneDimFiniteElement) {
			Set<MemberLoad> memLoadsApplicable = new HashSet<MemberLoad>();
			Set<MemberLoad> keySet = this.memberLoads.keySet();
			for (MemberLoad memberLoad : keySet) {
				Set<OneDimFiniteElement> setOfElements = memberLoads.get(memberLoad);
				boolean memberHasThisLoadFlag = setOfElements.contains(oneDimFiniteElement);
				if(memberHasThisLoadFlag)
					memLoadsApplicable.add(memberLoad);
			}
			return memLoadsApplicable;
		}

		public Map<OneDimFiniteElement, OneDBeamEndTorsionEntity> getBeamEndTorsionEntities() {
			return beamEndTorsionEntities;
		}

		public void setBeamEndTorsionEntities(Map<OneDimFiniteElement, OneDBeamEndTorsionEntity> beamEndTosionEntities) {
			this.beamEndTorsionEntities = beamEndTosionEntities;
		}

		public Map<OneDimFiniteElement, OneDBeamLocalRotations> getBeamEndLocalRotations() {
			return beamEndLocalRotations;
		}

		public void setBeamEndLocalRotations(Map<OneDimFiniteElement, OneDBeamLocalRotations> beamEndLocalRotations) {
			this.beamEndLocalRotations = beamEndLocalRotations;
		}
		
		public List<SupportSettlementEntity> getSupportSettlements() {
			return supportSettlements;
		}

		public void addSupportSettlement(SupportSettlementEntity supportSettlement) {
			this.supportSettlements.add(supportSettlement);
		}

		public class SupportSettlementEntity {
			private double settlement = 0;
			private Set<Node> nodes = null;
			private DOF_TYPE DOF = null;
			
			public double getSettlement() {
				return settlement;
			}
			public void setSettlement(double settlement) {
				this.settlement = settlement;
			}
			public Set<Node> getNodes() {
				return nodes;
			}
			public void setNodes(Set<Node> nodes) {
				this.nodes = nodes;
			}
			public DOF_TYPE getCordinate() {
				return DOF;
			}
			public void setCordinate(String cordinate) {
				if(cordinate.equals("FX"))
					this.DOF = DOF_TYPE.FX;
				else if(cordinate.equals("FY"))
					this.DOF = DOF_TYPE.FY;
				else if(cordinate.equals("FZ"))
					this.DOF = DOF_TYPE.FZ;
				else if(cordinate.equals("MX"))
					this.DOF = DOF_TYPE.MX;
				else if(cordinate.equals("MY"))
					this.DOF = DOF_TYPE.MY;
				else if(cordinate.equals("MZ"))
					this.DOF = DOF_TYPE.MZ;
			} 
		}
	}
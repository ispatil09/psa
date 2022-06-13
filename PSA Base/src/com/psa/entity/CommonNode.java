package com.psa.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.psa.entity.impl.OneDimFiniteElement;
import com.psa.entity.impl.SupportDetails;

public class CommonNode implements Serializable {
	private int nodeNumber = 0;
	private double xCordinate = 0;
	private double yCordinate = 0;
	private double zCordinate = 0;
	private Set<FiniteElement> finiteElements = new HashSet<FiniteElement>();
	private SupportDetails supportDetails;

	private int xCordinateNum = 0;
	private int yCordinateNum = 0;
	private int zCordinateNum = 0;

	private int mzCordinateNum = 0;
	private int myCordinateNum = 0;
	private int mxCordinateNum = 0;
	
	public int getNodeNumber() {
		return nodeNumber;
	}

	public void setNodeNumber(int nodeNum) {
		nodeNumber = nodeNum;
	}

	public double getxCordinate() {
		return xCordinate;
	}

	public void setxCordinate(double xCordinate) {
		this.xCordinate = xCordinate;
	}

	public double getyCordinate() {
		return yCordinate;
	}

	public void setyCordinate(double yCordinate) {
		this.yCordinate = yCordinate;
	}

	public double getzCordinate() {
		return zCordinate;
	}

	public void setzCordinate(double zCordinate) {
		this.zCordinate = zCordinate;
	}

	public Set<FiniteElement> getFiniteElements() {
		return finiteElements;
	}

	public void setFiniteElements(Set<FiniteElement> finiteElements) {
		this.finiteElements = finiteElements;
	}

	public int getxCordinateNum() {
		return xCordinateNum;
	}

	public void setxCordinateNum(int xCordinateNum) {
		this.xCordinateNum = xCordinateNum;
	}

	public int getyCordinateNum() {
		return yCordinateNum;
	}

	public void setyCordinateNum(int yCordinateNum) {
		this.yCordinateNum = yCordinateNum;
	}

	public int getzCordinateNum() {
		return zCordinateNum;
	}

	public void setzCordinateNum(int zCordinateNum) {
		this.zCordinateNum = zCordinateNum;
	}

	public int getMzCordinateNum() {
		return mzCordinateNum;
	}

	public void setMzCordinateNum(int mzCordinateNum) {
		this.mzCordinateNum = mzCordinateNum;
	}

	public int getMyCordinateNum() {
		return myCordinateNum;
	}

	public void setMyCordinateNum(int myCordinateNum) {
		this.myCordinateNum = myCordinateNum;
	}

	public int getMxCordinateNum() {
		return mxCordinateNum;
	}

	public void setMxCordinateNum(int mxCordinateNum) {
		this.mxCordinateNum = mxCordinateNum;
	}

	public boolean addFiniteElement(FiniteElement finiteElement) {
		boolean isAdded = finiteElements.add(finiteElement);
		return isAdded;
	}

	public boolean removeFiniteElement(OneDimFiniteElement oneDimFiniteElement) {
		boolean isRemoved = finiteElements.remove(oneDimFiniteElement);
		return isRemoved;
	}

	public SupportDetails getSupportDetails() {
		return supportDetails;
	}

	public void setSupportDetails(SupportDetails supportDetails) {
		this.supportDetails = supportDetails;
	}

	@Override
	public String toString() {
		return nodeNumber + "";
	}

	@Override
	public boolean equals(Object obj) {
		CommonNode cn = (CommonNode) obj;
		if (cn.getNodeNumber() == this.nodeNumber)
			return true;
		else
			return false;
	}

	@Override
	public int hashCode() {
		return this.nodeNumber;
	}
}

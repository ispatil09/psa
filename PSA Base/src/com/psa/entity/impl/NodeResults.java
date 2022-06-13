package com.psa.entity.impl;

import java.io.Serializable;

public class NodeResults implements Serializable {
	private Double xDisplacement = 0d;
	private Double yDisplacement = 0d;
	private Double zDisplacement = 0d;
	
	private Double xReaction=0d;
	private Double yReaction=0d;
	private Double zReaction=0d;
	
	private Double zRotation=0d;
	private Double yRotation=0d;
	private Double xRotation=0d;
	
	private Double zMoment=0d;
	private Double yMoment=0d;
	private Double xMoment=0d;
	
	public Double getxReaction() {
		return xReaction;
	}
	public void setxReaction(Double xReaction) {
		this.xReaction = xReaction;
	}
	public Double getyReaction() {
		return yReaction;
	}
	public void setyReaction(Double yReaction) {
		this.yReaction = yReaction;
	}
	public Double getzReaction() {
		return zReaction;
	}
	public void setzReaction(Double zReaction) {
		this.zReaction = zReaction;
	}
	public Double getxDisplacement() {
		return xDisplacement;
	}
	public void setxDisplacement(Double xDisplacement) {
		this.xDisplacement = xDisplacement;
	}
	public Double getyDisplacement() {
		return yDisplacement;
	}
	public void setyDisplacement(Double yDisplacement) {
		this.yDisplacement = yDisplacement;
	}
	public Double getzDisplacement() {
		return zDisplacement;
	}
	public void setzDisplacement(Double zDisplacement) {
		this.zDisplacement = zDisplacement;
	}
	public Double getzMoment() {
		return zMoment;
	}
	public void setzMoment(Double zMoment) {
		this.zMoment = zMoment;
	}
	public Double getyMoment() {
		return yMoment;
	}
	public void setyMoment(Double yMoment) {
		this.yMoment = yMoment;
	}
	public Double getxMoment() {
		return xMoment;
	}
	public void setxMoment(Double xMoment) {
		this.xMoment = xMoment;
	}
	public Double getzRotation() {
		return zRotation;
	}
	public void setzRotation(Double zRotation) {
		this.zRotation = zRotation;
	}
	public Double getyRotation() {
		return yRotation;
	}
	public void setyRotation(Double yRotation) {
		this.yRotation = yRotation;
	}
	public Double getxRotation() {
		return xRotation;
	}
	public void setxRotation(Double xRotation) {
		this.xRotation = xRotation;
	}
	
	
	
}

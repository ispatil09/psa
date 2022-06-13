package com.sd.entity.country;

import com.sd.entity.SectionDetailer;
import com.sd.enums.COUNTRY_STEEL_SECTION;
import com.sd.util.ExcelDataReader;

public class IndianSection implements SectionDetailer {
	private INDIAN_SUB_CATEGORY sub_CATEGORY = null;
	private boolean foundSectionFlag = false;
	private String sectionName = null;
	private double csArea = 0;
	private double inertia_IY = 0;
	private double inertia_IZ = 0;
	private double polarMoment = 0;
	public IndianSection(String sectionName) {
		this.sectionName = sectionName;
		double[] sectionData =  ExcelDataReader.getSectionData(COUNTRY_STEEL_SECTION.INDIAN,sectionName);
		if(sectionData!=null) {
			foundSectionFlag = true;
			if(sectionData[0]==0)
				sub_CATEGORY = INDIAN_SUB_CATEGORY.S;
			
			csArea=sectionData[1];
			polarMoment=sectionData[2];
			inertia_IY=sectionData[3];
			inertia_IZ=sectionData[4];
		}
		
	}

	@Override
	public double getCSArea() {
		return csArea;
	}

	@Override
	public double getInertialMomentY() {
		return inertia_IY;
	}

	@Override
	public double getInertialMomentZ() {
		return inertia_IZ;
	}

	@Override
	public double getPolarMoment() {
		return polarMoment;
	}

	@Override
	public COUNTRY_STEEL_SECTION getCountry() {
		return COUNTRY_STEEL_SECTION.INDIAN;
	}
	
	public INDIAN_SUB_CATEGORY getSubCategory() {
		return sub_CATEGORY;
	}

	@Override
	public boolean isSectionFound() {
		return foundSectionFlag;
	}

	@Override
	public String sectionName() {
		return sectionName;
	}
}

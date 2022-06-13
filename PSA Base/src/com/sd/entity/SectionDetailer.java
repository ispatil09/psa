package com.sd.entity;

import com.sd.enums.COUNTRY_STEEL_SECTION;

public interface SectionDetailer {
	public double getCSArea();
	public double getInertialMomentY();
	public double getInertialMomentZ();
	public double getPolarMoment();
	public COUNTRY_STEEL_SECTION getCountry();
	public boolean isSectionFound();
	public String sectionName();
}

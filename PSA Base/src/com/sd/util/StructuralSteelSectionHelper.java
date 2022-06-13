package com.sd.util;

import com.sd.entity.SectionDetailer;
import com.sd.entity.country.IndianSection;

public class StructuralSteelSectionHelper {

	public static SectionDetailer getSectionDetailer(String countryName, String sectionName) {
		if(countryName.equalsIgnoreCase("INDIAN"))
			return new IndianSection(sectionName);
		return null;
	}

}

package com.sd.test;

import com.sd.entity.SectionDetailer;
import com.sd.util.StructuralSteelSectionHelper;

public class SectionTester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String countryName = "INDIAN";
		String sectionName = "ISLB75";
//		StructuralSteelSectionHelper helper = new StructuralSteelSectionHelper();
		SectionDetailer detailer = StructuralSteelSectionHelper.getSectionDetailer(countryName,sectionName);
		if(detailer.isSectionFound()) {
			double polar = detailer.getPolarMoment();
			System.out.println("Polar Moment : "+polar);
			double area = detailer.getCSArea();
			System.out.println("Area : "+area);
		} else
			System.out.println("SECTION"+sectionName+" NOT FOUND..");
	}
}

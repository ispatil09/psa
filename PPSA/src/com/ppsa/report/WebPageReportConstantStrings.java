package com.ppsa.report;

public class WebPageReportConstantStrings {
	public static String HEAD = "<html>\n<head>\n<title>";
	public static String CSS_STRING = "</title>\n<style type=\"text/css\">\n .elementDiv {\nbackground-color: aqua;\nwidth: 800px;\nheight: 400px;\noverflow: scroll; }\n</style>";
	public static String JAVASCRIPT_BEGIN = "\n<script type=\"text/javascript\">\nfunction populateVehicle() {\n";
	public static String HEAD_CLOSE_NOW = "}\n</script>\n</head>\n<body onload=\"populateVehicle()\"style=\"overflow: auto; white-space: nowrap;\">\n";
	public static String CLOSE_BODY = "</body>\n</html>";
	public static final Object ELEMENT_DIV_BEGIN = "<div class=\"elementDiv\">";
	public static final Object DIV_END = "</div>";
	public static final Object ELEMENT_CANVAS_DIV_BEGIN = "<div style=\"float: right;border:4px solid #000;\"><canvas id=\"";
	
	public static final Object ELEMENT_CANVAS_DIV_END = "\" width=\"500\" height=\"700\"	style=\"background-color: activeborder;\"> </canvas></div>";
	public static final Object MATRIX_TABLE_BEGIN = "<div id=\"";
	public static final Object MATRIX_TABLE_BEGIN2 = "\">\n<table border=0 cellpadding=6 cellspacing=0 style=\"color: #000;\"><tr><td></td>";
	public static final Object MATRIX_TABLE_END = "</tr></table></div>";
	public static final String CO_ORD_NUM_CELL = "<td align=\"center\" valign=\"center\" width=30 style=\"color:maroon;font-style: italic;font: bold;\">";
	
	public static final Object EMPTY_CELL_BORDER_LEFTnTOP = "<td style=\"border-left: 4px solid #000;; border-top: 4px solid #000;\">&nbsp</td>";
	public static final Object EMPTY_CELL_BORDER_LEFT = "<td style=\"border-left: 4px solid #000;\">&nbsp</td>";
	public static final Object EMPTY_CELL_BORDER_RIGHTnTOP = "<td style=\"border-right: 4px solid #000; border-top: 4px solid #000;\">&nbsp</td>";
	public static final Object EMPTY_CELL_BORDER_RIGHT = "<td style=\"border-right: 4px solid #000;\">&nbsp</td>";
	public static final Object EMPTY_CELL_BORDER_LEFTnBOTTOM = "<td style=\"border-bottom: 4px solid #000; border-left: 4px solid #000;\">&nbsp</td>";
	public static final Object EMPTY_CELL_BORDER_RIGHTnBOTTOM = "<td style=\"border-right: 4px solid #000;border-bottom: 4px solid #000\">&nbsp</td>";
	
	public static final String CELL_DATA = "<td align=\"center\" valign=\"center\" width=30 title=\"";
	public static final String CELL_DATA_FOR_NO_TITLE = "<td align=\"center\" valign=\"center\" width=30 title=\"R1,C1\">";
}
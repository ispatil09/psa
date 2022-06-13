package com.ppsa.report;

import java.util.Set;

import com.ppsa.math.MathHelper;
import com.psa.entity.FiniteElement;
import com.psa.entity.impl.Node;
import com.psa.entity.impl.OneDimFiniteElement;
import com.psa.entity.impl.Structure;

public class WebCanvasJavaScriptDrawer {
	private static final double CANVAS_REAL_SPACE = 250;
	private static final double MARGIN = 60;
	private static final String NODE_INDICATOR_SIZE = "5";
	/**
	 * Length of lines indicating coOrdinates.
	 */
	private static final double CO_ORD_NUM_LENGTH = 25;
	private static final double CO_ORD_GAP = 4;
	private static final double POINTER_LENGTH = 6;
	private static final double POINTER_DEPTH = 3;
	
	private static final double CO_ORD_NUM_GAP = 3;
	private static final double RIGHT_CANVAS_MARGIN = 180;
	private static final double BOTTOM_CANVAS_MARGIN = 120;

	public static String getCanvasScript(Structure structure) {

		StringBuilder stringBuilder = new StringBuilder();

		Set<FiniteElement> finiteElements = structure.getFiniteElements();
		for (FiniteElement finiteElement : finiteElements) {
			OneDimFiniteElement oneDimFiniteElement = (OneDimFiniteElement) finiteElement;
			String drawSingleString = drawSingleFiniteElement(oneDimFiniteElement);
			stringBuilder.append(drawSingleString);
		}

		return stringBuilder.toString();
	}

	private static String drawSingleFiniteElement(
			OneDimFiniteElement oneDimFiniteElement) {

		double angleOfInclination = oneDimFiniteElement.getAngleOfInclination();
		double lengthOfElement = oneDimFiniteElement.getLengthOfElement();

		Node firstNode = oneDimFiniteElement.getFirstNode();
		Node secondNode = oneDimFiniteElement.getSecondNode();

		double x1 = 0;
		double y1 = 0;

		double x2 = secondNode.getxCordinate() - firstNode.getxCordinate();
		double y2 = firstNode.getyCordinate() - secondNode.getyCordinate();

		/*
		 * x2 =
		 * MathHelper.resolveForceInY(oneDimFiniteElement.getLengthOfElement(),
		 * oneDimFiniteElement.getAngleOfInclination()); y2 =
		 * MathHelper.resolveForceInX(oneDimFiniteElement.getLengthOfElement(),
		 * oneDimFiniteElement.getAngleOfInclination());
		 */

		double scaleX = CANVAS_REAL_SPACE / x2;
		double scaleY = CANVAS_REAL_SPACE / y2;
		double scale = 0;

		if (MathHelper.getMod(scaleX) < MathHelper.getMod(scaleY))
			scale = MathHelper.getMod(scaleX);
		else
			scale = MathHelper.getMod(scaleY);

		x1 = x1 * scale;
		y1 = y1 * scale;

		x2 = x2 * scale;
		y2 = y2 * scale;

		// x2=x1-x2;
		// y2=y1-y2;

		StringBuilder stringBuilder = new StringBuilder();

		stringBuilder.append("\n<!-- Member : "+ oneDimFiniteElement.getElementNumber() +" started -->\n");
		
				
		stringBuilder.append("var c = document.getElementById('");
		stringBuilder.append(ReporterCalculationWebPageNew.ELEMENT_CANVAS_ID
				+ oneDimFiniteElement.getElementNumber());
		stringBuilder.append("');\n");
		
		// Change size of canvas border
		stringBuilder.append("c.width="+(MathHelper.getMod(x2)+RIGHT_CANVAS_MARGIN)+";");
		stringBuilder.append("c.height="+(MathHelper.getMod(y2)+BOTTOM_CANVAS_MARGIN)+";");
		
		stringBuilder.append("var ctx = c.getContext('2d');\n");

		if ((y2 <= 0) && !(x2 < 0))
			stringBuilder.append("ctx.translate(" + MARGIN + ","
					+ (MathHelper.getMod(y2) + MARGIN) + ");\n");
		else if ((x2 < 0) && !(y2 < 0))
			stringBuilder.append("ctx.translate("
					+ (MathHelper.getMod(x2) + MARGIN) + "," + MARGIN + ");\n");
		else if ((x2 < 0) && (y2 < 0))
			stringBuilder.append("ctx.translate("
					+ (MathHelper.getMod(x2) + MARGIN) + ","
					+ (MathHelper.getMod(y2) + MARGIN) + ");\n");
		else
			stringBuilder.append("ctx.translate(" + MARGIN + "," + MARGIN
					+ ");\n");

		/**
		 * Draw Member line
		 */
		stringBuilder.append("ctx.strokeStyle = 'blue';\n");
		stringBuilder.append("ctx.lineWidth = 5;\n");

		stringBuilder.append("ctx.beginPath();\n");
		stringBuilder.append("ctx.moveTo(" + x1 + "," + y1 + ");\n");
		stringBuilder.append("ctx.lineTo(" + x2 + "," + y2 + ");\n");
		stringBuilder.append("ctx.stroke();\n");

		/**
		 * Draw Angle showing horizontal line
		 */
		stringBuilder.append("ctx.lineWidth = 1;\n");
		stringBuilder.append("ctx.strokeStyle = 'yellow';\n");

		stringBuilder.append("ctx.beginPath();\n");
		stringBuilder.append("ctx.moveTo(" + x1 + "," + y1 + ");\n");
		stringBuilder.append("ctx.lineTo(" + (x1 + 100) + "," + y1 + ");\n");
		stringBuilder.append("ctx.stroke();\n\n");

		/**
		 * Arc for showing angle.
		 */
		stringBuilder.append("ctx.lineWidth = 3;\n");
		stringBuilder.append("ctx.beginPath();\n");

		if (angleOfInclination < 0)
			stringBuilder.append("ctx.arc(" + x1 + "," + y1 + "," + 20
					+ ",+(Math.PI/180)*" + 0 + ", -(Math.PI/180)*"
					+ angleOfInclination + ",false);\n");
		else
			stringBuilder.append("ctx.arc(" + x1 + "," + y1 + "," + 20
					+ ",+(Math.PI/180)*" + 0 + ", -(Math.PI/180)*"
					+ angleOfInclination + ",true);\n");

		stringBuilder.append("ctx.stroke();\n\n");

		/**
		 * Showing nodes and node numbers.
		 */
		stringBuilder.append("ctx.beginPath();\n");
		stringBuilder.append("ctx.arc(" + x1 + "," + y1 + ","
				+ NODE_INDICATOR_SIZE + ", 0, (Math.PI/180)*360, false);");
		stringBuilder.append("ctx.arc(" + x2 + "," + y2 + ","
				+ NODE_INDICATOR_SIZE + ", 0, (Math.PI/180)*360, false);");

		stringBuilder.append("ctx.font = 'bold 16px serif';");
		stringBuilder.append("ctx.fillText('" + firstNode.getNodeNumber()
				+ "'," + (x1 - 15) + "," + y1 + ");\n");
		stringBuilder.append("ctx.fillText('" + secondNode.getNodeNumber()
				+ "'," + (x2 - 15) + "," + y2 + ");\n");

		stringBuilder.append("ctx.fill();\n\n");

		/**
		 * Showing coOrdinate lines
		 */
		stringBuilder.append("ctx.lineWidth = 2;\n");
		stringBuilder.append("ctx.strokeStyle = 'green';\n");

		// right side line
		stringBuilder.append("ctx.beginPath();\n");
		double coOrdGap = 0;
		if (angleOfInclination > 0)
			coOrdGap = CO_ORD_GAP;
		else
			coOrdGap = -CO_ORD_GAP;

		// right coOrd line coORdinates
		double rightCoOrdLineEndForFirstNode_X = x1 + CO_ORD_NUM_LENGTH;
		double rightCoOrdLineEndForFirstNode_Y = y1 + coOrdGap;
		// top coOrd line coORdinates
		double topCoOrdLineEndForFirstNode_X = x1 - coOrdGap;
		double topCoOrdLineEndForFirstNode_Y = y1 - CO_ORD_NUM_LENGTH;

		stringBuilder.append("ctx.moveTo(" + topCoOrdLineEndForFirstNode_X
				+ "," + rightCoOrdLineEndForFirstNode_Y + ");\n");
		stringBuilder.append("ctx.lineTo(" + rightCoOrdLineEndForFirstNode_X
				+ "," + rightCoOrdLineEndForFirstNode_Y + ");\n");
		stringBuilder.append("ctx.stroke();\n\n");

		// Top line
		stringBuilder.append("ctx.beginPath();\n");
		stringBuilder.append("ctx.moveTo(" + topCoOrdLineEndForFirstNode_X
				+ "," + rightCoOrdLineEndForFirstNode_Y + ");\n");
		stringBuilder.append("ctx.lineTo(" + topCoOrdLineEndForFirstNode_X
				+ "," + topCoOrdLineEndForFirstNode_Y + ");\n");
		stringBuilder.append("ctx.stroke();\n\n");

		// draw pointer to coOrd lines
		stringBuilder.append("ctx.fillStyle='green';\n");
		stringBuilder.append("<!-- Draw Pointers -->\n");
		stringBuilder.append("<!-- Right Pointers -->\n");
		stringBuilder.append("ctx.beginPath();\n");

		stringBuilder.append("ctx.moveTo(" + rightCoOrdLineEndForFirstNode_X
				+ "," + rightCoOrdLineEndForFirstNode_Y + ");\n");
		stringBuilder.append("ctx.lineTo("
				+ (rightCoOrdLineEndForFirstNode_X - POINTER_LENGTH) + ","
				+ (rightCoOrdLineEndForFirstNode_Y + POINTER_DEPTH) + ");\n");
		stringBuilder.append("ctx.lineTo("
				+ (rightCoOrdLineEndForFirstNode_X - POINTER_LENGTH) + ","
				+ (rightCoOrdLineEndForFirstNode_Y - POINTER_DEPTH) + ");\n");

		stringBuilder.append("ctx.closePath();\n");
		stringBuilder.append("ctx.stroke();\n\n");
		stringBuilder.append("ctx.fill();\n");

		stringBuilder.append("<!-- Top Pointers -->\n");
		stringBuilder.append("ctx.beginPath();\n");

		stringBuilder.append("ctx.moveTo(" + topCoOrdLineEndForFirstNode_X
				+ "," + topCoOrdLineEndForFirstNode_Y + ");\n");
		stringBuilder.append("ctx.lineTo("
				+ (topCoOrdLineEndForFirstNode_X - POINTER_DEPTH) + ","
				+ (topCoOrdLineEndForFirstNode_Y + POINTER_LENGTH) + ");\n");
		stringBuilder.append("ctx.lineTo("
				+ (topCoOrdLineEndForFirstNode_X + POINTER_DEPTH) + ","
				+ (topCoOrdLineEndForFirstNode_Y + POINTER_LENGTH) + ");\n");

		stringBuilder.append("ctx.closePath();\n");
		stringBuilder.append("ctx.stroke();\n\n");
		stringBuilder.append("ctx.fill();\n\n");

		/**
		 * For Second node.
		 */
		// right coOrd line coORdinates
		double rightCoOrdLineEndForSecondNode_X = x2 + CO_ORD_NUM_LENGTH;
		double rightCoOrdLineEndForSecondNode_Y = y2 + coOrdGap;
		// top coOrd line coORdinates
		double topCoOrdLineEndForSecondNode_X = x2 - coOrdGap;
		double topCoOrdLineEndForSecondNode_Y = y2 - CO_ORD_NUM_LENGTH;

		stringBuilder.append("ctx.beginPath();\n");
		stringBuilder.append("ctx.moveTo(" + topCoOrdLineEndForSecondNode_X
				+ "," + rightCoOrdLineEndForSecondNode_Y + ");\n");
		stringBuilder.append("ctx.lineTo(" + rightCoOrdLineEndForSecondNode_X
				+ "," + rightCoOrdLineEndForSecondNode_Y + ");\n");
		stringBuilder.append("ctx.stroke();\n\n");

		// Top line
		stringBuilder.append("ctx.beginPath();\n");
		stringBuilder.append("ctx.moveTo(" + topCoOrdLineEndForSecondNode_X
				+ "," + rightCoOrdLineEndForSecondNode_Y + ");\n");
		stringBuilder.append("ctx.lineTo(" + topCoOrdLineEndForSecondNode_X
				+ "," + topCoOrdLineEndForSecondNode_Y + ");\n");
		stringBuilder.append("ctx.stroke();\n\n");

		// draw pointer to coOrd lines
		stringBuilder.append("<!-- Draw Pointers -->\n");
		stringBuilder.append("<!-- Right Pointers -->\n");
		stringBuilder.append("ctx.beginPath();\n");
		stringBuilder.append("ctx.fillStyle='green';\n");

		stringBuilder.append("ctx.moveTo(" + rightCoOrdLineEndForSecondNode_X
				+ "," + rightCoOrdLineEndForSecondNode_Y + ");\n");
		stringBuilder.append("ctx.lineTo("
				+ (rightCoOrdLineEndForSecondNode_X - POINTER_LENGTH) + ","
				+ (rightCoOrdLineEndForSecondNode_Y + POINTER_DEPTH) + ");\n");
		stringBuilder.append("ctx.lineTo("
				+ (rightCoOrdLineEndForSecondNode_X - POINTER_LENGTH) + ","
				+ (rightCoOrdLineEndForSecondNode_Y - POINTER_DEPTH) + ");\n");

		stringBuilder.append("ctx.closePath();\n");
		stringBuilder.append("ctx.stroke();\n\n");
		stringBuilder.append("ctx.fill();\n");

		stringBuilder.append("<!-- Top Pointers -->\n");
		stringBuilder.append("ctx.beginPath();\n");

		stringBuilder.append("ctx.moveTo(" + topCoOrdLineEndForSecondNode_X
				+ "," + topCoOrdLineEndForSecondNode_Y + ");\n");
		stringBuilder.append("ctx.lineTo("
				+ (topCoOrdLineEndForSecondNode_X - POINTER_DEPTH) + ","
				+ (topCoOrdLineEndForSecondNode_Y + POINTER_LENGTH) + ");\n");
		stringBuilder.append("ctx.lineTo("
				+ (topCoOrdLineEndForSecondNode_X + POINTER_DEPTH) + ","
				+ (topCoOrdLineEndForSecondNode_Y + POINTER_LENGTH) + ");\n");

		stringBuilder.append("ctx.closePath();\n");
		stringBuilder.append("ctx.stroke();\n\n");
		stringBuilder.append("ctx.fill();\n\n");

	
		/**
		 * CoOrd numbering.
		 */
		stringBuilder.append("ctx.font = 'italic 12px serif';");
		// Numbering for first node
		stringBuilder.append("ctx.fillText('"+firstNode.getxCordinateNum()+"',"+(rightCoOrdLineEndForFirstNode_X+CO_ORD_NUM_GAP)+","+rightCoOrdLineEndForFirstNode_Y+");");
		stringBuilder.append("ctx.fillText('"+firstNode.getyCordinateNum()+"',"+(topCoOrdLineEndForFirstNode_X)+","+(topCoOrdLineEndForFirstNode_Y-CO_ORD_NUM_GAP)+");");
		// Numbering for second node
		stringBuilder.append("ctx.fillText('"+secondNode.getxCordinateNum()+"',"+(rightCoOrdLineEndForSecondNode_X+CO_ORD_NUM_GAP)+","+rightCoOrdLineEndForSecondNode_Y+");");
		stringBuilder.append("ctx.fillText('"+secondNode.getyCordinateNum()+"',"+(topCoOrdLineEndForSecondNode_X)+","+(topCoOrdLineEndForSecondNode_Y-CO_ORD_NUM_GAP)+");");

		/**
		 * writing length of member.
		 */
		stringBuilder.append("\n<!-- Length of member -->\n");
		stringBuilder.append("ctx.font = \'italic bold 18px serif';");
		stringBuilder.append("ctx.fillStyle = 'blue';\n");
		stringBuilder.append("ctx.save();");
		stringBuilder.append("ctx.rotate(-(Math.PI/180)*"+angleOfInclination+");");
		stringBuilder.append("ctx.fillText('l = "+(lengthOfElement)+"',"+(lengthOfElement*scale/2-50)+","+(y1-10)+");");
		stringBuilder.append("ctx.restore();");
		
		/**
		 * writing angle theta.
		 */
		stringBuilder.append("ctx.fillStyle = 'yellow';\n");
		//stringBuilder.append("ctx.rotate(-(Math.PI/180)*"+(oneDimFiniteElement.getAngleOfInclination()/2)+");");
		if(angleOfInclination>0)
			stringBuilder.append("ctx.fillText(' ang = "+(angleOfInclination)+"',"+(x1+20)+","+(y1-10)+");\n");
		else
			stringBuilder.append("ctx.fillText(' ang = "+(angleOfInclination)+"',"+(x1+20)+","+(y1+15)+");\n");
		

		stringBuilder.append("\n<!-- Member : "+ oneDimFiniteElement.getElementNumber() +" finished -->\n\n");

		return stringBuilder.toString();
	}
}

package psa.smt.util;
import java.util.LinkedList;

import psa.smt.entity.MatrixSkyLine;


public class MatrixFactory {

	public static MatrixSkyLine createSkylineMatrix(MatrixSkyLine matrixSkyLine) {
		long nanoTimeOne = System.nanoTime();
		LinkedList<Integer> positions = new LinkedList<Integer>();
		LinkedList<Double> values = new LinkedList<Double>();
		
		positions.addAll(matrixSkyLine.getPositionMarker());
		values.addAll(matrixSkyLine.getValues());
		
		MatrixSkyLine matrixSkyLineDup = new MatrixSkyLine(positions, values);
		long nanoTimeSec = System.nanoTime();
		System.out.println("TD (mS) duplicateMat gen time... : "+(nanoTimeSec-nanoTimeOne)/1000000);
		return matrixSkyLineDup;
	}
	
	public static MatrixSkyLine createEmptySkylineMatrix() {
		LinkedList<Integer> positions = new LinkedList<Integer>();
		LinkedList<Double> values = new LinkedList<Double>();
		
		MatrixSkyLine matrixSkyLineDup = new MatrixSkyLine(positions, values);
		return matrixSkyLineDup;
	}

	public static MatrixSkyLine createSkylineMatWithOnlyDiagElements(MatrixSkyLine matrix) {
		LinkedList<Integer> positions = new LinkedList<Integer>();
		LinkedList<Double> values = new LinkedList<Double>();
	
		int matSize = matrix.getSize();
		for (int i = 1; i < (matSize+1); i++) {
			positions.add(i);
			values.add(0d);
		}
		
		
		MatrixSkyLine matrixSkyLineDup = new MatrixSkyLine(positions, values);
		return matrixSkyLineDup;
	}
	
}

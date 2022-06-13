package psa.smt.test;

import java.util.LinkedList;

import psa.smt.entity.MatrixSkyLine;

public class TestSkyLine {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		LinkedList<Integer> positions = new LinkedList<>();
		
		//positions.add(0);
		positions.add(1);
		positions.add(2);
		positions.add(5);
		positions.add(8);
		positions.add(9);
		positions.add(15);
		
		LinkedList<Double> values = new LinkedList<>();
		
		//11,22,13,0,33,24,34,44,55,16,0,0,46,56,66;
		
		values.add(11d);
		values.add(22d);
		values.add(13d);
		values.add(0d);
		values.add(33d);
		values.add(24d);
		values.add(34d);
		values.add(44d);
		values.add(55d);
		values.add(16d);
		values.add(0d);
		values.add(0d);
		values.add(46d);
		values.add(56d);
		values.add(66d);
		
		
		MatrixSkyLine matrixSkyLine = new MatrixSkyLine(positions,values);
		System.out.println("SkyLineMatrix size is : "+matrixSkyLine.getSize()+"\n");
		System.out.println(matrixSkyLine);
		
		int res = 7/2;
		System.out.println(res);
		
		System.out.println("LowerMatrix : ");
		String lowerTraingularMatrix = matrixSkyLine.getLowerTraingularMatrix();
		System.out.println(lowerTraingularMatrix);
		
		System.out.println("UpperMatrix : ");
		String upperTraingularMatrix = matrixSkyLine.getUpperTraingularMatrix();
		System.out.println(upperTraingularMatrix);
		
		System.out.println("Size = "+matrixSkyLine.getSize());
	}

}

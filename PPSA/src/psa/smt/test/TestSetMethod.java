package psa.smt.test;

import java.util.LinkedList;

import psa.smt.entity.MatrixSkyLine;

public class TestSetMethod {
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
		String lowerTraingularMatrix = matrixSkyLine.getLowerTraingularMatrix();
		System.out.println(lowerTraingularMatrix);
		
		matrixSkyLine.set(4, 1, 1.2);
		System.out.println("Adding 1.2");
		String lowerTraingularMatrixSec = matrixSkyLine.getLowerTraingularMatrix();
		System.out.println(lowerTraingularMatrixSec);
		
		
		matrixSkyLine.set(4, 2, 2.4);
		System.out.println("Adding 2.4");
		String lowerTraingularMatrixThird = matrixSkyLine.getLowerTraingularMatrix();
		System.out.println(lowerTraingularMatrixThird);
		
		matrixSkyLine.set(5, 5, 253.54);
		System.out.println("Change 55.0 to 253.54");
		String lowerTraingularMatrixFourth = matrixSkyLine.getLowerTraingularMatrix();
		System.out.println(lowerTraingularMatrixFourth);
		
		matrixSkyLine.set(0, 0, 145.56);
		System.out.println("Change 0,0");
		String lowerTraingularMatrixFifth = matrixSkyLine.getLowerTraingularMatrix();
		System.out.println(lowerTraingularMatrixFifth);
		

		System.out.println("\n"+matrixSkyLine);
/*		positions.add(2,77);
		positions.add(2,88);
		System.out.println("Hi");*/
	}
}

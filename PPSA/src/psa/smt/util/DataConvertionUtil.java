package psa.smt.util;

import java.util.LinkedList;

public class DataConvertionUtil {

	public static double[] getDoubleVals(LinkedList<Double> dVector) {
		double[] vals = new double[dVector.size()];
		int i=0;
		for (Double doubleVal : dVector) {
			vals[i] = doubleVal;
			i++;
		}
		return vals;
	}

}

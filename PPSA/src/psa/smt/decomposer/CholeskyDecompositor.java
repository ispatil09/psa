package psa.smt.decomposer;
import psa.smt.util.MatrixFactory;
import psa.smt.entity.MatrixSkyLine;

/*
 * Copyright 2011-2013, by Vladimir Kostyukov and Contributors.
 * 
 * This file is part of la4j project (http://la4j.org)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Contributor(s): -
 * 
 */


/**
 * This class represents Cholesky decomposition of matrices. More details
 * <p>
 * <a href="http://mathworld.wolfram.com/CholeskyDecomposition.html"> here</a>
 * </p>
 */
public class CholeskyDecompositor {

	protected MatrixSkyLine matrix;
    public CholeskyDecompositor(MatrixSkyLine matrix) {

        if (!applicableTo(matrix)) {
            fail("Given matrix can not be used with this decompositor.");
        }

        this.matrix = matrix;        
    }

    protected void fail(String message) {
        throw new IllegalArgumentException(message);
    }


    /**
     * Returns the result of Cholesky decomposition of given matrix
     * <p>
     * See <a href="http://mathworld.wolfram.com/CholeskyDecomposition.html">
     * http://mathworld.wolfram.com/CholeskyDecomposition.html</a> for more
     * details.
     * </p>
     * 
     * @param matrix
     * @param factory
     * @return { L }
     */
    public MatrixSkyLine decompose() {

        if (!applicableTo(matrix)) {
            fail("This matrix can not be decomposed with Cholesky.");
        }

        MatrixSkyLine l = MatrixFactory.createSkylineMatWithOnlyDiagElements(matrix);

        for (int j = 0; j < l.rows(); j++) {

            double d = 0.0;

            // change '0' nonZeroMultBegin
            //bcos both num elements would be zero
            int envelopeIndexSec=matrix.getEnvelopeIndexOfThisRow(j);
            /*System.out.println("envInd : for ind "+envelopeIndexSec+" -- "+j);*/
            for (int k = envelopeIndexSec; k < j; k++) {

                double s = 0.0;
                
                int envelopeIndexFirst=matrix.getEnvelopeIndexOfThisRow(k);
                // '0' to changeToMinEnvelope
                int nonZeroMultBegin = Math.max(envelopeIndexFirst, envelopeIndexSec);
                
                double matVal = matrix.get(j, k);
                for (int i = nonZeroMultBegin; i < k; i++) {
					s += l.get(k, i) * l.get(j, i);
                }

				s = (matVal - s) / l.get(k, k);
                
                if(s!=0)
                l.set(j, k, s);
                d = d + s * s;
            }

            d = matrix.get(j, j) - d;

            //remove Math.max
            l.set(j, j, Math.sqrt(d));
//            System.out.println("Finished "+j+" th row");

            /*for (int k = j + 1; k < l.rows(); k++) {
                l.set(j, k, 0.0);
            }*/
        }

        return l ;
    }

    /**
     * Checks if the matrix is positive definite
     * <p>
     * See <a href="http://mathworld.wolfram.com/PositiveDefiniteMatrix.html">
     * http://mathworld.wolfram.com/PositiveDefiniteMatrix.html</a> for more
     * details.
     * </p>
     * 
     * @param matrix
     * @return <code>true</code> if matrix is positive definite
     */
    /*private boolean isPositiveDefinite(Matrix matrix) {

        //
        // TODO: Issue 12
        //

        int n = matrix.rows();
        boolean result = true;

        Matrix l = matrix.blank();

        for (int j = 0; j < n; j++) {

            Vector rowj = l.getRow(j);

            double d = 0.0;
            for (int k = 0; k < j; k++) {

                Vector rowk = l.getRow(k);
                double s = 0.0;

                for (int i = 0; i < k; i++) {
                    s += rowk.get(i) * rowj.get(i);
                }

                s = (matrix.get(j, k) - s) / l.get(k, k);

                rowj.set(k, s);
                l.setRow(j, rowj);

                d = d + s * s;
            }

            d = matrix.get(j, j) - d;

            result = result && (d > 0.0);

            l.set(j, j, Math.sqrt(Math.max(d, 0.0)));

            for (int k = j + 1; k < n; k++) {
                l.set(j, k, 0.0);
            }
        }

        return result;
    }*/

    public boolean applicableTo(MatrixSkyLine matrix) {
        /*return matrix.rows() == matrix.columns() &&
               matrix.isSymmetric();
               //isPositiveDefinite(matrix);*/
    	return true;
    	}
}

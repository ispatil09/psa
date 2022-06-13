package psa.smt.entity;
import java.util.LinkedList;

import psa.smt.decomposer.CholeskyDecompositor;


public class MatrixSkyLine {

	private LinkedList<Integer> positions = null;
	private LinkedList<Double> values = null;
	private int[] backSubEnvelopeIndices = null;
	
	public MatrixSkyLine(LinkedList<Integer> positions , LinkedList<Double> values) {
		this.positions=positions;
		this.values= values;
		
	}
	
	public MatrixSkyLine() {
		this.positions=new LinkedList<Integer>();
		this.values= new LinkedList<Double>();
		
	}
	
	public MatrixSkyLine(double[][] valuesMatrix) {
		//StringBuilder sb = new StringBuilder();
		this.positions=new LinkedList<Integer>();
		this.values= new LinkedList<Double>();
		
		int sizeOfMat = valuesMatrix.length;
		
		for (int i = 0; i < sizeOfMat; i++) {
			positions.add(1);
		}
		
        for (int i = 0; i < sizeOfMat; i++) {
			for (int j = 0; j <= i; j++) {
				if(valuesMatrix[i][j]!=0)
					set(i,j,valuesMatrix[i][j]);
			}
		}
	}
	public void initializeBackSubEnvelopeData() {
		int maxInd = this.getSize()-1;
		int backSubEnvIndeices[] = new int[maxInd+1];
		for (int i = maxInd; i >= 0; i--) {
			boolean foundNonZeroValFlag = true;
			for (int j = maxInd; j > i; j--) {
				//foundNonZeroValFlag = false;
				double val = get(i,j);
				if(val!=0) {
					backSubEnvIndeices[i]=j;
					foundNonZeroValFlag=false;
					break;
				}
			}
			if(foundNonZeroValFlag)
				backSubEnvIndeices[i]=i;
		}
		this.backSubEnvelopeIndices = backSubEnvIndeices;
	}
	public int getBackSubEnvelopeForThisRow(int rowId) {
		/*int maxInd = this.getSize()-1;
		for (int i = maxInd; i > rowId; i--) {
			int envInd = getEnvelopeIndexOfThisRow(i);
			if(envInd<=rowId)
				return i;
		}
		return maxInd-rowId;*/
		return this.backSubEnvelopeIndices[rowId];
	}
	/**
	 * Get value from the mentioned position.
	 * @param k
	 * @param i
	 * @return
	 */
	public double get(int i, int j) {
		
		/*i=j;
		int temp = j;
		j=temp;*/
		
		int rowInd = i;
		int colInd = j;
		
		if(j>i) {
			colInd=i;
			rowInd=j;
		}
		
		int posOfDiagElementInRowMentioned ;
		int posOfDiagElementInRowMentionedPrev=0 ;
		
		if(rowInd!=0) {
			posOfDiagElementInRowMentioned = this.positions.get(rowInd);
			posOfDiagElementInRowMentionedPrev = this.positions.get(rowInd-1);
		} else {
			posOfDiagElementInRowMentioned = 1;
			posOfDiagElementInRowMentionedPrev = 0;
		}
		
		int numOfElementsInThisRow = posOfDiagElementInRowMentioned-posOfDiagElementInRowMentionedPrev;
		int fullElements = rowInd+1;
		
		int envelopeIndexOfThisRow = fullElements - numOfElementsInThisRow;
		
		if(colInd<envelopeIndexOfThisRow) {
			return 0;
		} else {
			int posOfElementFromEnvelope = colInd-envelopeIndexOfThisRow;
			int posInValuesList = posOfDiagElementInRowMentionedPrev+posOfElementFromEnvelope;
			
			double valuAtSpecifiedIndex = 0;
			valuAtSpecifiedIndex = this.values.get(posInValuesList);
			return valuAtSpecifiedIndex;
		}
			
	}
	
	/**
	 * Set Value at mentioned positions.
	 * @param j
	 * @param k
	 * @param s
	 */
	public void set(int i, int j, double val) {
		
		int rowInd = i;
		int colInd = j;
		
		if(j>i) {
			colInd=i;
			rowInd=j;
		}
		boolean indexInRange = true;
		if(i>(positions.size()-1))
			indexInRange=false;

		if(indexInRange)
			indexWithinRange(rowInd, colInd, val);
		else
			indexOutofRange(rowInd, colInd, val);
		
			
	}

	private void indexOutofRange(int rowInd, int colInd, double val) {
		int presentMaxRowIndex = positions.size()-1;
		int extraRowsToBeGenerated = rowInd - presentMaxRowIndex;
		if(positions.size()==0)
			positions.add(0);
		for (int i = 0; i < extraRowsToBeGenerated-1; i++) {
			Integer lastRowsDiagElementPos = positions.getLast();
			positions.add(lastRowsDiagElementPos+1);
			values.add(1d);
		}
		positions.remove(0);
			
		// Now add the last rows 
		int envelopeDistFromDiagElementPosition = rowInd - colInd;
		values.add(val);
		positions.add(envelopeDistFromDiagElementPosition+positions.getLast()+1);
		for (int i = 0; i < envelopeDistFromDiagElementPosition-1; i++) {
			values.add(0d);
		}
		values.add(1d);
		this.getLowerTraingularMatrix();
		
	}

	private void indexWithinRange(int rowInd, int colInd, double val) {
		int posOfDiagElementInRowMentioned=0 ;
		int posOfDiagElementInRowMentionedPrev=0 ;
		
		if(rowInd==0 && colInd ==0) {
			//double valPrev = this.values.get(0);
			if(values.size()!=0)
				values.remove(0);
			
			values.add(0,val);
			return;
		}
//		else {
			posOfDiagElementInRowMentioned = this.positions.get(rowInd);
			posOfDiagElementInRowMentionedPrev = this.positions.get(rowInd-1);
//		}
//		
		int numOfElementsInThisRow = posOfDiagElementInRowMentioned-posOfDiagElementInRowMentionedPrev;
		int fullElements = rowInd+1;
		
		int actualColBeginIndexInFullMatrix = fullElements - numOfElementsInThisRow;
		
		if(colInd<actualColBeginIndexInFullMatrix) {
			int indexDiff = actualColBeginIndexInFullMatrix-colInd;
			increamentAllPositionsByThisMuch(rowInd,indexDiff);
			
			posOfDiagElementInRowMentioned = this.positions.get(rowInd);
			//posOfDiagElementInRowMentionedPrev = this.positions.get(rowInd-1);
			numOfElementsInThisRow = posOfDiagElementInRowMentioned-posOfDiagElementInRowMentionedPrev;
			
			actualColBeginIndexInFullMatrix = fullElements - numOfElementsInThisRow;
			
			int pushtoIndex = posOfDiagElementInRowMentionedPrev;
			for (int k = 1; k < indexDiff; k++) {
				this.values.add(pushtoIndex,0d);
			}
			this.values.add(pushtoIndex,val);
			return;
			
		} else {
			

			int posOfElementFromEnvelope = colInd-actualColBeginIndexInFullMatrix;
			int posInValuesList = posOfDiagElementInRowMentionedPrev+posOfElementFromEnvelope;
			
			values.remove(posInValuesList);
			values.add(posInValuesList,val);
			return;
		}
	}
	
	private void increamentAllPositionsByThisMuch(int rowInd, int indexDiff) {
		
		for (int rowIndex = rowInd; rowIndex < getSize(); rowIndex++) {
			
			int prevPosVal = positions.get(rowIndex);
			int presPosVal = prevPosVal+indexDiff;
			
			changePositionOfDiagElementInThisRow(rowIndex,presPosVal);
			
		}
		
	}

	private void changePositionOfDiagElementInThisRow(int rowIndex, int presPosVal) {
		positions.remove(rowIndex);
		positions.add(rowIndex,presPosVal);
	}

	/**
	 * Get number of rows.
	 * @return
	 */
	public int rows() {
		return this.positions.size();
	}

	/**
	 * Ger num of cols.
	 * @return
	 */
	public int columns() {
		return this.positions.size();
	}

	/**
	 * Check if method is symmetric.
	 * @return
	 */
	/*public boolean isSymmetric() {
		// TODO Auto-generated method stub
		return true;
	}*/

	public LinkedList<Integer> getPositionMarker() {
		return positions;
	}

	public void setPositionMarker(LinkedList<Integer> positionMarker) {
		this.positions = positionMarker;
	}

	public LinkedList<Double> getValues() {
		return values;
	}

	public void setValues(LinkedList<Double> values) {
		this.values = values;
	}
	
	public int getSize() {
		return this.positions.size();
	}
	
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		int sizeOfMat = getSize();
        int rows = sizeOfMat;
        int columns = sizeOfMat;
        
        for (int i = 0; i <rows; i++) {
			for (int j = 0; j <columns; j++) {
				sb.append(get(i,j)+" ");
			}
			sb.append("\n");
		}
		
		
		return sb.toString();
	}
	
	/**
	 * It gives brief Idea of how to consider lower triangular matrix.
	 * @return
	 */
	public String getLowerTraingularMatrix() {
		StringBuilder sb = new StringBuilder();
		int sizeOfMat = getSize();
        int rows = sizeOfMat;
        int columns = sizeOfMat;
        
        for (int i = 0; i < rows; i++) {
			for (int j = 0; j <= i; j++) {
				sb.append(get(i,j)+" ");
			}
			sb.append("\n");
		}
		
		
		return sb.toString();
	}
	
	
	/**
	 * It gives brief Idea of how to consider upper triangular matrix. 
	 * @return
	 */
	public String getUpperTraingularMatrix() {
		StringBuilder sb = new StringBuilder();
		int sizeOfMat = getSize();
        int rows = sizeOfMat;
        int columns = sizeOfMat;
        
        for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				if(i>j)
					sb.append(" - ");
				else
					sb.append(get(i,j)+" ");
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	public MatrixSkyLine decomposeByCholesky() {
		CholeskyDecompositor decompositor = new CholeskyDecompositor(this);
		return decompositor.decompose();
		
	}

	public int getEnvelopeIndexOfThisRow(int rowInd) {
		/*int prevRowDiagElement = positions.get(rowInd-1);
		int presRowDiagElement = positions.get(rowInd);

		int fullElementsInThisRow = rowInd+1;
		
		int envelopeIndexOfThisRow = */
		
		int posOfDiagElementInRowMentioned ;
		int posOfDiagElementInRowMentionedPrev=0 ;
		
		if(rowInd!=0) {
			posOfDiagElementInRowMentioned = this.positions.get(rowInd);
			posOfDiagElementInRowMentionedPrev = this.positions.get(rowInd-1);

			// Same code as above 'LL343'
			int numOfElementsInThisRow = posOfDiagElementInRowMentioned-posOfDiagElementInRowMentionedPrev;
			int fullElements = rowInd+1;
			
			int actualColBeginIndexInFullMatrix = fullElements - numOfElementsInThisRow;
			
			return actualColBeginIndexInFullMatrix;
		} else {
			posOfDiagElementInRowMentioned = 1;
			posOfDiagElementInRowMentionedPrev = 0;
			
			// Same code as above 'LL343'
			int numOfElementsInThisRow = posOfDiagElementInRowMentioned-posOfDiagElementInRowMentionedPrev;
			int fullElements = rowInd+1;
			
			int actualColBeginIndexInFullMatrix = fullElements - numOfElementsInThisRow;
			
			return actualColBeginIndexInFullMatrix;
		}
		
//		
		
	}

	/*@Override
	public String toString() {


        final int precision = 3; 

        int sizeOfMat = getSize();
        int rows = sizeOfMat;
        int columns = sizeOfMat;
		int formats[] = new int[sizeOfMat];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                double value = get(i, j);
                int size = String.valueOf((long) value).length() 
                           + precision + (value < 0 && value > -1.0 ? 1 : 0) + 2;
                formats[j] = size > formats[j] ? size : formats[j];
            }
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                sb.append(String.format("%" + Integer.toString(formats[j])
                        + "." + precision + "f", get(i, j)));
            	sb.append(get(i, j));
            }
            sb.append("\n");
        }

        return sb.toString();
    
	}*/


}

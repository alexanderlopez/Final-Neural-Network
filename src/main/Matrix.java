package main;

public class Matrix {

	private int rows;
	private int cols;
	private double[][] data;
	
	public Matrix(double[][] _data) {
		rows = _data.length;
		cols = _data[0].length;
		data = _data;
	}
	
	public Matrix(int _rows, int _cols) {
		rows = _rows;
		cols = _cols;
		data = new double[rows][cols];
	}
	
	public int getRows() {
		return rows;
	}
	
	public int getCols() {
		return cols;
	}
	
	public double getValueAt(int row, int col) {
		return data[row][col];
	}
	
	public void setValue(double val, int row, int col) {
		data[row][col] = val;
	}
	
	public double[][] getData(){
		return data;
	}
	
	//STATIC METHODS
	
	public static Matrix transpose(Matrix m) {
		Matrix transposed = new Matrix(m.getCols(), m.getRows());
		
		for (int i = 0; i < m.getRows(); i++)
			for (int j = 0; j < m.getCols(); j++) {
				transposed.setValue(m.getValueAt(i, j), j, i);
			}
		
		return transposed;
	}
	
	public static Matrix randomize(int rows, int cols, double range) {
		
		double[][] rData = new double[rows][cols];
		
		for (int i = 0; i < rows; i++)
			for (int j = 0; j < cols; j++) {
				rData[i][j] = (Math.random()*range) - (range/2);
			}
		
		Matrix random = new Matrix(rData);
		
		return random;
	}
	
	public static Matrix zero(int rows, int cols) {
		double[][] rData = new double[rows][cols];
		
		for (int i = 0; i < rows; i++)
			for (int j = 0; j < cols; j++) {
				rData[i][j] = 0;
			}
		
		Matrix random = new Matrix(rData);
		
		return random;
	}
	
	public static Matrix sigmoid(Matrix input) {
		Matrix result = new Matrix(input.getRows(), input.getCols());
		
		for (int i = 0; i < input.getRows(); i++)
			for (int j = 0; j < input.getCols(); j++) {
				double sigmoid_v = 1/(1+Math.exp(-input.getValueAt(i, j)));
				result.setValue(sigmoid_v, i, j);
			}
		
		return result;
	}
	
	public static Matrix sigprime(Matrix input) {
		Matrix result = new Matrix(input.getRows(), input.getCols());
		
		for (int i = 0; i < input.getRows(); i++)
			for (int j = 0; j < input.getCols(); j++) {
				double x = input.getValueAt(i, j);
				double sigmoid_v = (Math.exp(-x))/(Math.pow(1+Math.exp(-x), 2));
				result.setValue(sigmoid_v, i, j);
			}
		
		return result;
	}
	
	public static Matrix multiply(Matrix A, double scalar) {
		Matrix resultant = new Matrix(A.getRows(), A.getCols());
		for (int i = 0; i < A.getRows(); i++)
			for (int j = 0; j < A.getCols(); j++) {
				double val = A.getValueAt(i, j)*scalar;
				resultant.setValue(val, i, j);
			}
		
		return resultant;
	}
	
	public static Matrix add(Matrix A, Matrix B) {
		if (A.getRows() != B.getRows() || A.getCols() != B.getCols())
			return null;
		
		Matrix resultant = new Matrix(A.getRows(), A.getCols());
		
		for (int i = 0; i < A.getRows(); i++)
			for (int j = 0; j < A.getCols(); j++) {
				double sum = A.getValueAt(i, j) + B.getValueAt(i, j);
				resultant.setValue(sum, i, j);
			}
		
		return resultant;
	}
	
	public static Matrix dot(Matrix A, Matrix B) {
		if (A.getCols() != B.getRows())
			return null;
		
		Matrix resultant = new Matrix(A.getRows(), B.getCols());
		
		for (int i = 0; i < A.getRows(); i++) {
			for (int j = 0; j < B.getCols(); j++) {
				double sum = 0;
				
				for (int k = 0; k < A.getCols(); k++) {
					sum += A.getValueAt(i, k) * B.getValueAt(k, j);
				}
				
				resultant.setValue(sum, i, j);
			}
		}
		
		return resultant;
	}
	
	public static Matrix hadamard(Matrix A, Matrix B) {
		if (A.getCols() != B.getCols() && A.getRows() != B.getRows())
			return null;
		
		Matrix resultant = new Matrix(A.getRows(), A.getCols());
		
		for (int i = 0; i < resultant.getRows(); i++)
			for (int j = 0; j < resultant.getCols(); j++)
				resultant.setValue(A.getValueAt(i, j)*B.getValueAt(i, j), i, j);
		
		return resultant;
	}
	
	public static String printString(Matrix A) {
		StringBuilder sb = new StringBuilder();
		
		sb.append('[');
		
		for (int i = 0; i < A.getRows(); i++) {
			sb.append('[');
			for (int j = 0; j < A.getCols() - 1; j++) {
				sb.append(A.getValueAt(i, j));
				sb.append(", ");
			}
			sb.append(A.getValueAt(i, A.getCols() - 1));
			sb.append(']');
			
			if (i != (A.getRows()-1))
				sb.append(System.lineSeparator());
		}
		
		sb.append(']');
		
		return sb.toString();
	}
	
	public static String toCSV(Matrix A) {
		StringBuilder sb = new StringBuilder();
		
		sb.append(A.getRows() + "," + A.getCols());
		sb.append(System.lineSeparator());
		
		for (int i = 0; i < A.getRows(); i++) {
			for (int j = 0; j < A.getCols(); j++) {
				sb.append(A.getValueAt(i, j));
				
				if (j != (A.getCols()-1))
					sb.append(',');
			}
			
			if (i != (A.getRows()-1))
				sb.append(System.lineSeparator());
		}
		
		return sb.toString();
	}
}

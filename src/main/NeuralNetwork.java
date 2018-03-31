package main;

import java.util.Random;
import java.util.function.BiConsumer;

public class NeuralNetwork {
	
	public static int BATCH_LENGTH = 10;
	private static double INIT_RANGE = 1;
	private static double EPSILON = 0.0001;
	
	private double learningRate = 0.1d;
	
	private int layers; //Includes input and output layer counts.
	private int[] layerLengths; //Indexes go from input layer as 0, to output layer as the final item of the array.
	
	private Matrix[] weights; //0 - first layer after input, n, last, output layer.
	private Matrix[] biases; //Same as above for weights.

	public NeuralNetwork(int _layers, int[] _lengths) {
		layers = _layers;
		layerLengths = _lengths;
	}
	
	public NeuralNetwork(int _layers, int[] _lengths, double _learningRate) {
		layers = _layers;
		layerLengths = _lengths;
		learningRate = _learningRate;
	}
	
	public void init() {
		weights = new Matrix[layers-1];
		biases = new Matrix[layers-1];
		
		for (int i = 0; i < layers-1; i++) {
			weights[i] = Matrix.randomize(layerLengths[i+1], layerLengths[i], INIT_RANGE);
			biases[i] = Matrix.randomize(layerLengths[i+1], 1, INIT_RANGE);
		}
	}
	
	public Matrix compute(Matrix normalized_input) {
		if (normalized_input.getRows() != layerLengths[0])
			return null;
		
		Matrix resultant = normalized_input;
		
		for (int i = 0; i < layers-1; i++) {
			Matrix temp = Matrix.sigmoid(Matrix.add(Matrix.dot(weights[i], resultant), biases[i]));
			resultant = temp;
		}
		
		return resultant;
	}
	
	public double analyticalDerivative(ResourceHandler r, int refNum, int row, int col) {
		Matrix[] weightedSum = new Matrix[layers]; //INDEX 0 IS INPUT LAYER, N IS OUTPUT, DIFFERENT MATRIX LENGTH AS INPUT ACTIVATION IS REQUIRED
		
		Matrix inputMatrix = r.getImageData(refNum);
		int expectedOutput = r.getLabel(refNum);
		
		//COMPUTE PORTION
		if (inputMatrix.getRows() != layerLengths[0])
			return 0.0d;
		
		Matrix resultant = inputMatrix;
		weightedSum[0] = reverseSigmoid(inputMatrix);
		
		for (int i = 0; i < layers-1; i++) {
			weightedSum[i+1] = Matrix.add(Matrix.dot(weights[i], resultant), biases[i]);
			resultant = Matrix.sigmoid(weightedSum[i+1]);
		}
		
		//BACKPROP PORTION
		Matrix I = errorVector(resultant, expectedOutput);
		
		Matrix[] deltaWeights = new Matrix[layers-1]; //Indexed, 0 input layer, n last, output layer
		Matrix[] deltaBiases = new Matrix[layers-1];
		
		for (int i = 0; i < layers-1; i++) {
			int realIndex = (layers-2)-i;
			
			Matrix P = Matrix.hadamard(I, Matrix.sigprime(weightedSum[realIndex+1]));
			deltaWeights[realIndex] = Matrix.dot(P, Matrix.transpose(Matrix.sigmoid(weightedSum[realIndex])));
			deltaBiases[realIndex] = P;
			
			I = Matrix.dot(Matrix.transpose(weights[realIndex]), P);
		}
		
		return deltaWeights[0].getValueAt(row, col);
	}
	
	public double numericalEstimation(ResourceHandler r, int refNum) {
		Random rand = new Random();
		int row = rand.nextInt(weights[0].getRows());
		int col = rand.nextInt(weights[0].getCols());
		
		double normalval = weights[0].getValueAt(row, col);
		
		//SET VALUE TO W + E
		weights[0].setValue(normalval + EPSILON, row, col);
		Matrix errorMatrix = errorVector(compute(r.getImageData(refNum)), r.getLabel(refNum));
		
		double error1 = 0;
		
		for (int i = 0; i < errorMatrix.getRows(); i++)
			error1 += 0.5*Math.pow(errorMatrix.getValueAt(i, 0), 2);
		
		//SET VALUE TO W - E
		weights[0].setValue(normalval - EPSILON, row, col);
		errorMatrix = errorVector(compute(r.getImageData(refNum)), r.getLabel(refNum));
		
		double error2 = 0;
		
		for (int i = 0; i < errorMatrix.getRows(); i++)
			error2 += 0.5*Math.pow(errorMatrix.getValueAt(i, 0), 2);
		
		//RETURN VALUE TO NORMAL
		weights[0].setValue(normalval, row, col);
		
		//COMPUTE NUMERICAL PARTIAL DERIVATIVE
		double partial = (error1 - error2)/(2*EPSILON);
		
		return partial;
	}
	
	public double numericalEstimation(ResourceHandler r, int refNum, int row, int col) {
		double normalval = weights[0].getValueAt(row, col);
		
		//SET VALUE TO W + E
		weights[0].setValue(normalval + EPSILON, row, col);
		Matrix errorMatrix = errorVector(compute(r.getImageData(refNum)), r.getLabel(refNum));
		
		double error1 = 0;
		
		for (int i = 0; i < errorMatrix.getRows(); i++)
			error1 += 0.5*Math.pow(errorMatrix.getValueAt(i, 0), 2);
		
		//SET VALUE TO W - E
		weights[0].setValue(normalval - EPSILON, row, col);
		errorMatrix = errorVector(compute(r.getImageData(refNum)), r.getLabel(refNum));
		
		double error2 = 0;
		
		for (int i = 0; i < errorMatrix.getRows(); i++)
			error2 += 0.5*Math.pow(errorMatrix.getValueAt(i, 0), 2);
		
		//RETURN VALUE TO NORMAL
		weights[0].setValue(normalval, row, col);
		
		//COMPUTE NUMERICAL PARTIAL DERIVATIVE
		double partial = (error1 - error2)/(2*EPSILON);
		
		return partial;
	}
	
	public void learn(ResourceHandler r, double epochs) {
		//Finished ResourceHandler. Test the network computing values, and then proceed to prepare for backpropagation
		//this method. It is given the number of epochs to test and etc, etc.
		int n = (int)Math.round(epochs*r.getDataLength());
		
		Matrix[] weightSum = new Matrix[layers-1];
		Matrix[] biasSum = new Matrix[layers-1];
		
		double average = 1.0d/(double)BATCH_LENGTH;
		
		for (int k = 0; k < layers-1; k++) {
			weightSum[k] = Matrix.zero(weights[k].getRows(), weights[k].getCols());
			biasSum[k] = Matrix.zero(biases[k].getRows(), biases[k].getCols());
		}
		
		for (int i = 0; i < n; i++) {
			
			if ((i % BATCH_LENGTH) == 0 && i != 0) {
				
				for (int j = 0; j < layers-1; j++) {
					weights[j] = Matrix.add(weights[j], Matrix.multiply(weightSum[j], -1*learningRate*average));
					biases[j] = Matrix.add(biases[j], Matrix.multiply(biasSum[j], -1*learningRate*average));
				}
				
				for (int k = 0; k < layers-1; k++) {
					weightSum[k] = Matrix.zero(weights[k].getRows(), weights[k].getCols());
					biasSum[k] = Matrix.zero(biases[k].getRows(), biases[k].getCols());
				}
			}
			
			int index = i % r.getDataLength();
			
			Matrix input = r.getImageData(index);
			int expectedOutput = r.getLabel(index);
			
			Matrix[][] result = backprop(input, expectedOutput);
			
			for (int j = 0; j < layers-1; j++) {
				weightSum[j]=Matrix.add(weightSum[j], result[j][0]);
				biasSum[j]=Matrix.add(biasSum[j], result[j][1]);
			}
		}
	}
	
	public void learn(ResourceHandler r, double epochs, BiConsumer<Integer, Double> update) {
		//Finished ResourceHandler. Test the network computing values, and then proceed to prepare for backpropagation
		//this method. It is given the number of epochs to test and etc, etc.
		int n = (int)Math.round(epochs*r.getDataLength());
		
		Matrix[] weightSum = new Matrix[layers-1];
		Matrix[] biasSum = new Matrix[layers-1];
		
		double average = 1.0d/(double)BATCH_LENGTH;
		
		for (int k = 0; k < layers-1; k++) {
			weightSum[k] = Matrix.zero(weights[k].getRows(), weights[k].getCols());
			biasSum[k] = Matrix.zero(biases[k].getRows(), biases[k].getCols());
		}
		
		for (int i = 0; i < n; i++) {
			
			if ((i % BATCH_LENGTH) == 0 && i != 0) {
				
				update.accept(i, epochs);
				
				for (int j = 0; j < layers-1; j++) {
					weights[j] = Matrix.add(weights[j], Matrix.multiply(weightSum[j], -1*learningRate*average));
					biases[j] = Matrix.add(biases[j], Matrix.multiply(biasSum[j], -1*learningRate*average));
				}
				
				for (int k = 0; k < layers-1; k++) {
					weightSum[k] = Matrix.zero(weights[k].getRows(), weights[k].getCols());
					biasSum[k] = Matrix.zero(biases[k].getRows(), biases[k].getCols());
				}
			}
			
			int index = i % r.getDataLength();
			
			Matrix input = r.getImageData(index);
			int expectedOutput = r.getLabel(index);
			
			Matrix[][] result = backprop(input, expectedOutput);
			
			for (int j = 0; j < layers-1; j++) {
				weightSum[j]=Matrix.add(weightSum[j], result[j][0]);
				biasSum[j]=Matrix.add(biasSum[j], result[j][1]);
			}
		}
	}
	
	private Matrix reverseSigmoid(Matrix A) {
		Matrix output = new Matrix(A.getRows(), A.getCols());
		
		for (int i = 0; i < A.getRows(); i++)
			for (int j = 0; j < A.getCols(); j++)
			{
				double val = A.getValueAt(i, j);
				double set = Math.log((1/val)-1)*-1;
				output.setValue(set, i, j);
			}
		
		return output;
	}
	
	private Matrix[][] backprop(Matrix inputMatrix, int expectedOutput) {
		
		Matrix[] weightedSum = new Matrix[layers]; //INDEX 0 IS INPUT LAYER, N IS OUTPUT, DIFFERENT MATRIX LENGTH AS INPUT ACTIVATION IS REQUIRED
		
		//COMPUTE PORTION
		if (inputMatrix.getRows() != layerLengths[0])
			return null;
		
		Matrix resultant = inputMatrix;
		weightedSum[0] = reverseSigmoid(inputMatrix);
		
		for (int i = 0; i < layers-1; i++) {
			weightedSum[i+1] = Matrix.add(Matrix.dot(weights[i], resultant), biases[i]);
			resultant = Matrix.sigmoid(weightedSum[i+1]);
		}
		
		//BACKPROP PORTION
		Matrix I = errorVector(resultant, expectedOutput);
		
		Matrix[] deltaWeights = new Matrix[layers-1]; //Indexed, 0 input layer, n last, output layer
		Matrix[] deltaBiases = new Matrix[layers-1];
		
		for (int i = 0; i < layers-1; i++) {
			int realIndex = (layers-2)-i;
			
			Matrix P = Matrix.hadamard(I, Matrix.sigprime(weightedSum[realIndex+1]));
			deltaWeights[realIndex] = Matrix.dot(P, Matrix.transpose(Matrix.sigmoid(weightedSum[realIndex])));
			deltaBiases[realIndex] = P;
			
			I = Matrix.dot(Matrix.transpose(weights[realIndex]), P);
		}
		
		Matrix[][] result = new Matrix[layers-1][2];
		
		for (int i = 0; i < layers-1; i++) {
			result[i][0] = deltaWeights[i];
			result[i][1] = deltaBiases[i];
		}
		
		return result;
	}
	
	private Matrix errorVector(Matrix A, int label) {
		
		Matrix error = new Matrix(A.getRows(), A.getCols());
		
		for (int i = 0; i < A.getRows(); i++) {
			double base = 0;
			if (i == label)
				base = 1;
			
			error.setValue(A.getValueAt(i, 0)-base, i, 0);
		}
		
		return error;
	}
	
	public Matrix[] getWeights() {
		return weights;
	}
	
	public Matrix[] getBiases() {
		return biases;
	}
	
	public double getLearningRate() {
		return learningRate;
	}
	
	public int[] getLayers() {
		return layerLengths;
	}
}

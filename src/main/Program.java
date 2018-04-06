package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.function.BiConsumer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Program {
	
	private Options options;
	private String[] args;
	private double learningRate;
	private int[] layers;
	private Scanner s;
	
	private NeuralNetwork network;
	private ResourceHandler rHandler;
	
	public Program(String[] _args) {
		args = _args;
		
		options = new Options();
		options.addOption("h", "help", false, "Displays this message.");
		options.addOption("l", "layers", true, "Layers to use and their respective lengths.");
		options.addOption("r", "rate", true, "Learning rate to use for the network.");
	}
	
	public void start() {
		CommandLineParser parser = new DefaultParser();
		CommandLine line = null;
		try {
			line = parser.parse(options, args);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		if (line.hasOption("h")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("nnet", options);
			return;
		}
		
		if (line.hasOption('l')) {
			String[] tokens = line.getOptionValue('l').split(":");
			layers = new int[tokens.length];
			
			for (int i = 0; i < tokens.length; i++) {
				layers[i] = Integer.parseInt(tokens[i]);
			}
			
			if (line.hasOption('r'))
				learningRate = Double.parseDouble(line.getOptionValue('r'));
			else
				learningRate = Double.NaN;
			
			run();
			
		} else {
			System.out.println("Layers argument is missing.");
			return;
		}
	}
	
	private void run() {
		s = new Scanner(System.in);
		
		if (!Double.isNaN(learningRate))
			network = new NeuralNetwork(layers.length, layers, learningRate);
		else
			network = new NeuralNetwork(layers.length, layers);
		
		network.init();
		rHandler = new ResourceHandler();
		try {
			rHandler.init(ResourceHandler.TRAIN_SET);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		System.out.print(String.format("Instantiated new NeuralNet with %d layers: ", layers.length));
		
		for (int i : layers)
			System.out.print(String.format("%d ", i));
		
		System.out.println();
		
		while (true) {
			try {
				System.out.print(">");
				String command = s.nextLine();
				
				String[] tokens = command.split(" ");
				
				if (tokens[0].equals("quit") || tokens[0].equals("exit"))
					break;
				
				if (tokens[0].equals("compute"))
					compute(Integer.parseInt(tokens[1]));
				
				if (tokens[0].equals("train"))
					train(Double.parseDouble(tokens[1]));
				
				if (tokens[0].equals("verify"))
					verifyAccuracy();
				
				if (tokens[0].equals("mcompute"))
					mcompute(tokens[1]);
				
				if (tokens[0].equals("rate"))
					changeRate(tokens[1]);
				
				if (tokens[0].equals("print"))
					print();
				
				if (tokens[0].equals("export"))
					export(tokens[1]);
				
				if (tokens[0].equals("import"))
					getImport(tokens[1]);
				
				if (tokens[0].equals("check"))
					check(tokens[1], tokens[2]);
				
				if (tokens[0].equals("set"))
					changeSet(tokens[1]);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void changeRate(String string) {
		double learningRate = Double.parseDouble(string);
		network.setLearningRate(learningRate);
	}
	
	
	private void changeSet(String string) {
		try {
			if (string.equals("test")) {
				rHandler.init(ResourceHandler.TEST_SET);
				System.out.println("Changed to Test Set.");
			} else if (string.equals("train")) {
				rHandler.init(ResourceHandler.TRAIN_SET);
				System.out.println("Changed to Train Set.");
			} else {
				System.out.println("Not a valid set");
			}
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	private void getImport(String _path) {
		String baseWName = "weightData%d.csv";
		String baseBName = "biasData%d.csv";
		
		try {
			File dir = new File(_path);
			
			Matrix[] weights = new Matrix[layers.length-1];
			Matrix[] biases = new Matrix[layers.length-1];
			
			for (int i = 0; i < layers.length-1; i++) {
				File weightFile = new File(dir, String.format(baseWName, i));
				File biasFile = new File(dir, String.format(baseBName, i));
				
				BufferedReader bWr = new BufferedReader(new FileReader(weightFile));
				BufferedReader bBr = new BufferedReader(new FileReader(biasFile));
				
				Matrix tempBias = new Matrix(layers[i+1], 1);
				Matrix tempWeight = new Matrix(layers[i+1], layers[i]);
				
				//COMPLETE USE LAYER LENGTH TO DETERMINE HOW MUCH LOOPS TO READLINE
				for (int j = 0; j < layers[i+1]; j++) {
					double bDouble = Double.parseDouble(bBr.readLine());
					tempBias.setValue(bDouble, j, 0);
					
					String[] tokens = bWr.readLine().split(",");
					for (int k = 0; k < tokens.length; k++) {
						double tmpD = Double.parseDouble(tokens[k]);
						tempWeight.setValue(tmpD, j, k);
					}
				}
				weights[i] = tempWeight;
				biases[i] = tempBias;
				
				bWr.close();
				bBr.close();
			}
			
			network.setWeights(weights);
			network.setBiases(biases);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void export(String _path) {
		try {
			File dir = new File(_path);
			
			if (dir.isDirectory()) {
				Matrix[] weights = network.getWeights();
				Matrix[] biases = network.getBiases();
				double _learningRate = network.getLearningRate();
				
				for (int i = 0; i < weights.length; i++) {
					File weight = new File(dir, String.format("weightData%d.csv", i));
					weight.createNewFile();
					File bias = new File(dir, String.format("biasData%d.csv", i));
					bias.createNewFile();
					
					BufferedWriter wos = new BufferedWriter(new FileWriter(weight));
					BufferedWriter bos = new BufferedWriter(new FileWriter(bias));
					
					for (int j = 0; j < weights[i].getRows(); j++)
						for (int k = 0; k < weights[i].getCols(); k++) {
							if ((k+1) == weights[i].getCols()) {
								wos.write(Double.toString(weights[i].getValueAt(j, k)));
								wos.newLine();
							} else {
								wos.write(Double.toString(weights[i].getValueAt(j, k)));
								wos.write(",");
							}
						}
					
					for (int j = 0; j < biases[i].getRows(); j++)
						for (int k = 0; k < biases[i].getCols(); k++) {
							if ((k+1) == biases[i].getCols()) {
								bos.write(Double.toString(biases[i].getValueAt(j, k)));
								bos.newLine();
							} else {
								bos.write(Double.toString(biases[i].getValueAt(j, k)));
								bos.write(",");
							}
						}
					
					wos.flush();
					bos.flush();
					wos.close();
					bos.close();
				}
				
				File info = new File(dir, "netconfig");
				info.createNewFile();
				
				BufferedWriter bw = new BufferedWriter(new FileWriter(info));
				bw.write("Network Layer configuration:");
				bw.newLine();
				
				for (int i = 0; i < layers.length; i++) {
					bw.write(Integer.toString(layers[i]));
					bw.newLine();
				}
				
				bw.write(String.format("Learning rate: %f", _learningRate));
				bw.flush();
				bw.close();
				
				System.out.println(String.format("Exported to path: %s", _path));
				
			} else {
				System.out.println("Input is not a directory.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void check(String _index, String coordinate) {
		int index = Integer.parseInt(_index);
		
		String[] tokens = coordinate.split(",");
		
		int row = Integer.parseInt(tokens[0]);
		int col = Integer.parseInt(tokens[1]);
		
		double numerical = network.numericalEstimation(rHandler, index, row, col);
		double analytical = network.analyticalDerivative(rHandler, index, row, col);
		double relative = Math.abs((analytical-numerical)/Math.max(analytical, numerical));
		
		System.out.println(String.format("Numerical Derivative: %g\nAnalytical Derivative: %g\nRelative Error:"
				+ " %g\nROW:%d COL: %d", numerical, analytical, relative, row, col));
	}
	
	private void print() {
		System.out.println("Network characteristics: ");
		System.out.print("Layers: ");
		
		for (int i = 0; i < layers.length; i++)
			System.out.print(String.format("%d ", layers[i]));
		
		System.out.println();
		
		System.out.println(String.format("Learning Rate: %f", learningRate));
	}
	
	private void mcompute(String values) {
		String[] tokens = values.split("-");
		
		int from = Integer.parseInt(tokens[0]);
		int to = Integer.parseInt(tokens[1]);
		
		for (int i = from; i <= to; i++) {
			compute(i);
		}
	}
	
	private void verifyAccuracy() {
		int count = 0;
		
		for (int i = 0; i < rHandler.getDataLength(); i++) {
			int expectedValue = rHandler.getLabel(i);
			Matrix result = network.compute(rHandler.getImageData(i));
			
			double highestVal = 0.0d;
			int index = -1;
			
			for (int j = 0; j < result.getRows(); j++) {
				double temp = result.getValueAt(j, 0);
				if (temp > highestVal) {
					highestVal = temp;
					index = j;
				}
			}
			
			if (index == expectedValue)
				count++;
		}
		
		double acc = (double)count / (double)rHandler.getDataLength();
		double percentage = Math.round(acc*10000)/100;
		
		System.out.println(String.format("Accuracy out of %d examples: %.2f", rHandler.getDataLength(), percentage));
	}
	
	private void train(double epochs) {
		
		BiConsumer<Integer, Double> cons = (pos, epoch) -> {
			if (pos % 1000 == 0) {
				System.out.println(String.format("Progress: %d/%.0f examples", pos, epoch*rHandler.getDataLength()));
			}
		};
		
		network.learn(rHandler, epochs, cons);
		System.out.println(String.format("Learning completed: %.3f epochs", epochs));
	}

	
	private void compute(int exampleNum) {
		int expectedValue = rHandler.getLabel(exampleNum);
		Matrix result = network.compute(rHandler.getImageData(exampleNum));
		
		double highestVal = 0.0d;
		int index = -1;
		
		for (int i = 0; i < result.getRows(); i++) {
			double temp = result.getValueAt(i, 0);
			if (temp > highestVal) {
				highestVal = temp;
				index = i;
			}
		}
		
		System.out.println(String.format("Network Result: %d, Label Value: %d", index, expectedValue));
	}

	public static void main(String[] _args) {
		new Program(_args).start();
	}

}

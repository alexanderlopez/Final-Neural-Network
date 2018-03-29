package main;

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
			
			run();
			
		} else {
			System.out.println("Layers argument is missing.");
			return;
		}
	}
	
	private void run() {
		System.out.println();
	}

	public static void main(String[] _args) {
		new Program(_args).start();
	}

}

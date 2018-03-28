package main;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class Main {

//	static final int[] LAYER_LENGTHS = { 784, 800, 10 };
	static final int[] LAYER_LENGTHS = { 784, 40, 40, 30, 10 };
	static final int LAYERS = 5;
	
	private JFrame frame;
	private JTextField txtIndex;
	
	private ResourceHandler rHandler;
	private NeuralNetwork network;
	private JTextField txtEpochs;
	
	private boolean isTestSet;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Main window = new Main();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Main() {
		rHandler = new ResourceHandler();
		try {
			rHandler.init(ResourceHandler.TRAIN_SET);
			isTestSet = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
		network = new NeuralNetwork(LAYERS, LAYER_LENGTHS);
		network.init();
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] {150, 150, 150, 0};
		gridBagLayout.rowHeights = new int[] {87, 87, 87, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0};
		frame.getContentPane().setLayout(gridBagLayout);
		
		//MANUAL
		JLabel lblResult = new JLabel("");
		
		JButton btnPrevious = new JButton("Previous");
		GridBagConstraints gbc_btnPrevious = new GridBagConstraints();
		gbc_btnPrevious.fill = GridBagConstraints.BOTH;
		gbc_btnPrevious.insets = new Insets(0, 0, 5, 5);
		gbc_btnPrevious.gridx = 0;
		gbc_btnPrevious.gridy = 0;
		frame.getContentPane().add(btnPrevious, gbc_btnPrevious);
		
		JButton btnCompute = new JButton("Compute");
		btnCompute.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int pos = Integer.parseInt(txtIndex.getText().toString());
				Matrix result = network.compute(rHandler.getImageData(pos));
				
				int largestIndex = -1;
				double largestVal = 0;
				
				for (int i = 0; i < result.getRows(); i++) {
					double val = result.getValueAt(i, 0);
					if (result.getValueAt(i, 0) > largestVal) {
						largestVal = val;
						largestIndex = i;
					}
				}
				
				lblResult.setText("Returned: " + largestIndex + " ACT: " + rHandler.getLabel(pos));
				//System.out.println("RET: " + largestIndex + " VAL: " + largestVal);
				System.out.println(Matrix.printString(result));
			}
		});
		GridBagConstraints gbc_btnCompute = new GridBagConstraints();
		gbc_btnCompute.fill = GridBagConstraints.BOTH;
		gbc_btnCompute.insets = new Insets(0, 0, 5, 5);
		gbc_btnCompute.gridx = 1;
		gbc_btnCompute.gridy = 0;
		frame.getContentPane().add(btnCompute, gbc_btnCompute);
		
		JButton btnNext = new JButton("Next");
		GridBagConstraints gbc_btnNext = new GridBagConstraints();
		gbc_btnNext.fill = GridBagConstraints.BOTH;
		gbc_btnNext.insets = new Insets(0, 0, 5, 5);
		gbc_btnNext.gridx = 2;
		gbc_btnNext.gridy = 0;
		frame.getContentPane().add(btnNext, gbc_btnNext);
		
		JLabel lblImage = new JLabel("");
		GridBagConstraints gbc_lblImage = new GridBagConstraints();
		gbc_lblImage.fill = GridBagConstraints.BOTH;
		gbc_lblImage.insets = new Insets(0, 0, 5, 5);
		gbc_lblImage.gridx = 0;
		gbc_lblImage.gridy = 1;
		frame.getContentPane().add(lblImage, gbc_lblImage);
		
		//MANUAL
		btnNext.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				txtIndex.setText(Integer.toString(Integer.parseInt(txtIndex.getText())+1));
				lblImage.setIcon(new ImageIcon(rHandler.getImage(
						Integer.parseInt(txtIndex.getText()))));
			}
		});
		
		JButton btnGo = new JButton("Go");
		btnGo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				lblImage.setIcon(new ImageIcon(rHandler.getImage(
						Integer.parseInt(txtIndex.getText()))));
			}
		});
		GridBagConstraints gbc_btnGo = new GridBagConstraints();
		gbc_btnGo.fill = GridBagConstraints.BOTH;
		gbc_btnGo.insets = new Insets(0, 0, 5, 5);
		gbc_btnGo.gridx = 1;
		gbc_btnGo.gridy = 1;
		frame.getContentPane().add(btnGo, gbc_btnGo);
		
		txtIndex = new JTextField();
		txtIndex.setText("0");
		GridBagConstraints gbc_txtIndex = new GridBagConstraints();
		gbc_txtIndex.fill = GridBagConstraints.BOTH;
		gbc_txtIndex.insets = new Insets(0, 0, 5, 5);
		gbc_txtIndex.gridx = 2;
		gbc_txtIndex.gridy = 1;
		frame.getContentPane().add(txtIndex, gbc_txtIndex);
		txtIndex.setColumns(10);
		
		JButton btnTrain = new JButton("Train");
		btnTrain.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int epochs = Integer.parseInt(txtEpochs.getText());
				network.learn(rHandler, epochs);
				System.out.println("DONE_LEARNING " + epochs + " EPOCHS");
			}
		});
		GridBagConstraints gbc_btnTrain = new GridBagConstraints();
		gbc_btnTrain.fill = GridBagConstraints.BOTH;
		gbc_btnTrain.insets = new Insets(0, 0, 5, 5);
		gbc_btnTrain.gridx = 0;
		gbc_btnTrain.gridy = 2;
		frame.getContentPane().add(btnTrain, gbc_btnTrain);
		
		GridBagConstraints gbc_lblResult = new GridBagConstraints();
		gbc_lblResult.anchor = GridBagConstraints.EAST;
		gbc_lblResult.insets = new Insets(0, 0, 5, 5);
		gbc_lblResult.gridx = 1;
		gbc_lblResult.gridy = 2;
		frame.getContentPane().add(lblResult, gbc_lblResult);
		
		txtEpochs = new JTextField();
		txtEpochs.setText("1");
		GridBagConstraints gbc_txtEpochs = new GridBagConstraints();
		gbc_txtEpochs.insets = new Insets(0, 0, 5, 5);
		gbc_txtEpochs.fill = GridBagConstraints.BOTH;
		gbc_txtEpochs.gridx = 2;
		gbc_txtEpochs.gridy = 2;
		frame.getContentPane().add(txtEpochs, gbc_txtEpochs);
		txtEpochs.setColumns(10);
		
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenuItem mntmChangeToTest = new JMenuItem("Change to Test Set");
		mntmChangeToTest.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if (isTestSet) {
						rHandler.init(ResourceHandler.TRAIN_SET);
						mntmChangeToTest.setText("Change to Test Set");
					} else if (!isTestSet) {
						rHandler.init(ResourceHandler.TEST_SET);
						mntmChangeToTest.setText("Change to Train Set");
					}
					isTestSet = !isTestSet;
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		mnFile.add(mntmChangeToTest);
		
		JMenuItem mntmVerifyAccuracy = new JMenuItem("Verify Accuracy");
		mntmVerifyAccuracy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int count = 0;
				int length = rHandler.getDataLength();
				
				for (int i = 0; i < rHandler.getDataLength(); i++) {
					Matrix res = network.compute(rHandler.getImageData(i));
					
					int index = -1;
					double maxVal = 0;
					for (int j = 0; j < res.getRows(); j++) {
						double val = res.getValueAt(j, 0);
						if (val > maxVal) {
							maxVal = val;
							index = j;
						}
					}
					
					if (index == rHandler.getLabel(i))
						count++;
				}
				
				double accuracy = (double)count/(double)length;
				double rAccuracy = (double)Math.round(accuracy*10000)/100.0d;
				
				lblResult.setText("Accuracy: " + rAccuracy + "%");
			}
		});
		mnFile.add(mntmVerifyAccuracy);
		
		JMenuItem mntmComputePartial = new JMenuItem("Compute Partial");
		mntmComputePartial.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int index = Integer.parseInt(txtIndex.getText());
				
				String[] tokens = txtEpochs.getText().split(",");
				
				int row = Integer.parseInt(tokens[0]);
				int col = Integer.parseInt(tokens[1]);
				
				double numerical = network.numericalEstimation(rHandler, index, row, col);
				double analytical = network.analyticalDerivative(rHandler, index, row, col);
				
				System.out.println(numerical + " ROW: " + row + " COL: " + col);
				System.out.println(analytical);
				System.out.println("REL: " + Math.abs(analytical-numerical)/Math.max(analytical, numerical));
			}
		});
		mnFile.add(mntmComputePartial);
	}

}

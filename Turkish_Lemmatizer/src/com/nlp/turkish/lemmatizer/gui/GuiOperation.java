package com.nlp.turkish.lemmatizer.gui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.nlp.turkish.lemmatizer.*;

public class GuiOperation extends JFrame{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2281722270360700522L;
	private TextField ambiguousTextField;
	private JButton runButton;
	private JButton setStartButton;
	private JFileChooser fileChooser;
	private JButton browseButton;
	private JButton clearButton;
	private JButton runWithCacheButton;
	private JButton clearCacheButton;
	private JTextArea testTextField;
	private Lemmatization lemma = null;
	private CorpusOperation operation = null;
	public String ambiguousWord = null;
	private ArrayList<String> testSentences = null;
	/*
		More than one test sentence can be added.
	*/
	int response = -1;
	public static void main(String[] args) {
		GuiOperation g = new GuiOperation();
		g.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public GuiOperation(){
		super();
		init();
		setTitle("Lemmatizer");
		setSize(new Dimension(360,240));
		setEnabled(true);
		setVisible(true);
		setComponents();
		setLayout(new GridLayout(4,2,5,10));
		setMaximizedBounds(new Rectangle(new Dimension(360,360)));
		setResizable(false);
	}
	private void init() {
		this.fileChooser = new JFileChooser();
		this.ambiguousTextField = new TextField();
		this.testTextField = new JTextArea();
		this.testTextField.setText("Test Sentence");
		this.ambiguousTextField.setText("Ambiguous Word");
		this.testTextField.setLineWrap(true);
		this.testTextField.setWrapStyleWord(true);
		this.clearCacheButton = new JButton();
		this.clearCacheButton.setText("Clear Cache");
		this.clearCacheButton.setEnabled(new File("src/cachedTrain").exists());
		this.clearCacheButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				File f = new File("src/cachedTrain");
				File txt = new File("src/cachedTrain.txt");
				if(f!=null && txt!=null){
					if(f.delete() && txt.delete()){
						clearCacheButton.setEnabled(false);
						runWithCacheButton.setEnabled(false);
					}
				}
			}
		});
		this.runWithCacheButton = new JButton();
		this.runWithCacheButton.setText("Run With Cache");
		this.runWithCacheButton.setEnabled(false);
		this.runWithCacheButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					lemma.testTokenizerAPICall(testTextField.getText().toLowerCase());
					lemma.morphSpecialParse();
					operation.readAnalyzedTrainFile("src/cachedTrain");
					lemma.p.setAmbiguousTrainSentences(operation.ambiguousTrainSentences);
					lemma.p.scanForAmbiguousWords();
					lemma.p.scanForWordCount();
					lemma.p.calculatePMIValuesOfAmbiguousWords(Lemmatization.ambiguousList);
					double max = -Double.MAX_VALUE;
					int index = -1;
					for(int i = 0;i<lemma.p.pmiArray.length;i++){
						if(max<lemma.p.pmiArray[i]){
							max = lemma.p.pmiArray[i];
							index = i; 
						}
					}
					if(index != -1){
						JOptionPane.showMessageDialog(getContentPane(),
								Lemmatization.ambiguousWordArray[index],
							    "Result",
							    JOptionPane.PLAIN_MESSAGE);
					}else{
						JOptionPane.showMessageDialog(getContentPane(),
								"No Disambiguation",
							    "Result",
							    JOptionPane.OK_OPTION);
					}
				} catch (ClassNotFoundException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		this.browseButton = new JButton();
		this.browseButton.setText("Browse");
		this.browseButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				response = fileChooser.showOpenDialog(getContentPane());
				if(response  == JFileChooser.APPROVE_OPTION){
					if(fileChooser.getSelectedFile() != null){
						lemma.init(fileChooser.getSelectedFile());
					}
				}
			}
		});
		this.runButton = new JButton();
		this.runButton.setText("Run");
		this.runButton.setEnabled(false);
		this.runButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(response!=-1){
					try {
						operation.trainFromAnalyzedRaw(fileChooser.getSelectedFile());
						lemma.p.setAmbiguousTrainSentences(operation.ambiguousTrainSentences);
						lemma.p.scanForAmbiguousWords();
						lemma.p.scanForWordCount();
						lemma.p.calculatePMIValuesOfAmbiguousWords(Lemmatization.ambiguousList);
						double max = -Double.MAX_VALUE;
						int index = -1;
						for(int i = 0;i<lemma.p.pmiArray.length;i++){
							if(max<lemma.p.pmiArray[i]){
								max = lemma.p.pmiArray[i];
								index = i; 
							}
							System.out.println(Lemmatization.ambiguousWordArray[i] + ": " + lemma.p.pmiArray[i]);
						}
						if(index != -1){
							JOptionPane.showMessageDialog(getContentPane(),
									Lemmatization.ambiguousWordArray[index],
								    "Result",
								    JOptionPane.PLAIN_MESSAGE);
							operation.writeAnalyzedTrainFile("src/cachedTrain");
							operation.writeMorphAnalyzedTrainFile("src/cachedTrain.txt");
							runWithCacheButton.setEnabled(true);
							clearCacheButton.setEnabled(true);
						}else{
							JOptionPane.showMessageDialog(getContentPane(),
									"No Disambiguation",
								    "Result",
								    JOptionPane.OK_OPTION);
						}
					} catch (IOException e) {
						JOptionPane.showMessageDialog(getContentPane(),
							    e.getMessage(),
							    "Error",
							    JOptionPane.ERROR_MESSAGE);
						e.printStackTrace();
					}
				}else{
					JOptionPane.showMessageDialog(getContentPane(),
						    "Please choose a file.",
						    "Warning",
						    JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		this.setStartButton = new JButton();
		this.setStartButton.setText("Start");
		this.setStartButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!testTextField.getText().equalsIgnoreCase("Test Sentence")){
					if(!ambiguousTextField.getText().equalsIgnoreCase("Ambiguous Word")){
						if(testTextField.getText().charAt(testTextField.getText().length()-1) == '.' || 
							testTextField.getText().charAt(testTextField.getText().length()-1) == '!' ||
							testTextField.getText().charAt(testTextField.getText().length()-1) == '?'){
							try {
								lemma = new Lemmatization(ambiguousTextField.getText().toLowerCase().trim());
								lemma.testTokenizerAPICall(testTextField.getText().toLowerCase());
								lemma.morphSpecialParse();
								operation = new CorpusOperation();
								operation.readCorpus("src/corpus.itutagged", Lemmatization.ambiguousWord);
								operation.writeCorpus("src/rawCorpus.txt");
								runButton.setEnabled(true);
							} catch (IOException e1) {
								JOptionPane.showMessageDialog(getContentPane(),
									    e1.getMessage(),
									    "Error",
									    JOptionPane.ERROR_MESSAGE);
								e1.printStackTrace();
							}
						}else{
							JOptionPane.showMessageDialog(getContentPane(),
								    "Please add punctuation to the end of test sentence.",
								    "Warning",
								    JOptionPane.WARNING_MESSAGE);
						}
					}else{
						JOptionPane.showMessageDialog(getContentPane(),
							    "Test sentence must be added.",
							    "Error",
							    JOptionPane.ERROR_MESSAGE);
					}
				}else{
					JOptionPane.showMessageDialog(getContentPane(),
						    "Ambiguous word must be added.",
						    "Error",
						    JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		this.clearButton = new JButton();
		this.clearButton.setText("Clear");
		this.clearButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				testTextField.setText("Test Sentence");
				ambiguousTextField.setText("Ambiguous Word");
			}
		});
		
	}

	

	private void setComponents(){
		add(ambiguousTextField);
		JScrollPane areaScrollPane = new JScrollPane(testTextField);
		areaScrollPane.setVerticalScrollBarPolicy(
		                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		add(areaScrollPane);
		add(browseButton);
		add(clearButton);
		add(setStartButton);
		add(runButton);
		add(runWithCacheButton);
		add(clearCacheButton);
	}
}

package com.nlp.turkish.lemmatizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public class PointwiseMI {
	

	private HashMap<String, Integer> wordCounts;
	
	private ArrayList<HashMap<String, Integer>> ambiguousWordHashes;
	
	private int numberOfWords = 0;
	
	private ArrayList<ArrayList<ArrayList<String>>> ambiguousTrainSentences;
	
	public double[] pmiArray;
	
	private File file;
	
	public PointwiseMI(File f) {
		wordCounts = new HashMap<String, Integer>();
		ambiguousTrainSentences = new ArrayList<ArrayList<ArrayList<String>>>(1);
		ambiguousWordHashes = new ArrayList<HashMap<String, Integer>>(1);
		for(int i = 0;i<Lemmatization.ambiguousWordArray.length;i++){
			ambiguousWordHashes.add(new HashMap<String, Integer>());
		}
		file = f;
		pmiArray = new double[Lemmatization.ambiguousWordArray.length];
		for(int i = 0;i<pmiArray.length;i++){
			pmiArray[i] = 0.0;
		}
		/*readTrainFile(Lemmatization.ambiguousWord);
		scanForAmbiguousWords();
		scanForWordCount();*/
	}
	
	public ArrayList<ArrayList<ArrayList<String>>> getAmbiguousTrainSentences() {
		return ambiguousTrainSentences;
	}


	public void setAmbiguousTrainSentences(
			ArrayList<ArrayList<ArrayList<String>>> ambiguousTrainSentences) {
		this.ambiguousTrainSentences = ambiguousTrainSentences;
	}
	
	public void readTrainFile(String ambiguousWord){
		BufferedReader br = null;
		try {
 
			String sCurrentLine;
 
			br = new BufferedReader(new FileReader(file));
			
			ArrayList<ArrayList<String>> list = new ArrayList<ArrayList<String>>();
			ArrayList<String> subList = new ArrayList<String>();
			ArrayList<String> k = new ArrayList<String>();
			
			while ((sCurrentLine = br.readLine()) != null) {
				if(sCurrentLine.length() > 1){
					sCurrentLine = sCurrentLine.toLowerCase();
					if(sCurrentLine.charAt(0) == ':' || sCurrentLine.charAt(sCurrentLine.length()-1) == ':')
					{
						continue;
					}
					String[] array = sCurrentLine.split("\\s|\\+");
					if(!array[1].contains("punc") && !array[0].contains("?")){
						if(array[0].startsWith(ambiguousWord)){
							if(!k.contains(array[0])){
								k.add(array[0]);
								subList.add(array[0]);
							}	
						}
						else if(!subList.contains(array[0].toLowerCase())){
							subList.add(array[0]);
						}
					}
					if((array[0].contains(".") || array[0].contains("!") || array[0].contains("?"))){
						if(k.size()>0){
							int index = Lemmatization.getIndexOfAmbiguousWord(list);
							if(index!=-1){
								ArrayList<ArrayList<String>> sentence_list = new ArrayList<ArrayList<String>>(1);
								for(int j = 0;j<list.get(index).size();j++){
									for(int i=0;i<list.size();i++){
										if(i != index){
											sentence_list.add(list.get(i));
										}else{
											ArrayList<String> tmp = new ArrayList<String>(1);
											tmp.add(list.get(i).get(j));
											sentence_list.add(new ArrayList<String>(tmp));
										}
									}
									ambiguousTrainSentences.add(new ArrayList<ArrayList<String>>(sentence_list));
									sentence_list.clear();
								}
								list.clear();
								subList.clear();
								k.clear();
							}
						}
					}
				}else{
					if(!subList.isEmpty()){
						list.add(new ArrayList<>(subList));
						subList.clear();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public void scanForWordCount(){
		for(ArrayList<ArrayList<String>> sentence : ambiguousTrainSentences){
			for(ArrayList<String> words : sentence){
				for(String word : words){
					Integer count = wordCounts.get(word);
					if(count != null){
						wordCounts.put(word, ++count);
					}else{
						wordCounts.put(word, 1);
					}
				}
			}
		}
		for (Entry<String, Integer> count : wordCounts.entrySet()) {
			numberOfWords += count.getValue();
		}
	}
	
	
	public void scanForAmbiguousWords(){
		for(ArrayList<ArrayList<String>> sentence : ambiguousTrainSentences){
			for(int i = 0;i<Lemmatization.ambiguousWordArray.length;i++)
				if(isContain(sentence, Lemmatization.ambiguousWordArray[i])){
					for(ArrayList<String> words : sentence){
						for(String word : words){
							Integer count = ambiguousWordHashes.get(i).get(word);
							if(count != null){
								ambiguousWordHashes.get(i).put(word, ++count);
							}else{
								ambiguousWordHashes.get(i).put(word, 1);
							}
						}
					}
				}
		}
	}
	
	private boolean isContain(ArrayList<ArrayList<String>> sentence, String word){
		boolean contains = false;
		for(ArrayList<String> words : sentence){
			if(words.contains(word)){
				contains = true;
				break;
			}
		}
		return contains;
	}
	
	
	public double calculatePMIForGivenAmbiguousWord(String word, int index){
		double conditionProb;
		double wordProb;
		Integer  countInHashes = ambiguousWordHashes.get(index).get(word);
		if(countInHashes != null){
			conditionProb = (double)countInHashes.intValue() / (double)ambiguousWordHashes.get(index).get(Lemmatization.ambiguousWordArray[index]).intValue();
		}else{
			conditionProb = Double.MIN_VALUE;
		}
		Integer wordCount = wordCounts.get(word);
		if(wordCount != null){
			wordProb = (double)wordCount.intValue() / (double)numberOfWords;
		}else{
			wordProb = Double.MIN_VALUE;
			return -10;
		}
		return Math.log(conditionProb / wordProb) / Math.log(2);
	}
	
	
	public int getIndex(ArrayList<ArrayList<String>> sentence, String word){
		int index = -1;
		for(int i = 0;i<sentence.size();i++){
			if(sentence.get(i).contains(word))
			{
				index = i;
				break;
			}
		}
		return index;
	}
	
	public void calculatePMIValuesOfAmbiguousWords(ArrayList<ArrayList<ArrayList<String>>> testSentences){
		//sentence parameter points test sentence 
		double toRet = 0;
		if(testSentences.size()>0){
			for(int i = 0;i<Lemmatization.ambiguousWordArray.length;i++){
				int index = getIndex(testSentences.get(i), Lemmatization.ambiguousWordArray[i]);
				if(index != -1){
					for(int k = index-1;k>=0 && k>index-5;k--){
						for(String word : testSentences.get(i).get(k))
							toRet += calculatePMIForGivenAmbiguousWord(word, i);
					}
					for(int k = index+1;k<testSentences.get(i).size() && k<index+5;k++){
						for(String word : testSentences.get(i).get(k))
							toRet += calculatePMIForGivenAmbiguousWord(word, i);
					}
					pmiArray[i] = toRet;
				}else{
					pmiArray[i] = Double.MAX_VALUE;
				}
			}
		}
	}
	
	public void printAmbiguousWordsValues(){
		for(int i = 0;i<Lemmatization.ambiguousWordArray.length;i++){
			System.out.println(Lemmatization.ambiguousWordArray[i] + "(" + ambiguousWordHashes.get(i).get(Lemmatization.ambiguousWordArray[i]) + "):");
			for(Entry<String, Integer> entry : ambiguousWordHashes.get(i).entrySet()){
				if(!entry.getKey().equals(Lemmatization.ambiguousWordArray[i])){
					System.out.println(entry.getKey() + ":" + entry.getValue());
				}
			}
		}
	}
}

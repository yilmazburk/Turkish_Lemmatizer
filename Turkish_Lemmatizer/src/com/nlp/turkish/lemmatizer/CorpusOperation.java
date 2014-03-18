package com.nlp.turkish.lemmatizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

public class CorpusOperation {
	
	private ArrayList<ArrayList<String>> sentence;
	private ArrayList<ArrayList<String>> analyzedSentences;
	public ArrayList<ArrayList<ArrayList<String>>> ambiguousTrainSentences;
	
	public CorpusOperation(){
		sentence = new ArrayList<ArrayList<String>>(0);
		analyzedSentences = new ArrayList<ArrayList<String>>(0);
		ambiguousTrainSentences = new ArrayList<ArrayList<ArrayList<String>>>(0);
		
		//readCorpus("src/corpus.itutagged", this.ambiguousWord);
		//writeCorpus("src/raw3.txt");
	}
	
	/*public static void main(String[] args) {
		CorpusOperation c = new CorpusOperation("kalem");
		//c.trainFromAnalyzedRaw("src/morphAnalyzedRaw.txt");
		//c.writeTrain("src/train3.txt");
	}*/
	
	public boolean contains(String word){
		for(int i = 0 ;i<Lemmatization.ambiguousWordArray.length;i++){
			if(word.equalsIgnoreCase(Lemmatization.ambiguousWordArray[i])){
				return true;
			}
		}
		return false;
	}
	
	public int getIndexOfAmbiguousWord(ArrayList<ArrayList<String>> list){
		int index = -1;
		for(int i = 0;i<list.size();i++)
			for(int j=0;j<list.get(i).size();j++){
				if(list.get(i).get(j).contains(Lemmatization.ambiguousWord)){
					index = i;
					i = list.size();
					break;
				}
			}
		return index;
	}
	
	public void trainFromAnalyzedRaw(File file){
		/*
		 * Reading morphanalyzed raw file and storing info in arraylist
		 * */
		BufferedReader br = null;
		try {
 
			String sCurrentLine;
			if(file!=null){
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
							if(contains(array[0])){
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
							if(k.size()==1){
								ambiguousTrainSentences.add(new ArrayList<ArrayList<String>>(list));
							}
							list.clear();
							subList.clear();
							k.clear();
						}
					}else{
						if(!subList.isEmpty()){
							list.add(new ArrayList<>(subList));
							subList.clear();
						}
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
	
	public void trainFromMorphAnalyzedRaw(String path){
		/*
		 * Reading morphanalyzed raw file and storing info in arraylist
		 * */
		File f = null;
		BufferedReader r=null;
		try{
			f = new File(path);
			if(f != null){
			r = new BufferedReader(new FileReader(f));
				String line = "";
				boolean valid = false;
				ArrayList<String> list = new ArrayList<String>();
				ArrayList<String> tmpList = new ArrayList<String>();
				while((line = r.readLine())!=null){
					if(line.length() > 1){
						if(line.charAt(line.length()-1) == ':'){
							line = line.toLowerCase();
							if(line.contains(Lemmatization.ambiguousWord)){
								String tmpString = line.split("\\:")[0];
								while((line = r.readLine()).length()>1){
									String[] array = line.split("\\+");
									if(contains(array[0])){
										if(!tmpList.contains(array[0]))
											tmpList.add(array[0]);
									}
								}
								if(tmpList.size() == 1){
									valid = true;
									list.add(new String(tmpString));
								}
								tmpList.clear();
							}else{
								if(line.charAt(0) != ':'){
									String[] array = line.split("\\:");
									list.add(array[0]);
								}else{
									list.add(":");
								}
							}
						} 
						if(line.length()>2 && (line.charAt(0) == '.' || line.charAt(0) == '!' || line.charAt(0) == '?')){
							if(valid){
								analyzedSentences.add(new ArrayList<String>(list));
								valid = false;
							}
							list.clear();
						}
					}
				}
				r.close();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	
	public void readCorpus(String path,String ambiguousWord){
		/*
		 * Reading tagged corpus file and storing info in arraylist
		 * */
		File f = new File(path);
		try{
			FileReader reader = new FileReader(f);
			BufferedReader r = new BufferedReader(reader);
			String line = "";
			ArrayList<String> list = new ArrayList<String>();
			while((line = r.readLine())!=null){
				if(!line.contains("<") && line.length()!=0){
					String[] array = line.split("\\s|\\+");
					if(!list.contains(array[0]))
						list.add(array[0]);
					if(array[0].matches("\\.|\\!|\\?")){
						boolean contain = false;
						for (String string : list) {
							if(string.startsWith(ambiguousWord))
							{
								contain = true;
								break;
							}
						}
						if(contain){
							ArrayList<String> s = new ArrayList<>(list);
							sentence.add(s);
						}
						list.clear();
					}
				}
			}
			r.close();
			reader.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void writeCorpus(String path){
		/*
		 * Writing sentences which are parsed from corpus to file 
		 * */
		File f = new File(path);
		try{
			PrintWriter w = new PrintWriter(new FileWriter(f));
			for (ArrayList<String> list : sentence) {
				for (String string : list) {
					w.print(string + " ");
					if(string.matches("\\.|\\!|\\?")){
						w.println();
					}
				}
			}
			w.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	
	public void writeTrain(String path){
		 //*
		 //* Writing analyzed sentences to file
		 //*
		File f = new File(path);
		try{
			PrintWriter w = new PrintWriter(new FileWriter(f));
			for (ArrayList<String> list : analyzedSentences) {
				for (String string : list) {
					w.print(string + " ");
				}
				w.println();
			}
			w.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void writeAnalyzedTrainFile(String path) throws IOException{
 		//Writing ArrayList like a serialized class object
 		FileOutputStream fos = null;
 		ObjectOutputStream oos = null;
		fos = new FileOutputStream(new File(path));
		oos = new ObjectOutputStream(fos);
		oos.writeObject(ambiguousTrainSentences);
		oos.close();
		fos.close();
 	}
	
	public void writeMorphAnalyzedTrainFile(String path){
		File f = new File(path);
		try{
			PrintWriter w = new PrintWriter(new FileWriter(f));
			for(ArrayList<ArrayList<String>> sentence : ambiguousTrainSentences){
				for(ArrayList<String> words : sentence){
					for(String word : words){
						w.print(word + " ");
					}
					w.print("| ");
				}
				w.println();
			}
			w.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public void readAnalyzedTrainFile(String path) throws IOException, ClassNotFoundException{
 		/*
			Reading Arraylist from ObjectInputStream like writeAnalyzedTrainFile function
 		*/
 		FileInputStream ios = null;
 		ObjectInputStream ois = null;
 		ios = new FileInputStream(new File(path));
 		ois = new ObjectInputStream(ios);
 		ambiguousTrainSentences = (ArrayList<ArrayList<ArrayList<String>>>) ois.readObject();
 		ois.close();
 		ios.close();
 	}
	
 	
 	
	
}

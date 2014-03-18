package com.nlp.turkish.lemmatizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;


@SuppressWarnings("deprecation")
public class Lemmatization {
	public PointwiseMI p;
	private String test = "";
	public static String ambiguousWord;
	public static String[] ambiguousWordArray;
	public static ArrayList<ArrayList<ArrayList<String>>> ambiguousList;
 	/*public static void main(String[] args) {
		Lemmatization l = new Lemmatization("Kadın");
		try {
			//l.trainMorphologicalAnalyzerAPICall();
			l.testTokenizerAPICall();
			l.morphSpecialParse();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		l.p.calculatePMIValuesOfAmbiguousWords(ambiguousList);
		double max = -Double.MAX_VALUE;
		int index = -1;
		for(int i = 0;i<l.p.pmiArray.length;i++){
			if(max<l.p.pmiArray[i]){
				max = l.p.pmiArray[i];
				index = i; 
			}
		}
		if(index != -1){
			System.out.println(ambiguousWordArray[index]);
		}else{
			System.out.println("No Disambiguation");
		}
	}*/
	
	public Lemmatization(String word){
		ambiguousList = new ArrayList<ArrayList<ArrayList<String>>>(0);
		ambiguousWordArray = wordBasedMorphologicalAnalyzer(word);
		if(ambiguousWordArray.length == 0){
			System.err.println("Word is not ambiguous.");
			System.exit(-1);
		}
		ambiguousWord = findMinLemma(ambiguousWordArray);
	}
	
	public void init(File file){
		p = new PointwiseMI(file);
	}
	
 	@SuppressWarnings("resource")
	public void testTokenizerAPICall() throws IOException{
 		/*
			Call to tokenize test sentence with API
 		*/
 		Scanner in = null;
		try {
			in = new Scanner(new File("src/test.txt"));
			String line=in.nextLine();
			HttpClient 		client 		= new DefaultHttpClient();
			HttpPost 		post 		= new HttpPost("http://tools.nlp.itu.edu.tr/SimpleApi");
			List<NameValuePair> 	parameters 	= new ArrayList<NameValuePair>(3);

			parameters.add(new BasicNameValuePair("tool", "tokenizer"));
			parameters.add(new BasicNameValuePair("input", line));
			parameters.add(new BasicNameValuePair("token", "******************************"));
			post.setEntity(new UrlEncodedFormEntity(parameters, "UTF-8"));
			HttpResponse resp = client.execute(post);

			test = EntityUtils.toString(resp.getEntity());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			if(in != null){
				in.close();
			}
		}
 	}
 	
 	@SuppressWarnings("resource")
	public void testTokenizerAPICall(String line) throws IOException{
 		/*
			Call to tokenize test sentence with API
 		*/
		try {
			HttpClient 		client 		= new DefaultHttpClient();
			HttpPost 		post 		= new HttpPost("http://tools.nlp.itu.edu.tr/SimpleApi");
			List<NameValuePair> 	parameters 	= new ArrayList<NameValuePair>(3);

			parameters.add(new BasicNameValuePair("tool", "tokenizer"));
			parameters.add(new BasicNameValuePair("input", line));
			parameters.add(new BasicNameValuePair("token", "******************************"));
			post.setEntity(new UrlEncodedFormEntity(parameters, "UTF-8"));
			HttpResponse resp = client.execute(post);

			test = EntityUtils.toString(resp.getEntity());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
 	}
 	
	
 	
 	@SuppressWarnings("resource")
	public void trainMorphologicalAnalyzerAPICall() throws IOException {
 		/*
			Call to morphologically analyze raw data with API
 		*/
 		BufferedReader reader = null;
 		try{
 			reader = new BufferedReader(new FileReader(new File("src/raw.txt")));
 			String line = "";
 			ArrayList<ArrayList<String>> analyzedSentences = new ArrayList<ArrayList<String>>();
 			while((line=reader.readLine()) != null){
 				StringTokenizer tokens = new StringTokenizer(line);
 				ArrayList<String> array = new ArrayList<String>();
 				while(tokens.hasMoreTokens()){
 					HttpClient 		client 		= new DefaultHttpClient();
 					HttpPost 		post 		= new HttpPost("http://tools.nlp.itu.edu.tr/SimpleApi");
 					List<NameValuePair> 	parameters 	= new ArrayList<NameValuePair>(3);
 					parameters.add(new BasicNameValuePair("tool", "morphanalyzer"));
 					parameters.add(new BasicNameValuePair("input", tokens.nextToken()));
 					parameters.add(new BasicNameValuePair("token", "******************************"));
 					post.setEntity(new UrlEncodedFormEntity(parameters, "UTF-8"));
 					HttpResponse resp = client.execute(post);
 					String result = EntityUtils.toString(resp.getEntity());
					StringTokenizer analysisTokenizer = new StringTokenizer(result);
					analysisTokenizer.nextToken();
					while(analysisTokenizer.hasMoreTokens()){
						result = analysisTokenizer.nextToken();
						String s = result.split("\\+")[0].toLowerCase();
					    if(!array.contains(s)){
					    	array.add(s);
					    	System.out.println(s);
					    }
					}
 					
 				}
 				analyzedSentences.add(array);
 				array.clear();
 			}
 		}catch(IOException ex){
 			ex.printStackTrace();
 		}
 		finally{
 			if(reader != null)
 				reader.close();
 		}
 	}
	
	public String findMinLemma(String[] array){
		/*
		 * Use to find minumum lemma of given array. 
		 * It is necessary for contain() function in morphSpecialParse() function.
		 * */
		int min = array[0].length();
		int index = 0;
		for(int i=1;i<array.length;i++){
			if(min>array[i].length()){
				min = array[i].length();
				index= i;
			}
		}
		return array[index];
	}
	
	@SuppressWarnings("resource")
	public String[] wordBasedMorphologicalAnalyzer(String word){
		/*
		 * Like function name this function return morphological analysis of given word with test sentence
		 * All words in test sentence morphologically analyzed and add to ambiguousList sentence by sentence
		 * */
		ArrayList<String> results = new ArrayList<String>();
		try {
			HttpClient 		client 		= new DefaultHttpClient();
			HttpPost 		post 		= new HttpPost("http://tools.nlp.itu.edu.tr/SimpleApi");
			List<NameValuePair> 	parameters 	= new ArrayList<NameValuePair>(3);
			parameters.add(new BasicNameValuePair("tool", "morphanalyzer"));
			parameters.add(new BasicNameValuePair("input", word));
			parameters.add(new BasicNameValuePair("token", "******************************"));
			post.setEntity(new UrlEncodedFormEntity(parameters, "UTF-8"));
			HttpResponse resp = client.execute(post);
			word = EntityUtils.toString(resp.getEntity());
			StringTokenizer tokens = new StringTokenizer(word.toLowerCase());
			
			while(tokens.hasMoreTokens()){
				String[] array = tokens.nextToken().split("\\s|\\+|\\:");
				if(!results.contains(array[0]))
					results.add(array[0]);
			}
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String[] array = new String[results.size()];
		array  = results.toArray(array);
		return array;
	}
	
	
	@SuppressWarnings("resource")
	public void morphSpecialParse() throws IOException {
		
		try {
			StringTokenizer t = new StringTokenizer(test);
			HttpClient 		client 		= new DefaultHttpClient();
			HttpPost 		post 		= new HttpPost("http://tools.nlp.itu.edu.tr/SimpleApi");
			ArrayList<String> morphs = new ArrayList<String>();
			while(t.hasMoreTokens()){
				List<NameValuePair> 	parameters 	= new ArrayList<NameValuePair>(3);
				parameters.add(new BasicNameValuePair("tool", "morphanalyzer"));
				parameters.add(new BasicNameValuePair("input", t.nextToken()));
				parameters.add(new BasicNameValuePair("token", "******************************"));
				post.setEntity(new UrlEncodedFormEntity(parameters, "UTF-8"));
				HttpResponse resp = client.execute(post);
				morphs.add(EntityUtils.toString(resp.getEntity()));
			}
			ArrayList<String> analysis = new ArrayList<String>(1);
			for(String s:morphs){
				analysis.add(s);
				analysis.add("\n");
			}
			String line = "";
			ArrayList<ArrayList<String>> list = new ArrayList<ArrayList<String>>(1);
			ArrayList<String> subList = new ArrayList<String>(1);
			ArrayList<String> k = new ArrayList<String>(1);
			
			for(String s : analysis){
				line = s;
				if(line.length() > 1){
					line = line.toLowerCase();
					StringTokenizer tokens = new StringTokenizer(line);
					while(tokens.hasMoreTokens()){
						String[] array = tokens.nextToken().split("\\s|\\+");
						if(!array[1].contains("punc")){
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
							if(k.size()>1){
								int index = getIndexOfAmbiguousWord(list);
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
										ambiguousList.add(new ArrayList<ArrayList<String>>(sentence_list));
										sentence_list.clear();
									}
								}
							}
							list.clear();
							subList.clear();
							k.clear();
						}
					}
				}else{
					list.add(new ArrayList<>(subList));
					subList.clear();
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e){
			e.printStackTrace();
		} 
	}
	
	public static int getIndexOfAmbiguousWord(ArrayList<ArrayList<String>> list){
		int index = -1;
		for(int i = 0;i<list.size();i++)
			for(int j=0;j<list.get(i).size();j++){
				if(list.get(i).get(j).contains(ambiguousWord)){
					index = i;
					i = list.size();
					break;
				}
			}
		return index;
	}	
	
}

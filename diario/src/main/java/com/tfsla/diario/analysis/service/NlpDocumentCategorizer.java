package com.tfsla.diario.analysis.service;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import opennlp.tools.doccat.DoccatFactory;
import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizer;
import opennlp.tools.doccat.DocumentCategorizerME;
import opennlp.tools.doccat.DocumentSample;
import opennlp.tools.doccat.DocumentSampleStream;
import opennlp.tools.doccat.FeatureGenerator;
import opennlp.tools.doccat.NGramFeatureGenerator;
import opennlp.tools.util.InputStreamFactory;
import opennlp.tools.util.MarkableFileInputStreamFactory;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.TrainingParameters;

public class NlpDocumentCategorizer {
DocumentCategorizer doccat = null;
	
	public class Result {
		public String bestCategory;
		public double proba;
		
		private List<String> categories = new ArrayList<String>();
		private Map<String,Double> probability = new HashMap<>();
		
		protected Result(String bestCategory) {
			this.bestCategory = bestCategory;
		}
		
		protected void addCategory(String name, double proba) {
			categories.add(name);
			probability.put(name, proba);
			
			if (name.equals(bestCategory))
				this.proba = proba;
		}
		
		public String getBestCategory() {
			return bestCategory;
		}
		
		public double getProba() {
			return proba;
		}
		
		public double getCategoryProba(String name) {
			return probability.get(name);
		}
		
		public List<String> getCategories() {
			return this.categories;
		}
	}
	
	public void prepareTrainingSet(String inputFileName, String outputFileName) {
		
		FileWriter fw = null;
		BufferedReader b = null;
        try {

        	
        	fw = new FileWriter(outputFileName);
        	 
            File f = new File(inputFileName);

            b = new BufferedReader(new FileReader(f));

            String readLine = "";

            while ((readLine = b.readLine()) != null) {
            	String line = replaceNumbers(replaceNonTextAndNumber(normalizeText(readLine)));
            	fw.write(line);
            	fw.write(System.getProperty("line.separator"));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
        	if (fw!=null)
				try {
					fw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	
        	if (b!=null)
				try {
					b.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        }
	}

	
	public void trainModel(String exampleFile, String trainedModeFile) {
		try {
			InputStreamFactory dataIn = new MarkableFileInputStreamFactory(new File(exampleFile));

	        ObjectStream<String> lineStream = new PlainTextByLineStream(dataIn, "UTF-8");
	        ObjectStream<DocumentSample> sampleStream = new DocumentSampleStream(lineStream);

	     // define the training parameters
            TrainingParameters params = new TrainingParameters();
            params.put(TrainingParameters.ITERATIONS_PARAM, 10+"");
            params.put(TrainingParameters.CUTOFF_PARAM, 0+"");
            
            // feature generators - N-gram feature generators
            FeatureGenerator[] featureGenerators = { new NGramFeatureGenerator(1,1),
                    new NGramFeatureGenerator(2,3) };
            DoccatFactory factory = new DoccatFactory(featureGenerators);
 
            // create a model from traning data
            DoccatModel model = DocumentCategorizerME.train("sp", sampleStream, params, factory);
            System.out.println("\nModel is successfully trained.");
 
            // save the model to local
            BufferedOutputStream modelOut = new BufferedOutputStream(new FileOutputStream(trainedModeFile));
            model.serialize(modelOut);
            System.out.println("\nTrained Model is saved locally at : "+trainedModeFile);
 
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void loadTrainedMode(String modelTrainedFile) throws IOException {
		
		InputStream is = NlpDocumentCategorizer.class.getResourceAsStream(modelTrainedFile); 
		//new FileInputStream(modelTrainedFile);
		DoccatModel model = new DoccatModel(is);

		doccat = new DocumentCategorizerME(model);
	}
	
	public Result categorize(String text) throws Exception {
		
		if (doccat==null)
			throw new Exception("Debe inicializar el modelo");
		
		
		String[] docWords =  replaceNumbers(replaceNonTextAndNumber(normalizeText(text))).split(" ");
		double[] aProbs = doccat.categorize(docWords);
		
		Result result = new Result(doccat.getBestCategory(aProbs));
		for(int i=0;i<doccat.getNumberOfCategories();i++){
			result.addCategory(doccat.getCategory(i),aProbs[i]);
		}		
		return result;
		
	}
	
	private String normalizeText(String text) {
		String string = Normalizer.normalize(text, Normalizer.Form.NFD);
		string = string.replaceAll("[^\\p{ASCII}]", "");
		string = string.toLowerCase();
		return string;
	}
	
	private String replaceNumbers(String text) {
		String string = text.replaceAll("^\\d*\\s|\\s\\d*\\s|\\s\\d*$", " [number] ");
		return string;
	}
	
	private String replaceNonTextAndNumber(String text) {
		String string = text.replaceAll("[^\\sA-Za-z0-9]", "");
		return string;
	} 


}

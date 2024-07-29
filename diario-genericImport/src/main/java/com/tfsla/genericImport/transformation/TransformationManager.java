package com.tfsla.genericImport.transformation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TransformationManager {
	private static Map<String,I_dataTransformation> transformations = new LinkedHashMap<String,I_dataTransformation>();
	
	static {
		DateToStringDataTransformation dateToString = new DateToStringDataTransformation();
		transformations.put(dateToString.getName(), dateToString);
		
		StringToDateDataTransformation stringToDate = new StringToDateDataTransformation();
		transformations.put(stringToDate.getName(), stringToDate);
		
		ConditionalDataTransformation conditionalReplace = new ConditionalDataTransformation();
		transformations.put(conditionalReplace.getName(), conditionalReplace);

		FindReplaceDataTransformation findreplace = new FindReplaceDataTransformation();
		transformations.put(findreplace.getName(), findreplace);
		
		RegexDataExtractor regexExtractor = new RegexDataExtractor();
		transformations.put(regexExtractor.getName(), regexExtractor);

		RegexRewriteTransformation regexRewrite = new RegexRewriteTransformation();
		transformations.put(regexRewrite.getName(), regexRewrite);

		JoinDataTransformation joinData = new JoinDataTransformation();
		transformations.put(joinData.getName(), joinData);

		NumberToStringTransformation numberToString = new NumberToStringTransformation();
		transformations.put(numberToString.getName(), numberToString);
		
		NullExtractorTransformation nullExtractor = new NullExtractorTransformation();
		transformations.put(nullExtractor.getName(), nullExtractor);
		
		StringFormatTransformation stringFormat = new StringFormatTransformation();
		transformations.put(stringFormat.getName(), stringFormat);
		
		DateToLongTransformation dateToLong = new DateToLongTransformation();
		transformations.put(dateToLong.getName(), dateToLong);
		
		CaseConditionalTransformation caseConditional = new CaseConditionalTransformation();
		transformations.put(caseConditional.getName(), caseConditional);
		
		ImageSizeTransformation imageSize = new ImageSizeTransformation();
		transformations.put(imageSize.getName(), imageSize);
		
		StringEncodingDataTransformation strinFormatEncoding= new StringEncodingDataTransformation();
		transformations.put(strinFormatEncoding.getName(), strinFormatEncoding);
		
		StringHTMLTransformation stringCleanHTML = new StringHTMLTransformation();
		transformations.put(stringCleanHTML.getName(), stringCleanHTML);
		
	}
	
	public I_dataTransformation getTransformation(String tranformation) {
		if (tranformation==null)
			return null;
		
		String[] split = tranformation.split("\\|\\|");
		return transformations.get(split[0]);
	}
	
	public List<I_dataTransformation> getTransformations() {
		List<I_dataTransformation> valueList = new ArrayList<I_dataTransformation>(transformations.values());		
		return valueList;
	}
	
	
}

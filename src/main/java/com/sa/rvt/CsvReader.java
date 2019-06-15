package com.sa.rvt;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.opencsv.CSVReader;

public class CsvReader {
	static String projectPath = System.getProperty("user.dir").toString();
final static String RATING_WORKSHEET_CSV_FILE_PATH = projectPath + "//input//119432119J Ratings Worksheet.csv";
final static String FACTORS_LOOKUP_CSV_FILE_PATH = projectPath + "//factorslookup//FRrating factors lookup_updated.csv";
	//public static void main(String[] args) throws IOException {


public static Map<String,Map<String,Map<String,String>>> getComparedValues() throws IOException {
		// Read all the factors from the lookup file and store it in an arraylist
	Map<String,Map<String,Map<String,String>>> resultToCompare = new HashMap<>();
		List<String[]> factorValues = new ArrayList<String[]>();
		try (
	            CSVReader ratingWorkSheetReader = readData(RATING_WORKSHEET_CSV_FILE_PATH);
				CSVReader factorsLookupReader = readData(FACTORS_LOOKUP_CSV_FILE_PATH);
				
				
	        ) {
			
			
			factorValues.addAll(readValues(factorsLookupReader));
	            // Reading Records One by One in a String array
			Map<String,Map<String,List<String>>> parentMap = new HashMap<>();
			
			 
	            String[] nextRecord;
	            int iSkipCount =0;
	            String tempValue ="";
	            String parentElement ="";
	            boolean bLineLevelFound = false;
	            int iteration = 0;
	            
	            while ((nextRecord = ratingWorkSheetReader.readNext()) != null) {
	            	
	            	iSkipCount++;
	            	if(iSkipCount < 3) continue;
	            	Map<String, List<String>> mapInputFileValues = parentMap.get(parentElement);
	            	
	            	if(StringUtils.isNotEmpty(nextRecord[0])&& nextRecord[0].trim().length()>0 && !"TermAmount".equalsIgnoreCase(nextRecord[0].trim())){
	            		iteration++;
	            		//adding path
	            		if(nextRecord[0].trim().contains("Line-level")){
	            			bLineLevelFound = true;
	            		}
						if (nextRecord[0].trim().contains(":") || nextRecord[0].trim().contains("Line-level")) {
							iteration = 1;
							Map<String, List<String>> subMap = new HashMap<String, List<String>>();
							parentElement = nextRecord[0].trim();
							parentMap.put(nextRecord[0].trim(), subMap);
							continue;
						}
						if (tempValue.equalsIgnoreCase("line-level")){
							System.out.println("Linelevel");
						}
						
	            		//ending path
						if(bLineLevelFound && iteration == 2){
							tempValue = nextRecord[0];
							List<String> listValues = new ArrayList<String>();
							mapInputFileValues.put(nextRecord[0], listValues);
					} else if(!bLineLevelFound) {
						tempValue = nextRecord[0];
						List<String> listValues = new ArrayList<String>();
						mapInputFileValues.put(nextRecord[0], listValues);
					}
	            	}
	            	if(StringUtils.isNotEmpty(nextRecord[13])&& nextRecord[13].trim().length()>0){
	            		String temp= nextRecord[13];
	            		if(nextRecord[1] != null && "TermAmount".equalsIgnoreCase(nextRecord[1].trim())){
	            			temp = "TermAmount";
	            		}else if(nextRecord[0] != null && "TermAmount".equalsIgnoreCase(nextRecord[0].trim())){//added for block splitting
	            			temp = "TermAmount";
	            			iteration = 1;
	            		}
	            		final String value= temp;
	            		final String lastValue= nextRecord[15];
	            		final String tempFinalValue= tempValue;
	            		//factoryValues.stream().filter(strarray -> strarray[1].trim().equalsIgnoreCase(value.trim())).map(str -> {
	            		//Rani replaced the below code
	            		//String[] coverages =tempValue.split("-");
	            		String[] coverages=null;
	            		if (tempValue.contains("-")){
	            			//System.out.println(tempValue);
	            			if (tempValue.contains("Blanket Farm Personal Property")){
	            				System.out.println(tempValue);
	            			}
	            			coverages =tempValue.split("-");
	            		}
	            		            		
	            		for(int iLookup=0;iLookup<factorValues.size();iLookup++ ){
	            			String[] lookupVals = factorValues.get(iLookup);
	            			//System.out.println("Lookup Vals length - "+lookupVals.length);
	            			if(lookupVals != null && coverages != null &&(lookupVals.length>4) && (coverages.length > 2) && lookupVals[1].trim().equalsIgnoreCase(coverages[0].trim()) && lookupVals[2].trim().equalsIgnoreCase(coverages[2].trim()) && (temp.equalsIgnoreCase(lookupVals[4].trim()) || lookupVals[4].trim().equalsIgnoreCase(nextRecord[13].trim()))){
	            				List<String> listValues = mapInputFileValues.get(tempFinalValue);
	    	            		listValues.add(value);
	    	            		listValues.add(lastValue);
	    	            		//System.out.println(lookupVals[5].trim());
	    	            		listValues.add(lookupVals[5].trim());
	    	            		mapInputFileValues.put(tempFinalValue, listValues);
	            			}
	            			//Rani - added to have the Line-Level items included.
	            			else if(lookupVals != null && coverages != null &&(lookupVals.length>4) && (coverages.length == 2) && lookupVals[1].trim().equalsIgnoreCase(coverages[0].trim()) && lookupVals[2].trim().equalsIgnoreCase(coverages[1].trim()) && (temp.equalsIgnoreCase(lookupVals[4].trim()) || lookupVals[4].trim().equalsIgnoreCase(nextRecord[13].trim()))){
	            				List<String> listValues = mapInputFileValues.get(tempFinalValue);
	    	            		listValues.add(value);
	    	            		listValues.add(lastValue);
	    	            		//System.out.println(lookupVals[5].trim());
	    	            		listValues.add(lookupVals[5].trim());
	    	            		mapInputFileValues.put(tempFinalValue, listValues);
	            			}
	            		}
	            		
	            		//});
	            	}
	            }
	            System.out.println(parentMap+"\n");
			for (String setKey : parentMap.keySet()) {
				resultToCompare.putIfAbsent(setKey,saveFactorsPerCoverage(parentMap.get(setKey)));
				resultToCompare.entrySet().stream()
						.forEach(e -> System.out.println(e.getKey() + "---- " + e.getValue()));
			}
	           
	            //csvReader.iterator().forEach(csv);
	        }
		return resultToCompare;

	}
	/*public static void updateOutput(Map<String, String> coverageFactors){
		coverageFactors.entrySet().stream().forEach(e -> System.out.println(e.getKey()+ "---- "+e.getValue()));
		
	}*/
	
	public static Map<String,Map<String, String>> saveFactorsPerCoverage(Map<String,List<String>> mapInputFileValues){
		
		Map<String,Map<String, String>> finalCovrageFactors = new HashMap<>();
		 mapInputFileValues.entrySet().stream().forEach(e -> {
			 Map<String, String> coverageFactors = new HashMap<String, String>();
         	if (e.getValue()!=null){
         		System.out.println(e.getKey() + " -- " + e.getValue());
         		for(int i=0;i<e.getValue().size();i=i+3){
         			if(e.getValue().get(i+2) != null && e.getValue().get(i+2).trim().length()>0)
         			coverageFactors.putIfAbsent(e.getValue().get(i+2), extractValue(e.getValue().get(i+1)));
         		}
         		finalCovrageFactors.putIfAbsent(e.getKey().toUpperCase(), coverageFactors);
         	}
         });
		return finalCovrageFactors;
	}
	
	public static Map<String, String> saveFactorsPerCoverage(String coverageName, List<String> coverageFactorsList){
		Map<String, String> factorList = new HashMap<String, String>();
		for(int i=0;i<coverageFactorsList.size();i=i+3){
		//for(Iterator<String> factor = coverageFactorsList.iterator(); factor.hasNext(); ) {
			  //String item = factor.next();
			  /*System.out.println(item);
			  if (item.contains(":")){
				  
				  String temp = factor.next();*/
				  //if(temp!=null)
					  factorList.put(coverageFactorsList.get(i+2), extractValue(coverageFactorsList.get(i+1)));
			 // }
			}
		System.out.println(factorList);
		return factorList;
	}
	/**
	 * Description: This method will extract the factor value from the given string next to = symbol
	 * @param inputVal
	 * @return
	 */
	public static String extractValue(String inputVal){
		String extractedValue="";
		if (inputVal.contains("="))
			extractedValue=inputVal.substring(inputVal.indexOf("=")+1);
		else
			extractedValue=inputVal;
		//System.out.println("Value is " + extractedValue);
		
		return extractedValue.trim();
	}
	public static CSVReader readData(String path){
		CSVReader csvReader = null;
		Reader reader;
		
		try {
			//reader = Files.newBufferedReader(Paths.get(path));
			 //reader = Files.newBufferedReader(Paths.get(path), StandardCharsets.UTF_8);
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8));
			 csvReader=  new CSVReader(reader);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return csvReader;
	}
	
	public static List<String[]> readValues(CSVReader reader){
		List<String[]> returnRecord = new ArrayList<String[]>();
		String[] nextRecord;
		try {
			while ((nextRecord = reader.readNext()) != null) {
				returnRecord.add(nextRecord);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return returnRecord;
		
	}

}

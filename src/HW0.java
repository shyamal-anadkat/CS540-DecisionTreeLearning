/**
 * CS540 HW0: Hand-in Practice and Parsing ML Datasets
 * 
 * @author Shyamal Anadkat (NetID: anadkat) 9/13/2016
 *
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

public class HW0 {

	//***VARIABLES AND DATA STRUCTURES***//
	static int numFeatures; //tot. features in dataSet
	static int numExamples; //tot. examples in dataSet 
	static int numLabels; //tot. output labels in dataSet 
	static Map<String,List<String>> featuresToValues = new LinkedHashMap<String,List<String>>(); //preserving order
	static List<Feature> listOfFeatureValues = new ArrayList<Feature>();
	static List<Example> examples = new ArrayList<Example>();
	static List<String> outputLabels = new ArrayList<String>();
	static int l1Count=0,l2Count=0;
	//**********************************//

	public static void main(String[] args) {

		System.out.println("HW0: Parsing ML Dataset\n========================\n");
		if (args.length != 1) {
			System.err.println("Please supply a filename on the " +
					"command line: java ScannerSample" + 
					" <filename>");
			System.exit(1);
		}

		//creating a scanner to read the input file.
		Scanner fileScanner = null;
		try {
			fileScanner = new Scanner(new File(args[0]));
		} catch(FileNotFoundException e) {
			System.err.println("Could not find file '" + args[0] + 
					"'.");
			System.exit(1);
		}

		while(fileScanner.hasNext()) {
			String line = fileScanner.nextLine().trim();

			// Skip blank lines and comments, skip insignificant lines 
			if(line.length() == 0 || (line.length() > 2 && line.substring(0,2).equals("//"))) {
				continue;
			}

			//number of features and number of examples 
			else if (line.matches("\\d+")) {
				if (numFeatures == 0) {
					numFeatures = Integer.parseInt(line);
				} else {
					numExamples = Integer.parseInt(line);
				}  
			} else if(line.contains("-")) {  //checking for features 

				List<String> listOfFeats = new ArrayList<String>();
				String[] tokens = line.split("-");
				String feature = tokens[0].trim();
				String[] values = tokens[1].trim().split("\\s+");

				Feature newFeat = new Feature(values[0].trim(), values[1].trim());
				newFeat.setIdentifier(feature);
				for(int i = 0; i < values.length; i++) {
					listOfFeats.add(values[i].trim());
				}
				newFeat.setIdentifier(feature);
				listOfFeatureValues.add(newFeat);
				featuresToValues.put(feature, listOfFeats);

			} else if(line.trim().split(" ").length == 1) {
				outputLabels.add(line);
				numLabels++;
			} else {
				//parsing in the examples 
				List<String> features = new ArrayList<String>();
				String[] tokens = line.split("\\s+"); //whitespaces and stuff 
				Example dataEntry = new Example(tokens[0],tokens[1]);

				for(int i = 2; i < tokens.length; i ++) {
					features.add(tokens[i]);
				}
				dataEntry.addFeatures(features);
				examples.add(dataEntry);
			}
		}
		processDataAndPrint();
	}
	/**
	 * Extract count of value from a label, feature
	 * @param label
	 * @param value
	 * @param feature
	 * @return
	 */
	public static int extractCount(String label, String value, String feature) {
		int count = 0 ; 
		for (Example ex: examples) {
			if (ex.getLabel().equalsIgnoreCase(label)) {
				Iterator<Entry<String, List<String>>> it  = ex.getFeaturesMap().entrySet().iterator();
				while (it.hasNext()) {
					Entry<String, List<String>> pair = it.next();
					List<String> vals = pair.getValue();
					for(int k = 0; k < vals.size(); k++) {
						if(getFeatureIDFromIndex(k).equalsIgnoreCase(feature)) {
							if(vals.get(k).equalsIgnoreCase(value)) {
								count++;
							}							
						}						
					}
				}
			}
		}
		return count;
	}

	/**
	 * Get Percent statistic from count and total
	 * @param count
	 * @param total
	 * @return
	 */
	public static String getPercent(int count, int total) {
		DecimalFormat df = new DecimalFormat("##.##");
		double retVal = (count * 100.0d / total);
		return df.format(retVal)+"%";
	}

	/**
	 * Process parsed DS and print info 
	 */
	public static void processDataAndPrint() {

		System.out.format("There are %d features in the dataset.%n"
				+ "There are %d examples%n",numFeatures,numExamples);
		System.out.println("--------------------------------");

		//keeping count of the output labels from populated examples
		for(int i = 0; i < examples.size(); i++) {
			String label = examples.get(i).getLabel();
			if (label.equalsIgnoreCase(outputLabels.get(0))) {
				l1Count++;
			} else {
				l2Count++;
			}
		}

		for(int i = 0 ; i < numLabels; i++) {
			int count = (i==0) ? l1Count:l2Count;
			System.out.println( count + " have output label '"+ outputLabels.get(i)+"'");
		}
		System.out.println("--------------------------------\n");

		Iterator<Entry<String, List<String>>> it = featuresToValues.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, List<String>> pair = it.next();
			List<String> vals = pair.getValue();
			System.out.println("Feature: "+pair.getKey().toUpperCase());
			System.out.println("========================");
			for(int v = 0; v < vals.size(); v++) {
				for(int i = 0 ; i < numLabels; i++) {
					int totalCount = (i==0) ? l1Count:l2Count;
					System.out.println("Label: "+outputLabels.get(i)+" || Value: "+vals.get(v)+ " || Percent: "+ 
							getPercent(extractCount(outputLabels.get(i),vals.get(v),pair.getKey()),totalCount)+" "
							+ "|| Count: "+extractCount(outputLabels.get(i),vals.get(v),pair.getKey()));
				}
			}
			System.out.println();
		}
	}

	/**
	 * 
	 * @param index
	 * @return
	 */
	public static String getFeatureIDFromIndex (int index) {
		String retVal = null;
		for (int i = 0 ; i < listOfFeatureValues.size(); i ++) {
			if (i == index) {
				retVal = listOfFeatureValues.get(i).getIdentifier();
			}
		}
		return retVal;
	}
}

/**
 * One Example/Data Set Entry Object, mimicking database
 * @author Shyamal Anadkat
 *
 */
class Example {
	Map<String,List<String>> featureValues;
	String name,label;

	Example(String name, String label) {
		this.name = name;
		this.label = label;
		this.featureValues = new LinkedHashMap<String,List<String>>(); //preserve order
	}
	public void addFeatures(List<String> features) {
		featureValues.put(label, features);
	}
	public String getLabel(){
		return label;
	}
	public String getName() {
		return name;
	}
	public Map<String,List<String>> getFeaturesMap(){
		return featureValues;
	}
}

/**
 * Represents a single feature.
 * @author Shyamal Anadkat
 *
 */
class Feature{
	String identifier,val1,val2;

	public Feature() {}
	public Feature(String val1, String val2) {
		this.val1 = val1;
		this.val2 = val2;
	}
	public String getIdentifier() {return this.identifier;}
	public String getVal1() {return this.val1;}
	public String getVal2() {return this.val2;}
	public void setVal1(String val1) {this.val1 = val1;}
	public void setVal2(String val2) {this.val2 = val2;}
	public void setIdentifier(String id) {this.identifier = id;}

}




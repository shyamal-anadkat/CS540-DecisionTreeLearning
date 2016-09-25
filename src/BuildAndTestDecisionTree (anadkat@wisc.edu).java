import java.util.*;
import java.io.*;

////////////////////////////////////////////////////////////////////////////
//                                                                       
// Code for HW1, Problem 2
//               Inducing Decision Trees
//               CS540 (Shavlik)
//                                                                        
////////////////////////////////////////////////////////////////////////////

/* BuildAndTestDecisionTree.java 

   Copyright 2008, 2011, 2013 by Jude Shavlik and Nick Bridle.
   May be freely used for non-profit educational purposes.

   To run after compiling, type:

     java BuildAndTestDecisionTree <trainsetFilename> <testsetFilename>

   Eg,

     java BuildAndTestDecisionTree train-hepatitis.data test-hepatitis.data

   where <trainsetFilename> and <testsetFilename> are the 
   input files of examples.

   Notes:  

           Please place all your HW1 code in a single file. 

           All that is required is that you keep the name of the
          BuildAndTestDecisionTree class and don't change the 
          calling convention for its main function.  

           There is no need to worry about "error detection" when reading data files.
           We'll be responsible for that.  HOWEVER, DO BE AWARE THAT
           WE WILL USE ONE OR MORE DIFFERENT DATASETS DURING TESTING,
           SO DON'T  WRITE CODE THAT IS SPECIFIC TO THE PROVIDED
           DATASETS.  (As  stated above, you may assume that our additional datasets
           are properly formatted in the style used for the provided dataset.)

           A weakness of our design is that the category and feature
           names are defined in BOTH the train and test files.  These
           names MUST match, though this isn't checked.  However,
           we'll live with the weakness because it reduces complexity
           overall (note: you can use the SAME filename for both the
           train and the test set, as a debugging method; you should
           get ALL the test examples correct in this case, since we are
	   not "pruning" decision trees to avoid overfitting the training data).
 */

public class BuildAndTestDecisionTree
{

	// "Main" reads in the names of the files we want to use, then reads 
	// in their examples.
	public static void main(String[] args)
	{   
		if (args.length != 2)
		{
			System.err.println("You must call BuildAndTestDecisionTree as " + 
					"follows:\n\njava BuildAndTestDecisionTree " + 
					"<trainsetFilename> <testsetFilename>\n");
			System.exit(1);
		}    

		// Read in the file names.
		String trainset = args[0];
		String testset  = args[1];

		// Read in the examples from the files.
		ListOfExamples trainExamples = new ListOfExamples();
		ListOfExamples testExamples  = new ListOfExamples();
		if (!trainExamples.ReadInExamplesFromFile(trainset) ||
				!testExamples.ReadInExamplesFromFile(testset))
		{
			System.err.println("Something went wrong reading the datasets ... " +
					"giving up.");
			System.exit(1);
		}
		else
		{ /* The following is included so you can see the data organization.
         You'll need to REPLACE it with code that:

          1) uses the TRAINING SET of examples to build a decision tree

          2) prints out the induced decision tree (using simple, indented 
             ASCII text)

          3) categorizes the TESTING SET using the induced tree, reporting
             which examples were INCORRECTLY classified, as well as the
             FRACTION that were incorrectly classified.
             Just print out the NAMES of the examples incorrectly classified
             (though during debugging you might wish to print out the full
             example to see if it was processed correctly by your decision 
             tree)       
		 */

			//System.out.println(infoRemaining(1,1,1,2));
			trainExamples.DescribeDataset();
			// testExamples.DescribeDataset();
			//trainExamples.PrintThisExample(0);  // Print out an example
			//trainExamples.PrintAllExamples(); // Don't waste paper printing all 
			// of this out!
			//System.out.println(getIndexOfFeature(trainExamples,"weight"));
			//System.out.println(trainExamples.getAttributeForLabelCnt("neg", "Y"));
			//System.out.println(infoNeeded((double)1/3,(double)2/3));
			System.out.println("Remainder for "+trainExamples.getfeatures()[0].getName()+":"+getRemainder(trainExamples.getfeatures()[0],trainExamples));
			System.out.println("Remainder for "+trainExamples.getfeatures()[1].getName()+":"+getRemainder(trainExamples.getfeatures()[1],trainExamples));
			System.out.println("Remainder for "+trainExamples.getfeatures()[2].getName()+":"+getRemainder(trainExamples.getfeatures()[2],trainExamples));
			//testExamples.PrintAllExamples();  // Instead, just view it on the screen
			System.out.println(getBestAttribute(trainExamples).getName());
			//System.out.println(attributeInFeatureCnt("R", trainExamples, 0)); //WORKS !!!!
		}

		Utilities.waitHere("Hit <enter> when ready to exit.");
	}


	private static BinaryFeature getBestAttribute(ListOfExamples in) {
		BinaryFeature best = in.getfeatures()[0]; //default/random
		double leastRemainder = Integer.MAX_VALUE; //or max info gain
		for(int i = 0; i < in.getNumberOfFeatures(); i++) {
			if(getRemainder(in.getfeatures()[i],in) < leastRemainder) {
				System.out.println("happended");
				best = in.getfeatures()[i];
			}
		}
		return best;
	}


	public static double getRemainder(BinaryFeature a, ListOfExamples set) {
		int totalExamples = set.size();
		int index = getIndexOfFeature(set,a.getName());
		//System.out.println(index);

		//first value 
		int numFirstVal = attributeInFeatureCnt(a.getFirstValue(), set, index);
		int numAttFromLabel1_1 = set.getAttributeForLabelCnt(set.getOutputLabels().getFirstValue(), index , a.getFirstValue());
		//System.out.println(numFirstVal);
		int numAttFromLabel2_1 = set.getAttributeForLabelCnt(set.getOutputLabels().getSecondValue(), index, a.getFirstValue());		
		double infoNeeded1 = infoNeeded((double)numAttFromLabel1_1/numFirstVal,(double)numAttFromLabel2_1/numFirstVal);
		//System.out.println();
		//second value 
		int numSecondVal = attributeInFeatureCnt(a.getSecondValue(), set, index);
		int numAttFromLabel1_2 = set.getAttributeForLabelCnt(set.getOutputLabels().getFirstValue(), index , a.getSecondValue());
		int numAttFromLabel2_2 = set.getAttributeForLabelCnt(set.getOutputLabels().getSecondValue(), index , a.getSecondValue());
		double infoNeeded2 = infoNeeded((double)numAttFromLabel1_2/numSecondVal, (double)numAttFromLabel2_2/numSecondVal);
		//System.out.println(infoNeeded2);
		return (((double)numFirstVal/totalExamples)*infoNeeded1) + ((double)numSecondVal/totalExamples)*infoNeeded2; 
	}


	//info remaining for one value per attribute/feature 
	@SuppressWarnings("unused")
	private static double infoRemaining(double numPos, double numNeg, double numExamples, double count ) {
		if(count == 0 || numExamples == 0) return 0;
		return (double)(count/numExamples) * infoNeeded((double)numPos/count,(double)numNeg/count) ;
	}

	//information needed calculation
	private static double infoNeeded(double pos, double neg) {
		if(pos == 0 || neg == 0) {
			return 0.0;
		}
		return (double)(-1*pos*log2(pos))+(-1*neg*log2(neg));
	}

	public static int attributeInFeatureCnt(String value, ListOfExamples set, int idx) {
		//System.out.println(value +" "+idx);
		int retVal = 0;
		for(int i = 0; i < set.size(); i++) { //all examples 
			if(set.get(i).get(idx).equalsIgnoreCase(value)) {
				retVal++;
			}
		}
		//System.out.println(retVal);
		return retVal;
	}

	/**
	 * Returns index of feature name. 
	 * @param set
	 * @param feature
	 * @return
	 */
	public static int getIndexOfFeature(ListOfExamples set, String feature) {
		for(int i = 0; i < set.getNumberOfFeatures(); i ++) {
			if(feature.equalsIgnoreCase(set.getFeatureName(i))) {
				return i;
			}
		}
		return 0;

	}

	//log2 function
	public static final double log2(double f)
	{
		return (Math.log(f)/Math.log(2.0));
	}
}










// This class, an extension of ArrayList, holds an individual example.
// The new method PrintFeatures() can be used to
// display the contents of the example. 
// The items in the ArrayList are the feature values.
class Example extends ArrayList<String>
{
	// The name of this example.
	private String name;  

	// The output label of this example.
	private String label;

	// The data set in which this is one example.
	private ListOfExamples parent;  

	// Constructor which stores the dataset which the example belongs to.
	public Example(ListOfExamples parent) {
		this.parent = parent;
	}

	// Print out this example in human-readable form.
	public void PrintFeatures()
	{
		System.out.print("Example " + name + ",  label = " + label + "\n");
		for (int i = 0; i < parent.getNumberOfFeatures(); i++)
		{
			System.out.print("     " + parent.getFeatureName(i)
			+ " = " +  this.get(i) + "\n");
		}
	}

	// Adds a feature value to the example.
	public void addFeatureValue(String value) {
		this.add(value);
	}

	// Accessor methods.
	public String getName() {
		return name;
	}

	public String getLabel() {
		return label;
	}

	// Mutator methods.
	public void setName(String name) {
		this.name = name;
	}

	public void setLabel(String label) {
		this.label = label;
	}
}

/* This class holds all of our examples from one dataset
   (train OR test, not BOTH).  It extends the ArrayList class.
   Be sure you're not confused.  We're using TWO types of ArrayLists.  
   An Example is an ArrayList of feature values, while a ListOfExamples is 
   an ArrayList of examples. Also, there is one ListOfExamples for the 
   TRAINING SET and one for the TESTING SET. 
 */
class ListOfExamples extends ArrayList<Example>
{
	// The name of the dataset.
	private String nameOfDataset = "";

	// The number of features per example in the dataset.
	private int numFeatures = -1;

	// An array of the parsed features in the data.
	private BinaryFeature[] features;

	// A binary feature representing the output label of the dataset.
	private BinaryFeature outputLabel;

	// The number of examples in the dataset.
	private int numExamples = -1;

	public ListOfExamples() {} 

	// Print out a high-level description of the dataset including its features.
	public void DescribeDataset()
	{
		System.out.println("Dataset '" + nameOfDataset + "' contains "
				+ numExamples + " examples, each with "
				+ numFeatures + " features.");
		System.out.println("Valid category labels: "
				+ outputLabel.getFirstValue() + ", "
				+ outputLabel.getSecondValue());
		System.out.println("The feature names (with their possible values) are:");
		for (int i = 0; i < numFeatures; i++)
		{
			BinaryFeature f = features[i];
			System.out.println("   " + f.getName() + " (" + f.getFirstValue() +
					" or " + f.getSecondValue() + ")");
		}
		//System.out.println("Positive Label Count: "+getLabelCount("pos"));
		//System.out.printl(getRemainder(features[0],features[0].getFirstValue()));


	}


	public int getLabelCount(String label) {
		int count = 0; 
		for(int i = 0; i < size(); i++) {
			if(this.get(i).getLabel().equalsIgnoreCase(label)) {
				count++;
			}
		}
		return count;
	}

	public BinaryFeature getOutputLabels() {
		return this.outputLabel;
	}





	// eg : 4 of the red are possitive 
	public int getAttributeForLabelCnt(String label, int index, String attrVal) {
		int retVal = 0;
		for(int i = 0; i < size(); i ++) {
			if(this.get(i).getLabel().equalsIgnoreCase(label)) {// for that label 
				if(this.get(i).get(index).equalsIgnoreCase(attrVal))
					retVal++;
			}
		}
		return retVal;
	}




	// Print out ALL the examples.
	public void PrintAllExamples()
	{
		System.out.println("List of Examples\n================");
		for (int i = 0; i < size(); i++)
		{
			Example thisExample = this.get(i);  
			thisExample.PrintFeatures();
		}
	}

	// Print out the SPECIFIED example.
	public void PrintThisExample(int i)
	{
		Example thisExample = this.get(i); 
		thisExample.PrintFeatures();
	}

	// Returns the number of features in the data.
	public int getNumberOfFeatures() {
		return numFeatures;
	}

	// Returns the name of the ith feature.
	public String getFeatureName(int i) {
		return features[i].getName();
	}

	public BinaryFeature[] getfeatures() {
		return features;
	}

	// Takes the name of an input file and attempts to open it for parsing.
	// If it is successful, it reads the dataset into its internal structures.
	// Returns true if the read was successful.
	public boolean ReadInExamplesFromFile(String dataFile) {
		nameOfDataset = dataFile;
		// Try creating a scanner to read the input file.
		Scanner fileScanner = null;
		try {
			fileScanner = new Scanner(new File(dataFile));
		} catch(FileNotFoundException e) {
			return false;
		}
		// If the file was successfully opened, read the file
		this.parse(fileScanner);
		return true;
	}

	/**
	 * Does the actual parsing work. We assume that the file is in proper format.
	 *
	 * @param fileScanner a Scanner which has been successfully opened to read
	 * the dataset file
	 */
	public void parse(Scanner fileScanner) {
		// Read the number of features per example.
		numFeatures = Integer.parseInt(parseSingleToken(fileScanner));

		// Parse the features from the file.
		parseFeatures(fileScanner);

		// Read the two possible output label values.
		String labelName = "output";
		String firstValue = parseSingleToken(fileScanner);
		String secondValue = parseSingleToken(fileScanner);
		outputLabel = new BinaryFeature(labelName, firstValue, secondValue);

		// Read the number of examples from the file.
		numExamples = Integer.parseInt(parseSingleToken(fileScanner));

		parseExamples(fileScanner);
	}

	/**
	 * Returns the first token encountered on a significant line in the file.
	 *
	 * @param fileScanner a Scanner used to read the file.
	 */
	private String parseSingleToken(Scanner fileScanner) {
		String line = findSignificantLine(fileScanner);
		// Once we find a significant line, parse the first token on the
		// line and return it.
		Scanner lineScanner = new Scanner(line);
		return lineScanner.next();
	}

	/**
	 * Reads in the feature metadata from the file.
	 * 
	 * @param fileScanner a Scanner used to read the file.
	 */
	private void parseFeatures(Scanner fileScanner) {
		// Initialize the array of features to fill.
		features = new BinaryFeature[numFeatures];
		for(int i = 0; i < numFeatures; i++) {
			String line = findSignificantLine(fileScanner);
			// Once we find a significant line, read the feature description
			// from it.
			Scanner lineScanner = new Scanner(line);
			String name = lineScanner.next();
			String dash = lineScanner.next();  // Skip the dash in the file.
			String firstValue = lineScanner.next();
			String secondValue = lineScanner.next();
			features[i] = new BinaryFeature(name, firstValue, secondValue);
		}
	}

	private void parseExamples(Scanner fileScanner) {
		// Parse the expected number of examples.
		for(int i = 0; i < numExamples; i++) {
			String line = findSignificantLine(fileScanner);
			Scanner lineScanner = new Scanner(line);

			// Parse a new example from the file.
			Example ex = new Example(this);

			String name = lineScanner.next();
			ex.setName(name);

			String label = lineScanner.next();
			ex.setLabel(label);

			// Iterate through the features and increment the count for any feature
			// that has the first possible value.
			for(int j = 0; j < numFeatures; j++) {
				String feature = lineScanner.next();
				ex.addFeatureValue(feature);
			}

			// Add this example to the list.
			this.add(ex);
		}
	}

	/**
	 * Returns the next line in the file which is significant (i.e. is not
	 * all whitespace or a comment.
	 *
	 * @param fileScanner a Scanner used to read the file
	 */
	private String findSignificantLine(Scanner fileScanner) {
		// Keep scanning lines until we find a significant one.
		while(fileScanner.hasNextLine()) {
			String line = fileScanner.nextLine().trim();
			if (isLineSignificant(line)) {
				return line;
			}
		}

		// If the file is in proper format, this should never happen.
		System.err.println("Unexpected problem in findSignificantLine.");

		return null;
	}

	/**
	 * Returns whether the given line is significant (i.e., not blank or a
	 * comment). The line should be trimmed before calling this.
	 *
	 * @param line the line to check
	 */
	private boolean isLineSignificant(String line) {
		// Blank lines are not significant.
		if(line.length() == 0) {
			return false;
		}
		// Lines which have consecutive forward slashes as their first two
		// characters are comments and are not significant.
		if(line.length() > 2 && line.substring(0,2).equals("//")) {
			return false;
		}

		return true;
	}
}

/**
 * Represents a single binary feature with two String values.
 */
class BinaryFeature {
	private String name;
	private String firstValue;
	private String secondValue;

	public BinaryFeature(String name, String first, String second) {
		this.name = name;
		firstValue = first;
		secondValue = second;
	}

	public String getName() {
		return name;
	}

	public String getFirstValue() {
		return firstValue;
	}

	public String getSecondValue() {
		return secondValue;
	}
}

class Utilities
{
	// This method can be used to wait until you're ready to proceed.
	public static void waitHere(String msg)
	{
		System.out.print("\n" + msg);
		try { System.in.read(); }
		catch(Exception e) {} // Ignore any errors while reading.
	}
}

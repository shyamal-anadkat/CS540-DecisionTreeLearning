import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * CS540 HW1: Building Decision Trees in Java
 * 
 * @author Shishir Kumar Prasad (skprasad@cs.wisc.edu)
 * 
 */
public class test {

	private static final String COMMENT = "//";
	private static final String DASH    = "-";
	private static final String SPACES  = "[\\s\\t]+";
	private static final String DIGITS  = "[0-9]+";
	
	private static final String TRAINING_DATA_SET = "Training";
	private static final String TEST_DATA_SET     = "Test";
	
	// Enum to describe the various kinds of lines in the dataset
	public enum DataSetLineType {
		INSIGNIFICANT,
		COUNT,
		FEATURE_VALUES,
		OUTPUT_LABELS,
		EXAMPLE_VALUES
	}

	// Enum to describe the kind of node in the decision tree
	public enum DecisionTreeNodeType {
		FEATURE_NODE,
		OUTPUT_LEAF_NODE
	}

	// Sample global string for pretty printing decision tree
	private static String testStr = "";
	
	public static void main(String[] args) {
	    if (args.length != 2) {
	      System.err.println("You must call BuildAndTestDecisionTree as follows:\n\n" + 
				             "java BuildAndTestDecisionTree <trainsetFilename> <testsetFilename>\n");
	      System.exit(1);
	    }    

	    String trainDataSetFile = args[0].trim();
	    String testDataSetFile  = args[1].trim();

	    // Extract the data set in files to in-memory data structures
	    ListOfExamples trainDataSet = readDataSetFromFile(trainDataSetFile, TRAINING_DATA_SET);
	    if(trainDataSet == null) {
	    	System.err.println("Failed to read training data set file.");
	    	System.exit(1);
	    }
	    ListOfExamples testDataSet = readDataSetFromFile(testDataSetFile, TEST_DATA_SET);
	    if(testDataSet == null) {
	    	System.err.println("Failed to read test data set file.");
	    	System.exit(1);
	    }

	    List<String> outputLabels = trainDataSet.outputLabels;
	    String majorityLabel = outputLabels.get(1); // Assuming second output label is the default output label
	    
	    // Build the decision tree from training data set
	    DecisionTreeNode dtree = buildDecisionTree(trainDataSet.examples, trainDataSet.features, outputLabels, majorityLabel);
	    
	    // Print the decision tree built from training data set
	    // If mode set to true, print debugging information at each node
	    printDecisionTree(dtree, "", false);
	    
	    // Test the decision tree
	    System.out.println("\n<!--------------- TRAINING DATA SET ------------------->");
	    testDecisionTree(dtree, trainDataSet.examples, TRAINING_DATA_SET);
	    System.out.println("\n<!--------------- TEST DATA SET ----------------------->");
	    testDecisionTree(dtree, testDataSet.examples, TEST_DATA_SET);
	    
	}

	// Represents a generic node in decision tree
	public static class DecisionTreeNode
	{
		// Remaining examples available to this node
		List<Example> examples = null;
		
		// Remaining features available to this node
		List<Feature> features = null;

		// Feature that this node represents
		Feature feature = null;
		
		// Contains state of the node like number of positive and negative examples at this node for debugging.
		String debug = null;
		
		/*
		 *  What is the majority value label at this node in decision tree ? This is equal to the the calculated
		 *  majority value at this node or the majority value at the parent node.
		 */
		String majorityValue = null;

		// Node type - Feature node, Leaf Node. Default : Feature Node
		DecisionTreeNodeType nodeType = DecisionTreeNodeType.FEATURE_NODE;
		
		// Traversing left is assumed to take the first feature value
		DecisionTreeNode firstValueNode = null;
		// Traversing right is assumed to take the second feature value 
		DecisionTreeNode secondValueNode = null;
		
		public DecisionTreeNode(List<Example> examples, List<Feature> features, Feature feature, String majorityValue)
		{
			this.examples 		= examples;
			this.features 		= features;
			this.feature  		= feature;
			this.majorityValue  = majorityValue;
		}
	}

	/**
	 * Builds a decision tree using the training data set and ID3 decision tree learning algorithm.
	 */
	public static DecisionTreeNode buildDecisionTree(
			List<Example> examples, List<Feature> features, List<String> outputLabels, String majorityVal)
	{
		String firstOpLabel = outputLabels.get(0);
		String secondOpLabel = outputLabels.get(1);

		// If no examples left, create a leaf node with the default majority value as label
		if(examples == null || examples.isEmpty()) {
			DecisionTreeNode node = createLeafNode(examples, features, null, majorityVal);
			node.debug = "#EXEMPTY(0/0)";
			return node;
		}
		
		// If all examples have the same classification, return a leaf node with this classification label
		Map<String, Integer> opValueLabelMap = getOutputValuesDistribution(examples, outputLabels, null, null);
		int firstOpLabelCnt = opValueLabelMap.get(firstOpLabel);
		int secondOpLabelCnt = opValueLabelMap.get(secondOpLabel);
		String debug = "{ " + firstOpLabelCnt + "(+)" +" / " + secondOpLabelCnt + "(-)"+ " }";
		if(firstOpLabelCnt == 0) {
			DecisionTreeNode node = createLeafNode(examples, features, null, secondOpLabel);
			node.debug = "#ONLYNEG" + debug;
			return node;
		}
		else if(secondOpLabelCnt == 0) {
			DecisionTreeNode node = createLeafNode(examples, features, null, firstOpLabel);
			node.debug = "#ONLYPOS" + debug;
			return node;
		}
		
		// Determine the majority output label at this decision tree node
		String majorityLabel = null;
		if(secondOpLabelCnt >= firstOpLabelCnt) {
			majorityLabel = secondOpLabel;
		}
		else {
			majorityLabel = firstOpLabel;
		}

		// If features is empty, the return the majority value of examples
		if(features == null || features.isEmpty()) {
			DecisionTreeNode node = createLeafNode(examples, null, null, majorityLabel);
			node.debug = "#FEATEMPTY" + debug;
			return node;
		}

		Feature bestFeature = findBestFeature(examples, features, outputLabels);
		List<Feature> remFeatures = getRemainingFeatures(features, bestFeature);
		DecisionTreeNode root = new DecisionTreeNode(examples, features, bestFeature, majorityLabel);
		root.debug = "#NORMAL" + debug;

		// Create the first feature value subtree on left
		List<Example> firstOpLabelExamples = filterExamplesByFeature(examples, bestFeature, bestFeature.firstValue);
		root.firstValueNode = buildDecisionTree(firstOpLabelExamples, remFeatures, outputLabels, majorityLabel);
		
		// Create the second feature value subtree on right
		List<Example> secondLabelOpExamples = filterExamplesByFeature(examples, bestFeature, bestFeature.secondValue);
		root.secondValueNode = buildDecisionTree(secondLabelOpExamples, remFeatures, outputLabels, majorityLabel);
		
		return root;
	}

	/*
	 * Prints the decision tree visually. If debug mode enabled, prints statistics about each node.
	 */
	private static void printDecisionTree(DecisionTreeNode root, String prefix, boolean isDebugMode)
	{
		if(root == null) {
			return;
		}
		
		boolean isFeatureNode = (root.nodeType == DecisionTreeNodeType.FEATURE_NODE) ? true : false;
		// Leaf nodes
		if(!isFeatureNode) {
			System.out.println(prefix + root.majorityValue.toUpperCase() + (isDebugMode == true ? root.debug : ""));
		}
		// Recurse for feature nodes
		else {
			Feature feature = root.feature;
			
			// Print the left subtree
			System.out.println(prefix + feature.name + " = " + feature.firstValue + ":" + (isDebugMode == true ? root.debug : ""));
			printDecisionTree(root.firstValueNode, prefix + "\t", isDebugMode);
			
			// Print the right subtree
			System.out.println(prefix + feature.name + " = " + feature.secondValue + ":" + (isDebugMode == true ? root.debug : ""));
			printDecisionTree(root.secondValueNode, prefix + "\t", isDebugMode);
		}
	}

	/*
	 * Tests the decision tree against data sets and reports accuracy of the results
	 */
	private static void testDecisionTree(DecisionTreeNode dtree, List<Example> examples, String nameOfDataSet)
	{
		int totalExamples = examples.size();
		int totalFailures = 0;
		List<Example> failedExamples = new ArrayList<Example>();
		for(Example ex : examples) {
			String expectedLabel = ex.outputLabel;
			String generatedLabel = findDtreeLabelForExample(ex, dtree);
			if(!generatedLabel.equals(expectedLabel)) {
				++totalFailures;
				failedExamples.add(ex);
			}
		}
		
		if(!failedExamples.isEmpty()) {
			System.out.print("List of failed examples : [ ");
			for(Example ex : failedExamples) {
				System.out.print(ex.name + " ");
			}
			System.out.print(" ] ");
		}
		int totalSuccess = totalExamples - totalFailures;
		double accuracy = ((double)(totalSuccess)*100)/totalExamples;
		DecimalFormat df = new DecimalFormat("##.##");
		System.out.println("\nAccuracy for " + nameOfDataSet + " data set : " + df.format(accuracy) + " % :(" +  
		                    totalSuccess + "/" + totalExamples + ")");
	}

	/*
	 * Generates the output label for the example using the decision tree
	 */
	private static String findDtreeLabelForExample(Example ex, DecisionTreeNode dtree)
	{
		String opLabel = null;
		Map<Feature, String> featureValMap = ex.featureValMap;
		while(dtree != null && dtree.nodeType != DecisionTreeNodeType.OUTPUT_LEAF_NODE) {
			Feature currFeature = dtree.feature;
			String currFeatureVal = featureValMap.get(currFeature);
			testStr = testStr + "#" + currFeature.name;

			// first feature values are on left side of tree
			if(currFeature.firstValue.equals(currFeatureVal)) {
				dtree = dtree.firstValueNode;
			}
			// second feature values are on right side of tree
			else {
				dtree = dtree.secondValueNode;
			}
		}
		
		if(dtree != null) {
			opLabel = dtree.majorityValue;
			testStr = "";
		}

		return opLabel;
	}

	/*
	 * Filters and returns all the examples with the specified value for the feature
	 */
	private static List<Example> filterExamplesByFeature(List<Example> examples, Feature feature, String featureVal)
	{
		List<Example> filteredExamples = new ArrayList<Example>();
		for(Example ex : examples) {
			// Apply the filter of a specific feature's value
			if(feature != null) {
				Map<Feature, String> featureValMap = ex.featureValMap;
				if(featureValMap.get(feature).equals(featureVal)) {
					filteredExamples.add(ex);
				}
			}
		}

		return filteredExamples;
	}

	// Generates the new feature list containing all features except best feature for current node
	private static List<Feature> getRemainingFeatures(List<Feature> features, Feature bestFeature)
	{
		List<Feature> remainingFeatures = new ArrayList<Feature>();
		for(Feature f : features) {
			if(!f.equals(bestFeature)) {
				remainingFeatures.add(f);
			}
		}
		return remainingFeatures;
	}

	// Creates a leaf node for decision tree
	private static DecisionTreeNode createLeafNode(
			List<Example> examples, List<Feature> remFeatures, Feature currFeature, String majorVal)
	{
		DecisionTreeNode leafNode = new DecisionTreeNode(examples, remFeatures, currFeature, majorVal);
		leafNode.nodeType = DecisionTreeNodeType.OUTPUT_LEAF_NODE;

		return leafNode;
	}
	
	/*
	 * Finds the best feature to partition the decision tree. The idea is to choose a feature which leads to max
	 * information gain. In case of a tie among features choose the one which comes earlier in the lexical order.
	 * 
	 * Gain(A) = I(p/p+n, n/p+n) - Remainder(A)
	 * Remainder(A) = Sum p(i) + n(i)/(p+n) I(p(i)/(p(i) + n(i)) , n(i)/(p(i) + n(i))
	 * 
	 * Calculate Remainder(A) for each feature and choose the feature which has the minimum value of Remainder(A).
	 */
	private static Feature findBestFeature(List<Example> examples, List<Feature> features, List<String> opLabels)
	{
		Feature bestFeature = null;
		double minRemainingInfo = Integer.MAX_VALUE;
		
		String firstOpLabel = opLabels.get(0);
		String secondOpLabel = opLabels.get(1);

		int totalExamples = examples.size();
		for(Feature f : features) {
			Map<String, Integer> featureValMap = getFeatureValuesDistribution(examples, f);
			int firstValueCnt = featureValMap.get(f.firstValue);
			int secondValueCnt = featureValMap.get(f.secondValue);

			Map<String, Integer> opLabelMapFirstVal = getOutputValuesDistribution(examples, opLabels, f, f.firstValue);
			int firstValPosLabelCnt = opLabelMapFirstVal.get(firstOpLabel);
			int firstValNegLabelCnt = opLabelMapFirstVal.get(secondOpLabel);
			
			Map<String, Integer> opLabelMapSecondVal = getOutputValuesDistribution(examples, opLabels, f, f.secondValue);
			int secondValPosLabelCnt = opLabelMapSecondVal.get(firstOpLabel);
			int secondValNegLabelCnt = opLabelMapSecondVal.get(secondOpLabel);
			
			double firstValInfoGain = getInfoGain(firstValueCnt, totalExamples, firstValPosLabelCnt, firstValNegLabelCnt);
			double secondValInfoGain = getInfoGain(secondValueCnt, totalExamples, secondValPosLabelCnt, secondValNegLabelCnt);
			
			double infoGain = firstValInfoGain + secondValInfoGain;
			
			int diff = Double.compare(infoGain, minRemainingInfo);
			if(diff < 0) {
				minRemainingInfo = infoGain;
				bestFeature = f;
			}
			else if(diff == 0) {
				// If same info gain, choose one which comes earlier in lexical ordering
				if(f.name.compareTo(bestFeature.name) < 0) {
					bestFeature = f;
				}
			}
		}

		return bestFeature;
	}

	// Returns the information gained by choosing a particular feature value node
	private static double getInfoGain(double featureValCnt, double totalExamples, double posCnt, double negCnt)
	{
		if(featureValCnt == 0.0 || (posCnt + negCnt) == 0.0) {
			return 0.0;
		}

		double posInfoGain = 0.0;
		double negInfoGain = 0.0;
		if(posCnt != 0) {
			posInfoGain = (-1*(posCnt/featureValCnt)*(Math.log(posCnt/featureValCnt)/Math.log(2)));
		}
		if(negCnt != 0) {
			negInfoGain = (-1*(negCnt/featureValCnt)*(Math.log(negCnt/featureValCnt)/Math.log(2)));
		}

		return (featureValCnt/totalExamples)*(posInfoGain + negInfoGain);
	}

	/*
	 * Gets the distribution of a feature's values in the sets of examples
	 */
	private static Map<String, Integer> getFeatureValuesDistribution(List<Example> examples, Feature feature)
	{
		Map<String, Integer> classifierMap = new HashMap<String, Integer>();
		
		classifierMap.put(feature.firstValue, 0);
		classifierMap.put(feature.secondValue, 0);
		
		for(Example e : examples) {
			String featureVal = e.featureValMap.get(feature);
			int count = classifierMap.get(featureVal) + 1;
			classifierMap.put(featureVal, count);
		}

		return classifierMap;
	}

	/*
	 *  Returns the number of examples classified by each output label in the input set of examples.
	 *  Additionally, a filter like a particular feature's value can be specified to find distribution
	 *  of output labels in a filtered set.
	 */
	public static Map<String, Integer> getOutputValuesDistribution(
			List<Example> examples, List<String> opLabels, Feature feature, String featureValFilter)
	{
		Map<String, Integer> classifierMap = new HashMap<String, Integer>();
		classifierMap.put(opLabels.get(0), 0);
		classifierMap.put(opLabels.get(1), 0);

		for(Example ex : examples) {
			// Apply the filter of a specific feature's value
			if(feature != null) {
				Map<Feature, String> featureValMap = ex.featureValMap;
				if(!featureValMap.get(feature).equals(featureValFilter)) {
					continue;
				}
			}

			String opLabel = ex.outputLabel;
			int count = 1;
			if(classifierMap.containsKey(opLabel)) {
				count = classifierMap.get(opLabel) + 1;
			}
			classifierMap.put(opLabel, count);
		}

		return classifierMap;
	}

	/**
	 *	Reads data set from a file and populates the various entities into ListOfExamples object. 
	 */
	public static ListOfExamples readDataSetFromFile(String fileName, String nameOfDataSet)
	{
		List<Feature> features = new ArrayList<Feature>();
		Integer featureCount = null;
		
		List<Example> examples = new ArrayList<Example>();
		Integer exampleCount = null;

		List<String> opLabels = new ArrayList<String>();
		
		Scanner fileScanner = null;
		try {
			fileScanner = new Scanner(new File(fileName));
		} catch (Exception e) {
			System.err.println("Unable to read the file " + fileName + ". Exception : " + e.getMessage());
			System.exit(1);
		}
		while (fileScanner.hasNext()) {
			String currLine = fileScanner.nextLine().trim();
			DataSetLineType lineType = getLineType(currLine);
			
			// Skip for blank or empty lines
			if(lineType.equals(DataSetLineType.INSIGNIFICANT)) {
				continue;
			}
			
			// Populate feature count and example count values
			if(lineType.equals(DataSetLineType.COUNT)) {
				if(featureCount == null) {
					featureCount = Integer.parseInt(currLine);
				}
				else {
					exampleCount = Integer.parseInt(currLine);
				}
			}
			// Populate feature values
			else if(lineType.equals(DataSetLineType.FEATURE_VALUES)) {
				features.add(extractFeature(currLine));
			}
			// Populate output labels
			else if(lineType.equals(DataSetLineType.OUTPUT_LABELS)) {
				opLabels.add(currLine);
			}
			// Populate example values
			else {
				examples.add(extractExample(currLine));
			}
			
		}
		
		// Pre-process examples to allow easier access of feature values
		for(Example e : examples) {
			List<String> featureValues = e.featureValues;
			Map<Feature, String> featureValMap = new HashMap<Feature, String>();
			for(int i=0; i < features.size(); i++) {
				featureValMap.put(features.get(i), featureValues.get(i));
			}
			
			e.featureValMap = featureValMap;
		}
		
		return new ListOfExamples(nameOfDataSet, features, featureCount, opLabels, examples, exampleCount);
	}

	/**
	 * Returns the line type of the current line in the dataset. 
	 */
	private static DataSetLineType getLineType(String line)
	{
		DataSetLineType lineType = null;
		if(line.isEmpty() || line.startsWith(COMMENT)) {
			lineType = DataSetLineType.INSIGNIFICANT;
		}
		else if(line.contains(DASH)) {
			lineType = DataSetLineType.FEATURE_VALUES;
		}
		else if(line.matches(DIGITS)) {
			lineType = DataSetLineType.COUNT;
		}
		else {
			String[] words = line.split(SPACES);
			if(words.length == 1) {
				lineType = DataSetLineType.OUTPUT_LABELS;
			}
			else {
				lineType = DataSetLineType.EXAMPLE_VALUES;	
			}
		}
		
		return lineType;
	}

	// Extracts a feature object from dataset line
	private static Feature extractFeature(String line)
	{
		int lastDashIndex = line.lastIndexOf(DASH);
		String beforeFeatureVal = line.substring(0, lastDashIndex);
		String afterFeatureVal = line.substring(lastDashIndex+1);
		
		String featureName = beforeFeatureVal.trim();
		String[] featureValues = afterFeatureVal.trim().split(" ");
		String firstValue = featureValues[0].trim();
		String secondValue = featureValues[1].trim();

		return new Feature(featureName, firstValue, secondValue);
	}

	// Extracts an example object from a dataset line
	private static Example extractExample(String line)
	{
		String[] words = line.split(SPACES);
		
		String exampleName = words[0].trim();
		String outputLabel = words[1].trim();
		
		List<String> featureValues = new ArrayList<String>();
		for(int i=2; i < words.length; i++) {
			featureValues.add(words[i].trim());
		}
		
		return new Example(exampleName, outputLabel, featureValues);
	}

	/**
	 * This class represents a binary feature in the dataset. 
	 */
	public static class Feature 
	{
		private String name;
		private String firstValue;
		private String secondValue;
		
		public Feature(String name, String firstVal, String secondVal)
		{
			this.name = name;
			this.firstValue = firstVal;
			this.secondValue = secondVal;
		}
		
		@Override
		public String toString() {
			return "Feature [name=" + name + ", firstValue=" + firstValue + ", secondValue=" + secondValue + "]";
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((firstValue == null) ? 0 : firstValue.hashCode());
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result
					+ ((secondValue == null) ? 0 : secondValue.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Feature other = (Feature) obj;
			if (firstValue == null) {
				if (other.firstValue != null)
					return false;
			} else if (!firstValue.equals(other.firstValue))
				return false;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			if (secondValue == null) {
				if (other.secondValue != null)
					return false;
			} else if (!secondValue.equals(other.secondValue))
				return false;
			return true;
		}
	}

	/**
	 * This class represents an example in the dataset consisting of example name, list of feature values 
	 * and the output label.
	 */
	public static class Example 
	{
		private String name;
		private String outputLabel;
		private List<String> featureValues;
		private Map<Feature, String> featureValMap = null;
		
		public Example(String name, String outputLabel, List<String> featureValues) 
		{
			this.name = name;
			this.outputLabel = outputLabel;
			this.featureValues = featureValues;
		}

		@Override
		public String toString() {
			return "Example [\nname=" + name + ", \noutputLabel=" + outputLabel
					+ ", \nfeatureValues=" + Arrays.toString(featureValues.toArray()) + "]";
		}
	}

	/**
	 * This class represents a data set and contains a list of examples.
	 */
	public static class ListOfExamples
	{
		private String nameOfDataset = "";
		
		// Information about features in a dataset
		private int numFeatures = -1;
		private List<Feature> features = null;
		
		// Information about examples in a dataset
		private int numExamples = -1;
		private List<Example> examples = null;
		
		private List<String> outputLabels = null;

		public ListOfExamples(String name, List<Feature> features, int numFeatures, List<String> opLabels, 
				List<Example> examples, int numExamples)
		{
			this.nameOfDataset = name;
			this.features = features;
			this.numFeatures = numFeatures;
			this.outputLabels = opLabels;
			this.examples = examples;
			this.numExamples = numExamples;
		}

		@Override
		public String toString() {
			return "ListOfExamples [\nnameOfDataset=" + nameOfDataset
					+ ", \nnumFeatures=" + numFeatures + ", \nfeatures=" + Arrays.toString(features.toArray())
					+ ", \nnumExamples=" + numExamples + ", \nexamples=" + Arrays.toString(examples.toArray())
					+ ", \noutputLabels=" + Arrays.toString(outputLabels.toArray()) + "]";
		}

		
	}

}
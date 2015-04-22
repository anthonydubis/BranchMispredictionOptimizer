
import java.io.*;
import java.util.*;

public class BPOptimizer {
    // Variables that hold for each array of selectivities being optimized
    private int r;
    private int t;
    private int l;
    private int m;
    private int a;
    private int f;
    
    /*
     * This function is called for each line of selectivities read from the file.
     * It calls other functions to determine and then output the plan and cost.
     */
    private void findOptimalPlan(Double[] pValues) {
        int[] S = getBasicTerms(pValues);
        Record[] A = createSubsetsOfTerms(S, pValues);
        
        considerLogicalAndNoBranchingPlans(A);
        considerBranchingAndPlans(A);
        outputPlan(pValues,A);
    }
    
    /*
     * This function outputs the optimized plan and its cost given the selectivities to standard out
     */
    private void outputPlan(Double[] pValues,Record[] A){
        System.out.println("============================================================================");
        for(int i=0;i<pValues.length;i++)
            System.out.print(pValues[i]+" ");
        System.out.println();
        System.out.println("----------------------------------------------------------------------------");
        Record last = A[A.length-1];
        String result = "if (" + produceOptimalPlan(last);
        result = handleNoBranchingCase(result);
        System.out.println(result);
        System.out.println("----------------------------------------------------------------------------");
        System.out.println("cost: " + last.c);
    }
    
    /*
     * In the event that all terms are optimized to a no-branching plan, remove the surrounding if-statement
     */
    private String handleNoBranchingCase(String result) {
        if (result.indexOf("()") < 0) return result;
        int start = result.indexOf("{");
        int end = result.indexOf("}");
        return result.substring(start+1, end);
    }
    
    /*
     * This recursive function assembles the c-code for a particlar plan.
     * It is initially called by "outputPlan" with the last Record of A[],
     * which can be thought of as the parent node of the binary tree.
     */
    private String produceOptimalPlan(Record record) {
        if (record == null) return "";
        
        // No children
        if (record.L == null && record.R == null) {
            if(record.b){
                return ") {\n    answer[j] = i;" +
                    "\n    j+= "+ andTermForRecord(record)+";\n}";
            }
            else{
                return andTermForRecord(record)+ "){" +
                    "\n    answer[j++] = i;\n}";
            }
        }
        
        String result = andTermForRecord(record.L);
        if(record.R.L == null && record.R.R == null){
            //last term - treat it differently
            result += produceOptimalPlan(record.R);
        }
        else
            result += " && " + produceOptimalPlan(record.R);
        
        return result;
    }
    
    /*
     * This creates the &-term for a given Record and its terms
     */
    private String andTermForRecord(Record record) {
        if (record == null) return "NULL left point";
        String result = "";
        if (record.n > 1)
            result = "(";
        
        for (int i = 0; i < record.n; i++) {
            Integer func = record.terms.get(i) + 1;
            if (i == 0)
                result = result + snippetTermForFunctionNumber(func);
            else
                result = result + " & " + snippetTermForFunctionNumber(func);
        }
        
        if (record.n > 1) 
            result = result + ")";
        
        return result;
    }
    
    /*
     * This returns the surround snippet for a given function number (tf[of[i]])
     */
    private String snippetTermForFunctionNumber(int i) {
        return "t" + i + "[o" + i + "[i]]";
    }
    
    /*
     * Return the integer array S from 1 to k
     */
    private int[] getBasicTerms(Double[] pValues) {
        int[] S = new int[pValues.length];
        for (int i = 0; i < pValues.length; i++)
            S[i] = i;
        return S;
    }
    
    /*
     * Create the subsets of terms in S
     * Subsets are created in the specified "increasing" order
     */
    private Record[] createSubsetsOfTerms(int[] S, Double[] pValues) {
        // Create the subsets array
        int N = S.length;
        Record[] subsets = new Record[(int)Math.pow(2,N)];
        
        // Add all subsets of S as Records to the array
        int mask = (1 << N);
        for (int i = 0; i < mask; i++) {
            ArrayList<Integer> terms = new ArrayList<Integer>();
            ArrayList<Double> selectivities = new ArrayList<Double>();
            for (int j = 0; j < N; j++) {
                if ((i & (1 << j)) > 0) {
                    //The j-th element is used
                    terms.add(j);
                    selectivities.add(pValues[j]);
                }
            }
            subsets[i] = new Record(terms, selectivities);
        }
        return subsets;
    }
    
    /*
     * Stage 1 of the algorithm
     * Generate all 2^k-1 plans using only &-terms and No-Branch algorithm
     */
    private void considerLogicalAndNoBranchingPlans(Record[] subsets) {
        for (int i = 1; i < subsets.length; i++) {
            Record subset = subsets[i];
            double logicalAndCost = computeLogicalAndCost(subset.selectivities);
            double noBranchCost = computeNoBranchCost(subset.n);
            if (logicalAndCost < noBranchCost) {
                subset.c = logicalAndCost;
            } else {
                subset.c = noBranchCost;
                subset.b = true;
            }
        }
    }
    
    /*
     * Stage 2 of the Algorithm - consider branching and plans
     */
    private void considerBranchingAndPlans(Record[] subsets) {
        for (int i = 1; i < subsets.length; i++) {
            for (int j = 1; j < subsets.length; j++) {
                if ((i & j) == 0) { // Empty intersection
                    Record right = subsets[i];
                    Record left = subsets[j];
                    if (isLeftCMetricDominatedByRight(left, right)) {
                        // System.out.println("c-metric condition executed - do nothing.");
                    } else if (left.p <= 0.5 && isLeftDMetricDominatedBySomeAndTermInRight(left, right)) {
                        // System.out.println("d-metric condition executed - do nothing.");
                    } else {
                        // Optimal plan for subset
                        double cost = computeCostOfCombinedPlan(left, right);
                        Record combinedPlan = subsets[i + j];
                        if (cost < combinedPlan.c) {
                            combinedPlan.c = cost;
                            combinedPlan.L = left;
                            combinedPlan.R = right;
                        }
                    }
                }
            }
        }
    }
    
    /*
     * Stage 2 - First condition based on c-metric
     */
    private boolean isLeftCMetricDominatedByRight(Record left, Record right) {
        if (right.L != null) {
            right = right.L;
        }
        
        double leftFCost = getFCost(left.n);
        double rightFCost = getFCost(right.n);
        double p1 = left.p;
        double p2 = right.p;
     
        // Test if right c-metric dominates left c-metric
        if ((p2 <= p1) 
                && (((p2 - 1) / rightFCost) < ((p1 - 1) / leftFCost)))
            return true;
        else 
            return false;
    }
    
    /*
     * Stage 2 - Second condition based on d-metric
     */
    private boolean isLeftDMetricDominatedBySomeAndTermInRight(Record left, Record right) {
        boolean isDominated = false;
        double p1 = left.p;
        double leftFCost = getFCost(left.n);

        while (right.R != null) {
            right = right.R;
            Record andTerm = right.L;
            if (andTerm == null) 
                continue;
            
            double p2 = andTerm.p;
            double rightFCost = getFCost(andTerm.n);
            
            // Check if d-metric in right &-term dominates d-metric of left
            if ((p2 < p1) && (rightFCost < leftFCost)) {
                isDominated = true;
                break;
            }
        }
        return isDominated;
    }
    
    /*
     * Stage 3 - Compute cost of combined plan in third condition
     */
    private double computeCostOfCombinedPlan(Record left, Record right) {
        double cost = getFCost(left.n);
        double q = Math.min(left.p, 1 - left.p);
        cost += (m * q);
        cost += (left.p * right.c);
        return cost;
    }
    
    /************************ Read in the Property Values ********************/
    
    /*
     * Read the cost values from the config.txt file into our instance
     * variables
     */
    private void getCostValues(String filename) throws IOException {
        Properties prop = new Properties();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filename);
        if (inputStream != null) {
            prop.load(inputStream);
        } else {
            throw new FileNotFoundException("property file '" + filename + "' not found in the classpath");
        }
        
        r = Integer.parseInt(prop.getProperty("r"));
        t = Integer.parseInt(prop.getProperty("t"));
        l = Integer.parseInt(prop.getProperty("l"));
        m = Integer.parseInt(prop.getProperty("m"));
        a = Integer.parseInt(prop.getProperty("a"));
        f = Integer.parseInt(prop.getProperty("f"));
    }
    
    /*********************** Computing Cost of Plans ***************************/
    /*
     * Following Example 4.4
     */
    private double computeNoBranchCost(int k) {
        return k * r + (k-1) * l + k * f + a;
    }
    
    /*
     * Following Example 4.5
     */
    private double computeLogicalAndCost(ArrayList<Double> pValues){
        int k = pValues.size();
        
        double pprod = 1.0;
        for (Double d : pValues)
            pprod *= d;
        
        double q = pprod <= 0.5 ? (pprod) : (1-pprod);
        
        return k * r + (k-1) * l + k * f + t + m * q + pprod * a;
    }
    
    /*
     * Computing fcost by Definition 4.7
     */
    private double getFCost(int k) {
        return k * r + (k - 1) * l + k * f + t; 
    }

    /*
     * Kick-off point for the program
     * Argument 0 is the query.txt file
     * Argument 1 is the config.txt file
     */
    public static void main(String[] args) throws IOException {
        BPOptimizer optimizer = new BPOptimizer();
        optimizer.getCostValues(args[1]);
        
        // Find the optimal plan for each line of selecitivities in file given as the first argument
        try {
            BufferedReader br = new BufferedReader(new FileReader(args[0]));
            String line;
            // For each line of selectivities
            while ((line = br.readLine()) != null) {
                // Parse the selectivities
                String[] splitLine = line.split(" ");
                Double[] pValues = new Double[splitLine.length];
                for (int i = 0; i < splitLine.length; i++) {
                    pValues[i] = Double.parseDouble(splitLine[i]);
                }
                // Have the optimizer find the optimal plan
                optimizer.findOptimalPlan(pValues);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } 
    }
}

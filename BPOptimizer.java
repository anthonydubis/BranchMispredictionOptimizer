
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
     * Find the optimal plan for given values of p (for function f1 through fn)
     * Output C-snippet
     */
    private void findOptimalPlan(Double[] pValues) {
        int[] S = getBasicTerms(pValues);
        Record[] A = createSubsetsOfTerms(S, pValues);
        
        considerLogicalAndNoBranchingPlans(A);
        considerBranchingAndPlans(A);
        produceOptimalPlan(A);
    }
    
    /*
     * Produce the optimal plan as c-code
     */
    private void produceOptimalPlan(Record[] A) {
        Record last = A[A.length-1];
        String plan = produceOptimalPlan(last);
        System.out.println(plan);
    }
    
    private String produceOptimalPlan(Record record) {
        if (record == null) return "";
        
        // No children
        if (record.L == null && record.R == null) {
            return andTermForRecord(record);
        }
        
        String result = "(" + andTermForRecord(record.L) + " && " + produceOptimalPlan(record.R) + ")";
        return result;
    }
    
    private String andTermForRecord(Record record) {
        if (record == null) return "NULL left point";
        String result = "";
        if (record.n > 1)
            result = "(";
        
        for (int i = 0; i < record.n; i++) {
            Integer func = record.terms.get(i);
            if (i == 0)
                result = result + func;
            else
                result = result + " & " + func;
        }
        
        if (record.n > 1) 
            result = result + ")";
        
        return result;
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
     * Stage 2 of the Algorithm
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
                        System.out.println("d-metric condition executed - do nothing.");
                    } else {
                        // System.out.println("Found a new optimal plan");
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
// WARNING: I assume that if right has no left child we should use right itself (as it would be the left-most &-term)
        if (right.L != null) {
            right = right.L;
        }
        double leftFCost = getFCost(left.n);
        double rightFCost = getFCost(right.n);
        double p1 = left.p;
        double p2 = right.p;
     
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
        // System.out.println("Testing second condition");
        boolean isDominated = false;
        double p1 = left.p;
        double leftFCost = getFCost(left.n);
// Warning - should we also compare to the same and term compared in the cMetric condition?
        while (right.R != null) {
            right = right.R;
            Record andTerm = right.L;
            if (andTerm == null) 
                continue;
            
            double p2 = andTerm.p;
            double rightFCost = getFCost(andTerm.n);
            
            // System.out.println("Right d-metric (" + p2 + ", " + rightFCost + ") and left d-metric (" + p1 + ", " + leftFCost + ")");
            
            // Is this the right condition? 
            if ((p2 < p1) && (rightFCost < leftFCost)) {
                isDominated = true;
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
        cost += (left.p * computeBranchingAndCost(right.selectivities));
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
     * Following Example 4.6
     */
    private double computeBranchingAndCost(ArrayList<Double> pValues){
        int k = pValues.size();
        return computeBranchingAndCost(0, k-1, pValues);
    }
    
    /*
     * Following recursive formula for Example 4.6
     */
    private double computeBranchingAndCost(int n, int k, ArrayList<Double> pValues) {
        if (n == k) return a;
        
        double q = pValues.get(n) <= 0.5 ? (pValues.get(n)) : (1-pValues.get(n));
        return r + t + f + (m * q) + (pValues.get(n) * computeBranchingAndCost(n+1, k, pValues));
    }
    
    /*
     * Fcost
     */
    private double getFCost(int k) {
        return k * r + (k - 1) * l + k * f + t; 
    }

    public static void main(String[] args) throws IOException {
        BPOptimizer optimizer = new BPOptimizer();
        optimizer.getCostValues(args[1]);
        
        // Find the optimal plan for each line of selecitivities in file given as the first argument
        try {
            BufferedReader br = new BufferedReader(new FileReader(args[0]));
            String line;
            while ((line = br.readLine()) != null) {
                String[] splitLine = line.split(" ");
                Double[] pValues = new Double[splitLine.length];
                for (int i = 0; i < splitLine.length; i++) {
                    pValues[i] = Double.parseDouble(splitLine[i]);
                }
                optimizer.findOptimalPlan(pValues);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } 
    }
}

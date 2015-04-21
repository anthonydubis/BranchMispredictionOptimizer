
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
            System.out.println("LogicalAndCost: " + logicalAndCost + " NoBranchCost: " + noBranchCost);
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
                if ((i & j) == 0) {
                    // No intersection
                }
            }
        }
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
    private double computeBranchingAndCost(Double[] pValues){
        int k = pValues.length;
        return computeBranchingAndCost(0, k-1, pValues);
    }
    
    /*
     * Following recursive formula for Example 4.6
     */
    private double computeBranchingAndCost(int n, int k, Double[] pValues) {
        if (n == k) return a;
        
        double q = pValues[n] <= 0.5 ? (pValues[n]) : (1-pValues[n]);
        return r + t + f + (m * q) + (pValues[n] * computeBranchingAndCost(n+1, k, pValues));
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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


public class Optimizer {

	private int r;
    private int t;
    private int l;
    private int m;
    private int a;
    private int f;
    /*
    private void findOptimalPlan(Double[] pValues) {
        int[] S = getBasicTerms(pValues);
        Record[] A = createSubsetsOfTerms(S, pValues);
        
        considerLogicalAndNoBranchingPlans(A);
        considerBranchingAndPlans(A);
        outputPlan(pValues,A);
    }
    
    private void outputPlan(Double[] pValues,Record[] A){
    	System.out.println("==================================================================");
    	for(int i=0;i<pValues.length;i++)
    		System.out.print(pValues[i]+" ");
  
    	System.out.println("\n------------------------------------------------------------------");
    	Record last = A[A.length-1];
    	String result = "if(" + produceOptimalPlan(last);
    	System.out.println(result);
    	System.out.println("------------------------------------------------------------------");
    	System.out.println(last.c);
    	System.out.println("==================================================================");
    }
    
    
    private String produceOptimalPlan(Record record) {
        if (record == null) return "";
        
        // No children
        if (record.L == null && record.R == null) {
        	if(record.b){
        		return "){\n \t answer[j] = i;" +
        				"\n \t j+= "+ andTermForRecord(record)+"\n}";
        	}
        	else{
        		return andTermForRecord(record)+ "){" +
        				"\n \t answer[j++] = i;\n}";
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
    */

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
    
    
    /*
     * Find the optimal plan for given values of p (for function f1 through fn)
     * Output C-snippet
     */
    private void findOptimalPlan(Double[] pValues) {
        System.out.println(Arrays.toString(pValues));
        
        Map<String,Plan> A = new HashMap<String,Plan>();
       
        for(int i=0;i<pValues.length-1;i++){
        	Double[] values = new Double[2];
        	values[0] = pValues[i];
        	values[1] = pValues[i+1];
        	Plan plan = new Plan(values);
        	double cost = computeLogicalAndCost(values);
        	double nobranchCost = computeNoBranchCost(values.length);
        	if(nobranchCost < cost){
        		plan.b = true;
        		cost = nobranchCost;
        	}
        	plan.setCost(cost);
        	A.put(Integer.toString(i)+Integer.toString(i+1), plan);
        }
        
        for(int i=0;i<pValues.length-1;i++){
        	
        }
    }
    
    private float computeNoBranchCost(int k){
    	return k * r + (k-1) * l + k * f + a;
    }
    
    private double computeLogicalAndCost(Double[] pValues){
    	double pprod = 1f;
    	int k=pValues.length;
    	
    	for(int i=0;i<k;i++)
    		pprod *= pValues[i];
    	
    	double q = pprod<=0.5 ? pprod:(1-pprod);
    	
    	
    	return k * r + (k-1) * l + k * f + t + m * q + pprod * a;
    }
    
    private double[] computeBranchingAndCost(Double[] pValues){
    	int k=pValues.length;
    	double[] costs = new double[k];
    	
    	for(int i=k;i>=0;i--){
    		double q = pValues[i]<=0.5 ? pValues[i]:(1-pValues[i]);
    		
    		double cnext = a;
    		
    		if(i < k){
    			cnext = costs[i+1];
    		}
    		
    		costs[i] = r + t + f + m * q + pValues[i] * cnext;
    	}
    	
    	return costs;
    }
    
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Optimizer optimizer = new Optimizer();
		
		//read cost values
        try {
			optimizer.getCostValues(args[1]);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
        // Read selectivities
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

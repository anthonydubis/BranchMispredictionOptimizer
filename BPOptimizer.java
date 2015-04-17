
import java.io.*;
import java.util.*;

public class BPOptimizer {
    private int r;
    private int t;
    private int l;
    private int m;
    private int a;
    private int f;
    
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
    
    private void findOptimalPlan(Double[] pValues) {
        System.out.println(Arrays.toString(pValues));
    }
    
    public static void main(String[] args) throws IOException {
        BPOptimizer optimizer = new BPOptimizer();
        optimizer.getCostValues(args[1]);
        
        // Read the selectivities
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

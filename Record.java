
import java.util.ArrayList;

public class Record {
    ArrayList<Integer> terms;
    ArrayList<Double> selectivities;
    int n;
    int index;
    double p, c;
    boolean b = false;
    Record R = null, L = null;
    
    public Record(ArrayList<Integer> terms, ArrayList<Double> selectivities) {
        this.terms = terms;
        this.n = terms.size();
        this.selectivities = selectivities; 
        setPForSelectivities(selectivities);
    }
    
    public String toString() {
        return "Set: " + terms + " Cost: = " + c + " with no-branching algorithm = " + b;
    }
    
    private void setPForSelectivities(ArrayList<Double> selectivities) {
        p = 1;
        for (Double d : selectivities) {
            p *= d;
        }
    }
}

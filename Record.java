
import java.util.ArrayList;

/*
 * Record objects fill the elements in A[] as described by the algorithm
 */
public class Record {
    ArrayList<Integer> terms;          // The terms (1, 2, 3...) this Record contains
    ArrayList<Double> selectivities;   // The selectivities for the terms in respective order
    int n;                             // The number of terms
    double p, c;                       // p = product of selectivities, c = current cost of plan for this subset
    boolean b = false;                 // Whether no-branch plan was optimal over logical & plan
    Record R = null, L = null;         // Pointers to left and right children
    
    public Record(ArrayList<Integer> terms, ArrayList<Double> selectivities) {
        this.terms = terms;
        this.n = terms.size();
        this.selectivities = selectivities; 
        setPForSelectivities(selectivities);
    }
    
    public String toString() {
        return "Set: " + terms + " Cost: = " + c + " with no-branching algorithm = " + b + " Left " + L + " Right " + R;
    }
    
    private void setPForSelectivities(ArrayList<Double> selectivities) {
        p = 1;
        for (Double d : selectivities) {
            p *= d;
        }
    }
}

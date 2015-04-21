
import java.util.ArrayList;

public class Record {
    ArrayList<Integer> terms;
    ArrayList<Double> selectivities;
    int n;
    int index;
    double p, c;
    boolean b = false;
    Plan R = null, L = null;
    
    public Record(ArrayList<Integer> terms, ArrayList<Double> selectivities) {
        this.terms = terms;
        this.n = terms.size();
        this.selectivities = selectivities; 
        setPForSelectivities(selectivities);
    }
    
    public String toString() {
        return "Set: " + terms + " Cost: = " + c + " with no-branching algorithm = " + b;
    }
    
    public void setCost(double c) {
        this.c = c;
    }
    
    public double getCost() {
        return this.c;
    }
    
    public void setRightChild(Plan R) {
        this.R = R;
    }
    
    public Plan getRightChild() {
        return this.R;
    }
    
    public void setLeftChild(Plan L) {
        this.L = L;
    }
    
    public Plan getLeftChild() {
        return this.L;
    }
    
    private void setPForSelectivities(ArrayList<Double> selectivities) {
        p = 1;
        for (Double d : selectivities) {
            p *= d;
        }
    }
}

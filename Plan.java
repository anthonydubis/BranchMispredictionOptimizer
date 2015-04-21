
public class Plan {
 Double[] terms;
 int n;
 double p,c;
 boolean b = false;
 
 Plan R = null,L = null;
 
 public Plan(Double[] pValues){
  this.terms = pValues;
  this.n = terms.length;
 }
 
 public void setCost(double c){
  this.c = c;
 }
 
 public double getCost(){
  return this.c;
 }
 
 public void setRightChild(Plan R){
  this.R = R;
 }
 
 public Plan getRightChild(){
  return this.R;
 }
 
 public void setLeftChild(Plan L){
  this.L = L;
 }
 
 public Plan getLeftChild(){
  return this.L;
 }
}

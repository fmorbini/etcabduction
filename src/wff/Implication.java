package wff;

import java.util.List;

public class Implication extends WFF {

	private WFF antecedent=null;
	private Predication consequent=null;
	public Implication(Object antecedent, Object consequent) throws Exception {
		if (antecedent!=null && consequent!=null) {
			WFF a=WFF.create(antecedent);
			if (a!=null) this.antecedent=a;
			Predication c=(Predication) WFF.create(consequent);
			if (c!=null) this.consequent=c;
		}
	}
	public List<Predication> getAntecedents() {
		if (antecedent!=null) return antecedent.getAllBasicConjuncts();
		else return null;
	}
	public Predication getConsequent() {
		return consequent;
	}
	
	@Override
	public String toString() {
		if (antecedent!=null && consequent!=null) {
			return "(if "+antecedent+" "+consequent+")";
		}
		return null;
	}
}

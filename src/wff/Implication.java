package wff;

import java.util.ArrayList;
import java.util.List;

public class Implication extends WFF {

	private WFF antecedent=null;
	private Predication consequent=null;
	public static Implication create(Object antecedent, Object consequent) throws Exception {
		if (antecedent!=null && consequent!=null) {
			WFF a=WFF.create(antecedent);
			Predication c=(Predication) WFF.create(consequent);
			return new Implication(a,c);
		}
		return null;
	}
	public Implication(WFF antecedent, Predication consequent) throws Exception {
		this.antecedent=antecedent;
		this.consequent=consequent;
	}
	public List<Predication> getAntecedents() {
		if (antecedent!=null) return antecedent.getAllBasicConjuncts();
		else return null;
	}
	public Predication getConsequent() {
		return consequent;
	}
	public WFF getAntecedent() {
		return antecedent;
	}
	
	@Override
	public String toString() {
		if (antecedent!=null && consequent!=null) {
			return "(if "+antecedent+" "+consequent+")";
		}
		return null;
	}
	
	@Override
	public List<? extends UnifiableFormulaElement> getArguments() {
		List<UnifiableFormulaElement> ret=null;
		if (antecedent!=null) {
			if (ret==null) ret=new ArrayList<>();
			ret.add(antecedent);
		}
		if (consequent!=null) {
			if (ret==null) ret=new ArrayList<>();
			ret.add(consequent);
		}
		return ret;
	}
}

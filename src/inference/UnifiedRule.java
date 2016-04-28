package inference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import unify.Unify;
import wff.Implication;
import wff.Predication;
import wff.Term;
import wff.Variable;

class UnifiedRule {
	private Implication rule=null;
	private Map<Variable,Term> unifier=null;
	public UnifiedRule(Implication rule,Map<Variable,Term> theta) {
		this.rule=rule;
		this.unifier=theta;
	}
	public List<Predication> getAntecedents() {
		List<Predication> ants = null;
		if (rule!=null) ants=rule.getAntecedents();
		if (ants!=null && unifier!=null) {
			List<Predication> nAnts=ants;
			boolean changed=false;
			int i=0;
			for(Predication a:ants) {
				Predication na = (Predication) Unify.subst(a, unifier);
				if (na!=a) {
					if (!changed) {
						nAnts=new ArrayList<>();
						if (i>0) nAnts.addAll(ants.subList(0, i));
						changed=true;
					}
					nAnts.add(na);
				} else if (changed) {
					nAnts.add(a);
				}
				i++;
			}
			return nAnts;
		}
		return ants;
	}
	public Map<Variable,Term> getUnifier() {
		return unifier;
	}
	@Override
	public String toString() {
		return unifier+": "+rule;
	}
}
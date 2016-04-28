package inference;

import java.util.ArrayList;
import java.util.List;

import edu.usc.ict.nl.util.graph.Edge;

public class AbductionStep extends Edge {
	
	private List<UnifiedRule> rules=null;
	
	@Override
	public AbductionNode getTarget() {
		return (AbductionNode)super.getTarget();
	}
	@Override
	public AbductionNode getSource() {
		return (AbductionNode)super.getSource();
	}

	public void addRule(UnifiedRule r) {
		if (r!=null) {
			if (rules==null) rules=new ArrayList<>();
			rules.add(r);
		}
	}
	public List<UnifiedRule> getRules() {
		return rules;
	}
	public void setRules(List<UnifiedRule> rules) {
		this.rules = rules;
	}
	
	@Override
	public String toString() {
		if (getRules()!=null) {
			StringBuffer ret=new StringBuffer();
			for(UnifiedRule r:getRules()) {
				ret.append(r.toString()+"\n");
			}
			return ret.toString();
		}
		return null;
	}
}

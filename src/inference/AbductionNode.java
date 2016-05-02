package inference;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.usc.ict.nl.util.graph.Edge;
import edu.usc.ict.nl.util.graph.Node;
import unify.Unify;
import wff.Predication;
import wff.Term;
import wff.Variable;

public class AbductionNode extends Node {
	
	private int depth;
	private Predication[] antecedents=null,assumptions=null;
	private boolean hasOverlap=false;
	private Set<String> predicatesInIt=null;
	
	public AbductionNode(List<Predication> ants, List<Predication> ass) {
		this();
		this.assumptions=null;
		addAssumptions(ass);
		addAntecedents(ants);
	}
	public AbductionNode() {
		depth=0;
	}
	public void addAssumptions(List<Predication> ass) {
		if (ass!=null && !ass.isEmpty()) {
			int i=0;
			if (assumptions==null) assumptions=new Predication[ass.size()];
			else {
				i=assumptions.length;
				assumptions=Arrays.copyOf(assumptions, assumptions.length+ass.size());
			}
			for(Predication a:ass) {
				assumptions[i++]=a;
				storePredicate(a.getName());
			}
		}
	}
	public void addAssumptions(Predication[] ass) {
		if (ass!=null && ass.length>0) {
			int i=0;
			if (assumptions==null) assumptions=new Predication[ass.length];
			else {
				i=assumptions.length;
				assumptions=Arrays.copyOf(assumptions, assumptions.length+ass.length);
			}
			for(Predication a:ass) {
				assumptions[i++]=a;
				storePredicate(a.getName());
			}
		}
	}
	public void addAntecedents(List<Predication> ants) {
		if (ants!=null && !ants.isEmpty()) {
			int i=0;
			if (antecedents==null) antecedents=new Predication[ants.size()];
			else {
				i=antecedents.length;
				antecedents=Arrays.copyOf(antecedents, antecedents.length+ants.size());
			}
			for(Predication a:ants) {
				antecedents[i++]=a;
				storePredicate(a.getName());
			}
		}
	}
	public Predication[] getAssumptions() {
		return assumptions;
	}
	public Predication[] getAntecedents() {
		return antecedents;
	}
	
	private void storePredicate(String p) {
		if (p!=null) {
			if (predicatesInIt==null) predicatesInIt=new HashSet<>();
			if (predicatesInIt.contains(p)) hasOverlap=true;
			else predicatesInIt.add(p);
		}
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		AbductionNode ret=new AbductionNode();
		if (this.antecedents!=null) ret.antecedents=Arrays.copyOf(this.antecedents, this.antecedents.length);
		if (this.assumptions!=null) ret.assumptions=Arrays.copyOf(this.assumptions, this.assumptions.length);
		return ret;
	}
	
	public void applyUnifier(Map<Variable, Term> unifier) {
		if (unifier!=null) {
			if (antecedents!=null) {
				int l=antecedents.length;
				for(int i=0;i<l;i++) {
					Predication np=(Predication) Unify.subst(antecedents[i], unifier);
					antecedents[i]=np;
				}
			}
			if (assumptions!=null) {
				int l=assumptions.length;
				for(int i=0;i<l;i++) {
					Predication np=(Predication) Unify.subst(assumptions[i], unifier);
					assumptions[i]=np;
				}
			}
		}
	}
	
	@Override
	public String toString() {
		StringBuffer ret=new StringBuffer();
		if (getAntecedents()!=null) {
			ret.append("antecedents: \n");
			for(Predication p:getAntecedents()) ret.append("  "+p+"\n");
		}
		if (getAssumptions()!=null) {
			ret.append("assumptions: \n");
			for(Predication p:getAssumptions()) ret.append("  "+p+"\n");
		}
		return ret.toString();
	}
	
	@Override
	public boolean addEdge(Edge e, boolean noCycles, boolean stopIfCycle) throws Exception {
		boolean r=super.addEdge(e, noCycles, stopIfCycle);
		AbductionNode target = (AbductionNode) e.getTarget();
		if (target!=this && target!=null) target.setDepth(getDepth()+1);
		return r;
	}

	public int getDepth() {
		return depth;
	}
	public void setDepth(int depth) {
		this.depth = depth;
	}
	
	public boolean getHasOverlap() {return hasOverlap;}
}
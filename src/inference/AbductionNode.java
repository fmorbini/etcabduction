package inference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.usc.ict.nl.util.graph.Edge;
import edu.usc.ict.nl.util.graph.Node;
import wff.Predication;

public class AbductionNode extends Node {
	
	private int depth;
	private List<Predication> antecedents=null,assumptions=null;
	private boolean hasOverlap=false;
	private Set<String> predicatesInIt=null;
	private Signature signature=null;
	
	public AbductionNode(List<Predication> ants, List<Predication> ass) throws Exception {
		this();
		addAssumptions(ass);
		addAntecedents(ants);
	}
	public Signature getSignature() {
		return signature;
	}
	public AbductionNode() {
		depth=0;
		this.assumptions=null;
		this.antecedents=null;
		this.signature=new Signature();
	}

	public void addAssumptions(List<Predication> ass) throws Exception {
		if (ass!=null && !ass.isEmpty()) {
			List<Predication> toBeAdded=filterToAdd(ass);
			if (toBeAdded!=null && !toBeAdded.isEmpty()) {
				if (assumptions==null) assumptions=new ArrayList<>();
				assumptions.addAll(toBeAdded);
				for(Predication a:toBeAdded) storePredicate(a);
			}
		}
	}
	private List<Predication> filterToAdd(Collection<Predication> input) throws Exception {
		Signature s = getSignature();
		List<Predication> toBeAdded=null;
		for(Predication a:input) {
			if (s.addToSignature(a)) {
				if (toBeAdded==null) toBeAdded=new ArrayList<>(input.size());
				toBeAdded.add(a);
			}
		}
		return toBeAdded;
	}
	public void addAntecedents(List<Predication> ants) throws Exception {
		List<Predication> toBeAdded=filterToAdd(ants);
		if (toBeAdded!=null && !toBeAdded.isEmpty()) {
			if (antecedents==null) antecedents=new ArrayList<>();
			antecedents.addAll(toBeAdded);
			for(Predication a:toBeAdded) storePredicate(a);
		}
	}
	public List<Predication> getAssumptions() {
		return assumptions;
	}
	public List<Predication> getAntecedents() {
		return antecedents;
	}
	
	private void storePredicate(Predication p) {
		if (p!=null) {
			String name=p.getName();
			if (predicatesInIt==null) predicatesInIt=new HashSet<>();
			if (predicatesInIt.contains(name)) hasOverlap=true;
			else predicatesInIt.add(name);
		}
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		AbductionNode ret=new AbductionNode();
		if (this.antecedents!=null) ret.antecedents=new ArrayList<>(this.antecedents);
		if (this.assumptions!=null) ret.assumptions=new ArrayList<>(this.assumptions);
		ret.hasOverlap=this.hasOverlap;
		ret.depth=this.depth;
		ret.signature=this.signature.clone();
		ret.predicatesInIt=new HashSet<>(this.predicatesInIt);
		return ret;
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
	
	public Map<String, Set<Predication>> getUnifiableSets() throws Exception {
		Map<String, Set<Predication>> ret=null;
		if (getHasOverlap()) {
			List<Predication> ants = getAntecedents();
			ret=addToSets(ants,ret);
			List<Predication> ass = getAssumptions();
			ret=addToSets(ass,ret);
			if (ret!=null) {
				Iterator<String> it=ret.keySet().iterator();
				while(it.hasNext()) {
					String s=it.next();
					Set<Predication> ps=ret.get(s);
					if (ps==null || ps.size()<2) it.remove();
				}
			}
		}
		return ret;
	}
	private Map<String, Set<Predication>> addToSets(List<Predication> toAdd, Map<String, Set<Predication>> ret) throws Exception {
		if (toAdd!=null) {
			for(Predication p:toAdd) {
				String name=p.getPredicate();
				int c=p.getArgCount();
				String s=name+"_"+c;
				if (ret==null) ret=new HashMap<>();
				Set<Predication> ss=ret.get(s);
				if (ss==null) ret.put(s, ss=new HashSet<>());
				ss.add(p);
			}
		}
		return ret;
	}
	
	public boolean allEtcs() {
		boolean ret=true;
		List<Predication> ps=getAntecedents();
		if(ps!=null) for(Predication p:ps) if (!p.getIsEtc()) return false;
		ps=getAssumptions();
		if(ps!=null) for(Predication p:ps) if (!p.getIsEtc()) return false;
		return ret;
	}

}
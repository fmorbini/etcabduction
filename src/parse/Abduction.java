package parse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.usc.ict.nl.util.graph.Node;
import wff.Implication;
import wff.Predication;
import wff.Term;
import wff.UnifiableFormulaElement;
import wff.Variable;
import wff.WFF;

public class Abduction {
	
	private IndexedKB iKb=null;
	private int depth;
	private List<Predication> obs;
	private boolean skolemize;
	private AbductionNode root=null;
	
	public Abduction(List<Predication> obs,List<Implication> kb,int depth) {
		this(obs, kb, depth,true);
	}
	public Abduction(List<Predication> obs,List<Implication> kb,int depth,boolean skolemize) {
		this.iKb = new IndexedKB(kb);
		this.depth=depth;
		this.obs=obs;
		this.skolemize=skolemize;
	}
	
	public AbductionNode getInitialNode() {
		if (root==null) {
			root=new AbductionNode((List<Predication>) this.obs, null);
		}
		return root;
	}
	
	public class AbductionNode extends Node {
		Predication[] antecedents=null,assumptions=null;
		int depth;
		public AbductionNode(List<Predication> ants, List<Predication> ass) {
			this.assumptions=null;
			if (ass!=null && !ass.isEmpty()) {
				int i=0;
				assumptions=new Predication[ass.size()];
				for(Predication a:ass) assumptions[i++]=a;
			}
			this.antecedents=null;
			if (ants!=null && !ants.isEmpty()) {
				int i=0;
				antecedents=new Predication[ants.size()];
				for(Predication a:ants) antecedents[i++]=a;
			}
		}
		public AbductionNode() {
		}
		public void addAssumptions(Predication[] ass) {
			if (ass!=null && ass.length>0) {
				int i=0;
				if (assumptions==null) assumptions=new Predication[ass.length];
				else {
					i=assumptions.length;
					assumptions=Arrays.copyOf(assumptions, assumptions.length+ass.length);
				}
				for(Predication a:ass) assumptions[i++]=a;
			}
		}
		public Predication[] getAssumptions() {
			return assumptions;
		}
		public Predication[] getAntecedents() {
			return antecedents;
		}
		public void addAntecedents(List<Predication> ants) {
			if (ants!=null && !ants.isEmpty()) {
				int i=0;
				if (antecedents==null) antecedents=new Predication[ants.size()];
				else {
					i=antecedents.length;
					antecedents=Arrays.copyOf(antecedents, antecedents.length+ants.size());
				}
				for(Predication a:ants) antecedents[i++]=a;
			}
		}
		@Override
		protected Object clone() throws CloneNotSupportedException {
			AbductionNode ret=new AbductionNode();
			if (this.antecedents!=null) ret.antecedents=Arrays.copyOf(this.antecedents, this.antecedents.length);
			if (this.assumptions!=null) ret.assumptions=Arrays.copyOf(this.assumptions, this.assumptions.length);
			return ret;
		}
		
		public void applyUnifier(Map<Variable, Term> unifier) throws Exception {
			if (antecedents!=null && assumptions!=null && unifier!=null) {
				int l=antecedents.length;
				for(int i=0;i<l;i++) {
					Predication np=(Predication) Unify.subst(antecedents[i], unifier);
					antecedents[i]=np;
				}
				l=assumptions.length;
				for(int i=0;i<l;i++) {
					Predication np=(Predication) Unify.subst(assumptions[i], unifier);
					assumptions[i]=np;
				}
			}
		}
	}
	
	private class UnifiedRule {
		Implication rule=null;
		Map<Variable,Term> unifier=null;
		public UnifiedRule(Implication rule,Map<Variable,Term> theta) {
			this.rule=rule;
			this.unifier=theta;
		}
		public List<Predication> getAntecedents() throws Exception {
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
							if (i>0) {
								nAnts=new ArrayList<>();
								nAnts.addAll(ants.subList(0, i));
							}
							changed=true;
						}
						nAnts.add(na);
					} else if (changed) {
						nAnts.add(a);
					}
					i++;
				}
			}
			return ants;
		}
		public Map<Variable,Term> getUnifier() {
			return unifier;
		}
	}
	
	/**
	 * input set of literals l1,...,ln and kb
	 * find set of possible inferences that can be made 
	 * a1,a2->l1
	 * a3->l1
	 * a5->n
	 * assume all without inferences {l2,...,ln-1}
	 * compute combinations of the ones with inferences: {a1,a2,a5}+ass,{a3,a5}+ass
	 * for each possible set call recursively and decrement depth 
	 * @return
	 * @throws Exception 
	 */
	public List<AbductionNode> doAbductionStep(AbductionNode n) throws Exception {
		//for each item in literals, find out if it can be derived from inference or if it must be assumed.
		Predication[] literals=n.getAntecedents();
		List<Predication> assumptions=null;
		Map<Predication,List<UnifiedRule>> options=null;
		for(Predication l:literals) {
			String p=l.getPredicate();
			List<Implication> rules = iKb.get(p);
			if (rules==null || rules.isEmpty()) {
				if (assumptions==null) assumptions=new ArrayList<>();
				assumptions.add(l);
			} else {
				List<UnifiedRule> uRules=null;
				for(Implication r:rules) {
					Map<Variable, Term> theta = Unify.unify(l, r.getConsequent());
					if (theta!=null) {
						if (uRules==null) uRules=new ArrayList<>();
						uRules.add(new UnifiedRule(r, theta));
					}
				}
				if (uRules!=null) {
					if (options==null) options=new HashMap<>();
					options.put(l,uRules);
				}
			}
		}
		// compute combinations of the ones with inferences.
		List<AbductionNode> combinations=null;
		for(Predication l:options.keySet()) {
			List<UnifiedRule> rules=options.get(l);
			if (rules!=null && !rules.isEmpty()) {
				if (combinations==null || combinations.isEmpty()) {
					if (combinations==null) combinations=new ArrayList<>();
					for(UnifiedRule r:rules) {
						List<Predication> aas=r.getAntecedents();
						AbductionNode nn=new AbductionNode(aas,assumptions);
						nn.addAssumptions(n.getAssumptions());
						n.addEdgeTo(nn, false, false);
						combinations.add(nn);
						nn.applyUnifier(r.getUnifier());
					}
				} else {
					List<AbductionNode> next=null;
					List<AbductionNode> current=combinations;
					int i=1;
					int len=rules.size();
					for(UnifiedRule r:rules) {
						if (i<len) {
							next=makeCopy(current);
							combinations.addAll(next);
						}
						for(AbductionNode literalsOptions:current) {
							if (i>1) n.addEdgeTo(literalsOptions, false, false);
							literalsOptions.addAntecedents(r.getAntecedents());
						}
						current=next;
						i++;
					}
				}
			}
		}
		return combinations;
	}

	public static List<AbductionNode> makeCopy(List<AbductionNode> current) {
		List<AbductionNode> copyOfCombinations=new ArrayList<>();
		for(AbductionNode c:current) {
			try {
				AbductionNode cc = (AbductionNode) c.clone();
				copyOfCombinations.add(cc);
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
		}
		return copyOfCombinations;
	}
	
	public static void main(String[] args) throws Exception {
		List<WFF> content = Parse.parse(Parse.kb);
		List<WFF> obs=Parse.parse("(and (creepUpOn' E1 C BT) (flinch' E2 BT) (seq E1 E2))");
		if (obs!=null && !obs.isEmpty() && obs.size()==1) {
			Abduction a = new Abduction((List)obs.get(0).getAllBasicConjuncts(), (List)content, 10, true);
			List<AbductionNode> rs = a.doAbductionStep(a.getInitialNode());
			a.getInitialNode().toGDLGraph("test.gdl");
		}
		System.out.println(content);
	}
}

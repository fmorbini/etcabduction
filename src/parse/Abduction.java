package parse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.usc.ict.nl.util.graph.Node;
import wff.Implication;
import wff.Predication;
import wff.WFF;

public class Abduction {
	
	private IndexedKB iKb=null;
	private int depth;
	private List obs;
	private boolean skolemize;
	
	public Abduction(List<WFF> obs,List<WFF> kb,int depth) {
		this(obs, kb, depth,true);
	}
	public Abduction(List<WFF> obs,List<WFF> kb,int depth,boolean skolemize) {
		this.iKb = new IndexedKB((List)kb);
		this.depth=depth;
		this.obs=obs;
		this.skolemize=skolemize;
		
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
			// TODO Auto-generated constructor stub
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
	public List<AbductionNode> doAbductionStep(AbductionNode n,int depth,boolean skolemize) throws Exception {
		//for each item in literals, find out if it can be derived from inference or if it must be assumed.
		Predication[] literals=n.getAntecedents();
		List<Predication> assumptions=null;
		Map<Predication,List<Implication>> options=null;
		for(Predication l:literals) {
			String p=l.getPredicate();
			List<Implication> rules = iKb.get(p);
			if (rules==null || rules.isEmpty()) {
				if (assumptions==null) assumptions=new ArrayList<>();
				assumptions.add(l);
			} else {
				if (options==null) options=new HashMap<>();
				options.put(l,rules);
			}
		}
		// compute combinations of the ones with inferences.
		List<AbductionNode> combinations=null;
		for(Predication l:options.keySet()) {
			List<Implication> rules=options.get(l);
			if (rules!=null && !rules.isEmpty()) {
				if (combinations==null || combinations.isEmpty()) {
					if (combinations==null) combinations=new ArrayList<>();
					for(Implication r:rules) {
						List<Predication> aas=r.getAntecedents();
						AbductionNode nn=new AbductionNode(aas,assumptions);
						nn.addAssumptions(n.getAssumptions());
						n.addEdgeTo(nn, false, false);
						combinations.add(nn);
					}
				} else {
					List<AbductionNode> next=null;
					List<AbductionNode> current=combinations;
					int i=1;
					int len=rules.size();
					for(Implication r:rules) {
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
		System.out.println(content);
	}
}

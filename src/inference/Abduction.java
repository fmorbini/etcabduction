package inference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.usc.ict.nl.util.graph.Edge;
import kb.IndexedKB;
import parse.Parse;
import unify.Unify;
import wff.Implication;
import wff.Predication;
import wff.Term;
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
	public List<AbductionNode> doAbductionStep(AbductionNode n) throws Exception  {
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
		List<AbductionStep> combinations=null;
		for(Predication l:options.keySet()) {
			List<UnifiedRule> rules=options.get(l);
			if (rules!=null && !rules.isEmpty()) {
				if (combinations==null || combinations.isEmpty()) {
					if (combinations==null) combinations=new ArrayList<>();
					for(UnifiedRule r:rules) {
						List<Predication> aas=r.getAntecedents();
						AbductionNode nn=new AbductionNode(aas,assumptions);
						nn.addAssumptions(n.getAssumptions());
						AbductionStep edge = new AbductionStep();
						edge.setTarget(nn);
						edge.setSource(n);
						edge.addRule(r);
						n.addEdge(edge, false, false);
						combinations.add(edge);
						//nn.applyUnifier(r.getUnifier());
					}
				} else {
					List<AbductionStep> next=null;
					List<AbductionStep> current=combinations;
					int i=1;
					int len=rules.size();
					for(UnifiedRule r:rules) {
						if (i<len) next=makeCopy(current);
						else next=null;
						for(AbductionStep literalsOptions:current) {
							literalsOptions.addRule(r);
							AbductionNode target = literalsOptions.getTarget();
							target.addAntecedents(r.getAntecedents());
							//target.applyUnifier(r.getUnifier());
						}
						current=next;
						if (next!=null) combinations.addAll(next);
						i++;
					}
				}
			}
		}
		List<AbductionNode> ret=null;
		if (combinations!=null && combinations.isEmpty()) {
			for(AbductionStep c:combinations) {
				AbductionNode an=c.getTarget();
				if (an!=null) {
					if (ret==null) ret=new ArrayList<>();
					ret.add(an);
				}
			}
		}
		return ret;
	}

	public static List<AbductionStep> makeCopy(List<AbductionStep> current) throws Exception {
		List<AbductionStep> copyOfCombinations=new ArrayList<>();
		for(AbductionStep e:current) {
			try {
				AbductionNode c=e.getTarget();
				AbductionNode s=e.getSource();
				AbductionNode cc = (AbductionNode) c.clone();
				AbductionStep ne=new AbductionStep();
				ne.setSource(s);
				ne.setTarget(cc);
				ne.setRules(new ArrayList<>(e.getRules()));
				s.addEdge(ne, false, false);
				copyOfCombinations.add(ne);
			} catch (CloneNotSupportedException ex) {
				ex.printStackTrace();
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

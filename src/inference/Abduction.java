package inference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import kb.IndexedKB;
import parse.Parse;
import unify.Unify;
import wff.Implication;
import wff.Predication;
import wff.Term;
import wff.Variable;
import wff.WFF;
import wff.lts.LTSConverter;
import wff.lts.link.LinkLTS;

public class Abduction {
	
	private IndexedKB iKb=null;
	private List<Predication> obs;
	private boolean skolemize;
	private AbductionNode root=null;
	protected List<AbductionNode> etcSolutions=null;
	protected Map<Signature,AbductionNode> encouteredNodes=null;
	
	public Abduction(List<Predication> obs,List<Implication> kb) {
		this(obs, kb,true);
	}
	public Abduction(List<Predication> obs,List<Implication> kb,boolean skolemize) {
		this.iKb = new IndexedKB(kb);
		this.obs=obs;
		this.skolemize=skolemize;
		this.etcSolutions=new ArrayList<>();
		this.encouteredNodes=new HashMap<>();
	}
	
	public AbductionNode getInitialNode() throws Exception {
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
	 * @param etcSolutions 
	 * @return
	 * @throws Exception 
	 */
	public List<AbductionNode> doAbductionStep(AbductionNode n) throws Exception  {
		//for each item in literals, find out if it can be derived from inference or if it must be assumed.
		List<Predication> literals=n.getAntecedents();
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
				boolean ruleFound=false;
				for(Implication r:rules) {
					Map<Variable, Term> theta = Unify.unify(l, r.getConsequent());
					if (theta!=null) {
						if (uRules==null) uRules=new ArrayList<>();
						ruleFound=true;
						uRules.add(new UnifiedRule(r, theta));
					}
				}
				if (!ruleFound) {
					if (assumptions==null) assumptions=new ArrayList<>();
					assumptions.add(l);
				}
				if (uRules!=null) {
					if (options==null) options=new HashMap<>();
					options.put(l,uRules);
				}
			}
		}
		// compute combinations of the ones with inferences.
		List<AbductionStep> combinations=null;
		if (options!=null) {
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
		}
		//compute list of new abduction nodes and filter for duplicates.
		List<AbductionNode> ret=null;
		if (combinations!=null && !combinations.isEmpty()) {
			synchronized (etcSolutions) {
				ret=filterEdges(combinations);
				if (ret!=null) for(AbductionNode an:ret) if (an.getIsEtc()) etcSolutions.add(an);
			}
		}
		return ret;
	}
	private List<AbductionNode> filterEdges(List<AbductionStep> edges) throws Exception {
		List<AbductionNode> ret=null;
		for(AbductionStep c:edges) {
			AbductionNode an=c.getTarget();
			if (an!=null) {
				Signature ans=an.getSignature();
				AbductionNode ean=encouteredNodes.get(ans);
				if (ean!=null) {
					c.getSource().removeThisOutgoingEdge(c);
					c.getSource().addEdgeTo(ean, false, false);
				} else {
					if (ret==null) ret=new ArrayList<>();
					encouteredNodes.put(ans, an);
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

	/**
	 * do unification step of solution nodes.
	 * @param sols
	 * @return
	 * @throws Exception
	 */
	private List<AbductionNode> doUnificationStep(List<AbductionNode> sols) throws Exception {
		List<AbductionStep> edges=null;
		int u=0,r=0,eq=0,req=0,un=0;
		/**
		 * find list of nodes to try to unify
		 *  find in each abduction node the set of literals that could be unified (same name ad number of arguments). 
		 */
		Map<Signature,Map<Variable,Term>> pairToUnif=null;
		Set<LinkLTS> ltss = new HashSet<>();
		if (sols!=null) {
			for(AbductionNode a:sols) {
				boolean succU=false;
				Set<LinkLTS> altss=Utils.getPredicates(a);
				ltss.addAll(altss);
				Map<String,Set<Predication>> us=a.getUnifiableSets();
				if (us!=null) {
					for (Set<Predication> ss:us.values()) {
						Choose<Predication> x=new Choose<Predication>(ss,2);
						while (x.hasNext()) {
							Predication[] comb = x.next();
							LinkLTS l1=LTSConverter.toLTS(comb[0],true);
							LinkLTS l2=LTSConverter.toLTS(comb[1],true);
							if (l1==l2) {
								eq++;
								if (comb[0].toString().equalsIgnoreCase(comb[1].toString())) req++;
								else {
									System.out.println(a);
								}
							}
							Signature s=new Signature();
							s.addToSignature(l1);
							s.addToSignature(l2);
							if (pairToUnif==null) pairToUnif=new HashMap<>();
							Map<Variable, Term> unif = null;
							if (pairToUnif.containsKey(s)) {
								unif=pairToUnif.get(s);
								r++;
							} else {
								u++;
								pairToUnif.put(s, unif=Unify.unify(comb[0], comb[1]));
							}
							if (unif!=null && !unif.isEmpty()) {
								succU=true;
								//apply unifier and create a new child node. return the set of newly created child nodes.
								Predication eqP=(Predication) Unify.subst(comb[0],unif);
								AbductionNode nn=a.clone();
								nn.removePredication(comb[0]);
								nn.removePredication(comb[1]);
								nn.addAntecedents(Arrays.asList(new Predication[]{eqP}));
								AbductionStep edge = new AbductionStep();
								edge.setTarget(nn);
								edge.setSource(a);
								a.addEdge(edge, false, false);
								if (edges==null) edges=new ArrayList<>();
								edges.add(edge);
							}
						}
					}
				}
				if (succU) un++;
			}
		}
		//System.out.println("lts "+ltss.size());
		//System.out.println("u "+u+" r "+r+" eq "+eq+" req "+req);
		List<AbductionNode> ret=null;
		if (edges!=null && !edges.isEmpty()) {
			ret=filterEdges(edges);
		}
		{
			int solss=sols!=null?sols.size():0;
			int rets=ret!=null?ret.size():0;
			System.out.println("input nodes: "+solss+" output new nodes: "+rets+" from "+un+" parent nodes. Total nodes after unification: "+(solss-un+rets));
		}
		return ret;
	}

	private void clear() {
		etcSolutions.clear();
		encouteredNodes.clear();
	}
	private List<AbductionNode> run(int levelsToCrunch) throws Exception {
		return run(getInitialNode(),levelsToCrunch); 
	}
	private List<AbductionNode> run(AbductionNode start,int levelsToCrunch) throws Exception {
		clear();
		long startTime=System.currentTimeMillis();
		Stack<AbductionNode> s=new Stack<>();
		s.push(start);
		List<AbductionNode> newLevel=null;
		while(!s.isEmpty()) {
			AbductionNode consider=s.pop();
			List<AbductionNode> rs = doAbductionStep(consider);
			if (rs!=null && !rs.isEmpty()) {
				if (newLevel==null) newLevel=rs;
				else newLevel.addAll(rs);
			}
			if (s.isEmpty()) {
				levelsToCrunch--;
				if (levelsToCrunch>0 && newLevel!=null && !newLevel.isEmpty()) {
					s.addAll(newLevel);
					System.out.println(newLevel.size());
					newLevel=null;
				} else {
					break;
				}
			}
		}
		long endTime=System.currentTimeMillis();
		System.out.println("runtime="+(endTime-startTime)+" ms");
		
		return newLevel;
	}
	
	public List<AbductionNode> getSolutions() {return etcSolutions;}

	private void addSolutions(List<AbductionNode> sols) {
		if (sols!=null) {
			synchronized (etcSolutions) {
				for(AbductionNode an:sols) if (an.getIsEtc()) etcSolutions.add(an);
			}
		}
	}

	public static void main(String[] args) throws Exception {
		List<WFF> content = Parse.parse(Parse.kb);
		List<WFF> obs=Parse.parse("(and (creepUpOn' E1 C BT) (flinch' E2 BT) (seq E1 E2))");
		if (obs!=null && !obs.isEmpty() && obs.size()==1) {
			Abduction a = new Abduction((List)obs.get(0).getAllBasicConjuncts(), (List)content, true);
			a.run(5);
			List<AbductionNode> sols = a.getSolutions();
			//List<AbductionNode> csols=Utils.findUnique(sols);
			List<AbductionNode> sss = Utils.getSimplifiableSolutions(sols);
			System.out.println((sols!=null?sols.size():0)
					//+" "+(csols!=null?csols.size():0)
					+" "+(sss!=null?sss.size():0));
			//Map<String,Set<LinkLTS>> uuu=Utils.findSetsOfUnifiableLiterals(csols);
			//a.doUnificationStep(sss);
			a.addSolutions(a.doUnificationStep(sss));
			List<AbductionNode> allSols=a.getSolutions();
			allSols.sort(new Comparator<AbductionNode>() {
				@Override
				public int compare(AbductionNode n1, AbductionNode n2) {
					return (int)Math.signum(n2.getProbability()-n1.getProbability());
				}
			});
			Iterator<AbductionNode> it = a.getSolutions().iterator();
			double pr=1;
			while(it.hasNext()) {
				AbductionNode n=it.next();
				if (pr>0 || n.getProbability()>=pr) {
					System.out.println(n);
					pr=n.getProbability();
					System.out.println(" "+n.getProbability());
				} else break;
			}
			//Utils.findSetsOfUnifiableLiterals(sss,true);
			//System.out.println(sss);
			//Utils.computeStats(csols);
			//Utils.computeStats(sols);
			//Utils.groupUniqueLTSs(sss);
			//a.runParallel(9);
			//a.killWorkers();
			//a.getInitialNode().toGDLGraph("test.gdl");
		}
		//System.out.println(content);
	}
}

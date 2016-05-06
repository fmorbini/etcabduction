package inference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentLinkedQueue;

import kb.IndexedKB;
import parse.Parse;
import unify.Unify;
import wff.Implication;
import wff.Predication;
import wff.Term;
import wff.Variable;
import wff.WFF;
import wff.lts.link.LinkLTS;

public class Abduction {
	
	private IndexedKB iKb=null;
	private List<Predication> obs;
	private boolean skolemize;
	private AbductionNode root=null;
	private ConcurrentLinkedQueue<AbductionNode> jobQueue=null;
	private List<AbductionWorker> workers=null;
	private List<AbductionNode> etcSolutions=null;
	
	public Abduction(List<Predication> obs,List<Implication> kb) {
		this(obs, kb,true,1);
	}
	public Abduction(List<Predication> obs,List<Implication> kb,boolean skolemize,int workers) {
		this.iKb = new IndexedKB(kb);
		this.obs=obs;
		this.skolemize=skolemize;
		this.jobQueue=new ConcurrentLinkedQueue<>();
		if (this.workers==null) this.workers=new ArrayList<>();
		this.workers.clear();
		this.etcSolutions=new ArrayList<>();
		for(int i=0;i<workers;i++) this.workers.add(new AbductionWorker(jobQueue, this,etcSolutions));
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
	 * @param etcSolutions 
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
		List<AbductionNode> ret=null;
		if (combinations!=null && !combinations.isEmpty()) {
			synchronized (etcSolutions) {
				for(AbductionStep c:combinations) {
					AbductionNode an=c.getTarget();
					if (an!=null) {
						if (allEtcs(an)) {
							etcSolutions.add(an);
						} else {
							if (ret==null) ret=new ArrayList<>();
							ret.add(an);
						}
					}
				}
			}
		}
		return ret;
	}

	private boolean allEtcs(AbductionNode an) {
		boolean ret=true;
		Predication[] ps=an.getAntecedents();
		if(ps!=null) for(Predication p:ps) if (!p.getIsEtc()) return false;
		ps=an.getAssumptions();
		if(ps!=null) for(Predication p:ps) if (!p.getIsEtc()) return false;
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
	
	private List<AbductionNode> run(int levelsToCrunch) throws Exception {
		return run(getInitialNode(),levelsToCrunch); 
	}
	private List<AbductionNode> run(AbductionNode start,int levelsToCrunch) throws Exception {
		etcSolutions.clear();
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
	private void runParallel(int levelsToCrunch) throws InterruptedException {
		runParallel(getInitialNode(), levelsToCrunch);
	}
	private void runParallel(AbductionNode start,int levelsToCrunch) throws InterruptedException {
		long startTime=System.currentTimeMillis();
		if (workers!=null && !workers.isEmpty()) {
			System.out.println("waiting for queue to be empty");
			while(!jobQueue.isEmpty()) {Thread.sleep(100);}
			for(AbductionWorker w:workers) {
				w.setMaxDepth(levelsToCrunch);
				w.start();
			}
			System.out.println("adding start node to queue");
			jobQueue.add(start);
			while(!jobQueue.isEmpty()) {Thread.sleep(100);}
		}
		long endTime=System.currentTimeMillis();
		System.out.println("runtime="+(endTime-startTime)+" ms");
	}

	private void killWorkers() {
		if (workers!=null) for(AbductionWorker w:workers) w.kill();
	}
	
	public List<AbductionNode> getSolutions() {return etcSolutions;}
	public List<AbductionNode> getSimplifiableSolutions() {
		List<AbductionNode> ret=null;
		List<AbductionNode> sols = getSolutions();
		if (sols!=null) {
			for(AbductionNode s:sols) {
				if (s.getHasOverlap()) {
					if (ret==null) ret=new ArrayList<>();
					ret.add(s);
				}
			}
		}
		return ret;
	}

	private static List<AbductionNode> compressSolutions(List<AbductionNode> sols) {
		// TODO Auto-generated method stub
		return null;
	}

	public static void main(String[] args) throws Exception {
		List<WFF> content = Parse.parse(Parse.kb);
		List<WFF> obs=Parse.parse("(and (creepUpOn' E1 C BT) (flinch' E2 BT) (seq E1 E2))");
		if (obs!=null && !obs.isEmpty() && obs.size()==1) {
			Abduction a = new Abduction((List)obs.get(0).getAllBasicConjuncts(), (List)content, true,8);
			a.run(5);
			List<AbductionNode> sols = a.getSolutions();
			List<AbductionNode> csols = compressSolutions(sols);
			List<AbductionNode> sss = a.getSimplifiableSolutions();
			System.out.println((sols!=null?sols.size():0)+" "+(sss!=null?sss.size():0));
			//System.out.println(sss);
			//Utils.computeStats(sss);
			List<AbductionNode> ds=Utils.findDuplicates(sss);
			//Utils.groupUniqueLTSs(sss);
			//a.runParallel(9);
			//a.killWorkers();
			//a.getInitialNode().toGDLGraph("test.gdl");
		}
		//System.out.println(content);
	}
}

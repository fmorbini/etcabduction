package inference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import wff.Predication;
import wff.lts.LTSConverter;
import wff.lts.link.LinkLTS;

public class Utils {
	/**
	 * computes sharing across abduction nodes. generates an histogram of number of nodes that share a certain number of literals.
	 * @param ans
	 */
	public static void computeStats(List<AbductionNode> ans) {
		Map<LinkLTS,Set<AbductionNode>> x=new HashMap<>();
		int maxAss=0,maxAnts=0,maxAll=0;
		if (ans!=null) {
			/**
			 * compute the largest number of antecendets and assumptions
			 * compute histogram of nodes that overlap n items with other nodes.
			 */
			for(AbductionNode an:ans) {
				Predication[] ants = an.getAntecedents();
				Predication[] ass = an.getAssumptions();
				int all=0;
				if (ants!=null) {
					all+=ants.length;
					if (ants.length>maxAnts) maxAnts=ants.length;
					for(Predication ant:ants) {
						try {
							LinkLTS aLts=LTSConverter.toLTS(ant);
							Set<AbductionNode> set = x.get(aLts);
							if (set==null) x.put(aLts, set=new HashSet<>());
							set.add(an);
						} catch (Exception e) {
							e.printStackTrace();
						} 
					}
				}
				if (ass!=null) {
					all+=ass.length;
					if (ass.length>maxAss) maxAss=ass.length;
					for(Predication ant:ass) {
						try {
							LinkLTS aLts=LTSConverter.toLTS(ant);
							Set<AbductionNode> set = x.get(aLts);
							if (set==null) x.put(aLts, set=new HashSet<>());
							set.add(an);
						} catch (Exception e) {
							e.printStackTrace();
						} 
					}
				}
				if (all>maxAll) maxAll=all;
			}
			System.out.println("antecedents: "+maxAnts);
			System.out.println("assumptions: "+maxAss);
			System.out.println("total: "+maxAll);
			System.out.println("unique LTS: "+x.keySet().size());
			Map<AbductionNode,Integer> shared=new HashMap<>();
			int[] h=new int[maxAll];
			for(LinkLTS l:x.keySet()) {
				Set<AbductionNode> s=x.get(l);
				if (s!=null && s.size()>1) {
					for(AbductionNode n:s) {
						Integer count=shared.get(n);
						if (count==null) shared.put(n, 0);
						else shared.put(n, count+1);
					}
				}
			}
			List<AbductionNode> zeroSharing=new ArrayList<>(ans);
			zeroSharing.removeAll(shared.keySet());
			h[0]=zeroSharing.size();
			for(int i=1;i<h.length;i++) h[i]=0;
			for(Integer i:shared.values()) {
				h[i]++;
			}
			System.out.println(Arrays.toString(h));
		}
	}
	public static Set<LinkLTS> uniqueLTS(List<AbductionNode> ans) {
		Set<LinkLTS> ret=null;
		for(AbductionNode an:ans) {
			Set<LinkLTS> ps = getPredicates(an);
			if (ps!=null) {
				if(ret==null) ret=new HashSet<>();
				ret.addAll(ps);
			}
		}
		return ret;
	}
	public static Set<LinkLTS> getPredicates(AbductionNode an) {
		Set<LinkLTS> ret=null;
		Predication[] ants = an.getAntecedents();
		Predication[] ass = an.getAssumptions();
		if (ants!=null) {
			for(Predication ant:ants) {
				try {
					LinkLTS aLts=LTSConverter.toLTS(ant);
					if (ret==null) ret=new HashSet<>();
					ret.add(aLts);
				} catch (Exception e) {
					e.printStackTrace();
				} 
			}
		}
		if (ass!=null) {
			for(Predication ant:ass) {
				try {
					LinkLTS aLts=LTSConverter.toLTS(ant);
					if (ret==null) ret=new HashSet<>();
					ret.add(aLts);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return ret;
	}
	public static Map<String,Set<LinkLTS>> groupByPredicate(Set<LinkLTS> ltss) {
		Map<String,Set<LinkLTS>> ret=null;
		if (ltss!=null) {
			for(LinkLTS l:ltss) {
				String p=l.getPredicate();
				if (p!=null) {
					if (ret==null) ret=new HashMap<>();
					Set<LinkLTS> ps=ret.get(p);
					if (ps==null) ret.put(p, ps=new HashSet<>());
					ps.add(l);
				}
			}
		}
		return ret;
	}
	/**
	 * prints the ordered list of unique predications. group by predicate name.
	 * @param ans
	 */
	public static void groupUniqueLTSsAndPrint(List<AbductionNode> ans) {
		Set<LinkLTS> ltss = Utils.uniqueLTS(ans);
		Map<String, Set<LinkLTS>> gs = Utils.groupByPredicate(ltss);
		List<String> names=new ArrayList<>(gs.keySet());
		Collections.sort(names,new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return gs.get(o2).size()-gs.get(o1).size();
			}
		});
		for(String n:names) {
			Set<LinkLTS> things=gs.get(n);
			System.out.println(n+"("+things.size()+"): "+things);
		}
	}
	public static List<AbductionNode> findDuplicates(List<AbductionNode> sss) {
		// TODO Auto-generated method stub
		return null;
	}
}

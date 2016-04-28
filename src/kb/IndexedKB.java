package kb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import wff.Implication;
import wff.Predication;

public class IndexedKB {
	
	Map<String,List<Implication>> iKb=null;
	
	public IndexedKB(List<Implication> rules) {
		if (rules!=null) {
			for(Implication r:rules) {
				if (r!=null) {
					Predication p=(Predication) r.getConsequent();
					String pred=p.getPredicate();
					if (pred!=null) {
						if (iKb==null) iKb=new HashMap<>();
						List<Implication> iRules=iKb.get(pred);
						if (iRules==null) iKb.put(pred, iRules=new ArrayList<>());
						iRules.add(r);
					}
				}
			}
		}
	}

	public List<Implication> get(String predName) {
		if (iKb!=null) {
			List<Implication> rules = iKb.get(predName);
			return rules;
		}
		return null;
	}
}

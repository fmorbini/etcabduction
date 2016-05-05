package wff;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public abstract class WFF implements UnifiableFormulaElement {
	public static WFF create(Object input) throws Exception {
		WFF ret=null;
		if (input!=null && input instanceof List && !((List)input).isEmpty()) {
			List thing=(List)input;
			Object f=thing.get(0);
			if (f!=null && f instanceof String) {
				int s=thing.size();
				if (((String)f).equalsIgnoreCase("if")) {
					if (s==3) {
						ret=Implication.create(thing.get(1),thing.get(2));
					} else {
						throw new Exception("Invalid number of arguments for implications: "+s+" "+input);
					}
				} else if (((String)f).equalsIgnoreCase("and")) {
					if (s>2) ret=Conjunction.create(thing.subList(1, thing.size()));
					else ret=WFF.create((List) thing.get(1));
				} else {
					ret=Predication.create((String)f,thing.subList(1, s));
				}
			} else throw new Exception("invalid first element: "+f);
		}
		return ret;
	}
		
	public List<Predication> getAllBasicConjuncts() {
		List<Predication> ret=null;
		if (this instanceof Conjunction) {
			Stack<WFF> s=new Stack<>();
			s.push(this);
			while(!s.isEmpty()) {
				WFF t=s.pop();
				if (t instanceof Conjunction) s.addAll(((Conjunction)t).getConjuncts());
				else {
					if (ret==null) ret=new ArrayList<>();
					ret.add((Predication) t);
				}
			}
		} else {
			ret=new ArrayList<>();
			ret.add((Predication) this);
		}
		return ret;
	}

}

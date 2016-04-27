package wff;

import java.util.ArrayList;
import java.util.List;

public class Conjunction extends WFF {

	private List<WFF> conjuncts=null;
	public Conjunction(List arguments) throws Exception {
		if (arguments!=null && !arguments.isEmpty()) {
			for(Object a:arguments) {
				if (a!=null) {
					WFF aWff=WFF.create((List)a);
					if (aWff!=null) {
						if (this.conjuncts==null) this.conjuncts=new ArrayList<>();
						this.conjuncts.add(aWff);
					}
				}
			}
		}
	}
	public List<WFF> getConjuncts() {
		return conjuncts;
	}
	
	@Override
	public String toString() {
		if (getConjuncts()!=null && !getConjuncts().isEmpty()) {
			StringBuffer ret=new StringBuffer();
			ret.append("(and");
			for(WFF c:getConjuncts()) {
				ret.append(" "+c);
			}
			ret.append(")");
			return ret.toString();
		}
		return null;
	}

}

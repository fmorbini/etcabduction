package wff;

import java.util.ArrayList;
import java.util.List;

public class Conjunction extends WFF {

	private List<WFF> conjuncts=null;
	public static Conjunction create(List arguments) throws Exception {
		Conjunction ret=new Conjunction();
		if (arguments!=null && !arguments.isEmpty()) {
			for(Object a:arguments) {
				if (a!=null) {
					WFF aWff=WFF.create((List)a);
					if (aWff!=null) {
						if (ret.conjuncts==null) ret.conjuncts=new ArrayList<>();
						ret.conjuncts.add(aWff);
					}
				}
			}
		}
		return ret;
	}
	public List<WFF> getConjuncts() {
		return getArguments();
	}
	@Override
	public List<WFF> getArguments() {
		return conjuncts;
	}
	
	public void setConjuncts(List<WFF> conjuncts) {
		this.conjuncts = conjuncts;
	}
	
	@Override
	public int getArgCount() {
		if (getArguments()!=null) return getArguments().size();
		else return 0;
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

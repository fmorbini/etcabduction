package wff;

import java.util.ArrayList;
import java.util.List;

public class Predication extends WFF {
	protected String pred=null;
	private List<Term> arguments=null;
	public static Predication create(String pred,List arguments) throws Exception {
		Predication ret=new Predication(pred);
		List<Term> as=null;
		if (arguments!=null && !arguments.isEmpty()) {
			for(Object a:arguments) {
				if (a!=null) {
					Term t=Term.create(a);
					if (t!=null) {
						if (as==null) as=new ArrayList<>();
						as.add(t);
					}
				}
			}
		}
		if (as!=null) ret.setArguments(as);
		return ret;
	}
	public Predication(String name) {
		this.pred=name;
	}
	public String getName() {
		return getPredicate();
	}
	public String getPredicate() {
		return pred;
	}
	
	@Override
	public String toString() {
		if (pred!=null) {
			if (getArguments()!=null && !getArguments().isEmpty()) {
				StringBuffer ret=new StringBuffer();
				ret.append("("+pred);
				for(UnifiableFormulaElement a:getArguments()) {
					ret.append(" "+a);
				}
				ret.append(")");
				return ret.toString();
			} else {
				return pred;
			}
		}
		return null;
	}
	public int getArgCount() {
		if (getArguments()!=null) return getArguments().size();
		else return 0;
	}
	
	@Override
	public List<Term> getArguments() {
		return arguments;
	}
	public void setArguments(List<Term> arguments) {
		this.arguments = arguments;
	}
}

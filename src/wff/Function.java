package wff;

import java.util.ArrayList;
import java.util.List;

public class Function extends Term {
	protected String pred=null;
	protected List<Term> arguments=null;
	public Function(String pred,List arguments) throws Exception {
		this.pred=pred;
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
		if (as!=null) this.arguments=as;
	}
	
	@Override
	public String toString() {
		if (pred!=null) {
			StringBuffer ret=new StringBuffer();
			if (arguments!=null && !arguments.isEmpty()) {
				ret.append("("+pred);
				for(Term a:arguments) {
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
}

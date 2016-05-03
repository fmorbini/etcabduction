package wff;

import java.util.ArrayList;
import java.util.List;

public class Function extends Term {
	protected String pred=null;
	private List<Term> arguments=null;
	public static Function create(String pred,List arguments) throws Exception {
		Function ret=new Function(pred);
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
	public Function(String name) {
		this.pred=name;
	}
	
	@Override
	public String toString() {
		if (pred!=null) {
			StringBuffer ret=new StringBuffer();
			if (getArguments()!=null && !getArguments().isEmpty()) {
				ret.append("("+pred);
				for(Term a:getArguments()) {
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

	@Override
	public List<Term> getArguments() {
		return arguments;
	}
	
	@Override
	public int getArgCount() {
		if (getArguments()!=null) return getArguments().size();
		else return 0;
	}

	public void setArguments(List<Term> arguments) {
		this.arguments = arguments;
	}

	public String getName() {
		return pred;
	}

}

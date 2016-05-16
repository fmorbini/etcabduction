package wff;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Predication extends WFF {
	protected String pred=null;
	private boolean isEtc=false;
	private double logP=1;
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
		if (name.startsWith("etc")) isEtc=true;
	}
	public String getName() {
		return getPredicate();
	}
	public String getPredicate() {
		return pred;
	}
	
	public boolean getIsEtc() {return isEtc;}
	
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
	@Override
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
	
	public Double getProbability() {
		if (isEtc) {
			NumericConstant arg = (NumericConstant) getArguments().get(0);
			if (logP>0) logP=Math.log(arg.getValue());
			return logP;
		}
		return null;
	}
}

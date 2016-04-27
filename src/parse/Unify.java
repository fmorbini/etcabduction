package parse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class Unify {
	public static boolean isVariable(Object v) {
		return (v!=null && v instanceof String && ((String)v).length()>1 && ((String)v).charAt(0)=='?');
	}
	
	public static Map<String,Object> robinson(Object x,Object y) {
		return robinson(x,y,null);
	}
	public static Map<String,Object> robinson(Object x,Object y,Map<String,Object> theta) {
		x=subst(x,theta);
		y=subst(y,theta);
		if (!x.equals(y)) {
			if (isVariable(x)) {
				if (isVariable(y)) {
					if (theta==null) theta=new HashMap<>();
					if (((String)x).compareTo((String)y)<0) {
						theta.put((String)x, y);
					} else {
						theta.put((String)y, x);
					}
				} else if (occurCheck((String) x,y,theta)) {
					theta.put((String)x, y);
				} else return null;
			} else if (isVariable(y) && occurCheck((String) y, x, theta)) {
				theta.put((String)y, x);
			} else if (y instanceof List && x instanceof List && ((List)x).size()==((List)y).size() && ((List)x).get(0).equals(((List)y).get(0))) {
				int l=((List)x).size();
				for(int i=1;i<l;i++) {
					theta=robinson(((List)x).get(i), ((List)y).get(i), theta);
					if (theta==null) return null;
				}
			} else return null;
		}
		return theta;
	}
	
	private static boolean occurCheck(String var, Object y, Map<String, Object> theta) {
		if (y!=null && var!=null) {
			Stack terms=new Stack<>();
			terms.add(y);
			while(!terms.isEmpty()) {
				Object thing=terms.pop();
				if (thing!=null) {
					if (thing instanceof List) {
						terms.addAll((List)y);
					} else if (thing instanceof String) {
						if (((String)thing).equals(var)) {
							return false;
						}
					} else System.err.println("unknown options.");
				}
			}
		}
		return true;
	}

	public static Object subst(Object thing,Map<String,Object> theta) {
		if (thing!=null && theta!=null) {
			if (thing instanceof List) {
				Object ret=thing;
				int l=((List) thing).size();
				boolean changed=false;
				for(int i=0;i<l;i++) {
					Object t=((List)thing).get(i);
					Object nt=subst(t,theta);
					if (nt!=t) {
						if (!changed) {
							ret=new ArrayList<>();
							if (i-1>0) ((List)ret).addAll(((List)thing).subList(0, i));
							changed=true;
						}
						((List)ret).add(nt);
					} else if (changed) {
						((List)ret).add(nt);
					}
				}
				return ret;
			} else if (thing instanceof String) {
				if (theta.containsKey(thing)) {
					return theta.get(thing);
				}
			}
		}
		return thing;
	}
		
	public static Map<String,Object> unify(Object x,Object y) {
		return robinson(x, y);
	}
	
	public static Set allVariables(Object sexp) {
		Set ret=null;
		Stack s=new Stack<>();
		s.push(sexp);
		while(!s.isEmpty()) {
			Object t=s.pop();
			if (t!=null) {
				if (t instanceof List) s.addAll((List)t);
				else if (isVariable(t)) {
					if (ret==null) ret=new HashSet<>();
					ret.add(t);
				}
			}
		}
		return ret;
	}
	
	public static Object skolem(Object sexp) {
		Set vars=allVariables(sexp);
		Map<String,Object> unif=null;
		if (vars!=null) {
			for(Object v:vars) {
				if (unif==null) unif=new HashMap<>();
				unif.put((String) v, Generator.skolemGenerator.next());
			}
		}
		if (unif!=null) return Unify.subst(sexp, unif);
		else return sexp;
	}
	
}
